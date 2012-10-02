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

import java.io.Serializable;
import java.util.Set;



// TODO: Auto-generated Javadoc
/**
 * The Interface PositionSetBI. TODO-javadoc: ?
 */
public interface PositionSetBI extends Set<PositionBI>, Serializable {
    
    /**
     * Gets the view path nid set.
     *
     * @return the view path nid set
     */
    public NidSetBI getViewPathNidSet();
    
    /**
     * Gets the position array.
     *
     * @return the position array
     */
    public PositionBI[] getPositionArray();
	
}
