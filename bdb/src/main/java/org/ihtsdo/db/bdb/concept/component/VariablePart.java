package org.ihtsdo.db.bdb.concept.component;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.StatusAtPositionBdb;

import com.sleepycat.bind.tuple.TupleOutput;

@SuppressWarnings("unchecked")
public abstract class VariablePart<P extends VariablePart> implements I_AmPart {
	
	private static StatusAtPositionBdb sapBdb = Bdb.getStatusAtPositionDb();
	
	public int statusAtPositionNid;

	protected VariablePart(int statusAtPositionNid) {
		super();
		this.statusAtPositionNid = statusAtPositionNid;
	}

	public VariablePart(int statusNid, int pathNid, long time) {
		this.statusAtPositionNid = sapBdb.getStatusAtPositionNid(statusNid, pathNid, time);
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
	public P duplicate() {
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
	public abstract P makeAnalog(int statusNid, int pathNid, long time);

	public void setStatusAtPosition(int statusNid, int pathNid, long time) {
		this.statusAtPositionNid = sapBdb.getStatusAtPositionNid(statusNid, pathNid, time);
	}
	
	public void writePartToBdb(TupleOutput output) {
		writeFieldsToBdb(output);
		output.writeInt(statusAtPositionNid);
	}

	protected abstract void writeFieldsToBdb(TupleOutput output);

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
