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
package org.dwfa.ace.api;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;

import org.dwfa.ace.api.I_HostConceptPlugins.HOST_ENUM;
import org.dwfa.ace.api.I_HostConceptPlugins.REFSET_TYPES;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.gui.toptoggles.TopToggleTypes;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.data.SortedSetModel;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.tapi.TerminologyException;

public interface I_ConfigAceFrame extends I_HandleSubversion {

    public enum SPECIAL_SVN_ENTRIES {
        PROFILE_CSU();
    };

    public enum LANGUAGE_SORT_PREF {
        LANG_B4_TYPE("language before type"), TYPE_B4_LANG("type before language");

        private String desc;

        private LANGUAGE_SORT_PREF(String desc) {
            this.desc = desc;
        }

        public String toString() {
            return desc;
        }
    }

    public LANGUAGE_SORT_PREF getLanguageSortPref();

    public void setLanguageSortPref(LANGUAGE_SORT_PREF langSortPref);

    public boolean searchWithDescTypeFilter();

    public void setSearchWithDescTypeFilter(boolean filter);

    public boolean isActive();

    public void setActive(boolean active);

    public boolean isAdministrative();

    public void setAdministrative(boolean isAdministrative);

    public I_IntSet getAllowedStatus();

    public void setAllowedStatus(I_IntSet allowedStatus);

    /**
     * Description types for display when the concept view has the use
     * preferences toggle on.
     * 
     * @return
     */
    public I_IntSet getDescTypes();

    /**
     * Relationship types for display when the concept view has the use
     * preferences toggle on.
     * 
     * @return
     */
    public I_IntSet getPrefFilterTypesForRel();

    public void setDescTypes(I_IntSet allowedTypes);

    public String getFrameName();

    public void setFrameName(String frameName);

    public void setViewPositions(Set<I_Position> positions);

    public VetoableChangeSupport getVetoSupport();

    public void setVetoSupport(VetoableChangeSupport vetoSupport);

    public Rectangle getBounds();

    public void setBounds(Rectangle bounds);

    public I_IntSet getSourceRelTypes();

    public void setSourceRelTypes(I_IntSet browseDownRels);

    /**
     * Get the destination rel types that should be used to determine children
     * of a concept in a hierarchy display. These types are typically the is-a
     * relationship types.
     */
    public I_IntSet getDestRelTypes();

    public void setDestRelTypes(I_IntSet browseUpRels);

    public void addEditingPath(I_Path p);

    public void removeEditingPath(I_Path p);

    public void replaceEditingPath(I_Path oldPath, I_Path newPath);

    public Set<I_Path> getEditingPathSet();
    
    public PathSetReadOnly getEditingPathSetReadOnly();

    public void addPromotionPath(I_Path p);

    public void removePromotionPath(I_Path p);

    public void replacePromotionPathSet(I_Path oldPath, I_Path newPath);

    public Set<I_Path> getPromotionPathSet();

    public PathSetReadOnly getPromotionPathSetReadOnly();

    public void addViewPosition(I_Position p);

    public void removeViewPosition(I_Position p);

    public void replaceViewPosition(I_Position oldPosition, I_Position newPosition);

    public Set<I_Position> getViewPositionSet();
    
    public PositionSetReadOnly getViewPositionSetReadOnly();

    public I_IntSet getChildrenExpandedNodes();

    public I_IntSet getParentExpandedNodes();

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void fireCommit();

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

    public I_IntSet getRoots();

    public void setRoots(I_IntSet roots);

    public I_IntList getEditDescTypePopup();

    public I_IntList getEditImageTypePopup();

    public I_IntList getEditRelCharacteristicPopup();

    public I_IntList getEditRelRefinabiltyPopup();

    public I_IntList getEditRelTypePopup();

    public I_IntList getEditStatusTypePopup();

    public void setEditDescTypePopup(I_IntList editDescTypePopup);

    public void setEditImageTypePopup(I_IntList editImageTypePopup);

    public void setEditRelCharacteristicPopup(I_IntList editRelCharacteristicPopup);

    public void setEditRelRefinabiltyPopup(I_IntList editRelRefinabiltyPopup);

