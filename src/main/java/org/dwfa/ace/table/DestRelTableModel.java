package org.dwfa.ace.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.ConceptPanel;
import org.dwfa.ace.IntSet;
import org.dwfa.vodb.types.I_GetConceptData;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.ThinRelTuple;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.je.DatabaseException;

public class DestRelTableModel extends RelTableModel {

	public DestRelTableModel(ConceptPanel parentPanel, REL_FIELD[] columns, boolean showHistory) {
		super(parentPanel, columns, showHistory);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public List<ThinRelTuple> getRels(I_GetConceptData cb, boolean usePrefs, boolean showHistory) throws DatabaseException {
		List<ThinRelTuple> selectedTuples = new ArrayList<ThinRelTuple>();
		IntSet allowedStatus = parentPanel.getConfig().getAllowedStatus();
		IntSet allowedTypes = null;
		Set<Position> positions = null;
		if (usePrefs) {
			allowedTypes = parentPanel.getConfig().getDestRelTypes();
			positions = parentPanel.getConfig().getViewPositionSet();
		}
		if (showHistory) {
			positions = null;
			allowedStatus = null;
		}
		for (ThinRelVersioned rel: cb.getDestRels()) {
			rel.addTuples(allowedStatus, allowedTypes, positions, selectedTuples, true);
		}
		
		return selectedTuples;
	}

	public void doDrop(I_GetConceptData obj) {
		throw new UnsupportedOperationException();
	}
	
}