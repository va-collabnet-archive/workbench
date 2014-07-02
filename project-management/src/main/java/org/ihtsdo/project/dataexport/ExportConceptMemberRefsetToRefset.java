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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.task.refset.members.RefsetUtilImpl;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.refset.ConceptMembershipRefset;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class ExportConceptMemberRefsetToRefset.
 */
public class ExportConceptMemberRefsetToRefset {

    /**
     * The output file writer.
     */
    BufferedWriter outputUUIDFileWriter;
    /**
     * The report file writer.
     */
    BufferedWriter logFileWriter;
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
    SimpleDateFormat formatter2;
    /**
     * The output file writer2.
     */
    private BufferedWriter outputSCTFileWriter;
    private BufferedWriter outputReportFileWriter;

    /**
     * Instantiates a new export concept member refset to refset.
     * @param rf2Format 
     */
    public ExportConceptMemberRefsetToRefset() {
        termFactory = Terms.get();
        formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter2 = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
    }

    /**
     * Export file.
     *
     * @param exportUUIDFile the export file
     * @param exportSCTFile the export file2
     * @param logFile the report file
     * @param reportFile2, File reportFile3 
     * @param refsetConcept the refset concept
     * @param exportToCsv the export to csv
     * @return the long[]
     * @throws Exception the exception
     */
    public Long[] exportFile(File exportUUIDFile, File exportSCTFile, File exportReportFile, File logFile, I_GetConceptData refsetConcept, boolean exportToCsv) throws Exception {

        logFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF8"));
        outputUUIDFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportUUIDFile), "UTF8"));
        outputSCTFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportSCTFile), "UTF8"));
        outputReportFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportReportFile), "UTF8"));

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
        long reportLineCount = 0;
        String refsetId = refsetConcept.getUUIDs().iterator().next().toString();
        Collection<? extends I_ExtendByRef> extensions = Terms.get().getRefsetExtensionMembers(
                refsetConcept.getConceptNid());

        if (extensions.size() == 0) {
            AnnotationProcesser processor = new AnnotationProcesser(refsetConcept.getConceptNid());
            Terms.get().iterateConcepts(processor);
            extensions = processor.getExtensions();
        }

        RefsetUtilImpl rUtil = new RefsetUtilImpl();

        String refsetSCTID = rUtil.getSnomedId(refsetConcept.getNid(), Terms.get()).toString();
        try {
            Long.parseLong(refsetSCTID);
        } catch (NumberFormatException e) {

            logFileWriter.append("The refset UUID " + refsetId
                    + " has no Snomed Concept ID, It will be replaced with its UUID." + "\r\n");

            refsetSCTID = refsetId;
        }
        for (I_ExtendByRef ext : extensions) {

            I_ExtendByRefPart<?> lastPart = TerminologyProjectDAO.getLastExtensionPart(ext);
            if (lastPart.getStatusNid() != ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid()
                    && lastPart.getStatusNid() != SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid()) {

                //UUID file
                String id = ext.getUUIDs().iterator().next().toString();
                String effectiveTime = formatter.format(lastPart.getTime());
                String reportTime = formatter2.format(lastPart.getTime());
                String compoID = Terms.get().getConcept(ext.getComponentNid()).getUUIDs().iterator().next().toString();
                String moduleId = Terms.get().getConcept(lastPart.getPathNid()).getUids().iterator().next().toString();

                outputUUIDFileWriter.append(begEnd + id + sep + effectiveTime + sep + "1" + sep + moduleId + sep + refsetId + sep + compoID + begEnd
                        + "\r\n");

                UUIDlineCount++;

                //SCTID File
                String conceptId = rUtil.getSnomedId(ext.getComponentNid(), termFactory);
                String sctId_id = rUtil.getSnomedId(ext.getNid(), termFactory);
                String sctId_moduleId = rUtil.getSnomedId(lastPart.getPathNid(), termFactory);
                boolean hasInvalidSCTID = false;
                try
                {
                    Long.parseLong(sctId_id);
                }
                catch (Exception e)
                {
                    logFileWriter.append("The concept UUID " + id + " has no Snomed Concept ID." + "\r\n");
                    sctId_id = id;
                    hasInvalidSCTID = true;
                }
                try
                {
                    Long.parseLong(conceptId);
                }
                catch (NumberFormatException e)
                {
                    logFileWriter.append("The component UUID " + compoID + " has no Snomed Concept ID." + "\r\n");
                    conceptId = compoID;
                    hasInvalidSCTID = true;
                }

                try
                {
                    Long.parseLong(sctId_moduleId);
                }
                catch (NumberFormatException e)
                {
                    logFileWriter.append("The module concept UUID " + moduleId + " has no Snomed Concept ID." + "\r\n");
                    sctId_moduleId = moduleId;
                    hasInvalidSCTID = true;
                }
                //We ignore invalid SCTIDs, and print anyway, at the moment.
                //if (!hasInvalidSCTID)
                //{
                    outputSCTFileWriter.append(begEnd + sctId_id + sep + effectiveTime + sep + "1" + sep + sctId_moduleId + sep + refsetSCTID 
                        + sep + conceptId + begEnd + "\r\n");
                    SCTIDlineCount++;
                //}

                //Report file
                if (refsetConcept.getInitialText().contains("JIF Reactants")) {
                    I_ExtendByRefPartCid<?> concExt = (I_ExtendByRefPartCid<?>)ext;
                    outputReportFileWriter.append(begEnd + conceptId + sep + getDescForCon(conceptId) + sep + getDescForCon(concExt.getC1id()) 
                        + sep + reportTime + begEnd + "\r\n");
                } else {
                    outputReportFileWriter.append(begEnd + conceptId + sep + getDescForCon(conceptId) + sep + reportTime + begEnd + "\r\n");
                }
                reportLineCount++;
            }
        }
        logFileWriter.append("Exported to UUID file " + exportUUIDFile.getName() + " : " + UUIDlineCount + " lines" + "\r\n");
        logFileWriter.append("Exported to SCTID file " + exportSCTFile.getName() + " : " + SCTIDlineCount + " lines" + "\r\n");
        logFileWriter.append("Exported to Report file " + exportReportFile.getName() + " : " + reportLineCount + " lines" + "\r\n");
        logFileWriter.flush();
        logFileWriter.close();
        outputUUIDFileWriter.flush();
        outputUUIDFileWriter.close();
        outputSCTFileWriter.flush();
        outputSCTFileWriter.close();
        outputReportFileWriter.flush();
        outputReportFileWriter.close();

        return new Long[]{UUIDlineCount, SCTIDlineCount, reportLineCount};
    }
    
    private String getDescForCon(int nid) {
        try {
            I_GetConceptData con  = Terms.get().getConcept(nid);
            return con.getInitialText();
        } catch (Exception e) {
            e.printStackTrace();
            return "<BAD CON ID>";
        }
    }

    private String getDescForCon(String id) {
        try {
            Set<I_GetConceptData> cons = null;
            boolean isLong = true;
            try
            {
                Long.parseLong(id);
            }
            catch (NumberFormatException e)
            {
                isLong = false;
            }
            
            if (isLong) {
                cons = Terms.get().getConcept(id);
            } else {
                I_GetConceptData con = Terms.get().getConcept(UUID.fromString(id));
                cons = new HashSet<I_GetConceptData>();
                cons.add(con);
            }
            if (cons.size() != 1) {
                throw new Exception("Id: " + id + " could not be found");
            } else {
                return cons.iterator().next().getInitialText();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "<BAD CON ID>";
        }
    }

    private class AnnotationProcesser implements I_ProcessConcepts {
        private List<I_ExtendByRef> extensions = new ArrayList<I_ExtendByRef>();
        private int refsetNid;
        private int rootNid;

        public AnnotationProcesser(int refsetNid) {
                this.refsetNid = refsetNid;
                try {
                    rootNid = Ts.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8")).getConceptNid();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }

        public void processConcept(I_GetConceptData concept) throws Exception {
            if (Ts.get().isKindOf(concept.getConceptNid(), rootNid, Terms.get().getActiveAceFrameConfig().getViewCoordinate())) {
                for (RefexChronicleBI<?> annotation : concept.getAnnotations()) {
                    if (annotation.getRefexNid() == refsetNid) {
                        extensions.add((I_ExtendByRef) annotation);
                    }
                }
            } 
        }
        
        public Collection<? extends I_ExtendByRef> getExtensions() {
            return extensions;
        }
    }

}