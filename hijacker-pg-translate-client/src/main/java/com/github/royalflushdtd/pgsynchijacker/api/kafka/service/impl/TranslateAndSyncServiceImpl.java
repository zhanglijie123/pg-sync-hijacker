package com.github.royalflushdtd.pgsynchijacker.api.kafka.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.github.royalflushdtd.pgsynchijacker.api.kafka.config.PgTranslateConfig;
import com.github.royalflushdtd.pgsynchijacker.api.kafka.dao.PgDaoServiceImpl;
import com.github.royalflushdtd.pgsynchijacker.api.kafka.model.TranslateRelateBo;
import com.github.royalflushdtd.pgsynchijacker.api.kafka.service.TranslateAndSyncService;
import com.github.royalflushdtd.pgsynchijacker.model.ColumnData;
import com.github.royalflushdtd.pgsynchijacker.model.Event;

import ch.qos.logback.classic.Logger;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhanglijie
 * @version 1.0
 * @since 1.1.0 2022/8/7 0007 17:57
 *
 */
@Service
@Slf4j
public class TranslateAndSyncServiceImpl implements TranslateAndSyncService {
    private  static final String SEP = ".";
    private static final String TRANSLATE_TYPE_SEP = "2";
    private static final String SEP2 = ",";
    private static final String DELETE = "DELETE";
    private static final String UPDATE = "UPDATE";
    private static final String INSERT = "INSERT";
    private static final Set<String> EVENT_SET = new HashSet();

    @Autowired
    private PgTranslateConfig pgTranslateConfig;
    @Autowired
    private PgDaoServiceImpl pgDaoService;

