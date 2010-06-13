/**
 * 
 */
package org.ihtsdo.db.bdb.computer.refset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.refset.spec.I_HelpMemberRefset;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery;
import org.dwfa.tapi.ComputationCanceled;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_FetchConceptFromCursor;
import org.ihtsdo.concept.I_ProcessUnfetchedConceptData;
import org.ihtsdo.concept.ParallelConceptIterator;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.time.TimeUtil;

public class RefsetComputer implements I_ProcessUnfetchedConceptData {
    public class StopActionListener implements ActionListener {

        RefsetComputer computer;

        public StopActionListener(RefsetComputer computer) {
            this.computer = computer;
        }

        public void actionPerformed(ActionEvent e) {
            canceled = true;
            List<ParallelConceptIterator> pcis = computer.getParallelConceptIterators();
            for (ParallelConceptIterator pci : pcis) {
                pci.getCurrentThread().interrupt();
            }
            for (I_ShowActivity a: activities) {
            	a.cancel();
        		a.setProgressInfoLower("Cancelled.");
            }
        }
    }

    private int refsetNid;
    private RefsetSpecQuery query;
    private Collection<? extends I_ExtendByRef> allRefsetMembers;
    private I_HelpMemberRefset memberRefsetHelper;
    private I_RepresentIdSet currentRefsetMemberIds;
    private Concept refsetConcept;
    private Concept markedParentRefsetConcept;
    private AtomicInteger processedCount = new AtomicInteger();
    private AtomicInteger members = new AtomicInteger();
    private AtomicInteger newMembers = new AtomicInteger();
    private AtomicInteger retiredMembers = new AtomicInteger();
    private boolean canceled = false;
    private boolean informed = false;
    private I_ShowActivity activity;
    private Collection<I_ShowActivity> activities;
    private long startTime = System.currentTimeMillis();
    private int conceptCount;
    private I_RepresentIdSet possibleCNids;
    private I_ConfigAceFrame frameConfig;
    private RefsetSpec specHelper;

    public RefsetComputer(int refsetNid, RefsetSpecQuery query, I_ConfigAceFrame frameConfig,
            I_RepresentIdSet possibleIds, HashSet<I_ShowActivity> activities) throws Exception {
        super();
        this.activities = activities;
        this.possibleCNids = possibleIds;
        this.frameConfig = frameConfig;
        this.refsetNid = refsetNid;
        this.refsetConcept = Bdb.getConcept(refsetNid);
        conceptCount = possibleIds.cardinality();

        activity =
                Terms.get().newActivityPanel(true, frameConfig, 
                		"Computing refset: " + refsetConcept.toString(), true);
        activities.add(activity);
        activity.setIndeterminate(true);
        activity.setProgressInfoUpper("Computing refset: " + refsetConcept.toString());
        activity.setProgressInfoLower("Setting up the computer...");
        activity.addStopActionListener(new StopActionListener(this));
        ActivityViewer.addActivity(activity);

        this.query = query;
        allRefsetMembers = Terms.get().getRefsetExtensionMembers(refsetNid);

        memberRefsetHelper =
                Terms.get().getSpecRefsetHelper(frameConfig).getMemberHelper(refsetNid,
                    ReferenceConcepts.NORMAL_MEMBER.getNid());
        memberRefsetHelper.setAutocommitActive(false);
        currentRefsetMemberIds =
                filterNonCurrentRefsetMembers(allRefsetMembers, memberRefsetHelper, refsetNid,
                    ReferenceConcepts.NORMAL_MEMBER.getNid());

        markedParentRefsetConcept =
                (Concept) memberRefsetHelper.getMarkedParentRefsetForRefset(refsetConcept, frameConfig).iterator()
                    .next();

        activity.setProgressInfoLower("Starting computation...");
        activity.setValue(0);
        activity.setMaximum(possibleIds.cardinality());
        activity.setIndeterminate(false);
        specHelper = new RefsetSpec(refsetConcept, true, frameConfig);

    }

    private I_RepresentIdSet filterNonCurrentRefsetMembers(Collection<? extends I_ExtendByRef> allRefsetMembers,
            I_HelpMemberRefset refsetHelper, int refsetId, int memberTypeId) throws Exception {

        I_RepresentIdSet newList = Terms.get().getEmptyIdSet();
        for (I_ExtendByRef v : allRefsetMembers) {
            if (refsetHelper.hasCurrentRefsetExtension(refsetId, v.getComponentId(), memberTypeId)) {
                newList.setMember(v.getComponentId());
            }
        }
        return newList;
    }

