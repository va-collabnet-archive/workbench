/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	public List<I_RelTuple> getRels(I_GetConceptData cb, boolean usePrefs, boolean showHistory, TableChangedSwingWorker tableChangedSwingWorker) throws IOException {
		List<I_RelTuple> selectedTuples = new ArrayList<I_RelTuple>();
		I_IntSet allowedStatus = host.getConfig().getAllowedStatus();
		I_IntSet allowedTypes = null;
		Set<I_Position> positions = host.getConfig().getViewPositionSet();
		if (usePrefs) {
         if (host.getConfig().getSourceRelTypes().getSetValues().length == 0) {
            allowedTypes = null;
         } else {
            allowedTypes = host.getConfig().getSourceRelTypes();
         }
			
		}
		if (showHistory) {
			positions = null;
			allowedStatus = null;
		}
		for (I_RelVersioned rel: cb.getSourceRels()) {
			if (tableChangedSwingWorker.isWorkStopped()) {
				return selectedTuples;
			}
			rel.addTuples(allowedStatus, allowedTypes, positions, selectedTuples, true);
		}
		for (I_RelVersioned rel: cb.getUncommittedSourceRels()) {
			if (tableChangedSwingWorker.isWorkStopped()) {
				return selectedTuples;
			}
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
