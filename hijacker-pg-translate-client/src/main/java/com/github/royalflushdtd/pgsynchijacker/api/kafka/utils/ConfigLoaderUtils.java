package com.github.royalflushdtd.pgsynchijacker.api.kafka.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.alibaba.fastjson.JSON;
import com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList;

import cn.hutool.json.JSONUtil;

/**
 * @author zhanglijie
 * @version 1.0
 * @since 1.1.0 2022/8/6 0006 16:50
 */
public class ConfigLoaderUtils {
    private static final char TRANSFER_CHAR  = '-';
    public static List<Object> parse (String file) throws IOException {
        Object load = null;
        try(FileInputStream fis = new FileInputStream(new File(file))){
            Yaml yaml = new Yaml();
            load = yaml.load(fis);
            if(load == null){
                throw new IOException("load yml config happen error!!");
            }
        }
        String s = JSONUtil.toJsonStr(transfer(load));
        if(!JSONUtil.isJsonArray(s)){
            throw new IllegalArgumentException();
        }
        return JSONUtil.parseArray(s);
    }

    private static Object transfer(Object config) {
        if(config instanceof ArrayList){
            ArrayList<LinkedHashMap<String,Object>> newDatas = new ArrayList<>();
            for(LinkedHashMap<String,Object> item:(ArrayList<LinkedHashMap<String, Object>>)config){
                newDatas.add((LinkedHashMap<String,Object>) transfer(item));
            }
            return newDatas;
        }else if(config instanceof LinkedHashMap){
            LinkedHashMap<String, Object> newMap = new LinkedHashMap<>();
            for(Map.Entry<String,Object> entry : ((LinkedHashMap<String,Object>)config).entrySet()){
                String key = entry.getKey();
                while(true){
                    int index = key.indexOf(TRANSFER_CHAR);
                    if(index >= 0 ){
                        key = toUpper(key,index);
                    }else {
                        break;
                    }
                }
                //递归执行
                newMap.put(key,transfer(entry.getValue()));
            }
            return newMap;
        }
        return config;
    }

    /**
     * 将index 位置元素删除  index+1元素转大写
     * @param value
     * @param index
     * @return
     */
    private static String toUpper(String value, int index) {
        char[] chars = value.toCharArray();
        char[] newChars = new char[chars.length - 1];
        //复制开始部分
        System.arraycopy(chars,0,newChars,0,index);
        //中间部分
        newChars[index] = (char)(((int)chars[index+1])-32);
        //复制结束部分
        System.arraycopy(chars,index+2,newChars,index+1,newChars.length-index-1);
        return new String(newChars);
    }
}
