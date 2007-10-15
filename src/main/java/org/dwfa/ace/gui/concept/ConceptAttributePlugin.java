package org.dwfa.ace.gui.concept;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.table.ConceptAttributeTableModel;
import org.dwfa.ace.table.ConceptAttributeTableRenderer;
import org.dwfa.ace.table.I_CellTextWithTuple;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.ConceptAttributeTableModel.CONCEPT_FIELD;
import org.dwfa.ace.table.ConceptAttributeTableModel.StringWithConceptTuple;
import org.dwfa.ace.table.refset.RefsetUtil;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;

public class ConceptAttributePlugin extends AbstractPlugin implements TableModelListener {

    private I_HostConceptPlugins host;

    private JPanel conceptAttributes;

    private ConceptAttributeTableModel conceptTableModel;

    private JTableWithDragImage conceptTable;

    protected Set<EXT_TYPE> visibleExtensions = new HashSet<EXT_TYPE>();

    public ConceptAttributePlugin() {
        super(true);
    }

    @Override
    protected ImageIcon getImageIcon() {
        return new ImageIcon(ACE.class.getResource("/24x24/plain/bullet_triangle_blue.png"));
    }

    @Override
    public void update() throws IOException {
        if (host != null) {

            if (RefsetUtil.refSetsChanged(host, TOGGLES.ATTRIBUTES, this, visibleExtensions)) {
                createPluginComponent(host);
            }

            PropertyChangeEvent evt = new PropertyChangeEvent(host, "termComponent", null, host.getTermComponent());
            CONCEPT_FIELD[] columnEnums = getConceptColumns(host);
            conceptTableModel.setColumns(getConceptColumns(host));
            for (int i = 0; i < conceptTableModel.getColumnCount(); i++) {
                TableColumn column = conceptTable.getColumnModel().getColumn(i);
                CONCEPT_FIELD columnDesc = columnEnums[i];
                column.setIdentifier(columnDesc);
                column.setPreferredWidth(columnDesc.getPref());
                column.setMaxWidth(columnDesc.getMax());
                column.setMinWidth(columnDesc.getMin());
            }
            conceptTableModel.propertyChange(evt);
        }
    }

    public JComponent getComponent(I_HostConceptPlugins host) {
        if (conceptAttributes == null || RefsetUtil.refSetsChanged(host, TOGGLES.ATTRIBUTES, this, visibleExtensions)) {
            createPluginComponent(host);
        }
        return conceptAttributes;
    }

    private void createPluginComponent(I_HostConceptPlugins host) {
        conceptAttributes = getConceptAttributesPanel(host);
        host.addPropertyChangeListener(I_HostConceptPlugins.SHOW_HISTORY, this);
        host.addPropertyChangeListener("commit", this);
        this.host = host;
        PropertyChangeEvent evt = new PropertyChangeEvent(host, "termComponent", null, host.getTermComponent());
        conceptTableModel.propertyChange(evt);
    }

    private CONCEPT_FIELD[] getConceptColumns(I_HostConceptPlugins host) {
        List<CONCEPT_FIELD> fields = new ArrayList<CONCEPT_FIELD>();
        fields.add(CONCEPT_FIELD.DEFINED);
        fields.add(CONCEPT_FIELD.STATUS);
        if (host.getShowHistory()) {
            fields.add(CONCEPT_FIELD.VERSION);
            fields.add(CONCEPT_FIELD.PATH);
        }
        return fields.toArray(new CONCEPT_FIELD[fields.size()]);
    }

