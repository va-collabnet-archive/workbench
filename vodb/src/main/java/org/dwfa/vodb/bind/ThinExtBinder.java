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
package org.dwfa.vodb.bind;

import java.util.List;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguage;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguageScoped;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.vodb.I_MapIds;
import org.dwfa.vodb.types.ThinExtByRefPartBoolean;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.dwfa.vodb.types.ThinExtByRefPartConceptConcept;
import org.dwfa.vodb.types.ThinExtByRefPartConceptConceptConcept;
import org.dwfa.vodb.types.ThinExtByRefPartConceptConceptString;
import org.dwfa.vodb.types.ThinExtByRefPartConceptInt;
import org.dwfa.vodb.types.ThinExtByRefPartConceptString;
import org.dwfa.vodb.types.ThinExtByRefPartCrossmap;
import org.dwfa.vodb.types.ThinExtByRefPartCrossmapForRel;
import org.dwfa.vodb.types.ThinExtByRefPartInteger;
import org.dwfa.vodb.types.ThinExtByRefPartLanguage;
import org.dwfa.vodb.types.ThinExtByRefPartLanguageScoped;
import org.dwfa.vodb.types.ThinExtByRefPartMeasurement;
import org.dwfa.vodb.types.ThinExtByRefPartString;
import org.dwfa.vodb.types.ThinExtByRefPartTemplate;
import org.dwfa.vodb.types.ThinExtByRefPartTemplateForRel;
import org.dwfa.vodb.types.ThinExtByRefVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ThinExtBinder extends TupleBinding {
    private static int BOOLEAN_ID = 1;
    private static int CONCEPT_ID = 2;
    private static int INTEGER_ID = 3;
    private static int MEASUREMENT_ID = 4;
    private static int LANGUAGE_ID = 5;
    private static int SCOPED_LANGUAGE_ID = 6;
    private static int STRING_ID = 7;
    private static int CON_INT_ID = 8;
    private static int TEMPLATE_FOR_REL_ID = 9;
    private static int TEMPLATE_ID = 10;
    private static int CROSS_MAP_FOR_REL_ID = 11;
    private static int CROSS_MAP_ID = 12;
    private static int CONCEPT_CONCEPT_ID = 13;
    private static int CONCEPT_CONCEPT_CONCEPT_ID = 14;
    private static int CONCEPT_CONCEPT_STRING_ID = 15;
    private static int CONCEPT_STRING_ID = 16;

    public static enum EXT_TYPE {
        BOOLEAN(BOOLEAN_ID, "boolean", I_ThinExtByRefPartBoolean.class),
        CONCEPT(CONCEPT_ID, "concept", I_ThinExtByRefPartConcept.class),
        CON_INT(CON_INT_ID, "con int", I_ThinExtByRefPartConceptInt.class),
        STRING(STRING_ID, "string", I_ThinExtByRefPartString.class),
        INTEGER(INTEGER_ID, "integer", I_ThinExtByRefPartInteger.class),
        MEASUREMENT(MEASUREMENT_ID, "measurement", I_ThinExtByRefPartMeasurement.class),
        LANGUAGE(LANGUAGE_ID, "language", I_ThinExtByRefPartLanguage.class),
        SCOPED_LANGUAGE(SCOPED_LANGUAGE_ID, "scoped language", I_ThinExtByRefPartLanguageScoped.class),
        TEMPLATE_FOR_REL(TEMPLATE_FOR_REL_ID, "template for rel", ThinExtByRefPartTemplateForRel.class),
        TEMPLATE(TEMPLATE_ID, "template", ThinExtByRefPartTemplate.class),
        CROSS_MAP_FOR_REL(CROSS_MAP_FOR_REL_ID, "cross map for rel", ThinExtByRefPartCrossmapForRel.class),
        CROSS_MAP(CROSS_MAP_ID, "cross map", ThinExtByRefPartCrossmap.class),
        CONCEPT_CONCEPT(CONCEPT_CONCEPT_ID, "concept-concept", I_ThinExtByRefPartConceptConcept.class),
        CONCEPT_CONCEPT_CONCEPT(CONCEPT_CONCEPT_CONCEPT_ID, "concept-concept-concept", I_ThinExtByRefPartConceptConceptConcept.class),
        CONCEPT_CONCEPT_STRING(CONCEPT_CONCEPT_STRING_ID, "concept concept string", I_ThinExtByRefPartConceptConceptString.class),
        CONCEPT_STRING(CONCEPT_STRING_ID, "concept-string", I_ThinExtByRefPartConceptString.class), ;

        private int enumId;

        private String interfaceName;

        private Class<? extends I_ThinExtByRefPart> partClass;

        private EXT_TYPE(int id, String interfaceName, Class<? extends I_ThinExtByRefPart> partClass) {
            this.enumId = id;
            this.interfaceName = interfaceName;
            this.partClass = partClass;
        }

        public int getEnumId() {
            return enumId;
        }

        public static EXT_TYPE fromEnumId(int id) {
            switch (id) {
            case 1:
                return BOOLEAN;
            case 2:
                return CONCEPT;
            case 3:
                return INTEGER;
            case 4:
                return MEASUREMENT;
            case 5:
                return LANGUAGE;
            case 6:
                return SCOPED_LANGUAGE;
            case 7:
                return STRING;
            case 8:
                return CON_INT;
            case 9:
                return TEMPLATE_FOR_REL;
            case 10:
                return TEMPLATE;
            case 11:
                return CROSS_MAP_FOR_REL;
            case 12:
                return CROSS_MAP;
            case 13:
                return CONCEPT_CONCEPT;
            case 14:
                return CONCEPT_CONCEPT_CONCEPT;
            case 15:
                return CONCEPT_CONCEPT_STRING;
            case 16:
                return CONCEPT_STRING;

            default:
                throw new RuntimeException("Can't convert to EXT_TYPE: " + id);
            }
        }

        public String getInterfaceName() {
            return interfaceName;
        }

        public Class<? extends I_ThinExtByRefPart> getPartClass() {
            return partClass;
        }

        public int getNid() {
            return getExtensionTypeNid(this);
        }
    }

    private boolean fixedOnly;

    public ThinExtBinder() {
        super();
        this.fixedOnly = false;
    }

    public ThinExtBinder(boolean fixedOnly) {
        super();
        this.fixedOnly = fixedOnly;
    }

    public I_ThinExtByRefVersioned entryToObject(TupleInput ti) {
        int refsetId = ti.readInt();
        int memberId = ti.readInt();
        int componentId = ti.readInt();
        EXT_TYPE type = EXT_TYPE.fromEnumId(ti.readInt());
        int typeId = getExtensionTypeNid(type);

        int partCount = ti.readInt();
        I_ThinExtByRefVersioned versioned = new ThinExtByRefVersioned(refsetId, memberId, componentId, typeId,
            partCount);
        if (fixedOnly) {
            return versioned;
        }
        switch (type) {
        case STRING:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartString part = new ThinExtByRefPartString();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setStringValue(ti.readString());
                versioned.addVersion(part);
            }
            break;
        case BOOLEAN:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartBoolean part = new ThinExtByRefPartBoolean();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setValue(ti.readBoolean());
                versioned.addVersion(part);
            }
            break;
        case CONCEPT:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartConcept part = new ThinExtByRefPartConcept();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setConceptId(ti.readInt());
                versioned.addVersion(part);
            }
            break;
        case CON_INT:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartConceptInt part = new ThinExtByRefPartConceptInt();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setConceptId(ti.readInt());
                part.setIntValue(ti.readInt());
                versioned.addVersion(part);
            }
            break;
        case MEASUREMENT:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartMeasurement part = new ThinExtByRefPartMeasurement();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setUnitsOfMeasureId(ti.readInt());
                part.setMeasurementValue(ti.readDouble());
                versioned.addVersion(part);
            }
            break;

        case INTEGER:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartInteger part = new ThinExtByRefPartInteger();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setValue(ti.readInt());
                versioned.addVersion(part);
            }
            break;
        case LANGUAGE:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartLanguage part = new ThinExtByRefPartLanguage();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setAcceptabilityId(ti.readInt());
                part.setCorrectnessId(ti.readInt());
                part.setDegreeOfSynonymyId(ti.readInt());
                versioned.addVersion(part);
            }
            break;
        case SCOPED_LANGUAGE:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartLanguageScoped part = new ThinExtByRefPartLanguageScoped();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setAcceptabilityId(ti.readInt());
                part.setCorrectnessId(ti.readInt());
                part.setDegreeOfSynonymyId(ti.readInt());
                part.setScopeId(ti.readInt());
                part.setTagId(ti.readInt());
                part.setPriority(ti.readInt());
                versioned.addVersion(part);
            }
            break;
        case CROSS_MAP:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartCrossmap part = new ThinExtByRefPartCrossmap();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setAdditionalCodeId(ti.readInt());
                part.setBlockNo(ti.readInt());
                part.setElementNo(ti.readInt());
                part.setMapStatusId(ti.readInt());
                part.setRefineFlagId(ti.readInt());
                part.setTargetCodeId(ti.readInt());
                versioned.addVersion(part);
            }
            break;
        case CROSS_MAP_FOR_REL:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartCrossmapForRel part = new ThinExtByRefPartCrossmapForRel();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setAdditionalCodeId(ti.readInt());
                part.setBlockNo(ti.readInt());
                part.setElementNo(ti.readInt());
                part.setRefineFlagId(ti.readInt());
                versioned.addVersion(part);
            }
            break;
        case TEMPLATE:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartTemplate part = new ThinExtByRefPartTemplate();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setAttributeDisplayStatusId(ti.readInt());
                part.setAttributeId(ti.readInt());
                part.setBrowseAttributeOrder(ti.readInt());
                part.setBrowseValueOrder(ti.readInt());
                part.setCardinality(ti.readInt());
                part.setCharacteristicStatusId(ti.readInt());
                part.setNotesScreenOrder(ti.readInt());
                part.setSemanticStatusId(ti.readInt());
                part.setTargetCodeId(ti.readInt());
                part.setValueTypeId(ti.readInt());
                versioned.addVersion(part);
            }
            break;
        case TEMPLATE_FOR_REL:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartTemplateForRel part = new ThinExtByRefPartTemplateForRel();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setAttributeDisplayStatusId(ti.readInt());
                part.setBrowseAttributeOrder(ti.readInt());
                part.setBrowseValueOrder(ti.readInt());
                part.setCardinality(ti.readInt());
                part.setCharacteristicStatusId(ti.readInt());
                part.setNotesScreenOrder(ti.readInt());
                part.setSemanticStatusId(ti.readInt());
                part.setValueTypeId(ti.readInt());
                versioned.addVersion(part);
            }
            break;
        case CONCEPT_CONCEPT:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartConceptConcept part = new ThinExtByRefPartConceptConcept();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setC1id(ti.readInt());
                part.setC2id(ti.readInt());
                versioned.addVersion(part);
            }
            break;

        case CONCEPT_CONCEPT_CONCEPT:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartConceptConceptConcept part = new ThinExtByRefPartConceptConceptConcept();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setC1id(ti.readInt());
                part.setC2id(ti.readInt());
                part.setC3id(ti.readInt());
                versioned.addVersion(part);
            }
            break;
        case CONCEPT_CONCEPT_STRING:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartConceptConceptString part = new ThinExtByRefPartConceptConceptString();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setC1id(ti.readInt());
                part.setC2id(ti.readInt());
                part.setStringValue(ti.readString());
                versioned.addVersion(part);
            }
            break;
        case CONCEPT_STRING:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartConceptString part = new ThinExtByRefPartConceptString();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatusId(ti.readInt());
                part.setC1id(ti.readInt());
                part.setStr(ti.readString());
                versioned.addVersion(part);
            }
            break;

        default:
            throw new RuntimeException("Can't handle type: " + type);
        }
        return versioned;
    }

    private static Integer booleanNid;
    private static Integer stringNid;
    private static Integer conceptNid;
    private static Integer integerNid;
    private static Integer conIntNid;
    private static Integer languageNid;
    private static Integer scopedLanguageNid;
    private static Integer measurementNid;
    private static Integer crossMapNid;
    private static Integer crossMapForRelNid;
    private static Integer templateNid;
    private static Integer templateForRelNid;
    private static Integer conceptConceptNid;
    private static Integer conceptConceptConceptNid;
    private static Integer conceptConceptStringNid;
    private static Integer conceptStringNid;

    public static int getExtensionTypeNid(EXT_TYPE type) {
        try {
            switch (type) {
            case BOOLEAN:
                if (booleanNid == null) {
                    booleanNid = AceConfig.getVodb().uuidToNative(RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.getUids());
                }
                return booleanNid;
            case STRING:
                if (stringNid == null) {
                    stringNid = AceConfig.getVodb().uuidToNative(RefsetAuxiliary.Concept.STRING_EXTENSION.getUids());
                }
                return stringNid;
            case CONCEPT:
                if (conceptNid == null) {
                    conceptNid = AceConfig.getVodb().uuidToNative(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids());
                }
                return conceptNid;
            case INTEGER:
                if (integerNid == null) {
                    integerNid = AceConfig.getVodb().uuidToNative(RefsetAuxiliary.Concept.INT_EXTENSION.getUids());
                }
                return integerNid;
            case CON_INT:
                if (conIntNid == null) {
                    conIntNid = AceConfig.getVodb().uuidToNative(
                        RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.getUids());
                }
                return conIntNid;
            case LANGUAGE:
                if (languageNid == null) {
                    languageNid = AceConfig.getVodb()
                        .uuidToNative(RefsetAuxiliary.Concept.LANGUAGE_EXTENSION.getUids());
                }
                return languageNid;
            case SCOPED_LANGUAGE:
                if (scopedLanguageNid == null) {
                    scopedLanguageNid = AceConfig.getVodb().uuidToNative(
                        RefsetAuxiliary.Concept.SCOPED_LANGUAGE_EXTENSION.getUids());
                }
                return scopedLanguageNid;
            case MEASUREMENT:
                if (measurementNid == null) {
                    measurementNid = AceConfig.getVodb().uuidToNative(
                        RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION.getUids());
                }
                return measurementNid;
            case CROSS_MAP:
                if (crossMapNid == null) {
                    crossMapNid = AceConfig.getVodb().uuidToNative(
                        RefsetAuxiliary.Concept.CROSS_MAP_EXTENSION.getUids());
                }
                return crossMapNid;
            case CROSS_MAP_FOR_REL:
                if (crossMapForRelNid == null) {
                    crossMapForRelNid = AceConfig.getVodb().uuidToNative(
                        RefsetAuxiliary.Concept.CROSS_MAP_REL_EXTENSION.getUids());
                }
                return crossMapForRelNid;
            case TEMPLATE:
                if (templateNid == null) {
                    templateNid = AceConfig.getVodb()
                        .uuidToNative(RefsetAuxiliary.Concept.TEMPLATE_EXTENSION.getUids());
                }
                return templateNid;
            case TEMPLATE_FOR_REL:
                if (templateForRelNid == null) {
                    templateForRelNid = AceConfig.getVodb().uuidToNative(
                        RefsetAuxiliary.Concept.TEMPLATE_REL_EXTENSION.getUids());
                }
                return templateForRelNid;
            case CONCEPT_CONCEPT:
                if (conceptConceptNid == null) {
                    conceptConceptNid = AceConfig.getVodb().uuidToNative(
                        RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.getUids());
                }
                return conceptConceptNid;
            case CONCEPT_CONCEPT_CONCEPT:
                if (conceptConceptConceptNid == null) {
                    conceptConceptConceptNid = AceConfig.getVodb().uuidToNative(
                        RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.getUids());
                }
                return conceptConceptConceptNid;
            case CONCEPT_CONCEPT_STRING:
                if (conceptConceptStringNid == null) {
                    conceptConceptStringNid = AceConfig.getVodb().uuidToNative(
                        RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION.getUids());
                }
                return conceptConceptStringNid;
            case CONCEPT_STRING:
                if (conceptStringNid == null) {
                    conceptStringNid = AceConfig.getVodb().uuidToNative(
                        RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION.getUids());
                }
                return conceptStringNid;
            default:
                throw new RuntimeException("Can't convert to type: " + type);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int getExtensionTypeNid(EXT_TYPE type, I_MapIds map, I_Path idPath, int version) {
        try {
            switch (type) {
            case BOOLEAN:
                if (booleanNid == null) {
                    booleanNid = map.getIntId(RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.getUids(), idPath, version);
                }
                return booleanNid;
            case STRING:
                if (stringNid == null) {
                    stringNid = map.getIntId(RefsetAuxiliary.Concept.STRING_EXTENSION.getUids(), idPath, version);
                }
                return stringNid;
            case CONCEPT:
                if (conceptNid == null) {
                    conceptNid = map.getIntId(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids(), idPath, version);
                }
                return conceptNid;
            case INTEGER:
                if (integerNid == null) {
                    integerNid = map.getIntId(RefsetAuxiliary.Concept.INT_EXTENSION.getUids(), idPath, version);
                }
                return integerNid;
            case CON_INT:
                if (conIntNid == null) {
                    conIntNid = map.getIntId(RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.getUids(), idPath, version);
                }
                return conIntNid;
            case LANGUAGE:
                if (languageNid == null) {
                    languageNid = map.getIntId(RefsetAuxiliary.Concept.LANGUAGE_EXTENSION.getUids(), idPath, version);
                }
                return languageNid;
            case SCOPED_LANGUAGE:
                if (scopedLanguageNid == null) {
                    scopedLanguageNid = map.getIntId(RefsetAuxiliary.Concept.SCOPED_LANGUAGE_EXTENSION.getUids(),
                        idPath, version);
                }
                return scopedLanguageNid;
            case MEASUREMENT:
                if (measurementNid == null) {
                    measurementNid = map.getIntId(RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION.getUids(), idPath,
                        version);
                }
                return measurementNid;
            case CROSS_MAP:
                if (crossMapNid == null) {
                    crossMapNid = map.getIntId(RefsetAuxiliary.Concept.CROSS_MAP_EXTENSION.getUids(), idPath, version);
                }
                return crossMapNid;
            case CROSS_MAP_FOR_REL:
                if (crossMapForRelNid == null) {
                    crossMapForRelNid = map.getIntId(RefsetAuxiliary.Concept.CROSS_MAP_REL_EXTENSION.getUids(), idPath,
                        version);
                }
                return crossMapForRelNid;
            case TEMPLATE:
                if (templateNid == null) {
                    templateNid = map.getIntId(RefsetAuxiliary.Concept.TEMPLATE_EXTENSION.getUids(), idPath, version);
                }
                return templateNid;
            case TEMPLATE_FOR_REL:
                if (templateForRelNid == null) {
                    templateForRelNid = map.getIntId(RefsetAuxiliary.Concept.TEMPLATE_REL_EXTENSION.getUids(), idPath,
                        version);
                }
                return templateForRelNid;
            case CONCEPT_CONCEPT:
                if (conceptConceptNid == null) {
                    conceptConceptNid = map.getIntId(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.getUids(),
                        idPath, version);
                }
                return conceptConceptNid;
            case CONCEPT_CONCEPT_CONCEPT:
                if (conceptConceptConceptNid == null) {
                    conceptConceptConceptNid = map.getIntId(
                        RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.getUids(), idPath, version);
                }
                return conceptConceptConceptNid;
            case CONCEPT_CONCEPT_STRING:
                if (conceptConceptStringNid == null) {
                    conceptConceptStringNid = map.getIntId(
                        RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION.getUids(), idPath, version);
                }
                return conceptConceptStringNid;
            case CONCEPT_STRING:
                if (conceptStringNid == null) {
                    conceptStringNid = map.getIntId(RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION.getUids(), idPath,
                        version);
                }
                return conceptStringNid;
            default:
                throw new RuntimeException("Can't convert to type: " + type);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * private int refsetId; private int memberId; private int componentId;
     * private int typeId;
     */
    @SuppressWarnings("unchecked")
    public void objectToEntry(Object obj, TupleOutput to) {
        I_ThinExtByRefVersioned versioned = (I_ThinExtByRefVersioned) obj;
        to.writeInt(versioned.getRefsetId());
        to.writeInt(versioned.getMemberId());
        to.writeInt(versioned.getComponentId());
        EXT_TYPE extType = EXT_TYPE.BOOLEAN;
        extType = getExtensionType(versioned);
        to.writeInt(extType.getEnumId());

        to.writeInt(versioned.getVersions().size());
        switch (extType) {
        case BOOLEAN:
            List<ThinExtByRefPartBoolean> booleanParts = (List<ThinExtByRefPartBoolean>) versioned.getVersions();
            for (I_ThinExtByRefPartBoolean part : booleanParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeBoolean(part.getValue());
            }
            break;
        case STRING:
            List<ThinExtByRefPartString> stringParts = (List<ThinExtByRefPartString>) versioned.getVersions();
            for (I_ThinExtByRefPartString part : stringParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeString(part.getStringValue());
            }
            break;
        case CONCEPT:
            List<ThinExtByRefPartConcept> conceptParts = (List<ThinExtByRefPartConcept>) versioned.getVersions();
            for (I_ThinExtByRefPartConcept part : conceptParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeInt(part.getConceptId());
            }
            break;
        case CON_INT:
            List<ThinExtByRefPartConceptInt> conceptIntParts = (List<ThinExtByRefPartConceptInt>) versioned.getVersions();
            for (I_ThinExtByRefPartConceptInt part : conceptIntParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeInt(part.getConceptId());
                to.writeInt(part.getIntValue());
            }
            break;
        case MEASUREMENT:
            List<ThinExtByRefPartMeasurement> measurementParts = (List<ThinExtByRefPartMeasurement>) versioned.getVersions();
            for (I_ThinExtByRefPartMeasurement part : measurementParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeInt(part.getUnitsOfMeasureId());
                to.writeDouble(part.getMeasurementValue());
            }
            break;

        case INTEGER:
            List<ThinExtByRefPartInteger> intParts = (List<ThinExtByRefPartInteger>) versioned.getVersions();
            for (I_ThinExtByRefPartInteger part : intParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeInt(part.getValue());
            }
            break;
        case LANGUAGE:
            List<ThinExtByRefPartLanguage> langParts = (List<ThinExtByRefPartLanguage>) versioned.getVersions();
            for (I_ThinExtByRefPartLanguage part : langParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeInt(part.getAcceptabilityId());
                to.writeInt(part.getCorrectnessId());
                to.writeInt(part.getDegreeOfSynonymyId());
            }
            break;
        case SCOPED_LANGUAGE:
            List<ThinExtByRefPartLanguageScoped> scopedLangParts = (List<ThinExtByRefPartLanguageScoped>) versioned.getVersions();
            for (I_ThinExtByRefPartLanguageScoped part : scopedLangParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeInt(part.getAcceptabilityId());
                to.writeInt(part.getCorrectnessId());
                to.writeInt(part.getDegreeOfSynonymyId());
                to.writeInt(part.getScopeId());
                to.writeInt(part.getTagId());
                to.writeInt(part.getPriority());
            }
        case CROSS_MAP:
            List<ThinExtByRefPartCrossmap> crossMapParts = (List<ThinExtByRefPartCrossmap>) versioned.getVersions();
            for (ThinExtByRefPartCrossmap part : crossMapParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeInt(part.getAdditionalCodeId());
                to.writeInt(part.getBlockNo());
                to.writeInt(part.getElementNo());
                to.writeInt(part.getMapStatusId());
                to.writeInt(part.getRefineFlagId());
                to.writeInt(part.getTargetCodeId());
                versioned.addVersion(part);
            }
            break;
        case CROSS_MAP_FOR_REL:
            List<ThinExtByRefPartCrossmapForRel> crossMapForRelParts = (List<ThinExtByRefPartCrossmapForRel>) versioned.getVersions();
            for (ThinExtByRefPartCrossmapForRel part : crossMapForRelParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeInt(part.getAdditionalCodeId());
                to.writeInt(part.getBlockNo());
                to.writeInt(part.getElementNo());
                to.writeInt(part.getRefineFlagId());
                versioned.addVersion(part);
            }
            break;
        case TEMPLATE:
            List<ThinExtByRefPartTemplate> templateParts = (List<ThinExtByRefPartTemplate>) versioned.getVersions();
            for (ThinExtByRefPartTemplate part : templateParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeInt(part.getAttributeDisplayStatusId());
                to.writeInt(part.getAttributeId());
                to.writeInt(part.getBrowseAttributeOrder());
                to.writeInt(part.getBrowseValueOrder());
                to.writeInt(part.getCardinality());
                to.writeInt(part.getCharacteristicStatusId());
                to.writeInt(part.getNotesScreenOrder());
                to.writeInt(part.getSemanticStatusId());
                to.writeInt(part.getTargetCodeId());
                to.writeInt(part.getValueTypeId());
                versioned.addVersion(part);
            }
            break;
        case TEMPLATE_FOR_REL:
            List<ThinExtByRefPartTemplateForRel> templateForRelParts = (List<ThinExtByRefPartTemplateForRel>) versioned.getVersions();
            for (ThinExtByRefPartTemplateForRel part : templateForRelParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeInt(part.getAttributeDisplayStatusId());
                to.writeInt(part.getBrowseAttributeOrder());
                to.writeInt(part.getBrowseValueOrder());
                to.writeInt(part.getCardinality());
                to.writeInt(part.getCharacteristicStatusId());
                to.writeInt(part.getNotesScreenOrder());
                to.writeInt(part.getSemanticStatusId());
                to.writeInt(part.getValueTypeId());
                versioned.addVersion(part);
            }
            break;
        case CONCEPT_CONCEPT:
            List<ThinExtByRefPartConceptConcept> ccParts = (List<ThinExtByRefPartConceptConcept>) versioned.getVersions();
            for (ThinExtByRefPartConceptConcept part : ccParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeInt(part.getC1id());
                to.writeInt(part.getC2id());
            }
            break;
        case CONCEPT_CONCEPT_CONCEPT:
            List<ThinExtByRefPartConceptConceptConcept> cccParts = (List<ThinExtByRefPartConceptConceptConcept>) versioned.getVersions();
            for (ThinExtByRefPartConceptConceptConcept part : cccParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeInt(part.getC1id());
                to.writeInt(part.getC2id());
                to.writeInt(part.getC3id());
            }
            break;
        case CONCEPT_CONCEPT_STRING:
            List<ThinExtByRefPartConceptConceptString> ccsParts = (List<ThinExtByRefPartConceptConceptString>) versioned.getVersions();
            for (ThinExtByRefPartConceptConceptString part : ccsParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeInt(part.getC1id());
                to.writeInt(part.getC2id());
                to.writeString(part.getStringValue());
            }
            break;
        case CONCEPT_STRING:
            List<ThinExtByRefPartConceptString> csParts = (List<ThinExtByRefPartConceptString>) versioned.getVersions();
            for (ThinExtByRefPartConceptString part : csParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatusId());
                to.writeInt(part.getC1id());
                to.writeString(part.getStr());
            }
            break;
        default:
            throw new RuntimeException("Can't handle type: " + extType);
        }
    }

    public static EXT_TYPE getExtensionType(Class<? extends I_ThinExtByRefPart> partType) {
        for (EXT_TYPE extType : EXT_TYPE.values()) {
            if (extType.getPartClass().equals(partType)) {
                return extType;
            }
        }
        throw new UnsupportedOperationException("Can't convert to type: " + partType);
    }

    public static EXT_TYPE getExtensionType(I_ThinExtByRefVersioned versioned) {
        if (versioned.getVersions() == null || versioned.getVersions().size() == 0) {
            try {
                if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.BOOLEAN)) {
                    return EXT_TYPE.BOOLEAN;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.STRING)) {
                    return EXT_TYPE.STRING;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.CONCEPT)) {
                    return EXT_TYPE.CONCEPT;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.CON_INT)) {
                    return EXT_TYPE.CON_INT;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.INTEGER)) {
                    return EXT_TYPE.INTEGER;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.LANGUAGE)) {
                    return EXT_TYPE.LANGUAGE;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.SCOPED_LANGUAGE)) {
                    return EXT_TYPE.SCOPED_LANGUAGE;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.MEASUREMENT)) {
                    return EXT_TYPE.MEASUREMENT;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.CROSS_MAP)) {
                    return EXT_TYPE.CROSS_MAP;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.CROSS_MAP_FOR_REL)) {
                    return EXT_TYPE.CROSS_MAP_FOR_REL;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.TEMPLATE)) {
                    return EXT_TYPE.TEMPLATE;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.TEMPLATE_FOR_REL)) {
                    return EXT_TYPE.TEMPLATE_FOR_REL;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.CONCEPT_CONCEPT)) {
                    return EXT_TYPE.CONCEPT_CONCEPT;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.CONCEPT_CONCEPT_CONCEPT)) {
                    return EXT_TYPE.CONCEPT_CONCEPT_CONCEPT;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.CONCEPT_CONCEPT_STRING)) {
                    return EXT_TYPE.CONCEPT_CONCEPT_STRING;
                } else if (versioned.getTypeId() == getExtensionTypeNid(EXT_TYPE.CONCEPT_STRING)) {
                    return EXT_TYPE.CONCEPT_STRING;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            Object firstPart = versioned.getVersions().get(0);
            if (ThinExtByRefPartBoolean.class.equals(firstPart.getClass())) {
                return EXT_TYPE.BOOLEAN;
            } else if (ThinExtByRefPartString.class.equals(firstPart.getClass())) {
                return EXT_TYPE.STRING;
            } else if (ThinExtByRefPartConcept.class.equals(firstPart.getClass())) {
                return EXT_TYPE.CONCEPT;
            } else if (ThinExtByRefPartConceptInt.class.equals(firstPart.getClass())) {
                return EXT_TYPE.CON_INT;
            } else if (ThinExtByRefPartInteger.class.equals(firstPart.getClass())) {
                return EXT_TYPE.INTEGER;
            } else if (ThinExtByRefPartLanguage.class.equals(firstPart.getClass())) {
                return EXT_TYPE.LANGUAGE;
            } else if (ThinExtByRefPartLanguageScoped.class.equals(firstPart.getClass())) {
                return EXT_TYPE.SCOPED_LANGUAGE;
            } else if (ThinExtByRefPartMeasurement.class.equals(firstPart.getClass())) {
                return EXT_TYPE.MEASUREMENT;
            } else if (ThinExtByRefPartTemplate.class.equals(firstPart.getClass())) {
                return EXT_TYPE.TEMPLATE;
            } else if (ThinExtByRefPartTemplateForRel.class.equals(firstPart.getClass())) {
                return EXT_TYPE.TEMPLATE_FOR_REL;
            } else if (ThinExtByRefPartCrossmap.class.equals(firstPart.getClass())) {
                return EXT_TYPE.CROSS_MAP;
            } else if (ThinExtByRefPartCrossmapForRel.class.equals(firstPart.getClass())) {
                return EXT_TYPE.CROSS_MAP_FOR_REL;
            } else if (ThinExtByRefPartConceptConcept.class.equals(firstPart.getClass())) {
                return EXT_TYPE.CONCEPT_CONCEPT;
            } else if (ThinExtByRefPartConceptConceptConcept.class.equals(firstPart.getClass())) {
                return EXT_TYPE.CONCEPT_CONCEPT_CONCEPT;
            } else if (ThinExtByRefPartConceptConceptString.class.equals(firstPart.getClass())) {
                return EXT_TYPE.CONCEPT_CONCEPT_STRING;
            } else if (ThinExtByRefPartConceptString.class.equals(firstPart.getClass())) {
                return EXT_TYPE.CONCEPT_STRING;
            }
        }
        throw new UnsupportedOperationException("Can't convert to type: " + versioned);
    }

}
