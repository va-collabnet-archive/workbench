package org.ihtsdo.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public class BatchActionProcessor implements ProcessUnfetchedConceptDataBI {

    // WORKBENCH INTERNALS
    EditCoordinate ec;
    ViewCoordinate vc;

    // ACTION and CONCEPTS
    List<BatchActionTask> batchActionList;
    NidBitSetBI conceptSet;

    /**
     * EditCoordinate and ViewCoordinate context is setup when this processor is instantiated.
     * 
     * @param concepts
     * @param actions
     * @param ec
     * @param vc
     * @throws IOException
     * @throws Exception
     */
    public BatchActionProcessor(Collection<ConceptChronicleBI> concepts, List<BatchActionTask> actions, EditCoordinate ec, ViewCoordinate vc) throws IOException, Exception {
        // SETUP WORKBENCH CONTEXT
        this.ec = ec;
        this.vc = vc;
 
        // SETUP CONCEPTS SET FOR PROCESSING
        this.conceptSet = Ts.get().getEmptyNidSet();
        for (ConceptChronicleBI ccbi : concepts) {
            this.conceptSet.setMember(ccbi.getNid());
        }

        // SETUP LIST OF CONCEPTS TO BE APPLIED TO EACH CONCEPT
        this.batchActionList = new ArrayList<BatchActionTask>();
        for (BatchActionTask bat : actions) {
            this.batchActionList.add(bat);
        }
    }

    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
        if (conceptSet.isMember(cNid)) {
            ConceptVersionBI c = fetcher.fetch(vc);

            boolean changed = false;
            for (BatchActionTask bat : batchActionList) {
                if (bat.execute(c, ec, vc)) {
                    changed = true;
                }
            }
            if (changed) {
                Ts.get().addUncommitted(c);
            }
        }
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return conceptSet;
    }

    @Override
    public boolean continueWork() {
        // :!!!:NYI: BatchActionProcessor Future user cancel button would set this return value to false.
        return true;
    }
}
