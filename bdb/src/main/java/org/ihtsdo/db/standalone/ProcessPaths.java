package org.ihtsdo.db.standalone;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;

public class ProcessPaths {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
    	Bdb.setup();
    	try {
    		printConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT);
    		printConcept(RefsetAuxiliary.Concept.REFSET_PATHS);
    		printConcept(RefsetAuxiliary.Concept.REFSET_PATH_ORIGINS);
    		printConcept(ArchitectonicAuxiliary.Concept.PATH);
    		System.out.println("\nNHS UK Extension Path\n");
    		System.out.println(Bdb.getConceptDb().getConcept(
    				Bdb.uuidToNid(UUID.fromString("c281a8f7-01f4-58bb-813b-911d28754133"))));

    		System.out.println("\nNHS UK Drug Extension Path\n");
    		System.out.println(Bdb.getConceptDb().getConcept(
    				Bdb.uuidToNid(UUID.fromString("087de18f-edbb-5b96-af11-117c6c063e20"))));

    		
    		System.out.println("\nfirst\n");
    		System.out.println(Bdb.getConceptDb().getConcept(Bdb.getNidCNidMap().getCNid(-2147479472)));
    		System.out.println(Bdb.getComponent(-2147479472));
    		System.out.println("\nsecond\n");
    		System.out.println(Bdb.getConceptDb().getConcept(Bdb.getNidCNidMap().getCNid(-2147481805)));
    		System.out.println(Bdb.getComponent(-2147481805));
            for (I_Path p: Terms.get().getPaths()) {
				AceLog.getAppLog().info("Found path: " + p);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		AceLog.getAppLog().info("Complete");
		System.exit(0);
	}

	private static void printConcept(I_ConceptualizeUniversally aceConcept)
			throws IOException, TerminologyException {
		Concept c = Bdb.getConceptDb().getConcept(aceConcept.localize().getNid());
		System.out.println(" Found: " + c.toString());
	}
}
