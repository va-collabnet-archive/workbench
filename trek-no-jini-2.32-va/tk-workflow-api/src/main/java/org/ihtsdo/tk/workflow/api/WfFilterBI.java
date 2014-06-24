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
 * A filter used in workflow searches, a collection of filters is applied during
 * the search and candidate intances are rejected or approved to be included in
 * the results of the search
 * 
 * @author alo
 */
public interface WfFilterBI {
    
    /** 
     * Evaluates the instance and is rejected or not, depending on filter
     * values. It can be implemented to filter by state, role, destination, 
     * etc.
     */
    boolean evaluateInstance(WfProcessInstanceBI instance);
    
    @Deprecated
    public String getType();
}
