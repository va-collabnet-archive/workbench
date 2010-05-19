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

import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.refset.spec.I_HelpMemberRefset;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Computes the members of a description refset. This refset spec is the
 * one currently displayed in the refset spec editing panel. The refset spec's
 * "specifies refset" relationship indicates which member refset will be
 * created.
 * 
 * @deprecated use ComputeRefsetFromSpecTask instead
 * @author Chrissy Hill
 * 
 */
@Deprecated
@BeanList(specs = { @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN) })
public class ComputeDescRefsetFromSpecTask extends AbstractTask {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private I_TermFactory termFactory;

    private boolean cancelComputation = false;

    private Set<Integer> nestedRefsets;

    private Set<Integer> excludedRefsets;

    private Set<Integer> membersFromPreviousComputeToRemove;

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

    public Condition computeRefset(I_ConfigAceFrame configFrame, I_GetConceptData refset, boolean showActivityPanel) {

        if (refset == null) {
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                "No refset in refset spec panel to compute.", "", JOptionPane.ERROR_MESSAGE);
            AceLog.getAppLog().info("No refset in refset spec panel to compute.");
            return Condition.ITEM_CANCELED;
        }

        I_ShowActivity computeRefsetActivityPanel = null;
        ProgressReport progressReportHtmlGenerator = null;

        try {
            long startTime = new Date().getTime();
            termFactory = Terms.get();
            membersFromPreviousComputeToRemove = new HashSet<Integer>();
            I_GetConceptData normalMemberConcept =
                    termFactory.getConcept(RefsetAuxiliary.Concept.NORMAL_MEMBER.getUids());
            int descriptionsToProcess = termFactory.getConceptCount(); // TODO

            int descriptionsProcessed = 0;
            I_DescriptionVersioned currentDescription;
            List<UUID> markedParentsUuid = Arrays.asList(ConceptConstants.INCLUDES_MARKED_PARENTS_REL_TYPE.getUuids());
            Set<Integer> newMembers = new HashSet<Integer>();
            Set<Integer> retiredMembers = new HashSet<Integer>();

            // initialise the progress panel

            if (showActivityPanel) {
                computeRefsetActivityPanel = termFactory.newActivityPanel(true, configFrame, 
                    "Computing Refset: " + refset.toString(), true);
                computeRefsetActivityPanel.setStringPainted(true);
                computeRefsetActivityPanel.setValue(0);
                computeRefsetActivityPanel.setIndeterminate(true);
                progressReportHtmlGenerator = new ProgressReport();
                progressReportHtmlGenerator.setDatabaseCount(descriptionsToProcess);
                progressReportHtmlGenerator.setStartTime(startTime);
            }

            if (refset == null) {
                if (showActivityPanel) {
                    progressReportHtmlGenerator.setComplete(true);
                    computeRefsetActivityPanel.complete();
                    computeRefsetActivityPanel.setProgressInfoLower("Refset is null.");
                }

                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "No refset to compute.", "",
                    JOptionPane.ERROR_MESSAGE);
                return Condition.ITEM_CANCELED;
            }

            RefsetSpec refsetSpecHelper = new RefsetSpec(refset, true, configFrame);
            I_GetConceptData refsetSpec = refsetSpecHelper.getRefsetSpecConcept();

