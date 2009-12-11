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
package org.dwfa.ace.config;

import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_FilterTaxonomyRels;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HoldRefsetPreferences;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_OverrideTaxonomyRenderer;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.api.I_HostConceptPlugins.REFSET_TYPES;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.data.SortedSetModel;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;

/**
 * Implements I_ConfigAceFrame with only the properties needed for search, and
 * puts those properties into unsynchronized collections so that changes in the
 * underlying configuration will not affect a search while in progress.
 * 
 * @author kec
 * 
 */
public class FrameConfigSnapshot implements I_ConfigAceFrame {

	I_ConfigAceFrame baseFrame;


	public List<String> svnList(SubversionData svd) {
		return baseFrame.svnList(svd);
	}

	public void svnCompleteRepoInfo(SubversionData svd) {
		baseFrame.svnCompleteRepoInfo(svd);
	}

	public boolean isAdministrative() {
		return baseFrame.isAdministrative();
	}

	public void setAdministrative(boolean isAdministrative) {
		baseFrame.setAdministrative(isAdministrative);
	}

	Set<I_Position> viewPositionSet;

	I_IntSet allowedStatus;

	I_IntSet destRelTypes;

	I_IntSet srcRelTypes;

	public FrameConfigSnapshot(I_ConfigAceFrame baseFrame) {
		super();
		this.baseFrame = baseFrame;
		Set<I_Position> baseViewPositions = baseFrame.getViewPositionSet();
		synchronized (baseViewPositions) {
			viewPositionSet = new HashSet<I_Position>(
					baseViewPositions);
		}
		this.allowedStatus = new IntSet(baseFrame.getAllowedStatus()
				.getSetValues());
		this.destRelTypes = new IntSet(baseFrame.getDestRelTypes()
				.getSetValues());
		this.srcRelTypes = new IntSet(baseFrame.getSourceRelTypes()
				.getSetValues());
	}

	public Set<I_Position> getViewPositionSet() {
		return viewPositionSet;
	}

	public I_IntSet getAllowedStatus() {
		return allowedStatus;
	}

	public I_IntSet getDestRelTypes() {
		return destRelTypes;
	}

	public I_IntSet getSourceRelTypes() {
		return this.srcRelTypes;
	}


	public void addEditingPath(I_Path p) {
		throw new UnsupportedOperationException();
	}

	public void addImported(I_GetConceptData conceptBean) {
		throw new UnsupportedOperationException();
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		throw new UnsupportedOperationException();

	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		throw new UnsupportedOperationException();

	}

	public void addUncommitted(I_GetConceptData conceptBean) {
		throw new UnsupportedOperationException();

	}

	public void addViewPosition(I_Position p) {
		throw new UnsupportedOperationException();

	}

	public void fireCommit() {
		throw new UnsupportedOperationException();

	}



	public void performLuceneSearch(String query, I_GetConceptData root) {
		throw new UnsupportedOperationException();

	}

	public void performLuceneSearch(String query,
			List<I_TestSearchResults> extraCriterion) {
		throw new UnsupportedOperationException();

	}

	public void removeEditingPath(I_Path p) {
		throw new UnsupportedOperationException();

	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		throw new UnsupportedOperationException();

	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		throw new UnsupportedOperationException();

	}

	public void removeUncommitted(I_GetConceptData uncommitted) {
		throw new UnsupportedOperationException();

	}

	public void removeViewPosition(I_Position p) {
		throw new UnsupportedOperationException();

	}

	public void replaceEditingPath(I_Path oldPath, I_Path newPath) {
		throw new UnsupportedOperationException();

	}

	public void replaceViewPosition(I_Position oldPosition,
			I_Position newPosition) {
		throw new UnsupportedOperationException();

	}

	public void selectConceptViewer(int hostIndex) {
		throw new UnsupportedOperationException();

	}

	public void setActive(boolean active) {
		throw new UnsupportedOperationException();

	}

	public void setAddressToggleVisible(boolean visible) {
		throw new UnsupportedOperationException();

	}

