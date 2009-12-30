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
	implements I_AmPart, I_AmTuple {
	
	private static StatusAtPositionBdb sapBdb = Bdb.getStatusAtPositionDb();
	
	public int statusAtPositionNid;
	public C conceptComponent;

	public Version(int statusAtPositionNid) {
		super();
		this.statusAtPositionNid = statusAtPositionNid;
	}

	public Version(int statusNid, int pathNid, long time) {
		this.statusAtPositionNid = sapBdb.getStatusAtPositionNid(statusNid, pathNid, time);
	}
	
	public Version(TupleInput input) {
		this(input.readInt());
	}

	
	public final void writePartToBdb(TupleOutput output) {
		output.writeInt(statusAtPositionNid);
		writeFieldsToBdb(output);
	}

	protected abstract void writeFieldsToBdb(TupleOutput output);
	
	public final C getVersioned() {
		return conceptComponent;
	}

	@Override
	public final C getFixedPart() {
		return conceptComponent;
	}

	@Override
	public final int getNid() {
		return conceptComponent.getNid();
	}

	public final Set<TimePathId> getTimePathSet() {
		return conceptComponent.getTimePathSet();
	}
	
	public List<V> getVersions() {
		return conceptComponent.componentVersion;
	}

	@Override
	public final int hashCode() {
		return HashFunction.hashCode( new int[] { conceptComponent.nid });
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
	public V duplicate() {
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
	

}
