package org.dwfa.ace.task;

public enum AttachmentKeys {
	I_GET_CONCEPT_DATA, ACE_FRAME_CONFIG, I_TERM_FACTORY,
	I_HOST_CONCEPT_PLUGINS, ACTIVE_CONCEPT, NEW_STATUS, REL_TYPE;
	
	public String getAttachmentKey() {
		return "A: " + this.name();
	}
}
