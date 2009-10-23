package org.dwfa.ace.table.refset;

import java.beans.PropertyChangeEvent;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntSet;

public class ReflexiveRefsetCommentTableModel extends ReflexiveRefsetTableModel {

	public ReflexiveRefsetCommentTableModel(I_HostConceptPlugins host,
			ReflexiveRefsetFieldData[] columns) {
		super(host, columns);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
		if (host.getTermComponent() != null) {
			this.tableComponentId = Integer.MIN_VALUE;
			I_GetConceptData refsetConcept = ConceptBean.get(host.getTermComponent().getTermComponentId());
			I_IntSet allowedTypes = new IntSet();
			try {
				allowedTypes.add(RefsetAuxiliary.Concept.COMMENTS_REL.localize().getNid());
				Set<I_GetConceptData> commentRefsets = 
					refsetConcept.getSourceRelTargets(host.getConfig().getAllowedStatus(), 
						allowedTypes, 
						host.getConfig().getViewPositionSet(), false);
				if (commentRefsets.size() > 0) {
					this.tableComponentId = commentRefsets.iterator().next().getConceptId();
				}
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			
		}
		fireTableDataChanged();
	}

}
