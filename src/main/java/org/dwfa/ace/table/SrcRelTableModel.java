package org.dwfa.ace.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;

public class SrcRelTableModel extends RelTableModel {

	public SrcRelTableModel(I_HostConceptPlugins host, REL_FIELD[] columns) {
		super(host, columns);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public List<I_RelTuple> getRels(I_GetConceptData cb, boolean usePrefs, boolean showHistory) throws IOException {
		List<I_RelTuple> selectedTuples = new ArrayList<I_RelTuple>();
		I_IntSet allowedStatus = host.getConfig().getAllowedStatus();
		I_IntSet allowedTypes = null;
		Set<I_Position> positions = null;
		if (usePrefs) {
			allowedTypes = host.getConfig().getSourceRelTypes();
			positions = host.getConfig().getViewPositionSet();
		}
		if (showHistory) {
			positions = null;
			allowedStatus = null;
		}
		for (I_RelVersioned rel: cb.getSourceRels()) {
			rel.addTuples(allowedStatus, allowedTypes, positions, selectedTuples, true);
		}
		for (I_RelVersioned rel: cb.getUncommittedSourceRels()) {
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