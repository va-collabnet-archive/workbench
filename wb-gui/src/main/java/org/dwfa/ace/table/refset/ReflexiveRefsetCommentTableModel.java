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
package org.dwfa.ace.table.refset;

import java.beans.PropertyChangeEvent;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.vodb.types.IntSet;

public class ReflexiveRefsetCommentTableModel extends ReflexiveRefsetTableModel {

    public ReflexiveRefsetCommentTableModel(I_HostConceptPlugins host, ReflexiveRefsetFieldData[] columns) {
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
        values = null;
        allExtensions = null;
        conceptCache = null;
        if (getProgress() != null) {
            getProgress().setVisible(true);
            getProgress().getProgressBar().setValue(0);
            getProgress().getProgressBar().setIndeterminate(true);
        }
        if (host.getTermComponent() != null) {
            try {
                this.tableComponentId = Integer.MIN_VALUE;
                I_GetConceptData refsetConcept = Terms.get().getConcept(host.getTermComponent().getNid());
                I_IntSet allowedTypes = new IntSet();
                allowedTypes.add(RefsetAuxiliary.Concept.COMMENTS_REL.localize().getNid());
                Set<? extends I_GetConceptData> commentRefsets =
                        refsetConcept.getSourceRelTargets(host.getConfig().getAllowedStatus(), allowedTypes, host
                            .getConfig().getViewPositionSetReadOnly(), host.getConfig().getPrecedence(), host.getConfig()
                            .getConflictResolutionStrategy());
                if (commentRefsets.size() > 0) {
                    this.tableComponentId = commentRefsets.iterator().next().getConceptNid();
                }
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }

        }
        fireTableDataChanged();
    }
}
