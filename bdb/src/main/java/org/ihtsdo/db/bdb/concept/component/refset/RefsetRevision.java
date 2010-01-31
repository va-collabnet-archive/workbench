package org.ihtsdo.db.bdb.concept.component.refset;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.Revision;
import org.ihtsdo.db.bdb.concept.component.image.ImageRevision;
import org.ihtsdo.etypes.EVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class RefsetRevision<V extends RefsetRevision<V, C>, 
                                              C extends RefsetMember<V, C>> 
	extends Revision<V, C> 
	implements I_ThinExtByRefPart {


	public RefsetRevision(int statusNid, int pathNid, long time, 
			C primordialComponent) {
		super(statusNid, pathNid, time, primordialComponent);
	}

	public RefsetRevision(int statusAtPositionNid, C primordialComponent) {
		super(statusAtPositionNid, primordialComponent);
	}

	public RefsetRevision(TupleInput input, C primordialComponent) {
		super(input, primordialComponent);
	}

	public RefsetRevision(EVersion eVersion,
			C member) {
		super(Bdb.uuidToNid(eVersion.getStatusUuid()), 
				Bdb.uuidToNid(eVersion.getPathUuid()),
				eVersion.getTime(),
				member);
	}

    public RefsetRevision() {
        super();
    }
    
    @Override
	public final int getStatus() {
		return getStatusId();
	}

	@Override
	public final void setStatus(int idStatus) {
		throw new UnsupportedOperationException();
	}


	@Override
	public V makeAnalog(int statusNid, int pathNid, long time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefPart duplicate() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" }=> ");
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (RefsetRevision.class.isAssignableFrom(obj.getClass())) {
            RefsetRevision<?, ?> another = (RefsetRevision<?, ?>) obj;
            if (this.sapNid == another.sapNid) {
                return true;
            }
        }
        return false;
    }


}
