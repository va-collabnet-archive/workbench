package org.ihtsdo.tk.api.test;

import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.intsdo.junit.bdb.BdbTestInitialiser;

/**
 * Creates a new concept to be utilised by a test cases. Each concept is unique to
 * ensure tests can be run multiple times on a re-used datasource.<p> 
 * 
 * All concepts will be children of {@link #PARENT_CONCEPT_UUID}<p>
 * 
 * The concept may be obtained from the {@link TestArtefact} store using the key {@link #ARTEFACT_KEY}
 */
public class NewConceptBuilder implements BdbTestInitialiser {

    public static final String PARENT_CONCEPT_UUID = "95f41098-8391-3f5e-9d61-4b019f1de99d";
    
    public static final String ARTEFACT_KEY = "new concept";
    
    @Override
    public void init() {
        try {
            int parentConceptNid = Ts.get().getComponent(UUID.fromString(PARENT_CONCEPT_UUID)).getNid();
            
            String fsnTerm = String.format("New Concept %d (test concept)", System.currentTimeMillis());
            String prefTerm = "New Concept";
            
            ConceptChronicleBI newConcept = 
                    ConceptHelper.createNewConcept(fsnTerm, prefTerm, parentConceptNid);
            TestArtefact.set(ARTEFACT_KEY, newConcept);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
