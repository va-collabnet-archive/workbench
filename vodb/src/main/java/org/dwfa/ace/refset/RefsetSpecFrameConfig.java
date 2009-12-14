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
package org.dwfa.ace.refset;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;

import org.dwfa.ace.api.BundleType;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_FilterTaxonomyRels;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HoldRefsetPreferences;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.I_OverrideTaxonomyRenderer;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_PluginToConceptPanel;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.api.I_HostConceptPlugins.HOST_ENUM;
import org.dwfa.ace.api.I_HostConceptPlugins.REFSET_TYPES;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.gui.toptoggles.TopToggleTypes;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.data.SortedSetModel;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinExtBinder;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntList;
import org.dwfa.vodb.types.IntSet;
import org.tigris.subversion.javahl.PromptUserPassword3;

public class RefsetSpecFrameConfig implements I_ConfigAceFrame {

    I_ConfigAceFrame frameConfig;

    public JTree getTreeInTaxonomyPanel() {
        return frameConfig.getTreeInTaxonomyPanel();
    }

    public void fireRefsetSpecChanged(I_ThinExtByRefVersioned ext) {
        frameConfig.fireRefsetSpecChanged(ext);
    }

    public void addConceptPanelPlugins(HOST_ENUM host, UUID id, I_PluginToConceptPanel plugin) {
        frameConfig.addConceptPanelPlugins(host, id, plugin);
    }

    public I_PluginToConceptPanel getConceptPanelPlugin(HOST_ENUM host, UUID id) {
        return frameConfig.getConceptPanelPlugin(host, id);
    }

    public Set<UUID> getConceptPanelPluginKeys(HOST_ENUM host) {
        return frameConfig.getConceptPanelPluginKeys(host);
    }

    public Collection<I_PluginToConceptPanel> getConceptPanelPlugins(HOST_ENUM host) {
        return frameConfig.getConceptPanelPlugins(host);
    }

    public List<I_PluginToConceptPanel> getDefaultConceptPanelPluginsForEditor() {
        return frameConfig.getDefaultConceptPanelPluginsForEditor();
    }

    public List<I_PluginToConceptPanel> getDefaultConceptPanelPluginsForViewer() {
        return frameConfig.getDefaultConceptPanelPluginsForViewer();
    }

    public I_PluginToConceptPanel removeConceptPanelPlugin(HOST_ENUM host, UUID id) {
        return frameConfig.removeConceptPanelPlugin(host, id);
    }

    public Map<String, Object> getProperties() throws IOException {
        return frameConfig.getProperties();
    }

    public Object getProperty(String key) throws IOException {
        return frameConfig.getProperty(key);
    }

    public void setProperty(String key, Object value) throws IOException {
        frameConfig.setProperty(key, value);
    }

    public void invalidate() {
        frameConfig.invalidate();
    }

    public void repaint() {
        frameConfig.repaint();
    }

    public void validate() {
        frameConfig.validate();
    }

    public Color getColorForPath(int pathNid) {
        return frameConfig.getColorForPath(pathNid);
    }

    public void setColorForPath(int pathNid, Color pathColor) {
        frameConfig.setColorForPath(pathNid, pathColor);
    }

    private I_IntSet childrenExpandedNodes;

    RefsetSpecFrameConfig(I_ConfigAceFrame frameConfig, I_IntSet childrenExpandedNodes, boolean refsetParentOnly) {
        super();
        this.frameConfig = frameConfig;
        this.childrenExpandedNodes = childrenExpandedNodes;
        this.refsetParentOnly = refsetParentOnly;
    }

    public void addEditingPath(I_Path p) {
        frameConfig.addEditingPath(p);
    }

    public void addImported(I_GetConceptData conceptBean) {
        frameConfig.addImported(conceptBean);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        frameConfig.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        frameConfig.addPropertyChangeListener(propertyName, listener);
    }

    public void addUncommitted(I_GetConceptData conceptBean) {
        frameConfig.addUncommitted(conceptBean);
    }

    public void addViewPosition(I_Position p) {
        frameConfig.addViewPosition(p);
    }

    public void closeFrame() {
        frameConfig.closeFrame();
    }

    public void fireCommit() {
        frameConfig.fireCommit();
    }

    public SortedSetModel<String> getAddressesList() {
        return frameConfig.getAddressesList();
    }

    public String getAdminPassword() {
        return frameConfig.getAdminPassword();
    }

    public String getAdminUsername() {
        return frameConfig.getAdminUsername();
    }

