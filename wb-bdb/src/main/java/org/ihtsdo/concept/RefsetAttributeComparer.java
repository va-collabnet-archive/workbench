package org.ihtsdo.concept;


import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.contradiction.ComponentType;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class RefsetAttributeComparer extends AttributeComparer {

	private int compId = 0;
	private int referencedComponentNid = 0;
	private int typeId = 0;
	private int refsetId = 0;
	private int lcaStatusNid = 0;
	private int hashCode = 0;

	private int wfHxRefsetId;

	private UUID workflowId = UUID.randomUUID();
	private UUID workflowState = UUID.randomUUID();
	
	public RefsetAttributeComparer() throws TerminologyException, IOException {
		super();
		componentType = ComponentType.REFSET;
		wfHxRefsetId = Terms.get().getConcept(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getPrimoridalUid()).getConceptNid();
	}

	@Override
	boolean hasSameAttributes(ComponentVersionBI v) {
		I_ExtendByRefVersion  refsetVersion = (I_ExtendByRefVersion )v;

		if ((refsetVersion.getComponentId() != compId) || 
			(refsetVersion.getReferencedComponentNid() != referencedComponentNid) || 
			(refsetVersion.getTypeId() != typeId) ||
			(refsetVersion.getRefsetId() != refsetId) ||
			(refsetVersion.getStatusNid() != lcaStatusNid) ||
			(refsetVersion.hashCodeOfParts() != hashCode)) {
			return false;
		}

		if (refsetId == wfHxRefsetId) {
			WorkflowHistoryJavaBean bean =  WorkflowHelper.populateWorkflowHistoryJavaBean((I_ExtendByRef) refsetVersion);

			if ((bean.getState() != workflowState) || 
				(bean.getWorkflowId() != workflowId)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void initializeAttributes(ComponentVersionBI v) {
		I_ExtendByRefVersion refsetVersion = (I_ExtendByRefVersion)v;
		
	    compId = refsetVersion.getComponentId();
		referencedComponentNid = refsetVersion.getReferencedComponentNid();
	    typeId = refsetVersion.getTypeId();
		refsetId = refsetVersion.getRefsetId();
		lcaStatusNid = refsetVersion.getStatusNid();
	    hashCode = refsetVersion.hashCodeOfParts();
		
		comparerInitialized = true;
		
		if (refsetId == wfHxRefsetId) {
			try {
				WorkflowHistoryJavaBean bean =  WorkflowHelper.populateWorkflowHistoryJavaBean((I_ExtendByRef) refsetVersion);

				workflowState = bean.getState();
				workflowId = bean.getWorkflowId();
			} catch (Exception e) {
	            AceLog.getAppLog().log(Level.WARNING, "Failure to read WfHx Java Bean from Refset Version");
			}
		}
	}
}
