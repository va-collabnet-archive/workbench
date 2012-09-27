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

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Interface PathBI.
 */
public interface PathBI {
   
   /**
    * To html string.
    *
    * @return the string
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public String toHtmlString() throws IOException;

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept nid.
    *
    * @return the concept nid
    */
   public int getConceptNid();

   /**
    * Get all origins and origin of origins, etc., for this path.
    *
    * @return the inherited origins
    */
   public Set<? extends PositionBI> getInheritedOrigins();

   /**
    * Gets the matching path.
    *
    * @param pathNid the path nid
    * @return the matching path
    */
   public PathBI getMatchingPath(int pathNid);

   /**
    * Similar to {@link #getInheritedOrigins()} however superseded origins
    * (where there is more than one origin for the same path but with an
    * earlier version) will be excluded.
    *
    * @return the normalised origins
    */
   public Set<? extends PositionBI> getNormalisedOrigins();

   /**
    * Gets the origins.
    *
    * @return the origins
    */
   public Collection<? extends PositionBI> getOrigins();

   /**
    * Gets the uUI ds.
    *
    * @return the uUI ds
    */
   public List<UUID> getUUIDs();
}
