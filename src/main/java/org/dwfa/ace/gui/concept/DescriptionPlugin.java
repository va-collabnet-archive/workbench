package org.dwfa.ace.gui.concept;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

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
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.edit.AddDescription;
import org.dwfa.ace.table.DescriptionTableModel;
import org.dwfa.ace.table.DescriptionTableRenderer;
import org.dwfa.ace.table.DescriptionsForConceptTableModel;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.DescriptionTableModel.DESC_FIELD;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;
import org.dwfa.bpa.util.TableSorter;

import com.sleepycat.je.DatabaseException;

public class DescriptionPlugin extends AbstractPlugin {

	private JPanel descPanel;
	private I_HostConceptPlugins host;
	private DescriptionsForConceptTableModel descTableModel;
	private JTableWithDragImage descTable;

	public DescriptionPlugin() {
		super(true);
	}

	@Override
	protected ImageIcon getImageIcon() {
		return new ImageIcon(ACE.class.getResource("/24x24/plain/paragraph.png"));
	}

	@Override
	public void update() throws DatabaseException {
		System.out.println("Description plugin update...");
		if (host != null) {
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
			descTableModel.propertyChange(evt);
		}
	}

	public JComponent getComponent(I_HostConceptPlugins host) {
		if (descPanel == null) {
			this.host = host;
			descPanel = getDescPanel(host);
			host.addPropertyChangeListener(I_HostConceptPlugins.SHOW_HISTORY, this);
		}
		return descPanel;
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
			fields.add(DESC_FIELD.BRANCH);
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
		JButton rowAddAfter = new JButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/row_add_after.png")));
		descPanel.add(rowAddAfter, c);
		rowAddAfter.addActionListener(new AddDescription(host, host.getConfig()));

		c.gridheight = 1;
		c.gridx++;
		TableSorter sortingTable = new TableSorter(descTableModel);
		descTable = new JTableWithDragImage(sortingTable);
		descTable.setDragEnabled(true);
		descTable.setTransferHandler(new TerminologyTransferHandler());
		descTable.addMouseListener(descTableModel.makePopupListener(descTable,
				host.getConfig()));
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
		descTable.setDefaultEditor(Boolean.class, new DefaultCellEditor(
				comboBox));

		descTable.setDefaultEditor(StringWithDescTuple.class,
				new DescriptionTableModel.DescTextFieldEditor());
		descTable.setDefaultRenderer(StringWithDescTuple.class, renderer);
		descTable.getColumn(DESC_FIELD.TYPE).setCellEditor(
				new DescriptionTableModel.DescTypeFieldEditor(host.getConfig()));
		descTable.getColumn(DESC_FIELD.STATUS).setCellEditor(
				new DescriptionTableModel.DescStatusFieldEditor(host.getConfig()));

		descTable.setDefaultRenderer(Number.class, renderer);
		descTable.setDefaultRenderer(String.class, renderer);
		descPanel.add(descTable, c);
		descPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(1, 1, 1, 3), BorderFactory
				.createLineBorder(Color.GRAY)));

		c.gridheight = 1;
		c.gridx = 0;
		return descPanel;
	}

}
