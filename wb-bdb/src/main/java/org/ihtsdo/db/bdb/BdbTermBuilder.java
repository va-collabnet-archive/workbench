package org.ihtsdo.db.bdb;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.attributes.ConceptAttributesRevision;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.description.DescriptionRevision;
import org.ihtsdo.concept.component.image.Image;
import org.ihtsdo.concept.component.image.ImageRevision;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refset.RefsetMemberFactory;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.concept.component.relationship.RelationshipRevision;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.blueprint.ConAttrAB;
import org.ihtsdo.tk.api.blueprint.DescCAB;

import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.blueprint.RelCAB;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.MediaCAB;
import org.ihtsdo.tk.api.conattr.ConAttrChronicleBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;

public class BdbTermBuilder implements TerminologyBuilderBI {

    EditCoordinate ec;
    ViewCoordinate vc;

    public BdbTermBuilder(EditCoordinate ec, ViewCoordinate vc) {
        this.ec = ec;
        this.vc = vc;
    }

    @Override
    public RefexChronicleBI<?> construct(RefexCAB blueprint)
            throws IOException, InvalidCAB, ContradictionException {
        RefsetMember<?, ?> refex = getRefex(blueprint);
        if (refex != null) {
            return updateRefex(refex, blueprint);
        }
        RefexChronicleBI<?> annot = createRefex(blueprint);
        ComponentChroncileBI<?> component = Ts.get().getComponent(annot.getReferencedComponentNid());
        component.addAnnotation(annot);
        return annot;
    }

    public ConceptAttributes getConAttr(ConAttrAB blueprint) throws IOException, InvalidCAB {
        ConceptAttributes cac = (ConceptAttributes) Ts.get().getConcept(blueprint.getComponentUuid()).getConAttrs();
        if (cac == null) {
            throw new InvalidCAB("ConAttrAB can only be used for amendment, not creation."
                    + " Use ConceptCB instead. " + blueprint);
        }
        return cac;
    }

