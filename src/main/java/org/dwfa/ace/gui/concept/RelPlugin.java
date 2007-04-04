package org.dwfa.ace.gui.concept;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.DropButton;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.edit.AddRelationship;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.RelTableModel;
import org.dwfa.ace.table.RelationshipTableRenderer;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;
import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;
import org.dwfa.bpa.util.TableSorter;


public abstract class RelPlugin extends AbstractPlugin {

	public RelPlugin(boolean selectedByDefault) {
		super(selectedByDefault);
	}

	private JTableWithDragImage relTable;

	protected JPanel getRelPanel(I_HostConceptPlugins host, RelTableModel model, String labelText,
			boolean enableEdit) {
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

		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;

		c.gridwidth = 1;
		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridheight = 2;
		DropButton rowAddAfter = new DropButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/row_add_after.png")), model);
		relPanel.add(rowAddAfter, c);
		rowAddAfter.setEnabled(enableEdit);
		rowAddAfter.addActionListener(new AddRelationship(host, host.getConfig()));
		if (enableEdit) {
			rowAddAfter.setTransferHandler(new TerminologyTransferHandler());
			// rowAddAfter.setDragEnabled(true);
		}
		c.gridheight = 1;
		c.gridx++;
		c.gridwidth = 1;

		TableSorter relSortingTable = new TableSorter(model);
		relTable = new JTableWithDragImage(relSortingTable);
		relSortingTable.setTableHeader(relTable.getTableHeader());
		relSortingTable
				.getTableHeader()
				.setToolTipText(
						"Click to specify sorting; Control-Click to specify secondary sorting");
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
		relTable.addMouseListener(model
				.makePopupListener(relTable, host.getConfig()));
		// Set up tool tips for column headers.
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		relPanel.add(relTable.getTableHeader(), c);
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.gridheight = 5;
		relTable.setDefaultRenderer(StringWithRelTuple.class,
				new RelationshipTableRenderer());
		relPanel.add(relTable, c);
		relPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(1, 1, 1, 3), BorderFactory
				.createLineBorder(Color.GRAY)));
		return relPanel;
	}

	protected void setupEditors(I_HostConceptPlugins host) {
		relTable.setDragEnabled(true);
		relTable.setTransferHandler(new TerminologyTransferHandler());
		relTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		relTable.getColumn(REL_FIELD.REL_TYPE).setCellEditor(
				new RelTableModel.RelTypeFieldEditor(host.getConfig()));
		relTable.getColumn(REL_FIELD.CHARACTERISTIC).setCellEditor(
				new RelTableModel.RelCharactisticFieldEditor(host.getConfig()));
		relTable.getColumn(REL_FIELD.REFINABILITY).setCellEditor(
				new RelTableModel.RelRefinabilityFieldEditor(host.getConfig()));
		relTable.getColumn(REL_FIELD.STATUS)
				.setCellEditor(
						new RelTableModel.RelStatusFieldEditor(host.getConfig()));
	}

	public JTableWithDragImage getRelTable() {
		return relTable;
	}

}
