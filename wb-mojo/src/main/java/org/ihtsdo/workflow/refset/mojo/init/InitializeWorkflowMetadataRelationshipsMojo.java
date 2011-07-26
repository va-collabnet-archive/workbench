package org.ihtsdo.workflow.refset.mojo.init;

import java.util.UUID;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * @author Jesse Efron
 *
 * @goal initialize-workflow-metadata-relationships
 * @requiresDependencyResolution compile
 */

/*
1)  Add to      O: Accept Workflow Action:                              A: Workflow action value                        V: Workflow end action
2)  Add to      O: Commit in Batch Workflow Action:                     A: Workflow action value                        V: Workflow begin action
3)  Add to      O: Commit in Batch Workflow Action:                     A: Workflow commit value                        V: Workflow batch commit
4)  Add to      O: Commit Workflow Action:                              A: Workflow action value                        V: Workflow begin action
5)  Add to      O: Commit Workflow Action:                              A: Workflow commit value                        V: Workflow single commit

6)  Add to      O: Initialize concept workflow state               		A: Workflow use case                            V: Workflow existing concept 
7)  Add to      O: Create conceptworkflow state       					A: Workflow use case                            V: Workflow new concept 

8)  Add to      O: Clinical editor role C                               A: Workflow role value                          V: Workflow automatic approval
9)  Add to      O: Clinical editor role D                               A: Workflow role value                          V: Workflow automatic approval


10) Add to 		O: Accept                                               A: Workflow action value                        V:Workflow user action
11) Add to 		O: Chief Terminologist review                           A: Workflow action value                        V:Workflow user action
12) Add to 		O: Discuss                                              A: Workflow action value                        V:Workflow user action
13) Add to 		O: Escalate                                             A: Workflow action value                        V:Workflow user action
14) Add to 		O: Review                                               A: Workflow action value                        V:Workflow user action

15) Add to      O: Generic User                                         A: Workflow editor status                       V: Workflow default editor                                                                
16) Add to      O: Monique Van Berkum:                                  A: Workflow editor status                       V: Workflow lead editor
*/

public class InitializeWorkflowMetadataRelationshipsMojo extends AbstractMojo {

    private I_ConfigAceFrame config;
    
   @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        System.setProperty("java.awt.headless", "true");

