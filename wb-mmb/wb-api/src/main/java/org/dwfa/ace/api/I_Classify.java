/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.api;

public interface I_Classify {

    /**
     * 
     * @param input the data to classify
     * @return the results of the classification
     */
    public Object classify(Object input) throws Exception;

    /**
     * 
     * @param input the data to classify
     * @param lastOutput the results of the previous classification
     * @return the results of the incremental classification
     */
    public Object increment(Object input, Object lastOutput) throws Exception;

}
