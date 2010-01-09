package org.ihtsdo.db.bdb.concept.component.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.etypes.ERefset;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class RefsetMember<V extends RefsetVersion<V, C>, 
								   C extends RefsetMember<V, C>> 
			extends ConceptComponent<V, C> 
			implements I_ThinExtByRefVersioned {

	private int memberTypeNid; 
	private int referencedComponentNid;


	public RefsetMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}
	
	public RefsetMember(ERefset<?> refsetMember, 
			Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		memberTypeNid = refsetMember.getType().getTypeNid();
		referencedComponentNid = Bdb.uuidToNid(refsetMember.getComponentUuid());
		primordialStatusAtPositionNid = Bdb.getStatusAtPositionNid(refsetMember);
		assert primordialStatusAtPositionNid != Integer.MAX_VALUE;
	}
	

	@Override
	public boolean fieldsEqual(ConceptComponent<V, C> obj) {
		if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
			RefsetMember<V,C> another = (RefsetMember<V,C>) obj;
			if (this.memberTypeNid != another.memberTypeNid) {
				return false;
			}
			if (membersEqual(obj)) {
				return conceptComponentFieldsEqual(another);
			}
		}
		return false;
	}

	protected abstract boolean membersEqual(ConceptComponent<V, C> obj);

	public void readFromBdb(TupleInput input)  {
		memberTypeNid = input.readInt();
		referencedComponentNid = input.readInt();
		readMember(input);
		int additionalVersionCount = input.readShort();
		readMemberParts(input, additionalVersionCount);
	}

	protected abstract void readMember(TupleInput input);

	protected abstract void readMemberParts(TupleInput input, int additionalVersionCount);


	@Override
	public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		List<RefsetVersion<V, C>> additionalVersionsToWrite = new ArrayList<RefsetVersion<V, C>>();
		if (additionalVersions != null) {
			for (RefsetVersion<V, C> p: additionalVersions) {
				if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
					additionalVersionsToWrite.add(p);
				}
			}
		}
		output.writeInt(referencedComponentNid);
		output.writeInt(memberTypeNid);
		writeMember(output);
		output.writeShort(additionalVersionsToWrite.size());
		for (RefsetVersion<V, C> p: additionalVersionsToWrite) {
			p.writePartToBdb(output);
		}		
	}

	protected abstract void writeMember(TupleOutput output);
	
	
	@Override
	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		return false;
	}



	@SuppressWarnings("unchecked")
	@Override
	public void addVersion(I_ThinExtByRefPart part) {
		additionalVersions.add((V) part);
	}


	@Override
	public int getComponentId() {
		return referencedComponentNid;
	}


	@Override
	public int getMemberId() {
		return nid;
	}


	@Override
	public int getRefsetId() {
		return enclosingConcept.getNid();
	}


	@Override
	public List<I_ThinExtByRefTuple> getTuples(boolean addUncommitted,
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int getTypeId() {
		return memberTypeNid;
	}


	@Override
	public void setRefsetId(int refsetId) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void setTypeId(int typeId) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions,
			List<I_ThinExtByRefTuple> returnTuples, boolean addUncommitted) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions,
			List<I_ThinExtByRefTuple> returnTuples, boolean addUncommitted,
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}


	@Override
	public void addTuples(List<I_ThinExtByRefTuple> returnTuples,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}


	@Override
	public List<I_ThinExtByRefTuple> getTuples(I_IntSet allowedStatus,
			Set<I_Position> positions, boolean addUncommitted) {
		throw new UnsupportedOperationException();
	}


	@Override
	public List<I_ThinExtByRefTuple> getTuples(I_IntSet allowedStatus,
			Set<I_Position> positions, boolean addUncommitted,
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public RefsetMember<V, C> getMutablePart() {
		return this;
	}

}
