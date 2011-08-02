/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ihtsdo.arena.contradiction;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_OverrideTaxonomyRenderer;
import org.dwfa.ace.api.I_PluginToConceptPanel;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_HostConceptPlugins.HOST_ENUM;
import org.dwfa.ace.api.I_HostConceptPlugins.REFSET_TYPES;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.task.gui.toptoggles.TopToggleTypes;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.data.SortedSetModel;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.PropertyChangeSupportWithPropagationId;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.PromptUserPassword3;

/**
 *
 * @author kec
 */
public class ContradictionConfig implements I_ConfigAceFrame {
    
    ContradictionEditorFrame editor;
    I_ConfigAceFrame config;

    public ContradictionConfig(ContradictionEditorFrame editor, I_ConfigAceFrame config) {
        this.editor = editor;
        this.config = config;
    }

    public void svnUpdateDatabase(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException {
        config.svnUpdateDatabase(svd, authenticator, interactive);
    }

    public void svnUpdateDatabase(SubversionData svd) throws TaskFailedException {
        config.svnUpdateDatabase(svd);
    }

    public void svnUpdate(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException {
        config.svnUpdate(svd, authenticator, interactive);
    }

    public void svnUpdate(SubversionData svd) throws TaskFailedException {
        config.svnUpdate(svd);
    }

    public void svnUnlock(SubversionData svd, File toUnlock, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException, ClientException {
        config.svnUnlock(svd, toUnlock, authenticator, interactive);
    }

    public void svnUnlock(SubversionData svd, File toUnLock) throws TaskFailedException, ClientException {
        config.svnUnlock(svd, toUnLock);
    }

    public void svnStatus(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException {
        config.svnStatus(svd, authenticator, interactive);
    }

    public void svnStatus(SubversionData svd) throws TaskFailedException {
        config.svnStatus(svd);
    }

    public void svnRevert(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException {
        config.svnRevert(svd, authenticator, interactive);
    }

    public void svnRevert(SubversionData svd) throws TaskFailedException {
        config.svnRevert(svd);
    }

    public void svnPurge(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException {
        config.svnPurge(svd, authenticator, interactive);
    }

    public void svnPurge(SubversionData svd) throws TaskFailedException {
        config.svnPurge(svd);
    }

    public void svnLock(SubversionData svd, File toLock, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException {
        config.svnLock(svd, toLock, authenticator, interactive);
    }

    public void svnLock(SubversionData svd, File toLock) throws TaskFailedException {
        config.svnLock(svd, toLock);
    }

    public List<String> svnList(SubversionData svd) throws TaskFailedException, ClientException {
        return config.svnList(svd);
    }

    public void svnImport(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException {
        config.svnImport(svd, authenticator, interactive);
    }

    public void svnImport(SubversionData svd) throws TaskFailedException {
        config.svnImport(svd);
    }

    public void svnCompleteRepoInfo(SubversionData svd) throws TaskFailedException {
        config.svnCompleteRepoInfo(svd);
    }

    public void svnCommit(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException {
        config.svnCommit(svd, authenticator, interactive);
    }

    public void svnCommit(SubversionData svd) throws TaskFailedException {
        config.svnCommit(svd);
    }

    public void svnCleanup(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException {
        config.svnCleanup(svd, authenticator, interactive);
    }

    public void svnCleanup(SubversionData svd) throws TaskFailedException {
        config.svnCleanup(svd);
    }

    public void svnCheckout(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException {
        config.svnCheckout(svd, authenticator, interactive);
    }

    public void svnCheckout(SubversionData svd) throws TaskFailedException {
        config.svnCheckout(svd);
    }

    public void validate() {
        config.validate();
    }

    public void showRefsetSpecPanel() {
        config.showRefsetSpecPanel();
    }

    public void showListView() {
        config.showListView();
    }

    public void setWorkflowStates(TreeSet<? extends ConceptVersionBI> states) {
        config.setWorkflowStates(states);
    }

    public void setWorkflowRoles(TreeSet<? extends ConceptVersionBI> roles) {
        config.setWorkflowRoles(roles);
    }

    public void setWorkflowDetailSheetDimensions(Dimension dim) {
        config.setWorkflowDetailSheetDimensions(dim);
    }

    public void setWorkflowActions(TreeSet<? extends ConceptVersionBI> actions) {
        config.setWorkflowActions(actions);
    }

    public void setWorker(MasterWorker worker) {
        config.setWorker(worker);
    }

    public void setViewPositions(Set<PositionBI> positions) {
        config.setViewPositions(positions);
    }

    public void setVetoSupport(VetoableChangeSupport vetoSupport) {
        config.setVetoSupport(vetoSupport);
    }

    public void setVariableHeightTaxonomyView(Boolean variableHeightTaxonomyView) {
        config.setVariableHeightTaxonomyView(variableHeightTaxonomyView);
    }

    public void setUsername(String username) {
        config.setUsername(username);
    }

    public void setTreeTermDividerLoc(int termTreeDividerLoc) {
        config.setTreeTermDividerLoc(termTreeDividerLoc);
    }

    public void setTopActivity(I_ShowActivity activity) {
        config.setTopActivity(activity);
    }

    public void setTogglesInComponentPanelVisible(TOGGLES toggle, boolean visible) {
        config.setTogglesInComponentPanelVisible(toggle, visible);
    }

    public void setSubversionToggleVisible(boolean visible) {
        config.setSubversionToggleVisible(visible);
    }

    public void setStatusMessage(String statusMessage) {
        config.setStatusMessage(statusMessage);
    }

    public void setSourceRelTypes(I_IntSet browseDownRels) {
        config.setSourceRelTypes(browseDownRels);
    }

    public void setSortTaxonomyUsingRefset(Boolean sortTaxonomyUsingRefset) {
        config.setSortTaxonomyUsingRefset(sortTaxonomyUsingRefset);
    }

    public void setSignpostToggleVisible(boolean show) {
        config.setSignpostToggleVisible(show);
    }

    public void setSignpostToggleIcon(ImageIcon icon) {
        config.setSignpostToggleIcon(icon);
    }

    public void setSignpostToggleEnabled(boolean enabled) {
        config.setSignpostToggleEnabled(enabled);
    }

    public void setShowWorkflowSignpostPanel(boolean show) {
        config.setShowWorkflowSignpostPanel(show);
    }

    public void setShowWorkflowDetailSheet(boolean visible) {
        config.setShowWorkflowDetailSheet(visible);
    }

    public void setShowViewerImagesInTaxonomy(Boolean showViewerImagesInTaxonomy) {
        config.setShowViewerImagesInTaxonomy(showViewerImagesInTaxonomy);
    }

    public void setShowSignpostPanel(boolean show) {
        config.setShowSignpostPanel(show);
    }

    public void setShowSearch(boolean shown) {
        config.setShowSearch(shown);
    }

    public void setShowRefsetInfoInTaxonomy(Boolean showRefsetInfoInTaxonomy) {
        config.setShowRefsetInfoInTaxonomy(showRefsetInfoInTaxonomy);
    }

    public void setShowQueueViewer(boolean show) {
        config.setShowQueueViewer(show);
    }

    public void setShowPromotionTab(Boolean show) {
        config.setShowPromotionTab(show);
    }

    public void setShowPromotionFilters(Boolean show) {
        config.setShowPromotionFilters(show);
    }

    public void setShowPromotionCheckBoxes(Boolean show) {
        config.setShowPromotionCheckBoxes(show);
    }

    public void setShowProcessBuilder(boolean show) {
        config.setShowProcessBuilder(show);
    }

    public void setShowPreferences(boolean shown) {
        config.setShowPreferences(shown);
    }

    public void setShowPathInfoInTaxonomy(Boolean showPathInfoInTaxonomy) {
        config.setShowPathInfoInTaxonomy(showPathInfoInTaxonomy);
    }

    public void setShowHistory(boolean shown) {
        config.setShowHistory(shown);
    }

    public void setShowHierarchyView(boolean shown) {
        config.setShowHierarchyView(shown);
    }

    public void setShowComponentView(boolean shown) {
        config.setShowComponentView(shown);
    }

    public void setShowAllQueues(boolean show) {
        config.setShowAllQueues(show);
    }

    public void setShowAddresses(boolean shown) {
        config.setShowAddresses(shown);
    }

    public void setShowActivityViewer(boolean show) {
        config.setShowActivityViewer(show);
    }

    public void setSelectedPreferencesTab(String tabName) {
        config.setSelectedPreferencesTab(tabName);
    }

    public void setSearchWithDescTypeFilter(boolean filter) {
        config.setSearchWithDescTypeFilter(filter);
    }

    public void setRoots(I_IntSet roots) {
        config.setRoots(roots);
    }

    public void setRelAssertionType(RelAssertionType relAssertionType) {
        config.setRelAssertionType(relAssertionType);
    }

    public void setRefsetInToggleVisible(REFSET_TYPES refsetType, TOGGLES toggle, boolean visible) {
        config.setRefsetInToggleVisible(refsetType, toggle, visible);
    }

    public void setRefsetInSpecEditor(I_GetConceptData refset) {
        config.setRefsetInSpecEditor(refset);
    }

    public void setProperty(String key, Object value) throws IOException {
        config.setProperty(key, value);
    }

    public void setPreferencesToggleVisible(boolean visible) {
        config.setPreferencesToggleVisible(visible);
    }

    public void setPrecedence(Precedence precedence) {
        config.setPrecedence(precedence);
    }

    public void setPassword(String password) {
        config.setPassword(password);
    }

    public void setOverride(boolean b) {
        config.setOverride(b);
    }

    public void setLastViewed(I_GetConceptData conceptBean) {
        config.setLastViewed(conceptBean);
    }

    public void setLanguageSortPref(LANGUAGE_SORT_PREF langSortPref) {
        config.setLanguageSortPref(langSortPref);
    }

    public void setInboxToggleVisible(boolean visible) {
        config.setInboxToggleVisible(visible);
    }

    public void setHistoryToggleVisible(boolean visible) {
        config.setHistoryToggleVisible(visible);
    }

    public void setHighlightConflictsInTaxonomyView(Boolean highlightConflictsInTaxonomyView) {
        config.setHighlightConflictsInTaxonomyView(highlightConflictsInTaxonomyView);
    }

    public void setHighlightConflictsInComponentPanel(Boolean highlightConflictsInComponentPanel) {
        config.setHighlightConflictsInComponentPanel(highlightConflictsInComponentPanel);
    }

    public void setHierarchyToggleVisible(boolean visible) {
        config.setHierarchyToggleVisible(visible);
    }

    public void setHierarchySelectionAndExpand(I_GetConceptData hierarchySelection) throws IOException {
        //config.setHierarchySelectionAndExpand(hierarchySelection);
    }

    public void setHierarchySelection(I_GetConceptData hierarchySelection) {
        //config.setHierarchySelection(hierarchySelection);
    }

    public void setFrameVisible(boolean visible) throws Exception {
        config.setFrameVisible(visible);
    }

    public void setFrameName(String frameName) {
        config.setFrameName(frameName);
    }

    public void setEnabledNewInboxButton(boolean enable) {
        config.setEnabledNewInboxButton(enable);
    }

    public void setEnabledMoveListenerButton(boolean enable) {
        config.setEnabledMoveListenerButton(enable);
    }

    public void setEnabledExistingInboxButton(boolean enable) {
        config.setEnabledExistingInboxButton(enable);
    }

    public void setEnabledAllQueuesButton(boolean enable) {
        config.setEnabledAllQueuesButton(enable);
    }

    public void setEditStatusTypePopup(I_IntList editStatusTypePopup) {
        config.setEditStatusTypePopup(editStatusTypePopup);
    }

    public void setEditRelTypePopup(I_IntList editRelTypePopup) {
        config.setEditRelTypePopup(editRelTypePopup);
    }

    public void setEditRelRefinabiltyPopup(I_IntList editRelRefinabiltyPopup) {
        config.setEditRelRefinabiltyPopup(editRelRefinabiltyPopup);
    }

    public void setEditRelCharacteristicPopup(I_IntList editRelCharacteristicPopup) {
        config.setEditRelCharacteristicPopup(editRelCharacteristicPopup);
    }

    public void setEditImageTypePopup(I_IntList editImageTypePopup) {
        config.setEditImageTypePopup(editImageTypePopup);
    }

    public void setEditDescTypePopup(I_IntList editDescTypePopup) {
        config.setEditDescTypePopup(editDescTypePopup);
    }

    public void setDestRelTypes(I_IntSet browseUpRels) {
        config.setDestRelTypes(browseUpRels);
    }

    public void setDescTypes(I_IntSet allowedTypes) {
        config.setDescTypes(allowedTypes);
    }

    public void setDefaultStatus(I_GetConceptData defaultStatus) {
        config.setDefaultStatus(defaultStatus);
    }

    public void setDefaultRelationshipType(I_GetConceptData defaultRelationshipType) {
        config.setDefaultRelationshipType(defaultRelationshipType);
    }

    public void setDefaultRelationshipRefinability(I_GetConceptData defaultRelationshipRefinability) {
        config.setDefaultRelationshipRefinability(defaultRelationshipRefinability);
    }

    public void setDefaultRelationshipCharacteristic(I_GetConceptData defaultRelationshipCharacteristic) {
        config.setDefaultRelationshipCharacteristic(defaultRelationshipCharacteristic);
    }

    public void setDefaultImageType(I_GetConceptData defaultImageType) {
        config.setDefaultImageType(defaultImageType);
    }

    public void setDefaultDescriptionType(I_GetConceptData defaultDescriptionType) {
        config.setDefaultDescriptionType(defaultDescriptionType);
    }

    public void setDbConfig(I_ConfigAceDb dbConfig) {
        config.setDbConfig(dbConfig);
    }

    public void setContext(I_GetConceptData context) {
        config.setContext(context);
    }

    public <T extends I_ManageContradiction> void setConflictResolutionStrategy(Class<T> conflictResolutionStrategyClass) {
        config.setConflictResolutionStrategy(conflictResolutionStrategyClass);
    }

    public void setConflictResolutionStrategy(I_ManageContradiction conflictResolutionStrategy) {
        config.setConflictResolutionStrategy(conflictResolutionStrategy);
    }

    public void setComponentToggleVisible(boolean visible) {
        config.setComponentToggleVisible(visible);
    }

    public void setCommitEnabled(boolean enabled) {
        config.setCommitEnabled(enabled);
    }

    public void setCommitAbortButtonsVisible(boolean visible) {
        //editor.setCommitAbortButtonsVisible(visible);
    }

    public void setColorForPath(int pathNid, Color pathColor) {
        config.setColorForPath(pathNid, pathColor);
    }

    public void setClassifierOutputPath(I_GetConceptData outputPath) {
        config.setClassifierOutputPath(outputPath);
    }

    public void setClassifierIsaType(I_GetConceptData classifierIsaType) {
        config.setClassifierIsaType(classifierIsaType);
    }

    public void setClassifierInputPath(I_GetConceptData inputPath) {
        config.setClassifierInputPath(inputPath);
    }

    public void setClassifierInputMode(CLASSIFIER_INPUT_MODE_PREF classifierInputMode) {
        config.setClassifierInputMode(classifierInputMode);
    }

    public void setClassifierConcept(I_GetConceptData classifierConcept) {
        config.setClassifierConcept(classifierConcept);
    }

    public void setClassificationRoot(I_GetConceptData classificationRoot) {
        config.setClassificationRoot(classificationRoot);
    }

    public void setClassificationRoleRoot(I_GetConceptData classificationRoleRoot) {
        config.setClassificationRoleRoot(classificationRoleRoot);
    }

    public void setBuilderToggleVisible(boolean visible) {
        config.setBuilderToggleVisible(visible);
    }

    public void setBounds(Rectangle bounds) {
        config.setBounds(bounds);
    }

    public void setAutoApprove(boolean b) {
        config.setAutoApprove(b);
    }

    public void setAllowedStatus(I_IntSet allowedStatus) {
        config.setAllowedStatus(allowedStatus);
    }

    public void setAllAvailableWorkflowActionUids(List<UUID> actions) {
        config.setAllAvailableWorkflowActionUids(actions);
    }

    public void setAdministrative(boolean isAdministrative) {
        config.setAdministrative(isAdministrative);
    }

    public void setAdminUsername(String adminUsername) {
        config.setAdminUsername(adminUsername);
    }

    public void setAdminPassword(String adminPassword) {
        config.setAdminPassword(adminPassword);
    }

    public void setAddressToggleVisible(boolean visible) {
        config.setAddressToggleVisible(visible);
    }

    public void setActive(boolean active) {
        config.setActive(active);
    }

    @Override
    public void selectConceptViewer(int hostIndex) {
           editor.selectTab(hostIndex);
    }

    public boolean searchWithDescTypeFilter() {
        return config.searchWithDescTypeFilter();
    }

    public void replaceViewPosition(PositionBI oldPosition, PositionBI newPosition) {
        config.replaceViewPosition(oldPosition, newPosition);
    }

    public void replacePromotionPathSet(PathBI oldPath, PathBI newPath) {
        config.replacePromotionPathSet(oldPath, newPath);
    }

    public void replaceEditingPath(PathBI oldPath, PathBI newPath) {
        config.replaceEditingPath(oldPath, newPath);
    }

    public void repaint() {
        config.repaint();
    }

    public void removeViewPosition(PositionBI p) {
        config.removeViewPosition(p);
    }
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public void removePromotionPath(PathBI p) {
        config.removePromotionPath(p);
    }

    public void removeEditingPath(PathBI p) {
        config.removeEditingPath(p);
    }

    public I_PluginToConceptPanel removeConceptPanelPlugin(HOST_ENUM host, UUID id) {
        return config.removeConceptPanelPlugin(host, id);
    }

    public void refreshRefsetTab() {
        config.refreshRefsetTab();
    }

    public void quit() {
        config.quit();
    }

    public void performLuceneSearch(String query, List<I_TestSearchResults> extraCriterion) {
        config.performLuceneSearch(query, extraCriterion);
    }

    public void performLuceneSearch(String query, I_GetConceptData root) {
        config.performLuceneSearch(query, root);
    }

    public boolean isToggleVisible(TOGGLES toggle) {
        return config.isToggleVisible(toggle);
    }

    public boolean isSubversionToggleVisible() {
        return config.isSubversionToggleVisible();
    }

    public boolean isRefsetInToggleVisible(REFSET_TYPES refsetType, TOGGLES toggle) {
        return config.isRefsetInToggleVisible(refsetType, toggle);
    }

    public boolean isPreferencesToggleVisible() {
        return config.isPreferencesToggleVisible();
    }

    public boolean isOverrideOn() {
        return config.isOverrideOn();
    }

    public boolean isInboxToggleVisible() {
        return config.isInboxToggleVisible();
    }

    public boolean isHistoryToggleVisible() {
        return config.isHistoryToggleVisible();
    }

    public boolean isHierarchyToggleVisible() {
        return config.isHierarchyToggleVisible();
    }

    public boolean isComponentToggleVisible() {
        return config.isComponentToggleVisible();
    }

    public boolean isCommitEnabled() {
        return config.isCommitEnabled();
    }

    public boolean isBuilderToggleVisible() {
        return config.isBuilderToggleVisible();
    }

    public boolean isAutoApproveOn() {
        return config.isAutoApproveOn();
    }

    public boolean isAdministrative() {
        return config.isAdministrative();
    }

    public boolean isAddressToggleVisible() {
        return config.isAddressToggleVisible();
    }

    public boolean isActive() {
        return config.isActive();
    }

    public void invalidate() {
        config.invalidate();
    }

    public TreeSet<? extends ConceptVersionBI> getWorkflowStates() {
        return config.getWorkflowStates();
    }

    public TreeSet<? extends ConceptVersionBI> getWorkflowRoles() {
        return config.getWorkflowRoles();
    }

    public JPanel getWorkflowPanel() {
        return config.getWorkflowPanel();
    }

    public JPanel getWorkflowDetailsSheet() {
        return config.getWorkflowDetailsSheet();
    }

    public TreeSet<? extends ConceptVersionBI> getWorkflowActions() {
        return config.getWorkflowActions();
    }

    public MasterWorker getWorker() {
        return config.getWorker();
    }

    public PositionSetReadOnly getViewPositionSetReadOnly() {
        return config.getViewPositionSetReadOnly();
    }

    public Set<PositionBI> getViewPositionSet() {
        return config.getViewPositionSet();
    }

    public ViewCoordinate getViewCoordinate() {
        return config.getViewCoordinate();
    }

    public VetoableChangeSupport getVetoSupport() {
        return config.getVetoSupport();
    }

    public Boolean getVariableHeightTaxonomyView() {
        return config.getVariableHeightTaxonomyView();
    }

    public String getUsername() {
        return config.getUsername();
    }

    public int getTreeTermDividerLoc() {
        return config.getTreeTermDividerLoc();
    }

    public JTree getTreeInTaxonomyPanel() {
        return config.getTreeInTaxonomyPanel();
    }

    public JTree getTreeInSpecEditor() {
        return config.getTreeInSpecEditor();
    }

    public I_IntList getTreeDescPreferenceList() {
        return config.getTreeDescPreferenceList();
    }

    public I_ShowActivity getTopActivity() {
        return config.getTopActivity();
    }

    public List<I_OverrideTaxonomyRenderer> getTaxonomyRendererOverrideList() {
        return config.getTaxonomyRendererOverrideList();
    }

    public List<I_FilterTaxonomyRels> getTaxonomyRelFilterList() {
        return config.getTaxonomyRelFilterList();
    }

    public I_IntList getTableDescPreferenceList() {
        return config.getTableDescPreferenceList();
    }

    public Map<String, List<I_GetConceptData>> getTabHistoryMap() {
        return config.getTabHistoryMap();
    }

    public Map<String, SubversionData> getSubversionMap() {
        return config.getSubversionMap();
    }

    public String getStatusMessage() {
        return config.getStatusMessage();
    }

    public I_IntSet getSourceRelTypes() {
        return config.getSourceRelTypes();
    }

    public Boolean getSortTaxonomyUsingRefset() {
        return config.getSortTaxonomyUsingRefset();
    }

    public JPanel getSignpostPanel() {
        return config.getSignpostPanel();
    }

    public boolean getShowViewerImagesInTaxonomy() {
        return config.getShowViewerImagesInTaxonomy();
    }

    public Boolean getShowRefsetInfoInTaxonomy() {
        return config.getShowRefsetInfoInTaxonomy();
    }

    public Boolean getShowPromotionTab() {
        return config.getShowPromotionTab();
    }

    public Boolean getShowPromotionFilters() {
        return config.getShowPromotionFilters();
    }

    public Boolean getShowPromotionCheckBoxes() {
        return config.getShowPromotionCheckBoxes();
    }

    public Boolean getShowPathInfoInTaxonomy() {
        return config.getShowPathInfoInTaxonomy();
    }

    public I_IntList getShortLabelDescPreferenceList() {
        return config.getShortLabelDescPreferenceList();
    }

    public I_ExtendByRef getSelectedRefsetClauseInSpecEditor() {
        return config.getSelectedRefsetClauseInSpecEditor();
    }

    public List<String> getSelectedAddresses() {
        return config.getSelectedAddresses();
    }

    public I_DescriptionTuple getSearchResultsSelection() {
        return config.getSearchResultsSelection();
    }

    public I_IntSet getRoots() {
        return config.getRoots();
    }

    public RelAssertionType getRelAssertionType() {
        return config.getRelAssertionType();
    }

    public I_IntList getRefsetsToSortTaxonomy() {
        return config.getRefsetsToSortTaxonomy();
    }

    public I_IntList getRefsetsToShowInTaxonomy() {
        return config.getRefsetsToShowInTaxonomy();
    }

    public I_GetConceptData getRefsetSpecInSpecEditor() throws IOException, TerminologyException {
        return config.getRefsetSpecInSpecEditor();
    }

    public Map<TOGGLES, I_HoldRefsetPreferences> getRefsetPreferencesMap() {
        return config.getRefsetPreferencesMap();
    }

    public I_HoldRefsetPreferences getRefsetPreferencesForToggle(TOGGLES toggle) throws TerminologyException, IOException {
        return config.getRefsetPreferencesForToggle(toggle);
    }

    public I_GetConceptData getRefsetInSpecEditor() {
        return config.getRefsetInSpecEditor();
    }

    public Collection<String> getQueueAddressesToShow() {
        return config.getQueueAddressesToShow();
    }

    public Object getProperty(String key) throws IOException {
        return config.getProperty(key);
    }

    public Map<String, Object> getProperties() throws IOException {
        return config.getProperties();
    }

    public PathSetReadOnly getPromotionPathSetReadOnly() {
        return config.getPromotionPathSetReadOnly();
    }

    public Set<PathBI> getPromotionPathSet() {
        return config.getPromotionPathSet();
    }

    public I_IntSet getPrefFilterTypesForRel() {
        return config.getPrefFilterTypesForRel();
    }

    public Precedence getPrecedence() {
        return config.getPrecedence();
    }

    public Map<Integer, Color> getPathColorMap() {
        return config.getPathColorMap();
    }

    public String getPassword() {
        return config.getPassword();
    }

    public I_IntSet getParentExpandedNodes() {
        return config.getParentExpandedNodes();
    }

    public I_IntList getLongLabelDescPreferenceList() {
        return config.getLongLabelDescPreferenceList();
    }

    public I_HostConceptPlugins getListConceptViewer() {
        return editor.getC1Panel();
    }

    public I_GetConceptData getLastViewed() {
        return config.getLastViewed();
    }

    public LANGUAGE_SORT_PREF getLanguageSortPref() {
        return config.getLanguageSortPref();
    }

    public I_IntList getLanguagePreferenceList() {
        return config.getLanguagePreferenceList();
    }

    public Boolean getHighlightConflictsInTaxonomyView() {
        return config.getHighlightConflictsInTaxonomyView();
    }

    public Boolean getHighlightConflictsInComponentPanel() {
        return config.getHighlightConflictsInComponentPanel();
    }

    public I_GetConceptData getHierarchySelection() {
        return config.getHierarchySelection();
    }

    public Set<TopToggleTypes> getHiddenTopToggles() {
        return config.getHiddenTopToggles();
    }

    public String getFrameName() {
        return config.getFrameName();
    }

    public PathSetReadOnly getEditingPathSetReadOnly() {
        return config.getPromotionPathSetReadOnly();
    }

    public Set<PathBI> getEditingPathSet() {
        return config.getPromotionPathSet();
    }

    public I_IntList getEditStatusTypePopup() {
        return config.getEditStatusTypePopup();
    }

    public I_IntList getEditRelTypePopup() {
        return config.getEditRelTypePopup();
    }

    public I_IntList getEditRelRefinabiltyPopup() {
        return config.getEditRelRefinabiltyPopup();
    }

    public I_IntList getEditRelCharacteristicPopup() {
        return config.getEditRelCharacteristicPopup();
    }

    @Override
    public I_IntList getEditImageTypePopup() {
        return config.getEditImageTypePopup();
    }

    public I_IntList getEditDescTypePopup() {
        return config.getEditDescTypePopup();
    }

    public EditCoordinate getEditCoordinate() {
      NidSet editPaths = new NidSet(getPromotionPathSet());
      return new EditCoordinate(Terms.get().getAuthorNid(),
              editPaths);
    }

    public I_IntSet getDestRelTypes() {
        return config.getDestRelTypes();
    }

    public I_IntSet getDescTypes() {
        return config.getDescTypes();
    }

    public I_GetConceptData getDefaultStatus() {
        return config.getDefaultStatus();
    }

    public I_GetConceptData getDefaultRelationshipType() {
        return config.getDefaultRelationshipType();
    }

    public I_GetConceptData getDefaultRelationshipRefinability() {
        return config.getDefaultRelationshipRefinability();
    }

    public I_GetConceptData getDefaultRelationshipCharacteristic() {
        return config.getDefaultRelationshipCharacteristic();
    }

    public I_GetConceptData getDefaultImageType() {
        return config.getDefaultImageType();
    }

    public I_GetConceptData getDefaultDescriptionType() {
        return config.getDefaultDescriptionType();
    }

    public List<I_PluginToConceptPanel> getDefaultConceptPanelPluginsForViewer() {
        return config.getDefaultConceptPanelPluginsForViewer();
    }

    public List<I_PluginToConceptPanel> getDefaultConceptPanelPluginsForEditor() {
        return config.getDefaultConceptPanelPluginsForEditor();
    }

    public I_ConfigAceDb getDbConfig() {
        return config.getDbConfig();
    }

    public I_GetConceptData getContext() {
        return config.getContext();
    }

    public I_ManageContradiction getConflictResolutionStrategy() {
        return config.getConflictResolutionStrategy();
    }

    public I_HostConceptPlugins getConceptViewer(int index) {
        return editor.getC1Panel();
    }

    public Collection<I_PluginToConceptPanel> getConceptPanelPlugins(HOST_ENUM host) {
        return config.getConceptPanelPlugins(host);
    }

    public Set<UUID> getConceptPanelPluginKeys(HOST_ENUM host) {
        return config.getConceptPanelPluginKeys(host);
    }

    public I_PluginToConceptPanel getConceptPanelPlugin(HOST_ENUM host, UUID id) {
        return config.getConceptPanelPlugin(host, id);
    }

    public Color getColorForPath(int pathNid) {
        return config.getColorForPath(pathNid);
    }

    public I_GetConceptData getClassifierOutputPath() {
        return config.getClassifierOutputPath();
    }

    public I_GetConceptData getClassifierIsaType() {
        return config.getClassifierIsaType();
    }

    public I_GetConceptData getClassifierInputPath() {
        return config.getClassifierInputPath();
    }

    public CLASSIFIER_INPUT_MODE_PREF getClassifierInputMode() {
        return config.getClassifierInputMode();
    }

    public I_GetConceptData getClassifierConcept() {
        return config.getClassifierConcept();
    }

    public I_GetConceptData getClassificationRoot() {
        return config.getClassificationRoot();
    }

    public I_GetConceptData getClassificationRoleRoot() {
        return config.getClassificationRoleRoot();
    }

    public I_IntSet getChildrenExpandedNodes() {
        return config.getChildrenExpandedNodes();
    }

    public Collection<I_WriteChangeSet> getChangeSetWriters() {
        return config.getChangeSetWriters();
    }

    public Collection<I_ReadChangeSet> getChangeSetReaders() {
        return config.getChangeSetReaders();
    }

    public BundleType getBundleType() {
        return config.getBundleType();
    }

    public Rectangle getBounds() {
        return config.getBounds();
    }

    public JList getBatchConceptList() {
        return config.getBatchConceptList();
    }

    public I_IntSet getAllowedStatus() {
        return config.getAllowedStatus();
    }

    public I_ManageContradiction[] getAllConflictResolutionStrategies() {
        return config.getAllConflictResolutionStrategies();
    }

    public List<UUID> getAllAvailableWorkflowActionUids() {
        return config.getAllAvailableWorkflowActionUids();
    }

    public String getAdminUsername() {
        return config.getAdminUsername();
    }

    public String getAdminPassword() {
        return config.getAdminPassword();
    }

    public SortedSetModel<String> getAddressesList() {
        return config.getAddressesList();
    }

    public void fireUpdateHierarchyView() {
        config.fireUpdateHierarchyView();
    }

    public void fireRefsetSpecChanged(I_ExtendByRef ext) {
        config.fireRefsetSpecChanged(ext);
    }

    public void fireCommit() {
        PropertyChangeEvent pce = new PropertyChangeEvent(this, "commit", null, null);
        pce.setPropagationId(AceFrameConfig.propigationId.incrementAndGet());
        changeSupport.firePropertyChange(pce);
    }

    public void closeFrame() {
        config.closeFrame();
    }

    public void addViewPosition(PositionBI p) {
        config.addViewPosition(p);
    }

   private transient PropertyChangeSupport changeSupport = new PropertyChangeSupportWithPropagationId(this);


    public void addUncommitted(I_GetConceptData uncommitted) {
        this.changeSupport.firePropertyChange("uncommitted", null, uncommitted);
    }

    public void removeUncommitted(I_GetConceptData uncommitted) {
        this.changeSupport.firePropertyChange("uncommitted", uncommitted, null);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void addPromotionPath(PathBI p) {
        config.addPromotionPath(p);
    }

    public void addImported(I_GetConceptData conceptBean) {
        config.addImported(conceptBean);
    }

    public void addEditingPath(PathBI p) {
        config.addEditingPath(p);
    }

    public void addConceptPanelPlugins(HOST_ENUM host, UUID id, I_PluginToConceptPanel plugin) {
        config.addConceptPanelPlugins(host, id, plugin);
    }
    
    

}
