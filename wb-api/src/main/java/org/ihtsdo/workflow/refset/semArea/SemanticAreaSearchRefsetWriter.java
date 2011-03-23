package org.ihtsdo.workflow.refset.semArea;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
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
	
	public void setReferencedComponentId(UUID uid) {
		((SemanticAreaSearchRSFields)fields).setReferencedComponentUid(uid);
	}
	
	public void setHierarchy(I_GetConceptData hierarchy) {
		setReferencedComponentId(hierarchy.getPrimUuid());
	}
	
	public void setSearchTerm(String term) {
		((SemanticAreaSearchRSFields)fields).setSearchTerm(term);
	}
	
	
	public UUID getReferencedComponentUid() {
		return ((SemanticAreaSearchRSFields)fields).getReferencedComponentId();
	}

	public I_GetConceptData getHierarchy() {
		try {
			return Terms.get().getConcept(getReferencedComponentUid());
		} catch (Exception e) {
	    	AceLog.getAppLog().log(Level.SEVERE, "Unable to get the Category (refCompId) from the SemanticAreaSearch Refset");
		}
		
		return null;
	}

	public String getSearchTerm() {
		return ((SemanticAreaSearchRSFields)fields).getSearchTerm();
	}

	
	
	
	private class SemanticAreaSearchRSFields extends WorkflowRefsetFields {
		private String searchTerm = null;
		 		
		private SemanticAreaSearchRSFields() {

		}
		
		public void setReferencedComponentUid(UUID uid) {
			try {
				setReferencedComponentId(uid);
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.SEVERE, "Unable to set WorkflowHistoryRefset's refCompId: " + uid);
			}
		}
		
		private void setHierarchy(I_GetConceptData hierarchy) {
			setReferencedComponentUid(hierarchy.getPrimUuid());
		}
		
		private void setSearchTerm(String term) {
			searchTerm = term;
		}

		public I_GetConceptData getReferencedComponent() {
			try {
				return Terms.get().getConcept(getReferencedComponentId());
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.SEVERE, "Unable to set WorkflowHistoryRefset's refCompId");
			}
			
			return null;
		}
		
		public UUID getReferencedComponentUid() {
			return getReferencedComponentId();
		}
		
		public I_GetConceptData getHierarchy() {
			return getReferencedComponent();
		}
		
		private String getSearchTerm() {
			return searchTerm;
		}

		public String toString() {
			try { 


				return  "\nWorknSearch Term: " + searchTerm;
			} catch (Exception io) {
				return "Failed to identify searchTerm for SemanticAreaSearch Refset";
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
			boolean retVal = ((getReferencedComponentId() != null) && 
							  (searchTerm != null));
									
			if (!retVal)
			{
				StringBuffer str = new StringBuffer();
				try {
					str.append("\nError in adding to Semantic Area Search Refset");
					str.append("\nReferencedComponentId:" + getReferencedComponent().getInitialText());
					str.append("\nsearchTerm:" + searchTerm);
					AceLog.getAppLog().log(Level.WARNING, "Failure in updating Semantic Area Search Refset for concept: " + str.toString());
				} catch (Exception e) {
					AceLog.getAppLog().log(Level.WARNING, "Failure in updating Semantic Area Search Refset for concept: " + str.toString());
				}
			}
			
			return retVal;
		}
	}

	@Override
	public String fieldsToRefsetString() throws IOException {
		return  getSearchTerm();
	}
}
