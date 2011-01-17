package org.ihtsdo.workflow.refset.semArea;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.WorkflowRefsetFields;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetWriter;



/* 
* @author Jesse Efron
* 
*/
public class SemanticAreaSearchRefsetWriter extends WorkflowRefsetWriter 
{
	public SemanticAreaSearchRefsetWriter() throws IOException, TerminologyException {
		refset = new SemanticAreaSearchRefset();
		fields = new SemanticAreaSearchRSFields();
	
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}
	
	public void setSearchTerm(String term) {
		((SemanticAreaSearchRSFields)fields).setSearchTerm(term);
	}
	
	public void setHierarchy(I_GetConceptData hierarchy) {
		((SemanticAreaSearchRSFields)fields).setHierarchy(hierarchy);
	}

	public String getSearchTerm() {
		return ((SemanticAreaSearchRSFields)fields).getSearchTerm();
	}

	public I_GetConceptData getHierarchy() {
		return ((SemanticAreaSearchRSFields)fields).getHierarchy();
	}

	private class SemanticAreaSearchRSFields extends WorkflowRefsetFields {
		private String searchTerm = null;
		 		
		private SemanticAreaSearchRSFields() {

		}
		
		private void setHierarchy(I_GetConceptData hierarchy) {
			setReferencedComponentId(hierarchy);
		}

		private I_GetConceptData getHierarchy() {
			return getReferencedComponentId();
		}
		
		private void setSearchTerm(String term) {
			searchTerm = term;
		}

		private String getSearchTerm() {
			return searchTerm;
		}

		public String toString() {
			try {
				if (searchTerm == null)
					throw new Exception("Search Term is NULL");
				
				return "\nSearch Term: " + searchTerm;
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return "No Term";
			}
		}

		@Override
		public void cleanValues() {
			// TODO Auto-generated method stub
			setReferencedComponentId(null);
			searchTerm = null;
		}

		@Override
		public boolean valuesExist() {
			// TODO Auto-generated method stub
			return ((getReferencedComponentId() != null) && 
					(searchTerm != null));
		}
	}

	@Override
	public String fieldsToRefsetString() throws IOException {
		return  getSearchTerm();
	}
}
