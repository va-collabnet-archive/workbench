package org.ihtsdo.db.bdb.concept.component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class Revision<V extends Revision<V, C>, 
							  C extends ConceptComponent<V, C>> 
	implements I_AmPart, I_HandleFutureStatusAtPositionSetup {
	
	public static SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");

	
	public int sapNid = Integer.MAX_VALUE;
	public C primordialComponent;

	public Revision(int statusAtPositionNid, C primordialComponent) {
		super();
		this.sapNid = statusAtPositionNid;
		this.primordialComponent = primordialComponent;
		assert primordialComponent != null;
		assert statusAtPositionNid != Integer.MAX_VALUE;
	}

	public Revision(int statusNid, int pathNid, long time, C primordialComponent) {
		this.sapNid = Bdb.getStatusAtPositionDb().getSapNid(statusNid, pathNid, time);
		this.primordialComponent = primordialComponent;
		assert primordialComponent != null;
		assert sapNid != Integer.MAX_VALUE;
	}
	
	public Revision(TupleInput input, C conceptComponent) {
		this(input.readInt(), conceptComponent);
	}

    public Revision() {
        super();
    }
    	
	public final void writePartToBdb(TupleOutput output) {
		output.writeInt(sapNid);
		writeFieldsToBdb(output);
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup#isSetup()
	 */
	public boolean isSetup() {
		return sapNid != Integer.MAX_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup#setStatusAtPositionNid(int)
	 */
	public void setStatusAtPositionNid(int sapNid) {
		this.sapNid = sapNid;
	}
	
	protected abstract void writeFieldsToBdb(TupleOutput output);
	
	public final C getVersioned() {
		return primordialComponent;
	}

	public final Set<TimePathId> getTimePathSet() {
		return primordialComponent.getTimePathSet();
	}
	
	public List<V> getVersions() {
		return primordialComponent.revisions;
	}

	@Override
	public final int hashCode() {
		return HashFunction.hashCode( new int[] { primordialComponent.nid });
	}


	public final int getStatusAtPositionNid() {
		return sapNid;
	}

	public final ArrayIntList getPartComponentNids() {
		ArrayIntList resultList = getVariableVersionNids();
		resultList.add(getPathId());
		resultList.add(getStatusId());
		return resultList;
	}
	
	protected abstract ArrayIntList getVariableVersionNids();
	
	@Override
	public int getPathId() {
		return Bdb.getStatusAtPositionDb().getPathId(sapNid);
	}

	@Override
	public int getStatusId() {
		return Bdb.getStatusAtPositionDb().getStatusId(sapNid);
	}

	@Override
	public int getVersion() {
		return Bdb.getStatusAtPositionDb().getVersion(sapNid);
	}

	public long getTime() {
		return Bdb.getStatusAtPositionDb().getTime(sapNid);
	}

	@Override
	public I_AmPart duplicate() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 1. Analog, an object, concept or situation which in some way resembles a different situation
	 * 2. Analogy, in language, a comparison between concepts
	 * @param statusNid
	 * @param pathNid
	 * @param time
	 * @return
	 */
	public abstract V makeAnalog(int statusNid, int pathNid, long time);

	public void setStatusAtPosition(int statusNid, int pathNid, long time) {
		this.sapNid = Bdb.getStatusAtPositionDb().getSapNid(statusNid, pathNid, time);
	}

	@Override
	@Deprecated
	public void setPathId(int pathId) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void setStatusId(int statusId) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void setVersion(int version) {
		throw new UnsupportedOperationException();
	}
	
    /**
     * Returns a string representation of the object.
     */ 
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getSimpleName() + ": ");
        buf.append(" (path:");
        ConceptComponent.addNidToBuffer(buf, getPathId());
        buf.append(" tm:");
        buf.append(fileDateFormat.format(new Date(getTime())));
        buf.append(" status:");
        ConceptComponent.addNidToBuffer(buf, getStatusId());
        buf.append("); ");
        return buf.toString();
    }

		
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
		if (Revision.class.isAssignableFrom(obj.getClass())) {
			Revision<V, C> another = (Revision<V, C>) obj;
			if (this.sapNid == another.sapNid) {
				return true;
			}
		}
		return false;
	}

}
