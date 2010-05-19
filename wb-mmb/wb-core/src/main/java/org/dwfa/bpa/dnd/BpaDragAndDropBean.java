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
 * Created on Mar 19, 2005
 */
package org.dwfa.bpa.dnd;

import java.awt.Color;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class BpaDragAndDropBean implements PropertyChangeListener, I_SupportDragAndDrop {
    private I_DoDragAndDrop dndComponent;

    private DragSource dragSource;

    private DragGestureListener gestureListener;

    private BpaSourceListener sourceListener;

    private DropTarget dropTarget;

    private DropTargetListener dtListener;

    private int acceptableActions = DnDConstants.ACTION_COPY;

    private Border originalBorder;

    private Border highlightBorder;

    private DragGestureRecognizer recognizer;

    /**
     * @param prefix
     * @throws ClassNotFoundException
     */
    public BpaDragAndDropBean(String prefix, I_DoDragAndDrop dndComponent, boolean allowDrop, boolean allowDrag)
            throws ClassNotFoundException {
        this.dndComponent = dndComponent;
        this.dndComponent.getJComponent().addPropertyChangeListener("border", this);
        if (allowDrag) {
            this.dragSource = DragSource.getDefaultDragSource();
            this.sourceListener = new BpaSourceListener(dndComponent, prefix);
            this.gestureListener = new BpaGestureListener(dndComponent, this.sourceListener);

            recognizer = this.dragSource.createDefaultDragGestureRecognizer(dndComponent.getJComponent(),
                DnDConstants.ACTION_COPY, this.gestureListener);

        }

        this.originalBorder = dndComponent.getJComponent().getBorder();
        this.highlightForDrop(false);

        if (allowDrop) {
            this.dtListener = new BpaTargetListener(dndComponent, prefix);
            this.dropTarget = new DropTarget(dndComponent.getJComponent(), this.acceptableActions, this.dtListener,
                true);
        }
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
            this.dndComponent.getJComponent().setBorder(this.highlightBorder);
        } else {
            this.highlightBorder = BorderFactory.createCompoundBorder(this.originalBorder,
                BorderFactory.createEmptyBorder(2, 2, 2, 2));
            this.dndComponent.getJComponent().setBorder(this.highlightBorder);
        }
    }

    /**
     * @see org.dwfa.bpa.dnd.I_SupportDragAndDrop#highlightForDrop(boolean)
     */
    public void highlightForDrop(boolean highlight) {
        if (highlight) {
            this.highlightBorder = BorderFactory.createCompoundBorder(this.originalBorder,
                BorderFactory.createLineBorder(Color.GREEN, 2));
            this.dndComponent.getJComponent().setBorder(this.highlightBorder);
            this.dndComponent.getJComponent().requestFocus();
        } else {
            this.highlightBorder = BorderFactory.createCompoundBorder(this.originalBorder,
                BorderFactory.createEmptyBorder(2, 2, 2, 2));
            this.dndComponent.getJComponent().setBorder(this.highlightBorder);
        }

    }

    /**
     * @see org.dwfa.bpa.dnd.I_SupportDragAndDrop#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue().equals(this.highlightBorder) == false) {
            this.originalBorder = this.dndComponent.getJComponent().getBorder();
            highlightForDrag(false);
        }

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
        return this.dndComponent.getTransferable();
    }

    /**
     * @see org.dwfa.bpa.dnd.I_SupportDragAndDrop#isDropActive()
     */
    public boolean isDropActive() {
        return dropTarget.isActive();
    }
}
