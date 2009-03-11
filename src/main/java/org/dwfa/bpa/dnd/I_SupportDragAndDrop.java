/*
 * Created on Jan 23, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.dnd;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;

public interface I_SupportDragAndDrop {

    /**
     * @return Returns the acceptableActions.
     */
    public int getAcceptableActions();

    public boolean isDragging();

    public void highlightForDrag(boolean highlight);

    public void highlightForDrop(boolean highlight);

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt);

    /**
     * 
     */
    public void resetRecognizer();

    public Transferable getTransferable() throws Exception;

    /**
     * @see java.awt.dnd.DropTarget#isActive()
     */
    public boolean isDropActive();

}