/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.helper.report;

import org.ihtsdo.tk.api.StampBI;

/**
 *Possible enhancement to <code>ReportingHelper</code>. Multiple similar classes
 * would contain the knowledge of the test criteria. <code>ReportingHelper</code>
 * would be constructed using on of these test classes.
 * @author akf
 */
public interface SapTest {
    
    /**
     * Contains the information about what should be tested.
     * @param stamp
     * @return <code>true</code> if stamp nid meets criteria, otherwise <code>false</code
     */
    boolean test(StampBI stamp);
    
    //would represent a specific test
    
}
