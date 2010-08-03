package org.ihtsdo.workflow.refset.edcat;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.RefsetFields;
import org.ihtsdo.workflow.refset.utilities.RefsetWriterUtility;



/* 
* @author Jesse Efron
* 
*/
public class EditorCategoryRefsetWriter extends RefsetWriterUtility {
	public EditorCategoryRefsetWriter() throws IOException, TerminologyException {
		EditorCategoryRefset refset = new EditorCategoryRefset();
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
		fields = new EditorCategoryRSFields();
	}
	
	public void setClinicalEditorField(I_GetConceptData user) {
			((EditorCategoryRSFields)fields).setClinicalEditorField(user);
	}

 	
	public void setSemanticArea(String area) {
			((EditorCategoryRSFields)fields).setSemanticArea(area);
	}
	
	
	public void setEditorCategory(I_GetConceptData category) {
			((EditorCategoryRSFields)fields).setEditorCategory(category);
	}
	
	public I_GetConceptData getClinicalEditorField() {
		return ((EditorCategoryRSFields)fields).getReferencedComponentId();
	}

	public I_GetConceptData getEditorCategory() {
		return ((EditorCategoryRSFields)fields).getEditorCategory();
	}

	public String getSemanticArea() {
		return ((EditorCategoryRSFields)fields).getSemanticArea();
	}

	public class EditorCategoryRSFields extends RefsetFields{
		public String semanticArea = null;
		public I_GetConceptData editorCategory = null;
		 		
		public EditorCategoryRSFields() {
			
		}
		
		public void setClinicalEditorField(I_GetConceptData user) {
			setReferencedComponentId(user);
		}
		
		public void setSemanticArea(String area) {
			semanticArea = area;
		}

		public void setEditorCategory(I_GetConceptData category) {
			editorCategory = category;
		}
		
		public String getSemanticArea() {
			return semanticArea;
		}
		
		public I_GetConceptData getEditorCategory() {
			return editorCategory;
		}
		
		public String toString() {
			try {
				return "\nReferenced Component Id(Editor SCT) = " + getReferencedComponentId().getInitialText() + 
					   "(" + getReferencedComponentId().getConceptId() + ")" +
					   "\nSemantic Area = " + semanticArea +
					   "\nEditor Category = " + editorCategory.getInitialText() +
					   "(" + editorCategory.getConceptId() + ")";
			} catch (IOException io) {
				return "Fai" +
						"led to identify referencedComponentId or editorCategory" + 
					   "\nError msg: " + io.getMessage();
			}
		}

		@Override
		public void cleanValues() {
			// TODO Auto-generated method stub
			setReferencedComponentId(null);
			semanticArea = "";
			editorCategory = null;
		}

		@Override
		public boolean valuesExist() {
			// TODO Auto-generated method stub
			return ((getReferencedComponentId() != null) && 
					(semanticArea != null) && (semanticArea.length() > 0) && 
					(editorCategory != null));
		}
	}

	public String fieldsToRefsetString() {
		return "<properties>\n" +
				   	"<property>\n" +
				   		"<key>editorCategory</key>" +
				   		"<value>" + getEditorCategory().getConceptId() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
			   		"<key>semanticArea</key>" +
			   		"<value>" + getSemanticArea() + "</value>" +
			   	"</property>" + 
		   	"</properties>"; 
	}
}
