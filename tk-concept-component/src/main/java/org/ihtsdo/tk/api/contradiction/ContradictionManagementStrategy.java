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
package org.ihtsdo.tk.api.contradiction;

import java.io.IOException;
import java.util.ArrayList;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

/**
 * The Class ContradictionManagementStrategy contains methods general to
 * determining if a concept is in contradiction and managing the contradiction.
 */
public abstract class ContradictionManagementStrategy implements ContradictionManagerBI {

    private static final long serialVersionUID = 1L;
    protected transient ViewCoordinate vc;
    protected transient EditCoordinate ec;

    /**
     * Sets the view coordinate for determining which versions of the concept
     * are active or inactive.
     *
     * @param viewCoordinate the view coordinate for this contradiction
     * management strategy
     */
    public void setViewCoordinate(ViewCoordinate viewCoordinate) {
        this.vc = viewCoordinate;
    }

    /**
     * Gets the view coordinate associated with this contradiction management
     * strategy.
     *
     * @return the view coordinate associated with this contradiction management
     * strategy
     */
    public ViewCoordinate getViewCoordinate() {
        return vc;
    }

    /**
     * Sets the edit coordinate containing the editing metadata associated with
     * this contradiction management strategy.
     *
     * @param editCoordinate the edit coordinate associated with this
     * contradiction management strategy
     */
    public void setEditCoordinate(EditCoordinate editCoordinate) {
        this.ec = editCoordinate;
    }

    /**
     * Gets the edit coordinate associated with this contradiction management
     * strategy.
     *
     * @return the edit coordinate associated with this contradiction management
     * strategy
     */
    public EditCoordinate getEditCoordinate() {
        return ec;
    }

    /**
     * Returns the display name of this contradiction management strategy.
     *
     * @return the display name of this contradiction management strategy
     */
    @Override
    public String toString() {
        return getDisplayName();
    }

    /**
     * Checks if the given object is null.
     *
     * @param obj the object to check
     * @return <code>true</code>, if the object is null
     */
    private boolean isNull(Object... obj) {
        if (obj == null) {
            return true;
        }
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the display name of this contradiction management strategy is
     * equal to the other.
     *
     * @param obj another contradiction management strategy
     * @return <code>true</code> if the display name of this contradiction
     * management strategy is equal to the other
     */
    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    /**
     * Returns a hashcode of this contradiction management strategy's display
     * name
     *
     * @return a hashcode of this contradiction management strategy's display
     * name
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Base implementation of the method that determines if a conceptChronicle
     * is in conflict. This method can check just the conceptChronicle
     * attributes, or if passed the instruction to check the dependent entities
     * of the conceptChronicle (descriptions, relationships, extensions etc) it
     * will check through each.
     *
     * @param conceptChronicle the concept chronicle to check
     * @param includeDependentEntities set to <code>true</code> to check the
     * components of a concept in addition to the concept
     * @return <code>true</code>, if concept or its components is in contradiction
     * @throws IOException signals that an I/O exception has occurred
     * @see
     * org.dwfa.ace.api.I_ManageContradiction#isInConflict(org.dwfa.ace.api.I_GetConceptData,
     * boolean)
     */
    @Override
    public boolean isInConflict(ConceptChronicleBI conceptChronicle, boolean includeDependentEntities) throws IOException {

        if (resolveVersions(new ArrayList(conceptChronicle.getVersions(vc))).size() > 1) {
            return true;
        }

        if (!includeDependentEntities) {
            return false;
        }

        for (DescriptionChronicleBI description : conceptChronicle.getDescriptions()) {
            if (resolveVersions(new ArrayList(description.getVersions(vc))).size() > 1) {
                return true;
            }
        }

        for (RelationshipChronicleBI relationship : conceptChronicle.getRelationshipsOutgoing()) {
            if (resolveVersions(new ArrayList(relationship.getVersions(vc))).size() > 1) {
                return true;
            }
        }

        for (RefexChronicleBI refex : conceptChronicle.getRefexes()) {
            if (resolveVersions(new ArrayList(refex.getVersions(vc))).size() > 1) {
                return true;
            }
        }

        for (MediaChronicleBI media : conceptChronicle.getMedia()) {
            if (resolveVersions(new ArrayList(media.getVersions(vc))).size() > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param conceptChronicle concept to test
     * @return <code>true</code> if this conceptChronicle is in conflict according to the resolution
     * strategy
     */
    @Override
    public boolean isInConflict(ConceptChronicleBI conceptChronicle) {
        if (resolveVersions(new ArrayList(conceptChronicle.getVersions(vc))).size() > 1) {
            return true;
        }
        return false;
    }

    /**
     * 
     * @param conceptAttributeChronicle the concept attribute to test
     * @return <code>true</code> if this conceptAttribute is in conflict according to the
     * resolution strategy
     */
    @Override
    public boolean isInConflict(ConceptAttributeChronicleBI conceptAttributeChronicle) {
        if (resolveVersions(new ArrayList(conceptAttributeChronicle.getVersions(vc))).size() > 1) {
            return true;
        }
        return false;
    }

    /**
     * 
     * @param mediaChronicle the media to test
     * @return <code>true</code> if this image is in conflict according to the resolution
     * strategy 
     */
    @Override
    public boolean isInConflict(MediaChronicleBI mediaChronicle) {
        if (resolveVersions(new ArrayList(mediaChronicle.getVersions(vc))).size() > 1) {
            return true;
        }
        return false;
    }

    /**
     * 
     * @param relationshipChronicle relationship to test
     * @return <code>true</code> if this relationshipChronicle is in conflict according to the
     * resolution strategy
     */
    @Override
    public boolean isInConflict(RelationshipChronicleBI relationshipChronicle) {
        if (resolveVersions(new ArrayList(relationshipChronicle.getVersions(vc))).size() > 1) {
            return true;
        }
        return false;
    }

    /**
     * 
     * @param descriptionChronicle description to test
     * @return <code>true</code> if this descriptionChronicle is in conflict according to the
     * resolution strategy
     */
    @Override
    public boolean isInConflict(DescriptionChronicleBI descriptionChronicle) {
        if (resolveVersions(new ArrayList(descriptionChronicle.getVersions(vc))).size() > 1) {
            return true;
        }
        return false;
    }

    /**
     * 
     * @param refexChronicle the refex to test
     * @return <code>true</code> if this extension is in conflict according to the resolution
     * strategy 
     */
    @Override
    public boolean isInConflict(RefexChronicleBI refexChronicle) {
        if (resolveVersions(new ArrayList(refexChronicle.getVersions(vc))).size() > 1) {
            return true;
        }
        return false;
    }
}
