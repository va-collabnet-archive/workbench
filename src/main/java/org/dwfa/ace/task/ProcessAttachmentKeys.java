package org.dwfa.ace.task;

public enum ProcessAttachmentKeys {
    /**
     * When a plugin process is executed from the concept panel,
     * the concept contained within that panel is written to the process
     * panel using this property name.
     */
    
    ACTIVE_CONCEPT_UUID, 
    ACTIVE_CONCEPT, 
    ACTIVE_SVN_ENTRY, 
    ADDRESS_LIST, 
    ADMIN_PASSWORD, 
    ADMIN_USERNAME, 
    ALT_ASSIGNEE, 
    ASSIGNEE, 
    BATCH_UUID_LIST2, 
    CASE_SENSITIVITY, 
    CON_CON_MAP, 
    CON_STATUS_MAP, 
    CONCEPT_UUID, 
    DEFAULT_CONCEPT_LIST, 
    DEFAULT_FILE, 
    DESTINATION_ADR, 
    DETAIL_HTML_DIR, 
    DETAIL_HTML_FILE, 
    DUP_REVIEW_PROCESS, 
    DUP_UUID_L2, 
    EDIT_PATH_CONCEPT, 
    ELEM_STATUS_MAP, 
    FIND_TEXT, 
    FROM_PATH_CONCEPT, 
    HTML_DETAIL, 
    HTML_INSTRUCTION, 
    HTML_STR, 
    I_GET_CONCEPT_DATA, 
    INSTRUCTION_FILENAME, 
    MESSAGE, 
    NEW_STATUS, 
    PARENT_PATH, 
    PASSWORD, 
    PATH_LIST_LIST, 
    POSITION_LIST, 
    POT_DUP_UUID, 
    PROCESS_FILENAME, 
    PROCESS_NAME, 
    PROCESS_SUBJECT, 
    PROCESS_TO_LAUNCH, 
    REL_PARENT, 
    REL_TYPE, 
    REPLACE_TEXT, 
    RETIRE_AS_STATUS,
    ROOT_CONCEPT, 
    SEARCH_ALL, 
    SEARCH_FSN, 
    SEARCH_PT, 
    SEARCH_SYNONYM, 
    SEARCH_TEST_ITEM, 
    SELECTED_ADDRESSES, 
    SIGNPOST_HTML, 
    STATUS_CONCEPT, 
    TERM_ENTRY_MAP, 
    TO_ASSIGN_PROCESS, 
    TO_PATH_CONCEPT, 
    USERNAME, 
    UUID_LIST_FILENAME, 
    UUID_LIST_LIST, 
    UUID_LIST, 
    VIEW_PATH_CONCEPT, 
    WORKING_DIR, 
    WORKING_PROFILE, 
    WORKING_REFSET;

    
    public String getAttachmentKey() {
        return "A: " + this.name();
    }
}
