package org.dwfa.ace.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_ContainTermComponent;
import org.dwfa.ace.IntSet;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.gui.concept.I_HostConceptPlugins;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.swing.SwingWorker;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;

import com.sleepycat.je.DatabaseException;

public class DescriptionsForConceptTableModel extends DescriptionTableModel
		implements PropertyChangeListener {

	private List<I_DescriptionTuple> allTuples;

	public class ReferencedConceptsSwingWorker extends
			SwingWorker<Map<Integer, ConceptBean>> {
		private boolean stopWork = false;

		@Override
		protected Map<Integer, ConceptBean> construct() throws Exception {
			getProgress().setActive(true);
			Map<Integer, ConceptBean> concepts = new HashMap<Integer, ConceptBean>();
			for (Integer id : new HashSet<Integer>(conceptsToFetch)) {
				if (stopWork) {
					break;
				}
				ConceptBean b = ConceptBean.get(id);
				b.getDescriptions();
				concepts.put(id, b);

			}
			return concepts;
		}

		@Override
		protected void finished() {
			super.finished();
			if (getProgress() != null) {
				getProgress().getProgressBar().setIndeterminate(false);
				if (conceptsToFetch.size() == 0) {
					getProgress().getProgressBar().setValue(1);
				} else {
					getProgress().getProgressBar().setValue(conceptsToFetch.size());
				}
			}
			if (stopWork) {
				return;
			}
			try {
				referencedConcepts = get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			fireTableDataChanged();
			if (getProgress() != null) {
				getProgress().setProgressInfo("   " + getRowCount() + "   ");
				getProgress().setActive(false);
			}

		}

		public void stop() {
			stopWork = true;
		}

	}

	public class TableChangedSwingWorker extends SwingWorker<Integer> {
		I_GetConceptData cb;

		private boolean stopWork = false;

		public TableChangedSwingWorker(I_GetConceptData cb) {
			super();
			this.cb = cb;
		}

		@Override
		protected Integer construct() throws Exception {
			if (refConWorker != null) {
				refConWorker.stop();
			}
			conceptsToFetch.clear();
			referencedConcepts.clear();
			if (cb == null) {
				return 0;
			}
			List<I_DescriptionVersioned> descs = cb.getDescriptions();
			for (I_DescriptionVersioned d : descs) {
				if (stopWork) {
					return -1;
				}
				for (I_DescriptionPart descVersion : d.getVersions()) {
					conceptsToFetch.add(descVersion.getTypeId());
					conceptsToFetch.add(descVersion.getStatusId());
					conceptsToFetch.add(descVersion.getPathId());
				}

			}

			refConWorker = new ReferencedConceptsSwingWorker();
			refConWorker.start();
			return descs.size();
		}

		@Override
		protected void finished() {
			super.finished();
			if (getProgress() != null) {
				getProgress().getProgressBar().setIndeterminate(false);
				if (conceptsToFetch.size() == 0) {
					getProgress().getProgressBar().setValue(1);
					getProgress().getProgressBar().setMaximum(1);
				} else {
					getProgress().getProgressBar().setValue(1);
					getProgress().getProgressBar().setMaximum(conceptsToFetch.size());
				}
			}
			if (stopWork) {
				return;
			}
			try {
				get();
			} catch (InterruptedException e) {
				;
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			fireTableDataChanged();

		}

		public void stop() {
			stopWork = true;
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private TableChangedSwingWorker tableChangeWorker;

	private ReferencedConceptsSwingWorker refConWorker;

	private Set<Integer> conceptsToFetch = new HashSet<Integer>();

	private Map<Integer, ConceptBean> referencedConcepts = new HashMap<Integer, ConceptBean>();

	private I_HostConceptPlugins host;
	
	public DescriptionsForConceptTableModel(DESC_FIELD[] columns,
			I_HostConceptPlugins host) {
		super(columns, host.getConfig());
		this.host = host;
		host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
	}
	public List<I_DescriptionTuple> getDescriptions() throws DatabaseException {
		List<I_DescriptionTuple> selectedTuples = new ArrayList<I_DescriptionTuple>();
		IntSet allowedStatus = host.getConfig().getAllowedStatus();
		IntSet allowedTypes = null;
		Set<Position> positions = null;
		if (host.getUsePrefs()) {
			allowedTypes = host.getConfig().getDescTypes();
			positions = host.getConfig().getViewPositionSet();
		}
		if (host.getShowHistory()) {
			positions = null;
			allowedStatus = null;
		}
		I_GetConceptData cb = (I_GetConceptData) host.getTermComponent();
		if (cb == null) {
			return selectedTuples;
		}
		for (I_DescriptionVersioned desc: cb.getDescriptions()) {
			desc.addTuples(allowedStatus, allowedTypes, positions, selectedTuples);
		}
		for (I_DescriptionVersioned desc: cb.getUncommittedDescriptions()) {
			desc.addTuples(allowedStatus, allowedTypes, positions, selectedTuples);
		}
		return selectedTuples;
	}

	protected I_DescriptionTuple getDescription(int rowIndex)
			throws DatabaseException {
		
		
		I_GetConceptData cb = (I_GetConceptData) host.getTermComponent();
		if (cb == null) {
			return null;
		}
		if (allTuples == null) {
			allTuples = getDescriptions();
		}
		return allTuples.get(rowIndex);
	}

	public int getRowCount() {
		I_GetConceptData cb = (I_GetConceptData) host.getTermComponent();
		if (cb == null) {
			return 0;
		}
		try {
			if (allTuples == null) {
				allTuples = getDescriptions();
			}
			return allTuples.size();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		allTuples = null;
		if (getProgress() != null) {
			getProgress().setVisible(true);
			getProgress().getProgressBar().setValue(0);
			getProgress().getProgressBar().setIndeterminate(true);
		}
		fireTableDataChanged();
		if (tableChangeWorker != null) {
			tableChangeWorker.stop();
		}
		tableChangeWorker = new TableChangedSwingWorker((I_GetConceptData) evt
				.getNewValue());
		tableChangeWorker.start();
	}

	public Map<Integer, ConceptBean> getReferencedConcepts() {
		return referencedConcepts;
	}
	public PopupListener makePopupListener(JTable table, AceFrameConfig config) {
		return new PopupListener(table, config);
	}

	public class PopupListener extends MouseAdapter {
		private class ChangeActionListener implements ActionListener {

			public ChangeActionListener() {
				super();
			}

			public void actionPerformed(ActionEvent e) {
				for (Path p : config.getEditingPathSet()) {
					I_DescriptionPart newPart = selectedObject.getTuple()
							.duplicatePart();
					newPart.setPathId(p.getConceptId());
					newPart.setVersion(Integer.MAX_VALUE);
					selectedObject.getTuple().getDescVersioned().getVersions().add(
							newPart);
				}
				ACE.addUncommitted(ConceptBean.get(selectedObject.getTuple().getConceptId()));
				allTuples = null;
				DescriptionsForConceptTableModel.this.fireTableDataChanged();
			}
		}

		private class RetireActionListener implements ActionListener {

			public RetireActionListener() {
				super();
			}

			public void actionPerformed(ActionEvent e) {
				try {
					for (Path p : config.getEditingPathSet()) {
						I_DescriptionPart newPart = selectedObject.getTuple()
								.duplicatePart();
						newPart.setPathId(p.getConceptId());
						newPart.setVersion(Integer.MAX_VALUE);
						newPart.setStatusId(AceConfig.vodb
								.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED
										.getUids()));
						selectedObject.getTuple().getDescVersioned()
								.getVersions().add(newPart);
					}
					ACE.addUncommitted(ConceptBean.get(selectedObject.getTuple().getConceptId()));
					allTuples = null;
					DescriptionsForConceptTableModel.this.fireTableDataChanged();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

		JPopupMenu popup;

		JTable table;

		ActionListener retire;

		ActionListener change;

		StringWithDescTuple selectedObject;

		AceFrameConfig config;

		public PopupListener(JTable table, AceFrameConfig config) {
			super();
			this.table = table;
			this.config = config;
			retire = new RetireActionListener();
			change = new ChangeActionListener();
		}

		private void makePopup(MouseEvent e) {
			popup = new JPopupMenu();
			JMenuItem noActionItem = new JMenuItem("");
			popup.add(noActionItem);
			int column = table.columnAtPoint(e.getPoint());
			int row = table.rowAtPoint(e.getPoint());
			selectedObject = (StringWithDescTuple) table
					.getValueAt(row, column);
			JMenuItem changeItem = new JMenuItem("Change: "
					+ selectedObject.getTuple().getText());
			popup.add(changeItem);
			changeItem.addActionListener(change);
			JMenuItem retireItem = new JMenuItem("Retire: "
					+ selectedObject.getTuple().getText());
			popup.add(retireItem);
			retireItem.addActionListener(retire);
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				if (config.getEditingPathSet().size() > 0) {
					makePopup(e);
					popup.show(e.getComponent(), e.getX(), e.getY());
				} else {
		            JOptionPane.showMessageDialog(table.getTopLevelAncestor(), "You must select at least one path to edit on...");
				}
			}
		}
	}

}
