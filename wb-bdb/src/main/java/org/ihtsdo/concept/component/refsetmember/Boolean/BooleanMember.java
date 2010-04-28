package org.ihtsdo.concept.component.refsetmember.Boolean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.ERefsetBooleanMember;
import org.ihtsdo.etypes.ERefsetBooleanRevision;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class BooleanMember extends RefsetMember<BooleanRevision, BooleanMember> implements I_ExtendByRefPartBoolean {

	private static VersionComputer<RefsetMember<BooleanRevision, BooleanMember>.Version> computer = 
		new VersionComputer<RefsetMember<BooleanRevision, BooleanMember>.Version>();

	protected VersionComputer<RefsetMember<BooleanRevision, BooleanMember>.Version> getVersionComputer() {
		return computer;
	}

	public class Version 
	extends RefsetMember<BooleanRevision, BooleanMember>.Version 
	implements I_ExtendByRefVersion, I_ExtendByRefPartBoolean {

		private Version() {
			super();
		}

		private Version(int index) {
			super(index);
		}

		@Override
		public ArrayIntList getVariableVersionNids() {
		    return new ArrayIntList();
		}

		@Override
		public int compareTo(I_ExtendByRefPart o) {
			if (I_ExtendByRefPartBoolean.class.isAssignableFrom(o.getClass())) {
				I_ExtendByRefPartBoolean another = (I_ExtendByRefPartBoolean) o;
				if (this.getBooleanValue() == another.getBooleanValue()) {
					return super.compareTo(another);
				}
				if (this.getBooleanValue()) {
					return 1;
				}
				return -1;
			}
			return super.compareTo(o);
		}

		@Override
		public I_ExtendByRefPartStr duplicate() {
			return (I_ExtendByRefPartStr) super.duplicate();
		}

		@Override
		public boolean getBooleanValue() {
			if (index >= 0) {
				return revisions.get(index).getBooleanValue();
			}
			return BooleanMember.this.getBooleanValue();
		}

		@Override
		public void setBooleanValue(boolean value) {
			if (index >= 0) {
				revisions.get(index).setBooleanValue(value);
			}
			BooleanMember.this.setBooleanValue(value);
		}

		@Override
		public ERefsetBooleanMember getERefsetMember() throws TerminologyException, IOException {
			return new ERefsetBooleanMember(this);
		}

		@Override
		public ERefsetBooleanRevision getERefsetRevision() throws TerminologyException, IOException {
			return new ERefsetBooleanRevision(this);
		}
	}

	private boolean booleanValue;

	public BooleanMember(Concept enclosingConcept, TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}

	public BooleanMember(ERefsetBooleanMember refsetMember,
			Concept enclosingConcept) throws IOException {
		super(refsetMember, enclosingConcept);
		booleanValue = refsetMember.getBooleanValue();
		if (refsetMember.getRevisionList() != null) {
			revisions = new CopyOnWriteArrayList<BooleanRevision>();
			for (ERefsetBooleanRevision eVersion : refsetMember
					.getRevisionList()) {
				revisions.add(new BooleanRevision(eVersion, this));
			}
		}
	}

    public BooleanMember() {
		super();
	}

    
	@SuppressWarnings("unchecked")
	protected List<Version> getVersions() {
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
	public I_ExtendByRefPart makePromotionPart(I_Path promotionPath) {
		throw new UnsupportedOperationException();
	}
	
	public I_ExtendByRefPart duplicate() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected boolean membersEqual(
			ConceptComponent<BooleanRevision, BooleanMember> obj) {
		if (BooleanMember.class.isAssignableFrom(obj.getClass())) {
			BooleanMember another = (BooleanMember) obj;
			return this.booleanValue = another.booleanValue;
		}
		return false;
	}

	@Override
	protected final BooleanRevision readMemberRevision(TupleInput input) {
		return new BooleanRevision(input, this);
	}
	@Override
	protected void readMemberFields(TupleInput input) {
		booleanValue = input.readBoolean();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeBoolean(booleanValue);
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        if (getTime() == time && getPathId() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
		BooleanRevision newR = new BooleanRevision(statusNid, pathNid, time, this);
		addRevision(newR);
		return newR;
	}

	@Override
	public BooleanRevision makeAnalog() {
	    BooleanRevision newR = new BooleanRevision(getStatusId(), getPathId(), getTime(), this);
	    return newR;
	}


	public boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
        modified();
	}

	@Override
	public int getTypeId() {
		return REFSET_TYPES.BOOLEAN.getTypeNid();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    StringBuffer buf = new StringBuffer();  
	    buf.append(this.getClass().getSimpleName() + ":{");
	    buf.append(" booleanValue:" + this.booleanValue);
	    return buf.toString();
	}

	@Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (BooleanMember.class.isAssignableFrom(obj.getClass())) {
            BooleanMember another = (BooleanMember) obj;
            return this.nid == another.nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { nid });
    }

}
