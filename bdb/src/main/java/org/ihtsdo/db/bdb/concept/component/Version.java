package org.ihtsdo.db.bdb.concept.component;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.StatusAtPositionBdb;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class Version<V extends Version<V, C>, 
							  C extends ConceptComponent<V, C>> 
	implements I_AmPart, I_AmTuple, I_HandleFutureStatusAtPositionSetup {
	
	private static StatusAtPositionBdb sapBdb = Bdb.getStatusAtPositionDb();
	
	public int statusAtPositionNid = Integer.MAX_VALUE;
	public C primordialComponent;

	public Version(int statusAtPositionNid, C primordialComponent) {
		super();
		this.statusAtPositionNid = statusAtPositionNid;
		this.primordialComponent = primordialComponent;
		assert primordialComponent != null;
		assert statusAtPositionNid != Integer.MAX_VALUE;
	}

	public Version(int statusNid, int pathNid, long time, C primordialComponent) {
		this.statusAtPositionNid = sapBdb.getStatusAtPositionNid(statusNid, pathNid, time);
		this.primordialComponent = primordialComponent;
		assert primordialComponent != null;
		assert statusAtPositionNid != Integer.MAX_VALUE;
	}
	
	public Version(TupleInput input, C conceptComponent) {
		this(input.readInt(), conceptComponent);
	}

	
	public final void writePartToBdb(TupleOutput output) {
		output.writeInt(statusAtPositionNid);
		writeFieldsToBdb(output);
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup#isSetup()
	 */
	public boolean isSetup() {
		return statusAtPositionNid != Integer.MAX_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup#setStatusAtPositionNid(int)
	 */
	public void setStatusAtPositionNid(int sapNid) {
		this.statusAtPositionNid = sapNid;
	}
	
	protected abstract void writeFieldsToBdb(TupleOutput output);
	
	public final C getVersioned() {
		return primordialComponent;
	}

	@Override
	public final C getFixedPart() {
		return primordialComponent;
	}

	@Override
	public final int getNid() {
		return primordialComponent.getNid();
	}

	public final Set<TimePathId> getTimePathSet() {
		return primordialComponent.getTimePathSet();
	}
	
	public List<V> getVersions() {
		return primordialComponent.additionalVersions;
	}

	@Override
	public final int hashCode() {
		return HashFunction.hashCode( new int[] { primordialComponent.nid });
	}


	public final int getStatusAtPositionNid() {
		return statusAtPositionNid;
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
		return sapBdb.getPathId(statusAtPositionNid);
	}

	@Override
	public int getStatusId() {
		return sapBdb.getStatusId(statusAtPositionNid);
	}

	@Override
	public int getVersion() {
		return sapBdb.getVersion(statusAtPositionNid);
	}

	public long getTime() {
		return sapBdb.getTime(statusAtPositionNid);
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
		this.statusAtPositionNid = sapBdb.getStatusAtPositionNid(statusNid, pathNid, time);
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
	
	
	
	public String toString() {
		return " sapNid: " + statusAtPositionNid;
	}

	@Override
	public boolean equals(Object obj) {
		if (Version.class.isAssignableFrom(obj.getClass())) {
			Version<V, C> another = (Version<V, C>) obj;
			if (this.statusAtPositionNid == another.statusAtPositionNid) {
				return true;
			}
		}
		return false;
	}

}
