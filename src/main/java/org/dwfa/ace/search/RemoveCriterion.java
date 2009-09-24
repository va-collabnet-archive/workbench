/**
 * 
 */
package org.dwfa.ace.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

public class RemoveCriterion implements ActionListener {
    
    /**
     * 
     */
    private final I_MakeCriterionPanel searchPanel;
    JPanel criterionPanel;

    RemoveCriterion(I_MakeCriterionPanel searchPanel, JPanel criterionPanel) {
        super();
        this.searchPanel = searchPanel;
        this.criterionPanel = criterionPanel;
    }

    public void actionPerformed(ActionEvent e) {
        this.searchPanel.getCriterionPanels().remove(criterionPanel);
        this.searchPanel.layoutCriterion();
    }

}