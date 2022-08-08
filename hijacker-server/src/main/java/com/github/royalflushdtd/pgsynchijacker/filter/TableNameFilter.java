package com.github.royalflushdtd.pgsynchijacker.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.github.royalflushdtd.pgsynchijacker.model.Event;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class TableNameFilter implements IEventFilter {

    private final String tableNameExp;
    private final Pattern pattern;

    public TableNameFilter(String tableNameExp) {
        this.tableNameExp = StringUtils.isBlank(tableNameExp) ? ".*" : tableNameExp;
        this.pattern = Pattern.compile(this.tableNameExp);
    }

    @Override
    public boolean filter(Event event) {
        if (event == null || event.getTable() == null) {
            return false;
        }
        Matcher matcher = pattern.matcher(event.getTable());
        return matcher.matches();
    }

}
