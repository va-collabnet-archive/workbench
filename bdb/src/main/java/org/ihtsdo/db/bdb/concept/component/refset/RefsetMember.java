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
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.etypes.ERefset;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class RefsetMember<V extends RefsetRevision<V, C>, 
								   C extends RefsetMember<V, C>> 
			extends ConceptComponent<V, C> 
			implements I_ThinExtByRefVersioned {

	private int referencedComponentNid;


	public RefsetMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}
	
	public RefsetMember(ERefset<?> refsetMember, 
			Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		referencedComponentNid = Bdb.uuidToNid(refsetMember.getComponentUuid());
		primordialSapNid = Bdb.getStatusAtPositionNid(refsetMember);
		assert primordialSapNid != Integer.MAX_VALUE;
	}
	

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("nid: ");
		buf.append(nid);
		buf.append(" refset: ");
		try {
			buf.append(enclosingConcept.getInitialText());
		} catch (IOException e1) {
			buf.append(e1.getLocalizedMessage());
		}
		buf.append(" type: ");
		try {
			buf.append(REFSET_TYPES.nidToType(getTypeId()));
		} catch (TerminologyException e) {
			buf.append(e.getLocalizedMessage());
		} catch (IOException e) {
			buf.append(e.getLocalizedMessage());
		}
		buf.append(" rcNid: ");
		addNidToBuffer(buf, referencedComponentNid);
		buf.append(" ");
		buf.append(getTypeFieldsString());
		buf.append(" ");
		buf.append(super.toString());
		return buf.toString();
	}

	protected abstract String getTypeFieldsString();

	@Override
	public boolean fieldsEqual(ConceptComponent<V, C> obj) {
		if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
			RefsetMember<V,C> another = (RefsetMember<V,C>) obj;
			if (this.getTypeId() != another.getTypeId()) {
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
		referencedComponentNid = input.readInt();
		readMember(input);
		int additionalVersionCount = input.readShort();
		if (additionalVersionCount > 0) {
			readMemberParts(input, additionalVersionCount);
		}
	}

	protected abstract void readMember(TupleInput input);

	protected abstract void readMemberParts(TupleInput input, int additionalVersionCount);


	@Override
	public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		List<RefsetRevision<V, C>> additionalVersionsToWrite = new ArrayList<RefsetRevision<V, C>>();
		if (additionalVersions != null) {
			for (RefsetRevision<V, C> p: additionalVersions) {
				if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
					additionalVersionsToWrite.add(p);
				}
			}
		}
		output.writeInt(referencedComponentNid);
		writeMember(output);
		output.writeShort(additionalVersionsToWrite.size());
		for (RefsetRevision<V, C> p: additionalVersionsToWrite) {
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

	public UniversalAceExtByRefPart getUniversalPart()
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}


}
