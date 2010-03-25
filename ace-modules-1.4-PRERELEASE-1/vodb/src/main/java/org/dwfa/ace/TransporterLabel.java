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
package org.dwfa.ace;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.FilteredImageSource;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.Border;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.tree.ExpandPathToNodeStateListener;
import org.dwfa.vodb.types.ConceptBean;

public class TransporterLabel extends JLabel implements I_ContainTermComponent, ActionListener {

    private I_AmTermComponent termComponent;

    private ACE ace;

    private class MyFocusListener implements FocusListener {

        public void focusGained(FocusEvent arg0) {
            setBorder(focusBorder);
        }

        public void focusLost(FocusEvent arg0) {
            setBorder(plainBorder);
        }

    }

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

    private class DragGestureListenerWithImage implements DragGestureListener {

        DragSourceListener dsl;

        public DragGestureListenerWithImage(DragSourceListener dsl) {
            super();
            this.dsl = dsl;
        }

        public void dragGestureRecognized(DragGestureEvent dge) {
            if (termComponent == null) {
                return;
            }
            Image dragImage = getDragImage();
            Point imageOffset = new Point(0, 0);
            dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, new ConceptTransferable(
                (ConceptBean) termComponent), dsl);
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    Border focusBorder = BorderFactory.createLoweredBevelBorder();

    Border plainBorder = BorderFactory.createRaisedBevelBorder();

    JPopupMenu popup;

    public TransporterLabel(Icon image, ACE ace) {
        super(image);
        this.ace = ace;
        this.setFocusable(true);
        this.setEnabled(true);
        this.setBorder(plainBorder);
        this.addFocusListener(new MyFocusListener());
        this.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                TransporterLabel.this.requestFocusInWindow();
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
                TransporterLabel.this.requestFocusInWindow();
            }

        });
        setTransferHandler(new TerminologyTransferHandler(this));

        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY,
            new DragGestureListenerWithImage(new TermLabelDragSourceListener()));

        ActionMap map = this.getActionMap();
        map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());

        popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Show in taxonomy");
        popup.add(menuItem);
        menuItem.addActionListener(this);
        menuItem = new JMenuItem("Put in Concept Tab L-1");
        popup.add(menuItem);
        menuItem.addActionListener(this);
        popup.addSeparator();
        menuItem = new JMenuItem("Put in Concept Tab R-1");
        popup.add(menuItem);
        menuItem.addActionListener(this);
        menuItem = new JMenuItem("Put in Concept Tab R-2");
        popup.add(menuItem);
        menuItem.addActionListener(this);
        menuItem = new JMenuItem("Put in Concept Tab R-3");
        popup.add(menuItem);
        menuItem.addActionListener(this);
        menuItem = new JMenuItem("Put in Concept Tab R-4");
        popup.add(menuItem);
        popup.addSeparator();
        menuItem.addActionListener(this);
        menuItem = new JMenuItem("Add to list");
        menuItem.addActionListener(this);
        popup.add(menuItem);

        this.setToolTipText("drag and drop--or click then paste--concepts transport to the selected location");

    }

    public JComponent getComponent() {
        return this;
    }

    public JToggleButton getToggle() {
        return null;
    }

    private boolean onByDefault = true;

    public boolean isOnByDefault() {
        return onByDefault;
    }

    public I_AmTermComponent getTermComponent() {
        return termComponent;
    }

    public void setTermComponent(I_AmTermComponent termComponent) {
        this.termComponent = termComponent;
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mouseLocation, this);
        popup.show(this, mouseLocation.x, mouseLocation.y);
    }

    @Override
    public void setText(String text) {

    }

    public I_ConfigAceFrame getConfig() {
        return this.ace.getAceFrameConfig();
    }

    public Image getDragImage() {
        JLabel dragLabel = TermLabelMaker.makeLabel(termComponent.toString());
        dragLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        Image dragImage = createImage(dragLabel.getWidth(), dragLabel.getHeight());
        dragLabel.setVisible(true);
        Graphics og = dragImage.getGraphics();
        og.setClip(dragLabel.getBounds());
        dragLabel.paint(og);
        og.dispose();
        FilteredImageSource fis = new FilteredImageSource(dragImage.getSource(), TermLabelMaker.getTransparentFilter());
        dragImage = Toolkit.getDefaultToolkit().createImage(fis);
        return dragImage;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Show in taxonomy")) {
            try {
                new ExpandPathToNodeStateListener(this.ace.getTree(), this.ace.getAceFrameConfig(),
                    (I_GetConceptData) termComponent);
                this.ace.getAceFrameConfig().setHierarchySelection((I_GetConceptData) termComponent);
            } catch (IOException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }
        } else if (e.getActionCommand().equals("Put in Concept Tab L-1")) {
            I_HostConceptPlugins viewer = this.ace.getAceFrameConfig().getConceptViewer(5);
            viewer.setTermComponent(termComponent);
        } else if (e.getActionCommand().equals("Put in Concept Tab R-1")) {
            I_HostConceptPlugins viewer = this.ace.getAceFrameConfig().getConceptViewer(1);
            viewer.setTermComponent(termComponent);
        } else if (e.getActionCommand().equals("Put in Concept Tab R-2")) {
            I_HostConceptPlugins viewer = this.ace.getAceFrameConfig().getConceptViewer(2);
            viewer.setTermComponent(termComponent);
        } else if (e.getActionCommand().equals("Put in Concept Tab R-3")) {
            I_HostConceptPlugins viewer = this.ace.getAceFrameConfig().getConceptViewer(3);
            viewer.setTermComponent(termComponent);
        } else if (e.getActionCommand().equals("Put in Concept Tab R-4")) {
            I_HostConceptPlugins viewer = this.ace.getAceFrameConfig().getConceptViewer(4);
            viewer.setTermComponent(termComponent);
        } else if (e.getActionCommand().equals("Add to list")) {
            JList conceptList = this.ace.getAceFrameConfig().getBatchConceptList();
            I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
            model.addElement((I_GetConceptData) termComponent);
        }
    }
}