    public I_ManageConflict[] getAllConflictResolutionStrategies() {
        return frameConfig.getAllConflictResolutionStrategies();
    }

    public I_IntSet getAllowedStatus() {
        return frameConfig.getAllowedStatus();
    }

    public JList getBatchConceptList() {
        return frameConfig.getBatchConceptList();
    }

    public Rectangle getBounds() {
        return frameConfig.getBounds();
    }

    public BundleType getBundleType() {
        return frameConfig.getBundleType();
    }

    public Collection<I_ReadChangeSet> getChangeSetReaders() {
        return frameConfig.getChangeSetReaders();
    }

    public Collection<I_WriteChangeSet> getChangeSetWriters() {
        return frameConfig.getChangeSetWriters();
    }

    public I_IntSet getChildrenExpandedNodes() {
        return childrenExpandedNodes;
    }

    public I_GetConceptData getClassificationRoleRoot() {
        return frameConfig.getClassificationRoleRoot();
    }

    public I_GetConceptData getClassificationRoot() {
        return frameConfig.getClassificationRoot();
    }

    public I_GetConceptData getClassifierInputPath() {
        return frameConfig.getClassifierInputPath();
    }

    public I_GetConceptData getClassifierIsaType() {
        return frameConfig.getClassifierIsaType();
    }

    public I_GetConceptData getClassifierOutputPath() {
        return frameConfig.getClassifierOutputPath();
    }

    public I_HostConceptPlugins getConceptViewer(int index) {
        return frameConfig.getConceptViewer(index);
    }

    public I_ManageConflict getConflictResolutionStrategy() {
        return frameConfig.getConflictResolutionStrategy();
    }

    public I_GetConceptData getContext() {
        return frameConfig.getContext();
    }

    public I_ConfigAceDb getDbConfig() {
        return frameConfig.getDbConfig();
    }

    public I_GetConceptData getDefaultDescriptionType() {
        return frameConfig.getDefaultDescriptionType();
    }

    public I_GetConceptData getDefaultImageType() {
        return frameConfig.getDefaultImageType();
    }

    public I_GetConceptData getDefaultRelationshipCharacteristic() {
        return frameConfig.getDefaultRelationshipCharacteristic();
    }

    public I_GetConceptData getDefaultRelationshipRefinability() {
        return frameConfig.getDefaultRelationshipRefinability();
    }

    public I_GetConceptData getDefaultRelationshipType() {
        return frameConfig.getDefaultRelationshipType();
    }

    public I_GetConceptData getDefaultStatus() {
        return frameConfig.getDefaultStatus();
    }

    public I_IntSet getDescTypes() {
        return frameConfig.getDescTypes();
    }

    public I_IntSet getDestRelTypes() {
        return frameConfig.getDestRelTypes();
    }

    public I_IntList getEditDescTypePopup() {
        return frameConfig.getEditDescTypePopup();
    }

    public I_IntList getEditImageTypePopup() {
        return frameConfig.getEditImageTypePopup();
    }

    public Set<I_Path> getEditingPathSet() {
        return frameConfig.getEditingPathSet();
    }

    public I_IntList getEditRelCharacteristicPopup() {
        return frameConfig.getEditRelCharacteristicPopup();
    }

    public I_IntList getEditRelRefinabiltyPopup() {
        return frameConfig.getEditRelRefinabiltyPopup();
    }

    public I_IntList getEditRelTypePopup() {
        return frameConfig.getEditRelTypePopup();
    }

    public I_IntList getEditStatusTypePopup() {
        return frameConfig.getEditStatusTypePopup();
    }

    public String getFrameName() {
        return frameConfig.getFrameName();
    }

    public Set<TopToggleTypes> getHiddenTopToggles() {
        return frameConfig.getHiddenTopToggles();
    }

    public I_GetConceptData getHierarchySelection() {
        return frameConfig.getHierarchySelection();
    }

    public Boolean getHighlightConflictsInComponentPanel() {
        return frameConfig.getHighlightConflictsInComponentPanel();
    }

    public Boolean getHighlightConflictsInTaxonomyView() {
        return frameConfig.getHighlightConflictsInTaxonomyView();
    }

    public I_IntSet getInferredViewTypes() {
        return frameConfig.getInferredViewTypes();
    }

    public I_GetConceptData getLastViewed() {
        return frameConfig.getLastViewed();
    }

    public I_HostConceptPlugins getListConceptViewer() {
        return frameConfig.getListConceptViewer();
    }

