package org.ihtsdo.workflow.refset.semhier;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.RefsetFields;
import org.ihtsdo.workflow.refset.utilities.RefsetWriterUtility;



/* 
* @author Jesse Efron
* 
*/
public class SemanticAreaHierarchyRefsetWriter extends RefsetWriterUtility {
	private final I_GetConceptData identifiedReferencedComponentId = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SEMANTIC_PARENT_REL.getUids());
	
	public SemanticAreaHierarchyRefsetWriter() throws IOException, TerminologyException {
		SemanticAreaHierarchyRefset refset = new SemanticAreaHierarchyRefset();
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
		fields = new SemanticAreaHierarchyRSFields();
	}
	
	public void setParentSemanticArea(String area) {
		((SemanticAreaHierarchyRSFields)((SemanticAreaHierarchyRSFields)fields)).setParentSemanticArea(area);
}
	
	public void setChildSemanticArea(String area) {
			((SemanticAreaHierarchyRSFields)fields).setChildSemanticArea(area);
	}
	
	public I_GetConceptData getClinicalEditorField() {
		return ((SemanticAreaHierarchyRSFields)fields).getReferencedComponentId();
	}

	public String getChildSemanticArea() {
		return ((SemanticAreaHierarchyRSFields)fields).childSemanticArea;
	}

	public String getParentSemanticArea() {
		return ((SemanticAreaHierarchyRSFields)fields).parentSemanticArea;
	}

	private class SemanticAreaHierarchyRSFields extends RefsetFields{
		private String childSemanticArea = null;
		private String parentSemanticArea = null;
		 		
		private SemanticAreaHierarchyRSFields() {
			setReferencedComponentId(identifiedReferencedComponentId );
		}
		
		private void setChildSemanticArea(String area) {
			childSemanticArea = area;
		}

		private void setParentSemanticArea(String area) {
			parentSemanticArea = area;
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

	public String fieldsToRefsetString() {
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
