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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
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

public class DescriptionPlugin extends AbstractPlugin {

	private JPanel descPanel;
	private I_HostConceptPlugins host;
	private DescriptionsForConceptTableModel descTableModel;
	private JTableWithDragImage descTable;
   protected Set<EXT_TYPE> visibleExtensions = new HashSet<EXT_TYPE>();


	public DescriptionPlugin() {
		super(true);
	}

	@Override
	protected ImageIcon getImageIcon() {
		return new ImageIcon(ACE.class.getResource("/24x24/plain/paragraph.png"));
	}

	@Override
	public void update() throws IOException {
		if (host != null) {

         if (RefsetUtil.refSetsChanged(host, TOGGLES.DESCRIPTIONS, this, visibleExtensions)) {
            createPluginComponent(host);
         } 

         PropertyChangeEvent evt = new PropertyChangeEvent(host, "termComponent", null, host.getTermComponent());
			DESC_FIELD[] columnEnums = getDescColumns(host);
			descTableModel.setColumns(columnEnums);
			for (int i = 0; i < descTableModel.getColumnCount(); i++) {
				TableColumn column = descTable.getColumnModel().getColumn(i);
				DESC_FIELD columnDesc = columnEnums[i];
				column.setIdentifier(columnDesc);
				column.setPreferredWidth(columnDesc.getPref());
				column.setMaxWidth(columnDesc.getMax());
				column.setMinWidth(columnDesc.getMin());
			}
			setupEditorsAndRenderers(host);
			descTableModel.propertyChange(evt);
		}
	}

	public JComponent getComponent(I_HostConceptPlugins host) {
		if (descPanel == null || RefsetUtil.refSetsChanged(host, TOGGLES.DESCRIPTIONS, this, visibleExtensions)) {
			createPluginComponent(host);
		}
		return descPanel;
	}

   private void createPluginComponent(I_HostConceptPlugins host) {
      this.host = host;
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
		descTableModel = new DescriptionsForConceptTableModel(getDescColumns(host),
				host);
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
         JButton rowAddAfter = new JButton(new ImageIcon(ACE.class
               .getResource("/24x24/plain/row_add_after.png")));
         descPanel.add(rowAddAfter, c);
         rowAddAfter.addActionListener(new AddDescription(host, host.getConfig()));
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
		descTable.setTransferHandler(new TerminologyTransferHandler());
      
      if (ACE.editMode) {
         descTable.addMouseListener(descTableModel.makePopupListener(descTable,
               host.getConfig()));
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
		sortingTable
				.getTableHeader()
				.setToolTipText(
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
      visibleExtensions.clear();
      RefsetUtil.addRefsetTables(host, this, TOGGLES.DESCRIPTIONS, c, visibleExtensions, descPanel);

      descPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(1, 1, 1, 3), BorderFactory
				.createLineBorder(Color.GRAY)));

		return descPanel;
	}

	private void setupEditorsAndRenderers(I_HostConceptPlugins host) {
		DescriptionTableRenderer renderer = new DescriptionTableRenderer();
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
				} else if (StringWithDescTuple.class.isAssignableFrom(anObject
						.getClass())) {
					StringWithDescTuple swt = (StringWithDescTuple) anObject;
					value = Boolean.parseBoolean(swt.getCellText());
				}
				super.setSelectedItem(value);
			}
		};
		comboBox.addItem(new Boolean(true));
		comboBox.addItem(new Boolean(false));
      if (ACE.editMode) {
         descTable.setDefaultEditor(Boolean.class, new DefaultCellEditor(
               comboBox));

         descTable.setDefaultEditor(StringWithDescTuple.class,
               new DescriptionTableModel.DescTextFieldEditor());
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

}
