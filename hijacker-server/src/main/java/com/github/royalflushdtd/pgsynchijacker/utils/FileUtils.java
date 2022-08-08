package com.github.royalflushdtd.pgsynchijacker.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;

import com.github.royalflushdtd.pgsynchijacker.PgSyncStarter;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class FileUtils {

    private FileUtils() {
    }

    public static InputStream load(String file) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
        if (is == null) {
            is = PgSyncStarter.class.getResourceAsStream(file);
        }
        if (is == null) {
            is = loadFromFileSystem(file);
        }
        return is;
    }

    private static InputStream loadFromFileSystem(String configPath) {
        if (Paths.get(configPath).toFile().exists()) {
            try {
                return new FileInputStream(Paths.get(configPath).toFile());
            } catch (Exception ignore) {
                //
            }
        }
        return null;
    }
}
