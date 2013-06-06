/**
 *
 */
package org.ihtsdo.tk.refset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class RefsetComputer implements ProcessUnfetchedConceptDataBI {

    public enum ComputeType {

        CONCEPT, DESCRIPTION, RELATIONSHIP, MIXED
    };

    public class StopActionListener implements ActionListener {

        public StopActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!canceled) {
                canceled = true;
//TODO: how to stop parallel concept interator?
//                List<ParallelConceptIterator> pcis = getParallelConceptIterators();
//                for (ParallelConceptIterator pci : pcis) {
//                    pci.stop();
//                }
            }
        }
    }
    private int refsetNid;
    private RefsetSpecQuery query;
    private SpecRefsetHelper memberRefsetHelper;
    private SpecMarkedParentRefsetHelper markedParentRefsetHelper;
    private ConceptChronicleBI refsetConcept;
    private ConceptChronicleBI markedParentRefsetConcept;
    private AtomicInteger processedCount = new AtomicInteger();
    private ConcurrentSkipListSet<Integer> resultNids = new ConcurrentSkipListSet<>();
    private AtomicInteger members = new AtomicInteger();
    private AtomicInteger newMembers = new AtomicInteger();
    private AtomicInteger retiredMembers = new AtomicInteger();
    private AtomicInteger processed = new AtomicInteger();
    private boolean canceled = false;
    private boolean informed = false;
    private long startTime = System.currentTimeMillis();
    private int conceptCount;
    private NidBitSetBI possibleCNids;
    private ComputeType computeType;
    private StopActionListener stopListener;
    private TerminologyStoreDI ts;
    private ViewCoordinate viewCoordinate;
    protected NidBitSetBI retiredRefsetRefCompNids;
    protected NidBitSetBI activeRefsetRefCompNids;
    protected NidBitSetBI activeMarkedParentRefCompNids;
    private boolean persist;
    private int normalMemberNid;
    NidBitSetBI resultSet;
    Set<Integer> newMarkedParents = new HashSet<>();
    
     public RefsetComputer(RefsetSpecQuery query, ViewCoordinate viewCoordinate,
            NidBitSetBI possibleIds, ComputeType computeType) throws Exception {
        super();
        ts = Ts.get();
        this.possibleCNids = possibleIds;
        this.viewCoordinate = viewCoordinate;
        this.computeType = computeType;
        this.query = query;
        this.persist = false;
     }
    public RefsetComputer(RefsetSpecQuery query, ViewCoordinate viewCoordinate,
            NidBitSetBI possibleIds, ComputeType computeType, int refsetNid, 
            EditCoordinate editCoordinate) throws Exception {
        super();
        ts = Ts.get();
        this.possibleCNids = possibleIds;
        this.viewCoordinate = viewCoordinate;
        this.computeType = computeType;
        this.query = query;
        this.persist = true;
        this.refsetNid = refsetNid;
        this.refsetConcept = ts.getConcept(refsetNid);
        this.retiredRefsetRefCompNids = ts.getEmptyNidSet();
        this.activeRefsetRefCompNids = ts.getEmptyNidSet();
        this.activeMarkedParentRefCompNids = ts.getEmptyNidSet();
        for (RefexChronicleBI r : refsetConcept.getRefsetMembers()) {
            if (r.getVersions(viewCoordinate).isEmpty()) {
                retiredRefsetRefCompNids.setMember(r.getReferencedComponentNid());
            } else {
                activeRefsetRefCompNids.setMember(r.getReferencedComponentNid());
            }
        }
        memberRefsetHelper = new SpecRefsetHelper(viewCoordinate, editCoordinate);
        markedParentRefsetHelper = new SpecMarkedParentRefsetHelper(viewCoordinate, editCoordinate, refsetNid);
        markedParentRefsetConcept = ts.getConcept(markedParentRefsetHelper.getParentRefsetNid());
        for (RefexChronicleBI r : markedParentRefsetConcept.getRefsetMembers()) {
            if (!r.getVersions(viewCoordinate).isEmpty()) {
                activeMarkedParentRefCompNids.setMember(r.getReferencedComponentNid());
            }
        }
        normalMemberNid = ts.getNidForUuids(RefsetAuxiliary.Concept.NORMAL_MEMBER.getUids());
        resultSet = ts.getEmptyNidSet();
    }
    
    public void compute() throws Exception{
        System.out.println("Starting refset computation.");
        Ts.get().iterateConceptDataInParallel(this);
        System.out.println("Finished refset computation.");
        if(persist){
            System.out.println("Updating marked parents.");
            MarkedParentEditor helper = new MarkedParentEditor();
            Ts.get().iterateConceptDataInParallel(helper);
            System.out.println("Marked parents complete.");
        }
    }

    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fcfc) throws Exception {
        if (canceled) {
            return;
        }

        if (possibleCNids.isMember(cNid)) {
            switch (computeType) {
                case CONCEPT:
                    ConceptChronicleBI concept = fcfc.fetch();
                    executeComponent(concept, cNid, cNid);
                    break;
                case DESCRIPTION:
                    concept = fcfc.fetch();
                    Collection<? extends DescriptionChronicleBI> descriptions = concept.getDescriptions();
                    for (DescriptionChronicleBI desc : descriptions) {
                        DescriptionVersionBI descVersioned = desc.getVersion(viewCoordinate.getViewCoordinateWithAllStatusValues());
                        executeComponent(descVersioned, cNid,
                                descVersioned.getNid());
                    }
                    break;
                case RELATIONSHIP:
                    concept = fcfc.fetch();
                    Collection<? extends RelationshipChronicleBI> relationships =
                            concept.getRelationshipsOutgoing();
                    for (RelationshipChronicleBI rel : relationships) {
                        RelationshipVersionBI relVersioned = rel.getVersion(viewCoordinate.getViewCoordinateWithAllStatusValues());
                        if (relVersioned != null) { //could be null if classifier has been run on a previous merge path
                            executeComponent(relVersioned, cNid,
                                    relVersioned.getNid());
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

    private void executeComponent(Object component, int conceptNid, int componentNid) throws Exception {
        if (possibleCNids.isMember(conceptNid)) {
            if (query.execute(componentNid, component, null, query.getV1Is(),
                    query.getV2Is())) {
                resultNids.add(componentNid);
                members.incrementAndGet();
                if(persist){
                        activeRefsetRefCompNids.setMember(conceptNid);
                        newMembers.incrementAndGet();
                        memberRefsetHelper.newRefsetExtension(refsetNid, conceptNid,
                                normalMemberNid);
                        Set<Integer> parents = ts.getAncestors(conceptNid, viewCoordinate);
                        newMarkedParents.addAll(parents);
                }
            }else if (persist){
                if(activeRefsetRefCompNids.isMember(conceptNid)){
                    activeRefsetRefCompNids.setNotMember(conceptNid);
                    retiredRefsetRefCompNids.setMember(conceptNid);
                    retiredMembers.incrementAndGet();
                    memberRefsetHelper.retireRefsetExtension(refsetNid, conceptNid,
                            normalMemberNid);
                }
            }
            processed.incrementAndGet();
            if(processed.get() % 500 == 0){
                System.out.println("processed: " + processed + " time: " + System.currentTimeMillis());
            }
            if(processed.get() == possibleCNids.cardinality()){
                System.out.println("processed last concept: " + processed + " time: " + System.currentTimeMillis());
            }
        }
    }
    
    private class MarkedParentEditor implements ProcessUnfetchedConceptDataBI{
        NidBitSetBI processSet;

        @Override
        public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
            if(processSet.isMember(conceptNid)){
                if(!newMarkedParents.contains(conceptNid) && activeMarkedParentRefCompNids.isMember(conceptNid)){
                    markedParentRefsetHelper.removeParentMembers(conceptNid);
                }
                if(!activeMarkedParentRefCompNids.isMember(conceptNid) && newMarkedParents.contains(conceptNid)){
                    markedParentRefsetHelper.addParentMembers(conceptNid);
                }
            }
        }

        @Override
        public NidBitSetBI getNidSet() throws IOException {
            processSet = Ts.get().getEmptyNidSet();
            for(int nid : newMarkedParents){
                processSet.setMember(nid);
            }
            processSet.or(activeMarkedParentRefCompNids);
            return processSet;
        }

        @Override
        public boolean continueWork() {
            return true;
        }
        
    }

    protected void addUncommitted() throws Exception {
        if (!canceled) {
            long endTime = System.currentTimeMillis();
            long elapsed = endTime - startTime;
            String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
            System.out.println("Adding marked parents. Elapsed: " + elapsedStr
                    + ";  Members: " + members.get() + " New: " + newMembers.get() + " Ret: "
                    + retiredMembers.get());

        }
        if (!canceled) {
            long endTime = System.currentTimeMillis();
            long elapsed = endTime - startTime;
            String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
            System.out.println("Removing old marked parents. Elapsed: " + elapsedStr
                    + ";  Members: " + members.get() + " New: " + newMembers.get() + " Ret: "
                    + retiredMembers.get());
        }
        if (!canceled) {
            ts.addUncommittedNoChecks(refsetConcept);
            ts.addUncommittedNoChecks(markedParentRefsetConcept);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
        if (!canceled) {
            System.out.println("Complete. Time: " + elapsedStr
                    + "; Members: " + members.get() + " New: "
                    + newMembers.get() + " Ret: " + retiredMembers.get());
        } else {
            System.out.println("Cancelled.");
        }
    }

    public AtomicInteger getProcessedCount() {
        return processedCount;
    }

    @Override
    public boolean continueWork() {
        return true;
    }

    @Override
    public NidBitSetBI getNidSet() {
        return possibleCNids;
    }

    protected NidBitSetBI getResultNids() throws IOException {
        NidBitSetBI result = Ts.get().getEmptyNidSet();
        for (int nid : resultNids) {
            result.setMember(nid);
        }
        return result;
    }
}