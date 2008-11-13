package org.dwfa.mojo.relformat.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ReleaseFormat")
public class ReleaseFormat {

    @XStreamAlias("Type")
    private String type;

    @XStreamAlias("Schema")
    private String schema;

    public ReleaseFormat(final String type, final String schema) {
        this.type = type;
        this.schema = schema;
    }

    public String getType() {
        return type;
    }

    public String getSchema() {
        return schema;
    }
}
