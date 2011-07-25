package org.ihtsdo.workflow.refset.semHier;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.WorkflowRefset;
import org.ihtsdo.workflow.refset.WorkflowRefsetFields;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetWriter;



/* 
* @author Jesse Efron
* 
*/
public class SemanticHierarchyRefsetWriter extends WorkflowRefsetWriter {
	// Immutable refCompId
	private final I_GetConceptData identifiedReferencedComponent = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SEMANTIC_PARENT_REL.getUids());
	
	public SemanticHierarchyRefsetWriter() throws IOException, TerminologyException {
		super(semanticHierarchyConcept);
		fields = new SemanticAreaHierarchyRSFields();
	}
	
	public void setReferencedComponentId(UUID uid) {
		setReferencedComponentId(uid);
	}
		
	public UUID getReferencedComponentUid() {
		return getReferencedComponentUid();
	}

	public void setParentSemanticArea(String area) {
		((SemanticAreaHierarchyRSFields)fields).setParentSemanticArea(area);
	}

	public String getParentSemanticArea() {
		return ((SemanticAreaHierarchyRSFields)fields).getParentSemanticArea();
	}

	public void setChildSemanticArea(String area) {
		((SemanticAreaHierarchyRSFields)fields).setChildSemanticArea(area);
	}
	
	public String getChildSemanticArea() {
		return ((SemanticAreaHierarchyRSFields)fields).getChildSemanticArea();
	}

	
	
	private class SemanticAreaHierarchyRSFields extends WorkflowRefsetFields {
		private String childSemanticArea = null;
		private String parentSemanticArea = null;
		 		
		private SemanticAreaHierarchyRSFields() {
			// Immutable refCompId
			setReferencedComponentUid(identifiedReferencedComponent.getPrimUuid());
		}
		
		private void setParentSemanticArea(String area) {
			parentSemanticArea = area;
		}
		
		private String getParentSemanticArea() {
			return parentSemanticArea;
		}
		
		private void setChildSemanticArea(String area) {
			childSemanticArea = area;
		}

		private String getChildSemanticArea() {
			return childSemanticArea;
		}

		public String toString() {
			try {
				return "\nReferenced Component Id (Same for each row -- HardCoded) = " + getReferencedComponentUid() + 
					   "(" + getReferencedComponentUid() + ")" +
					   "\nChild Semantic Area = " + childSemanticArea +
					   "\nParentSemantic Area = " + parentSemanticArea;
			} catch (Exception io) {
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
			boolean retVal = ((getReferencedComponentUid() != null) && 
					(parentSemanticArea != null) && (parentSemanticArea.length() > 0) && 
					(childSemanticArea != null) && (childSemanticArea.length() > 0)); 
									
			if (!retVal)
			{
				StringBuffer str = new StringBuffer();
				str.append("\nError in adding to Semantic Hierarchy Refset");
				str.append("\nReferencedComponentId:" + getReferencedComponentUid());
				str.append("\nparentSemanticArea:" + parentSemanticArea);
				str.append("\nchildSemanticArea:" + childSemanticArea);
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
