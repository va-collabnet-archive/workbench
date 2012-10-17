/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.binding.snomed;

import java.io.IOException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * The Class SnomedMetadataRfx contains methods which retrieve either the
 * Release Format 1 (RF1) or Release Format 2 (RF2) metatdata values for a
 * metatdata concept. The database is checked when first opened to determine
 * which style of metatdata is used. The corresponding style of metatdata will
 * be returned by these methods.
 */
public class SnomedMetadataRfx {

    /**
     * The boolean representing if the release format is RF2 or not.
     */
    private static boolean isReleaseFormatSetupB = false;
    /**
     * The release format type. Will be set to 1 for RF1 or 2 for RF2. 0
     * indicated the test for which type of metadata has not yet been performed.
     */
    private static int releaseFormat = 0;
    // --- NIDs ---
    // DESCRIPTION NIDs
    /**
     * The fully specified name concept nid.
     */
    private static int DES_FULL_SPECIFIED_NAME_NID;
    /**
     * The synonym preferred name concept nid.
     */
    private static int DES_SYNONYM_PREFERRED_NAME_NID;
    /**
     * The the synonym concept nid.
     */
    private static int DES_SYNONYM_NID;
    /**
     * The acceptable concept nid.
     */
    private static int ACCEPTABLE_NID;
    /**
     * The preferred concept nid.
     */
    private static int PREFERRED_NID;
    // RELATIONSHIP NIDs
    /**
     * The additional characteristic concept nid.
     */
    private static int REL_CH_ADDITIONAL_CHARACTERISTIC_NID;
    /**
     * The defining characteristic concept nid.
     */
    private static int REL_CH_DEFINING_CHARACTERISTIC_NID;
    /**
     * The inferred relationship concept nid.
     */
    private static int REL_CH_INFERRED_RELATIONSHIP_NID;
    /**
     * The qualifier characteristic concept nid.
     */
    private static int REL_CH_QUALIFIER_CHARACTERISTIC_NID;
    /**
     * The stated relationship concept nid.
     */
    private static int REL_CH_STATED_RELATIONSHIP_NID;
    /**
     * The historic concept nid.
     */
    private static int REL_HISTORY_HISTORIC_NID;
    /**
     * The moved to concept nid.
     */
    private static int REL_HISTORY_MOVED_TO_NID;
    /**
     * The optional refinability concept nid.
     */
    private static int REL_OPTIONAL_REFINABILITY_NID;
    /**
     * The refinable concept nid.
     */
    private static int REL_NOT_REFINABLE_NID;
    /**
     * The mandatory refinability concept nid.
     */
    private static int REL_MANDATORY_REFINABILITY_NID;
    // REFEX NIDs
    /**
     * The us dialect refex concept nid.
     */
    private static int US_DIALECT_REFEX_NID;
    /**
     * The gb dialect refex concept nid.
     */
    private static int GB_DIALECT_REFEX_NID;
    /**
     * The synonymy refex concept nid.
     */
    private static int SYNONYMY_REFEX_NID;
    /**
     * The refers to refex concept nid.
     */
    private static int REFERS_TO_REFEX_NID;
    // DESCRIPTION CONCEPTSPECS
    /**
     * The preferred description concept spec.
     */
    private static ConceptSpec DESC_PREFERRED;
    /**
     * The acceptable description concept spec.
     */
    private static ConceptSpec DESC_ACCEPTABLE;
    // STATUS NIDs
    /**
     * The status current concept nid.
     */
    private static int STATUS_CURRENT_NID;
    /**
     * The status retired concept nid.
     */
    private static int STATUS_RETIRED_NID;
    /**
     * The status inappropriate concept nid.
     */
    private static int STATUS_INAPPROPRIATE_NID;
    /**
     * The status duplicate concept nid.
     */
    private static int STATUS_DUPLICATE_NID;
    /**
     * The status ambiguous concept nid.
     */
    private static int STATUS_AMBIGUOUS_NID;
    /**
     * The status erroneous concept nid.
     */
    private static int STATUS_ERRONEOUS_NID;
    /**
     * The status outdated concept nid.
     */
    private static int STATUS_OUTDATED_NID;
    /**
     * The status limited concept nid.
     */
    private static int STATUS_LIMITED_NID;
    /**
     * The status current concept spec.
     */
    private static ConceptSpec STATUS_CURRENT;
    /**
     * The status retired concept spec.
     */
    private static ConceptSpec STATUS_RETIRED;
    /**
     * The status inappropriate concept spec.
     */
    private static ConceptSpec STATUS_INAPPROPRIATE;
    /**
     * The status duplicate concept spec.
     */
    private static ConceptSpec STATUS_DUPLICATE;
    /**
     * The status ambiguous concept spec.
     */
    private static ConceptSpec STATUS_AMBIGUOUS;
    /**
     * The status erroneous concept spec.
     */
    private static ConceptSpec STATUS_ERRONEOUS;
    /**
     * The status outdated concept spec.
     */
    private static ConceptSpec STATUS_OUTDATED;
    /**
     * The status limited concept spec.
     */
    private static ConceptSpec STATUS_LIMITED;
    //REFEX CONCEPTS
    /**
     * The refex non human concept spec.
     */
    private static ConceptSpec REFEX_NON_HUMAN;
    /**
     * The refex vtm concept spec.
     */
    private static ConceptSpec REFEX_VTM;
    /**
     * The refex vmp concept spec.
     */
    private static ConceptSpec REFEX_VMP;
    /**
     * The refex synonymy concept spec.
     */
    private static ConceptSpec REFEX_SYNONYMY;
    /**
     * The terminology store..
     */
    private static TerminologyStoreDI tf;

