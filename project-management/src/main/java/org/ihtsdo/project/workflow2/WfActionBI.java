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

import java.util.UUID;

/**
 * A representation of a workflow Action, these actions will be executed
 * manually by the users or automatically triggered by events.
 * Executables are represented as generic objects, each implementation will
 * specialize this classes to use specific technologies.
 * Example actions:
 *  - Send to reviewer
 *  - Approve for publication
 *  - Reject translation
 *  - Escalate to Chief Terminologist
 *  - etc.
 * 
 * @author alo
 */
public interface WfActionBI {
    
    /** 
     * Gets the name of the workflow action
     */
    String getName();
    
    /** 
     * Gets the UUID of the workflow action
     */
    UUID getUuid();
    
    /** 
     * Gets an executable object for the workflow action. This can be
     * implemented as Swing actions, Workbench Business Processes, etc.
     */
    Object getExecutable();
    
    /** 
     * Performs the action
     */
    void perform();
    
    /** 
     * When true, the action should be executed automatically by the engine, 
     * when false, options will be presented to the user and he will explicitly
     * execute the action
     */
    boolean isAutomatic();
    
}
