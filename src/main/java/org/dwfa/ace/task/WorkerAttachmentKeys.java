package org.dwfa.ace.task;

public enum WorkerAttachmentKeys {
	ACE_FRAME_CONFIG,
	I_HOST_CONCEPT_PLUGINS;
	
	public String getAttachmentKey() {
		return "A: " + this.name();
	}
}
