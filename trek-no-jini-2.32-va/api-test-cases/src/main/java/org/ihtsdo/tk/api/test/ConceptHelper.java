package org.ihtsdo.tk.api.test;

import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.binding.snomed.Snomed;

public class ConceptHelper {

    public static ConceptChronicleBI createNewConcept(String fsnTerm, String prefTerm, int parentConceptNid) {
        
        System.out.println("Creating concept: " + fsnTerm);
        
        try {            
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
            TerminologyStoreDI termStore = Ts.get();
            TerminologyBuilderBI builder = 
                    termStore.getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());
            
            ConceptChronicleBI parentConcept = termStore.getConcept(parentConceptNid);
            ConceptChronicleBI relTypeConcept = Snomed.IS_A.getLenient();
            TestArtefact.set("parentConcept", parentConcept);
            TestArtefact.set("relTypeConcept", relTypeConcept);
            
            // Create blueprint
            LANG_CODE langCode = LANG_CODE.EN; // EN_AU not supported!
            ConceptCB conceptBp = 
                    new ConceptCB(fsnTerm, prefTerm, langCode, relTypeConcept.getPrimUuid(), parentConcept.getPrimUuid());
            
            final UUID conceptUuid = UUID.randomUUID();
            conceptBp.setComponentUuid(conceptUuid);
            TestArtefact.set("conceptUuid", conceptUuid);
            
            List<DescriptionCAB> fsnCABs = conceptBp.getFullySpecifiedNameCABs();
            List<DescriptionCAB> prefCABs = conceptBp.getPreferredNameCABs();
            for (DescriptionCAB fsn : fsnCABs) {
                conceptBp.addFullySpecifiedName(fsn, langCode);
            }
            for (DescriptionCAB pref : prefCABs) {
                conceptBp.addPreferredName(pref, langCode);
            }
            
            return builder.construct(conceptBp);

        } catch (Exception e) {            
            throw new RuntimeException(e);
        }
    }

}
