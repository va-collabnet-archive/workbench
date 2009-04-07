package org.dwfa.mojo.refset.scrub.markedparents;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

import java.io.Serializable;

/**
 * Uniquely identifies a component in a specific reference set.
 */
public final class ComponentRefsetKey implements Serializable {

    private static final long serialVersionUID = 3960306563510866348L;
    
    private final Integer componentId;
    private final Integer refsetId;

    public ComponentRefsetKey(final I_ThinExtByRefVersioned member) {
        componentId = member.getComponentId();
        refsetId = member.getRefsetId();
    }

    public Integer getComponentId() {
        return componentId;
    }

    public Integer getRefsetId() {
        return refsetId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComponentRefsetKey that = (ComponentRefsetKey) o;

        if (componentId != null ? !componentId.equals(that.componentId) : that.componentId != null) {
            return false;
        }
        if (refsetId != null ? !refsetId.equals(that.refsetId) : that.refsetId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = componentId != null ? componentId.hashCode() : 0;
        result = 31 * result + (refsetId != null ? refsetId.hashCode() : 0);
        return result;
    }
}
