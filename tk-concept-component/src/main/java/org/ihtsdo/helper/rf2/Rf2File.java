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
package org.ihtsdo.helper.rf2;

// TODO: Auto-generated Javadoc
/**
 * The Class Rf2File.
 *
 * @author kec
 */
public class Rf2File {
    //~--- enums ---------------------------------------------------------------

    /**
     * The Enum ConceptsFileFields.
     */
    public enum ConceptsFileFields {

        /**
         * The id.
         */
        ID("id", "\t"),
        /**
         * The effective time.
         */
        EFFECTIVE_TIME("effectiveTime", "\t"),
        /**
         * The active.
         */
        ACTIVE("active", "\t"),
        /**
         * The module id.
         */
        MODULE_ID("moduleId", "\t"),
        /**
         * The definition status id.
         */
        DEFINITION_STATUS_ID("definitionStatusId", "\r\n");
        /**
         * The header text.
         */
        public final String headerText;
        /**
         * The seperator.
         */
        public final String seperator;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new concepts file fields.
         *
         * @param headerText the header text
         * @param seperator the seperator
         */
        private ConceptsFileFields(String headerText, String seperator) {
            this.headerText = headerText;
            this.seperator = seperator;
        }
    }

    /**
     * The Enum DescriptionsFileFields.
     */
    public enum DescriptionsFileFields {

        /**
         * The id.
         */
        ID("id", "\t"),
        /**
         * The effective time.
         */
        EFFECTIVE_TIME("effectiveTime", "\t"),
        /**
         * The active.
         */
        ACTIVE("active", "\t"),
        /**
         * The module id.
         */
        MODULE_ID("moduleId", "\t"),
        /**
         * The concept id.
         */
        CONCEPT_ID("conceptId", "\t"),
        /**
         * The language code.
         */
        LANGUAGE_CODE("languageCode", "\t"),
        /**
         * The type id.
         */
        TYPE_ID("typeId", "\t"),
        /**
         * The term.
         */
        TERM("term", "\t"),
        /**
         * The case significance id.
         */
        CASE_SIGNIFICANCE_ID("caseSignificanceId", "\r\n");
        /**
         * The header text.
         */
        public final String headerText;
        /**
         * The seperator.
         */
        public final String seperator;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new descriptions file fields.
         *
         * @param headerText the header text
         * @param seperator the seperator
         */
        private DescriptionsFileFields(String headerText, String seperator) {
            this.headerText = headerText;
            this.seperator = seperator;
        }
    }

    /**
     * The Enum IdentifiersFileFields.
     */
    public enum IdentifiersFileFields {

        /**
         * The identifier scheme id.
         */
        IDENTIFIER_SCHEME_ID("identifierSchemeId", "\t"),
        /**
         * The alternate identifier.
         */
        ALTERNATE_IDENTIFIER("alternateIdentifier", "\t"),
        /**
         * The effective time.
         */
        EFFECTIVE_TIME("effectiveTime", "\t"),
        /**
         * The active.
         */
        ACTIVE("active", "\t"),
        /**
         * The module id.
         */
        MODULE_ID("moduleId", "\t"),
        /**
         * The referenced component id.
         */
        REFERENCED_COMPONENT_ID("referencedComponentId", "\r\n");
        /**
         * The header text.
         */
        public final String headerText;
        /**
         * The seperator.
         */
        public final String seperator;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new identifiers file fields.
         *
         * @param headerText the header text
         * @param seperator the seperator
         */
        private IdentifiersFileFields(String headerText, String seperator) {
            this.headerText = headerText;
            this.seperator = seperator;
        }
    }

    /**
     * The Enum RelationshipsFileFields.
     */
    public enum RelationshipsFileFields {

