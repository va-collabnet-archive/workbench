package org.ihtsdo.db.bdb;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refset.RefsetMemberFactory;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.concept.component.relationship.RelationshipRevision;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.blueprint.ConceptCAB;
import org.ihtsdo.tk.api.blueprint.DescCAB;

import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.MediaCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.blueprint.RelCAB;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetType;
import org.ihtsdo.tk.example.binding.TermAux;

public class BdbTerminologyConstructor implements TerminologyConstructorBI {

    EditCoordinate ec;
    ViewCoordinate vc;

    public BdbTerminologyConstructor(EditCoordinate ec, ViewCoordinate vc) {
        this.ec = ec;
        this.vc = vc;
    }

    @Override
    public RefexChronicleBI<?> construct(RefexCAB res)
            throws IOException, InvalidCAB {
        RefsetMember<?, ?> refex = getRefex(res);
        if (refex != null) {
            return updateRefex(refex, res);
        }
        return createRefex(res);
    }

    private RefexChronicleBI<?> updateRefex(RefsetMember<?, ?> refex,
            RefexCAB res) throws InvalidCAB {
        for (int pathNid : ec.getEditPaths()) {
            RefsetRevision refexRevision =
                    refex.makeAnalog(res.getInt(RefexProperty.STATUS_NID),
                    ec.getAuthorNid(), pathNid, Long.MAX_VALUE);
            try {
                res.setPropertiesExceptSap(refexRevision);
            } catch (PropertyVetoException ex) {
                throw new InvalidCAB("Refex: " + refex
                        + "\n\nRefexAmendmentSpec: " + res, ex);
            }
        }
        return refex;
    }

    private RefsetMember<?, ?> getRefex(RefexCAB res)
            throws InvalidCAB, IOException {
        if (Ts.get().hasUuid(res.getMemberUUID())) {
            ComponentChroncileBI<?> component =
                    Ts.get().getComponent(res.getMemberUUID());
            if (component == null) {
                return null;
            }
            if (res.getMemberType()
                    == TkRefsetType.classToType(component.getClass())) {
                return (RefsetMember<?, ?>) component;
            } else {
                throw new InvalidCAB(
                        "Component exists of different type: "
                        + component + "\n\nRefexAmendmentSpec: " + res);
            }
        }
        return null;
    }

    @Override
    public RefexChronicleBI<?> constructIfNotCurrent(RefexCAB res)
            throws IOException, InvalidCAB {
        RefsetMember<?, ?> refex = getRefex(res);
        if (refex != null) {
            boolean current = false;
            for (RefexVersionBI refexv : refex.getVersions(vc)) {
                if (res.validate(refexv)) {
                    current = true;
                    break;
                }
            }
            if (current) {
                return refex;
            }
            return updateRefex(refex, res);
        }

        return createRefex(res);
    }

    private RefexChronicleBI<?> createRefex(RefexCAB res)
            throws IOException, InvalidCAB {
        return RefsetMemberFactory.create(res, ec);
    }

    private RelationshipChronicleBI getRel(RelCAB res)
            throws InvalidCAB, IOException {
        if (Ts.get().hasUuid(res.getComponentUuid())) {
            ComponentChroncileBI<?> component =
                    Ts.get().getComponent(res.getComponentUuid());
            if (component == null) {
                return null;
            }
            if (component instanceof RelationshipChronicleBI) {
                return (RelationshipChronicleBI) component;
            } else {
                throw new InvalidCAB(
                        "Component exists of different type: "
                        + component + "\n\nRelAmendmentSpec: " + res);
            }
        }
        return null;
    }

    @Override
    public RelationshipChronicleBI construct(RelCAB spec) throws IOException, InvalidCAB {
        RelationshipChronicleBI relc = getRel(spec);
        int statusNid = TermAux.CURRENT.getStrict(Ts.get().getMetadataVC()).getNid();

        if (relc == null) {
            Concept c = (Concept) Ts.get().getConcept(spec.getSourceNid());
            Relationship r = new Relationship();
            relc = r;
            Bdb.gVersion.incrementAndGet();
            r.enclosingConceptNid = c.getNid();
            r.nid = Bdb.uuidToNid(spec.getComponentUuid());
            Bdb.getNidCNidMap().setCNidForNid(c.getNid(), r.nid);
            r.primordialUNid = Bdb.getUuidsToNidMap().getUNid(spec.getComponentUuid());
            r.setC2Id(spec.getDestNid());
            r.setTypeId(spec.getTypeNid());
            r.setRefinabilityId(spec.getRefinabilityNid());
            r.setCharacteristicId(spec.getCharacteristicNid());
            r.primordialSapNid = Integer.MIN_VALUE;
            r.setGroup(spec.getGroup());
            for (int p : ec.getEditPaths()) {
                if (r.primordialSapNid == Integer.MIN_VALUE) {
                    r.primordialSapNid =
                            Bdb.getSapDb().getSapNid(statusNid, ec.getAuthorNid(), p,
                            Long.MAX_VALUE);
                } else {
                    if (r.revisions == null) {
                        r.revisions = new CopyOnWriteArrayList<RelationshipRevision>();
                    }
                    r.revisions.add((RelationshipRevision) r.makeAnalog(statusNid,
                            ec.getAuthorNid(), p, Long.MAX_VALUE));
                }
            }
            c.getSourceRels().add(r);
            return r;
        } else {
            Relationship r = (Relationship) relc;
            for (int p : ec.getEditPaths()) {
                RelationshipRevision rv = r.makeAnalog(statusNid,
                        ec.getAuthorNid(),
                        p,
                        Long.MAX_VALUE);
                r.setC2Id(spec.getDestNid());
                r.setTypeId(spec.getTypeNid());
                r.setRefinabilityId(spec.getRefinabilityNid());
                r.setCharacteristicId(spec.getCharacteristicNid());
            }


        }
        return relc;
    }

    @Override
    public RelationshipChronicleBI constructIfNotCurrent(RelCAB spec) throws IOException, InvalidCAB {
        RelationshipChronicleBI relc = getRel(spec);

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DescriptionChronicleBI construct(DescCAB spec) throws IOException, InvalidCAB {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DescriptionChronicleBI constructIfNotCurrent(DescCAB spec) throws IOException, InvalidCAB {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MediaChronicleBI construct(MediaCAB spec) throws IOException, InvalidCAB {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MediaChronicleBI constructIfNotCurrent(MediaCAB spec) throws IOException, InvalidCAB {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ConceptChronicleBI construct(ConceptCAB spec) throws IOException, InvalidCAB {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ConceptChronicleBI constructIfNotCurrent(ConceptCAB spec) throws IOException, InvalidCAB {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
