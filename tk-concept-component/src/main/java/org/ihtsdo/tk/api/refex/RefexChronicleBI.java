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
package org.ihtsdo.tk.api.refex;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.ComponentChronicleBI;

// TODO: Auto-generated Javadoc
/**
 * The Interface RefexChronicleBI.
 *
 * @param <A> the generic type
 */
public interface RefexChronicleBI<A extends RefexAnalogBI<A>>
        extends ComponentChronicleBI<RefexVersionBI<A>> {
   
   /**
    * Gets the refex nid.
    *
    * @return the refex nid
    */
   int getRefexNid();

   /**
    * Gets the referenced component nid.
    *
    * @return the referenced component nid
    */
   int getReferencedComponentNid();
}
