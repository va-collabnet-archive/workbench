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
package org.ihtsdo.batch;

import java.io.IOException;
import java.util.UUID;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.binding.snomed.HistoricalRelType;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.spec.ConceptSpec;

public abstract class BatchActionTask {

    public static int RETIRED_NID;
    public static int CURRENT_NID;
    public static I_IntSet HISTORIC_ROLE_TYPES = null;
    public static TerminologyStoreDI ts;
    public static TerminologyBuilderBI tsSnapshot;
    public static I_ConfigAceFrame config;

    public enum BatchActionTaskType {

        CONCEPT_PARENT_ADD_NEW,
        CONCEPT_PARENT_REPLACE,
        CONCEPT_PARENT_RETIRE,
        CONCEPT_REFSET_ADD_MEMBER,
        CONCEPT_REFSET_MOVE_MEMBER,
        CONCEPT_REFSET_REPLACE_VALUE,
        CONCEPT_REFSET_RETIRE_MEMBER,
        DESCRIPTION_INITIAL_CHAR_CASE_SENSITIVITY,
        DESCRIPTION_RETIRE,
        DESCRIPTION_TEXT_FIND_REPLACE,
        DESCRIPTION_TEXT_FIND_CREATE,
        DESCRIPTION_REFSET_ADD_MEMBER,
        DESCRIPTION_REFSET_CHANGE_VALUE,
        DESCRIPTION_REFSET_RETIRE_MEMBER,
        RELATIONSHIP_ROLE_ADD,
        RELATIONSHIP_ROLE_REPLACE_VALUE,
        RELATIONSHIP_ROLE_RETIRE,
        LOGIC_DISJOINT_SET_ADD, // :SNOOWL:ADD:
        LOGIC_DISJOINT_SET_RETIRE, // :SNOOWL:ADD:
        LOGIC_NEGATE_RELATIONSHIP_VALUE, // :SNOOWL:ADD:
        LOGIC_UNION_SET_CREATE, // :SNOOWL:ADD:
        LOGIC_UNION_SET_RETIRE, // :SNOOWL:ADD:
        SIMPLE
    }

    public BatchActionTask() {
    }

    public abstract boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc)
            throws Exception;

    /**
     * Call once prior to execution of task to setup common values used to process all tasks.
     * @param ec
     * @param vc
     * @param aceConfig
     * @throws IOException 
     */
    public static void setup(EditCoordinate ec, ViewCoordinate vc, I_ConfigAceFrame aceConfig) throws IOException {
        RETIRED_NID = SnomedMetadataRfx.getSTATUS_RETIRED_NID();
        CURRENT_NID = SnomedMetadataRfx.getSTATUS_CURRENT_NID();
        ts = Ts.get();
        tsSnapshot = ts.getTerminologyBuilder(ec, vc);
        config = aceConfig;

        // SETUP HISTORIC ROLE TYPES
        ConceptSpec[] historicalTypes = HistoricalRelType.getHistoricalTypes();
        HISTORIC_ROLE_TYPES = Terms.get().newIntSet();
        for (ConceptSpec conceptSpec : historicalTypes) {
            HISTORIC_ROLE_TYPES.add(conceptSpec.getStrict(vc).getNid());
        }
    }

    public static String nidToName(int nid) throws IOException {
        return Ts.get().getComponent(nid).toUserString();
    }

    public static String uuidToName(UUID uuid) throws IOException {
        return Ts.get().getComponent(uuid).toUserString();
    }
}
