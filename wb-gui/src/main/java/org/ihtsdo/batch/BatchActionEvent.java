/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.batch;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.batch.BatchActionTask.BatchActionTaskType;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

/**
 *
 * @author marc
 */
public class BatchActionEvent implements Comparable<BatchActionEvent> {

    ConceptVersionBI conceptA; // main concept of operation 
    BatchActionTaskType actionType;

    public BatchActionEvent(ConceptVersionBI conceptA, BatchActionTaskType actionType) {
        this.conceptA = conceptA;
        this.actionType = actionType;

    }

    @Override
    public int compareTo(BatchActionEvent t) {
        if (this.actionType.compareTo(t.actionType) > 0) {
            return 1; // this is greater than recieved
        } else if (this.actionType.compareTo(t.actionType) < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        try {

            switch (actionType) {
                case PARENT_RETIRE:
                    sb.append("PARENT_RETIRE");
                    break;

                case PARENT_REPLACE:
                    sb.append("PARENT_REPLACE");
                    break;

                case PARENT_ADD_NEW:
                    sb.append("PARENT_ADD_NEW");
                    break;
            }
            sb.append("\t");
            sb.append(conceptA.getPreferredDescription().getText());
            sb.append("\r\n");

        } catch (IOException ex) {
            Logger.getLogger(BatchActionEvent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ContraditionException ex) {
            Logger.getLogger(BatchActionEvent.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sb.toString();
    }
}
