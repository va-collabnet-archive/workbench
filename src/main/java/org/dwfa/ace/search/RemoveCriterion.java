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
    private final SearchPanel searchPanel;
    JPanel criterionPanel;

    RemoveCriterion(SearchPanel searchPanel, JPanel criterionPanel) {
        super();
        this.searchPanel = searchPanel;
        this.criterionPanel = criterionPanel;
    }

    public void actionPerformed(ActionEvent e) {
        this.searchPanel.criterionPanels.remove(criterionPanel);
        this.searchPanel.layoutCriterion();
    }

}