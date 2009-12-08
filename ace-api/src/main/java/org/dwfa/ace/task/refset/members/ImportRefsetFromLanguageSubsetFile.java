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
package org.dwfa.ace.task.refset.members;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.BeanPropertyMap;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.ThinExtByRefPartProperty;
import org.dwfa.ace.batch.BatchCancelledException;
import org.dwfa.ace.batch.BatchMonitor;
import org.dwfa.ace.file.LanguageSubsetMemberReader;
import org.dwfa.ace.file.LanguageSubsetMemberReader.LanguageSubsetMemberLine;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.refset.MemberRefsetHelper;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Creates reference set extensions from items in a subset language file.
 * 
 * @author ean
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/refset", type = BeanType.TASK_BEAN) })
public class ImportRefsetFromLanguageSubsetFile extends AbstractTask {
    private static final long serialVersionUID = 2223331516185959145L;

    /** Data version for this task. */
    private static final int dataVersion = 1;

    /**
     * Ye old term factory.
     */
    private I_TermFactory termFactory;

    /** the refset we are adding to */
    private String refsetConceptPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();

    /** the name of the Language Specification file to be imported */
    private String languageSpcificationFileName = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();

    /** Message buffer. */
    private StringBuffer processMessages;

    /** Maximum number of uncommitted operations to hold. */
    private static int COMMIT_SIZE = 10000;

    /** Create/updated reset extensions. */
    private MemberRefsetHelper memberRefsetHelper;

    /** The number of processed lines */
    private long processedLineCount;

    /**
     * refset to add update/
     */
    int refsetId;

    /**
     * Acceptable (foundation metadata concept)
     */
    private int acceptableDescriptionTypeNid;

    /**
     * Preferred (foundation metatdata concept)
     */
    private int preferredDescriptionTypeNid;

    /**
     * Snomed ct root node.
     */
    private I_GetConceptData snomedCt;

    /**
     * full specified name native id
     */
    private int fsnNid;

    /**
     * prefferred term native id.
     */
    private int prefferredTermNid;

    /**
     * Process Monitor.
     */
    private BatchMonitor monitor = null;

    /**
     * Bean constructor.
     * 
     * @throws Exception if de-serialisation fails.
     */
    public ImportRefsetFromLanguageSubsetFile() throws Exception {
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.Serializable
     * 
     * @param out to write object to.
     * @throws IOException on write error.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.refsetConceptPropName);
        out.writeObject(this.languageSpcificationFileName);
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.io.Serializable
     * 
     * @param in to read object from
     * @throws IOException on read error
     * @throws ClassNotFoundException on read error
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            this.refsetConceptPropName = (String) in.readObject();
            this.languageSpcificationFileName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * nothing to do.
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    /**
     * {@inheritDoc}
     * 
     * Read in the language subset file and add new I_ThinExtByRefPartConcept to
     * the selected reference set (I_GetConceptData).
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        termFactory = LocalVersionedTerminology.get();
        processMessages = new StringBuffer();
        long readLineCount = 0;

        try {
            validateTaskData((I_GetConceptData) process.readProperty(refsetConceptPropName),
                (String) process.readProperty(languageSpcificationFileName));
            refsetId = ((I_GetConceptData) process.readProperty(refsetConceptPropName)).getConceptId();
            snomedCt = termFactory.getConcept(UUID.fromString("768e720f-8833-415a-b726-5245bbca5941"));
            acceptableDescriptionTypeNid = termFactory.getConcept(ConceptConstants.ACCEPTABLE.getUuids())
                .getConceptId();
            preferredDescriptionTypeNid = termFactory.getConcept(ConceptConstants.PREFERRED.getUuids()).getConceptId();
            fsnNid = termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids())
                .getConceptId();
            prefferredTermNid = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()).getConceptId();

            memberRefsetHelper = new MemberRefsetHelper(refsetId, RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize()
                .getNid());
            LanguageSubsetMemberReader reader = new LanguageSubsetMemberReader();
            reader.setSourceFile(new File((String) process.readProperty(languageSpcificationFileName)));
            reader.setHasHeader(true);

            monitor = new BatchMonitor("Language Import Progress", reader.getSize() + 388000, 1000, true);
            monitor.start();

            LanguageSubsetMemberLine conceptDescription = null;

            do {
                monitor.mark();
                readLineCount++;
                try {
                    conceptDescription = reader.iterator().next();
                    updateResetExtentions(monitor, refsetId, conceptDescription);
                    processedLineCount++;
                    if ((monitor.getEventCount() % COMMIT_SIZE) == 0) {
                        commit();
                    }
                } catch (Exception te) {
                    processMessages.append("<b>Error while processing line</b> " + readLineCount + " "
                        + te.getMessage() + "<br/>");
                    monitor.setText("<html>" + processMessages + "</html>");
                }
            } while (reader.iterator().hasNext());

            commit();
            termFactory.iterateConcepts(new ConceptIterator());
        } catch (Exception e) {
            getLogger().info("Exception " + e.getStackTrace());
            throw new TaskFailedException("Unable to import refset from file. " + e.getMessage(), e);
        } finally {
            commit();
            processMessages.append("<br/><br/><b>Processed " + processedLineCount + " of " + readLineCount
                + " lines sucessfully</b>");
            monitor.setText("<html>" + processMessages + "</html>");
            if (monitor != null) {
                try {
                    monitor.setFinishedButtonVisible(true);
                    monitor.complete();
                } catch (BatchCancelledException e) {
                    getLogger().info("BatchCancelledException " + e.getStackTrace());
                }
            }
        }

        return Condition.CONTINUE;
    }

    /**
     * Updates the concept extension for the refset.
     * 
     * If the concept extension exists for the refset and description
     * this will be retired if the extension type is different to the language
     * subset type and a new extension is created.
     * 
     * @param monitor used to inform the user of any errors or warnings.
     * @param refsetId int the reset to update the extension with
     * @param conceptDescription LanguageSubsetMemberLine the current line in
     *            the subset file
     * 
     * @throws IOException file read errors
     * @throws TerminologyException looking up concepts etc.
     * @throws Exception MemberRefsetHelper errors
     */
    private void updateResetExtentions(BatchMonitor monitor, int refsetId, LanguageSubsetMemberLine conceptDescription)
            throws IOException, TerminologyException, Exception {
        updateResetExtentions(monitor, refsetId, conceptDescription.getDescriptionVersioned().getDescId(),
            getSnomedDescriptionTypeForSubsetStatus(conceptDescription.getDescriptionStatusId()));
    }

