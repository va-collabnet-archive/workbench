/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ihtsdo.batch;

import javax.swing.JLabel;
import javax.swing.JPanel;
import org.dwfa.ace.CollectionEditorContainer;

/**
 *
 * @author kec
 */
public class Z_BatchActionEditor {

    JPanel batchEditorPanel = new JPanel();
    private final CollectionEditorContainer cec;

    public JPanel getBatchEditorPanel() {
        return batchEditorPanel;
    }
    public Z_BatchActionEditor(CollectionEditorContainer cec) {
        this.cec = cec;
        
        // :!!!: BATCH ACTION PANEL MAIN EDITOR
        // batchEditorPanel.add(new JLabel("Batch editor actions here"));
        batchEditorPanel.add(new BatchActionEditPanel());
    }

}
