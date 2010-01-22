package org.ihtsdo.db.bdb.concept.component.refsetmember.cid;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetCidMember;
import org.ihtsdo.etypes.ERefsetCidVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidMember extends RefsetMember<CidRevisioin, CidMember> implements I_ThinExtByRefPartConcept {

	private int c1Nid;

	public CidMember(Concept enclosingConcept, 
			TupleInput input) {
		super(enclosingConcept, input);
	}


	public CidMember(ERefsetCidMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		if (refsetMember.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<CidRevisioin>(refsetMember.getExtraVersionsList().size());
			for (ERefsetCidVersion eVersion: refsetMember.getExtraVersionsList()) {
				additionalVersions.add(new CidRevisioin(eVersion, this));
			}
		}
	}

	@Override
	protected boolean membersEqual(
			ConceptComponent<CidRevisioin, CidMember> obj) {
		if (CidMember.class.isAssignableFrom(obj.getClass())) {
			CidMember another = (CidMember) obj;
			return this.c1Nid == another.c1Nid;
		}
		return false;
	}

	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (additionalVersions == null) {
			additionalVersions = new ArrayList<CidRevisioin>(
					additionalVersionCount);
		} else {
			additionalVersions.ensureCapacity(additionalVersions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			additionalVersions.add(new CidRevisioin(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		c1Nid = input.readInt();

	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
	}

	@Override
	protected String getTypeFieldsString() {
		StringBuffer buf = new StringBuffer();
		buf.append("cNid: ");
		addNidToBuffer(buf, c1Nid);
		return buf.toString();
	}



	@Override
	protected ArrayIntList getVariableVersionNids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
	}
	
	@Override
	public int getTypeId() {
		return REFSET_TYPES.CID.getTypeNid();
	}


	@Override
	@Deprecated
	public int getC1id() {
		return getC1Nid();
	}


	@Override
	@Deprecated
	public int getConceptId() {
		return getC1Nid();
	}


	@Override
	@Deprecated
	public void setC1id(int c1id) {
		setC1Nid(c1id);
	}


	@Override
	public void setConceptId(int conceptId) {
		setConceptId(conceptId);
	}

	@Override
	public int compareTo(I_ThinExtByRefPart o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefPart makePromotionPart(I_Path promotionPath) {
		throw new UnsupportedOperationException();
	}
	
	public I_ThinExtByRefPart duplicate() {
		throw new UnsupportedOperationException();
	}


}
