package com.github.royalflushdtd.pgsynchijacker;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class MonitorFactory {

    private static volatile MonitorExporter exporter;

    private MonitorFactory() {
    }

    public static Monitor getMonitor() {
        return exporter.getMonitor();
    }

    public static synchronized MonitorExporter initializeExporter(boolean useYukon, ExporterConfig config) {
        if (exporter == null) {
            synchronized (MonitorFactory.class) {
                exporter = new PrometheusExporter(config);
            }
        }

        return exporter;
    }

}
