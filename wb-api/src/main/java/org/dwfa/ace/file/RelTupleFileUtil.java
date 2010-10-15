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
            UUID c1Uuid = null;
            try {
                c1Uuid = termFactory.getUids(relTuple.getC1Id()).iterator().next();
            } catch (NullPointerException e) {
                c1Uuid =
                        termFactory.getUids(
                            ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.localize().getNid()).iterator()
                            .next();
            }
            UUID c2Uuid = null;
            try {
                c2Uuid = termFactory.getUids(relTuple.getC2Id()).iterator().next();
            } catch (NullPointerException e) {
                c2Uuid =
                        termFactory.getUids(
                            ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.localize().getNid()).iterator()
                            .next();
            }
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
            I_ConfigAceFrame importConfig) throws TerminologyException {

        try {
            String[] lineParts = inputLine.split("\t");

            UUID relUuid;
            UUID c1Uuid;
            UUID c2Uuid;
            UUID charUuid;
            int group;
            UUID refUuid;
            UUID relTypeUuid;
            UUID statusUuid;
            long effectiveDate;

            try {
                relUuid = UUID.fromString(lineParts[1]);
                c1Uuid = UUID.fromString(lineParts[2]);
                c2Uuid = UUID.fromString(lineParts[3]);
                charUuid = UUID.fromString(lineParts[4]);
                refUuid = UUID.fromString(lineParts[6]);
                relTypeUuid = UUID.fromString(lineParts[7]);
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
                String errorMessage = "Rel: Cannot parse UUID from string -> UUID " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            try {
                group = Integer.parseInt(lineParts[5]);
            } catch (Exception e) {
                String errorMessage = "Rel: Cannot parse Integer from string: " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            try {
                effectiveDate = Long.parseLong(lineParts[10]);
            } catch (Exception e) {
                String errorMessage = "Rel: Cannot parse Long from string: " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            I_TermFactory termFactory = Terms.get();

            I_GetConceptData pathConcept = termFactory.getConcept((UUID) importConfig.getProperty("pathUuid"));

            I_GetConceptData concept = termFactory.getConcept(new UUID[] { c1Uuid });
            int rNid = termFactory.uuidToNative(relUuid);
            assert rNid != Integer.MAX_VALUE;
            I_RelVersioned irv = termFactory.getRelationship(rNid);

            if (!termFactory.hasId(c1Uuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "Rel: c1Uuid matches no identifier in database.");
            }
            if (!termFactory.hasId(c2Uuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "Rel: c2Uuid matches no identifier in database.");
            }
            if (!termFactory.hasId(charUuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "Rel: charUuid matches no identifier in database.");
            }
            if (!termFactory.hasId(refUuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "Rel: refUuid matches no identifier in database.");
            }
            if (!termFactory.hasId(relTypeUuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "Rel: relTypeUuid matches no identifier in database.");
            }
            if (!termFactory.hasId(statusUuid)) {
                ConceptConceptConceptExtTupleFileUtil.writeWarning(outputFileWriter, lineCount,
                    "Rel: statusUuid matches no identifier in database.");
            }

            if (irv == null) {
                irv =
                        termFactory.newRelationship(relUuid, concept, termFactory.getConcept(relTypeUuid), termFactory
                            .getConcept(c2Uuid), termFactory.getConcept(charUuid), termFactory.getConcept(refUuid),
                            termFactory.getConcept(statusUuid), group, importConfig, effectiveDate);
                termFactory.addUncommittedNoChecks(concept);
            } else {
                boolean found = false;
                for (I_RelTuple irt : irv.getTuples()) {
                    if (termFactory.getConcept(relTypeUuid).getNid() == irt.getTypeId()
                        && termFactory.getConcept(c2Uuid).getNid() == irt.getC2Id()
                        && termFactory.getConcept(charUuid).getNid() == irt.getCharacteristicId()
                        && termFactory.getConcept(refUuid).getNid() == irt.getRefinabilityId()
                        && termFactory.getConcept(statusUuid).getNid() == irt.getStatusId() && group == irt.getGroup()
                        && pathConcept.getNid() == irt.getPathId()) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    I_RelPart newPart =
                            (I_RelPart) irv.getTuples().iterator().next().makeAnalog(
                                termFactory.getConcept(statusUuid).getNid(), pathConcept.getNid(), effectiveDate);
                    newPart.setTypeId(termFactory.getConcept(relTypeUuid).getNid());
                    newPart.setCharacteristicId(termFactory.getConcept(charUuid).getNid());
                    newPart.setRefinabilityId(termFactory.getConcept(refUuid).getNid());
                    newPart.setGroup(group);
                    termFactory.addUncommittedNoChecks(concept);
                }
            }
        } catch (Exception e) {
            String errorMessage = "Rel: Exception thrown while importing rel tuple : " + e.getLocalizedMessage();
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
