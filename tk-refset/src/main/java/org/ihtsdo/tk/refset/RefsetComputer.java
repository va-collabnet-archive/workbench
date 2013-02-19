/**
 *
 */
package org.ihtsdo.tk.refset;

import java.io.IOException;
import org.ihtsdo.tk.refset.other.ActivityBI;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class RefsetComputer implements ProcessUnfetchedConceptDataBI {

    protected enum ComputeType {

        CONCEPT, DESCRIPTION, RELATIONSHP, MIXED
    };
    
//TODO: need activity panel first
//    public class StopActionListener implements ActionListener {
//
//        public StopActionListener() {
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            if (!canceled) {
//                canceled = true;
//                List<ParallelConceptIterator> pcis = getParallelConceptIterators();
//                for (ParallelConceptIterator pci : pcis) {
//                    pci.stop();
//                }
//                for (ActivityBI a : activities) {
//                    a.cancel();
//                    a.setProgressInfoLower("Cancelled.");
//                }
//                activity.removeStopActionListener(this);
//            }
//        }
//    }
    private RefsetSpecQuery query;
    private ConcurrentSkipListSet<Integer> resultNids = new ConcurrentSkipListSet<>(); 
    private AtomicInteger processedCount = new AtomicInteger();
    private AtomicInteger members = new AtomicInteger();
    private boolean canceled = false;
    private boolean informed = false;
    private ActivityBI activity;
    private Collection<ActivityBI> activities;
    private NidBitSetBI possibleCNids;
    private ViewCoordinate viewCoordinate;
    private ComputeType computeType;
//TODO: need activity panel
//    private StopActionListener stopListener;
    private TerminologyStoreDI ts;

    public RefsetComputer(RefsetSpecQuery query, ViewCoordinate viewCoordinate,
            NidBitSetBI possibleIds, HashSet<ActivityBI> activities, ComputeType computeType) throws Exception {
        super();
        ts = Ts.get();
        this.activities = activities;
        this.possibleCNids = possibleIds;
        this.viewCoordinate = viewCoordinate;
        this.computeType = computeType;

//TODO: need activity panel
//        activity =
//                Terms.get().newActivityPanel(true, frameConfig,
//                "Computing refset: " + refsetConcept.toString(), true);
//        activities.add(activity);
//        activity.setIndeterminate(true);
//        activity.setProgressInfoUpper("Computing refset: " + refsetConcept.toString());
//        activity.setProgressInfoLower("Setting up the computer...");
//        stopListener = new StopActionListener();
//        activity.addStopActionListener(stopListener);
//        ActivityViewer.addActivity(activity);

        this.query = query;

//TODO: need activity panel
//        activity.setProgressInfoLower("Starting computation...");
//        activity.setValue(0);
//        activity.setMaximum(possibleIds.cardinality());
//        activity.setIndeterminate(false);

    }


    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fcfc) throws Exception {
        if (canceled) {
            if (!informed) {
                activity.setProgressInfoLower("Cancelling.");
            }
            return;
        }

        if (possibleCNids.isMember(cNid)) {
            switch (computeType) {
                case CONCEPT:
                    ConceptChronicleBI concept = fcfc.fetch();
                    executeComponent(concept, cNid, cNid, activities);
                    break;
                case DESCRIPTION:
                    concept = fcfc.fetch();
                    Collection<? extends DescriptionChronicleBI> descriptions = concept.getDescriptions();
                    for (DescriptionChronicleBI desc : descriptions) {
                        DescriptionVersionBI descVersioned = desc.getVersion(viewCoordinate.getViewCoordinateWithAllStatusValues());
                        executeComponent(descVersioned, cNid,
                                descVersioned.getNid(), activities);
                    }
                    break;
                case RELATIONSHP:
                    concept = fcfc.fetch();
                    Collection<? extends RelationshipChronicleBI> relationships =
                            concept.getRelationshipsOutgoing();
                    for (RelationshipChronicleBI rel : relationships) {
                        RelationshipVersionBI relVersioned = rel.getVersion(viewCoordinate.getViewCoordinateWithAllStatusValues());
                        if(relVersioned != null){ //could be null if classifier has been run on a previous merge path
                            executeComponent(relVersioned, cNid,
                                relVersioned.getNid(), activities);
                        }
                    }
                    break;
                 case MIXED:
//TODO: would be good to get this working, need to combine queries currently
//                    concept = fcfc.fetch();
//                    ConceptAttributeChronicleBI conceptAttributes = concept.getConceptAttributes();
//                    executeComponent(conceptAttributes.getVersion(viewCoordinate.getViewCoordinateWithAllStatusValues()),
//                            cNid,
//                            conceptAttributes.getNid(),
//                            activities);
//                    descriptions = concept.getDescriptions();
//                    for (DescriptionChronicleBI desc : descriptions) {
//                        DescriptionVersionBI descVersioned = desc.getVersion(viewCoordinate.getViewCoordinateWithAllStatusValues());
//                        executeComponent(descVersioned, cNid,
//                                descVersioned.getNid(), activities);
//                    }
//                    relationships =
//                            concept.getRelationshipsOutgoing();
//                    for (RelationshipChronicleBI rel : relationships) {
//                        RelationshipVersionBI relVersioned = rel.getVersion(viewCoordinate.getViewCoordinateWithAllStatusValues());
//                        executeComponent(relVersioned, cNid,
//                                relVersioned.getNid(), activities);
//                    }
                    break;

                default:
                    throw new UnsupportedOperationException("Unknown compute type");
            }
        }
    }

    private void executeComponent(Object component, int conceptNid, int componentNid,
            Collection<ActivityBI> activities) throws Exception {
        if (possibleCNids.isMember(conceptNid)) {
            if (query.execute(componentNid, component, null, query.getV1Is(),
                    query.getV2Is(), activities)) {
                    resultNids.add(componentNid);
                    members.incrementAndGet();
                }
            }
//TODO: need activity panel first        
//            int completed = processedCount.incrementAndGet();
//            if (completed % 500 == 0) {
//                activity.setValue(completed);
//                if (!canceled) {
//                    long endTime = System.currentTimeMillis();
//
//                    long elapsed = endTime - startTime;
//                    String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
//
//                    String remainingStr = TimeHelper.getRemainingTimeString(completed,
//                            conceptCount, elapsed);
//
//                    activity.setProgressInfoLower("Elapsed: " + elapsedStr
//                            + ";  Remaining: " + remainingStr
//                            + ";  Members: " + members.get()
//                            + " New: " + newMembers.get() + " Ret: "
//                            + retiredMembers.get());
//                } else {
//                    for (ActivityBI a : activities) {
//                        if (!a.isComplete()) {
//                            if (!a.isComplete()) {
//                                a.cancel();
//                                a.setProgressInfoLower("Cancelled.");
//                            }
//                        }
//                    }
//                }
//            }
        }
    
    protected NidBitSetBI getResultNids() throws IOException{
        NidBitSetBI result = Ts.get().getEmptyNidSet();
        for(int nid : resultNids){
            result.setMember(nid);
        }
        return result;
    }


    public AtomicInteger getProcessedCount() {
        return processedCount;
    }

    @Override
    public boolean continueWork() {
        return true;
    }
//TODO: need activity panel first
//    List<ParallelConceptIterator> pcis;
//
//    @Override
//    public void setParallelConceptIterators(List<ParallelConceptIterator> pcis) {
//        this.pcis = pcis;
//    }
//
//    public List<ParallelConceptIterator> getParallelConceptIterators() {
//        return pcis;
//    }

    @Override
    public NidBitSetBI getNidSet() {
        return possibleCNids;
    }
}