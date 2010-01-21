package org.ihtsdo.db.standalone;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.I_ProcessConceptData;
import org.ihtsdo.db.bdb.concept.component.description.Description;

public class ProcessPaths {

	private static class CountDescriptions implements I_ProcessConceptData {
		AtomicInteger descCount = new AtomicInteger();
		@Override
		public void processConceptData(Concept concept) throws Exception {
			for (Description d: concept.getDescriptions()) {
				
				descCount.incrementAndGet();
			}
		}
	}
	private static class ParallelDescriptions implements I_ProcessConceptData {
		AtomicInteger descCount = new AtomicInteger();
		@Override
		public void processConceptData(Concept concept) throws Exception {
			for (Description d: concept.getDescriptions()) {
				descCount.incrementAndGet();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
    	Bdb.setup();
    	try {
    		/*printConcept(RefsetAuxiliary.Concept.REFSET_PATHS);
    		 printConcept(RefsetAuxiliary.Concept.REFSET_PATH_ORIGINS);
    		 printConcept(ArchitectonicAuxiliary.Concept.PATH);
    		 System.out.println("\nNHS UK Extension Path\n");
    		 System.out.println(Bdb.getConceptDb().getConcept(
    				Bdb.uuidToNid(UUID.fromString("c281a8f7-01f4-58bb-813b-911d28754133"))));

    		 System.out.println("\nNHS UK Drug Extension Path\n");
    		 System.out.println(Bdb.getConceptDb().getConcept(
    				Bdb.uuidToNid(UUID.fromString("087de18f-edbb-5b96-af11-117c6c063e20"))));
			*/
            for (I_Path p: Terms.get().getPaths()) {
				AceLog.getAppLog().info("Found path: " + p);
				long startTime = System.currentTimeMillis();
				CountDescriptions counter = new CountDescriptions();
				Bdb.getConceptDb().iterateConceptDataInSequence(counter);
				System.out.println("sequential iteration found " + counter.descCount.get() + " descriptions in: " + 
						(System.currentTimeMillis() - startTime) + " ms.");
				ParallelDescriptions parallelCounter = new ParallelDescriptions();
				startTime = System.currentTimeMillis();
				Bdb.getConceptDb().iterateConceptDataInParallel(parallelCounter);
				System.out.println("parallel iteration found " + parallelCounter.descCount.get() + " descriptions in: " + 
						(System.currentTimeMillis() - startTime) + " ms.");
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