    private JPanel getConceptAttributesPanel(I_HostConceptPlugins host) {
        if (conceptTableModel != null) {
            conceptTableModel.removeTableModelListener(this);
        }
        conceptTableModel = new ConceptAttributeTableModel(getConceptColumns(host), host);
        conceptTableModel.addTableModelListener(this);
        JPanel conceptPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 2;
        JLabel conceptLabel = new JLabel("Concept attributes:");
        conceptLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
        conceptPanel.add(conceptLabel, c);

        SmallProgressPanel concProgress = new SmallProgressPanel();
        concProgress.setVisible(false);
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.gridx++;
        conceptPanel.add(concProgress, c);
        conceptTableModel.setProgress(concProgress);

        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridheight = 2;
        // JButton rowAddAfter = new JButton(new ImageIcon(ACE.class
        // .getResource("/24x24/plain/row_add_after.png")));
        // conceptPanel.add(rowAddAfter, c);
        // rowAddAfter.addActionListener(new AddConceptPart(host,
        // host.getConfig()));
        JPanel filler = new JPanel();
        filler.setMaximumSize(new Dimension(40, 32));
        filler.setMinimumSize(new Dimension(40, 32));
        filler.setPreferredSize(new Dimension(40, 32));
        conceptPanel.add(filler, c);

        c.gridheight = 1;
        c.gridx++;
        TableSorter sortingTable = new TableSorter(conceptTableModel);
        conceptTable = new JTableWithDragImage(sortingTable);
        conceptTable.getSelectionModel().addListSelectionListener(this);
        if (ACE.editMode) {
            conceptTable.addMouseListener(conceptTableModel.makePopupListener(conceptTable, host.getConfig()));
        }

        conceptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sortingTable.setTableHeader(conceptTable.getTableHeader());

        CONCEPT_FIELD[] columnEnums = conceptTableModel.getColumnEnums();
        for (int i = 0; i < conceptTable.getColumnCount(); i++) {
            TableColumn column = conceptTable.getColumnModel().getColumn(i);
            CONCEPT_FIELD columnDesc = columnEnums[i];
            column.setIdentifier(columnDesc);
            column.setPreferredWidth(columnDesc.getPref());
            column.setMaxWidth(columnDesc.getMax());
            column.setMinWidth(columnDesc.getMin());
        }

        // Set up tool tips for column headers.
        sortingTable.getTableHeader()
                .setToolTipText("Click to specify sorting; Control-Click to specify secondary sorting");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        conceptPanel.add(conceptTable.getTableHeader(), c);
        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridheight = 6;

        ConceptAttributeTableRenderer renderer = new ConceptAttributeTableRenderer();
        conceptTable.setDefaultRenderer(StringWithConceptTuple.class, renderer);
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
        conceptTable.getColumn(CONCEPT_FIELD.DEFINED).setCellEditor(new DefaultCellEditor(comboBox));

        conceptTable.getColumn(CONCEPT_FIELD.STATUS)
                .setCellEditor(new ConceptAttributeTableModel.ConceptStatusFieldEditor(host.getConfig()));

        conceptTable.setDefaultRenderer(String.class, renderer);
        conceptTable.setDefaultRenderer(Boolean.class, renderer);
        conceptPanel.add(conceptTable, c);

        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridy = c.gridy + c.gridheight;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridwidth = 2;
        visibleExtensions.clear();
        RefsetUtil.addRefsetTables(host, this, TOGGLES.ATTRIBUTES, c, visibleExtensions, conceptPanel);
        conceptPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 3),
                                                                  BorderFactory.createLineBorder(Color.GRAY)));
        return conceptPanel;
    }

    @Override
    protected String getToolTipText() {
        return "show/hide primitive/defind and status value for this concept";
    }

    @Override
    protected int getComponentId() {
        if (conceptTable.getSelectedRow() < 0) {
            return Integer.MIN_VALUE;
        }
        StringWithConceptTuple swct = (StringWithConceptTuple) conceptTable
                .getValueAt(conceptTable.getSelectedRow(), 0);
        return swct.getTuple().getConId();
    }

    public void tableChanged(TableModelEvent tme) {
        if (conceptTable.getSelectedRow() == -1) {
            if (conceptTable.getRowCount() > 0) {
                int rowToSelect = conceptTable.getRowCount() -1;
                conceptTable.setRowSelectionInterval(rowToSelect, rowToSelect);
            }
        }
        
    }

}
