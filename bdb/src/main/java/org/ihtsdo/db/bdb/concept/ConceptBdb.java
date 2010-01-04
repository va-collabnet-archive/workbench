package org.ihtsdo.db.bdb.concept;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.DatabaseEntry;


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

	public void writeConcept(Concept concept) {
		ConceptBinder binder = ConceptBinder.getBinder();
		DatabaseEntry key = new DatabaseEntry();
		IntegerBinding.intToEntry(concept.getNid(), key);
		DatabaseEntry value = new DatabaseEntry();
		binder.objectToEntry(concept, value);
		readWrite.put(null, key, value);
	}
}
