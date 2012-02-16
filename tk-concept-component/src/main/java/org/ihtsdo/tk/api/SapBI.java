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
package org.ihtsdo.tk.api;

/**
 *Interface for the <code>SAP</code> class.
 * @author akf
 */
public interface SapBI {
    
    /**
     * 
     * @return <code>int</code>
     */
    int getStatusNid();
    /**
     * 
     * @return <code>int</code>
     */
    int getAuthorNid();
    /**
     * 
     * @return <code>int</code>
     */
    int getModuleNid();
    /**
     * 
     * @return <code>int</code>
     */
    int getPathNid();
    /**
     * 
     * @return <code>int</code>
     */
    long getTime();
    /**
     * Returns the sap nid with which the SAP object was constructed.
     * @return sap nid as an <code>int</code> 
     */
    int getSapNid();
    
}
