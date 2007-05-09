package org.dwfa.ace.config;

import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JList;
import javax.swing.JPanel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntList;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Position;

import com.sleepycat.je.DatabaseException;

public class AceFrameConfig implements Serializable, I_ConfigAceFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private static final int dataVersion = 16;
    
    private static final int DEFAULT_TREE_TERM_DIV_LOC = 350;
    
    private transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);
    private transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    private boolean active = true;
    private String frameName = "Ace Frame";
    private I_IntSet destRelTypes = new IntSet();
    private I_IntSet sourceRelTypes = new IntSet();
    private I_IntSet allowedStatus = new IntSet();
    private I_IntSet descTypes = new IntSet();
    private Set<I_Position> viewPositions = new HashSet<I_Position>();
    private Rectangle bounds = new Rectangle(0, 0, 1400, 1028);
    private Set<I_Path> editingPathSet = new HashSet<I_Path>();
    private I_IntSet childrenExpandedNodes = new IntSet();
    private I_IntSet parentExpandedNodes = new IntSet();
    private I_IntSet roots = new IntSet();
    
    private I_IntSet editRelTypePopup = new IntSet();
    private I_IntSet editRelRefinabiltyPopup = new IntSet();
    private I_IntSet editRelCharacteristicPopup = new IntSet();
    private I_IntSet editDescTypePopup = new IntSet();
    private I_IntSet editStatusTypePopup = new IntSet();

    private I_IntSet statedViewTypes = new IntSet();
    private I_IntSet inferredViewTypes = new IntSet();
    
    private I_GetConceptData defaultStatus;
    private I_GetConceptData defaultDescriptionType;
    private I_GetConceptData defaultRelationshipType;
    private I_GetConceptData defaultRelationshipCharacteristic;
    private I_GetConceptData defaultRelationshipRefinability;
    
    private IntList treeDescPreferenceList = new IntList();
    private IntList tableDescPreferenceList = new IntList();
    private IntList shortLabelDescPreferenceList = new IntList();
    private IntList longLabelDescPreferenceList = new IntList();
	private int termTreeDividerLoc = DEFAULT_TREE_TERM_DIV_LOC;
	
    private I_GetConceptData hierarchySelection;
    
    // 14
    private String repositoryUrlStr;
    private String svnWorkingCopy;
    private String changeSetWriterFileName;
    
    // 15
    private String username;
    private String password;
    
    // 16
    private AceConfig masterConfig;
    
    private transient MasterWorker worker;
    private transient String statusMessage;
    private transient boolean commitEnabled = false;
    private transient I_GetConceptData lastViewed;
    private transient AceFrame aceFrame;

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
		out.writeObject(this.getChangeSetReaders());
		out.writeObject(this.getChangeSetWriters());
		//14
		out.writeObject(repositoryUrlStr);
		out.writeObject(svnWorkingCopy);
		out.writeObject(changeSetWriterFileName);
		//15
		out.writeObject(username);
		out.writeObject(password);
		//16
		out.writeObject(masterConfig);
   }


    @SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
    	commitEnabled = false;
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
            	editingPathSet = (Set<I_Path>) in.readObject();
            } else {
            	editingPathSet = new HashSet<I_Path>();
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
            if (objDataVersion >= 13) {
            	Collection<I_ReadChangeSet> readers = (Collection<I_ReadChangeSet>) in.readObject();
            	this.getChangeSetReaders().addAll(readers);
            	Collection<I_WriteChangeSet> writers = (Collection<I_WriteChangeSet>) in.readObject();
            	this.getChangeSetWriters().addAll(writers);
            }
            if (objDataVersion >= 14) {
        		repositoryUrlStr = (String) in.readObject();
        		svnWorkingCopy = (String) in.readObject();
        		changeSetWriterFileName = (String) in.readObject();
            }
            if (objDataVersion >= 15) {
        		username = (String) in.readObject();
        		password = (String) in.readObject();
             }
            if (objDataVersion >= 16) {
            	masterConfig = (AceConfig) in.readObject();
             }
       } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }


	public AceFrameConfig(AceConfig masterConfig) {
		super();
		this.masterConfig = masterConfig;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#isActive()
	 */
	public boolean isActive() {
		return active;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setActive(boolean)
	 */
	public void setActive(boolean active) {
		this.active = active;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getAllowedStatus()
	 */
	public I_IntSet getAllowedStatus() {
		return allowedStatus;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setAllowedStatus(org.dwfa.ace.api.IntSet)
	 */
	public void setAllowedStatus(I_IntSet allowedStatus) {
		this.allowedStatus = allowedStatus;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getDescTypes()
	 */
	public I_IntSet getDescTypes() {
		return descTypes;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setDescTypes(org.dwfa.ace.api.IntSet)
	 */
	public void setDescTypes(I_IntSet allowedTypes) {
		this.descTypes = allowedTypes;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getFrameName()
	 */
	public String getFrameName() {
		return frameName;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setFrameName(java.lang.String)
	 */
	public void setFrameName(String frameName) {
		this.frameName = frameName;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setViewPositions(java.util.Set)
	 */
	public void setViewPositions(Set<I_Position> positions) {
		this.viewPositions = positions;
		this.changeSupport.firePropertyChange("viewPositions", null, positions);
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getVetoSupport()
	 */
	public VetoableChangeSupport getVetoSupport() {
		return vetoSupport;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setVetoSupport(java.beans.VetoableChangeSupport)
	 */
	public void setVetoSupport(VetoableChangeSupport vetoSupport) {
		this.vetoSupport = vetoSupport;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getBounds()
	 */
	public Rectangle getBounds() {
		return bounds;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setBounds(java.awt.Rectangle)
	 */
	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getSourceRelTypes()
	 */
	public I_IntSet getSourceRelTypes() {
		return sourceRelTypes;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setSourceRelTypes(org.dwfa.ace.api.IntSet)
	 */
	public void setSourceRelTypes(I_IntSet browseDownRels) {
		this.sourceRelTypes = browseDownRels;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getDestRelTypes()
	 */
	public I_IntSet getDestRelTypes() {
		return destRelTypes;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setDestRelTypes(org.dwfa.ace.api.IntSet)
	 */
	public void setDestRelTypes(I_IntSet browseUpRels) {
		this.destRelTypes = browseUpRels;
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#addEditingPath(org.dwfa.vodb.types.Path)
	 */
	public void addEditingPath(I_Path p) {
		editingPathSet.add(p);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#removeEditingPath(org.dwfa.vodb.types.Path)
	 */
	public void removeEditingPath(I_Path p) {
		editingPathSet.remove(p);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#replaceEditingPath(org.dwfa.vodb.types.Path, org.dwfa.vodb.types.Path)
	 */
	public void replaceEditingPath(I_Path oldPath, I_Path newPath) {
		this.editingPathSet.remove(oldPath);
		this.editingPathSet.add(newPath);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditingPathSet()
	 */
	public Set<I_Path> getEditingPathSet() {
		return editingPathSet;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#addViewPosition(org.dwfa.ace.api.I_Position)
	 */
	public void addViewPosition(I_Position p) {
		viewPositions.add(p);
		this.changeSupport.firePropertyChange("viewPositions", null, p);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#removeViewPosition(org.dwfa.ace.api.I_Position)
	 */
	public void removeViewPosition(I_Position p) {
		viewPositions.remove(p);
		this.changeSupport.firePropertyChange("viewPositions", p, null);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#replaceViewPosition(org.dwfa.ace.api.I_Position, org.dwfa.ace.api.I_Position)
	 */
	public void replaceViewPosition(I_Position oldPosition, I_Position newPosition) {
		this.viewPositions.remove(oldPosition);
		this.viewPositions.add(newPosition);
		this.changeSupport.firePropertyChange("viewPositions", oldPosition, newPosition);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getViewPositionSet()
	 */
	public Set<I_Position> getViewPositionSet() {
		return viewPositions;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getChildrenExpandedNodes()
	 */
	public I_IntSet getChildrenExpandedNodes() {
		return childrenExpandedNodes;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getParentExpandedNodes()
	 */
	public I_IntSet getParentExpandedNodes() {
		return parentExpandedNodes;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#fireCommit()
	 */
	public void fireCommit() {
		changeSupport.firePropertyChange("commit", null, null);
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(propertyName, listener);
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getRoots()
	 */
	public I_IntSet getRoots() {
		return roots;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setRoots(org.dwfa.ace.api.IntSet)
	 */
	public void setRoots(I_IntSet roots) {
		this.roots = roots;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditDescTypePopup()
	 */
	public I_IntSet getEditDescTypePopup() {
		return editDescTypePopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditRelCharacteristicPopup()
	 */
	public I_IntSet getEditRelCharacteristicPopup() {
		return editRelCharacteristicPopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditRelRefinabiltyPopup()
	 */
	public I_IntSet getEditRelRefinabiltyPopup() {
		return editRelRefinabiltyPopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditRelTypePopup()
	 */
	public I_IntSet getEditRelTypePopup() {
		return editRelTypePopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditStatusTypePopup()
	 */
	public I_IntSet getEditStatusTypePopup() {
		return editStatusTypePopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setEditDescTypePopup(org.dwfa.ace.api.IntSet)
	 */
	public void setEditDescTypePopup(I_IntSet editDescTypePopup) {
		this.editDescTypePopup = editDescTypePopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setEditRelCharacteristicPopup(org.dwfa.ace.api.IntSet)
	 */
	public void setEditRelCharacteristicPopup(I_IntSet editRelCharacteristicPopup) {
		this.editRelCharacteristicPopup = editRelCharacteristicPopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setEditRelRefinabiltyPopup(org.dwfa.ace.api.IntSet)
	 */
	public void setEditRelRefinabiltyPopup(I_IntSet editRelRefinabiltyPopup) {
		this.editRelRefinabiltyPopup = editRelRefinabiltyPopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setEditRelTypePopup(org.dwfa.ace.api.IntSet)
	 */
	public void setEditRelTypePopup(I_IntSet editRelTypePopup) {
		this.editRelTypePopup = editRelTypePopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setEditStatusTypePopup(org.dwfa.ace.api.IntSet)
	 */
	public void setEditStatusTypePopup(I_IntSet editStatusTypePopup) {
		this.editStatusTypePopup = editStatusTypePopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getInferredViewTypes()
	 */
	public I_IntSet getInferredViewTypes() {
		return inferredViewTypes;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setInferredViewTypes(org.dwfa.ace.api.IntSet)
	 */
	public void setInferredViewTypes(I_IntSet inferredViewTypes) {
		this.inferredViewTypes = inferredViewTypes;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getStatedViewTypes()
	 */
	public I_IntSet getStatedViewTypes() {
		return statedViewTypes;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setStatedViewTypes(org.dwfa.ace.api.IntSet)
	 */
	public void setStatedViewTypes(I_IntSet statedViewTypes) {
		this.statedViewTypes = statedViewTypes;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getDefaultDescriptionType()
	 */
	public I_GetConceptData getDefaultDescriptionType() {
		return defaultDescriptionType;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setDefaultDescriptionType(org.dwfa.vodb.types.ConceptBean)
	 */
	public void setDefaultDescriptionType(I_GetConceptData defaultDescriptionType) {
		this.defaultDescriptionType = defaultDescriptionType;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getDefaultRelationshipCharacteristic()
	 */
	public I_GetConceptData getDefaultRelationshipCharacteristic() {
		return defaultRelationshipCharacteristic;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setDefaultRelationshipCharacteristic(org.dwfa.vodb.types.ConceptBean)
	 */
	public void setDefaultRelationshipCharacteristic(
			I_GetConceptData defaultRelationshipCharacteristic) {
		this.defaultRelationshipCharacteristic = defaultRelationshipCharacteristic;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getDefaultRelationshipRefinability()
	 */
	public I_GetConceptData getDefaultRelationshipRefinability() {
		return defaultRelationshipRefinability;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setDefaultRelationshipRefinability(org.dwfa.vodb.types.ConceptBean)
	 */
	public void setDefaultRelationshipRefinability(
			I_GetConceptData defaultRelationshipRefinability) {
		this.defaultRelationshipRefinability = defaultRelationshipRefinability;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getDefaultRelationshipType()
	 */
	public I_GetConceptData getDefaultRelationshipType() {
		return defaultRelationshipType;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setDefaultRelationshipType(org.dwfa.vodb.types.ConceptBean)
	 */
	public void setDefaultRelationshipType(I_GetConceptData defaultRelationshipType) {
		this.defaultRelationshipType = defaultRelationshipType;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getDefaultStatus()
	 */
	public I_GetConceptData getDefaultStatus() {
		return defaultStatus;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setDefaultStatus(org.dwfa.vodb.types.ConceptBean)
	 */
	public void setDefaultStatus(I_GetConceptData defaultStatus) {
		this.defaultStatus = defaultStatus;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getTreeDescPreferenceList()
	 */
	public I_IntList getTreeDescPreferenceList() {
		return treeDescPreferenceList;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getTableDescPreferenceList()
	 */
	public I_IntList getTableDescPreferenceList() {
		return tableDescPreferenceList;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getLongLabelDescPreferenceList()
	 */
	public I_IntList getLongLabelDescPreferenceList() {
		return longLabelDescPreferenceList;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getShortLabelDescPreferenceList()
	 */
	public I_IntList getShortLabelDescPreferenceList() {
		return shortLabelDescPreferenceList;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getTreeTermDividerLoc()
	 */
	public int getTreeTermDividerLoc() {
		return termTreeDividerLoc;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setTreeTermDividerLoc(int)
	 */
	public void setTreeTermDividerLoc(int termTreeDividerLoc) {
		this.termTreeDividerLoc = termTreeDividerLoc;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getHierarchySelection()
	 */
	public I_GetConceptData getHierarchySelection() {
		return hierarchySelection;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setHierarchySelection(org.dwfa.vodb.types.ConceptBean)
	 */
	public void setHierarchySelection(I_GetConceptData hierarchySelection) {
		Object old = this.hierarchySelection;
		this.hierarchySelection = hierarchySelection;
		this.changeSupport.firePropertyChange("hierarchySelection", old, hierarchySelection);
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getWorker()
	 */
	public MasterWorker getWorker() {
		return worker;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setWorker(org.dwfa.bpa.worker.MasterWorker)
	 */
	public void setWorker(MasterWorker worker) {
		Object old = this.worker;
		this.worker = worker;
		this.changeSupport.firePropertyChange("worker", old, worker);
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getStatusMessage()
	 */
	public String getStatusMessage() {
		return statusMessage;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setStatusMessage(java.lang.String)
	 */
	public void setStatusMessage(String statusMessage) {
		Object old = this.statusMessage;
		this.statusMessage = statusMessage;
		this.changeSupport.firePropertyChange("statusMessage", old, statusMessage);
	}


	public Collection<I_ReadChangeSet> getChangeSetReaders() {
		return ACE.getCsReaders();
	}


	public Collection<I_WriteChangeSet> getChangeSetWriters() {
		return ACE.getCsWriters();
	}


	public String getChangeSetWriterFileName() {
		return changeSetWriterFileName;
	}


	public void setChangeSetWriterFileName(String changeSetWriterFileName) {
		Object old = this.changeSetWriterFileName;
		this.changeSetWriterFileName = changeSetWriterFileName;
		this.changeSupport.firePropertyChange("changeSetWriterFileName", old, changeSetWriterFileName);
	}


	public String getSvnRepository() {
		return repositoryUrlStr;
	}


	public void setSvnRepository(String repositoryUrlStr) {
		Object old = this.repositoryUrlStr;
		this.repositoryUrlStr = repositoryUrlStr;
		this.changeSupport.firePropertyChange("repositoryUrlStr", old, repositoryUrlStr);
	}


	public String getSvnWorkingCopy() {
		return svnWorkingCopy;
	}


	public void setSvnWorkingCopy(String svnWorkingCopy) {
		Object old = this.svnWorkingCopy;
		this.svnWorkingCopy = svnWorkingCopy;
		this.changeSupport.firePropertyChange("svnWorkingCopy", old, svnWorkingCopy);
	}


	public String getPassword() {
		return masterConfig.getPassword();
	}


	public void setPassword(String password) {
		masterConfig.setPassword(password);
	}


	public String getUsername() {
		return masterConfig.getUsername();
	}


	public void setUsername(String username) {
		Object old = this.username;
		this.username = username;
		this.changeSupport.firePropertyChange("username", old, username);
	}


	public boolean isCommitEnabled() {
		return commitEnabled;
	}


	public void setCommitEnabled(boolean commitEnabled) {
		boolean old = this.commitEnabled;
		this.commitEnabled = commitEnabled;
		this.changeSupport.firePropertyChange("commitEnabled", old, commitEnabled);
	}


	public I_GetConceptData getLastViewed() {
		return lastViewed;
	}


	public void setLastViewed(I_GetConceptData lastViewed) {
		if (lastViewed != null) {
			Object old = this.lastViewed;
			this.lastViewed = lastViewed;
			this.changeSupport.firePropertyChange("lastViewed", old, lastViewed);
		}
	}

	public void addUncommitted(I_GetConceptData uncommitted) {
		this.changeSupport.firePropertyChange("uncommitted", null, uncommitted);
	}
	public void addImported(I_GetConceptData imported) {
		this.changeSupport.firePropertyChange("imported", null, imported);
	}


	public AceConfig getMasterConfig() {
		return masterConfig;
	}


	public void setMasterConfig(AceConfig masterConfig) {
		this.masterConfig = masterConfig;
	}


	public AceFrame getAceFrame() {
		return aceFrame;
	}


	public void setAceFrame(AceFrame aceFrame) {
		this.aceFrame = aceFrame;
	}


	public JList getBatchConceptList() {
		return aceFrame.getBatchConceptList();
	}


	public I_HostConceptPlugins getConceptViewer(int index) {
		return aceFrame.getCdePanel().getConceptPanels().get(index);
	}
	
	public void selectConceptViewer(int index) {
		aceFrame.getCdePanel().getConceptTabs().setSelectedIndex(index);
	}


	public JPanel getWorkflowPanel() {
		// TODO Auto-generated method stub
		return null;
	}


}
