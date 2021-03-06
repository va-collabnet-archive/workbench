package org.ihtsdo.ace.task.workflow.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.SortedSet;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
                   @Spec(directory = "search/workflow", type = BeanType.TASK_BEAN) })

public class PathWorkflowHistory extends AbstractWorkflowHistorySearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * Property name for the Path being searched.
     */
     private TermEntry testPath = new TermEntry(ArchitectonicAuxiliary.Concept.IHTSDO.getUids());

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.testPath);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) 
        {
            Object obj = in.readObject();
           
			if (obj instanceof TermEntry) {
            	this.testPath = (TermEntry) obj;
            } else {
            	this.testPath = null;
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public TermEntry getTestPath() {
        return testPath;
    }

    public UUID getCurrentTestUUID() throws TaskFailedException {
        return testPath.getIds()[0];
    }

    public void setTestPath(TermEntry testPath) {
        this.testPath = testPath;
    }

	@Override
	public boolean test(SortedSet<WorkflowHistoryJavaBean> wfHistory)
			throws TaskFailedException {

		try {
			
			I_GetConceptData path = AceTaskUtil.getConceptFromObject(testPath);
			UUID testUid = path.getUids().get(0);
	  	
	    	//If any item in the list passes the filter, return true.
	    	for (WorkflowHistoryJavaBean wfHistoryItem : wfHistory) {
				if (testUid.equals(wfHistoryItem.getPath())) {
					return true;
				}
			}
	    	
	    	return false;
	    
		} catch (Exception e) {
			throw new TaskFailedException("Couldn't read search Path!");
		}
	}

   @Override
	public int getTestType() {
	   return path;
	}

	@Override
	public Object getTestValue() {
		return getTestPath();
	}

}