    public void setEditRelTypePopup(I_IntList editRelTypePopup);

    public void setEditStatusTypePopup(I_IntList editStatusTypePopup);

    public I_GetConceptData getDefaultImageType();

    public I_GetConceptData getDefaultDescriptionType();

    public void setDefaultDescriptionType(I_GetConceptData defaultDescriptionType);

    public void setDefaultImageType(I_GetConceptData defaultImageType);

    public I_GetConceptData getDefaultRelationshipCharacteristic();

    public void setDefaultRelationshipCharacteristic(I_GetConceptData defaultRelationshipCharacteristic);

    public I_GetConceptData getDefaultRelationshipRefinability();

    public void setDefaultRelationshipRefinability(I_GetConceptData defaultRelationshipRefinability);

    public I_GetConceptData getDefaultRelationshipType();

    public void setDefaultRelationshipType(I_GetConceptData defaultRelationshipType);

    public I_GetConceptData getDefaultStatus();

    public void setDefaultStatus(I_GetConceptData defaultStatus);

    public I_IntList getTreeDescPreferenceList();

    public I_IntList getTableDescPreferenceList();

    public I_IntList getLongLabelDescPreferenceList();

    public I_IntList getShortLabelDescPreferenceList();

    public int getTreeTermDividerLoc();

    public void setTreeTermDividerLoc(int termTreeDividerLoc);

    public I_GetConceptData getHierarchySelection();

    public void setHierarchySelection(I_GetConceptData hierarchySelection);

    public void setHierarchySelectionAndExpand(I_GetConceptData hierarchySelection) throws IOException;

    public MasterWorker getWorker();

    public void setWorker(MasterWorker worker);

    public String getStatusMessage();

    public void setStatusMessage(String statusMessage);

    public Collection<I_ReadChangeSet> getChangeSetReaders();

    public Collection<I_WriteChangeSet> getChangeSetWriters();

    public void setUsername(String username);

    public String getUsername();

    public void setPassword(String password);

    public String getPassword();

    public String getAdminPassword();

    public void setAdminPassword(String adminPassword);

    public String getAdminUsername();

    public void setAdminUsername(String adminUsername);

    public void setCommitEnabled(boolean enabled);

    public boolean isCommitEnabled();

    public void addUncommitted(I_GetConceptData conceptBean);

    public void removeUncommitted(I_GetConceptData uncommitted);

    public void addImported(I_GetConceptData conceptBean);

    public void setLastViewed(I_GetConceptData conceptBean);

    public I_GetConceptData getLastViewed();

    public JList getBatchConceptList();

    public I_HostConceptPlugins getConceptViewer(int index);

    public I_HostConceptPlugins getListConceptViewer();

    public void selectConceptViewer(int hostIndex);

    public SortedSetModel<String> getAddressesList();

    public List<String> getSelectedAddresses();

    public Map<String, SubversionData> getSubversionMap();

    public void setShowHierarchyView(boolean shown);

    public void showListView();

    public void showRefsetSpecPanel();

    public void setShowAddresses(boolean shown);

    public void setShowComponentView(boolean shown);

    public void setShowSearch(boolean shown);

    public void performLuceneSearch(String query, I_GetConceptData root);

    public void performLuceneSearch(String query, List<I_TestSearchResults> extraCriterion);

    public void setShowPreferences(boolean shown);

    public void setSelectedPreferencesTab(String tabName);

    public void setShowHistory(boolean shown);

    public void setShowAllQueues(boolean show);

    public void setShowQueueViewer(boolean show);

    public void setShowProcessBuilder(boolean show);

    public Collection<String> getQueueAddressesToShow();

    public JPanel getWorkflowPanel();

    public JPanel getSignpostPanel();

    public void setShowSignpostPanel(boolean show);

    public void setSignpostToggleVisible(boolean show);

    public void setSignpostToggleEnabled(boolean enabled);

    public void setSignpostToggleIcon(ImageIcon icon);

    public I_ConfigAceDb getDbConfig();

    public void setDbConfig(I_ConfigAceDb dbConfig);

    public void setPreferencesToggleVisible(boolean visible);