        try {
			config = Terms.get().getActiveAceFrameConfig();

			createWfRelationship(1, ArchitectonicAuxiliary.Concept.WORKFLOW_ACCEPT_ACTION, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE, ArchitectonicAuxiliary.Concept.WORKFLOW_END_WF_CONCEPT);
			createWfRelationship(2, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_IN_BATCH_ACTION, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE, ArchitectonicAuxiliary.Concept.WORKFLOW_BEGIN_WF_CONCEPT);
			createWfRelationship(3, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_IN_BATCH_ACTION, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_VALUE, ArchitectonicAuxiliary.Concept.WORKFLOW_BATCH_COMMIT);
			createWfRelationship(4, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_ACTION, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE, ArchitectonicAuxiliary.Concept.WORKFLOW_BEGIN_WF_CONCEPT);
			createWfRelationship(5, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_ACTION, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_VALUE, ArchitectonicAuxiliary.Concept.WORKFLOW_SINGLE_COMMIT);
			Terms.get().addUncommitted(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACCEPT_ACTION.getPrimoridalUid()));
			Terms.get().addUncommitted(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_IN_BATCH_ACTION.getPrimoridalUid()));
			Terms.get().addUncommitted(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_ACTION.getPrimoridalUid()));

			createWfRelationship(6, ArchitectonicAuxiliary.Concept.WORKFLOW_INITIAL_HISTORY_STATE, ArchitectonicAuxiliary.Concept.WORKFLOW_USE_CASE, ArchitectonicAuxiliary.Concept.WORKFLOW_EXISTING_CONCEPT);
			createWfRelationship(7, ArchitectonicAuxiliary.Concept.WORKFLOW_CONCEPT_CREATION_STATE, ArchitectonicAuxiliary.Concept.WORKFLOW_USE_CASE, ArchitectonicAuxiliary.Concept.WORKFLOW_NEW_CONCEPT);
			createWfRelationship(8, ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_C, ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_VALUE, ArchitectonicAuxiliary.Concept.WORKFLOW_AUTOMOTAIC_APPROVAL);
			createWfRelationship(9, ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_D, ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_VALUE, ArchitectonicAuxiliary.Concept.WORKFLOW_AUTOMOTAIC_APPROVAL);
			Terms.get().addUncommitted(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_INITIAL_HISTORY_STATE.getPrimoridalUid()));
			Terms.get().addUncommitted(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_CONCEPT_CREATION_STATE.getPrimoridalUid()));
			Terms.get().addUncommitted(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_C.getPrimoridalUid()));
			Terms.get().addUncommitted(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_D.getPrimoridalUid()));
			
			createWfRelationship(10, ArchitectonicAuxiliary.Concept.WORKFLOW_ACCEPT_ACTION, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE, ArchitectonicAuxiliary.Concept.WORKFLOW_USER_ACTION);
			createWfRelationship(11, ArchitectonicAuxiliary.Concept.WORKFLOW_CHIEF_TERMINOLOGIST_REVIEW_ACTION, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE, ArchitectonicAuxiliary.Concept.WORKFLOW_USER_ACTION);
			createWfRelationship(12, ArchitectonicAuxiliary.Concept.WORKFLOW_DISCUSS_ACTION, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE, ArchitectonicAuxiliary.Concept.WORKFLOW_USER_ACTION);
			createWfRelationship(13, ArchitectonicAuxiliary.Concept.WORKFLOW_ESCALATE_ACTION, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE, ArchitectonicAuxiliary.Concept.WORKFLOW_USER_ACTION);
			createWfRelationship(14, ArchitectonicAuxiliary.Concept.WORKFLOW_REVIEW_ACTION, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE, ArchitectonicAuxiliary.Concept.WORKFLOW_USER_ACTION);
			Terms.get().addUncommitted(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACCEPT_ACTION.getPrimoridalUid()));
			Terms.get().addUncommitted(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_CHIEF_TERMINOLOGIST_REVIEW_ACTION.getPrimoridalUid()));
			Terms.get().addUncommitted(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_DISCUSS_ACTION.getPrimoridalUid()));
			Terms.get().addUncommitted(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ESCALATE_ACTION.getPrimoridalUid()));
			Terms.get().addUncommitted(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_REVIEW_ACTION.getPrimoridalUid()));
			
			createWfRelationship(15, ArchitectonicAuxiliary.Concept.GENERIC_USER, ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS, ArchitectonicAuxiliary.Concept.WORKFLOW_DEFAULT_MODELER);
			createWfRelationship(16, ArchitectonicAuxiliary.Concept.MONIQUE_VAN_BERKUM, ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS, ArchitectonicAuxiliary.Concept.WORKFLOW_LEAD_MODELER);
			Terms.get().addUncommitted(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.GENERIC_USER.getPrimoridalUid()));
			Terms.get().addUncommitted(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.MONIQUE_VAN_BERKUM.getPrimoridalUid()));
			
        } catch (Exception e) {
			   AceLog.getAppLog().log(Level.WARNING, "Failed to initialize ConfigAceFrame");
		}
    }
   
   private void createWfRelationship(int id, Concept workflowAcceptAction, Concept workflowActionValue, Concept workflowEndWfConcept) {
	   
	   try {
		   Terms.get().newRelationship(UUID.randomUUID(), 
				   Terms.get().getConcept(workflowAcceptAction.getPrimoridalUid()), 
				   Terms.get().getConcept(workflowActionValue.getPrimoridalUid()), 
				   Terms.get().getConcept(workflowEndWfConcept.getPrimoridalUid()), 
	               Terms.get().getConcept(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids()), 
	               Terms.get().getConcept(SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids()), 
	               Terms.get().getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()), 0, 
	               config);
	   } catch (Exception e) {
		   AceLog.getAppLog().log(Level.WARNING, "Failed to add wf relationship id#: " + id);
	   }
   }
}
