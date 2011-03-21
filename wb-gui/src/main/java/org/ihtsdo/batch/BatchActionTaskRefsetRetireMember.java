package org.ihtsdo.batch;

import org.ihtsdo.tk.api.concept.ConceptVersionBI;


/**
 * BatchActionTaskRefsetRetireMember
 * 
 */

public class BatchActionTaskRefsetRetireMember extends BatchActionTask {

    //
    
    
    @Override
    public boolean execute(ConceptVersionBI c) {
        System.out.println("## BatchActionTaskRefsetRetireMember concept: " + c);
        
        return true;
    }

}
