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

public class RefsetMemberFactory {

    @SuppressWarnings("unchecked")
    public RefsetMember create(int nid, int typeNid, int enclosingConceptNid,
            TupleInput input) throws IOException {
        REFSET_TYPES memberType;
        try {
            assert Arrays.asList(REFSET_TYPES.values()).contains(REFSET_TYPES.nidToType(typeNid));
            memberType = REFSET_TYPES.nidToType(typeNid);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert memberType != null : " Member type null for nid: " + nid;
        switch (memberType) {
            case BOOLEAN:
                return new BooleanMember(enclosingConceptNid, input);
            case CID:
                return new CidMember(enclosingConceptNid, input);
            case CID_CID:
                return new CidCidMember(enclosingConceptNid, input);
            case CID_CID_CID:
                return new CidCidCidMember(enclosingConceptNid, input);
            case CID_CID_STR:
                return new CidCidStrMember(enclosingConceptNid, input);
            case CID_INT:
                return new CidIntMember(enclosingConceptNid, input);
            case CID_STR:
                return new CidStrMember(enclosingConceptNid, input);
            case INT:
                return new IntMember(enclosingConceptNid, input);
            case CID_FLOAT:
                return new CidFloatMember(enclosingConceptNid, input);
            case MEMBER:
                return new MembershipMember(enclosingConceptNid, input);
            case STR:
                return new StrMember(enclosingConceptNid, input);
            case CID_LONG:
                return new CidLongMember(enclosingConceptNid, input);
            case LONG:
                return new LongMember(enclosingConceptNid, input);

            default:
                throw new UnsupportedOperationException(
                        "Can't handle member type: " + memberType);
        }
    }

    public static RefsetMember<?, ?> create(TkRefsetAbstractMember<?> refsetMember, int enclosingConceptNid) throws IOException {
        switch (refsetMember.getType()) {
            case BOOLEAN:
                return new BooleanMember((TkRefsetBooleanMember) refsetMember, enclosingConceptNid);
            case CID:
                return new CidMember((TkRefsetCidMember) refsetMember, enclosingConceptNid);
            case CID_CID:
                return new CidCidMember((TkRefsetCidCidMember) refsetMember, enclosingConceptNid);
            case CID_CID_CID:
                return new CidCidCidMember((TkRefsetCidCidCidMember) refsetMember, enclosingConceptNid);
            case CID_CID_STR:
                return new CidCidStrMember((TkRefsetCidCidStrMember) refsetMember, enclosingConceptNid);
            case CID_INT:
                return new CidIntMember((TkRefsetCidIntMember) refsetMember, enclosingConceptNid);
            case CID_STR:
                return new CidStrMember((TkRefsetCidStrMember) refsetMember, enclosingConceptNid);
            case INT:
                return new IntMember((TkRefsetIntMember) refsetMember, enclosingConceptNid);
            case CID_FLOAT:
                return new CidFloatMember((TkRefsetCidFloatMember) refsetMember, enclosingConceptNid);
            case MEMBER:
                return new MembershipMember((TkRefsetMember) refsetMember, enclosingConceptNid);
            case STR:
                return new StrMember((TkRefsetStrMember) refsetMember, enclosingConceptNid);
            case CID_LONG:
                return new CidLongMember((TkRefsetCidLongMember) refsetMember, enclosingConceptNid);
            case LONG:
                return new LongMember((TkRefsetLongMember) refsetMember, enclosingConceptNid);

            default:
                throw new UnsupportedOperationException(
                        "Can't handle member type: " + refsetMember.getType());
        }
    }
}