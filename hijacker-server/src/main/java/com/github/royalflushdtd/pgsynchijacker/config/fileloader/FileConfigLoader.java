package com.github.royalflushdtd.pgsynchijacker.config.fileloader;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.royalflushdtd.pgsynchijacker.config.ConfigListener;
import com.github.royalflushdtd.pgsynchijacker.utils.NamedThreadFactory;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public abstract class FileConfigLoader implements ConfigLoader, AutoCloseable {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private File file;

    private Map<String, String> properties = new ConcurrentHashMap<>();
    private List<ConfigListener> listeners = new CopyOnWriteArrayList<>();
    private ExecutorService executor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), new NamedThreadFactory("FileWatcherThread"));
    private AtomicBoolean started = new AtomicBoolean(Boolean.FALSE);

    public FileConfigLoader(String fileName) {
        this.file = new File(fileName);
        this.properties.putAll(load(this.file));
        addFileWatcher(this.file);
    }

    protected abstract Map<String, String> load(File file);

    @Override
    public String getProperty(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    @Override
    public void addChangeListener(ConfigListener configListener) {
        if (!this.listeners.contains(configListener)) {
            this.listeners.add(configListener);
        }
    }

    @Override
    public void close() {
        this.started.compareAndSet(Boolean.TRUE, Boolean.FALSE);
        this.executor.shutdown();
    }

    private void addFileWatcher(File file) {
        FileWatchTask fileWatchTask = new FileWatchTask(file);

        this.started.compareAndSet(Boolean.FALSE, Boolean.TRUE);
        this.executor.submit(fileWatchTask);
    }

    private static class ThreeTuple<O1, O2, O3> {

        private O1 k1;
        private O2 v1;
        private O3 v2;

        private ThreeTuple(O1 k1, O2 v1, O3 v2) {
            this.k1 = k1;
            this.v1 = v1;
            this.v2 = v2;
        }

        private boolean valueEquals() {
            if (v1 == null) {
                return v2 == null;
            }
            return v1.equals(v2);
        }
    }

    private class FileWatchTask implements Runnable {

        private File file;

        private FileWatchTask(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                Path dir = Paths.get(this.file.getParent());
                dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (started.get()) {
                    try {
                        WatchKey key = watchService.poll(50, TimeUnit.MILLISECONDS);
                        if (key == null) {
                            continue;
                        }

                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            List<ThreeTuple<String, String, String>> data = new ArrayList<>();
                            Path path = (Path) event.context();
                            if (!path.toFile().getName().equals(this.file.getName())) {
                                continue;
                            }
                            if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                Map<String, String> newData = load(this.file);
                                mergeData(properties, newData, data);
                            }

                            onDataChange(data);
                        }
                        key.reset();

                    } catch (Throwable e) {
                        //
                    }
                }

            } catch (Throwable t) {
                //
            }
        }

        private synchronized void mergeData(Map<String, String> oldProp, Map<String, String> newProp, List<ThreeTuple<String, String, String>> data) {
            Map<String, ThreeTuple<String, String, String>> tmp = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : oldProp.entrySet()) {
                String key = e.getKey();
                String oldVal = e.getValue();
                String newVal = newProp.get(key);

                if (newVal == null) {
                    oldProp.remove(key);
                }

                tmp.put(key, new ThreeTuple<>(key, oldVal, newVal));
            }

            for (Map.Entry<String, String> e : newProp.entrySet()) {
                String key = e.getKey();
                String newVal = e.getValue();

                String oldVal = oldProp.putIfAbsent(key, newVal);

                tmp.putIfAbsent(key, new ThreeTuple<>(key, oldVal, newVal));
            }

            data.addAll(new ArrayList<>(tmp.values()));
            oldProp.clear();
            oldProp.putAll(newProp);
        }

        private void onDataChange(List<ThreeTuple<String, String, String>> data) {
            for (ThreeTuple<String, String, String> tuple : data) {
                for (ConfigListener listener : listeners) {
                    if (!tuple.valueEquals()) {
                        listener.onChange(tuple.k1, tuple.v1, tuple.v2);
                    }
                }
            }
        }


    }


}
