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
package org.dwfa.ace.task.classify;

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
    private static int isCURRENT;
    private static int isLIMITED;
    private static int isRETIRED;
    private static int isOPTIONAL_REFINABILITY;
    private static int isNOT_REFINABLE;
    private static int isMANDATORY_REFINABILITY;
    private static int isCh_STATED_RELATIONSHIP;
    private static int isCh_INFERRED_RELATIONSHIP;
    private static int isCh_DEFINING_CHARACTERISTIC;

    public static int getIsCURRENT() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return isCURRENT;
    }

    public static int getIsCh_DEFINING_CHARACTERISTIC() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return isCh_DEFINING_CHARACTERISTIC;
    }

    public static int getIsCh_STATED_RELATIONSHIP() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return isCh_STATED_RELATIONSHIP;
    }

    public static int getIsCh_INFERRED_RELATIONSHIP() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return isCh_INFERRED_RELATIONSHIP;
    }

    public static int getIsLIMITED() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return isLIMITED;
    }

    public static int getIsMANDATORY_REFINABILITY() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return isMANDATORY_REFINABILITY;
    }

    public static int getIsNOT_REFINABLE() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return isNOT_REFINABLE;
    }

    public static int getIsOPTIONAL_REFINABILITY() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return isOPTIONAL_REFINABILITY;
    }

    public static int getIsRETIRED() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return isRETIRED;
    }

    public static int getReleaseFormat() throws TerminologyException, IOException {
        if (releaseFormat != 1 && releaseFormat != 2) {
            setupSnoRf1Rf2();
        }
        return releaseFormat;
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
            isCURRENT = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
            isLIMITED = tf.uuidToNative(ArchitectonicAuxiliary.Concept.LIMITED.getUids());
            isRETIRED = tf.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
            isOPTIONAL_REFINABILITY = tf.uuidToNative(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            isNOT_REFINABLE = tf.uuidToNative(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());
            isMANDATORY_REFINABILITY = tf.uuidToNative(ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY.getUids());
            isCh_STATED_RELATIONSHIP = tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
            isCh_INFERRED_RELATIONSHIP = tf.uuidToNative(ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.getUids());
            isCh_DEFINING_CHARACTERISTIC = tf.uuidToNative(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
        } else if (releaseFormat == 2) {
            isCURRENT = tf.uuidToNative(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids());
            isLIMITED = tf.uuidToNative(SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getUuids());
            isRETIRED = tf.uuidToNative(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getUuids());
            isOPTIONAL_REFINABILITY = tf.uuidToNative(SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids());
            isNOT_REFINABLE = tf.uuidToNative(SnomedMetadataRf2.NOT_REFINABLE_RF2.getUuids());
            isMANDATORY_REFINABILITY = tf.uuidToNative(SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2.getUuids());
            isCh_STATED_RELATIONSHIP = tf.uuidToNative(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids());
            isCh_INFERRED_RELATIONSHIP = tf.uuidToNative(SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getUuids());
            isCh_DEFINING_CHARACTERISTIC = tf.uuidToNative(SnomedMetadataRf2.DEFINING_RELATIONSHIP_RF2.getUuids());
        } else {
            String errStr = "releaseFormat must equal 1 or 2";
            TerminologyException ex = new TerminologyException(errStr);
            AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, ex);
            throw ex;
        }
        System.out.println(":!!!:DEBUG: Rfx.releaseFormat: " + releaseFormat);
    }
}
