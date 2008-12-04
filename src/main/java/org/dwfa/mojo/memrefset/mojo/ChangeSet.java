package org.dwfa.mojo.memrefset.mojo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ChangeSet {

    private Long time;
    private UUID pathUUID;
    private UUID refsetUUID;
    private String refsetName;
    private final List<RefSet> refSetList;

    public ChangeSet() {
        refSetList = new ArrayList<RefSet>();
    }

    public void setPathUUID(final UUID pathUUID) {
        this.pathUUID = pathUUID;
    }

    public void setRefsetUUID(final UUID refsetUUID) {
        this.refsetUUID = refsetUUID;
    }

    public void add(final RefSet rs) {
        refSetList.add(rs);
    }

    public UUID getPathUUID() {
        return pathUUID;
    }

    public UUID getRefsetUUID() {
        return refsetUUID;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(final Long time) {
        this.time = time;
    }

    public List<RefSet> getRefsetList() {
        return refSetList;
    }

    public String getRefsetName() {
        return refsetName;
    }

    public void setRefsetName(final String refsetName) {
        this.refsetName = refsetName;
    }

    @Override
    public String toString() {
        return "ChangeSet{" +
                "time=" + time +
                ", pathUUID=" + pathUUID +
                ", refsetUUID=" + refsetUUID +
                ", refsetName='" + refsetName + '\'' +
                ", refSetList=" + refSetList +
                '}';
    }
}
