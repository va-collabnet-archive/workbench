package org.ihtsdo.workflow.refset.semHier;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.WorkflowRefsetFields;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetWriter;



/* 
* @author Jesse Efron
* 
*/
public class SemanticAreaHierarchyRefsetWriter extends WorkflowRefsetWriter {
	private final I_GetConceptData identifiedReferencedComponent = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SEMANTIC_PARENT_REL.getUids());
	
	public SemanticAreaHierarchyRefsetWriter() throws IOException, TerminologyException {
		refset = new SemanticAreaHierarchyRefset();
		fields = new SemanticAreaHierarchyRSFields();

		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}
	
	public void setReferencedComponentId(UUID uid) {
		((SemanticAreaHierarchyRSFields)fields).setReferencedComponentUid(uid);
	}
		
	public UUID getReferencedComponentUid() {
		return ((SemanticAreaHierarchyRSFields)fields).getReferencedComponentId();
	}


	public void setParentSemanticArea(String area) {
		((SemanticAreaHierarchyRSFields)fields).setParentSemanticArea(area);
	}
	
	public void setChildSemanticArea(String area) {
			((SemanticAreaHierarchyRSFields)fields).setChildSemanticArea(area);
	}

	public String getChildSemanticArea() {
		return ((SemanticAreaHierarchyRSFields)fields).getChildSemanticArea();
	}

	public String getParentSemanticArea() {
		return ((SemanticAreaHierarchyRSFields)fields).getParentSemanticArea();
	}

	
	
	private class SemanticAreaHierarchyRSFields extends WorkflowRefsetFields {
		private String childSemanticArea = null;
		private String parentSemanticArea = null;
		 		
		private SemanticAreaHierarchyRSFields() {
			setReferencedComponentId(identifiedReferencedComponent.getPrimUuid());
		}
		
		public void setReferencedComponentUid(UUID uid) {
			try {
				setReferencedComponentId(uid);
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.SEVERE, "Unable to set WorkflowHistoryRefset's refCompId: ", e);
			}
		}

		public I_GetConceptData getReferencedComponent() {
			try {
				return Terms.get().getConcept(getReferencedComponentId());
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.SEVERE, "Unable to set WorkflowHistoryRefset's refCompId: ", e);
			}
			
			return null;
		}
		
		public UUID getReferencedComponentUid() {
			return getReferencedComponentId();
		}
		
		private void setParentSemanticArea(String area) {
			parentSemanticArea = area;
		}
		
		private void setChildSemanticArea(String area) {
			childSemanticArea = area;
		}

		private String getParentSemanticArea() {
			return parentSemanticArea;
		}
		
		private String getChildSemanticArea() {
			return childSemanticArea;
		}

		public String toString() {
			try {
				return "\nReferenced Component Id (Same for each row -- HardCoded) = " + getReferencedComponent().getInitialText() + 
					   "(" + getReferencedComponent().getConceptNid() + ")" +
					   "\nChild Semantic Area = " + childSemanticArea +
					   "\nParentSemantic Area = " + parentSemanticArea;
			} catch (IOException io) {
				return "Failed to identify referencedComponentId" + 
					   "\nError msg: " + io.getMessage();
			}
		}

		@Override
		public void cleanValues() {
			// TODO Auto-generated method stub
			childSemanticArea = "";
			parentSemanticArea = "";
		}

		@Override
		public boolean valuesExist() {
			boolean retVal = ((getReferencedComponentId() != null) && 
					(parentSemanticArea != null) && (parentSemanticArea.length() > 0) && 
					(childSemanticArea != null) && (childSemanticArea.length() > 0)); 
									
			if (!retVal)
			{
				StringBuffer str = new StringBuffer();
				str.append("\nError in adding to Semantic Hierarchy Refset");
				str.append("\nReferencedComponentId:" + getReferencedComponentId());
				str.append("\nparentSemanticArea:" + parentSemanticArea);
				str.append("\nchildSemanticArea:" + childSemanticArea);
				AceLog.getAppLog().log(Level.WARNING, "Failure in updating Semantic Area Hierarchy Refset for concept: " + str.toString());
			}
			
			return retVal;
		}
	}

	@Override
	public String fieldsToRefsetString() throws IOException {
		return "<properties>\n" +
				   	"<property>\n" +
				   		"<key>childSemanticArea</key>" +
				   		"<value>" + getChildSemanticArea() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
				   		"<key>parentSemanticArea</key>" +
				   		"<value>" + getParentSemanticArea() + "</value>" +
			   	"</property>" + 
		   	"</properties>"; 
	}
}
