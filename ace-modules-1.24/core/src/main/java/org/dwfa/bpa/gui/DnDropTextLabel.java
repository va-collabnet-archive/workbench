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
/*
 * Created on Mar 1, 2006
 */
package org.dwfa.bpa.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;

import org.dwfa.bpa.dnd.BpaGestureListener;
import org.dwfa.bpa.dnd.BpaTargetListener;
import org.dwfa.bpa.dnd.I_DoDragAndDrop;

public class DnDropTextLabel extends JLabel implements I_DoDragAndDrop, ActionListener {

    static class TransferAction extends AbstractAction implements UIResource {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        TransferAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src instanceof JComponent) {
                JComponent c = (JComponent) src;
                TransferHandler th = c.getTransferHandler();
                Clipboard clipboard = getClipboard(c);
                String name = (String) getValue(Action.NAME);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Doing transfer action: " + name + " with transfer handler: " + th);
                }

                Transferable trans = null;

                // any of these calls may throw IllegalStateException
                try {
                    if ((clipboard != null) && (th != null) && (name != null)) {
                        if ("cut".equals(name)) {
                            th.exportToClipboard(c, clipboard, DnDConstants.ACTION_MOVE);
                        } else if ("copy".equals(name)) {
                            th.exportToClipboard(c, clipboard, DnDConstants.ACTION_COPY);
                        } else if ("paste".equals(name)) {
                            trans = clipboard.getContents(null);
                        }
                    } else {
                        logger.log(Level.WARNING, "clipboard, th, or name is null: " + clipboard + " " + th + " "
                            + name);
                    }
                } catch (IllegalStateException ise) {
                    logger.log(Level.SEVERE, ise.getMessage(), ise);
                    UIManager.getLookAndFeel().provideErrorFeedback(c);
                    return;
                }

