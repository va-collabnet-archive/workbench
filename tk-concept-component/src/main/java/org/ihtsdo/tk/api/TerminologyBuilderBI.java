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
package org.ihtsdo.tk.api;

import java.io.IOException;
import org.ihtsdo.tk.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.MediaCAB;
import org.ihtsdo.tk.api.blueprint.PathCB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;

import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

/**
 * The Interface TerminologyBuilderBI is used for constructing blueprints.
 * Constructing is the processes of taking the blueprint specification and a
 * creating a component. This is the preferred method for updating or creating
 * new components. The edits occur according to the editing metadata values set
 * in the
 * <code>editCoordinate</code>, such as the author, time, module and path to be
 * associated with the edit.
 *
 * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint
 */
public interface TerminologyBuilderBI {

    /**
     * Constructs a refex from the
     * <code>refexBlueprint</code>.
     *
     * @param refexBlueprint the refex blueprint
     * @return A <code>RefexChronicleBI</code> of
     * the <code>refexBlueprint</code> regardless of if the RefexChronicleBI was
     * modified.
     *
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    RefexChronicleBI<?> construct(RefexCAB refexBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Constructs a refex from the
     * <code>refexBlueprint</code>.. This method incurs an extra cost to
     * determine if a current version already meets the specification.
     *
     * @param refexBlueprint the refex blueprint
     * @return A <code>RefexChronicleBI</code> of
     * the <code>refexBlueprint</code> regardless of if the RefexChronicleBI was
     * modified.
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    RefexChronicleBI<?> constructIfNotCurrent(RefexCAB refexBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Constructs a relationship from the
     * <code>relBlueprint</code>..
     *
     * @param relBlueprint the relationship blueprint
     * @return A <code>RelationshipChronicleBI</code> of
     * the <code>relBlueprint</code> regardless of if the
     * RelationshipChronicleBI was modified.
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    RelationshipChronicleBI construct(RelationshipCAB relBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Constructs a relationship from the
     * <code>relBlueprint</code>. This method incurs an extra cost to determine
     * if a current version already meets the specification.
     *
     * @param relBlueprint the relationship blueprint
     * @return A <code>RelationshipChronicleBI</code> of the
     * <code>relBlueprint</code> regardless of if the RelationshipChronicleBI
     * was modified.
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    RelationshipChronicleBI constructIfNotCurrent(RelationshipCAB relBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Constructs a description from the
     * <code>descriptionBlueprint</code>.
     *
     * @param descriptionBlueprint the description blueprint
     * @return A <code>DescriptionChronicleBI</code> of
     * the <code>descriptionBlueprint</code> regardless of if the
     * DescriptionChronicleBI was modified.
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    DescriptionChronicleBI construct(DescriptionCAB descriptionBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Constructs a description from the
     * <code>descriptionBlueprint</code>. This method incurs an extra cost to
     * determine if a current version already meets the specification.
     *
     * @param descriptionBlueprint the description blueprint
     * @return A <code>DescriptionChronicleBI</code> of the
     * <code>descriptionBlueprint</code> regardless of if the
     * DescriptionChronicleBI was modified.
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    DescriptionChronicleBI constructIfNotCurrent(DescriptionCAB descriptionBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Constructs a type of media from the
     * <code>mediaBlueprint</code>.
     *
     * @param mediaBlueprint the media blueprint
     * @return A <code>MediaChronicleBI</code> of
     * the <code>mediaBlueprint</code> regardless of if the MediaChronicleBI was
     * modified.
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    MediaChronicleBI construct(MediaCAB mediaBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Constructs a type of media from the
     * <code>mediaBlueprint</code>.. This method incurs an extra cost to
     * determine if a current version already meets the specification.
     *
     * @param mediaBlueprint the media blueprint
     * @return A <code>MediaChronicleBI</code> of
     * the <code>mediaBlueprint</code> regardless of if the MediaChronicleBI was
     * modified.
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    MediaChronicleBI constructIfNotCurrent(MediaCAB mediaBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Constructs a concept from the
     * <code>conceptBlueprint</code>.
     *
     * @param conceptBlueprint the concept blueprint
     * @return A <code>ConceptChronicleBI</code> of
     * the <code>conceptBlueprint</code> regardless of if the ConceptChronicleBI
     * was modified.
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    ConceptChronicleBI construct(ConceptCB conceptBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Constructs a concept from the
     * <code>conceptBlueprint</code>. This method incurs an extra cost to
     * determine if a current version already meets the specification.
     *
     * @param conceptBlueprint the concept blueprint
     * @return A <code>ConceptChronicleBI</code> of
     * the <code>conceptBlueprint</code> regardless of if the ConceptChronicleBI
     * was modified.
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    ConceptChronicleBI constructIfNotCurrent(ConceptCB conceptBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Constructs a concept attribute from the
     * <code>conceptAttributeBlueprint</code>.
     *
     * @param conceptAttributeBlueprint the concept attribute blueprint
     * @return A <code>ConceptAttributeChronicleBI</code> of
     * the <code>conceptAttributeBlueprint</code> regardless of if the
     * ConceptAttributeChronicleBI was modified.
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    ConceptAttributeChronicleBI construct(ConceptAttributeAB conceptAttributeBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Constructs a concept attribute from the
     * <code>conceptAttributeBlueprint</code>. This method incurs an extra cost
     * to determine if a current version already meets the specification.
     *
     * @param conceptAttributeBlueprint the concept attribute blueprint
     * @return A <code>ConceptAttributeChronicleBI</code> of
     * the <code>conceptAttributeBlueprint</code> regardless of if the
     * ConceptAttributeChronicleBI was modified.
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    ConceptAttributeChronicleBI constructIfNotCurrent(ConceptAttributeAB conceptAttributeBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Gets the edit coordinate used in constructing this terminology builder.
     * The edit coordinate contains the metadata needed for editing such as the
     * author, time, module and path to be associated with the edit.
     *
     * @return the edit coordinate associated with this builder
     */
    EditCoordinate getEditCoordinate();
    
    PathBI construct(PathCB blueprint) throws IOException, InvalidCAB, ContradictionException;
}
