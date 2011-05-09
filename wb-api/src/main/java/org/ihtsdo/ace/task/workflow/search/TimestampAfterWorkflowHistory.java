package org.ihtsdo.ace.task.workflow.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
                   @Spec(directory = "search/workflow", type = BeanType.TASK_BEAN) })

public class TimestampAfterWorkflowHistory extends AbstractWorkflowHistorySearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * Property name for the Timestamp After being searched.
     */
     private String testTimestampAfter = getEarliestWorkflowReleaseStartDate();        

     protected String getEarliestWorkflowReleaseStartDate() {
     	DateFormat dfm = new SimpleDateFormat(DEFAULT_TIME_STAMP);

     	Calendar date = Calendar.getInstance();

        if (date != null) {
            date.set(Calendar.HOUR_OF_DAY, 12);	// Noon to ensure mid-day at first release date
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
             
            date.set(Calendar.DAY_OF_MONTH, WorkflowHelper.EARLIEST_WORKFLOW_HISTORY_DATE);
            date.set(Calendar.MONTH, WorkflowHelper.EARLIEST_WORKFLOW_HISTORY_MONTH);
            date.set(Calendar.YEAR, WorkflowHelper.EARLIEST_WORKFLOW_HISTORY_YEAR);
            return dfm.format(date.getTime());
        }

        return "";
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.testTimestampAfter);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion == 1) 
        {
            this.testTimestampAfter = (String) getEarliestWorkflowReleaseStartDate();
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
