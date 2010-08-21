package org.ihtsdo.concept.component.relationship.group;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;

public class RelGroupChronicle implements RelGroupChronicleBI {
	
	
	private int nid;
	private UUID uuid;
	private int relGroup;
	private int conceptNid;

	private Collection<RelationshipChronicleBI> rels;
	
	

	public RelGroupChronicle(Concept c, int relGroup, Collection<RelationshipChronicleBI> rels) throws IOException {
		super();
		this.relGroup = relGroup;
		try {
			uuid = Type5UuidFactory.get(Type5UuidFactory.REL_GROUP_NAMESPACE, c.getPrimUuid().toString() + relGroup);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
		nid = Bdb.uuidToNid(uuid);
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
	public List<UUID> getUUIDs() throws IOException {
		return Arrays.asList(new UUID[] { uuid });
	}

	public int getRelGroup() {
		return relGroup;
	}


}
