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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.FilteredImageSource;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.types.ConceptBean;

public class TermComponentLabel extends JLabel implements FocusListener,
        I_ContainTermComponent {

    private I_AmTermComponent termComponent;

    private I_ConfigAceFrame config;

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
            Image dragImage = getDragImage();
            Point imageOffset = new Point(0, 0);
            dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset,
                new ConceptTransferable((ConceptBean) termComponent), dsl);
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public TermComponentLabel(I_ConfigAceFrame config) {
        super("<html><font color=red>Empty");
        this.config = config;
        addFocusListener(this);
        setTransferHandler(new TerminologyTransferHandler(this));

        DragSource.getDefaultDragSource()
            .createDefaultDragGestureRecognizer(
                this,
                DnDConstants.ACTION_COPY,
                new DragGestureListenerWithImage(
                    new TermLabelDragSourceListener()));

        setFocusable(true);
        setEnabled(true);

        this.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }

            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            public void mouseReleased(MouseEvent e) {
                requestFocusInWindow();
            }

        });

        ActionMap map = this.getActionMap();
        map.put(TransferHandler.getCutAction().getValue(Action.NAME),
            TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
            TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
            TransferHandler.getPasteAction());
        setBorder(noFocusBorder);
    }

    private static final Border hasFocusBorder =
            UIManager.getBorder("List.focusCellHighlightBorder");

    private static final Border noFocusBorder =
            BorderFactory.createEmptyBorder(1, 1, 1, 1);

    public void focusGained(FocusEvent e) {
        setBorder(hasFocusBorder);

    }

    public void focusLost(FocusEvent e) {
        setBorder(noFocusBorder);
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.I_ContainTermComponent#getTermComponent()
     */
    public I_AmTermComponent getTermComponent() {
        return termComponent;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.I_ContainTermComponent#setTermComponent(org.dwfa.vodb.types.I_AmTermComponent)
     */
    public void setTermComponent(I_AmTermComponent termComponent) {
        Object old = this.termComponent;
        this.termComponent = termComponent;
        if (termComponent != null) {
            if (ConceptBean.class.isAssignableFrom(termComponent.getClass())) {
                ConceptBean cb = (ConceptBean) termComponent;
                try {
                    I_DescriptionTuple desc =
                            cb.getDescTuple(config
                                .getLongLabelDescPreferenceList(), config);
                    if (desc != null) {
                        this.setText(desc.getText());
                    } else {
                        this.setText(cb.getInitialText());
                    }
                } catch (IOException e) {
                    this.setText(e.getMessage());
                    AceLog.getAppLog().alertAndLogException(e);
                }
            } else {
                this.setText(this.termComponent.toString());
            }
        } else {
            this.setText("<html><font color=red>Empty");
        }
        if (old == null) {
            firePropertyChange("termComponent", old, termComponent);
        } else if (old.equals(termComponent)) {
            firePropertyChange("termComponent", null, termComponent);
        } else {
            firePropertyChange("termComponent", old, termComponent);
        }
    }

    public void addTermChangeListener(PropertyChangeListener l) {
        addPropertyChangeListener("termComponent", l);
    }

    public void removeTermChangeListener(PropertyChangeListener l) {
        removePropertyChangeListener("termComponent", l);
    }

    public I_ConfigAceFrame getConfig() {
        throw new UnsupportedOperationException();
    }

    public Image getDragImage() {
        JLabel dragLabel = TermLabelMaker.makeLabel(getText());
        dragLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        Image dragImage =
                createImage(dragLabel.getWidth(), dragLabel.getHeight());
        dragLabel.setVisible(true);
        Graphics og = dragImage.getGraphics();
        og.setClip(dragLabel.getBounds());
        dragLabel.paint(og);
        og.dispose();
        FilteredImageSource fis =
                new FilteredImageSource(dragImage.getSource(), TermLabelMaker
                    .getTransparentFilter());
        dragImage = Toolkit.getDefaultToolkit().createImage(fis);
        return dragImage;
    }

}
