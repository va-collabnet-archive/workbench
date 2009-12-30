package org.ihtsdo.db.bdb.concept;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;


public class ConceptBdb extends ComponentBdb {
		
	public ConceptBdb(Bdb readOnlyBdbEnv, Bdb readWriteBdbEnv)
			throws IOException {
		super(readOnlyBdbEnv, readWriteBdbEnv);
	}

	@Override
	protected String getDbName() {
		return "conceptDb";
	}

	@Override
	protected void init() throws IOException {
		// TODO Auto-generated method stub
	}
	
	public List<UUID> getUuidsForConcept(int nid) throws IOException {
		//TODO add some type of buffer here...
		return getConcept(nid).getUids();
	}
	
	public Concept getConcept(int nid) throws IOException {
		return Concept.get(nid, false);
	}

	public Concept getWritableConcept(int nid) throws IOException {
		return Concept.get(nid, true);
	}
}
