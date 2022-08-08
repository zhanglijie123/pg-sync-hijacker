package com.github.royalflushdtd.pgsynchijacker.filter;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.github.royalflushdtd.pgsynchijacker.model.ColumnData;
import com.github.royalflushdtd.pgsynchijacker.model.Event;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class ColumnFilter implements IEventFilter {

    private final List<String> columnNames;

    public ColumnFilter(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    @Override
    public boolean filter(Event event) {

        if (CollectionUtils.isEmpty(columnNames)) {
            return true;
        }
        List<ColumnData> dataList = event.getDataList();
        if(dataList == null || dataList.size() == 0){
            return false;
        }
        int count = 0;
        for (ColumnData columnData : dataList) {
            if(columnNames.contains(columnData.getName())){
                columnData.setColumn(true);
                count++;
            }
        }
        return count>0;
    }

}
