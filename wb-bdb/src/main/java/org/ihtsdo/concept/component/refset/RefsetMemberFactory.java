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
import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;

public class RefsetMemberFactory {

    public static RefsetMember<?, ?> reCreate(RefexCAB res, RefsetMember<?, ?> member, EditCoordinate ec) throws IOException, InvalidCAB {
        Concept refexColCon = (Concept) Ts.get().getConcept(res.getRefexColNid());
        member.refsetNid = refexColCon.getNid();
        member.nid = Bdb.uuidToNid(res.getMemberUUID());
        if (refexColCon.isAnnotationStyleRefex()) {
            member.enclosingConceptNid = Ts.get().getConceptNidForNid(res.getRcNid());
            Bdb.getNidCNidMap().setCNidForNid(member.enclosingConceptNid, member.nid);
            Ts.get().getComponent(res.getRcNid()).addAnnotation(member);
        } else {
            member.enclosingConceptNid = refexColCon.getNid();
            Bdb.getNidCNidMap().setCNidForNid(member.enclosingConceptNid, member.nid);
            refexColCon.getData().add(member);
        }
        for (int i = 0; i < ec.getEditPaths().length; i++) {
            if (i == 0) {
                member.setStatusAtPositionNid(
                        Bdb.getSapNid(res.getInt(RefexProperty.STATUS_NID),
                        ec.getAuthorNid(),
                        ec.getEditPaths()[i],
                        Long.MAX_VALUE));
                member.primordialUNid =
                        Bdb.getUuidDb().addUuid(res.getMemberUUID());
                try {
                    res.setPropertiesExceptSap(member);
                } catch (PropertyVetoException ex) {
                    throw new InvalidCAB("RefexAmendmentSpec: " + res, ex);
                }

            } else {
                member.makeAnalog(res.getInt(RefexProperty.STATUS_NID),
                        ec.getAuthorNid(),
                        ec.getEditPaths()[i],
                        Long.MAX_VALUE);
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

    public static RefsetMember<?, ?> createNoTx(RefexCAB res,
            EditCoordinate ec, long time)
            throws IOException, InvalidCAB {
        RefsetMember<?, ?> member = createBlank(res);
        Concept refexColCon = (Concept) Ts.get().getConcept(res.getRefexColNid());
        int refexNid = Bdb.uuidToNid(res.getMemberUUID());
        member.nid = refexNid;
        if (refexColCon.isAnnotationStyleRefex()) {
            member.enclosingConceptNid = Ts.get().getConceptNidForNid(res.getRcNid());
            Bdb.getNidCNidMap().setCNidForNid(member.enclosingConceptNid, refexNid);
            Ts.get().getComponent(res.getRcNid()).addAnnotation(member);
        } else {
            member.enclosingConceptNid = refexColCon.getNid();
            Bdb.getNidCNidMap().setCNidForNid(member.enclosingConceptNid, refexNid);
            refexColCon.getData().add(member);
        }
        for (int i = 0; i < ec.getEditPaths().length; i++) {
            if (i == 0) {
                member.setStatusAtPositionNid(
                        Bdb.getSapNid(res.getInt(RefexProperty.STATUS_NID),
                        ec.getAuthorNid(),
                        ec.getEditPaths()[i],
                        time));
                member.primordialUNid =
                        Bdb.getUuidDb().addUuid(res.getMemberUUID());
                try {
                    res.setPropertiesExceptSap(member);
                } catch (PropertyVetoException ex) {
                    throw new InvalidCAB("RefexAmendmentSpec: " + res, ex);
                }

            } else {
                member.makeAnalog(res.getInt(RefexProperty.STATUS_NID),
                        ec.getAuthorNid(),
                        ec.getEditPaths()[i],
                        time);
            }

        }
        if (refexColCon.isAnnotationStyleRefex()) {
            Bdb.getConceptDb().writeConcept(
                    Bdb.getConcept(Bdb.getNidCNidMap().getCNid(res.getRcNid())));
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

            default:
                throw new UnsupportedOperationException(
                        "Can't handle member type: " + res.getMemberType());
        }

    }
}
