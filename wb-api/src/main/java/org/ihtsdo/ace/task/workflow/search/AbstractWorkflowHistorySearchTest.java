/*
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.ihtsdo.ace.task.workflow.search;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedSet;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.search.AbstractSearchTest;
import org.dwfa.bpa.process.TaskFailedException;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;

public abstract class AbstractWorkflowHistorySearchTest extends AbstractSearchTest 
{
    public static final String DEFAULT_TIME_STAMP = "MM/dd/yyyy";

    public static final int currentAction = 0;							// 0
    public static final int hasAction = currentAction + 1;				// 1
    public static final int currentModeler = hasAction + 1;			// 2
    public static final int hasModeler = currentModeler + 1;			// 3
    public static final int path = hasModeler + 1;						// 4
    public static final int semTag = path + 1;						// 5
    public static final int currentState = semTag + 1;				// 6
    public static final int hasState = currentState + 1;				// 7
    public static final int timestampBefore = hasState + 1;			// 8
    public static final int timestampAfter = timestampBefore + 1;		// 9

    private static final long serialVersionUID = 1;


    public abstract int getTestType();
    
    public abstract Object getTestValue();

    public abstract boolean test(SortedSet<WorkflowHistoryJavaBean> wfHistory) throws TaskFailedException;

    public final boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
    	throw new TaskFailedException("Test Type not used for Wf Hx Search Tests");
    }
    
    protected static String getStaticCurrentTime() {
    	DateFormat dfm = new SimpleDateFormat(DEFAULT_TIME_STAMP);

    	Date d = new Date();
        return dfm.format(d);
    }
}
