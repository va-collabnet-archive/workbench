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
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class ConceptStringExtTupleFileUtil {

    public static String exportTuple(I_ExtendByRefVersion tuple) throws TerminologyException, IOException {

        try {
            I_TermFactory termFactory = Terms.get();

            UUID tupleUuid = ArchitectonicAuxiliary.Concept.EXT_CONCEPT_STRING_TUPLE.getUids().iterator().next();

            UUID memberUuid = termFactory.getUids(tuple.getMemberId()).iterator().next();
            UUID refsetUuid = termFactory.getUids(tuple.getRefsetId()).iterator().next();
            UUID componentUuid = termFactory.getUids(tuple.getComponentId()).iterator().next();
            UUID typeUuid = termFactory.getUids(tuple.getTypeId()).iterator().next();
            if (!typeUuid.equals(RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION.getUids().iterator().next())) {
                throw new TerminologyException("Non concept string ext tuple passed to concept file util.");
            }

            I_ExtendByRefPartCidString part = (I_ExtendByRefPartCidString) tuple.getMutablePart();

            UUID conceptUuid = termFactory.getUids(part.getC1id()).iterator().next();
            UUID pathUuid = termFactory.getUids(tuple.getPathId()).iterator().next();
            UUID statusUuid = termFactory.getUids(tuple.getStatusId()).iterator().next();
            long effectiveDate = tuple.getTime();
            String extString = part.getStringValue();

            return tupleUuid + "\t" + memberUuid + "\t" + refsetUuid + "\t" + componentUuid + "\t" + typeUuid + "\t"
                + conceptUuid + "\t" + extString + "\t" + pathUuid + "\t" + statusUuid + "\t" + effectiveDate + "\n";
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
            UUID conceptUuid;
            UUID statusUuid;
            long effectiveDate;
            String extString;

            try {
                memberUuid = UUID.fromString(lineParts[1]);
                refsetUuid = UUID.fromString(lineParts[2]);
                componentUuid = UUID.fromString(lineParts[3]);
                extString = lineParts[4];
                conceptUuid = UUID.fromString(lineParts[5]);
                if ((Boolean) importConfig.getProperty("override") == false) {
                    UUID pathUuid = UUID.fromString(lineParts[7]);
                    if (Terms.get().hasPath(Terms.get().uuidToNative(pathUuid))) {
                        importConfig.getEditingPathSet().clear();
                        importConfig.getEditingPathSet().add(Terms.get().getPath(pathUuid));
                        importConfig.setProperty("pathUuid", pathUuid);
                    } else {
                        String errorMessage = "No path with identifier: " + pathUuid + " and no path override specified";
                        throw new Exception(errorMessage);
                    }
                } 
                statusUuid = UUID.fromString(lineParts[8]);
            } catch (Exception e) {
                String errorMessage = "CidStr: Cannot parse UUID from string -> UUID " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            try {
                effectiveDate = Long.parseLong(lineParts[9]);
            } catch (Exception e) {
                String errorMessage = "CidStr: Cannot parse Long from string: " + e.getMessage();
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
                        "CidStr: Refset UUID has no identifier - skipping import of this concept-string ext tuple.";
                throw new Exception(errorMessage);
            }
            if (!termFactory.hasId(componentUuid)) {
                String errorMessage =
                        "CidStr: Component UUID has no identifier - skipping import of this concept-string ext tuple.";
                throw new Exception(errorMessage);
            }
            if (!termFactory.hasId(conceptUuid)) {
                String errorMessage =
                        "CidStr: conceptUuid UUID has no identifier - skipping import of this concept-string ext tuple.";
                throw new Exception(errorMessage);
            }
            if (!termFactory.hasId(statusUuid)) {
                String errorMessage =
                        "CidStr: statusUuid has no identifier - skipping import of this concept-string ext tuple.";
                throw new Exception(errorMessage);
            }
            try {
                refsetHelper.newConceptStringRefsetExtension(termFactory.getId(refsetUuid).getNid(), termFactory.getId(
                    componentUuid).getNid(), termFactory.getId(conceptUuid).getNid(), extString, memberUuid, (UUID) importConfig.getProperty("pathUuid"),
                    statusUuid, effectiveDate);
            } catch (Exception e) {
                String errorMessage = "CidStr: Exception thrown while creating new concept string refset extension";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

        } catch (Exception e) {
            String errorMessage =
                    "CidStr: Exception thrown while importing concept string ext tuple : " + e.getLocalizedMessage();
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
