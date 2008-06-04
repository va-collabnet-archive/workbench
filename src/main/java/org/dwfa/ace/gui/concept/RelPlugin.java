package org.dwfa.ace.gui.concept;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
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
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.edit.AddRelationship;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.RelTableModel;
import org.dwfa.ace.table.RelationshipTableRenderer;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;
import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;
import org.dwfa.ace.table.refset.RefsetUtil;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;


public abstract class RelPlugin extends AbstractPlugin implements TableModelListener, I_HostConceptPlugins {

	public RelPlugin(boolean selectedByDefault) {
		super(selectedByDefault);
	}
   
   protected Set<EXT_TYPE> visibleExtensions = new HashSet<EXT_TYPE>();
	private JTableWithDragImage relTable;
	protected boolean idToggleState = false;
	protected IdPlugin idPlugin;

	protected JPanel getRelPanel(I_HostConceptPlugins host, RelTableModel model, String labelText,
			boolean enableEdit, TOGGLES toggle) {
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
			DropButton rowAddAfter = new DropButton(new ImageIcon(ACE.class
					.getResource("/24x24/plain/row_add_after.png")), model);
			relPanel.add(rowAddAfter, c);
			rowAddAfter.setEnabled(enableEdit);
			rowAddAfter.addActionListener(new AddRelationship(host, host.getConfig()));
			rowAddAfter.setTransferHandler(new TerminologyTransferHandler(rowAddAfter));
		} else {
			JPanel filler = new JPanel();
			filler.setMaximumSize(new Dimension(40, 32));
			filler.setMinimumSize(new Dimension(40, 32));
			filler.setPreferredSize(new Dimension(40, 32));
			relPanel.add(filler, c);
			
		}
		c.gridheight = 1;
		c.gridx++;
		c.gridwidth = 1;

		TableSorter relSortingTable = new TableSorter(model);
		relTable = new JTableWithDragImage(relSortingTable);
        relTable.getSelectionModel().addListSelectionListener(this);
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
      if (ACE.editMode) {
         relTable.addMouseListener(model
               .makePopupListener(relTable, host.getConfig()));
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
		relTable.setDefaultRenderer(StringWithRelTuple.class,
				new RelationshipTableRenderer());
		relPanel.add(relTable, c);
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.gridy = c.gridy + c.gridheight;
      c.gridheight = 1;
      c.gridx = 0;
      c.gridwidth = 2;

		if (host.getToggleState(TOGGLES.ID) == true) {
			idPlugin = new IdPlugin();
			idPlugin.setShowBorder(false);
			relPanel.add(idPlugin.getComponent(this), c);
			c.gridy++;
		}

      visibleExtensions.clear();
      RefsetUtil.addRefsetTables(host, this, toggle, c, visibleExtensions, relPanel);
		relPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(1, 1, 1, 3), BorderFactory
				.createLineBorder(Color.GRAY)));
		return relPanel;
	}


	protected void setupEditors(I_HostConceptPlugins host) {
		relTable.setDragEnabled(true);
		relTable.setTransferHandler(new TerminologyTransferHandler(relTable));
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


   @Override
   protected int getComponentId() {
      if (relTable.getSelectedRow() < 0) {
         return Integer.MIN_VALUE;
      }
      StringWithRelTuple swrt = (StringWithRelTuple) relTable.getValueAt(relTable.getSelectedRow(), 0);
      return swrt.getTuple().getRelId();
   }
   
   public void tableChanged(TableModelEvent tme) {
       if (relTable.getSelectedRow() == -1) {
           if (relTable.getRowCount() > 0) {
               int rowToSelect = 0; //relTable.getRowCount() -1;
               relTable.setRowSelectionInterval(rowToSelect, rowToSelect);
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

	public VIEW_TYPE getViewType() {
		return getHost().getViewType();
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

	PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
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
	
	protected I_AmTermComponent getSelectedPluginComponent() {
		if (relTable.getSelectedRow() < 0) {
			return null;
		}
		StringWithRelTuple swrt = (StringWithRelTuple) relTable.getValueAt(
				relTable.getSelectedRow(), 0);
		return swrt.getTuple().getRelVersioned();
	}

	
	protected abstract I_HostConceptPlugins getHost();
}