    @Override
    public void processUnfetchedConceptData(int cNid, I_FetchConceptFromCursor fcfc) throws Exception {
        if (canceled) {
            if (!informed) {
                activity.setProgressInfoLower("Cancelling.");
            }
            return;
        }

        if (possibleCNids.isMember(cNid)) {
            Concept concept = fcfc.fetch();
            if (specHelper.isDescriptionComputeType()) {
                List<? extends I_DescriptionTuple> descriptionTuples =
                        concept.getDescriptionTuples(null, null, frameConfig.getViewPositionSetReadOnly(), frameConfig
                            .getPrecedence(), frameConfig.getConflictResolutionStrategy());
                for (I_DescriptionTuple tuple : descriptionTuples) {
                    I_DescriptionVersioned descVersioned = tuple.getDescVersioned();
                    executeComponent(descVersioned, cNid, 
                    		descVersioned.getDescId(), frameConfig, activities);
                }
            } else if (specHelper.isConceptComputeType()) {
                executeComponent(concept, cNid, cNid, frameConfig, activities);
            }
        }
    }

    private void executeComponent(I_AmTermComponent component, int conceptNid, 
    		int componentNid, I_ConfigAceFrame config, 
    		Collection<I_ShowActivity> activities)
            throws Exception {
        if (possibleCNids.isMember(conceptNid)) {
            boolean containsCurrentMember = currentRefsetMemberIds.isMember(componentNid);

            if (query.execute(component, config, activities)) {
                members.incrementAndGet();
                if (!containsCurrentMember) {
                    newMembers.incrementAndGet();
                    memberRefsetHelper.newRefsetExtension(refsetNid, componentNid, ReferenceConcepts.NORMAL_MEMBER
                        .getNid(), false);
                    memberRefsetHelper.addMarkedParents(new Integer[] { conceptNid });
                }
            } else {
                if (containsCurrentMember) {
                    retiredMembers.incrementAndGet();
                    memberRefsetHelper.retireRefsetExtension(refsetNid, componentNid, ReferenceConcepts.NORMAL_MEMBER
                        .getNid());
                    memberRefsetHelper.removeMarkedParents(new Integer[] { conceptNid });
                }
            }
            int completed = processedCount.incrementAndGet();
            if (completed % 500 == 0) {
                activity.setValue(completed);
                if (!canceled) {
                    long endTime = System.currentTimeMillis();

                    long elapsed = endTime - startTime;
                    String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);

                    String remainingStr = TimeUtil.getRemainingTimeString(completed, conceptCount, elapsed);

                    activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";  Remaining: " + remainingStr
                        + ";  Members: " + members.get() + " New: " + newMembers.get() + " Ret: "
                        + retiredMembers.get());
                } else {
                	for (I_ShowActivity a: activities) {
                		if (!a.isComplete()) {
                    		if (!a.isComplete()) {
                        		a.cancel();
                        		a.setProgressInfoLower("Cancelled.");
                    		}
                		}
                	}
                }
            }
        }
    }

    public void addUncommitted() throws ComputationCanceled {
        BdbCommitManager.addUncommittedNoChecks(refsetConcept);
        BdbCommitManager.addUncommittedNoChecks(markedParentRefsetConcept);
        long elapsed = System.currentTimeMillis() - startTime;
        String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);
        if (!canceled) {
            activity.setProgressInfoLower("Complete. Time: " + elapsedStr + "; Members: " + members.get() + " New: "
                + newMembers.get() + " Ret: " + retiredMembers.get());
        } else {
            activity.setProgressInfoLower("Cancelled.");
        	for (I_ShowActivity a: activities) {
        		if (!a.isComplete()) {
            		a.cancel();
            		a.setProgressInfoLower("Cancelled.");
        		}
        	}
        }
        activity.complete();
    }

    public AtomicInteger getProcessedCount() {
        return processedCount;
    }

    @Override
    public boolean continueWork() {
        return !canceled;
    }

    List<ParallelConceptIterator> pcis;

    @Override
    public void setParallelConceptIterators(List<ParallelConceptIterator> pcis) {
        this.pcis = pcis;
    }

    public List<ParallelConceptIterator> getParallelConceptIterators() {
        return pcis;
    }
}