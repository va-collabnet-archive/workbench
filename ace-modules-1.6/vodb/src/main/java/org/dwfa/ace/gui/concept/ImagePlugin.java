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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.edit.AddImage;
import org.dwfa.ace.table.ImageTableModel;
import org.dwfa.ace.table.ImageTableRenderer;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.ImageTableModel.IMAGE_FIELD;
import org.dwfa.ace.table.ImageTableModel.ImageWithImageTuple;
import org.dwfa.ace.table.ImageTableModel.StringWithImageTuple;
import org.dwfa.ace.table.refset.RefsetUtil;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;

public class ImagePlugin extends AbstractPlugin {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private transient JPanel imagePanel;
    private transient ImageTableModel imageTableModel;
    private transient JTableWithDragImage imageTable;
    protected transient Set<EXT_TYPE> visibleExtensions = new HashSet<EXT_TYPE>();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            visibleExtensions = new HashSet<EXT_TYPE>();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public ImagePlugin(boolean shownByDefault, int sequence) {
        super(shownByDefault, sequence);
    }

    public UUID getId() {
        return I_HostConceptPlugins.TOGGLES.IMAGE.getPluginId();
    }

    @Override
    protected ImageIcon getImageIcon() {
        return new ImageIcon(ACE.class.getResource("/24x24/plain/photo_scenery.png"));
    }

    @Override
    public void update() throws IOException {
        if (getHost() != null) {

            if (RefsetUtil.refSetsChanged(getHost(), TOGGLES.IMAGE, this, visibleExtensions)) {
                createPluginComponent(getHost());
            }

            PropertyChangeEvent evt = new PropertyChangeEvent(getHost(), "termComponent", null,
                getHost().getTermComponent());
            IMAGE_FIELD[] columnEnums = getImageColumns(getHost());
            imageTableModel.setColumns(getImageColumns(getHost()));
            for (int i = 0; i < imageTableModel.getColumnCount(); i++) {
                TableColumn column = imageTable.getColumnModel().getColumn(i);
                IMAGE_FIELD columnDesc = columnEnums[i];
                column.setIdentifier(columnDesc);
                column.setPreferredWidth(columnDesc.getPref());
                column.setMaxWidth(columnDesc.getMax());
                column.setMinWidth(columnDesc.getMin());
            }
            setupEditorsAndRenderers(getHost());
            imageTableModel.propertyChange(evt);
        }
    }

    public JComponent getComponent(I_HostConceptPlugins host) {
        setHost(host);
        if (imagePanel == null || RefsetUtil.refSetsChanged(host, TOGGLES.IMAGE, this, visibleExtensions)) {
            createPluginComponent(host);
        }
        return imagePanel;
    }

    private void createPluginComponent(I_HostConceptPlugins host) {
        setHost(host);
        imageTableModel = new ImageTableModel(host, getImageColumns(host), host.getShowHistory());
        imagePanel = getImagePanel(host);
        host.addPropertyChangeListener(I_HostConceptPlugins.SHOW_HISTORY, this);
        host.addPropertyChangeListener("commit", this);
        PropertyChangeEvent evt = new PropertyChangeEvent(host, "termComponent", null, host.getTermComponent());
        imageTableModel.propertyChange(evt);
    }

    private IMAGE_FIELD[] getImageColumns(I_HostConceptPlugins host) {
        setHost(host);
        List<IMAGE_FIELD> fields = new ArrayList<IMAGE_FIELD>();
        fields.add(IMAGE_FIELD.IMAGE);
        fields.add(IMAGE_FIELD.TYPE);
        fields.add(IMAGE_FIELD.DESC);
        fields.add(IMAGE_FIELD.STATUS);
        if (host.getShowHistory()) {
            fields.add(IMAGE_FIELD.VERSION);
            fields.add(IMAGE_FIELD.PATH);
        }
        return fields.toArray(new IMAGE_FIELD[fields.size()]);
    }

    private JPanel getImagePanel(I_HostConceptPlugins host) {
        setHost(host);
        JPanel imagePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 2;
        JLabel imageLabel = new JLabel("Images:");
        imageLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
        imagePanel.add(imageLabel, c);

        SmallProgressPanel imageProgress = new SmallProgressPanel();
        imageProgress.setVisible(false);
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.gridx++;
        imagePanel.add(imageProgress, c);
        imageTableModel.setProgress(imageProgress);

        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridheight = 2;
        JButton rowAddAfter = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/row_add_after.png")));
        imagePanel.add(rowAddAfter, c);
        rowAddAfter.addActionListener(new AddImage(host, host.getConfig()));
        c.gridheight = 1;
        c.gridx++;
        TableSorter sortingTable = new TableSorter(imageTableModel);
        imageTable = new JTableWithDragImage(sortingTable);
        imageTable.getSelectionModel().addListSelectionListener(this);
        imageTable.addMouseListener(imageTableModel.makePopupListener(imageTable, host.getConfig()));
        imageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sortingTable.setTableHeader(imageTable.getTableHeader());

        IMAGE_FIELD[] columnEnums = imageTableModel.getColumnEnums();
        for (int i = 0; i < imageTable.getColumnCount(); i++) {
            TableColumn column = imageTable.getColumnModel().getColumn(i);
            IMAGE_FIELD columnDesc = columnEnums[i];
            column.setPreferredWidth(columnDesc.getPref());
            column.setMaxWidth(columnDesc.getMax());
            column.setMinWidth(columnDesc.getMin());
            column.setIdentifier(columnDesc);
        }

        // Set up tool tips for column headers.
        sortingTable.getTableHeader().setToolTipText(
            "Click to specify sorting; Control-Click to specify secondary sorting");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        imagePanel.add(imageTable.getTableHeader(), c);
        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridheight = 6;

        setupEditorsAndRenderers(host);
        imagePanel.add(imageTable, c);

        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridy = c.gridy + c.gridheight;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridwidth = 2;
        visibleExtensions.clear();
        RefsetUtil.addRefsetTables(host, this, TOGGLES.IMAGE, c, visibleExtensions, imagePanel);

        imagePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 3),
            BorderFactory.createLineBorder(Color.GRAY)));

        return imagePanel;
    }

    @Override
    protected String getToolTipText() {
        return "show/hide images associated with this concept";
    }

    @Override
    protected int getComponentId() {
        if (imageTable.getSelectedRow() < 0) {
            return Integer.MIN_VALUE;
        }
        StringWithImageTuple swit = (StringWithImageTuple) imageTable.getValueAt(imageTable.getSelectedRow(), 0);
        return swit.getTuple().getImageId();
    }

    private void setupEditorsAndRenderers(I_HostConceptPlugins host) {
        ImageTableRenderer renderer = new ImageTableRenderer();
        if (ACE.editMode) {
            imageTable.setDefaultEditor(StringWithImageTuple.class, new ImageTableModel.TextFieldEditor());
            imageTable.getColumn(IMAGE_FIELD.TYPE).setCellEditor(new ImageTableModel.TypeFieldEditor(host.getConfig()));
            imageTable.getColumn(IMAGE_FIELD.STATUS).setCellEditor(
                new ImageTableModel.StatusFieldEditor(host.getConfig()));
        }

        imageTable.setDefaultRenderer(ImageWithImageTuple.class, renderer);
        imageTable.setDefaultRenderer(StringWithImageTuple.class, renderer);
        imageTable.setDefaultRenderer(Number.class, renderer);
        imageTable.setDefaultRenderer(String.class, renderer);
    }

}
