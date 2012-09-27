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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

// TODO: Auto-generated Javadoc
/**
 * Interface for classes that resolve contradictions - i.e. the scenario where two
 * different paths have different data for a given component.
 * <p>
 * Given a component's versions and an optional time point (latest is assumed
 * without a time point), implementations of this interface will calculate the
 * "contradiction resolved view" of the component at that time point.
 */

public interface ContradictionManagerBI extends Serializable {
    /**
     * Method to get the display name of this conflict resolution strategy.
     * Note that this is intended to be something meaningful to an end user
     * attempting to choose a conflict resolution strategy.
     * 
     * @return The display name of this conflict resolution strategy
     */
    String getDisplayName();

    /**
     * Method to get a description of this conflict resolution strategy.
     * Note that this is intended to be something meaningful to an end user
     * attempting to choose a conflict resolution strategy. This content
     * may contain XHTML markup for readability.
     * 
     * @return The display name of this conflict resolution strategy
     */
    String getDescription();

    /**
     * Resolves the supplied versions, which may be from more than one entity,
     * to a conflict resolved latest state.
     * <p>
     * Best case this will resolve to one tuple, however this will depend upon
     * the data and the resolution strategy in use.
     * <p>
     * Note that the input list of tuples will not be modified by this method.
     *
     * @param <T> the generic type
     * @param versions the versions
     * @return tuples resolved as per the resolution strategy
     */
    <T extends ComponentVersionBI> List<T> resolveVersions(List<T> versions);

    /**
     * Resolves the supplied parts to a conflict resolved latest state.
     * <p>
     * Best case this will resolve to one part, however this will depend upon
     * the data and the resolution strategy in use.
     * <p>
     * Note that the input list of parts will not be modified by this method.
     * <p>
     * <strong> NB This method requires that all the parts are from the same
     * entity! If they are not there is no way for this method to determine that
     * and resolution will take place assuming they are all from the same
     * entity. </strong>
     *
     * @param <T> the generic type
     * @param part1 the part1
     * @param part2 the part2
     * @return parts resolved as per the resolution strategy
     */
    <T extends ComponentVersionBI> List<T> resolveVersions(T part1, T part2);
    
    /**
     * Checks if is in conflict.
     *
     * @param conceptChronicle conceptChronicle to test
     * @param includeDependentEntities indicates that this conceptChronicle should be
     * considered
     * in conflict if the conceptChronicle's parts of any of its dependent
     * objects (descriptions,
     * relationships, extensions...etc) are in conflict.
     * @return true if this conceptChronicle is in conflict according to the resolution
     * strategy
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean isInConflict(ConceptChronicleBI conceptChronicle, boolean includeDependentEntities) throws IOException;
    
    /**
     * Checks if is in conflict.
     *
     * @param conceptChronicle conceptChronicle to test
     * @return true if this conceptChronicle is in conflict according to the resolution
     * strategy
     */
    boolean isInConflict(ConceptChronicleBI conceptChronicle);

    /**
     * Checks if is in conflict.
     *
     * @param descriptionChronicle descriptionChronicle to test
     * @return true if this descriptionChronicle is in conflict according to the
     * resolution strategy
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean isInConflict(DescriptionChronicleBI descriptionChronicle) throws IOException;

    /**
     * Checks if is in conflict.
     *
     * @param refexChronicle the refex chronicle
     * @return true if this extension is in conflict according to the resolution
     * strategy
     */
    boolean isInConflict(RefexChronicleBI refexChronicle);

    /**
     * Checks if is in conflict.
     *
     * @param mediaChronicle the media chronicle
     * @return true if this image is in conflict according to the resolution
     * strategy
     */
    boolean isInConflict(MediaChronicleBI mediaChronicle);

    /**
     * Checks if is in conflict.
     *
     * @param relationshipChronicle relationshipChronicle to test
     * @return true if this relationshipChronicle is in conflict according to the
     * resolution strategy
     */
    boolean isInConflict(RelationshipChronicleBI relationshipChronicle);

    /**
     * Checks if is in conflict.
     *
     * @param conceptAttributteChronicle the concept attributte chronicle
     * @return true if this conceptAttribute is in conflict according to the
     * resolution strategy
     */
    boolean isInConflict(ConceptAttributeChronicleBI conceptAttributteChronicle);
}
