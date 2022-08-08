package com.github.royalflushdtd.pgsynchijacker.store;
import com.github.royalflushdtd.pgsynchijacker.model.InvokeContext;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public interface IStore {

    /**
     * 一次sql事件
     *
     * @param ctx 上下文
     */
    void store(InvokeContext ctx);

}
