package com.github.royalflushdtd.pgsynchijacker.publisher;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.collections.CollectionUtils;

import com.github.royalflushdtd.pgsynchijacker.PgFullSyncServer;
import com.github.royalflushdtd.pgsynchijacker.model.Event;
import com.github.royalflushdtd.pgsynchijacker.model.InvokeContext;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class PublisherManager implements IPublisher {

    private static final PublisherManager INSTANCE = new PublisherManager();


    public Map<String, IPublisher> publishers = new ConcurrentHashMap<>();

    public Map<String, IPublisher> getPublishers() {
        return publishers;
    }

    public PublisherManager() {
    }

    public static PublisherManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void publish(Event event, Callback callback) {
        this.publishers.get(event.getSlotName()).publish(event, callback);
    }

    @Override
    public void publish(InvokeContext context, Callback callback) {
        this.publishers.get(context.getSlotName()).publish(context, callback);
    }

    @Override
    public void close() {
        this.publishers.values().forEach(IPublisher::close);
        this.publishers.clear();
    }

    @Override
    public void publish(List<InvokeContext> contexts) {
        if (CollectionUtils.isEmpty(contexts)) {
            return;
        }
    }

    public void putPublisher(String slotName, IPublisher publisher) {
        this.publishers.put(slotName, publisher);
    }
}
