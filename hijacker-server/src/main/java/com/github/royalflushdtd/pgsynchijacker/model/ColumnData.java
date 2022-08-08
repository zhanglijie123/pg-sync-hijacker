package com.github.royalflushdtd.pgsynchijacker.model;

import java.io.Serializable;
import java.util.Objects;


/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class ColumnData implements Serializable  {

    private String name;
    private String dataType;
    private boolean isColumn;
    private boolean isKey;
    private String value;

    public ColumnData() {
    }

    public ColumnData(String name, String dataType, String value) {
        this.name = name;
        this.dataType = dataType;
        this.value = value;
        this.isKey = false;
        this.isColumn = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setColumn(boolean column) {
        isColumn = column;
    }

    public void setKey(boolean key) {
        isKey = key;
    }

    public boolean isColumn() {
        return isColumn;
    }

    public boolean isKey() {
        return isKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ColumnData data = (ColumnData) o;
        return Objects.equals(name, data.name) &&
                Objects.equals(dataType, data.dataType) &&
                Objects.equals(value, data.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dataType, value);
    }

    @Override
    public String toString() {
        return "ColumnData{" + "name='" + name + '\'' + ", dataType='" + dataType + '\'' + ", isColumn=" + isColumn
            + ", isKey=" + isKey + ", value='" + value + '\'' + '}';
    }


    public ColumnData copyColumnData(){
        ColumnData columnData = new ColumnData();
        columnData.setColumn(this.isColumn);
        columnData.setKey(this.isKey);
        columnData.setDataType(this.dataType);
        columnData.setName(this.name);
        columnData.setValue(this.value);
        return columnData;
    }
}
