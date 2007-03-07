/*
 * Created on Mar 19, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.dnd;

import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;


public class BpaSourceListener implements DragSourceListener {
    private I_DoDragAndDrop dragSource;

    private String prefix;
    
    private boolean dragging = false;
    

    /**
     * @param dragSource
     * @param prefix
     */
    public BpaSourceListener(I_DoDragAndDrop dragSource, String prefix) {
        super();
        this.dragSource = dragSource;
        this.prefix = prefix;
    }

    /**
     * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
     */
    public void dragEnter(DragSourceDragEvent ev) {
        /*System.out.println("Source " + this.prefix + " DragEnter: "
                + ev.toString());
        */
        this.dragging = true;
        this.dragSource.highlightForDrag(true);
    }

    /**
     * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
     */
    public void dragOver(DragSourceDragEvent ev) {
        /*System.out.println("Source " + this.prefix + " dragOver: "
                + ev.toString());*/
        this.dragging = true;

    }

    /**
     * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
     */
    public void dropActionChanged(DragSourceDragEvent ev) {
        System.out.println("Source " + this.prefix + " dropActionChanged: "
                + ev.toString());
        
    }

    /**
     * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
     */
    public void dragExit(DragSourceEvent ev) {
        /*System.out.println("Source " + this.prefix + " dragExit: "
                + ev.toString());
                */
        this.dragging = false;
        this.dragSource.highlightForDrag(false);
        
    }

    /**
     * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
     */
    public void dragDropEnd(DragSourceDropEvent ev) {
        /*System.out.println("Source " + this.prefix + " dragDropEnd: "
                + ev.toString());
                */
        this.dragging = false;
        this.dragSource.highlightForDrag(false);
        //System.out.println(" drop success = " + ev.getDropSuccess());

    }

    /**
     * @return Returns the isDragging.
     */
    public boolean isDragging() {
        return dragging;
    }
}