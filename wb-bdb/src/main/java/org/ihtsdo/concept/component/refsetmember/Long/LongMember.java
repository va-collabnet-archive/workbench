package org.ihtsdo.concept.component.refsetmember.Long;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartLong;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refsetmember.str.StrRevision;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.ERefsetLongMember;
import org.ihtsdo.etypes.ERefsetLongRevision;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class LongMember extends RefsetMember<LongRevision, LongMember> implements I_ExtendByRefPartLong {

    private static VersionComputer<RefsetMember<LongRevision, LongMember>.Version> computer =
            new VersionComputer<RefsetMember<LongRevision, LongMember>.Version>();

    protected VersionComputer<RefsetMember<LongRevision, LongMember>.Version> getVersionComputer() {
        return computer;
    }

    public class Version extends RefsetMember<LongRevision, LongMember>.Version implements I_ExtendByRefVersion,
            I_ExtendByRefPartLong {

        private Version() {
            super();
        }

        private Version(int index) {
            super(index);
        }

        public int compareTo(I_ExtendByRefPart o) {
            if (I_ExtendByRefPartLong.class.isAssignableFrom(o.getClass())) {
                I_ExtendByRefPartLong another = (I_ExtendByRefPartLong) o;
                if (this.getLongValue() != another.getLongValue()) {
                    if (this.getLongValue() > another.getLongValue()) {
                        return 1;
                    } else if (this.getLongValue() < another.getLongValue()) {
                        return -1;
                    }
                }
            }
            return super.compareTo(o);
        }

        @Override
        public I_ExtendByRefPartInt duplicate() {
            return (I_ExtendByRefPartInt) super.duplicate();
        }

        @Override
        public long getLongValue() {
            if (index >= 0) {
                return revisions.get(index).getLongValue();
            }
            return LongMember.this.getLongValue();
        }

        @Override
        public void setLongValue(long value) {
            if (index >= 0) {
                revisions.get(index).setLongValue(value);
            }
            LongMember.this.setLongValue(value);
        }

        @Override
        public ERefsetLongMember getERefsetMember() throws TerminologyException, IOException {
            return new ERefsetLongMember(this);
        }

        @Override
        public ERefsetLongRevision getERefsetRevision() throws TerminologyException, IOException {
            return new ERefsetLongRevision(this);
        }
    }

    private long longValue;

    public LongMember(Concept enclosingConcept, TupleInput input) throws IOException {
        super(enclosingConcept, input);
    }

    public LongMember(ERefsetLongMember refsetMember, Concept enclosingConcept) throws IOException {
        super(refsetMember, enclosingConcept);
        longValue = refsetMember.getLongValue();
        if (refsetMember.getRevisionList() != null) {
            revisions = new CopyOnWriteArrayList<LongRevision>();
            for (ERefsetLongRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new LongRevision(eVersion, this));
            }
        }
    }

    public LongMember() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (LongMember.class.isAssignableFrom(obj.getClass())) {
            LongMember another = (LongMember) obj;
            return this.nid == another.nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { this.nid });
    }

    @Override
    protected boolean membersEqual(ConceptComponent<LongRevision, LongMember> obj) {
        if (LongMember.class.isAssignableFrom(obj.getClass())) {
            LongMember another = (LongMember) obj;
            return this.longValue == another.longValue;
        }
        return false;
    }

    @Override
    protected final LongRevision readMemberRevision(TupleInput input) {
        return new LongRevision(input, this);
    }

    @Override
    protected void readMemberFields(TupleInput input) {
        longValue = input.readLong();
    }

    @Override
    protected void writeMember(TupleOutput output) {
        output.writeLong(longValue);
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
        LongRevision newR = new LongRevision(statusNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public LongRevision makeAnalog() {
        LongRevision newR = new LongRevision(getStatusId(), getPathId(), getTime(), this);
        return newR;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
        modified();
    }

    @Override
    public int getTypeId() {
        return REFSET_TYPES.LONG.getTypeNid();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" longValue:" + this.longValue);
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
            if (getTime() != Long.MIN_VALUE) {
                list.add(new Version());
            }
            if (revisions != null) {
                for (int i = 0; i < revisions.size(); i++) {
                    if (revisions.get(i).getTime() != Long.MIN_VALUE) {
                        list.add(new Version(i));
                    }
                }
            }
            versions = list;
        }
        return (List<Version>) versions;
    }

}
