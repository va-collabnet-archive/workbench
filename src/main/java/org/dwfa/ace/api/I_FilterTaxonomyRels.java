package org.dwfa.ace.api;

import java.io.IOException;
import java.util.List;

import org.dwfa.tapi.TerminologyException;

public interface I_FilterTaxonomyRels {
	
	/**
	 * The filter can suppress children in the taxonomy view by removing any relationship tuples
	 * from the srcRels or destRels lists. Only the relationship tuples remaining after the filter will
	 * be displayed in the taxonomy view. 
	 * @param node The taxonomy node that the srcRels and destRels are relative to. 
	 * @param srcRels The source relationships tuples that meet the criterion in the preferences panel to be displayed in the taxonomy view. 
	 * @param destRels The destination relationship tuples that meet the criterion in the preference panel to be displayed in the taxonomy view. 
	 * @throws IOException 
	 * @throws TerminologyException 
	 */
	public void filter(I_GetConceptData node, List<I_RelTuple> srcRels, 
			List<I_RelTuple> destRels, I_ConfigAceFrame frameConfig) throws TerminologyException, IOException;

}
