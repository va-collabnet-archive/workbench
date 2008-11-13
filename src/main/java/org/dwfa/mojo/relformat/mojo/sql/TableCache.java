package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;
import org.dwfa.mojo.relformat.xml.ReleaseConfig;

public interface TableCache {

    void cache(ReleaseConfig releaseConfig);

    Table getTable(String type);
}
