package org.ihtsdo.workflow.refset.edcat;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
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
		super(editorCategoryConcept);
		fields = new EditorCategoryRSFields();
	}
	
	public void setReferencedComponentId(UUID uid) {
		((EditorCategoryRSFields)fields).setEditor(uid);
	}
	
	public void setEditor(ConceptVersionBI user) {
		setReferencedComponentId(user.getPrimUuid());
	}

	public void setEditor(UUID uid) {
		setReferencedComponentId(uid);
	}

	public UUID getReferencedComponentUid() {
		return ((EditorCategoryRSFields)fields).getEditor();
	}

	public UUID getEditorUid() {
		return getReferencedComponentUid();
	}
	
	public String getSemanticArea() {
		return ((EditorCategoryRSFields)fields).getSemanticArea();
	}
	
	public void setSemanticArea(String area) {
		((EditorCategoryRSFields)fields).setSemanticArea(area);
	}	
	
	public UUID getCategoryUid() {
		return ((EditorCategoryRSFields)fields).getCategory();
	}

	public void setCategory(ConceptVersionBI category) {
		((EditorCategoryRSFields)fields).setCategory(category.getPrimUuid());
	}

	public void setCategory(UUID uid) {
		((EditorCategoryRSFields)fields).setCategory(uid);
	}

	
	
	public class EditorCategoryRSFields extends WorkflowRefsetFields
	{
		private String semanticArea = null;
		private UUID editorCategory = null;

		private void setEditor(UUID editor) {
			setReferencedComponentUid(editor);
		}
				
		public UUID getEditor() {
			return getReferencedComponentUid();
		}

		private void setSemanticArea(String area) {
			semanticArea = area;
		}
		
		private String getSemanticArea() {
			return semanticArea;
		}
		
		private void setCategory(UUID uid) {
			editorCategory = uid;
		}
		
		private UUID getCategory() {
			return editorCategory;
		}
		
		public String toString() {
			try {
				return "\nReferenced Component Id(Editor SCT) = " + Terms.get().getConcept(getReferencedComponentUid()).getInitialText() + 
					   "(" + getReferencedComponentUid() + ")" +
					   "\nSemantic Area = " + semanticArea +
					   "\nEditor Category = " + Terms.get().getConcept(editorCategory).getInitialText() +
					   "(" + editorCategory + ")";
			} catch (Exception io) {
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
			boolean retVal =  ((getReferencedComponentUid() != null) && 
					(semanticArea != null) && (semanticArea.length() > 0) && 
					(editorCategory != null));
			
			if (!retVal)
			{
				StringBuffer str = new StringBuffer();
				str.append("\nError in adding to Editor Category Refset");
				str.append("\nReferencedComponentId:" + getReferencedComponentUid());
				str.append("\nsemanticArea:" + semanticArea);
				str.append("\neditorCategory:" + editorCategory);
			}
			
			return retVal;
		}
	}

	@Override
	public String fieldsToRefsetString() throws IOException {
		return "<properties>\n" +
				   	"<property>\n" +
				   		"<key>editorCategory</key>" +
				   		"<value>" + getCategoryUid() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
			   		"<key>semanticArea</key>" +
			   		"<value>" + getSemanticArea() + "</value>" +
			   	"</property>" + 
		   	"</properties>"; 
	}
}
