package org.ihtsdo.concept.component.refsetmember.str;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetStrMember;
import org.ihtsdo.etypes.ERefsetStrRevision;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec.RefexProperty;
import org.ihtsdo.tk.api.refex.type_str.RefexStrAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class StrMember extends RefsetMember<StrRevision, StrMember>
        implements I_ExtendByRefPartStr<StrRevision>, RefexStrAnalogBI<StrRevision> {

    private static VersionComputer<RefsetMember<StrRevision, StrMember>.Version> computer =
            new VersionComputer<RefsetMember<StrRevision, StrMember>.Version>();

    protected VersionComputer<RefsetMember<StrRevision, StrMember>.Version> getVersionComputer() {
        return computer;
    }

    public class Version
            extends RefsetMember<StrRevision, StrMember>.Version
            implements I_ExtendByRefVersion<StrRevision>, I_ExtendByRefPartStr<StrRevision>,
            RefexStrAnalogBI<StrRevision> {

        private Version() {
            super();
        }

        private Version(int index) {
            super(index);
        }

        public int compareTo(I_ExtendByRefPart<StrRevision> o) {
            if (I_ExtendByRefPartStr.class.isAssignableFrom(o.getClass())) {
                I_ExtendByRefPartStr<StrRevision> another =
                        (I_ExtendByRefPartStr<StrRevision>) o;
                if (!this.getStringValue().equals(another.getStringValue())) {
                    return this.getStringValue().compareTo(another.getStringValue());
                }
            }
            return super.compareTo(o);
        }

        @Override
        public I_ExtendByRefPartStr<StrRevision> duplicate() {
            return (I_ExtendByRefPartStr<StrRevision>) super.duplicate();
        }

        @Override
        public String getStringValue() {
            if (index >= 0) {
                return revisions.get(index).getStringValue();
            }
            return StrMember.this.getStringValue();
        }

        @Override
        public String getStr1() {
            if (index >= 0) {
                return revisions.get(index).getStr1();
            }
            return StrMember.this.getStr1();
        }

        @Override
        public void setStr1(String str) throws PropertyVetoException {
            if (index >= 0) {
                revisions.get(index).setStr1(str);
            }
            StrMember.this.setStr1(str);
        }

        @Override
        public void setStringValue(String stringValue) {
            if (index >= 0) {
                revisions.get(index).setStringValue(stringValue);
            }
            StrMember.this.setStringValue(stringValue);
        }

        @Override
        public ERefsetStrMember getERefsetMember() throws TerminologyException, IOException {
            return new ERefsetStrMember(this);
        }

        @Override
        public ERefsetStrRevision getERefsetRevision() throws TerminologyException, IOException {
            return new ERefsetStrRevision(this);
        }
    }
    private String stringValue;

    public StrMember(int enclosingConceptNid, TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    public StrMember(TkRefsetStrMember refsetMember, int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        stringValue = refsetMember.getStrValue();
        if (refsetMember.getRevisionList() != null) {
            revisions = new CopyOnWriteArrayList<StrRevision>();
            for (TkRefsetStrRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new StrRevision(eVersion, this));
            }
        }
    }

    public StrMember() {
        super();
    }

    @Override
    protected boolean membersEqual(
            ConceptComponent<StrRevision, StrMember> obj) {
        if (StrMember.class.isAssignableFrom(obj.getClass())) {
            StrMember another = (StrMember) obj;
            return this.stringValue.equals(another.stringValue);
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (StrMember.class.isAssignableFrom(obj.getClass())) {
            StrMember another = (StrMember) obj;
            return this.nid == another.nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[]{this.nid});
    }

    @Override
    protected final StrRevision readMemberRevision(TupleInput input) {
        return new StrRevision(input, this);
    }

    @Override
    protected void readMemberFields(TupleInput input) {
        stringValue = input.readString();
    }

    @Override
    protected void writeMember(TupleOutput output) {
        output.writeString(stringValue);
    }

    @Override
    protected ArrayIntList getVariableVersionNids() {
        return new ArrayIntList(2);
    }

    @Override
    public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        StrRevision newR = new StrRevision(statusNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public StrRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        StrRevision newR = new StrRevision(statusNid, authorNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public StrRevision makeAnalog() {
        StrRevision newR = new StrRevision(getStatusNid(), getPathNid(), getTime(), this);
        return newR;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
        modified();
    }

    @Override
    public int getTypeId() {
        return REFSET_TYPES.STR.getTypeNid();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getClass().getSimpleName()).append(": ");
        buf.append(" stringValue: '").append(this.stringValue).append("'");
        buf.append("; ");
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public StrRevision duplicate() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Version> getVersions() {
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

    @Override
    public int getPartsHashCode() {
        return HashFunction.hashCode(new int[]{getStringValue().hashCode()});
    }

    @Override
    public void setStr1(String str) throws PropertyVetoException {
        this.stringValue = str;
        modified();
    }

    @Override
    public String getStr1() {
        return stringValue;
    }

    protected TK_REFSET_TYPE getTkRefsetType() {
        return TK_REFSET_TYPE.STR;
    }

    protected void addSpecProperties(RefexAmendmentSpec rcs) {
        rcs.with(RefexProperty.STRING1, getStr1());
    }
}
