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

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.refset.members.RefsetUtilImpl;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class ConceptTupleFileUtil {

    public static I_GetConceptData lastConcept = null;

    public static String exportTuple(I_GetConceptData concept) throws TerminologyException, IOException {

        I_TermFactory termFactory = Terms.get();

        String idTuple = IDTupleFileUtil.exportTuple(termFactory.getId(concept.getUids().iterator().next()));

        RefsetUtilImpl refsetUtil = new RefsetUtilImpl();
        I_ConceptAttributePart part = refsetUtil.getLastestAttributePart(concept);

        UUID conceptTupleUuid = ArchitectonicAuxiliary.Concept.CON_TUPLE.getUids().iterator().next();
        UUID conceptUuid = termFactory.getUids(concept.getConceptId()).iterator().next();
        UUID statusUuid = termFactory.getUids(part.getStatusId()).iterator().next();
        boolean isDefined = part.isDefined();
        UUID pathUuid = termFactory.getUids(part.getPathId()).iterator().next();
        long effectiveDate = part.getTime();

        return idTuple + conceptTupleUuid + "\t" + conceptUuid + "\t" + isDefined + "\t" + pathUuid + "\t" + statusUuid
            + "\t" + effectiveDate + "\n";
    }

    public static boolean importTuple(String inputLine, BufferedWriter outputFileWriter, int lineCount,
            UUID pathToOverrideUuid) throws TerminologyException {

        try {

            String[] lineParts = inputLine.split("\t");

            UUID conceptUuid;
            boolean isDefined;
            UUID pathUuid;
            UUID statusUuid;
            long effectiveDate;

            try {
                conceptUuid = UUID.fromString(lineParts[1]);
                if (pathToOverrideUuid == null) {
                    pathUuid = UUID.fromString(lineParts[3]);
                } else {
                    pathUuid = pathToOverrideUuid;
                }
                statusUuid = UUID.fromString(lineParts[4]);
            } catch (Exception e) {
                String errorMessage = "Cannot parse UUID from string -> UUID " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            try {
                isDefined = Boolean.parseBoolean(lineParts[2]);
            } catch (Exception e) {
                String errorMessage = "Cannot parse boolean from string -> boolean " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            try {
                effectiveDate = Terms.get().convertToThickVersion(Integer.parseInt(lineParts[5]));
            } catch (Exception e) {
                String errorMessage = "Cannot parse integer from string -> integer " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            I_TermFactory termFactory = Terms.get();

            TupleFileUtil.pathUuids.add(pathUuid);

            if (!termFactory.hasId(pathUuid)) {
                String errorMessage = "pathUuid has no identifier - skipping import of this concept tuple.";
                throw new Exception(errorMessage);
            }
            if (!termFactory.hasId(statusUuid)) {
                String errorMessage = "statusUuid has no identifier - skipping import of this concept tuple.";
                throw new Exception(errorMessage);
            }

            if (termFactory.getId(conceptUuid) != null
                && termFactory.hasConcept(termFactory.getId(conceptUuid).getNid())) {

                int conceptId = termFactory.getId(conceptUuid).getNid();
                I_IntSet allowedStatus = termFactory.newIntSet();
                allowedStatus.add(termFactory.getId(statusUuid).getNid());
                I_GetConceptData concept = termFactory.getConcept(conceptId);
                lastConcept = concept;
                boolean addUncommitted = true;
                boolean returnConflictResolvedLatestState = true;

                // check if the part exists
                List<? extends I_ConceptAttributeTuple> parts =
                        concept.getConceptAttributeTuples(allowedStatus, null, addUncommitted,
                            returnConflictResolvedLatestState);

                I_ConceptAttributeTuple latestTuple = null;
                for (I_ConceptAttributeTuple part : parts) {
                    if (latestTuple == null || part.getVersion() >= latestTuple.getVersion()) {
                        latestTuple = part;
                    }
                }

                if (latestTuple == null) {
                    throw new Exception("Concept UUID exists but has no tuples.");
                } else {
                    I_ConceptAttributePart newPart =
                            (I_ConceptAttributePart) latestTuple.getMutablePart().makeAnalog(
                                termFactory.getId(statusUuid).getNid(), termFactory.getId(pathUuid).getNid(),
                                Long.MAX_VALUE);
                    newPart.setDefined(isDefined);
                    latestTuple.getConVersioned().addVersion(newPart);
                    termFactory.addUncommittedNoChecks(concept);
                }
            } else {
                // need to create concept + part
                I_GetConceptData newConcept =
                        termFactory.newConcept(conceptUuid, isDefined, termFactory.getActiveAceFrameConfig());
                I_ConceptAttributeVersioned v = newConcept.getConceptAttributes();

                lastConcept = newConcept;

                // edit the existing part's effectiveDate/version
                int index = v.getMutableParts().size() - 1;

                if (index >= 0) {
                    I_ConceptAttributePart part =
                            (I_ConceptAttributePart) v.getMutableParts().get(index).makeAnalog(
                                termFactory.getId(statusUuid).getNid(), termFactory.getId(pathUuid).getNid(),
                                effectiveDate);
                    part.setDefined(isDefined);

                    v.addVersion(part);
                }

                termFactory.addUncommittedNoChecks(newConcept);

            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage =
                    "Exception of unknown cause thrown while importing concept tuple : " + e.getLocalizedMessage();
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

    public static I_GetConceptData getLastConcept() {
        return lastConcept;
    }
}
