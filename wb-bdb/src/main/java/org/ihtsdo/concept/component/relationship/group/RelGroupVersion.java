package org.ihtsdo.concept.component.relationship.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.tk.api.ContraditionException;
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

	public Collection<? extends RefexChronicleBI<?>> getRefexes()
			throws IOException {
		return rg.getRefexes();
	}

	public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(
			ViewCoordinate xyz) throws IOException {
		return rg.getCurrentRefexes(xyz);
	}

	public boolean addAnnotation(RefexChronicleBI<?> annotation)
			throws IOException {
		return rg.addAnnotation(annotation);
	}

	public Collection<? extends RefexChronicleBI<?>> getAnnotations()
			throws IOException {
		return rg.getAnnotations();
	}

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
        this.coordinate = coordinate;
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

}

