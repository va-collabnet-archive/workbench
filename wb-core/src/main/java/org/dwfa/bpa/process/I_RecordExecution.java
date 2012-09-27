/**
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

package org.dwfa.bpa.process;

import java.io.Serializable;
import java.util.Date;

/**
 * Documentation of the completion of a task.
 * 
 * @author kec
 * 
 */
public interface I_RecordExecution extends Serializable, Comparable<I_RecordExecution> {

    /**
     * @return The date this task completed.
     */
    public Date getDate();

    /**
     * @return The identifier of the task associated with this execution record.
     */
    public int getTaskId();

    /**
     * @return The identifier of the worker that completed the associated task.
     */
    public String getWorkerId();

    /**
     * @return A description of the worker that completed this task.
     */
    public String getWorkerDesc();

    /**
     * @return The completion condition of the task.
     */
    public Condition getCondition();

}
