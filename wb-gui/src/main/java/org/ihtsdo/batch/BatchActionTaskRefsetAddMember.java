package org.ihtsdo.batch;

import org.ihtsdo.tk.api.concept.ConceptVersionBI;


/**
 * Sample BatchAction
 * 
 */

public class BatchActionTaskRefsetAddMember extends BatchActionTask {

    public BatchActionTaskRefsetAddMember() {
    }

    //
    
    
    @Override
    public boolean execute(ConceptVersionBI c) throws Exception {
        System.out.println("## BatchActionTaskRefsetAddMember concept: " + c);
        
        return true;
    }

}
