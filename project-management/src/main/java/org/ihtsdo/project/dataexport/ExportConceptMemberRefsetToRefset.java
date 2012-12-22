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
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
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
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.refset.members.RefsetUtilImpl;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.refset.ConceptMembershipRefset;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class ExportConceptMemberRefsetToRefset.
 */
public class ExportConceptMemberRefsetToRefset {

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
    int lineCount;
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
     * The formatter.
     */
    SimpleDateFormat formatter;
    /**
     * The output file writer2.
     */
    private BufferedWriter outputFileWriter2;

    /**
     * Instantiates a new export concept member refset to refset.
     */
    public ExportConceptMemberRefsetToRefset() {
        termFactory = Terms.get();
        formatter = new SimpleDateFormat("yyyyMMddHHmmss");

    }

    /**
     * Export file.
     *
     * @param exportFile the export file
     * @param exportFile2 the export file2
     * @param reportFile the report file
     * @param refsetConcept the refset concept
     * @param exportToCsv the export to csv
     * @return the long[]
     * @throws Exception the exception
     */
    public Long[] exportFile(File exportFile, File exportFile2, File reportFile, I_GetConceptData refsetConcept, boolean exportToCsv) throws Exception {

        reportFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile), "UTF8"));
        outputFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportFile), "UTF8"));
        outputFileWriter2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportFile2), "UTF8"));

        String begEnd;
        String sep;
        if (exportToCsv) {
            sep = "\",\"";
            begEnd = "\"";
        } else {
            sep = "\t";
            begEnd = "";
        }
        long UUIDlineCount = 0;
        long SCTIDlineCount = 0;
        String refsetId = refsetConcept.getUUIDs().iterator().next().toString();
        Collection<? extends I_ExtendByRef> extensions = Terms.get().getRefsetExtensionMembers(
                refsetConcept.getConceptNid());

        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

        RefsetUtilImpl rUtil = new RefsetUtilImpl();

        String refsetSCTID = rUtil.getSnomedId(refsetConcept.getNid(), Terms.get()).toString();
        try {
            Long.parseLong(refsetSCTID);
        } catch (NumberFormatException e) {

            reportFileWriter.append("The refset UUID " + refsetId
                    + " has not Snomed Concept ID, It will be replaced with its UUID." + "\r\n");

            refsetSCTID = refsetId;
        }
        I_HelpSpecRefset helper = termFactory.getSpecRefsetHelper(config);
        for (I_ExtendByRef ext : extensions) {

            I_ExtendByRefPart lastPart = TerminologyProjectDAO.getLastExtensionPart(ext);
            if (lastPart.getStatusNid() != ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid()
                    && lastPart.getStatusNid() != SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid()) {
//
//			List<? extends I_ExtendByRefVersion> tuples = ext.getTuples(helper.getCurrentStatusIntSet(), null, 
//					config.getPrecedence(),
//					config.getConflictResolutionStrategy());
//
//			if (tuples.size() > 0) {
//				I_ExtendByRefVersion thinTuple = tuples.get(0);

                //UUID file
                String id = ext.getUUIDs().iterator().next().toString();
                String effectiveTime = formatter.format(lastPart.getTime());
                String compoID = Terms.get().getConcept(ext.getComponentNid()).getUUIDs().iterator().next().toString();
                String moduleId = Terms.get().getConcept(lastPart.getPathNid()).getUids().iterator().next().toString();

                outputFileWriter.append(begEnd + id + sep + effectiveTime + sep + "1" + sep + moduleId + sep + refsetId + sep + compoID + begEnd
                        + "\r\n");

                UUIDlineCount++;

                //SCTID File
                String conceptId = rUtil.getSnomedId(ext.getComponentNid(), termFactory);
                boolean bSkip = false;
                try {
                    Long.parseLong(conceptId);
                } catch (NumberFormatException e) {
                    reportFileWriter.append("The concept UUID " + compoID + " has not Snomed Concept ID." + "\r\n");

                    bSkip = true;
                }
                if (!bSkip) {
                    String sctId_id = rUtil.getSnomedId(ext.getNid(), termFactory);
                    try {
                        Long.parseLong(sctId_id);
                    } catch (NumberFormatException e) {
                        sctId_id = id;
                    }
                    String sctId_moduleId = rUtil.getSnomedId(lastPart.getPathNid(), termFactory);
                    try {
                        Long.parseLong(sctId_moduleId);
                    } catch (NumberFormatException e) {
                        sctId_moduleId = moduleId;
                    }
                    outputFileWriter2.append(begEnd + sctId_id + sep + effectiveTime + sep + "1" + sep + sctId_moduleId + sep + refsetSCTID + sep + conceptId + begEnd
                            + "\r\n");

                    SCTIDlineCount++;

                }
            }
        }
        reportFileWriter.append("Exported to UUID file " + exportFile.getName() + " : " + UUIDlineCount + " lines" + "\r\n");
        reportFileWriter.append("Exported to SCTID file " + exportFile2.getName() + " : " + SCTIDlineCount + " lines" + "\r\n");
        reportFileWriter.flush();
        reportFileWriter.close();
        outputFileWriter.flush();
        outputFileWriter.close();
        outputFileWriter2.flush();
        outputFileWriter2.close();

        return new Long[]{UUIDlineCount, SCTIDlineCount};
    }
}