package org.dwfa.ace.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JTable;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.log.AceLog;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.types.ConceptBean;

public class DescriptionsForConceptTableModel extends DescriptionTableModel
		implements PropertyChangeListener {

	List<I_DescriptionTuple> allTuples;

	public class ReferencedConceptsSwingWorker extends
			SwingWorker<Boolean> {
		private boolean stopWork = false;
		private HashMap<Integer, ConceptBean> concepts;

		@Override
		protected Boolean construct() throws Exception {
			getProgress().setActive(true);
			concepts = new HashMap<Integer, ConceptBean>();
            Set<Integer> fetchSet = null;
            synchronized (conceptsToFetch) {
                fetchSet = new HashSet<Integer>(conceptsToFetch);
            }
            for (Integer id : fetchSet) {
 				if (stopWork) {
					return false;
				}
				ConceptBean b = ConceptBean.get(id);
				b.getDescriptions();
				concepts.put(id, b);

			}
			if (stopWork) {
				return false;
			}
			return true;
		}

		@Override
		protected void finished() {
			super.finished();
			try {
				if (get()) {
					if (stopWork) {
						return;
					}
					if (getProgress() != null) {
						getProgress().getProgressBar().setIndeterminate(false);
						if (conceptsToFetch.size() == 0) {
							getProgress().getProgressBar().setValue(1);
						} else {
							getProgress().getProgressBar().setValue(conceptsToFetch.size());
						}
					}
					referencedConcepts = concepts;
					fireTableDataChanged();
					if (getProgress() != null) {
						getProgress().setProgressInfo("   " + getRowCount() + "   ");
						getProgress().setActive(false);
					}
				}
			} catch (InterruptedException ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			} catch (ExecutionException ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}

		}

		public void stop() {
			stopWork = true;
		}

	}

	public class TableChangedSwingWorker extends SwingWorker<Boolean> {
		I_GetConceptData cb;

		private boolean stopWork = false;

		public TableChangedSwingWorker(I_GetConceptData cb) {
			super();
			this.cb = cb;
		}

		@Override
		protected Boolean construct() throws Exception {
			if (refConWorker != null) {
				refConWorker.stop();
			}
			if (cb == null) {
				return true;
			}
			List<I_DescriptionVersioned> descs = cb.getDescriptions();
			addToConceptsToFetch(descs);
			if (stopWork) {
				return false;
			}
			descs = cb.getUncommittedDescriptions();
			if (stopWork) {
				return false;
			}
			addToConceptsToFetch(descs);
			refConWorker = new ReferencedConceptsSwingWorker();
			refConWorker.start();
			return true;
		}

		@Override
		protected void finished() {
			super.finished();
			try {
				if (get()) {
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
					fireTableDataChanged();
				}
			} catch (InterruptedException e) {
				;
			} catch (ExecutionException ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}
		}

		private void addToConceptsToFetch(List<I_DescriptionVersioned> descs) {
			for (I_DescriptionVersioned d : descs) {
				if (stopWork) {
					return;
				}
				for (I_DescriptionPart descVersion : d.getVersions()) {
					conceptsToFetch.add(descVersion.getTypeId());
					conceptsToFetch.add(descVersion.getStatusId());
					conceptsToFetch.add(descVersion.getPathId());
				}
			}
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

	Map<Integer, ConceptBean> referencedConcepts = new HashMap<Integer, ConceptBean>();

	I_HostConceptPlugins host;

	public DescriptionsForConceptTableModel(DESC_FIELD[] columns,
			I_HostConceptPlugins host) {
		super(columns, host.getConfig());
		this.host = host;
		host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
	}
	public List<I_DescriptionTuple> getDescriptions() throws IOException {
		List<I_DescriptionTuple> selectedTuples = new ArrayList<I_DescriptionTuple>();
		I_IntSet allowedStatus = host.getConfig().getAllowedStatus();
		I_IntSet allowedTypes = null;
		Set<I_Position> positions = host.getConfig().getViewPositionSet();
		if (host.getUsePrefs()) {
         if (host.getConfig().getDescTypes().getSetValues().length == 0) {
            allowedTypes = null;
         } else {
            allowedTypes = host.getConfig().getDescTypes();
         }
			
		}
		if (host.getShowHistory()) {
			positions = null;
			allowedStatus = null;
		}
		I_GetConceptData cb = (I_GetConceptData) host.getTermComponent();
		if (cb == null) {
			return selectedTuples;
		}
		try {
			for (I_DescriptionVersioned desc : cb.getDescriptions()) {
				desc.addTuples(allowedStatus, allowedTypes, positions,
						selectedTuples, true, !host.getShowHistory());
			}
			for (I_DescriptionVersioned desc : cb.getUncommittedDescriptions()) {
				desc.addTuples(allowedStatus, allowedTypes, positions,
						selectedTuples, true, !host.getShowHistory());
			}
		} catch (TerminologyException e) {
			throw new ToIoException(e);
		}
		return selectedTuples;
	}

	protected I_DescriptionTuple getDescription(int rowIndex)
			throws IOException {
		
		if (rowIndex == -1) {
			return null;
		}
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
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
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
		if (tableChangeWorker != null) {
			tableChangeWorker.stop();
		}
		conceptsToFetch.clear();
		referencedConcepts.clear();
		tableChangeWorker = new TableChangedSwingWorker((I_GetConceptData) evt
				.getNewValue());
		tableChangeWorker.start();
		fireTableDataChanged();
	}

	public Map<Integer, ConceptBean> getReferencedConcepts() {
		return referencedConcepts;
	}
	public DescPopupListener makePopupListener(JTable table, I_ConfigAceFrame config) {
		return new DescPopupListener(table, config, this);
	}

	@Override
	public String getScore(int rowIndex) {
		return "";
	}

}
