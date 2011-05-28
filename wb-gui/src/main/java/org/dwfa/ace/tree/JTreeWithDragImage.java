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
package org.dwfa.ace.tree;

import java.awt.Color;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
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
import java.awt.event.KeyEvent;
import java.awt.image.FilteredImageSource;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.AceTransferAction;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

import sun.awt.dnd.SunDragSourceContextPeer;

public class JTreeWithDragImage extends JTree {

    private class TermLabelDragSourceListener implements DragSourceListener {

        public void dragDropEnd(DragSourceDropEvent dsde) {
            // TODO Auto-generated method stub
        }

        public void dragEnter(DragSourceDragEvent dsde) {
            // TODO Auto-generated method stub
        }

        public void dragExit(DragSourceEvent dse) {
            // TODO Auto-generated method stub
        }

        public void dragOver(DragSourceDragEvent dsde) {
            // TODO Auto-generated method stub
        }

        public void dropActionChanged(DragSourceDragEvent dsde) {
            // TODO Auto-generated method stub
        }
    }

    private class SelectionListener implements TreeSelectionListener {

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            nextToLastSelection = lastSelection;
            lastSelection = e.getNewLeadSelectionPath();
        }
    }

    private class DragGestureListenerWithImage implements DragGestureListener {

        DragSourceListener dsl;

        public DragGestureListenerWithImage(DragSourceListener dsl) {
            super();
            this.dsl = dsl;
        }

        @Override
        public void dragGestureRecognized(DragGestureEvent dge) {
            int selRow = getRowForLocation(dge.getDragOrigin().x, dge.getDragOrigin().y);
            TreePath path = getPathForLocation(dge.getDragOrigin().x, dge.getDragOrigin().y);
            if (selRow != -1) {
                try {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    I_GetConceptData obj = (I_GetConceptData) node.getUserObject();
                    Image dragImage = getDragImage(obj);
                    Point imageOffset = new Point(-10, -(dragImage.getHeight(JTreeWithDragImage.this) + 1));
                    try {
                        dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, getTransferable(obj), dsl);
                    } catch (InvalidDnDOperationException e) {
                        //AceLog.getAppLog().log(Level.WARNING, e.getMessage(), e);
                        AceLog.getAppLog().log(Level.INFO, "Resetting SunDragSourceContextPeer [4.]");
                        SunDragSourceContextPeer.setDragDropInProgress(false);
                    } catch (Exception ex) {
                        AceLog.getAppLog().alertAndLogException(ex);
                    }
                } catch (IOException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                } 
            }
        }

        private Transferable getTransferable(I_GetConceptData obj) throws TerminologyException, IOException {
            return new ConceptTransferable(Terms.get().getConcept(obj.getConceptNid()));
        }

        public Image getDragImage(I_GetConceptData obj) throws IOException {

            I_DescriptionTuple desc = obj.getDescTuple(config.getTreeDescPreferenceList(), config);
            if (desc == null) {
                desc = obj.getDescriptions().iterator().next().getFirstTuple();
            }
            JLabel dragLabel = TermLabelMaker.newLabel(desc, false, false).getLabel();
            dragLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            Image dragImage = createImage(dragLabel.getWidth(), dragLabel.getHeight());
            dragLabel.setVisible(true);
            Graphics og = dragImage.getGraphics();
            og.setClip(dragLabel.getBounds());
            dragLabel.paint(og);
            og.dispose();
            FilteredImageSource fis = new FilteredImageSource(dragImage.getSource(),
                    TermLabelMaker.getTransparentFilter());
            dragImage = Toolkit.getDefaultToolkit().createImage(fis);
            return dragImage;
        }
    }

    private class RefreshListener implements PropertyChangeListener {

        public RefreshListener() {
            super();
            lastPropagationId = Long.MIN_VALUE;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (lastPropagationId == evt.getPropagationId()) {
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().info("rf pc suppressed: "
                            + evt.getPropertyName() + " "
                            + evt.getPropagationId());
                }
                return;
            }
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().info(
                        "rf pc: " + evt.getPropertyName() + " "
                        + evt.getPropagationId() + " (" + lastPropagationId
                        + ") Thread: " + Thread.currentThread().getName()
                        + " tree hash: " + JTreeWithDragImage.this.hashCode());
            }
            lastPropagationId = evt.getPropagationId();

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    TreePath selection = lastSelection;
                    if (selection == null) {
                        selection = nextToLastSelection;
                    }
                    logSelectionPaths(selection, "Initial Selection Paths:");
                    int horizValue = Integer.MAX_VALUE;
                    int vertValue = Integer.MAX_VALUE;
                    if (scroller != null) {
                        horizValue = scroller.getHorizontalScrollBar().getValue();
                        vertValue = scroller.getVerticalScrollBar().getValue();
                    }
                    restoreTreePositionAndSelection(selection, horizValue, vertValue);
                }
            });

        }

        private void logSelectionPaths(TreePath selectionPath, String prefix) {
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                if (selectionPath != null) {
                    AceLog.getAppLog().fine(prefix + " \n  " + Arrays.asList(selectionPath));
                } else {
                    AceLog.getAppLog().fine(prefix + " \n  null");
                }
            }
        }
    }
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private I_ConfigAceFrame config;
    private List<ChangeListener> workerFinishedListeners = new ArrayList<ChangeListener>();
    public Object lastPropagationId;
    public JScrollPane scroller;
    private TreePath lastSelection;
    private TreePath nextToLastSelection;
    private TermTreeHelper helper;

    public JScrollPane getScroller() {
        return scroller;
    }

    public void setScroller(JScrollPane scroller) {
        this.scroller = scroller;
    }

    public Object getLastPropagationId() {
        return lastPropagationId;
    }

    public JTreeWithDragImage(I_ConfigAceFrame config) {
        this(config, null);
    }

    protected JTreeWithDragImage(I_ConfigAceFrame config, TermTreeHelper helper) {
        super();
        this.config = config;
        this.helper = helper;
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY,
                new DragGestureListenerWithImage(new TermLabelDragSourceListener()));
        InputMap imap = this.getInputMap();
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                TransferHandler.getCutAction().getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                TransferHandler.getCopyAction().getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                TransferHandler.getPasteAction().getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK),
                TransferHandler.getCutAction().getValue(
                Action.NAME));
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK),
                TransferHandler.getCopyAction().getValue(
                Action.NAME));
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK),
                TransferHandler.getPasteAction().getValue(
                Action.NAME));

        ActionMap map = this.getActionMap();
        map.put("cut", new AceTransferAction("cut"));
        map.put("copy", new AceTransferAction("copy"));
        map.put("paste", new AceTransferAction("paste"));
        RefreshListener rl = new RefreshListener();
        config.addPropertyChangeListener("commit", rl);
        config.addPropertyChangeListener("viewPositions", rl);
        config.addPropertyChangeListener("sortTaxonomyUsingRefset", rl);
        config.addPropertyChangeListener("showViewerImagesInTaxonomy", rl);
        config.addPropertyChangeListener("highlightConflictsInTaxonomyView", rl);
        config.addPropertyChangeListener("highlightConflictsInComponentPanel", rl);
        config.addPropertyChangeListener("conflictResolutionStrategy", rl);
        config.addPropertyChangeListener("showRefsetInfoInTaxonomy", rl);
        config.addPropertyChangeListener("showPathInfoInTaxonomy", rl);
        config.addPropertyChangeListener("updateHierarchyView", rl);
        addTreeSelectionListener(new SelectionListener());
    }

    public I_ConfigAceFrame getConfig() {
        return config;
    }

    public void addWorkerFinishedListener(ChangeListener l) {
        workerFinishedListeners.add(l);
    }

    public void removeWorkerFinishedListener(ChangeListener l) {
        workerFinishedListeners.remove(l);
    }

    public void workerFinished(ExpandNodeSwingWorker worker) {
        ChangeEvent event = new ChangeEvent(worker);
        List<ChangeListener> listeners;
        synchronized (workerFinishedListeners) {
            listeners = new ArrayList<ChangeListener>(workerFinishedListeners);
        }
        for (ChangeListener l : listeners) {
            l.stateChanged(event);
        }
    }

    public void restoreTreePositionAndSelection(int horizValue, int vertValue) {
        restoreTreePositionAndSelection(null, horizValue, vertValue);
    }

    @SuppressWarnings("unchecked")
    public void restoreTreePositionAndSelection(TreePath selection, int horizValue, int vertValue) {
        DefaultTreeModel m = (DefaultTreeModel) JTreeWithDragImage.this.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) m.getRoot();
        Enumeration<DefaultMutableTreeNode> childEnum = root.children();
        while (childEnum.hasMoreElements()) {
            DefaultMutableTreeNode node = childEnum.nextElement();
            if (node != null) {
                m.nodeChanged(node);
            }
        }
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Tree data changed");
        }

        if (scroller != null) {
            Timer timer = new Timer(1000, new RestoreSelectionSwingWorker(JTreeWithDragImage.this, lastPropagationId,
                    horizValue, vertValue, selection, helper));
            timer.setRepeats(false);
            timer.start();
        }
    }
}
