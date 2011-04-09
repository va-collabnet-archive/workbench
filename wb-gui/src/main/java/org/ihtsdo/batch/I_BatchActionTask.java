/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ihtsdo.batch;

import javax.swing.JPanel;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

/**
 *
 * @author marc
 */
public interface I_BatchActionTask {

    void doTaskExecution(ConceptVersionBI c);

    JPanel getPanel();

}
