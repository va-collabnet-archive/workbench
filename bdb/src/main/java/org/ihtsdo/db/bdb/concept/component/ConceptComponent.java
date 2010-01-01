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
import org.dwfa.ace.utypes.UniversalAceIdentificationPart;
import org.dwfa.cement.PrimordialId;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.ConceptBdb;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVersion;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVersionLong;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVersionString;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVersionUuid;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class ConceptComponent<V extends Version<V, C>, C extends ConceptComponent<V, C>> 
	implements I_AmTermComponent, I_AmPart, I_AmTuple, I_Identify, I_IdPart, I_IdVersion {
	
	private static ConceptBdb conceptBdb = Bdb.getConceptDb();
	
	private static List<UUID> getUuids(int conceptNid) throws IOException {
		return conceptBdb.getUuidsForConcept(conceptNid);
	}

	public enum IDENTIFIER_PART_TYPES {
		LONG(1), STRING(2), UUID(3), PRIMORDIAL(4);

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
			case 4:
				return PRIMORDIAL;
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
	
	public ArrayList<V> additionalVersions;
	private ArrayList<IdentifierVersion> identifierParts;
	
	protected ConceptComponent(int nid, int versionCount, boolean editable) {
		super();
		this.nid = nid;
		this.editable = editable;
		this.additionalVersions = new ArrayList<V>(versionCount - 1);
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
			case PRIMORDIAL:
				primordialUuidMsb = input.readLong();
				primordialUuidLsb = input.readLong();
				break;
				default:
					throw new UnsupportedOperationException();
			}
		}
	}

	public void writeIdentifierToBdb(TupleOutput output,
			int maxReadOnlyStatusAtPositionNid) {
		List<IdentifierVersion> partsToWrite = new ArrayList<IdentifierVersion>();
		if (identifierParts != null) {
			for (IdentifierVersion p: identifierParts) {
				if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
					partsToWrite.add(p);
				}
			}
		}
		// Start writing
		output.writeInt(nid);
		if (primordialStatusAtPositionNid > maxReadOnlyStatusAtPositionNid) {
			output.writeShort(partsToWrite.size() + 1);
			IDENTIFIER_PART_TYPES.PRIMORDIAL.writeType(output);
			output.writeLong(primordialUuidMsb);
			output.writeLong(primordialUuidLsb);
		} else {
			output.writeShort(partsToWrite.size());
		}
		for (IdentifierVersion p: partsToWrite) {
			p.getType().writeType(output);
			p.writeIdPartToBdb(output);
		}
	}
	
	@Override
	public boolean addMutableIdPart(I_IdPart srcId) {
		return addIdVersion((IdentifierVersion) srcId);
	}
	public boolean addIdVersion(IdentifierVersion srcId) {
		if (identifierParts == null) {
			identifierParts = new ArrayList<IdentifierVersion>();
		}
		return identifierParts.add(srcId);
	}

	@Override
	public final List<I_IdVersion> getIdVersions() {
		List<I_IdVersion> returnValues = new ArrayList<I_IdVersion>();
		returnValues.addAll(identifierParts);
		returnValues.add(this);
		return Collections.unmodifiableList(returnValues);
	}


	@Override
	public final int getAuthorityNid() {
		return PrimordialId.PRIMORDIAL_UUID.getNativeId(Integer.MIN_VALUE);
	}


	@Override
	public final Object getDenotation() {
		return new UUID(primordialUuidMsb, primordialUuidLsb);
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
        UniversalAceIdentification universal = new UniversalAceIdentification(1);
		if (identifierParts == null) {
			universal = new UniversalAceIdentification(1);
		} else {
			universal = new UniversalAceIdentification(identifierParts.size() + 1);
		}
        UniversalAceIdentificationPart universalPart = new UniversalAceIdentificationPart();
        universalPart.setIdStatus(getUuids(getStatusId()));
        universalPart.setPathId(getUuids(getPathId()));
        universalPart.setSource(getUuids(getAuthorityNid()));
        universalPart.setSourceId(getDenotation());
        universalPart.setTime(getTime());
        universal.addVersion(universalPart);
        if (identifierParts != null) {
            for (IdentifierVersion part : identifierParts) {
                universalPart = new UniversalAceIdentificationPart();
                universalPart.setIdStatus(getUuids(part.getStatusId()));
                universalPart.setPathId(getUuids(part.getPathId()));
                universalPart.setSource(getUuids(part.getAuthorityNid()));
                universalPart.setSourceId(part.getDenotation());
                universalPart.setTime(part.getTime());
                universal.addVersion(universalPart);
            }
        }
        return universal;
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


	public final boolean addMutablePart(V version) {
		return addVersion(version);
	}

	public final List<V> getMutableParts() {
		if (editable) {
			return additionalVersions;
		}
		return Collections.unmodifiableList(additionalVersions);
	}
	

	public final List<V> getMutableParts(boolean returnConflictResolvedLatestState) throws TerminologyException, IOException {
		if (returnConflictResolvedLatestState) {
	        List<V> returnList = new ArrayList<V>(additionalVersions.size());
	        if (returnConflictResolvedLatestState) {
	            I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
	            returnList = config.getConflictResolutionStrategy().resolveParts(returnList);
	        }
	        return returnList;
		}
		return getMutableParts();
	}

	
	public final int getMutablePartCount() {
		return additionalVersions.size();
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
			return additionalVersions.add(newPart);
		}
		throw new RuntimeException("versions is not editable");
	}
	
	public final boolean addVersionNoRedundancyCheck(V newPart) {
		if (editable) {
			return additionalVersions.add(newPart);
		}
		throw new RuntimeException("versions is not editable");
	}
	
	public final boolean hasVersion(V version) {
		return additionalVersions.contains(version);
	}
	
	public final List<? extends V> getVersions(boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		return getMutableParts(returnConflictResolvedLatestState);
	}

	public final int versionCount() {
		if (additionalVersions == null) {
			return 1;
		}
		return additionalVersions.size() + 1;
	}
	
	public final Set<TimePathId> getTimePathSet() {
		Set<TimePathId> set = new TreeSet<TimePathId>();
		set.add(new TimePathId(getVersion(), getPathId()));
		if (additionalVersions != null) {
			for (V p : additionalVersions) {
				set.add(new TimePathId(p.getVersion(), p.getPathId()));
			}
		}
		return set;
	}

	@Override
	public I_Identify getFixedPart() {
		return this;
	}

	public Object getDenotation(int authorityNid) throws IOException,
			TerminologyException {
		if (getAuthorityNid() == authorityNid) {
			return new UUID(primordialUuidMsb, primordialUuidLsb);
		}
		for (I_IdPart id: getMutableIdParts()) {
			if (id.getAuthorityNid() == authorityNid) {
				return id.getDenotation();
			}
		}
		return null;
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
		throw new UnsupportedOperationException();
	}
}
