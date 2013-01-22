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
public interface WorkListBI {
    
    /** 
     * Gets the name of the work list
     */
    String getName();
    
    /** 
     * Gets the UUID of the work list
     */
    UUID getUuid();
    
    /** 
     * Gets the UUID of the work list
     */
    Collection<WfProcessInstanceBI> getInstances() throws Exception;
    
    /** 
     * Gets a description of the worklist, goals, instructions on how to edit, etc.
     */
    String getDescription();
    
    WfProcessInstanceBI createInstanceForComponent(UUID componentUuid, WfProcessDefinitionBI definition) throws Exception;
    
}