    /**
     * Updates the concept extension for the refset.
     * 
     * If the concept extension exists for the refset and description
     * this will be retired if the extension type is different to the language
     * subset type and a new extension is created.
     * 
     * @param monitor used to inform the user of any errors or warnings.
     * @param refsetId int the reset to update the extension with
     * @param conceptDescription LanguageSubsetMemberLine the current line in
     *            the subset file
     * 
     * @throws IOException file read errors
     * @throws TerminologyException looking up concepts etc.
     * @throws Exception MemberRefsetHelper errors
     */
    private void updateResetExtentions(BatchMonitor monitor, int refsetId, int descriptionNid, int extensionTypeId)
            throws IOException, TerminologyException, Exception {
        // check if a current extension exists
        I_ThinExtByRefPartConcept currentRefsetExtension = memberRefsetHelper.getFirstCurrentRefsetExtension(refsetId,
            descriptionNid);
        if (currentRefsetExtension != null) {
            int currentRefsetExtensionType = currentRefsetExtension.getC1id();
            if (currentRefsetExtensionType != extensionTypeId) {
                // retire the old type.
                memberRefsetHelper.retireRefsetExtension(refsetId, descriptionNid, new BeanPropertyMap().with(
                    ThinExtByRefPartProperty.CONCEPT_ONE, currentRefsetExtensionType));
                addRetiredExtensionMessage(monitor, currentRefsetExtension);
            }
        }
        // create a new extension if not currently set.
        memberRefsetHelper.newRefsetExtension(refsetId, descriptionNid, I_ThinExtByRefPartConcept.class,
            new BeanPropertyMap().with(ThinExtByRefPartProperty.CONCEPT_ONE, extensionTypeId));
    }

    /**
     * Converts the 0, 1, 2 subset status to a snomed description type.
     * 
     * @param subsetStatusType int
     * 
     * @return nid
     */
    public int getSnomedDescriptionTypeForSubsetStatus(int subsetStatusType) {
        int snomedDescriptionType = acceptableDescriptionTypeNid;

        switch (subsetStatusType) {
        case 1:
            snomedDescriptionType = preferredDescriptionTypeNid;
        }
        return snomedDescriptionType;

    }

