package com.github.royalflushdtd.pgsynchijacker.filter;
import com.github.royalflushdtd.pgsynchijacker.model.Event;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public interface IEventFilter {

    /**
     * @param event msg event
     * @return whether test success
     */
    boolean filter(Event event);

}
