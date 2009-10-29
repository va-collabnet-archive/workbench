package org.dwfa.ace.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.ThinDescVersioned;

public class ActiveConceptAndDescTest implements I_TestSearchResults {

	public boolean test(I_AmTermComponent component,
			I_ConfigAceFrame frameConfig)
			throws TaskFailedException {
		ThinDescVersioned descV = (ThinDescVersioned) component;
		ConceptBean concept = ConceptBean.get(descV.getConceptId());
		try {
			List<I_ConceptAttributeTuple> attributes = concept.getConceptAttributeTuples(frameConfig.getAllowedStatus(), 
					frameConfig.getViewPositionSet(), true, false);
			if (attributes == null || attributes.size() == 0) {
				return false;
			}
			List<I_DescriptionTuple> matchingTuples = new ArrayList<I_DescriptionTuple>();
			I_IntSet allowedTypes = null;
			if (frameConfig.searchWithDescTypeFilter()) {
				allowedTypes = frameConfig.getDescTypes();
			}
			
			descV.addTuples(allowedTypes, 
					matchingTuples, true, false);
			if (matchingTuples.size() == 0) {
				return false;
			}
			
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return true;
	}

}
