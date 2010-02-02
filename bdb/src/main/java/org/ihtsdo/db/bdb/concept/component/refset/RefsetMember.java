package org.ihtsdo.db.bdb.concept.component.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.concept.component.description.Description.Version;
import org.ihtsdo.etypes.ERefset;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class RefsetMember<V extends RefsetRevision<V, C>, 
								   C extends RefsetMember<V, C>> 
			extends ConceptComponent<V, C> 
			implements I_ThinExtByRefVersioned {

	private int referencedComponentNid;

	public class Version 
	extends ConceptComponent<V, C>.Version 
	implements I_ThinExtByRefTuple, I_ThinExtByRefPart {

		public Version() {
			super();
		}

		public Version(int index) {
			super(index);
		}

		@Override
		public ArrayIntList getVariableVersionNids() {
			throw new UnsupportedOperationException();
		}

		@Override
		public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
			return RefsetMember.this.makeAnalog(statusNid, pathNid, time);
		}

		@Override
		public void addVersion(I_ThinExtByRefPart part) {
			if (!enclosingConcept.isEditable()) {
				throw new UnsupportedOperationException("enclosing concept is not editable");
			}
			versions = null;
			RefsetMember.this.addVersion((V) part);
		}

		@Override
		public int getComponentId() {
			return referencedComponentNid;
		}

		@Override
		public I_ThinExtByRefVersioned getCore() {
			return RefsetMember.this;
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
		public int getStatus() {
			if (index >= 0) {
				return revisions.get(index).getStatus();
			}
			return RefsetMember.this.getStatusId();
		}

		@Override
		public int getTypeId() {
			return RefsetMember.this.getTypeId();
		}

		@Override
		public List<? extends I_ThinExtByRefPart> getVersions() {
			return RefsetMember.this.getVersions();
		}

		@Override
		public boolean promote(I_Path path) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setStatus(int idStatus) {
			if (index >= 0) {
				revisions.get(index).setStatus(idStatus);
			} else {
				RefsetMember.this.setStatusId(idStatus);
			}
		}

		@Override
		public UniversalAceExtByRefPart getUniversalPart()
				throws TerminologyException, IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public I_ThinExtByRefPart makePromotionPart(I_Path promotionPath) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int compareTo(I_ThinExtByRefPart o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public I_ThinExtByRefPart duplicate() {
			throw new UnsupportedOperationException();
		}

		@Override
		public I_ThinExtByRefPart getMutablePart() {
			return (I_ThinExtByRefPart) super.getMutablePart();
		}
		
		
		
	}

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
	

	public RefsetMember() {
		super();
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getSimpleName() + ":{");
		buf.append(" refset:");
		try {
			buf.append("'" + enclosingConcept.getInitialText() + "'");
		} catch (IOException e1) {
			buf.append(e1.getLocalizedMessage());
		}
		buf.append(" type:");
		try {
			buf.append(REFSET_TYPES.nidToType(getTypeId()));
		} catch (TerminologyException e) {
			buf.append(e.getLocalizedMessage());
		} catch (IOException e) {
			buf.append(e.getLocalizedMessage());
		}
		buf.append(" rcNid:");
		addNidToBuffer(buf, referencedComponentNid);
		buf.append(" ");
		buf.append(getTypeFieldsString());
		buf.append(" }=> ");
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
		if (revisions != null) {
			for (RefsetRevision<V, C> p: revisions) {
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
		versions = null;
		super.addVersion((V) part);
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

	@Override
	public List<? extends I_ThinExtByRefPart> getMutableParts() {
		return getVersions();
	}

	List<Version> versions;

	public List<Version> getTuples() {
		return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
	}

	private List<Version> getVersions() {
		if (versions == null) {
			int count = 1;
			if (revisions != null) {
				count = count + revisions.size();
			}
			ArrayList<Version> list = new ArrayList<Version>(count);
			list.add(new Version());
			if (revisions != null) {
				for (int i = 0; i < revisions.size(); i++) {
					list.add(new Version(i));
				}
			}
			versions = list;
		}
		return versions;
	}
	
		@Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (RefsetMember.class.isAssignableFrom(obj.getClass())) {
            RefsetMember<?,?> another = (RefsetMember<?,?>) obj;
            return this.referencedComponentNid == another.referencedComponentNid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { referencedComponentNid });
    }
	
}
