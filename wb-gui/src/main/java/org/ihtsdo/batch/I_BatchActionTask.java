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
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 *
 * @author marc
 */
public interface I_BatchActionTask {

    JPanel getPanel();

    /**
     * getTask(EditCoordinate ec, ViewCoordinate vc) is responsible for:<br>
     * (1) updates all the values from the user GUI selections and<br>
     * (2) updates any UUID and NID cache values for efficient execution.
     *
     * @param ec
     * @param vc
     * @return
     */
    BatchActionTask getTask(EditCoordinate ec, ViewCoordinate vc) throws Exception;

    /**
     * updateExisting() updates the GUI objects based information from the current concept list.<br>
     * Call this routine each time the concept list content is changed.
     *
     * @param existingParents
     * @param existingRefsets
     * @param existingRoles
     */
    void updateExisting(List<ComponentVersionBI> existingParents, List<ComponentVersionBI> existingRefsets, List<ComponentVersionBI> existingRoles);
}
