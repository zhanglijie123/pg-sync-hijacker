package com.github.royalflushdtd.pgsynchijacker;

import lombok.Data;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
@Data
public class ExporterConfig {

    private int exportPort = 8080;
    private String metricName = "PgSyncMetric";
    private String[] labelNames = labelNames();
    private String loggerName = "PgSyncLogger";


    private static String[] labelNames() {
        return new String[]{
                "slotName", "appId",
                "database", "table",
                "target", "total",
                "currentTime", "error"
        };
    }

}

