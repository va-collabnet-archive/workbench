package org.ihtsdo.db.standalone;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;

public class ProcessPaths {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
    	Bdb.setup();
    	try {
    		
    		Concept c = Bdb.getConceptDb().getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.localize().getNid());
    		System.out.println(" Found: " + c.toString());
			for (I_Path p: Terms.get().getPaths()) {
				AceLog.getAppLog().info("Found path: " + p);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		AceLog.getAppLog().info("Complete");
		System.exit(0);
	}
}
