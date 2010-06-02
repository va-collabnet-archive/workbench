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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.RefsetSpecEditor;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;

public class ReflexiveRefsetTableModel extends ReflexiveTableModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public class TableChangedSwingWorker extends SwingWorker<Boolean> implements I_ChangeTableInSwing {

        private boolean stopWork = false;

        public TableChangedSwingWorker(Integer componentId, Integer promotionFilterId) {
            super();
            refsetId = componentId;

            try {
                I_GetConceptData refsetConcept = Terms.get().getConcept(refsetId);
                IntSet promotionRefsetIds = new IntSet();
                List<? extends I_RelTuple> promotionTuples;
                promotionRefsetIds.add(RefsetAuxiliary.Concept.PROMOTION_REL.localize().getNid());
                promotionTuples =
                        refsetConcept.getSourceRelTuples(host.getConfig().getAllowedStatus(), promotionRefsetIds, host
                            .getConfig().getViewPositionSetReadOnly(), host.getConfig().getPrecedence(),
                            host.getConfig().getConflictResolutionStrategy());
                Iterator<? extends I_RelTuple> promotionIterator = promotionTuples.iterator();
                promotionRefsetComponentMap = new HashMap<Integer, I_ExtendByRef>();
                if (promotionIterator.hasNext()) {
                    promotionRefsetId = promotionTuples.iterator().next().getC2Id();
                    promotionRefsetIdentify = Terms.get().getConcept(promotionRefsetId);
                    Collection<? extends I_ExtendByRef> members = Terms.get().getRefsetExtensionMembers(promotionRefsetId);
                    promotionRefsetComponentMap = new HashMap<Integer, I_ExtendByRef>(members.size());
                    for (I_ExtendByRef ext: members) {
                        promotionRefsetComponentMap.put(ext.getComponentId(), ext);
                    }
                }
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            } 
        }

        public TableChangedSwingWorker(Integer componentId) {
            this(componentId, null);
        }

