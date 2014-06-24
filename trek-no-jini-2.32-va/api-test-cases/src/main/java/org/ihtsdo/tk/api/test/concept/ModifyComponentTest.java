package org.ihtsdo.tk.api.test.concept;

import static junit.framework.Assert.assertEquals;

import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.test.DefaultProfileBuilder;
import org.ihtsdo.tk.api.test.NewConceptBuilder;
import org.ihtsdo.tk.api.test.TestArtefact;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedRelationshipType;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipType;
import org.intsdo.junit.bdb.BdbTestRunner;
import org.intsdo.junit.bdb.BdbTestRunnerConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig(init = {DefaultProfileBuilder.class, NewConceptBuilder.class})
public class ModifyComponentTest {

    private static final String UUID_DIGESTIVE_ORGAN = "3cbbca9b-7aa8-3463-8eb5-24391adfb3ec";
    
    @Test
    public void addDescriptions() throws Exception {
        ConceptChronicleBI concept = TestArtefact.get(NewConceptBuilder.ARTEFACT_KEY, ConceptChronicleBI.class);
        
        Collection<? extends DescriptionChronicleBI> descriptions = concept.getDescriptions();
        int descCount = descriptions.size();
        
        int synonymTypeNid = SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid();
        
        DescriptionCAB descBlueprint = new DescriptionCAB(concept.getConceptNid(), synonymTypeNid, LANG_CODE.EN_AU, "NAF Description", false);
        
        TerminologyStoreDI termStore = Ts.get();
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
        
        TerminologyBuilderBI builder = termStore.getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());
        
        DescriptionChronicleBI newDescription = builder.construct(descBlueprint);
        
        concept = TestArtefact.get(NewConceptBuilder.ARTEFACT_KEY, ConceptChronicleBI.class);
        descriptions = concept.getDescriptions();
        
        assertEquals(descCount + 1, descriptions.size());
    }
    
    @Test
    public void addRelationship() throws Exception {
        TerminologyStoreDI termStore = Ts.get();
        ConceptChronicleBI digestiveOrgan = termStore.getConcept(UUID.fromString(UUID_DIGESTIVE_ORGAN));
        int targetDestRelCount = digestiveOrgan.getRelationshipsIncoming().size();

        ConceptChronicleBI concept = TestArtefact.get(NewConceptBuilder.ARTEFACT_KEY, ConceptChronicleBI.class);

        Collection<? extends RelationshipChronicleBI> sourceRels = concept.getRelationshipsOutgoing();
        int sourceRelCount = sourceRels.size();
        
        int relTypeNid = SnomedRelationshipType.FINDING_SITE.getLenient().getNid();
        
        RelationshipCAB relBlueprint = new RelationshipCAB(concept.getConceptNid(), relTypeNid, digestiveOrgan.getNid(), 0, TkRelationshipType.STATED_ROLE);
        
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        TerminologyBuilderBI builder = termStore.getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());
        
        RelationshipChronicleBI newSourceRel = builder.construct(relBlueprint);
       
        concept = TestArtefact.get(NewConceptBuilder.ARTEFACT_KEY, ConceptChronicleBI.class);
        sourceRels = concept.getRelationshipsOutgoing();
        assertEquals(sourceRelCount + 1, sourceRels.size());
        
        digestiveOrgan = termStore.getConcept(UUID.fromString(UUID_DIGESTIVE_ORGAN));
        assertEquals(targetDestRelCount + 1, digestiveOrgan.getRelationshipsIncoming().size());
    }
    
}
