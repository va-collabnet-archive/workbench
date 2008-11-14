package org.dwfa.mojo.relformat.mojo.converter;

import java.util.List;

//TODO: This is a duplicate class created to allow manven to automagically pick up the format configuration.
public final class Format {

    private String type;

    private List<String> filters;

    public Format() {
        //for maven.
    }

    public Format(final String type, final List<String> filters) {
        this.type = type;
        this.filters = filters;
    }

    public String getType() {
        return type;
    }

    public List<String> getFilters() {
        return filters;
    }

    public String toString() {
        return "[Format type=" + type + ", filters=" + filters + " ]";
    }
}