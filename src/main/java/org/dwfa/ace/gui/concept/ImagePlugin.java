package org.dwfa.ace.gui.concept;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

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
import org.dwfa.ace.edit.AddImage;
import org.dwfa.ace.table.ImageTableModel;
import org.dwfa.ace.table.ImageTableRenderer;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.ImageTableModel.IMAGE_FIELD;
import org.dwfa.ace.table.ImageTableModel.ImageWithImageTuple;
import org.dwfa.bpa.util.TableSorter;

import com.sleepycat.je.DatabaseException;

public class ImagePlugin extends AbstractPlugin {

	private JPanel imagePanel;
	private I_HostConceptPlugins host;
	private ImageTableModel imageTableModel;
	private JTableWithDragImage imageTable;

	public ImagePlugin() {
		super(false);
	}

	@Override
	protected ImageIcon getImageIcon() {
		return new ImageIcon(ACE.class.getResource("/24x24/plain/photo_scenery.png"));
	}

	@Override
	public void update() throws DatabaseException {
		if (host != null) {
			PropertyChangeEvent evt = new PropertyChangeEvent(host, "termComponent", null, host.getTermComponent());
			IMAGE_FIELD[] columnEnums = getImageColumns(host);
			imageTableModel.setColumns(getImageColumns(host));
			for (int i = 0; i < imageTableModel.getColumnCount(); i++) {
				TableColumn column = imageTable.getColumnModel().getColumn(i);
				IMAGE_FIELD columnDesc = columnEnums[i];
				column.setIdentifier(columnDesc);
				column.setPreferredWidth(columnDesc.getPref());
				column.setMaxWidth(columnDesc.getMax());
				column.setMinWidth(columnDesc.getMin());
			}
			imageTableModel.propertyChange(evt);
		}
	}

	public JComponent getComponent(I_HostConceptPlugins host) {
		if (imagePanel == null) {
			this.host = host;
			imageTableModel = new ImageTableModel(host,
					getImageColumns(host), host.getShowHistory());
			imagePanel = getImagePanel(host);
			host.addPropertyChangeListener(I_HostConceptPlugins.SHOW_HISTORY, this);
		}
		return imagePanel;
	}

	private IMAGE_FIELD[] getImageColumns(I_HostConceptPlugins host) {
		List<IMAGE_FIELD> fields = new ArrayList<IMAGE_FIELD>();
		fields.add(IMAGE_FIELD.IMAGE);
		fields.add(IMAGE_FIELD.TYPE);
		fields.add(IMAGE_FIELD.DESC);
		fields.add(IMAGE_FIELD.STATUS);
		if (host.getShowHistory()) {
			fields.add(IMAGE_FIELD.VERSION);
			fields.add(IMAGE_FIELD.BRANCH);
		}
		return fields.toArray(new IMAGE_FIELD[fields.size()]);
	}

	private JPanel getImagePanel(I_HostConceptPlugins host) {
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
		JButton rowAddAfter = new JButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/row_add_after.png")));
		imagePanel.add(rowAddAfter, c);
		rowAddAfter.addActionListener(new AddImage(host, host.getConfig()));
		c.gridheight = 1;
		c.gridx++;
		TableSorter sortingTable = new TableSorter(imageTableModel);
		imageTable = new JTableWithDragImage(sortingTable);
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
		sortingTable
				.getTableHeader()
				.setToolTipText(
						"Click to specify sorting; Control-Click to specify secondary sorting");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		imagePanel.add(imageTable.getTableHeader(), c);
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridheight = 6;

		imageTable.setDefaultRenderer(String.class, new ImageTableRenderer());
		imageTable.setDefaultRenderer(ImageWithImageTuple.class,
				new ImageTableRenderer());
		imagePanel.add(imageTable, c);
		imagePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(1, 1, 1, 3), BorderFactory
				.createLineBorder(Color.GRAY)));

		c.gridheight = 1;
		c.gridx = 0;
		return imagePanel;
	}

}
