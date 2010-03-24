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

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
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
            if (latestPart == null || part.getVersion() >= latestPart.getVersion()) {
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
        int effectiveDate = latestPart.getVersion();

        return tupleUuid + "\t" + primaryUuid + "\t" + sourceSystemUuid + "\t" + sourceId + "\t" + pathUuid + "\t"
            + statusUuid + "\t" + effectiveDate + "\n";
    }

    public static boolean importTuple(String inputLine, BufferedWriter outputFileWriter, int lineCount,
            UUID pathToOverrideUuid) throws TerminologyException {

        try {

            I_TermFactory termFactory = Terms.get();
            String[] lineParts = inputLine.split("\t");

            UUID primaryUuid = UUID.fromString(lineParts[1]);
            UUID sourceSystemUuid = UUID.fromString(lineParts[2]);
            String sourceId = lineParts[3];

            UUID pathUuid;
            if (pathToOverrideUuid == null) {
                pathUuid = UUID.fromString(lineParts[4]);
            } else {
                pathUuid = pathToOverrideUuid;
            }
            UUID statusUuid = UUID.fromString(lineParts[5]);
            int effectiveDate = Integer.parseInt(lineParts[6]);

            TupleFileUtil.pathUuids.add(pathUuid);

            if (!termFactory.hasId(pathUuid)) {
                String errorMessage = "pathUuid has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(pathUuid, pathUuid);
            }
            if (!termFactory.hasId(statusUuid)) {
                String errorMessage = "statusUuid has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(statusUuid, pathUuid);
            }

            if (!termFactory.hasId(primaryUuid)) {
                termFactory.uuidToNative(primaryUuid);
            }

            I_Identify versioned = termFactory.getId(primaryUuid);

            if (versioned != null) {

                if (sourceSystemUuid
                    .equals(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids().iterator().next())) {
                    versioned.addUuidId(UUID.fromString(sourceId), termFactory.uuidToNative(sourceSystemUuid),
                        termFactory.uuidToNative(statusUuid), termFactory.uuidToNative(pathUuid),
                        convert(effectiveDate));
                } else if (sourceSystemUuid.equals(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator()
                    .next())) {
                    versioned.addLongId(new Long(sourceId), termFactory.uuidToNative(sourceSystemUuid), termFactory
                        .uuidToNative(statusUuid), termFactory.uuidToNative(pathUuid), convert(effectiveDate));
                } else {
                    versioned.addStringId(sourceId, termFactory.uuidToNative(sourceSystemUuid), termFactory
                        .uuidToNative(statusUuid), termFactory.uuidToNative(pathUuid), convert(effectiveDate));
                    // use string as default
                }

                if (termFactory.hasConcept(versioned.getNid())) {
                    I_GetConceptData concept = termFactory.getConcept(versioned.getNid());
                    termFactory.addUncommitted(concept);
                }

            } else {
                throw new Exception("UUID did not exist in database: " + primaryUuid);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public static void generateIdFromUuid(UUID uuidToGenerate, UUID pathUuid) throws TerminologyException, IOException {
        Terms.get().uuidToNative(uuidToGenerate);
    }

    /**
     * Adapted from ThinVersionHelper in VODB - not accessible in ace-api.
     * 
     * @param version
     * @return
     */
    public static long convert(int version) {
        int timeZeroInt = 1830407753;
        if (version == Integer.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        if (version == Integer.MIN_VALUE) {
            return Long.MIN_VALUE;
        }
        long added = timeZeroInt + version;
        return added * 1000;
    }
}