    public I_IntList getLongLabelDescPreferenceList() {
        return frameConfig.getLongLabelDescPreferenceList();
    }

    public I_IntSet getParentExpandedNodes() {
        return frameConfig.getParentExpandedNodes();
    }

    public String getPassword() {
        return frameConfig.getPassword();
    }

    public Collection<String> getQueueAddressesToShow() {
        return frameConfig.getQueueAddressesToShow();
    }

    public I_GetConceptData getRefsetInSpecEditor() {
        return frameConfig.getRefsetInSpecEditor();
    }

    public I_HoldRefsetPreferences getRefsetPreferencesForToggle(TOGGLES toggle) throws TerminologyException,
            IOException {
        return frameConfig.getRefsetPreferencesForToggle(toggle);
    }

    public Map<TOGGLES, I_HoldRefsetPreferences> getRefsetPreferencesMap() {
        return frameConfig.getRefsetPreferencesMap();
    }

    public I_GetConceptData getRefsetSpecInSpecEditor() throws IOException, TerminologyException {
        return frameConfig.getRefsetSpecInSpecEditor();
    }

    public I_IntList getRefsetsToShowInTaxonomy() {
        IntList refsetsToShow = new IntList();
        if (getRefsetInSpecEditor() != null) {
            I_GetConceptData refset = getRefsetInSpecEditor();
            IntSet allowedTypes = new IntSet();
            try {
                allowedTypes.add(RefsetAuxiliary.Concept.MARKED_PARENT_REFSET.localize().getNid());
                boolean addUncommitted = true;
                List<? extends I_RelTuple> markedParentRefset = refset.getSourceRelTuples(frameConfig.getAllowedStatus(),
                    allowedTypes, frameConfig.getViewPositionSetReadOnly(), addUncommitted);
                for (I_RelTuple rel : markedParentRefset) {
                    refsetsToShow.add(rel.getC2Id());
                }
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (TerminologyException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
            refsetsToShow.add(refset.getConceptId());
        }
        return refsetsToShow;
    }

    public I_IntList getRefsetsToSortTaxonomy() {
        return this.getRefsetsToShowInTaxonomy();
    }

    public I_IntSet getRoots() {
        IntSet refsetRoots = new IntSet();
        try {
            I_IntList refsets = getRefsetsToShowInTaxonomy();
            for (int rootNid : frameConfig.getRoots().getSetValues()) {
                ConceptBean rootBean = ConceptBean.get(rootNid);
                for (I_ThinExtByRefVersioned ext : rootBean.getExtensions()) {
                    if (refsets.contains(ext.getRefsetId())) {
                        List<I_ThinExtByRefTuple> tuples = ext.getTuples(frameConfig.getAllowedStatus(),
                            frameConfig.getViewPositionSet(), true);
                        if (tuples != null && tuples.size() > 0) {
                            refsetRoots.add(rootNid);
                        }
                    }
                }
            }
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return refsetRoots;
    }

    public List<String> getSelectedAddresses() {
        return frameConfig.getSelectedAddresses();
    }

    public I_ThinExtByRefVersioned getSelectedRefsetClauseInSpecEditor() {
        return frameConfig.getSelectedRefsetClauseInSpecEditor();
    }

    public I_IntList getShortLabelDescPreferenceList() {
        return frameConfig.getShortLabelDescPreferenceList();
    }

    public Boolean getShowInferredInTaxonomy() {
        return frameConfig.getShowInferredInTaxonomy();
    }

    public Boolean getShowRefsetInfoInTaxonomy() {
        return true;
    }

    public boolean getShowViewerImagesInTaxonomy() {
        return true;
    }

    public JPanel getSignpostPanel() {
        return frameConfig.getSignpostPanel();
    }

    public Boolean getSortTaxonomyUsingRefset() {
        return true;
    }

    public I_IntSet getSourceRelTypes() {
        return frameConfig.getSourceRelTypes();
    }

    public I_IntSet getStatedViewTypes() {
        return frameConfig.getStatedViewTypes();
    }

    public String getStatusMessage() {
        return frameConfig.getStatusMessage();
    }

    public Map<String, SubversionData> getSubversionMap() {
        return frameConfig.getSubversionMap();
    }

    public Map<String, List<I_GetConceptData>> getTabHistoryMap() {
        return frameConfig.getTabHistoryMap();
    }

    public I_IntList getTableDescPreferenceList() {
        return frameConfig.getTableDescPreferenceList();
    }

    private class RefsetParentOnlyFilter implements I_FilterTaxonomyRels {

        public void filter(I_GetConceptData node, List<? extends I_RelTuple> srcRels, List<? extends I_RelTuple> destRels,
                I_ConfigAceFrame frameConfig) throws TerminologyException, IOException {

            List<I_RelTuple> relsToRemove = new ArrayList<I_RelTuple>();
            for (I_RelTuple rt : srcRels) {
                ConceptBean child = ConceptBean.get(rt.getC2Id());
                if (notMarkedParent(child)) {
                    relsToRemove.add(rt);
                }
            }
            srcRels.removeAll(relsToRemove);

            relsToRemove = new ArrayList<I_RelTuple>();
            for (I_RelTuple rt : destRels) {
                ConceptBean child = ConceptBean.get(rt.getC1Id());
                if (notMarkedParent(child)) {
                    relsToRemove.add(rt);
                }
            }
            destRels.removeAll(relsToRemove);

        }

        private boolean notMarkedParent(ConceptBean child) throws IOException, TerminologyException {
            for (I_ThinExtByRefVersioned ext : child.getExtensions()) {
                if (getRefsetsToShowInTaxonomy().contains(ext.getRefsetId())) {
                    if (ThinExtBinder.getExtensionType(ext) == EXT_TYPE.CONCEPT) {
                        List<I_ThinExtByRefTuple> returnTuples = new ArrayList<I_ThinExtByRefTuple>();
                        ext.addTuples(getAllowedStatus(), getViewPositionSet(), returnTuples, false);
                        if (returnTuples.size() > 0) {
                            return false;
                        }

                    }

                }
            }
            return true;
        }

    }

    List<I_FilterTaxonomyRels> filterList;
    private boolean refsetParentOnly;

    public List<I_FilterTaxonomyRels> getTaxonomyRelFilterList() {
        if (filterList == null) {
            filterList = new ArrayList<I_FilterTaxonomyRels>(frameConfig.getTaxonomyRelFilterList());
            if (refsetParentOnly) {
                filterList.add(new RefsetParentOnlyFilter());
            }
        }
        return filterList;
    }

    public List<I_OverrideTaxonomyRenderer> getTaxonomyRendererOverrideList() {
        return frameConfig.getTaxonomyRendererOverrideList();
    }

    public I_IntList getTreeDescPreferenceList() {
        return frameConfig.getTreeDescPreferenceList();
    }

    public JTree getTreeInSpecEditor() {
        return frameConfig.getTreeInSpecEditor();
    }

    public int getTreeTermDividerLoc() {
        return frameConfig.getTreeTermDividerLoc();
    }

    public String getUsername() {
        return frameConfig.getUsername();
    }

    public Boolean getVariableHeightTaxonomyView() {
        return frameConfig.getVariableHeightTaxonomyView();
    }

    public VetoableChangeSupport getVetoSupport() {
        return frameConfig.getVetoSupport();
    }

    public Set<I_Position> getViewPositionSet() {
        return frameConfig.getViewPositionSet();
    }

    public MasterWorker getWorker() {
        return frameConfig.getWorker();
    }

    public JPanel getWorkflowPanel() {
        return frameConfig.getWorkflowPanel();
    }

    public boolean isActive() {
        return frameConfig.isActive();
    }

    public boolean isAddressToggleVisible() {
        return frameConfig.isAddressToggleVisible();
    }

    public boolean isAdministrative() {
        return frameConfig.isAdministrative();
    }

    public boolean isBuilderToggleVisible() {
        return frameConfig.isBuilderToggleVisible();
    }

    public boolean isCommitEnabled() {
        return frameConfig.isCommitEnabled();
    }

    public boolean isComponentToggleVisible() {
        return frameConfig.isComponentToggleVisible();
    }

    public boolean isHierarchyToggleVisible() {
        return frameConfig.isHierarchyToggleVisible();
    }

    public boolean isHistoryToggleVisible() {
        return frameConfig.isHistoryToggleVisible();
    }

    public boolean isInboxToggleVisible() {
        return frameConfig.isInboxToggleVisible();
    }

    public boolean isPreferencesToggleVisible() {
        return frameConfig.isPreferencesToggleVisible();
    }

    public boolean isRefsetInToggleVisible(REFSET_TYPES refsetType, TOGGLES toggle) {
        return frameConfig.isRefsetInToggleVisible(refsetType, toggle);
    }

    public boolean isSubversionToggleVisible() {
        return frameConfig.isSubversionToggleVisible();
    }

    public boolean isToggleVisible(TOGGLES toggle) {
        return frameConfig.isToggleVisible(toggle);
    }

    public void performLuceneSearch(String query, I_GetConceptData root) {
        frameConfig.performLuceneSearch(query, root);
    }

    public void performLuceneSearch(String query, List<I_TestSearchResults> extraCriterion) {
        frameConfig.performLuceneSearch(query, extraCriterion);
    }

    public void removeEditingPath(I_Path p) {
        frameConfig.removeEditingPath(p);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        frameConfig.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        frameConfig.removePropertyChangeListener(propertyName, listener);
    }

    public void removeUncommitted(I_GetConceptData uncommitted) {
        frameConfig.removeUncommitted(uncommitted);
    }

    public void removeViewPosition(I_Position p) {
        frameConfig.removeViewPosition(p);
    }

    public void replaceEditingPath(I_Path oldPath, I_Path newPath) {
        frameConfig.replaceEditingPath(oldPath, newPath);
    }

    public void replaceViewPosition(I_Position oldPosition, I_Position newPosition) {
        frameConfig.replaceViewPosition(oldPosition, newPosition);
    }

    public void selectConceptViewer(int hostIndex) {
        frameConfig.selectConceptViewer(hostIndex);
    }

    public void setActive(boolean active) {
        frameConfig.setActive(active);
    }

    public void setAddressToggleVisible(boolean visible) {
        frameConfig.setAddressToggleVisible(visible);
    }

    public void setAdministrative(boolean isAdministrative) {
        frameConfig.setAdministrative(isAdministrative);
    }

    public void setAdminPassword(String adminPassword) {
        frameConfig.setAdminPassword(adminPassword);
    }

    public void setAdminUsername(String adminUsername) {
        frameConfig.setAdminUsername(adminUsername);
    }

    public void setAllowedStatus(I_IntSet allowedStatus) {
        frameConfig.setAllowedStatus(allowedStatus);
    }

    public void setBounds(Rectangle bounds) {
        frameConfig.setBounds(bounds);
    }

    public void setBuilderToggleVisible(boolean visible) {
        frameConfig.setBuilderToggleVisible(visible);
    }

    public void setClassificationRoleRoot(I_GetConceptData classificationRoleRoot) {
        frameConfig.setClassificationRoleRoot(classificationRoleRoot);
    }

    public void setClassificationRoot(I_GetConceptData classificationRoot) {
        frameConfig.setClassificationRoot(classificationRoot);
    }

    public void setClassifierInputPath(I_GetConceptData inputPath) {
        frameConfig.setClassifierInputPath(inputPath);
    }

    public void setClassifierIsaType(I_GetConceptData classifierIsaType) {
        frameConfig.setClassifierIsaType(classifierIsaType);
    }

    public void setClassifierOutputPath(I_GetConceptData outputPath) {
        frameConfig.setClassifierOutputPath(outputPath);
    }

    public void setCommitAbortButtonsVisible(boolean visible) {
        frameConfig.setCommitAbortButtonsVisible(visible);
    }

    public void setCommitEnabled(boolean enabled) {
        frameConfig.setCommitEnabled(enabled);
    }

    public void setComponentToggleVisible(boolean visible) {
        frameConfig.setComponentToggleVisible(visible);
    }

    public <T extends I_ManageConflict> void setConflictResolutionStrategy(Class<T> conflictResolutionStrategyClass) {
        frameConfig.setConflictResolutionStrategy(conflictResolutionStrategyClass);
    }

    public void setConflictResolutionStrategy(I_ManageConflict conflictResolutionStrategy) {
        frameConfig.setConflictResolutionStrategy(conflictResolutionStrategy);
    }

    public void setContext(I_GetConceptData context) {
        frameConfig.setContext(context);
    }

    public void setDbConfig(I_ConfigAceDb dbConfig) {
        frameConfig.setDbConfig(dbConfig);
    }

    public void setDefaultDescriptionType(I_GetConceptData defaultDescriptionType) {
        frameConfig.setDefaultDescriptionType(defaultDescriptionType);
    }

    public void setDefaultImageType(I_GetConceptData defaultImageType) {
        frameConfig.setDefaultImageType(defaultImageType);
    }

    public void setDefaultRelationshipCharacteristic(I_GetConceptData defaultRelationshipCharacteristic) {
        frameConfig.setDefaultRelationshipCharacteristic(defaultRelationshipCharacteristic);
    }

    public void setDefaultRelationshipRefinability(I_GetConceptData defaultRelationshipRefinability) {
        frameConfig.setDefaultRelationshipRefinability(defaultRelationshipRefinability);
    }

    public void setDefaultRelationshipType(I_GetConceptData defaultRelationshipType) {
        frameConfig.setDefaultRelationshipType(defaultRelationshipType);
    }

    public void setDefaultStatus(I_GetConceptData defaultStatus) {
        frameConfig.setDefaultStatus(defaultStatus);
    }

    public void setDescTypes(I_IntSet allowedTypes) {
        frameConfig.setDescTypes(allowedTypes);
    }

    public void setDestRelTypes(I_IntSet browseUpRels) {
        frameConfig.setDestRelTypes(browseUpRels);
    }

    public void setEditDescTypePopup(I_IntList editDescTypePopup) {
        frameConfig.setEditDescTypePopup(editDescTypePopup);
    }

    public void setEditImageTypePopup(I_IntList editImageTypePopup) {
        frameConfig.setEditImageTypePopup(editImageTypePopup);
    }

    public void setEditRelCharacteristicPopup(I_IntList editRelCharacteristicPopup) {
        frameConfig.setEditRelCharacteristicPopup(editRelCharacteristicPopup);
    }

    public void setEditRelRefinabiltyPopup(I_IntList editRelRefinabiltyPopup) {
        frameConfig.setEditRelRefinabiltyPopup(editRelRefinabiltyPopup);
    }

    public void setEditRelTypePopup(I_IntList editRelTypePopup) {
        frameConfig.setEditRelTypePopup(editRelTypePopup);
    }

    public void setEditStatusTypePopup(I_IntList editStatusTypePopup) {
        frameConfig.setEditStatusTypePopup(editStatusTypePopup);
    }

    public void setFrameName(String frameName) {
        frameConfig.setFrameName(frameName);
    }

    public void setFrameVisible(boolean visible) throws Exception {
        frameConfig.setFrameVisible(visible);
    }

    public void setHierarchySelection(I_GetConceptData hierarchySelection) {
        frameConfig.setHierarchySelection(hierarchySelection);
    }

    public void setHierarchySelectionAndExpand(I_GetConceptData hierarchySelection) throws IOException {
        frameConfig.setHierarchySelectionAndExpand(hierarchySelection);
    }

    public void setHierarchyToggleVisible(boolean visible) {
        frameConfig.setHierarchyToggleVisible(visible);
    }

    public void setHighlightConflictsInComponentPanel(Boolean highlightConflictsInComponentPanel) {
        frameConfig.setHighlightConflictsInComponentPanel(highlightConflictsInComponentPanel);
    }

    public void setHighlightConflictsInTaxonomyView(Boolean highlightConflictsInTaxonomyView) {
        frameConfig.setHighlightConflictsInTaxonomyView(highlightConflictsInTaxonomyView);
    }

    public void setHistoryToggleVisible(boolean visible) {
        frameConfig.setHistoryToggleVisible(visible);
    }

    public void setInboxToggleVisible(boolean visible) {
        frameConfig.setInboxToggleVisible(visible);
    }

    public void setInferredViewTypes(I_IntSet inferredViewTypes) {
        frameConfig.setInferredViewTypes(inferredViewTypes);
    }

    public void setLastViewed(I_GetConceptData conceptBean) {
        frameConfig.setLastViewed(conceptBean);
    }

    public void setPassword(String password) {
        frameConfig.setPassword(password);
    }

    public void setPreferencesToggleVisible(boolean visible) {
        frameConfig.setPreferencesToggleVisible(visible);
    }

    public void setRefsetInToggleVisible(REFSET_TYPES refsetType, TOGGLES toggle, boolean visible) {
        frameConfig.setRefsetInToggleVisible(refsetType, toggle, visible);
    }

    public void setRoots(I_IntSet roots) {
        frameConfig.setRoots(roots);
    }

    public void setShowAddresses(boolean shown) {
        frameConfig.setShowAddresses(shown);
    }

    public void setShowAllQueues(boolean show) {
        frameConfig.setShowAllQueues(show);
    }

    public void setShowComponentView(boolean shown) {
        frameConfig.setShowComponentView(shown);
    }

    public void setShowHierarchyView(boolean shown) {
        frameConfig.setShowHierarchyView(shown);
    }

    public void setShowHistory(boolean shown) {
        frameConfig.setShowHistory(shown);
    }

    public void setShowInferredInTaxonomy(Boolean showInferredInTaxonomy) {
        frameConfig.setShowInferredInTaxonomy(showInferredInTaxonomy);
    }

    public void setShowPreferences(boolean shown) {
        frameConfig.setShowPreferences(shown);
    }

    public void setShowProcessBuilder(boolean show) {
        frameConfig.setShowProcessBuilder(show);
    }

    public void setShowQueueViewer(boolean show) {
        frameConfig.setShowQueueViewer(show);
    }

    public void setShowRefsetInfoInTaxonomy(Boolean showRefsetInfoInTaxonomy) {

    }

    public void setShowSearch(boolean shown) {
        frameConfig.setShowSearch(shown);
    }

    public void setShowSignpostPanel(boolean show) {
        frameConfig.setShowSignpostPanel(show);
    }

    public void setShowViewerImagesInTaxonomy(Boolean showViewerImagesInTaxonomy) {

    }

    public void setSignpostToggleEnabled(boolean enabled) {
        frameConfig.setSignpostToggleEnabled(enabled);
    }

    public void setSignpostToggleIcon(ImageIcon icon) {
        frameConfig.setSignpostToggleIcon(icon);
    }

    public void setSignpostToggleVisible(boolean show) {
        frameConfig.setSignpostToggleVisible(show);
    }

    public void setSortTaxonomyUsingRefset(Boolean sortTaxonomyUsingRefset) {

    }

    public void setSourceRelTypes(I_IntSet browseDownRels) {
        frameConfig.setSourceRelTypes(browseDownRels);
    }

    public void setStatedViewTypes(I_IntSet statedViewTypes) {
        frameConfig.setStatedViewTypes(statedViewTypes);
    }

    public void setStatusMessage(String statusMessage) {
        frameConfig.setStatusMessage(statusMessage);
    }

    public void setSubversionToggleVisible(boolean visible) {
        frameConfig.setSubversionToggleVisible(visible);
    }

    public void setTogglesInComponentPanelVisible(TOGGLES toggle, boolean visible) {
        frameConfig.setTogglesInComponentPanelVisible(toggle, visible);
    }

    public void setTreeTermDividerLoc(int termTreeDividerLoc) {
        frameConfig.setTreeTermDividerLoc(termTreeDividerLoc);
    }

    public void setUsername(String username) {
        frameConfig.setUsername(username);
    }

    public void setVariableHeightTaxonomyView(Boolean variableHeightTaxonomyView) {
        frameConfig.setVariableHeightTaxonomyView(variableHeightTaxonomyView);
    }

    public void setVetoSupport(VetoableChangeSupport vetoSupport) {
        frameConfig.setVetoSupport(vetoSupport);
    }

    public void setViewPositions(Set<I_Position> positions) {
        frameConfig.setViewPositions(positions);
    }

    public void setWorker(MasterWorker worker) {
        frameConfig.setWorker(worker);
    }

    public void showListView() {
        frameConfig.showListView();
    }

    public void svnCheckout(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        frameConfig.svnCheckout(svd, authenticator, interactive);
    }

    public void svnCheckout(SubversionData svd) throws TaskFailedException {
        frameConfig.svnCheckout(svd);
    }

    public void svnCleanup(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        frameConfig.svnCleanup(svd, authenticator, interactive);
    }

    public void svnCleanup(SubversionData svd) throws TaskFailedException {
        frameConfig.svnCleanup(svd);
    }

    public void svnCommit(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        frameConfig.svnCommit(svd, authenticator, interactive);
    }

    public void svnCommit(SubversionData svd) throws TaskFailedException {
        frameConfig.svnCommit(svd);
    }

    public void svnCompleteRepoInfo(SubversionData svd) throws TaskFailedException {
        frameConfig.svnCompleteRepoInfo(svd);
    }

    public void svnImport(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        frameConfig.svnImport(svd, authenticator, interactive);
    }

    public void svnImport(SubversionData svd) throws TaskFailedException {
        frameConfig.svnImport(svd);
    }

    public List<String> svnList(SubversionData svd) throws TaskFailedException {
        return frameConfig.svnList(svd);
    }

    public void svnLock(SubversionData svd, File toLock, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        frameConfig.svnLock(svd, toLock, authenticator, interactive);
    }

    public void svnLock(SubversionData svd, File toLock) throws TaskFailedException {
        frameConfig.svnLock(svd, toLock);
    }

    public void svnPurge(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        frameConfig.svnPurge(svd, authenticator, interactive);
    }

    public void svnPurge(SubversionData svd) throws TaskFailedException {
        frameConfig.svnPurge(svd);
    }

    public void svnRevert(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        frameConfig.svnRevert(svd, authenticator, interactive);
    }

    public void svnRevert(SubversionData svd) throws TaskFailedException {
        frameConfig.svnRevert(svd);
    }

    public void svnStatus(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        frameConfig.svnStatus(svd, authenticator, interactive);
    }

    public void svnStatus(SubversionData svd) throws TaskFailedException {
        frameConfig.svnStatus(svd);
    }

    public void svnUnlock(SubversionData svd, File toUnlock, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        frameConfig.svnUnlock(svd, toUnlock, authenticator, interactive);
    }

    public void svnUnlock(SubversionData svd, File toUnLock) throws TaskFailedException {
        frameConfig.svnUnlock(svd, toUnLock);
    }

    public void svnUpdate(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        frameConfig.svnUpdate(svd, authenticator, interactive);
    }

    public void svnUpdate(SubversionData svd) throws TaskFailedException {
        frameConfig.svnUpdate(svd);
    }

    public void svnUpdateDatabase(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        frameConfig.svnUpdateDatabase(svd, authenticator, interactive);
    }

    public void svnUpdateDatabase(SubversionData svd) throws TaskFailedException {
        frameConfig.svnUpdateDatabase(svd);
    }

    public I_IntList getLanguagePreferenceList() {
        return frameConfig.getLanguagePreferenceList();
    }

    public void setRefsetInSpecEditor(I_GetConceptData refset) {
        frameConfig.setRefsetInSpecEditor(refset);
    }

    public LANGUAGE_SORT_PREF getLanguageSortPref() {
        return frameConfig.getLanguageSortPref();
    }

    public void setLanguageSortPref(LANGUAGE_SORT_PREF langSortPref) {
        frameConfig.setLanguageSortPref(langSortPref);
    }

    public I_IntSet getPrefFilterTypesForRel() {
        return frameConfig.getPrefFilterTypesForRel();
    }

    public I_DescriptionTuple getSearchResultsSelection() {
        return frameConfig.getSearchResultsSelection();
    }

    public void showRefsetSpecPanel() {
        frameConfig.showRefsetSpecPanel();
    }

    public void setShowActivityViewer(boolean show) {
        frameConfig.setShowActivityViewer(show);
    }

    public JPanel getWorkflowDetailsSheet() {
        return frameConfig.getWorkflowDetailsSheet();
    }

    public void setShowWorkflowDetailSheet(boolean visible) {
        frameConfig.setShowWorkflowDetailSheet(visible);
    }

    public void setWorkflowDetailSheetDimensions(Dimension dim) {
        frameConfig.setWorkflowDetailSheetDimensions(dim);
    }

    public Map<Integer, Color> getPathColorMap() {
        return frameConfig.getPathColorMap();
    }

    public Boolean getShowPathInfoInTaxonomy() {
        return frameConfig.getShowPathInfoInTaxonomy();
    }

    public void setShowPathInfoInTaxonomy(Boolean showPathInfoInTaxonomy) {
        frameConfig.setShowPathInfoInTaxonomy(showPathInfoInTaxonomy);
    }

    public void fireUpdateHierarchyView() {
        frameConfig.fireUpdateHierarchyView();
    }

    public I_ShowActivity getTopActivityListener() {
        return frameConfig.getTopActivityListener();
    }

    public boolean searchWithDescTypeFilter() {
        return frameConfig.searchWithDescTypeFilter();
    }

    public void setSearchWithDescTypeFilter(boolean filter) {
        frameConfig.setSearchWithDescTypeFilter(filter);
    }

    public void addPromotionPath(I_Path p) {
        frameConfig.addPromotionPath(p);
    }

    public Set<I_Path> getPromotionPathSet() {
        return frameConfig.getPromotionPathSet();
    }

    public void removePromotionPath(I_Path p) {
        frameConfig.removePromotionPath(p);
    }

    public void replacePromotionPathSet(I_Path oldPath, I_Path newPath) {
        frameConfig.replacePromotionPathSet(oldPath, newPath);
    }

    public void setSelectedPreferencesTab(String tabName) {
        frameConfig.setSelectedPreferencesTab(tabName);
    }

	public PositionSetReadOnly getViewPositionSetReadOnly() {
		return frameConfig.getViewPositionSetReadOnly();
	}

	public PathSetReadOnly getEditingPathSetReadOnly() {
		return frameConfig.getEditingPathSetReadOnly();
	}

	public PathSetReadOnly getPromotionPathSetReadOnly() {
		return frameConfig.getPromotionPathSetReadOnly();
	}
}
