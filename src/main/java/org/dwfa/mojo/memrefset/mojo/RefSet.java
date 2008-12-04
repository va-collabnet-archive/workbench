package org.dwfa.mojo.memrefset.mojo;

import java.util.UUID;

public final class RefSet {

    private UUID componentUUID;
    private UUID statusUUID;
    private UUID conceptUUID;
    private UUID memberUUID;

    public void setComponentUUID(final UUID componentUUID) {
        this.componentUUID = componentUUID;
    }

    public void setStatusUUID(final UUID statusUUID) {
        this.statusUUID = statusUUID;
    }

    public void setConceptUUID(final UUID conceptUUID) {
        this.conceptUUID = conceptUUID;
    }

    public UUID getComponentUUID() {
        return componentUUID;
    }

    public UUID getStatusUUID() {
        return statusUUID;
    }

    public UUID getConceptUUID() {
        return conceptUUID;
    }

    public UUID getMemberUUID() {
        return memberUUID;
    }

    public void setMemberUUID(final UUID memberUUID) {
        this.memberUUID = memberUUID;
    }

    @Override
    public String toString() {
        return "RefSet{" +
                "componentUUID=" + componentUUID +
                ", statusUUID=" + statusUUID +
                ", conceptUUID=" + conceptUUID +
                ", memberUUID=" + memberUUID +
                '}';
    }
}
