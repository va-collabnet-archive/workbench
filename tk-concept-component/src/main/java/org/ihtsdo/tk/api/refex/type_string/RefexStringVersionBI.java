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
package org.ihtsdo.tk.api.refex.type_string;

import org.ihtsdo.tk.api.refex.RefexVersionBI;

// TODO: Auto-generated Javadoc
/**
 * The Interface RefexStringVersionBI provides methods for interacting with a
 * particular version of a string type refex member.
 *
 * @param <A> the type of object returned by the analog generator
 * @see ComponentVersionBI
 */
public interface RefexStringVersionBI <A extends RefexStringAnalogBI<A>>
        extends RefexVersionBI<A> {
    
     /**
      * Gets the string associated with this version of the string refex
     * member.
      *
      * @return the string associated with this refex version
      */
     String getString1();

}
