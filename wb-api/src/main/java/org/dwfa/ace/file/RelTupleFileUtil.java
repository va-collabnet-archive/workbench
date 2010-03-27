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

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class RelTupleFileUtil {

    public static String exportTuple(I_RelTuple relTuple) throws TerminologyException, IOException {

        try {
            I_TermFactory termFactory = Terms.get();

            UUID tupleUuid = ArchitectonicAuxiliary.Concept.REL_TUPLE.getUids().iterator().next();
            UUID relUuid = termFactory.getUids(relTuple.getRelId()).iterator().next();
            UUID c1Uuid = termFactory.getUids(relTuple.getC1Id()).iterator().next();
            UUID c2Uuid = termFactory.getUids(relTuple.getC2Id()).iterator().next();
            UUID charUuid = termFactory.getUids(relTuple.getCharacteristicId()).iterator().next();
            int group = relTuple.getGroup();
            UUID refUuid = termFactory.getUids(relTuple.getRefinabilityId()).iterator().next();
            UUID relTypeUuid = termFactory.getUids(relTuple.getTypeId()).iterator().next();
            UUID pathUuid = termFactory.getUids(relTuple.getPathId()).iterator().next();
            UUID statusUuid = termFactory.getUids(relTuple.getStatusId()).iterator().next();
            long effectiveDate = relTuple.getTime();

            String idTuple = IDTupleFileUtil.exportTuple(termFactory.getId(relUuid));

            return idTuple + tupleUuid + "\t" + relUuid + "\t" + c1Uuid + "\t" + c2Uuid + "\t" + charUuid + "\t"
                + group + "\t" + refUuid + "\t" + relTypeUuid + "\t" + pathUuid + "\t" + statusUuid + "\t"
                + effectiveDate + "\n";
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    public static boolean importTuple(String inputLine, BufferedWriter outputFileWriter, int lineCount,
            UUID pathToOverrideUuid) throws TerminologyException {

        try {
            String[] lineParts = inputLine.split("\t");

            UUID relUuid;
            UUID c1Uuid;
            UUID c2Uuid;
            UUID charUuid;
            int group;
            UUID refUuid;
            UUID relTypeUuid;
            UUID pathUuid;
            UUID statusUuid;
            long effectiveDate;

            try {
                relUuid = UUID.fromString(lineParts[1]);
                c1Uuid = UUID.fromString(lineParts[2]);
                c2Uuid = UUID.fromString(lineParts[3]);
                charUuid = UUID.fromString(lineParts[4]);
                refUuid = UUID.fromString(lineParts[6]);
                relTypeUuid = UUID.fromString(lineParts[7]);
                if (pathToOverrideUuid == null) {
                    pathUuid = UUID.fromString(lineParts[8]);
                } else {
                    pathUuid = pathToOverrideUuid;
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
                group = Integer.parseInt(lineParts[5]);
            } catch (Exception e) {
                String errorMessage = "Cannot parse Integer from string: " + e.getMessage();
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

            I_TermFactory termFactory = Terms.get();

            TupleFileUtil.pathUuids.add(pathUuid);

            if (!termFactory.hasId(pathUuid)) {
                String errorMessage = "pathUuid has no identifier - skipping import of this relationship.";
                throw new Exception(errorMessage);
            }

            if (!termFactory.hasId(c1Uuid)) {
                String errorMessage = "c1Uuid has no identifier - skipping import of this relationship.";
                throw new Exception(errorMessage);
            }
            if (!termFactory.hasId(c2Uuid)) {
                String errorMessage = "c2Uuid has no identifier - skipping import of this relationship.";
                throw new Exception(errorMessage);
            }
            if (!termFactory.hasId(charUuid)) {
                String errorMessage = "charUuid has no identifier - skipping import of this relationship.";
                throw new Exception(errorMessage);
            }
            if (!termFactory.hasId(refUuid)) {
                String errorMessage = "refUuid has no identifier - skipping import of this relationship.";
                throw new Exception(errorMessage);
            }
            if (!termFactory.hasId(relTypeUuid)) {
                String errorMessage = "relTypeUuid has no identifier - skipping import of this relationship.";
                throw new Exception(errorMessage);
            }
            if (!termFactory.hasId(statusUuid)) {
                String errorMessage = "statusUuid has no identifier - skipping import of this relationship.";
                throw new Exception(errorMessage);
            }

            I_IntSet allowedStatus = termFactory.newIntSet();
            allowedStatus.add(termFactory.getId(statusUuid).getNid());
            I_IntSet allowedTypes = termFactory.newIntSet();
            allowedTypes.add(termFactory.getId(relTypeUuid).getNid());

            I_GetConceptData concept = termFactory.getConcept(new UUID[] { c1Uuid });
            boolean returnConflictResolvedLatestState = true;
            boolean addUncommitted = true;

            // check if the part exists
            List<? extends I_RelTuple> parts =
                    concept.getSourceRelTuples(allowedStatus, allowedTypes, null, addUncommitted,
                        returnConflictResolvedLatestState);
            I_RelTuple latestTuple = null;
            for (I_RelTuple part : parts) {
                if (latestTuple == null || part.getTime() >= latestTuple.getTime()) {
                    if (part.getC1Id() == termFactory.getId(c1Uuid).getNid()
                        && part.getC2Id() == termFactory.getId(c2Uuid).getNid()) {
                        latestTuple = part;
                    }
                }
            }

            if (latestTuple == null) {
                Collection<I_Path> paths = termFactory.getPaths();
                paths.clear();
                paths.add(termFactory.getPath(new UUID[] { pathUuid }));

                I_RelVersioned v =
                        termFactory.newRelationship(relUuid, concept, termFactory
                            .getConcept(new UUID[] { relTypeUuid }), termFactory.getConcept(new UUID[] { c2Uuid }),
                            termFactory.getConcept(new UUID[] { charUuid }), termFactory
                                .getConcept(new UUID[] { refUuid }), termFactory.getConcept(new UUID[] { statusUuid }),
                            group, termFactory.getActiveAceFrameConfig());

                I_RelPart newPart =
                        (I_RelPart) v.getLastTuple().makeAnalog(termFactory.getId(statusUuid).getNid(),
                            termFactory.getId(pathUuid).getNid(), effectiveDate);
                newPart.setCharacteristicId(termFactory.getId(charUuid).getNid());
                newPart.setGroup(group);
                newPart.setRefinabilityId(termFactory.getId(refUuid).getNid());
                newPart.setTypeId(termFactory.getId(relTypeUuid).getNid());
                v.addVersion(newPart);
                termFactory.addUncommittedNoChecks(concept);
            } else {
                I_RelPart newPart =
                        (I_RelPart) latestTuple.getMutablePart().makeAnalog(termFactory.getId(statusUuid).getNid(),
                            termFactory.getId(pathUuid).getNid(), effectiveDate);
                newPart.setCharacteristicId(termFactory.getId(charUuid).getNid());
                newPart.setGroup(group);

                newPart.setRefinabilityId(termFactory.getId(refUuid).getNid());
                newPart.setTypeId(termFactory.getId(relTypeUuid).getNid());

                latestTuple.getRelVersioned().addVersion(newPart);
                termFactory.addUncommittedNoChecks(concept);
            }
        } catch (Exception e) {
            String errorMessage =
                    "Exception of unknown cause thrown while importing rel tuple : " + e.getLocalizedMessage();
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
