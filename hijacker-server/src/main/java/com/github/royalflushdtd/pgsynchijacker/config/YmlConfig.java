package com.github.royalflushdtd.pgsynchijacker.config;

import lombok.Data;


/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
@Data
public class YmlConfig {
    private YmlSubscribeConfig pg_sync_subscribe_config;
    private String pg_sync_zookeeper_address;
}
