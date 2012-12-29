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
import java.util.UUID;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 * A representation of a group of workflow instances. Instances are grouped
 * in work lists in order to provide a way to monitor progress, ("this work
 * list is 60% done", "this work list is complete", etc.). In some cases,
 * special tasks can be performed over whole work lists, like promotions, bulk
 * workflow status changes, etc.
 * Work lists can also be used to define what edit and view coordinates are used
 * in this workflow.
 * 
 * @author alo
 */
public interface ProjectBI {
    
    /** 
     * Gets the name of the work list
     */
    String getName();
    
    /** 
     * Gets the UUID of the work list
     */
    UUID getUuid();
    
    /** 
     * Gets the ViewCoordinate of this project, this allows the retrieval of
     * of the current concept version for this instance
     */
    ViewCoordinate getViewCoordinate();
    
    /** 
     * Gets the EditCoordinate of this project, actions should use this edit
     * coordinate to perform changes in the data
     */
    EditCoordinate getEditCoordinate();
    
    /** 
     * Gets all work lists in the project
     */
    Collection<WorkListBI> getWorkLists();
    
    /** 
     * Gets all users assigned to this project
     */
    Collection<WfUserBI> getUsers();
    
    /** 
     * Gets roles for the user (in this project)
     */
    Collection<WfRoleBI> getRolesForUser(WfUserBI user);
    
    /** 
     * Gets a description of the project, goals, instructions on how to edit, etc.
     */
    String getDescription();
    
}
