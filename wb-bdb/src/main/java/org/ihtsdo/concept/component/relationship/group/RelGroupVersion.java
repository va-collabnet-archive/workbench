package org.ihtsdo.concept.component.relationship.group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;

public class RelGroupVersion
		implements RelGroupVersionBI{

	private RelGroupChronicleBI rg;
	
	public UUID getPrimUuid() {
		return rg.getPrimUuid();
	}

	private Coordinate coordinate;

	public RelGroupVersion(RelGroupChronicleBI rg, Coordinate coordinate) {
		assert rg != null;
		assert coordinate != null;
		this.rg = rg;
		this.coordinate = coordinate;
	}

	@Override
	public Collection<? extends RelationshipVersionBI> getRels() throws ContraditionException {
		ArrayList<RelationshipVersionBI> results = new ArrayList<RelationshipVersionBI>();
		for (RelationshipChronicleBI relc: rg.getRels()) {
			if (coordinate != null) {
				RelationshipVersionBI rv = relc.getVersion(coordinate);
				if (rv != null) {
					if (rv.getGroup() == rg.getRelGroup() && 
							coordinate.getAllowedStatusNids().contains(rv.getStatusNid())) {
						results.add(rv);
					}
				}
			} else {
				for (RelationshipVersionBI rv: relc.getVersions()) {
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
	public RelGroupVersionBI getVersion(Coordinate c)
			throws ContraditionException {
		return rg.getVersion(c);
	}

	@Override
	public Collection<? extends RelGroupVersionBI> getVersions(Coordinate c) {
		return Arrays.asList(new RelGroupVersionBI[] { new RelGroupVersion(this, c) });
	}

	@Override
	public List<RelGroupVersion> getVersions() {
		return Arrays.asList(new RelGroupVersion[] { new RelGroupVersion(this, null) });
	}

	@Override
	public int getAuthorNid() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getPathNid() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getStatusNid() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toUserString() {
	    StringBuffer buff = new StringBuffer();
    	buff.append("Group: ");
	    try {
			for (RelationshipVersionBI rel: getRels()) {
				buff.append(rel.toUserString());
				buff.append("; ");
			}
		} catch (ContraditionException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return buff.toString();
	}

	@Override
	public boolean isUncommitted() {
		return false;
	}

}
