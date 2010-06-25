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

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.spec.SpecRefsetHelper;
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
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Exports the refset currently in the refset spec panel to the specified file,
 * in a tab delimited format of
 * (concept name, sct id, associated comment).
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
    private TermEntry descriptionTypeTermEntry = new TermEntry(
        ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
    private String refsetSpecUuidPropName = ProcessAttachmentKeys.REFSET_SPEC_UUID.getAttachmentKey();
    private Integer maxLineCount = 10000;
    private boolean addUncommitted = true;
    private boolean returnConflictResolvedLatestState = true;
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
            activityPanel = LocalVersionedTerminology.get().newActivityPanel(true,
                LocalVersionedTerminology.get().getActiveAceFrameConfig());
            I_ConfigAceFrame configFrame = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

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

            exportRefset(outputFileName, LocalVersionedTerminology.get().getConcept(new UUID[] { uuid }));

            activityPanel.setProgressInfoUpper("Exporting refset spec : "
                + configFrame.getRefsetSpecInSpecEditor().getInitialText());
            activityPanel.setProgressInfoLower("<html><font color='red'> COMPLETE. <font color='black'>");

            activityPanel.complete();

            return returnCondition;
        } catch (Exception ex) {
            if (activityPanel != null) {
                activityPanel.complete();
            }
            ex.printStackTrace();
            throw new TaskFailedException(ex);
        }
    }

    private void exportRefset(String fileNameWithTxt, I_GetConceptData refsetSpec) throws Exception {
        String fileNameNoTxt = fileNameWithTxt.replaceAll(".txt", "");
        File outputFile = new File(fileNameNoTxt + ".txt");

        I_TermFactory termFactory = LocalVersionedTerminology.get();
        RefsetSpec refsetSpecHelper = new RefsetSpec(refsetSpec);
        I_GetConceptData memberRefset = refsetSpecHelper.getMemberRefsetConcept();
        BufferedWriter exportFileWriter = new BufferedWriter(new FileWriter(outputFile, false));

        if (memberRefset == null) {
            throw new TerminologyException(
                "No member spec found. Please put the refset to be exported in the refset spec panel.");
        }

        List<I_ThinExtByRefVersioned> extensions = LocalVersionedTerminology.get().getRefsetExtensionMembers(
            memberRefset.getConceptId());

        writeHeader(exportFileWriter);

        SpecRefsetHelper helper = new SpecRefsetHelper();
        int lineCount = 1; // header
        int fileNumber = 0;

        for (I_ThinExtByRefVersioned ext : extensions) {

            List<I_ThinExtByRefTuple> tuples = ext.getTuples(helper.getCurrentStatusIntSet(), null, addUncommitted,
                returnConflictResolvedLatestState);

            for (I_ThinExtByRefTuple thinExtByRefTuple : tuples) {
                if (thinExtByRefTuple.getPart() instanceof I_ThinExtByRefPartConcept) {
                    if (thinExtByRefTuple.getRefsetId() == memberRefset.getConceptId()) {
                        I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) thinExtByRefTuple.getPart();
                        if (part.getC1id() == termFactory.getConcept(RefsetAuxiliary.Concept.NORMAL_MEMBER.getUids())
                            .getConceptId()) {
                            lineCount++;
                            if (lineCount > maxLineCount) {
                                fileNumber++;
                                lineCount = 2; // header + this record
                                outputFile = new File(fileNameNoTxt + "-" + fileNumber + ".txt");
                                exportFileWriter.flush();
                                exportFileWriter.close();
                                exportFileWriter = new BufferedWriter(new FileWriter(outputFile, false));
                                writeHeader(exportFileWriter);
                            }
                            // write to file
                            String description = getDescription(descriptionTypeTermEntry,
                                thinExtByRefTuple.getComponentId());
                            if (description == null) {
                                description = "UNKNOWN";
                            }
                            String sctId = getSctId(thinExtByRefTuple.getComponentId());
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

    private String getSctId(int componentId) throws TerminologyException, IOException {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        I_IdVersioned idVersioned = termFactory.getId(componentId);
        int snomedIntegerId = termFactory.getId(
            ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator().next()).getNativeId();

        List<I_IdPart> parts = idVersioned.getVersions();
        I_IdPart latestPart = null;
        for (I_IdPart part : parts) {
            if (latestPart == null || part.getVersion() >= latestPart.getVersion()) {
                if (part.getSource() == snomedIntegerId) {
                    latestPart = part;
                }
            }
        }

        if (latestPart != null) {
            return latestPart.getSourceId().toString();
        } else {
            return null;
        }
    }

    private String getDescription(TermEntry descriptionTypeTermEntry, int conceptId) throws Exception {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        I_GetConceptData descType = termFactory.getConcept(descriptionTypeTermEntry.getIds());
        I_GetConceptData concept = termFactory.getConcept(conceptId);
        SpecRefsetHelper helper = new SpecRefsetHelper();

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(descType.getConceptId());

        String latestDescription = null;
        int latestVersion = Integer.MIN_VALUE;

        List<I_DescriptionTuple> descriptionResults = concept.getDescriptionTuples(helper.getCurrentStatusIntSet(),
            allowedTypes, null, true);

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
