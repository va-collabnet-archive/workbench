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
package org.dwfa.ace.search;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.dwfa.ace.api.I_OverrideTaxonomyRenderer;
import org.dwfa.ace.api.I_PluginToConceptPanel;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.api.I_HostConceptPlugins.HOST_ENUM;
import org.dwfa.ace.api.I_HostConceptPlugins.REFSET_TYPES;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.task.gui.toptoggles.TopToggleTypes;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.data.SortedSetModel;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.descriptionlogic.DescriptionLogic;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.PromptUserPassword3;

public class DifferenceSearchConfig implements I_ConfigAceFrame {

    I_ConfigAceFrame frameConfig;

   public EditCoordinate getEditCoordinate() {
      return frameConfig.getEditCoordinate();
   }

    @Override
    public void setRelAssertionType(RelAssertionType relAssertionType) {
        frameConfig.setRelAssertionType(relAssertionType);
    }

    @Override
    public RelAssertionType getRelAssertionType() {
        return frameConfig.getRelAssertionType();
    }

   @Override
    public void setClassifierConcept(I_GetConceptData classifierConcept) {
        frameConfig.setClassifierConcept(classifierConcept);
    }

    @Override
    public I_GetConceptData getClassifierConcept() {
        return frameConfig.getClassifierConcept();
    }

    @Override
	public void quit() {
		frameConfig.quit();
	}

    @Override
	public ViewCoordinate getViewCoordinate() {
		return frameConfig.getViewCoordinate();
	}

	public Set<PathBI> getPromotionPathSet() {
		return frameConfig.getPromotionPathSet();
	}

	public void addViewPosition(PositionBI p) {
		frameConfig.addViewPosition(p);
	}

	public void removeViewPosition(PositionBI p) {
		frameConfig.removeViewPosition(p);
	}

    public void replaceViewPosition(PositionBI oldPosition, PositionBI newPosition) {
		frameConfig.replaceViewPosition(oldPosition, newPosition);
	}

	public I_ShowActivity getTopActivity() {
        return frameConfig.getTopActivity();
    }

    public void setTopActivity(I_ShowActivity activity) {
        frameConfig.setTopActivity(activity);
    }

    public Precedence getPrecedence() {
        return frameConfig.getPrecedence();
    }

    public void setPrecedence(Precedence precedence) {
        frameConfig.setPrecedence(precedence);
    }

    public void refreshRefsetTab() {
        frameConfig.refreshRefsetTab();
    }

    Set<PositionBI> positionSet;

    public DifferenceSearchConfig(I_ConfigAceFrame frameConfig, Set<PositionBI> positionSet) {
        super();
        this.frameConfig = frameConfig;
        this.positionSet = positionSet;
    }

    public void addConceptPanelPlugins(HOST_ENUM host, UUID id, I_PluginToConceptPanel plugin) {
        frameConfig.addConceptPanelPlugins(host, id, plugin);
    }