	public void setAdminPassword(String adminPassword) {
		throw new UnsupportedOperationException();

	}

	public void setAdminUsername(String adminUsername) {
		throw new UnsupportedOperationException();

	}

	public void setAllowedStatus(I_IntSet allowedStatus) {
		throw new UnsupportedOperationException();

	}

	public void setBounds(Rectangle bounds) {
		throw new UnsupportedOperationException();

	}

	public void setBuilderToggleVisible(boolean visible) {
		throw new UnsupportedOperationException();

	}

	public void setCommitAbortButtonsVisible(boolean visible) {
		throw new UnsupportedOperationException();

	}

	public void setCommitEnabled(boolean enabled) {
		throw new UnsupportedOperationException();

	}

	public void setComponentToggleVisible(boolean visible) {
		throw new UnsupportedOperationException();

	}

	public void setDefaultDescriptionType(
			I_GetConceptData defaultDescriptionType) {
		throw new UnsupportedOperationException();

	}

	public void setDefaultImageType(I_GetConceptData defaultImageType) {
		throw new UnsupportedOperationException();

	}

	public void setDefaultRelationshipCharacteristic(
			I_GetConceptData defaultRelationshipCharacteristic) {
		throw new UnsupportedOperationException();

	}

	public void setDefaultRelationshipRefinability(
			I_GetConceptData defaultRelationshipRefinability) {
		throw new UnsupportedOperationException();

	}

	public void setDefaultRelationshipType(
			I_GetConceptData defaultRelationshipType) {
		throw new UnsupportedOperationException();

	}

	public void setDefaultStatus(I_GetConceptData defaultStatus) {
		throw new UnsupportedOperationException();

	}

	public void setDescTypes(I_IntSet allowedTypes) {
		throw new UnsupportedOperationException();

	}

	public void setDestRelTypes(I_IntSet browseUpRels) {
		throw new UnsupportedOperationException();

	}

	public void setEditDescTypePopup(I_IntList editDescTypePopup) {
		throw new UnsupportedOperationException();

	}

	public void setEditImageTypePopup(I_IntList editImageTypePopup) {
		throw new UnsupportedOperationException();

	}

	public void setEditRelCharacteristicPopup(
			I_IntList editRelCharacteristicPopup) {
		throw new UnsupportedOperationException();

	}

	public void setEditRelRefinabiltyPopup(I_IntList editRelRefinabiltyPopup) {
		throw new UnsupportedOperationException();

	}

	public void setEditRelTypePopup(I_IntList editRelTypePopup) {
		throw new UnsupportedOperationException();

	}

	public void setEditStatusTypePopup(I_IntList editStatusTypePopup) {
		throw new UnsupportedOperationException();

	}

	public void setFrameName(String frameName) {
		throw new UnsupportedOperationException();

	}

	public void setHierarchySelection(I_GetConceptData hierarchySelection) {
		throw new UnsupportedOperationException();

	}

	public void setHierarchyToggleVisible(boolean visible) {
		throw new UnsupportedOperationException();

	}

	public void setHistoryToggleVisible(boolean visible) {
		throw new UnsupportedOperationException();

	}

	public void setInboxToggleVisible(boolean visible) {
		throw new UnsupportedOperationException();

	}

	public void setInferredViewTypes(I_IntSet inferredViewTypes) {
		throw new UnsupportedOperationException();

	}

	public void setLastViewed(I_GetConceptData conceptBean) {
		throw new UnsupportedOperationException();

	}

	public void setPassword(String password) {
		throw new UnsupportedOperationException();

	}

	public void setPreferencesToggleVisible(boolean visible) {
		throw new UnsupportedOperationException();

	}

	public void setProgressToggleVisible(boolean visible) {
		throw new UnsupportedOperationException();

	}

	public void setRefsetInToggleVisible(REFSET_TYPES refsetType,
			TOGGLES toggle, boolean visible) {
		throw new UnsupportedOperationException();

	}

	public void setRoots(I_IntSet roots) {
		throw new UnsupportedOperationException();

	}

