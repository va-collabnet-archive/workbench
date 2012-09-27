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
package org.dwfa.tapi.spec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_RelateConceptsLocally;
import org.dwfa.tapi.TerminologyException;

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
    public boolean contains(I_ConceptualizeLocally concept) throws IOException, TerminologyException {
        // first check if it is the root node
        I_ConceptualizeLocally root = rootNode.localize();
        if (root.getNid() == concept.getNid()) {
            return true;
        }
        return processChildren(root, concept);
    }

    private boolean processChildren(I_ConceptualizeLocally root, I_ConceptualizeLocally concept) throws IOException,
            TerminologyException {
        // if not, for each of the children
        for (I_RelateConceptsLocally relationship : root.getDestRels(convertToConceptualizeList(relationshipTypes))) {
            if (relationship.getC1().getNid() == concept.getNid()) {
                return true;
            } else {
                if (processChildren(relationship.getC1(), concept)) {
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
    private Collection<I_ConceptualizeLocally> convertToConceptualizeList(ConceptSpec[] conceptSpecs) {

        Collection<I_ConceptualizeLocally> concepts = new ArrayList<I_ConceptualizeLocally>();

        for (ConceptSpec conceptSpec : conceptSpecs) {
            concepts.add(conceptSpec.localize());
        }
        return concepts;
    }

}
