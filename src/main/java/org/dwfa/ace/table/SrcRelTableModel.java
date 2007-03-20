package org.dwfa.ace.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.IntSet;
import org.dwfa.ace.gui.concept.I_HostConceptPlugins;
import org.dwfa.vodb.types.I_GetConceptData;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.ThinRelTuple;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.je.DatabaseException;

public class SrcRelTableModel extends RelTableModel {

	public SrcRelTableModel(I_HostConceptPlugins host, REL_FIELD[] columns) {
		super(host, columns);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public List<ThinRelTuple> getRels(I_GetConceptData cb, boolean usePrefs, boolean showHistory) throws DatabaseException {
		List<ThinRelTuple> selectedTuples = new ArrayList<ThinRelTuple>();
		IntSet allowedStatus = host.getConfig().getAllowedStatus();
		IntSet allowedTypes = null;
		Set<Position> positions = null;
		if (usePrefs) {
			allowedTypes = host.getConfig().getSourceRelTypes();
			positions = host.getConfig().getViewPositionSet();
		}
		if (showHistory) {
			positions = null;
			allowedStatus = null;
		}
		for (ThinRelVersioned rel: cb.getSourceRels()) {
			rel.addTuples(allowedStatus, allowedTypes, positions, selectedTuples, true);
		}
		
		return selectedTuples;
	}

	public void doDrop(I_GetConceptData obj) {
		throw new UnsupportedOperationException();
		/*
		ThinRelVersioned rel = new ThinRelVersioned(int relId, this.tableBean.getConceptId(), obj.getConceptId(),
				1);
		ThinRelPart relPart = new ThinRelPart();
		relPart.setCharacteristicId(characteristicId);
		relPart.setGroup(0);
		relPart.setPathId(pathId);
		relPart.setRefinabilityId(refinabilityId);
		relPart.setRelTypeId(relTypeId);
		relPart.setStatusId(statusId);
		relPart.setVersion(version);
		rel.addVersion(relPart);
		super.tableBean.getUncommittedSourceRels().add(rel);
		fireTableDataChanged();
		*/
		
	}

}