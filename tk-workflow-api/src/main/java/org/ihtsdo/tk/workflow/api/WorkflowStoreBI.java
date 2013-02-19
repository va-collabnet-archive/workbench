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
 * A class that handles all workflow operations
 * 
 * @author alo
 */
public interface WorkflowStoreBI {
    
    /** 
     * Gets all workflow process instances for a concept
     * @throws Exception 
     */
    Collection<WfProcessInstanceBI> getProcessInstances(UUID componentUuid) throws Exception;
    
    /** 
     * Gets the workflow process instance for a concept in the provided workList
     * @throws Exception 
     */
    WfProcessInstanceBI getProcessInstance(WorkListBI workList, UUID componentUuid) throws Exception;
    
    /** 
     * Gets active workflow process instances for a concept
     * @throws Exception 
     */
    Collection<WfProcessInstanceBI> getActiveProcessInstances(UUID componentUuid) throws Exception;
    
    /** 
     * Gets active and not completed workflow process instances for a concept
     * @throws Exception 
     */
    Collection<WfProcessInstanceBI> getIncompleteProcessInstances(UUID componentUuid) throws Exception;
    
    /** 
     * Gets all instances in the system using the filters criteria
     */
    Collection<WfProcessInstanceBI> searchWorkflow(Collection<WfFilterBI> filters) throws Exception;
    
    /** 
     * Gets all users in the system
     */
    Collection<WfUserBI> getAllUsers();
    
    /** 
     * Gets all states in the system
     */
    Collection<WfStateBI> getAllStates();
    
    /** 
     * Gets all activities in the system
     */
    Collection<WfActivityBI> getAllActivities();
    
    /** 
     * Gets all roles in the system
     */
    Collection<WfRoleBI> getAllRoles();
    
    /** 
     * Gets all workflow process definitions in the system
     */
    Collection<WfProcessDefinitionBI> getAllProcessDefinitions();
    
    /** 
     * Gets all projects in the system
     * @throws Exception 
     */
    Collection<ProjectBI> getAllProjects() throws Exception;
    
    ProjectBI createProject(String name, ProjectBI.ProjectType type) throws Exception;
    
    Collection<WfActivityBI> getActivities(WfProcessInstanceBI instance, WfUserBI user) throws Exception;
    
    WorkListBI getWorklist(UUID worklistUuid) throws Exception;
    
    ProjectBI getProject(UUID projectUuid) throws Exception;

}
