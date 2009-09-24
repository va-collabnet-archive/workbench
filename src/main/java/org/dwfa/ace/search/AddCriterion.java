/**
 * 
 */
package org.dwfa.ace.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dwfa.ace.log.AceLog;

public class AddCriterion implements ActionListener {

    /**
     * 
     */
    private final I_MakeCriterionPanel searchPanel;

    /**
     * @param searchPanel
     */
    AddCriterion(I_MakeCriterionPanel searchPanel) {
        this.searchPanel = searchPanel;
    }

    public void actionPerformed(ActionEvent e) {
        try {
        	searchPanel.getCriterionPanels().add(searchPanel.makeCriterionPanel());
        } catch (ClassNotFoundException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (InstantiationException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IllegalAccessException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
        searchPanel.layoutCriterion();
    }

}