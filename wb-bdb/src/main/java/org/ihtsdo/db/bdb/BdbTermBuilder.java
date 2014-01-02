package org.ihtsdo.db.bdb;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.Terms;
import org.dwfa.vodb.types.Path;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
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
import org.ihtsdo.db.change.ChangeNotifier;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;

import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.MediaCAB;
import org.ihtsdo.tk.api.blueprint.PathCB;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
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
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

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
        if(Ts.get().getConcept(annot.getRefexNid()).isAnnotationStyleRefex() 
                && annot.getReferencedComponentNid() != Integer.MAX_VALUE){
            ComponentChronicleBI<?> component = Ts.get().getComponent(annot.getReferencedComponentNid());
            if(component == null){
                System.out.println("### DIAGNOSTIC: component was null. Nid: "  + annot.getReferencedComponentNid());
                System.out.println("For annotation: " + annot.toString());
                System.out.println("For blueprint: " + blueprint.toString());
                throw new InvalidCAB("Referenced component is null. Please see log for diagnostic details and create a tracker.");
            }
            component.addAnnotation(annot);
            if(annot.getReferencedComponentNid() ==
                    Ts.get().getConceptNidForNid(annot.getReferencedComponentNid())){
                ConceptChronicleBI concept = Ts.get().getConcept(annot.getReferencedComponentNid());
                concept.addAnnotation(annot);
            }
        }
        return annot;
    }

    public ConceptAttributes getConAttr(ConceptAttributeAB blueprint) throws IOException, InvalidCAB {
        ConceptAttributes cac = (ConceptAttributes) Ts.get().getConcept(blueprint.getComponentUuid()).getConceptAttributes();
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
                    Long.MAX_VALUE,
                    ec.getAuthorNid(),
                    ec.getModuleNid(),
                    pathNid);
            try {
                blueprint.setPropertiesExceptSap(refexRevision);
            } catch (PropertyVetoException ex) {
                throw new InvalidCAB("Refex: " + member
                        + "\n\nRefexAmendmentSpec: " + blueprint, ex);
            }
        }
        ChangeNotifier.touchRefexRC(member.referencedComponentNid);
        for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
            construct(annotBp);
        }
        return member;
    }

    private RefsetMember<?, ?> getRefex(RefexCAB blueprint)
            throws InvalidCAB, IOException {
        if (Ts.get().hasUuid(blueprint.getMemberUUID())) {
            ComponentChronicleBI<?> component =
                    Ts.get().getComponent(blueprint.getMemberUUID());
            if (component == null) {
                return null;
            }
            if (blueprint.getMemberType()
                    == TK_REFEX_TYPE.classToType(component.getClass())) {
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
            if (refex.getStampNid() == -1) {
                RefexChronicleBI<?> member = reCreateRefex(refex, blueprint);
                ChangeNotifier.touchRefexRC(member.getReferencedComponentNid());
                return member;

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
                RefexChronicleBI<?> member =  updateRefex(refex, blueprint);
                ChangeNotifier.touchRefexRC(member.getReferencedComponentNid());
                return member;
            }
        }

        RefexChronicleBI<?> member =  createRefex(blueprint);
        ChangeNotifier.touchRefexRC(member.getReferencedComponentNid());
        return member;
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
        RefexChronicleBI<?> member = RefsetMemberFactory.create(blueprint, ec);
        ChangeNotifier.touchRefexRC(member.getReferencedComponentNid());
        return member;
    }

    private RelationshipChronicleBI getRel(RelationshipCAB blueprint)
            throws InvalidCAB, IOException {
        if (Ts.get().hasUuid(blueprint.getComponentUuid())) {
            ComponentChronicleBI<?> component =
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
    public RelationshipChronicleBI construct(RelationshipCAB blueprint) throws IOException, InvalidCAB, ContradictionException {
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
                r.setTargetNid(blueprint.getTargetNid());
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
                            Bdb.getSapDb().getSapNid(blueprint.getStatusNid(), Long.MAX_VALUE,
                            ec.getAuthorNid(), ec.getModuleNid(), p);
                } else {
                    if (r.revisions == null) {
                        r.revisions = new RevisionSet(r.primordialSapNid);
                    }
                    r.revisions.add((RelationshipRevision) r.makeAnalog(blueprint.getStatusNid(),
                            Long.MAX_VALUE,
                            ec.getAuthorNid(),
                            ec.getModuleNid(),
                            p));
                }
            }
            c.getSourceRels().add(r);
            for (int p : ec.getEditPaths()) {
                for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
                    construct(annotBp);
                }
            }
            if (blueprint.hasAdditionalIds()) {
                handleAdditionalIdentifiers(r, blueprint.getIdMap());
            }
            return r;
        } else {
            Relationship r = (Relationship) relc;
            for (int p : ec.getEditPaths()) {
                RelationshipRevision rv = r.makeAnalog(blueprint.getStatusNid(),
                        Long.MAX_VALUE,
                        ec.getAuthorNid(),
                        ec.getModuleNid(),
                        p);
                if (r.getTargetNid() != blueprint.getTargetNid()) {
                    throw new InvalidCAB(
                            "r.getDestinationNid() != spec.getDestNid(): "
                            + r.getTargetNid() + " : " + blueprint.getTargetNid());
                }
                rv.setTypeNid(blueprint.getTypeNid());
                rv.setRefinabilityNid(blueprint.getRefinabilityNid());
                rv.setCharacteristicNid(blueprint.getCharacteristicNid());
            }
            for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
                construct(annotBp);
            }
            if (blueprint.hasAdditionalIds()) {
                handleAdditionalIdentifiers(r, blueprint.getIdMap());
            }
        }
        return relc;
    }

    @Override
    public RelationshipChronicleBI constructIfNotCurrent(RelationshipCAB blueprint) throws IOException, InvalidCAB, ContradictionException {
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

    private DescriptionChronicleBI getDesc(DescriptionCAB blueprint)
            throws InvalidCAB, IOException {
        if (Ts.get().hasUuid(blueprint.getComponentUuid())) {
            ComponentChronicleBI<?> component =
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
    public DescriptionChronicleBI constructIfNotCurrent(DescriptionCAB blueprint) throws IOException,
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
    public DescriptionChronicleBI construct(DescriptionCAB blueprint) throws IOException, InvalidCAB, ContradictionException {
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
                            Bdb.getSapDb().getSapNid(blueprint.getStatusNid(), Long.MAX_VALUE, ec.getAuthorNid(),
                            ec.getModuleNid(), p);
                } else {
                    if (d.revisions == null) {
                        d.revisions = new RevisionSet(d.primordialSapNid);
                    }
                    d.revisions.add((DescriptionRevision) d.makeAnalog(blueprint.getStatusNid(),
                            Long.MAX_VALUE,
                            ec.getAuthorNid(),
                            ec.getModuleNid(),
                            p));
                }
            }
            c.getDescs().add(d);
            for (int p : ec.getEditPaths()) {
                for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
                    construct(annotBp);
                }
            }
            if (blueprint.hasAdditionalIds()) {
                handleAdditionalIdentifiers(d, blueprint.getIdMap());
            }
            return d;
        } else {
            Description d = (Description) desc;
            for (int p : ec.getEditPaths()) {
                DescriptionRevision dr = d.makeAnalog(blueprint.getStatusNid(),
                        Long.MAX_VALUE,
                        ec.getAuthorNid(),
                        ec.getModuleNid(),
                        p);
                dr.setTypeNid(blueprint.getTypeNid());
                dr.setText(blueprint.getText());
                dr.setLang(blueprint.getLang());
                dr.setInitialCaseSignificant(blueprint.isInitialCaseSignificant());
                for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
                    construct(annotBp);
                }
            }
            if (blueprint.hasAdditionalIds()) {
                handleAdditionalIdentifiers(d, blueprint.getIdMap());
            }
        }
        
        return desc;
    }

    private MediaChronicleBI getMedia(MediaCAB blueprint)
            throws InvalidCAB, IOException {
        if (Ts.get().hasUuid(blueprint.getComponentUuid())) {
            ComponentChronicleBI<?> component =
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
                            Bdb.getSapDb().getSapNid(blueprint.getStatusNid(), Long.MAX_VALUE, ec.getAuthorNid(),
                            ec.getModuleNid(), p);
                } else {
                    if (img.revisions == null) {
                        img.revisions = new RevisionSet(img.primordialSapNid);
                    }
                    img.revisions.add((ImageRevision) img.makeAnalog(blueprint.getStatusNid(),
                            Long.MAX_VALUE,
                            ec.getAuthorNid(),
                            ec.getModuleNid(),
                            p));
                }
            }
            c.getMedia().add(img);
            for (int p : ec.getEditPaths()) {
                for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
                    construct(annotBp);
                }
            }
            if (blueprint.hasAdditionalIds()) {
                handleAdditionalIdentifiers(img, blueprint.getIdMap());
            }
            return img;
        } else {
            Image img = (Image) imgC;
            for (int p : ec.getEditPaths()) {
                ImageRevision imgR = img.makeAnalog(blueprint.getStatusNid(),
                        Long.MAX_VALUE,
                        ec.getAuthorNid(),
                        ec.getModuleNid(),
                        p);
                imgR.setTypeNid(blueprint.getTypeNid());
                imgR.setTextDescription(blueprint.getTextDescription());
                for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
                    construct(annotBp);
                }
            }
            if (blueprint.hasAdditionalIds()) {
                handleAdditionalIdentifiers(img, blueprint.getIdMap());
            }
        }

        return imgC;
    }

    private ConceptChronicleBI getConcept(ConceptCB blueprint)
            throws InvalidCAB, IOException {
        if (Ts.get().hasUuid(blueprint.getComponentUuid())) {
            ComponentChronicleBI<?> component =
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
            ConceptAttributeChronicleBI conceptAttributes = concept.getConceptAttributes();
            if (conceptAttributes == null || concept.isCanceled() 
                    || concept.getPrimUuid().toString().length() == 0
                    || concept.getConceptAttributes().getVersions().isEmpty()) {
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
            a = newC.getConAttrs();
            for (int pathNid : ec.getEditPaths()) {
                a.resetUncommitted(blueprint.getStatusNid(), ec.getAuthorNid(), ec.getModuleNid(), pathNid);
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
                        Bdb.getSapDb().getSapNid(blueprint.getStatusNid(), Long.MAX_VALUE, ec.getAuthorNid(),
                            ec.getModuleNid(), p);
            } else {
                if (a.revisions == null) {
                    a.revisions = new RevisionSet(a.primordialSapNid);
                }
                a.revisions.add((ConceptAttributesRevision) a.makeAnalog(blueprint.getStatusNid(),
                        Long.MAX_VALUE,
                        ec.getAuthorNid(),
                        ec.getModuleNid(),
                        p));
            }
        }
        if(blueprint.hasAdditionalIds()){
            handleAdditionalIdentifiers(a, blueprint.getIdMap());
        }
        List<DescriptionCAB> fsnBps = blueprint.getFullySpecifiedNameCABs();
        List<DescriptionCAB> prefBps = blueprint.getPreferredNameCABs();
        List<DescriptionCAB> descBps = blueprint.getDescriptionCABs();
        List<RelationshipCAB> relBps = blueprint.getRelationshipCABs();
        List<MediaCAB> mediaBps = blueprint.getMediaCABs();

        if (blueprint.getConceptAttributeAB() != null) {
            for (RefexCAB annot : blueprint.getConceptAttributeAB().getAnnotationBlueprints()) {
                this.construct(annot);
            }
        }

        for (DescriptionCAB fsnBp : fsnBps) {
            this.constructIfNotCurrent(fsnBp);
        }
        for (DescriptionCAB prefBp : prefBps) {
            this.constructIfNotCurrent(prefBp);
        }
        for (DescriptionCAB descBp : descBps) {
            if (fsnBps.contains(descBp) || prefBps.contains(descBp)) {
                continue;
            } else {
                this.constructIfNotCurrent(descBp);
            }
        }
        for (RelationshipCAB relBp : relBps) {
            this.constructIfNotCurrent(relBp);
        }
        for (MediaCAB mediaBp : mediaBps) {
            this.constructIfNotCurrent(mediaBp);
        }
        return newC;
    }

    @Override
    public ConceptAttributeChronicleBI construct(ConceptAttributeAB blueprint) throws IOException, InvalidCAB, ContradictionException {
        ConceptAttributes cac = getConAttr(blueprint);
        for (ConceptAttributeVersionBI cav : cac.getVersions(vc)) {
            for (int p : ec.getEditPaths()) {

                if (cac.revisions == null) {
                    cac.revisions =
                            new RevisionSet(cac.primordialSapNid);
                }
                ConceptAttributesRevision r = (ConceptAttributesRevision) cac.makeAnalog(blueprint.getStatusNid(),
                        Long.MAX_VALUE,
                        ec.getAuthorNid(),
                        ec.getModuleNid(),
                        p);
                r.setDefined(blueprint.defined);
                cac.revisions.add(r);
            }
        }
        for (int p : ec.getEditPaths()) {
            for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
                construct(annotBp);
            }
        }
        if (blueprint.hasAdditionalIds()) {
                handleAdditionalIdentifiers(cac, blueprint.getIdMap());
            }
        return cac;
    }

    @Override
    public ConceptAttributeChronicleBI constructIfNotCurrent(ConceptAttributeAB blueprint) throws IOException, InvalidCAB {
        ConceptAttributes cac = getConAttr(blueprint);
        for (ConceptAttributeVersionBI cav : cac.getVersions(vc)) {
            if (blueprint.validate(cav)) {
                return cac;
            }
            for (int p : ec.getEditPaths()) {

                if (cac.revisions == null) {
                    cac.revisions =
                            new RevisionSet(cac.primordialSapNid);
                }
                ConceptAttributesRevision r = (ConceptAttributesRevision) cac.makeAnalog(blueprint.getStatusNid(),
                        Long.MAX_VALUE,
                        ec.getAuthorNid(),
                        ec.getModuleNid(),
                        p);
                cac.revisions.add(r);

            }
        }

        return cac;
    }
    
    @Override
    public EditCoordinate getEditCoordinate() {
        return ec;
    }
    
    private void handleAdditionalIdentifiers(ConceptComponent component, HashMap<Object, Integer> idMap) throws IOException {
        for (int p : ec.getEditPaths()) {
            for (Object id : idMap.keySet()) {
                if(Long.class.isAssignableFrom(id.getClass())){
                    component.addLongId((long)id,
                            idMap.get(id),
                            SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                            ec,
                            Long.MAX_VALUE);
                } else if(String.class.isAssignableFrom(id.getClass())){
                    component.addStringId((String) id,
                            idMap.get(id),
                            SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                            Long.MAX_VALUE,
                            ec.getAuthorNid(),
                            ec.getModuleNid(),
                            p);
                } else if(UUID.class.isAssignableFrom(id.getClass())){
                    component.addUuidId((UUID) id,
                            idMap.get(id),
                            SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                            Long.MAX_VALUE,
                            ec.getAuthorNid(),
                            ec.getModuleNid(),
                            p);
                }
            }
            
            
        }
    }
    
    @Override
    /**
     * Constructs a path and commits the path concept and refset member additions.
     * 
     */
    public PathBI construct(PathCB blueprint) throws IOException, InvalidCAB, ContradictionException{
        ConceptChronicleBI pathConcept = construct(blueprint.getPathBp());
        Ts.get().addUncommitted(pathConcept);
        Ts.get().commit(pathConcept);
        
        RefexChronicleBI<?> pathRefex = construct(blueprint.getPathRefsetBp());
        ConceptChronicleBI pathRefexConcept = Ts.get().getConcept(pathRefex.getConceptNid());
        if(pathRefexConcept.isAnnotationStyleRefex()){
            Ts.get().addUncommitted(Ts.get().getConcept(pathRefex.getConceptNid()));
            Ts.get().commit(Ts.get().getConcept(pathRefex.getConceptNid()));
        }else{
            Ts.get().addUncommitted(pathRefexConcept);
            Ts.get().commit(pathRefexConcept);
        }
        
        //adds path as orgin of another path
        if (blueprint.getPathAsOriginBp() != null) {
            RefexChronicleBI<?> pathOriginRefexOther = construct(blueprint.getPathAsOriginBp());
            ConceptChronicleBI pathOriginRefexConcept = Ts.get().getConcept(pathOriginRefexOther.getRefexNid());
            if (pathOriginRefexConcept.isAnnotationStyleRefex()) {
                Ts.get().addUncommitted(Ts.get().getConcept(pathOriginRefexOther.getConceptNid()));
                Ts.get().commit(Ts.get().getConcept(pathOriginRefexOther.getConceptNid()));
            }
        }
        
        RefexChronicleBI<?> pathOriginRefex = construct(blueprint.getPathOriginRefsetBp());
        ConceptChronicleBI pathOriginRefexConcept = Ts.get().getConcept(pathOriginRefex.getRefexNid());
        if (pathOriginRefexConcept.isAnnotationStyleRefex()) {
            Ts.get().addUncommitted(Ts.get().getConcept(pathOriginRefex.getConceptNid()));
            Ts.get().commit(Ts.get().getConcept(pathOriginRefex.getConceptNid()));
        } else {
            Ts.get().addUncommitted(pathOriginRefexConcept);
            Ts.get().commit(pathOriginRefexConcept);
        }

        Collection<ConceptChronicleBI> originConcepts = blueprint.getOrigins();
        Collection<PositionBI> origins = new HashSet<PositionBI>();
        for(ConceptChronicleBI origin : originConcepts){
            PathBI originPath = Ts.get().getPath(origin.getConceptNid());
            PositionBI orginPosition = Ts.get().newPosition(originPath, Long.MAX_VALUE);
            origins.add(orginPosition);
        }
        
         ArrayList<PositionBI> originList = new ArrayList<PositionBI>();

        if (origins != null) {
            if (origins.size() > 1) {

                // find any duplicates
                HashMap<Integer, PositionBI> originMap = new HashMap<Integer, PositionBI>();

                for (PositionBI p : origins) {
                    if (originMap.containsKey(p.getPath().getConceptNid())) {
                        PositionBI first = originMap.get(p.getPath().getConceptNid());

                        if (first.getTime() < p.getTime()) {
                            originMap.put(p.getPath().getConceptNid(), p);
                        }
                    } else {
                        originMap.put(p.getPath().getConceptNid(), p);
                    }
                }

                origins = originMap.values();
            }

            originList.addAll(origins);
        }

        Path newPath = new Path(pathConcept.getConceptNid(), originList);
        return newPath;
    }
}
