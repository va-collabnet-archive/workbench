package org.ihtsdo.concept.component.relationship.group;

//~--- non-JDK imports --------------------------------------------------------
import org.dwfa.vodb.types.Position;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RelGroupVersion implements RelationshipGroupVersionBI {

    private long time = Long.MIN_VALUE;
    private int authorNid;
    private ViewCoordinate coordinate;
    private int pathNid;
    private RelationshipGroupChronicleBI rg;
    private int statusNid;
    private int moduleNid;

    //~--- constructors --------------------------------------------------------
    public RelGroupVersion(RelationshipGroupChronicleBI rg, ViewCoordinate coordinate) {
        assert rg != null;
        assert coordinate != null;
        this.rg = rg;
        this.coordinate = new ViewCoordinate(coordinate);
        setupLatest();
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException {
        return rg.addAnnotation(annotation);
    }

    @Override
    public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws Exception {
        return rg.makeAdjudicationAnalogs(ec, vc);
    }

    @Override
    public boolean stampIsInRange(int min, int max) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void setupLatest() {
        time = Long.MIN_VALUE;

        for (RelationshipVersionBI relV : getRelationshipsActiveAllVersions()) {
            if (relV.getTime() > time) {
                time = relV.getTime();
                authorNid = relV.getAuthorNid();
                pathNid = relV.getPathNid();
                statusNid = relV.getStatusNid();
                moduleNid = relV.getModuleNid();
            }
        }

        if (time == Long.MIN_VALUE) {
            for (RelationshipChronicleBI rel : getRelationships()) {
                for (RelationshipVersionBI relV : rel.getVersions(coordinate)) {
                    if (relV.getTime() > time) {
                        time = relV.getTime();
                        authorNid = relV.getAuthorNid();
                        pathNid = relV.getPathNid();
                        statusNid = relV.getStatusNid();
                        moduleNid = relV.getModuleNid();
                    }
                }
            }
        }
    }

    @Override
    public String toUserString() {
        StringBuilder buff = new StringBuilder();

        buff.append("Group: ");

        for (RelationshipChronicleBI rel : getRelationships()) {
            buff.append(rel.toUserString());
            buff.append("; ");
        }

        return buff.toString();
    }

    @Override
    public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException {
        return toUserString();
    }

    @Override
    public boolean versionsEqual(ViewCoordinate vc1, ViewCoordinate vc2, Boolean compareAuthoring) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public Collection<? extends IdBI> getAdditionalIds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends RelationshipVersionBI> getRelationshipsActiveAllVersions() {
        ArrayList<RelationshipVersionBI> results = new ArrayList<RelationshipVersionBI>();

        for (RelationshipChronicleBI relc : rg.getRelationships()) {
            if (coordinate != null) {
                for (RelationshipVersionBI rv : relc.getVersions(coordinate)) {
                    if ((rv.getGroup() == rg.getRelationshipGroupNumber())
                            && coordinate.getAllowedStatusNids().contains(rv.getStatusNid())) {
                        results.add(rv);
                    }
                }
            }
        }

        return results;
    }

    @Override
    public Collection<? extends IdBI> getAllIds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Integer> getAllNidsForVersion() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends RelationshipVersionBI> getRelationshipsAll() throws ContradictionException {
        ArrayList<RelationshipVersionBI> results = new ArrayList<RelationshipVersionBI>();

        for (RelationshipChronicleBI relc : rg.getRelationships()) {
            if (coordinate != null) {
                try {
                    RelationshipVersionBI rv = relc.getVersion(coordinate.getViewCoordinateWithAllStatusValues());
                    if (rv != null) {
                        if (rv.getGroup() == rg.getRelationshipGroupNumber()) {
                            results.add(rv);
                        }
                    }
                } catch (ContradictionException ex) {
                    for (RelationshipVersionBI rv : relc.getVersions(coordinate.getViewCoordinateWithAllStatusValues())) {
                        if (rv.getGroup() == rg.getRelationshipGroupNumber()) {
                            results.add(rv);
                        }
                    }
                }
            }
        }

        return results;
    }

    @Override
    public Set<Integer> getAllStampNids() throws IOException {
        return rg.getAllStampNids();
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getAnnotations() throws IOException {
        return rg.getAnnotations();
    }

    @Override
    public int getAuthorNid() {
        return authorNid;
    }

    @Override
    public ComponentChronicleBI getChronicle() {
        return rg;
    }

    @Override
    public int getConceptNid() {
        return rg.getConceptNid();
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz)
            throws IOException {
        return rg.getAnnotationsActive(xyz);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getAnnotationMembersActive(ViewCoordinate xyz, int refexNid)
            throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate xyz)
            throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate xyz, int refexNid)
            throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid)
            throws IOException {
        return rg.getRefexMembersActive(xyz, refsetNid);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexesActive(ViewCoordinate xyz) throws IOException {
        return rg.getRefexesActive(xyz);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getActiveRefexes(ViewCoordinate xyz, int refsetNid)
            throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends RelationshipVersionBI> getRelationshipsActive() throws ContradictionException {
        ArrayList<RelationshipVersionBI> results = new ArrayList<RelationshipVersionBI>();

        for (RelationshipChronicleBI relc : rg.getRelationships()) {
            if (coordinate != null) {
                RelationshipVersionBI rv = relc.getVersion(coordinate);

                if (rv != null) {
                    if ((rv.getGroup() == rg.getRelationshipGroupNumber())
                            && coordinate.getAllowedStatusNids().contains(rv.getStatusNid())) {
                        results.add(rv);
                    }
                }
            } 
        }

        return results;
    }

    @Override
    public ConceptChronicleBI getEnclosingConcept() {
        return rg.getEnclosingConcept();
    }

    @Override
    public CreateOrAmendBlueprint makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexesInactive(ViewCoordinate xyz) throws IOException {
        return rg.getRefexesInactive(xyz);
    }

    @Override
    public int getNid() {
        return rg.getNid();
    }

    @Override
    public int getPathNid() {
        return pathNid;
    }

    @Override
    public PositionBI getPosition() throws IOException {
        return new Position(getTime(), Ts.get().getPath(getPathNid()));
    }

    @Override
    public Set<PositionBI> getPositions() throws IOException {
        return rg.getPositions();
    }

    @Override
    public UUID getPrimUuid() {
        return rg.getPrimUuid();
    }

    @Override
    public RelationshipGroupVersionBI getPrimordialVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
        return rg.getRefexes();
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid) throws IOException {
        return rg.getRefexes(refsetNid);
    }

    @Override
    public int getRelationshipGroupNumber() {
        return rg.getRelationshipGroupNumber();
    }

    @Override
    public Collection<? extends RelationshipChronicleBI> getRelationships() {
        return rg.getRelationships();
    }

    @Override
    public int getStampNid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatusNid() {
        return statusNid;
    }
    
    @Override
    public int getModuleNid() {
        return moduleNid;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public List<UUID> getUUIDs() {
        return rg.getUUIDs();
    }

    @Override
    public RelationshipGroupVersionBI getVersion(ViewCoordinate c) throws ContradictionException {
        return rg.getVersion(c);
    }

    @Override
    public List<RelGroupVersion> getVersions() {
        return Arrays.asList(new RelGroupVersion[]{new RelGroupVersion(this, null)});
    }

    @Override
    public Collection<? extends RelationshipGroupVersionBI> getVersions(ViewCoordinate c) {
        return Arrays.asList(new RelationshipGroupVersionBI[]{new RelGroupVersion(this, c)});
    }

    @Override
    public boolean hasAnnotationMemberActive(ViewCoordinate xyz, int refsetNid) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasRefexMemberActive(ViewCoordinate xyz, int refsetNid) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isActive() throws IOException {
        return isActive(coordinate);
    }

    @Override
    public boolean isActive(NidSetBI allowedStatusNids) {
        return allowedStatusNids.contains(statusNid);
    }

    @Override
    public boolean isActive(ViewCoordinate vc) throws IOException {
        return vc.getAllowedStatusNids().contains(statusNid);
    }

    @Override
    public boolean isBaselineGeneration() {
        for (RelationshipChronicleBI rc : getRelationships()) {
            for (RelationshipVersionBI rv : rc.getVersions()) {
                if (!rv.isBaselineGeneration()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean isUncommitted() {
        return false;
    }

    @Override
    public boolean isCanceled() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
