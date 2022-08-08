package com.github.royalflushdtd.pgsynchijacker.filter;
import com.github.royalflushdtd.pgsynchijacker.model.Event;
import com.github.royalflushdtd.pgsynchijacker.model.EventType;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class EventTypeFilter implements IEventFilter {

    private final EventType eventType;

    public EventTypeFilter(EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public boolean filter(Event event) {
        if (event == null) {
            return false;
        }
        return getEventType() == null || event.getEventType() == getEventType();
    }

    public EventType getEventType() {
        return eventType;
    }

}
