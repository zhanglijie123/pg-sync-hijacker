package com.github.royalflushdtd.pgsynchijacker.utils;



import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.royalflushdtd.pgsynchijacker.model.Event;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class MsgUtils {


    private MsgUtils() {
    }

    public static String toUpdate(Event event, List<String> pkNames) {
        String schema = event.getSchema();
        String table = event.getTable();

        String kv = event.getDataList().stream().map(cd -> cd.getName() + "=" + cd.getValue()).reduce((s1, s2) -> s1 + "," + s2).orElse("");
        String pk = getPKValues(event, pkNames);

        return "update " + schema + "." + table + " set " + pk + " where " + kv;
    }

    public static String toDelete(Event event, List<String> pkNames) {
        String schema = event.getSchema();
        String table = event.getTable();
        String pk = getPKValues(event, pkNames);
        return "delete from " + schema + "." + table + " where " + pk;
    }

    public static String toInsert(Event event) {
        String schema = event.getSchema();
        String table = event.getTable();

        List<String> keys = new ArrayList<>();
        List<String> vals = new ArrayList<>();
        event.getDataList().forEach(cd -> {
            keys.add(cd.getName());
            vals.add("'" + cd.getValue() + "'");
        });

        return "insert into " + schema + "." + table + "(" + StringUtils.join(keys, ',') + ") values(" + StringUtils.join(vals, ',') + ")";
    }

    private static String getPKValues(Event event, List<String> pkNames) {
        return event.getDataList().stream()
                .filter(cd -> pkNames.contains(cd.getName()))
                .map(cd -> cd.getName() + "=" + cd.getValue())
                .reduce((s1, s2) -> s1 + "," + s2)
                .orElse("");

    }

}
