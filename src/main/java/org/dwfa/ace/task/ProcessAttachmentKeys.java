package org.dwfa.ace.task;

public enum ProcessAttachmentKeys {
    I_GET_CONCEPT_DATA,
    ACTIVE_CONCEPT, NEW_STATUS, REL_TYPE,
    DEFAULT_CONCEPT_LIST, REL_PARENT, DEFAULT_FILE,
    HTML_STR, ACTIVE_CONCEPT_UUID;

    public String getAttachmentKey() {
        return "A: " + this.name();
    }
}
