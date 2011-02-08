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
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.refset.spec.I_HelpMemberRefset;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_FetchConceptFromCursor;
import org.ihtsdo.concept.I_ProcessUnfetchedConceptData;
import org.ihtsdo.concept.ParallelConceptIterator;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.db.bdb.computer.kindof.IsaCache;
import org.ihtsdo.db.bdb.computer.kindof.KindOfComputer;
import org.ihtsdo.time.TimeUtil;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public class RefsetComputer implements I_ProcessUnfetchedConceptData {
	
	private static final int SETUP_ISA_CACHE_THRESHOLD = 5000;

	private enum ComputeType { CONCEPT, DESCRIPTION };
    public class StopActionListener implements ActionListener {

        RefsetComputer computer;

        public StopActionListener(RefsetComputer computer) {
            this.computer = computer;
        }

        public void actionPerformed(ActionEvent e) {
            canceled = true;
            List<ParallelConceptIterator> pcis = computer.getParallelConceptIterators();
            for (ParallelConceptIterator pci : pcis) {
                pci.stop();
            }
            for (I_ShowActivity a : activities) {
                a.cancel();
                a.setProgressInfoLower("Cancelled.");
            }
        }
    }

    private int refsetNid;
    private RefsetSpecQuery query;
    private Collection<? extends I_ExtendByRef> allRefsetMembers;
    private I_HelpMemberRefset memberRefsetHelper;
    private I_RepresentIdSet currentRefsetMemberComponentNids;
    private I_RepresentIdSet newMemberNids;
    private I_RepresentIdSet retiredMemberNids;
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
    private ComputeType computeType;
	private IsaCache isaCache;

    public RefsetComputer(int refsetNid, RefsetSpecQuery query, I_ConfigAceFrame frameConfig,
            I_RepresentIdSet possibleIds, HashSet<I_ShowActivity> activities) throws Exception {
        super();
        this.activities = activities;
        this.possibleCNids = possibleIds;
        this.frameConfig = frameConfig;
        this.refsetNid = refsetNid;
        this.refsetConcept = Bdb.getConcept(refsetNid);
        this.newMemberNids = Bdb.getConceptDb().getEmptyIdSet();
        this.retiredMemberNids = Bdb.getConceptDb().getEmptyIdSet();
        
        conceptCount = possibleIds.cardinality();

        activity =
                Terms.get().newActivityPanel(true, frameConfig, "Computing refset: " + refsetConcept.toString(), true);
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
        currentRefsetMemberComponentNids =
                filterNonCurrentRefsetMembers((Collection<RefsetMember<?, ?>>) allRefsetMembers, memberRefsetHelper, refsetNid,
                    ReferenceConcepts.NORMAL_MEMBER.getNid());

        markedParentRefsetConcept =
                (Concept) memberRefsetHelper.getMarkedParentRefsetForRefset(refsetConcept, frameConfig).iterator()
                    .next();
        
        activity.setProgressInfoLower("Setting up is-a cache...");
        if (possibleIds.cardinality() > SETUP_ISA_CACHE_THRESHOLD) {
            isaCache = KindOfComputer.setupIsaCacheAndWait();
        }

        activity.setProgressInfoLower("Starting computation...");
        activity.setValue(0);
        activity.setMaximum(possibleIds.cardinality());
        activity.setIndeterminate(false);
        specHelper = new RefsetSpec(refsetConcept, true, frameConfig);
        if (specHelper.isDescriptionComputeType()) {
        	computeType = ComputeType.DESCRIPTION;
        } else if (specHelper.isConceptComputeType()) {
        	computeType = ComputeType.CONCEPT;
        } else {
        	throw new UnsupportedOperationException("Unknown compute type.");
        }

    }

    private I_RepresentIdSet filterNonCurrentRefsetMembers(Collection<RefsetMember<?, ?>> allRefsetMembers,
            I_HelpMemberRefset refsetHelper, int refsetId, int memberTypeId) throws Exception {

        I_RepresentIdSet newList = Terms.get().getEmptyIdSet();
        ViewCoordinate coord = frameConfig.getViewCoordinate();
        for (RefsetMember<?, ?> e : allRefsetMembers) {
            if (e.getVersions(coord).size() > 0) {
                newList.setMember(e.getComponentNid());
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
            switch (computeType) {
			case CONCEPT:
                executeComponent(concept, cNid, cNid, frameConfig, activities);
				break;
			case DESCRIPTION:
                List<? extends I_DescriptionTuple> descriptionTuples =
                    concept.getDescriptionTuples(null, null, frameConfig.getViewPositionSetReadOnly(), frameConfig
                        .getPrecedence(), frameConfig.getConflictResolutionStrategy());
                for (I_DescriptionTuple tuple : descriptionTuples) {
                	I_DescriptionVersioned descVersioned = tuple.getDescVersioned();
                	executeComponent(descVersioned, cNid, descVersioned.getDescId(), frameConfig, activities);
                }
				break;

			default:
				throw new UnsupportedOperationException("Unknown compute type");
			}           
        }
    }

    private void executeComponent(I_AmTermComponent component, int conceptNid, int componentNid,
            I_ConfigAceFrame config, Collection<I_ShowActivity> activities) throws Exception {
        if (possibleCNids.isMember(conceptNid)) {
            boolean containsCurrentMember = currentRefsetMemberComponentNids.isMember(componentNid);
			if (query.execute(component, null, (I_Position) query.getV1Is(),
					(I_Position) query.getV2Is(), activities)) {
            	newMemberNids.setMember(componentNid);
                members.incrementAndGet();
                if (!containsCurrentMember) {
                	currentRefsetMemberComponentNids.setMember(componentNid);
                    newMembers.incrementAndGet();
                    memberRefsetHelper.newRefsetExtension(refsetNid, componentNid, ReferenceConcepts.NORMAL_MEMBER
                        .getNid(), false);
                    if (isaCache == null) {
                        memberRefsetHelper.addMarkedParents(new Integer[] { conceptNid });
                    }
                }
            } else {
                if (containsCurrentMember) {
                	currentRefsetMemberComponentNids.setNotMember(componentNid);
                	retiredMemberNids.setMember(componentNid);
                    retiredMembers.incrementAndGet();
                    memberRefsetHelper.retireRefsetExtension(refsetNid, componentNid, ReferenceConcepts.NORMAL_MEMBER
                        .getNid());
                    if (isaCache == null) {
                    	memberRefsetHelper.removeMarkedParents(new Integer[] { conceptNid });
                    }
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
                    for (I_ShowActivity a : activities) {
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

    public void addUncommitted() throws Exception {
        int parentMemberTypeNid =
            Terms.get().getConcept(RefsetAuxiliary.Concept.MARKED_PARENT.getUids()).getConceptNid();
   	if (!canceled && isaCache != null) {
            long endTime = System.currentTimeMillis();
            long elapsed = endTime - startTime;
            String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);
            activity.setIndeterminate(true);
            activity.setProgressInfoLower("Adding marked parents. Elapsed: " + elapsedStr 
                    + ";  Members: " + members.get() + " New: " + newMembers.get() + " Ret: "
                    + retiredMembers.get());
            
            I_RepresentIdSet newParents = Bdb.getConceptDb().getEmptyIdSet();
            NidBitSetItrBI newMemberItr = newMemberNids.iterator();
            while (newMemberItr.next()) {
            	isaCache.addParents(newMemberItr.nid(), newParents);
            }
            NidBitSetItrBI newParentItr = newParents.iterator();
            while (newParentItr.next()) {
            	memberRefsetHelper.newRefsetExtension(markedParentRefsetConcept.getNid(), newParentItr.nid(), parentMemberTypeNid);
            }
     	}
    	if (!canceled && isaCache != null) {
            long endTime = System.currentTimeMillis();
            long elapsed = endTime - startTime;
            String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);
            activity.setIndeterminate(true);
            activity.setProgressInfoLower("Removing old marked parents. Elapsed: " + elapsedStr 
                    + ";  Members: " + members.get() + " New: " + newMembers.get() + " Ret: "
                    + retiredMembers.get());
            
            retiredMemberNids.andNot(newMemberNids);

            I_RepresentIdSet parentsToRetire = Bdb.getConceptDb().getEmptyIdSet();
            NidBitSetItrBI retiredMemberItr = retiredMemberNids.iterator();
            while (retiredMemberItr.next()) {
            	isaCache.addParents(retiredMemberItr.nid(), parentsToRetire);
            }
            I_RepresentIdSet currentParents = Bdb.getConceptDb().getEmptyIdSet();
            NidBitSetItrBI currentMemberItr = currentRefsetMemberComponentNids.iterator();
            while (currentMemberItr.next()) {
            	isaCache.addParents(currentMemberItr.nid(), currentParents);
            }
            parentsToRetire.andNot(currentParents);
            NidBitSetItrBI parentToRetireItr = parentsToRetire.iterator();
            while (parentToRetireItr.next()) {
            	memberRefsetHelper.retireRefsetExtension(markedParentRefsetConcept.getNid(), parentToRetireItr.nid(), parentMemberTypeNid);
            }
    	}
    	if (!canceled) {
    		BdbCommitManager.addUncommittedNoChecks(refsetConcept);
    		BdbCommitManager.addUncommittedNoChecks(markedParentRefsetConcept);
    	}
        long elapsed = System.currentTimeMillis() - startTime;
        String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);
        if (!canceled) {
            activity.setProgressInfoLower("Complete. Time: " + elapsedStr + "; Members: " + members.get() + " New: "
                + newMembers.get() + " Ret: " + retiredMembers.get());
        } else {
            activity.setProgressInfoLower("Cancelled.");
            for (I_ShowActivity a : activities) {
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

	@Override
	public NidBitSetBI getNidSet() {
		return possibleCNids;
	}
}