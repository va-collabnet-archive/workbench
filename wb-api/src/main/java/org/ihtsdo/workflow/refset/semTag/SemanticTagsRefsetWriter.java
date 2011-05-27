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
	private final I_GetConceptData identifiedReferencedComponent = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SEMTAG_UUID_REL.getUids());

	public SemanticTagsRefsetWriter() throws IOException, TerminologyException {
		refset = new SemanticTagsRefset();
		fields = new SemanticTagRSFields();
	
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}
	
	public void setReferencedComponentId(UUID uid) {
		((SemanticTagRSFields)fields).setReferencedComponentUid(uid);
	}
	
	public UUID getReferencedComponentUid() {
		return ((SemanticTagRSFields)fields).getReferencedComponentId();
	}

	public void setSemanticTag(String semTag) {
		((SemanticTagRSFields)fields).setSemanticTag(semTag);
	}
	
	public void setUUID(String uid) {
		((SemanticTagRSFields)fields).setUid(uid);
	}
	
	public String getSemanticTag() {
		return ((SemanticTagRSFields)fields).getSemanticTag();
	}

	public String getUid() {
		return ((SemanticTagRSFields)fields).getUid();
	}
	
	

	
	
	
	private class SemanticTagRSFields extends WorkflowRefsetFields {
		private String semanticTag = null;
		private String uid = null;
		 		
		private SemanticTagRSFields() {
			setReferencedComponentId(identifiedReferencedComponent.getPrimUuid());
		}
		
		public void setReferencedComponentUid(UUID uid) {
			try {
				setReferencedComponentId(uid);
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.SEVERE, "Unable to set WorkflowHistoryRefset's refCompId: " + uid + " with error: " + e.getMessage());
			}
		}

		public I_GetConceptData getReferencedComponent() {
			try {
				return Terms.get().getConcept(getReferencedComponentId());
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.SEVERE, "Unable to set WorkflowHistoryRefset's refCompId with error: " + e.getMessage());
			}
			
			return null;
		}
		
		
		private void setSemanticTag(String semTag) {
			semanticTag = semTag;
		}
		
		private void setUid(String uid) {
			this.uid = uid;
		}

		public String getSemanticTag() {
			return semanticTag;
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
			boolean retVal = ((getReferencedComponentId() != null) && 
							  (semanticTag != null) &&
							  (uid != null));
									
			if (!retVal)
			{
				StringBuffer str = new StringBuffer();
				str.append("\nError in adding to Semantic Area Search Refset");
				str.append("\nReferencedComponentId:" + getReferencedComponent().getPrimUuid());
				str.append("\nsearchTerm:" + semanticTag);
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