            // verify a valid refset spec construction
            if (refsetSpec == null) {
                if (showActivityPanel) {
                    progressReportHtmlGenerator.setComplete(true);
                    computeRefsetActivityPanel.complete();
                    computeRefsetActivityPanel.setProgressInfoLower("Refset spec is null.");
                }

                getLogger().info(
                    "Invalid refset spec to compute - unable to get spec from the refset currently in the spec panel.");
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                    "Invalid refset spec to compute - unable to get spec from the refset currently in the spec panel.",
                    "", JOptionPane.ERROR_MESSAGE);
                return Condition.ITEM_CANCELED;
            }

            if (showActivityPanel) {
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
                computeRefsetActivityPanel.addStopActionListener(new ButtonListener(this));
                computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());
            }

            // Step 1: create the query object, based on the refset spec
            RefsetSpecQuery query =
                    RefsetQueryFactory.createQuery(configFrame, termFactory, refsetSpec, refset,
                        RefsetComputeType.DESCRIPTION);

            I_HelpMemberRefset memberRefsetHelper =
                    Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig()).getMemberHelper(
                        refset.getConceptId(), normalMemberConcept.getConceptId());

            // check validity of query
            if (!query.isValidQuery() && query.getTotalStatementCount() != 0) {
                if (showActivityPanel) {
                    progressReportHtmlGenerator.setComplete(true);
                    computeRefsetActivityPanel.complete();
                    computeRefsetActivityPanel
                        .setProgressInfoLower("Refset spec has dangling AND/OR. These must have sub-statements.");
                }
                getLogger().info("Refset spec has dangling AND/OR. These must have sub-statements.");
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                    "Refset spec has dangling AND/OR. These must have sub-statements.", "", JOptionPane.ERROR_MESSAGE);
                return Condition.ITEM_CANCELED;
            }

            // compute any nested refsets (e.g. if this spec uses
            // "Concept is member of : refset2", then the members of
            // "Refset2" will be calculated as part of the computation
            Set<Integer> nestedRefsets = query.getNestedRefsets();
            this.setNestedRefsets(nestedRefsets);
            for (Integer nestedRefsetId : nestedRefsets) {
                if (!excludedRefsets.contains(nestedRefsetId)) {

                    Condition condition =
                            computeRefset(configFrame, termFactory.getConcept(nestedRefsetId), showActivityPanel);
                    if (condition == Condition.ITEM_CANCELED) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "Error computing dependant refset: "
                                + termFactory.getConcept(nestedRefsetId).getInitialText() + ". Re-run separately.", "",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    getNestedRefsets().addAll(nestedRefsets);

                }
                termFactory.commit();
            }

            getLogger().info("Start execution of refset spec : " + refsetSpec.getInitialText());

            if (showActivityPanel) {
                progressReportHtmlGenerator.setStep1Complete(true);
                computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());
            }

            // create a list of all the current refset members (this requires
            // filtering out retired versions)
            Collection<? extends I_ExtendByRef> allRefsetMembers =
                    termFactory.getRefsetExtensionMembers(refset.getConceptId());

            HashSet<Integer> currentRefsetMemberIds =
                    filterNonCurrentRefsetMembers(allRefsetMembers, memberRefsetHelper, refset.getConceptId(),
                        normalMemberConcept.getConceptId());

            // Compute the possible concepts to iterate over here...
            if (query.getTotalStatementCount() != 0) {
                I_RepresentIdSet possibleDescriptions = query.getPossibleDescriptions(configFrame, null);
                possibleDescriptions.or(termFactory.getIdSetFromIntCollection(currentRefsetMemberIds));

                getLogger().info(
                    "************* Search space: " + possibleDescriptions.cardinality() + " descriptions *******");

                if (showActivityPanel) {
                    computeRefsetActivityPanel.setMaximum(termFactory.getConceptCount());
                    computeRefsetActivityPanel.setIndeterminate(false);
                }

                I_IterateIds nidIterator = possibleDescriptions.iterator();
                while (nidIterator.next()) {
                    int nid = nidIterator.nid();
                    if (possibleDescriptions.isMember(nid)) {
                        currentDescription = termFactory.getDescription(nid);
                        descriptionsProcessed++;

                        boolean containsCurrentMember = currentRefsetMemberIds.contains(currentDescription.getDescId());

                        if (query.execute(currentDescription, configFrame)) {
                            if (!containsCurrentMember) {
                                newMembers.add(currentDescription.getDescId());
                            }
                        } else {
                            if (containsCurrentMember) {
                                retiredMembers.add(currentDescription.getDescId());
                            }
                        }

                        if (descriptionsProcessed % 10000 == 0) {
                            if (showActivityPanel) {
                                progressReportHtmlGenerator.setNewMembersCount(newMembers.size());
                                progressReportHtmlGenerator.setToBeRetiredMembersCount(retiredMembers.size());
                                computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());
                                computeRefsetActivityPanel.setValue(descriptionsProcessed);
                            }

                            getLogger().info(
                                "Concepts processed: " + descriptionsProcessed + " / " + termFactory.getConceptCount());
                            getLogger().info("New members: " + newMembers.size());
                            getLogger().info("Retired members: " + retiredMembers.size());
                        }
                        if (cancelComputation) {
                            break;
                        }
                    }
                }

                if (showActivityPanel) {
                    progressReportHtmlGenerator.setStep2Complete(true);
                    progressReportHtmlGenerator.setNewMembersCount(newMembers.size());
                    progressReportHtmlGenerator.setToBeRetiredMembersCount(retiredMembers.size());
                    computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());
                    computeRefsetActivityPanel.setIndeterminate(true);
                    computeRefsetActivityPanel.setStringPainted(false);
                }

                if (cancelComputation) {
                    termFactory.cancel();
                    if (showActivityPanel) {
                        computeRefsetActivityPanel.setStopButtonVisible(false);
                        progressReportHtmlGenerator.setComplete(true);
                        computeRefsetActivityPanel.complete();
                        computeRefsetActivityPanel.setProgressInfoLower("User cancelled.");
                    }
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "User cancelled. ", "",
                        JOptionPane.ERROR_MESSAGE);
                    return Condition.ITEM_CANCELED;
                }

            } else { // empty refset, so we need to retire all the previous members
                retiredMembers.addAll(currentRefsetMemberIds);
            }

            // Step 3 : create new member refsets
            long createComponentsStartTime = System.currentTimeMillis();
            getLogger().info("Creating new member refsets.");
            for (Integer memberId : newMembers) {
                memberRefsetHelper.newRefsetExtension(refset.getConceptId(), memberId, normalMemberConcept
                    .getConceptId(), false);
                if (cancelComputation) {
                    break;
                }
            }

            if (showActivityPanel) {
                progressReportHtmlGenerator.setStep3Complete(true);
                computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());
            }

            // Step 4: retire old member refsets
            getLogger().info("Retiring old member refsets.");
            retiredMembers.addAll(membersFromPreviousComputeToRemove);
            for (Integer retiredMemberId : retiredMembers) {
                memberRefsetHelper.retireRefsetExtension(refset.getConceptId(), retiredMemberId, normalMemberConcept
                    .getConceptId());
                if (cancelComputation) {
                    break;
                }
            }

            if (showActivityPanel) {
                progressReportHtmlGenerator.setStep4Complete(true);
                computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());
            }

            // Step 5 : add / remove marked parent refsets
            if (termFactory.hasId(markedParentsUuid)) {
                getLogger().info("Adding marked parents.");
                for (Integer newMember : newMembers) {
                    if (termFactory.hasConcept(newMember)) {
                        memberRefsetHelper.addMarkedParents(new Integer[] { newMember });
                    } else {
                        memberRefsetHelper.addDescriptionMarkedParents(new Integer[] { newMember });
                    }
                    if (cancelComputation) {
                        break;
                    }
                }

                getLogger().info("Retiring marked parents.");
                for (Integer retiredMember : retiredMembers) {
                    if (termFactory.hasConcept(retiredMember)) {
                        memberRefsetHelper.removeMarkedParents(new Integer[] { retiredMember });
                    } else {
                        memberRefsetHelper.removeDescriptionMarkedParents(new Integer[] { retiredMember });
                    }
                    if (cancelComputation) {
                        break;
                    }
                }
            }
            long elapsedTime = System.currentTimeMillis() - createComponentsStartTime;
            long minutes = elapsedTime / 60000;
            long seconds = (elapsedTime % 60000) / 1000;

            getLogger().info("Finished component creation : " + minutes + " minutes, " + seconds + " seconds.");

            if (showActivityPanel) {
                progressReportHtmlGenerator.setStep5Complete(true);
                computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());
            }

            long endTime = new Date().getTime();
            long totalMinutes = (endTime - startTime) / 60000;
            long totalSeconds = ((endTime - startTime) % 60000) / 1000;
            String executionTimeString =
                    "Total execution time: " + totalMinutes + " minutes, " + totalSeconds + " seconds.";

            getLogger().info(">>>>>>>>>>><<<<<<<<<<<");
            getLogger().info("Number of new refset members: " + newMembers.size());
            getLogger().info("Total number of concepts processed: " + descriptionsProcessed);
            getLogger().info("End execution of refset spec: " + refsetSpec.getInitialText());
            getLogger().info("Total execution time: " + executionTimeString);
            getLogger().info(">>>>>> COMPLETE <<<<<<");

            if (showActivityPanel) {
                progressReportHtmlGenerator.setMembersCount(newMembers.size());
                progressReportHtmlGenerator.setNonMembersCleanedCount(retiredMembers.size());
                progressReportHtmlGenerator.setEndTime(new Date().getTime());
                progressReportHtmlGenerator.setComplete(true);
                computeRefsetActivityPanel.setProgressInfoLower(progressReportHtmlGenerator.toString());
                computeRefsetActivityPanel.complete();
                computeRefsetActivityPanel.setStopButtonVisible(false);
            }

            if (cancelComputation) {
                termFactory.cancel();
                if (showActivityPanel) {
                    progressReportHtmlGenerator.setComplete(true);
                    computeRefsetActivityPanel.complete();
                    computeRefsetActivityPanel.setProgressInfoLower("User cancelled.");
                }

                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "User cancelled. ", "",
                    JOptionPane.ERROR_MESSAGE);
                return Condition.ITEM_CANCELED;
            }

            return Condition.ITEM_COMPLETE;
        } catch (Exception e) {

            if (showActivityPanel) {
                progressReportHtmlGenerator.setComplete(true);
                try {
                    computeRefsetActivityPanel.complete();
                } catch (ComputationCanceled e1) {
                    return Condition.ITEM_CANCELED;
                }
                computeRefsetActivityPanel.setProgressInfoLower(e.getMessage());
            }

            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), e.getMessage(), "",
                JOptionPane.ERROR_MESSAGE);

            try {
                termFactory.cancel();
                termFactory.getActiveAceFrameConfig().setCommitAbortButtonsVisible(true);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (TerminologyException termException) {
                termException.printStackTrace();
            }

            return Condition.ITEM_CANCELED;
        }
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame configFrame =
                    (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            I_GetConceptData refset = configFrame.getRefsetInSpecEditor();
            boolean showActivityPanel = true;
            excludedRefsets = new HashSet<Integer>(); // no excluded refsets when running as part of task
            nestedRefsets = new HashSet<Integer>();
            Condition condition = computeRefset(configFrame, refset, showActivityPanel);
            termFactory.getActiveAceFrameConfig().setCommitAbortButtonsVisible(true);
            return condition;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Unable to complete refset compute: "
                + ex.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            try {
                termFactory.cancel();
                termFactory.getActiveAceFrameConfig().setCommitAbortButtonsVisible(true);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (TerminologyException termException) {
                termException.printStackTrace();
            }
            return Condition.ITEM_CANCELED;
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    public boolean isCancelComputation() {
        return cancelComputation;
    }

    public void setCancelComputation(boolean cancelComputation) {
        this.cancelComputation = cancelComputation;
    }

    private class ButtonListener implements ActionListener {
        ComputeDescRefsetFromSpecTask task;

        public ButtonListener(ComputeDescRefsetFromSpecTask task) {
            this.task = task;
        }

        public void actionPerformed(ActionEvent e) {
            task.setCancelComputation(true);
        }
    }

    private HashSet<Integer> filterNonCurrentRefsetMembers(Collection<? extends I_ExtendByRef> allRefsetMembers,
            I_HelpMemberRefset refsetHelper, int refsetId, int memberTypeId) throws Exception {

        HashSet<Integer> newList = new HashSet<Integer>();
        for (I_ExtendByRef v : allRefsetMembers) {
            if (refsetHelper.hasCurrentRefsetExtension(refsetId, v.getComponentId(), memberTypeId)) {
                if (!termFactory.hasConcept(v.getComponentId())) { // TODO this needs to include rel check
                    newList.add(v.getComponentId());
                } else {
                    membersFromPreviousComputeToRemove.add(v.getComponentId());
                }
            }
        }
        return newList;
    }

    public Set<Integer> getNestedRefsets() {
        return nestedRefsets;
    }

    public void setNestedRefsets(Set<Integer> nestedRefsets) {
        this.nestedRefsets = nestedRefsets;
    }

    public Set<Integer> getExcludedRefsets() {
        return excludedRefsets;
    }

    public void setExcludedRefsets(Set<Integer> excludedRefsets) {
        this.excludedRefsets = excludedRefsets;
    }
}
