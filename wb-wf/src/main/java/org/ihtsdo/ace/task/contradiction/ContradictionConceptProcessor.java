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
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.contradiction.ContradictionIdentifierBI;
import org.ihtsdo.tk.contradiction.ContradictionResult;

public class ContradictionConceptProcessor implements ProcessUnfetchedConceptDataBI {

    NidBitSetBI cNids = new IdentifierSet();
    private ContradictionIdentifierBI detector = null;
    private ContradictionIdentificationResults results = null;
    private I_ShowActivity activityMonitor;
    private AtomicInteger count = new AtomicInteger();
    private AtomicInteger found = new AtomicInteger();

    public ContradictionConceptProcessor(PositionBI position) {
        buildInvestigationSet();
        results = new ContradictionIdentificationResults();
        detector = Ts.get().getConflictIdentifier();
    }

    public ContradictionConceptProcessor(PositionBI position,
            NidBitSetBI cNids) {
        this.cNids = cNids;
        results = new ContradictionIdentificationResults();
        detector = Ts.get().getConflictIdentifier();
    }

    public ContradictionConceptProcessor(PositionBI position,
            NidBitSetBI cNids, I_ShowActivity activityMonitor) {
        this.cNids = cNids;
        results = new ContradictionIdentificationResults();
        detector = Ts.get().getConflictIdentifier();
        this.activityMonitor = activityMonitor;
        activityMonitor.setMaximum(cNids.cardinality());
        activityMonitor.setValue(count.get());
        activityMonitor.setIndeterminate(false);
    }

    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fcfc) throws Exception {
        int currentCount = count.incrementAndGet();
        if (activityMonitor != null && activityMonitor.isCanceled()) {
            return;
        }
        if (cNids.isMember(cNid)) {
            ConceptChronicleBI c = fcfc.fetch();
            ContradictionResult position = (detector.inConflict(c));

            if (position.equals(ContradictionResult.CONTRADICTION)) {
                results.addConflict(c.getConceptNid());
                found.incrementAndGet();
            } //			else if (position.equals(CONTRADICTION_RESULT.UNREACHABLE))
            //				results.addUnreachable(c.getConceptNid());
            else if (position.equals(ContradictionResult.SINGLE)) {
                results.addSingle(c.getConceptNid());
            } else {
                results.addNoneConflicting(c.getConceptNid());
            }
        }
        if (activityMonitor != null && currentCount % 500 == 0) {
            activityMonitor.setValue(count.get());
            activityMonitor.setProgressInfoLower("Contradictions: " + found.get());
        }
    }

    private void buildInvestigationSet() {
        I_RepresentIdSet set = new IdentifierSet();

        try {

            // Unit Test #1 (Procedure (procedure))
            set.setMember(Terms.get().getConcept(UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790")).getConceptNid());
            // Unit Test #2 ( Newly Modeled Concept PT (under procedure) )
            set.setMember(Terms.get().getConcept(UUID.fromString("f3a3a835-8de5-4bba-820e-90ad02a8fc49")).getConceptNid());

            // Unit Test #3 (Administrative procedure (procedure) )
            // Unit Test #4 (Blood bank procedure (procedure) )
            // Unit Test #5 (Community health procedure (procedure) )
            set.setMember(Terms.get().getConcept(UUID.fromString("5ee78031-c76d-3b01-8df7-3d5243ba7876")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("4c0a8992-2e78-3b97-afbd-cf6f6d5dc666")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("97cd9c54-9e7e-31a8-a1e4-af3d13db6ebf")).getConceptNid());

            // Unit Test #6 (Determination of information related to transfusion (procedure)))
            // Unit Test #7 (Environmental care procedure (procedure)) 
            // Unit Test #8 (Management procedure (procedure))
            // Unit Test #9 (Mechanical construction (procedure))
            set.setMember(Terms.get().getConcept(UUID.fromString("3e67adcd-0740-34a3-90c9-f06d5c5da830")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("f28b3624-2bfe-3ebb-9d27-3ca97b097aae")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("a5241b46-853c-3e7a-8bd3-5c74f5aa1d60")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("9e8d0c6f-9ef9-3d84-9682-fdc2afee94f8")).getConceptNid());


            // Unit Test #10 (Clinical finding (finding))
            // Unit Test #11 (Administrative statuses (finding))
            // Unit Test #12 (Adverse incident outcome categories (finding))
            // Unit Test #13 (Bleeding (finding))
            // Unit Test #14 (Calculus finding (finding))
            // Unit Test #15 (Clinical history and observation findings (finding))

            set.setMember(Terms.get().getConcept(UUID.fromString("bd83b1dd-5a82-34fa-bb52-06f666420a1c")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("d851e4c9-2c1f-32ab-8648-0a8bcd086ce1")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("b807ab7f-e643-31dc-9143-4469e9bf9ce2")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("89ce6b87-545b-3138-82c7-aafa76f8f9a0")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("181c903d-aaa7-38eb-863b-dceeccf07cef")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("54db2917-84be-313b-acd5-defce77c0077")).getConceptNid());


            /*	
             * Invalid Test
             * 
            // Unit Test #16 (Jesse FSN/Jesse Preferred Term)
            set.setMember(Terms.get().getConcept(UUID.fromString("efac07fb-6f7b-4f4e-98a2-236d34f99389")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("61ed2f1f-a65d-424d-9c73-a7af8b008bc4")).getConceptNid());
             */

            // Unit Test #17 (Event (event))
            // Unit Test #18 (Abuse (event))
            // Unit Test #19 (Accidental event (event))
            // Unit Test #20 (Bioterrorism related event (event))

            // Unit Test #21 (Death (event))
            // Unit Test #22 (Disease outbreak (event))
            // Unit Test #23 (E-mail received from patient (event))
            // Unit Test #24 (Killing (event))

            // Unit Test #25 (Event of undetermined intent (event))
            // Unit Test #26 (Event related to biological agent (event))
            // Unit Test #27 (Exposure to potentially harmful entity (event))
            // Unit Test #28 (Immediately dangerous to life and health condition (event))  

            // Unit Test #29 (Legal intervention (event))
            // Unit Test #30 (Environmental event (event))
            // Unit Test #31 (Notable event (event))  

            set.setMember(Terms.get().getConcept(UUID.fromString("c7243365-510d-3e5f-82b3-7286b27d7698")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("d721d31d-8131-3164-972d-0f53cb591f17")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("4ad9817a-ff22-3d19-9213-de5270beb17a")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("90bda575-2b85-3e8e-9a9d-e259ed2d5fe8")).getConceptNid());

            set.setMember(Terms.get().getConcept(UUID.fromString("ba7e96f8-7cad-3157-8c95-0290395c13d2")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("4895c796-da12-396e-9f8f-a677ee887a08")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("7560feb1-0778-314d-bc76-2d5071def2fa")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("d030dbe6-5399-306c-82e1-231fb2976b3f")).getConceptNid());

            set.setMember(Terms.get().getConcept(UUID.fromString("45b68c33-459d-3142-885d-3112c6b80167")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("ffdb4258-23ed-3644-add7-ebff1b96faf3")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("9a4977e0-259d-3281-9edb-3eb576676109")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("a28c605f-0830-3f0a-a0cb-080db7bf07cf")).getConceptNid());

            set.setMember(Terms.get().getConcept(UUID.fromString("7d25ce7c-d547-35d9-a0f8-3b19fa854cb6")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("bc38e179-f284-317e-b8a5-5da4af35dac4")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("08515a59-728c-32fc-840d-0bd835ef58a5")).getConceptNid());




            // Unit Test #32 (Observable entity (observable entity))
            // Unit Test #33 (Age AND/OR growth period (observable entity))
            // Unit Test #34 (Body product observable (observable entity))

            // Unit Test #35 (Clinical history/examination observable (observable entity))
            // Unit Test #36 (Device observable (observable entity))
            // Unit Test #37 (Disease activity score using 28 joint count (observable entity))

            // Unit Test #38 (Drug therapy observable (observable entity))
            // Unit Test #39 (Function (observable entity)) 
            // Unit Test #40 (Feature of entity (observable entity)) 

            set.setMember(Terms.get().getConcept(UUID.fromString("d678e7a6-5562-3ff1-800e-ab070e329824")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("ea200c6b-7184-3f76-9f20-67a9945fa993")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("8b16bdf8-a552-317c-ae59-3d55f8c9ecf0")).getConceptNid());

            set.setMember(Terms.get().getConcept(UUID.fromString("3ee307a6-c10c-3145-bdbe-1126cee4f149")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("bfd5181c-1eac-3b62-adcf-47ecb6c1034a")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("fc447a06-bd02-3411-a949-8c2bebb2a0f8")).getConceptNid());

            set.setMember(Terms.get().getConcept(UUID.fromString("a0b5514e-ef66-3361-b4db-50207dd27ed8")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("ecad6cb8-d639-39c5-b8a6-b88196b6a96b")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("ae323791-2c09-30cc-a255-8ba9fa2f8213")).getConceptNid());


            // Unit Test #41 (Organism (organism))
            // Unit Test #42 (Kingdom Animalia (organism))
            // Unit Test #43 (Kingdom Chromista (organism)) 
            // Unit Test #44 (Kingdom Plantae (organism)) 

            set.setMember(Terms.get().getConcept(UUID.fromString("0bab48ac-3030-3568-93d8-aee0f63bf072")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("bb5fc878-253a-34f4-b043-64f2241bc965")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("adc51007-36f4-3d62-9aa1-9fe318ed700e")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("c4ac913b-f6e9-3993-a1a8-d6a6a429b2bd")).getConceptNid());

            // Unit Test #45 (Pharmaceutical / biologic product (product))
            // Unit Test #46 (Acetic acid product (product))
            // Unit Test #47 (Alcohol products (product))
            // Unit Test #48 (Analgesic (product)) 

            set.setMember(Terms.get().getConcept(UUID.fromString("5032532f-6b58-31f9-84c1-4a365dde4449")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("0a31807b-1368-330b-8c1c-079eb4241195")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("e4a3993a-ac6a-3969-8c3c-fd419a8b1c83")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("0a0b1483-5b90-36fc-9691-6286ef42c4e2")).getConceptNid());

            // Unit Test #49 (Physical object (physical object)) 
            // Unit Test #50 (Device (physical object)) 
            // Unit Test #51 (Domestic, office and garden artefact (physical object)) 
            // Unit Test #52 (Fastening (physical object)) 
            set.setMember(Terms.get().getConcept(UUID.fromString("72765109-6b53-3814-9b05-34ebddd16592")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("6e8f5d95-0505-3aa8-a730-e1e9d821e4aa")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("f4690ead-1a6e-3f8c-8dcb-f213b5cf9796")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("2ed8c550-a396-3b37-aa64-d2d6096bf591")).getConceptNid());


            /*
            // *** REFSETS ***
            // Unit Test #1  (Death)
            // Unit Test #2  
            // Unit Test #3  (Accidental Death)
            // Unit Test #4  (Death-Expected)
            // Unit Test #5  (Death by ashphyxiation)
            // Unit Test #6  (Death by electrocution)
            // Unit Test #7  (Alejandro Lopez)
            set.setMember(Terms.get().getConcept(UUID.fromString("ba7e96f8-7cad-3157-8c95-0290395c13d2")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("c4d25f5f-1f63-37e9-8775-c851fe99cb87")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("21cdeb5d-f402-3124-9f8d-00c6176dc37f")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("935ca22f-7703-3370-9e9f-1025694f3f9b")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("5a27bef4-41e0-33e7-8da3-76450d82cbf5")).getConceptNid());
            set.setMember(Terms.get().getConcept(UUID.fromString("800e6651-a619-3edf-bb90-74ab279966c9")).getConceptNid());
             */
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Error in intializing Contradiction Concept Processor", e);
        }

        setNidSet(set);
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
