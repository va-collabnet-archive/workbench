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
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HoldRefsetPreferences;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.refset.RefsetPreferences;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.data.SortedSetModel;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.ElectronicAddress;
import org.dwfa.svn.SvnPanel;
import org.dwfa.svn.SvnPrompter;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntList;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;

import com.sleepycat.je.DatabaseException;

public class AceFrameConfig implements Serializable, I_ConfigAceFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private static final int dataVersion = 25;
    
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
    
    private I_IntList editRelTypePopup = new IntList();
    private I_IntList editRelRefinabiltyPopup = new IntList();
    private I_IntList editRelCharacteristicPopup = new IntList();
    private I_IntList editDescTypePopup = new IntList();
    private I_IntList editStatusTypePopup = new IntList();

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
    //private String repositoryUrlStr;
    //private String svnWorkingCopy;
    private String changeSetWriterFileName;
    
    // 15
    private String username;
    private String password;
    
    // 16
    private transient AceConfig masterConfig; 
    
    // 17
    private SortedSetModel<String> addressesList = new SortedSetModel<String>();
    
    // 18
    private String adminUsername;
    private String adminPassword;
    
    // 19
    private Map<String, SubversionData> subversionMap = new HashMap<String, SubversionData>();
    
    //20
    private boolean showAllQueues = false;
    private SortedSetModel<String> queueAddressesToShow = new SortedSetModel<String>();
    
    //21
    
	private I_GetConceptData defaultImageType;
	private I_IntList editImageTypePopup;
   
   //22
   private Set<EXT_TYPE> enabledConceptExtTypes = new HashSet<EXT_TYPE>();
   private Set<EXT_TYPE> enabledDescExtTypes = new HashSet<EXT_TYPE>();
   private Set<EXT_TYPE> enabledRelExtTypes = new HashSet<EXT_TYPE>();
   private Set<EXT_TYPE> enabledImageExtTypes = new HashSet<EXT_TYPE>();

   //23
   private Set<TOGGLES> visibleComponentToggles = new HashSet<TOGGLES>();
   //24
   private Set<String> visibleRefsets = new HashSet<String>();
   
   //25 private
   private Map<TOGGLES, RefsetPreferences> refsetPreferencesMap = setupRefsetPreferences();
   
	//transient
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
        Path.writePathSet(out, editingPathSet);
        IntSet.writeIntSet(out, childrenExpandedNodes);
        IntSet.writeIntSet(out, parentExpandedNodes);
        IntSet.writeIntSet(out, roots);
        
        IntList.writeIntList(out, editRelTypePopup);
        IntList.writeIntList(out, editRelRefinabiltyPopup);
        IntList.writeIntList(out, editRelCharacteristicPopup);
        IntList.writeIntList(out, editDescTypePopup);
        IntList.writeIntList(out, editStatusTypePopup);

        IntSet.writeIntSet(out, statedViewTypes);
        IntSet.writeIntSet(out, inferredViewTypes);
        
		try {
			out.writeObject(AceConfig.getVodb().nativeToUuid(defaultStatus.getConceptId()));
			out.writeObject(AceConfig.getVodb().nativeToUuid(defaultDescriptionType.getConceptId()));
			out.writeObject(AceConfig.getVodb().nativeToUuid(defaultRelationshipType.getConceptId()));
			out.writeObject(AceConfig.getVodb().nativeToUuid(defaultRelationshipCharacteristic.getConceptId()));
			out.writeObject(AceConfig.getVodb().nativeToUuid(defaultRelationshipRefinability.getConceptId()));
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
				out.writeObject(AceConfig.getVodb().nativeToUuid(hierarchySelection.getConceptId()));				
			} else {
				out.writeObject(null);				
			}
		} catch (DatabaseException e) {
			IOException newEx = new IOException();
			newEx.initCause(e);
			throw newEx;
		}
		out.writeObject(null);
		out.writeObject(null);
		//14
		out.writeObject(null);
		out.writeObject(null);
		out.writeObject(changeSetWriterFileName);
		//15
		out.writeObject(username);
		out.writeObject(password);
		//16
		out.writeObject(null);
		
		//17 
		out.writeObject(addressesList);
		
		// 18
		out.writeObject(adminUsername);
		out.writeObject(adminPassword);
		
		// 19
		out.writeObject(subversionMap);
        
        // 20
        out.writeBoolean(showAllQueues);
        out.writeObject(queueAddressesToShow);
        
        //21
		try {
			if (defaultImageType == null) {
				defaultImageType = AceConfig.getVodb().getConcept(ArchitectonicAuxiliary.Concept.AUXILLARY_IMAGE.getUids());
			}
			out.writeObject(AceConfig.getVodb().nativeToUuid(defaultImageType.getConceptId()));				
		} catch (DatabaseException e) {
			IOException newEx = new IOException();
			newEx.initCause(e);
			throw newEx;
		} catch (TerminologyException e) {
			IOException newEx = new IOException();
			newEx.initCause(e);
			throw newEx;
		}
		IntList.writeIntList(out, editImageTypePopup);
      
      //22
      out.writeObject(enabledConceptExtTypes);
      out.writeObject(enabledDescExtTypes);
      out.writeObject(enabledRelExtTypes);
      out.writeObject(enabledImageExtTypes);
     
      //23
      out.writeObject(visibleComponentToggles);
      //24
      out.writeObject(visibleRefsets);
      
      //25
      out.writeObject(refsetPreferencesMap);
               
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
            sourceRelTypes = IntSet.readIntSetIgnoreMapErrors(in);
            destRelTypes = IntSet.readIntSetIgnoreMapErrors(in);
            allowedStatus = IntSet.readIntSetIgnoreMapErrors(in);
            descTypes = IntSet.readIntSetIgnoreMapErrors(in);
            viewPositions = Position.readPositionSet(in);
            bounds = (Rectangle) in.readObject();
            if (objDataVersion >= 3) {
            	editingPathSet = Path.readPathSet(in);
            } else {
            	editingPathSet = new HashSet<I_Path>();
            }
            if (objDataVersion >= 4) {
            	childrenExpandedNodes = IntSet.readIntSetIgnoreMapErrors(in);
            	parentExpandedNodes = IntSet.readIntSetIgnoreMapErrors(in);
            } else {
                childrenExpandedNodes = new IntSet();
                parentExpandedNodes = new IntSet();
            }
            if (objDataVersion >= 5) {
            	roots = IntSet.readIntSetIgnoreMapErrors(in);
            } else {
            	roots = new IntSet();
            }
            if (objDataVersion >= 6) {
                editRelTypePopup = IntList.readIntListIgnoreMapErrors(in);
                editRelRefinabiltyPopup = IntList.readIntListIgnoreMapErrors(in);
                editRelCharacteristicPopup = IntList.readIntListIgnoreMapErrors(in);
                editDescTypePopup = IntList.readIntListIgnoreMapErrors(in);
                editStatusTypePopup = IntList.readIntListIgnoreMapErrors(in);
            } else {
                editRelTypePopup = new IntList();
                editRelRefinabiltyPopup = new IntList();
                editRelCharacteristicPopup = new IntList();
                editDescTypePopup = new IntList();
                editStatusTypePopup = new IntList();
            }
            if (objDataVersion >= 7) {
                statedViewTypes = IntSet.readIntSetIgnoreMapErrors(in);
                inferredViewTypes = IntSet.readIntSetIgnoreMapErrors(in);
            } else {
            	statedViewTypes = new IntSet();
            	inferredViewTypes = new IntSet();
            }
            if (objDataVersion >= 8) {
                try {
					defaultStatus = ConceptBean.get(AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject()));
	                defaultDescriptionType = ConceptBean.get(AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject()));
	                defaultRelationshipType = ConceptBean.get(AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject()));
	                defaultRelationshipCharacteristic = ConceptBean.get(AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject()));
	                defaultRelationshipRefinability = ConceptBean.get(AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject()));
				} catch (Exception e) {
					IOException newEx = new IOException();
					newEx.initCause(e);
					throw newEx;
				}
            } else {
				try {
					defaultStatus = ConceptBean.get(AceConfig.getVodb().getId(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()).getNativeId());
	                defaultDescriptionType = ConceptBean.get(AceConfig.getVodb().getId(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()).getNativeId());
	                defaultRelationshipType = ConceptBean.get(AceConfig.getVodb().getId(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getNativeId());
	                defaultRelationshipCharacteristic = ConceptBean.get(AceConfig.getVodb().getId(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()).getNativeId());
	                defaultRelationshipRefinability = ConceptBean.get(AceConfig.getVodb().getId(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()).getNativeId());
				} catch (Exception e) {
					IOException newEx = new IOException();
					newEx.initCause(e);
					throw newEx;
				}
           }
            if (objDataVersion >= 9) {
            	treeDescPreferenceList = IntList.readIntListIgnoreMapErrors(in);
            } else {
            	treeDescPreferenceList = new IntList();
            }
            if (objDataVersion >= 10) {
            	tableDescPreferenceList = IntList.readIntListIgnoreMapErrors(in);
            	shortLabelDescPreferenceList = IntList.readIntListIgnoreMapErrors(in);
            	longLabelDescPreferenceList = IntList.readIntListIgnoreMapErrors(in);
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
                     try {
                        hierarchySelection = ConceptBean.get(AceConfig.getVodb().uuidToNative(uuidList));
                     } catch (NoMappingException e) {
                        AceLog.getAppLog().info("No mapping for hierarchySelection: " + uuidList);
                     }
            		}
				} catch (Exception e) {
					IOException newEx = new IOException();
					newEx.initCause(e);
					throw newEx;
				}
            } 
            if (objDataVersion >= 13) {
                // Do nothing here, the change set readers and writers are now managed differently, and should be null... 
            	@SuppressWarnings("unused")
				Collection<I_ReadChangeSet> readers = (Collection<I_ReadChangeSet>) in.readObject();
            	@SuppressWarnings("unused")
				Collection<I_WriteChangeSet> writers = (Collection<I_WriteChangeSet>) in.readObject();
            }
            if (objDataVersion >= 14) {
        		in.readObject(); // repositoryUrlStr deprecated
        		in.readObject(); // svnWorkingCopy deprecated
        		changeSetWriterFileName = (String) in.readObject();
            }
            if (objDataVersion >= 15) {
        		username = (String) in.readObject();
        		password = (String) in.readObject();
             }
            if (objDataVersion >= 16) {
            	masterConfig = (AceConfig) in.readObject();
             }
            
            if (objDataVersion >= 17) {
           		addressesList = new SortedSetModel<String>((Collection<String>) in.readObject());
            } else {
         		addressesList = new SortedSetModel<String>();
            }
            if (objDataVersion >= 18) {
            	adminUsername = (String) in.readObject();
            	adminPassword = (String) in.readObject();
            } else {
            	adminUsername = "admin";
            	adminPassword = "help";
            }
            if (objDataVersion >= 19) {
            	subversionMap = (Map<String, SubversionData>) in.readObject();
            } else {
            	subversionMap = new HashMap<String, SubversionData>();
            }
            if (objDataVersion >= 20) {
                showAllQueues = in.readBoolean();
                queueAddressesToShow = new SortedSetModel<String>((Collection<String>) in.readObject());
            } else {
                showAllQueues = false;
                queueAddressesToShow = new SortedSetModel<String>();
                queueAddressesToShow.add(username);
            }
            if (objDataVersion >= 21) {
            	try {
					defaultImageType = ConceptBean.get(AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject()));
				} catch (TerminologyException e) {
					throw new ToIoException(e);
				}
            	editImageTypePopup = IntList.readIntListIgnoreMapErrors(in);
            } else {
            	try {
					defaultImageType = AceConfig.getVodb().getConcept(ArchitectonicAuxiliary.Concept.AUXILLARY_IMAGE.getUids());
				} catch (TerminologyException e) {
					throw new ToIoException(e);
				}
            	editImageTypePopup = new IntList();
            	editImageTypePopup.add(defaultImageType.getConceptId());
            }
            if (objDataVersion >= 22) {
               enabledConceptExtTypes = (Set<EXT_TYPE>) in.readObject();
               enabledDescExtTypes = (Set<EXT_TYPE>) in.readObject();
               enabledRelExtTypes = (Set<EXT_TYPE>) in.readObject();
               enabledImageExtTypes = (Set<EXT_TYPE>) in.readObject();
            } else {
               enabledConceptExtTypes = new HashSet<EXT_TYPE>();
               enabledDescExtTypes = new HashSet<EXT_TYPE>();
               enabledRelExtTypes = new HashSet<EXT_TYPE>();
               enabledImageExtTypes = new HashSet<EXT_TYPE>();
           }
            if (objDataVersion >= 23) {
               visibleComponentToggles = (Set<TOGGLES>) in.readObject();
           } else {
              visibleComponentToggles = new HashSet<TOGGLES>();
              setTogglesInComponentPanelVisible(TOGGLES.ID, true);
              setTogglesInComponentPanelVisible(TOGGLES.ATTRIBUTES, true);
              setTogglesInComponentPanelVisible(TOGGLES.DESCRIPTIONS, true);
              setTogglesInComponentPanelVisible(TOGGLES.SOURCE_RELS, true);
              setTogglesInComponentPanelVisible(TOGGLES.DEST_RELS, true);
              setTogglesInComponentPanelVisible(TOGGLES.LINEAGE, true);
              setTogglesInComponentPanelVisible(TOGGLES.IMAGE, true);
              setTogglesInComponentPanelVisible(TOGGLES.CONFLICT, true);
              setTogglesInComponentPanelVisible(TOGGLES.STATED_INFERRED, false);
              setTogglesInComponentPanelVisible(TOGGLES.PREFERENCES, true);
              setTogglesInComponentPanelVisible(TOGGLES.HISTORY, true);
              setTogglesInComponentPanelVisible(TOGGLES.REFSETS, false);
           }
            if (objDataVersion >= 24) {
                visibleRefsets = (Set<String>) in.readObject();
           } else {
              visibleRefsets = new HashSet<String>();
           }
            if (objDataVersion >= 25) {
               refsetPreferencesMap = (Map<TOGGLES, RefsetPreferences>) in.readObject();
               if (refsetPreferencesMap == null) {
                   refsetPreferencesMap = setupRefsetPreferences();
               }
          } else {
              refsetPreferencesMap = setupRefsetPreferences();
          }
       } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }
        addListeners();
    }


    private static HashMap<TOGGLES, RefsetPreferences> setupRefsetPreferences() throws IOException {
        HashMap<TOGGLES, RefsetPreferences> map = new HashMap<TOGGLES, RefsetPreferences>();
         for (TOGGLES toggle: TOGGLES.values()) {
            try {
                map.put(toggle, new RefsetPreferences());
           } catch (TerminologyException e) {
              if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                 AceLog.getAppLog().log(Level.FINE, e.getLocalizedMessage(), e);
              } else {
                 AceLog.getAppLog().info("Missing terms to initialize refests: " + 26);
              }
           }
         }
         return map;
    }


	public AceFrameConfig(AceConfig masterConfig) throws IOException {
		super();
		this.masterConfig = masterConfig;
        addListeners();
	}

	public AceFrameConfig() throws IOException {
		super();
        addListeners();
	}

	private void addListeners() {
		addRootListener();
		
	}


	private void addRootListener() {
		this.roots.addListDataListener(new ListDataListener() {

			public void contentsChanged(ListDataEvent e) {
				changeSupport.firePropertyChange("roots", null, roots);
			}

			public void intervalAdded(ListDataEvent e) {
				changeSupport.firePropertyChange("roots", null, roots);
			}

			public void intervalRemoved(ListDataEvent e) {
				changeSupport.firePropertyChange("roots", null, roots);
			}
			
		});
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
		addRootListener();
		changeSupport.firePropertyChange("roots", null, roots);
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditDescTypePopup()
	 */
	public I_IntList getEditDescTypePopup() {
		return editDescTypePopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditRelCharacteristicPopup()
	 */
	public I_IntList getEditRelCharacteristicPopup() {
		return editRelCharacteristicPopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditRelRefinabiltyPopup()
	 */
	public I_IntList getEditRelRefinabiltyPopup() {
		return editRelRefinabiltyPopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditRelTypePopup()
	 */
	public I_IntList getEditRelTypePopup() {
		return editRelTypePopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditStatusTypePopup()
	 */
	public I_IntList getEditStatusTypePopup() {
		return editStatusTypePopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setEditDescTypePopup(org.dwfa.ace.api.IntSet)
	 */
	public void setEditDescTypePopup(I_IntList editDescTypePopup) {
		this.editDescTypePopup = editDescTypePopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setEditRelCharacteristicPopup(org.dwfa.ace.api.IntSet)
	 */
	public void setEditRelCharacteristicPopup(I_IntList editRelCharacteristicPopup) {
		this.editRelCharacteristicPopup = editRelCharacteristicPopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setEditRelRefinabiltyPopup(org.dwfa.ace.api.IntSet)
	 */
	public void setEditRelRefinabiltyPopup(I_IntList editRelRefinabiltyPopup) {
		this.editRelRefinabiltyPopup = editRelRefinabiltyPopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setEditRelTypePopup(org.dwfa.ace.api.IntSet)
	 */
	public void setEditRelTypePopup(I_IntList editRelTypePopup) {
		this.editRelTypePopup = editRelTypePopup;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.config.I_ConfigAceFrame#setEditStatusTypePopup(org.dwfa.ace.api.IntSet)
	 */
	public void setEditStatusTypePopup(I_IntList editStatusTypePopup) {
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
		Object old = this.defaultDescriptionType;
		this.defaultDescriptionType = defaultDescriptionType;
		changeSupport.firePropertyChange("defaultDescriptionType", old, defaultDescriptionType);

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
		Object old = this.defaultRelationshipCharacteristic;
		this.defaultRelationshipCharacteristic = defaultRelationshipCharacteristic;
		changeSupport.firePropertyChange("defaultRelationshipCharacteristic", old, defaultRelationshipCharacteristic);
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
		Object old = this.defaultRelationshipRefinability;
		this.defaultRelationshipRefinability = defaultRelationshipRefinability;
		changeSupport.firePropertyChange("defaultRelationshipRefinability", old, defaultRelationshipRefinability);
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
		Object old = this.defaultRelationshipType;
		this.defaultRelationshipType = defaultRelationshipType;
		changeSupport.firePropertyChange("defaultRelationshipType", old, defaultRelationshipType);
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
		Object old = this.defaultStatus;
		this.defaultStatus = defaultStatus;
		changeSupport.firePropertyChange("defaultStatus", old, defaultStatus);
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
		this.worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), this);
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



	private SubversionData getChangeSetSubversionData() {
		if (subversionMap.get("change sets") == null) {
		   	SubversionData svd = new SubversionData();
	    	subversionMap.put("change sets", svd);
		}
		return subversionMap.get("change sets");
	}

	public String getSvnRepository() {
		return subversionMap.get("change sets").getRepositoryUrlStr();
	}


	public void setSvnRepository(String repositoryUrlStr) {
		Object old = getChangeSetSubversionData().getRepositoryUrlStr();
		getChangeSetSubversionData().setRepositoryUrlStr(repositoryUrlStr);
		this.changeSupport.firePropertyChange("repositoryUrlStr", old, repositoryUrlStr);
	}


	public String getSvnWorkingCopy() {
		return getChangeSetSubversionData().getWorkingCopyStr();
	}


	public void setSvnWorkingCopy(String svnWorkingCopy) {
		Object old =  getChangeSetSubversionData().getWorkingCopyStr();
		getChangeSetSubversionData().setWorkingCopyStr(svnWorkingCopy);
		this.changeSupport.firePropertyChange("svnWorkingCopy", old, svnWorkingCopy);
	}


	public String getPassword() {
        if (password == null) {
            password = "";
        }
		return password;
	}


	public void setPassword(String password) {
        Object old = this.password;
        this.password = password;
        this.changeSupport.firePropertyChange("password", old, password);
	}


	public String getUsername() {
        if (username == null) {
            username = "";
        }
		return username;
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
		aceFrame.getCdePanel().getConceptTabs().setSelectedIndex(index -1);
	}


	public JPanel getWorkflowPanel() {
		return aceFrame.getCdePanel().getWorkflowPanel();
	}


	public SortedSetModel<String> getAddressesList() {
		return addressesList;
	}


	public List<String> getSelectedAddresses() {
		List<String> addresses = new ArrayList<String>();
		for (Object address: aceFrame.getCdePanel().getAddressList().getSelectedValues()) {
			addresses.add((String) address);
		}
		return addresses;
	}


	public String getAdminPassword() {
		return adminPassword;
	}


	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}


	public String getAdminUsername() {
		return adminUsername;
	}


	public void setAdminUsername(String adminUsername) {
		this.adminUsername = adminUsername;
	}


	public Map<String, SubversionData> getSubversionMap() {
		return subversionMap;
	}


	public void performLuceneSearch(String query, I_GetConceptData root) {
		aceFrame.performLuceneSearch(query, root);
	}


	public void setShowAddresses(boolean show) {
		aceFrame.setShowAddresses(show);
	}


	public void setShowComponentView(boolean show) {
		aceFrame.setShowComponentView(show);
	}


	public void setShowHierarchyView(boolean show) {
		aceFrame.setShowHierarchyView(show);
	}


	public void setShowHistory(boolean show) {
		aceFrame.setShowHistory(show);
	}


	public void setShowPreferences(boolean show) {
		aceFrame.setShowPreferences(show);
	}


	public void setShowSearch(boolean show) {
		aceFrame.setShowSearch(show);
	}


	public void showListView() {
		aceFrame.showListView();
	}


    public void svnCheckout(SubversionData svd) {
        aceFrame.setupSvn();
        SvnPanel.checkout(svd, getAuthenticator(svd));
    }


    private SvnPrompter getAuthenticator(SubversionData svd) {
        SvnPrompter authenticator = new SvnPrompter();
        authenticator.setUsername(svd.getUsername());
        authenticator.setPassword(svd.getPassword());
        return authenticator;
    }


    public void svnCleanup(SubversionData svd) {
        aceFrame.setupSvn();
        SvnPanel.cleanup(svd, getAuthenticator(svd));
    }


    public void svnCommit(SubversionData svd) {
        aceFrame.setupSvn();
        SvnPanel.commit(svd, getAuthenticator(svd));
    }


    public void svnPurge(SubversionData svd) {
        aceFrame.setupSvn();
        SvnPanel.purge(svd, getAuthenticator(svd));
    }


    public void svnStatus(SubversionData svd) {
        aceFrame.setupSvn();
        SvnPanel.status(svd, getAuthenticator(svd));
    }


    public void svnUpdate(SubversionData svd) {
        aceFrame.setupSvn();
        SvnPanel.update(svd, getAuthenticator(svd));
    }


    public class QueueFilter implements ServiceItemFilter {

        public boolean check(ServiceItem item) {
            if (showAllQueues) {
                return true;
            }
            HashSet<Entry> itemAttributes = new HashSet<Entry>(Arrays.asList(item.attributeSets));
             for (String address: queueAddressesToShow) {
                if (itemAttributes.contains(new ElectronicAddress(address))) {
                    AceLog.getAppLog().info(" true");
                   return true;
                }
            }
            return false;
        }
        
    }
    public ServiceItemFilter getInboxQueueFilter() {
        return new QueueFilter();
    }


    public boolean getShowAllQueues() {
        return showAllQueues;
    }


    public void setShowAllQueues(boolean showAllQueues) {
        boolean old = this.showAllQueues;
        this.showAllQueues = showAllQueues;
        this.changeSupport.firePropertyChange("showAllQueues", old, showAllQueues);
    }


    public SortedSetModel<String> getQueueAddressesToShow() {
        return queueAddressesToShow;
    }


    public void setQueueAddressesToShow(SortedSetModel<String> queueAddressesToShow) {
        this.queueAddressesToShow = queueAddressesToShow;
    }


    public void setShowProcessBuilder(boolean show) {
        aceFrame.setShowProcessBuilder(show);
    }


    public void setShowQueueViewer(boolean show) {
        aceFrame.setShowQueueViewer(show);
        
    }


    public JPanel getSignpostPanel() {
        return aceFrame.getSignpostPanel();
    }


    public void setShowSignpostPanel(boolean show) {
        aceFrame.setShowSignpostPanel(show);
        
    }


    public void setSignpostToggleVisible(boolean visible) {
        aceFrame.setShowSignpostToggleVisible(visible);
        
    }


    public void setSignpostToggleEnabled(boolean enabled) {
        aceFrame.setShowSignpostToggleEnabled(enabled);
        
    }


    public void setSignpostToggleIcon(ImageIcon icon) {
        aceFrame.setSignpostToggleIcon(icon);
        
    }


	public I_ConfigAceDb getDbConfig() {
		return getMasterConfig();
	}


	public I_GetConceptData getDefaultImageType() {
		return defaultImageType;
	}


	public I_IntList getEditImageTypePopup() {
		return editImageTypePopup;
	}


	public void setDefaultImageType(I_GetConceptData defaultImageType) {
		Object old = this.defaultImageType;
		this.defaultImageType = defaultImageType;
		changeSupport.firePropertyChange("defaultImageType", old, defaultImageType);
	}


	public void setEditImageTypePopup(I_IntList editImageTypePopup) {
		this.editImageTypePopup = editImageTypePopup;
	}


	public I_HostConceptPlugins getListConceptViewer() {
		return aceFrame.getListConceptViewer();
	}


   public boolean isAddressToggleVisible() {
      return aceFrame.getCdePanel().isAddressToggleVisible();
   }
   public boolean isBuilderToggleVisible() {
      return aceFrame.getCdePanel().isBuilderToggleVisible();
   }
   public boolean isComponentToggleVisible() {
      return aceFrame.getCdePanel().isComponentToggleVisible();
   }
   public boolean isHierarchyToggleVisible() {
      return aceFrame.getCdePanel().isHierarchyToggleVisible();
   }
   public boolean isHistoryToggleVisible() {
      return aceFrame.getCdePanel().isHistoryToggleVisible();
   }
   public boolean isInboxToggleVisible() {
      return aceFrame.getCdePanel().isInboxToggleVisible();
   }
   public boolean isPreferencesToggleVisible() {
      return aceFrame.getCdePanel().isPreferencesToggleVisible();
   }
   public boolean isProgressToggleVisible() {
      return aceFrame.getCdePanel().isProgressToggleVisible();
   }
   public boolean isSubversionToggleVisible() {
      return aceFrame.getCdePanel().isSubversionToggleVisible();
   }
   public void setAddressToggleVisible(boolean visible) {
      aceFrame.getCdePanel().setAddressToggleVisible(visible);
   }
   public void setBuilderToggleVisible(boolean visible) {
      aceFrame.getCdePanel().setBuilderToggleVisible(visible);
    }
   public void setComponentToggleVisible(boolean visible) {
      aceFrame.getCdePanel().setComponentToggleVisible(visible);
   }
   public void setHierarchyToggleVisible(boolean visible) {
      aceFrame.getCdePanel().setHierarchyToggleVisible(visible);
   }
   public void setHistoryToggleVisible(boolean visible) {
      aceFrame.getCdePanel().setHistoryToggleVisible(visible);
   }
   public void setInboxToggleVisible(boolean visible) {
      aceFrame.getCdePanel().setInboxToggleVisible(visible);
   }
   public void setPreferencesToggleVisible(boolean visible) {
      aceFrame.getCdePanel().setPreferencesToggleVisible(visible);
   }
   public void setProgressToggleVisible(boolean visible) {
      aceFrame.getCdePanel().setProgressToggleVisible(visible);
   }
   public void setSubversionToggleVisible(boolean visible) {
      aceFrame.getCdePanel().setSubversionToggleVisible(visible);
   }


   public Set<EXT_TYPE> getEnabledConceptExtTypes() {
      return enabledConceptExtTypes;
   }


   public Set<EXT_TYPE> getEnabledDescExtTypes() {
      return enabledDescExtTypes;
   }


   public Set<EXT_TYPE> getEnabledImageExtTypes() {
      return enabledImageExtTypes;
   }


   public Set<EXT_TYPE> getEnabledRelExtTypes() {
      return enabledRelExtTypes;
   }

   public void setTogglesInComponentPanelVisible(TOGGLES toggle, boolean visible) {
      if (visible) {
         visibleComponentToggles.add(toggle);
      } else {
         visibleComponentToggles.remove(toggle);
      }
      changeSupport.firePropertyChange("visibleComponentToggles", null, visibleComponentToggles);
   }
   public boolean isToggleVisible(TOGGLES toggle) {
      return visibleComponentToggles.contains(toggle);
   }
   public void setRefsetInToggleVisible(EXT_TYPE refsetType, TOGGLES toggle, boolean visible) {
      if (visible) {
         visibleRefsets.add(refsetType.name() + toggle.toString());
      } else {
         visibleRefsets.remove(refsetType.name() + toggle.toString());
      }
      changeSupport.firePropertyChange("visibleRefsets", null, visibleRefsets);
   }
   public boolean isRefsetInToggleVisible(EXT_TYPE refsetType, TOGGLES toggle) {
      return visibleRefsets.contains(refsetType.name() + toggle.toString());
   }

   public I_HoldRefsetPreferences getRefsetPreferencesForToggle(TOGGLES toggle) {
       if (refsetPreferencesMap == null) {
           try {
            refsetPreferencesMap = setupRefsetPreferences();
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
       }
      return refsetPreferencesMap.get(toggle);
   }


   public void setCommitAbortButtonsVisible(boolean visible) {
      aceFrame.getCdePanel().setCommitAbortButtonsVisible(visible);
      
   }
}
