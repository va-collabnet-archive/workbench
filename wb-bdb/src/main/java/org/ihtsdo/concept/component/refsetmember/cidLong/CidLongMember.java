package org.ihtsdo.concept.component.refsetmember.cidLong;

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
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.concept.component.refset.cidlong.TkRefsetCidLongMember;
import org.ihtsdo.tk.concept.component.refset.cidlong.TkRefsetCidLongRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidLongMember 
				extends RefsetMember<CidLongRevision, CidLongMember> {

	private static VersionComputer<RefsetMember<CidLongRevision, CidLongMember>.Version> computer = 
		new VersionComputer<RefsetMember<CidLongRevision, CidLongMember>.Version>();

	protected VersionComputer<RefsetMember<CidLongRevision, CidLongMember>.Version> getVersionComputer() {
		return computer;
	}

	private int c1Nid;
	private long longValue;

	public CidLongMember(Concept enclosingConcept, TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}

	public CidLongMember(TkRefsetCidLongMember refsetMember, Concept enclosingConcept) throws IOException {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		longValue = refsetMember.getLongValue();
		if (refsetMember.getRevisionList() != null) {
			revisions = new CopyOnWriteArrayList<CidLongRevision>();
			for (TkRefsetCidLongRevision eVersion: refsetMember.getRevisionList()) {
				revisions.add(new CidLongRevision(eVersion, this));
			}
		}
	}

    public CidLongMember() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidLongMember.class.isAssignableFrom(obj.getClass())) {
            CidLongMember another = (CidLongMember) obj;
            return this.c1Nid == another.c1Nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { c1Nid });
    } 
    
    @Override
	protected boolean membersEqual(
			ConceptComponent<CidLongRevision, CidLongMember> obj) {
		if (CidLongMember.class.isAssignableFrom(obj.getClass())) {
			CidLongMember another = (CidLongMember) obj;
			return this.c1Nid == another.c1Nid && this.longValue == another.longValue;
		}
		return false;
	}

	@Override
	protected final CidLongRevision readMemberRevision(TupleInput input) {
		return new CidLongRevision(input, this);
	}
	@Override
	protected void readMemberFields(TupleInput input) {
		c1Nid = input.readInt();
		longValue = input.readLong();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
		output.writeLong(longValue);
	}

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(3);
        variableNids.add(getC1Nid());
        return variableNids;
    }

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        if (getTime() == time && getPathId() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
		CidLongRevision newR = new CidLongRevision(statusNid, pathNid, time, this);
		addRevision(newR);
		return newR;
	}

    @Override
    public CidLongRevision makeAnalog() {
        CidLongRevision newR = new CidLongRevision(getStatusId(), getPathId(), getTime(), this);
        return newR;
    }

	
	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
        modified();
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
		return REFSET_TYPES.CID_LONG.getTypeNid();
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
        buf.append(" longValue:" + this.longValue);
        buf.append(super.toString());
        return buf.toString();
    }

}