    public void addEditingPath(PathBI p) {
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

    public void fireRefsetSpecChanged(I_ExtendByRef ext) {
        frameConfig.fireRefsetSpecChanged(ext);
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

    public ContradictionManagerBI[] getAllConflictResolutionStrategies() {
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
        return frameConfig.getChildrenExpandedNodes();
    }

    public I_GetConceptData getClassificationRoleRoot() {
        return frameConfig.getClassificationRoleRoot();
    }

    public I_GetConceptData getClassificationRoot() {
        return frameConfig.getClassificationRoot();
    }

    public CLASSIFIER_INPUT_MODE_PREF getClassifierInputMode() {
        return frameConfig.getClassifierInputMode();
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

    public Boolean getClassifierOwlFeatureStatus() {
        return frameConfig.getClassifierOwlFeatureStatus();
    }

    public Color getColorForPath(int pathNid) {
        return frameConfig.getColorForPath(pathNid);
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

    public I_HostConceptPlugins getConceptViewer(int index) {
        return frameConfig.getConceptViewer(index);
    }

    public ContradictionManagerBI getConflictResolutionStrategy() {
        return frameConfig.getConflictResolutionStrategy();
    }

    public I_GetConceptData getContext() {
        return frameConfig.getContext();
    }

    public I_ConfigAceDb getDbConfig() {
        return frameConfig.getDbConfig();
    }

    public List<I_PluginToConceptPanel> getDefaultConceptPanelPluginsForEditor() {
        return frameConfig.getDefaultConceptPanelPluginsForEditor();
    }

    public List<I_PluginToConceptPanel> getDefaultConceptPanelPluginsForViewer() {
        return frameConfig.getDefaultConceptPanelPluginsForViewer();
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

    public Set<PathBI> getEditingPathSet() {
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

    public I_IntList getLanguagePreferenceList() {
        return frameConfig.getLanguagePreferenceList();
    }

    public LANGUAGE_SORT_PREF getLanguageSortPref() {
        return frameConfig.getLanguageSortPref();
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

    public Map<Integer, Color> getPathColorMap() {
        return frameConfig.getPathColorMap();
    }

    public I_IntSet getPrefFilterTypesForRel() {
        return frameConfig.getPrefFilterTypesForRel();
    }

    public Map<String, Object> getProperties() throws IOException {
        return frameConfig.getProperties();
    }

    public Object getProperty(String key) throws IOException {
        return frameConfig.getProperty(key);
    }

    public Collection<String> getQueueAddressesToShow() {
        return frameConfig.getQueueAddressesToShow();
    }

    public I_GetConceptData getRefsetInSpecEditor() {
        return frameConfig.getRefsetInSpecEditor();
    }

    public I_HoldRefsetPreferences getRefsetPreferencesForToggle(TOGGLES toggle) throws TerminologyException, IOException {
        return frameConfig.getRefsetPreferencesForToggle(toggle);
    }

    public Map<TOGGLES, I_HoldRefsetPreferences> getRefsetPreferencesMap() {
        return frameConfig.getRefsetPreferencesMap();
    }

    public I_GetConceptData getRefsetSpecInSpecEditor() throws IOException, TerminologyException {
        return frameConfig.getRefsetSpecInSpecEditor();
    }

    public I_IntList getRefsetsToShowInTaxonomy() {
        return frameConfig.getRefsetsToShowInTaxonomy();
    }

    public I_IntList getRefsetsToSortTaxonomy() {
        return frameConfig.getRefsetsToSortTaxonomy();
    }

    public I_IntSet getRoots() {
        return frameConfig.getRoots();
    }

    public I_DescriptionTuple getSearchResultsSelection() {
        return frameConfig.getSearchResultsSelection();
    }

    public List<String> getSelectedAddresses() {
        return frameConfig.getSelectedAddresses();
    }

    public I_ExtendByRef getSelectedRefsetClauseInSpecEditor() {
        return frameConfig.getSelectedRefsetClauseInSpecEditor();
    }

    public I_IntList getShortLabelDescPreferenceList() {
        return frameConfig.getShortLabelDescPreferenceList();
    }

    public Boolean getShowRefsetInfoInTaxonomy() {
        return frameConfig.getShowRefsetInfoInTaxonomy();
    }

    public boolean getShowViewerImagesInTaxonomy() {
        return frameConfig.getShowViewerImagesInTaxonomy();
    }

    public JPanel getSignpostPanel() {
        return frameConfig.getSignpostPanel();
    }

    public Boolean getSortTaxonomyUsingRefset() {
        return frameConfig.getSortTaxonomyUsingRefset();
    }

    public I_IntSet getSourceRelTypes() {
        return frameConfig.getSourceRelTypes();
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

    public List<I_FilterTaxonomyRels> getTaxonomyRelFilterList() {
        return frameConfig.getTaxonomyRelFilterList();
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

    public JTree getTreeInTaxonomyPanel() {
        return frameConfig.getTreeInTaxonomyPanel();
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

    public Set<PositionBI> getViewPositionSet() {
        return positionSet;
    }

    public MasterWorker getWorker() {
        return frameConfig.getWorker();
    }

    public JPanel getWorkflowDetailsSheet() {
        return frameConfig.getWorkflowDetailsSheet();
    }

    public JPanel getWorkflowPanel() {
        return frameConfig.getWorkflowPanel();
    }

    public void invalidate() {
        frameConfig.invalidate();
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

    public I_PluginToConceptPanel removeConceptPanelPlugin(HOST_ENUM host, UUID id) {
        return frameConfig.removeConceptPanelPlugin(host, id);
    }

    public void removeEditingPath(PathBI p) {
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

    public void repaint() {
        frameConfig.repaint();
    }

    public void replaceEditingPath(PathBI oldPath, PathBI newPath) {
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
        frameConfig.setClassificationRoot(classificationRoleRoot);
    }

    public void setClassificationRoot(I_GetConceptData classificationRoot) {
        frameConfig.setClassificationRoot(classificationRoot);
    }

    public void setClassifierInputMode(CLASSIFIER_INPUT_MODE_PREF classifierInputMode) {
        frameConfig.setClassifierInputMode(classifierInputMode);
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

    public void setClassifierOwlFeatureStatus(Boolean classifierOwlFeatureStatus) { // :SNOOWL:
        DescriptionLogic.setVisible(classifierOwlFeatureStatus);
        frameConfig.setClassifierOwlFeatureStatus(classifierOwlFeatureStatus);
    }

    public void setColorForPath(int pathNid, Color pathColor) {
        frameConfig.setColorForPath(pathNid, pathColor);
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

    public <T extends ContradictionManagerBI> void setConflictResolutionStrategy(Class<T> conflictResolutionStrategyClass) {
        frameConfig.setConflictResolutionStrategy(conflictResolutionStrategyClass);
    }

    public void setConflictResolutionStrategy(ContradictionManagerBI conflictResolutionStrategy) {
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
        //frameConfig.setHierarchySelection(hierarchySelection);
    }

    public void setHierarchySelectionAndExpand(I_GetConceptData hierarchySelection) throws IOException {
        //frameConfig.setHierarchySelectionAndExpand(hierarchySelection);
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

    public void setLanguageSortPref(LANGUAGE_SORT_PREF langSortPref) {
        frameConfig.setLanguageSortPref(langSortPref);
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

    public void setProperty(String key, Object value) throws IOException {
        frameConfig.setProperty(key, value);
    }

    public void setRefsetInSpecEditor(I_GetConceptData refset) {
        frameConfig.setRefsetInSpecEditor(refset);
    }

    public void setRefsetInToggleVisible(REFSET_TYPES refsetType, TOGGLES toggle, boolean visible) {
        frameConfig.setRefsetInToggleVisible(refsetType, toggle, visible);
    }

    public void setRoots(I_IntSet roots) {
        frameConfig.setRoots(roots);
    }

    public void setShowActivityViewer(boolean show) {
        frameConfig.setShowActivityViewer(show);
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
        frameConfig.setShowRefsetInfoInTaxonomy(showRefsetInfoInTaxonomy);
    }

    public void setShowSearch(boolean shown) {
        frameConfig.setShowSearch(shown);
    }

    public void setShowSignpostPanel(boolean show) {
        frameConfig.setShowSignpostPanel(show);
    }

    public void setShowWorkflowSignpostPanel(boolean show) {
    	frameConfig.setShowWorkflowSignpostPanel(show);
    }

    public void setShowViewerImagesInTaxonomy(Boolean showViewerImagesInTaxonomy) {
        frameConfig.setShowViewerImagesInTaxonomy(showViewerImagesInTaxonomy);
    }

    public void setShowWorkflowDetailSheet(boolean visible) {
        frameConfig.setShowWorkflowDetailSheet(visible);
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
        frameConfig.setSortTaxonomyUsingRefset(sortTaxonomyUsingRefset);
    }

    public void setSourceRelTypes(I_IntSet browseDownRels) {
        frameConfig.setSourceRelTypes(browseDownRels);
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

    public void setViewPositions(Set<PositionBI> positions) {
        frameConfig.setViewPositions(positions);
    }

    public void setWorker(MasterWorker worker) {
        frameConfig.setWorker(worker);
    }

    public void setWorkflowDetailSheetDimensions(Dimension dim) {
        frameConfig.setWorkflowDetailSheetDimensions(dim);
    }

    public void showListView() {
        frameConfig.showListView();
    }

    public void showRefsetSpecPanel() {
        frameConfig.showRefsetSpecPanel();
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

    public List<String> svnList(SubversionData svd) throws TaskFailedException, ClientException {
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
            throws TaskFailedException, ClientException {
        frameConfig.svnUnlock(svd, toUnlock, authenticator, interactive);
    }

    public void svnUnlock(SubversionData svd, File toUnLock) throws TaskFailedException, ClientException {
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

    public void validate() {
        frameConfig.validate();
    }

    public void fireUpdateHierarchyView() {
        frameConfig.fireUpdateHierarchyView();
    }

    public Boolean getShowPathInfoInTaxonomy() {
        return frameConfig.getShowPathInfoInTaxonomy();
    }

    public void setShowPathInfoInTaxonomy(Boolean showPathInfoInTaxonomy) {
        frameConfig.setShowPathInfoInTaxonomy(showPathInfoInTaxonomy);
    }

    public boolean searchWithDescTypeFilter() {
        return frameConfig.searchWithDescTypeFilter();
    }

    public void setSearchWithDescTypeFilter(boolean filter) {
        frameConfig.setSearchWithDescTypeFilter(filter);
    }

    public void addPromotionPath(PathBI p) {
        frameConfig.addPromotionPath(p);
    }

    public void removePromotionPath(PathBI p) {
        frameConfig.removePromotionPath(p);
    }

    public void replacePromotionPathSet(PathBI oldPath, PathBI newPath) {
        frameConfig.replacePromotionPathSet(oldPath, newPath);
    }

    public void setSelectedPreferencesTab(String tabName) {
        frameConfig.setSelectedPreferencesTab(tabName);
    }

    @Override
    public PositionSetReadOnly getViewPositionSetReadOnly() {
        return new PositionSetReadOnly(positionSet);
    }

    public PathSetReadOnly getEditingPathSetReadOnly() {
        return frameConfig.getEditingPathSetReadOnly();
    }

    public PathSetReadOnly getPromotionPathSetReadOnly() {
        return frameConfig.getPromotionPathSetReadOnly();
    }

    @Override
    public Boolean getShowPromotionCheckBoxes() {
        return frameConfig.getShowPromotionCheckBoxes();
    }

    @Override
    public void setShowPromotionCheckBoxes(Boolean show) {
        frameConfig.setShowPromotionCheckBoxes(show);
    }

    @Override
    public Boolean getShowPromotionFilters() {
        return frameConfig.getShowPromotionFilters();
    }

    @Override
    public Boolean getShowPromotionTab() {
        return frameConfig.getShowPromotionTab();
    }

    @Override
    public void setShowPromotionFilters(Boolean show) {
        frameConfig.setShowPromotionFilters(show);
    }

    @Override
    public void setShowPromotionTab(Boolean show) {
        frameConfig.setShowPromotionTab(show);
    }


	@Override
	public boolean isAutoApproveOn() {
		return frameConfig.isAutoApproveOn();
	}
    @Override
    public void setEnabledAllQueuesButton(boolean enable) {
        frameConfig.setEnabledAllQueuesButton(enable);
    }

    @Override
    public void setEnabledExistingInboxButton(boolean enable) {
        frameConfig.setEnabledExistingInboxButton(enable);
    }

    @Override
    public void setEnabledMoveListenerButton(boolean enable) {
        frameConfig.setEnabledMoveListenerButton(enable);
    }

    @Override
    public void setEnabledNewInboxButton(boolean enable) {
        frameConfig.setEnabledNewInboxButton(enable);
    }

	@Override
	public boolean isOverrideOn() {
		return frameConfig.isOverrideOn();
	}


	@Override
	public void setAutoApprove(boolean b) {
		frameConfig.setAutoApprove(b);
	}


	@Override
	public void setOverride(boolean b) {
		frameConfig.setOverride(b);
	}

	@Override
	public TreeSet<? extends ConceptVersionBI> getWorkflowRoles() {
		return frameConfig.getWorkflowRoles();
	}

	@Override
	public void setWorkflowRoles(TreeSet<? extends ConceptVersionBI> roles) {
		frameConfig.setWorkflowRoles(roles);
	}

	@Override
	public TreeSet<? extends ConceptVersionBI> getWorkflowStates() {
		return frameConfig.getWorkflowStates();
	}

	@Override
	public void setWorkflowStates(TreeSet<? extends ConceptVersionBI> states) {
		frameConfig.setWorkflowStates(states);
	}

	@Override
	public TreeSet<? extends ConceptVersionBI> getWorkflowActions() {
		return frameConfig.getWorkflowActions();
	}

	@Override
	public void setWorkflowActions(TreeSet<? extends ConceptVersionBI> actions) {
		frameConfig.setWorkflowActions(actions);
	}


	@Override
	public List<UUID> getAllAvailableWorkflowActionUids() {
		return frameConfig.getAllAvailableWorkflowActionUids();
	}

	@Override
	public void setAllAvailableWorkflowActionUids(List<UUID> actions) {
		frameConfig.setAllAvailableWorkflowActionUids(actions);
	}

    @Override
    public void setModuleNid(int moduleNid) {
        frameConfig.setModuleNid(moduleNid);
    }

	@Override
	public void setDefaultProjectForChangedConcept(I_GetConceptData project) {
		frameConfig.setDefaultProjectForChangedConcept(project);
	}

	@Override
	public I_GetConceptData getDefaultProjectForChangedConcept() {
		return frameConfig.getDefaultProjectForChangedConcept();
	}

	@Override
	public void setDefaultWorkflowForChangedConcept(I_GetConceptData workflow) {
		frameConfig.setDefaultWorkflowForChangedConcept(workflow);
	}

	@Override
	public I_GetConceptData getDefaultWorkflowForChangedConcept() {
		return frameConfig.getDefaultWorkflowForChangedConcept();
	}

}
