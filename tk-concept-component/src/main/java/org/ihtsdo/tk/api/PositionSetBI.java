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

/**
 * The Interface PositionSetBI represents a serializable set of positions.
 */
public interface PositionSetBI extends Set<PositionBI>, Serializable {
    
    /**
     * Gets a nid set of all of the view path nids represented in this position set.
     *
     * @return a set of view path nids
     */
    public NidSetBI getViewPathNidSet();
    
    /**
     * Gets an array representation of all of the positions in this position set.
     *
     * @return an array representing this position set
     */
    public PositionBI[] getPositionArray();
	
}