        /**
         * The id.
         */
        ID("id", "\t"),
        /**
         * The effective time.
         */
        EFFECTIVE_TIME("effectiveTime", "\t"),
        /**
         * The active.
         */
        ACTIVE("active", "\t"),
        /**
         * The module id.
         */
        MODULE_ID("moduleId", "\t"),
        /**
         * The source id.
         */
        SOURCE_ID("sourceId", "\t"),
        /**
         * The destination id.
         */
        DESTINATION_ID("destinationId", "\t"),
        /**
         * The relationship group.
         */
        RELATIONSHIP_GROUP("relationshipGroup", "\t"),
        /**
         * The type id.
         */
        TYPE_ID("typeId", "\t"),
        /**
         * The charcteristic id.
         */
        CHARCTERISTIC_ID("characteristicTypeId", "\t"),
        /**
         * The modifier id.
         */
        MODIFIER_ID("modifierId", "\r\n");
        /**
         * The header text.
         */
        public final String headerText;
        /**
         * The seperator.
         */
        public final String seperator;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new relationships file fields.
         *
         * @param headerText the header text
         * @param seperator the seperator
         */
        private RelationshipsFileFields(String headerText, String seperator) {
            this.headerText = headerText;
            this.seperator = seperator;
        }
    }

    /**
     * The Enum StatedRelationshipsFileFields.
     */
    public enum StatedRelationshipsFileFields {

        /**
         * The id.
         */
        ID("id", "\t"),
        /**
         * The effective time.
         */
        EFFECTIVE_TIME("effectiveTime", "\t"),
        /**
         * The active.
         */
        ACTIVE("active", "\t"),
        /**
         * The module id.
         */
        MODULE_ID("moduleId", "\t"),
        /**
         * The source id.
         */
        SOURCE_ID("sourceId", "\t"),
        /**
         * The destination id.
         */
        DESTINATION_ID("destinationId", "\t"),
        /**
         * The relationship group.
         */
        RELATIONSHIP_GROUP("relationshipGroup", "\t"),
        /**
         * The type id.
         */
        TYPE_ID("typeId", "\t"),
        /**
         * The charcteristic id.
         */
        CHARCTERISTIC_ID("characteristicTypeId", "\t"),
        /**
         * The modifier id.
         */
        MODIFIER_ID("modifierId", "\r\n");
        /**
         * The header text.
         */
        public final String headerText;
        /**
         * The seperator.
         */
        public final String seperator;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new stated relationships file fields.
         *
         * @param headerText the header text
         * @param seperator the seperator
         */
        private StatedRelationshipsFileFields(String headerText, String seperator) {
            this.headerText = headerText;
            this.seperator = seperator;
        }
    }

    /**
     * The Enum LanguageRefsetFileFields.
     */
    public enum LanguageRefsetFileFields {

        /**
         * The id.
         */
        ID("id", "\t"),
        /**
         * The effective time.
         */
        EFFECTIVE_TIME("effectiveTime", "\t"),
        /**
         * The active.
         */
        ACTIVE("active", "\t"),
        /**
         * The module id.
         */
        MODULE_ID("moduleId", "\t"),
        /**
         * The refset id.
         */
        REFSET_ID("refSetId", "\t"),
        /**
         * The referenced component id.
         */
        REFERENCED_COMPONENT_ID("referencedComponentId", "\t"),
        /**
         * The acceptability.
         */
        ACCEPTABILITY("acceptabilityId", "\r\n");
        /**
         * The header text.
         */
        public final String headerText;
        /**
         * The seperator.
         */
        public final String seperator;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new language refset file fields.
         *
         * @param headerText the header text
         * @param seperator the seperator
         */
        private LanguageRefsetFileFields(String headerText, String seperator) {
            this.headerText = headerText;
            this.seperator = seperator;
        }
    }

    /**
     * The Enum ModuleDependencyFileFields.
     */
    public enum ModuleDependencyFileFields {

