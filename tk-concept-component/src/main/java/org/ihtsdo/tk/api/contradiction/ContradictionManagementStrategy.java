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

public abstract class ContradictionManagementStrategy implements ContradictionManagerBI{

    private static final long serialVersionUID = 1L;
    
    protected transient ViewCoordinate vc;
    
    protected transient EditCoordinate ec;

    public void setViewCoordinate(ViewCoordinate viewCoordinate) {
        this.vc = viewCoordinate;
    }

    public ViewCoordinate getViewCoordinate() {
        return vc;
    }
    
    public void setEditCoordinate(EditCoordinate editCoordinate) {
        this.ec = editCoordinate;
    }

    public EditCoordinate getEditCoordinate() {
        return ec;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    private boolean isNull(Object... obj) {
        if (obj == null) {
            return true;
        }
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] == null)
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
         return toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    
    /**
     * Base implementation of the method that determines if a conceptChronicle is in
     * conflict.
     * This method can check just the conceptChronicle attributes, or if passed the
     * instruction
     * to check the dependent entities of the conceptChronicle (descriptions,
     * relationships,
     * extensions etc) it will check through each.
     * 
     * @see org.dwfa.ace.api.I_ManageContradiction#isInConflict(org.dwfa.ace.api.I_GetConceptData,
     *      boolean)
     */
    @Override
    public boolean isInConflict(ConceptChronicleBI conceptChronicle, boolean includeDependentEntities) throws IOException{

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
    
    @Override
    public boolean isInConflict(ConceptChronicleBI conceptChronicle){
        if(resolveVersions(new ArrayList(conceptChronicle.getVersions(vc))).size() > 1){
            return true;
        }
        return false;
    }

    @Override
    public boolean isInConflict(ConceptAttributeChronicleBI conceptAttributeChronicle){
        if(resolveVersions(new ArrayList(conceptAttributeChronicle.getVersions(vc))).size() > 1){
            return true;
        }
        return false;
    }

    @Override
    public boolean isInConflict(MediaChronicleBI mediaChronicle){
        if(resolveVersions(new ArrayList(mediaChronicle.getVersions(vc))).size() > 1){
            return true;
        }
        return false;
    }

    @Override
    public boolean isInConflict(RelationshipChronicleBI relationshipChronicle){
        if(resolveVersions(new ArrayList(relationshipChronicle.getVersions(vc))).size() > 1){
            return true;
        }
        return false;
    }

    @Override
    public boolean isInConflict(DescriptionChronicleBI descriptionChronicle){
        if(resolveVersions(new ArrayList(descriptionChronicle.getVersions(vc))).size() > 1){
            return true;
        }
        return false;
    }

    @Override
    public boolean isInConflict(RefexChronicleBI refexChronicle){
        if(resolveVersions(new ArrayList(refexChronicle.getVersions(vc))).size() > 1){
            return true;
        }
        return false;
    }

   
}
