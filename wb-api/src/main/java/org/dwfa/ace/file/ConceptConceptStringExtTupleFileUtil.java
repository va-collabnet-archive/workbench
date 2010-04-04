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
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

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
            String strValue = part.getStringValue();

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

    public static boolean importTuple(String inputLine, BufferedWriter outputFileWriter, int lineCount,
            I_ConfigAceFrame importConfig) throws TerminologyException {

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
                componentUuid = UUID.fromString(lineParts[3]);
                c1Uuid = UUID.fromString(lineParts[5]);
                c2Uuid = UUID.fromString(lineParts[6]);
                strValue = lineParts[7];
                if ((Boolean) importConfig.getProperty("override") == false) {
                    UUID pathUuid = UUID.fromString(lineParts[8]);
                    if (!Terms.get().hasId(pathUuid)) {
                        String errorMessage = "pathUuid has no identifier - skipping import of this string ext tuple.";
                        throw new Exception(errorMessage);
                    }
                    importConfig.getEditingPathSet().clear();
                    importConfig.getEditingPathSet().add(Terms.get().getPath(pathUuid));
                    importConfig.setProperty("pathUuid", pathUuid);
                } 
                statusUuid = UUID.fromString(lineParts[9]);
            } catch (Exception e) {
                String errorMessage = "Cannot parse UUID from string -> UUID " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            try {
                effectiveDate = Long.parseLong(lineParts[10]);
            } catch (Exception e) {
                String errorMessage = "Cannot parse Long from string: " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            I_HelpSpecRefset refsetHelper = Terms.get().getSpecRefsetHelper(importConfig);
            refsetHelper.setAutocommitActive(false);
            I_TermFactory termFactory = Terms.get();

            if (!termFactory.hasId(refsetUuid)) {
                String errorMessage =
                        "Refset UUID has no identifier - skipping import of this concept-concept-string ext tuple.";
                throw new Exception(errorMessage);
            }
            if (!termFactory.hasId(componentUuid)) {
                String errorMessage =
                        "Component UUID has no identifier - skipping import of this concept-concept-string ext tuple.";
                throw new Exception(errorMessage);
            }
            if (!termFactory.hasId(c1Uuid)) {
                String errorMessage =
                        "c1Uuid UUID has no identifier - skipping import of this concept-concept-string ext tuple.";
                throw new Exception(errorMessage);
            }
            if (!termFactory.hasId(c2Uuid)) {
                String errorMessage =
                        "c2Uuid has no identifier - skipping import of this concept-concept-string ext tuple.";
                throw new Exception(errorMessage);
            }
            if (!termFactory.hasId(statusUuid)) {
                String errorMessage =
                        "statusUuid has no identifier - skipping import of this concept-concept-string ext tuple.";
                throw new Exception(errorMessage);
            }

            try {
                refsetHelper.newConceptConceptStringRefsetExtension(termFactory.getId(refsetUuid).getNid(), termFactory
                    .getId(componentUuid).getNid(), termFactory.getId(c1Uuid).getNid(), termFactory.getId(c2Uuid)
                    .getNid(), strValue, memberUuid, (UUID) importConfig.getProperty("pathUuid"), statusUuid, effectiveDate);
            } catch (Exception e) {
                String errorMessage = "Exception thrown while creating new concept-concept-string refset extension";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

        } catch (Exception e) {
            String errorMessage =
                    "Exception thrown while importing concept-concept-string ext tuple : " + e.getLocalizedMessage();
            try {
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            } catch (IOException e1) {
                e1.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
