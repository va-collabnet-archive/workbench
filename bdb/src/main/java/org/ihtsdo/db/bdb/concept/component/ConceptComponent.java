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
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.ConceptBdb;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVersion;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVersionLong;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVersionString;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVersionUuid;
import org.ihtsdo.etypes.EComponent;
import org.ihtsdo.etypes.EIdentifierVersion;
import org.ihtsdo.etypes.EIdentifierVersionLong;
import org.ihtsdo.etypes.EIdentifierVersionString;
import org.ihtsdo.etypes.EIdentifierVersionUuid;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class ConceptComponent<V extends Version<V, C>, 
									   C extends ConceptComponent<V, C>> 
	implements I_AmTermComponent, I_AmPart, I_AmTuple, I_Identify, I_IdPart, I_IdVersion,
	I_HandleFutureStatusAtPositionSetup {
	
	private static ConceptBdb conceptBdb = Bdb.getConceptDb();
	
	private static List<UUID> getUuids(int conceptNid) throws IOException {
		return conceptBdb.getUuidsForConcept(conceptNid);
	}

	public enum IDENTIFIER_PART_TYPES {
		LONG(1), STRING(2), UUID(3);

		private int partTypeId;

		IDENTIFIER_PART_TYPES(int partTypeId) {
			this.partTypeId = partTypeId;
		}

		public static IDENTIFIER_PART_TYPES getType(Class<?> denotationClass) {
			if (UUID.class.isAssignableFrom(denotationClass)) {
				return UUID;
			} else if (Long.class.isAssignableFrom(denotationClass)) {
				return LONG;
			} else if (String.class.isAssignableFrom(denotationClass)) {
				return STRING;
			} 
			throw new UnsupportedOperationException(denotationClass.toString());
		}

		public void writeType(TupleOutput output) {
			output.writeByte(partTypeId);
		}
		
		public static IDENTIFIER_PART_TYPES readType(TupleInput input) {
			int partTypeId = input.readByte();
			switch (partTypeId) {
			case 1:
				return LONG;
			case 2:
				return STRING;
			case 3:
				return UUID;
			}
			throw new UnsupportedOperationException("partTypeId: " + partTypeId);
		}
	};


	
	public int nid;
	
	public Concept enclosingConcept;

	/**
	 * priámorádiáal:  first created or developed
	 * 
	 */
	public int primordialStatusAtPositionNid = Integer.MAX_VALUE;
	/**
	 * priámorádiáal:  first created or developed
	 * 
	 */
	public long primordialUuidMsb = 0;
	/**
	 * priámorádiáal:  first created or developed
	 * 
	 */
	public long primordialUuidLsb = 0;
	
	public ArrayList<V> additionalVersions;
	private ArrayList<IdentifierVersion> additionalIdentifierParts;
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("primordialUuidMsb: ");
		buf.append(primordialUuidMsb);
		buf.append(" primordialUuidLsb: ");
		buf.append(primordialUuidLsb);
		buf.append(" additionalIdParts: " + additionalIdentifierParts);
		buf.append(" additionalVersions: " + additionalVersions);
		return buf.toString();
		
	}
	
	protected ConceptComponent(Concept enclosingConcept, TupleInput input) {
		super();
		assert enclosingConcept != null;
		this.enclosingConcept = enclosingConcept;
		readComponentFromBdb(input);
		if (this.primordialUuidMsb == 0) {
			System.out.println("bad id"); // TODO remove me...
		}
		assert this.primordialUuidMsb != 0 : "Processing nid: " + enclosingConcept.getNid();
		assert this.primordialUuidLsb != 0: "Processing nid: " + enclosingConcept.getNid();
		assert nid != Integer.MAX_VALUE: "Processing nid: " + enclosingConcept.getNid();
		assert nid != Integer.MIN_VALUE: "Processing nid: " + enclosingConcept.getNid();
	}
	
	//TODO move the EComponent constructors to a helper class or factory class...
	// So that the size of this class is kept limited. 
	protected ConceptComponent(EComponent<?> eComponent,
			Concept enclosingConcept) {
		super();
		assert enclosingConcept != null;
		this.nid = Bdb.uuidToNid(eComponent.primordialComponentUuid);
		assert this.nid != Integer.MAX_VALUE: "Processing nid: " + enclosingConcept.getNid();
		this.enclosingConcept = enclosingConcept;
		this.primordialStatusAtPositionNid = Bdb.getStatusAtPositionNid(eComponent);
		if (eComponent.getVersionCount() > 1) {
			this.additionalVersions = new ArrayList<V>(eComponent.getVersionCount() - 1);
		}
		this.primordialUuidMsb = eComponent.getPrimordialComponentUuid().getMostSignificantBits();
		this.primordialUuidLsb = eComponent.getPrimordialComponentUuid().getLeastSignificantBits();
		convertId(eComponent.additionalIdComponents);
		assert this.primordialUuidMsb != 0: "Processing nid: " + enclosingConcept.getNid();
		assert this.primordialUuidLsb != 0: "Processing nid: " + enclosingConcept.getNid();
		assert nid != Integer.MAX_VALUE: "Processing nid: " + enclosingConcept.getNid();
		assert nid != Integer.MIN_VALUE: "Processing nid: " + enclosingConcept.getNid();
	}
	
	public void convertId(List<EIdentifierVersion> list)  {
		if (list == null || list.size() == 0) {
			return;
		}
		additionalIdentifierParts = new ArrayList<IdentifierVersion>(list.size());
		for (EIdentifierVersion idv: list) {
			Object denotation = idv.getDenotation();
			switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
			case LONG:
				additionalIdentifierParts.add(new IdentifierVersionLong((EIdentifierVersionLong)idv));
				break;
			case STRING:
				additionalIdentifierParts.add(new IdentifierVersionString((EIdentifierVersionString)idv));
				break;
			case UUID:
				additionalIdentifierParts.add(new IdentifierVersionUuid((EIdentifierVersionUuid)idv));
				
				break;
			default:
				throw new UnsupportedOperationException();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup#isSetup()
	 */
	public boolean isSetup() {
		assert primordialUuidMsb != 0 && primordialUuidLsb != 0;
		return primordialStatusAtPositionNid != Integer.MAX_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup#setStatusAtPositionNid(int)
	 */
	public void setStatusAtPositionNid(int sapNid) {
		this.primordialStatusAtPositionNid = sapNid;
	}
	
	private void readIdentifierFromBdb(TupleInput input) {
		// nid, list size, and conceptNid are read already by the binder...
		primordialUuidMsb = input.readLong();
		primordialUuidLsb = input.readLong();
		int listSize = input.readShort();
		if (listSize != 0) {
			additionalIdentifierParts = new ArrayList<IdentifierVersion>(listSize);
		}
		for (int i = 0; i < listSize; i++) {
			switch (IDENTIFIER_PART_TYPES.readType(input)) {
			case LONG:
				additionalIdentifierParts.add(new IdentifierVersionLong(input));
				break;
			case STRING:
				IdentifierVersionString idv = new IdentifierVersionString(input);
				additionalIdentifierParts.add(idv);
				break;
			case UUID:
				additionalIdentifierParts.add(new IdentifierVersionUuid(input));
				break;
				default:
					throw new UnsupportedOperationException();
			}
		}
	}

	private final void writeIdentifierToBdb(TupleOutput output,
			int maxReadOnlyStatusAtPositionNid) {
		assert primordialStatusAtPositionNid != Integer.MAX_VALUE: "Processing nid: " + enclosingConcept.getNid();
		assert primordialUuidMsb != 0: "Processing nid: " + enclosingConcept.getNid();
		assert primordialUuidLsb != 0: "Processing nid: " + enclosingConcept.getNid();
		output.writeLong(primordialUuidMsb);
		output.writeLong(primordialUuidLsb);
		List<IdentifierVersion> partsToWrite = new ArrayList<IdentifierVersion>();
		if (additionalIdentifierParts != null) {
			for (IdentifierVersion p: additionalIdentifierParts) {
				if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
					partsToWrite.add(p);
				}
			}
		}
		// Start writing
		
		output.writeShort(partsToWrite.size());
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
		if (additionalIdentifierParts == null) {
			additionalIdentifierParts = new ArrayList<IdentifierVersion>();
		}
		return additionalIdentifierParts.add(srcId);
	}

	@Override
	public final List<I_IdVersion> getIdVersions() {
		List<I_IdVersion> returnValues = new ArrayList<I_IdVersion>();
		returnValues.addAll(additionalIdentifierParts);
		returnValues.add(this);
		return Collections.unmodifiableList(returnValues);
	}


	@Override
	public final int getAuthorityNid() {
		try {
			return ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (TerminologyException e) {
			throw new RuntimeException(e);
		}
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
		returnValues.add(new UUID(primordialUuidMsb, primordialUuidLsb));
		for (IdentifierVersion idv : additionalIdentifierParts) {
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
		if (additionalIdentifierParts == null) {
			universal = new UniversalAceIdentification(1);
		} else {
			universal = new UniversalAceIdentification(additionalIdentifierParts.size() + 1);
		}
        UniversalAceIdentificationPart universalPart = new UniversalAceIdentificationPart();
        universalPart.setIdStatus(getUuids(getStatusId()));
        universalPart.setPathId(getUuids(getPathId()));
        universalPart.setSource(getUuids(getAuthorityNid()));
        universalPart.setSourceId(getDenotation());
        universalPart.setTime(getTime());
        universal.addVersion(universalPart);
        if (additionalIdentifierParts != null) {
            for (IdentifierVersion part : additionalIdentifierParts) {
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
		return additionalIdentifierParts.contains(newPart);
	}


	public final boolean addMutablePart(V version) {
		return addVersion(version);
	}

	public final List<V> getMutableParts() {
		if (enclosingConcept.isEditable()) {
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

	public final void readComponentFromBdb(TupleInput input) {
		nid = input.readInt();
		primordialStatusAtPositionNid = input.readInt();
		readIdentifierFromBdb(input);
		readFromBdb(input);
	}
	
	public final void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		output.writeInt(nid);
		output.writeInt(primordialStatusAtPositionNid);
		writeIdentifierToBdb(output, maxReadOnlyStatusAtPositionNid);
		writeToBdb(output, maxReadOnlyStatusAtPositionNid);
	}

	public abstract void readFromBdb(TupleInput input);
	
	public abstract void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid);
	
	/*
	 * Below methods have confusing naming, and should be considered for deprecation...
	 */
	public final List<? extends I_IdVersion> getMutableIdParts() {
		return additionalIdentifierParts;
	}

	public final boolean addVersion(V newPart) {
		if (enclosingConcept.isEditable()) {
			return additionalVersions.add(newPart);
		}
		throw new RuntimeException("versions is not editable");
	}
	
	public final boolean addVersionNoRedundancyCheck(V newPart) {
		if (enclosingConcept.isEditable()) {
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

	protected int getPrimordialStatusAtPositionNid() {
		return primordialStatusAtPositionNid;
	}

	protected Concept getEnclosingConcept() {
		return enclosingConcept;
	}
	
	
	
	public abstract boolean fieldsEqual(ConceptComponent<V, C> another);
	
	public boolean conceptComponentFieldsEqual(ConceptComponent<V, C> another) { 
		if (this.nid != another.nid) {
			return false;
		}
		if (this.primordialStatusAtPositionNid != another.primordialStatusAtPositionNid) {
			return false;
		}
		if (this.primordialUuidLsb != another.primordialUuidLsb) {
			return false;
		}
		if (this.primordialUuidMsb != another.primordialUuidMsb) {
			return false;
		}
		if (this.additionalIdentifierParts != null && another.additionalIdentifierParts == null) {
			return false;
		}
		if (this.additionalIdentifierParts == null && another.additionalIdentifierParts != null) {
			return false;
		}
		if (this.additionalIdentifierParts != null) {
			if (this.additionalIdentifierParts.equals(another.additionalIdentifierParts) == false) {
				return false;
			}
		}
		if (this.additionalVersions != null && another.additionalVersions == null) {
			return false;
		}
		if (this.additionalVersions == null && another.additionalVersions != null) {
			return false;
		}
		if (this.additionalVersions != null) {
			if (this.additionalVersions.equals(another.additionalVersions) == false) {
				return false;
			}
		}
			
		
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		return fieldsEqual((ConceptComponent<V, C>) obj);
	}

	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] { nid, primordialStatusAtPositionNid });
	}
}
