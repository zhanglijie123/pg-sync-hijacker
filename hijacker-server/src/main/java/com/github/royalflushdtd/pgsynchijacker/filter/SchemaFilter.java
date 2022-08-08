package com.github.royalflushdtd.pgsynchijacker.filter;

import com.github.royalflushdtd.pgsynchijacker.model.Event;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class SchemaFilter implements IEventFilter {

    private final String schemaName;

    public SchemaFilter(String schemaName) {
        this.schemaName = schemaName;
    }

    @Override
    public boolean filter(Event event) {
        if (event == null || event.getSchema() == null) {
            return false;
        }
        return getSchemaName() == null || getSchemaName().contains(event.getSchema());
    }

    public String getSchemaName() {
        return schemaName;
    }
}
