package org.ihtsdo.concept.component.refsetmember.integer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refsetmember.str.StrRevision;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.ERefsetIntMember;
import org.ihtsdo.etypes.ERefsetIntRevision;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.concept.component.refset.integer.TkRefsetIntMember;
import org.ihtsdo.tk.concept.component.refset.integer.TkRefsetIntRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IntMember extends RefsetMember<IntRevision, IntMember> implements I_ExtendByRefPartInt {

	private static VersionComputer<RefsetMember<IntRevision, IntMember>.Version> computer = 
		new VersionComputer<RefsetMember<IntRevision, IntMember>.Version>();

	protected VersionComputer<RefsetMember<IntRevision, IntMember>.Version> getVersionComputer() {
		return computer;
	}
	public class Version 
	extends RefsetMember<IntRevision, IntMember>.Version 
	implements I_ExtendByRefVersion, I_ExtendByRefPartInt {

		private Version() {
			super();
		}

		private Version(int index) {
			super(index);
		}

		public int compareTo(I_ExtendByRefPart o) {
			if (I_ExtendByRefPartInt.class.isAssignableFrom(o.getClass())) {
				I_ExtendByRefPartInt another = (I_ExtendByRefPartInt) o;
				if (this.getIntValue() != another.getIntValue()) {
					return this.getIntValue() - another.getIntValue();
				}
			}
			return super.compareTo(o);
		}

		@Override
		public I_ExtendByRefPartInt duplicate() {
			return (I_ExtendByRefPartInt) super.duplicate();
		}

		@Override
		public int getIntValue() {
			if (index >= 0) {
				return revisions.get(index).getIntValue();
			}
			return IntMember.this.getIntValue();
		}

		@Override
		public void setIntValue(int value) {
			if (index >= 0) {
				revisions.get(index).setIntValue(value);
			}
			IntMember.this.setIntValue(value);
		}
		
		@Override
		public ERefsetIntMember getERefsetMember() throws TerminologyException, IOException {
			return new ERefsetIntMember(this);
		}

		@Override
		public ERefsetIntRevision getERefsetRevision() throws TerminologyException, IOException {
			return new ERefsetIntRevision(this);
		}
	}

	private int intValue;

	public IntMember(Concept enclosingConcept, TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}

	public IntMember(TkRefsetIntMember refsetMember, 
			Concept enclosingConcept) throws IOException {
		super(refsetMember, enclosingConcept);
		intValue =refsetMember.getIntValue();
		if (refsetMember.getRevisionList() != null) {
			revisions = new CopyOnWriteArrayList<IntRevision>();
			for (TkRefsetIntRevision eVersion: refsetMember.getRevisionList()) {
				revisions.add(new IntRevision(eVersion, this));
			}
		}
	}

    public IntMember() {
        super();
    }

	
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (IntMember.class.isAssignableFrom(obj.getClass())) {
            IntMember another = (IntMember) obj;
            return this.nid == another.nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { this.nid });
    } 
    
    @Override
	protected boolean membersEqual(
			ConceptComponent<IntRevision, IntMember> obj) {
		if (IntMember.class.isAssignableFrom(obj.getClass())) {
			IntMember another = (IntMember) obj;
			return this.intValue == another.intValue;
		}
		return false;
	}

	@Override
	protected final IntRevision readMemberRevision(TupleInput input) {
	    return new IntRevision(input, this);
	}
	@Override
	protected void readMemberFields(TupleInput input) {
		intValue = input.readInt();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(intValue);
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		return new ArrayIntList(2);
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        if (getTime() == time && getPathId() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
		IntRevision newR = new IntRevision(statusNid, pathNid, time, this);
		addRevision(newR);
		return newR;
	}

    @Override
    public IntRevision makeAnalog() {
        IntRevision newR = new IntRevision(getStatusId(), getPathId(), getTime(), this);
        return newR;
    }

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
        modified();
	}
	@Override
	public int getTypeId() {
		return REFSET_TYPES.INT.getTypeNid();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" intValue:" + this.intValue);
        buf.append(" }=> ");
        buf.append(super.toString());
        return buf.toString();
    }
	@Override
	public StrRevision duplicate() {
		throw new UnsupportedOperationException();
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

}
