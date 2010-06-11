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
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class DescTupleFileUtil {

    public static String exportTuple(I_DescriptionTuple descTuple) throws TerminologyException, IOException {

        I_TermFactory termFactory = LocalVersionedTerminology.get();

        UUID tupleUuid = ArchitectonicAuxiliary.Concept.DESC_TUPLE.getUids().iterator().next();
        UUID conceptUuid = termFactory.getUids(descTuple.getConceptId()).iterator().next();
        UUID descUuid = termFactory.getUids(descTuple.getDescId()).iterator().next();
        String text = descTuple.getText();
        String lang = descTuple.getLang();
        UUID typeUuid = termFactory.getUids(descTuple.getTypeId()).iterator().next();
        UUID pathUuid = termFactory.getUids(descTuple.getPathId()).iterator().next();
        UUID statusUuid = termFactory.getUids(descTuple.getStatusId()).iterator().next();
        boolean initialCapSignificant = descTuple.getInitialCaseSignificant();
        int effectiveDate = descTuple.getVersion();

        String idTuple = IDTupleFileUtil.exportTuple(termFactory.getId(descUuid));

        return idTuple + tupleUuid + "\t" + conceptUuid + "\t" + descUuid + "\t" + text + "\t" + lang + "\t"
            + initialCapSignificant + "\t" + typeUuid + "\t" + pathUuid + "\t" + statusUuid + "\t" + effectiveDate
            + "\n";
    }

    public static boolean importTuple(String inputLine, BufferedWriter outputFileWriter, int lineCount,
            UUID pathToOverrideUuid) throws TerminologyException {

        try {
            String[] lineParts = inputLine.split("\t");

            UUID conceptUuid;
            UUID descUuid;
            String text;
            String lang;
            boolean initialCapSignificant;
            UUID typeUuid;
            UUID pathUuid;
            UUID statusUuid;
            int effectiveDate;

            try {
                conceptUuid = UUID.fromString(lineParts[1]);
                descUuid = UUID.fromString(lineParts[2]);
                typeUuid = UUID.fromString(lineParts[6]);
                if (pathToOverrideUuid == null) {
                    pathUuid = UUID.fromString(lineParts[7]);
                } else {
                    pathUuid = pathToOverrideUuid;
                }
                statusUuid = UUID.fromString(lineParts[8]);
            } catch (Exception e) {
                String errorMessage = "Cannot parse UUID from string -> UUID " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            try {
                text = lineParts[3];
                lang = lineParts[4];
                initialCapSignificant = Boolean.parseBoolean(lineParts[5]);
            } catch (Exception e) {
                String errorMessage = "Cannot parse boolean " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            try {
                effectiveDate = Integer.parseInt(lineParts[9]);
            } catch (Exception e) {
                String errorMessage = "Cannot parse Integer from string -> Integer " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            I_TermFactory termFactory = LocalVersionedTerminology.get();

            TupleFileUtil.pathUuids.add(pathUuid);

            if (!termFactory.hasId(pathUuid)) {
                String errorMessage = "pathUuid has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(pathUuid, pathUuid);
            }
            if (!termFactory.hasId(descUuid)) {
                String errorMessage = "descUuid has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(descUuid, pathUuid);
            }
            if (!termFactory.hasId(conceptUuid)) {
                String errorMessage = "conceptUuid has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(conceptUuid, pathUuid);
            }

            if (!termFactory.hasId(statusUuid)) {
                String errorMessage = "statusUuid has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(statusUuid, pathUuid);
            }

            if (!termFactory.hasId(typeUuid)) {
                String errorMessage = "typeUuid has no identifier - importing with temporary assigned ID.";
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();

                IDTupleFileUtil.generateIdFromUuid(typeUuid, pathUuid);
            }

            int conceptId = termFactory.getId(conceptUuid).getNativeId();
            I_IntSet allowedStatus = termFactory.newIntSet();
            allowedStatus.add(termFactory.getId(statusUuid).getNativeId());
            I_IntSet allowedTypes = termFactory.newIntSet();
            allowedTypes.add(termFactory.getId(typeUuid).getNativeId());
            I_GetConceptData concept = termFactory.getConcept(conceptId);
            // Set<I_Position> positions =
            // termFactory.getActiveAceFrameConfig().getViewPositionSet();
            boolean returnConflictResolvedLatestState = true;

            // check if the part exists
            List<I_DescriptionTuple> parts = concept.getDescriptionTuples(allowedStatus, allowedTypes, null,
                returnConflictResolvedLatestState);
            I_DescriptionTuple latestTuple = null;
            for (I_DescriptionTuple part : parts) {
                if (latestTuple == null || part.getVersion() >= latestTuple.getVersion()) {
                    latestTuple = part;
                }
            }

            if (latestTuple == null) {
                Collection<I_Path> paths = termFactory.getPaths();
                paths.clear();
                paths.add(termFactory.getPath(new UUID[] { pathUuid }));
                termFactory.uuidToNativeWithGeneration(descUuid,
                    ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), paths, effectiveDate);

                I_DescriptionVersioned v = termFactory.newDescription(descUuid, concept, lang, text,
                    termFactory.getConcept(new UUID[] { typeUuid }), termFactory.getActiveAceFrameConfig());

                I_DescriptionPart newLastPart = v.getLastTuple().getPart();
                newLastPart.setLang(lang);
                newLastPart.setText(text);
                newLastPart.setInitialCaseSignificant(initialCapSignificant);
                newLastPart.setTypeId(termFactory.getId(typeUuid).getNativeId());
                newLastPart.setStatusId(termFactory.getId(statusUuid).getNativeId());
                newLastPart.setPathId(termFactory.getId(pathUuid).getNativeId());
                newLastPart.setVersion(effectiveDate);

                v.addVersion(newLastPart);
                termFactory.addUncommittedNoChecks(concept);
            } else {
                I_DescriptionPart newLastPart = latestTuple.getDescVersioned().getLastTuple().getPart().duplicate();
                newLastPart.setLang(lang);
                newLastPart.setText(text);
                newLastPart.setInitialCaseSignificant(initialCapSignificant);
                newLastPart.setTypeId(termFactory.getId(typeUuid).getNativeId());
                newLastPart.setStatusId(termFactory.getId(statusUuid).getNativeId());
                newLastPart.setPathId(termFactory.getId(pathUuid).getNativeId());
                newLastPart.setVersion(effectiveDate);

                latestTuple.getDescVersioned().addVersion(newLastPart);
                termFactory.addUncommittedNoChecks(concept);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Exception of unknown cause thrown while importing desc tuple : "
                + e.getLocalizedMessage();
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
