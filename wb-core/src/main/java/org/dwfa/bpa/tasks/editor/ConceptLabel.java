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
package org.dwfa.bpa.tasks.editor;

import java.awt.Event;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.dnd.FixedTerminologyTransferable;

import sun.awt.dnd.SunDragSourceContextPeer;

/**
 * 
 * @author kec
 * @deprecated use TermComponentLabel
 */
public class ConceptLabel extends JEditorPane {
    private static Logger logger = Logger.getLogger(ConceptLabel.class.getName());

    public class TermViewerTransferHandler extends TransferHandler implements DragGestureListener, DragSourceListener {

        JComponent c;

        String propertyName;

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public TermViewerTransferHandler(JComponent c, String propertyName) {
            super(propertyName);
            this.c = c;
            this.propertyName = propertyName;
        }

        public TermViewerTransferHandler(JComponent c) {
            super();
            this.c = c;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            if (JTable.class.isAssignableFrom(c.getClass())) {
                return makeTableTransferable((JTable) c);
            } else if (JTree.class.isAssignableFrom(c.getClass())) {
                return makeTreeTransferable((JTree) c);
            } else if (ConceptLabel.class.isAssignableFrom(c.getClass())) {
                return makeConceptLabelTransferable((ConceptLabel) c);
            }
            logger.info("Making super.createTransferable(c): " + c);
            return super.createTransferable(c);
        }

        private Transferable makeConceptLabelTransferable(ConceptLabel label) {
            I_ConceptualizeLocally concept = label.getConcept();
            return FixedTerminologyTransferable.get(concept);
        }

        private Transferable makeTreeTransferable(JTree tree) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null) {
                return FixedTerminologyTransferable.get(node.getUserObject());
            }
            return null;
        }

        private Transferable makeTableTransferable(JTable table) {
            return FixedTerminologyTransferable.get(table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()));
        }

        public void dragGestureRecognized(DragGestureEvent dge) {
            try {
                dge.startDrag(DragSource.DefaultCopyDrop, null, null, createTransferable(c), this);
            } catch (InvalidDnDOperationException e) {
                e.printStackTrace();
                SunDragSourceContextPeer.setDragDropInProgress(false);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void dragDropEnd(DragSourceDropEvent arg0) {
            // TODO Auto-generated method stub

        }

        public void dragEnter(DragSourceDragEvent arg0) {
            // TODO Auto-generated method stub

        }

        public void dragExit(DragSourceEvent arg0) {
            // TODO Auto-generated method stub

        }

        public void dragOver(DragSourceDragEvent arg0) {
            // TODO Auto-generated method stub

        }

        public void dropActionChanged(DragSourceDragEvent arg0) {
            // TODO Auto-generated method stub

        }

    }

    private static String nullLabel = "<html><font color='red'>null";

    private class MyFocusListener implements FocusListener {
        Border focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");

        Border plainBorder;

        public void focusGained(FocusEvent arg0) {
            plainBorder = getBorder();
            // System.out.println("focus gained");
            setBorder(focusBorder);
        }

        public void focusLost(FocusEvent arg0) {
            // System.out.println("focus lost");
            setBorder(plainBorder);
        }

    }

    I_ConceptualizeLocally concept;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private class DeleteAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            setNull();
        }

    }

    public ConceptLabel() {
        super("text/html", nullLabel);
        this.setEditable(false);
        this.setFocusable(true);
        // this.setBorder(BorderFactory.createLoweredBevelBorder());
        TermViewerTransferHandler handler = new TermViewerTransferHandler(this, "universalConcept");
        this.setTransferHandler(handler);
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, handler);
        this.addFocusListener(new MyFocusListener());

        InputMap imap = this.getInputMap();
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            TransferHandler.getCutAction().getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            TransferHandler.getCopyAction().getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            TransferHandler.getPasteAction().getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK), TransferHandler.getCutAction().getValue(
            Action.NAME));
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK), TransferHandler.getCopyAction().getValue(
            Action.NAME));
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK), TransferHandler.getPasteAction().getValue(
            Action.NAME));

        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DEL");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DEL");

        ActionMap map = this.getActionMap();
        map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
        map.put("DEL", new DeleteAction());
    }

    public JComponent getComponent() {
        return this;
    }

    public JToggleButton getToggle() {
        return null;
    }

    public void setUniversalConcept(I_ConceptualizeUniversally concept) throws Exception {
        logger.info("Setting universal concept to: " + concept);
        this.setConcept(concept.localize());
    }

    public I_ConceptualizeUniversally getUniversalConcept() throws Exception {
        return concept.universalize();
    }

    public void setNull() {
        Object old = this.concept;
        this.concept = null;
        setText(nullLabel);
        firePropertyChange("concept", old, this.concept);
    }

    public void setConcept(I_ConceptualizeLocally concept) throws Exception {
        Object old = this.concept;
        this.concept = concept;
        if (this.concept == null) {
            setText(nullLabel);
        } else {
            setText("<html>"
                + concept.getDescription(ArchitectonicAuxiliary.getLocalFullySpecifiedDescPrefList()).getText());
        }
        firePropertyChange("concept", old, this.concept);
    }

    private boolean onByDefault = true;

    public boolean isOnByDefault() {
        return onByDefault;
    }

    public I_ConceptualizeLocally getConcept() {
        return concept;
    }
}
