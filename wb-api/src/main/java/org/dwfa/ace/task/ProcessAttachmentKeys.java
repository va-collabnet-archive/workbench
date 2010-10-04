/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    NEW_CONCEPT,
    NEW_STATUS,
    PARENT_PATH,
    PASSWORD,
    PATH_LIST_LIST,
    POSITION_LIST,
    POSITION_SET,
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
    FULLNAME,
    UUID_LIST_FILENAME,
    UUID_LIST_LIST,
    UUID_LIST,
    VIEW_PATH_CONCEPT,
    WORKING_DIR,
    WORKING_PROFILE,
    WORKING_REFSET,
    FILE_ATTACHMENTS,
    REQUESTOR,
    REFSET_UUID,
    OUTPUT_FILE,
    PATH_UUID,
    CURRENT_PROFILE,
    COMMIT_PROFILE,
    STATUS_UUID,
    OWNER_UUID,
    REVIEWER_UUID,
    EDITOR_UUID,
    NEXT_USER,
    REFSET_SPEC_UUID,
    ERRORS_AND_WARNINGS,
    PROMOTION_UUID,
    BP_STRING,
    REFSET_MEMBER_UUID,
    CONCEPT_TO_REPLACE_UUID,
    REFSET_VERSION,
    SNOMED_VERSION,
    OWNER_INBOX,
    OWNER_COMMENTS,
    EDITOR_INBOX,
    EDITOR_COMMENTS,
    CHANGES_LIST,
    REVIEW_COUNT,
    REVIEW_INDEX,
    REVIEWER_INBOX,
    REVIEWER_COMMENTS,
    ACTIVE_DESCRIPTION_UUID,
    REFSET_COMPUTE_TYPE_UUID,
    ISSUE_REPO_CONCEPT,
    ISSUE_REPO_PROP_NAME,
    ISSUE_REPO_PROP_PASSWORD,
    REFSET_NAME, 
    EXTRA_CHANGE_SET_GENERATOR_LIST,
    ;

    public String getAttachmentKey() {
        return "A: " + this.name();
    }
}