        /**
         * The id.
         */
        ID("id", "\t"),
        /**
         * The effective time.
         */
        EFFECTIVE_TIME("effectiveTime", "\t"),
        /**
         * The active.
         */
        ACTIVE("active", "\t"),
        /**
         * The module id.
         */
        MODULE_ID("moduleId", "\t"),
        /**
         * The refset id.
         */
        REFSET_ID("refSetId", "\t"),
        /**
         * The referenced component id.
         */
        REFERENCED_COMPONENT_ID("referencedComponentId", "\t"),
        /**
         * The source time.
         */
        SOURCE_TIME("sourceEffectiveTime", "\t"),
        /**
         * The target time.
         */
        TARGET_TIME("targetEffectiveTime", "\r\n");
        /**
         * The header text.
         */
        public final String headerText;
        /**
         * The seperator.
         */
        public final String seperator;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new module dependency file fields.
         *
         * @param headerText the header text
         * @param seperator the seperator
         */
        private ModuleDependencyFileFields(String headerText, String seperator) {
            this.headerText = headerText;
            this.seperator = seperator;
        }
    }

    /**
     * The Enum DescTypeFileFields.
     */
    public enum DescTypeFileFields {

        /**
         * The id.
         */
        ID("id", "\t"),
        /**
         * The effective time.
         */
        EFFECTIVE_TIME("effectiveTime", "\t"),
        /**
         * The active.
         */
        ACTIVE("active", "\t"),
        /**
         * The module id.
         */
        MODULE_ID("moduleId", "\t"),
        /**
         * The refset id.
         */
        REFSET_ID("refSetId", "\t"),
        /**
         * The referenced component id.
         */
        REFERENCED_COMPONENT_ID("referencedComponentId", "\t"),
        /**
         * The desc format.
         */
        DESC_FORMAT("descriptionFormat", "\t"),
        /**
         * The desc length.
         */
        DESC_LENGTH("descriptionLength", "\r\n");
        /**
         * The header text.
         */
        public final String headerText;
        /**
         * The seperator.
         */
        public final String seperator;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new desc type file fields.
         *
         * @param headerText the header text
         * @param seperator the seperator
         */
        private DescTypeFileFields(String headerText, String seperator) {
            this.headerText = headerText;
            this.seperator = seperator;
        }
    }

    /**
     * The Enum RefsetDescriptorFileFields.
     */
    public enum RefsetDescriptorFileFields {

        /**
         * The id.
         */
        ID("id", "\t"),
        /**
         * The effective time.
         */
        EFFECTIVE_TIME("effectiveTime", "\t"),
        /**
         * The active.
         */
        ACTIVE("active", "\t"),
        /**
         * The module id.
         */
        MODULE_ID("moduleId", "\t"),
        /**
         * The refset id.
         */
        REFSET_ID("refSetId", "\t"),
        /**
         * The referenced component id.
         */
        REFERENCED_COMPONENT_ID("referencedComponentId", "\t"),
        /**
         * The attrib desc.
         */
        ATTRIB_DESC("attributeDescription", "\t"),
        /**
         * The attrib type.
         */
        ATTRIB_TYPE("attributeType", "\t"),
        /**
         * The attrib order.
         */
        ATTRIB_ORDER("attributeOrder", "\r\n");
        /**
         * The header text.
         */
        public final String headerText;
        /**
         * The seperator.
         */
        public final String seperator;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new refset descriptor file fields.
         *
         * @param headerText the header text
         * @param seperator the seperator
         */
        private RefsetDescriptorFileFields(String headerText, String seperator) {
            this.headerText = headerText;
            this.seperator = seperator;
        }
    }

    /**
     * The Enum SimpleRefsetFileFields.
     */
    public enum SimpleRefsetFileFields {

