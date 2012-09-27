/**
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
package org.ihtsdo.tk.api;

// TODO: Auto-generated Javadoc
/**
 *Interface for the <code>STAMP</code> class.
 */
public interface StampBI extends VersionPointBI {
    
    /**
     * Gets the status nid.
     *
     * @return <code>int</code>
     */
    int getStatusNid();
    
    /**
     * Gets the author nid.
     *
     * @return <code>int</code>
     */
    int getAuthorNid();
    
    /**
     * Gets the module nid.
     *
     * @return <code>int</code>
     */
    int getModuleNid();

    /**
     * Returns the stamp nid with which the SAP object was constructed.
     * @return stamp nid as an <code>int</code> 
     */
    int getStampNid();
    
}
