package org.ihtsdo.db.bdb.concept.component.refset;

import java.io.IOException;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.ComponentFactory;
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
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;

@SuppressWarnings("unchecked")
public class RefsetMemberFactory
	extends
		ComponentFactory {

	@Override
	public RefsetMember create(int nid, int partCount, boolean editable,
			TupleInput input) {
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
			return new BooleanMember(nid, partCount, editable);
		case CID:
			return new CidMember(nid, partCount, editable);
		case CID_CID:
			return new CidCidMember(nid, partCount, editable);
		case CID_CID_CID:
			return new CidCidCidMember(nid, partCount, editable);
		case CID_CID_STR:
			return new CidCidStrMember(nid, partCount, editable);
		case CID_INT:
			return new CidIntMember(nid, partCount, editable);
		case CID_STR:
			return new CidStrMember(nid, partCount, editable);
		case INT:
			return new IntMember(nid, partCount, editable);
		case CID_FLOAT:
			return new CidFloatMember(nid, partCount, editable);
		case MEMBER:
			return new MembershipMember(nid, partCount, editable);
		case STR:
			return new StrMember(nid, partCount, editable);
		case CID_LONG:
			return new CidLongMember(nid, partCount, editable);

		default:
			throw new UnsupportedOperationException(
					"Can't handle member type: " + memberType);
		}
	}
}