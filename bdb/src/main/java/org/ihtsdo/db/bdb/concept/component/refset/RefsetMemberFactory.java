package org.ihtsdo.db.bdb.concept.component.refset;

import java.io.IOException;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refsetmember.Boolean.BooleanMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cid.CidMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidCid.CidCidMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidCid.CidCidCidMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidStr.CidCidStrMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidFloat.CidFloatMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidInt.CidIntMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidLong.CidLongMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidStr.CidStrMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.integer.IntMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membership.MembershipMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.str.StrMember;
import org.ihtsdo.etypes.ERefset;
import org.ihtsdo.etypes.ERefsetBooleanMember;
import org.ihtsdo.etypes.ERefsetCidCidCidMember;
import org.ihtsdo.etypes.ERefsetCidCidMember;
import org.ihtsdo.etypes.ERefsetCidCidStrMember;
import org.ihtsdo.etypes.ERefsetCidFloatMember;
import org.ihtsdo.etypes.ERefsetCidIntMember;
import org.ihtsdo.etypes.ERefsetCidLongMember;
import org.ihtsdo.etypes.ERefsetCidMember;
import org.ihtsdo.etypes.ERefsetCidStrMember;
import org.ihtsdo.etypes.ERefsetIntMember;
import org.ihtsdo.etypes.ERefsetStrMember;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;

public class RefsetMemberFactory  {

	@SuppressWarnings("unchecked")
	public RefsetMember create(int nid, int partCount, boolean editable,
			TupleInput input, int refsetNid) {
		int typeNid = input.readInt();
		REFSET_TYPES memberType;
		try {
			memberType = REFSET_TYPES.nidToType(typeNid);
		} catch (TerminologyException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		switch (memberType) {
		case BOOLEAN:
			return new BooleanMember(nid, partCount, editable, refsetNid);
		case CID:
			return new CidMember(nid, partCount, editable, refsetNid);
		case CID_CID:
			return new CidCidMember(nid, partCount, editable, refsetNid);
		case CID_CID_CID:
			return new CidCidCidMember(nid, partCount, editable, refsetNid);
		case CID_CID_STR:
			return new CidCidStrMember(nid, partCount, editable, refsetNid);
		case CID_INT:
			return new CidIntMember(nid, partCount, editable, refsetNid);
		case CID_STR:
			return new CidStrMember(nid, partCount, editable, refsetNid);
		case INT:
			return new IntMember(nid, partCount, editable, refsetNid);
		case CID_FLOAT:
			return new CidFloatMember(nid, partCount, editable, refsetNid);
		case MEMBER:
			return new MembershipMember(nid, partCount, editable, refsetNid);
		case STR:
			return new StrMember(nid, partCount, editable, refsetNid);
		case CID_LONG:
			return new CidLongMember(nid, partCount, editable, refsetNid);

		default:
			throw new UnsupportedOperationException(
					"Can't handle member type: " + memberType);
		}
	}
	
	public static RefsetMember<?,?> create(ERefset refsetMember) {
		switch (refsetMember.getType()) {
		case BOOLEAN:
			return new BooleanMember((ERefsetBooleanMember) refsetMember);
		case CID:
			return new CidMember((ERefsetCidMember) refsetMember);
		case CID_CID:
			return new CidCidMember((ERefsetCidCidMember) refsetMember);
		case CID_CID_CID:
			return new CidCidCidMember((ERefsetCidCidCidMember) refsetMember);
		case CID_CID_STR:
			return new CidCidStrMember((ERefsetCidCidStrMember) refsetMember);
		case CID_INT:
			return new CidIntMember((ERefsetCidIntMember) refsetMember);
		case CID_STR:
			return new CidStrMember((ERefsetCidStrMember) refsetMember);
		case INT:
			return new IntMember((ERefsetIntMember) refsetMember);
		case CID_FLOAT:
			return new CidFloatMember((ERefsetCidFloatMember) refsetMember);
		case MEMBER:
			return new MembershipMember(refsetMember);
		case STR:
			return new StrMember((ERefsetStrMember) refsetMember);
		case CID_LONG:
			return new CidLongMember((ERefsetCidLongMember) refsetMember);

		default:
			throw new UnsupportedOperationException(
					"Can't handle member type: " + refsetMember.getType());
		}
	}

}