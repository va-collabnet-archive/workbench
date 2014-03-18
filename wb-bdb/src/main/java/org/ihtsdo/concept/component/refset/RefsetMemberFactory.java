package org.ihtsdo.concept.component.refset;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Arrays;

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
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_long.TkRefexLongMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid.TkRefexUuidUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_uuid.TkRefexUuidUuidUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_string.TkRefexUuidUuidStringMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float.TkRefexUuidFloatMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long.TkRefexUuidLongMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_string.TkRefexUuidStringMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_member.TkRefexMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;

import com.sleepycat.bind.tuple.TupleInput;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_ManageConceptData;
import org.ihtsdo.concept.component.refsetmember.array.bytearray.ArrayOfBytearrayMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.dto.concept.component.refex.type_array_of_bytearray.TkRefexArrayOfBytearrayMember;

public class RefsetMemberFactory {

    public static RefsetMember<?, ?> reCreate(RefexCAB res, RefsetMember<?, ?> member, EditCoordinate ec) throws IOException, InvalidCAB {
        Concept refexColCon = (Concept) Ts.get().getConcept(res.getRefexCollectionNid());
        member.refsetNid = refexColCon.getNid();
        member.nid = Bdb.uuidToNid(res.getMemberUUID());
        Integer rcNid = res.getReferencedComponentNid();
        if(rcNid == null){
            rcNid = Ts.get().getNidForUuids(res.getReferencedComponentUuid());
        }
        if (refexColCon.isAnnotationStyleRefex()) {
            member.enclosingConceptNid = Ts.get().getConceptNidForNid(rcNid);
            Bdb.getNidCNidMap().setCNidForNid(member.enclosingConceptNid, member.nid);
        } else {
            member.enclosingConceptNid = refexColCon.getNid();
            Bdb.getNidCNidMap().setCNidForNid(member.enclosingConceptNid, member.nid);
        }
        for (int i = 0; i < ec.getEditPaths().length; i++) {
            if (i == 0) {
                member.setStatusAtPositionNid(
                        Bdb.getSapNid(res.getInt(RefexProperty.STATUS_NID),
                        Long.MAX_VALUE,
                        ec.getAuthorNid(),
                        ec.getModuleNid(),
                        ec.getEditPaths()[i]));
                member.setPrimordialUuid(res.getMemberUUID());
                try {
                    res.setPropertiesExceptSap(member);
                } catch (PropertyVetoException ex) {
                    throw new InvalidCAB("RefexAmendmentSpec: " + res, ex);
                }

            } else {
                member.makeAnalog(res.getInt(RefexProperty.STATUS_NID),
                        Long.MAX_VALUE,
                        ec.getAuthorNid(),
                        ec.getModuleNid(),
                        ec.getEditPaths()[i]);
            }

            if (refexColCon.isAnnotationIndex()) {
                refexColCon.getData().getMemberNids().add(member.nid);
            }else {
                refexColCon.getData().add(member);
            }
        }        
        return member;
    }

