package org.dwfa.ace.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.IntSet;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.gui.concept.I_HostConceptPlugins;
import org.dwfa.vodb.types.Position;

import com.sleepycat.je.DatabaseException;

public class DestRelTableModel extends RelTableModel {

	public DestRelTableModel(I_HostConceptPlugins host, REL_FIELD[] columns) {
		super(host, columns);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public List<I_RelTuple> getRels(I_GetConceptData cb, boolean usePrefs, boolean showHistory) throws DatabaseException {
		List<I_RelTuple> selectedTuples = new ArrayList<I_RelTuple>();
		IntSet allowedStatus = host.getConfig().getAllowedStatus();
		IntSet allowedTypes = null;
		Set<Position> positions = null;
		if (usePrefs) {
			allowedTypes = host.getConfig().getDestRelTypes();
			positions = host.getConfig().getViewPositionSet();
		}
		if (showHistory) {
			positions = null;
			allowedStatus = null;
		}
		for (I_RelVersioned rel: cb.getDestRels()) {
			rel.addTuples(allowedStatus, allowedTypes, positions, selectedTuples, true);
		}
		
		return selectedTuples;
	}
	
	public void doDrop(I_GetConceptData obj) {
		throw new UnsupportedOperationException();
	}
	
}