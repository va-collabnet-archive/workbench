package org.ihtsdo.db.bdb.concept.component.relationship;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.util.TupleComputer;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Relationship extends ConceptComponent<RelationshipPart> 
	implements I_RelVersioned<RelationshipPart, Relationship, RelationshipTuple> {

	private static class RelTupleComputer extends
			TupleComputer<RelationshipTuple, Relationship, RelationshipPart> {

		public RelationshipTuple makeTuple(RelationshipPart part, Relationship core) {
			return new RelationshipTuple(core, part);
		}
	}

	private static RelTupleComputer computer = new RelTupleComputer();

	private int c1Nid;
	private int c2Nid;

	public Relationship(int nid, int parts,
			boolean editable) {
		super(nid, parts, editable);
	}
	

	@Override
	public void readComponentFromBdb(TupleInput input, int conceptNid) {
		this.c1Nid = conceptNid;
		this.c2Nid = input.readInt();
		this.editable = input.readBoolean();
	}

	@Override
	public void readPartFromBdb(TupleInput input) {
		versions.add(new RelationshipPart(input));
	}

	@Override
	public void writeComponentToBdb(TupleOutput output) {
		output.writeInt(c2Nid);
		output.writeBoolean(editable);
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

	public void addTuples(I_IntSet allowedStatus, I_Position viewPosition,
			List<RelationshipTuple> matchingTuples) {
		computer.addTuples(allowedStatus, viewPosition, matchingTuples,
				versions, this);
	}

	@Override
	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<RelationshipTuple> returnRels,
			boolean addUncommitted) {
		computer.addTuples(allowedStatus, allowedTypes, 
				positions, returnRels, 
				addUncommitted, versions, this);
	}

	@Override
	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<RelationshipTuple> returnRels,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
	    List<RelationshipTuple> tuples = new ArrayList<RelationshipTuple>();
	    
	    addTuples(allowedStatus, allowedTypes, positions, tuples, addUncommitted);
		
		if (returnConflictResolvedLatestState) {
		    I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
			I_ManageConflict conflictResolutionStrategy;
			if (config == null) {
				conflictResolutionStrategy = new IdentifyAllConflictStrategy();
			} else {
				conflictResolutionStrategy = config.getConflictResolutionStrategy();
			}
			
			tuples = conflictResolutionStrategy.resolveTuples(tuples);
		}
		
		returnRels.addAll(tuples);
	}

	@Override
	public void addTuples(I_IntSet allowedTypes, List<RelationshipTuple> returnRels,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
		addTuples(config.getAllowedStatus(), allowedTypes, config.getViewPositionSetReadOnly(), 
				returnRels, addUncommitted, returnConflictResolvedLatestState);
	}

	@Override
	public boolean addVersion(RelationshipPart part) {
		return versions.add(part);
	}

	@Override
	public boolean addVersionNoRedundancyCheck(RelationshipPart part) {
		return versions.add(part);
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
	public RelationshipTuple getFirstTuple() {
		return new RelationshipTuple(this, versions.get(0));
	}

	@Override
	public RelationshipTuple getLastTuple() {
		return new RelationshipTuple(this, versions.get(versions.size() - 1));
	}

	@Override
	public int getRelId() {
		return nid;
	}

	@Override
	public Set<TimePathId> getTimePathSet() {
		Set<TimePathId> tpSet = new HashSet<TimePathId>();
		for (RelationshipPart p : versions) {
			tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return tpSet;
	}

	@Override
	public List<RelationshipTuple> getTuples() {
		List<RelationshipTuple> tuples = new ArrayList<RelationshipTuple>();
		for (RelationshipPart p: versions) {
			tuples.add(new RelationshipTuple(this, p));
		}
		return tuples;
	}

	@Override
	public List<RelationshipTuple> getTuples(boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		List<RelationshipTuple> tuples = new ArrayList<RelationshipTuple>();
		for (RelationshipPart p : getVersions(returnConflictResolvedLatestState)) {
			tuples.add(new RelationshipTuple(this, p));
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
				versions.size());
		for (RelationshipPart part : versions) {
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
	public List<RelationshipPart> getVersions(
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		List<RelationshipPart> returnList = versions; 
		  
		if (returnConflictResolvedLatestState) {
			I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
			returnList = config.getConflictResolutionStrategy().resolveParts(returnList);
		}
   
		return returnList;
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
		List<RelationshipTuple> matchingTuples = new ArrayList<RelationshipTuple>();
		addTuples(allowedStatus, viewPosition, matchingTuples);
		boolean promotedAnything = false;
		for (I_Path promotionPath : pomotionPaths) {
			for (RelationshipTuple rt : matchingTuples) {
				if (rt.getPathId() == viewPathId) {
					RelationshipPart promotionPart = rt.getPart()
							.makeAnalog(rt.getStatusId(),
									promotionPath.getConceptId(),
									Long.MAX_VALUE);
					rt.getRelVersioned().addVersion(promotionPart);
					promotedAnything = true;
				}
			}
		}
		return promotedAnything;
	}
}