	public void setShowAddresses(boolean shown) {
		throw new UnsupportedOperationException();

	}

	public void setShowAllQueues(boolean show) {
		throw new UnsupportedOperationException();

	}

	public void setShowComponentView(boolean shown) {
		throw new UnsupportedOperationException();

	}

	public void setShowHierarchyView(boolean shown) {
		throw new UnsupportedOperationException();

	}

	public void setShowHistory(boolean shown) {
		throw new UnsupportedOperationException();

	}

	public void setShowInferredInTaxonomy(Boolean showInferredInTaxonomy) {
		throw new UnsupportedOperationException();

	}

	public void setShowPreferences(boolean shown) {
		throw new UnsupportedOperationException();

	}

	public void setShowProcessBuilder(boolean show) {
		throw new UnsupportedOperationException();

	}

	public void setShowQueueViewer(boolean show) {
		throw new UnsupportedOperationException();

	}

	public void setShowRefsetInfoInTaxonomy(Boolean showRefsetInfoInTaxonomy) {
		throw new UnsupportedOperationException();

	}

	public void setShowSearch(boolean shown) {
		throw new UnsupportedOperationException();

	}

	public void setShowSignpostPanel(boolean show) {
		throw new UnsupportedOperationException();

	}

	public void setShowViewerImagesInTaxonomy(Boolean showViewerImagesInTaxonomy) {
		throw new UnsupportedOperationException();

	}

	public void setSignpostToggleEnabled(boolean enabled) {
		throw new UnsupportedOperationException();

	}

	public void setSignpostToggleIcon(ImageIcon icon) {
		throw new UnsupportedOperationException();

	}

	public void setSignpostToggleVisible(boolean show) {
		throw new UnsupportedOperationException();

	}

	public void setSortTaxonomyUsingRefset(Boolean sortTaxonomyUsingRefset) {
		throw new UnsupportedOperationException();

	}

	public void setSourceRelTypes(I_IntSet browseDownRels) {
		throw new UnsupportedOperationException();

	}

	public void setStatedViewTypes(I_IntSet statedViewTypes) {
		throw new UnsupportedOperationException();

	}

	public void setSubversionToggleVisible(boolean visible) {
		throw new UnsupportedOperationException();

	}

	public void setTogglesInComponentPanelVisible(TOGGLES toggle,
			boolean visible) {
		throw new UnsupportedOperationException();

	}

	public void setTreeTermDividerLoc(int termTreeDividerLoc) {
		throw new UnsupportedOperationException();

	}

	public void setUsername(String username) {
		throw new UnsupportedOperationException();

	}

	public void setVariableHeightTaxonomyView(Boolean variableHeightTaxonomyView) {
		throw new UnsupportedOperationException();

	}

	public void setVetoSupport(VetoableChangeSupport vetoSupport) {
		throw new UnsupportedOperationException();

	}

	public void setViewPositions(Set<I_Position> positions) {
		throw new UnsupportedOperationException();

	}

	public void setWorker(MasterWorker worker) {
		throw new UnsupportedOperationException();

	}

	public void showListView() {
		throw new UnsupportedOperationException();

	}

	public void svnCheckout(SubversionData svd) {
		throw new UnsupportedOperationException();

	}

	public void svnCleanup(SubversionData svd) {
		throw new UnsupportedOperationException();

	}

	public void svnCommit(SubversionData svd) {
		throw new UnsupportedOperationException();

	}

	public void svnPurge(SubversionData svd) {
		throw new UnsupportedOperationException();

	}

	public void svnStatus(SubversionData svd) {
		throw new UnsupportedOperationException();

	}

	public void svnUpdate(SubversionData svd) {
		throw new UnsupportedOperationException();

	}

	public String getStatusMessage() {
		return baseFrame.getStatusMessage();
	}

	public List<I_FilterTaxonomyRels> getTaxonomyRelFilterList() {
		return baseFrame.getTaxonomyRelFilterList();
	}

