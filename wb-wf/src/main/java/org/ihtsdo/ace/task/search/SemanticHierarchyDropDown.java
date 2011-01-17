package org.ihtsdo.ace.task.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.semArea.SemanticAreaSearchRefset;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
                   @Spec(directory = "search/workflow", type = BeanType.TASK_BEAN) })

public class SemanticHierarchyDropDown extends AbstractWorkflowHistorySearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private static SemanticAreaSearchRefset refset = null;

    private static I_ConfigAceFrame frameConfig = null;
    
    /**
     * Property name for the Hierarchy being searched.
     */
     private String testHierarchy = null;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.testHierarchy);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.testHierarchy = (String) in.readObject();
            try 
            {
	            if (this.testHierarchy == null || this.testHierarchy.length() == 0)
	            {
	            	throw new Exception("Must fix this bug in setting up Hierarchy DropDown--Using Default Observable Entity");
	            }
            } catch (Exception e) {
            	//e.printStackTrace();
				System.out.println(e.getMessage());
            	testHierarchy = "observable_entity";
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    @Override
    public boolean test(WorkflowHistoryJavaBean bean, I_ConfigAceFrame frameConfig) throws TaskFailedException {
    	return false;
    	
    }

    public String getTestHierarchy() {
        return testHierarchy;
    }

    public void setTestHierarchy(String hierarchy) {
        this.testHierarchy = hierarchy;
    }

	private I_GetConceptData translateStringToHierarchy(String testStr) throws Exception {
		for (I_ExtendByRef extension : Terms.get().getRefsetExtensionMembers(refset.getRefsetId())) 
		{
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)extension;
		
			if (props.getStringValue().equalsIgnoreCase(testHierarchy))
				return Terms.get().getConcept(extension.getComponentId());
        }

		throw new Exception("String not found");
	}
	
    public I_ConfigAceFrame getFrameConfig() {
    	if (frameConfig == null) {
    		try {
				frameConfig = Terms.get().getActiveAceFrameConfig();
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
    	}
        return frameConfig;
    }

	@Override
	public boolean test(Set<WorkflowHistoryJavaBean> wfHistory)
			throws TaskFailedException {

		WorkflowHistoryJavaBean bean = wfHistory.iterator().next();
		
    	try {
            if (refset == null)
        		refset = new SemanticAreaSearchRefset();
            if (testHierarchy == null || testHierarchy.length() == 0)
            {
            	System.out.println("testHierarchy is null and bean is: " + bean.getFSN());
            	return false;
            }
            I_GetConceptData parentHierarchy = translateStringToHierarchy(testHierarchy);    		
            I_GetConceptData testConcept = Terms.get().getConcept(bean.getConceptId());
            
    		if (parentHierarchy.isParentOfOrEqualTo(testConcept, getFrameConfig().getAllowedStatus(), getFrameConfig().getDestRelTypes(), getFrameConfig().getViewPositionSetReadOnly(), getFrameConfig().getPrecedence(), getFrameConfig().getConflictResolutionStrategy())) 
    			return true;
    		else
    			return false;

    	} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new TaskFailedException("Couldn't read search Hierarchy!");
		}

	}

}