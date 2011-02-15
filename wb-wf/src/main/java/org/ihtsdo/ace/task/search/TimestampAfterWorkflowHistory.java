package org.ihtsdo.ace.task.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
                   @Spec(directory = "search/workflow", type = BeanType.TASK_BEAN) })

public class TimestampAfterWorkflowHistory extends AbstractWorkflowHistorySearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * Property name for the Timestamp After being searched.
     */
     private String testTimestampAfter = getCurrentTime();        

     protected String getCurrentTime() {
     	DateFormat dfm = new SimpleDateFormat(DEFAULT_TIME_STAMP);

     	Date d = new Date();
         return dfm.format(d);

     }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.testTimestampAfter);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.testTimestampAfter = (String) getCurrentTime();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    @Override
    public boolean test(WorkflowHistoryJavaBean bean, I_ConfigAceFrame frameConfig)  
    {
    	return false;
    }

    public String getTestTimestampAfter() {
        return testTimestampAfter;
    }

    public void setTestTimestampAfter(String testTimestamp) {
        this.testTimestampAfter = testTimestamp;
    }

	@Override
	public boolean test(Set<WorkflowHistoryJavaBean> wfHistory)
			throws TaskFailedException 
	{
		try {
			DateFormat dfm = new SimpleDateFormat(DEFAULT_TIME_STAMP);
	    	long testTimestampAfterThisDate = dfm.parse(testTimestampAfter).getTime();
	
	    	//If any item in the list passes the filter, return true.
	    	for (WorkflowHistoryJavaBean wfHistoryItem : wfHistory) 
	    	{
	            if (wfHistoryItem.getWorkflowTime().longValue() < testTimestampAfterThisDate) 
	            {
	            	return true;
				}
			}
		} catch (Exception e) {
			throw new TaskFailedException("Couldn't read search Timestamp!");
		}
		
		return false;
	}
	
	@Override
	public int getTestType() {
		return timestampAfter;
	}
	
	@Override
	public Object getTestValue() {
		return getTestTimestampAfter();
	}
}