        @Override
        protected Boolean construct() throws Exception {
            if (refConWorker != null) {
                refConWorker.stop();
            }
            if (refsetId == null || refsetId == Integer.MIN_VALUE) {
                return true;
            }
            Collection<? extends I_ExtendByRef> refsetMembers = Terms.get().getRefsetExtensionMembers(refsetId);

            if (stopWork || refsetMembers.size() == 0) {
                return false;
            }
            for (I_ExtendByRef extension : refsetMembers) {

                I_IntSet statusSet = host.getConfig().getAllowedStatus();
                PositionSetReadOnly positionSet = host.getConfig().getViewPositionSetReadOnly();
                if (host instanceof RefsetSpecEditor) {
                    if (((RefsetSpecEditor) host).getRefsetSpecPanel().getShowPromotionCheckBoxes()) {
                        // include retired statuses
                        statusSet = null;
                        positionSet = null;
                    }
                }
                if (host.getShowHistory()) {
                    statusSet = null;
                    positionSet = null;
                }

                List<? extends I_ExtendByRefVersion> original =
                        extension.getTuples(statusSet, positionSet, 
                            host.getConfig().getPrecedence(), host.getConfig().getConflictResolutionStrategy());
                List<I_ExtendByRefVersion> allParts = new ArrayList<I_ExtendByRefVersion>();
                allParts.addAll(original);

                if (!host.getShowHistory()) {
                    if (host instanceof RefsetSpecEditor) {
                        if (((RefsetSpecEditor) host).getRefsetSpecPanel().getShowPromotionCheckBoxes()) {
                            // need to remove all except for the latest
                            I_ExtendByRefVersion latestTuple = null;
                            for (I_ExtendByRefVersion tuple : allParts) {
                                if (latestTuple == null || tuple.getVersion() >= latestTuple.getVersion()) {
                                    latestTuple = tuple;
                                }
                            }

                            allParts.clear();
                            if (latestTuple != null) {
                                allParts.add(latestTuple);
                            }
                        }
                    }
                }

                for (I_ExtendByRefVersion part : allParts) {
                    I_ExtendByRefVersion ebrTuple = (I_ExtendByRefVersion) part;
                    conceptsToFetch.add(ebrTuple.getStatusId());
                    boolean addPart = true;
                    for (ReflexiveRefsetFieldData col : columns) {
                        if (col.getType() == REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER) {
                            switch (col.invokeOnObjectType) {
                            case CONCEPT_COMPONENT:
                                if (col.readParamaters != null) {
                                    Object readValue =
                                            col.getReadMethod().invoke(
                                                Terms.get().getConcept(extension.getComponentId()), col.readParamaters);
                                    if (readValue != null && Integer.class.isAssignableFrom(readValue.getClass())) {
                                        conceptsToFetch.add((Integer) readValue);
                                    }
                                } else {
                                    Object readValue =
                                            col.getReadMethod().invoke(
                                                Terms.get().getConcept(extension.getComponentId()));
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
                                    conceptsToFetch.add((Integer) col.getReadMethod().invoke(ebrTuple.getMutablePart(),
                                        col.readParamaters));
                                } else {
                                    switch (col.type) {
                                    case COMPONENT_IDENTIFIER:
                                        if (Terms.get().hasConcept(
                                            (Integer) col.getReadMethod().invoke(ebrTuple.getMutablePart())) == false) {
                                            conceptsToFetch.add((Integer) col.getReadMethod().invoke(
                                                ebrTuple.getMutablePart()));
                                            break;
                                        }
                                    case CONCEPT_IDENTIFIER:
                                        I_ExtendByRefPart conceptIdPart = ebrTuple.getMutablePart();
                                        if (conceptIdPart != null) {
                                            if (I_ExtendByRefPartCid.class.isAssignableFrom(conceptIdPart.getClass())) {
                                                try {
                                                    Object obj = col.getReadMethod().invoke(conceptIdPart);
                                                    if (obj instanceof Integer) {
                                                        conceptsToFetch.add((Integer) obj);
                                                    } else {
                                                        AceLog.getAppLog().alertAndLogException(
                                                            new Exception(obj + " is not an instance of Integer"));
                                                    }
                                                } catch (Exception e) {
                                                    AceLog.getAppLog().alertAndLogException(new Exception(
                                                        "ReflexiveRefsetTableModel.CONCEPT_IDENTIFIER:", e));
                                                }
                                            } 
                                        }
                                        break;
                                    case STRING:
                                        break;
                                    case TIME:
                                        break;
                                    default:
                                        throw new UnsupportedOperationException("Don't know how to handle: " + col.type);
                                    }
                                }
                                break;
                            case PROMOTION_REFSET_PART:
                                Object obj = getPromotionRefsetValue(extension, col);
                                if (obj != null) {
                                    if (obj instanceof Integer) {
                                        conceptsToFetch.add((Integer) obj);
                                        if (promotionFilterId != null && !promotionFilterId.equals((Integer) obj)) {
                                            addPart = false;
                                        }
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
                    if (addPart) {
                        allTuples.add(ebrTuple);
                    } else {
                        allTuples.remove(ebrTuple);
                    }
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

    @SuppressWarnings("unchecked")
    public Object getPromotionRefsetValue(I_ExtendByRef extension, ReflexiveRefsetFieldData col) throws IOException,
            IllegalAccessException, InvocationTargetException, TerminologyException {
        if (promotionRefsetComponentMap != null && 
                promotionRefsetComponentMap.containsKey(extension.getNid())) {
            I_ExtendByRef promotionMember = promotionRefsetComponentMap.get(extension.getNid());
            if (promotionMember != null) {
                List<I_ExtendByRefVersion> promotionTuples = (List<I_ExtendByRefVersion>) 
                promotionMember.getTuples(
                    host.getConfig().getAllowedStatus(), 
                    host.getConfig().getViewPositionSetReadOnly(),
                    host.getConfig().getPrecedence(), 
                    host.getConfig().getConflictResolutionStrategy());
                if (promotionTuples.size() > 0) {
                    return col.getReadMethod().invoke(promotionTuples.get(0).getMutablePart());
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
            this.tableComponentId = host.getTermComponent().getNid();
        }
        fireTableDataChanged();
    }

    public I_GetConceptData getPromotionRefsetIdentityConcept() {
        return promotionRefsetIdentify;
    }

    protected Integer refsetId;
    protected I_GetConceptData promotionRefsetIdentify;
    protected int promotionRefsetId;
    protected Map<Integer, I_ExtendByRef> promotionRefsetComponentMap;

    public ReflexiveRefsetTableModel(I_HostConceptPlugins host, ReflexiveRefsetFieldData[] columns) {
        super(host, columns);
    }

    @Override
    protected I_ChangeTableInSwing getTableChangedSwingWorker(int tableComponentId2, Integer promotionFilterId) {
        return new TableChangedSwingWorker(tableComponentId2, promotionFilterId);
    }

    public Set<Integer> getSelectedTuples() {
        Set<Integer> memberNids = new HashSet<Integer>(checkedRows.cardinality());
        for (int i = checkedRows.nextSetBit(0); i >= 0; i = checkedRows.nextSetBit(i + 1)) {
            if (i < allTuples.size()) {
                I_ExtendByRefVersion tuple = allTuples.get(i);
                memberNids.add(tuple.getNid());
            } else {
                AceLog.getAppLog().warning("Selected row > row collection: " + i + 
                    " all tuples: " + allTuples);
            }
        }
        return memberNids;
    }

    public void clearSelectedTuples() {
        this.checkedRows.clear();
        fireTableDataChanged();
    }

    public void selectAllTuples() {
        this.checkedRows.set(0, getRowCount());
        fireTableDataChanged();
    }

    public void setShowPromotionCheckBoxes(boolean show) {
        // nothing to do
    }

}
