package org.dwfa.mojo.compare;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.mojo.CompareComponents;

public class MonitorComponents  {

	List<I_Position> positions = new LinkedList<I_Position>();
	I_TermFactory termFactory;

	public MonitorComponents() {
		termFactory = LocalVersionedTerminology.get();
	}

	public List<Match> checkConcept(I_GetConceptData concept, List<Integer> acceptedStatusIds) throws Exception {

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
			Set<I_Position> firstPosition = new HashSet<I_Position>();
			firstPosition.add(positions.get(0));
			conceptAttributeTuples1 =
				concept.getConceptAttributeTuples(null, firstPosition);
			descriptionTuples1 = concept.getDescriptionTuples(null, null,
					firstPosition);
			relationshipTuples1 = concept.getSourceRelTuples(null, null,
					firstPosition, false);

				
			/*
			 * Is the status correct??
			 * */
			attributesMatch = false;
			for (int tuple = 0; tuple < conceptAttributeTuples1.size();tuple++) {
				if ((acceptedStatusIds.size()==0 || (acceptedStatusIds.size()!=0 && acceptedStatusIds.contains(conceptAttributeTuples1.get(tuple).getConceptStatus())))) {
					
					attributesMatch = true;
				}
			}					
			descriptionsMatch = false;
			for (int tuple = 0; tuple < descriptionTuples1.size();tuple++) {
				if ((acceptedStatusIds.size()==0 || (acceptedStatusIds.size()!=0 && acceptedStatusIds.contains(descriptionTuples1.get(tuple).getStatusId())))) {
					descriptionsMatch = true;
				}
			}
			relationshipsMatch = false;
			if (relationshipTuples1.size()==0) {
				relationshipsMatch = true;
			}
			for (int tuple = 0; tuple < relationshipTuples1.size();tuple++) {
				if ((acceptedStatusIds.size()==0 || (acceptedStatusIds.size()!=0 && acceptedStatusIds.contains(relationshipTuples1.get(tuple).getStatusId())))) {
					relationshipsMatch = true;
				}
			}
			
			if (attributesMatch && descriptionsMatch && relationshipsMatch) {
				Match match = new Match(positions.get(0),positions.get(0));
				match.matchConceptAttributeTuples.addAll(conceptAttributeTuples1);
				match.matchDescriptionTuples.addAll(descriptionTuples1);
				match.matchRelationshipTuples.addAll(relationshipTuples1);
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


				if (!CompareComponents.attributeListsEqual(
						conceptAttributeTuples1, conceptAttributeTuples2)) {
					attributesMatch = false;
				} else {

					/*
					 * There is a match, but is the status correct??
					 * */
					attributesMatch = false;
					for (int tuple = 0; tuple < conceptAttributeTuples1.size();tuple++) {
						if ((acceptedStatusIds.size()==0 || (acceptedStatusIds.size()!=0 && acceptedStatusIds.contains(conceptAttributeTuples1.get(tuple).getConceptStatus())))) {
							attributesMatch = true;
						}
					}					
				}
				if (!CompareComponents.descriptionListsEqual(
						descriptionTuples1, descriptionTuples2)) {
					descriptionsMatch = false;
				} else {
					/*
					 * There is a match, but is the status correct??
					 * */
					descriptionsMatch = false;
					for (int tuple = 0; tuple < descriptionTuples1.size();tuple++) {
						if ((acceptedStatusIds.size()==0 || (acceptedStatusIds.size()!=0 && acceptedStatusIds.contains(descriptionTuples1.get(tuple).getStatusId())))) {
							descriptionsMatch = true;
						}
					}
				}

				if (!CompareComponents.relationshipListsEqual(
						relationshipTuples1, relationshipTuples2)) {
					relationshipsMatch = false;
				} else {
					/*
					 * There is a match, but is the status correct??
					 * */
					relationshipsMatch = false;
					if (relationshipTuples1.size()==0) {
						relationshipsMatch = true;
					}
					for (int tuple = 0; tuple < relationshipTuples1.size();tuple++) {
						if ((acceptedStatusIds.size()==0 || (acceptedStatusIds.size()!=0 && acceptedStatusIds.contains(relationshipTuples1.get(tuple).getStatusId())))) {
							relationshipsMatch = true;
						}
					}
				}


				if (descriptionsMatch && relationshipsMatch && attributesMatch) {					
					Match match = new Match(positions.get(j),positions.get(i+1));
					match.matchConceptAttributeTuples.addAll(conceptAttributeTuples1);
					match.matchDescriptionTuples.addAll(descriptionTuples1);
					match.matchRelationshipTuples.addAll(relationshipTuples1);
					matches.add(match);
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
