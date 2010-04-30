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
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
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
            I_ConfigAceFrame importConfig) throws TerminologyException {

        try {
            String[] lineParts = inputLine.split("\t");

            UUID conceptUuid;
            UUID descUuid;
            String text;
            String lang;
            boolean initialCapSignificant;
            UUID typeUuid;
            UUID statusUuid;
            long effectiveDate;

            try {
                conceptUuid = UUID.fromString(lineParts[1]);
                descUuid = UUID.fromString(lineParts[2]);
                typeUuid = UUID.fromString(lineParts[6]);
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
                String errorMessage = "Desc: Cannot parse UUID from string -> UUID " + e.getMessage();
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
                String errorMessage = "Desc: Cannot parse boolean " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            try {
                effectiveDate = Long.parseLong(lineParts[9]);
            } catch (Exception e) {
                String errorMessage = "Desc: Cannot parse Long from string: " + e.getMessage();
                outputFileWriter.write("Error on line " + lineCount + " : ");
                outputFileWriter.write(errorMessage);
                outputFileWriter.newLine();
                return false;
            }

            I_TermFactory termFactory = Terms.get();

            if (!termFactory.hasId(conceptUuid)) {
                String errorMessage = "Desc: conceptUuid has no identifier - skipping import of this desc tuple.";
                throw new Exception(errorMessage);
            }

            if (!termFactory.hasId(statusUuid)) {
                String errorMessage = "Desc: statusUuid has no identifier - skipping import of this desc tuple.";
                throw new Exception(errorMessage);
            }

            if (!termFactory.hasId(typeUuid)) {
                String errorMessage = "Desc: typeUuid has no identifier - skipping import of this desc tuple.";
                throw new Exception(errorMessage);
            }

            if (termFactory.hasId(conceptUuid)) {
                int conceptId = termFactory.uuidToNative(conceptUuid);
                int dNid = termFactory.uuidToNative(descUuid);
                I_GetConceptData concept = termFactory.getConcept(conceptId);
                
                I_DescriptionVersioned idv = termFactory.getDescription(dNid);
                I_GetConceptData typeConcept = termFactory.getConcept(typeUuid);
                I_GetConceptData statusConcept = termFactory.getConcept(statusUuid);
                I_GetConceptData pathConcept = termFactory.getConcept((UUID) importConfig.getProperty("pathUuid")); 
                if (idv == null) {
                    idv = termFactory.newDescription(descUuid, concept, lang, text, typeConcept, importConfig, statusConcept, effectiveDate);
                    termFactory.addUncommittedNoChecks(concept);
                } else {
                    boolean found = false;
                    for (I_DescriptionTuple idt: idv.getTuples()) {
                        if (lang.equals(idt.getLang()) &&
                                text.equals(idt.getText()) &&
                                typeConcept.getNid() == idt.getTypeId() &&
                                statusConcept.getNid() == idt.getStatusId() &&
                                pathConcept.getNid() == idt.getPathId()) {
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        I_DescriptionPart newPart = (I_DescriptionPart) idv.getTuples().iterator().next().makeAnalog(statusConcept.getNid(), 
                            pathConcept.getNid(), effectiveDate);
                        newPart.setLang(lang);
                        newPart.setText(text);
                        newPart.setTypeId(typeConcept.getNid());
                        newPart.setInitialCaseSignificant(initialCapSignificant);
                        termFactory.addUncommittedNoChecks(concept);
                    }
                }
            } else {
                outputFileWriter.write("Desc: Error on line " + lineCount + " : ");
                outputFileWriter.write("No concept id : " + conceptUuid + " for desc: " + descUuid + "\nLind: " + inputLine);
                outputFileWriter.newLine();
                return false;
            }
        } catch (Exception e) {
            String errorMessage = "Desc: Exception thrown while importing desc tuple : " + e.getLocalizedMessage();
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
