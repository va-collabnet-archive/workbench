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
 * A representation of a workflow user.
 * Example values:
 *  - John
 *  - Mary
 *  - etc.
 * 
 * @author alo
 */
public interface WfUserBI {
    
    /** 
     * Gets the name of the workflow user
     */
    String getName();
    
    /** 
     * Gets the UUID of the workflow user
     */
    UUID getUuid();
    
    /** 
     * Gets the Roles for the workflow user in a project, empty is not a member of the project
     */
    Collection<WfPermissionBI> getPermissions(ProjectBI project);
    
}
