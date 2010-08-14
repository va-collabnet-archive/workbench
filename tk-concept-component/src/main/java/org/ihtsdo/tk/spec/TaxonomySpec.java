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
package org.ihtsdo.tk.spec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 * Class that allows a hierarchy to be specified or identified by specifying a
 * root node and the relationship
 * type to children of the hierarchy. Implemented on top of the ConceptSpec
 * class.
 * 
 * @author Dion McMurtrie
 */
public class TaxonomySpec {

    private ConceptSpec rootNode;
    private ConceptSpec[] relationshipTypes;

    public ConceptSpec getRootNode() {
        return rootNode;
    }

    public void setRootNode(ConceptSpec rootNode) {
        this.rootNode = rootNode;
    }

    public ConceptSpec[] getRelationshipTypes() {
        return relationshipTypes;
    }

    public void setRelationshipTypes(ConceptSpec[] relationshipTypes) {
        this.relationshipTypes = relationshipTypes;
    }

    /**
     * Method that determines whether the specified concept falls within the
     * concept hierarchy
     * defined by this class
     * 
     * @param concept to be tested
     * @return true if the concept is in the hierarchy described by this object,
     *         false otherwise
     * @throws IOException
     * @throws TerminologyException
     */
    public boolean contains(ConceptVersionBI concept, Coordinate c) throws IOException {
        // first check if it is the root node
    	ConceptVersionBI root = rootNode.get(c);
        if (root.getNid() == concept.getNid()) {
            return true;
        }
        return processChildren(root, concept, c);
    }

    private boolean processChildren(ConceptVersionBI root, ConceptVersionBI concept, Coordinate c) throws IOException {
        // if not, for each of the children
        for (ConceptVersionBI origin : root.getRelsIncomingOriginsActive((convertToConceptList(relationshipTypes, c)))) {
            if (origin.getNid() == concept.getNid()) {
                return true;
            } else {
                if (processChildren(origin, concept, c)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Helper method that converts an array of ConceptSpec to a collection of
     * I_ConceptualizeLocally
     * 
     * @param conceptSpecs array of ConceptSpec to convert
     * @return conceptSpecs converted to a collection of I_ConceptualizeLocally
     *         using the ConceptSpec.localize() method
     */
    private Collection<ConceptVersionBI> convertToConceptList(ConceptSpec[] conceptSpecs, Coordinate c) {

        Collection<ConceptVersionBI> concepts = new ArrayList<ConceptVersionBI>();

        for (ConceptSpec conceptSpec : conceptSpecs) {
            concepts.add(conceptSpec.get(c));
        }
        return concepts;
    }

}
