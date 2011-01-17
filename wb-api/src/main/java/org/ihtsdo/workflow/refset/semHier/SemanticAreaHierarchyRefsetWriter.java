package org.ihtsdo.workflow.refset.semHier;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.WorkflowRefsetFields;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetWriter;



/* 
* @author Jesse Efron
* 
*/
public class SemanticAreaHierarchyRefsetWriter extends WorkflowRefsetWriter {
	private final I_GetConceptData identifiedReferencedComponentId = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SEMANTIC_PARENT_REL.getUids());
	
	public SemanticAreaHierarchyRefsetWriter() throws IOException, TerminologyException {
		refset = new SemanticAreaHierarchyRefset();
		fields = new SemanticAreaHierarchyRSFields();

		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}
	
	public I_GetConceptData getReferencedComponentId() {
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
			setReferencedComponentId(identifiedReferencedComponentId );
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
				return "\nReferenced Component Id (Same for each row -- HardCoded) = " + getReferencedComponentId().getInitialText() + 
					   "(" + getReferencedComponentId().getConceptNid() + ")" +
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
			// TODO Auto-generated method stub
			return ((getReferencedComponentId() != null) && 
					(parentSemanticArea != null) && (parentSemanticArea.length() > 0) && 
					(childSemanticArea != null) && (childSemanticArea.length() > 0)); 
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
