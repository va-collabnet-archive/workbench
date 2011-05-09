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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.cement.RefsetAuxiliary.Concept;
import org.dwfa.mojo.file.FileHandler;
import org.dwfa.mojo.refset.writers.BooleanRefsetHandler;
import org.dwfa.mojo.refset.writers.ConceptIntegerRefsetHandler;
import org.dwfa.mojo.refset.writers.ConceptRefsetHandler;
import org.dwfa.mojo.refset.writers.IntegerRefsetHandler;
import org.dwfa.mojo.refset.writers.MemberRefsetHandler;
import org.dwfa.mojo.refset.writers.StringRefsetHandler;
import org.dwfa.mojo.refset.writers.ConceptDoubleRefsetHandler;

enum RefsetType {

    CONCEPT(
        Concept.CONCEPT_EXTENSION,
        ConceptRefsetHandler.class,
        ".concept.refset"
    ),

    INTEGER(
        Concept.INT_EXTENSION,
        IntegerRefsetHandler.class,
        ".integer.refset"
    ),

    STRING(
        Concept.STRING_EXTENSION,
        StringRefsetHandler.class,
        ".string.refset"
    ),

    BOOLEAN(
        Concept.BOOLEAN_EXTENSION,
        BooleanRefsetHandler.class,
        ".boolean.refset"
    ),

    CONCEPT_INTEGER(
        Concept.CONCEPT_INT_EXTENSION,
        ConceptIntegerRefsetHandler.class,
        ".concept.integer.refset"
    ),

    CONCEPT_DOUBLE(
            Concept.MEASUREMENT_EXTENSION,
            ConceptDoubleRefsetHandler.class,
            ".concept.double.refset"
    );

    private Class<? extends MemberRefsetHandler> refsetWriterClass;
    private MemberRefsetHandler refsetWriter = null;
    private Concept auxiliaryConcept;
    private String fileExtension = null;

    RefsetType(Concept auxiliaryConcept, Class<? extends MemberRefsetHandler> refsetWriterClass, String fileExtension) {
        this.auxiliaryConcept = auxiliaryConcept;
        this.refsetWriterClass = refsetWriterClass;
        this.fileExtension = fileExtension;
    }

    public static RefsetType findByExtension(I_ThinExtByRefPart part) {
        if (part instanceof I_ThinExtByRefPartBoolean) {
            return BOOLEAN;
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

        throw new EnumConstantNotPresentException(RefsetType.class, "No refset type for the class "
                + part.getClass() + " exists. Full object is " + part);
    }

    /**
     * @param filename
     * @return The appropriate refset type based on the file name.
     * @throws EnumConstantNotPresentException where the filename cannot be matched
     */
    public static RefsetType findByFilename(String filename) {
        for (RefsetType t : RefsetType.values()) {
            if (t.matches(filename)) {
                return t;
            }
        }
        throw new EnumConstantNotPresentException(RefsetType.class, "No refset type for " + filename + " exists");
    }

    private boolean matches(String filename) {
        return (filename.toLowerCase().endsWith(this.fileExtension));
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

    public String getFileExtension() {
        return fileExtension;
    }

    public static FilenameFilter getFileNameFilter() {

        return new FilenameFilter() {
            List<String> filenameExtensions;

            public boolean accept(File dir, String name) {
                if (filenameExtensions == null) {
                    filenameExtensions = new ArrayList<String>();
                    for (RefsetType refsetType : RefsetType.values()) {
                        filenameExtensions.add(refsetType.getFileExtension());
                    }
                }

                for (String extension : filenameExtensions) {
                    if (name.endsWith(extension)) {
                        return true;
                    }
                }
                return false;
            }

        };
    }

    public static FileHandler<I_ThinExtByRefPart> getHandlerForFile(File file) throws InstantiationException, IllegalAccessException {
        return findByFilename(file.getName()).getRefsetHandler();
    }
}
