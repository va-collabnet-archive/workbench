package org.ihtsdo.ace.task.workflow.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedSet;

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

        if (objDataVersion == 1) 
        {
            this.testTimestampBefore = (String) getCurrentTime();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public String getTimestampAsString() {
        return testTimestampBefore;
		}

    public void setTestTimestampBefore(String timestamp) {
        testTimestampBefore = timestamp;
    }

    public String getTestTimestampBefore() {
		return getTimestampAsString();
    }

	public long getTimestampAsLong() {
     	DateFormat dfm = new SimpleDateFormat(DEFAULT_TIME_STAMP);

     	try {
			return dfm.parse(testTimestampBefore).getTime();
		} catch (ParseException e) {
			return -1;
		}
    }

	@Override
	public boolean test(SortedSet<WorkflowHistoryJavaBean> wfHistory)
			throws TaskFailedException 
	{
		try {
			DateFormat dfm = new SimpleDateFormat(DEFAULT_TIME_STAMP);
	    	long testTimestampBeforeThisDate;
		
			testTimestampBeforeThisDate = dfm.parse(testTimestampBefore).getTime();
	
	    	//If any item in the list passes the filter, return true.
	    	for (WorkflowHistoryJavaBean wfHistoryItem : wfHistory) 
	    	{
	            if (wfHistoryItem.getWorkflowTime().longValue() < testTimestampBeforeThisDate) 
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
		return timestampBefore;
	}
	
	@Override
	public Object getTestValue() {
		return getTimestampAsString();
	}

}
