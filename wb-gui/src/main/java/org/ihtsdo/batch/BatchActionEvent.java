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

    public static enum BatchActionEventType {

        EVENT_ERROR,
        EVENT_NOOP,
        EVENT_SUCCESS,
        TASK_INVALID
    }
    private ConceptVersionBI conceptA; // main concept of operation
    private BatchActionTaskType actionTaskType;
    private BatchActionEventType eventType;
    private String eventNote;

    public BatchActionEvent(ConceptVersionBI conceptA, BatchActionTaskType actionType, BatchActionEventType eventType, String eventNote) {
        this.conceptA = conceptA;
        this.actionTaskType = actionType;
        this.eventType = eventType;
        this.eventNote = eventNote;
        System.out.println("!!! BatchActionEvent " + this);
    }

    @Override
    public int compareTo(BatchActionEvent t) {
        if (this.actionTaskType.compareTo(t.actionTaskType) > 0) {
            return 1; // this is greater than recieved
        } else if (this.actionTaskType.compareTo(t.actionTaskType) < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    public BatchActionTaskType getActionTaskType() {
        return actionTaskType;
    }

    public ConceptVersionBI getConceptA() {
        return conceptA;
    }

    public String getEventNote() {
        return eventNote;
    }

    public BatchActionEventType getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        try {

            sb.append(actionTaskType.toString());
            sb.append("\t");
            sb.append(conceptA.getPreferredDescription().getText());
            sb.append("\t");
            sb.append(eventType);
            sb.append("\t");
            if (eventNote != null) {
                sb.append(eventNote);
            }

        } catch (IOException ex) {
            Logger.getLogger(BatchActionEvent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ContraditionException ex) {
            Logger.getLogger(BatchActionEvent.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sb.toString();
    }
}
