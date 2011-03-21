package org.ihtsdo.db.bdb;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.description.DescriptionRevision;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refset.RefsetMemberFactory;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.concept.component.relationship.RelationshipRevision;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.blueprint.DescCAB;

import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.blueprint.RelCAB;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.blueprint.ConceptCAB;
import org.ihtsdo.tk.api.blueprint.MediaCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetType;

public class BdbTermConstructor implements TerminologyConstructorBI {

    EditCoordinate ec;
    ViewCoordinate vc;

    public BdbTermConstructor(EditCoordinate ec, ViewCoordinate vc) {
        this.ec = ec;
        this.vc = vc;
    }

    @Override
    public RefexChronicleBI<?> construct(RefexCAB blueprint)
            throws IOException, InvalidCAB {
        RefsetMember<?, ?> refex = getRefex(blueprint);
        if (refex != null) {
            return updateRefex(refex, blueprint);
        }
        return createRefex(blueprint);
    }

    private RefexChronicleBI<?> updateRefex(RefsetMember<?, ?> blueprint,
            RefexCAB res) throws InvalidCAB {
        for (int pathNid : ec.getEditPaths()) {
            RefsetRevision refexRevision =
                    blueprint.makeAnalog(res.getInt(RefexProperty.STATUS_NID),
                    ec.getAuthorNid(), pathNid, Long.MAX_VALUE);
            try {
                res.setPropertiesExceptSap(refexRevision);
            } catch (PropertyVetoException ex) {
                throw new InvalidCAB("Refex: " + blueprint
                        + "\n\nRefexAmendmentSpec: " + res, ex);
            }
        }
        return blueprint;
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
                    == TkRefsetType.classToType(component.getClass())) {
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
            throws IOException, InvalidCAB {
        RefsetMember<?, ?> refex = getRefex(blueprint);
        if (refex != null) {
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

        return createRefex(blueprint);
    }

    private RefexChronicleBI<?> createRefex(RefexCAB blueprint)
            throws IOException, InvalidCAB {
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
    public RelationshipChronicleBI construct(RelCAB blueprint) throws IOException, InvalidCAB {
        RelationshipChronicleBI relc = getRel(blueprint);

        if (relc == null) {
            Concept c = (Concept) Ts.get().getConcept(blueprint.getSourceNid());
            Relationship r = new Relationship();
            relc = r;
            Bdb.gVersion.incrementAndGet();
            r.enclosingConceptNid = c.getNid();
            r.nid = Bdb.uuidToNid(blueprint.getComponentUuid());
            Bdb.getNidCNidMap().setCNidForNid(c.getNid(), r.nid);
            r.primordialUNid = Bdb.getUuidsToNidMap().getUNid(blueprint.getComponentUuid());
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
                        r.revisions = new CopyOnWriteArrayList<RelationshipRevision>();
                    }
                    r.revisions.add((RelationshipRevision) r.makeAnalog(blueprint.getStatusNid(),
                            ec.getAuthorNid(), p, Long.MAX_VALUE));
                }
            }
            c.getSourceRels().add(r);
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
        }
        return relc;
    }

    @Override
    public RelationshipChronicleBI constructIfNotCurrent(RelCAB blueprint) throws IOException, InvalidCAB {
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
    public DescriptionChronicleBI constructIfNotCurrent(DescCAB blueprint) throws IOException, InvalidCAB {
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
    public DescriptionChronicleBI construct(DescCAB blueprint) throws IOException, InvalidCAB {
        DescriptionChronicleBI desc = getDesc(blueprint);

        if (desc == null) {
            Concept c = (Concept) Ts.get().getConcept(blueprint.getConceptNid());
            Description d = new Description();
            desc = d;
            Bdb.gVersion.incrementAndGet();
            d.enclosingConceptNid = c.getNid();
            d.nid = Bdb.uuidToNid(blueprint.getComponentUuid());
            Bdb.getNidCNidMap().setCNidForNid(c.getNid(), d.nid);
            d.primordialUNid = Bdb.getUuidsToNidMap().getUNid(blueprint.getComponentUuid());
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
                        d.revisions = new CopyOnWriteArrayList<DescriptionRevision>();
                    }
                    d.revisions.add((DescriptionRevision) d.makeAnalog(blueprint.getStatusNid(),
                            ec.getAuthorNid(), p, Long.MAX_VALUE));
                }
            }
            c.getDescriptions().add(d);
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
            }
        }
        return desc;
    }

    @Override
    public MediaChronicleBI constructIfNotCurrent(MediaCAB spec) throws IOException, InvalidCAB {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MediaChronicleBI construct(MediaCAB spec) throws IOException, InvalidCAB {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ConceptChronicleBI constructIfNotCurrent(ConceptCAB spec) throws IOException, InvalidCAB {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ConceptChronicleBI construct(ConceptCAB spec) throws IOException, InvalidCAB {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
