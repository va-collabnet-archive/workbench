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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.table.I_CellTextWithTuple;
import org.dwfa.ace.table.IdTableModel;
import org.dwfa.ace.table.IdTableRenderer;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.ConceptAttributeTableModel.StringWithConceptTuple;
import org.dwfa.ace.table.IdTableModel.ID_FIELD;
import org.dwfa.ace.table.IdTableModel.StringWithIdTuple;
import org.dwfa.bpa.util.SortClickListener;
import org.dwfa.tapi.TerminologyException;

public class IdPlugin extends AbstractPlugin {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private transient IdTableModel idTableModel;
    private transient JPanel idPanel;
    private transient JTableWithDragImage idTable;
    private transient boolean showBorder = true;

    // @TODO find a way to handle this field dynamically, like pressing the
    // shift key...
    private static boolean showNids = false;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeBoolean(showBorder);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            showBorder = in.readBoolean();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public IdPlugin(boolean shownByDefault, int sequence) {
        super(shownByDefault, sequence);
    }

    public UUID getId() {
        return I_HostConceptPlugins.TOGGLES.ID.getPluginId();
    }

    @Override
    protected ImageIcon getImageIcon() {
        return new ImageIcon(ACE.class.getResource("/24x24/plain/id_card.png"));
    }

    @Override
    public void update() throws IOException {
        if (getHost() != null) {
            PropertyChangeEvent evt = new PropertyChangeEvent(getHost(), "termComponent", null,
                getHost().getTermComponent());
            ID_FIELD[] columnEnums = getIdColumns(getHost(), showNids);
            idTableModel.setColumns(getIdColumns(getHost(), showNids));
            for (int i = 0; i < idTableModel.getColumnCount(); i++) {
                TableColumn column = idTable.getColumnModel().getColumn(i);
                ID_FIELD columnDesc = columnEnums[i];
                column.setIdentifier(columnDesc);
                column.setPreferredWidth(columnDesc.getPref());
                column.setMaxWidth(columnDesc.getMax());
                column.setMinWidth(columnDesc.getMin());
            }
            idTableModel.propertyChange(evt);
        }
    }

    public JComponent getComponent(I_HostConceptPlugins host) throws TerminologyException, IOException {
        if (idPanel == null) {
            setHost(host);
            idPanel = getIdPanel(host, showNids);
            host.addPropertyChangeListener(I_HostConceptPlugins.SHOW_HISTORY, this);
            host.addPropertyChangeListener("commit", this);
            PropertyChangeEvent evt = new PropertyChangeEvent(host, "termComponent", null, host.getTermComponent());
            idTableModel.propertyChange(evt);
        }
        return idPanel;
    }

    private ID_FIELD[] getIdColumns(I_HostConceptPlugins host, boolean showNatives) {
        setHost(host);
        List<ID_FIELD> fields = new ArrayList<ID_FIELD>();
        if (showNatives) {
            fields.add(ID_FIELD.LOCAL_ID);
        }
        fields.add(ID_FIELD.SOURCE);
        fields.add(ID_FIELD.EXT_ID);
        fields.add(ID_FIELD.STATUS);
        if (host.getShowHistory()) {
            fields.add(ID_FIELD.VERSION);
            fields.add(ID_FIELD.PATH);
        }
        return fields.toArray(new ID_FIELD[fields.size()]);
    }

    private JPanel getIdPanel(I_HostConceptPlugins host, boolean showNatives) throws TerminologyException, IOException {
        setHost(host);
        idTableModel = new IdTableModel(getIdColumns(host, showNatives), host);
        JPanel idPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 2;
        JLabel idLabel = new JLabel("Id:");
        idLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
        idPanel.add(idLabel, c);

        SmallProgressPanel idProgress = new SmallProgressPanel();
        idProgress.setVisible(false);
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.gridx++;
        idPanel.add(idProgress, c);
        idTableModel.setProgress(idProgress);

        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridheight = 2;
        JPanel filler = new JPanel();
        filler.setMaximumSize(new Dimension(40, 32));
        filler.setMinimumSize(new Dimension(40, 32));
        filler.setPreferredSize(new Dimension(40, 32));
        idPanel.add(filler, c);
        c.gridheight = 1;
        c.gridx++;
        idTable = new JTableWithDragImage(idTableModel);
        SortClickListener.setupSorter(idTable);
        idTable.getSelectionModel().addListSelectionListener(this);
        /*
         * idTable.addMouseListener(idTableModel.makePopupListener(idTable,
         * host.getConfig()));
         */
        idTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ID_FIELD[] columnEnums = idTableModel.getColumnEnums();
        for (int i = 0; i < idTable.getColumnCount(); i++) {
            TableColumn column = idTable.getColumnModel().getColumn(i);
            ID_FIELD columnDesc = columnEnums[i];
            column.setIdentifier(columnDesc);
            column.setPreferredWidth(columnDesc.getPref());
            column.setMaxWidth(columnDesc.getMax());
            column.setMinWidth(columnDesc.getMin());
        }

        // Set up tool tips for column headers.
        idTable.getTableHeader().setToolTipText(
            "Click to specify sorting");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        idPanel.add(idTable.getTableHeader(), c);
        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridheight = 6;

        IdTableRenderer renderer = new IdTableRenderer();

        idTable.setDefaultRenderer(StringWithIdTuple.class, renderer);
        idTable.setDefaultRenderer(Number.class, renderer);
        idTable.setDefaultRenderer(String.class, renderer);
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
                } else if (StringWithConceptTuple.class.isAssignableFrom(anObject.getClass())) {
                    I_CellTextWithTuple swt = (I_CellTextWithTuple) anObject;
                    value = Boolean.parseBoolean(swt.getCellText());
                }
                super.setSelectedItem(value);
            }
        };
        comboBox.addItem(new Boolean(true));
        comboBox.addItem(new Boolean(false));
        idTable.setDefaultEditor(Boolean.class, new DefaultCellEditor(comboBox));

        idTable.getColumn(ID_FIELD.STATUS).setCellEditor(new IdTableModel.IdStatusFieldEditor(host.getConfig()));

        idTable.setDefaultRenderer(String.class, renderer);
        idPanel.add(idTable, c);
        if (showBorder) {
            idPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 3),
                BorderFactory.createLineBorder(Color.GRAY)));
        }
        c.gridheight = 1;
        c.gridx = 0;
        return idPanel;
    }

    @Override
    protected String getToolTipText() {
        return "show/hide identifiers for this concept";
    }

    @Override
    protected int getComponentId() {
        return Integer.MIN_VALUE;
    }

    public boolean getShowBorder() {
        return showBorder;
    }

    public void setShowBorder(boolean showBorder) {
        this.showBorder = showBorder;
    }

}
