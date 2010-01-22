package org.ihtsdo.db.bdb.concept.component.relationship;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.util.VersionComputer;
import org.ihtsdo.etypes.ERelationship;
import org.ihtsdo.etypes.ERelationshipVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Relationship extends ConceptComponent<RelationshipRevision, Relationship> 
	implements I_RelVersioned, I_RelPart, I_RelTuple {

	private static class RelTupleComputer extends
			VersionComputer<Relationship, RelationshipRevision> {
	}

	private static RelTupleComputer computer = new RelTupleComputer();

	private int c2Nid;

	private int characteristicNid;
	private int group;
	private int refinabilityNid;
	private int typeNid;

	public Relationship(Concept enclosingConcept, 
			TupleInput input) {
		super(enclosingConcept, 
				input);
	}
	

	public Relationship(ERelationship eRel, Concept enclosingConcept) {
		super(eRel, enclosingConcept);
		c2Nid = Bdb.uuidToNid(eRel.getC2Uuid());
		characteristicNid = Bdb.uuidToNid(eRel.getCharacteristicUuid());
		group = eRel.getRelGroup();
		refinabilityNid = Bdb.uuidToNid(eRel.getRefinabilityUuid());
		typeNid = Bdb.uuidToNid(eRel.getTypeUuid());
		primordialSapNid = Bdb.getStatusAtPositionNid(eRel);
		if (eRel.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<RelationshipRevision>(eRel.getExtraVersionsList().size());
			for (ERelationshipVersion erv: eRel.getExtraVersionsList()) {
				additionalVersions.add(new RelationshipRevision(erv, this));
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("relid: ");
		buf.append(nid);
		buf.append(" c2: ");
		ConceptComponent.addNidToBuffer(buf, c2Nid);
		buf.append(" group: ");
		buf.append(group);
		buf.append(" refinability: ");
		ConceptComponent.addNidToBuffer(buf, refinabilityNid);
		buf.append(" type: ");
		ConceptComponent.addNidToBuffer(buf, typeNid);
		buf.append(" ");
		buf.append(super.toString());
		return buf.toString();
	}

	@Override
	public boolean fieldsEqual(ConceptComponent<RelationshipRevision, Relationship> obj) {
		if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
			Relationship another = (Relationship) obj;
			if (this.c2Nid != another.c2Nid) {
				return false;
			}
			if (this.characteristicNid != another.characteristicNid) {
				return false;
			}
			if (this.group != another.group) {
				return false;
			}
			if (this.refinabilityNid != another.refinabilityNid) {
				return false;
			}
			if (this.typeNid != another.typeNid) {
				return false;
			}
			return conceptComponentFieldsEqual(another);
		}
		return false;
	}


	@Override
	public void readFromBdb(TupleInput input) {
		// nid, list size, and conceptNid are read already by the binder...
		c2Nid = input.readInt();
		characteristicNid = input.readInt();
		group = input.readInt();
		refinabilityNid = input.readInt();
		typeNid = input.readInt();
		int additionalVersionCount = input.readShort();
		if (additionalVersionCount > 0) {
			additionalVersions = new ArrayList<RelationshipRevision>(additionalVersionCount);
			for (int i = 0; i < additionalVersionCount; i++) {
				additionalVersions.add(new RelationshipRevision(input, this));
			}
		}
	}

	@Override
	public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		//
		List<RelationshipRevision> partsToWrite = new ArrayList<RelationshipRevision>();
		if (additionalVersions != null) {
			for (RelationshipRevision p : additionalVersions) {
				if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
					partsToWrite.add(p);
				}
			}
		}
		// Start writing
		// c1Nid is the enclosing concept, does not need to be written. 
		output.writeInt(c2Nid);
		output.writeInt(characteristicNid);
		output.writeInt(group);
		output.writeInt(refinabilityNid);
		output.writeInt(typeNid);
		output.writeShort(partsToWrite.size());
		for (RelationshipRevision p : partsToWrite) {
			p.writePartToBdb(output);
		}
	}


	@Override
	public boolean equals(Object obj) {
		if (Relationship.class.isAssignableFrom(obj.getClass())) {
			Relationship another = (Relationship) obj;
			return nid == another.nid;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] { nid, c2Nid, enclosingConcept.getNid() });
	}


	@Override
	public boolean addRetiredRec(int[] releases, int retiredStatusId) {
		throw new UnsupportedOperationException();
	}

	private void addTuples(I_IntSet allowedStatus, I_Position viewPosition,
			List<RelationshipRevision> matchingTuples) {
		computer.addTuples(allowedStatus, viewPosition, matchingTuples,
				additionalVersions, this);
	}

	public void addTuples(I_IntSet allowedTypes, 
						  List<I_RelTuple> returnRels,
						  boolean addUncommitted, 
						  boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
		addTuples(config.getAllowedStatus(), allowedTypes, config.getViewPositionSetReadOnly(), 
				returnRels, addUncommitted, returnConflictResolvedLatestState);
	}

	public boolean addVersion(I_RelPart part) {
		return additionalVersions.add((RelationshipRevision) part);
	}

	public boolean addPart(RelationshipRevision part) {
		return additionalVersions.add(part);
	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getC1Id() {
		return enclosingConcept.getNid();
	}

	@Override
	public int getC2Id() {
		return c2Nid;
	}

	@Override
	public RelationshipRevision getFirstTuple() {
		return additionalVersions.get(0);
	}

	@Override
	public RelationshipRevision getLastTuple() {
		return additionalVersions.get(additionalVersions.size() - 1);
	}

	@Override
	public int getRelId() {
		return nid;
	}

	@Override
	public List<I_RelTuple> getTuples() {
		List<I_RelTuple> tuples = new ArrayList<I_RelTuple>();
		for (RelationshipRevision p: additionalVersions) {
			tuples.add(p);
		}
		return tuples;
	}

	@Override
	public List<I_RelTuple> getTuples(boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		List<I_RelTuple> tuples = new ArrayList<I_RelTuple>();
		for (RelationshipRevision p : getVersions(returnConflictResolvedLatestState)) {
			tuples.add(p);
		}
		return tuples;
	}

	@Override
	public UniversalAceRelationship getUniversal() throws IOException,
			TerminologyException {
		UniversalAceRelationship universal = new UniversalAceRelationship(
				getEnclosingConcept().getUidsForComponent(nid), 
				enclosingConcept.getUids(), 
				Bdb.getConceptDb().getConcept(c2Nid).getUids(),
				additionalVersions.size());
		for (RelationshipRevision part : additionalVersions) {
			UniversalAceRelationshipPart universalPart = new UniversalAceRelationshipPart();
			universalPart.setPathId(Bdb.getConceptDb().getConcept(part.getPathId()).getUids());
			universalPart.setStatusId(Bdb.getConceptDb().getConcept(part.getStatusId()).getUids());
			universalPart.setCharacteristicId(Bdb.getConceptDb().getConcept(part.getCharacteristicId()).getUids());
			universalPart.setGroup(part.getGroup());
			universalPart.setRefinabilityId(Bdb.getConceptDb().getConcept(part.getRefinabilityId()).getUids());
			universalPart.setTypeId(Bdb.getConceptDb().getConcept(part.getTypeId()).getUids());
			universalPart.setTime(part.getTime());
			universal.addVersion(universalPart);
		}
		return universal;
	}
	
	@Override
	public boolean removeRedundantRecs() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setC2Id(int destNid) {
		this.c2Nid = destNid;
	}

	@Override
	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus)
			throws IOException, TerminologyException {
		int viewPathId = viewPosition.getPath().getConceptId();
		List<RelationshipRevision> matchingTuples = new ArrayList<RelationshipRevision>();
		addTuples(allowedStatus, viewPosition, matchingTuples);
		boolean promotedAnything = false;
		for (I_Path promotionPath : pomotionPaths) {
			for (RelationshipRevision rt : matchingTuples) {
				if (rt.getPathId() == viewPathId) {
					RelationshipRevision promotionPart = 
						rt.makeAnalog(rt.getStatusId(),
									promotionPath.getConceptId(),
									Long.MAX_VALUE);
					rt.getRelVersioned().addVersion(promotionPart);
					promotedAnything = true;
				}
			}
		}
		return promotedAnything;
	}


	@Override
	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positions, List<I_RelTuple> returnRels,
			boolean addUncommitted) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positions, List<I_RelTuple> returnRels,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean addVersionNoRedundancyCheck(I_RelPart rel) {
		throw new UnsupportedOperationException();
	}


	@Override
	public int getCharacteristicId() {
		return characteristicNid;
	}


	@Override
	public int getGroup() {
		return group;
	}


	@Override
	public int getRefinabilityId() {
		return refinabilityNid;
	}


	@Override
	public void setCharacteristicId(int characteristicNid) {
		this.characteristicNid = characteristicNid;
	}


	@Override
	public void setGroup(int group) {
		this.group = group;
	}


	@Override
	public void setRefinabilityId(int refinabilityId) {
		this.refinabilityNid = refinabilityId;
	}


	@Override
	public I_RelVersioned getRelVersioned() {
		return this;
	}

	@Override
	public int getTypeId() {
		return this.typeNid;
	}


	@Override
	public void setTypeId(int typeNid) {
		this.typeNid = typeNid;
	}


	@Override
	public ArrayIntList getVariableVersionNids() {
		ArrayIntList nidList = new ArrayIntList(7);
		nidList.add(enclosingConcept.getNid());
		nidList.add(c2Nid);
		nidList.add(characteristicNid);
		nidList.add(refinabilityNid);
		nidList.add(typeNid);
		return nidList;
	}


	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		return new RelationshipRevision(this, statusNid, pathNid, time, this);
	}

	@Override
	public I_RelPart duplicate() {
		throw new UnsupportedOperationException();
	}
	
	public Relationship getFixedPart() {
		return this;
	}


	@Override
	public Relationship getMutablePart() {
		return this;
	}
}
