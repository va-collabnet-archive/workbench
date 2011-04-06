
package org.ihtsdo.ace.task.contradiction;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.contradiction.ContradictionIdentifierBI;
import org.ihtsdo.tk.contradiction.ContradictionResult;

public class ContradictionConceptProcessor implements ProcessUnfetchedConceptDataBI {

    private void buildInvestigationSet() {
        I_RepresentIdSet set = new IdentifierSet();

		try
		{
			buildSet3(set);
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Error in intializing Contradiction Concept Processor", e);
        }
        setNidSet(set);
    }

    NidBitSetBI cNids = new IdentifierSet();
    private ContradictionIdentifierBI detector = null;
    private ContradictionIdentificationResults results = null;
    private I_ShowActivity activityMonitor;
    private AtomicInteger count = new AtomicInteger();
    private AtomicInteger found = new AtomicInteger();

    public ContradictionConceptProcessor(PositionBI position) {
        // Via Task
        buildInvestigationSet();
        results = new ContradictionIdentificationResults();
        detector = Ts.get().getConflictIdentifier();
        detector.setViewPos(position);
    }

    public ContradictionConceptProcessor(PositionBI position,
            NidBitSetBI cNids) {
        this.cNids = cNids;
        results = new ContradictionIdentificationResults();
        detector = Ts.get().getConflictIdentifier();
        detector.setViewPos(position);
    }

