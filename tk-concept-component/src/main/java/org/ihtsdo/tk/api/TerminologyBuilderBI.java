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
import org.ihtsdo.tk.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.MediaCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;

import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

// TODO: Auto-generated Javadoc
/**
 * The Interface TerminologyBuilderBI.
 */
public interface TerminologyBuilderBI {

    /**
     * Construct.
     *
     * @param refexBlueprint the refex blueprint
     * @return A <code>RefexChronicleBI</code> if the <code>refexBlueprint</code>
     * regardless of if the RefexChronicleBI was modified.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    RefexChronicleBI<?> construct(RefexCAB refexBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * This method incurs an extra cost to determine if a current version already meets the specification.
     *
     * @param refexBlueprint the refex blueprint
     * @return A <code>RefexChronicleBI</code> if the <code>refexBlueprint</code>
     * regardless of if the RefexChronicleBI was modified.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    RefexChronicleBI<?> constructIfNotCurrent(RefexCAB refexBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Construct.
     *
     * @param relBlueprint the rel blueprint
     * @return A <code>RelationshipChronicleBI</code> if the <code>relBlueprint</code>
     * regardless of if the RelationshipChronicleBI was modified.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    RelationshipChronicleBI construct(RelationshipCAB relBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * This method incurs an extra cost to determine if a current version already meets the specification.
     *
     * @param relBlueprint the rel blueprint
     * @return A <code>RelationshipChronicleBI</code> if the
     * <code>relBlueprint</code> regardless of if the RelationshipChronicleBI was modified.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    RelationshipChronicleBI constructIfNotCurrent(RelationshipCAB relBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Construct.
     *
     * @param descriptionBlueprint the description blueprint
     * @return A <code>DescriptionChronicleBI</code> if the <code>descriptionBlueprint</code>
     * regardless of if the DescriptionChronicleBI was modified.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    DescriptionChronicleBI construct(DescriptionCAB descriptionBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * This method incurs an extra cost to determine if a current version already meets the specification.
     *
     * @param descriptionBlueprint the description blueprint
     * @return A <code>DescriptionChronicleBI</code> if the
     * <code>descriptionBlueprint</code> regardless of if the DescriptionChronicleBI was modified.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    DescriptionChronicleBI constructIfNotCurrent(DescriptionCAB descriptionBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Construct.
     *
     * @param mediaBlueprint the media blueprint
     * @return A <code>MediaChronicleBI</code> if the <code>mediaBlueprint</code>
     * regardless of if the MediaChronicleBI was modified.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    MediaChronicleBI construct(MediaCAB mediaBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * This method incurs an extra cost to determine if a current version already meets the specification.
     *
     * @param mediaBlueprint the media blueprint
     * @return A <code>MediaChronicleBI</code> if the <code>mediaBlueprint</code>
     * regardless of if the MediaChronicleBI was modified.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    MediaChronicleBI constructIfNotCurrent(MediaCAB mediaBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Construct.
     *
     * @param conceptBlueprint the concept blueprint
     * @return A <code>ConceptChronicleBI</code> if the <code>conceptBlueprint</code>
     * regardless of if the ConceptChronicleBI was modified.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    ConceptChronicleBI construct(ConceptCB conceptBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * This method incurs an extra cost to determine if a current version
     * already meets the specification.
     *
     * @param conceptBlueprint the concept blueprint
     * @return A <code>ConceptChronicleBI</code> if the <code>conceptBlueprint</code>
     * regardless of if the ConceptChronicleBI was modified.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    ConceptChronicleBI constructIfNotCurrent(ConceptCB conceptBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Construct.
     *
     * @param conceptAttributeBlueprint the concept attribute blueprint
     * @return A <code>ConceptAttributeChronicleBI</code> if the <code>conceptAttributeBlueprint</code> regardless of if the ConceptAttributeChronicleBI was modified.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    ConceptAttributeChronicleBI construct(ConceptAttributeAB conceptAttributeBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * This method incurs an extra cost to determine if a current version already meets the specification.
     *
     * @param conceptAttributeBlueprint the concept attribute blueprint
     * @return A <code>ConceptAttributeChronicleBI</code> if the <code>conceptAttributeBlueprint</code> regardless of if the ConceptAttributeChronicleBI was modified.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    ConceptAttributeChronicleBI constructIfNotCurrent(ConceptAttributeAB conceptAttributeBlueprint) throws IOException, InvalidCAB, ContradictionException;
    
    /**
     * Gets the edits the coordinate.
     *
     * @return the edits the coordinate
     */
    EditCoordinate getEditCoordinate();
}
