/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.snomed.release;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.example.binding.SnomedMetadataRf2;

/**
 *
 * @author marc
 */
public class Rfx {

    private static int releaseFormat = 0;
    // NIDs
    private static int CURRENT_NID;
    private static int LIMITED_NID;
    private static int RETIRED_NID;
    private static int OPTIONAL_REFINABILITY_NID;
    private static int NOT_REFINABLE_NID;
    private static int MANDATORY_REFINABILITY_NID;
    private static int CH_STATED_RELATIONSHIP_NID;
    private static int Ch_INFERRED_RELATIONSHIP_NID;
    private static int Ch_DEFINING_CHARACTERISTIC_NID;

    private static int FULL_SPECIFIED_NAME_NID;
    private static int SYNONYM_PREFERRED_NAME_NID;
    // CONCEPT BEANS
    private static I_GetConceptData FULL_SPECIFIED_NAME_CB;
    private static I_GetConceptData SYNONYM_PREFERRED_NAME_CB;

    public static int getIsCURRENT() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return CURRENT_NID;
    }

    public static int getIsCh_DEFINING_CHARACTERISTIC() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return Ch_DEFINING_CHARACTERISTIC_NID;
    }

    public static int getIsCh_STATED_RELATIONSHIP() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return CH_STATED_RELATIONSHIP_NID;
    }

    public static int getIsCh_INFERRED_RELATIONSHIP() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return Ch_INFERRED_RELATIONSHIP_NID;
    }

    public static int getIsLIMITED() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return LIMITED_NID;
    }

    public static int getIsMANDATORY_REFINABILITY() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return MANDATORY_REFINABILITY_NID;
    }

    public static int getIsNOT_REFINABLE() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return NOT_REFINABLE_NID;
    }

    public static int getIsOPTIONAL_REFINABILITY() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return OPTIONAL_REFINABILITY_NID;
    }

    public static int getIsRETIRED() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return RETIRED_NID;
    }

    public static int getReleaseFormat() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return releaseFormat;
    }

    public static I_GetConceptData getFULL_SPECIFIED_NAME_CB() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return FULL_SPECIFIED_NAME_CB;
    }

    public static int getFULL_SPECIFIED_NAME_NID() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return FULL_SPECIFIED_NAME_NID;
    }

    public static I_GetConceptData getSYNONYM_PREFERRED_NAME_CB() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return SYNONYM_PREFERRED_NAME_CB;
    }

    public static int getSYNONYM_PREFERRED_NAME_NID() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return SYNONYM_PREFERRED_NAME_NID;
    }

    public static void setupSnoRf1Rf2() throws TerminologyException, IOException {
        I_TermFactory tf = Terms.get();

        // Determine if RF1 or RF2 release
        int rootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());
        I_GetConceptData rootCB = tf.getConcept(rootNid);
        int rootStatusNid = rootCB.getConAttrs().getVersions().iterator().next().getStatusNid();
        UUID rootStatusUuid = tf.nidToUuid(rootStatusNid);
        if (rootStatusUuid.compareTo(UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66")) == 0) {
            // RF1 'current' 2faa9261-8fb2-11db-b606-0800200c9a66
            releaseFormat = 1;
        } else if (rootStatusUuid.compareTo(UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f")) == 0) {
            // RF2 'Active value' d12702ee-c37f-385f-a070-61d56d4d0f1f
            releaseFormat = 2;
        } else {
            String errStr = "Rfx.setupSnoRf1Rf2() cannot determine RF1 vs. RF2 data set";
            TerminologyException ex = new TerminologyException(errStr);
            AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, ex);
            throw ex;
        }

        if (releaseFormat == 1) {
            // 0 CURRENT, 1 RETIRED
            CURRENT_NID = tf.uuidToNative(
                    ArchitectonicAuxiliary.Concept.CURRENT.getUids());
            LIMITED_NID = tf.uuidToNative(
                    ArchitectonicAuxiliary.Concept.LIMITED.getUids());
            RETIRED_NID = tf.uuidToNative(
                    ArchitectonicAuxiliary.Concept.RETIRED.getUids());
            OPTIONAL_REFINABILITY_NID = tf.uuidToNative(
                    ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            NOT_REFINABLE_NID = tf.uuidToNative(
                    ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());
            MANDATORY_REFINABILITY_NID = tf.uuidToNative(
                    ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY.getUids());
            CH_STATED_RELATIONSHIP_NID = tf.uuidToNative(
                    ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
            Ch_INFERRED_RELATIONSHIP_NID = tf.uuidToNative(
                    ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.getUids());
            Ch_DEFINING_CHARACTERISTIC_NID = tf.uuidToNative(
                    ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
        } else if (releaseFormat == 2) {
            CURRENT_NID = tf.uuidToNative(
                    SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids());
            LIMITED_NID = tf.uuidToNative(
                    SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getUuids());
            RETIRED_NID = tf.uuidToNative(
                    SnomedMetadataRf2.INACTIVE_VALUE_RF2.getUuids());
            OPTIONAL_REFINABILITY_NID = tf.uuidToNative(
                    SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids());
            NOT_REFINABLE_NID = tf.uuidToNative(
                    SnomedMetadataRf2.NOT_REFINABLE_RF2.getUuids());
            MANDATORY_REFINABILITY_NID = tf.uuidToNative(
                    SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2.getUuids());
            CH_STATED_RELATIONSHIP_NID = tf.uuidToNative(
                    SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids());
            Ch_INFERRED_RELATIONSHIP_NID = tf.uuidToNative(
                    SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getUuids());
            Ch_DEFINING_CHARACTERISTIC_NID = tf.uuidToNative(
                    SnomedMetadataRf2.DEFINING_RELATIONSHIP_RF2.getUuids());
        } else {
            String errStr = "releaseFormat must equal 1 or 2";
            TerminologyException ex = new TerminologyException(errStr);
            AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, ex);
            throw ex;
        }
        System.out.println(":!!!:DEBUG: Rfx.releaseFormat: " + releaseFormat);
    }
}
