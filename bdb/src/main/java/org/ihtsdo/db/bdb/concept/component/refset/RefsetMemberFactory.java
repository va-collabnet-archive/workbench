package org.ihtsdo.db.bdb.concept.component.refset;

import org.ihtsdo.db.bdb.concept.component.ComponentFactory;
import org.ihtsdo.db.bdb.concept.component.refset.AbstractRefsetMember.REFSET_MEMBER_TYPE;
import org.ihtsdo.db.bdb.concept.component.refsetmember.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cid.CidMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidCid.CidCidMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidCid.CidCidCidMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidStr.CidCidStrMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidInt.CidIntMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidStr.CidStrMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.crossMap.CrossMapMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.crossMapForRel.CrossMapForRelMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.floatConcept.FloatCidMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.integer.IntMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.language.LangMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.scopedLanguage.ScopedLangMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.string.StrMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.template.TemplateMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.templateForRel.TemplateForRelMember;

import com.sleepycat.bind.tuple.TupleInput;

public class RefsetMemberFactory extends
		ComponentFactory<AbstractRefsetMember, RefsetMemberMutablePart> {

	@Override
	public AbstractRefsetMember create(int nid, int partCount, boolean editable,
			TupleInput input) {
		REFSET_MEMBER_TYPE memberType = REFSET_MEMBER_TYPE.readType(input);
		switch (memberType) {
		case BOOLEAN:
			throw new UnsupportedOperationException();
		case CID:
			return new CidMember(nid, partCount, editable);
		case CID_CID:
			return new CidCidMember(nid, partCount, editable);
		case CID_CID_CID:
			return new CidCidCidMember(nid, partCount, editable);
		case CID_CID_STRING:
			return new CidCidStrMember(nid, partCount, editable);
		case CID_INT:
			return new CidIntMember(nid, partCount, editable);
		case CID_STRING:
			return new CidStrMember(nid, partCount, editable);
		case CROSS_MAP:
			return new CrossMapMember(nid, partCount, editable);
		case CROSS_MAP_FOR_REL:
			return new CrossMapForRelMember(nid, partCount, editable);
		case INTEGER:
			return new IntMember(nid, partCount, editable);
		case LANGUAGE:
			return new LangMember(nid, partCount, editable);
		case MEASUREMENT:
			return new FloatCidMember(nid, partCount, editable);
		case MEMBER:
			return new RefsetMember(nid, partCount, editable);
		case SCOPED_LANGUAGE:
			return new ScopedLangMember(nid, partCount, editable);
		case STRING:
			return new StrMember(nid, partCount, editable);
		case TEMPLATE:
			return new TemplateMember(nid, partCount, editable);
		case TEMPLATE_FOR_REL:
			return new TemplateForRelMember(nid, partCount, editable);

		default:
			throw new UnsupportedOperationException(
					"Can't handle member type: " + memberType);
		}
	}

}