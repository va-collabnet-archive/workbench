/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.binding.snomed;

import java.io.IOException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.spec.ConceptSpec;

// TODO: Auto-generated Javadoc
/**
 * The Class SnomedMetadataRfx.
 *
 * @author marc
 */
public class SnomedMetadataRfx {

    /** The is release format setup b. */
    private static boolean isReleaseFormatSetupB = false;
    
    /** The release format. */
    private static int releaseFormat = 0;
    // --- NIDs ---
    // DESCRIPTION NIDs
    /** The des full specified name nid. */
    private static int DES_FULL_SPECIFIED_NAME_NID;
    
    /** The des synonym preferred name nid. */
    private static int DES_SYNONYM_PREFERRED_NAME_NID;
    
    /** The des synonym nid. */
    private static int DES_SYNONYM_NID;
    
    /** The acceptable nid. */
    private static int ACCEPTABLE_NID;
    
    /** The preferred nid. */
    private static int PREFERRED_NID;
    // RELATIONSHIP NIDs
    /** The rel ch additional characteristic nid. */
    private static int REL_CH_ADDITIONAL_CHARACTERISTIC_NID;
    
    /** The rel ch defining characteristic nid. */
    private static int REL_CH_DEFINING_CHARACTERISTIC_NID;
    
    /** The rel ch inferred relationship nid. */
    private static int REL_CH_INFERRED_RELATIONSHIP_NID;
    
    /** The rel ch qualifier characteristic nid. */
    private static int REL_CH_QUALIFIER_CHARACTERISTIC_NID;
    
    /** The rel ch stated relationship nid. */
    private static int REL_CH_STATED_RELATIONSHIP_NID;
    
    /** The rel history historic nid. */
    private static int REL_HISTORY_HISTORIC_NID;
    
    /** The rel history moved to nid. */
    private static int REL_HISTORY_MOVED_TO_NID;
    
    /** The rel optional refinability nid. */
    private static int REL_OPTIONAL_REFINABILITY_NID;
    
    /** The rel not refinable nid. */
    private static int REL_NOT_REFINABLE_NID;
    
    /** The rel mandatory refinability nid. */
    private static int REL_MANDATORY_REFINABILITY_NID;
    // REFEX NIDs
    /** The us dialect refex nid. */
    private static int US_DIALECT_REFEX_NID;
    
    /** The gb dialect refex nid. */
    private static int GB_DIALECT_REFEX_NID;
    
    /** The synonymy refex nid. */
    private static int SYNONYMY_REFEX_NID;
    
    /** The refers to refex nid. */
    private static int REFERS_TO_REFEX_NID;
    // DESCRIPTION CONCEPTSPECS
    /** The desc preferred. */
    private static ConceptSpec DESC_PREFERRED;
    
    /** The desc acceptable. */
    private static ConceptSpec DESC_ACCEPTABLE;
    // STATUS NIDs
    /** The status current nid. */
    private static int STATUS_CURRENT_NID;
    
    /** The status retired nid. */
    private static int STATUS_RETIRED_NID;
    
    /** The status inappropriate nid. */
    private static int STATUS_INAPPROPRIATE_NID;
    
    /** The status duplicate nid. */
    private static int STATUS_DUPLICATE_NID;
    
    /** The status ambiguous nid. */
    private static int STATUS_AMBIGUOUS_NID;
    
    /** The status erroneous nid. */
    private static int STATUS_ERRONEOUS_NID;
    
    /** The status outdated nid. */
    private static int STATUS_OUTDATED_NID;
    
    /** The status limited nid. */
    private static int STATUS_LIMITED_NID;
    // STATUS CONCEPTSPECS
    /** The status current. */
    private static ConceptSpec STATUS_CURRENT;
    
    /** The status retired. */
    private static ConceptSpec STATUS_RETIRED;
    
    /** The status inappropriate. */
    private static ConceptSpec STATUS_INAPPROPRIATE;
    
    /** The status duplicate. */
    private static ConceptSpec STATUS_DUPLICATE;
    
    /** The status ambiguous. */
    private static ConceptSpec STATUS_AMBIGUOUS;
    
    /** The status erroneous. */
    private static ConceptSpec STATUS_ERRONEOUS;
    
    /** The status outdated. */
    private static ConceptSpec STATUS_OUTDATED;
    