    @Autowired
    private TextTranslateServiceImpl textTranslateService;
    @Override
    public void translate(String content) {
        Event msg = JSON.parseObject(content, Event.class);
        if(msg.getEventType() == null){
            return;
        }
        String schema = msg.getSchema();
        String table = msg.getTable();
        String key = '"'+schema+'"'+"."+'"'+table+'"';

        List<ColumnData> dataList = msg.getDataList();
        Map<String, Set<TranslateRelateBo>> relateBoMap = pgTranslateConfig.getRelateBoMap();
        Map<String, String> dataRela = new HashMap<>();
        Map<String, String> unique = new HashMap<>();
        List<String> uniqueSet = new ArrayList<>();

        for (ColumnData columnData : dataList) {
            try {
                dataRela.put(columnData.getName(), columnData.getValue());
                if(columnData.isKey()) {
                    unique.put(columnData.getName(), columnData.getValue());
                    uniqueSet.add(columnData.getName());
                }
            }catch (Exception e){
               log.warn("delete map error ,the msg is {},the exe is {}",content,e);
            }
        }


        //包含了 表明是指定某表某字段-》某表某字段的翻译类型
        if(relateBoMap.containsKey(key)){
            Set<TranslateRelateBo> translateRelateBos = relateBoMap.get(key);
            for (TranslateRelateBo translateRelateBo : translateRelateBos) {
                Map<String, String> describe = translateRelateBo.getDescribe();
                String targetTable = translateRelateBo.getTargetTable();
                String relaSql ="";
                if(describe != null && describe.size() != 0 ) {
                    int size = describe.size();
                    int index = 0;
                    for (Map.Entry<String, String> entry : describe.entrySet()) {
                        if(index==(size-1)) {
                            String k = entry.getKey();
                            String v = entry.getValue();
                            String value = dataRela.get(k);
                            relaSql += " "+v+" = "+value;
                        }else{
                            String k = entry.getKey();
                            String v = entry.getValue();
                            String value = dataRela.get(k);
                            relaSql +=" "+v+" = "+value+" AND ";
                        }
                        index++;
                    }
                }
                if(DELETE.equals(msg.getEventType().name())){
                    String[] split = translateRelateBo.getTargetField().split(",");
                    String updateSql = "";
                    int leng = split.length;
                    int index = 0;
                    for (String s : split) {
                        if(index == (leng-1)){
                            updateSql +=" "+s+" = ''";
                        }else{
                            updateSql += " " + s + " = ''" +" , ";
                        }
                        index++;
                    }
                    String sql = "";
                    sql = "UPDATE "+targetTable+ " set "+updateSql+ " where " +relaSql+" ;";
                    pgDaoService.exeSql(sql);
                }else {
                    //update | insert (insert這種情況目的表已存在記錄只是更新下翻譯文本）
                    String sourceField = translateRelateBo.getSourceField();
                    List<ColumnData> translateRes = translate(sourceField, dataList, translateRelateBo);
                    int size = translateRes.size();
                    int index = 0;
                    String updateSql = "";
                    for (ColumnData columnData : translateRes) {
                        if (index == (size - 1)) {
                            updateSql += " " + columnData.getName() + " = '" + columnData.getValue() + "' ";
                        } else {
                            updateSql += " " + columnData.getName() + " = '" + columnData.getValue() + "' ,  ";
                        }
                        index++;
                    }
                    String sql = "UPDATE " + targetTable + " SET " + updateSql + " where " +relaSql+" ;";
                    pgDaoService.exeSql(sql);
                }
            }
        }else{
            /**
             * 如果有主从同步情况下  下面insert|delete不用管了  删除主从同步 从库自己删了   插入 可用更新替代去翻译即可
             * 下面演示的是没有主从同步情况下
             */
            if(DELETE.equals(msg.getEventType().name())){
                String sql = "";
                sql = "DELETE FROM "+key+ " where "+
                    uniqueSet.get(0)+" = "+ dataRela.get(uniqueSet.get(0))+ " ;";
                pgDaoService.exeSql(sql);
            }
            List<ColumnData> translateRes = translate(null,dataList,null);


            if(INSERT.equals(msg.getEventType().name())){
                HashMap<String, ColumnData> translateMap = new HashMap<>();
                for (ColumnData translateRe : translateRes) {
                    translateMap.put(translateRe.getName(),translateRe);
                }
                HashMap<String, ColumnData> all = new HashMap<>();
                for (ColumnData columnData : dataList) {
                    all.put(columnData.getName(),columnData);
                }
                Map<String, String> res = new HashMap<>();
                for (Map.Entry<String, String> entry : dataRela.entrySet()) {
                    String k = entry.getKey();
                    String v = entry.getValue();
                    if(translateMap.containsKey(k)){
                      v = translateMap.get(k).getValue();
                    }
                    res.put(k,v);
                }
                String sql = " ( ";
                String sql2 = "VALUES (";
                int size = res.size();
                int index = 0;
                for (Map.Entry<String, String> entry : res.entrySet()) {
                    if(index==(size-1)){
                        String k = entry.getKey();
                        String v = entry.getValue();
                        ColumnData columnData = all.get(k);
                        if("integer".equals( columnData.getDataType())){
                            sql2 += v + " ";
                        }else{
                            sql2 +=" '"+ v +"' ";
                        }
                        sql += k+" ";

                    }else{
                        String k = entry.getKey();
                        String v = entry.getValue();
                        ColumnData columnData = all.get(k);
                        if("integer".equals( columnData.getDataType())){
                            sql2 += v + " , ";
                        }else{
                            sql2 +=" '"+ v +"' "+ " , ";
                        }
                        sql += k+" , ";
                    }
                    index++;

                }
                sql += " ) ";
                sql2 += " ) ";
                String insertSql = "INSERT INTO "+ key+sql+sql2+" ;";
                pgDaoService.exeSql(insertSql);
            }
            else if(UPDATE.equals(msg.getEventType().name())){
                int size = translateRes.size();
                int index = 0;
                String updateSql = "";
                for (ColumnData columnData : translateRes) {
                    if(index == (size-1)){
                        updateSql += " "+columnData.getName()+" = '"+columnData.getValue()+ "' ";
                    }else {
                        updateSql += " " + columnData.getName() + " = '" + columnData.getValue() + "' ,   ";
                    }
                    index++;
                }
                String sql = "UPDATE "+key+ " SET " +updateSql +" WHERE "+
                    uniqueSet.get(0)+" = "+ dataRela.get(uniqueSet.get(0))+ " ;";
                pgDaoService.exeSql(sql);
            }else{}

        }
    }













    private List<ColumnData> translate(String sourceField, List<ColumnData> dataList,TranslateRelateBo translateRelateBo) {
        if(sourceField == null){
            ArrayList<ColumnData> res = new ArrayList<>();
            for (ColumnData columnData : dataList) {
                if(columnData.getDataType() != null && columnData.getDataType().contains("character")){
                    columnData.setValue(textTranslateService.textTranslate(columnData.getValue(),null,null));
                    res.add(columnData);
                }
            }
            return res;
        }
        String[] sourceFields = sourceField.split(SEP2);
        ArrayList<ColumnData> res = new ArrayList<>();
        for (String field : sourceFields) {
            for (ColumnData columnData : dataList) {
                if(field.equals(columnData.getName())){
                    columnData.setValue(textTranslateService.textTranslate(columnData.getValue(),translateRelateBo.getAppId(),translateRelateBo.getAppKey()));
                    res.add(columnData);
                }
            }
        }
        return res;
    }


}