	public List<I_OverrideTaxonomyRenderer> getTaxonomyRendererOverrideList() {
		return baseFrame.getTaxonomyRendererOverrideList();
	}

	public I_IntList getTreeDescPreferenceList() {
		return baseFrame.getTreeDescPreferenceList();
	}

	public int getTreeTermDividerLoc() {
		return baseFrame.getTreeTermDividerLoc();
	}

	public String getUsername() {
		return baseFrame.getUsername();
	}

	public Boolean getVariableHeightTaxonomyView() {
		return baseFrame.getVariableHeightTaxonomyView();
	}

	public void setStatusMessage(String statusMessage) {
		baseFrame.setStatusMessage(statusMessage);
	}

	public Boolean getSortTaxonomyUsingRefset() {
		return baseFrame.getSortTaxonomyUsingRefset();
	}

	public SortedSetModel<String> getAddressesList() {
		return baseFrame.getAddressesList();
	}

	public String getAdminPassword() {
		return baseFrame.getAdminPassword();
	}

	public String getAdminUsername() {
		return baseFrame.getAdminUsername();
	}

	public JList getBatchConceptList() {
		return baseFrame.getBatchConceptList();
	}

	public Rectangle getBounds() {
		return baseFrame.getBounds();
	}

	public Collection<I_ReadChangeSet> getChangeSetReaders() {
		return baseFrame.getChangeSetReaders();
	}

	public Collection<I_WriteChangeSet> getChangeSetWriters() {
		return baseFrame.getChangeSetWriters();
	}

	public I_IntSet getChildrenExpandedNodes() {
		return baseFrame.getChildrenExpandedNodes();
	}

	public I_HostConceptPlugins getConceptViewer(int index) {
		return baseFrame.getConceptViewer(index);
	}

	public I_ConfigAceDb getDbConfig() {
		return baseFrame.getDbConfig();
	}

	public I_GetConceptData getDefaultDescriptionType() {
		return baseFrame.getDefaultDescriptionType();
	}

	public I_GetConceptData getDefaultImageType() {
		return baseFrame.getDefaultImageType();
	}

	public I_GetConceptData getDefaultRelationshipCharacteristic() {
		return baseFrame.getDefaultRelationshipCharacteristic();
	}

	public I_GetConceptData getDefaultRelationshipRefinability() {
		return baseFrame.getDefaultRelationshipRefinability();
	}

	public I_GetConceptData getDefaultRelationshipType() {
		return baseFrame.getDefaultRelationshipType();
	}

	public I_GetConceptData getDefaultStatus() {
		return baseFrame.getDefaultStatus();
	}

	public I_IntSet getDescTypes() {
		return baseFrame.getDescTypes();
	}

	public I_IntList getEditDescTypePopup() {
		return baseFrame.getEditDescTypePopup();
	}

	public I_IntList getEditImageTypePopup() {
		return baseFrame.getEditImageTypePopup();
	}

	public Set<I_Path> getEditingPathSet() {
		return baseFrame.getEditingPathSet();
	}

	public I_IntList getEditRelCharacteristicPopup() {
		return baseFrame.getEditRelCharacteristicPopup();
	}

	public I_IntList getEditRelRefinabiltyPopup() {
		return baseFrame.getEditRelRefinabiltyPopup();
	}

	public I_IntList getEditRelTypePopup() {
		return baseFrame.getEditRelTypePopup();
	}

	public I_IntList getEditStatusTypePopup() {
		return baseFrame.getEditStatusTypePopup();
	}

	public String getFrameName() {
		return baseFrame.getFrameName();
	}

	public I_GetConceptData getHierarchySelection() {
		return baseFrame.getHierarchySelection();
	}

	public I_IntSet getInferredViewTypes() {
		return baseFrame.getInferredViewTypes();
	}

	public I_GetConceptData getLastViewed() {
		return baseFrame.getLastViewed();
	}

	public I_HostConceptPlugins getListConceptViewer() {
		return baseFrame.getListConceptViewer();
	}

	public I_IntList getLongLabelDescPreferenceList() {
		return baseFrame.getLongLabelDescPreferenceList();
	}

