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
 * A representation of a workflow permission.
 * Example values:
 *  - John is reviewer for clinical findings in the Danish Translation project
 *  - Mary is editoral board for substances in the Swedish extension project
 *  - etc.
 * 
 * @author alo
 */
public interface WfPermissionBI {
    
    /** 
     * Gets the workflow user
     */
    WfUserBI getUser();
    
    /** 
     * Gets the role
     */
    WfRoleBI getRole();
    
    /** 
     * Gets the UUID for the concept that is the parent of the hierarchy 
     * where this permission applies
     */
    UUID getHierarchyParent();
    
    /** 
     * Gets the project where this permission applies
     */
    UUID getProject();
    
}
