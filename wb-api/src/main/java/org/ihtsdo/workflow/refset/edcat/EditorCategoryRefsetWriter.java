package org.ihtsdo.workflow.refset.edcat;

import java.io.IOException;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.WorkflowRefsetFields;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetWriter;



/* 
* @author Jesse Efron
* 
*/
public class EditorCategoryRefsetWriter extends WorkflowRefsetWriter 
{
	public EditorCategoryRefsetWriter() throws IOException, TerminologyException 
	{
		refset = new EditorCategoryRefset();
		fields = new EditorCategoryRSFields();

		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}
	
	public I_GetConceptData getReferencedComponentId() {
		return getEditor();
	}

	public void setEditor(I_GetConceptData editor) {
		((EditorCategoryRSFields)fields).setEditor(editor);
	}
 	
	public void setSemanticArea(String area) {
		((EditorCategoryRSFields)fields).setSemanticArea(area);
	}	
	
	public void setCategory(I_GetConceptData category) {
		((EditorCategoryRSFields)fields).setCategory(category);
	}
	
	public I_GetConceptData getEditor() {
		return ((EditorCategoryRSFields)fields).getEditor();
	}

	public I_GetConceptData getCategory() {
		return ((EditorCategoryRSFields)fields).getCategory();
	}

	public String getSemanticArea() {
		return ((EditorCategoryRSFields)fields).getSemanticArea();
	}

	public class EditorCategoryRSFields extends WorkflowRefsetFields
	{
		private String semanticArea = null;
		private I_GetConceptData editorCategory = null;

		private void setEditor(I_GetConceptData user) {
			setReferencedComponentId(user);
		}
		
		private void setSemanticArea(String area) {
			semanticArea = area;
		}

		private void setCategory(I_GetConceptData category) {
			editorCategory = category;
		}
		
		private I_GetConceptData getEditor() {
			return getReferencedComponentId();
		}
		
		private String getSemanticArea() {
			return semanticArea;
		}
		
		private I_GetConceptData getCategory() {
			return editorCategory;
		}
		
		public String toString() {
			try {
				return "\nReferenced Component Id(Editor SCT) = " + getReferencedComponentId().getInitialText() + 
					   "(" + getReferencedComponentId().getConceptNid() + ")" +
					   "\nSemantic Area = " + semanticArea +
					   "\nEditor Category = " + editorCategory.getInitialText() +
					   "(" + editorCategory.getConceptNid() + ")";
			} catch (IOException io) {
				return "Failed to identify referencedComponentId or editorCategory" + 
					   "\nError msg: " + io.getMessage();
			}
		}

		@Override
		public void cleanValues() {
			setReferencedComponentId(null);
			semanticArea = "";
			editorCategory = null;
		}

		@Override
		public boolean valuesExist() {
			boolean retVal =  ((getReferencedComponentId() != null) && 
					(semanticArea != null) && (semanticArea.length() > 0) && 
					(editorCategory != null));
			
			if (!retVal)
			{
				StringBuffer str = new StringBuffer();
				str.append("\nError in adding to Editor Category Refset");
				str.append("\nReferencedComponentId:" + getReferencedComponentId());
				str.append("\nsemanticArea:" + semanticArea);
				str.append("\neditorCategory:" + editorCategory);
	        	AceLog.getAppLog().alertAndLog(Level.SEVERE, str.toString(), new Exception("Failure in updating Editor Category Refset"));
			}
			
			return retVal;
		}
	}

	@Override
	public String fieldsToRefsetString() throws IOException {
		return "<properties>\n" +
				   	"<property>\n" +
				   		"<key>editorCategory</key>" +
				   		"<value>" + getCategory().getPrimUuid() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
			   		"<key>semanticArea</key>" +
			   		"<value>" + getSemanticArea() + "</value>" +
			   	"</property>" + 
		   	"</properties>"; 
	}
}
