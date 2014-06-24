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
 * A description of a piece of work that forms one logical step within a process. 
 * An activity may be a manual activity, which does not support computer automation, 
 * or a workflow (automated) activity. A workflow activity requires human and/or 
 * machine resources(s) to support process execution; where human resource is required 
 * an activity may be allocated to a workflow participant.
 * 
 * Source: Workflow Management Coalition http://www.wfmc.org/reference-model.html
 * 
 * Example activities:
 *  - Send to reviewer
 *  - Approve for publication
 *  - Reject translation
 *  - Escalate to Chief Terminologist
 *  - etc.
 * 
 * @author alo
 */
public interface WfActivityBI {
    
    /** 
     * Gets the name of the workflow activity
     */
    String getName();
    
    /** 
     * Gets the UUID of the workflow activity
     */
    UUID getUuid();
    
    /** 
     * Gets an executable object for the workflow activity. This can be
     * implemented as Swing actions, Workbench Business Processes, etc.
     */
    Object getExecutable();
    
    /** 
     * Performs the activity
     */
    void perform(WfProcessInstanceBI instance) throws Exception;
    
    /** 
     * When true, the activity should be executed automatically by the engine, 
     * when false, options will be presented to the user and he will explicitly
     * execute the action
     */
    boolean isAutomatic();
    
}
