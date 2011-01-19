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
/**
 *
 */
package org.dwfa.mojo.refset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.StringTokenizer;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.file.IterableFileReader;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.cement.RefsetAuxiliary.Concept;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.refset.writers.BooleanRefsetHandler;
import org.dwfa.mojo.refset.writers.ConceptConceptConceptRefsetHandler;
import org.dwfa.mojo.refset.writers.ConceptConceptRefsetHandler;
import org.dwfa.mojo.refset.writers.ConceptDoubleRefsetHandler;
import org.dwfa.mojo.refset.writers.ConceptIntegerRefsetHandler;
import org.dwfa.mojo.refset.writers.ConceptRefsetHandler;
import org.dwfa.mojo.refset.writers.IntegerRefsetHandler;
import org.dwfa.mojo.refset.writers.MemberRefsetHandler;
import org.dwfa.mojo.refset.writers.StringRefsetHandler;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;

enum RefsetType {

    CONCEPT(Concept.CONCEPT_EXTENSION, ConceptRefsetHandler.class, new ConceptDescriptor(
        "be7b2b7e-b397-4cb4-83e1-e443d3c53998", "Attribute value type reference set (foundation metadata concept)"), new ConceptDescriptor(
        "98ffd934-9564-49f3-93c3-954b56b5da89", "Language type reference set (foundation metadata concept)"), new ConceptDescriptor(
        "6298198d-dbe3-4343-893b-3e06d34330d7", "Association type reference set (foundation metadata concept)")
        , new ConceptDescriptor("d815700e-dd66-3f91-8f05-99c60b995eb4", "concept extension by reference")),

    CONCEPT_CONCEPT(Concept.CONCEPT_CONCEPT_EXTENSION, ConceptConceptRefsetHandler.class),

    CONCEPT_CONCEPT_CONCEPT(Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION, ConceptConceptConceptRefsetHandler.class),

    INTEGER(Concept.INT_EXTENSION, IntegerRefsetHandler.class),

    STRING(Concept.STRING_EXTENSION, StringRefsetHandler.class, new ConceptDescriptor(
        "c1a4d7ba-6861-4d2e-8e91-d5f721920d27", "Simple map type reference set (foundation metadata concept)"), new ConceptDescriptor(
        "63c8b731-a6a3-4f09-a7a2-9a02f2c0a918", "Annotation type reference set (foundation metadata concept)")),

    BOOLEAN(Concept.BOOLEAN_EXTENSION, BooleanRefsetHandler.class),

    CONCEPT_INTEGER(Concept.CONCEPT_INT_EXTENSION, ConceptIntegerRefsetHandler.class),

    CONCEPT_DOUBLE(Concept.MEASUREMENT_EXTENSION, ConceptDoubleRefsetHandler.class);

    private Class<? extends MemberRefsetHandler> refsetWriterClass;
    private MemberRefsetHandler refsetWriter = null;
    private Concept auxiliaryConcept;
    private ConceptDescriptor[] refsetTypeParents = null;
    private static I_IntSet status;
    private static I_IntSet types;

    RefsetType(Concept auxiliaryConcept, Class<? extends MemberRefsetHandler> refsetWriterClass,
            ConceptDescriptor... refsetTypeParents) {
        this.auxiliaryConcept = auxiliaryConcept;
        this.refsetWriterClass = refsetWriterClass;
        this.refsetTypeParents = refsetTypeParents;
    }

