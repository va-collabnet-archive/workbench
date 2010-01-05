package org.ihtsdo.db.bdb.concept.component.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import org.ihtsdo.etypes.ERefset;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class RefsetMember<V extends RefsetVersion<V, C>, 
								   C extends RefsetMember<V, C>> 
			extends ConceptComponent<V, C> 
			implements I_ThinExtByRefVersioned {

	private int memberTypeNid; 
	private int referencedComponentNid;


	public RefsetMember(int nid, int partCount, Concept enclosingConcept, 
			UUID primordialUuid) {
		super(nid, partCount, enclosingConcept, 
				primordialUuid);
	}
	
	public RefsetMember(ERefset refsetMember, 
			Concept enclosingConcept) {
		super(Bdb.uuidsToNid(refsetMember.getUuids()), 
				refsetMember.getVersionCount(), enclosingConcept,
				refsetMember.primordialComponentUuid);
		memberTypeNid = refsetMember.getType().getTypeNid();
		referencedComponentNid = Bdb.uuidToNid(refsetMember.getComponentUuid());
		primordialStatusAtPositionNid = Bdb.getStatusAtPositionNid(refsetMember);
		assert primordialStatusAtPositionNid != Integer.MAX_VALUE;
	}
	

	public void readFromBdb(TupleInput input, int listSize)  {
		memberTypeNid = input.readInt();
		referencedComponentNid = input.readInt();
		readMemberParts(input);
		throw new UnsupportedOperationException();
	}

	protected abstract void readMemberParts(TupleInput input);


	@Override
	public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		List<RefsetVersion<V, C>> partsToWrite = new ArrayList<RefsetVersion<V, C>>();
		for (RefsetVersion<V, C> p: additionalVersions) {
			if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
				partsToWrite.add(p);
			}
		}
		output.writeShort(partsToWrite.size());
		output.writeInt(referencedComponentNid);
		output.writeInt(memberTypeNid);

		for (RefsetVersion<V, C> p: partsToWrite) {
			p.writePartToBdb(output);
		}		
	}


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
