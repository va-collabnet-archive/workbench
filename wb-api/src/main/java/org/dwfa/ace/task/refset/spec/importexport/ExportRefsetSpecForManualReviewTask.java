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
package org.dwfa.ace.task.refset.spec.importexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.lucene.queryParser.ParseException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Exports the refset currently in the refset spec panel to the specified file,
 * in a tab delimited format of (concept name, sct id, associated comment).
 * Comment is left blank as this is filled in externally.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/export", type = BeanType.TASK_BEAN) })
public class ExportRefsetSpecForManualReviewTask extends AbstractTask {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String outputFilePropName = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();
    private TermEntry descriptionTypeTermEntry =
            new TermEntry(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
    private String refsetSpecUuidPropName = ProcessAttachmentKeys.REFSET_SPEC_UUID.getAttachmentKey();
    private Integer maxLineCount = 10000;
    private String delimiter = "\t";
    private transient Condition returnCondition = Condition.ITEM_COMPLETE;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(outputFilePropName);
        out.writeObject(descriptionTypeTermEntry);
        out.writeObject(maxLineCount);
        out.writeObject(refsetSpecUuidPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion >= 1) {
            outputFilePropName = (String) in.readObject();
            descriptionTypeTermEntry = (TermEntry) in.readObject();
            maxLineCount = (Integer) in.readObject();
            refsetSpecUuidPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        I_ShowActivity activityPanel = null;
        returnCondition = Condition.ITEM_COMPLETE;
        delimiter = "\t";
        try {
            activityPanel =
                    Terms.get().newActivityPanel(true, Terms.get().getActiveAceFrameConfig(),
                        "Exporting refset spec...", true);
            I_ConfigAceFrame configFrame =
                    (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            String outputFileName = (String) process.getProperty(outputFilePropName);

            Object obj = process.getProperty(refsetSpecUuidPropName);
            UUID uuid = null;
            if (obj == null) {
                uuid = null;
            } else {
                uuid = (UUID) obj;
            }

            if (uuid == null) {
                throw new TerminologyException("Refset spec UUID was not specified.");
            }

            // initialise the progress panel
            activityPanel.setIndeterminate(true);
            activityPanel.setProgressInfoUpper("Exporting refset spec : "
                + configFrame.getRefsetSpecInSpecEditor().getInitialText());
            activityPanel.setProgressInfoLower("<html><font color='black'> In progress.");

            exportRefset(outputFileName, Terms.get().getConcept(new UUID[] { uuid }));

            activityPanel.setProgressInfoUpper("Exporting refset spec : "
                + configFrame.getRefsetSpecInSpecEditor().getInitialText());
            activityPanel.setProgressInfoLower("<html><font color='red'> COMPLETE. <font color='black'>");

            activityPanel.complete();

            return returnCondition;
        } catch (Exception ex) {
            if (activityPanel != null) {
                ex.printStackTrace();
                try {
                    activityPanel.complete();
                } catch (ComputationCanceled e) {
                    throw new TaskFailedException(ex);
                }
            }
            throw new TaskFailedException(ex);
        }
    }

    private void exportRefset(String fileNameWithTxt, I_GetConceptData refsetSpec) throws Exception {
        String fileNameNoTxt = fileNameWithTxt.replaceAll(".txt", "");
        File outputFile = new File(fileNameNoTxt + ".txt");

        I_TermFactory termFactory = Terms.get();
        // TODO replace with passed in config...
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        RefsetSpec refsetSpecHelper = new RefsetSpec(refsetSpec, config);
        I_GetConceptData memberRefset = refsetSpecHelper.getMemberRefsetConcept();
        BufferedWriter exportFileWriter = new BufferedWriter(new FileWriter(outputFile, false));

        if (memberRefset == null) {
            throw new TerminologyException(
                "No member spec found. Please put the refset to be exported in the refset spec panel.");
        }

        Collection<? extends I_ExtendByRef> extensions =
                termFactory.getRefsetExtensionMembers(memberRefset.getConceptNid());

        writeRefsetName(exportFileWriter, memberRefset);
        writeHeader(exportFileWriter);

        I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
        int lineCount = 2; // refset name, header
        int fileNumber = 0;

        for (I_ExtendByRef ext : extensions) {

            List<? extends I_ExtendByRefVersion> tuples =
                    ext.getTuples(null, config.getViewPositionSetReadOnly(), config.getPrecedence(), config
                        .getConflictResolutionStrategy());
            I_ExtendByRefVersion latestTuple = null;

            for (I_ExtendByRefVersion currentTuple : tuples) {
                if (latestTuple == null || latestTuple.getTime() > currentTuple.getTime()) {
                    latestTuple = currentTuple;
                }
            }

            if (latestTuple != null) {
                if (latestTuple.getMutablePart() instanceof I_ExtendByRefPartCid) {
                    if (latestTuple.getRefsetId() == memberRefset.getConceptNid()) {
                        if (helper.getCurrentStatusIntSet().contains(latestTuple.getStatusNid())) {
                            I_ExtendByRefPartCid part = (I_ExtendByRefPartCid) latestTuple.getMutablePart();
                            if (part.getC1id() == termFactory.getConcept(
                                RefsetAuxiliary.Concept.NORMAL_MEMBER.getUids()).getConceptNid()) {
                                lineCount++;
                                if (lineCount > maxLineCount) {
                                    fileNumber++;
                                    lineCount = 3; // refset name + header + this record
                                    outputFile = new File(fileNameNoTxt + "-" + fileNumber + ".txt");
                                    exportFileWriter.flush();
                                    exportFileWriter.close();
                                    exportFileWriter = new BufferedWriter(new FileWriter(outputFile, false));
                                    writeRefsetName(exportFileWriter, memberRefset);
                                    writeHeader(exportFileWriter);
                                }
                                // write to file
                                String description =
                                        getDescription(descriptionTypeTermEntry, latestTuple.getComponentId());
                                if (description == null) {
                                    description = "UNKNOWN";
                                }
                                String sctId = getSctId(latestTuple.getComponentId());
                                if (sctId == null) {
                                    sctId = "";
                                }

                                exportFileWriter.write(description);
                                exportFileWriter.write(delimiter);
                                exportFileWriter.write(sctId);
                                exportFileWriter.write(delimiter);
                                exportFileWriter.newLine();
                            }
                        }
                    }
                }
            }
        }

        exportFileWriter.flush();
        exportFileWriter.close();
    }

    private void writeHeader(BufferedWriter exportFileWriter) throws IOException {
        exportFileWriter.write("Concept name");
        exportFileWriter.write(delimiter);
        exportFileWriter.write("SCT ID");
        exportFileWriter.write(delimiter);
        exportFileWriter.write("Comments");
        exportFileWriter.newLine();
    }

    private void writeRefsetName(BufferedWriter exportFileWriter, I_GetConceptData memberRefset)
            throws TerminologyException, Exception {
        exportFileWriter.write("Refset Name: ");
        exportFileWriter.write(getDescription(Terms.get().getConcept(
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), memberRefset.getConceptNid()));
        exportFileWriter.newLine();
    }

    private String getSctId(int componentId) throws TerminologyException, IOException {
        I_TermFactory termFactory = Terms.get();
        I_Identify idVersioned = termFactory.getId(componentId);
        int snomedIntegerId =
                termFactory.getId(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator().next()).getNid();

        List<? extends I_IdPart> parts = idVersioned.getMutableIdParts();
        I_IdPart latestPart = null;
        for (I_IdPart part : parts) {
            if (latestPart == null || part.getVersion() >= latestPart.getVersion()) {
                if (part.getAuthorityNid() == snomedIntegerId) {
                    latestPart = part;
                }
            }
        }

        if (latestPart != null) {
            return latestPart.getDenotation().toString();
        } else {
            return null;
        }
    }

    private String getDescription(I_GetConceptData descType, int componentId) throws Exception {
        I_TermFactory termFactory = Terms.get();
        I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(descType.getConceptNid());

        String latestDescription =
                getLatestDescription(componentId, helper.getCurrentStatusIntSet(), allowedTypes, termFactory
                    .getActiveAceFrameConfig().getViewPositionSetReadOnly());

        if (latestDescription == null) {
            // try relaxing the rules for searching - check all statuses & positions
            latestDescription = getLatestDescription(componentId, null, allowedTypes, null);
        }

        return latestDescription;
    }

    private String getLatestDescription(int componentId, I_IntSet statuses, I_IntSet allowedTypes,
            PositionSetReadOnly positions) throws IOException, TerminologyException, ParseException {
        I_TermFactory termFactory = Terms.get();
        String latestDescription = null;
        int latestVersion = Integer.MIN_VALUE;
        // TODO replace with passed in config...
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        List<? extends I_DescriptionTuple> descriptionResults = null;
        if (termFactory.hasConcept(componentId)) {
            I_GetConceptData concept = termFactory.getConcept(componentId);
            descriptionResults =
                    concept.getDescriptionTuples(statuses, allowedTypes, positions, config.getPrecedence(), config
                        .getConflictResolutionStrategy());
        } else {
            I_DescriptionVersioned descVersioned = termFactory.getDescription(componentId);
            descriptionResults =
                    descVersioned.getTuples(Terms.get().getActiveAceFrameConfig().getConflictResolutionStrategy());
        }

        // find the latest tuple, so that the latest edited version of the
        // description is always used
        for (I_DescriptionTuple descriptionTuple : descriptionResults) {
            if (descriptionTuple.getVersion() > latestVersion) {
                latestVersion = descriptionTuple.getVersion();
                latestDescription = descriptionTuple.getText();
            }
        }

        return latestDescription;
    }

    private String getDescription(TermEntry descriptionTypeTermEntry, int conceptId) throws Exception {
        I_TermFactory termFactory = Terms.get();
        I_GetConceptData descType = termFactory.getConcept(descriptionTypeTermEntry.getIds());
        return getDescription(descType, conceptId);
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    public String getOutputFilePropName() {
        return outputFilePropName;
    }

    public void setOutputFilePropName(String outputFilePropName) {
        this.outputFilePropName = outputFilePropName;
    }

    public TermEntry getDescriptionTypeTermEntry() {
        return descriptionTypeTermEntry;
    }

    public void setDescriptionTypeTermEntry(TermEntry descriptionTypeTermEntry) {
        this.descriptionTypeTermEntry = descriptionTypeTermEntry;
    }

    public Integer getMaxLineCount() {
        return maxLineCount;
    }

    public void setMaxLineCount(Integer maxLineCount) {
        this.maxLineCount = maxLineCount;
    }

    public String getRefsetSpecUuidPropName() {
        return refsetSpecUuidPropName;
    }

    public void setRefsetSpecUuidPropName(String refsetSpecUuidPropName) {
        this.refsetSpecUuidPropName = refsetSpecUuidPropName;
    }
}
