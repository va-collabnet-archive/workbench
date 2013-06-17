/**
 *
 */
package org.ihtsdo.tk.query;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
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
                continueWorking.getAndSet(false);
            }
        }
    }
//    private int refsetNid;
    private RefsetSpecQuery query;
//    private SpecRefsetHelper memberRefsetHelper;
//    private SpecMarkedParentRefsetHelper markedParentRefsetHelper;
//    private ConceptChronicleBI refsetConcept;
//    private ConceptChronicleBI markedParentRefsetConcept;
    private AtomicInteger processedCount = new AtomicInteger();
    private ConcurrentSkipListSet<Integer> resultNids = new ConcurrentSkipListSet<>();
//    private AtomicInteger members = new AtomicInteger();
//    private AtomicInteger newMembers = new AtomicInteger();
//    private AtomicInteger retiredMembers = new AtomicInteger();
    private AtomicInteger processed = new AtomicInteger();
    private boolean canceled = false;
    private long startTime = System.currentTimeMillis();
    private NidBitSetBI possibleCNids;
    private ComputeType computeType;
    private TerminologyStoreDI ts;
    private ViewCoordinate viewCoordinate;
//    protected NidBitSetBI retiredRefsetRefCompNids;
//    protected NidBitSetBI activeRefsetRefCompNids;
//    protected NidBitSetBI activeMarkedParentRefCompNids;
//    private boolean persist;
    private int normalMemberNid;
    NidBitSetBI resultSet;
    private AtomicBoolean continueWorking = new AtomicBoolean(true);
//    ConcurrentSkipListSet<Integer> newMarkedParents = new ConcurrentSkipListSet<>();
    
     public RefsetComputer(RefsetSpecQuery query, ViewCoordinate viewCoordinate,
            NidBitSetBI possibleIds, ComputeType computeType) throws Exception {
        super();
        ts = Ts.get();
        this.possibleCNids = possibleIds;
        this.viewCoordinate = viewCoordinate;
        this.computeType = computeType;
        this.query = query;
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
                    //TODO?
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
                processed.incrementAndGet();
                if (processed.get() % 500 == 0) {
                    System.out.println("processed: " + processed + " time: " + System.currentTimeMillis());
                }
                if (processed.get() == possibleCNids.cardinality()) {
                    System.out.println("processed last concept: " + processed + " time: " + System.currentTimeMillis());
                }
            }
        }
    }
    


    public AtomicInteger getProcessedCount() {
        return processedCount;
    }

    @Override
    public boolean continueWork() {
        return continueWorking.get();
    }

    @Override
    public NidBitSetBI getNidSet() {
        return possibleCNids;
    }

    public NidBitSetBI getMemberNids() throws IOException {
        NidBitSetBI result = Ts.get().getEmptyNidSet();
        for (int nid : resultNids) {
            result.setMember(nid);
        }
        return result;
    }
}