package org.ihtsdo.batch;

import org.ihtsdo.tk.api.concept.ConceptVersionBI;

/**
 * BatchActionTaskRefsetMoveMember
 * 
 */
public class BatchActionTaskRefsetMoveMember extends BatchActionTask {

    public BatchActionTaskRefsetMoveMember() {
    }

    //
    @Override
    public boolean execute(ConceptVersionBI c) throws Exception {
        System.out.println("## BatchActionTaskRefsetMoveMember concept: " + c);

        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.REFSET_MOVE_MEMBER));

        return true;
    }
}