    public boolean isPreferencesToggleVisible();

    public void setSubversionToggleVisible(boolean visible);

    public boolean isSubversionToggleVisible();

    public void setBuilderToggleVisible(boolean visible);

    public boolean isBuilderToggleVisible();

    public void setInboxToggleVisible(boolean visible);

    public boolean isInboxToggleVisible();

    public void setComponentToggleVisible(boolean visible);

    public boolean isComponentToggleVisible();

    public void setHierarchyToggleVisible(boolean visible);

    public boolean isHierarchyToggleVisible();

    public void setAddressToggleVisible(boolean visible);

    public boolean isAddressToggleVisible();

    public void setHistoryToggleVisible(boolean visible);

    public boolean isHistoryToggleVisible();

    public void setTogglesInComponentPanelVisible(TOGGLES toggle, boolean visible);

    public boolean isToggleVisible(TOGGLES toggle);

    public I_HoldRefsetPreferences getRefsetPreferencesForToggle(TOGGLES toggle) throws TerminologyException,
            IOException;

    public void setRefsetInToggleVisible(REFSET_TYPES refsetType, TOGGLES toggle, boolean visible);

    public boolean isRefsetInToggleVisible(REFSET_TYPES refsetType, TOGGLES toggle);

    public void setCommitAbortButtonsVisible(boolean visible);

    public Map<TOGGLES, I_HoldRefsetPreferences> getRefsetPreferencesMap();

    public I_IntList getRefsetsToShowInTaxonomy();

    public boolean getShowViewerImagesInTaxonomy();

    public void setShowViewerImagesInTaxonomy(Boolean showViewerImagesInTaxonomy);

    public Boolean getVariableHeightTaxonomyView();

    public void setVariableHeightTaxonomyView(Boolean variableHeightTaxonomyView);

    /**
     * 
     * @return
     * @deprecated view on the classifier path instead...
     */
    public Boolean getShowInferredInTaxonomy();

    /**
     * 
     * @deprecated view on the classifier path instead...
     */
    public void setShowInferredInTaxonomy(Boolean showInferredInTaxonomy);

    public Boolean getShowPathInfoInTaxonomy();

    public void setShowPathInfoInTaxonomy(Boolean showPathInfoInTaxonomy);

    public Boolean getShowRefsetInfoInTaxonomy();

    public void setShowRefsetInfoInTaxonomy(Boolean showRefsetInfoInTaxonomy);

    public I_IntList getRefsetsToSortTaxonomy();

    public Boolean getSortTaxonomyUsingRefset();

    public void setSortTaxonomyUsingRefset(Boolean sortTaxonomyUsingRefset);

    /**
     * Processes can add and remove I_OverrideTaxonomyRenderer objects.
     * 
     * @return
     */
    public List<I_OverrideTaxonomyRenderer> getTaxonomyRendererOverrideList();

    /**
     * Processes can add and remove I_FilterTaxonomyRels objects.
     * 
     * @return
     */
    public List<I_FilterTaxonomyRels> getTaxonomyRelFilterList();

    /**
     * For storing history of concepts viewed by each component viewer.
     * 
     * @return
     */
    public Map<String, List<I_GetConceptData>> getTabHistoryMap();

    public void setFrameVisible(boolean visible) throws Exception;

    public void closeFrame();

    public Set<TopToggleTypes> getHiddenTopToggles();

    public void setContext(I_GetConceptData context);

    public I_GetConceptData getContext();

    public BundleType getBundleType();

    /**
     * @return the conflict resolution strategy in use by the profile
     */
    I_ManageConflict getConflictResolutionStrategy();

    /**
     * Sets the conflict resolution strategy for this profile
     * 
     * @param conflictResolutionStrategy
     */
    void setConflictResolutionStrategy(I_ManageConflict conflictResolutionStrategy);

    /**
     * Sets the conflict resolution strategy for this profile
     * 
     * @param conflictResolutionStrategy
     */
    public <T extends I_ManageConflict> void setConflictResolutionStrategy(Class<T> conflictResolutionStrategyClass);

    public Boolean getHighlightConflictsInTaxonomyView();

