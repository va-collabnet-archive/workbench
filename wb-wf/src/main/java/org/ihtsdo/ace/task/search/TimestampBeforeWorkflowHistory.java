package org.ihtsdo.ace.task.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
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

public class TimestampBeforeWorkflowHistory extends AbstractWorkflowHistorySearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * Property name for the TimestampBefore being searched.
     */
    private String testTimestampBefore = getCurrentTime();        

    protected String getCurrentTime() {
    	DateFormat dfm = new SimpleDateFormat(DEFAULT_TIME_STAMP);

    	Date d = new Date();
        return dfm.format(d);

    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.testTimestampBefore);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.testTimestampBefore = getCurrentTime();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    @Override
    public boolean test(WorkflowHistoryJavaBean bean, I_ConfigAceFrame frameConfig) throws TaskFailedException {
       	try {
        	DateFormat dfm = new SimpleDateFormat(DEFAULT_TIME_STAMP);
    		long testTimestampBeforeThisDate = dfm.parse(testTimestampBefore).getTime();

       		if (bean.getTimeStamp() < testTimestampBeforeThisDate)
            	return true;
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Couldn't read search Timestamp Before Than", e);
		}

		return false;
    }

    public String getTestTimestampBefore() {
        return testTimestampBefore;
    }

    public void setTestTimestampBefore(String testTimestampBefore) {
        this.testTimestampBefore = testTimestampBefore;
    }

	@Override
	public boolean test(Set<WorkflowHistoryJavaBean> wfHistory)
			throws TaskFailedException {

		try {
			DateFormat dfm = new SimpleDateFormat(DEFAULT_TIME_STAMP);
	    	long testTimestampBeforeThisDate;
		
			testTimestampBeforeThisDate = dfm.parse(testTimestampBefore).getTime();
	
	    	//If any item in the list passes the filter, return true.
	    	for (WorkflowHistoryJavaBean wfHistoryItem : wfHistory) {
	            if (wfHistoryItem.getTimeStamp() < testTimestampBeforeThisDate) {
	            	return true;
				}
			}
	
	    	return false;

		} catch (Exception e) {
			throw new TaskFailedException("Couldn't read search Timestamp!");
		}
		

	}

}
