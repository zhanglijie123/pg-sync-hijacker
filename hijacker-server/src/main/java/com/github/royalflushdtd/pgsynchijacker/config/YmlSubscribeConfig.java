
package com.github.royalflushdtd.pgsynchijacker.config;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
@Data
public class YmlSubscribeConfig {

    @JSONField(name = "pg_dump_path")
    private String pgDumpPath;
    private List<Subscribe> subscribes;

    @Data
    public static class Subscribe {
        private String slotName;
        private PgConnConf pgConnConf;
        private List<Rule> rules;
        private KafkaConf kafkaConf;

    }

    @Data
    public static class PgConnConf {
        private String host;
        private int port;
        private String database;
        private String schema;
        private String user;
        private String password;
    }

    @Data
    public static class KafkaConf {
        private List<String> addrs;
    }


    @Data
    public static class Rule {

        private String table;

        private String topic;
        private int partition;
        private List<String> keys;
        private List<String> columns;
        private String scheme;


        private List<String> esid;
        private String index;
        private String type;
        private Map<String, String> fields;


        private String sql;
        private List<String> parameters;

        private String family;
        private String qualifier;


        private String hbaseTable;
        private List<String> hbaseKey;

        private String hiveTable;
        private List<String> hiveFields;

    }

    /**
     * <pre>
     *     例如在es索引中,一个复合索引 es_idx_t1包含10个字段,其中5个来自tb1,5个来自tb2,
     *     当tb1或者tb2有新增或删除的时候,查询另外一个表的内容,添加到索引中
     *
     *     val: insert into tb1(id1,name1,age1) values('id1','name1','age1');
     *
     *     sql: select id2,name2,age2 from tb2 where id = @id and name= @name
     *
     *     idx: append into es1(id1,id2,name1,name2,age1,age2) values()
     *
     *     假设目前只支持根据主键的join
     *
     * </pre>
     */
    @Data
    public static class Join {

        private String table;
        private String sql;
        private List<String> parameters;

    }

}
