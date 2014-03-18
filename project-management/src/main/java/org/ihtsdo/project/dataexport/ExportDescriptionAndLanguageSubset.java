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
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.refset.members.RefsetUtilImpl;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.query.RefsetSpec;
//import org.ihtsdo.rf2.util.ExportUtil;

/**
 * The Class ExportDescriptionAndLanguageSubset.
 */
public class ExportDescriptionAndLanguageSubset implements I_ProcessConcepts {

    /**
     * The output desc file writer.
     */
    BufferedWriter outputDescFileWriter;
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
    I_GetConceptData refsetConcept;
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
     * The output subs file writer.
     */
    private BufferedWriter outputSubsFileWriter;
    /**
     * The CONCEP t_ retired.
     */
    private int CONCEPT_RETIRED;
    /**
     * The LIMITED.
     */
    private int LIMITED;
    /**
     * The FSN.
     */
    private int FSN;
    /**
     * The PREFERRED.
     */
    private int PREFERRED;
    /**
     * The r util.
     */
    private RefsetUtilImpl rUtil;
    /**
     * The sep.
     */
    private String sep;
    /**
     * The beg end.
     */
    private String begEnd;
    /**
     * The desc line count.
     */
    private long descLineCount;
    /**
     * The subs line count.
     */
    private long subsLineCount;
    /**
     * The refset sctid.
     */
    private String refsetSCTID;
    /**
     * The export desc file.
     */
    private File exportDescFile;
    /**
     * The export subs file.
     */
    private File exportSubsFile;
    /**
     * The excluded status.
     */
    private HashSet<Integer> excludedStatus;
    /**
     * The complete with core tems.
     */
    private boolean completeWithCoreTems;
    /**
     * The promo refset.
     */
    private ConceptChronicleBI promoRefset;
    /**
     * The config.
     */
    private I_ConfigAceFrame config;
    /**
     * The snomed root.
     */
    private I_GetConceptData snomedRoot;
    /**
     * The source refset.
     */
    private I_GetConceptData sourceRefset;

    /**
     * Instantiates a new export description and language subset.
     *
     * @param config the config
     * @param exportDescFile the export desc file
     * @param exportSubsFile the export subs file
     * @param reportFile the report file
     * @param refsetConcept the refset concept
     * @param excludedStatus the excluded status
     * @param sourceRefset the source refset
     * @param completeWithCoreTems the complete with core tems
     * @throws Exception the exception
     */
    public ExportDescriptionAndLanguageSubset(I_ConfigAceFrame config, File exportDescFile,
            File exportSubsFile, File reportFile, I_GetConceptData refsetConcept,
            HashSet<Integer> excludedStatus, I_GetConceptData sourceRefset, boolean completeWithCoreTems) throws Exception {
        termFactory = Terms.get();
        this.config = config;
        this.sourceRefset = sourceRefset;
        termFactory.setActiveAceFrameConfig(config);
        formatter = new SimpleDateFormat("yyyyMMdd");
        this.exportDescFile = exportDescFile;
        this.exportSubsFile = exportSubsFile;
        this.refsetConcept = refsetConcept;
        this.excludedStatus = excludedStatus;
        this.completeWithCoreTems = completeWithCoreTems;
        try {
            CONCEPT_RETIRED = SnomedMetadataRf2.CONCEPT_NON_CURRENT_RF2.getLenient().getNid();
            LIMITED = SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getNid();
            FSN = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid();
            //TODO change logic for detect preferred from language refset
            PREFERRED = SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid();

            snomedRoot = Terms.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));

            reportFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile), "UTF8"));
            outputDescFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportDescFile), "UTF8"));
            outputSubsFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportSubsFile), "UTF8"));
            
            RefsetSpec refsetSpec = new RefsetSpec(refsetConcept, true, config.getViewCoordinate());
            promoRefset = refsetSpec.getPromotionRefsetConcept();
            if (promoRefset == null) {
                reportFileWriter.append("The promotion refset concept for target language refset " + refsetConcept + " doesn't exists." + "\r\n");
                throw new Exception("The promotion refset concept for target language refset " + refsetConcept + " doesn't exists.");
            } else {

                sep = "\t";
                begEnd = "";

                outputDescFileWriter.append(begEnd);
                outputDescFileWriter.append("DescriptionId");
                outputDescFileWriter.append(sep);
                outputDescFileWriter.append("DescriptionStatus");
                outputDescFileWriter.append(sep);
                outputDescFileWriter.append("ConceptId");
                outputDescFileWriter.append(sep);
                outputDescFileWriter.append("Term");
                outputDescFileWriter.append(sep);
                outputDescFileWriter.append("InitialCapitalStatus");
                outputDescFileWriter.append(sep);
                outputDescFileWriter.append("DescriptionType");
                outputDescFileWriter.append(sep);
                outputDescFileWriter.append("LanguageCode");
                outputDescFileWriter.append(begEnd + "\r\n");


                outputSubsFileWriter.append(begEnd);
                outputSubsFileWriter.append("SubsetId");
                outputSubsFileWriter.append(sep);
                outputSubsFileWriter.append("MemberId");
                outputSubsFileWriter.append(sep);
                outputSubsFileWriter.append("MemberStatus");
                outputSubsFileWriter.append(sep);
                outputSubsFileWriter.append("LinkedId");
                outputSubsFileWriter.append(begEnd + "\r\n");

                descLineCount = 0l;
                subsLineCount = 0l;

                rUtil = new RefsetUtilImpl();
                refsetSCTID = rUtil.getSnomedId(refsetConcept.getNid(), termFactory).toString();
                try {
                    Long.parseLong(refsetSCTID);
                } catch (NumberFormatException e) {
                    refsetSCTID = refsetConcept.getUUIDs().iterator().next().toString();
                    reportFileWriter.append("The refset UUID " + refsetSCTID + " has not Snomed Concept ID, It will be replaced with its UUID." + "\r\n");

                }
            }

        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    /**
     * Gets the results.
     *
     * @return the results
     */
    public Long[] getResults() {
        return new Long[]{descLineCount, subsLineCount};
    }

    /**
     * Write terms.
     *
     * @param concept the concept
     * @param refset the refset
     * @return true, if successful
     */
    private boolean writeTerms(I_GetConceptData concept, I_GetConceptData refset) {
        List<ContextualizedDescription> descriptions;
        boolean bwrite = false;
//        try {
//            descriptions = ContextualizedDescription.getContextualizedDescriptions(
//                    concept.getConceptNid(), refset.getConceptNid(), true);
//
//            I_ConceptAttributeVersioned attrib = concept.getConAttrs();
//            int conceptStatus = attrib.getStatusNid();
//
//            String conceptId = rUtil.getSnomedId(concept.getConceptNid(), termFactory).toString();
//            try {
//                Long.parseLong(conceptId);
//            } catch (NumberFormatException e) {
//                conceptId = concept.getUUIDs().iterator().next().toString();
//
//                reportFileWriter.append("The concept " + conceptId + " has not Snomed Concept ID, It will be replaced with its UUID." + "\r\n");
//
//            }
//            for (I_ContextualizeDescription cdescription : descriptions) {
//                if (cdescription.getLanguageExtension() != null) {
//
//                    String did = rUtil.getSnomedId(cdescription.getDescId(), termFactory).toString();
//                    try {
//                        Long.parseLong(did);
//                    } catch (NumberFormatException e) {
//                        did = cdescription.getDescriptionVersioned().getUUIDs().iterator().next().toString();
//                        reportFileWriter.append("The description " + did + " has not Snomed Description ID, It will be replaced with its UUID." + "\r\n");
//
//                    }
//                    String dStatus = ExportUtil.getStatusType(cdescription.getDescriptionStatusId());
//
//                    String lang = cdescription.getLang();
//
//                    int typeId = cdescription.getTypeId();
//                    String dType = "";
//                    if (typeId == FSN) {
//                        dType = "3";
//                    } else {
//                        int acceptId = cdescription.getAcceptabilityId();
//                        if (acceptId == PREFERRED) {
//                            dType = "1";
//                        } else {
//                            dType = "2";
//                        }
//                    }
//                    String term = cdescription.toString();
//
//                    String ics = cdescription.isInitialCaseSignificant() ? "1" : "0";
//
//
//                    outputDescFileWriter.append(begEnd + did + sep + dStatus + sep + conceptId + sep + term + sep + ics + sep + dType + sep + lang + begEnd + "\r\n");
//
//                    descLineCount++;
//
//                    if (dStatus == "0") {
//                        outputSubsFileWriter.append(begEnd + refsetSCTID + sep + did + sep + dType + sep + begEnd + "\r\n");
//
//                        subsLineCount++;
//                    }
//                    bwrite = true;
//                }
//            }
//        } catch (TerminologyException e) {
//            AceLog.getAppLog().alertAndLogException(e);
//        } catch (IOException e) {
//            AceLog.getAppLog().alertAndLogException(e);
//        } catch (Exception e) {
//            AceLog.getAppLog().alertAndLogException(e);
//        }
        return bwrite;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api.I_GetConceptData)
     */
    @Override
    public void processConcept(I_GetConceptData concept) throws Exception {

        if (snomedRoot.isParentOf(concept)) {

            Integer statusId = TerminologyProjectDAO.getPromotionStatusIdForRefsetId(promoRefset.getConceptNid(), concept.getConceptNid(), config);

            if (statusId == null) {
                if (!writeTerms(concept, refsetConcept)) {
                    if (completeWithCoreTems) {
                        writeTerms(concept, sourceRefset);
                    }
                }
            } else if (excludedStatus.contains(statusId) && completeWithCoreTems) {
                writeTerms(concept, sourceRefset);
            } else if (!excludedStatus.contains(statusId)) {
                writeTerms(concept, refsetConcept);
            }

        }

    }

    /**
     * Close files.
     */
    public void closeFiles() {
        try {
            reportFileWriter.append("Exported to UUID file " + exportDescFile.getName() + " : " + descLineCount + " lines" + "\r\n");
            reportFileWriter.append("Exported to SCTID file " + exportSubsFile.getName() + " : " + subsLineCount + " lines" + "\r\n");
            reportFileWriter.flush();
            reportFileWriter.close();
            outputDescFileWriter.flush();
            outputDescFileWriter.close();
            outputSubsFileWriter.flush();
            outputSubsFileWriter.close();
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }
}