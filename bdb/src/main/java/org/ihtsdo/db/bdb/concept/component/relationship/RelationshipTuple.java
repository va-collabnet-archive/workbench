package org.ihtsdo.db.bdb.concept.component.relationship;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.Tuple;

public class RelationshipTuple extends Tuple<RelationshipPart, Relationship> 
	implements I_RelTuple<RelationshipPart, RelationshipTuple, Relationship> {

	private Relationship fixed;
	
	private RelationshipPart part;
	
	public RelationshipTuple(Relationship fixed, RelationshipPart part) {
		super();
		this.fixed = fixed;
		this.part = part;
	}

	public boolean addRetiredRec(int[] releases, int retiredStatusId) {
		return fixed.addRetiredRec(releases, retiredStatusId);
	}

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<RelationshipTuple> returnRels,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		fixed.addTuples(allowedStatus, allowedTypes, positions, returnRels,
				addUncommitted, returnConflictResolvedLatestState);
	}

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<RelationshipTuple> returnRels,
			boolean addUncommitted) {
		fixed.addTuples(allowedStatus, allowedTypes, positions, returnRels,
				addUncommitted);
	}

	public void addTuples(I_IntSet allowedTypes, List<RelationshipTuple> returnRels,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		fixed.addTuples(allowedTypes, returnRels, addUncommitted,
				returnConflictResolvedLatestState);
	}

	public <T extends RelationshipPart> boolean addVersion(T rel) {
		return fixed.addVersion(rel);
	}

	public <T extends RelationshipPart> boolean addVersionNoRedundancyCheck(T rel) {
		return fixed.addVersionNoRedundancyCheck(rel);
	}

	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		fixed.convertIds(jarToDbNativeMap);
	}

	public int getC1Id() {
		return fixed.getC1Id();
	}

	public int getC2Id() {
		return fixed.getC2Id();
	}

	public RelationshipTuple getFirstTuple() {
		return fixed.getFirstTuple();
	}

	public RelationshipTuple getLastTuple() {
		return fixed.getLastTuple();
	}

	public int getNid() {
		return fixed.getNid();
	}

	public int getRelId() {
		return fixed.getRelId();
	}

	public Set<TimePathId> getTimePathSet() {
		return fixed.getTimePathSet();
	}

	public List<RelationshipTuple> getTuples() {
		return fixed.getTuples();
	}

	public List<RelationshipTuple> getTuples(boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		return fixed.getTuples(returnConflictResolvedLatestState);
	}

	public UniversalAceRelationship getUniversal() throws IOException,
			TerminologyException {
		return fixed.getUniversal();
	}

	public List<RelationshipPart> getVersions() {
		return fixed.getVersions();
	}

	public List<RelationshipPart> getVersions(
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		return fixed.getVersions(returnConflictResolvedLatestState);
	}

	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus)
			throws IOException, TerminologyException {
		return fixed.promote(viewPosition, pomotionPaths, allowedStatus);
	}

	public boolean removeRedundantRecs() {
		return fixed.removeRedundantRecs();
	}

	public void setC2Id(int destId) {
		fixed.setC2Id(destId);
	}

	public int versionCount() {
		return fixed.versionCount();
	}

	public RelationshipPart duplicate() {
		return part.duplicate();
	}

	public int getCharacteristicId() {
		return part.getCharacteristicId();
	}

	public int getGroup() {
		return part.getGroup();
	}

	public ArrayIntList getPartComponentNids() {
		return part.getPartComponentNids();
	}

	public int getPathId() {
		return part.getPathId();
	}

	public int getRefinabilityId() {
		return part.getRefinabilityId();
	}

	public int getStatusAtPositionNid() {
		return part.getStatusAtPositionNid();
	}

	public int getStatusId() {
		return part.getStatusId();
	}

	public long getTime() {
		return part.getTime();
	}

	public int getTypeId() {
		return part.getTypeId();
	}

	public int getVersion() {
		return part.getVersion();
	}

	public boolean hasNewData(RelationshipPart another) {
		return part.hasNewData(another);
	}

	public RelationshipPart makeAnalog(int statusNid, int pathNid, long time) {
		return part.makeAnalog(statusNid, pathNid, time);
	}

	public void setCharacteristicId(int characteristicId) {
		part.setCharacteristicId(characteristicId);
	}

	public void setGroup(int group) {
		part.setGroup(group);
	}

	@Deprecated
	public void setPathId(int pathId) {
		part.setPathId(pathId);
	}

	public void setRefinabilityId(int refinabilityId) {
		part.setRefinabilityId(refinabilityId);
	}

	@Deprecated
	public void setStatusId(int statusId) {
		part.setStatusId(statusId);
	}

	public void setTypeId(int type) {
		part.setTypeId(type);
	}

	@Deprecated
	public void setVersion(int version) {
		part.setVersion(version);
	}

	@Override
	public Relationship getFixedPart() {
		return fixed;
	}

	@Override
	public RelationshipPart getPart() {
		return part;
	}

	public Relationship getRelVersioned() {
		return fixed;
	}

	public void setCharacteristicId(Integer characteristicId) {
		part.setCharacteristicId(characteristicId);
	}

	public void setGroup(Integer group) {
		part.setGroup(group);
	}

	public void setRefinabilityId(Integer refinabilityId) {
		part.setRefinabilityId(refinabilityId);
	}
	
	@Deprecated
	public void setStatusId(Integer statusId) {
		part.setStatusId(statusId);
	}

	public int getFixedPartId() {
		return fixed.getNid();
	}

	
}