    public ContradictionConceptProcessor(PositionBI position,
            NidBitSetBI cNids, I_ShowActivity activityMonitor) {
        this.cNids = cNids;
        results = new ContradictionIdentificationResults();
        detector = Ts.get().getConflictIdentifier();
        detector.setViewPos(position);
        this.activityMonitor = activityMonitor;
        activityMonitor.setMaximum(cNids.cardinality());
        activityMonitor.setValue(count.get());
        activityMonitor.setIndeterminate(false);
    }

    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fcfc) throws Exception {
        int currentCount = count.incrementAndGet();
//        if (currentCount%12500 == 0) 
//        	System.out.println("HERE with " + currentCount);
//        if (activityMonitor != null && activityMonitor.isCanceled()) {
//            return;
//        }
//        
        if (cNids.isMember(cNid)) { 
            ConceptChronicleBI c = fcfc.fetch();
            ContradictionResult position = (detector.isConceptInConflict(c));

            if (position.equals(ContradictionResult.CONTRADICTION)) {
                results.addConflict(c.getConceptNid());
                found.incrementAndGet();
            } else if (position.equals(ContradictionResult.DUPLICATE_EDIT)) {
                results.addConflictingWithSameValueSameCompId(c.getConceptNid());
                found.incrementAndGet();
	        } else if (position.equals(ContradictionResult.DUPLICATE_NEW_COMPONENT)) {
	            results.addConflictingWithSameValueDifferentCompId(c.getConceptNid());
	            found.incrementAndGet();
	        } else if (position.equals(ContradictionResult.ERROR)) {
	        	throw new Exception("Failure in detecting contradictions on concept: " + c.getPrimUuid());
	        }
            // else if (position.equals(CONTRADICTION_RESULT.UNREACHABLE))
            //		results.addUnreachable(c.getConceptNid());
            else if (position.equals(ContradictionResult.SINGLE_MODELER_CHANGE)) {
                results.addSingle(c.getConceptNid());
            } else {
                results.addNoneConflicting(c.getConceptNid());
            }
        }
        
//        if (activityMonitor != null && currentCount % 5 == 0) {
//            activityMonitor.setValue(count.get());
//            activityMonitor.setProgressInfoLower("Contradictions: " + found.get());
//        }
    }




	private void buildSet1(I_RepresentIdSet set) throws TerminologyException, IOException {
		/* SHEET #1 - COMPONENTS */
 		// Unit Test #1 (Snomed CT)
		set.setMember(Terms.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8")).getConceptNid());
		
		// Unit Test #2  (Newly Modeled Concept #1 (under SCT Model Comp))
		// Unit Test #60 (Newly Modeled Concept #2 (under SCT Model Comp))
		// Unit Test #61 (Newly Modeled Concept #3 (under SCT Model Comp))
		/* Find out how this will work */
		set.setMember(Terms.get().getConcept(UUID.fromString("a0f7b747-807c-42a8-8bc2-ab4d7dbbf284")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("31bcce91-09f7-457b-8263-7cceb49b9b64")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("a4eec73d-1770-4505-abea-652ae0314ba9")).getConceptNid());

		// Unit Test #3 (Body structure (body structure))
 		// Unit Test #4 (Anatomical or acquired body structure (body structure))
 		// Unit Test #5 (Anatomical organizational pattern (body structure))
		set.setMember(Terms.get().getConcept(UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790")).getConceptNid());
  		set.setMember(Terms.get().getConcept(UUID.fromString("619c5b5f-1677-3a35-b624-2a23d35fc039")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("bfb87649-850d-32bc-bd96-bc67e850a6fe")).getConceptNid());
	
 		// Unit Test #6 (Anatomical site notations for tumor staging (body structure))
 		// Unit Test #7 (Body structure, altered from its original anatomical structure (morphologic abnormality)) 
 		// Unit Test #8 (Nonspecific site (body structure))
 		// Unit Test #9 (Physical anatomical entity (body structure))
		set.setMember(Terms.get().getConcept(UUID.fromString("45b6a265-15cc-3f2f-8727-79122b0fd180")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("9defab14-6d7b-3173-bbc2-ac30b76928b3")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("6df01706-a920-3444-9014-91253f16e1e5")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("afee3454-a2ef-3d89-a51e-c3337317296a")).getConceptNid());

 		// Unit Test #10 (Clinical finding (finding))
 		// Unit Test #11 (Clinical stage finding (finding))
 		// Unit Test #12 (Cyanosis (finding))
 		// Unit Test #13 (Bleeding (finding))
 		// Unit Test #14 (Calculus finding (finding))
 		// Unit Test #15 (Clinical history and observation findings (finding))

 		set.setMember(Terms.get().getConcept(UUID.fromString("bd83b1dd-5a82-34fa-bb52-06f666420a1c")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("a9ed3709-70ef-3e27-9651-a8d034fccb28")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("d52fb8f7-095c-3f34-9cb0-db4102dc1803")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("89ce6b87-545b-3138-82c7-aafa76f8f9a0")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("181c903d-aaa7-38eb-863b-dceeccf07cef")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("54db2917-84be-313b-acd5-defce77c0077")).getConceptNid());

			
//		// Unit Test #16 (Jesse FSN/Jesse Preferred Term) * Invalid Test
//		set.setMember(Terms.get().getConcept(UUID.fromString("efac07fb-6f7b-4f4e-98a2-236d34f99389")).getConceptNid());
//		set.setMember(Terms.get().getConcept(UUID.fromString("61ed2f1f-a65d-424d-9c73-a7af8b008bc4")).getConceptNid());
			
		
		
 		// Unit Test #17 (Abuse (event) (EVENT BAD))
 		// Unit Test #18 (Accidental event (event))
 		// Unit Test #19 (Bioterrorism related event (event))

		// Unit Test #20 (Death (event))
 		// Unit Test #21 (Legal intervention (event))
 		// Unit Test #22 (Environmental event (event))
		
 		// Unit Test #23 (Event of undetermined intent (event))
		// Unit Test #24 (Event related to biological agent (event))
		// Unit Test #25 (Exposure to potentially harmful entity (event))
 		
 		// Unit Test #26 (Immediately dangerous to life and health condition (event))  
		// Unit Test #27 (Intentionally harming self (event))
 		// Unit Test #28 (Killing (event))
 		set.setMember(Terms.get().getConcept(UUID.fromString("d721d31d-8131-3164-972d-0f53cb591f17")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("4ad9817a-ff22-3d19-9213-de5270beb17a")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("90bda575-2b85-3e8e-9a9d-e259ed2d5fe8")).getConceptNid());
 
 		set.setMember(Terms.get().getConcept(UUID.fromString("ba7e96f8-7cad-3157-8c95-0290395c13d2")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("7d25ce7c-d547-35d9-a0f8-3b19fa854cb6")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("bc38e179-f284-317e-b8a5-5da4af35dac4")).getConceptNid());

 		set.setMember(Terms.get().getConcept(UUID.fromString("45b68c33-459d-3142-885d-3112c6b80167")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("ffdb4258-23ed-3644-add7-ebff1b96faf3")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("9a4977e0-259d-3281-9edb-3eb576676109")).getConceptNid());

 		set.setMember(Terms.get().getConcept(UUID.fromString("a28c605f-0830-3f0a-a0cb-080db7bf07cf")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("5ffe7bf9-805a-3fbe-8630-9f4ac300532c")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("d030dbe6-5399-306c-82e1-231fb2976b3f")).getConceptNid());


 		
 
 		// Unit Test #29 (Function (observable entity)  (OBSERVABLE ENTITY BAD))
 		// Unit Test #30 (Age AND/OR growth period (observable entity))
 		// Unit Test #31 (Body product observable (observable entity))

 		// Unit Test #32 (Clinical history/examination observable (observable entity))
 		// Unit Test #33 (Device observable (observable entity))
 		// Unit Test #34 (Disease activity score using 28 joint count (observable entity))
 		
 		// Unit Test #35 (Drug therapy observable (observable entity))
 		// Unit Test #36 (Environment observable (observable entity)) 
 		// Unit Test #37 (Feature of entity (observable entity)) 
		set.setMember(Terms.get().getConcept(UUID.fromString("ecad6cb8-d639-39c5-b8a6-b88196b6a96b")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("ea200c6b-7184-3f76-9f20-67a9945fa993")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("8b16bdf8-a552-317c-ae59-3d55f8c9ecf0")).getConceptNid());
 		
 		set.setMember(Terms.get().getConcept(UUID.fromString("3ee307a6-c10c-3145-bdbe-1126cee4f149")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("bfd5181c-1eac-3b62-adcf-47ecb6c1034a")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("fc447a06-bd02-3411-a949-8c2bebb2a0f8")).getConceptNid());
 		
 		set.setMember(Terms.get().getConcept(UUID.fromString("a0b5514e-ef66-3361-b4db-50207dd27ed8")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("50f8dbed-d940-3277-9a9d-06960c137079")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("ae323791-2c09-30cc-a255-8ba9fa2f8213")).getConceptNid());


		// Unit Test #38 (Life-cycle form (organism)  (ORGANISM ENTITY BAD))
 		// Unit Test #39 (Kingdom Animalia (organism))
 		// Unit Test #40 (Kingdom Chromista (organism)) 
 		// Unit Test #41 (Kingdom Plantae (organism)) 
 		set.setMember(Terms.get().getConcept(UUID.fromString("85133052-e1b7-3a5d-aa09-2fd1fafc982e")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("bb5fc878-253a-34f4-b043-64f2241bc965")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("adc51007-36f4-3d62-9aa1-9fe318ed700e")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("c4ac913b-f6e9-3993-a1a8-d6a6a429b2bd")).getConceptNid());

		// Unit Test #42 (Laboratory procedure (procedure))
 		// Unit Test #43 (Activity of daily living procedures and interventions (procedure))
 		// Unit Test #44 (Administrative procedure (procedure))
 		// Unit Test #45 (General treatment (procedure)) 
 		set.setMember(Terms.get().getConcept(UUID.fromString("42bd9b4b-f6da-39d9-b6a7-bb0d4690d6f9")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("f1aa24f1-a3e9-3cb4-ac2a-a7a41b678559")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("5ee78031-c76d-3b01-8df7-3d5243ba7876")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("2705f5d9-e919-3026-bcc2-72bc922fba56")).getConceptNid());

 		// Unit Test #46 (Physical force (physical force)) 
 		// Unit Test #47 (Altitude (physical force)) 
 		// Unit Test #48 (Electricity (physical force)) 
 		// Unit Test #49 (Explosion (physical force)) 

		set.setMember(Terms.get().getConcept(UUID.fromString("32213bf6-c073-3ce1-b0c7-9463e43af2f1")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("01403529-5182-37d4-842c-92cd68aa4a0a")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("e5f52e8e-43f9-3190-a52f-53fdb4a5c86b")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("64df8df0-8efa-3a13-a52c-643ad7ebe7d8")).getConceptNid());

 		// Unit Test #50 (Explosive force (physical force))
 		// Unit Test #51 (Fire (physical force))
 		// Unit Test #52 (Friction (physical force))  
		set.setMember(Terms.get().getConcept(UUID.fromString("b772f759-5ab3-3f58-83a4-8cf533316220")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("58977bfa-7bbe-33fd-977c-a102c1871d03")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("ff611b01-6d07-3eaf-b5bd-f95e2b8dd7fe")).getConceptNid());


 		// Unit Test #53 (Community health procedure (procedure))
 		// Unit Test #54 (Determination of information related to transfusion (procedure))
 		// Unit Test #55 (Environmental care procedure (procedure))  
		set.setMember(Terms.get().getConcept(UUID.fromString("97cd9c54-9e7e-31a8-a1e4-af3d13db6ebf")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("3e67adcd-0740-34a3-90c9-f06d5c5da830")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("f28b3624-2bfe-3ebb-9d27-3ca97b097aae")).getConceptNid());

	}

	
	

	private void buildSet2(I_RepresentIdSet set) throws TerminologyException, IOException {
		
		/* SHEET #2 - POST RESOLUTION*/
/* Broken for now . . . how to handle these cases? 
		// Unit Test #1 (Physical force (physical force))
 		// Unit Test #2 (agente infeccioso (organismo))
 		// Unit Test #3 (Animal (organism))
		set.setMember(Terms.get().getConcept(UUID.fromString("a0f7b747-807c-42a8-8bc2-ab4d7dbbf284")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("31bcce91-09f7-457b-8263-7cceb49b9b64")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("a4eec73d-1770-4505-abea-652ae0314ba9")).getConceptNid());
*/
		
 		// Unit Test #4 (Body structure (body structure))
		// Unit Test #5 (Anatomical or acquired body structure (body structure)) 
 		// Unit Test #6 (Anatomical organizational pattern (body structure)) 
 		set.setMember(Terms.get().getConcept(UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("619c5b5f-1677-3a35-b624-2a23d35fc039")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("bfb87649-850d-32bc-bd96-bc67e850a6fe")).getConceptNid());
			
 		// Unit Test #7 (Anatomical site notations for tumor staging (body structure)) 
 		// Unit Test #8 (Body structure, altered from its original anatomical structure (morphologic abnormality)) 
		// Unit Test #9 (Nonspecific site (body structure)) 
		// Unit Test #10 (Physical anatomical entity (body structure)m) 
 		set.setMember(Terms.get().getConcept(UUID.fromString("45b6a265-15cc-3f2f-8727-79122b0fd180")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("9defab14-6d7b-3173-bbc2-ac30b76928b3")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("6df01706-a920-3444-9014-91253f16e1e5")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("afee3454-a2ef-3d89-a51e-c3337317296a")).getConceptNid());

 		// Unit Test #11 (Activity of daily living procedures and interventions (procedure))
 		// Unit Test #12 (Clinical history/examination observable (observable entity)) 
 		// Unit Test #13 (Clinical stage finding (finding)) 
 		// Unit Test #14 (Cyanosis (finding)) 
 		// Unit Test #15 (Function (observable entity)) 
 		// Unit Test #16 (Age AND/OR growth period (observable entity)) 
 		// Unit Test #17 (Body product observable (observable entity)) 
 		// Unit Test #21 (Legal intervention (event))
 		// Unit Test #19 (Environmental event (event)) 
 		// Unit Test #20 (Exposure to potentially harmful entity (event)) 
		set.setMember(Terms.get().getConcept(UUID.fromString("f1aa24f1-a3e9-3cb4-ac2a-a7a41b678559")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("3ee307a6-c10c-3145-bdbe-1126cee4f149")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("a9ed3709-70ef-3e27-9651-a8d034fccb28")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("d52fb8f7-095c-3f34-9cb0-db4102dc1803")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("ecad6cb8-d639-39c5-b8a6-b88196b6a96b")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("ea200c6b-7184-3f76-9f20-67a9945fa993")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("8b16bdf8-a552-317c-ae59-3d55f8c9ecf0")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("7d25ce7c-d547-35d9-a0f8-3b19fa854cb6")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("bc38e179-f284-317e-b8a5-5da4af35dac4")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("9a4977e0-259d-3281-9edb-3eb576676109")).getConceptNid());
		
 		// Unit Test #21 (Life-cycle form (organism)) 
 		// Unit Test #22 (Altitude (physical force)) 
 		// Unit Test #23 (Determination of information related to transfusion (procedure)) 
 		// Unit Test #24 (Immediately dangerous to life and health condition (event)) 
 		// Unit Test #25 (Abuse (event)
		 
 		// Unit Test #26 (Accidental event (event))
		// Unit Test #27 (Bioterrorism related event (event)) 
/* To fix 		// Unit Test #28 (Disease outbreak (event)) */ 
 		// Unit Test #29 (Explosion (physical force)) 
 		// Unit Test #30 (Drug therapy observable (observable entity)) 
		set.setMember(Terms.get().getConcept(UUID.fromString("85133052-e1b7-3a5d-aa09-2fd1fafc982e")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("01403529-5182-37d4-842c-92cd68aa4a0a")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("3e67adcd-0740-34a3-90c9-f06d5c5da830")).getConceptNid()); 
		set.setMember(Terms.get().getConcept(UUID.fromString("a28c605f-0830-3f0a-a0cb-080db7bf07cf")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("d721d31d-8131-3164-972d-0f53cb591f17")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("4ad9817a-ff22-3d19-9213-de5270beb17a")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("90bda575-2b85-3e8e-9a9d-e259ed2d5fe8")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("4895c796-da12-396e-9f8f-a677ee887a08")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("64df8df0-8efa-3a13-a52c-643ad7ebe7d8")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("a0b5514e-ef66-3361-b4db-50207dd27ed8")).getConceptNid());
	}
 

	
	
    private void buildSet3(I_RepresentIdSet set) throws TerminologyException, IOException {
    	/* Single Test */
    	set.setMember(Terms.get().getConcept(UUID.fromString("842bb237-4872-3bf3-8a04-2786db39f084")).getConceptNid());
    	set.setMember(Terms.get().getConcept(UUID.fromString("66387a1b-9bb6-361d-99f0-0a3147cad7f2")).getConceptNid());
    	set.setMember(Terms.get().getConcept(UUID.fromString("77e33983-b911-3099-bac2-6107ee48065d")).getConceptNid());
	}

    private void buildRefset1(I_RepresentIdSet set) throws TerminologyException, IOException {
    	set.setMember(Terms.get().getConcept(UUID.fromString("aa4052f3-0faa-39dd-b838-a4d1802ccd59")).getConceptNid());
    }
    public ContradictionIdentificationResults getResults() {
        return results;
    }

    public void setNidSet(NidBitSetBI nids) {
        this.cNids = nids; 
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return cNids;
    }

    @Override
    public boolean continueWork() {
        if (activityMonitor != null) {
            return !activityMonitor.isCanceled();
        }
        return true;
    }
}
 
