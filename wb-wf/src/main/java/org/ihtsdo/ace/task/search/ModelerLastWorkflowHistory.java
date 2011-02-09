package org.ihtsdo.ace.task.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.PrimordialId;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
                   @Spec(directory = "search/workflow", type = BeanType.TASK_BEAN) })

public class ModelerLastWorkflowHistory extends AbstractWorkflowHistorySearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * Property name for the Modeler being searched.
     */
     private String testModeler = "";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.testModeler);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.testModeler = (String) in.readObject();
			if (this.testModeler == null)
			{
				I_GetConceptData leadModeler = WorkflowHelper.getLeadModeler();
					
				if (leadModeler != null)
					this.testModeler = WorkflowHelper.getLeadModeler().getInitialText();
				else
					this.testModeler = WorkflowHelper.lookupModeler(WorkflowHelper.getModelerKeySet().iterator().next()).getInitialText();
			}
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }
     
    @Override
    public boolean test(WorkflowHistoryJavaBean bean, I_ConfigAceFrame frameConfig) throws TaskFailedException {
    	return false;
    }
    

    
    @Override
	public boolean test(Set<WorkflowHistoryJavaBean> wfHistory)
			throws TaskFailedException {
		
    	 try {
             UUID modUUID =  validateModeler(testModeler);

			 if (modUUID != null && getCurrent(wfHistory).getModeler().equals(modUUID))
				return true;
			else
				return false;
	
        } catch (Exception e) {

			throw new TaskFailedException("Couldn't read search Modeler!");
		}

    }


    
	public WorkflowHistoryJavaBean getCurrent (Set<WorkflowHistoryJavaBean> wfHistory) {

		WorkflowHistoryJavaBean current = null;
		
		for (WorkflowHistoryJavaBean wfHistoryItem : wfHistory) {
			if (current == null || wfHistoryItem.getEffectiveTime() > current.getEffectiveTime()) 
				current = wfHistoryItem;
		}
		
		
		return current;
	}
	
    
    
    public String getTestModeler() {
        return testModeler;
    }

    public void setTestModeler(String testModeler) {
        this.testModeler = testModeler;
    }

    private UUID validateModeler(String mod) throws IOException, TerminologyException {
		I_GetConceptData cap = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.IHTSDO.getPrimoridalUid());

    	final long pathNid = cap.getConceptAttributes().getPathNid();
    	final long relTypeNid = Terms.get().getConcept(PrimordialId.IS_A_REL_ID.getUids()).getConceptNid();
    	final long currentNid = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptNid();
		
		for (I_RelVersioned version : cap.getDestRels()) {
			List<? extends I_RelPart> parts = version.getMutableParts();
			for (I_RelPart relAttrPart : parts) {
				if ((relAttrPart.getPathNid() == pathNid) &&
					(relAttrPart.getTypeNid() == relTypeNid) &&
					(relAttrPart.getStatusNid() == currentNid))
				{
					if (Terms.get().getConcept(version.getC1Id()).getInitialText().equalsIgnoreCase(mod))
						return Terms.get().nidToUuid(version.getC1Id());
				}
			}
		}
		
		return null;
    }

}
