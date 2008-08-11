package org.dwfa.tapi.spec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_RelateConceptsLocally;
import org.dwfa.tapi.TerminologyException;

/**
 * Class that allows a hierarchy to be specified or identified by secifying a root node and the relationship
 * type to children of the hierarcy. Implemented on top of the ConceptSpec class.
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
	 * Method that determines whether the specified concept falls within the concept hierarchy
	 * defined by this class
	 * 
	 * @param concept to be tested
	 * @return true if the concept is in the hierarchy described by this object, false otherwise
	 * @throws IOException
	 * @throws TerminologyException
	 */
	public boolean contains(I_ConceptualizeLocally concept) throws IOException, TerminologyException {
		//first check if it is the root node
		if (rootNode.localize().equals(concept)) {
			return true;
		}
		//if not, for each of the children
		for (I_RelateConceptsLocally relationship : concept.getSourceRels(convertToConceptualizeList(relationshipTypes))) {
			if (relationship.getC2().equals(rootNode.localize())) {
				return true;
			} else {
				return contains(relationship.getC2());
			}
		}
		return false;
	}

	/**
	 * Helper method that converts an array of ConceptSpec to a collection of I_ConceptualizeLocally
	 * 
	 * @param conceptSpecs array of ConceptSpec to convert
	 * @return conceptSpecs converted to a collection of I_ConceptualizeLocally using the ConceptSpec.localize() method
	 */
	private Collection<I_ConceptualizeLocally> convertToConceptualizeList(
			ConceptSpec[] conceptSpecs) {
		
		Collection<I_ConceptualizeLocally> concepts = new ArrayList<I_ConceptualizeLocally>();
		
		for (ConceptSpec conceptSpec : conceptSpecs) {
			concepts.add(conceptSpec.localize());
		}
		return concepts;
	}
	
}