    /**
     * Notify the user that the concept extension was retired.
     * 
     * @param monitor BatchMonitor to add the notification to.
     * @param retiredRefsetExtension the extension that was retired.
     * @throws TerminologyException error getting the description id.
     * @throws IOException if LocalVersionedTerminology.get() fails.
     */
    private void addRetiredExtensionMessage(BatchMonitor monitor, I_ThinExtByRefPartConcept retiredRefsetExtension)
            throws IOException, TerminologyException {
        processMessages.append("<b>Retired Extension</b><br/>");
        processMessages.append(LocalVersionedTerminology.get()
            .getConcept(retiredRefsetExtension.getC1id())
            .getUids()
            .get(0)
            + "<br/>");
    }

    /**
     * Validate the required task information has been set.
     * 
     * @param refset I_GetConceptData to create a I_ThinExtByRefPartConcept to.
     * @param filename Language subset file String.
     * @throws TerminologyException if refset or filename afre null
     */
    private void validateTaskData(I_GetConceptData refset, String filename) throws TerminologyException {
        if (refset == null) {
            throw new TerminologyException("A working refset has not been selected.");
        }

        if (filename == null) {
            throw new TerminologyException("No file selected.");
        }
    }

    /**
     * Commits the current transaction.
     * 
     * @throws TaskFailedException if commits fails.
     */
    private void commit() throws TaskFailedException {
        getLogger().info("commit - Start");
        try {
            termFactory.commit();
            getLogger().info("Committed changes");
        } catch (Exception e) {
            getLogger().info("Cannot commit changes: " + e.getStackTrace());
            throw new TaskFailedException("Cannot commit changes: " + e.getMessage(), e);
        }
        getLogger().info("commit - end");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    /**
     * The reference set name.
     * 
     * @return String refset name
     */
    public String getRefsetConceptPropName() {
        return refsetConceptPropName;
    }

    /**
     * Set the reference set name.
     * 
     * @param refsetConceptPropName String
     */
    public void setRefsetConceptPropName(String refsetConceptPropName) {
        this.refsetConceptPropName = refsetConceptPropName;
    }

    /**
     * The language subset file name.
     * 
     * @return String file name.
     */
    public String getLanguageSpcificationFileName() {
        return languageSpcificationFileName;
    }

    /**
     * Set the subset file name.
     * 
     * @param languageSpcificationFileName String
     */
    public void setLanguageSpcificationFileName(String languageSpcificationFileName) {
        this.languageSpcificationFileName = languageSpcificationFileName;
    }

    /**
     * Go back over all snomed concepts and check we have a language extension
     * for both fsn and preferred term. if make it so.
     * 
     */
    class ConceptIterator implements I_ProcessConcepts {
        @Override
        public void processConcept(I_GetConceptData concept) throws Exception {
            List<I_DescriptionTuple> descriptions;
            I_DescriptionTuple latestFsn;
            I_DescriptionTuple latestPreferredTerm;

            if (snomedCt.isParentOf(concept, false)) {
                descriptions = concept.getDescriptionTuples(null, null, null, true);
                latestFsn = null;
                latestPreferredTerm = null;

                for (I_DescriptionTuple descriptionTuple : descriptions) {
                    if (isLatest(fsnNid, latestFsn, descriptionTuple)) {
                        latestFsn = descriptionTuple;
                    } else if (isLatest(prefferredTermNid, latestPreferredTerm, descriptionTuple)) {
                        latestPreferredTerm = descriptionTuple;
                    }
                }
                if (latestFsn != null && latestPreferredTerm != null) {
                    updateResetExtentions(monitor, refsetId, latestFsn.getDescId(), acceptableDescriptionTypeNid);
                    updateResetExtentions(monitor, refsetId, latestPreferredTerm.getDescId(),
                        preferredDescriptionTypeNid);
                }
            }

            if ((monitor.getEventCount() % COMMIT_SIZE) == 0) {
                commit();
            }
            monitor.mark();
        }

        /**
         * Is this the latest version of the description type.
         * 
         * @param typeNid int
         * @param currentLatest I_DescriptionTuple
         * @param description I_DescriptionTuple
         * @return true if the description is the latest.
         */
        boolean isLatest(int typeNid, I_DescriptionTuple currentLatest, I_DescriptionTuple description) {
            boolean latest = false;

            if (typeNid == description.getTypeId()
                && (currentLatest == null || description.getVersion() > currentLatest.getVersion())) {
                latest = true;
            }

            return latest;
        }
    }
}
