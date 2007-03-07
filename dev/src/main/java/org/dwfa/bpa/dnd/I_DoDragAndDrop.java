/*
 * Created on Mar 22, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.dnd;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.JComponent;


/**
 * @author kec
 *
 */
public interface I_DoDragAndDrop {
    public void highlightForDrop(boolean highlight);
    public void highlightForDrag(boolean highlight);
    /**
     * @return Returns the acceptableActions.
     */
    public int getAcceptableActions();
    
    public boolean isFlavorSupportedForImport(DataFlavor flavor);
    public boolean isFlavorSupportedForExport(DataFlavor flavor);
    public Collection<DataFlavor> getLocalFlavors();
    public Collection<DataFlavor> getSerialFlavors();
    public DataFlavor[] getImportDataFlavors();
    public Image createImage(int width, int height);
    public boolean isDragging();
    
    public JComponent getJComponent();
    
    public Transferable getTransferable() throws Exception;
    public void setDroppedObject(Object obj, DropTargetDropEvent ev);
    public void setDroppedObject(Object obj);
    public Logger getLogger();

}