    public void setHighlightConflictsInTaxonomyView(Boolean highlightConflictsInTaxonomyView);

    public Boolean getHighlightConflictsInComponentPanel();

    public void setHighlightConflictsInComponentPanel(Boolean highlightConflictsInComponentPanel);

    public I_GetConceptData getRefsetInSpecEditor();

    public void setRefsetInSpecEditor(I_GetConceptData refset);

    public I_GetConceptData getRefsetSpecInSpecEditor() throws IOException, TerminologyException;

    public JTree getTreeInSpecEditor();

    public I_ThinExtByRefVersioned getSelectedRefsetClauseInSpecEditor();

    public JTree getTreeInTaxonomyPanel();

    // Configuration items to support the classifier.
    public I_GetConceptData getClassificationRoot();

    public void setClassificationRoot(I_GetConceptData classificationRoot);

    public I_GetConceptData getClassificationRoleRoot();

    public void setClassificationRoleRoot(I_GetConceptData classificationRoleRoot);

    public I_GetConceptData getClassifierIsaType();

    public void setClassifierIsaType(I_GetConceptData classifierIsaType);

    public I_GetConceptData getClassifierInputPath();

    public void setClassifierInputPath(I_GetConceptData inputPath);

    public I_GetConceptData getClassifierOutputPath();

    public void setClassifierOutputPath(I_GetConceptData outputPath);

    public I_ManageConflict[] getAllConflictResolutionStrategies();

    public I_ShowActivity getTopActivityListener();

    /**
     * Shows or hides as the activity viewer.
     * 
     * @param show Whether to show the activity viewer.
     * @return
     */
    public void setShowActivityViewer(boolean show);

    public Color getColorForPath(int pathNid);

    public void setColorForPath(int pathNid, Color pathColor);

    public I_IntList getLanguagePreferenceList();

    public void invalidate();

    public void validate();

    public void repaint();

    public Object getProperty(String key) throws IOException;

    public void setProperty(String key, Object value) throws IOException;

    public Map<String, Object> getProperties() throws IOException;

    public void addConceptPanelPlugins(HOST_ENUM host, UUID id, I_PluginToConceptPanel plugin);

    public I_PluginToConceptPanel removeConceptPanelPlugin(HOST_ENUM host, UUID id);

    public Set<UUID> getConceptPanelPluginKeys(HOST_ENUM host);

    public I_PluginToConceptPanel getConceptPanelPlugin(HOST_ENUM host, UUID id);

    public Collection<I_PluginToConceptPanel> getConceptPanelPlugins(HOST_ENUM host);

    /**
     * 
     * @return A list of the default concept panel plugins for editing, that may
     *         be used to "reset the frame to defaults", or initialize a frame
     *         configuration. This list is static, and has no relationship to
     *         the current set of plugins for this frame.
     */
    public List<I_PluginToConceptPanel> getDefaultConceptPanelPluginsForEditor();

    /**
     * 
     * @return A list of the default concept panel plugins for a viewer, that
     *         may be used to "reset the frame to defaults", or initialize a
     *         frame configuration. This list is static, and has no relationship
     *         to the current set of plugins for this frame.
     */
    public List<I_PluginToConceptPanel> getDefaultConceptPanelPluginsForViewer();

    public void fireRefsetSpecChanged(I_ThinExtByRefVersioned ext);

    public I_DescriptionTuple getSearchResultsSelection();

    /**
     * 
     * @param visible True if you wish to make the workflow details sheet
     *            visible. Otherwise false.
     */
    public void setShowWorkflowDetailSheet(boolean visible);

    /**
     * 
     * @param dim dimensions of the workflow dimension sheet.
     */
    public void setWorkflowDetailSheetDimensions(Dimension dim);

    /**
     * Developers can place components on the details sheet for interaction with
     * the user.
     * 
     * @return The JPanel that implements the workflow details sheet.
     */
    public JPanel getWorkflowDetailsSheet();

    /**
     * 
     * @return a map of the path nids, and the color associated with that path.
     */
    public Map<Integer, Color> getPathColorMap();

    /**
     * Redraw the hierarchy view
     */
    public void fireUpdateHierarchyView();
}
