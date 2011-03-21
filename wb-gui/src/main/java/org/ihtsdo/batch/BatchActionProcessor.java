package org.ihtsdo.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public class BatchActionProcessor implements ProcessUnfetchedConceptDataBI {

    // WORKBENCH INTERNALS
    EditCoordinate ec;
    ViewCoordinate vc;
    TerminologyConstructorBI termConstructor;
    Exception e;
    // ACTION and CONCEPTS
    List<BatchActionTask> batchActionList;
    NidBitSetBI conceptSet;

    public BatchActionProcessor(Collection<ConceptChronicleBI> concepts, List<BatchActionTask> actions, EditCoordinate ec, ViewCoordinate vc) throws IOException {
        // SETUP WORKBENCH CONTEXT
        this.ec = ec;
        this.vc = vc;
        this.termConstructor = Ts.get().getTerminologyConstructor(ec, vc);
        this.e = null;

        // SETUP RUN FOR BATCH ACTION TASKS
        BatchActionTask.setup(termConstructor);
        BatchActionEventReporter.reset();

        // IDENTIFY CONCEPTS TO BE PROCESSED 
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
        if (conceptSet.isMember(cNid) && e == null) {
            ConceptVersionBI c = fetcher.fetch(vc);

            try {
                boolean changed = false;
                for (BatchActionTask bat : batchActionList) {
                    if (bat.execute(c)) {
                        changed = true;
                    }
                }
                if (changed) {
                    Ts.get().addUncommitted(c);
                }

            } catch (Exception exception) {
                e = exception; // :!!!: add GUI code for handling exception
            }
        }
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return conceptSet;
    }

    @Override
    public boolean continueWork() {
        // :NYI: Future user cancel button would set this return value to false. 
        return true;
    }
    // :!!!: ADD and REMOVE TASKS
    // :!!!: ADD and REMOVE CONCEPT 
    // :!!!: REPLACE ALL CONCEPTS)
    // :!!!: CHANGE ViewCoordinate --> then update BatchActionTask.setup(vc)
}
