package org.ihtsdo.concept.component.refset;

import java.beans.PropertyVetoException;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.blueprint.RefexCUB;
import org.ihtsdo.tk.api.refex.RefexAnalogBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetType;

import com.sleepycat.bind.tuple.TupleInput;
import java.io.IOException;

public abstract class RefsetRevision
			<V extends RefsetRevision<V, C>, 
			 C extends RefsetMember<V, C>>
        extends Revision<V, C>
        implements I_ExtendByRefPart<V>, RefexAnalogBI<V> {

    public RefsetRevision(int statusNid, int pathNid, long time,
            C primordialComponent) {
        super(statusNid,
                Terms.get().getAuthorNid(),
                pathNid, time, primordialComponent);
    }

    public RefsetRevision(int statusNid, int authorNid, int pathNid, long time,
            C primordialComponent) {
        super(statusNid, authorNid,
                pathNid, time, primordialComponent);
    }

    public RefsetRevision(int statusAtPositionNid, C primordialComponent) {
        super(statusAtPositionNid, primordialComponent);
    }

    public RefsetRevision(TupleInput input, C primordialComponent) {
        super(input, primordialComponent);
    }

    public RefsetRevision(TkRevision eVersion,
            C member) {
        super(Bdb.uuidToNid(eVersion.getStatusUuid()),
                Bdb.uuidToNid(eVersion.getAuthorUuid()),
                Bdb.uuidToNid(eVersion.getPathUuid()),
                eVersion.getTime(),
                member);
    }

    public RefsetRevision() {
        super();
    }

    @Override
    public final int compareTo(I_ExtendByRefPart<V> o) {
        return this.toString().compareTo(o.toString());
    }

    @Override
    @Deprecated
    public final int getStatus() {
        return getStatusNid();
    }

    @Override
    @Deprecated
    public final void setStatus(int idStatus) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public I_ExtendByRefPart<V> duplicate() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (RefsetRevision.class.isAssignableFrom(obj.getClass())) {
            RefsetRevision<?, ?> another = (RefsetRevision<?, ?>) obj;
            if (this.sapNid == another.sapNid) {
                return true;
            }
        }
        return false;
    }

    public abstract V makeAnalog();

    @Override
    public String toUserString() {
        return toString();
    }
    
    @Override
	public int getCollectionNid() {
		return primordialComponent.refsetNid;
	}

	@Override
	public void setCollectionNid(int collectionNid) throws PropertyVetoException {
		primordialComponent.setCollectionNid(collectionNid);
	}

	@Override
    public RefexCUB getRefexEditSpec() throws IOException {
    	RefexCUB rcs = new RefexCUB(getTkRefsetType(), 
    			primordialComponent.getReferencedComponentNid(), 
    			primordialComponent.getRefsetId(),
        		getPrimUuid());
    	addSpecProperties(rcs);
    	return rcs;
    }

	protected abstract TkRefsetType getTkRefsetType();

	protected abstract void addSpecProperties(RefexCUB rcs);

    @Override
    public int getReferencedComponentNid() {
       return primordialComponent.getReferencedComponentNid();
    }

    @Override
    public void setReferencedComponentNid(int componentNid) throws PropertyVetoException {
        primordialComponent.setReferencedComponentNid(componentNid);
    }

}
