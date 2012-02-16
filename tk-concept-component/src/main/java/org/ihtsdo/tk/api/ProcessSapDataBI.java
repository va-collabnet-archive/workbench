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
 * Allows all sap nids to be efficiently tested according to the criteria
 * specified in each implementation. Each component of a concept
 * contains a sap nid. By finding sap nids which meet a certain criteria, the
 * concepts in the database can be efficiently sorted based on those concepts
 * which contain a desired sap nid.
 * @see SapBI
 * @author akf
 */
public interface ProcessSapDataBI {
    /**
     * Implement this method to processes all sap nids according to certain criteria.
     * All sap nids in the database will be returned but in no particular order.
     * @param sap
     * @throws Exception
     */
    void processSapData(SapBI sap) throws Exception;
    
}
