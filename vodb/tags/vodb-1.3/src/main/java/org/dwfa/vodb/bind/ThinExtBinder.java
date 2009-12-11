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

import org.dwfa.ace.config.AceConfig;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.vodb.types.ThinExtByRefPartBoolean;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.dwfa.vodb.types.ThinExtByRefPartInteger;
import org.dwfa.vodb.types.ThinExtByRefPartLanguage;
import org.dwfa.vodb.types.ThinExtByRefPartLanguageScoped;
import org.dwfa.vodb.types.ThinExtByRefPartMeasurement;
import org.dwfa.vodb.types.ThinExtByRefPartString;
import org.dwfa.vodb.types.ThinExtByRefVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ThinExtBinder extends TupleBinding {
    public static enum EXT_TYPE {
        BOOLEAN(1, "boolean"), CONCEPT(2, "concept"), INTEGER(3, "integer"), MEASUREMENT(6, "measurement"), LANGUAGE(4,
                "language"), SCOPED_LANGUAGE(5, "scoped language"), STRING(7, "string");

        private int enumId;

        private String interfaceName;

        private EXT_TYPE(int id, String interfaceName) {
            this.enumId = id;
            this.interfaceName = interfaceName;
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
                return LANGUAGE;
            case 5:
                return SCOPED_LANGUAGE;
            case 6:
                return MEASUREMENT;
            case 7:
                return STRING;

            default:
                throw new RuntimeException("Can't convert to EXT_TYPE: " + id);
            }
        }

        public String getInterfaceName() {
            return interfaceName;
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

    public ThinExtByRefVersioned entryToObject(TupleInput ti) {
        int refsetId = ti.readInt();
        int memberId = ti.readInt();
        int componentId = ti.readInt();
        EXT_TYPE type = EXT_TYPE.fromEnumId(ti.readInt());
        int typeId = getExtensionType(type);

        int partCount = ti.readInt();
        ThinExtByRefVersioned versioned = new ThinExtByRefVersioned(refsetId, memberId, componentId, typeId, partCount);
        if (fixedOnly) {
            return versioned;
        }
        switch (type) {
        case STRING:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartString part = new ThinExtByRefPartString();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatus(ti.readInt());
                part.setStringValue(ti.readString());
                versioned.addVersion(part);
            }
            break;
        case BOOLEAN:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartBoolean part = new ThinExtByRefPartBoolean();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatus(ti.readInt());
                part.setValue(ti.readBoolean());
                versioned.addVersion(part);
            }
            break;
        case CONCEPT:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartConcept part = new ThinExtByRefPartConcept();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatus(ti.readInt());
                part.setConceptId(ti.readInt());
                versioned.addVersion(part);
            }
            break;
        case MEASUREMENT:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartMeasurement part = new ThinExtByRefPartMeasurement();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatus(ti.readInt());
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
                part.setStatus(ti.readInt());
                part.setValue(ti.readInt());
                versioned.addVersion(part);
            }
            break;
        case LANGUAGE:
            for (int x = 0; x < partCount; x++) {
                ThinExtByRefPartLanguage part = new ThinExtByRefPartLanguage();
                part.setPathId(ti.readInt());
                part.setVersion(ti.readInt());
                part.setStatus(ti.readInt());
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
                part.setStatus(ti.readInt());
                part.setAcceptabilityId(ti.readInt());
                part.setCorrectnessId(ti.readInt());
                part.setDegreeOfSynonymyId(ti.readInt());
                part.setScopeId(ti.readInt());
                part.setTagId(ti.readInt());
                part.setPriority(ti.readInt());
                versioned.addVersion(part);
            }
            break;
        default:
            throw new RuntimeException("Can't handle type: " + type);
        }
        return versioned;
    }

    public static int getExtensionType(EXT_TYPE type) {
        try {
            switch (type) {
            case BOOLEAN:
                return AceConfig.getVodb().uuidToNative(RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.getUids());
            case STRING:
                return AceConfig.getVodb().uuidToNative(RefsetAuxiliary.Concept.STRING_EXTENSION.getUids());
            case CONCEPT:
                return AceConfig.getVodb().uuidToNative(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids());
            case INTEGER:
                return AceConfig.getVodb().uuidToNative(RefsetAuxiliary.Concept.INT_EXTENSION.getUids());
            case LANGUAGE:
                return AceConfig.getVodb().uuidToNative(RefsetAuxiliary.Concept.LANGUAGE_EXTENSION.getUids());
            case SCOPED_LANGUAGE:
                return AceConfig.getVodb().uuidToNative(RefsetAuxiliary.Concept.SCOPED_LANGUAGE_EXTENSION.getUids());
            case MEASUREMENT:
                return AceConfig.getVodb().uuidToNative(RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION.getUids());
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
        ThinExtByRefVersioned versioned = (ThinExtByRefVersioned) obj;
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
            for (ThinExtByRefPartBoolean part : booleanParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatus());
                to.writeBoolean(part.getValue());
            }
            break;
        case STRING:
            List<ThinExtByRefPartString> stringParts = (List<ThinExtByRefPartString>) versioned.getVersions();
            for (ThinExtByRefPartString part : stringParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatus());
                to.writeString(part.getStringValue());
            }
            break;
        case CONCEPT:
            List<ThinExtByRefPartConcept> conceptParts = (List<ThinExtByRefPartConcept>) versioned.getVersions();
            for (ThinExtByRefPartConcept part : conceptParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatus());
                to.writeInt(part.getConceptId());
            }
            break;
        case MEASUREMENT:
            List<ThinExtByRefPartMeasurement> measurementParts = (List<ThinExtByRefPartMeasurement>) versioned
                    .getVersions();
            for (ThinExtByRefPartMeasurement part : measurementParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatus());
                to.writeInt(part.getUnitsOfMeasureId());
                to.writeDouble(part.getMeasurementValue());
            }
            break;

        case INTEGER:
            List<ThinExtByRefPartInteger> intParts = (List<ThinExtByRefPartInteger>) versioned.getVersions();
            for (ThinExtByRefPartInteger part : intParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatus());
                to.writeInt(part.getValue());
            }
            break;
        case LANGUAGE:
            List<ThinExtByRefPartLanguage> langParts = (List<ThinExtByRefPartLanguage>) versioned.getVersions();
            for (ThinExtByRefPartLanguage part : langParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatus());
                to.writeInt(part.getAcceptabilityId());
                to.writeInt(part.getCorrectnessId());
                to.writeInt(part.getDegreeOfSynonymyId());
            }
            break;
        case SCOPED_LANGUAGE:
            List<ThinExtByRefPartLanguageScoped> scopedLangParts = (List<ThinExtByRefPartLanguageScoped>) versioned
                    .getVersions();
            for (ThinExtByRefPartLanguageScoped part : scopedLangParts) {
                to.writeInt(part.getPathId());
                to.writeInt(part.getVersion());
                to.writeInt(part.getStatus());
                to.writeInt(part.getAcceptabilityId());
                to.writeInt(part.getCorrectnessId());
                to.writeInt(part.getDegreeOfSynonymyId());
                to.writeInt(part.getScopeId());
                to.writeInt(part.getTagId());
                to.writeInt(part.getPriority());
            }
            break;
        default:
            throw new RuntimeException("Can't handle type: " + extType);
        }
    }

    public static EXT_TYPE getExtensionType(ThinExtByRefVersioned versioned) {
        if (versioned.getVersions() == null || versioned.getVersions().size() == 0) {
            try {
                if (versioned.getTypeId() == AceConfig.getVodb().uuidToNative(
                                                                              RefsetAuxiliary.Concept.BOOLEAN_EXTENSION
                                                                                      .getUids())) {
                    return EXT_TYPE.BOOLEAN;
                } else if (versioned.getTypeId() == AceConfig.getVodb()
                        .uuidToNative(RefsetAuxiliary.Concept.STRING_EXTENSION.getUids())) {
                    return EXT_TYPE.STRING;
                } else if (versioned.getTypeId() == AceConfig.getVodb()
                        .uuidToNative(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids())) {
                    return EXT_TYPE.CONCEPT;
                } else if (versioned.getTypeId() == AceConfig.getVodb()
                        .uuidToNative(RefsetAuxiliary.Concept.INT_EXTENSION.getUids())) {
                    return EXT_TYPE.INTEGER;
                } else if (versioned.getTypeId() == AceConfig.getVodb()
                        .uuidToNative(RefsetAuxiliary.Concept.LANGUAGE_EXTENSION.getUids())) {
                    return EXT_TYPE.LANGUAGE;
                } else if (versioned.getTypeId() == AceConfig.getVodb()
                        .uuidToNative(RefsetAuxiliary.Concept.SCOPED_LANGUAGE_EXTENSION.getUids())) {
                    return EXT_TYPE.SCOPED_LANGUAGE;
                } else if (versioned.getTypeId() == AceConfig.getVodb()
                        .uuidToNative(RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION.getUids())) {
                    return EXT_TYPE.MEASUREMENT;
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
            } else if (ThinExtByRefPartInteger.class.equals(firstPart.getClass())) {
                return EXT_TYPE.INTEGER;
            } else if (ThinExtByRefPartLanguage.class.equals(firstPart.getClass())) {
                return EXT_TYPE.LANGUAGE;
            } else if (ThinExtByRefPartLanguageScoped.class.equals(firstPart.getClass())) {
                return EXT_TYPE.SCOPED_LANGUAGE;
            } else if (ThinExtByRefPartMeasurement.class.equals(firstPart.getClass())) {
                return EXT_TYPE.MEASUREMENT;
            }
        }
        throw new UnsupportedOperationException("Can't convert to type: " + versioned);
    }

}
