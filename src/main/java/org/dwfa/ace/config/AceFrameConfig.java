package org.dwfa.ace.config;

import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.IntList;
import org.dwfa.ace.IntSet;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;

import com.sleepycat.je.DatabaseException;

public class AceFrameConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private static final int dataVersion = 12;
    
    private static final int DEFAULT_TREE_TERM_DIV_LOC = 350;
    
    private transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);
    private transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    private boolean active = true;
    private String frameName = "Ace Frame";
    private IntSet destRelTypes = new IntSet();
    private IntSet sourceRelTypes = new IntSet();
    private IntSet allowedStatus = new IntSet();
    private IntSet descTypes = new IntSet();
    private Set<Position> viewPositions = new HashSet<Position>();
    private Rectangle bounds = new Rectangle(0, 0, 1400, 1028);
    private Set<Path> editingPathSet = new HashSet<Path>();
    private IntSet childrenExpandedNodes = new IntSet();
    private IntSet parentExpandedNodes = new IntSet();
    private IntSet roots = new IntSet();
    
    private IntSet editRelTypePopup = new IntSet();
    private IntSet editRelRefinabiltyPopup = new IntSet();
    private IntSet editRelCharacteristicPopup = new IntSet();
    private IntSet editDescTypePopup = new IntSet();
    private IntSet editStatusTypePopup = new IntSet();

    private IntSet statedViewTypes = new IntSet();
    private IntSet inferredViewTypes = new IntSet();
    
    private ConceptBean defaultStatus;
    private ConceptBean defaultDescriptionType;
    private ConceptBean defaultRelationshipType;
    private ConceptBean defaultRelationshipCharacteristic;
    private ConceptBean defaultRelationshipRefinability;
    
    private IntList treeDescPreferenceList = new IntList();
    private IntList tableDescPreferenceList = new IntList();
    private IntList shortLabelDescPreferenceList = new IntList();
    private IntList longLabelDescPreferenceList = new IntList();
	private int termTreeDividerLoc = DEFAULT_TREE_TERM_DIV_LOC;
	
    private ConceptBean hierarchySelection;
    
    private transient MasterWorker worker;
    private transient String statusMessage;

	private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeBoolean(active);
        out.writeObject(frameName);
        IntSet.writeIntSet(out, sourceRelTypes);
        IntSet.writeIntSet(out, destRelTypes);
        IntSet.writeIntSet(out, allowedStatus);
        IntSet.writeIntSet(out, descTypes);
        Position.writePositionSet(out, viewPositions);
        out.writeObject(bounds);
        out.writeObject(editingPathSet);
        IntSet.writeIntSet(out, childrenExpandedNodes);
        IntSet.writeIntSet(out, parentExpandedNodes);
        IntSet.writeIntSet(out, roots);
        
        IntSet.writeIntSet(out, editRelTypePopup);
        IntSet.writeIntSet(out, editRelRefinabiltyPopup);
        IntSet.writeIntSet(out, editRelCharacteristicPopup);
        IntSet.writeIntSet(out, editDescTypePopup);
        IntSet.writeIntSet(out, editStatusTypePopup);

        IntSet.writeIntSet(out, statedViewTypes);
        IntSet.writeIntSet(out, inferredViewTypes);
        
		try {
			out.writeObject(AceConfig.vodb.nativeToUuid(defaultStatus.getConceptId()));
			out.writeObject(AceConfig.vodb.nativeToUuid(defaultDescriptionType.getConceptId()));
			out.writeObject(AceConfig.vodb.nativeToUuid(defaultRelationshipType.getConceptId()));
			out.writeObject(AceConfig.vodb.nativeToUuid(defaultRelationshipCharacteristic.getConceptId()));
			out.writeObject(AceConfig.vodb.nativeToUuid(defaultRelationshipRefinability.getConceptId()));
		} catch (DatabaseException e) {
			IOException newEx = new IOException();
			newEx.initCause(e);
			throw newEx;
		}
		
		IntList.writeIntList(out, treeDescPreferenceList);
		IntList.writeIntList(out, tableDescPreferenceList);
		IntList.writeIntList(out, shortLabelDescPreferenceList);
		IntList.writeIntList(out, longLabelDescPreferenceList);
		out.writeInt(termTreeDividerLoc);
		try {
			if (hierarchySelection != null) {
				out.writeObject(AceConfig.vodb.nativeToUuid(hierarchySelection.getConceptId()));				
			} else {
				out.writeObject(null);				
			}
		} catch (DatabaseException e) {
			IOException newEx = new IOException();
			newEx.initCause(e);
			throw newEx;
		}

    }


    @SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion >= 1) {
            this.vetoSupport = new VetoableChangeSupport(this);
            this.changeSupport = new PropertyChangeSupport(this);
            active = in.readBoolean();
            frameName = (String) in.readObject();
            sourceRelTypes = IntSet.readIntSet(in);
            destRelTypes = IntSet.readIntSet(in);
            allowedStatus = IntSet.readIntSet(in);
            descTypes = IntSet.readIntSet(in);
            viewPositions = Position.readPositionSet(in);
            bounds = (Rectangle) in.readObject();
            if (objDataVersion >= 3) {
            	editingPathSet = (Set<Path>) in.readObject();
            } else {
            	editingPathSet = new HashSet<Path>();
            }
            if (objDataVersion >= 4) {
            	childrenExpandedNodes = IntSet.readIntSet(in);
            	parentExpandedNodes = IntSet.readIntSet(in);
            } else {
                childrenExpandedNodes = new IntSet();
                parentExpandedNodes = new IntSet();
            }
            if (objDataVersion >= 5) {
            	roots = IntSet.readIntSet(in);
            } else {
            	roots = new IntSet();
            }
            if (objDataVersion >= 6) {
                editRelTypePopup = IntSet.readIntSet(in);
                editRelRefinabiltyPopup = IntSet.readIntSet(in);
                editRelCharacteristicPopup = IntSet.readIntSet(in);
                editDescTypePopup = IntSet.readIntSet(in);
                editStatusTypePopup = IntSet.readIntSet(in);
            } else {
                editRelTypePopup = new IntSet();
                editRelRefinabiltyPopup = new IntSet();
                editRelCharacteristicPopup = new IntSet();
                editDescTypePopup = new IntSet();
                editStatusTypePopup = new IntSet();
            }
            if (objDataVersion >= 7) {
                statedViewTypes = IntSet.readIntSet(in);
                inferredViewTypes = IntSet.readIntSet(in);
            } else {
            	statedViewTypes = new IntSet();
            	inferredViewTypes = new IntSet();
            }
            if (objDataVersion >= 8) {
                try {
					defaultStatus = ConceptBean.get(AceConfig.vodb.uuidToNative((List<UUID>) in.readObject()));
	                defaultDescriptionType = ConceptBean.get(AceConfig.vodb.uuidToNative((List<UUID>) in.readObject()));
	                defaultRelationshipType = ConceptBean.get(AceConfig.vodb.uuidToNative((List<UUID>) in.readObject()));
	                defaultRelationshipCharacteristic = ConceptBean.get(AceConfig.vodb.uuidToNative((List<UUID>) in.readObject()));
	                defaultRelationshipRefinability = ConceptBean.get(AceConfig.vodb.uuidToNative((List<UUID>) in.readObject()));
				} catch (Exception e) {
					IOException newEx = new IOException();
					newEx.initCause(e);
					throw newEx;
				}
            } else {
				try {
					defaultStatus = ConceptBean.get(AceConfig.vodb.getId(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()).getNativeId());
	                defaultDescriptionType = ConceptBean.get(AceConfig.vodb.getId(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()).getNativeId());
	                defaultRelationshipType = ConceptBean.get(AceConfig.vodb.getId(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getNativeId());
	                defaultRelationshipCharacteristic = ConceptBean.get(AceConfig.vodb.getId(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()).getNativeId());
	                defaultRelationshipRefinability = ConceptBean.get(AceConfig.vodb.getId(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()).getNativeId());
				} catch (Exception e) {
					IOException newEx = new IOException();
					newEx.initCause(e);
					throw newEx;
				}
           }
            if (objDataVersion >= 9) {
            	treeDescPreferenceList = IntList.readIntList(in);
            } else {
            	treeDescPreferenceList = new IntList();
            }
            if (objDataVersion >= 10) {
            	tableDescPreferenceList = IntList.readIntList(in);
            	shortLabelDescPreferenceList = IntList.readIntList(in);
            	longLabelDescPreferenceList = IntList.readIntList(in);
            } else {
            	tableDescPreferenceList = new IntList();
            	shortLabelDescPreferenceList = new IntList();
            	longLabelDescPreferenceList = new IntList();
            }
            if (objDataVersion >= 11) {
            	termTreeDividerLoc = in.readInt();
            } else {
            	termTreeDividerLoc = DEFAULT_TREE_TERM_DIV_LOC;
            }
            if (objDataVersion >= 12) {
            	try {
            		List<UUID> uuidList = (List<UUID>) in.readObject();
            		if (uuidList != null) {
                		hierarchySelection = ConceptBean.get(AceConfig.vodb.uuidToNative(uuidList));
            		}
				} catch (Exception e) {
					IOException newEx = new IOException();
					newEx.initCause(e);
					throw newEx;
				}
            } 
       } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }


	public boolean isActive() {
		return active;
	}


	public void setActive(boolean active) {
		this.active = active;
	}


	public IntSet getAllowedStatus() {
		return allowedStatus;
	}


	public void setAllowedStatus(IntSet allowedStatus) {
		this.allowedStatus = allowedStatus;
	}


	public IntSet getDescTypes() {
		return descTypes;
	}


	public void setDescTypes(IntSet allowedTypes) {
		this.descTypes = allowedTypes;
	}


	public String getFrameName() {
		return frameName;
	}


	public void setFrameName(String frameName) {
		this.frameName = frameName;
	}


	public void setViewPositions(Set<Position> positions) {
		this.viewPositions = positions;
		this.changeSupport.firePropertyChange("viewPositions", null, positions);
	}


	public VetoableChangeSupport getVetoSupport() {
		return vetoSupport;
	}


	public void setVetoSupport(VetoableChangeSupport vetoSupport) {
		this.vetoSupport = vetoSupport;
	}


	public Rectangle getBounds() {
		return bounds;
	}


	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}


	public IntSet getSourceRelTypes() {
		return sourceRelTypes;
	}


	public void setSourceRelTypes(IntSet browseDownRels) {
		this.sourceRelTypes = browseDownRels;
	}


	public IntSet getDestRelTypes() {
		return destRelTypes;
	}


	public void setDestRelTypes(IntSet browseUpRels) {
		this.destRelTypes = browseUpRels;
	}
	
	public void addEditingPath(Path p) {
		editingPathSet.add(p);
	}
	public void removeEditingPath(Path p) {
		editingPathSet.remove(p);
	}
	public void replaceEditingPath(Path oldPath, Path newPath) {
		this.editingPathSet.remove(oldPath);
		this.editingPathSet.add(newPath);
	}
	public Set<Path> getEditingPathSet() {
		return editingPathSet;
	}

	public void addViewPosition(Position p) {
		viewPositions.add(p);
		this.changeSupport.firePropertyChange("viewPosition", null, p);
	}
	public void removeViewPosition(Position p) {
		viewPositions.remove(p);
		this.changeSupport.firePropertyChange("viewPosition", p, null);
	}
	public void replaceViewPosition(Position oldPosition, Position newPosition) {
		this.viewPositions.remove(oldPosition);
		this.viewPositions.add(newPosition);
		this.changeSupport.firePropertyChange("viewPosition", oldPosition, newPosition);
	}
	public Set<Position> getViewPositionSet() {
		return viewPositions;
	}


	public IntSet getChildrenExpandedNodes() {
		return childrenExpandedNodes;
	}


	public IntSet getParentExpandedNodes() {
		return parentExpandedNodes;
	}


	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}
	
	public void fireCommit() {
		changeSupport.firePropertyChange("commit", null, null);
	}


	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}


	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}


	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(propertyName, listener);
	}


	public IntSet getRoots() {
		return roots;
	}


	public void setRoots(IntSet roots) {
		this.roots = roots;
	}


	public IntSet getEditDescTypePopup() {
		return editDescTypePopup;
	}


	public IntSet getEditRelCharacteristicPopup() {
		return editRelCharacteristicPopup;
	}


	public IntSet getEditRelRefinabiltyPopup() {
		return editRelRefinabiltyPopup;
	}


	public IntSet getEditRelTypePopup() {
		return editRelTypePopup;
	}


	public IntSet getEditStatusTypePopup() {
		return editStatusTypePopup;
	}


	public void setEditDescTypePopup(IntSet editDescTypePopup) {
		this.editDescTypePopup = editDescTypePopup;
	}


	public void setEditRelCharacteristicPopup(IntSet editRelCharacteristicPopup) {
		this.editRelCharacteristicPopup = editRelCharacteristicPopup;
	}


	public void setEditRelRefinabiltyPopup(IntSet editRelRefinabiltyPopup) {
		this.editRelRefinabiltyPopup = editRelRefinabiltyPopup;
	}


	public void setEditRelTypePopup(IntSet editRelTypePopup) {
		this.editRelTypePopup = editRelTypePopup;
	}


	public void setEditStatusTypePopup(IntSet editStatusTypePopup) {
		this.editStatusTypePopup = editStatusTypePopup;
	}


	public IntSet getInferredViewTypes() {
		return inferredViewTypes;
	}


	public void setInferredViewTypes(IntSet inferredViewTypes) {
		this.inferredViewTypes = inferredViewTypes;
	}


	public IntSet getStatedViewTypes() {
		return statedViewTypes;
	}


	public void setStatedViewTypes(IntSet statedViewTypes) {
		this.statedViewTypes = statedViewTypes;
	}


	public ConceptBean getDefaultDescriptionType() {
		return defaultDescriptionType;
	}


	public void setDefaultDescriptionType(ConceptBean defaultDescriptionType) {
		this.defaultDescriptionType = defaultDescriptionType;
	}


	public ConceptBean getDefaultRelationshipCharacteristic() {
		return defaultRelationshipCharacteristic;
	}


	public void setDefaultRelationshipCharacteristic(
			ConceptBean defaultRelationshipCharacteristic) {
		this.defaultRelationshipCharacteristic = defaultRelationshipCharacteristic;
	}


	public ConceptBean getDefaultRelationshipRefinability() {
		return defaultRelationshipRefinability;
	}


	public void setDefaultRelationshipRefinability(
			ConceptBean defaultRelationshipRefinability) {
		this.defaultRelationshipRefinability = defaultRelationshipRefinability;
	}


	public ConceptBean getDefaultRelationshipType() {
		return defaultRelationshipType;
	}


	public void setDefaultRelationshipType(ConceptBean defaultRelationshipType) {
		this.defaultRelationshipType = defaultRelationshipType;
	}


	public ConceptBean getDefaultStatus() {
		return defaultStatus;
	}


	public void setDefaultStatus(ConceptBean defaultStatus) {
		this.defaultStatus = defaultStatus;
	}


	public IntList getTreeDescPreferenceList() {
		return treeDescPreferenceList;
	}


	public IntList getTableDescPreferenceList() {
		return tableDescPreferenceList;
	}


	public IntList getLongLabelDescPreferenceList() {
		return longLabelDescPreferenceList;
	}


	public IntList getShortLabelDescPreferenceList() {
		return shortLabelDescPreferenceList;
	}


	public int getTreeTermDividerLoc() {
		return termTreeDividerLoc;
	}

	public void setTreeTermDividerLoc(int termTreeDividerLoc) {
		this.termTreeDividerLoc = termTreeDividerLoc;
	}


	public ConceptBean getHierarchySelection() {
		return hierarchySelection;
	}


	public void setHierarchySelection(ConceptBean hierarchySelection) {
		Object old = this.hierarchySelection;
		this.hierarchySelection = hierarchySelection;
		this.changeSupport.firePropertyChange("hierarchySelection", old, hierarchySelection);
	}


	public MasterWorker getWorker() {
		return worker;
	}


	public void setWorker(MasterWorker worker) {
		Object old = this.worker;
		this.worker = worker;
		this.changeSupport.firePropertyChange("worker", old, worker);
	}


	public String getStatusMessage() {
		return statusMessage;
	}


	public void setStatusMessage(String statusMessage) {
		Object old = this.statusMessage;
		this.statusMessage = statusMessage;
		this.changeSupport.firePropertyChange("statusMessage", old, statusMessage);
	}

}
