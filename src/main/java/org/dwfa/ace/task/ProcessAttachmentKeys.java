package org.dwfa.ace.task;

public enum ProcessAttachmentKeys {
    I_GET_CONCEPT_DATA,
    ACTIVE_CONCEPT, NEW_STATUS, REL_TYPE,
    DEFAULT_CONCEPT_LIST, REL_PARENT, DEFAULT_FILE, WORKING_PROFILE, USERNAME, PASSWORD, ADMIN_USERNAME, ADMIN_PASSWORD,
    HTML_STR, ACTIVE_CONCEPT_UUID,SELECTED_ADDRESSES;
    
    public String getAttachmentKey() {
        return "A: " + this.name();
    }
}
