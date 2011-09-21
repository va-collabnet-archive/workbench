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
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
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
    public static TerminologyConstructorBI tsSnapshot;

    public enum BatchActionTaskType {

        PARENT_ADD_NEW,
        PARENT_REPLACE,
        PARENT_RETIRE,
        REFSET_ADD_MEMBER,
        REFSET_MOVE_MEMBER,
        REFSET_REPLACE_VALUE,
        REFSET_RETIRE_MEMBER,
        ROLE_ADD,
        ROLE_REPLACE_VALUE,
        ROLE_RETIRE,
        SIMPLE
    }

    public BatchActionTask() {
    }

    public abstract boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc)
            throws Exception;

    /**
     * Call once prior to execution of task to setup common values used to process all tasks.
     * @param tc
     * @throws IOException 
     */
    public static void setup(EditCoordinate ec, ViewCoordinate vc) throws IOException {
        RETIRED_NID = SnomedMetadataRfx.getSTATUS_RETIRED_NID();
        CURRENT_NID = SnomedMetadataRfx.getSTATUS_CURRENT_NID();
        ts = Ts.get();
        tsSnapshot = ts.getTerminologyConstructor(ec, vc);

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
