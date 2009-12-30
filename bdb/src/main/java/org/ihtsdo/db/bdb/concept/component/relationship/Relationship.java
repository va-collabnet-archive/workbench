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
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.util.VersionComputer;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Relationship extends ConceptComponent<RelationshipVersion, Relationship> 
	implements I_RelVersioned, I_RelPart, I_RelTuple {

	private static class RelTupleComputer extends
			VersionComputer<Relationship, RelationshipVersion> {
	}

	private static RelTupleComputer computer = new RelTupleComputer();

	private int c1Nid;
	private int c2Nid;

	private int characteristicNid;
	private int group;
	private int refinabilityNid;
	private int typeNid;

	public Relationship(int nid, int parts,
			boolean editable) {
		super(nid, parts, editable);
	}
	

	public Relationship(UniversalAceRelationship uRel, boolean editable) {
		super(Bdb.uuidsToNid(uRel.getRelId()), uRel.getVersions().size(), editable);
		c1Nid = Bdb.uuidsToNid(uRel.getC1Id());
		c2Nid = Bdb.uuidsToNid(uRel.getC2Id());
		for (UniversalAceRelationshipPart uPart: uRel.getVersions()) {
			mutableComponentParts.add(new RelationshipVersion(uPart));
		}
	}


	@Override
	public void readComponentFromBdb(TupleInput input, int conceptNid, int listSize) {
		// nid, list size, and conceptNid are read already by the binder...
		this.c1Nid = conceptNid;
		this.c2Nid = input.readInt();
		for (int i = 0; i < listSize; i++) {
			mutableComponentParts.add(new RelationshipVersion(input));
		}
	}

	@Override
	public void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		//
		List<RelationshipVersion> partsToWrite = new ArrayList<RelationshipVersion>();
		for (RelationshipVersion p : mutableComponentParts) {
			if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
				partsToWrite.add(p);
			}
		}
		// Start writing
		output.writeInt(nid);
		output.writeShort(partsToWrite.size());
		// c1Nid is the enclosing concept, does not need to be written. 
		output.writeInt(c2Nid);
		for (RelationshipVersion p : partsToWrite) {
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
		return HashFunction.hashCode(new int[] { nid, c2Nid, c1Nid });
	}


	@Override
	public boolean addRetiredRec(int[] releases, int retiredStatusId) {
		throw new UnsupportedOperationException();
	}

	private void addTuples(I_IntSet allowedStatus, I_Position viewPosition,
			List<RelationshipVersion> matchingTuples) {
		computer.addTuples(allowedStatus, viewPosition, matchingTuples,
				mutableComponentParts, this);
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
		return mutableComponentParts.add((RelationshipVersion) part);
	}

	public boolean addPart(RelationshipVersion part) {
		return mutableComponentParts.add(part);
	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getC1Id() {
		return c1Nid;
	}

	@Override
	public int getC2Id() {
		return c2Nid;
	}

	@Override
	public RelationshipVersion getFirstTuple() {
		return mutableComponentParts.get(0);
	}

	@Override
	public RelationshipVersion getLastTuple() {
		return mutableComponentParts.get(mutableComponentParts.size() - 1);
	}

	@Override
	public int getRelId() {
		return nid;
	}

	@Override
	public List<I_RelTuple> getTuples() {
		List<I_RelTuple> tuples = new ArrayList<I_RelTuple>();
		for (RelationshipVersion p: mutableComponentParts) {
			tuples.add(p);
		}
		return tuples;
	}

	@Override
	public List<I_RelTuple> getTuples(boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		List<I_RelTuple> tuples = new ArrayList<I_RelTuple>();
		for (RelationshipVersion p : getVersions(returnConflictResolvedLatestState)) {
			tuples.add(p);
		}
		return tuples;
	}

	@Override
	public UniversalAceRelationship getUniversal() throws IOException,
			TerminologyException {
		UniversalAceRelationship universal = new UniversalAceRelationship(
				Bdb.getConceptDb().getConcept(c1Nid).getUidsForComponent(nid), 
				Bdb.getConceptDb().getConcept(c1Nid).getUids(), 
				Bdb.getConceptDb().getConcept(c2Nid).getUids(),
				mutableComponentParts.size());
		for (RelationshipVersion part : mutableComponentParts) {
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
		List<RelationshipVersion> matchingTuples = new ArrayList<RelationshipVersion>();
		addTuples(allowedStatus, viewPosition, matchingTuples);
		boolean promotedAnything = false;
		for (I_Path promotionPath : pomotionPaths) {
			for (RelationshipVersion rt : matchingTuples) {
				if (rt.getPathId() == viewPathId) {
					RelationshipVersion promotionPart = 
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
		nidList.add(c1Nid);
		nidList.add(c2Nid);
		nidList.add(characteristicNid);
		nidList.add(refinabilityNid);
		nidList.add(typeNid);
		return nidList;
	}


	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		return new RelationshipVersion(this, statusNid, pathNid, time);
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
