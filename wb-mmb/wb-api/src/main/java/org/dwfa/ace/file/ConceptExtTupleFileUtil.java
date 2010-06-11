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
package org.dwfa.ace.file;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class ConceptExtTupleFileUtil {

    public static String exportTuple(I_ExtendByRefVersion tuple) throws TerminologyException, IOException {

        try {
            I_TermFactory termFactory = Terms.get();

            UUID tupleUuid = ArchitectonicAuxiliary.Concept.EXT_CONCEPT_TUPLE.getUids().iterator().next();

            UUID memberUuid = termFactory.getUids(tuple.getMemberId()).iterator().next();
            UUID refsetUuid = termFactory.getUids(tuple.getRefsetId()).iterator().next();
            UUID componentUuid = termFactory.getUids(tuple.getComponentId()).iterator().next();
            UUID typeUuid = termFactory.getUids(tuple.getTypeId()).iterator().next();
            if (!typeUuid.equals(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next())) {
                throw new TerminologyException("Non concept ext tuple passed to concept file util.");
            }

            I_ExtendByRefPartCid part = (I_ExtendByRefPartCid) tuple.getMutablePart();
            UUID conceptUuid = termFactory.getUids(part.getC1id()).iterator().next();
            UUID pathUuid = termFactory.getUids(tuple.getPathId()).iterator().next();
            UUID statusUuid = termFactory.getUids(tuple.getStatusId()).iterator().next();
            long effectiveDate = tuple.getTime();

            return tupleUuid + "\t" + memberUuid + "\t" + refsetUuid + "\t" + componentUuid + "\t" + typeUuid + "\t"
                + conceptUuid + "\t" + pathUuid + "\t" + statusUuid + "\t" + effectiveDate + "\n";
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    public static I_GetConceptData importTuple(String inputLine, BufferedWriter outputFileWriter, int lineCount,
            I_ConfigAceFrame importConfig) throws TerminologyException {
        I_GetConceptData refsetConcept = null;
        try {
            String[] lineParts = inputLine.split("\t");

            UUID memberUuid;
            UUID refsetUuid;
            UUID componentUuid;
            UUID conceptUuid;
            UUID statusUuid;
            long effectiveDate;

            try {
                memberUuid = UUID.fromString(lineParts[1]);
                refsetUuid = UUID.fromString(lineParts[2]);
                refsetConcept = Terms.get().getConcept(refsetUuid);
                componentUuid = UUID.fromString(lineParts[3]);
                conceptUuid = UUID.fromString(lineParts[5]);
                if ((Boolean) importConfig.getProperty("override") == false) {
                    UUID pathUuid = UUID.fromString(lineParts[6]);
                    if (Terms.get().hasPath(Terms.get().uuidToNative(pathUuid))) {
                        importConfig.getEditingPathSet().clear();
                        importConfig.getEditingPathSet().add(Terms.get().getPath(pathUuid));
                        importConfig.setProperty("pathUuid", pathUuid);
                    } else {
                        String errorMessage =
                                "No path with identifier: " + pathUuid + " and no path override specified";
                        throw new Exception(errorMessage);
                    }
                }
                statusUuid = UUID.fromString(lineParts[7]);
            } catch (Exception e) {
                String errorMessage = "CID: Cannot parse UUID from string -> UUID " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return null;
            }

            try {
                effectiveDate = Long.parseLong(lineParts[8]);
            } catch (Exception e) {
                String errorMessage = "CID: Cannot parse Long from string: " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return null;
            }

            I_HelpSpecRefset refsetHelper = Terms.get().getSpecRefsetHelper(importConfig);
            refsetHelper.setAutocommitActive(false);
            I_TermFactory termFactory = Terms.get();

            if (!termFactory.hasId(refsetUuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "CID: Refset UUID matches no identifier in database.");
            }
            if (!termFactory.hasId(componentUuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "CID: Component UUID matches no identifier in database.");
            }
            if (!termFactory.hasId(conceptUuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "CID: conceptUuid UUID matches no identifier in database.");
            }
            if (!termFactory.hasId(statusUuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "CID: statusUuid matches no identifier in database.");
            }

            try {
                refsetHelper.newConceptRefsetExtension(termFactory.uuidToNative(refsetUuid), termFactory
                    .uuidToNative(componentUuid), termFactory.uuidToNative(conceptUuid), memberUuid,
                    (UUID) importConfig.getProperty("pathUuid"), statusUuid, effectiveDate);
            } catch (Exception e) {
                String errorMessage = "CID: Exception thrown while creating new concept refset extension";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return null;
            }

        } catch (Exception e) {
            String errorMessage =
                    "CID: Exception thrown while importing concept ext tuple : " + e.getLocalizedMessage();
            try {
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return null;
            } catch (IOException e1) {
                e1.printStackTrace();
                return null;
            }
        }
        return refsetConcept;
    }
}
