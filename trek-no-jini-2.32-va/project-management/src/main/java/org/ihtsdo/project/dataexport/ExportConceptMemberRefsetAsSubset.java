/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.dataexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.refset.members.RefsetUtilImpl;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.refset.ConceptMembershipRefset;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class ExportConceptMemberRefsetAsSubset.
 */
public class ExportConceptMemberRefsetAsSubset {

    /**
     * The output file writer.
     */
    BufferedWriter outputFileWriter;
    /**
     * The report file writer.
     */
    BufferedWriter reportFileWriter;
    /**
     * The line count.
     */
    Long lineCount;
    /**
     * The refset concept.
     */
    ConceptMembershipRefset refsetConcept;
    /**
     * The refset uuid.
     */
    UUID refsetUUID;
    /**
     * The refset helper.
     */
    I_HelpRefsets refsetHelper;
    /**
     * The term factory.
     */
    I_TermFactory termFactory;
    /**
     * The id.
     */
    I_Identify id;

    /**
     * Instantiates a new export concept member refset as subset.
     */
    public ExportConceptMemberRefsetAsSubset() {
        termFactory = Terms.get();

    }

    /**
     * Export file.
     *
     * @param exportFile the export file
     * @param reportFile the report file
     * @param refsetConcept the refset concept
     * @param exportToCsv the export to csv
     * @return the long[]
     * @throws Exception the exception
     */
    public Long[] exportFile(File exportFile, File reportFile, I_GetConceptData refsetConcept, boolean exportToCsv) throws Exception {

        reportFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile), "UTF8"));
        outputFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportFile), "UTF8"));

        String begEnd;
        String sep;
        if (exportToCsv) {
            sep = "\",\"";
            begEnd = "\"";
        } else {
            sep = "\t";
            begEnd = "";
        }
        lineCount = 0l;
        RefsetUtilImpl rUtil = new RefsetUtilImpl();
        String subsetId = rUtil.getSnomedId(refsetConcept.getNid(), Terms.get()).toString();

        String memberStatus = "0";
        String linkedId = "";
        Collection<? extends I_ExtendByRef> extensions = Terms.get().getRefsetExtensionMembers(
                refsetConcept.getConceptNid());

        //TODO: move config to parameter
        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

        for (I_ExtendByRef ext : extensions) {

            I_ExtendByRefPart lastPart = TerminologyProjectDAO.getLastExtensionPart(ext);
            if (lastPart.getStatusNid() != ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid()
                    && lastPart.getStatusNid() != SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid()) {
//			List<? extends I_ExtendByRefVersion> tuples = ext.getTuples(helper.getCurrentStatusIntSet(), null, 
//					config.getPrecedence(),
//					config.getConflictResolutionStrategy());
//
//			if (tuples.size() > 0) {
//				I_ExtendByRefVersion thinTuple = lastPart.get(0);
                String conceptId = null;
                try {
                    conceptId = rUtil.getSnomedId(ext.getComponentNid(), Terms.get());
                } catch (IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (TerminologyException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
                try {
                    Long.parseLong(conceptId);
                } catch (NumberFormatException e) {
                    conceptId = null;
                }
                if (conceptId == null) {

                    reportFileWriter.append("The concept UUID " + termFactory.getUids(ext.getComponentNid()).iterator().next() + " has not Snomed Concept ID." + "\r\n");
//					reportFileWriter.newLine();
                } else {
//				UUID typeUuid = termFactory.getUids(thinTuple.getTypeId()).iterator().next();
//				if (!typeUuid.equals(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next())) {
//
//					reportFileWriter.println("Error on line " + lineCount + " : ");
//					reportFileWriter.println("The concept UUID " + termFactory.getUids(thinTuple.getTypeId()).iterator().next() + " has not concept extension (CONCEPT_EXT).");
////					reportFileWriter.newLine();
//					reportFileWriter.flush();
//					reportFileWriter.close();
//					outputFileWriter.flush();
//					outputFileWriter.close();
//					throw new TerminologyException("Non concept ext tuple passed to export concepts to file .");
//				}
                    outputFileWriter.append(begEnd + subsetId + sep + conceptId + sep + memberStatus + sep + linkedId + begEnd + "\r\n");
                    lineCount++;
                }
            }
        }

        reportFileWriter.append("Exported to file " + exportFile.getName() + " : " + lineCount + " lines" + "\r\n");
        reportFileWriter.flush();
        reportFileWriter.close();
        outputFileWriter.flush();
        outputFileWriter.close();

        return new Long[]{lineCount, 0l};
    }
}