package org.dwfa.mojo;

public class TupleKey {

    public Integer id;
    public Integer version;
    public Integer pathId;

    public TupleKey(int id, int pathId, int version) {
        this.id = id;
        this.version = version;
        this.pathId = pathId;
    }

    @Override
    public int hashCode() {
        return ("" + id +
                "" + pathId +
                "" + version).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        TupleKey compareKey = (TupleKey) o;
        return id.equals(compareKey.id) &&
                version.equals(compareKey.version) &&
                pathId.equals(compareKey.pathId);
    }
}
