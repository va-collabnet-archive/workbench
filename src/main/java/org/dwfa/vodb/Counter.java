package org.dwfa.vodb;

import org.dwfa.vodb.types.I_ProcessConcepts;
import org.dwfa.vodb.types.I_ProcessDescriptions;
import org.dwfa.vodb.types.I_ProcessIds;
import org.dwfa.vodb.types.I_ProcessImages;
import org.dwfa.vodb.types.I_ProcessRelationships;

import com.sleepycat.je.DatabaseEntry;

public class Counter implements I_ProcessConcepts, I_ProcessRelationships,
		I_ProcessDescriptions, I_ProcessIds, I_ProcessImages {

	int concepts = 0;
	int descriptions = 0;
	int relationships = 0;
	int ids = 0;
	int images = 0;
	int total = 0;
	
	public Counter() {
		super();
	}

	public void processConcept(DatabaseEntry key, DatabaseEntry value) throws Exception {
		concepts++;
		total++;
	}

	public void processRel(DatabaseEntry key, DatabaseEntry value) throws Exception {
		relationships++;
		total++;
	}

	public void processDesc(DatabaseEntry key, DatabaseEntry value) throws Exception {
		descriptions++;
		total++;
	}
	public void processId(DatabaseEntry key, DatabaseEntry value) throws Exception {
		ids++;
		total++;
	}
	public void processImages(DatabaseEntry key, DatabaseEntry value) throws Exception {
		images++;
		total++;
	}

	public int getConcepts() {
		return concepts;
	}

	public int getDescriptions() {
		return descriptions;
	}

	public int getRelationships() {
		return relationships;
	}

	public int getTotal() {
		return total;
	}

	public DatabaseEntry getDataEntry() {
		DatabaseEntry data = new DatabaseEntry(); 
		data.setPartial(0, 0, true);
		return data;
	}

	public DatabaseEntry getKeyEntry() {
		DatabaseEntry key = new DatabaseEntry(); 
		key.setPartial(0, 0, true);
		return key;
	}

	public int getIds() {
		return ids;
	}

	public int getImages() {
		return images;
	}



}
