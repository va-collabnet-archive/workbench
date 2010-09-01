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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.edit.AddDescription;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.DescriptionTableModel;
import org.dwfa.ace.table.DescriptionTableRenderer;
import org.dwfa.ace.table.DescriptionsForConceptTableModel;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.DescriptionTableModel.DESC_FIELD;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;
import org.dwfa.ace.table.refset.RefsetUtil;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;

public class DescriptionPlugin extends AbstractPlugin implements TableModelListener, I_HostConceptPlugins {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private transient JPanel descPanel;
    private transient DescriptionsForConceptTableModel descTableModel;
    private transient JTableWithDragImage descTable;
    private transient boolean idToggleState = false;
    protected transient Set<EXT_TYPE> visibleExtensions = new HashSet<EXT_TYPE>();
    private transient IdPlugin idPlugin;
    private transient I_GetConceptData lastSelectedConcept;
    private transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            visibleExtensions = new HashSet<EXT_TYPE>();
            pcs = new PropertyChangeSupport(this);
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public DescriptionPlugin(boolean shownByDefault, int sequence) {
        super(shownByDefault, sequence);
    }

    public UUID getId() {
        return I_HostConceptPlugins.TOGGLES.DESCRIPTIONS.getPluginId();
    }

    @Override
    protected ImageIcon getImageIcon() {
        return new ImageIcon(ACE.class.getResource("/24x24/plain/paragraph.png"));
    }

