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
package org.ihtsdo.tk.example.binding;

import java.io.IOException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;

/**
 * @deprecated  see package org.ihtsdo.tk.binding.snomed
 * @author marc
 */
public class SnomedMetadataRfx {

    private static boolean isReleaseFormatSetupB = false;
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

    public static int getCURRENT_NID() throws Exception {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return CURRENT_NID;
    }

        public static int getCh_DEFINING_CHARACTERISTIC_NID() throws Exception {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return Ch_DEFINING_CHARACTERISTIC_NID;
    }

    public static int getCh_STATED_RELATIONSHIP_NID() throws Exception {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return CH_STATED_RELATIONSHIP_NID;
    }

    public static int getCh_INFERRED_RELATIONSHIP_NID() throws Exception {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return Ch_INFERRED_RELATIONSHIP_NID;
    }

    public static int getLIMITED_NID() throws Exception {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return LIMITED_NID;
    }

    public static int getMANDATORY_REFINABILITY_NID() throws Exception {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return MANDATORY_REFINABILITY_NID;
    }

    public static int getNOT_REFINABLE_NID() throws Exception {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return NOT_REFINABLE_NID;
    }

    public static int getOPTIONAL_REFINABILITY_NID() throws Exception {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return OPTIONAL_REFINABILITY_NID;
    }

    public static int getRETIRED_NID() throws Exception {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return RETIRED_NID;
    }

    public static int getReleaseFormat() throws Exception {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return releaseFormat;
    }

    public static int getFULL_SPECIFIED_NAME_NID() throws Exception {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return FULL_SPECIFIED_NAME_NID;
    }

    public static int getSYNONYM_PREFERRED_NAME_NID() throws Exception {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return SYNONYM_PREFERRED_NAME_NID;
    }

    private static void setupSnoRf1Rf2() throws Exception {
        TerminologyStoreDI tf = Ts.get();

        if (tf.usesRf2Metadata()) {
            isReleaseFormatSetupB = true;
            releaseFormat = 2;
        } else {
            isReleaseFormatSetupB = true;
            releaseFormat = 1;
        }

        if (releaseFormat == 1) {
            // 0 CURRENT, 1 RETIRED
            CURRENT_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.CURRENT_RF1.getUuids());
            LIMITED_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getUuids());
            RETIRED_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getUuids());
            OPTIONAL_REFINABILITY_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.OPTIONAL_REFINABILITY_TYPE_RF1.getUuids());
            NOT_REFINABLE_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.NOT_REFINABLE_REFINABILITY_TYPE_RF1.getUuids());
            MANDATORY_REFINABILITY_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.MANDATORY_REFINABILITY_TYPE_RF1.getUuids());
            CH_STATED_RELATIONSHIP_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.STATED_DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids());
            Ch_INFERRED_RELATIONSHIP_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids());
            Ch_DEFINING_CHARACTERISTIC_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids());

        } else if (releaseFormat == 2) {
            CURRENT_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids());
            LIMITED_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getUuids());
            RETIRED_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.INACTIVE_VALUE_RF2.getUuids());
            OPTIONAL_REFINABILITY_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids());
            NOT_REFINABLE_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.NOT_REFINABLE_RF2.getUuids());
            MANDATORY_REFINABILITY_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2.getUuids());
            CH_STATED_RELATIONSHIP_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids());
            Ch_INFERRED_RELATIONSHIP_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getUuids());
            Ch_DEFINING_CHARACTERISTIC_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.DEFINING_RELATIONSHIP_RF2.getUuids());
        } else {
            throw new IOException("SnomedMetadataRfx releaseFormat must equal 1 or 2.");
        }
    }
}
