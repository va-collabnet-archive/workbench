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
 * Created on Mar 10, 2005
 */
package org.dwfa.bpa.gui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.transaction.TransactionException;

import org.dwfa.bpa.data.ArrayListModel;
import org.dwfa.bpa.gui.GridBagPanel.GridBagPanelConstraints;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.util.bean.PropertyChangeSupportWithPropagationId;

/**
 * @author kec
 * 
 */
public class WorkspacePanel extends JPanel implements ListSelectionListener, ComponentListener, RemoteEventListener,
        PropertyChangeListener, InternalFrameListener, I_Workspace {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(WorkspacePanel.class.getName());

    // private JSplitPane splitPane;

    private JList panelList;

    private JLayeredPane layoutPanel;

    private JPanel statusPanel;

    private HashMap<String, Object> attachments = new HashMap<String, Object>();

    private PropertyChangeSupport propChangeSupport = new PropertyChangeSupportWithPropagationId(this);

    private ChangePanelPropertiesPanel changePanelProperties = new ChangePanelPropertiesPanel();

    private JLabel statusMessage = new JLabel("<HTML><font color='blue'>Welcome to the Architectonic workbench...");

    private JLabel altMessage = new JLabel("<HTML><font align='right' color='green'>Alt message");

    private JButton commit = new JButton("commit");

    private JButton cancel = new JButton("cancel");

    private PropertyChangeListener gridPanelListener = new GridPanelPropertyListener();

    private boolean showInInternalFrame = false;

    private UUID id;

    private WorkspaceFrame frame;

    private I_ManageUserTransactions transactionInterface;

    private Semaphore ownership = new Semaphore(1);

    public boolean isShownInInternalFrame() {
        return this.showInInternalFrame;
    }

    /**
     * @throws TransactionException
     * @throws QueryException
     * 
     */
    public WorkspacePanel(List<GridBagPanel> panels, WorkspaceFrame frame) throws TransactionException {
        this(panels, frame, null);
    }

    public WorkspacePanel(List<GridBagPanel> panels, WorkspaceFrame frame, I_ManageUserTransactions transactionInterface)
            throws TransactionException {
        super(new GridBagLayout());
        this.transactionInterface = transactionInterface;
        if (transactionInterface != null) {
            this.transactionInterface.addActiveTransactionListener(new ActiveTransactionListener());
            this.transactionInterface.addUncommittedComponentsListener(new UncommittedComponentsListener());
            this.cancel.setEnabled(transactionInterface.isTransactionActive());
        } else {
            this.cancel.setEnabled(false);
        }
        updateCommitButton(transactionInterface);

        this.cancel.addActionListener(new AbortAction());
        this.commit.addActionListener(new CommitAction());
        this.frame = frame;
        this.statusPanel = makeStatusPanel(this.statusMessage, altMessage, commit, cancel);
        if (this.showInInternalFrame) {
            layoutPanel = new JDesktopPane();
        } else {
            layoutPanel = new JLayeredPane();
        }

        setupPanelList(panels);

        // JPanel configSide = new JPanel(new GridBagLayout());

        // setupConfigSide(configSide);
        // splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, configSide,
        // layoutPanel);
        // splitPane.setOneTouchExpandable(true);
        // splitPane.setDividerLocation(200);
        // splitPane.setDividerLocation(0);

        for (GridBagPanel gbp : panels) {
            gbp.addGridBagConstraintsListener(this.gridPanelListener);
        }
        redoLayout();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        this.add(layoutPanel, c);
        c.weighty = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(this.statusPanel, c);
        this.addComponentListener(this);
        // this.splitPane.addPropertyChangeListener(
        // JSplitPane.DIVIDER_LOCATION_PROPERTY, this);
    }

    private void updateCommitButton(I_ManageUserTransactions config) throws TransactionException {
        if (transactionInterface != null) {
            if (transactionInterface.isTransactionActive()) {
                this.commit.setText("<html><font color='#006400'>commit");
                this.commit.setEnabled(true);
            } else {
                this.commit.setText("commit");
                this.commit.setEnabled(false);
            }
        } else {
            this.commit.setText("commit");
            this.commit.setEnabled(false);
        }
    }

    /**
     * @param panels
     */
    private void setupPanelList(List<GridBagPanel> panels) {
        panelList = new JList(new ArrayListModel<GridBagPanel>(panels));
        panelList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        panelList.setLayoutOrientation(JList.VERTICAL);
        panelList.setVisibleRowCount(-1);
        panelList.addListSelectionListener(this);
    }

    private JPanel makeStatusPanel(JLabel statusMessage, JLabel altMessage, JButton commit, JButton cancel) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.weighty = 0;
        c.gridy = 0;
        c.gridx = 5;
        panel.add(new JLabel("    "), c); // filler for grow box.

        c.gridx = 4;
        if (this.transactionInterface != null) {
            panel.add(commit, c);
        }
        c.gridx = 3;
        if (this.transactionInterface != null) {
            panel.add(cancel, c);
        }
        c.gridx = 2;
        panel.add(altMessage, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        statusMessage.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(statusMessage, c);
        c.gridx = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("    "), c);

        return panel;
    }

    @SuppressWarnings("unchecked")
    public void addGridBagPanel(GridBagPanel panel) {

        ArrayListModel<GridBagPanel> listModel = (ArrayListModel<GridBagPanel>) this.panelList.getModel();
        panel.addGridBagConstraintsListener(this.gridPanelListener);
        listModel.add(panel);
        redoLayout();
        this.frame.redoWindowMenu();

    }

    private static class CandP {
        GridBagPanel p;
        GridBagPanelConstraints c;

        /**
         * @param c
         * @param p
         */
        public CandP(GridBagPanelConstraints c, GridBagPanel p) {
            super();
            this.c = c;
            this.p = p;
        }
    }

    @SuppressWarnings("unchecked")
    private void restartLayout() {

        // int dividerLocation = this.splitPane.getDividerLocation();
        // int lastDividerLocation = this.splitPane.getLastDividerLocation();
        ArrayListModel<GridBagPanel> listModel = (ArrayListModel<GridBagPanel>) this.panelList.getModel();
        List<CandP> panelAndConstraint = new ArrayList<CandP>();
        for (GridBagPanel gbp : listModel) {
            // This operation ensures that the layer and position properties
            // reflect any user changes.
            panelAndConstraint.add(new CandP(gbp.getConstraints(), gbp));
        }
        for (Component c : this.layoutPanel.getComponents()) {
            this.layoutPanel.remove(c);
        }
        if (this.showInInternalFrame) {
            if (JDesktopPane.class.isAssignableFrom(layoutPanel.getClass()) == false) {
                this.remove(layoutPanel);
                layoutPanel = new JDesktopPane();
                GridBagConstraints c = new GridBagConstraints();
                c.anchor = GridBagConstraints.NORTHWEST;
                c.fill = GridBagConstraints.BOTH;
                c.weightx = 1;
                c.weighty = 1;
                c.gridx = 0;
                c.gridy = 0;
                this.add(layoutPanel, c);
            }
        } else {
            if (JLayeredPane.class.isAssignableFrom(layoutPanel.getClass()) == false) {
                this.remove(layoutPanel);
                layoutPanel = new JLayeredPane();
                GridBagConstraints c = new GridBagConstraints();
                c.anchor = GridBagConstraints.NORTHWEST;
                c.fill = GridBagConstraints.BOTH;
                c.weightx = 1;
                c.weighty = 1;
                c.gridx = 0;
                c.gridy = 0;
                this.add(layoutPanel, c);
            }
        }
        // splitPane.setRightComponent(this.layoutPanel);
        for (GridBagPanel gbp : listModel) {
            gbp.setAddedToLayout(false);
            JInternalFrame intFrame = gbp.getInternalFrame();
            if (intFrame != null) {
                intFrame.removeInternalFrameListener(this);
                gbp.setInternalFrame(null);
            }

        }
        this.redoLayout();
        for (CandP entry : panelAndConstraint) {
            entry.p.setConstraints(entry.c);
        }
        // this.splitPane.setDividerLocation(dividerLocation);
        // this.splitPane.setLastDividerLocation(lastDividerLocation);
        this.setSize(this.getWidth() + 1, this.getHeight() + 1);
        this.setSize(this.getWidth() - 1, this.getHeight() - 1);
    }

    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    /**
     * @param panels
     * @param layout
     */
    @SuppressWarnings("unchecked")
    private void redoLayout() {
        ArrayListModel<GridBagPanel> listModel = (ArrayListModel<GridBagPanel>) this.panelList.getModel();
        int maxX = 0;
        int maxY = 0;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (GridBagPanel gbp : listModel) {
            if (gbp.isShownInLayout()) {
                boolean iconified = false;
                if ((showInInternalFrame) && (gbp.getInternalFrame() != null) && (gbp.getInternalFrame().isIcon())) {
                    iconified = true;
                }
                if (iconified == false) {
                    GridBagConstraints c = gbp.getConstraints();
                    if (c.gridx + c.gridwidth > maxX) {
                        maxX = c.gridx + c.gridwidth;
                    }
                    if (c.gridy + c.gridheight > maxY) {
                        maxY = c.gridy + c.gridheight;
                    }
                    if (c.gridx < minX) {
                        minX = c.gridx;
                    }
                    if (c.gridy < minY) {
                        minY = c.gridy;
                    }
                }
            }
        }
        if ((this.layoutPanel.getHeight() > 0) && (this.layoutPanel.getWidth() > 0)) {
            int gridHeight = this.layoutPanel.getHeight() / (maxY - minY);
            int gridWidth = this.layoutPanel.getWidth() / (maxX - minX);
            for (GridBagPanel gbp : listModel) {
                if (gbp.isShownInLayout()) {
                    GridBagPanelConstraints c = gbp.getConstraints();
                    if (showInInternalFrame) {
                        if (gbp.isAddedToLayout() == false) {
                            JInternalFrame frame = new JInternalFrame(gbp.getTitle(), false, // resizable
                                true, // closable
                                true, // maximizable
                                true);// iconifiable);
                            frame.addInternalFrameListener(this);
                            gbp.setInternalFrame(frame);
                            frame.setContentPane(gbp);
                            frame.setVisible(true);
                            this.layoutPanel.add(frame, c.layer, c.positionInLayer);
                            gbp.setAddedToLayout(true);
                        }
                        gbp.getInternalFrame().setBounds((c.gridx - minX) * gridWidth, (c.gridy - minY) * gridHeight,
                            c.gridwidth * gridWidth, c.gridheight * gridHeight);
                        gbp.revalidate();
                    } else {
                        gbp.setBounds((c.gridx - minX) * gridWidth, (c.gridy - minY) * gridHeight, c.gridwidth
                            * gridWidth, c.gridheight * gridHeight);
                        gbp.revalidate();
                        if (gbp.isAddedToLayout() == false) {
                            this.layoutPanel.add(gbp, c.layer, c.positionInLayer);
                            gbp.setAddedToLayout(true);
                        }

                    }
                }
            }
        }
    }

    private class GridPanelPropertyListener implements PropertyChangeListener {
        /**
         * Listens for changes in the GridBagConstraints for any of the managed
         * <code>GridBagPanels</code>.
         * 
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent evt) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(evt + " " + evt.getPropertyName());
            }
            if (evt.getPropertyName().equals("showInLayout")) {
                GridBagPanel p = (GridBagPanel) evt.getSource();
                if (p.isShownInLayout()) {
                    p.setVisible(true);
                    if (p.getInternalFrame() != null) {
                        p.getInternalFrame().setVisible(true);
                    }
                } else {
                    p.setVisible(false);
                    if (p.getInternalFrame() != null) {
                        p.getInternalFrame().setVisible(false);
                    }

                }
            }
            redoLayout();
            setSize(getWidth() + 1, getHeight() + 1);
            setSize(getWidth() - 1, getHeight() - 1);
        }
    }

    private class CommitAction implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            try {
                WorkspacePanel.this.transactionInterface.commitActiveTransaction();
            } catch (Exception e1) {
                logger.log(Level.WARNING, e1.getMessage(), e1);
            }

        }

    }

    private class AbortAction implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            try {
                int n = JOptionPane.showConfirmDialog(WorkspacePanel.this, "You will lose changes to "
                    + WorkspacePanel.this.transactionInterface.getUncommittedComponents().size() + " components.\n"
                    + "Do you wish to proceed?", "Warning", JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.YES_OPTION) {
                    WorkspacePanel.this.transactionInterface.abortActiveTransaction();
                }
            } catch (Exception e1) {
                logger.log(Level.WARNING, e1.getMessage(), e1);
            }

        }

    }

    private class ActiveTransactionListener implements PropertyChangeListener {
        /**
         * Listens for changes in the GridBagConstraints for any of the managed
         * <code>GridBagPanels</code>.
         * 
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent evt) {

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("WorkspacePanel propertyChange: " + evt.getPropertyName());
            }
            if (evt.getPropertyName().equals("activeTransaction")) {

                try {

                    WorkspacePanel.this.cancel.setEnabled(transactionInterface.isTransactionActive());
                    updateCommitButton(transactionInterface);
                } catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }

            }
        }
    }

    private class UncommittedComponentsListener implements PropertyChangeListener {
        /**
         * Listens for changes in the GridBagConstraints for any of the managed
         * <code>GridBagPanels</code>.
         * 
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        @SuppressWarnings("unchecked")
        public void propertyChange(PropertyChangeEvent evt) {

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("WorkspacePanel propertyChange: " + evt.getPropertyName());
            }
            if (evt.getPropertyName().equals("uncommittedComponents")) {
                Collection<Object> uncommitted = (Collection<Object>) evt.getNewValue();
                if (uncommitted.size() == 1) {
                    WorkspacePanel.this.statusMessage.setText("<HTML><font color='red'>1 uncommitted component...");
                } else if (uncommitted.size() > 1) {
                    WorkspacePanel.this.statusMessage.setText("<HTML><font color='red'>" + uncommitted.size()
                        + " uncommitted components...");
                } else {
                    WorkspacePanel.this.statusMessage.setText("<HTML><font color='blue'>no uncommitted components...");
                }

            }
        }
    }

    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {
            if (this.panelList.getSelectedIndex() >= 0) {
                GridBagPanel gbp = (GridBagPanel) this.panelList.getModel().getElementAt(
                    this.panelList.getSelectedIndex());
                this.changePanelProperties.setPanel(gbp);
                /*
                 * this.layoutPanel.setLayout(null);
                 * if (this.showInInternalFrame) {
                 * this.layoutPanel.moveToFront(gbp.getInternalFrame());
                 * } else {
                 * this.layoutPanel.moveToFront(gbp);
                 * }
                 */
            }
        } else {
            this.changePanelProperties.setPanel(null);
        }

    }

    /**
     * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
     */
    public void componentResized(ComponentEvent e) {
        this.redoLayout();

    }

    /**
     * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
     */
    public void componentMoved(ComponentEvent e) {

    }

    /**
     * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
     */
    public void componentShown(ComponentEvent e) {
        this.redoLayout();
    }

    /**
     * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
     */
    public void componentHidden(ComponentEvent e) {

    }

    /**
     * @see net.jini.core.event.RemoteEventListener#notify(net.jini.core.event.RemoteEvent)
     */
    public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    redoLayout();
                }
            });

        }
    }

    /**
     * @see javax.swing.event.InternalFrameListener#internalFrameOpened(javax.swing.event.InternalFrameEvent)
     */
    public void internalFrameOpened(InternalFrameEvent e) {
        // TODO Auto-generated method stub

    }

    /**
     * @see javax.swing.event.InternalFrameListener#internalFrameClosing(javax.swing.event.InternalFrameEvent)
     */
    public void internalFrameClosing(InternalFrameEvent e) {
        // TODO Auto-generated method stub

    }

    /**
     * @see javax.swing.event.InternalFrameListener#internalFrameClosed(javax.swing.event.InternalFrameEvent)
     */
    public void internalFrameClosed(InternalFrameEvent e) {
        JInternalFrame frame = e.getInternalFrame();
        GridBagPanel gbp = (GridBagPanel) frame.getContentPane();
        gbp.setShowInLayout(false);
    }

    /**
     * @see javax.swing.event.InternalFrameListener#internalFrameIconified(javax.swing.event.InternalFrameEvent)
     */
    public void internalFrameIconified(InternalFrameEvent e) {
        redoLayout();
    }

    /**
     * @see javax.swing.event.InternalFrameListener#internalFrameDeiconified(javax.swing.event.InternalFrameEvent)
     */
    public void internalFrameDeiconified(InternalFrameEvent e) {
        redoLayout();
    }

    /**
     * @see javax.swing.event.InternalFrameListener#internalFrameActivated(javax.swing.event.InternalFrameEvent)
     */
    public void internalFrameActivated(InternalFrameEvent e) {
        // TODO Auto-generated method stub

    }

    /**
     * @see javax.swing.event.InternalFrameListener#internalFrameDeactivated(javax.swing.event.InternalFrameEvent)
     */
    public void internalFrameDeactivated(InternalFrameEvent e) {
        // TODO Auto-generated method stub

    }

    /**
     * @return Returns the panelList.
     */
    @SuppressWarnings("unchecked")
    public List<GridBagPanel> getPanelList() {
        ArrayListModel<GridBagPanel> listModel = (ArrayListModel<GridBagPanel>) this.panelList.getModel();
        return listModel;
    }

    /**
     * @see org.dwfa.bpa.process.I_Workspace#setWorkspaceVisible(boolean)
     */
    public void setWorkspaceVisible(boolean visible) {
        this.getTopLevelAncestor().setVisible(visible);

    }

    /**
     * @see org.dwfa.bpa.process.I_Workspace#setWorkspaceBounds(int, int, int,
     *      int)
     */
    public void setWorkspaceBounds(int x, int y, int width, int height) {
        this.getTopLevelAncestor().setBounds(x, y, width, height);
    }

    public void setWorkspaceBounds(Rectangle bounds) {
        this.getTopLevelAncestor().setBounds(bounds);
    }

    /**
     * @see org.dwfa.bpa.process.I_Workspace#getPanel(java.lang.String)
     */
    public GridBagPanel getPanel(String panelName) throws NoMatchingEntryException {
        for (GridBagPanel p : this.getPanelList()) {
            if (p.getName().equals(panelName)) {
                return p;
            }
        }
        throw new NoMatchingEntryException(panelName);
    }

    /**
     * @return Returns the id.
     */
    public UUID getId() {
        return id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * @see org.dwfa.bpa.process.I_Workspace#bringToFront()
     */
    public void bringToFront() {
        this.requestFocus();
        JFrame frame = (JFrame) this.getTopLevelAncestor();
        frame.requestFocus();

    }

    /**
     * @return Returns the statusMessage.
     */
    public String getStatusMessage() {
        return statusMessage.getText();
    }

    /**
     * @param statusMessage
     *            The statusMessage to set.
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage.setText(statusMessage);
    }

    /**
     * @return Returns the statusMessage.
     */
    public String getAltMessage() {
        return altMessage.getText();
    }

    /**
     * @param statusMessage
     *            The statusMessage to set.
     */
    public void setAltMessage(String altMessage) {
        this.altMessage.setText(altMessage);
    }

    /**
     * @return Returns the frame.
     */
    public Frame getFrame() {
        return frame;
    }

    public Object getAttachment(String key) {
        return this.attachments.get(key);
    }

    public void setAttachment(String key, Object attachment) {
        Object oldValue = this.attachments.get(key);
        this.attachments.put(key, attachment);
        propChangeSupport.firePropertyChange(key, oldValue, attachment);
    }

    public void setShownInInternalFrame(boolean b) {
        this.showInInternalFrame = b;
        restartLayout();
    }

    public void setOneTouchExpandable(boolean b) {
        // this.splitPane.setOneTouchExpandable(b);

    }

    public void addAttachmentListener(PropertyChangeListener l) {
        this.propChangeSupport.addPropertyChangeListener(l);

    }

    public void addAttachmentListener(String property, PropertyChangeListener l) {
        this.propChangeSupport.addPropertyChangeListener(property, l);

    }

    public void removeAttachmentListener(PropertyChangeListener l) {
        this.propChangeSupport.removePropertyChangeListener(l);

    }

    public void removeAttachmentListener(String property, PropertyChangeListener l) {
        this.propChangeSupport.removePropertyChangeListener(property, l);

    }

    public void acquireOwnership() {
        ownership.acquireUninterruptibly();
    }

    public void releaseOwnership() {
        ownership.release();
    }
}
