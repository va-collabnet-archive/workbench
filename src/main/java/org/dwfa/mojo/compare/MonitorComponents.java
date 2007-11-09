package org.dwfa.mojo.compare;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.mojo.CompareComponents;
import org.dwfa.mojo.ConceptDescriptor;

public class MonitorComponents  {

	List<I_Position> positions = new LinkedList<I_Position>();
	ConceptDescriptor rejectedStatus;
	I_TermFactory termFactory;
	int flaggedStatusId;

	public MonitorComponents(ConceptDescriptor rejectedStatus) {
		termFactory = LocalVersionedTerminology.get();
		this.rejectedStatus = rejectedStatus;
	}
	
	public MonitorComponents() {
		termFactory = LocalVersionedTerminology.get();
		this.rejectedStatus = rejectedStatus;
	}
	
	public List<Match> checkConcept(I_GetConceptData concept) throws Exception {

		if (rejectedStatus != null) {
			flaggedStatusId = termFactory.getConcept(
					rejectedStatus.getVerifiedConcept().getUids()).
					getConceptId();
		}

		

		
		// get latest concept attributes/descriptions/relationships
		boolean attributesMatch = true;
		boolean descriptionsMatch = true;
		boolean relationshipsMatch = true;

		
		List<I_ConceptAttributeTuple> conceptAttributeTuples1 = new LinkedList<I_ConceptAttributeTuple>();
		List<I_ConceptAttributeTuple> conceptAttributeTuples2 = new LinkedList<I_ConceptAttributeTuple>();

		List<I_DescriptionTuple> descriptionTuples1 = new LinkedList<I_DescriptionTuple>();
		List<I_DescriptionTuple> descriptionTuples2 = new LinkedList<I_DescriptionTuple>();

		List<I_RelTuple> relationshipTuples1  = new LinkedList<I_RelTuple>();
		List<I_RelTuple> relationshipTuples2 = new LinkedList<I_RelTuple>();
		
		List<Match> matches = new ArrayList<Match>();

		/*
		 * If there is only one position here, then only compare the concept attributes
		 * to the required status.  
		 */
		if (positions.size()==1) {
			boolean ok = true;
			Set<I_Position> firstPosition = new HashSet<I_Position>();
			firstPosition.add(positions.get(0));
			conceptAttributeTuples1 =
				concept.getConceptAttributeTuples(null, firstPosition);
			descriptionTuples1 = concept.getDescriptionTuples(null, null,
					firstPosition);
			relationshipTuples1 = concept.getSourceRelTuples(null, null,
					firstPosition, false);
			
			for (I_ConceptAttributeTuple tuple: conceptAttributeTuples1) {
				if (!CompareComponents.compareToFlagged(tuple.getConceptStatus(), flaggedStatusId)) {
					ok = false;
				}
			}
			for (I_DescriptionTuple tuple: descriptionTuples1) {
				if (!CompareComponents.compareToFlagged(tuple.getStatusId(), flaggedStatusId)) {
					ok = false;
				}
			}
			for (I_RelTuple tuple: relationshipTuples1) {
				if (!CompareComponents.compareToFlagged(tuple.getStatusId(), flaggedStatusId)) {
					ok = false;
				}
			}
			if (ok) {
				Match match = new Match(positions.get(0),positions.get(0));
				matches.add(match);
			}
		}
		
		for (int j = 0; j < positions.size()-1; j++) {
			Set<I_Position> firstPosition = new HashSet<I_Position>();
			firstPosition.add(positions.get(j));
			conceptAttributeTuples1 =
				concept.getConceptAttributeTuples(null, firstPosition);				
			descriptionTuples1 = concept.getDescriptionTuples(null, null,
					firstPosition);
			relationshipTuples1 = concept.getSourceRelTuples(null, null,
					firstPosition, false);
			

			
			for (int i = j; i < positions.size()-1; i++) {
				Set<I_Position> secondPosition = new HashSet<I_Position>();
				secondPosition.add(positions.get(i+1));

				conceptAttributeTuples2 =
					concept.getConceptAttributeTuples(null, secondPosition);
				descriptionTuples2 = concept.getDescriptionTuples(null, null,
						secondPosition);
				relationshipTuples2 = concept.getSourceRelTuples(null, null,
						secondPosition, false);

				if (rejectedStatus != null) {
					if (!CompareComponents.attributeListsEqual(
							conceptAttributeTuples1, conceptAttributeTuples2,
							flaggedStatusId)) {
						attributesMatch = false;
						break;
					}
				} else if (!CompareComponents.attributeListsEqual(
						conceptAttributeTuples1, conceptAttributeTuples2)) {
					attributesMatch = false;
					break;
				}
				if (rejectedStatus != null) {
					if (!CompareComponents.descriptionListsEqual(
							descriptionTuples1, descriptionTuples2,
							flaggedStatusId)) {
						descriptionsMatch = false;
						break;
					}
				} else if (!CompareComponents.descriptionListsEqual(
						descriptionTuples1, descriptionTuples2)) {
					descriptionsMatch = false;
					break;
				}

				if (rejectedStatus != null) {
					if (!CompareComponents.relationshipListsEqual(
							relationshipTuples1, relationshipTuples2,
							flaggedStatusId)) {
						relationshipsMatch = false;
						break;
					}
				} else if (!CompareComponents.relationshipListsEqual(
						relationshipTuples1, relationshipTuples2)) {
					relationshipsMatch = false;
					break;
				}



				if (descriptionsMatch && relationshipsMatch && attributesMatch) {
					Match match = new Match(positions.get(j),positions.get(i+1));
					matches.add(match);
					match.matchConceptAttributeTuples.addAll(conceptAttributeTuples1);
					match.matchDescriptionTuples.addAll(descriptionTuples1);
					match.matchRelationshipTuples.addAll(relationshipTuples1);
					match.matchConceptAttributeTuples.addAll(conceptAttributeTuples2);
					match.matchDescriptionTuples.addAll(descriptionTuples2);
					match.matchRelationshipTuples.addAll(relationshipTuples2);
					
				}

			}
			
		}
		return matches;
	}
	

	public List<I_Position> getPositions() {
		return positions;
	}

	public void setPositions(List<I_Position> positions) {
		this.positions = positions;
	}
}
