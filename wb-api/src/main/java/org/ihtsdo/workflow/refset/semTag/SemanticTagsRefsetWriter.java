package org.ihtsdo.workflow.refset.semTag;

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
public class SemanticTagsRefsetWriter extends WorkflowRefsetWriter 
{
	// Immutable refCompId
	public SemanticTagsRefsetWriter() throws IOException, TerminologyException {
		super(semanticTagConcept);
		fields = new SemanticTagRSFields();
	}
	
	public void setReferencedComponentId(UUID uid) {
		fields.setReferencedComponentUid(uid);
	}
	
	public UUID getReferencedComponentUid() {
		return fields.getReferencedComponentUid();
	}

	public void setSemanticTag(String semTag) {
		((SemanticTagRSFields)fields).setSemanticTag(semTag);
	}
	
	public String getSemanticTag() {
		return ((SemanticTagRSFields)fields).getSemanticTag();
	}

	public void setSemanticTagUUID(String uid) {
		((SemanticTagRSFields)fields).setUid(uid);
	}
	
	public String getUid() {
		return ((SemanticTagRSFields)fields).getUid();
	}
	
	

	
	
	
	private class SemanticTagRSFields extends WorkflowRefsetFields {
		private String semanticTag = null;
		private String uid = null;
		 		
		private SemanticTagRSFields() {
			// Immutable refCompId
			try {
				setReferencedComponentUid(ArchitectonicAuxiliary.Concept.SEMTAG_UUID_REL.getPrimoridalUid());
			} catch (Exception e) {
				
			}
		}
		
		private void setSemanticTag(String semTag) {
			semanticTag = semTag;
		}
		
		public String getSemanticTag() {
			return semanticTag;
		}

		private void setUid(String uid) {
			this.uid = uid;
		}

		public String getUid() {
			return uid;
		}

		public String toString() {
			return  "\nSemantic Tag: " + semanticTag + 
					"\nUUID: " + uid;
		}

		@Override
		public void cleanValues() {
			semanticTag = null;
			uid = null;
		}

		@Override
		public boolean valuesExist() {
			boolean retVal = ((getReferencedComponentUid() != null) && 
							  (semanticTag != null) &&
							  (uid != null));
									
			if (!retVal)
			{
				StringBuffer str = new StringBuffer();
				str.append("\nError in adding to Semantic Area Search Refset");
				str.append("\nReferencedComponentId:" + getReferencedComponentUid());
				str.append("\nsearchTerm:" + semanticTag);
	            AceLog.getAppLog().log(Level.WARNING, "Failed to add semantic tag member with values: " + str.toString());
			}
			
			return retVal;
		}
	}

	@Override
	public String fieldsToRefsetString() throws IOException {
		return "<properties>\n" +
				   	"<property>\n" +
				   		"<key>semanticTag</key>" +
				   		"<value>" + getSemanticTag() + "</value>" +
				   	"</property>" + 
				   	"<property>" +
				   		"<key>uuid</key>" +
				   		"<value>" + getUid() + "</value>" +
				   	"</property>" + 
				"</properties>";
	}
}
