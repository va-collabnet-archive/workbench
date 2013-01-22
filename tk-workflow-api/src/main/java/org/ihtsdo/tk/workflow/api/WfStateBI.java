/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.workflow.api;

import java.util.UUID;

/**
 * A representation of the internal conditions defining the 
 * status of a process instance at a particular point in time. 
 * Most workflow management systems maintain such status 
 * information as part of their workflow control data.
 * 
 * The state of each process instance under enactment is maintained 
 * by the workflow management system. Different vendor systems have 
 * different ways of representing process state and may have their 
 * own set of state definitions
 * 
 * As the execution of a process instance proceeds it follows a 
 * series of transitions between the various states which it may take. 
 * The complete set of process states for a process definition fully 
 * defines the internal behavior which its process instances may 
 * follow.
 * 
 * Source: Workflow Management Coalition http://www.wfmc.org/reference-model.html
 * 
 * Example values:
 *  - Assigned
 *  - Reviewed
 *  - Approved
 *  - etc.
 * 
 * @author alo
 */
public interface WfStateBI {
    
    /** 
     * Gets the name of the workflow state
     */
    String getName();
    
    /** 
     * Gets the UUID of the workflow state
     */
    UUID getUuid();
    
}