    // DESCRIPTION CONCEPTS
    /**
     * Gets the RFX fully specified name nid.
     *
     * @return the fully specified name nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getDES_FULL_SPECIFIED_NAME_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (DES_FULL_SPECIFIED_NAME_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids());
        }
        return DES_FULL_SPECIFIED_NAME_NID;
    }

    /**
     * Gets the RFX synonym/preferred name nid. If RF2, returns nid for "Synonym."
     * If RF1, returns nid for "preferred term."
     *
     * @return the the synonym preferred name nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getDES_SYNONYM_PREFERRED_NAME_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (DES_SYNONYM_PREFERRED_NAME_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.SYNONYM_RF2.getUuids());
        }
        return DES_SYNONYM_PREFERRED_NAME_NID;
    }

    /**
     * Gets the RFX synonym nid.
     *
     * @return the synonym nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getDES_SYNONYM_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (DES_SYNONYM_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.SYNONYM_RF2.getUuids());
        }
        return DES_SYNONYM_NID;
    }

    /**
     * Gets the RFX acceptable concept spec. For the concept representing an acceptable description.
     *
     * @return the concept spec representing acceptable
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec getDESC_ACCEPTABLE() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (DESC_ACCEPTABLE == null) {
            return SnomedMetadataRf2.ACCEPTABLE_RF2;
        }
        return DESC_ACCEPTABLE;
    }

    /**
     * Gets the RFX acceptable nid. For the concept representing an acceptable description.
     *
     * @return the acceptable nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getDESC_ACCEPTABLE_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (ACCEPTABLE_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.ACCEPTABLE_RF2.getUuids());
        }
        return ACCEPTABLE_NID;
    }

    /**
     * Gets the RFX preferred concept spec. For the concept representing a preferred description.
     *
     * @return the concept spec representing preferred
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec getDESC_PREFERRED() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (DESC_PREFERRED == null) {
            return SnomedMetadataRf2.PREFERRED_RF2;
        }
        return DESC_PREFERRED;
    }

    /**
     * Gets the RFX preferred nid. For the concept representing a preferred description.
     *
     * @return the preferred nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getDESC_PREFERRED_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (PREFERRED_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.PREFERRED_RF2.getUuids());
        }
        return PREFERRED_NID;
    }

    /**
     * Gets the RFX additional characteristic nid.
     *
     * @return the additional characteristic nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getREL_CH_ADDITIONAL_CHARACTERISTIC_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REL_CH_ADDITIONAL_CHARACTERISTIC_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.ADDITIONAL_RELATIONSHIP_RF2.getUuids());
        }
        return REL_CH_ADDITIONAL_CHARACTERISTIC_NID;
    }

    /**
     * Gets the RFX defining characteristic nid.
     *
     * @return the defining characteristic nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getREL_CH_DEFINING_CHARACTERISTIC_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REL_CH_DEFINING_CHARACTERISTIC_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.DEFINING_RELATIONSHIP_RF2.getUuids());
        }
        return REL_CH_DEFINING_CHARACTERISTIC_NID;
    }

    /**
     * Gets the RFX inferred relationship nid.
     *
     * @return the rel ch inferred relationship nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getREL_CH_INFERRED_RELATIONSHIP_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REL_CH_INFERRED_RELATIONSHIP_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getUuids());
        }
        return REL_CH_INFERRED_RELATIONSHIP_NID;
    }

    /**
     * Gets the RFX qualifier characteristic nid.
     *
     * @return the rel ch qualifier characteristic nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getREL_CH_QUALIFIER_CHARACTERISTIC_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REL_CH_QUALIFIER_CHARACTERISTIC_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.QUALIFYING_RELATIONSSHIP_RF2.getUuids());
        }
        return REL_CH_QUALIFIER_CHARACTERISTIC_NID;
    }

    /**
     * Gets the RFX stated relationship nid.
     *
     * @return the stated relationship nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getREL_CH_STATED_RELATIONSHIP_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REL_CH_STATED_RELATIONSHIP_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids());
        }
        return REL_CH_STATED_RELATIONSHIP_NID;
    }

    /**
     * Gets the RFX historic nid.
     *
     * @return the historic nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getREL_HISTORY_HISTORIC_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REL_HISTORY_HISTORIC_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.HISTORICAL_RELATIONSSHIP_RF2.getUuids());
        }
        return REL_HISTORY_HISTORIC_NID;
    }

    /**
     * Gets the RFX "moved to" nid.
     *
     * @return the moved to nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getREL_HISTORY_MOVED_TO_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REL_HISTORY_MOVED_TO_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.MOVED_TO_REFSET_RF2.getUuids());
        }
        return REL_HISTORY_MOVED_TO_NID;
    }

    /**
     * Gets the RFX mandatory refinability nid.
     *
     * @return the mandatory refinability nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getREL_MANDATORY_REFINABILITY_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REL_MANDATORY_REFINABILITY_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2.getUuids());
        }
        return REL_MANDATORY_REFINABILITY_NID;
    }

    /**
     * Gets the RFX not refinable nid.
     *
     * @return the not refinable nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getREL_NOT_REFINABLE_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REL_NOT_REFINABLE_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.NOT_REFINABLE_RF2.getUuids());
        }
        return REL_NOT_REFINABLE_NID;
    }

    /**
     * Gets the RFX optional refinability nid.
     *
     * @return the optional refinability nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getREL_OPTIONAL_REFINABILITY_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REL_OPTIONAL_REFINABILITY_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids());
        }
        return REL_OPTIONAL_REFINABILITY_NID;
    }

    /**
     * Gets release format in use.
     *
     * @return 1 if using RF1, 2 if using RF2
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getReleaseFormat() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return releaseFormat;
    }

    // STATUS CONCEPTS
    /**
     * Gets the RFX status current concept spec.
     *
     * @return the status current concept spec
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec getSTATUS_CURRENT() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_CURRENT == null) {
            return SnomedMetadataRf2.ACTIVE_VALUE_RF2;
        }
        return STATUS_CURRENT;
    }

    /**
     * Gets the RFX status current nid.
     *
     * @return the status current nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getSTATUS_CURRENT_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_CURRENT_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids());
        }
        return STATUS_CURRENT_NID;
    }

    /**
     * Gets the RFX status limited concept spec.
     *
     * @return the status limited concept spec
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec getSTATUS_LIMITED() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_LIMITED == null) {
            return SnomedMetadataRf2.LIMITED_COMPONENT_RF2;
        }
        return STATUS_LIMITED;
    }

    /**
     * Gets the RFX status limited nid.
     *
     * @return the status limited nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getSTATUS_LIMITED_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_LIMITED_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getUuids());
        }
        return STATUS_LIMITED_NID;
    }

    /**
     * Gets the RFX status retired concept spec.
     *
     * @return the status retired concept spec
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec getSTATUS_RETIRED() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_RETIRED == null) {
            return SnomedMetadataRf2.INACTIVE_VALUE_RF2;
        }
        return STATUS_RETIRED;
    }

    /**
     * Gets the RFX status retired nid.
     *
     * @return the status retired nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getSTATUS_RETIRED_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_RETIRED_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getUuids());
        }
        return STATUS_RETIRED_NID;
    }

    /**
     * Gets the RFX status inappropriate concept spec.
     *
     * @return the status inappropriate concept spec
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec getSTATUS_INAPPROPRIATE() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_INAPPROPRIATE == null) {
            return SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2;
        }
        return STATUS_INAPPROPRIATE;
    }

    /**
     * Gets the RFX status inappropriate nid.
     *
     * @return the status inappropriate nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getSTATUS_INAPPROPRIATE_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_INAPPROPRIATE_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getUuids());
        }
        return STATUS_INAPPROPRIATE_NID;
    }

    /**
     * Gets the RFX status ambiguous concept spec.
     *
     * @return the status ambiguous concept spec
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec getSTATUS_AMBIGUOUS() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_AMBIGUOUS == null) {
            return SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2;
        }
        return STATUS_AMBIGUOUS;
    }

    /**
     * Gets the RFX status ambiguous nid.
     *
     * @return the status ambiguous nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getSTATUS_AMBIGUOUS_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_AMBIGUOUS_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getUuids());
        }
        return STATUS_AMBIGUOUS_NID;
    }

    /**
     * Gets the RFX status duplicate concept spec.
     *
     * @return the status duplicate concept spec
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec getSTATUS_DUPLICATE() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_DUPLICATE == null) {
            return SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2;
        }
        return STATUS_DUPLICATE;
    }

    /**
     * Gets the RFX status duplicate nid.
     *
     * @return the status duplicate nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getSTATUS_DUPLICATE_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_DUPLICATE_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getUuids());
        }
        return STATUS_DUPLICATE_NID;
    }

    /**
     * Gets the RFX status erroneous concept spec.
     *
     * @return the status erroneous concept spec
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec getSTATUS_ERRONEOUS() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_ERRONEOUS == null) {
            return SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2;
        }
        return STATUS_ERRONEOUS;
    }

    /**
     * Gets the RFX status erroneous nid.
     *
     * @return the status erroneous nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getSTATUS_ERRONEOUS_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_ERRONEOUS_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getUuids());
        }
        return STATUS_ERRONEOUS_NID;
    }

    /**
     * Gets the RFX status outdated concept spec.
     *
     * @return the status outdated concept spec
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec getSTATUS_OUTDATED() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_OUTDATED == null) {
            return SnomedMetadataRf2.OUTDATED_COMPONENT_RF2;
        }
        return STATUS_OUTDATED;
    }

    /**
     * Gets the RFX status outdated nid.
     *
     * @return the status outdated nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getSTATUS_OUTDATED_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (STATUS_OUTDATED_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.OUTDATED_COMPONENT_RF2.getUuids());
        }
        return STATUS_OUTDATED_NID;
    }

    /**
     * Gets the RFX US dialect refex nid.
     *
     * @return the US dialect refex nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getUS_DIALECT_REFEX_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (US_DIALECT_REFEX_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getUuids());
        }
        return US_DIALECT_REFEX_NID;
    }

    /**
     * Gets the RFX GB dialect refex nid.
     *
     * @return the GB dialect refex nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getGB_DIALECT_REFEX_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (GB_DIALECT_REFEX_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getUuids());
        }
        return GB_DIALECT_REFEX_NID;
    }

    /**
     * Gets the RFX synonymy refex nid.
     *
     * @return the synonymy refex nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getSYNONYMY_REFEX_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (SYNONYMY_REFEX_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2.getUuids());
        }
        return SYNONYMY_REFEX_NID;
    }

    /**
     * Gets the RFX refers to refex nid.
     *
     * @return the refers to refex nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public static int getREFERS_TO_REFEX_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REFERS_TO_REFEX_NID == 0) {
            return tf.getNidForUuids(SnomedMetadataRf2.REFERS_TO_REFSET_RF2.getUuids());
        }
        return REFERS_TO_REFEX_NID;
    }

    // REFEX CONCEPTS
    /**
     * Gets the RFX refex non human concept spec.
     *
     * @return the refex non human concept spec
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec getREFEX_NON_HUMAN() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REFEX_NON_HUMAN == null) {
            return SnomedMetadataRf2.NON_HUMAN_RF2;
        }
        return REFEX_NON_HUMAN;
    }

    /**
     * Gets the RFX refex VTM concept spec.
     *
     * @return the refex VTM concept spec
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec getREFEX_VTM() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REFEX_VTM == null) {
            return SnomedMetadataRf2.VTM_RF2;
        }
        return REFEX_VTM;
    }

    /**
     * Gets the RFX refex VMP concept spec.
     *
     * @return the refex VMP concept spec
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec getREFEX_VMP() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REFEX_VMP == null) {
            return SnomedMetadataRf2.VMP_RF2;
        }
        return REFEX_VMP;
    }

    /**
     * Gets the RFX synonymy refex concept spec.
     *
     * @return the synonymy refex concept spec
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptSpec getREFEX_SYNONYMY() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if (REFEX_SYNONYMY == null) {
            return SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2;
        }
        return REFEX_SYNONYMY;
    }

    /**
     * Sets up the RF1/RF2 format.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
    private static void setupSnoRf1Rf2() throws IOException {
        tf = Ts.get();

        if (tf.usesRf2Metadata()) {
            isReleaseFormatSetupB = true;
            releaseFormat = 2;
        } else {
            isReleaseFormatSetupB = true;
            releaseFormat = 1;
        }

        if (releaseFormat == 1) {
            // DESCRIPTIONS
            DES_FULL_SPECIFIED_NAME_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUuids());
            DES_SYNONYM_PREFERRED_NAME_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getUuids());
            DES_SYNONYM_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getUuids());
            ACCEPTABLE_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.ACCEPTABLE_DESCRIPTION_TYPE_RF1.getUuids());
            PREFERRED_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.PREFERRED_ACCEPTABILITY_RF1.getUuids());
            // RELATIONSHIPS
            REL_CH_ADDITIONAL_CHARACTERISTIC_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.ADDITIONAL_CHARACTERISTIC_TYPE_RF1.getUuids());
            REL_CH_DEFINING_CHARACTERISTIC_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids());
            REL_CH_INFERRED_RELATIONSHIP_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids());
            REL_CH_QUALIFIER_CHARACTERISTIC_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.QUALIFIER_CHARACTERISTICS_TYPE_RF1.getUuids());
            REL_CH_STATED_RELATIONSHIP_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.STATED_DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids());
            REL_HISTORY_HISTORIC_NID =
                    tf.getNidForUuids(SnomedMetadataRf1.HISTORICAL_CHARACTERISTIC_TYPE_RF1.getUuids());
            REL_HISTORY_MOVED_TO_NID =
                    tf.getNidForUuids(SnomedMetadataRf1.MOVED_TO_RF1.getUuids());
            REL_OPTIONAL_REFINABILITY_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.OPTIONAL_REFINABILITY_TYPE_RF1.getUuids());
            REL_NOT_REFINABLE_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.NOT_REFINABLE_REFINABILITY_TYPE_RF1.getUuids());
            REL_MANDATORY_REFINABILITY_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.MANDATORY_REFINABILITY_TYPE_RF1.getUuids());
            // STATUS: 0 CURRENT, 1 RETIRED
            STATUS_CURRENT = SnomedMetadataRf1.CURRENT_RF1;
            STATUS_CURRENT_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.CURRENT_RF1.getUuids());
            STATUS_LIMITED_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getUuids());
            STATUS_RETIRED = SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1;
            STATUS_RETIRED_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getUuids());
            STATUS_INAPPROPRIATE_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getUuids());
            // REFEX
            US_DIALECT_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.US_LANGUAGE_REFSET_RF1.getUuids());
            GB_DIALECT_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.GB_LANGUAGE_REFSET_RF1.getUuids());
            SYNONYMY_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.DEGREE_OF_SYNONYMY_REFSET_RF1.getUuids());
            REFERS_TO_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.REFERS_TO_REFSET_RF1.getUuids());
            // DESCRIPTION CONCEPT
            DESC_PREFERRED = SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1;
            DESC_ACCEPTABLE = SnomedMetadataRf1.ACCEPTABLE_DESCRIPTION_TYPE_RF1;
            // STATUS CONCEPTSPECS
            // STATUS: 0 CURRENT, 1 RETIRED
            STATUS_CURRENT_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.CURRENT_RF1.getUuids());
            STATUS_LIMITED_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getUuids());
            STATUS_RETIRED_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getUuids());
            STATUS_INAPPROPRIATE_NID = tf.getNidForUuids(
                    SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getUuids());
            STATUS_DUPLICATE = SnomedMetadataRf1.DUPLICATE_INACTIVE_STATUS_RF1;
            STATUS_DUPLICATE_NID = tf.getNidForUuids(STATUS_DUPLICATE.getUuids());
            STATUS_AMBIGUOUS = SnomedMetadataRf1.AMBIGUOUS_INACTIVE_STATUS_RF1;
            STATUS_AMBIGUOUS_NID = tf.getNidForUuids(STATUS_AMBIGUOUS.getUuids());
            STATUS_ERRONEOUS = SnomedMetadataRf1.ERRONEOUS_INACTIVE_STATUS_RF1;
            STATUS_ERRONEOUS_NID = tf.getNidForUuids(STATUS_ERRONEOUS.getUuids());
            STATUS_OUTDATED = SnomedMetadataRf1.OUTDATED_INACTIVE_STATUS_RF1;
            STATUS_OUTDATED_NID = tf.getNidForUuids(STATUS_OUTDATED.getUuids());
            STATUS_LIMITED = SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1;
            // REFEX CONCEPTSPECS
            REFEX_NON_HUMAN = SnomedMetadataRf1.NON_HUMAN_RF1;
            REFEX_VTM = SnomedMetadataRf1.VTM_RF1;
            REFEX_VMP = SnomedMetadataRf1.VMP_RF1;
            REFEX_SYNONYMY = SnomedMetadataRf1.DEGREE_OF_SYNONYMY_REFSET_RF1;

        } else if (releaseFormat == 2) {
            // DESCRIPTIONS
            DES_FULL_SPECIFIED_NAME_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids());
            DES_SYNONYM_PREFERRED_NAME_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.SYNONYM_RF2.getUuids());
            DES_SYNONYM_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.SYNONYM_RF2.getUuids());
            ACCEPTABLE_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.ACCEPTABLE_RF2.getUuids());
            PREFERRED_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.PREFERRED_RF2.getUuids());
            // RELATIONSHIPS
            REL_CH_ADDITIONAL_CHARACTERISTIC_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.ADDITIONAL_RELATIONSHIP_RF2.getUuids());
            REL_CH_DEFINING_CHARACTERISTIC_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.DEFINING_RELATIONSHIP_RF2.getUuids());
            REL_CH_INFERRED_RELATIONSHIP_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getUuids());
            REL_CH_QUALIFIER_CHARACTERISTIC_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.QUALIFYING_RELATIONSSHIP_RF2.getUuids());
            REL_CH_STATED_RELATIONSHIP_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids());
            REL_HISTORY_HISTORIC_NID =
                    tf.getNidForUuids(SnomedMetadataRf2.HISTORICAL_RELATIONSSHIP_RF2.getUuids());
            REL_HISTORY_MOVED_TO_NID =
                    tf.getNidForUuids(SnomedMetadataRf2.MOVED_TO_REFSET_RF2.getUuids());
            REL_OPTIONAL_REFINABILITY_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids());
            REL_NOT_REFINABLE_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.NOT_REFINABLE_RF2.getUuids());
            REL_MANDATORY_REFINABILITY_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2.getUuids());
            STATUS_CURRENT = SnomedMetadataRf2.ACTIVE_VALUE_RF2;
            STATUS_CURRENT_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids());
            STATUS_LIMITED_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getUuids());
            STATUS_RETIRED = SnomedMetadataRf2.INACTIVE_VALUE_RF2;
            STATUS_RETIRED_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.INACTIVE_VALUE_RF2.getUuids());
            STATUS_INAPPROPRIATE_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getUuids());
            //REFEX
            US_DIALECT_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getUuids());
            GB_DIALECT_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getUuids());
            SYNONYMY_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2.getUuids());
            REFERS_TO_REFEX_NID = tf.getNidForUuids(
                    SnomedMetadataRf2.REFERS_TO_REFSET_RF2.getUuids());
            // DESCRIPTION CONCEPT
            DESC_PREFERRED = SnomedMetadataRf2.PREFERRED_RF2;
            DESC_ACCEPTABLE = SnomedMetadataRf2.ACCEPTABLE_RF2;
            // STATUS CONCEPT
            STATUS_CURRENT = SnomedMetadataRf2.ACTIVE_VALUE_RF2;
            STATUS_CURRENT_NID = tf.getNidForUuids(STATUS_CURRENT.getUuids());
            STATUS_RETIRED = SnomedMetadataRf2.INACTIVE_VALUE_RF2;
            STATUS_RETIRED_NID = tf.getNidForUuids(STATUS_RETIRED.getUuids());
            STATUS_INAPPROPRIATE = SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2;
            STATUS_INAPPROPRIATE_NID = tf.getNidForUuids(STATUS_INAPPROPRIATE.getUuids());
            STATUS_DUPLICATE = SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2;
            STATUS_DUPLICATE_NID = tf.getNidForUuids(STATUS_DUPLICATE.getUuids());
            STATUS_AMBIGUOUS = SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2;
            STATUS_AMBIGUOUS_NID = tf.getNidForUuids(STATUS_AMBIGUOUS.getUuids());
            STATUS_ERRONEOUS = SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2;
            STATUS_ERRONEOUS_NID = tf.getNidForUuids(STATUS_ERRONEOUS.getUuids());
            STATUS_OUTDATED = SnomedMetadataRf2.OUTDATED_COMPONENT_RF2;
            STATUS_OUTDATED_NID = tf.getNidForUuids(STATUS_OUTDATED.getUuids());
            STATUS_LIMITED = SnomedMetadataRf2.LIMITED_COMPONENT_RF2;
            STATUS_LIMITED_NID = tf.getNidForUuids(STATUS_LIMITED.getUuids());
            //REFEX CONCEPTS
            REFEX_NON_HUMAN = SnomedMetadataRf2.NON_HUMAN_RF2;
            REFEX_VTM = SnomedMetadataRf2.VTM_RF2;
            REFEX_VMP = SnomedMetadataRf2.VMP_RF2;
            REFEX_SYNONYMY = SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2;

        } else {
            throw new IOException("SnomedMetadataRfx releaseFormat must equal 1 or 2.");
        }
    }
}
