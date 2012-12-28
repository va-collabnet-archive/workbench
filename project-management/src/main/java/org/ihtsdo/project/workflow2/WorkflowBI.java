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
package org.ihtsdo.project.workflow2;

import java.util.Collection;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

/**
 * A class that handle all workflow operations
 * 
 * @author alo
 */
public interface WorkflowBI {
    
    /** 
     * Gets all workflow instances for a concept
     */
    Collection<WfInstanceBI> getInstances(ConceptVersionBI concept);
    
    /** 
     * Gets all work lists in the system
     */
    Collection<WorkListBI> getWorkLists();
    
    /** 
     * Gets all instances in the system using the filters criteria
     */
    Collection<WfInstanceBI> searchWorkflow(Collection<WfFilterBI> filters);
    
    /** 
     * Gets all users in the system
     */
    Collection<WfUserBI> getUsers();
    
    /** 
     * Gets all states in the system
     */
    Collection<WfStateBI> getStates();
    
    /** 
     * Gets all actions in the system
     */
    Collection<WfActionBI> getActions();
    
    /** 
     * Gets all workflow definitions in the system
     */
    Collection<WfDefinitionBI> getDefinitions();
    
}
