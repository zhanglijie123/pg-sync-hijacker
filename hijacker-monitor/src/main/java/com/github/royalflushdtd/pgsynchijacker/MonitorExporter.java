package com.github.royalflushdtd.pgsynchijacker;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public interface MonitorExporter {

    /**
     * 启动 exporter 容器,以便于外界查询
     */
    void startup();

    /**
     * 关闭 exporter 容器
     */
    void destroy();

    /**
     * 获取监视器
     *
     * @return 监视器
     */
    Monitor getMonitor();

}
