package org.dwfa.ace.task;

public enum ProcessAttachmentKeys {
    /**
     * When a plugin process is executed from the concept panel, 
     * the concept contained within that panel is written to the process
     * panel using this property name. 
     */
    I_GET_CONCEPT_DATA,
    ACTIVE_CONCEPT, NEW_STATUS, REL_TYPE,
    DEFAULT_CONCEPT_LIST, REL_PARENT, DEFAULT_FILE, WORKING_PROFILE, USERNAME, PASSWORD, ADMIN_USERNAME, ADMIN_PASSWORD,
    HTML_STR, ACTIVE_CONCEPT_UUID, EDIT_PATH_CONCEPT, VIEW_PATH_CONCEPT, SELECTED_ADDRESSES, CONCEPT_UUID, UUID_LIST, DUP_UUID_L2,
    DETAIL_HTML_DIR, DETAIL_HTML_FILE, ACTIVE_SVN_ENTRY, DESTINATION_ADR, POT_DUP_UUID, DUP_REVIEW_PROCESS, ASSIGNEE, 
    ALT_ASSIGNEE, PROCESS_NAME, PROCESS_SUBJECT, SIGNPOST_HTML, UUID_LIST_FILENAME, UUID_LIST_LIST, 
    PROCESS_FILENAME, INSTRUCTION_FILENAME, BATCH_UUID_LIST2, TO_ASSIGN_PROCESS,
    HTML_DETAIL, HTML_INSTRUCTION, ROOT_CONCEPT, PARENT_PATH, FROM_PATH_CONCEPT, TO_PATH_CONCEPT, 
    ADDRESS_LIST, PATH_LIST_LIST, TERM_ENTRY_MAP, CON_STATUS_MAP, ELEM_STATUS_MAP, 
    MESSAGE, PROCESS_TO_LAUNCH, POSITION_LIST, STATUS_CONCEPT, SEARCH_TEST_ITEM;
    public String getAttachmentKey() {
        return "A: " + this.name();
    }
}
