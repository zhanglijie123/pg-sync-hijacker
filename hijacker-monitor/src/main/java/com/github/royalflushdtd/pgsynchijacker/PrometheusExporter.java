package com.github.royalflushdtd.pgsynchijacker;

import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class PrometheusExporter implements MonitorExporter {

    private ExporterConfig exporterConfig;
    private PrometheusMonitor monitor;
    private HTTPServer server;

    public PrometheusExporter(ExporterConfig exporterConfig) {
        this.exporterConfig = exporterConfig;
        this.monitor = new PrometheusMonitor(this.exporterConfig);
    }

    @Override
    public void startup() {
        PrometheusStatsCollector.createAndRegister();
        DefaultExports.initialize();
        try {
            this.server = new HTTPServer(this.exporterConfig.getExportPort());
        } catch (Exception e) {
            //
        }
    }

    @Override
    public void destroy() {
        if (this.server != null) {
            this.server.stop();
        }
    }

    @Override
    public Monitor getMonitor() {
        return monitor;
    }

}