                // this is a paste action, import data into the component
                if (trans != null) {
                    th.importData(c, trans);
                }
            }
        }

        /**
         * Returns the clipboard to use for cut/copy/paste.
         */
        private Clipboard getClipboard(JComponent c) {
            return Toolkit.getDefaultToolkit().getSystemClipboard();
        }

    }

    static Logger logger = Logger.getLogger(DnDropTextLabel.class.getName());

    private DragSource dragSource;

    private DragGestureListener gestureListener;

    private SourceListener sourceListener;

    private DropTarget dropTarget;

    private DropTargetListener dtListener;

    private int acceptableActions = DnDConstants.ACTION_COPY;

    private Border originalBorder;

    private Border highlightBorder;

    private DragGestureRecognizer recognizer;

    private class FocusListenerForThis implements FocusListener {

        public void focusGained(FocusEvent e) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("focus gained for label: " + DnDropTextLabel.this.getText());
            }
            DnDropTextLabel.this.setBackground(UIManager.getColor("Tree.selectionBackground"));
            DnDropTextLabel.this.setOpaque(true);
        }

        public void focusLost(FocusEvent e) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("focus lost for label: " + DnDropTextLabel.this.getText());
            }
            DnDropTextLabel.this.setBackground(Color.WHITE);
            DnDropTextLabel.this.setOpaque(false);
        }

    }

    private class DragMouseListener implements MouseListener {
        public void mousePressed(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
            DnDropTextLabel.this.requestFocusInWindow();
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }

    private class SourceListener implements DragSourceListener {
        private I_DoDragAndDrop dragSource;

        private String prefix;

        private boolean dragging = false;

        /**
         * @param dragSource
         * @param prefix
         */
        public SourceListener(I_DoDragAndDrop dragSource, String prefix) {
            super();
            this.dragSource = dragSource;
            this.prefix = prefix;
        }

        /**
         * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
         */
        public void dragEnter(DragSourceDragEvent ev) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Source " + this.prefix + " DragEnter: " + ev.toString());
            }
            this.dragging = true;
            this.dragSource.highlightForDrag(true);
        }

        /**
         * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
         */
        public void dragOver(DragSourceDragEvent ev) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Source " + this.prefix + " dragOver: " + ev.toString());
            }
            this.dragging = true;

        }

        /**
         * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
         */
        public void dropActionChanged(DragSourceDragEvent ev) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Source " + this.prefix + " dropActionChanged: " + ev.toString());
            }
        }

        /**
         * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
         */
        public void dragExit(DragSourceEvent ev) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Source " + this.prefix + " dragExit: " + ev.toString());
            }
            this.dragging = false;
            this.dragSource.highlightForDrag(false);

        }

        /**
         * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
         */
        public void dragDropEnd(DragSourceDropEvent ev) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Source " + this.prefix + " dragDropEnd: " + ev.toString());
            }
            this.dragging = false;
            this.dragSource.highlightForDrag(false);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(" drop success = " + ev.getDropSuccess());
            }

        }

        /**
         * @return Returns the isDragging.
         */
        public boolean isDragging() {
            return dragging;
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DnDropTextLabel() throws ClassNotFoundException {
        super("<html><color='red'>Drag Property Here");
        String prefix = "tl";
        this.dragSource = DragSource.getDefaultDragSource();
        this.sourceListener = new SourceListener(this, prefix);
        this.gestureListener = new BpaGestureListener(this, this.sourceListener);

        recognizer = this.dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY,
            this.gestureListener);

        this.originalBorder = this.getBorder();
        this.highlightForDrop(false);

        this.dtListener = new BpaTargetListener(this, prefix);
        this.dropTarget = new DropTarget(this, this.acceptableActions, this.dtListener, true);

        addMouseListener(new DragMouseListener());
        this.setEnabled(true);
        this.setFocusable(true);
        this.addFocusListener(new FocusListenerForThis());
        this.setBackground(Color.WHITE);

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

        ActionMap map = this.getActionMap();
        map.put("cut", new TransferAction("cut"));
        map.put("copy", new TransferAction("copy"));
        map.put("paste", new TransferAction("paste"));
        this.setTransferHandler(new TransferHandler("text"));

    }

    /**
     * @see javax.swing.JComponent#getMinimumSize()
     */
    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        if (d.height < 25) {
            d.height = 25;
        }
        return d;
    }

    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        Dimension p = super.getPreferredSize();
        Dimension m = this.getMinimumSize();
        if (p.height < m.height) {
            p.height = m.height;
        }
        if (p.width < m.width) {
            p.width = m.width;
        }
        if (p.width > 110) {
            double oversizeFactor = Math.ceil(p.width / 110);
            p.width = 110;
            p.height = (int) (p.height * oversizeFactor);
        }
        return p;
    }

    /**
     * @see org.dwfa.bpa.dnd.I_SupportDragAndDrop#getAcceptableActions()
     */
    public int getAcceptableActions() {
        return acceptableActions;
    }

    /**
     * @see org.dwfa.bpa.dnd.I_SupportDragAndDrop#isDragging()
     */
    public boolean isDragging() {
        if (sourceListener == null) {
            return false;
        }
        return sourceListener.isDragging();
    }

    /**
     * @see org.dwfa.bpa.dnd.I_SupportDragAndDrop#highlightForDrag(boolean)
     */
    public void highlightForDrag(boolean highlight) {
        if (highlight) {
            this.highlightBorder = BorderFactory.createCompoundBorder(this.originalBorder,
                BorderFactory.createLineBorder(Color.RED, 2));
            super.setBorder(this.highlightBorder);
        } else {
            this.highlightBorder = BorderFactory.createCompoundBorder(this.originalBorder,
                BorderFactory.createEmptyBorder(2, 2, 2, 2));
            super.setBorder(this.highlightBorder);
        }
    }

    /**
     * @see org.dwfa.bpa.dnd.I_SupportDragAndDrop#highlightForDrop(boolean)
     */
    public void highlightForDrop(boolean highlight) {
        if (highlight) {
            this.highlightBorder = BorderFactory.createCompoundBorder(this.originalBorder,
                BorderFactory.createLineBorder(Color.GREEN, 2));
            super.setBorder(this.highlightBorder);
        } else {
            this.highlightBorder = BorderFactory.createCompoundBorder(this.originalBorder,
                BorderFactory.createEmptyBorder(2, 2, 2, 2));

            super.setBorder(this.highlightBorder);

        }

    }

    /**
     * @see org.dwfa.bpa.dnd.I_SupportDragAndDrop#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void setBorder(Border b) {
        this.originalBorder = b;
        this.highlightForDrop(false);
    }

    /**
     * @see org.dwfa.bpa.dnd.I_SupportDragAndDrop#resetRecognizer()
     */
    public void resetRecognizer() {
        this.recognizer.resetRecognizer();

    }

    /**
     * @see org.dwfa.bpa.dnd.I_SupportDragAndDrop#getTransferable()
     */
    public Transferable getTransferable() throws Exception {
        return new StringSelection(this.getText());
    }

    /**
     * @see org.dwfa.bpa.dnd.I_SupportDragAndDrop#isDropActive()
     */
    public boolean isDropActive() {
        return dropTarget.isActive();
    }

    public boolean isFlavorSupportedForImport(DataFlavor flavor) {
        return flavor.equals(DataFlavor.stringFlavor);
    }

    public boolean isFlavorSupportedForExport(DataFlavor flavor) {
        return flavor.equals(DataFlavor.stringFlavor);
    }

    public Collection<DataFlavor> getLocalFlavors() {
        return Arrays.asList(getImportDataFlavors());
    }

    public Collection<DataFlavor> getSerialFlavors() {
        return Arrays.asList(getImportDataFlavors());
    }

    public DataFlavor[] getImportDataFlavors() {
        return new DataFlavor[] { DataFlavor.stringFlavor };
    }

    public JComponent getJComponent() {
        return this;
    }

    public void setDroppedObject(Object obj, DropTargetDropEvent ev) {
        this.setText(obj.toString());
    }

    public void setDroppedObject(Object obj) {
        this.setText(obj.toString());
    }

    public Logger getLogger() {
        return logger;
    }

    public void actionPerformed(ActionEvent e) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(e.toString());
        }

    }

}
