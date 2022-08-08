package com.github.royalflushdtd.pgsynchijacker.config;

import lombok.Data;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
@Data
public class PgSyncConfig {

    private String appId;
    private String configFile;
    private int processId;

}
