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
package org.dwfa.bpa.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import org.dwfa.bpa.dnd.DataContainerTransferable;
import org.dwfa.bpa.dnd.I_DoDragAndDrop;
import org.dwfa.bpa.dnd.I_SupportDragAndDrop;
import org.dwfa.bpa.dnd.TaskTransferable;
import org.dwfa.bpa.process.I_ContainData;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;


/**
 * @author kec
 * 
 */
public class DataContainerPanel extends JPanel implements I_DoDragAndDrop,
        MouseInputListener, PropertyChangeListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    static Logger logger = Logger.getLogger(DataContainerPanel.class
            .getName());

    private class DescriptionListener extends InputVerifier implements
            ActionListener {
        JTextField descriptionField;

        /**
         * @param descriptionField
         */
        public DescriptionListener(JTextField descriptionField) {
            super();
            this.descriptionField = descriptionField;
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            try {
                data.setDescription(this.descriptionField.getText());
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Changed description: " + data);
                }
            } catch (PropertyVetoException e1) {
                this.descriptionField.setText(data.getDescription());
                JOptionPane.showMessageDialog(DataContainerPanel.this, e1
                        .toString());
            }

        }

        /**
         * @see javax.swing.InputVerifier#verify(javax.swing.JComponent)
         */
        public boolean verify(JComponent arg0) {
            try {
                data.setDescription(this.descriptionField.getText());
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Changed description: " + data);
                }
            } catch (PropertyVetoException e1) {
                this.descriptionField.setText(data.getDescription());
                JOptionPane.showMessageDialog(DataContainerPanel.this, e1
                        .toString());
                return false;
            }
            return true;
        }

    }

    private I_SupportDragAndDrop dndBean;

    private I_ContainData data;

    boolean newInstanceOnDrop;

    private boolean movingComponent = false;

    private Point offset;

    private I_EncodeBusinessProcess process;

    JComponent descriptionComponent;

    I_Work worker;

    /**
     * @throws PropertyVetoException
     * @throws ClassNotFoundException
     * @throws QueryException
     * @throws InvalidComponentException
     * @throws ValidationException
     * @throws IdentifierIsNotNativeException
     * @throws NoMappingException
     * @throws RemoteException
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * 
     */
    public DataContainerPanel(I_ContainData data, boolean allowDrag,
            boolean newInstanceOnDrop, I_EncodeBusinessProcess process,
            I_Work worker) throws PropertyVetoException,
            Exception {
        super(new GridBagLayout());
        this.data = data;
        this.newInstanceOnDrop = newInstanceOnDrop;
        this.process = process;
        this.worker = worker;
        //this.dndBean = new BpaDragAndDropBean("Data", this, false, allowDrag);
        this.dndBean = data.getDragAndDropSupport("Data", this, true, allowDrag);
        this.setBackground(new Color(0xFFE4E1)); // MistyRose
        if (this.process != null) {
            if (this.getId() != -1) {
                layoutAttachedProcess();
            }
        } else {
            layoutNoAttachedProcess(data);
        }
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        //I_SupportDragAndDrop dndSupport = this.data.getDragAndDropSupport("dcp", this, true, true);
        //I_TransferData dndataTransfer = (I_TransferData) dndSupport.getTransferable();
    }

    /**
     * @throws PropertyVetoException
     * @throws ClassNotFoundException
     * @throws QueryException
     * @throws InvalidComponentException
     * @throws ValidationException
     * @throws IdentifierIsNotNativeException
     * @throws NoMappingException
     * @throws RemoteException
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * 
     */
    private void layoutAttachedProcess() throws PropertyVetoException,
            Exception {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;

        JTextField descriptionField = new JTextField(15);
        descriptionField.setText(data.getDescription());
        DescriptionListener l = new DescriptionListener(descriptionField);
        descriptionField.addActionListener(l);
        descriptionField.setInputVerifier(l);
        this.descriptionComponent = descriptionField;
        this.add(this.descriptionComponent, c);

        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridy++;
        c.gridx = 0;
        c.weightx = 1;
        c.weighty = 1;
        this.add(data.getEditor(), c);
        
        c.weightx = 0;
        c.weighty = 0;
        c.gridy++;
        c.gridx = 0;
        this.add(new JLabel("id: " + data.getId()), c);
        c.gridy++;
        if (data.isCollection()) {
            this.add(new JLabel("Collection<" + data.getElementClass().getSimpleName() + ">"), c);
        } else {
            this.add(new JLabel(data.getElementClass().getSimpleName()), c);
        }
        if (this.process.getDataContainer(this.getId())
                .getDataContainerBounds() != null) {
            this.setBounds(this.process.getDataContainer(this.getId())
                    .getDataContainerBounds());
        }
    }

    /**
     * @param data
     */
    private void layoutNoAttachedProcess(I_ContainData data) {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        JLabel nameLabel = new JLabel(data.getDescription(), JLabel.CENTER);
        this.descriptionComponent = nameLabel;
        this.add(nameLabel, c);
    }

    /**
     * @return
     */
    public int getAcceptableActions() {
        return dndBean.getAcceptableActions();
    }

    /**
     * @param highlight
     */
    public void highlightForDrag(boolean highlight) {
        dndBean.highlightForDrag(highlight);
    }

    /**
     * @param highlight
     */
    public void highlightForDrop(boolean highlight) {
        dndBean.highlightForDrop(highlight);
    }

    /**
     * @return
     */
    public boolean isDragging() {
        return dndBean.isDragging();
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
        return DataContainerTransferable.isFlavorSupported(flavor,
                TaskTransferable.getImportFlavors());
    }

    /**
     * @see org.dwfa.bpa.gui.dnd.I_DoTaskDragAndDrop#isFlavorSupportedForExport(java.awt.datatransfer.DataFlavor)
     */
    public boolean isFlavorSupportedForExport(DataFlavor flavor) {
        return DataContainerTransferable.isFlavorSupported(flavor,
                TaskTransferable.getExportFlavors());
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#setDroppedTask(org.dwfa.bpa.process.I_DefineTask)
     */
    public void setDroppedObject(Object task, DropTargetDropEvent ev) {
        throw new UnsupportedOperationException();

    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#getJComponent()
     */
    public JComponent getJComponent() {
        return this;
    }

    /**
     * @see org.dwfa.bpa.dnd.I_DoTaskDragAndDrop#setDroppedTask(org.dwfa.bpa.process.I_DefineTask)
     */
    public void setDroppedObject(Object task) {
        throw new UnsupportedOperationException();

    }

    /**
     * @return
     */
    public int getId() {
        return this.data.getId();
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        this.movingComponent = false;
        this.offset = null;
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        if (e.isShiftDown() && this.process != null) {
            this.dndBean.resetRecognizer();
            if (this.contains(e.getPoint())) {
                this.movingComponent = true;
                this.offset = e.getPoint();
            }
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e) {
        if (this.movingComponent) {
            Point oldLocation = this.getLocation();
            Point parentLocation = SwingUtilities.convertPoint(this, e
                    .getPoint(), this.getParent());
            this.setLocation(parentLocation.x - offset.x, parentLocation.y
                    - offset.y);
            this.process.getDataContainer(this.getId()).setDataContainerBounds(
                    this.getBounds());
            this.firePropertyChange("dataLocation", oldLocation, this
                    .getLocation());
        }
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent e) {

    }

    /**
     * @return Returns the data.
     */
    public I_ContainData getData() {
        return data;
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("id")) {
            this.firePropertyChange("branch", null, null);
        }

    }

    public Point getBranchEntrancePoint() {
        Rectangle nameBounds = this.descriptionComponent.getBounds();
        int x = this.getX() + nameBounds.x + this.getWidth();
        int y = this.getY() + nameBounds.y + (nameBounds.height / 2);
        return new Point(x, y);
    }

    public Transferable getTransferable() throws Exception {
        if (newInstanceOnDrop) {
                Constructor<? extends I_ContainData> c = 
                		this.data.getClass().getConstructor(new Class[] {});
                I_ContainData newData = c.newInstance(new Object[] {});
                return newData.getTransferable();
                //return new DataContainerTransferable(newData);
        }
        return this.data.getTransferable();
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
