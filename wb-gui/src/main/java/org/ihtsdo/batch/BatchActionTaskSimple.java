package org.ihtsdo.batch;

import org.ihtsdo.tk.api.concept.ConceptVersionBI;


/**
 * BatchActionSimple
 * 
 */

public class BatchActionTaskSimple extends BatchActionTask {

    @Override
    public boolean execute(ConceptVersionBI c) {
        System.out.println("## ## BatchActionTaskSimple concept: " + c);
        return true;
    }

}
