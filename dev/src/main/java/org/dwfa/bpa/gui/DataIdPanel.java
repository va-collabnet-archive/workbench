/*
 * Created on Mar 23, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.dwfa.bpa.dnd.BpaDragAndDropBean;
import org.dwfa.bpa.dnd.DataContainerTransferable;
import org.dwfa.bpa.dnd.I_DoDragAndDrop;
import org.dwfa.bpa.dnd.I_SupportDragAndDrop;
import org.dwfa.bpa.dnd.TaskTransferable;
import org.dwfa.bpa.process.I_ContainData;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;


/**
 * @author kec
 *
 */
public class DataIdPanel extends JLabel implements I_DoDragAndDrop, PropertyChangeListener {


    private static Logger logger = Logger.getLogger(DataIdPanel.class.getName());
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private I_SupportDragAndDrop dndBean;
    private int id = -1;
    private I_EncodeBusinessProcess process;

    /**
     * @throws ClassNotFoundException
     * 
     */
    public DataIdPanel(int id, I_EncodeBusinessProcess process, Class acceptableClass) throws ClassNotFoundException {
        super();
        this.process = process;
        this.dndBean = new BpaDragAndDropBean("DataId", this, true, false, acceptableClass);
        this.setId(id);
        this.updateTextAndTooltip();
    }

    /**
     * 
     */
    private void updateTextAndTooltip() {
        if (getId() == -1) {
             this.setText("<html><font color='red'>&Oslash");   
            this.setToolTipText("Not yet specified");
        } else {
         this.setText(Integer.toString(this.getId()));  
         if (this.process.getDataContainers().size() > this.getId()) {
            I_ContainData data = this.process.getDataContainer(this.getId());
            this.setToolTipText(data.getDescription());
         } else {
            this.setToolTipText("Error. Id is greater than number of data containers.");
         }
        }
        
        
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#setDroppedTask(org.dwfa.bpa.process.I_DefineTask, java.awt.dnd.DropTargetDropEvent)
     */
    public void setDroppedObject(Object obj, DropTargetDropEvent ev) {
        this.setId(((I_ContainData) obj).getId());
        this.updateTextAndTooltip();
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#setDroppedTask(org.dwfa.bpa.process.I_DefineTask)
     */
    public void setDroppedObject(Object obj) {
        this.setId(((I_ContainData) obj).getId());
        this.updateTextAndTooltip();
    }


    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#highlightForDrop(boolean)
     */
    public void highlightForDrop(boolean highlight) {
        this.dndBean.highlightForDrop(highlight);

    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#highlightForDrag(boolean)
     */
    public void highlightForDrag(boolean highlight) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#getAcceptableActions()
     */
    public int getAcceptableActions() {
        return this.dndBean.getAcceptableActions();
    }

    /**
     * @see org.dwfa.bpa.gui.dnd.I_DoTaskDragAndDrop#getImportDataFlavors()
     */
    public DataFlavor[] getImportDataFlavors() {
        return DataContainerTransferable.getImportFlavors();
    }

    /**
     * @see org.dwfa.bpa.gui.dnd.I_DoTaskDragAndDrop#isFlavorSupportedForImport(java.awt.datatransfer.DataFlavor)
     */
    public boolean isFlavorSupportedForImport(DataFlavor flavor) {
        return DataContainerTransferable.isFlavorSupported(flavor, TaskTransferable
                .getImportFlavors());
    }

    /**
     * @see org.dwfa.bpa.gui.dnd.I_DoTaskDragAndDrop#isFlavorSupportedForExport(java.awt.datatransfer.DataFlavor)
     */
    public boolean isFlavorSupportedForExport(DataFlavor flavor) {
        return DataContainerTransferable.isFlavorSupported(flavor, TaskTransferable
                .getExportFlavors());
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#isDragging()
     */
    public boolean isDragging() {
        return false;
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#getJComponent()
     */
    public JComponent getJComponent() {
        return this;
    }

    /**
     * @param id The id to set.
     */
    public void setId(int id) {
        int oldId = this.id;
        this.id = id;
        this.updateTextAndTooltip();
        this.firePropertyChange("id", oldId, this.id);
    }

    /**
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        this.setId(((Integer) evt.getNewValue()).intValue());
        
    }
    public Transferable getTransferable() throws ClassNotFoundException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoDragAndDrop#getLocalFlavors()
     */
    public Collection<DataFlavor> getLocalFlavors() {
        ArrayList<DataFlavor> flavorList = new ArrayList<DataFlavor>();
        flavorList.add(DataContainerTransferable.getLocalDataFlavor());
        return flavorList;
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoDragAndDrop#getSerialFlavors()
     */
    public Collection<DataFlavor> getSerialFlavors() {
        ArrayList<DataFlavor> flavorList = new ArrayList<DataFlavor>();
        flavorList.add(DataContainerTransferable.getSerialDataFlavor());
        return flavorList;
    }

    public Logger getLogger() {
        return logger;
    }
}
