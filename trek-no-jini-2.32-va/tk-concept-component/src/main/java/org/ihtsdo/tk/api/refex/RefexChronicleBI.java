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
package org.ihtsdo.tk.api.refex;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ComponentChronicleBI;

/**
 * The Interface RefexChronicleBI represents the collection of refex versions.
 * <p>Refex is a generic term for both refsets and annotations. While both
 * refsets and annotaitons have a collection concept, the knowledge of the
 * members is stored on the collection concept for a refset and on the member
 * concept for an annotation.<p> A RefexChronicleBI, or version or analog,
 * should not be confused with the concept representing the refex collection.
 * Each RefexChronicleBI object represents one member of the refex collection.
 * <p>Annotations are best used for a refex that has a large amount of members
 * and when operations aren't required against all of the members in the refex.
 * For an annotation, the member concept must be found first in order to find
 * the refex member. <p>If the refex will be large and operations against all
 * the members are required, and indexed annotation can be used. This stores an
 * index of the member nids on the collection concept. This allows the members
 * to be found easily, but each concept must be queried in order to find the
 * specific refex member.<p>If good refex search performance is required a
 * refset is best, but is less efficient means of storing the collection of
 * members.
 *
 * @param <A> the type of object returned by the analog generator
 * @see ComponentChronicleBI
 */
public interface RefexChronicleBI<A extends RefexAnalogBI<A>>
        extends ComponentChronicleBI<RefexVersionBI<A>> {

    /**
     * Gets the nid of the refex collection containing this refex member. This is the collection nid
     * and not the refex member nid.
     *
     * @return the nid of the refex collection containing this refex member
     */
    int getRefexNid();

    /**
     * Gets the nid associated with the referenced component for this refex member.
     *
     * @return the nid associated with the referenced component
     */
    int getReferencedComponentNid();
}