    @SuppressWarnings("rawtypes")
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
            case ARRAY_OF_BYTEARRAY:
                return new ArrayOfBytearrayMember(enclosingConceptNid, input);
            default:
                throw new UnsupportedOperationException(
                        "Can't handle member type: " + memberType);
        }
    }

    public static RefsetMember<?, ?> create(TkRefexAbstractMember<?> refsetMember, int enclosingConceptNid) throws IOException {
        switch (refsetMember.getType()) {
            case BOOLEAN:
                return new BooleanMember((TkRefexBooleanMember) refsetMember, enclosingConceptNid);
            case CID:
                return new CidMember((TkRefexUuidMember) refsetMember, enclosingConceptNid);
            case CID_CID:
                return new CidCidMember((TkRefexUuidUuidMember) refsetMember, enclosingConceptNid);
            case CID_CID_CID:
                return new CidCidCidMember((TkRefexUuidUuidUuidMember) refsetMember, enclosingConceptNid);
            case CID_CID_STR:
                return new CidCidStrMember((TkRefexUuidUuidStringMember) refsetMember, enclosingConceptNid);
            case CID_INT:
                return new CidIntMember((TkRefexUuidIntMember) refsetMember, enclosingConceptNid);
            case CID_STR:
                return new CidStrMember((TkRefexUuidStringMember) refsetMember, enclosingConceptNid);
            case INT:
                return new IntMember((TkRefexIntMember) refsetMember, enclosingConceptNid);
            case CID_FLOAT:
                return new CidFloatMember((TkRefexUuidFloatMember) refsetMember, enclosingConceptNid);
            case MEMBER:
                return new MembershipMember((TkRefexMember) refsetMember, enclosingConceptNid);
            case STR:
                return new StrMember((TkRefsetStrMember) refsetMember, enclosingConceptNid);
            case CID_LONG:
                return new CidLongMember((TkRefexUuidLongMember) refsetMember, enclosingConceptNid);
            case LONG:
                return new LongMember((TkRefexLongMember) refsetMember, enclosingConceptNid);
            case ARRAY_BYTEARRAY:
                return new ArrayOfBytearrayMember((TkRefexArrayOfBytearrayMember) refsetMember, enclosingConceptNid);
            default:
                throw new UnsupportedOperationException(
                        "Can't handle member type: " + refsetMember.getType());
        }
    }

    public static RefsetMember<?, ?> createNoTx(RefexCAB res,
            EditCoordinate ec, long time)
            throws IOException, InvalidCAB {
        TerminologyStoreDI ts = Ts.get();
        RefsetMember<?, ?> member = createBlank(res);
        Concept refexColCon = (Concept) ts.getConcept(res.getRefexCollectionNid());
        int refexNid = Bdb.uuidToNid(res.getMemberUUID());
        member.nid = refexNid;
        if (refexColCon.isAnnotationStyleRefex()) {
            int rcNid = ts.getNidForUuids(res.getReferencedComponentUuid());
            member.enclosingConceptNid = ts.getConceptNidForNid(rcNid);
            Bdb.getNidCNidMap().setCNidForNid(member.enclosingConceptNid, refexNid);
            ts.getComponent(res.getReferencedComponentUuid()).addAnnotation(member);
        } else {
            member.enclosingConceptNid = refexColCon.getNid();
            Bdb.getNidCNidMap().setCNidForNid(member.enclosingConceptNid, refexNid);
            refexColCon.getData().add(member);
        }
        for (int i = 0; i < ec.getEditPaths().length; i++) {
            if (i == 0) {
                member.setStatusAtPositionNid(
                        Bdb.getSapNid(res.getInt(RefexProperty.STATUS_NID),
                        time,
                        ec.getAuthorNid(),
                        ec.getModuleNid(),
                        ec.getEditPaths()[i]));
                member.setPrimordialUuid(res.getMemberUUID());
                try {
                    res.setPropertiesExceptSap(member);
                } catch (PropertyVetoException ex) {
                    throw new InvalidCAB("RefexAmendmentSpec: " + res, ex);
                }

            } else {
                member.makeAnalog(res.getInt(RefexProperty.STATUS_NID),
                        time,
                        ec.getAuthorNid(),
                        ec.getModuleNid(),
                        ec.getEditPaths()[i]);
            }

        }
        if (refexColCon.isAnnotationStyleRefex()) {
            int rcNid = ts.getNidForUuids(res.getReferencedComponentUuid());
            Bdb.getConceptDb().writeConcept(
                   (Concept) ts.getConcept(ts.getConceptNidForNid(rcNid)));
        } else {
            Bdb.getConceptDb().writeConcept(refexColCon);
        }
        return member;
    }

    public static RefsetMember<?, ?> create(RefexCAB res,
            EditCoordinate ec)
            throws IOException, InvalidCAB {
        RefsetMember<?, ?> member = createBlank(res);
        return reCreate(res, member, ec);
    }

    private static RefsetMember<?, ?> createBlank(RefexCAB res) {
        switch (res.getMemberType()) {
            case BOOLEAN:
                return new BooleanMember();
            case CID:
                return new CidMember();
            case CID_CID:
                return new CidCidMember();
            case CID_CID_CID:
                return new CidCidCidMember();
            case CID_CID_STR:
                return new CidCidStrMember();
            case CID_INT:
                return new CidIntMember();
            case CID_STR:
                return new CidStrMember();
            case INT:
                return new IntMember();
            case CID_FLOAT:
                return new CidFloatMember();
            case MEMBER:
                return new MembershipMember();
            case STR:
                return new StrMember();
            case CID_LONG:
                return new CidLongMember();
            case LONG:
                return new LongMember();
            case ARRAY_BYTEARRAY:
                return new ArrayOfBytearrayMember();

            default:
                throw new UnsupportedOperationException(
                        "Can't handle member type: " + res.getMemberType());
        }

    }
}
