/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.project;

import org.dwfa.ace.api.I_ShowActivity;
import org.ihtsdo.helper.time.TimeHelper;

/**
 * The Class ActivityUpdater.
 */
public class ActivityUpdater {

    /**
     * The activity.
     */
    private I_ShowActivity activity;
    /**
     * The task message.
     */
    private String taskMessage;
    /**
     * The overall start time.
     */
    private long overallStartTime;
    /**
     * The start time.
     */
    private long startTime;
    /**
     * The counter.
     */
    private int counter;

    /**
     * Instantiates a new activity updater.
     *
     * @param activity the activity
     * @param taskMessage the task message
     */
    public ActivityUpdater(I_ShowActivity activity, String taskMessage) {
        super();
        this.activity = activity;
        this.taskMessage = taskMessage;
    }

    /**
     * Gets the activity.
     *
     * @return the activity
     */
    public I_ShowActivity getActivity() {
        return activity;
    }

    /**
     * Sets the activity.
     *
     * @param activity the new activity
     */
    public void setActivity(I_ShowActivity activity) {
        this.activity = activity;
    }

    /**
     * Start activity.
     */
    public void startActivity() {
        overallStartTime = System.currentTimeMillis();
        startTime = System.currentTimeMillis();
        activity.setProgressInfoLower(taskMessage);
    }

    /**
     * Start count.
     *
     * @param max the max
     */
    public void startCount(Integer max) {
        startTime = System.currentTimeMillis();
        activity.setValue(0);
        activity.setMaximum(max);
        activity.setIndeterminate(false);
        counter = 0;
    }

    /**
     * Increment count.
     */
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
//				AceLog.getAppLog().alertAndLogException(e);
//			}
        }
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(int value) {
        activity.setValue(value);
        counter = value;
        if (counter < 100 || counter % 100 == 0 || counter > (activity.getMaximum() - 100)) {
            update();
        }
    }

    /**
     * Update.
     */
    public void update() {
        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;

        long estimation = (activity.getMaximum() * elapsed) / counter;

        String estimationStr = TimeHelper.getElapsedTimeString(estimation - elapsed);

        activity.setProgressInfoLower(taskMessage + ": " + counter
                + " from " + activity.getMaximum() + ". Remaining time: " + estimationStr);
    }

    /**
     * Finish.
     */
    public void finish() {
        long endTime = System.currentTimeMillis();
        long elapsed = endTime - overallStartTime;
        String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
        activity.setProgressInfoLower(taskMessage + ". Finished: " + activity.getMaximum()
                + " from " + activity.getMaximum() + " . Time: " + elapsedStr);
    }

    /**
     * Gets the task message.
     *
     * @return the task message
     */
    public String getTaskMessage() {
        return taskMessage;
    }

    /**
     * Sets the task message.
     *
     * @param taskMessage the new task message
     */
    public void setTaskMessage(String taskMessage) {
        this.taskMessage = taskMessage;
    }
}
