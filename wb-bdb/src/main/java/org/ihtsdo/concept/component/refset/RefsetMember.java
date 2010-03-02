package org.ihtsdo.concept.component.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.ERefsetMember;
import org.ihtsdo.etypes.ERevision;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class RefsetMember<V extends RefsetRevision<V, C>, 
								   C extends RefsetMember<V, C>> 
			extends ConceptComponent<V, C> 
			implements I_ExtendByRef {


	public int referencedComponentNid;

	public class Version 
	extends ConceptComponent<V, C>.Version 
	implements I_ExtendByRefVersion, I_ExtendByRefPart {

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
		public RefsetRevision<?, ?> makeAnalog(int statusNid, int pathNid, long time) {
			return (RefsetRevision<?, ?>) RefsetMember.this.makeAnalog(statusNid, pathNid, time);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void addVersion(I_ExtendByRefPart part) {
			versions = null;
			RefsetMember.this.addRevision((V) part);
		}

		@Override
		public int getComponentId() {
			return referencedComponentNid;
		}

		@Override
		public I_ExtendByRef getCore() {
			return RefsetMember.this;
		}

		@Override
		public int getMemberId() {
			return nid;
		}

		@Override
		public int getRefsetId() {
			return enclosingConceptNid;
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
		public List<? extends I_ExtendByRefPart> getVersions() {
			return RefsetMember.this.getVersions();
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
		public I_ExtendByRefPart makePromotionPart(I_Path promotionPath) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int compareTo(I_ExtendByRefPart o) {
			Version another = (Version) o;
			if (getSapNid() != another.getSapNid()) {
				return this.getSapNid() - another.getSapNid();
			}
			return this.index - another.index;
		}

		@Override
		public I_ExtendByRefPart duplicate() {
			throw new UnsupportedOperationException();
		}

		@Override
		public I_ExtendByRefPart getMutablePart() {
			return (I_ExtendByRefPart) super.getMutablePart();
		}

		public ERefsetMember<?> getERefsetMember() throws TerminologyException, IOException {
			throw new UnsupportedOperationException("subclass must override");
		}

		public ERevision getERefsetRevision() throws TerminologyException, IOException {
			throw new UnsupportedOperationException("subclass must override");
		}
				
	}

	public RefsetMember(Concept enclosingConcept, TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}
	

	public RefsetMember(ERefsetMember<?> refsetMember, 
			Concept enclosingConcept) throws IOException {
		super(refsetMember, enclosingConcept);
		referencedComponentNid = Bdb.uuidToNid(refsetMember.getComponentUuid());
		primordialSapNid = Bdb.getSapNid(refsetMember);
		assert primordialSapNid != Integer.MAX_VALUE;
	}
	

	public RefsetMember() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	public void merge(RefsetMember component) {
		super.merge((C) component);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
 		buf.append(" refset:");
		addNidToBuffer(buf, enclosingConceptNid);
		buf.append(" type:");
		try {
			buf.append(REFSET_TYPES.nidToType(getTypeId()));
		} catch (IOException e) {
			buf.append(e.getLocalizedMessage());
		}
		buf.append(" rcNid:");
		addNidToBuffer(buf, referencedComponentNid);
		buf.append(" ");
		buf.append(super.toString());
		return buf.toString();
	}

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

    /**
     * Test method to check to see if two objects are equal in all respects. 
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures. 
     * @throws IOException 
     */
    public String validate(RefsetMember<?, ?> another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        if (this.referencedComponentNid != another.referencedComponentNid) {
            buf.append("\tRefsetMember.referencedComponentNid not equal: \n" + 
                "\t\tthis.referencedComponentNid = " + this.referencedComponentNid + "\n" + 
                "\t\tanother.referencedComponentNid = " + another.referencedComponentNid + "\n");
        }
        // Compare the parents 
        buf.append(super.validate(another));
        return buf.toString();
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
				if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid &&
						p.getTime() != Long.MIN_VALUE) {
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
        int viewPathId = viewPosition.getPath().getConceptId();
        Collection<Version> matchingTuples = 
        	getVersionComputer().getSpecifiedVersions(allowedStatus, 
        			viewPosition, 
        			getVersions());
        boolean promotedAnything = false;
        for (I_Path promotionPath : pomotionPaths) {
            for (Version v : matchingTuples) {
                if (v.getPathId() == viewPathId) {
                    RefsetRevision<?, ?> revision =  v.makeAnalog(v.getStatusId(), 
							promotionPath.getConceptId(), Long.MAX_VALUE);
                    addVersion(revision);
                    promotedAnything = true;
                }
            }
        }
        return promotedAnything;
	}


	@SuppressWarnings("unchecked")
	@Override
	public void addVersion(I_ExtendByRefPart part) {
		versions = null;
		super.addRevision((V) part);
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
		return enclosingConceptNid;
	}


	@Override
	public List<I_ExtendByRefVersion> getTuples(boolean addUncommitted,
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
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
	public RefsetMember<V, C> getMutablePart() {
		return this;
	}

	public UniversalAceExtByRefPart getUniversalPart()
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<? extends I_ExtendByRefPart> getMutableParts() {
		return getVersions();
	}

	protected List<? extends Version> versions;

	public List<Version> getTuples() {
		return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
	}

	protected List<? extends Version> getVersions() {
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
		return (List<Version>) versions;
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


	public int getReferencedComponentNid() {
		return referencedComponentNid;
	}


	public void setReferencedComponentNid(int referencedComponentNid) {
		this.referencedComponentNid = referencedComponentNid;
	}
	

	public I_ExtendByRefPart makePromotionPart(I_Path promotionPath) {
		return (I_ExtendByRefPart) makeAnalog(getStatusId(), promotionPath.getConceptId(), Long.MAX_VALUE);
	}

	public int compareTo(I_ExtendByRefPart o) {
		throw new UnsupportedOperationException();
	}

	protected abstract VersionComputer<RefsetMember<V,C>.Version> getVersionComputer();

	@Override
	public void addTuples(I_IntSet allowedStatus, PositionSetReadOnly positions,
			List<I_ExtendByRefVersion> returnTuples, boolean addUncommitted,
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		List<RefsetMember<V,C>.Version> versionsToAdd = new ArrayList<RefsetMember<V,C>.Version>();
		getVersionComputer().addSpecifiedVersions(allowedStatus, 
				positions, versionsToAdd, addUncommitted, (List<Version>) getVersions());
		returnTuples.addAll((Collection<? extends I_ExtendByRefVersion>) versionsToAdd);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void addTuples(I_IntSet allowedStatus, PositionSetReadOnly positions,
			List<I_ExtendByRefVersion> returnTuples, boolean addUncommitted) {
		List<RefsetMember<V,C>.Version> versionsToAdd = new ArrayList<RefsetMember<V,C>.Version>();
		getVersionComputer().addSpecifiedVersions(allowedStatus, 
				positions, versionsToAdd, addUncommitted, (List<Version>) getVersions());
		returnTuples.addAll((Collection<? extends I_ExtendByRefVersion>) versionsToAdd);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void addTuples(List<I_ExtendByRefVersion> returnTuples,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		List<RefsetMember<V,C>.Version> versionsToAdd = new ArrayList<RefsetMember<V,C>.Version>();
		getVersionComputer().addSpecifiedVersions(Terms.get().getActiveAceFrameConfig().getAllowedStatus(), 
				Terms.get().getActiveAceFrameConfig().getViewPositionSetReadOnly(), versionsToAdd, 
				addUncommitted, (List<Version>) getVersions());
		returnTuples.addAll((Collection<? extends I_ExtendByRefVersion>) versionsToAdd);
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<? extends I_ExtendByRefVersion> getTuples(I_IntSet allowedStatus,
			PositionSetReadOnly positions, boolean addUncommitted,
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		List<RefsetMember<V,C>.Version> versionsToAdd = new ArrayList<RefsetMember<V,C>.Version>();
		getVersionComputer().addSpecifiedVersions(allowedStatus, 
				positions, versionsToAdd, addUncommitted, (List<Version>) getVersions());
		return versionsToAdd;
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<? extends I_ExtendByRefVersion> getTuples(I_IntSet allowedStatus,
			PositionSetReadOnly positions, boolean addUncommitted) {
		List<RefsetMember<V,C>.Version> versionsToAdd = new ArrayList<RefsetMember<V,C>.Version>();
		getVersionComputer().addSpecifiedVersions(allowedStatus, 
				positions, versionsToAdd, addUncommitted, (List<Version>) getVersions());
		return versionsToAdd;
	}
	

	@Override
	protected void clearVersions() {
		versions = null;
	}

}
