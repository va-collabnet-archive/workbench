package org.dwfa.mojo.relformat.mojo.sql.builder;

import org.dwfa.mojo.relformat.mojo.sql.ColumnTypeCleanerImpl;
import org.dwfa.mojo.relformat.mojo.sql.TableCache;
import org.dwfa.mojo.relformat.mojo.sql.TableCacheImpl;
import org.dwfa.mojo.relformat.mojo.sql.TableDataExtractorImpl;
import org.dwfa.mojo.relformat.mojo.sql.parser.TableSchemaParserImpl;
import org.dwfa.mojo.relformat.util.StringArrayCleanerImpl;

public final class TableCacheBuilder {

    public TableCache build() {
        StringArrayCleanerImpl stringArrayCleaner = new StringArrayCleanerImpl();
        return new TableCacheImpl(new TableSchemaParserImpl(
                new TableDataExtractorImpl(stringArrayCleaner, new ColumnTypeCleanerImpl()), stringArrayCleaner));
    }
}
