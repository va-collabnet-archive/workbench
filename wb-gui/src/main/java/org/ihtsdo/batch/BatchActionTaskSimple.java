package org.ihtsdo.batch;

import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;


/**
 * BatchActionSimple
 * 
 */

public class BatchActionTaskSimple extends BatchActionTask {

    @Override
    public boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) {
        System.out.println("## ## BatchActionTaskSimple concept: " + c);
        return true;
    }

}
