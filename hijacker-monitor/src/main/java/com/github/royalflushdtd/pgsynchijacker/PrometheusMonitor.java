package com.github.royalflushdtd.pgsynchijacker;

import io.prometheus.client.Gauge;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class PrometheusMonitor implements Monitor {

    private final Gauge gauge;

    public PrometheusMonitor(ExporterConfig config) {
        this.gauge = Gauge.build()
                .name(config.getMetricName())
                .labelNames(config.getLabelNames()).help("Pgsync Requests.")
                .register();
    }

    @Override
    public void collect(Statics statics) {
        this.gauge.labels(statics.getSlotName(), statics.getAppId(),
                statics.getDatabase(), statics.getTable(),
                statics.getTarget(), String.valueOf(statics.getTotal()),
                String.valueOf(statics.getCurrentTime()), statics.getError() == null ? "" : statics.getError()).inc();
    }

}
