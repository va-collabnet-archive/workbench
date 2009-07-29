package org.dwfa.ace.task.refset.spec.compute;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JButton;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.MemberRefsetHelper;
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

    private boolean useMonitor = false;
    private boolean cancelComputation = false;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            // Nothing to do
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

        try {

            long startTime = new Date().getTime();

            configFrame = (I_ConfigAceFrame) worker
                    .readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG
                            .name());

            I_GetConceptData refsetSpec = configFrame
                    .getRefsetSpecInSpecEditor();
            if (refsetSpec == null) {
                throw new TaskFailedException("Refset spec is null.");
            }

            I_GetConceptData refset = configFrame.getRefsetInSpecEditor();
            if (refset == null) {
                throw new TaskFailedException("Refset is null.");
            }

            termFactory = LocalVersionedTerminology.get();

            I_GetConceptData normalMemberConcept = termFactory
                    .getConcept(RefsetAuxiliary.Concept.NORMAL_MEMBER.getUids());
            int conceptsToProcess = termFactory.getConceptCount();

            // initialise the progress panel
            I_ShowActivity computeRefsetActivityPanel = termFactory
                    .newActivityPanel(true);
            computeRefsetActivityPanel.setMaximum(conceptsToProcess);
            computeRefsetActivityPanel.setStringPainted(true);
            computeRefsetActivityPanel.setValue(0);
            computeRefsetActivityPanel.setIndeterminate(false);
            computeRefsetActivityPanel.setProgressInfoUpper("Computing refset "
                    + ": " + refset.getInitialText());

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

            ProgressReport progressReport = new ProgressReport();
            progressReport.setStartTime(startTime);
            computeRefsetActivityPanel.setProgressInfoLower(progressReport
                    .toString());

            // create the query object, based on the refset spec currently in
            // the refset spec editor
            RefsetSpecQuery query = RefsetQueryFactory.createQuery(configFrame,
                    termFactory, refsetSpec, refset);
            if (query.getTotalStatementCount() == 0) {
                throw new TaskFailedException(
                        "Refset spec is empty - skipping execution.");
            }

            progressReport.setStep1Complete(true);
            computeRefsetActivityPanel.setProgressInfoLower(progressReport
                    .toString());

            Iterator<I_GetConceptData> conceptIterator = termFactory
                    .getConceptIterator();
            int conceptsProcessed = 0;
            HashSet<I_GetConceptData> refsetMembers = new HashSet<I_GetConceptData>();
            HashSet<I_GetConceptData> nonRefsetMembers = new HashSet<I_GetConceptData>();
            int refsetMembersCount = 0;
            int nonRefsetMembersCount = 0;

            MemberRefsetHelper memberRefsetHelper = new MemberRefsetHelper(
                    refset.getConceptId(), normalMemberConcept.getConceptId());

            // 1. iterate over each concept and run query against it
            // (this will also execute any sub-queries)
            // 2. any concepts that meet the query criteria are added
            // to results list
            getLogger().info(
                    "Start execution of refset spec : "
                            + refsetSpec.getInitialText());
            while (!cancelComputation && conceptIterator.hasNext()) {

                I_GetConceptData currentConcept = conceptIterator.next();
                conceptsProcessed++;

                if (query.execute(currentConcept)) {
                    refsetMembers.add(currentConcept);
                } else {
                    nonRefsetMembers.add(currentConcept);
                }

                if (cancelComputation) {
                    break;
                }
                if (refsetMembers.size() > 250) {
                    // add them now
                    memberRefsetHelper.addAllToRefset(refsetMembers,
                            "Adding new members to member refset", useMonitor);

                    refsetMembersCount = refsetMembersCount
                            + refsetMembers.size();
                    refsetMembers = new HashSet<I_GetConceptData>();
                }

                if (cancelComputation) {
                    break;
                }
                if (nonRefsetMembers.size() > 250) {
                    memberRefsetHelper.removeAllFromRefset(nonRefsetMembers,
                            "Cleaning up old members from member refset",
                            useMonitor);
                    nonRefsetMembersCount = nonRefsetMembersCount
                            + nonRefsetMembers.size();
                    nonRefsetMembers = new HashSet<I_GetConceptData>();
                }

                if (cancelComputation) {
                    break;
                }
                if (conceptsProcessed % 500 == 0) {
                    progressReport.setMembersCount(refsetMembersCount
                            + refsetMembers.size());
                    progressReport
                            .setNonMembersCleanedCount(nonRefsetMembersCount
                                    + nonRefsetMembers.size());
                    computeRefsetActivityPanel
                            .setProgressInfoLower(progressReport.toString());

                    computeRefsetActivityPanel.setValue(refsetMembersCount
                            + nonRefsetMembersCount);
                }
            }

            if (cancelComputation) {
                termFactory.cancel();
                cancelButton.setEnabled(false);
                cancelButton.setVisible(false);
                progressReport.setComplete(true);
                computeRefsetActivityPanel
                        .setProgressInfoLower("User cancelled.");
                throw new TaskFailedException(
                        "User cancelled refset computation.");
            }

            progressReport.setStep2Complete(true);

            computeRefsetActivityPanel.setProgressInfoLower(progressReport
                    .toString());

            // add any remaining members
            // add concepts from list view to the refset
            // this will skip any that already exist as current members of the
            // refset
            memberRefsetHelper.addAllToRefset(refsetMembers,
                    "Adding new members to member refset", useMonitor);

            // remaining parent refsets
            memberRefsetHelper.removeAllFromRefset(nonRefsetMembers,
                    "Cleaning up old members from member refset", useMonitor);

            getLogger().info(
                    "Number of member refset members: " + refsetMembersCount
                            + refsetMembers.size());
            getLogger().info(
                    "Total number of concepts processed: " + conceptsProcessed);
            getLogger().info(
                    "End execution of refset spec : "
                            + refsetSpec.getInitialText());

            progressReport.setStep2Complete(true);
            progressReport.setMembersCount(refsetMembersCount
                    + refsetMembers.size());
            progressReport.setNonMembersCleanedCount(nonRefsetMembersCount
                    + nonRefsetMembers.size());
            progressReport.setEndTime(new Date().getTime());
            progressReport.setComplete(true);
            computeRefsetActivityPanel.setProgressInfoLower(progressReport
                    .toString());
            computeRefsetActivityPanel.complete();
            cancelButton.setEnabled(false);
            cancelButton.setVisible(false);

            if (cancelComputation) {
                termFactory.cancel();
                progressReport.setComplete(true);
                computeRefsetActivityPanel
                        .setProgressInfoLower("User cancelled.");
                throw new TaskFailedException(
                        "User cancelled refset computation.");
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
}
