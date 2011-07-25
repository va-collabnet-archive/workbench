/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.sct;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.conflict.ContradictionManagementStrategy;
import org.dwfa.vodb.conflict.EditPathLosesStrategy;
import org.dwfa.vodb.conflict.EditPathWinsStrategy;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;
import org.dwfa.vodb.conflict.LastCommitWinsConflictResolutionStrategy;
import org.dwfa.vodb.conflict.ViewPathLosesStrategy;
import org.dwfa.vodb.conflict.ViewPathWinsStrategy;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.intsdo.junit.bdb.BdbTestRunner;
import org.intsdo.junit.bdb.BdbTestRunnerConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for tracker id: artf221341
 * @author kec
 */
@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig()

public class ConflictResolutionIntegrationTest {
	private int retireNid;
	private PathBI viewPath;
	private PathBI[] authorPaths;
	private UUID parentNode;
	private UUID conceptToRetire;
	
	private final String viewPathStr = "resolution origin path";
	private final String authorPathStr = "author n path";
	private ViewCoordinate vc;
	private I_GetConceptData userA;
	private I_GetConceptData userB;

    private void createAuthorPaths(Collection<? extends RelationshipChronicleBI> childPaths) {
    	authorPaths = new PathBI[2];

    	try {
	    	for (RelationshipChronicleBI childRel : childPaths) {
	    		I_GetConceptData pathCon = Terms.get().getConcept(childRel.getOriginNid());
	    		System.out.println("PATH = " + pathCon);
	    		PathBI path = Terms.get().getPath(childRel.getOriginNid());
	    		String s = pathCon.getInitialText();
	
	    		String[] searchWords = authorPathStr.split(" ");
	    		if (s.startsWith(searchWords[0]) && s.endsWith(searchWords[2])) {
		    		String[] compareWords = s.split(" ");
	    			authorPaths[Integer.parseInt(compareWords[1]) - 1] = path;
	    		}
	    	}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    private void getViewPath(Collection<? extends RelationshipChronicleBI> childPaths) {
    	try {
	    	for (RelationshipChronicleBI childRel : childPaths) {
	    		PathBI path = Terms.get().getPath(childRel.getOriginNid());
	    		I_GetConceptData pathCon = Terms.get().getConcept(childRel.getOriginNid());
	    		String s = pathCon.getInitialText();
	
	    		if (s.equals(viewPathStr)) {
	    			viewPath = path;
	    		}
	    	}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}

	@BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
		try {
	        userA = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.JALEH_MIZRA.getPrimoridalUid());
	        userB = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PHILLIP_BROTTMAN.getPrimoridalUid());

//	    	retireNid = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.RETIRED.getUids()).getConceptNid();
	    	retireNid = Terms.get().getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getUuids()).getConceptNid();
	
			// Body Structure
	        conceptToRetire = UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790");
	
	        // development Concept (path)
			parentNode = UUID.fromString("e89c2b90-c85a-3dfb-978e-8df49046592b"); 
			I_GetConceptData parentCon = Terms.get().getConcept(parentNode);	
			Collection<? extends RelationshipChronicleBI> childPaths = parentCon.getRelsIncoming();
			
			createAuthorPaths(childPaths);
			getViewPath(childPaths);
	
			vc = Ts.get().getMetadataVC();
	        
			PositionSetBI viewPos = new PositionSetReadOnly(Terms.get().newPosition(viewPath, Long.MAX_VALUE));
			vc.setPositionSet(viewPos);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @After
    public void tearDown() {
    }

    
    @Test
	public void testEditPathLosesStrategy() throws IOException, ContraditionException {
    	ContradictionManagementStrategy strategy = new EditPathLosesStrategy();
    	setupTest(strategy);
	}
	

    @Test
	public void testEditPathWinsStrategy() throws IOException, ContraditionException {
		setupTest(new EditPathWinsStrategy());
	}

    @Test
	public void testIdentifyAllConflictStrategy() throws IOException, ContraditionException {
		setupTest(new IdentifyAllConflictStrategy());
	}

    @Test
	public void testLastCommitWinsConflictResolutionStrategy() throws IOException, ContraditionException {
		setupTest(new LastCommitWinsConflictResolutionStrategy());
	}

    @Test
	public void testViewPathLosesStrategy() throws IOException, ContraditionException {
		setupTest(new ViewPathLosesStrategy());
	}

    @Test
	public void testViewPathWinsStrategy() throws IOException, ContraditionException {
		setupTest(new ViewPathWinsStrategy());
	}

	public void setupTest(ContradictionManagementStrategy strategy) throws IOException, ContraditionException {
        
        try {
			I_GetConceptData testCon = Terms.get().getConcept(conceptToRetire);
			vc.setContradictionManager(strategy);
	        	        
//	        retireConcept(testCon, userA, authorPaths[0], ArchitectonicAuxiliary.Concept.AMBIGUOUS.getUids(), strategy);
	        retireConcept(testCon, userB, authorPaths[1], SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getUuids(), strategy);
	        Terms.get().commit();
	
	        
//	        retireConcept(testCon, userB, authorPaths[1], ArchitectonicAuxiliary.Concept.DUPLICATE.getUids(), strategy);
	        retireConcept(testCon, userB, authorPaths[1], SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getUuids(), strategy);
	        Terms.get().commit();


	        Concept c = Concept.get(testCon.getConceptNid());
			I_ConceptAttributeVersioned attrs = c.getConceptAttributes();
			
			// This may fail
			ConAttrVersionBI cav = attrs.getVersion(vc);
				
        } catch (Exception e) {
            Assert.assertEquals(true, false);
		}


        Assert.assertEquals(true, true);
    }
     
//    private void retireConcept(I_GetConceptData concept, I_GetConceptData user, PathBI editPath, Collection<UUID> collection, ContradictionManagementStrategy conflictStrategy) 

    private void retireConcept(I_GetConceptData concept, I_GetConceptData user, PathBI editPath, UUID[] uuids, ContradictionManagementStrategy conflictStrategy) 
    		throws IOException, TerminologyException {

    	
        /* Retire Rols of Concept (from ChangeRolesToStatus) */
        Set<PositionBI> positionSet = new HashSet<PositionBI>();
        positionSet.add(Terms.get().newPosition(editPath, Long.MAX_VALUE));
        PositionSetReadOnly positionsForEdit = new PositionSetReadOnly(positionSet);
        
        /* Retire Concept (from ChangeConceptStatus) */
        Set<I_ConceptAttributePart> partsToAdd = new HashSet<I_ConceptAttributePart>();

        Precedence precedence = Precedence.PATH;
        ContradictionManagementStrategy contradictionMgr = conflictStrategy;

        
        List<? extends I_ConceptAttributeTuple> tuples = concept.getConceptAttributeTuples(null, positionsForEdit, 
        		precedence, contradictionMgr);

        for (I_ConceptAttributeTuple t : tuples) {
            if (t.getStatusNid() != retireNid) {
                I_ConceptAttributePart newPart = 
                	(I_ConceptAttributePart) t.makeAnalog(retireNid, editPath.getConceptNid(), Long.MAX_VALUE);
                partsToAdd.add(newPart);
            }
        }

        for (I_ConceptAttributePart p : partsToAdd) {
            concept.getConceptAttributes().addVersion(p);
        }
        
        Terms.get().addUncommitted(concept);


    }
}
