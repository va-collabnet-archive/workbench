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
package org.dwfa.ace.gui.concept;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import org.dwfa.ace.ACE;
import org.dwfa.ace.DropButton;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.edit.AddRelationship;
import org.dwfa.ace.table.DestRelTableModel;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.RelTableModel;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;
import org.dwfa.ace.table.RelTableModel.RelGroupFieldEditor;
import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;
import org.dwfa.ace.table.RelationshipTableRenderer;
import org.dwfa.ace.table.refset.RefsetUtil;
import org.dwfa.bpa.util.SortClickListener;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.ttk.preferences.gui.PanelLinkingPreferences.LINK_TYPE;

public abstract class RelPlugin extends AbstractPlugin implements TableModelListener, I_HostConceptPlugins {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    protected transient Set<REFSET_TYPES> visibleExtensions = new HashSet<REFSET_TYPES>();
    private transient JTableWithDragImage relTable;
    protected transient boolean idToggleState = false;
    protected transient IdPlugin idPlugin;
    private transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            visibleExtensions = new HashSet<REFSET_TYPES>();
            pcs = new PropertyChangeSupport(this);
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public RelPlugin(boolean selectedByDefault, int sequence) {
        super(selectedByDefault, sequence);
    }

    protected JPanel getRelPanel(I_HostConceptPlugins host, RelTableModel model, String labelText, boolean enableEdit,
            TOGGLES toggle) throws TerminologyException, IOException {
        model.addTableModelListener(this);
        if (ACE.editMode == false) {
            enableEdit = false;
        }
        JPanel relPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        JLabel srcRelLabel = new JLabel(labelText);
        srcRelLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 3, 0));
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        relPanel.add(srcRelLabel, c);

        SmallProgressPanel progress = new SmallProgressPanel();
        progress.setVisible(false);
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.gridx++;
        relPanel.add(progress, c);
        model.setProgress(progress);
        progress.setActive(false);
        progress.setProgressInfo("");

        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;

        c.gridwidth = 1;
        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridheight = 2;
        if (enableEdit) {
            DropButton rowAddAfter = new DropButton(new ImageIcon(
                ACE.class.getResource("/24x24/plain/row_add_after.png")), model);
            relPanel.add(rowAddAfter, c);
            rowAddAfter.setEnabled(enableEdit);
            rowAddAfter.addActionListener(new AddRelationship(host, host.getConfig()));
            rowAddAfter.setTransferHandler(new TerminologyTransferHandler(rowAddAfter));
            rowAddAfter.setToolTipText("add a new relationship (the concept selected in the taxonomy view will be the default destination)");
        } 
        c.gridheight = 1;
        c.gridx++;
        c.gridwidth = 1;
        
        relTable = new JTableWithDragImage(model);
        SortClickListener.setupSorter(relTable);
        relTable.getSelectionModel().addListSelectionListener(this);
        relTable.getTableHeader().setToolTipText(
            "Click to specify sorting");
        REL_FIELD[] columnEnums = model.getColumnEnums();
        for (int i = 0; i < relTable.getColumnCount(); i++) {
            TableColumn column = relTable.getColumnModel().getColumn(i);
            REL_FIELD columnDesc = columnEnums[i];
            column.setIdentifier(columnDesc);
            column.setPreferredWidth(columnDesc.getPref());
            column.setMaxWidth(columnDesc.getMax());
            column.setMinWidth(columnDesc.getMin());
        }
        
        setupEditors(host);
        if (ACE.editMode) {
            relTable.addMouseListener(model.makePopupListener(relTable, host.getConfig()));
        }
        // Set up tool tips for column headers.
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        relPanel.add(relTable.getTableHeader(), c);
        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.gridheight = 5;
        relTable.setDefaultRenderer(StringWithRelTuple.class, new RelationshipTableRenderer());
        relPanel.add(relTable, c);
        c.gridx = 0;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        if(enableEdit){
            c.gridy++;
        }else{
            c.anchor = GridBagConstraints.NORTH;
        }
        JButton addToList = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/notebook_add.png")));
        addToList.setToolTipText("add to list view");
        addToList.addActionListener(new AddToList(model));
        relPanel.add(addToList, c);
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 0.0;
        c.gridy = c.gridy + c.gridheight;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridwidth = 2;

        if (host.getToggleState(TOGGLES.ID) == true) {
            idPlugin = new IdPlugin(true, 1);
            idPlugin.setShowBorder(false);
            relPanel.add(idPlugin.getComponent(this), c);
            c.gridy++;
        }
        
        visibleExtensions.clear();
        RefsetUtil.addRefsetTables(host, this, toggle, c, visibleExtensions, relPanel);
        relPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 3),
            BorderFactory.createLineBorder(Color.GRAY)));
        return relPanel;
    }
    
    private class AddToList implements ActionListener {
        RelTableModel model;

        public AddToList(RelTableModel model) {
            this.model = model;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                JList conceptList = getHost().getConfig().getBatchConceptList();
                I_ModelTerminologyList conceptListModel = (I_ModelTerminologyList) conceptList.getModel();
                HashSet<Integer> conceptsAdded = new HashSet<Integer>();
                for (int i = 0; i < model.getRowCount(); i++) {
                    StringWithRelTuple row = (StringWithRelTuple) model.getValueAt(i, 1); //TODO: okay?
                    I_RelTuple tuple = row.getTuple();
                    ConceptChronicleBI concept = null;
                    if(model instanceof DestRelTableModel){
                        concept = Ts.get().getConcept(tuple.getC1Id());
                    }else{
                        concept = Ts.get().getConcept(tuple.getC2Id());
                    }
                    if (conceptsAdded.contains(concept.getConceptNid()) == false) {
                        conceptsAdded.add(concept.getConceptNid());
                        conceptListModel.addElement((I_GetConceptData) concept);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(RelPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected void setupEditors(I_HostConceptPlugins host) throws TerminologyException, IOException {
        relTable.setDragEnabled(false);
        relTable.setTransferHandler(new TerminologyTransferHandler(relTable));
        relTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        relTable.getColumn(REL_FIELD.REL_TYPE).setCellEditor(new RelTableModel.RelTypeFieldEditor(host.getConfig()));
        relTable.getColumn(REL_FIELD.CHARACTERISTIC).setCellEditor(
            new RelTableModel.RelCharactisticFieldEditor(host.getConfig()));
        relTable.getColumn(REL_FIELD.REFINABILITY).setCellEditor(
            new RelTableModel.RelRefinabilityFieldEditor(host.getConfig()));
        relTable.getColumn(REL_FIELD.STATUS).setCellEditor(new RelTableModel.RelStatusFieldEditor(host.getConfig()));
        if (SrcRelPlugin.class.isAssignableFrom(this.getClass())) {
            relTable.getColumn(REL_FIELD.GROUP).setCellEditor(new RelGroupFieldEditor());
        }
    }

    public JTableWithDragImage getRelTable() {
        return relTable;
    }

    @Override
    protected int getComponentId() {
        if (relTable.getSelectedRow() < 0) {
            return Integer.MIN_VALUE;
        }
        StringWithRelTuple swrt = (StringWithRelTuple) relTable.getValueAt(relTable.getSelectedRow(), 0);
        return swrt.getTuple().getRelId();
    }

    @Override
    public void tableChanged(TableModelEvent tme) {
        if (relTable.getSelectedRow() == -1) {
            if (relTable.getRowCount() > 0) {
                int rowToSelect = 0; // relTable.getRowCount() -1;
                relTable.setRowSelectionInterval(rowToSelect, rowToSelect);
            }
        }

    }

    @Override
    public I_GetConceptData getHierarchySelection() {
        return getHost().getHierarchySelection();
    }

    @Override
    public boolean getShowHistory() {
        return getHost().getShowHistory();
    }

    @Override
    public boolean getShowRefsets() {
        return getHost().getShowRefsets();
    }

    @Override
    public boolean getToggleState(TOGGLES toggle) {
        return getHost().getToggleState(toggle);
    }

    @Override
    public boolean getUsePrefs() {
        return getHost().getUsePrefs();
    }

    @Override
    public void setAllTogglesToState(boolean state) {
        getHost().setAllTogglesToState(state);
    }

    @Override
    public void setLinkType(LINK_TYPE link) {
        getHost().setLinkType(link);
    }

    @Override
    public void setToggleState(TOGGLES toggle, boolean state) {
        getHost().setToggleState(toggle, state);
    }

    @Override
    public void unlink() {
        getHost().unlink();
    }

    @Override
    public I_ConfigAceFrame getConfig() {
        return getHost().getConfig();
    }

    @Override
    public I_AmTermComponent getTermComponent() {
        return getSelectedPluginComponent();
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void setTermComponent(I_AmTermComponent arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        super.valueChanged(evt);
        pcs.firePropertyChange(I_ContainTermComponent.TERM_COMPONENT, null, getSelectedPluginComponent());
    }

    protected I_AmTermComponent getSelectedPluginComponent() {
        if (relTable.getSelectedRow() < 0) {
            return null;
        }
        StringWithRelTuple swrt = (StringWithRelTuple) relTable.getValueAt(relTable.getSelectedRow(), 0);
        if (swrt != null && swrt.getTuple() != null) {
            return swrt.getTuple().getRelVersioned();
        }
        return null;
    }

}
