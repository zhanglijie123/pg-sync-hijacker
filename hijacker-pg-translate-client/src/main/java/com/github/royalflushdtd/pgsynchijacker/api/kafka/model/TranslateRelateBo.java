package com.github.royalflushdtd.pgsynchijacker.api.kafka.model;

import java.util.Map;
import java.util.Objects;

import lombok.Data;

/**
 * @author zhanglijie
 * @version 1.0
 * @since 1.1.0 2022/8/6 0006 16:44
 */
@Data
public class TranslateRelateBo {
    private String sourceTable;
    private String sourceField;
    private String targetTable;
    private String targetField;

    /**翻译语言类型 zh2en| zh2en**/
    private String type;
    /**机器翻译接口认证信息**/
    private String appId;
    private String appKey;
    /**关联字段**/
    private Map<String,String> describe;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TranslateRelateBo that = (TranslateRelateBo) o;
        return Objects.equals(sourceTable, that.sourceTable) && Objects.equals(sourceField, that.sourceField)
            && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceTable, sourceField, type);
    }
}
