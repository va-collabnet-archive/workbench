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

/**
 * Allows all stamp nids to be efficiently tested according to the criteria
 * specified in each implementation. Each component of a concept
 * contains a stamp nid. By finding stamp nids which meet a certain criteria, the
 * concepts in the database can be efficiently sorted based on those concepts
 * which contain a desired stamp.
 * 
 * @see StampBI
 * @see TerminologyStoreDI#iterateStampDataInSequence(org.ihtsdo.tk.api.ProcessStampDataBI) 
 * 
 */
public interface ProcessStampDataBI {
    
    /**
     * Implement this method to processes all stamp nids according to certain criteria.
     * All stamp nids in the database will be returned but in no particular order.
     *
     * @param stamp the object representing the stamp nid to process
     * @throws Exception indicates an exception has occurred todo-javadoc: why?
     */
    void processStampData(StampBI stamp) throws Exception;
    
}