    private RefexChronicleBI<?> updateRefex(RefsetMember<?, ?> member,
            RefexCAB blueprint) throws InvalidCAB, IOException, ContradictionException {
        for (int pathNid : ec.getEditPaths()) {
            RefsetRevision refexRevision =
                    member.makeAnalog(blueprint.getInt(RefexProperty.STATUS_NID),
                    ec.getAuthorNid(), pathNid, Long.MAX_VALUE);
            try {
                blueprint.setPropertiesExceptSap(refexRevision);
            } catch (PropertyVetoException ex) {
                throw new InvalidCAB("Refex: " + member
                        + "\n\nRefexAmendmentSpec: " + blueprint, ex);
            }
        }
        for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
            construct(annotBp);
        }
        return member;
    }

    private RefsetMember<?, ?> getRefex(RefexCAB blueprint)
            throws InvalidCAB, IOException {
        if (Ts.get().hasUuid(blueprint.getMemberUUID())) {
            ComponentChroncileBI<?> component =
                    Ts.get().getComponent(blueprint.getMemberUUID());
            if (component == null) {
                return null;
            }
            if (blueprint.getMemberType()
                    == TK_REFSET_TYPE.classToType(component.getClass())) {
                return (RefsetMember<?, ?>) component;
            } else {
                throw new InvalidCAB(
                        "Component exists of different type: "
                        + component + "\n\nRefexCAB: " + blueprint);
            }
        }
        return null;
    }

    @Override
    public RefexChronicleBI<?> constructIfNotCurrent(RefexCAB blueprint)
            throws IOException, InvalidCAB, ContradictionException {
        RefsetMember<?, ?> refex = getRefex(blueprint);
        if (refex != null) {
            if (refex.getSapNid() == -1) {
                return reCreateRefex(refex, blueprint);
            } else {
                boolean current = false;
                for (RefexVersionBI refexv : refex.getVersions(vc)) {
                    if (blueprint.validate(refexv)) {
                        current = true;
                        break;
                    }
                }
                if (current) {
                    return refex;
                }
                return updateRefex(refex, blueprint);
            }
        }

        return createRefex(blueprint);
    }

    private RefexChronicleBI<?> reCreateRefex(RefsetMember<?, ?> refex,
            RefexCAB blueprint)
            throws IOException, InvalidCAB {
        return RefsetMemberFactory.reCreate(blueprint, refex, ec);
    }

    private RefexChronicleBI<?> createRefex(RefexCAB blueprint)
            throws IOException, InvalidCAB, ContradictionException {
        for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
            construct(annotBp);
        }
        return RefsetMemberFactory.create(blueprint, ec);
    }

    private RelationshipChronicleBI getRel(RelCAB blueprint)
            throws InvalidCAB, IOException {
        if (Ts.get().hasUuid(blueprint.getComponentUuid())) {
            ComponentChroncileBI<?> component =
                    Ts.get().getComponent(blueprint.getComponentUuid());
            if (component == null) {
                return null;
            }
            if (component instanceof RelationshipChronicleBI) {
                return (RelationshipChronicleBI) component;
            } else {
                throw new InvalidCAB(
                        "Component exists of different type: "
                        + component + "\n\nRelCAB: " + blueprint);
            }
        }
        return null;
    }

    @Override
    public RelationshipChronicleBI construct(RelCAB blueprint) throws IOException, InvalidCAB, ContradictionException {
        RelationshipChronicleBI relc = getRel(blueprint);

        if (relc == null) {
            Concept c = (Concept) Ts.get().getConcept(blueprint.getSourceNid());
            Relationship r = new Relationship();
            relc = r;
            Bdb.gVersion.incrementAndGet();
            r.enclosingConceptNid = c.getNid();
            r.nid = Bdb.uuidToNid(blueprint.getComponentUuid());
            Bdb.getNidCNidMap().setCNidForNid(c.getNid(), r.nid);
            r.setPrimordialUuid(blueprint.getComponentUuid());
            try {
                r.setDestinationNid(blueprint.getDestNid());
            } catch (PropertyVetoException ex) {
                throw new IOException(ex);
            }
            r.setTypeNid(blueprint.getTypeNid());
            r.setRefinabilityId(blueprint.getRefinabilityNid());
            r.setCharacteristicId(blueprint.getCharacteristicNid());
            r.primordialSapNid = Integer.MIN_VALUE;
            r.setGroup(blueprint.getGroup());
            for (int p : ec.getEditPaths()) {
                if (r.primordialSapNid == Integer.MIN_VALUE) {
                    r.primordialSapNid =
                            Bdb.getSapDb().getSapNid(blueprint.getStatusNid(), ec.getAuthorNid(), p,
                            Long.MAX_VALUE);
                } else {
                    if (r.revisions == null) {
                        r.revisions = new RevisionSet(r.primordialSapNid);
                    }
                    r.revisions.add((RelationshipRevision) r.makeAnalog(blueprint.getStatusNid(),
                            ec.getAuthorNid(), p, Long.MAX_VALUE));
                }
            }
            c.getSourceRels().add(r);
            for (int p : ec.getEditPaths()) {
                for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
                    construct(annotBp);
                }
            }
            return r;
        } else {
            Relationship r = (Relationship) relc;
            for (int p : ec.getEditPaths()) {
                RelationshipRevision rv = r.makeAnalog(blueprint.getStatusNid(),
                        ec.getAuthorNid(),
                        p,
                        Long.MAX_VALUE);
                if (r.getDestinationNid() != blueprint.getDestNid()) {
                    throw new InvalidCAB(
                            "r.getDestinationNid() != spec.getDestNid(): "
                            + r.getDestinationNid() + " : " + blueprint.getDestNid());
                }
                rv.setTypeNid(blueprint.getTypeNid());
                rv.setRefinabilityNid(blueprint.getRefinabilityNid());
                rv.setCharacteristicNid(blueprint.getCharacteristicNid());
            }
            for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
                construct(annotBp);
            }
        }
        return relc;
    }

    @Override
    public RelationshipChronicleBI constructIfNotCurrent(RelCAB blueprint) throws IOException, InvalidCAB, ContradictionException {
        RelationshipChronicleBI relc = getRel(blueprint);
        if (relc == null) {
            return construct(blueprint);
        }
        Collection<? extends RelationshipVersionBI> relvs = relc.getVersions(vc);
        for (RelationshipVersionBI rv : relvs) {
            if (!blueprint.validate(rv)) {
                return construct(blueprint);
            }
        }
        return relc;
    }

    private DescriptionChronicleBI getDesc(DescCAB blueprint)
            throws InvalidCAB, IOException {
        if (Ts.get().hasUuid(blueprint.getComponentUuid())) {
            ComponentChroncileBI<?> component =
                    Ts.get().getComponent(blueprint.getComponentUuid());
            if (component == null) {
                return null;
            }
            if (component instanceof DescriptionChronicleBI) {
                return (DescriptionChronicleBI) component;
            } else {
                throw new InvalidCAB(
                        "Component exists of different type: "
                        + component + "\n\nDescCAB: " + blueprint);
            }
        }
        return null;
    }

    @Override
    public DescriptionChronicleBI constructIfNotCurrent(DescCAB blueprint) throws IOException,
            InvalidCAB, ContradictionException {
        DescriptionChronicleBI desc = getDesc(blueprint);
        if (desc == null) {
            return construct(blueprint);
        }
        Collection<? extends DescriptionVersionBI> descvs = desc.getVersions(vc);
        for (DescriptionVersionBI dv : descvs) {
            if (!blueprint.validate(dv)) {
                return construct(blueprint);
            }
        }
        return desc;
    }

    @Override
    public DescriptionChronicleBI construct(DescCAB blueprint) throws IOException, InvalidCAB, ContradictionException {
        DescriptionChronicleBI desc = getDesc(blueprint);

        if (desc == null) {
            int conceptNid = blueprint.getConceptNid();
            Concept c = (Concept) Ts.get().getConcept(blueprint.getConceptNid());
            Description d = new Description();
            desc = d;
            Bdb.gVersion.incrementAndGet();
            d.enclosingConceptNid = c.getNid();
            d.nid = Bdb.uuidToNid(blueprint.getComponentUuid());
            Bdb.getNidCNidMap().setCNidForNid(c.getNid(), d.nid);
            d.setPrimordialUuid(blueprint.getComponentUuid());
            d.setTypeNid(blueprint.getTypeNid());
            d.primordialSapNid = Integer.MIN_VALUE;
            d.setLang(blueprint.getLang());
            d.setText(blueprint.getText());
            d.setInitialCaseSignificant(blueprint.isInitialCaseSignificant());
            for (int p : ec.getEditPaths()) {
                if (d.primordialSapNid == Integer.MIN_VALUE) {
                    d.primordialSapNid =
                            Bdb.getSapDb().getSapNid(blueprint.getStatusNid(), ec.getAuthorNid(), p,
                            Long.MAX_VALUE);
                } else {
                    if (d.revisions == null) {
                        d.revisions = new RevisionSet(d.primordialSapNid);
                    }
                    d.revisions.add((DescriptionRevision) d.makeAnalog(blueprint.getStatusNid(),
                            ec.getAuthorNid(), p, Long.MAX_VALUE));
                }
            }
            c.getDescriptions().add(d);
            for (int p : ec.getEditPaths()) {
                for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
                    construct(annotBp);
                }
            }
            return d;
        } else {
            Description d = (Description) desc;
            for (int p : ec.getEditPaths()) {
                DescriptionRevision dr = d.makeAnalog(blueprint.getStatusNid(),
                        ec.getAuthorNid(),
                        p,
                        Long.MAX_VALUE);
                dr.setTypeNid(blueprint.getTypeNid());
                dr.setText(blueprint.getText());
                dr.setLang(blueprint.getLang());
                dr.setInitialCaseSignificant(blueprint.isInitialCaseSignificant());
                for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
                    construct(annotBp);
                }
            }
        }
        return desc;
    }

    private MediaChronicleBI getMedia(MediaCAB blueprint)
            throws InvalidCAB, IOException {
        if (Ts.get().hasUuid(blueprint.getComponentUuid())) {
            ComponentChroncileBI<?> component =
                    Ts.get().getComponent(blueprint.getComponentUuid());
            if (component == null) {
                return null;
            }
            if (component instanceof MediaChronicleBI) {
                return (MediaChronicleBI) component;
            } else {
                throw new InvalidCAB(
                        "Component exists of different type: "
                        + component + "\n\nMediaCAB: " + blueprint);
            }
        }
        return null;
    }

    @Override
    public MediaChronicleBI constructIfNotCurrent(MediaCAB blueprint) throws IOException,
            InvalidCAB, ContradictionException {
        MediaChronicleBI mediaC = getMedia(blueprint);
        if (mediaC == null) {
            return construct(blueprint);
        }
        Collection<? extends MediaVersionBI> mediaV = mediaC.getVersions(vc);
        for (MediaVersionBI dv : mediaV) {
            if (!blueprint.validate(dv)) {
                return construct(blueprint);
            }
        }
        return mediaC;
    }

    @Override
    public MediaChronicleBI construct(MediaCAB blueprint) throws IOException, InvalidCAB, ContradictionException {
        MediaChronicleBI imgC = getMedia(blueprint);

        if (imgC == null) {
            Concept c = (Concept) Ts.get().getConcept(blueprint.getConceptNid());
            Image img = new Image();
            imgC = img;
            Bdb.gVersion.incrementAndGet();
            img.enclosingConceptNid = c.getNid();
            img.nid = Bdb.uuidToNid(blueprint.getComponentUuid());
            Bdb.getNidCNidMap().setCNidForNid(c.getNid(), img.nid);
            img.setPrimordialUuid(blueprint.getComponentUuid());
            img.setTypeNid(blueprint.getTypeNid());
            img.setFormat(blueprint.getFormat());
            img.setImage(blueprint.getDataBytes());
            img.setTextDescription(blueprint.getTextDescription());
            img.primordialSapNid = Integer.MIN_VALUE;
            for (int p : ec.getEditPaths()) {
                if (img.primordialSapNid == Integer.MIN_VALUE) {
                    img.primordialSapNid =
                            Bdb.getSapDb().getSapNid(blueprint.getStatusNid(), ec.getAuthorNid(), p,
                            Long.MAX_VALUE);
                } else {
                    if (img.revisions == null) {
                        img.revisions = new RevisionSet(img.primordialSapNid);
                    }
                    img.revisions.add((ImageRevision) img.makeAnalog(blueprint.getStatusNid(),
                            ec.getAuthorNid(), p, Long.MAX_VALUE));
                }
            }
            c.getMedia().add(img);
            for (int p : ec.getEditPaths()) {
                for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
                    construct(annotBp);
                }
            }
            return img;
        } else {
            Image img = (Image) imgC;
            for (int p : ec.getEditPaths()) {
                ImageRevision imgR = img.makeAnalog(blueprint.getStatusNid(),
                        ec.getAuthorNid(),
                        p,
                        Long.MAX_VALUE);
                imgR.setTypeNid(blueprint.getTypeNid());
                imgR.setTextDescription(blueprint.getTextDescription());
                for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
                    construct(annotBp);
                }
            }
        }

        return imgC;
    }

    private ConceptChronicleBI getConcept(ConceptCB blueprint)
            throws InvalidCAB, IOException {
        if (Ts.get().hasUuid(blueprint.getComponentUuid())) {
            ComponentChroncileBI<?> component =
                    Ts.get().getComponent(blueprint.getComponentUuid());
            if (component == null) {
                return null;
            }
            if (component instanceof ConceptChronicleBI) {
                return (ConceptChronicleBI) component;
            } else {
                throw new InvalidCAB(
                        "Component exists of different type: "
                        + component + "\n\nConceptCAB: " + blueprint);
            }
        }
        return null;
    }

    @Override
    public ConceptChronicleBI constructIfNotCurrent(ConceptCB blueprint)
            throws IOException, InvalidCAB, ContradictionException {
        ConceptChronicleBI cc = getConcept(blueprint);
        if (cc == null) {
            return construct(blueprint);
        } else {
            I_GetConceptData concept = Terms.get().getConceptForNid(cc.getNid());
            if (concept.isCanceled() || concept.getPrimUuid().toString().length() == 0
                    || concept.getConAttrs().getVersions().isEmpty()) {
                return construct(blueprint);
            } else {
                throw new InvalidCAB(
                        "Concept already exists: "
                        + cc + "\n\nConceptCAB cannot be used for update: " + blueprint);
            }
        }
    }

    @Override
    public ConceptChronicleBI construct(ConceptCB blueprint) throws IOException, InvalidCAB, ContradictionException {

        int cNid = Bdb.uuidToNid(blueprint.getComponentUuid());
        Bdb.getNidCNidMap().setCNidForNid(cNid, cNid);
        Concept newC = Concept.get(cNid);

        ConceptAttributes a = null;
        if (newC.getConceptAttributes() == null) {
            a = new ConceptAttributes();
            a.nid = cNid;
            a.enclosingConceptNid = cNid;
            newC.setConceptAttributes(a);
        } else if (newC.isCanceled()) {
            a = newC.getConceptAttributes();
            for (int pathNid : ec.getEditPaths()) {
                a.resetUncommitted(blueprint.getStatusNid(), ec.getAuthorNid(), pathNid);
            }
            a.nid = cNid;
            a.enclosingConceptNid = cNid;
        } else {
            throw new InvalidCAB("Concept already exists:\n" + blueprint + "\n\n" + newC);
        }

        a.setDefined(blueprint.isDefined());
        a.setPrimordialUuid(blueprint.getComponentUuid());

        boolean primoridal = true;
        for (int p : ec.getEditPaths()) {
            if (primoridal) {
                primoridal = false;
                a.primordialSapNid =
                        Bdb.getSapDb().getSapNid(blueprint.getStatusNid(),
                        ec.getAuthorNid(), p, Long.MAX_VALUE);
            } else {
                if (a.revisions == null) {
                    a.revisions = new RevisionSet(a.primordialSapNid);
                }
                a.revisions.add((ConceptAttributesRevision) a.makeAnalog(blueprint.getStatusNid(),
                        ec.getAuthorNid(), p, Long.MAX_VALUE));
            }
        }

        List<DescCAB> fsnBps = blueprint.getFsnCABs();
        List<DescCAB> prefBps = blueprint.getPrefCABs();
        List<DescCAB> descBps = blueprint.getDescCABs();
        List<RelCAB> relBps = blueprint.getRelCABs();
        List<MediaCAB> mediaBps = blueprint.getMediaCABs();

        if (blueprint.getConAttrAB() != null) {
            for (RefexCAB annot : blueprint.getConAttrAB().getAnnotationBlueprints()) {
                this.construct(annot);
            }
        }

        for (DescCAB fsnBp : fsnBps) {
            this.construct(fsnBp);
        }
        for (DescCAB prefBp : prefBps) {
            this.construct(prefBp);
        }
        for (DescCAB descBp : descBps) {
            if (fsnBps.contains(descBp) || prefBps.contains(descBp)) {
                continue;
            } else {
                this.construct(descBp);
            }
        }
        for (RelCAB relBp : relBps) {
            this.construct(relBp);
        }
        for (MediaCAB mediaBp : mediaBps) {
            this.construct(mediaBp);
        }
        return newC;
    }

    @Override
    public ConAttrChronicleBI construct(ConAttrAB blueprint) throws IOException, InvalidCAB, ContradictionException {
        ConceptAttributes cac = getConAttr(blueprint);
        for (ConAttrVersionBI cav : cac.getVersions(vc)) {
            for (int p : ec.getEditPaths()) {

                if (cac.revisions == null) {
                    cac.revisions =
                            new RevisionSet(cac.primordialSapNid);
                }
                ConceptAttributesRevision r = (ConceptAttributesRevision) cac.makeAnalog(blueprint.getStatusNid(),
                        ec.getAuthorNid(), p, Long.MAX_VALUE);
                cac.revisions.add(r);
            }
        }
        for (int p : ec.getEditPaths()) {
            for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
                construct(annotBp);
            }
        }

        return cac;
    }

    @Override
    public ConAttrChronicleBI constructIfNotCurrent(ConAttrAB blueprint) throws IOException, InvalidCAB {
        ConceptAttributes cac = getConAttr(blueprint);
        for (ConAttrVersionBI cav : cac.getVersions(vc)) {
            if (blueprint.validate(cav)) {
                return cac;
            }
            for (int p : ec.getEditPaths()) {

                if (cac.revisions == null) {
                    cac.revisions =
                            new RevisionSet(cac.primordialSapNid);
                }
                ConceptAttributesRevision r = (ConceptAttributesRevision) cac.makeAnalog(blueprint.getStatusNid(),
                        ec.getAuthorNid(), p, Long.MAX_VALUE);
                cac.revisions.add(r);

            }
        }

        return cac;
    }
}
