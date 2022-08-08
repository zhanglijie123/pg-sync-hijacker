package com.github.royalflushdtd.pgsynchijacker.config.fileloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.alibaba.fastjson.JSON;
import com.github.royalflushdtd.pgsynchijacker.config.YmlConfig;
import com.github.royalflushdtd.pgsynchijacker.constants.Constants;
/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class YmlFileConfigLoader extends FileConfigLoader {

    public YmlFileConfigLoader(String fileName) {
        super(fileName);
    }

    @Override
    protected Map<String, String> load(File file) {
        HashMap<String, String> map = new HashMap<>();
        try {
            Yaml yaml = new Yaml();
            YmlConfig ymlConfig = yaml.loadAs(new FileInputStream(file), YmlConfig.class);
            map.put(Constants.PG_SYNC_SUBSCRIBE_CONFIG, JSON.toJSONString(ymlConfig.getPg_sync_subscribe_config()));
            map.put(Constants.PG_SYNC_ZOOKEEPER_ADDRESS, ymlConfig.getPg_sync_zookeeper_address());
        } catch (FileNotFoundException e) {
            logger.warn("config file {} not found", file);
        } catch (RuntimeException e) {
            logger.warn("parse config error: ", e);
        }
        return map;
    }

}
