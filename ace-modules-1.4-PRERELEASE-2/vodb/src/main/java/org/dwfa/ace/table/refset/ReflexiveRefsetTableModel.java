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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.ThinExtByRefTuple;

public class ReflexiveRefsetTableModel extends ReflexiveTableModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public class TableChangedSwingWorker extends SwingWorker<Boolean> implements I_ChangeTableInSwing {

        private boolean stopWork = false;

        public TableChangedSwingWorker(Integer componentId) {
            super();
            refsetId = componentId;
            I_GetConceptData refsetConcept = ConceptBean.get(refsetId);
            IntSet promotionRefsetIds = new IntSet();
            List<I_RelTuple> promotionTuples;
            try {
                promotionRefsetIds.add(RefsetAuxiliary.Concept.PROMOTION_REL.localize().getNid());
                promotionTuples = refsetConcept.getSourceRelTuples(host.getConfig().getAllowedStatus(),
                    promotionRefsetIds, host.getConfig().getViewPositionSet(), true);
                Iterator<I_RelTuple> promotionIterator = promotionTuples.iterator();
                if (promotionIterator.hasNext()) {
                    promotionRefsetId = promotionTuples.iterator().next().getC2Id();
                    promotionRefsetIdentify = ConceptBean.get(promotionRefsetId);
                }
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (TerminologyException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

        @Override
        protected Boolean construct() throws Exception {
            if (refConWorker != null) {
                refConWorker.stop();
            }
            if (refsetId == null || refsetId == Integer.MIN_VALUE) {
                return true;
            }
            List<I_ThinExtByRefVersioned> refsetMembers = AceConfig.getVodb().getRefsetExtensionMembers(refsetId);

            if (stopWork || refsetMembers.size() == 0) {
                return false;
            }
            for (I_ThinExtByRefVersioned extension : refsetMembers) {
                I_IntSet statusSet = host.getConfig().getAllowedStatus();
                Set<I_Position> positionSet = host.getConfig().getViewPositionSet();
                if (host.getShowHistory() == true) {
                    statusSet = null;
                    positionSet = null;
                }
                for (I_ThinExtByRefPart part : extension.getTuples(statusSet, positionSet, true, false)) {
                    ThinExtByRefTuple ebrTuple = (ThinExtByRefTuple) part;
                    for (ReflexiveRefsetFieldData col : columns) {
                        if (col.getType() == REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER) {
                            switch (col.invokeOnObjectType) {
                            case CONCEPT_COMPONENT:
                                if (col.readParamaters != null) {
                                    Object readValue = col.getReadMethod().invoke(
                                        ConceptBean.get(extension.getComponentId()), col.readParamaters);
                                    if (readValue != null && Integer.class.isAssignableFrom(readValue.getClass())) {
                                        conceptsToFetch.add((Integer) readValue);
                                    }
                                } else {
                                    Object readValue = col.getReadMethod().invoke(
                                        ConceptBean.get(extension.getComponentId()));
                                    if (readValue != null && Integer.class.isAssignableFrom(readValue.getClass())) {
                                        conceptsToFetch.add((Integer) readValue);
                                    }
                                }
                                break;
                            case COMPONENT:
                                throw new UnsupportedOperationException();
                            case CONCEPT:
                                throw new UnsupportedOperationException();
                            case IMMUTABLE:
                                if (col.readParamaters != null) {
                                    conceptsToFetch.add((Integer) col.getReadMethod().invoke(ebrTuple,
                                        col.readParamaters));
                                } else {
                                    conceptsToFetch.add((Integer) col.getReadMethod().invoke(ebrTuple));
                                }
                                break;
                            case PART:
                                if (col.readParamaters != null) {
                                    conceptsToFetch.add((Integer) col.getReadMethod().invoke(ebrTuple.getPart(),
                                        col.readParamaters));
                                } else {
                                    switch (col.type) {
                                    case COMPONENT_IDENTIFIER:
                                        if (LocalVersionedTerminology.get().hasConcept(
                                            (Integer) col.getReadMethod().invoke(ebrTuple.getPart())) == false) {
                                            conceptsToFetch.add((Integer) col.getReadMethod()
                                                .invoke(ebrTuple.getPart()));
                                            break;
                                        }
                                    case CONCEPT_IDENTIFIER:
                                        I_ThinExtByRefPart conceptIdPart = ebrTuple.getPart();
                                        try {
                                            Object obj = col.getReadMethod().invoke(conceptIdPart);
                                            if (obj instanceof Integer) {
                                                conceptsToFetch.add((Integer) obj);
                                            } else {
                                                AceLog.getAppLog().alertAndLogException(
                                                    new Exception(obj + " is not an instance of Integer"));
                                            }
                                        } catch (Exception e) {
                                            AceLog.getAppLog().warning(
                                                "ReflexiveRefsetTableModel.CONCEPT_IDENTIFIER:"
                                                    + e.getLocalizedMessage());
                                        }
                                        break;
                                    case STRING:
                                        break;
                                    case VERSION:
                                        break;
                                    default:
                                        throw new UnsupportedOperationException("Don't know how to handle: " + col.type);
                                    }
                                }
                                break;
                            case PROMOTION_REFSET_PART:
                                // TODO need a more efficient manner of getting
                                // members for a component than
                                // iterating through all members...
                                Object obj = getPromotionRefsetValue(extension, col);
                                if (obj != null) {
                                    if (obj instanceof Integer) {
                                        conceptsToFetch.add((Integer) obj);
                                    } else {
                                        AceLog.getAppLog().alertAndLogException(
                                            new Exception(obj + " is not an instance of Integer"));
                                    }
                                }
                                break;

                            default:
                                throw new UnsupportedOperationException("Don't know how to handle: "
                                    + col.invokeOnObjectType);
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
                    tableComponentId = refsetId;
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

    protected Object getPromotionRefsetValue(I_ThinExtByRefVersioned extension, ReflexiveRefsetFieldData col)
            throws IOException, IllegalAccessException, InvocationTargetException {
        for (I_ThinExtByRefVersioned extForMember : LocalVersionedTerminology.get().getAllExtensionsForComponent(
            extension.getComponentId())) {
            if (promotionRefsetId == extForMember.getRefsetId()) {
                List<I_ThinExtByRefTuple> promotionTuples = extForMember.getTuples(host.getConfig().getAllowedStatus(),
                    host.getConfig().getViewPositionSet(), false);
                if (promotionTuples.size() > 0) {
                    return col.getReadMethod().invoke(promotionTuples.get(0).getPart());
                }
            }
        }
        return null;
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
        if (host.getTermComponent() != null) {
            this.tableComponentId = host.getTermComponent().getTermComponentId();
        }
        fireTableDataChanged();
    }

    public I_GetConceptData getPromotionRefsetIdentityConcept() {
        return promotionRefsetIdentify;
    }

    Integer refsetId;
    I_GetConceptData promotionRefsetIdentify;
    int promotionRefsetId;

    public ReflexiveRefsetTableModel(I_HostConceptPlugins host, ReflexiveRefsetFieldData[] columns) {
        super(host, columns);
    }

    @Override
    protected I_ChangeTableInSwing getTableChangedSwingWorker(int tableComponentId2) {
        return new TableChangedSwingWorker(tableComponentId2);
    }
}