    public static RefsetType findByExtension(I_ThinExtByRefPart part) {
        if (part instanceof I_ThinExtByRefPartBoolean) {
            return BOOLEAN;
        } else if (part instanceof I_ThinExtByRefPartConceptConceptConcept) {
            return CONCEPT_CONCEPT_CONCEPT;
        } else if (part instanceof I_ThinExtByRefPartConceptConcept) {
            return CONCEPT_CONCEPT;
        } else if (part instanceof I_ThinExtByRefPartConcept) {
            return CONCEPT;
        } else if (part instanceof I_ThinExtByRefPartConceptInt) {
            return CONCEPT_INTEGER;
        } else if (part instanceof I_ThinExtByRefPartInteger) {
            return INTEGER;
        } else if (part instanceof I_ThinExtByRefPartString) {
            return STRING;
        } else if (part instanceof I_ThinExtByRefPartMeasurement) {
            return CONCEPT_DOUBLE;
        }

        throw new EnumConstantNotPresentException(RefsetType.class, "No refset type for the class " + part.getClass()
            + " exists. Full object is " + part);
    }

    public MemberRefsetHandler getRefsetHandler() throws InstantiationException, IllegalAccessException {
        if (refsetWriter == null) {
            refsetWriter = refsetWriterClass.newInstance();
        }

        return refsetWriter;
    }

    public Concept getAuxiliaryConcept() {
        return this.auxiliaryConcept;
    }

    public static FilenameFilter getFileNameFilter() {

        return new FilenameFilter() {

            public boolean accept(File dir, String name) {
                if (name.startsWith("der2_") && name.endsWith(".txt")) {
                    return true;
                }
                return false;
            }

        };
    }

    public static IterableFileReader<I_ThinExtByRefPart> getHandlerForFile(File file) throws Exception {
        return findByFile(file).getRefsetHandler();
    }

    /**
     * Determines the correct RefsetType for a given file.
     * <p>
     * This is done by "peeking" into the file to get the refset identifier,
     * then matching the refset concept up against predefined parents to
     * determine its structural type.
     * <p>
     * See {@link #refsetTypeParents}
     *
     * @param file
     * @return appropriate RefsetType for the passed file, or an exception if
     *         one can't be found
     * @throws Exception
     */
    public static RefsetType findByFile(File file) throws Exception {
        I_GetConceptData refsetConcept;
        try {
            // peek in the file for the refset id
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line = reader.readLine();
            if (line.startsWith(MemberRefsetHandler.RF2_HEADER)) {
                line = reader.readLine();
            }
            reader.close();

            StringTokenizer st = new StringTokenizer(line, MemberRefsetHandler.COLUMN_DELIMITER);
            st.nextToken(); // id
            st.nextToken(); // effectiveTime
            st.nextToken(); // active
            st.nextToken(); // moduleId
            String refsetId = st.nextToken();

            int nid = MemberRefsetHandler.conceptFromIdString(refsetId);

            // find the type for the refset id
            I_TermFactory iTermFactory = LocalVersionedTerminology.get();
            LogWithAlerts editLog = iTermFactory.getEditLog();

            refsetConcept = iTermFactory.getConcept(nid);

            initialiseIntSets(iTermFactory);

            for (RefsetType refsetType : RefsetType.values()) {
                for (ConceptDescriptor parent : refsetType.refsetTypeParents) {
                    if (parent.getVerifiedConcept().isParentOf(refsetConcept, status, types, null, false)) {
                        editLog.info("Found type " + refsetType + " based on parent " + parent + " for refsest file "
                            + file);
                        return refsetType;
                    }
                }
            }
        } catch (Exception e) {
            throw new TerminologyException("Failed finding RefsetType for reference set file " + file
                + " due to underlying exception", e);
        }
        throw new TerminologyException("Cannot find appropriate RefsetType class for concept " + refsetConcept
            + " for file " + file);
    }

    private static void initialiseIntSets(I_TermFactory iTermFactory) throws IOException, TerminologyException,
            Exception {
        if (status == null) {
            status = iTermFactory.newIntSet();
            status.add(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
            status.add(org.dwfa.cement.ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
            status.add(org.dwfa.cement.ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid());
        }

        if (types == null) {
            types = iTermFactory.newIntSet();
            types.add(new ConceptDescriptor("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25", "Is a (attribute)").getVerifiedConcept()
                .getConceptId());
            types.add(ConceptConstants.REFSET_TYPE_REL.localize().getNid());
        }
    }
}
