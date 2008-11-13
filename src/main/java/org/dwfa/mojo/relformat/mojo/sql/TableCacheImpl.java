package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;
import org.dwfa.mojo.relformat.mojo.sql.parser.TableSchemaParser;
import org.dwfa.mojo.relformat.xml.ReleaseConfig;
import org.dwfa.mojo.relformat.xml.ReleaseFormat;

import java.util.HashMap;
import java.util.Map;

public final class TableCacheImpl implements TableCache {

    private final Map<String, Table> tableCache;
    private final TableSchemaParser tableSchemaParser;

    public TableCacheImpl(final TableSchemaParser tableSchemaParser) {
        this.tableSchemaParser = tableSchemaParser;
        tableCache = new HashMap<String, Table>();
    }

    public void cache(final ReleaseConfig releaseConfig) {
        for (ReleaseFormat format : releaseConfig.getReleaseFormats()) {
            tableCache.put(format.getType(), tableSchemaParser.parse(format.getSchema()));
        }
    }

    public Table getTable(final String type) {
        if (tableCache.containsKey(type)) {
            return tableCache.get(type);
        }

        throw new TableCacheException("Could not retrieve table for format: " + type);
    }
}