	public I_IntSet getParentExpandedNodes() {
		return baseFrame.getParentExpandedNodes();
	}

	public String getPassword() {
		return baseFrame.getPassword();
	}

	public Collection<String> getQueueAddressesToShow() {
		return baseFrame.getQueueAddressesToShow();
	}

	public I_HoldRefsetPreferences getRefsetPreferencesForToggle(TOGGLES toggle)
			throws TerminologyException, IOException {
		return baseFrame.getRefsetPreferencesForToggle(toggle);
	}

	public Map<TOGGLES, I_HoldRefsetPreferences> getRefsetPreferencesMap() {
		return baseFrame.getRefsetPreferencesMap();
	}

	public I_IntList getRefsetsToShowInTaxonomy() {
		return baseFrame.getRefsetsToShowInTaxonomy();
	}

	public I_IntList getRefsetsToSortTaxonomy() {
		return baseFrame.getRefsetsToSortTaxonomy();
	}

	public I_IntSet getRoots() {
		return baseFrame.getRoots();
	}

	public List<String> getSelectedAddresses() {
		return baseFrame.getSelectedAddresses();
	}

	public I_IntList getShortLabelDescPreferenceList() {
		return baseFrame.getShortLabelDescPreferenceList();
	}

	public Boolean getShowInferredInTaxonomy() {
		return baseFrame.getShowInferredInTaxonomy();
	}

	public Boolean getShowRefsetInfoInTaxonomy() {
		return baseFrame.getShowRefsetInfoInTaxonomy();
	}

	public boolean getShowViewerImagesInTaxonomy() {
		return baseFrame.getShowViewerImagesInTaxonomy();
	}

	public JPanel getSignpostPanel() {
		return baseFrame.getSignpostPanel();
	}

	public I_IntSet getStatedViewTypes() {
		return baseFrame.getStatedViewTypes();
	}

	public Map<String, SubversionData> getSubversionMap() {
		return baseFrame.getSubversionMap();
	}

	public I_IntList getTableDescPreferenceList() {
		return baseFrame.getTableDescPreferenceList();
	}

	public VetoableChangeSupport getVetoSupport() {
		return baseFrame.getVetoSupport();
	}

	public MasterWorker getWorker() {
		return baseFrame.getWorker();
	}

	public JPanel getWorkflowPanel() {
		return baseFrame.getWorkflowPanel();
	}

	public boolean isActive() {
		return baseFrame.isActive();
	}

	public boolean isAddressToggleVisible() {
		return baseFrame.isAddressToggleVisible();
	}

	public boolean isBuilderToggleVisible() {
		return baseFrame.isBuilderToggleVisible();
	}

	public boolean isCommitEnabled() {
		return baseFrame.isCommitEnabled();
	}

	public boolean isComponentToggleVisible() {
		return baseFrame.isComponentToggleVisible();
	}

	public boolean isHierarchyToggleVisible() {
		return baseFrame.isHierarchyToggleVisible();
	}

	public boolean isHistoryToggleVisible() {
		return baseFrame.isHistoryToggleVisible();
	}

	public boolean isInboxToggleVisible() {
		return baseFrame.isInboxToggleVisible();
	}

	public boolean isPreferencesToggleVisible() {
		return baseFrame.isPreferencesToggleVisible();
	}

	public boolean isProgressToggleVisible() {
		return baseFrame.isProgressToggleVisible();
	}

	public boolean isRefsetInToggleVisible(REFSET_TYPES refsetType,
			TOGGLES toggle) {
		return baseFrame.isRefsetInToggleVisible(refsetType, toggle);
	}

	public boolean isSubversionToggleVisible() {
		return baseFrame.isSubversionToggleVisible();
	}

	public boolean isToggleVisible(TOGGLES toggle) {
		return baseFrame.isToggleVisible(toggle);
	}

	public Map<String, List<I_GetConceptData>> getTabHistoryMap() {
		return baseFrame.getTabHistoryMap();
	}

}
