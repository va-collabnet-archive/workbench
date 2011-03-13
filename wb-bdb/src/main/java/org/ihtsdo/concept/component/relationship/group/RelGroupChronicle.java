package org.ihtsdo.concept.component.relationship.group;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;

public class RelGroupChronicle implements RelGroupChronicleBI {

    private int nid;
    private UUID uuid;
    private int relGroup;
    private int conceptNid;
    private Collection<RelationshipChronicleBI> rels;

    public RelGroupChronicle(Concept c, int relGroup, Collection<RelationshipChronicleBI> rels) throws IOException {
        super();
        this.relGroup = relGroup;
        this.conceptNid = c.getNid();
        try {
            uuid = Type5UuidFactory.get(Type5UuidFactory.REL_GROUP_NAMESPACE, c.getPrimUuid().toString() + relGroup);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        nid = Bdb.uuidToNid(uuid);
        Bdb.getNidCNidMap().setCNidForNid(conceptNid, nid);
        this.rels = rels;
    }

    @Override
    public Collection<? extends RelationshipChronicleBI> getRels() {
        return rels;
    }

    @Override
    public int getConceptNid() {
        return conceptNid;
    }

    @Override
    public int getNid() {
        return nid;
    }

    @Override
    public List<UUID> getUUIDs() {
        return Arrays.asList(new UUID[]{uuid});
    }

    @Override
    public int getRelGroup() {
        return relGroup;
    }

    @Override
    public RelGroupVersionBI getVersion(ViewCoordinate c)
            throws ContraditionException {
        return new RelGroupVersion(this, c);
    }

    @Override
    public Collection<? extends RelGroupVersionBI> getVersions(ViewCoordinate c) {
        return Arrays.asList(new RelGroupVersionBI[]{new RelGroupVersion(this, c)});
    }

    @Override
    public Collection<? extends RelGroupVersionBI> getVersions() {
        return Arrays.asList(new RelGroupVersionBI[]{new RelGroupVersion(this, null)});
    }

    @Override
    public UUID getPrimUuid() {
        return uuid;
    }

    @Override
    public String toUserString() {
        StringBuilder buff = new StringBuilder();
        buff.append("Group: ");
        for (RelationshipChronicleBI rc : rels) {
            buff.append(rc.toUserString());
            buff.append(";");
        }
        return buff.toString();
    }

    @Override
    public boolean isUncommitted() {
        return false;
    }

    @Override
    public boolean addAnnotation(RefexChronicleBI<?> annotation) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getAnnotations() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexes()
            throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(
            ViewCoordinate xyz) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getInactiveRefexes(
            ViewCoordinate xyz) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentAnnotations(
            ViewCoordinate xyz) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Set<Integer> getAllSapNids() throws IOException {
        HashSet<Integer> sapNids = new HashSet<Integer>();
        for (RelationshipChronicleBI r : rels) {
            sapNids.addAll(r.getAllSapNids());
        }
        return sapNids;
    }

    @Override
    public Set<PositionBI> getPositions() throws IOException {
        Set<PositionBI> positions = new HashSet<PositionBI>();
        for (RelationshipChronicleBI rc : rels) {
            positions.addAll(rc.getPositions());
        }
        return positions;
    }
}