    @Override
    public void update() throws IOException {
        if (getHost() != null) {
            int lastSelectedRowCount = descTable.getRowCount();
            int lastSelectedRow = descTable.getSelectedRow();
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine("desc rowcount: " + lastSelectedRowCount + " selected row: " + lastSelectedRow);
            }
            if (idPlugin != null) {
                idPlugin.update();
            }

            if (RefsetUtil.refSetsChanged(getHost(), TOGGLES.DESCRIPTIONS, this, visibleExtensions)
                || getHost().getToggleState(TOGGLES.ID) != idToggleState) {
                idToggleState = getHost().getToggleState(TOGGLES.ID);
                createPluginComponent(getHost());
            }

            PropertyChangeEvent evt = new PropertyChangeEvent(getHost(), "termComponent", null,
                getHost().getTermComponent());
            DESC_FIELD[] columnEnums = getDescColumns(getHost());
            descTableModel.setColumns(columnEnums);
            for (int i = 0; i < descTableModel.getColumnCount(); i++) {
                TableColumn column = descTable.getColumnModel().getColumn(i);
                DESC_FIELD columnDesc = columnEnums[i];
                column.setIdentifier(columnDesc);
                column.setPreferredWidth(columnDesc.getPref());
                column.setMaxWidth(columnDesc.getMax());
                column.setMinWidth(columnDesc.getMin());
            }
            setupEditorsAndRenderers(getHost());
            descTableModel.propertyChange(evt);

            if (lastSelectedConcept == getHost().getTermComponent()) {
                SwingUtilities.invokeLater(new ReselectRow(lastSelectedRowCount, lastSelectedRow));
            }
            lastSelectedConcept = (I_GetConceptData) getHost().getTermComponent();
        }
    }

    private class ReselectRow implements Runnable {

        int lastSelectedRowCount;
        int lastSelectedRow;

        public ReselectRow(int lastSelectedRowCount, int lastSelectedRow) {
            super();
            this.lastSelectedRowCount = lastSelectedRowCount;
            this.lastSelectedRow = lastSelectedRow;
        }

        public void run() {
            if (lastSelectedRowCount == descTable.getRowCount()) {
                descTable.getSelectionModel().setSelectionInterval(lastSelectedRow, lastSelectedRow);
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine("reselecting: " + lastSelectedRow);
                }
            } else {
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().info("rowcount changed to: " + descTable.getRowCount());
                }
            }
        }

    }

    public JComponent getComponent(I_HostConceptPlugins host) {
        setHost(host);
        if (descPanel == null || RefsetUtil.refSetsChanged(host, TOGGLES.DESCRIPTIONS, this, visibleExtensions)) {
            createPluginComponent(host);
        }
        return descPanel;
    }

    private void createPluginComponent(I_HostConceptPlugins host) {
        setHost(host);
        descPanel = getDescPanel(host);
        host.addPropertyChangeListener(I_HostConceptPlugins.SHOW_HISTORY, this);
        host.addPropertyChangeListener("commit", this);
        PropertyChangeEvent evt = new PropertyChangeEvent(host, "termComponent", null, host.getTermComponent());
        descTableModel.propertyChange(evt);
    }

    private DESC_FIELD[] getDescColumns(I_HostConceptPlugins host) {
        List<DESC_FIELD> fields = new ArrayList<DESC_FIELD>();
        fields.add(DESC_FIELD.TEXT);
        fields.add(DESC_FIELD.TYPE);
        fields.add(DESC_FIELD.CASE_FIXED);
        fields.add(DESC_FIELD.LANG);
        fields.add(DESC_FIELD.STATUS);
        if (host.getShowHistory()) {
            fields.add(DESC_FIELD.VERSION);
            fields.add(DESC_FIELD.PATH);
        }
        return fields.toArray(new DESC_FIELD[fields.size()]);
    }

    private JPanel getDescPanel(I_HostConceptPlugins host) {
        descTableModel = new DescriptionsForConceptTableModel(getDescColumns(host), host);
        descTableModel.addTableModelListener(this);
        JPanel descPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 2;
        JLabel descLabel = new JLabel("Descriptions:");
        descLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
        descPanel.add(descLabel, c);

        SmallProgressPanel descProgress = new SmallProgressPanel();
        descProgress.setVisible(false);
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.gridx++;
        descPanel.add(descProgress, c);
        descTableModel.setProgress(descProgress);

        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridheight = 2;

        if (ACE.editMode) {
            JButton rowAddAfter = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/row_add_after.png")));
            descPanel.add(rowAddAfter, c);
            rowAddAfter.addActionListener(new AddDescription(host, host.getConfig()));
            rowAddAfter.setToolTipText("add new description");
        } else {
            JPanel filler = new JPanel();
            filler.setMaximumSize(new Dimension(40, 32));
            filler.setMinimumSize(new Dimension(40, 32));
            filler.setPreferredSize(new Dimension(40, 32));
            descPanel.add(filler, c);
        }

        c.gridheight = 1;
        c.gridx++;
        TableSorter sortingTable = new TableSorter(descTableModel);
        descTable = new JTableWithDragImage(sortingTable);
        descTable.getSelectionModel().addListSelectionListener(this);
        descTable.setDragEnabled(true);
        descTable.setTransferHandler(new TerminologyTransferHandler(descTable));

        if (ACE.editMode) {
            descTable.addMouseListener(descTableModel.makePopupListener(descTable, host.getConfig()));
        }

        descTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sortingTable.setTableHeader(descTable.getTableHeader());

        DESC_FIELD[] columnEnums = descTableModel.getColumnEnums();

        for (int i = 0; i < descTable.getColumnCount(); i++) {
            TableColumn column = descTable.getColumnModel().getColumn(i);
            DESC_FIELD columnDesc = columnEnums[i];
            column.setIdentifier(columnDesc);
            column.setPreferredWidth(columnDesc.getPref());
            column.setMaxWidth(columnDesc.getMax());
            column.setMinWidth(columnDesc.getMin());
        }

        // Set up tool tips for column headers.
        sortingTable.getTableHeader().setToolTipText(
            "Click to specify sorting; Control-Click to specify secondary sorting");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        descPanel.add(descTable.getTableHeader(), c);
        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridheight = 6;

        setupEditorsAndRenderers(host);
        descPanel.add(descTable, c);

        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridy = c.gridy + c.gridheight;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridwidth = 2;

        if (host.getToggleState(TOGGLES.ID) == true) {
            idPlugin = new IdPlugin(true, 1);
            idPlugin.setShowBorder(false);
            descPanel.add(idPlugin.getComponent(this), c);
            c.gridy++;
        }

        visibleExtensions.clear();
        RefsetUtil.addRefsetTables(host, this, TOGGLES.DESCRIPTIONS, c, visibleExtensions, descPanel);

        descPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 3),
            BorderFactory.createLineBorder(Color.GRAY)));

        return descPanel;
    }

    private void setupEditorsAndRenderers(I_HostConceptPlugins host) {
        DescriptionTableRenderer renderer = new DescriptionTableRenderer(host.getConfig());
        descTable.setDefaultRenderer(Boolean.class, renderer);
        JComboBox comboBox = new JComboBox() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 1L;

            @Override
            public void setSelectedItem(Object anObject) {
                Boolean value = null;
                if (Boolean.class.isAssignableFrom(anObject.getClass())) {
                    value = (Boolean) anObject;
                } else if (StringWithDescTuple.class.isAssignableFrom(anObject.getClass())) {
                    StringWithDescTuple swt = (StringWithDescTuple) anObject;
                    value = Boolean.parseBoolean(swt.getCellText());
                }
                super.setSelectedItem(value);
            }
        };
        comboBox.addItem(new Boolean(true));
        comboBox.addItem(new Boolean(false));
        if (ACE.editMode) {
            descTable.setDefaultEditor(Boolean.class, new DefaultCellEditor(comboBox));

            descTable.setDefaultEditor(StringWithDescTuple.class, new DescriptionTableModel.DescTextFieldEditor());
            descTable.getColumn(DESC_FIELD.TYPE).setCellEditor(
                new DescriptionTableModel.DescTypeFieldEditor(host.getConfig()));
            descTable.getColumn(DESC_FIELD.STATUS).setCellEditor(
                new DescriptionTableModel.DescStatusFieldEditor(host.getConfig()));
        }

        descTable.setDefaultRenderer(StringWithDescTuple.class, renderer);
        descTable.setDefaultRenderer(Number.class, renderer);
        descTable.setDefaultRenderer(String.class, renderer);
    }

    @Override
    protected String getToolTipText() {
        return "show/hide descriptions for this concept";
    }

    @Override
    protected int getComponentId() {
        if (descTable.getSelectedRow() < 0) {
            return Integer.MIN_VALUE;
        }
        StringWithDescTuple swdt = (StringWithDescTuple) descTable.getValueAt(descTable.getSelectedRow(), 0);
        return swdt.getTuple().getDescId();
    }

    protected I_AmTermComponent getSelectedPluginComponent() {
        if (descTable.getSelectedRow() >= 0) {
            StringWithDescTuple swdt = (StringWithDescTuple) descTable.getValueAt(descTable.getSelectedRow(), 0);
            if (swdt != null && swdt.getTuple() != null) {
                return swdt.getTuple().getDescVersioned();
            }
        }
        return null;
    }

    public void tableChanged(TableModelEvent tme) {
        if (descTable.getSelectedRow() == -1) {
            if (descTable.getRowCount() > 0) {
                int rowToSelect = 0; // descTable.getRowCount() -1;
                descTable.setRowSelectionInterval(rowToSelect, rowToSelect);
            }
        }

    }

    public I_GetConceptData getHierarchySelection() {
        return getHost().getHierarchySelection();
    }

    public boolean getShowHistory() {
        return getHost().getShowHistory();
    }

    public boolean getShowRefsets() {
        return getHost().getShowRefsets();
    }

    public boolean getToggleState(TOGGLES toggle) {
        return getHost().getToggleState(toggle);
    }

    public boolean getUsePrefs() {
        return getHost().getUsePrefs();
    }

    public void setAllTogglesToState(boolean state) {
        getHost().setAllTogglesToState(state);
    }

    public void setLinkType(LINK_TYPE link) {
        getHost().setLinkType(link);
    }

    public void setToggleState(TOGGLES toggle, boolean state) {
        getHost().setToggleState(toggle, state);
    }

    public void unlink() {
        getHost().unlink();
    }

    public I_ConfigAceFrame getConfig() {
        return getHost().getConfig();
    }

    public I_AmTermComponent getTermComponent() {
        return getSelectedPluginComponent();
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void setTermComponent(I_AmTermComponent arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        super.valueChanged(evt);
        pcs.firePropertyChange(I_ContainTermComponent.TERM_COMPONENT, null, getSelectedPluginComponent());
    }

}
