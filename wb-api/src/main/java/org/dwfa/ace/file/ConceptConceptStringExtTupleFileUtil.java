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
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.query.helper.RefsetHelper;

public class ConceptConceptStringExtTupleFileUtil {

    public static String exportTuple(I_ExtendByRefVersion tuple) throws TerminologyException, IOException {

        try {
            I_TermFactory termFactory = Terms.get();

            UUID tupleUuid =
                    ArchitectonicAuxiliary.Concept.EXT_CONCEPT_CONCEPT_STRING_TUPLE.getUids().iterator().next();

            UUID memberUuid = termFactory.getUids(tuple.getMemberId()).iterator().next();
            UUID refsetUuid = termFactory.getUids(tuple.getRefsetId()).iterator().next();
            UUID componentUuid = termFactory.getUids(tuple.getComponentId()).iterator().next();
            UUID typeUuid = termFactory.getUids(tuple.getTypeId()).iterator().next(); // this

            if (!typeUuid.equals(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION.getUids().iterator().next())) {
                throw new TerminologyException(
                    "Non concept-concept-string ext tuple passed to concept-concept-string file util.");
            }

            I_ExtendByRefPartCidCidString part = (I_ExtendByRefPartCidCidString) tuple.getMutablePart();
            UUID c1Uuid = termFactory.getUids(part.getC1id()).iterator().next();
            UUID c2Uuid = termFactory.getUids(part.getC2id()).iterator().next();
            String strValue = part.getString1Value();

            UUID pathUuid = termFactory.getUids(tuple.getPathId()).iterator().next();
            UUID statusUuid = termFactory.getUids(tuple.getStatusId()).iterator().next();
            long effectiveDate = tuple.getTime();

            return tupleUuid + "\t" + memberUuid + "\t" + refsetUuid + "\t" + componentUuid + "\t" + typeUuid + "\t"
                + c1Uuid + "\t" + c2Uuid + "\t" + strValue + "\t" + pathUuid + "\t" + statusUuid + "\t" + effectiveDate
                + "\n";
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
            UUID c1Uuid;
            UUID c2Uuid;
            String strValue;
            UUID statusUuid;
            long effectiveDate;

            try {
                memberUuid = UUID.fromString(lineParts[1]);
                refsetUuid = UUID.fromString(lineParts[2]);
                refsetConcept = Terms.get().getConcept(refsetUuid);
                componentUuid = UUID.fromString(lineParts[3]);
                c1Uuid = UUID.fromString(lineParts[5]);
                c2Uuid = UUID.fromString(lineParts[6]);
                strValue = lineParts[7];
                if ((Boolean) importConfig.getProperty("override") == false) {
                    UUID pathUuid = UUID.fromString(lineParts[8]);
                    if (Terms.get().hasPath(Terms.get().uuidToNative(pathUuid))) {
                        importConfig.getEditingPathSet().clear();
                        importConfig.getEditingPathSet().add(Terms.get().getPath(pathUuid));
                        importConfig.setProperty("pathUuid", pathUuid);
                    } else {
                        String errorMessage =
                                "No path with identifier: " + pathUuid
                                    + " and no path override specified. Using WorkbenchAuxiliary as path instead.";
                        outputFileWriter.write("Error on line " + lineCount + " : ");
                        outputFileWriter.write(errorMessage);
                        outputFileWriter.newLine();

                        importConfig.getEditingPathSet().clear();
                        importConfig.getEditingPathSet().add(
                            Terms.get().getPath(
                                ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids().iterator().next()));
                        importConfig.setProperty("pathUuid", ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
                            .getUids().iterator().next());
                    }
                }
                statusUuid = UUID.fromString(lineParts[9]);
            } catch (Exception e) {
                String errorMessage = "CidCidStr: Cannot parse UUID from string -> UUID " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return null;
            }

            try {
                effectiveDate = Long.parseLong(lineParts[10]);
            } catch (Exception e) {
                String errorMessage = "CidCidStr: Cannot parse Long from string: " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return null;
            }

            I_TermFactory termFactory = Terms.get();

            if (!termFactory.hasId(refsetUuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "CidCidStr: Refset UUID matches no identifier in database: " + refsetUuid);
            }
            if (!termFactory.hasId(componentUuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "CidCidStr: Component UUID matches no identifier in database: " + componentUuid);
            }
            if (!termFactory.hasId(c1Uuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "CidCidStr: c1Uuid UUID matches no identifier in database: " + c1Uuid);
            }
            if (!termFactory.hasId(c2Uuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "CidCidStr: c2Uuid matches no identifier in database: " + c2Uuid);
            }
            if (!termFactory.hasId(statusUuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "CidCidStr: statusUuid matches no identifier in database: " + statusUuid);
            }

            try {
                RefsetHelper refsetHelper = new RefsetHelper(importConfig.getViewCoordinate(), importConfig.getEditCoordinate());
                refsetHelper.newConceptConceptStringRefsetExtension(termFactory.uuidToNative(refsetUuid), termFactory
                    .uuidToNative(componentUuid), termFactory.uuidToNative(c1Uuid), termFactory.uuidToNative(c2Uuid),
                    strValue);
            } catch (Exception e) {
                String errorMessage =
                        "CidCidStr: Exception thrown while creating new concept-concept-string refset extension";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return null;
            }

        } catch (Exception e) {
            String errorMessage =
                    "CidCidStr: Exception thrown while importing concept-concept-string ext tuple : "
                        + e.getLocalizedMessage();
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
