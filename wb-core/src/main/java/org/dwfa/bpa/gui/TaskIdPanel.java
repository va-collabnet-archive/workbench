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
 * Created on Mar 20, 2005
 */
package org.dwfa.bpa.gui;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.dwfa.bpa.dnd.BpaDragAndDropBean;
import org.dwfa.bpa.dnd.I_DoDragAndDrop;
import org.dwfa.bpa.dnd.I_SupportDragAndDrop;
import org.dwfa.bpa.dnd.TaskTransferable;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;

/**
 * @author kec
 * 
 */
public class TaskIdPanel extends JLabel implements I_DoDragAndDrop, PropertyChangeListener {

    private static Logger logger = Logger.getLogger(TaskIdPanel.class.getName());
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
    public TaskIdPanel(int id, I_EncodeBusinessProcess process) throws ClassNotFoundException {
        this(id, process, false);
    }

    public TaskIdPanel(int id, I_EncodeBusinessProcess process, boolean processOnly) throws ClassNotFoundException {
        super();
        this.process = process;
        if (processOnly) {
            this.dndBean = new BpaDragAndDropBean("TaskId", this, true, false);
        } else {
            this.dndBean = new BpaDragAndDropBean("TaskId", this, true, false);
        }
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
            if (this.process.getNumberOfTasks() > this.getId()) {
                I_DefineTask task = this.process.getTask(this.getId());
                try {
                    BeanInfo taskInfo = task.getBeanInfo();
                    this.setToolTipText(taskInfo.getBeanDescriptor().getDisplayName());
                } catch (IntrospectionException e) {
                    e.printStackTrace();
                    this.setToolTipText(task.getName());
                }

                this.setToolTipText(task.getName());
            } else {
                this.setToolTipText("root not yet set");
            }
        }

    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#setDroppedTask(org.dwfa.bpa.process.I_DefineTask,
     *      java.awt.dnd.DropTargetDropEvent)
     */
    public void setDroppedObject(Object task, DropTargetDropEvent ev) {
        this.setId(((I_DefineTask) task).getId());
        this.updateTextAndTooltip();
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#setDroppedTask(org.dwfa.bpa.process.I_DefineTask)
     */
    public void setDroppedObject(Object task) {
        this.setId(((I_DefineTask) task).getId());
        this.updateTextAndTooltip();
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#getTaskToDrop()
     */
    public I_DefineTask getTaskToDrop() {
        throw new UnsupportedOperationException();
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
        return TaskTransferable.getImportFlavors();
    }

    /**
     * @see org.dwfa.bpa.gui.dnd.I_DoTaskDragAndDrop#isFlavorSupportedForImport(java.awt.datatransfer.DataFlavor)
     */
    public boolean isFlavorSupportedForImport(DataFlavor flavor) {
        return TaskTransferable.isFlavorSupported(flavor, TaskTransferable.getImportFlavors());
    }

    /**
     * @see org.dwfa.bpa.gui.dnd.I_DoTaskDragAndDrop#isFlavorSupportedForExport(java.awt.datatransfer.DataFlavor)
     */
    public boolean isFlavorSupportedForExport(DataFlavor flavor) {
        return TaskTransferable.isFlavorSupported(flavor, TaskTransferable.getExportFlavors());
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
        if (this.id == -1) {
            this.setForeground(Color.RED);
        } else {
            this.setForeground(Color.BLACK);
        }
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
        return new TaskTransferable(this.getTaskToDrop());
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoDragAndDrop#getLocalFlavors()
     */
    public Collection<DataFlavor> getLocalFlavors() {
        ArrayList<DataFlavor> flavorList = new ArrayList<DataFlavor>();
        flavorList.add(TaskTransferable.getLocalTaskFlavor());
        return flavorList;
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoDragAndDrop#getSerialFlavors()
     */
    public Collection<DataFlavor> getSerialFlavors() {
        ArrayList<DataFlavor> flavorList = new ArrayList<DataFlavor>();
        flavorList.add(TaskTransferable.getSerialTaskFlavor());
        return flavorList;
    }

    public Logger getLogger() {
        return logger;
    }
}
