package org.ihtsdo.concept.component.relationship.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChroncileBI;

import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;

public class RelGroupVersion
        implements RelGroupVersionBI {

    private RelGroupChronicleBI rg;

    @Override
    public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws Exception {
        return rg.makeAdjudicationAnalogs(ec, vc);
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexes()
            throws IOException {
        return rg.getRefexes();
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid) throws IOException {
        return rg.getRefexes(refsetNid);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz)
            throws IOException {
        return rg.getCurrentRefexes(xyz);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz, int refsetNid)
            throws IOException {
        return rg.getCurrentRefexes(xyz, refsetNid);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getInactiveRefexes(
            ViewCoordinate xyz) throws IOException {
        return rg.getInactiveRefexes(xyz);
    }

    @Override
    public boolean addAnnotation(RefexChronicleBI<?> annotation)
            throws IOException {
        return rg.addAnnotation(annotation);
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getAnnotations()
            throws IOException {
        return rg.getAnnotations();
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentAnnotations(
            ViewCoordinate xyz) throws IOException {
        return rg.getCurrentAnnotations(xyz);
    }
    private int authorNid;
    private int statusNid;
    private int pathNid;
    private long time = Long.MIN_VALUE;

    @Override
    public UUID getPrimUuid() {
        return rg.getPrimUuid();
    }
    private ViewCoordinate coordinate;

    public RelGroupVersion(RelGroupChronicleBI rg, ViewCoordinate coordinate) {
        assert rg != null;
        assert coordinate != null;
        this.rg = rg;
        this.coordinate = new ViewCoordinate(coordinate);
        setupLatest();
    }

    @Override
    public Collection<? extends RelationshipChronicleBI> getRels() {
        return rg.getRels();
    }

    @Override
    public Collection<? extends RelationshipVersionBI> getCurrentRels() throws ContraditionException {
        ArrayList<RelationshipVersionBI> results = new ArrayList<RelationshipVersionBI>();
        for (RelationshipChronicleBI relc : rg.getRels()) {
            if (coordinate != null) {
                RelationshipVersionBI rv = relc.getVersion(coordinate);
                if (rv != null) {
                    if (rv.getGroup() == rg.getRelGroup()
                            && coordinate.getAllowedStatusNids().contains(rv.getStatusNid())) {
                        results.add(rv);
                    }
                }
            } else {
                for (RelationshipVersionBI rv : relc.getVersions()) {
                    if (rv.getGroup() == rg.getRelGroup()) {
                        results.add(rv);
                    }
                }
            }
        }
        return results;
    }

    @Override
    public boolean isBaselineGeneration() {
        for (RelationshipChronicleBI rc: getRels()) {
            for (RelationshipVersionBI rv: rc.getVersions()) {
                if (!rv.isBaselineGeneration()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    

    @Override
    public Collection<? extends RelationshipVersionBI> getAllRels() throws ContraditionException {
        ArrayList<RelationshipVersionBI> results = new ArrayList<RelationshipVersionBI>();
        for (RelationshipChronicleBI relc : rg.getRels()) {
            if (coordinate != null) {
                RelationshipVersionBI rv = relc.getVersion(coordinate.getVcWithAllStatusValues());
                if (rv != null) {
                    if (rv.getGroup() == rg.getRelGroup()) {
                        results.add(rv);
                    }
                }
            } else {
                for (RelationshipVersionBI rv : relc.getVersions()) {
                    if (rv.getGroup() == rg.getRelGroup()) {
                        results.add(rv);
                    }
                }
            }
        }
        return results;
    }

    @Override
    public int getConceptNid() {
        return rg.getConceptNid();
    }

    @Override
    public int getNid() {
        return rg.getNid();
    }

    @Override
    public List<UUID> getUUIDs() {
        return rg.getUUIDs();
    }

    @Override
    public int getRelGroup() {
        return rg.getRelGroup();
    }

    @Override
    public RelGroupVersionBI getVersion(ViewCoordinate c)
            throws ContraditionException {
        return rg.getVersion(c);
    }

    @Override
    public Collection<? extends RelGroupVersionBI> getVersions(ViewCoordinate c) {
        return Arrays.asList(new RelGroupVersionBI[]{new RelGroupVersion(this, c)});
    }

    @Override
    public List<RelGroupVersion> getVersions() {
        return Arrays.asList(new RelGroupVersion[]{new RelGroupVersion(this, null)});
    }

    private void setupLatest() {
        time = Long.MIN_VALUE;
        for (RelationshipChronicleBI rel : rg.getRels()) {
            for (RelationshipVersionBI relV : rel.getVersions(coordinate)) {
                if (relV.getTime() > time) {
                    time = relV.getTime();
                    authorNid = relV.getAuthorNid();
                    pathNid = relV.getPathNid();
                    statusNid = relV.getStatusNid();
                }
            }
        }
    }

    @Override
    public int getAuthorNid() {
        return authorNid;
    }

    @Override
    public int getPathNid() {
        return pathNid;
    }

    @Override
    public int getStatusNid() {
        return statusNid;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public String toUserString() {
        StringBuilder buff = new StringBuilder();
        buff.append("Group: ");
        for (RelationshipChronicleBI rel : getRels()) {
            buff.append(rel.toUserString());
            buff.append("; ");
        }
        return buff.toString();
    }
    
        @Override
    public String toUserString(TerminologySnapshotDI snapshot)
            throws IOException, ContraditionException {
        return toUserString();
    }


    @Override
    public boolean isUncommitted() {
        return false;
    }

    @Override
    public int getSapNid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Integer> getAllSapNids() throws IOException {
        return rg.getAllSapNids();
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
    public ComponentChroncileBI getChronicle() {
        return rg;
    }

    @Override
    public RelGroupVersionBI getPrimordialVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isActive(NidSetBI allowedStatusNids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean sapIsInRange(int min, int max) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
