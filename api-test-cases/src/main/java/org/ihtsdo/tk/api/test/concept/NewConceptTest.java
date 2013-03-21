package org.ihtsdo.tk.api.test.concept;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
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
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.test.ConceptHelper;
import org.ihtsdo.tk.api.test.DefaultProfileBuilder;
import org.ihtsdo.tk.api.test.TestArtefact;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.intsdo.junit.bdb.BdbTestRunner;
import org.intsdo.junit.bdb.BdbTestRunnerConfig;
import org.intsdo.junit.bdb.DependsOn;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig(init = { DefaultProfileBuilder.class })
public class NewConceptTest {

    private static final String fsnTerm = String.format("New Concept %d (test concept)", System.currentTimeMillis());
    private static final String prefTerm = "New Concept";
    
    @Test
    public void createConcept() throws Exception {

        TerminologyStoreDI termStore = Ts.get();
        
        ConceptChronicleBI newConcept = 
                ConceptHelper.createNewConcept(fsnTerm, prefTerm, getParentConcept().getConceptNid());

        termStore.addUncommitted(newConcept);
        termStore.commit();
    }

    @Test
    @DependsOn("createConcept")
    public void checkRelationships() throws Exception {
        
        ConceptChronicleBI parentConcept = TestArtefact.get("parentConcept", ConceptChronicleBI.class);
        ConceptChronicleBI relTypeConcept = TestArtefact.get("relTypeConcept", ConceptChronicleBI.class);
        ConceptChronicleBI storedConcept = Ts.get().getConcept(TestArtefact.get("conceptUuid", UUID.class));
        
        Collection<? extends RelationshipChronicleBI> sourceRels = storedConcept.getRelationshipsOutgoing();
        assertNotNull(sourceRels);
        assertEquals(1, sourceRels.size());
        for (RelationshipChronicleBI sourceRel : sourceRels) {
            assertTrue(sourceRel.getTargetNid() == parentConcept.getNid());
            assertTrue(sourceRel.getSourceNid() == storedConcept.getConceptNid());
            assertTrue(sourceRel.getEnclosingConcept().getNid() == storedConcept.getNid());
            Collection<? extends RelationshipVersionBI> versions = sourceRel.getVersions();
            assertEquals(1, versions.size());
            for (RelationshipVersionBI<?> relVersion : versions) {
                assertTrue(relVersion.getTypeNid() == relTypeConcept.getNid());                
            }
        }
    }
    
    protected ConceptChronicleBI getParentConcept() throws Exception {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
        TerminologyStoreDI termStore = Ts.get();

        ConceptSpec parentConceptSpec = new ConceptSpec("Substance (substance)",
            UUID.fromString("95f41098-8391-3f5e-9d61-4b019f1de99d"));

        ConceptVersionBI parent = parentConceptSpec.getStrict(config.getViewCoordinate());
        ConceptChronicleBI parentConcept = termStore.getConcept(parent.getConceptNid());

        return parentConcept;
    }

    @Test
    @DependsOn("createConcept")
    public void checkDescriptions() throws Exception {
        ConceptChronicleBI storedConcept = Ts.get().getConcept(TestArtefact.get("conceptUuid", UUID.class));
        verifyDescription(storedConcept, fsnTerm);
        verifyDescription(storedConcept, prefTerm);
    }
    
    protected void verifyDescription(ConceptChronicleBI concept, String term) throws Exception {

        int matches = 0;
        assertTrue(concept.getDescriptions().size() > 0);
        for (DescriptionChronicleBI desc : concept.getDescriptions()) {

            assertTrue("Concept desc chronicle contains no versions", desc.getVersions().size() > 0);
            for (DescriptionVersionBI<?> version : desc.getVersions()) {

                if (version.getText().equals(term)) {
                    ++matches;
                }
            }
        }

        assertTrue("Did not locate the term: " + term, matches > 0);
        assertTrue("Found more than one version of the term: " + term, matches < 2);
    }

}
