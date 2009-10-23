package org.dwfa.ace.table.refset;

import java.beans.PropertyChangeEvent;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.swing.SwingWorker;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ExtensionByReferenceBean;
import org.dwfa.vodb.types.ThinExtByRefTuple;

public class ReflexiveRefsetMemberTableModel extends ReflexiveTableModel  {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;
		

	public class TableChangedSwingWorker extends SwingWorker<Boolean> implements I_ChangeTableInSwing {
		Integer memberId;

		private boolean stopWork = false;

		public TableChangedSwingWorker(Integer componentId) {
			super();
			this.memberId = componentId;
		}

		@Override
		protected Boolean construct() throws Exception {
			if (refConWorker != null) {
				refConWorker.stop();
			}
			if (memberId == null || memberId == Integer.MIN_VALUE) {
				return true;
			}
			I_ThinExtByRefVersioned extension = null;
			if (AceConfig.getVodb().hasExtension(memberId)) {
				extension = ExtensionByReferenceBean.get(memberId).getExtension();
			} else {
				extension = ExtensionByReferenceBean.getNewExtensionMember(memberId);
			}

			if (stopWork || extension == null) {
				return false;
			}
			I_IntSet statusSet = host.getConfig().getAllowedStatus();
			Set<I_Position> positionSet = host.getConfig().getViewPositionSet();
			if (host.getShowHistory() == true) {
				statusSet = null;
				positionSet = null;
			}
			for (I_ThinExtByRefPart part : extension.getTuples(statusSet, positionSet, true, false)) {
				ThinExtByRefTuple ebrTuple = (ThinExtByRefTuple) part;
				for (ReflexiveRefsetFieldData col: columns) {
					if (col.getType() == REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER) {
						switch (col.invokeOnObjectType) {
						case CONCEPT_COMPONENT:
							if (col.readParamaters != null) {
								conceptsToFetch.add((Integer) col.getReadMethod().invoke(
										ConceptBean.get(extension.getComponentId()), col.readParamaters));
							} else {
								conceptsToFetch.add((Integer) col.getReadMethod().invoke(
										ConceptBean.get(extension.getComponentId())));
							}
							break;
						case COMPONENT:
							throw new UnsupportedOperationException();
						case CONCEPT:
							throw new UnsupportedOperationException();
						case IMMUTABLE:
							if (col.readParamaters != null) {
								conceptsToFetch.add((Integer) col.getReadMethod().invoke(
										ebrTuple, col.readParamaters));
							} else {
								conceptsToFetch.add((Integer) col.getReadMethod().invoke(ebrTuple));
							}
							break;
						case PART:
							if (col.readParamaters != null) {
								conceptsToFetch.add((Integer) col.getReadMethod().invoke(
										ebrTuple.getPart(), col.readParamaters));
							} else {
								conceptsToFetch.add((Integer) col.getReadMethod().invoke(ebrTuple.getPart()));
							}
							break;
						}
					}

				}
				if (stopWork) {
					return false;
				}
				if (allTuples == null) {
					AceLog.getAppLog()
					.info("all tuples for RefsetMemberTableModel is  null");
					return false;
				}
				allTuples.add(ebrTuple);
			}


			refConWorker = new ReferencedConceptsSwingWorker();
			refConWorker.start();
			return true;
		}

		@Override
		protected void finished() {
			super.finished();
			try {
				if (getProgress() != null) {
					getProgress().getProgressBar().setIndeterminate(false);
					if (conceptsToFetch.size() == 0) {
						getProgress().getProgressBar().setValue(1);
						getProgress().getProgressBar().setMaximum(1);
					} else {
						getProgress().getProgressBar().setValue(1);
						getProgress().getProgressBar().setMaximum(
								conceptsToFetch.size());
					}
				}
				if (get()) {
					tableComponentId = memberId;
				}
			} catch (InterruptedException e) {
				;
			} catch (ExecutionException ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}
			fireTableDataChanged();
		}

		public void stop() {
			stopWork = true;
		}

		public void setStopWork(boolean b) {
			stopWork = b;
		}
	}

	public ReflexiveRefsetMemberTableModel(I_HostConceptPlugins host,
			ReflexiveRefsetFieldData[] columns) {
		super(host, columns);
	}

	@Override
	protected I_ChangeTableInSwing getTableChangedSwingWorker(
			int tableComponentId2) {
		return new TableChangedSwingWorker(tableComponentId2);
	}	
	
	public void propertyChange(PropertyChangeEvent arg0) {
		if (tableChangeWorker != null) {
			tableChangeWorker.setStopWork(true);
		}
		allTuples = null;
		allExtensions = null;
		if (getProgress() != null) {
			getProgress().setVisible(true);
			getProgress().getProgressBar().setValue(0);
			getProgress().getProgressBar().setIndeterminate(true);
		}
		if (host.getConfig().getRefsetSpecInSpecEditor() == null) {
			this.tableComponentId = Integer.MIN_VALUE;
		}
		fireTableDataChanged();
	}

	public int getRowCount() {
		if (tableComponentId == Integer.MIN_VALUE) {
			return 1;
		}
		int count = super.getRowCount();
		if (count == 0) {
			return 1;
		}
		return count;

	}
}
