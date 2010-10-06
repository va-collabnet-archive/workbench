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
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.Boolean.TkRefsetBooleanMember;
import org.ihtsdo.tk.dto.concept.component.refset.Long.TkRefsetLongMember;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcid.TkRefsetCidCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcidcid.TkRefsetCidCidCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcidstr.TkRefsetCidCidStrMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidflt.TkRefsetCidFloatMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidint.TkRefsetCidIntMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidlong.TkRefsetCidLongMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidstr.TkRefsetCidStrMember;
import org.ihtsdo.tk.dto.concept.component.refset.integer.TkRefsetIntMember;
import org.ihtsdo.tk.dto.concept.component.refset.member.TkRefsetMember;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrMember;

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
	
	public static RefsetMember<?,?> create(TkRefsetAbstractMember<?> refsetMember, Concept enclosingConcept) throws IOException {
		assert enclosingConcept != null;
		switch (refsetMember.getType()) {
		case BOOLEAN:
			return new BooleanMember((TkRefsetBooleanMember) refsetMember, enclosingConcept);
		case CID:
			return new CidMember((TkRefsetCidMember) refsetMember, enclosingConcept);
		case CID_CID:
			return new CidCidMember((TkRefsetCidCidMember) refsetMember, enclosingConcept);
		case CID_CID_CID:
			return new CidCidCidMember((TkRefsetCidCidCidMember) refsetMember, enclosingConcept);
		case CID_CID_STR:
			return new CidCidStrMember((TkRefsetCidCidStrMember) refsetMember, enclosingConcept);
		case CID_INT:
			return new CidIntMember((TkRefsetCidIntMember) refsetMember, enclosingConcept);
		case CID_STR:
			return new CidStrMember((TkRefsetCidStrMember) refsetMember, enclosingConcept);
		case INT:
			return new IntMember((TkRefsetIntMember) refsetMember, enclosingConcept);
		case CID_FLOAT:
			return new CidFloatMember((TkRefsetCidFloatMember) refsetMember, enclosingConcept);
		case MEMBER:
			return new MembershipMember((TkRefsetMember) refsetMember, enclosingConcept);
		case STR:
			return new StrMember((TkRefsetStrMember) refsetMember, enclosingConcept);
		case CID_LONG:
			return new CidLongMember((TkRefsetCidLongMember) refsetMember, enclosingConcept);
		case LONG:
			return new LongMember((TkRefsetLongMember) refsetMember, enclosingConcept);

		default:
			throw new UnsupportedOperationException(
					"Can't handle member type: " + refsetMember.getType());
		}
	}

}