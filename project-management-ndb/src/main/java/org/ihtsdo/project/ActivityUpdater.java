package org.ihtsdo.project;

import org.dwfa.ace.api.I_ShowActivity;
import org.ihtsdo.helper.time.TimeHelper;

public class ActivityUpdater {

	private I_ShowActivity activity;
	private String taskMessage;
	private long overallStartTime;
	private long startTime;
	private int counter;

	public ActivityUpdater(I_ShowActivity activity,  String taskMessage) {
		super();
		this.activity = activity;
		this.taskMessage = taskMessage;
	}

	public I_ShowActivity getActivity() {
		return activity;
	}

	public void setActivity(I_ShowActivity activity) {
		this.activity = activity;
	}

	public void startActivity() {
		overallStartTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
		activity.setProgressInfoLower(taskMessage);
	}

	public void startCount(Integer max) {
		startTime = System.currentTimeMillis();
		activity.setValue(0);
		activity.setMaximum(max);
		activity.setIndeterminate(false);
		counter = 0;
	}
	
	public void incrementCount() {
		counter++;
		activity.setValue(counter);
		if (counter < 100 || counter % 100 == 0 || counter > (activity.getMaximum() - 100)) {
			update();
		}
		if (counter % 100 == 0) {
//			try {
//				Thread.sleep(15);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
	}

	public void setValue(int value) {
		activity.setValue(value);
		counter = value;
		if (counter < 100 || counter % 100 == 0 || counter > (activity.getMaximum() - 100)) {
			update();
		}
	}

	public void update() {
		long endTime = System.currentTimeMillis();
		long elapsed = endTime - startTime;

		long estimation = (activity.getMaximum() * elapsed) / counter;
		
		String estimationStr = TimeHelper.getElapsedTimeString(estimation - elapsed);
		
		activity.setProgressInfoLower(taskMessage + ": " + counter + 
				" from " + activity.getMaximum() + ". Remaining time: " + estimationStr);
	}

	public void finish() {
		long endTime = System.currentTimeMillis();
		long elapsed = endTime - overallStartTime;
		String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
		activity.setProgressInfoLower(taskMessage + ". Finished: " + counter + 
				" from " + activity.getMaximum() + " . Time: " + elapsedStr);
	}

	public String getTaskMessage() {
		return taskMessage;
	}

	public void setTaskMessage(String taskMessage) {
		this.taskMessage = taskMessage;
	}


}
