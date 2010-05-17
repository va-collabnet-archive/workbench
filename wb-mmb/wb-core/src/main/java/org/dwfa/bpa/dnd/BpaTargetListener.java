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
package org.dwfa.bpa.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BpaTargetListener implements DropTargetListener {

    private static Logger logger = Logger.getLogger("org.dwfa.bpa.dnd");

    private I_DoDragAndDrop targetComponent;

    private String prefix;

    private boolean dropHighlight = false;

    /**
     * @param targetComponent
     * @param prefix
     * @param label
     *            TODO
     * @throws ClassNotFoundException
     */
    public BpaTargetListener(I_DoDragAndDrop targetComponent, String prefix) throws ClassNotFoundException {
        super();
        this.targetComponent = targetComponent;
        this.prefix = prefix;
    }

    /**
     * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
     */
    public void dragEnter(DropTargetDragEvent ev) {
        if (this.isDragOk(ev)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Target " + this.prefix + " dragEnter targetComponent" + ev);
            }
            this.targetComponent.highlightForDrop(true);
            this.dropHighlight = true;
            ev.acceptDrag(this.targetComponent.getAcceptableActions());
        }

    }

    /**
     * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
     */
    public void dragOver(DropTargetDragEvent ev) {
        if (this.isDragOk(ev)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Target " + this.prefix + " dragOver targetComponent" + ev);
            }
            ev.acceptDrag(this.targetComponent.getAcceptableActions());
        }

    }

    /**
     * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
     */
    public void dropActionChanged(DropTargetDragEvent ev) {
        if (this.isDragOk(ev)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Target " + this.prefix + " dropActionChanged targetComponent" + ev);
            }
            ev.acceptDrag(this.targetComponent.getAcceptableActions());
        }

    }

    /**
     * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
     */
    public void dragExit(DropTargetEvent ev) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Target " + this.prefix + " dragExit targetComponent" + ev);
        }
        if (this.dropHighlight) {
            this.targetComponent.highlightForDrop(false);
            this.dropHighlight = false;
        }
    }

    /**
     * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
     */
    public void drop(DropTargetDropEvent ev) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Target " + this.prefix + " drop targetComponent" + ev);
        }
        this.targetComponent.getJComponent().requestFocus();
        if (this.dropHighlight) {
            this.targetComponent.highlightForDrop(false);
            this.dropHighlight = false;

            DataFlavor chosen = null;
            Iterator<DataFlavor> localFlavorItr = this.targetComponent.getLocalFlavors().iterator();
            while (localFlavorItr.hasNext()) {
                DataFlavor flavor = localFlavorItr.next();
                if (ev.isDataFlavorSupported(flavor)) {
                    chosen = flavor;
                    break;
                }
            }
            if (chosen == null) {
                Iterator<DataFlavor> serialFlavorItr = this.targetComponent.getLocalFlavors().iterator();
                while (serialFlavorItr.hasNext()) {
                    DataFlavor flavor = serialFlavorItr.next();
                    if (ev.isDataFlavorSupported(flavor)) {
                        chosen = flavor;
                        break;
                    }
                }
            }
            // the actions that the sourceId has specified with
            // DragGestureRecognizer
            int sa = ev.getSourceActions();
            if ((sa & this.targetComponent.getAcceptableActions()) == 0) {
                // System.out.println(" reject drop");
                ev.rejectDrop();
                ev.dropComplete(true);
                return;
            }
            Object data = null;
            try {
                // System.out.println(" accept drop");
                ev.acceptDrop(this.targetComponent.getAcceptableActions());
                data = ev.getTransferable().getTransferData(chosen);
                if (data == null) {
                    ev.rejectDrop();
                    ev.dropComplete(true);
                    throw new NullPointerException();
                }

                // Add drop handler here...
                this.targetComponent.setDroppedObject(data, ev);

                ev.dropComplete(true);
            } catch (Throwable t) {
                t.printStackTrace();
                ev.dropComplete(true);
                // showBorder(false);
                return;
            }

        } else {
            System.out.println(prefix + " reject drop");
            ev.rejectDrop();
            ev.dropComplete(true);
        }

    }

    private boolean isDragOk(DropTargetDragEvent ev) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Target " + this.prefix + " isDragOk targetComponent" + ev);
        }
        if (this.targetComponent.isDragging()) {
            return false;
        }

        DataFlavor chosen = null;
        Iterator<DataFlavor> localFlavorItr = this.targetComponent.getLocalFlavors().iterator();
        while (localFlavorItr.hasNext()) {
            DataFlavor flavor = localFlavorItr.next();
            if (ev.isDataFlavorSupported(flavor)) {
                chosen = flavor;
                break;
            }
        }
        if (chosen == null) {
            Iterator<DataFlavor> serialFlavorItr = this.targetComponent.getLocalFlavors().iterator();
            while (serialFlavorItr.hasNext()) {
                DataFlavor flavor = serialFlavorItr.next();
                if (ev.isDataFlavorSupported(flavor)) {
                    chosen = flavor;
                    break;
                }
            }
        }
        if (chosen == null) {
            return false;
        }
        // the actions specified when the sourceId
        // created the DragGestureRecognizer
        int sa = ev.getSourceActions();

        // we're saying that these actions are necessary
        if ((sa & this.targetComponent.getAcceptableActions()) == 0) {
            return false;
        }

        return true;
    }
}
