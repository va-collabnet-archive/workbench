package org.ihtsdo.db.bdb.concept.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.utypes.UniversalAceIdentification;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVersion;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVersionLong;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVersionString;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVersionUuid;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class ConceptComponent<V extends Version<V, C>, C extends ConceptComponent<V, C>> 
	implements I_AmTermComponent, I_AmPart, I_AmTuple, I_Identify, I_IdPart, I_IdVersion {

	public enum IDENTIFIER_PART_TYPES {
		LONG(1), STRING(2), UUID(3);

		private int partTypeId;

		IDENTIFIER_PART_TYPES(int partTypeId) {
			this.partTypeId = partTypeId;
		}

		public void writeType(TupleOutput output) {
			output.writeByte(partTypeId);
		}

		public static IDENTIFIER_PART_TYPES readType(TupleInput input) {
			switch (input.readByte()) {
			case 1:
				return LONG;
			case 2:
				return STRING;
			case 3:
				return UUID;
			}
			throw new UnsupportedOperationException();
		}
	};


	
	public int nid;
	public boolean editable;
	/**
	 * priámorádiáal:  first created or developed
	 * 
	 */
	public int primordialStatusAtPositionNid;
	/**
	 * priámorádiáal:  first created or developed
	 * 
	 */
	public long primordialUuidMsb;
	/**
	 * priámorádiáal:  first created or developed
	 * 
	 */
	public long primordialUuidLsb;
	
	public ArrayList<V> mutableComponentParts;
	public ArrayList<IdentifierVersion> identifierParts;
	
	protected ConceptComponent(int nid, int listSize, boolean editable) {
		super();
		this.nid = nid;
		this.editable = editable;
		this.mutableComponentParts = new ArrayList<V>(listSize);
	}
	
	public void readIdentifierFromBdb(TupleInput input, int conceptNid, int listSize) {
		// nid, list size, and conceptNid are read already by the binder...
		for (int i = 0; i < listSize; i++) {
			switch (IDENTIFIER_PART_TYPES.readType(input)) {
			case LONG:
				identifierParts.add(new IdentifierVersionLong(input));
				break;
			case STRING:
				identifierParts.add(new IdentifierVersionString(input));
				break;
			case UUID:
				identifierParts.add(new IdentifierVersionUuid(input));
				break;
				default:
					throw new UnsupportedOperationException();
			}
		}
	}

	public void writeIdentifierToBdb(TupleOutput output,
			int maxReadOnlyStatusAtPositionNid) {
		List<IdentifierVersion> partsToWrite = new ArrayList<IdentifierVersion>();
		for (IdentifierVersion p: identifierParts) {
			if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
				partsToWrite.add(p);
			}
		}
		// Start writing
		output.writeInt(nid);
		output.writeShort(partsToWrite.size());
		for (IdentifierVersion p: partsToWrite) {
			p.getType().writeType(output);
			p.writePartToBdb(output);
		}
	}
	

	@Override
	public boolean addMutableIdPart(I_IdPart srcId) {
		return identifierParts.add((IdentifierVersion) srcId);
	}

	@Override
	public final List<I_IdVersion> getIdVersions() {
		List<I_IdVersion> returnValues = new ArrayList<I_IdVersion>();
		for (IdentifierVersion idv : identifierParts) {
			returnValues.add(idv);
		}
		return returnValues;
	}


	@Override
	public final int getAuthorityNid() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public final Object getDenotation() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public final void setAuthorityNid(int sourceNid) {
		throw new UnsupportedOperationException();
	}


	@Override
	public final void setDenotation(Object sourceId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final I_Identify getIdentifier() {
		return this;
	}
	
	@Override
	public final List<UUID> getUUIDs() {
		List<UUID> returnValues = new ArrayList<UUID>();
		for (IdentifierVersion idv : identifierParts) {
			if (IdentifierVersionUuid.class.isAssignableFrom(idv
					.getClass())) {
				IdentifierVersionUuid uuidPart = (IdentifierVersionUuid) idv;
				returnValues.add(uuidPart.getUuid());
			}
		}
		return returnValues;
	}

	@Override
	public UniversalAceIdentification getUniversalId() throws IOException,
			TerminologyException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasMutableIdPart(I_IdPart newPart) {
		return identifierParts.contains(newPart);
	}


	public final boolean addMutablePart(V part) {
		return mutableComponentParts.add(part);
	}

	public final List<V> getMutableParts() {
		if (editable) {
			return mutableComponentParts;
		}
		return Collections.unmodifiableList(mutableComponentParts);
	}
	

	public final List<V> getMutableParts(boolean returnConflictResolvedLatestState) throws TerminologyException, IOException {
		if (returnConflictResolvedLatestState) {
	        List<V> returnList = new ArrayList<V>(mutableComponentParts.size());
	        if (returnConflictResolvedLatestState) {
	            I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
	            returnList = config.getConflictResolutionStrategy().resolveParts(returnList);
	        }
	        return returnList;
		}
		return getMutableParts();
	}

	
	public final int getMutablePartCount() {
		return mutableComponentParts.size();
	}

	public final int getNid() {
		return nid;
	}

	public abstract void readComponentFromBdb(TupleInput input, int conceptNid, int listSize);
	
	public abstract void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid);

	
	/*
	 * Below methods have confusing naming, and should be considered for deprecation...
	 */
	public final List<? extends I_IdVersion> getMutableIdParts() {
		return identifierParts;
	}

	public final boolean addVersion(V newPart) {
		if (editable) {
			return mutableComponentParts.add(newPart);
		}
		throw new RuntimeException("versions is not editable");
	}
	
	public final boolean addVersionNoRedundancyCheck(V newPart) {
		return mutableComponentParts.add(newPart);
	}
	
	public final boolean hasVersion(V p) {
		return mutableComponentParts.contains(p);
	}
	
	public final List<? extends V> getVersions(boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		return getMutableParts(returnConflictResolvedLatestState);
	}

	public final int versionCount() {
		return mutableComponentParts.size();
	}
	
	public final Set<TimePathId> getTimePathSet() {
		Set<TimePathId> set = new TreeSet<TimePathId>();
		for (V p : mutableComponentParts) {
			set.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return set;
	}

	@Override
	public I_Identify getFixedPart() {
		return this;
	}

	public Object getDenotation(int authorityNid) throws IOException,
			TerminologyException {
		return data.getDenotation(authorityNid);
	}

	@Override
	public final int getPathId() {
		return Bdb.getStatusAtPositionDb().getPathId(primordialStatusAtPositionNid);
	}

	@Override
	public final int getStatusId() {
		return Bdb.getStatusAtPositionDb().getStatusId(primordialStatusAtPositionNid);
	}

	@Override
	public final long getTime() {
		return Bdb.getStatusAtPositionDb().getTime(primordialStatusAtPositionNid);
	}

	@Override
	public final int getVersion() {
		return ThinVersionHelper.convert(getTime());
	}

	@Override
	public final void setPathId(int pathId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void setStatusId(int statusId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void setVersion(int version) {
		throw new UnsupportedOperationException();
	}
	
	public final ArrayIntList getPartComponentNids() {
		ArrayIntList resultList = getVariableVersionNids();
		resultList.add(getPathId());
		resultList.add(getStatusId());
		return resultList;
	}
	
	protected abstract ArrayIntList getVariableVersionNids();

	@Override
	public final I_Identify getFixedIdPart() {
		return this;
	}

	@Override
	public final I_IdPart getMutableIdPart() {
		return this;
	}

	@Override
	public final I_IdPart duplicateIdPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final I_IdPart makeIdAnalog(int statusNid, int pathNid, long time) {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_AmPart duplicate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		// TODO Auto-generated method stub
		return null;
	}

}
