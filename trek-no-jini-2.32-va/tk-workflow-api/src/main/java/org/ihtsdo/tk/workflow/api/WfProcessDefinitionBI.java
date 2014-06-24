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

import java.util.Collection;
import java.util.UUID;

/**
 * The representation of a business process in a form which 
 * supports automated manipulation, such as modelling, or enactment by a 
 * workflow management system. The process definition consists of a 
 * network of activities and their relationships, criteria to indicate 
 * the start and termination of the process, and information about the 
 * individual activities, such as participants, associated IT applications 
 * and data, etc.
 * 
 * Source: Workflow Management Coalition http://www.wfmc.org/reference-model.html
 * 
 * @author alo
 */
public interface WfProcessDefinitionBI {
    
    /** 
     * Gets the name of the process definition
     */
    String getName();
    
    /** 
     * Gets the UUID of the process definition
     * @throws Exception 
     */
    UUID getUuid() throws Exception;
    
    /** 
     * Gets the roles that participate in this process definition
     */
    Collection<WfRoleBI> getRoles();
    
    /** 
     * Gets the states that are used in this process definition
     */
    Collection<WfStateBI> getStates();
    
    /** 
     * Gets the activities that are used in this workflow definition
     */
    Collection<WfActivityBI> getActivities();
    
    /** 
     * Gets the logic that will define state transitions
     */
    Object getLogic();
    
    /** 
     * Validates if the logic covers all roles, states and actions included in
     * the definition
     */
    boolean logicIsValid();
    
    /** 
     * True is state is considered a complete state in this definition, false if not
     */
    boolean isCompleteState(WfStateBI state);
    
    /** 
     * True is state is considered a promote state in this definition, false if not
     */
    boolean isPromoteState(WfStateBI state);
    
}
