package org.ihtsdo.batch;

import org.ihtsdo.tk.api.concept.ConceptVersionBI;

/**
 * BatchActionTaskRoleReplaceValue
 * 
 */
public class BatchActionTaskRoleReplaceValue extends BatchActionTask {

    public BatchActionTaskRoleReplaceValue() {
    }

    //
    @Override
    public boolean execute(ConceptVersionBI c) {
        System.out.println("## BatchActionTaskSimple concept: " + c);

        BatchActionEventReporter.add(new BatchActionEvent(c, BatchActionTaskType.ROLE_REPLACE_VALUE));

        return true;
    }
}
