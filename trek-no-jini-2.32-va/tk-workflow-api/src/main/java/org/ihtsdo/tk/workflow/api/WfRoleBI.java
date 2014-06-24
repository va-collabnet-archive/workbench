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
 * The role defines the context in which the user 
 * participates in a particular process or activity. 
 * The role often embraces organisational concepts such as 
 * structure and relationships, responsibility or authority, 
 * but may also refer to other attributes such as skill, 
 * location, value data, time or date, etc.. 
 * 
 * Source: Workflow Management Coalition http://www.wfmc.org/reference-model.html
 * 
 * Activities will be available depending on the role.
 * Example values:
 *  - Editor Category A
 *  - Reviewer
 *  - Chief Terminologist
 *  - etc.
 * 
 * @author alo
 */
public interface WfRoleBI {
    
    /** 
     * Gets the name of the workflow role
     */
    String getName();
    
    /** 
     * Gets the UUID of the workflow role
     */
    UUID getUuid();
    
}
