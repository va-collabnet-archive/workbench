package org.ihtsdo.db.bdb.concept.component.identifier;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.StatusAtPositionBdb;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;

import com.sleepycat.bind.tuple.TupleOutput;

public abstract class IdentifierVersion implements I_IdPart, I_IdVersion {
	
	private static StatusAtPositionBdb sapBdb = Bdb.getStatusAtPositionDb();

	private transient ConceptComponent<?, ?> conceptComponent;
	private int statusAtPositionNid;
	private int authorityNid;
	
	protected IdentifierVersion(int statusNid, int pathNid, long time) {
		this.statusAtPositionNid = sapBdb.getStatusAtPositionNid(statusNid, pathNid, time);
	}

	protected IdentifierVersion(int statusAtPositionNid) {
		super();
		this.statusAtPositionNid = statusAtPositionNid;
	}
	
	public abstract ConceptComponent.IDENTIFIER_PART_TYPES getType();

	public final void writeIdPartToBdb(TupleOutput output) {
		output.writeInt(statusAtPositionNid);
		output.writeInt(authorityNid);
		writeSourceIdToBdb(output);
	}

	protected abstract void writeSourceIdToBdb(TupleOutput output);

	@Override
	public int getAuthorityNid() {
		return authorityNid;
	}

	@Override
	public void setAuthorityNid(int sourceNid) {
		this.authorityNid = sourceNid;
	}

	protected ArrayIntList getVariableVersionNids() {
		ArrayIntList nids = new ArrayIntList(3);
		nids.add(authorityNid);
		return nids;
	}


	@Override
	public I_IdPart duplicateIdPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ArrayIntList getPartComponentNids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPathId() {
		return sapBdb.getPathId(statusAtPositionNid);
	}

	@Override
	public int getStatusId() {
		return sapBdb.getStatusId(statusAtPositionNid);
	}

	@Override
	public long getTime() {
		return sapBdb.getTime(statusAtPositionNid);
	}

	@Override
	public int getVersion() {
		return ThinVersionHelper.convert(getTime());
	}

	@Override
	public I_IdPart makeIdAnalog(int statusNid, int pathNid, long time) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPathId(int pathId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStatusId(int statusId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setVersion(int version) {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_Identify getIdentifier() {
		return conceptComponent;
	}

	@Override
	public I_IdPart getMutableIdPart() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<TimePathId> getTimePathSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UUID> getUUIDs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final I_Identify getFixedIdPart() {
		return conceptComponent;
	}

	@Override
	public int getNid() {
		return conceptComponent.nid;
	}

	public int getStatusAtPositionNid() {
		return statusAtPositionNid;
	}


}
