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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.DescriptionTableModel;
import org.dwfa.ace.table.DescriptionTableRenderer;
import org.dwfa.ace.table.DescriptionsForConceptTableModel;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.DescriptionTableModel.DESC_FIELD;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;

public class LanguageRefsetDisplayPlugin extends AbstractPlugin implements TableModelListener {

	private static final long serialVersionUID = 1L;
	private static final int dataVersion = 1;

	private TOGGLES toggle;

    private transient JComponent pluginComponent;
    private transient I_GetConceptData languageConcept;
	private transient I_ImageVersioned pluginImage;
	private transient ImageIcon pluginIcon;
	private transient DescriptionsForConceptTableModel descTableModel;
	private transient JTableWithDragImage languageRefsetTable;

	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(toggle);
		out.writeObject(languageConcept.getUids());
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			toggle = (TOGGLES) in.readObject();
			List<UUID> uuids = (List<UUID>) in.readObject();
			try {
				languageConcept = LocalVersionedTerminology.get().getConcept(uuids);
			} catch (TerminologyException e) {
				throw new ToIoException(e);
			}
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}


    public LanguageRefsetDisplayPlugin(boolean selectedByDefault, int sequence, TOGGLES toggle, I_GetConceptData languageConcept) {
        super(selectedByDefault, sequence);
        this.languageConcept = languageConcept;
        this.toggle = toggle;
    }

    @Override
    protected ImageIcon getImageIcon() {
    	if (this.pluginImage == null) {
        	try {
        		if (this.languageConcept.getImages() != null) {
        			for (I_ImageVersioned image: this.languageConcept.getImages()) {
        				for (I_ImageTuple imageTuple: image.getTuples()) {
        					if (imageTuple.getTextDescription().toLowerCase().contains("24x24")) {
        						pluginIcon = new ImageIcon(imageTuple.getImage());
        						return pluginIcon;
        					}
        				}
        			}
        		}
    		} catch (IOException e) {
    			AceLog.getAppLog().alertAndLogException(e);
    		}
    	} else {
            return pluginIcon;    		
    	}
		return new ImageIcon(ACE.class.getResource("/24x24/plain/bullet_triangle_blue.png"));
    }

    @Override
    public void update() throws IOException {
        if (getHost() != null) {
			PropertyChangeEvent evt = new PropertyChangeEvent(getHost(),
					"termComponent", null, getHost().getTermComponent());
			DESC_FIELD[] columnEnums = getDescColumns(getHost());
			descTableModel.setColumns(columnEnums);
			for (int i = 0; i < descTableModel.getColumnCount(); i++) {
				TableColumn column = languageRefsetTable.getColumnModel().getColumn(i);
				DESC_FIELD columnDesc = columnEnums[i];
				column.setIdentifier(columnDesc);
				column.setPreferredWidth(columnDesc.getPref());
				column.setMaxWidth(columnDesc.getMax());
				column.setMinWidth(columnDesc.getMin());
			}
			setupEditorsAndRenderers(getHost());
			descTableModel.propertyChange(evt);
        }
    }

	private DESC_FIELD[] getDescColumns(I_HostConceptPlugins host) {
		setHost(host);
		List<DESC_FIELD> fields = new ArrayList<DESC_FIELD>();
		fields.add(DESC_FIELD.TEXT);
		fields.add(DESC_FIELD.TYPE);
		fields.add(DESC_FIELD.LANG);
		fields.add(DESC_FIELD.STATUS);
		if (host.getShowHistory()) {
			fields.add(DESC_FIELD.VERSION);
			fields.add(DESC_FIELD.PATH);
		}
		return fields.toArray(new DESC_FIELD[fields.size()]);
	}

	private void setupEditorsAndRenderers(I_HostConceptPlugins host) {
		setHost(host);
		DescriptionTableRenderer renderer = new DescriptionTableRenderer(host.getConfig());
		languageRefsetTable.setDefaultRenderer(Boolean.class, renderer);
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
			languageRefsetTable.setDefaultEditor(Boolean.class, new DefaultCellEditor(
					comboBox));

			languageRefsetTable.setDefaultEditor(StringWithDescTuple.class,
					new DescriptionTableModel.DescTextFieldEditor());
			languageRefsetTable.getColumn(DESC_FIELD.TYPE).setCellEditor(
					new DescriptionTableModel.DescTypeFieldEditor(host
							.getConfig()));
			languageRefsetTable.getColumn(DESC_FIELD.STATUS).setCellEditor(
					new DescriptionTableModel.DescStatusFieldEditor(host
							.getConfig()));
		}

		languageRefsetTable.setDefaultRenderer(StringWithDescTuple.class, renderer);
		languageRefsetTable.setDefaultRenderer(Number.class, renderer);
		languageRefsetTable.setDefaultRenderer(String.class, renderer);
	}

    public JComponent getComponent(I_HostConceptPlugins host) {
        if (pluginComponent == null) {
            try {
				createPluginComponent(host);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
        }
        return pluginComponent;
    }

    private void createPluginComponent(I_HostConceptPlugins host) throws IOException {
    	I_DescriptionTuple descTuple = languageConcept.getDescTuple(host.getConfig().getTableDescPreferenceList(), 
    			host.getConfig());
    	setHost(host);
        pluginComponent = getDescPanel(host, descTuple.getText());
        host.addPropertyChangeListener(I_HostConceptPlugins.SHOW_HISTORY, this);
        host.addPropertyChangeListener("commit", this);
    }

	private JPanel getDescPanel(I_HostConceptPlugins host, String DialectName) {
		descTableModel = new DescriptionsForConceptTableModel(
				getDescColumns(host), host);
		descTableModel.addTableModelListener(this);
		JPanel descPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 2;
		JLabel descLabel = new JLabel(DialectName + ":");
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

		JLabel filler = new JLabel(getImageIcon());
		filler.setMaximumSize(new Dimension(40, 32));
		filler.setMinimumSize(new Dimension(40, 32));
		filler.setPreferredSize(new Dimension(40, 32));
		descPanel.add(filler, c);

		c.gridheight = 1;
		c.gridx++;
		TableSorter sortingTable = new TableSorter(descTableModel);
		languageRefsetTable = new JTableWithDragImage(sortingTable);
		languageRefsetTable.getSelectionModel().addListSelectionListener(this);
		languageRefsetTable.setDragEnabled(true);
		languageRefsetTable.setTransferHandler(new TerminologyTransferHandler(languageRefsetTable));

		if (ACE.editMode) {
			languageRefsetTable.addMouseListener(descTableModel.makePopupListener(
					languageRefsetTable, host.getConfig()));
		}

		languageRefsetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sortingTable.setTableHeader(languageRefsetTable.getTableHeader());

		DESC_FIELD[] columnEnums = descTableModel.getColumnEnums();

		for (int i = 0; i < languageRefsetTable.getColumnCount(); i++) {
			TableColumn column = languageRefsetTable.getColumnModel().getColumn(i);
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
		descPanel.add(languageRefsetTable.getTableHeader(), c);
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridheight = 6;

		setupEditorsAndRenderers(host);
		descPanel.add(languageRefsetTable, c);

		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridy = c.gridy + c.gridheight;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridwidth = 2;

		descPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(1, 1, 1, 3), BorderFactory
				.createLineBorder(Color.GRAY)));

		return descPanel;
	}

    @Override
    protected String getToolTipText() {
        return "show/hide Languge Refset Display";
    }

	@Override
	protected int getComponentId() {
		return ((I_GetConceptData) getHost().getTermComponent()).getConceptId();
	}

	public void tableChanged(TableModelEvent tme) {
		if (languageRefsetTable.getSelectedRow() == -1) {
			if (languageRefsetTable.getRowCount() > 0) {
				int rowToSelect = 0; // descTable.getRowCount() -1;
				languageRefsetTable.setRowSelectionInterval(rowToSelect, rowToSelect);
			}
		}
	}

	public UUID getId() {
		return toggle.getPluginId();
	}
}
