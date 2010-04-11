package org.ihtsdo.concept.component.refsetmember.cidFloat;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.ERefsetCidFloatMember;
import org.ihtsdo.etypes.ERefsetCidFloatRevision;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidFloatMember extends RefsetMember<CidFloatRevision, CidFloatMember> {

	private static VersionComputer<RefsetMember<CidFloatRevision, CidFloatMember>.Version> computer = 
		new VersionComputer<RefsetMember<CidFloatRevision, CidFloatMember>.Version>();

	protected VersionComputer<RefsetMember<CidFloatRevision, CidFloatMember>.Version> getVersionComputer() {
		return computer;
	}

	private int c1Nid;
	private float floatValue;

	public CidFloatMember(Concept enclosingConcept, TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}

	public CidFloatMember(ERefsetCidFloatMember refsetMember, Concept enclosingConcept) throws IOException {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		floatValue = refsetMember.getFloatValue();
		if (refsetMember.getRevisionList() != null) {
			revisions = new CopyOnWriteArrayList<CidFloatRevision>();
			for (ERefsetCidFloatRevision eVersion: refsetMember.getRevisionList()) {
				revisions.add(new CidFloatRevision(eVersion, this));
			}
		}
	}

    public CidFloatMember() {
        super();
    }
    
    @Override
	protected boolean membersEqual(
			ConceptComponent<CidFloatRevision, CidFloatMember> obj) {
		if (CidFloatMember.class.isAssignableFrom(obj.getClass())) {
			CidFloatMember another = (CidFloatMember) obj;
			return this.c1Nid == another.c1Nid && this.floatValue == another.floatValue;
		}
		return false;
	}


	@Override
	protected final CidFloatRevision readMemberRevision(TupleInput input) {
	    return new CidFloatRevision(input, this);
	}
	@Override
	protected void readMemberFields(TupleInput input) {
		c1Nid = input.readInt();
		floatValue = input.readFloat();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
		output.writeFloat(floatValue);
	}
    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(3);
        variableNids.add(getC1Nid());
        return variableNids;
    }

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		CidFloatRevision newR = new CidFloatRevision(statusNid, pathNid, time, this);
		addRevision(newR);
		return newR;
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
        modified();
	}

	public float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
        modified();
	}

	@Override
	public int getTypeId() {
		return REFSET_TYPES.CID_FLOAT.getTypeNid();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid: ");
		addNidToBuffer(buf, c1Nid);
        buf.append(" floatValue:" + this.floatValue);
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidFloatMember.class.isAssignableFrom(obj.getClass())) {
            CidFloatMember another = (CidFloatMember) obj;
            return this.c1Nid == another.c1Nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { c1Nid });
    }  
}
