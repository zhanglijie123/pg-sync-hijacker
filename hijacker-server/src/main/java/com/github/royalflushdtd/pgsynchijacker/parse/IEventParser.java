package com.github.royalflushdtd.pgsynchijacker.parse;

import com.github.royalflushdtd.pgsynchijacker.model.InvokeContext;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public interface IEventParser {

    /**
     * 解析消息
     *
     * @param context 上下文信息
     */
    void parse(InvokeContext context);

}
