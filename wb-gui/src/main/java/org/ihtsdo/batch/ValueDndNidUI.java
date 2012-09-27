/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.batch;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

/**
 *
 * @author marc
 *
 * See also: TermComponentLabel.java, TransporterLabel.java
 */
public class ValueDndNidUI extends javax.swing.JPanel implements /*FocusListener,*/ I_ContainTermComponent, ClipboardOwner {

    /** Creates new form ValueDndNidUI */
    public ValueDndNidUI(String title) {
        initComponents();

        setBorder(BorderFactory.createTitledBorder(title));

        // CONFIG
        try {
            config = Terms.get().getActiveAceFrameConfig();
        } catch (TerminologyException ex) {
            Logger.getLogger(ValueDndNidUI.class.getName()).log(Level.SEVERE, null, ex);
            jLabelComponentName.setText("config error");
        } catch (IOException ex) {
            Logger.getLogger(ValueDndNidUI.class.getName()).log(Level.SEVERE, null, ex);
            jLabelComponentName.setText("config error");
        }

        // setTransferHandler(new TerminologyTransferHandler(this));
        setTransferHandler(Terms.get().makeTerminologyTransferHandler(this));
//        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this,
//                DnDConstants.ACTION_COPY,
//                new DragGestureListenerWithImage(new TermLabelDragSourceListener()));


        // OTHER SETUP
        // addFocusListener(this);

        setFocusable(true);
        setEnabled(true);

        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        ActionMap map = this.getActionMap();
        map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteTask");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "deleteTask");

//        map.put("deleteTask", new DeleteAction());
//        map.put("Copy TDT", new CopyTDT());
//        map.put("Copy XML", new CopyXML());

//        setBorder(noFocusBorder);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelComponentName = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Title:"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabelComponentName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabelComponentName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelComponentName;
    // End of variables declaration//GEN-END:variables

    public JPanel getPanel() {
        return this;
    }
    private I_ConfigAceFrame config;
    private I_AmTermComponent termComponent;

    @Override // I_ContainTermComponent
    public I_AmTermComponent getTermComponent() {
        return termComponent;
    }

    @Override // I_ContainTermComponent
    public void setTermComponent(I_AmTermComponent termComponent) {
        // if (isFrozen()) { return; }
        if (termComponent != null
                && I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
            try {
                if (((I_GetConceptData) termComponent).isCanceled()) {
                    termComponent = null;
                }
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
        Object old = this.termComponent;
        this.termComponent = termComponent;
        if (termComponent != null) {
            if (I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
                I_GetConceptData cb = (I_GetConceptData) termComponent;
                try {
                    I_DescriptionTuple desc = cb.getDescTuple(config.getLongLabelDescPreferenceList(), config);
                    if (desc != null) {
                        jLabelComponentName.setText(desc.getText());
                    } else {
                        jLabelComponentName.setText(cb.getInitialText());
                    }
                } catch (IOException e) {
                    jLabelComponentName.setText(e.getMessage());
                    AceLog.getAppLog().alertAndLogException(e);
                }
            } else {
                jLabelComponentName.setText(this.termComponent.toString());
            }
        } else {
            jLabelComponentName.setText("<html><font color=red>Empty");
        }
        if (old == null) {
            firePropertyChange("termComponent", old, termComponent);
        } else if (old.equals(termComponent)) {
            firePropertyChange("termComponent", null, termComponent);
        } else {
            firePropertyChange("termComponent", old, termComponent);
        }
    }

    @Override // I_ContainTermComponent
    public I_ConfigAceFrame getConfig() {
        return config;
    }

    @Override // I_ContainTermComponent
    public void unlink() {
        // nothing to do ...
    }

    /*
    private static final Border hasFocusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
    private static final Border noFocusBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

    @Override // FocusListener
    public void focusGained(FocusEvent fe) {
    setBorder(hasFocusBorder);
    }

    @Override // FocusListener
    public void focusLost(FocusEvent fe) {
    setBorder(noFocusBorder);
    }
     */
    @Override // ClipboardOwner
    public void lostOwnership(Clipboard clpbrd, Transferable t) {
        // nothing to do. :!!!:???: is ClipBoardOwner needed
    }

    public Integer getValue() {
        if (termComponent == null) {
            return null;
        }

        return termComponent.getNid();
    }
}
