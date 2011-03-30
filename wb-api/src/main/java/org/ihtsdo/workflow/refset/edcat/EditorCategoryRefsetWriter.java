package org.ihtsdo.workflow.refset.edcat;

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
public class EditorCategoryRefsetWriter extends WorkflowRefsetWriter 
{
	public EditorCategoryRefsetWriter() throws IOException, TerminologyException 
	{
		super(false);
		refset = new EditorCategoryRefset();
		fields = new EditorCategoryRSFields();

		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId(), true);
	}
	
	public void setReferencedComponentId(UUID uid) {
		((EditorCategoryRSFields)fields).setReferencedComponentUid(uid);
	}
	
	public void setEditor(I_GetConceptData user) {
		setReferencedComponentId(user.getPrimUuid());
	}

	public void setSemanticArea(String area) {
		((EditorCategoryRSFields)fields).setSemanticArea(area);
	}	
	
	public void setCategory(I_GetConceptData category) {
		((EditorCategoryRSFields)fields).setCategory(category);
	}
	
	public UUID getReferencedComponentUid() {
		return ((EditorCategoryRSFields)fields).getReferencedComponentId();
	}

	public I_GetConceptData getEditor() {
		try {
			return Terms.get().getConcept(getReferencedComponentUid());
		} catch (Exception e) {
	    	AceLog.getAppLog().log(Level.SEVERE, "Unable to get the Category (refCompId) from the EditorCategory Refset");
		}
		
		return null;
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

		private void setSemanticArea(String area) {
			semanticArea = area;
		}

		private void setCategory(I_GetConceptData category) {
			editorCategory = category;
		}
		
		public void setReferencedComponentUid(UUID uid) {
			try {
				setReferencedComponentId(uid);
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.SEVERE, "Unable to set WorkflowHistoryRefset's refCompId: " + uid);
			}
		}
		
		private void setEditor(I_GetConceptData editor) {
			setReferencedComponentUid(editor.getPrimUuid());
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
		
		public I_GetConceptData getEditor() {
			return getReferencedComponent();
		}
		
		private String getSemanticArea() {
			return semanticArea;
		}
		
		private I_GetConceptData getCategory() {
			return editorCategory;
		}
		
		public String toString() {
			try {
				return "\nReferenced Component Id(Editor SCT) = " + getReferencedComponent().getInitialText() + 
					   "(" + getReferencedComponent().getConceptNid() + ")" +
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
				AceLog.getAppLog().log(Level.WARNING, "Failure in updating Editor Category Refset for concept: " + str.toString());
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
