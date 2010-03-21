/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
