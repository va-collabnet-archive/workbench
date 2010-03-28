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
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class DescTupleFileUtil {

    public static String exportTuple(I_DescriptionTuple descTuple) throws TerminologyException, IOException {

        I_TermFactory termFactory = Terms.get();

        UUID tupleUuid = ArchitectonicAuxiliary.Concept.DESC_TUPLE.getUids().iterator().next();
        UUID conceptUuid = termFactory.getUids(descTuple.getConceptId()).iterator().next();
        UUID descUuid = termFactory.getUids(descTuple.getDescId()).iterator().next();
        String text = descTuple.getText();
        String lang = descTuple.getLang();
        UUID typeUuid = termFactory.getUids(descTuple.getTypeId()).iterator().next();
        UUID pathUuid = termFactory.getUids(descTuple.getPathId()).iterator().next();
        UUID statusUuid = termFactory.getUids(descTuple.getStatusId()).iterator().next();
        boolean initialCapSignificant = descTuple.isInitialCaseSignificant();
        long effectiveDate = descTuple.getTime();

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
            long effectiveDate;

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
                effectiveDate = Long.parseLong(lineParts[9]);
            } catch (Exception e) {
                String errorMessage = "Cannot parse Long from string: " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            I_TermFactory termFactory = Terms.get();

            TupleFileUtil.pathUuids.add(pathUuid);

            if (!termFactory.hasId(pathUuid)) {
                String errorMessage = "pathUuid has no identifier - skipping import of this desc tuple.";
                throw new Exception(errorMessage);
            }

            if (!termFactory.hasId(conceptUuid)) {
                String errorMessage = "conceptUuid has no identifier - skipping import of this desc tuple.";
                throw new Exception(errorMessage);
            }

            if (!termFactory.hasId(statusUuid)) {
                String errorMessage = "statusUuid has no identifier - skipping import of this desc tuple.";
                throw new Exception(errorMessage);
            }

            if (!termFactory.hasId(typeUuid)) {
                String errorMessage = "typeUuid has no identifier - skipping import of this desc tuple.";
                throw new Exception(errorMessage);
            }

            if (termFactory.getId(conceptUuid) != null) {
                int conceptId = termFactory.getId(conceptUuid).getNid();
                I_IntSet allowedStatus = termFactory.newIntSet();
                allowedStatus.add(termFactory.getId(statusUuid).getNid());
                I_IntSet allowedTypes = termFactory.newIntSet();
                allowedTypes.add(termFactory.getId(typeUuid).getNid());
                I_GetConceptData concept = termFactory.getConcept(conceptId);
                boolean returnConflictResolvedLatestState = true;

                // check if the part exists
                List<? extends I_DescriptionTuple> parts =
                        concept.getDescriptionTuples(allowedStatus, allowedTypes, null,
                            returnConflictResolvedLatestState);
                I_DescriptionTuple latestTuple = null;
                for (I_DescriptionTuple part : parts) {
                    if (latestTuple == null || part.getTime() >= latestTuple.getTime()) {
                        latestTuple = part;
                    }
                }

                if (latestTuple == null) {
                    Collection<I_Path> paths = termFactory.getPaths();
                    paths.clear();
                    paths.add(termFactory.getPath(new UUID[] { pathUuid }));
                    termFactory.uuidToNative(descUuid);

                    I_DescriptionVersioned v =
                            termFactory.newDescription(descUuid, concept, lang, text, termFactory
                                .getConcept(new UUID[] { typeUuid }), termFactory.getActiveAceFrameConfig());

                    I_DescriptionPart newLastPart =
                            (I_DescriptionPart) v.getLastTuple().makeAnalog(termFactory.getId(statusUuid).getNid(),
                                termFactory.getId(pathUuid).getNid(), effectiveDate);
                    newLastPart.setLang(lang);
                    newLastPart.setText(text);
                    newLastPart.setInitialCaseSignificant(initialCapSignificant);
                    newLastPart.setTypeId(termFactory.getId(typeUuid).getNid());

                    v.addVersion(newLastPart);
                    termFactory.addUncommittedNoChecks(concept);
                } else {
                    I_DescriptionPart newLastPart =
                            (I_DescriptionPart) latestTuple.getDescVersioned().getLastTuple().getMutablePart()
                                .makeAnalog(termFactory.getId(statusUuid).getNid(),
                                    termFactory.getId(pathUuid).getNid(), effectiveDate);
                    newLastPart.setLang(lang);
                    newLastPart.setText(text);
                    newLastPart.setInitialCaseSignificant(initialCapSignificant);
                    newLastPart.setTypeId(termFactory.getId(typeUuid).getNid());

                    latestTuple.getDescVersioned().addVersion(newLastPart);
                    termFactory.addUncommittedNoChecks(concept);
                }
            }
        } catch (Exception e) {
            String errorMessage = "Exception thrown while importing desc tuple : " + e.getLocalizedMessage();
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
