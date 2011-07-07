package org.ihtsdo.ace.task.workflow.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.SortedSet;
import java.util.logging.Level;

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
import org.ihtsdo.workflow.refset.semTag.SemanticTagsRefsetReader;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
                   @Spec(directory = "search/workflow", type = BeanType.TASK_BEAN) })

public class SemanticTagWorkflowHistory extends AbstractWorkflowHistorySearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private static SemanticTagsRefsetReader reader = null;

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
        if (objDataVersion == 1) 
        {
            Object obj = in.readObject();
            if (obj instanceof String) {
            	this.testHierarchy = (String) obj;
            } else {
            	this.testHierarchy = null;
            }
            
            if (this.testHierarchy == null || this.testHierarchy.length() == 0)
            {
	            testHierarchy = "procedure";
            }
        }
    }

    public String getTestHierarchy() {
        return testHierarchy;
    }

    public void setTestHierarchy(String hierarchy) {
        this.testHierarchy = hierarchy;
    }

	private I_GetConceptData translateStringToHierarchy(String testStr) throws Exception {
        if (reader == null) {
    		reader = new SemanticTagsRefsetReader();
    	}
        
        Collection<? extends I_ExtendByRef> exts = Terms.get().getRefsetExtensionMembers(reader.getRefsetNid());
        
        for (I_ExtendByRef extension : exts) {
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)extension;
		
			if (props.getStringValue().equalsIgnoreCase(testHierarchy)) {
				return Terms.get().getConcept(extension.getComponentId());
			}
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
	public boolean test(SortedSet<WorkflowHistoryJavaBean> wfHistory)
			throws TaskFailedException {

		boolean retVal = false;
		WorkflowHistoryJavaBean bean = wfHistory.iterator().next();
		
    	try {
            if (!(testHierarchy == null || testHierarchy.length() == 0)) {
	            if (frameConfig == null) {
	    			frameConfig = Terms.get().getActiveAceFrameConfig();
	            }
	            
	            I_GetConceptData parentHierarchy = translateStringToHierarchy(testHierarchy);    		
	            I_GetConceptData testConcept = Terms.get().getConcept(bean.getConcept());
	            
	    		if (parentHierarchy.isParentOfOrEqualTo(testConcept, frameConfig.getAllowedStatus(), frameConfig.getDestRelTypes(), 
	    												frameConfig.getViewPositionSetReadOnly(), frameConfig.getPrecedence(), 
	    												frameConfig.getConflictResolutionStrategy())) { 
	    			retVal = true; 
	    		} 
            }
    	} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to get active frame config");
		}
    	
    	return retVal;
	}

    @Override
	public int getTestType() {
		return semTag;
	}

	@Override
	public Object getTestValue() {
		return getTestHierarchy();
	}
}