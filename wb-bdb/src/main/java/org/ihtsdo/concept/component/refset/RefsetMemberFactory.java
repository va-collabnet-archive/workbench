package org.ihtsdo.concept.component.refset;

import java.io.IOException;
import java.util.Arrays;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.refsetmember.Boolean.BooleanMember;
import org.ihtsdo.concept.component.refsetmember.Long.LongMember;
import org.ihtsdo.concept.component.refsetmember.cid.CidMember;
import org.ihtsdo.concept.component.refsetmember.cidCid.CidCidMember;
import org.ihtsdo.concept.component.refsetmember.cidCidCid.CidCidCidMember;
import org.ihtsdo.concept.component.refsetmember.cidCidStr.CidCidStrMember;
import org.ihtsdo.concept.component.refsetmember.cidFloat.CidFloatMember;
import org.ihtsdo.concept.component.refsetmember.cidInt.CidIntMember;
import org.ihtsdo.concept.component.refsetmember.cidLong.CidLongMember;
import org.ihtsdo.concept.component.refsetmember.cidStr.CidStrMember;
import org.ihtsdo.concept.component.refsetmember.integer.IntMember;
import org.ihtsdo.concept.component.refsetmember.membership.MembershipMember;
import org.ihtsdo.concept.component.refsetmember.str.StrMember;
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
import org.ihtsdo.etypes.ERefsetLongMember;
import org.ihtsdo.etypes.ERefsetMember;
import org.ihtsdo.etypes.ERefsetMemberMember;
import org.ihtsdo.etypes.ERefsetStrMember;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;

public class RefsetMemberFactory  {

	@SuppressWarnings("unchecked")
	public RefsetMember create(int nid, int typeNid, Concept enclosingConcept,
			TupleInput input) throws IOException {
		assert enclosingConcept != null;
		REFSET_TYPES memberType;
		try {
			assert Arrays.asList(REFSET_TYPES.values()).contains(REFSET_TYPES.nidToType(typeNid));
			memberType = REFSET_TYPES.nidToType(typeNid);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		assert memberType != null: " Member type null for nid: " + nid;
		switch (memberType) {
		case BOOLEAN:
			return new BooleanMember(enclosingConcept, input);
		case CID:
			return new CidMember(enclosingConcept, input);
		case CID_CID:
			return new CidCidMember(enclosingConcept, input);
		case CID_CID_CID:
			return new CidCidCidMember(enclosingConcept, input);
		case CID_CID_STR:
			return new CidCidStrMember(enclosingConcept, input);
		case CID_INT:
			return new CidIntMember(enclosingConcept, input);
		case CID_STR:
			return new CidStrMember(enclosingConcept, input);
		case INT:
			return new IntMember(enclosingConcept, input);
		case CID_FLOAT:
			return new CidFloatMember(enclosingConcept, input);
		case MEMBER:
			return new MembershipMember(enclosingConcept, input);
		case STR:
			return new StrMember(enclosingConcept, input);
		case CID_LONG:
			return new CidLongMember(enclosingConcept, input);
		case LONG:
			return new LongMember(enclosingConcept, input);

		default:
			throw new UnsupportedOperationException(
					"Can't handle member type: " + memberType);
		}
	}
	
	public static RefsetMember<?,?> create(ERefsetMember<?> refsetMember, Concept enclosingConcept) throws IOException {
		assert enclosingConcept != null;
		switch (refsetMember.getType()) {
		case BOOLEAN:
			return new BooleanMember((ERefsetBooleanMember) refsetMember, enclosingConcept);
		case CID:
			return new CidMember((ERefsetCidMember) refsetMember, enclosingConcept);
		case CID_CID:
			return new CidCidMember((ERefsetCidCidMember) refsetMember, enclosingConcept);
		case CID_CID_CID:
			return new CidCidCidMember((ERefsetCidCidCidMember) refsetMember, enclosingConcept);
		case CID_CID_STR:
			return new CidCidStrMember((ERefsetCidCidStrMember) refsetMember, enclosingConcept);
		case CID_INT:
			return new CidIntMember((ERefsetCidIntMember) refsetMember, enclosingConcept);
		case CID_STR:
			return new CidStrMember((ERefsetCidStrMember) refsetMember, enclosingConcept);
		case INT:
			return new IntMember((ERefsetIntMember) refsetMember, enclosingConcept);
		case CID_FLOAT:
			return new CidFloatMember((ERefsetCidFloatMember) refsetMember, enclosingConcept);
		case MEMBER:
			return new MembershipMember((ERefsetMemberMember) refsetMember, enclosingConcept);
		case STR:
			return new StrMember((ERefsetStrMember) refsetMember, enclosingConcept);
		case CID_LONG:
			return new CidLongMember((ERefsetCidLongMember) refsetMember, enclosingConcept);
		case LONG:
			return new LongMember((ERefsetLongMember) refsetMember, enclosingConcept);

		default:
			throw new UnsupportedOperationException(
					"Can't handle member type: " + refsetMember.getType());
		}
	}

}