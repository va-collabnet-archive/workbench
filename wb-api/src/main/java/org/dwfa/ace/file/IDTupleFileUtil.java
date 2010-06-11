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
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class IDTupleFileUtil {

    public static String exportTuple(I_Identify iIdVersioned) throws TerminologyException, IOException {

        List<? extends I_IdPart> parts = iIdVersioned.getMutableIdParts();
        I_IdPart latestPart = null;
        for (I_IdPart part : parts) {
            if (latestPart == null || part.getTime() >= latestPart.getTime()) {
                latestPart = part;
            }
        }

        I_TermFactory termFactory = Terms.get();

        UUID tupleUuid = ArchitectonicAuxiliary.Concept.ID_TUPLE.getUids().iterator().next();
        UUID primaryUuid = termFactory.getUids(iIdVersioned.getNid()).iterator().next();
        UUID sourceSystemUuid = termFactory.getUids(latestPart.getAuthorityNid()).iterator().next();

        Object sourceId = latestPart.getDenotation();

        UUID pathUuid = termFactory.getUids(latestPart.getPathId()).iterator().next();
        UUID statusUuid = termFactory.getUids(latestPart.getStatusId()).iterator().next();
        long effectiveDate = latestPart.getTime();

        return tupleUuid + "\t" + primaryUuid + "\t" + sourceSystemUuid + "\t" + sourceId + "\t" + pathUuid + "\t"
            + statusUuid + "\t" + effectiveDate + "\n";

    }

    public static boolean importTuple(String inputLine, BufferedWriter outputFileWriter, int lineCount,
            I_ConfigAceFrame importConfig) throws TerminologyException {

        try {

            I_TermFactory termFactory = Terms.get();
            String[] lineParts = inputLine.split("\t");

            UUID primaryUuid = UUID.fromString(lineParts[1]);
            UUID sourceSystemUuid = UUID.fromString(lineParts[2]);
            String sourceId = lineParts[3];

            if ((Boolean) importConfig.getProperty("override") == false) {
                UUID pathUuid = UUID.fromString(lineParts[4]);
                if (!Terms.get().hasId(pathUuid)) {
                    String errorMessage = "pathUuid has no identifier - skipping import of this string ext tuple.";
                    throw new Exception(errorMessage);
                }
                importConfig.getEditingPathSet().clear();
                importConfig.getEditingPathSet().add(Terms.get().getPath(pathUuid));
                importConfig.setProperty("pathUuid", pathUuid);
            } 
            UUID statusUuid = UUID.fromString(lineParts[5]);
            long effectiveDate = Long.parseLong(lineParts[6]);

            if (!termFactory.hasId(statusUuid)) {
                ConceptConceptConceptExtTupleFileUtil.
                writeWarning(outputFileWriter, lineCount, "statusUuid matches no identifier in database.");
            }

            if (!termFactory.hasId(primaryUuid)) {
                termFactory.uuidToNative(primaryUuid);
            }

            I_Identify versioned = termFactory.getId(primaryUuid);
            I_GetConceptData pathConcept = termFactory.getConcept((UUID) importConfig.getProperty("pathUuid")); 

            if (versioned != null) {
                
                boolean found = false;
                for (I_IdVersion idv: versioned.getIdVersions()) {
                    if (idv.getDenotation().toString().equals(sourceId)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    if (sourceSystemUuid.equals(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids().iterator().next())) {
                        versioned.addUuidId(UUID.fromString(sourceId), termFactory.uuidToNative(sourceSystemUuid),
                            termFactory.uuidToNative(statusUuid), pathConcept.getNid(), effectiveDate);
                    } else if (sourceSystemUuid.equals(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator().next())) {
                        versioned.addLongId(new Long(sourceId), termFactory.uuidToNative(sourceSystemUuid), termFactory
                            .uuidToNative(statusUuid), pathConcept.getNid(), effectiveDate);
                    } else {
                        versioned.addStringId(sourceId, termFactory.uuidToNative(sourceSystemUuid), 
                            termFactory.uuidToNative(statusUuid), pathConcept.getNid(), effectiveDate);
                        // use string as default
                    }

                    if (termFactory.hasConcept(versioned.getNid())) {
                        I_GetConceptData concept = termFactory.getConcept(versioned.getNid());
                        termFactory.addUncommitted(concept);
                    }
                }
            } else {
                throw new Exception("UUID did not exist in database: " + primaryUuid);
            }
        } catch (Exception e) {
            String errorMessage = "Exception while importing ID tuple : " + e.getLocalizedMessage();
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

    public static int generateIdFromUuid(UUID uuidToGenerate, UUID pathUuid) throws TerminologyException, IOException {
        return Terms.get().uuidToNative(uuidToGenerate);
    }
}
