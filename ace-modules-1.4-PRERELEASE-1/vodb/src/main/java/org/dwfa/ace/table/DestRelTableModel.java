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

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;

public class DestRelTableModel extends RelTableModel {

    public DestRelTableModel(I_HostConceptPlugins host, REL_FIELD[] columns, I_ConfigAceFrame config) {
        super(host, columns, config);
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    @Override
    public List<I_RelTuple> getRels(I_GetConceptData cb, boolean usePrefs, boolean showHistory,
            TableChangedSwingWorker tableChangedSwingWorker) throws IOException {
        List<I_RelTuple> selectedTuples = new ArrayList<I_RelTuple>();
        I_IntSet allowedStatus = host.getConfig().getAllowedStatus();
        I_IntSet allowedTypes = null;
        Set<I_Position> positions = host.getConfig().getViewPositionSet();
        if (usePrefs) {
            if (host.getConfig().getPrefFilterTypesForRel().getSetValues().length == 0) {
                allowedTypes = null;
            } else {
                allowedTypes = host.getConfig().getPrefFilterTypesForRel();
            }
        }
        if (showHistory) {
            positions = null;
            allowedStatus = null;
        }
        for (I_RelVersioned rel : cb.getDestRels()) {
            if (tableChangedSwingWorker.isWorkStopped()) {
                return selectedTuples;
            }
            try {
                rel.addTuples(allowedStatus, allowedTypes, positions, selectedTuples, true, !showHistory);
            } catch (TerminologyException e) {
                throw new ToIoException(e);
            }
        }

        return selectedTuples;
    }

    public void doDrop(I_GetConceptData obj) {
        throw new UnsupportedOperationException();
    }

}