    /** The status limited. */
    private static ConceptSpec STATUS_LIMITED;
    //REFEX CONCEPTS
    /** The refex non human. */
    private static ConceptSpec REFEX_NON_HUMAN;
    
    /** The refex vtm. */
    private static ConceptSpec REFEX_VTM;
    
    /** The refex vmp. */
    private static ConceptSpec REFEX_VMP;
    
    /** The refex synonymy. */
    private static ConceptSpec REFEX_SYNONYMY;
    
    /** The tf. */
    private static TerminologyStoreDI tf;

    // DESCRIPTION CONCEPTS
    /**
     * Gets the des full specified name nid.
     *
     * @return the des full specified name nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getDES_FULL_SPECIFIED_NAME_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(DES_FULL_SPECIFIED_NAME_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids());
        }
        return DES_FULL_SPECIFIED_NAME_NID;
    }

    /**
     * Gets the des synonym preferred name nid.
     *
     * @return the des synonym preferred name nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getDES_SYNONYM_PREFERRED_NAME_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(DES_SYNONYM_PREFERRED_NAME_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.SYNONYM_RF2.getUuids());
        }
        return DES_SYNONYM_PREFERRED_NAME_NID;
    }

    /**
     * Gets the des synonym nid.
     *
     * @return the des synonym nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getDES_SYNONYM_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(DES_SYNONYM_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.SYNONYM_RF2.getUuids());
        }
        return DES_SYNONYM_NID;
    }

    /**
     * Gets the desc acceptable.
     *
     * @return the desc acceptable
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptSpec getDESC_ACCEPTABLE() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(DESC_ACCEPTABLE == null){
            return SnomedMetadataRf2.ACCEPTABLE_RF2;
        }
        return DESC_ACCEPTABLE;
    }

    /**
     * Gets the desc acceptable nid.
     *
     * @return the desc acceptable nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getDESC_ACCEPTABLE_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(ACCEPTABLE_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.ACCEPTABLE_RF2.getUuids());
        }
        return ACCEPTABLE_NID;
    }

    /**
     * Gets the desc preferred.
     *
     * @return the desc preferred
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptSpec getDESC_PREFERRED() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(DESC_PREFERRED == null){
            return SnomedMetadataRf2.PREFERRED_RF2;
        }
        return DESC_PREFERRED;
    }

    /**
     * Gets the desc preferred nid.
     *
     * @return the desc preferred nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getDESC_PREFERRED_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(PREFERRED_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.PREFERRED_RF2.getUuids());
        }
        return PREFERRED_NID;
    }

    /**
     * Gets the rel ch additional characteristic nid.
     *
     * @return the rel ch additional characteristic nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getREL_CH_ADDITIONAL_CHARACTERISTIC_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REL_CH_ADDITIONAL_CHARACTERISTIC_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.ADDITIONAL_RELATIONSHIP_RF2.getUuids());
        }
        return REL_CH_ADDITIONAL_CHARACTERISTIC_NID;
    }

    /**
     * Gets the rel ch defining characteristic nid.
     *
     * @return the rel ch defining characteristic nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getREL_CH_DEFINING_CHARACTERISTIC_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REL_CH_DEFINING_CHARACTERISTIC_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.DEFINING_RELATIONSHIP_RF2.getUuids());
        }
        return REL_CH_DEFINING_CHARACTERISTIC_NID;
    }

    /**
     * Gets the rel ch inferred relationship nid.
     *
     * @return the rel ch inferred relationship nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getREL_CH_INFERRED_RELATIONSHIP_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REL_CH_INFERRED_RELATIONSHIP_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getUuids());
        }
        return REL_CH_INFERRED_RELATIONSHIP_NID;
    }

    /**
     * Gets the rel ch qualifier characteristic nid.
     *
     * @return the rel ch qualifier characteristic nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getREL_CH_QUALIFIER_CHARACTERISTIC_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REL_CH_QUALIFIER_CHARACTERISTIC_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.QUALIFYING_RELATIONSSHIP_RF2.getUuids());
        }
        return REL_CH_QUALIFIER_CHARACTERISTIC_NID;
    }

    /**
     * Gets the rel ch stated relationship nid.
     *
     * @return the rel ch stated relationship nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getREL_CH_STATED_RELATIONSHIP_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REL_CH_STATED_RELATIONSHIP_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids());
        }
        return REL_CH_STATED_RELATIONSHIP_NID;
    }

    /**
     * Gets the rel history historic nid.
     *
     * @return the rel history historic nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getREL_HISTORY_HISTORIC_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REL_HISTORY_HISTORIC_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.HISTORICAL_RELATIONSSHIP_RF2.getUuids());
        }
        return REL_HISTORY_HISTORIC_NID;
    }

    /**
     * Gets the rel history moved to nid.
     *
     * @return the rel history moved to nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getREL_HISTORY_MOVED_TO_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REL_HISTORY_MOVED_TO_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.MOVED_TO_REFSET_RF2.getUuids());
        }
        return REL_HISTORY_MOVED_TO_NID;
    }

    /**
     * Gets the rel mandatory refinability nid.
     *
     * @return the rel mandatory refinability nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getREL_MANDATORY_REFINABILITY_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REL_MANDATORY_REFINABILITY_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2.getUuids());
        }
        return REL_MANDATORY_REFINABILITY_NID;
    }

    /**
     * Gets the rel not refinable nid.
     *
     * @return the rel not refinable nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getREL_NOT_REFINABLE_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REL_NOT_REFINABLE_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.NOT_REFINABLE_RF2.getUuids());
        }
        return REL_NOT_REFINABLE_NID;
    }

    /**
     * Gets the rel optional refinability nid.
     *
     * @return the rel optional refinability nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getREL_OPTIONAL_REFINABILITY_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REL_OPTIONAL_REFINABILITY_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids());
        }
        return REL_OPTIONAL_REFINABILITY_NID;
    }

    /**
     * Gets the release format.
     *
     * @return the release format
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getReleaseFormat() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        return releaseFormat;
    }

    // STATUS CONCEPTS
    /**
     * Gets the status current.
     *
     * @return the status current
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptSpec getSTATUS_CURRENT() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_CURRENT == null){
            return SnomedMetadataRf2.ACTIVE_VALUE_RF2;
        }
        return STATUS_CURRENT;
    }

    /**
     * Gets the status current nid.
     *
     * @return the status current nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getSTATUS_CURRENT_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_CURRENT_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids());
        }
        return STATUS_CURRENT_NID;
    }

    /**
     * Gets the status limited.
     *
     * @return the status limited
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptSpec getSTATUS_LIMITED() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_LIMITED == null){
            return SnomedMetadataRf2.LIMITED_COMPONENT_RF2;
        }
        return STATUS_LIMITED;
    }

    /**
     * Gets the status limited nid.
     *
     * @return the status limited nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getSTATUS_LIMITED_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_LIMITED_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getUuids());
        }
        return STATUS_LIMITED_NID;
    }

    /**
     * Gets the status retired.
     *
     * @return the status retired
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptSpec getSTATUS_RETIRED() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_RETIRED == null){
            return SnomedMetadataRf2.INACTIVE_VALUE_RF2;
        }
        return STATUS_RETIRED;
    }

    /**
     * Gets the status retired nid.
     *
     * @return the status retired nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getSTATUS_RETIRED_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_RETIRED_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getUuids());
        }
        return STATUS_RETIRED_NID;
    }

    /**
     * Gets the status inappropriate.
     *
     * @return the status inappropriate
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptSpec getSTATUS_INAPPROPRIATE() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_INAPPROPRIATE == null){
            return SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2;
        }
        return STATUS_INAPPROPRIATE;
    }

    /**
     * Gets the status inappropriate nid.
     *
     * @return the status inappropriate nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getSTATUS_INAPPROPRIATE_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_INAPPROPRIATE_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getUuids());
        }
        return STATUS_INAPPROPRIATE_NID;
    }

    /**
     * Gets the status ambiguous.
     *
     * @return the status ambiguous
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptSpec getSTATUS_AMBIGUOUS() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_AMBIGUOUS == null){
            return SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2;
        }
        return STATUS_AMBIGUOUS;
    }

    /**
     * Gets the status ambiguous nid.
     *
     * @return the status ambiguous nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getSTATUS_AMBIGUOUS_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_AMBIGUOUS_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getUuids());
        }
        return STATUS_AMBIGUOUS_NID;
    }

    /**
     * Gets the status duplicate.
     *
     * @return the status duplicate
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptSpec getSTATUS_DUPLICATE() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_DUPLICATE == null){
            return SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2;
        }
        return STATUS_DUPLICATE;
    }

    /**
     * Gets the status duplicate nid.
     *
     * @return the status duplicate nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getSTATUS_DUPLICATE_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_DUPLICATE_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getUuids());
        }
        return STATUS_DUPLICATE_NID;
    }

    /**
     * Gets the status erroneous.
     *
     * @return the status erroneous
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptSpec getSTATUS_ERRONEOUS() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_ERRONEOUS == null){
            return SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2;
        }
        return STATUS_ERRONEOUS;
    }

    /**
     * Gets the status erroneous nid.
     *
     * @return the status erroneous nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getSTATUS_ERRONEOUS_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_ERRONEOUS_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getUuids());
        }
        return STATUS_ERRONEOUS_NID;
    }

    /**
     * Gets the status outdated.
     *
     * @return the status outdated
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptSpec getSTATUS_OUTDATED() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_OUTDATED == null){
            return SnomedMetadataRf2.OUTDATED_COMPONENT_RF2;
        }
        return STATUS_OUTDATED;
    }

    /**
     * Gets the status outdated nid.
     *
     * @return the status outdated nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getSTATUS_OUTDATED_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(STATUS_OUTDATED_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.OUTDATED_COMPONENT_RF2.getUuids());
        }
        return STATUS_OUTDATED_NID;
    }

    /**
     * Gets the us dialect refex nid.
     *
     * @return the us dialect refex nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getUS_DIALECT_REFEX_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(US_DIALECT_REFEX_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getUuids());
        }
        return US_DIALECT_REFEX_NID;
    }

    /**
     * Gets the gb dialect refex nid.
     *
     * @return the gb dialect refex nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getGB_DIALECT_REFEX_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(GB_DIALECT_REFEX_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getUuids());
        }
        return GB_DIALECT_REFEX_NID;
    }

    /**
     * Gets the synonymy refex nid.
     *
     * @return the synonymy refex nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getSYNONYMY_REFEX_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(SYNONYMY_REFEX_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2.getUuids());
        }
        return SYNONYMY_REFEX_NID;
    }

    /**
     * Gets the refers to refex nid.
     *
     * @return the refers to refex nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static int getREFERS_TO_REFEX_NID() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REFERS_TO_REFEX_NID == 0){
            return tf.getNidForUuids(SnomedMetadataRf2.REFERS_TO_REFSET_RF2.getUuids());
        }
        return REFERS_TO_REFEX_NID;
    }

    // REFEX CONCEPTS
    /**
     * Gets the refex non human.
     *
     * @return the refex non human
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptSpec getREFEX_NON_HUMAN() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REFEX_NON_HUMAN == null){
            return SnomedMetadataRf2.NON_HUMAN_RF2;
        }
        return REFEX_NON_HUMAN;
    }

    /**
     * Gets the refex vtm.
     *
     * @return the refex vtm
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptSpec getREFEX_VTM() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REFEX_VTM == null){
            return SnomedMetadataRf2.VTM_RF2;
        }
        return REFEX_VTM;
    }

    /**
     * Gets the refex vmp.
     *
     * @return the refex vmp
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptSpec getREFEX_VMP() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REFEX_VMP == null){
            return SnomedMetadataRf2.VMP_RF2;
        }
        return REFEX_VMP;
    }

    /**
     * Gets the refex synonymy.
     *
     * @return the refex synonymy
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptSpec getREFEX_SYNONYMY() throws IOException {
        if (isReleaseFormatSetupB == false) {
            setupSnoRf1Rf2();
        }
        if(REFEX_SYNONYMY == null){
            return SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2;
        }
        return REFEX_SYNONYMY;
    }

    /**
     * Sets the up sno rf1 rf2.
     *
     * @param releaseFormatNumber the new up sno rf1 rf2
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static void setupSnoRf1Rf2(int releaseFormatNumber) throws IOException {
        if (releaseFormatNumber != 1 || releaseFormatNumber != 2) {
            throw new IOException("Invalid Release Format Number" + releaseFormatNumber);
        }

        isReleaseFormatSetupB = true;
        releaseFormat = releaseFormatNumber;
        setupSnoRf1Rf2();
    }

    /**
     * Setup sno rf1 rf2.
     *
     * @throws IOException signals that an I/O exception has occurred.
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