        /**
         * The id.
         */
        ID("id", "\t"),
        /**
         * The effective time.
         */
        EFFECTIVE_TIME("effectiveTime", "\t"),
        /**
         * The active.
         */
        ACTIVE("active", "\t"),
        /**
         * The module id.
         */
        MODULE_ID("moduleId", "\t"),
        /**
         * The refset id.
         */
        REFSET_ID("refSetId", "\t"),
        /**
         * The referenced component id.
         */
        REFERENCED_COMPONENT_ID("referencedComponentId", "\r\n");
        /**
         * The header text.
         */
        public final String headerText;
        /**
         * The seperator.
         */
        public final String seperator;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new simple refset file fields.
         *
         * @param headerText the header text
         * @param seperator the seperator
         */
        private SimpleRefsetFileFields(String headerText, String seperator) {
            this.headerText = headerText;
            this.seperator = seperator;
        }
    }

    /**
     * The Enum AttribValueRefsetFileFields.
     */
    public enum AttribValueRefsetFileFields {

        /**
         * The id.
         */
        ID("id", "\t"),
        /**
         * The effective time.
         */
        EFFECTIVE_TIME("effectiveTime", "\t"),
        /**
         * The active.
         */
        ACTIVE("active", "\t"),
        /**
         * The module id.
         */
        MODULE_ID("moduleId", "\t"),
        /**
         * The refset id.
         */
        REFSET_ID("refSetId", "\t"),
        /**
         * The referenced component id.
         */
        REFERENCED_COMPONENT_ID("referencedComponentId", "\t"),
        /**
         * The value id.
         */
        VALUE_ID("valueId", "\r\n");
        /**
         * The header text.
         */
        public final String headerText;
        /**
         * The seperator.
         */
        public final String seperator;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new attrib value refset file fields.
         *
         * @param headerText the header text
         * @param seperator the seperator
         */
        private AttribValueRefsetFileFields(String headerText, String seperator) {
            this.headerText = headerText;
            this.seperator = seperator;
        }
    }

    /**
     * The Enum AssociationRefsetFileFields.
     */
    public enum AssociationRefsetFileFields {

        /**
         * The id.
         */
        ID("id", "\t"),
        /**
         * The effective time.
         */
        EFFECTIVE_TIME("effectiveTime", "\t"),
        /**
         * The active.
         */
        ACTIVE("active", "\t"),
        /**
         * The module id.
         */
        MODULE_ID("moduleId", "\t"),
        /**
         * The refset id.
         */
        REFSET_ID("refSetId", "\t"),
        /**
         * The referenced component id.
         */
        REFERENCED_COMPONENT_ID("referencedComponentId", "\t"),
        /**
         * The target.
         */
        TARGET("targetComponent", "\r\n");
        /**
         * The header text.
         */
        public final String headerText;
        /**
         * The seperator.
         */
        public final String seperator;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new association refset file fields.
         *
         * @param headerText the header text
         * @param seperator the seperator
         */
        private AssociationRefsetFileFields(String headerText, String seperator) {
            this.headerText = headerText;
            this.seperator = seperator;
        }
    }

    /**
     * The Enum UuidToSctMapFileFields.
     */
    public enum UuidToSctMapFileFields {

        /**
         * The sct.
         */
        SCT("sctId", "\t"),
        /**
         * The uuid.
         */
        UUID("uuid", "\r\n");
        /**
         * The header text.
         */
        public final String headerText;
        /**
         * The seperator.
         */
        public final String seperator;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new uuid to sct map file fields.
         *
         * @param headerText the header text
         * @param seperator the seperator
         */
        private UuidToSctMapFileFields(String headerText, String seperator) {
            this.headerText = headerText;
            this.seperator = seperator;
        }
    }

    /**
     * The Enum ReleaseType.
     */
    public enum ReleaseType {

        /**
         * The delta.
         */
        DELTA("Delta"),
        /**
         * The full.
         */
        FULL("Full"),
        /**
         * The snapshot.
         */
        SNAPSHOT("Snapshot");
        /**
         * The suffix.
         */
        public final String suffix;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new release type.
         *
         * @param suffix the suffix
         */
        private ReleaseType(String suffix) {
            this.suffix = suffix;
        }
    }
}
