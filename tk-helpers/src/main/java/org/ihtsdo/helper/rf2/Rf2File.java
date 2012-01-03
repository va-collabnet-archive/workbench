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
package org.ihtsdo.helper.rf2;

/**
 *
 * @author kec
 */
public class Rf2File {
       //~--- enums ---------------------------------------------------------------

   public enum ConceptsFileFields {
      ID("id", "\t"), EFFECTIVE_TIME("effectiveTime", "\t"), ACTIVE("active", "\t"), MODULE_ID("moduleId", "\t"),
      DEFINITION_STATUS_ID("definitionStatusId", "\n");

      public final String headerText;
      public final String seperator;

      //~--- constructors -----------------------------------------------------

      private ConceptsFileFields(String headerText, String seperator) {
         this.headerText = headerText;
         this.seperator = seperator;
      }
   }

   public enum DescriptionsFileFields {
      ID("id", "\t"), EFFECTIVE_TIME("effectiveTime", "\t"), ACTIVE("active", "\t"), MODULE_ID("moduleId", "\t"),
      CONCEPT_ID("conceptId", "\t"), LANGUAGE_CODE("languageCode", "\t"), TYPE_ID("typeId", "\t"), TERM("term", "\t"),
      CASE_SIGNIFICANCE_ID("caseSignificanceId", "\n");

      public final String headerText;
      public final String seperator;

      //~--- constructors -----------------------------------------------------

      private DescriptionsFileFields(String headerText, String seperator) {
         this.headerText = headerText;
         this.seperator = seperator;
      }
   }

   public enum IdentifiersFileFields {
      IDENTIFIER_SCHEME_ID("identifierSchemeId", "\t"), ALTERNATE_IDENTIFIER("alternateIdentifier", "\t"),
      EFFECTIVE_TIME("effectiveTime", "\t"), ACTIVE("active", "\t"), MODULE_ID("moduleId", "\t"),
      REFERENCED_COMPONENT_ID("referencedComponentId", "\n");

      public final String headerText;
      public final String seperator;

      //~--- constructors -----------------------------------------------------

      private IdentifiersFileFields(String headerText, String seperator) {
         this.headerText = headerText;
         this.seperator = seperator;
      }
   }

   public enum RelationshipsFileFields {
      ID("id", "\t"), EFFECTIVE_TIME("effectiveTime", "\t"), ACTIVE("active", "\t"), MODULE_ID("moduleId", "\t"),
      SOURCE_ID("sourceId", "\t"), DESTINATION_ID("destinationId", "\t"), RELATIONSHIP_GROUP("relationshipGroup", "\t"),
      TYPE_ID("typeId", "\t"), CHARCTERISTIC_ID("characteristicTypeId", "\t"), MODIFIER_ID("modifierId", "\n");

      public final String headerText;
      public final String seperator;

      //~--- constructors -----------------------------------------------------

      private RelationshipsFileFields(String headerText, String seperator) {
         this.headerText = headerText;
         this.seperator = seperator;
      }
   }
   
   
   public enum ReleaseType {
      DELTA("Delta"), FULL("Full"), SNAPSHOT("Snapshot");

      public final String suffix;

      //~--- constructors -----------------------------------------------------

      private ReleaseType(String suffix) {
         this.suffix = suffix;
      }
   }

}
