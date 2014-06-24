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

/**
 * A representation of a historical state of a workflow
 * instance. a new historical entry is created when the 
 * workflow instance is created, and in
 * each subsequent action that progresses the workflow.
 * 
 * @author alo
 */
public interface WfActivityInstanceBI {
    
    /** 
     * Gets the timestamp of the activity instance
     */
    Long getTime();
    
    /** 
     * Gets the state of this instance at the historic time
     */
    WfStateBI getState();
    
    /** 
     * Gets the user that executed the action that created the activity instance
     */
    WfUserBI getAuthor();
    
    /** 
     * True when "override" was used to perform the action, false when
     * it was not used.
     */
    boolean usedOverride();
    
    /** 
     * True when the action was automatic, false when it was triggered by
     * a user
     */
    boolean automaticAction();
    
}
