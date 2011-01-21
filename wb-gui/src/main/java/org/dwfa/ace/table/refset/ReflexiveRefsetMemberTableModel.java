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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PositionBI;

public class ReflexiveRefsetMemberTableModel extends ReflexiveTableModel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private I_ChangeTableInSwing tableChangedSwingWorker;

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
            I_ExtendByRef extension = null;
            if (AceConfig.getVodb().hasExtension(memberId)) {
                extension = Terms.get().getExtension(memberId);
            } else {
                extension = Terms.get().getExtension(memberId);
            }

            if (stopWork || extension == null) {
                return false;
            }
            I_IntSet statusSet = host.getConfig().getAllowedStatus();
            Set<PositionBI> positionSet = host.getConfig().getViewPositionSet();
            if (positionSet == null || positionSet.size() == 0) {
                AceLog.getAppLog().alertAndLogException(new Exception("View position set is empty: " + positionSet));
            }
            if (host.getShowHistory() == true) {
                statusSet = null;
                positionSet = null;
            }
            for (I_ExtendByRefPart part : extension.getTuples(statusSet, new PositionSetReadOnly(positionSet), host
                .getConfig().getPrecedence(), host.getConfig().getConflictResolutionStrategy())) {
                I_ExtendByRefVersion ebrTuple = (I_ExtendByRefVersion) part;
                for (ReflexiveRefsetFieldData col : columns) {
                    if (col.getType() == REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER) {
                        switch (col.invokeOnObjectType) {
                        case CONCEPT_COMPONENT:
                            if (col.readParamaters != null) {
                                conceptsToFetch.add((Integer) col.getReadMethod().invoke(
                                    Terms.get().getConcept(extension.getComponentNid()), col.readParamaters));
                            } else {
                                conceptsToFetch.add((Integer) col.getReadMethod().invoke(
                                    Terms.get().getConcept(extension.getComponentNid())));
                            }
                            break;
                        case COMPONENT:
                            throw new UnsupportedOperationException();
                        case CONCEPT:
                            throw new UnsupportedOperationException();
                        case IMMUTABLE:
                            if (col.readParamaters != null) {
                                conceptsToFetch.add((Integer) col.getReadMethod().invoke(ebrTuple, col.readParamaters));
                            } else {
                                conceptsToFetch.add((Integer) col.getReadMethod().invoke(ebrTuple));
                            }
                            break;
                        case PART:
                            if (col.readParamaters != null) {
                                conceptsToFetch.add((Integer) col.getReadMethod().invoke(ebrTuple.getMutablePart(),
                                    col.readParamaters));
                            } else {
                                conceptsToFetch.add((Integer) col.getReadMethod().invoke(ebrTuple.getMutablePart()));
                            }
                            break;
                        default:
                            throw new UnsupportedOperationException("Don't know how to handle: " + col.invokeOnObjectType);
                        }
                    }

                }
                if (stopWork) {
                    return false;
                }

                if (allTuples == null) {
                    AceLog.getAppLog().info("all tuples for RefsetMemberTableModel is  null");
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
                        getProgress().getProgressBar().setMaximum(conceptsToFetch.size());
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

    public ReflexiveRefsetMemberTableModel(I_HostConceptPlugins host, ReflexiveRefsetFieldData[] columns) {
        super(host, columns);
    }

    @Override
    protected I_ChangeTableInSwing getTableChangedSwingWorker(int tableComponentId2, Integer promotionFilterId) {
        if (tableChangedSwingWorker == null) {
            tableChangedSwingWorker = new TableChangedSwingWorker(tableComponentId2);
        } else {
            while (!tableChangedSwingWorker.isDone()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            tableChangedSwingWorker = new TableChangedSwingWorker(tableComponentId2);
        }
        return tableChangedSwingWorker;
    }

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
        try {
            if (host.getConfig().getRefsetSpecInSpecEditor() == null) {
                this.tableComponentId = Integer.MIN_VALUE;
            }
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
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

    @Override
    public I_GetConceptData getPromotionRefsetIdentityConcept() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object getPromotionRefsetValue(I_ExtendByRef extension, ReflexiveRefsetFieldData reflexiveRefsetFieldData)
            throws IOException, IllegalAccessException, InvocationTargetException {
        throw new UnsupportedOperationException();
    }
}
