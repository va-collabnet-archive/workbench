/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.batch.BatchActionEvent.BatchActionEventType;
import org.ihtsdo.tk.api.ContraditionException;

/**
 *
 * An "unsorted" report will be in execution order <br>
 *  :!!!:NYI: sort by concept conceptA.getPreferredDescription().getText();
 *  :!!!:NYI: sort by operation
 * 
 * @author marc
 */
public class BatchActionEventReporter {

    private static List<BatchActionEvent> batchActionEventList;

    public BatchActionEventReporter() {
    }

    public static void reset() {
        batchActionEventList = new ArrayList<BatchActionEvent>();
    }

    public static void add(BatchActionEvent bae) {
        batchActionEventList.add(bae);
    }

    /**
     * generates Tab Separated value report.
     * @return 
     */
    public static String createReportTSV() {
        EnumSet<BatchActionEventType> reportEvents = EnumSet.of(BatchActionEventType.EVENT_ERROR,
                BatchActionEventType.EVENT_NOOP, BatchActionEventType.EVENT_SUCCESS,
                BatchActionEventType.TASK_INVALID);
        return createReportTSV(reportEvents);
    }

    public static String createReportTSV(EnumSet<BatchActionEventType> reportEvents) {
        StringBuilder sb = new StringBuilder();
        try {
            for (BatchActionEvent bae : batchActionEventList) {
                if (reportEvents.contains(bae.getEventType())) {
                    // ACTION TYPE
                    sb.append(bae.getActionTaskType());
                    // EVENT TYPE
                    sb.append("\t");
                    sb.append(bae.getEventType());
                    // EVENT CONCEPT
                    sb.append("\t");
                    if (bae.getConceptA() != null) {
                        sb.append(bae.getConceptA().getPreferredDescription().getText());
                    }
                    // EVENT NOTE
                    sb.append("\t");
                    if (bae.getEventNote() != null) {
                        sb.append(bae.getEventNote());
                    } else {
                        sb.append("");
                    }
                    sb.append("\r\n");
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(BatchActionEvent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ContraditionException ex) {
            Logger.getLogger(BatchActionEvent.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sb.toString();
    }

    /**
     * generates Tab Separated value report.
     * @return
     */
    public static String createReportHTML() {
        EnumSet<BatchActionEventType> reportEvents = EnumSet.of(BatchActionEventType.EVENT_ERROR,
                BatchActionEventType.EVENT_NOOP, BatchActionEventType.EVENT_SUCCESS,
                BatchActionEventType.TASK_INVALID);
        return createReportHTML(reportEvents);
    }

    public static String createReportHTML(EnumSet<BatchActionEventType> reportEvents) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><table><font color='black' face='Dialog' size='3'>");
        sb.append("<tr><td>Action</td><td>Status</td><td>Concept</td><td>Result</td></tr>");
        sb.append("</font>");
        try {
            for (BatchActionEvent bae : batchActionEventList) {
                if (reportEvents.contains(bae.getEventType())) {
                    // ACTION TYPE
                    sb.append("<tr><td><font color='black' face='Dialog' size='3'>");
                    sb.append(bae.getActionTaskType());
                    // EVENT TYPE
                    BatchActionEventType eventType = bae.getEventType();
                    switch (eventType) {
                        case EVENT_ERROR:
                        case TASK_INVALID:
                            sb.append("</font></td><td><font color='red' face='Dialog' size='3'>");
                            break;
                        case EVENT_NOOP:
                            sb.append("</font></td><td><font color='blue' face='Dialog' size='3'>");
                            break;
                        case EVENT_SUCCESS:
                            sb.append("</font></td><td><font color='blue' face='Dialog' size='3'>");
                            break;
                        default:
                            sb.append("</font></td><td><font color='blue' face='Dialog' size='3'>");
                    }
                    sb.append(bae.getEventType());
                    // EVENT CONCEPT
                    sb.append("</font></td><td><font color='black' face='Dialog' size='3'>");
                    if (bae.getConceptA() != null) {
                        sb.append(bae.getConceptA().getPreferredDescription().getText());
                    }
                    // EVENT NOTE
                    sb.append("</font></td><td><font color='black' face='Dialog' size='3'>");
                    if (bae.getEventNote() != null) {
                        sb.append(bae.getEventNote());
                    } else {
                        sb.append("");
                    }
                    sb.append("</font></td></tr>");
                }
            }
            sb.append("</table></html>");
        } catch (IOException ex) {
            Logger.getLogger(BatchActionEvent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ContraditionException ex) {
            Logger.getLogger(BatchActionEvent.class.getName()).log(Level.SEVERE, null, ex);
        }


        return sb.toString();
    }

    public static int getSize() {
        return batchActionEventList.size();
    }
}
