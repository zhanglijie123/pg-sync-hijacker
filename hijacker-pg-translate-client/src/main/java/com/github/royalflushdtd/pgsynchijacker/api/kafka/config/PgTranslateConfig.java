package com.github.royalflushdtd.pgsynchijacker.api.kafka.config;

import java.io.IOException;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.lang.model.element.VariableElement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.github.royalflushdtd.pgsynchijacker.api.kafka.model.TranslateRelateBo;
import com.github.royalflushdtd.pgsynchijacker.api.kafka.utils.ConfigLoaderUtils;

import cn.hutool.json.JSONObject;

/**
 * @author zhanglijie
 * @version 1.0
 * @since 1.1.0 2022/8/6 0006 16:05
 */
@Configuration
public class PgTranslateConfig {
    @Value("${pg.sync.translate.config}")
    private String translateConfigFile;
    @Value("${pg.sync.translate.on.master.enable:false}")
    private  boolean enable;
    private Map<String, Set<TranslateRelateBo>> relateBoMap = new ConcurrentHashMap<>();

    public Map<String,Set<TranslateRelateBo>> getRelateBoMap(){
        return this.relateBoMap;
    }
    private static final String SOURCETABLE = "sourceTable";
    private static final String TARGETTABLE = "targetTable";
    private static final String SOURCEFIELD = "sourceField";
    private static final String TARGETFIELD = "targetField";
    private static final String TRANSLATETYPE = "type";
    private static final String TRANSLATERELATE = "describe";
    private static final String APP_ID = "appId";
    private static final String APP_KEY = "appKey";

    public static void main(String[] args) throws IOException {
        PgTranslateConfig pgTranslateConfig = new PgTranslateConfig();
        String s="D:\\\\lagouStudy\\\\pg-sync-hijacker\\\\hijacker-pg-translate-client\\\\src\\\\main\\\\resources\\\\translate-pg.yml";
        pgTranslateConfig.translateConfigFile=s;
        pgTranslateConfig.buildRelate();
        System.out.println(pgTranslateConfig.relateBoMap);
    }

    @PostConstruct
    public void buildRelate() throws IOException {
        if(!enable){
            return;
        }
        List<Object> parse = ConfigLoaderUtils.parse(translateConfigFile);
        for (Object item : parse) {
            JSONObject jsonItem = (JSONObject) item;
           /* List<String> srcFieldList = Arrays.asList(jsonItem.getStr(SOURCEFIELD).split(","));
            List<String> tarFieldList = Arrays.asList(jsonItem.getStr(TARGETFIELD).split(","));*/
            String srcField = (String)jsonItem.getStr(SOURCEFIELD);
            String targetField = (String)jsonItem.getStr(TARGETFIELD);
            String sourceTable = (String)jsonItem.get(SOURCETABLE);
            String targetTable = (String)jsonItem.get(TARGETTABLE);
            String languageType = (String)jsonItem.get(TRANSLATETYPE);
            String appId = (String)jsonItem.get(APP_ID);
            String appKey = (String)jsonItem.get(APP_KEY);
            Map<String,String> desMap = (Map)jsonItem.get(TRANSLATERELATE);
            Set<TranslateRelateBo> translateRelateBoSet = new HashSet<>();
           // int len = srcFieldList.size();
         //   for (int i = 0; i < len; i++) {
                TranslateRelateBo bo = new TranslateRelateBo();
                bo.setSourceTable(sourceTable.toLowerCase());
                bo.setSourceField(srcField.toLowerCase());
                bo.setTargetTable(targetTable.toLowerCase());
                bo.setTargetField(targetField.toLowerCase());
                bo.setType(languageType.toLowerCase());
                bo.setAppId(appId);
                bo.setAppKey(appKey);
                bo.setDescribe(desMap);
                translateRelateBoSet.add(bo);
          //  }
            if(!relateBoMap.containsKey(sourceTable.toLowerCase())){
                relateBoMap.put(sourceTable.toLowerCase(),translateRelateBoSet);
            }else {
                relateBoMap.get(sourceTable.toLowerCase()).addAll(translateRelateBoSet);
            }

        }
    }
}
