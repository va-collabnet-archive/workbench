/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
