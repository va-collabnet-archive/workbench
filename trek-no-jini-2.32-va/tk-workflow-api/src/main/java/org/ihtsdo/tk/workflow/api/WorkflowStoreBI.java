/*
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.workflow.api;

import java.util.Collection;
import java.util.UUID;

/**
 * A class that performs all workflow operations.
 *
 * @author alo
 */
public interface WorkflowStoreBI {
    
    /**
     * Gets all workflow process instances for a concept.
     *
     * @param componentUuid the component uuid
     * @return the {@link Collection}
     * @throws Exception the exception
     */
    Collection<WfProcessInstanceBI> getProcessInstances(UUID componentUuid) throws Exception;
    
    /**
     * Gets the workflow process instance for a concept in the provided workList.
     *
     * @param workList the work list
     * @param componentUuid the component uuid
     * @return the {@link WfProcessInstanceBI}
     * @throws Exception the exception
     */
    WfProcessInstanceBI getProcessInstance(WorkListBI workList, UUID componentUuid) throws Exception;
    
    /**
     * Gets active workflow process instances for a concept.
     *
     * @param componentUuid the component uuid
     * @return the {@link Collection}
     * @throws Exception the exception
     */
    Collection<WfProcessInstanceBI> getActiveProcessInstances(UUID componentUuid) throws Exception;
    
    /**
     * Gets active and not completed workflow process instances for a concept.
     *
     * @param componentUuid the component uuid
     * @return the {@link Collection}
     * @throws Exception the exception
     */
    Collection<WfProcessInstanceBI> getIncompleteProcessInstances(UUID componentUuid) throws Exception;
    
    /**
     * Gets all instances in the system using the filters criteria.
     *
     * @param filters the filters
     * @return the collection
     * @throws Exception the exception
     */
    Collection<WfProcessInstanceBI> searchWorkflow(Collection<WfFilterBI> filters) throws Exception;
    
    /**
     * Gets all users in the system.
     *
     * @return the {@link Collection}
     */
    Collection<WfUserBI> getAllUsers();
    
    /**
     * Gets all states in the system.
     *
     * @return the {@link Collection}
     */
    Collection<WfStateBI> getAllStates();
    
    /**
     * Gets all activities in the system.
     *
     * @return the {@link Collection}
     */
    Collection<WfActivityBI> getAllActivities();
    
    /**
     * Gets all roles in the system.
     *
     * @return the {@link Collection}
     */
    Collection<WfRoleBI> getAllRoles();
    
    /**
     * Gets all workflow process definitions in the system.
     *
     * @return the {@link Collection}
     */
    Collection<WfProcessDefinitionBI> getAllProcessDefinitions();
    
    /**
     * Gets all projects in the system.
     *
     * @return the {@link Collection}
     * @throws Exception the exception
     */
    Collection<ProjectBI> getAllProjects() throws Exception;
    
    /**
     * Creates the project.
     *
     * @param name the name
     * @param type the type
     * @return the project bi
     * @throws Exception the exception
     */
    ProjectBI createProject(String name, ProjectBI.ProjectType type) throws Exception;
    
    /**
     * Gets the {@link Collection} representing the activities.
     *
     * @param instance the instance
     * @param user the user
     * @return the {@link Collection}
     * @throws Exception the exception
     */
    Collection<WfActivityBI> getActivities(WfProcessInstanceBI instance, WfUserBI user) throws Exception;
    
    /**
     * Gets the {@link WorkListBI} representing the worklist.
     *
     * @param worklistUuid the worklist uuid
     * @return the {@link WorkListBI}
     * @throws Exception the exception
     */
    WorkListBI getWorklist(UUID worklistUuid) throws Exception;
    
    /**
     * Gets the {@link ProjectBI} representing the project.
     *
     * @param projectUuid the project uuid
     * @return the {@link ProjectBI}
     * @throws Exception the exception
     */
    ProjectBI getProject(UUID projectUuid) throws Exception;

}
