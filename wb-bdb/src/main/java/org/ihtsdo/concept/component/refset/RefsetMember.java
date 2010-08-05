package org.ihtsdo.concept.component.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PRECEDENCE;
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

public abstract class RefsetMember<R extends RefsetRevision<R, C>, 
								   C extends RefsetMember<R, C>> 
			extends ConceptComponent<R, C> 
			implements I_ExtendByRef {


	public int referencedComponentNid;

	public class Version 
	extends ConceptComponent<R, C>.Version 
	implements I_ExtendByRefVersion, I_ExtendByRefPart {

		public Version() {
			super();
		}

		public Version(int index) {
			super(index);
		}

		@Override
		public ArrayIntList getVariableVersionNids() {
            if (index >= 0) {
                return revisions.get(index).getVariableVersionNids();
            } else {
                return RefsetMember.this.getVariableVersionNids();
            }
		}

		@Override
		public RefsetRevision<?, ?> makeAnalog(int statusNid, int pathNid, long time) {
            if (index >= 0) {
                return revisions.get(index).makeAnalog(statusNid, pathNid, time);
            } 
			return (RefsetRevision<?, ?>) RefsetMember.this.makeAnalog(statusNid, pathNid, time);
		}

        @Override
        public R makeAnalog() {
            if (index >= 0) {
                return revisions.get(index).makeAnalog();
            } 
            return (R) RefsetMember.this.makeAnalog();
        }

        @SuppressWarnings("unchecked")
		@Override
		public void addVersion(I_ExtendByRefPart part) {
			versions = null;
			RefsetMember.this.addRevision((R) part);
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
		    if (Version.class.isAssignableFrom(o.getClass())) {
	            Version another = (Version) o;
	            if (getSapNid() != another.getSapNid()) {
	                return this.getSapNid() - another.getSapNid();
	            }
	            return this.index - another.index;
		    }
		    return this.toString().compareTo(o.toString());
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
	

	public abstract R makeAnalog();


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
	public boolean fieldsEqual(ConceptComponent<R, C> obj) {
		if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
			RefsetMember<R,C> another = (RefsetMember<R,C>) obj;
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
	
	protected abstract boolean membersEqual(ConceptComponent<R, C> obj);

	public void readFromBdb(TupleInput input)  {
		referencedComponentNid = input.readInt();
		readMemberFields(input);
		int additionalVersionCount = input.readShort();
		if (additionalVersionCount > 0) {
	        if (revisions == null) {
	            revisions = new CopyOnWriteArrayList<R>();
	        } 
	        for (int i = 0; i < additionalVersionCount; i++) {
	            R r = readMemberRevision(input);
	            if (r.getTime() != Long.MIN_VALUE) {
	                revisions.add(r);
	            }
	        }
		}
	}

	protected abstract void readMemberFields(TupleInput input);

	protected abstract R readMemberRevision(TupleInput input);


	@Override
	public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		List<RefsetRevision<R, C>> additionalVersionsToWrite = new ArrayList<RefsetRevision<R, C>>();
		if (revisions != null) {
			for (RefsetRevision<R, C> p: revisions) {
				if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid &&
						p.getTime() != Long.MIN_VALUE) {
					additionalVersionsToWrite.add(p);
				}
			}
		}
		output.writeInt(referencedComponentNid);
		writeMember(output);
		output.writeShort(additionalVersionsToWrite.size());
		for (RefsetRevision<R, C> p: additionalVersionsToWrite) {
			p.writePartToBdb(output);
		}		
	}

	protected abstract void writeMember(TupleOutput output);
	
	
	@Override
	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus, PRECEDENCE precedence)
			throws IOException, TerminologyException {
        int viewPathId = viewPosition.getPath().getConceptId();
        Collection<Version> matchingTuples = 
        	getVersionComputer().getSpecifiedVersions(allowedStatus, 
        			viewPosition, 
        			getVersions(), precedence, null);
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
		super.addRevision((R) part);
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
	public void setRefsetId(int refsetNid) throws IOException {
		if (getTime() == Long.MAX_VALUE) {
			if (enclosingConceptNid != refsetNid) {
				int nid = primordialSapNid;
				Terms.get().forget(this);
				enclosingConceptNid = refsetNid;
				primordialSapNid = nid;
				Concept newRefsetConcept = Concept.get(refsetNid);
				newRefsetConcept.getExtensions().add(this);
				Bdb.getNidCNidMap().resetCidForNid(refsetNid, getNid());
				Terms.get().addUncommitted(newRefsetConcept);
			}
		} else {
			throw new UnsupportedOperationException("Cannot change refset unless member is uncommitted...");
		}
	}


	@Override
	public void setTypeId(int typeId) {
		if (typeId != getTypeId()) {
			throw new UnsupportedOperationException();
		}
	}


	@Override
	public RefsetMember<R, C> getMutablePart() {
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
        modified();
	}
	

	public I_ExtendByRefPart makePromotionPart(I_Path promotionPath) {
		return (I_ExtendByRefPart) makeAnalog(getStatusId(), promotionPath.getConceptId(), Long.MAX_VALUE);
	}

	public final int compareTo(I_ExtendByRefPart o) {
        return this.toString().compareTo(o.toString());
	}

	protected abstract VersionComputer<RefsetMember<R,C>.Version> getVersionComputer();

	@Override
	public void addTuples(I_IntSet allowedStatus, PositionSetReadOnly positions,
			List<I_ExtendByRefVersion> returnTuples, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
			throws TerminologyException, IOException {
		List<RefsetMember<R,C>.Version> versionsToAdd = new ArrayList<RefsetMember<R,C>.Version>();
		getVersionComputer().addSpecifiedVersions(allowedStatus, 
				positions, versionsToAdd, (List<Version>) getVersions(), precedencePolicy, contradictionManager);
		returnTuples.addAll((Collection<? extends I_ExtendByRefVersion>) versionsToAdd);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void addTuples(List<I_ExtendByRefVersion> returnTuples,
	        PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
			throws TerminologyException, IOException {
		List<RefsetMember<R,C>.Version> versionsToAdd = new ArrayList<RefsetMember<R,C>.Version>();
		getVersionComputer().addSpecifiedVersions(Terms.get().getActiveAceFrameConfig().getAllowedStatus(), 
				Terms.get().getActiveAceFrameConfig().getViewPositionSetReadOnly(), versionsToAdd, 
				(List<Version>) getVersions(), precedencePolicy, contradictionManager);
		returnTuples.addAll((Collection<? extends I_ExtendByRefVersion>) versionsToAdd);
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<? extends I_ExtendByRefVersion> getTuples(I_IntSet allowedStatus,
			PositionSetReadOnly positions, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
			throws TerminologyException, IOException {
		List<RefsetMember<R,C>.Version> versionsToAdd = new ArrayList<RefsetMember<R,C>.Version>();
		getVersionComputer().addSpecifiedVersions(allowedStatus, 
				positions, versionsToAdd, (List<Version>) getVersions(), precedencePolicy, contradictionManager);
		return versionsToAdd;
	}

    @Override
    public List<? extends I_ExtendByRefVersion> getTuples(I_ManageContradiction contradictionMgr)
            throws TerminologyException, IOException {
        // TODO Implement contradictionMgr part...
        return getVersions();
    }



	@Override
	protected void clearVersions() {
		versions = null;
	}
    @Override
    public boolean hasExtensions() throws IOException {
        if (getEnclosingConcept().hasExtensionExtensions()) {
            return getEnclosingConcept().hasExtensionsForComponent(nid);
        }
        return false;
    }

}
