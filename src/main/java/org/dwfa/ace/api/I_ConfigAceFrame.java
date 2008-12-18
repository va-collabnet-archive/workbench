package org.dwfa.ace.api;

import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_HostConceptPlugins.REFSET_TYPES;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.data.SortedSetModel;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.tapi.TerminologyException;

public interface I_ConfigAceFrame {

	public boolean isActive();

	public void setActive(boolean active);
	
    public boolean isAdministrative();

	public void setAdministrative(boolean isAdministrative);

	public I_IntSet getAllowedStatus();

	public void setAllowedStatus(I_IntSet allowedStatus);

	public I_IntSet getDescTypes();

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
	 * Get the destination rel types that should be used
	 * to determine children of a concept in a hierarchy
	 * display. These types are typically the is-a relationship
	 * types. 
	 */
	public I_IntSet getDestRelTypes();

	public void setDestRelTypes(I_IntSet browseUpRels);

	public void addEditingPath(I_Path p);

	public void removeEditingPath(I_Path p);

	public void replaceEditingPath(I_Path oldPath, I_Path newPath);

	public Set<I_Path> getEditingPathSet();

	public void addViewPosition(I_Position p);

	public void removeViewPosition(I_Position p);

	public void replaceViewPosition(I_Position oldPosition,
			I_Position newPosition);

	public Set<I_Position> getViewPositionSet();

	public I_IntSet getChildrenExpandedNodes();

	public I_IntSet getParentExpandedNodes();

	public void addPropertyChangeListener(PropertyChangeListener listener);

	public void fireCommit();

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener);

	public void removePropertyChangeListener(PropertyChangeListener listener);

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener);

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

	public I_IntSet getInferredViewTypes();

	public void setInferredViewTypes(I_IntSet inferredViewTypes);

	public I_IntSet getStatedViewTypes();

	public void setStatedViewTypes(I_IntSet statedViewTypes);

	public I_GetConceptData getDefaultImageType();
	
	public I_GetConceptData getDefaultDescriptionType();

	public void setDefaultDescriptionType(I_GetConceptData defaultDescriptionType);

	public void setDefaultImageType(I_GetConceptData defaultImageType);

	public I_GetConceptData getDefaultRelationshipCharacteristic();

	public void setDefaultRelationshipCharacteristic(
			I_GetConceptData defaultRelationshipCharacteristic);

	public I_GetConceptData getDefaultRelationshipRefinability();

	public void setDefaultRelationshipRefinability(
			I_GetConceptData defaultRelationshipRefinability);

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
    
    public void svnCheckout(SubversionData svd);
    public void svnCommit(SubversionData svd);
	public void svnCompleteRepoInfo(SubversionData svd);
    public void svnCleanup(SubversionData svd);
    public void svnStatus(SubversionData svd);
    public void svnPurge(SubversionData svd);
    public void svnUpdate(SubversionData svd);
	
	public void setShowHierarchyView(boolean shown);
	
	public void showListView();
	
	public void setShowAddresses(boolean shown);
	
	public void setShowComponentView(boolean shown);
	
	public void setShowSearch(boolean shown);
	
	public void performLuceneSearch(String query, I_GetConceptData root);
	
    public void performLuceneSearch(String query, List<I_TestSearchResults> extraCriterion);

    public void setShowPreferences(boolean shown);
	
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
    
    public void setPreferencesToggleVisible(boolean visible);
    public boolean isPreferencesToggleVisible();

    public void setSubversionToggleVisible(boolean visible);
    public boolean isSubversionToggleVisible();

    public void setBuilderToggleVisible(boolean visible);
    public boolean isBuilderToggleVisible();

    public void setInboxToggleVisible(boolean visible);
    public boolean isInboxToggleVisible();

    public void setProgressToggleVisible(boolean visible);
    public boolean isProgressToggleVisible();

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
    
    public I_HoldRefsetPreferences getRefsetPreferencesForToggle(TOGGLES toggle) throws TerminologyException, IOException;
    
    public void setRefsetInToggleVisible(REFSET_TYPES refsetType, TOGGLES toggle, boolean visible);
    
    public boolean isRefsetInToggleVisible(REFSET_TYPES refsetType, TOGGLES toggle);
    
    public void setCommitAbortButtonsVisible(boolean visible);
    

    public Map<TOGGLES, I_HoldRefsetPreferences> getRefsetPreferencesMap();

    public I_IntList getRefsetsToShowInTaxonomy();

    public boolean getShowViewerImagesInTaxonomy();

    public void setShowViewerImagesInTaxonomy(Boolean showViewerImagesInTaxonomy);

    public Boolean getVariableHeightTaxonomyView();

    public void setVariableHeightTaxonomyView(Boolean variableHeightTaxonomyView);

    public Boolean getShowInferredInTaxonomy();

    public void setShowInferredInTaxonomy(Boolean showInferredInTaxonomy);

    public Boolean getShowRefsetInfoInTaxonomy();

    public void setShowRefsetInfoInTaxonomy(Boolean showRefsetInfoInTaxonomy);

    public I_IntList getRefsetsToSortTaxonomy();

    public Boolean getSortTaxonomyUsingRefset();

    public void setSortTaxonomyUsingRefset(Boolean sortTaxonomyUsingRefset);
    
    /**
     * Processes can add and remove I_OverrideTaxonomyRenderer objects. 
     * @return
     */
    public List<I_OverrideTaxonomyRenderer> getTaxonomyRendererOverrideList();
    
    /**
     * Processes can add and remove I_FilterTaxonomyRels objects. 
     * @return
     */
    public List<I_FilterTaxonomyRels> getTaxonomyRelFilterList();
    
    /**
     * For storing history of concepts viewed by each component viewier. 
     * @return
     */
    public Map<String, List<I_GetConceptData>> getTabHistoryMap();

}