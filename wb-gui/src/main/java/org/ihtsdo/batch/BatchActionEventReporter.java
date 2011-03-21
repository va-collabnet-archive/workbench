/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        StringBuilder sb = new StringBuilder();
        try {
            for (BatchActionEvent bae : batchActionEventList) {
                switch (bae.actionType) {
                    case PARENT_ADD_NEW:
                        sb.append("PARENT_ADD_NEW");
                        break;
                    case PARENT_REPLACE:
                        sb.append("PARENT_REPLACE");
                        break;
                    case PARENT_RETIRE:
                        sb.append("PARENT_RETIRE");
                        break;
                    case REFSET_ADD_MEMBER:
                        sb.append("REFSET_ADD_MEMBER");
                        break;
                    case REFSET_MOVE_MEMBER:
                        sb.append("REFSET_MOVE_MEMBER");
                        break;
                    case REFSET_REPLACE_VALUE:
                        sb.append("REFSET_REPLACE_VALUE");
                        break;
                    case REFSET_RETIRE_MEMBER:
                        sb.append("REFSET_RETIRE_MEMBER");
                        break;
                    case ROLE_REPLACE_VALUE:
                        sb.append("ROLE_REPLACE_VALUE");
                        break;
                    case SIMPLE:
                        sb.append("SIMPLE");
                        break;
                }
                sb.append("\t");
                sb.append(bae.conceptA.getPreferredDescription().getText());
                sb.append("\r\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(BatchActionEvent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ContraditionException ex) {
            Logger.getLogger(BatchActionEvent.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sb.toString();
    }
}
