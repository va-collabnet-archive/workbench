package org.ihtsdo.concept.component.relationship.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;

public class RelGroupVersion
		implements RelGroupVersionBI{

	RelGroupChronicleBI rg;
	private Coordinate coordinate;

	public RelGroupVersion(RelGroupChronicleBI rg, Coordinate coordinate) throws IOException {
		this.rg = rg;
		this.coordinate = coordinate;
	}

	@Override
	public Collection<? extends RelationshipVersionBI> getRels() throws ContraditionException {
		ArrayList<RelationshipVersionBI> results = new ArrayList<RelationshipVersionBI>();
		for (RelationshipChronicleBI rc: rg.getRels()) {
			RelationshipVersionBI rv = rc.getVersion(coordinate);
			if (rv.getGroup() == rg.getRelGroup() && 
					coordinate.getAllowedStatusNids().contains(rv.getStatusNid())) {
				results.add(rv);
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
	public List<UUID> getUUIDs() throws IOException {
		return rg.getUUIDs();
	}

	@Override
	public int getRelGroup() {
		return rg.getRelGroup();
	}

}
