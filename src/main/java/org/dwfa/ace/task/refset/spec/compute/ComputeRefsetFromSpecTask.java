package org.dwfa.ace.task.refset.spec.compute;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JButton;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.refset.spec.SpecMemberRefsetHelper;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Computes the members of a refset given a refset spec. This refset spec is the
 * one currently displayed in the refset spec editing panel. The refset spec's
 * "specifies refset" relationship indicates which member refset will be
 * created.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN) })
public class ComputeRefsetFromSpecTask extends AbstractTask {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private I_ConfigAceFrame configFrame;
    private I_TermFactory termFactory;

    private boolean cancelComputation = false;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            // Nothing to do
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            configFrame = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            long startTime = new Date().getTime();
            termFactory = LocalVersionedTerminology.get();
            I_GetConceptData normalMemberConcept =
                    termFactory.getConcept(RefsetAuxiliary.Concept.NORMAL_MEMBER.getUids());
            int conceptsToProcess = termFactory.getConceptCount();

            int conceptsProcessed = 0;
            I_GetConceptData currentConcept;
            List<UUID> markedParentsUuid = Arrays.asList(ConceptConstants.INCLUDES_MARKED_PARENTS_REL_TYPE.getUuids());
            Set<Integer> newMembers = new HashSet<Integer>();
            Set<Integer> retiredMembers = new HashSet<Integer>();

            // initialise the progress panel
            I_ShowActivity computeRefsetActivityPanel = termFactory.newActivityPanel(true, configFrame);
            computeRefsetActivityPanel.setStringPainted(true);
            computeRefsetActivityPanel.setValue(0);
            computeRefsetActivityPanel.setIndeterminate(true);
            ProgressReport progressReportHtmlGenerator = new ProgressReport();
            progressReportHtmlGenerator.setDatabaseCount(conceptsToProcess);
            progressReportHtmlGenerator.setStartTime(startTime);

            // verify a refset spec has been set in the refset spec editor panel
            I_GetConceptData refsetSpec = configFrame.getRefsetSpecInSpecEditor();
            if (refsetSpec == null) {
                progressReportHtmlGenerator.setComplete(true);
                computeRefsetActivityPanel.complete();
                computeRefsetActivityPanel.setProgressInfoLower("Refset spec is null.");
                throw new TaskFailedException("Refset spec is null.");
            }
            I_GetConceptData refset = configFrame.getRefsetInSpecEditor();
            if (refset == null) {
                progressReportHtmlGenerator.setComplete(true);
                computeRefsetActivityPanel.complete();
                computeRefsetActivityPanel.setProgressInfoLower("Refset is null.");
                throw new TaskFailedException("Refset is null.");
            }

            computeRefsetActivityPanel.setProgressInfoUpper("Computing refset " + ": " + refset.getInitialText());

            // set up cancel button on activity viewer panel
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = 0;
            c.gridheight = 1;
            c.weightx = 0;
            c.weighty = 0;
            c.anchor = GridBagConstraints.EAST;
            c.fill = GridBagConstraints.HORIZONTAL;
            JButton cancelButton = computeRefsetActivityPanel.getStopButton();
            if (cancelButton == null) {
                cancelButton = new JButton("Cancel");
                computeRefsetActivityPanel.getViewPanel().add(cancelButton, c);
            }
            cancelButton.addActionListener(new ButtonListener(this));

            computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());

            // Step 1: create the query object, based on the refset spec
            RefsetSpecQuery query = RefsetQueryFactory.createQuery(configFrame, termFactory, refsetSpec, refset);
            // RefsetSpecQuery possibleQuery =
            // RefsetQueryFactory.createPossibleQuery(configFrame, termFactory, refsetSpec, refset);
            SpecMemberRefsetHelper memberRefsetHelper =
                    new SpecMemberRefsetHelper(refset.getConceptId(), normalMemberConcept.getConceptId());

            // check validity of query
            if (query.getTotalStatementCount() == 0) {
                progressReportHtmlGenerator.setComplete(true);
                computeRefsetActivityPanel.complete();
                computeRefsetActivityPanel.setProgressInfoLower("Refset spec is empty - skipping execution.");
                throw new TaskFailedException("Refset spec is empty - skipping execution.");
            }
            if (!query.isValidQuery()) {
                progressReportHtmlGenerator.setComplete(true);
                computeRefsetActivityPanel.complete();
                computeRefsetActivityPanel
                    .setProgressInfoLower("Refset spec has dangling AND/OR. These must have sub-statements.");
                throw new TaskFailedException("Refset spec has dangling AND/OR. These must have sub-statements.");
            }

            progressReportHtmlGenerator.setStep1Complete(true);
            computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());

            getLogger().info("Start execution of refset spec : " + refsetSpec.getInitialText());

            // create a list of all the current refset members (this requires
            // filtering out retired versions)
            List<I_ThinExtByRefVersioned> allRefsetMembers =
                    termFactory.getRefsetExtensionMembers(refset.getConceptId());

            HashSet<Integer> currentRefsetMemberIds =
                    filterNonCurrentRefsetMembers(allRefsetMembers, memberRefsetHelper, refset.getConceptId(),
                        normalMemberConcept.getConceptId());
            // Compute the possible concepts to iterate over here...
            I_RepresentIdSet possibleConcepts = query.getPossibleConcepts(configFrame, null);
            possibleConcepts.or(termFactory.getIdSetFromIntCollection(currentRefsetMemberIds));

            getLogger().info("************* Search space: " + possibleConcepts.size() + " concepts *******");

            computeRefsetActivityPanel.setMaximum(termFactory.getConceptCount());
            computeRefsetActivityPanel.setIndeterminate(false);

            I_IterateIds nidIterator = possibleConcepts.iterator();
            while (nidIterator.next()) {
                int nid = nidIterator.nid();
                if (possibleConcepts.isMember(nid)) {
                    currentConcept = termFactory.getConcept(nid);
                    conceptsProcessed++;

                    boolean containsCurrentMember = currentRefsetMemberIds.contains(currentConcept.getConceptId());
                    if (query.execute(currentConcept)) {
                        if (!containsCurrentMember) {
                            newMembers.add(currentConcept.getConceptId());
                        }
                    } else {
                        if (containsCurrentMember) {
                            retiredMembers.add(currentConcept.getConceptId());
                        }
                    }

                    if (conceptsProcessed % 10000 == 0) {
                        progressReportHtmlGenerator.setNewMembersCount(newMembers.size());
                        progressReportHtmlGenerator.setToBeRetiredMembersCount(retiredMembers.size());
                        computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());
                        computeRefsetActivityPanel.setValue(conceptsProcessed);
                    }
                    if (cancelComputation) {
                        break;
                    }
                }
            }

            progressReportHtmlGenerator.setStep2Complete(true);
            progressReportHtmlGenerator.setNewMembersCount(newMembers.size());
            progressReportHtmlGenerator.setToBeRetiredMembersCount(retiredMembers.size());
            computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());
            computeRefsetActivityPanel.setIndeterminate(true);
            computeRefsetActivityPanel.setStringPainted(false);

            if (cancelComputation) {
                termFactory.cancel();
                cancelButton.setEnabled(false);
                cancelButton.setVisible(false);
                progressReportHtmlGenerator.setComplete(true);
                computeRefsetActivityPanel.complete();
                computeRefsetActivityPanel.setProgressInfoLower("User cancelled.");
                throw new TaskFailedException("User cancelled refset computation.");
            }

            // Step 3 : create new member refsets
            for (Integer memberId : newMembers) {
                memberRefsetHelper.newRefsetExtension(refset.getConceptId(), memberId, normalMemberConcept
                    .getConceptId(), false);
                if (cancelComputation) {
                    break;
                }
            }
            progressReportHtmlGenerator.setStep3Complete(true);
            computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());

            // Step 4: retire old member refsets
            for (Integer retiredMemberId : retiredMembers) {
                memberRefsetHelper.retireRefsetExtension(refset.getConceptId(), retiredMemberId, normalMemberConcept
                    .getConceptId());
                if (cancelComputation) {
                    break;
                }
            }
            progressReportHtmlGenerator.setStep4Complete(true);
            computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());

            // Step 5 : add / remove marked parent refsets
            if (termFactory.hasId(markedParentsUuid)) {

                for (Integer newMember : newMembers) {
                    memberRefsetHelper.addMarkedParents(new Integer[] { newMember });
                    if (cancelComputation) {
                        break;
                    }
                }

                for (Integer retiredMember : retiredMembers) {
                    memberRefsetHelper.removeMarkedParents(new Integer[] { retiredMember });
                    if (cancelComputation) {
                        break;
                    }
                }
            }
            progressReportHtmlGenerator.setStep5Complete(true);
            computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());

            getLogger().info("Number of new refset members: " + newMembers.size());
            getLogger().info("Total number of concepts processed: " + conceptsProcessed);
            getLogger().info("End execution of refset spec : " + refsetSpec.getInitialText());

            progressReportHtmlGenerator.setMembersCount(newMembers.size());
            progressReportHtmlGenerator.setNonMembersCleanedCount(retiredMembers.size());
            progressReportHtmlGenerator.setEndTime(new Date().getTime());
            progressReportHtmlGenerator.setComplete(true);
            computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());
            computeRefsetActivityPanel.complete();
            cancelButton.setEnabled(false);
            cancelButton.setVisible(false);

            if (cancelComputation) {
                termFactory.cancel();
                progressReportHtmlGenerator.setComplete(true);
                computeRefsetActivityPanel.setProgressInfoLower("User cancelled.");
                throw new TaskFailedException("User cancelled refset computation.");
            }

            return Condition.CONTINUE;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TaskFailedException(ex);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public boolean isCancelComputation() {
        return cancelComputation;
    }

    public void setCancelComputation(boolean cancelComputation) {
        this.cancelComputation = cancelComputation;
    }

    private class ButtonListener implements ActionListener {
        ComputeRefsetFromSpecTask task;

        public ButtonListener(ComputeRefsetFromSpecTask task) {
            this.task = task;
        }

        public void actionPerformed(ActionEvent e) {
            task.setCancelComputation(true);
        }
    }

    private HashSet<Integer> filterNonCurrentRefsetMembers(List<I_ThinExtByRefVersioned> list,
            SpecMemberRefsetHelper refsetHelper, int refsetId, int memberTypeId) throws Exception {

        HashSet<Integer> newList = new HashSet<Integer>();
        for (I_ThinExtByRefVersioned v : list) {
            if (refsetHelper.hasCurrentRefsetExtension(refsetId, v.getComponentId(), memberTypeId)) {
                newList.add(v.getComponentId());
            }
        }
        return newList;
    }
}
