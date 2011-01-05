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
package org.dwfa.ace.api.ebr;

import org.dwfa.ace.api.BeanProperty;

public enum ThinExtByRefPartProperty implements BeanProperty {

    STATUS("statusId"), VERSION("version"), PATH("pathId"),

    CONCEPT_ONE("c1id"), CONCEPT_TWO("c2id"), CONCEPT_THREE("c3id"),

    BOOLEAN_VALUE("value"), INTEGER_VALUE("intValue"), STRING_VALUE("stringValue"),

    CROSSMAP_STATUS("mapStatusId"), CROSSMAP_TARGET("targetCodeId"),

    CROSSMAP_REL_REFINE_FLAG("refineFlagId"), CROSSMAP_REL_ADDITIONAL_CODE("additionalCodeId"), CROSSMAP_REL_ELEMENT_NUM("elementNo"), CROSSMAP_REL_BLOCK_NUM("blockNo"),

    LANGUAGE_ACCEPTABILITY("acceptabilityId"), LANGUAGE_CORRECTNESS("correctnessId"), LANGUAGE_DEGREE_OF_SYNONYMY("degreeOfSynonymyId"),

    MEASUREMENT_UNITS("unitsOfMeasureId"), MEASUREMENT_VALUE("measurementValue"),

    TEMPLATE_FOR_REL_VALUE_TYPE("valueTypeId"), TEMPLATE_FOR_REL_CARDINALITY("cardinality"), TEMPLATE_FOR_REL_SEMANTIC_STATUS("semanticStatusId"), TEMPLATE_FOR_REL_BROWSE_ATTRIB_ORDER("browseAttributeOrder"), TEMPLATE_FOR_REL_BROWSE_VALUE_ORDER("browseValueOrder"), TEMPLATE_FOR_REL_NOTES_SCREEN_ORDER("notesScreenOrder"), TEMPLATE_FOR_REL_ATTRIB_DISPLAY_STATUS("attributeDisplayStatusId"), TEMPLATE_FOR_REL_CHARACTERISTIC_STATUS("characteristicStatusId");

    protected String beanProperty;

    private ThinExtByRefPartProperty(String beanProperty) {
        this.beanProperty = beanProperty;
    }

    public String getPropertyName() {
        return this.beanProperty;
    }
}
