package org.dwfa.ace.classifier;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.types.ConceptBean;

public class DLNoGrpAddActionListener implements ActionListener {
    private I_ContainTermComponent termContainer; // :!!!:
    private I_ConfigAceFrame config; // :!!!:

    public DLNoGrpAddActionListener(I_ContainTermComponent termContainer,
            I_ConfigAceFrame config) {
        super();
        this.termContainer = termContainer;
        this.config = config;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (termContainer.getConfig().getClassifierInputPath() == null) {
                
                JOptionPane.showMessageDialog(new JFrame(),
                        "Please set the Classifier Edit Path ...");
                return;
            }
            doEdit(termContainer, e, config);
        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    void doEdit(I_ContainTermComponent termContainer, ActionEvent e,
            I_ConfigAceFrame config) {
        ConceptBean cb = (ConceptBean) termContainer.getTermComponent();
        
        int parentId = Integer.MAX_VALUE;
        if (config.getHierarchySelection() != null) {
            parentId = config.getHierarchySelection().getConceptId();
        }
        
    }

}
