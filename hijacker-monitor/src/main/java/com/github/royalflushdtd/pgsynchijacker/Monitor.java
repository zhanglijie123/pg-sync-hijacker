package com.github.royalflushdtd.pgsynchijacker;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public interface Monitor {

    /**
     * 收集统计数据
     *
     * @param statics 统计数据
     */
    void collect(final Statics statics);

}
