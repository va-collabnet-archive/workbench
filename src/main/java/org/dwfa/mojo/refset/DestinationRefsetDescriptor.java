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
package org.dwfa.mojo.refset;

import org.dwfa.mojo.ConceptDescriptor;

/**
 * Describes a destination refset to be modified, including the path 
 * it should be modified on (at present this is singular).  
 */
public class DestinationRefsetDescriptor {

    private ConceptDescriptor refset;

    private ConceptDescriptor path;

    public ConceptDescriptor getRefset() {
        return refset;
    }

    public void setRefset(ConceptDescriptor refset) {
        this.refset = refset;
    }

    public ConceptDescriptor getPath() {
        return path;
    }

    public void setPath(ConceptDescriptor path) {
        this.path = path;
    }

}
