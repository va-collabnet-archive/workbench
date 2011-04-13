/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.batch;

import java.util.List;
import javax.swing.JPanel;
import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

/**
 *
 * @author marc
 */
public interface I_BatchActionTask {

    void doTaskExecution(ConceptVersionBI c);

    JPanel getPanel();

    void updateExisting(List<ComponentVersionBI> existingParents, List<ComponentVersionBI> existingRefsets, List<ComponentVersionBI> existingRoles);
}
