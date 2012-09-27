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
package org.ihtsdo.tk.api.relationship.group;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

// TODO: Auto-generated Javadoc
/**
 * The Interface RelationshipGroupVersionBI.
 */
public interface RelationshipGroupVersionBI extends RelationshipGroupChronicleBI, ComponentVersionBI {
   
   /**
    * Gets the relationships active all versions.
    *
    * @return the relationships active all versions
    */
   Collection<? extends RelationshipVersionBI> getRelationshipsActiveAllVersions();

   /**
    * Gets the relationships all.
    *
    * @return the relationships all
    * @throws ContradictionException the contradiction exception
    */
   Collection<? extends RelationshipVersionBI> getRelationshipsAll() throws ContradictionException;

   /**
    * Gets the relationships active.
    *
    * @return the relationships active
    * @throws ContradictionException the contradiction exception
    */
   Collection<? extends RelationshipVersionBI> getRelationshipsActive() throws ContradictionException;
}
