package com.github.royalflushdtd.pgsynchijacker.config;

import com.github.royalflushdtd.pgsynchijacker.config.fileloader.ConfigLoader;
import com.github.royalflushdtd.pgsynchijacker.config.fileloader.FileConfigLoader;
import com.github.royalflushdtd.pgsynchijacker.config.fileloader.YmlFileConfigLoader;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class ConfigLoaderFactory {

    private ConfigLoaderFactory() {
    }

    public static ConfigLoader getConfigLoader(PgSyncConfig pgSyncConfig) {
        return getFileConfigLoader(pgSyncConfig.getConfigFile());
    }

    private static FileConfigLoader getFileConfigLoader(String fileName){
        String ext = "";
        try {
            ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        } catch (StringIndexOutOfBoundsException e) {
            // do nothing
        }

        switch (ext){
            case "yml":
            case "yaml": return new YmlFileConfigLoader(fileName);
            default: return new YmlFileConfigLoader(fileName);
        }
    }

}
