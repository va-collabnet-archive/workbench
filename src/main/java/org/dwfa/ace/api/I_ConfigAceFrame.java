package org.dwfa.ace.api;

import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;

import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.bpa.data.SortedSetModel;
import org.dwfa.bpa.worker.MasterWorker;

public interface I_ConfigAceFrame {

	public boolean isActive();

	public void setActive(boolean active);

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

	public I_IntSet getEditDescTypePopup();

	public I_IntSet getEditRelCharacteristicPopup();

	public I_IntSet getEditRelRefinabiltyPopup();

	public I_IntSet getEditRelTypePopup();

	public I_IntSet getEditStatusTypePopup();

	public void setEditDescTypePopup(I_IntSet editDescTypePopup);

	public void setEditRelCharacteristicPopup(I_IntSet editRelCharacteristicPopup);

	public void setEditRelRefinabiltyPopup(I_IntSet editRelRefinabiltyPopup);

	public void setEditRelTypePopup(I_IntSet editRelTypePopup);

	public void setEditStatusTypePopup(I_IntSet editStatusTypePopup);

	public I_IntSet getInferredViewTypes();

	public void setInferredViewTypes(I_IntSet inferredViewTypes);

	public I_IntSet getStatedViewTypes();

	public void setStatedViewTypes(I_IntSet statedViewTypes);

	public I_GetConceptData getDefaultDescriptionType();

	public void setDefaultDescriptionType(I_GetConceptData defaultDescriptionType);

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
	
	public void addImported(I_GetConceptData conceptBean);
	
	public void setLastViewed(I_GetConceptData conceptBean);
	public I_GetConceptData getLastViewed();
	
	public JList getBatchConceptList();
	
	public I_HostConceptPlugins getConceptViewer(int index);
	public void selectConceptViewer(int hostIndex);
	
	
	public SortedSetModel<String> getAddressesList();
	
	public List<String> getSelectedAddresses();
	
	public Map<String, SubversionData> getSubversionMap();
    
    public void svnCheckout(SubversionData svd);
    public void svnCommit(SubversionData svd);
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

}