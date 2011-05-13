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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.ace.ACE;
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
import org.dwfa.ace.api.PRECEDENCE;
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
import org.dwfa.ace.exceptions.ToIoException;
import org.dwfa.ace.graph.GraphPlugin;
import org.dwfa.ace.gui.concept.ConceptAttributePlugin;
import org.dwfa.ace.gui.concept.ConflictPlugin;
import org.dwfa.ace.gui.concept.DescriptionPlugin;
import org.dwfa.ace.gui.concept.DestRelPlugin;
import org.dwfa.ace.gui.concept.IdPlugin;
import org.dwfa.ace.gui.concept.ImagePlugin;
import org.dwfa.ace.gui.concept.LanguageRefsetDisplayPlugin;
import org.dwfa.ace.gui.concept.LineagePlugin;
import org.dwfa.ace.gui.concept.SrcRelPlugin;
import org.dwfa.ace.gui.concept.StatedAndNormalFormsPlugin;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.refset.RefsetPreferences;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.gui.toptoggles.TopToggleTypes;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.ace.task.search.IsKindOf;
import org.dwfa.ace.task.svn.SvnPrompter;
import org.dwfa.ace.tree.ExpandPathToNodeStateListener;
import org.dwfa.bpa.data.SortedSetModel;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.ElectronicAddress;
import org.dwfa.jini.TermEntry;
import org.dwfa.svn.Svn;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.PropertyChangeSupportWithPropagationId;
import org.dwfa.vodb.conflict.EditPathLosesStrategy;
import org.dwfa.vodb.conflict.EditPathWinsStrategy;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;
import org.dwfa.vodb.conflict.LastCommitWinsConflictResolutionStrategy;
import org.dwfa.vodb.conflict.ViewPathLosesStrategy;
import org.dwfa.vodb.conflict.ViewPathWinsStrategy;
import org.dwfa.vodb.types.IntList;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.tigris.subversion.javahl.PromptUserPassword3;

public class AceFrameConfig implements Serializable, I_ConfigAceFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 49; // keep current with
    // objDataVersion logic

    private static final int DEFAULT_TREE_TERM_DIV_LOC = 350;

    private transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);

    private transient PropertyChangeSupport changeSupport = new PropertyChangeSupportWithPropagationId(this);

    private boolean active = true;

    private String frameName = "Workbench Frame";

    private I_IntSet destRelTypes = new IntSet();

    private I_IntSet sourceRelTypes = new IntSet();

    private I_IntSet allowedStatus = new IntSet();

    private I_IntSet descTypes = new IntSet();

    private Set<PositionBI> viewPositions = Collections.synchronizedSet(new HashSet<PositionBI>());

    private Rectangle bounds = new Rectangle(0, 0, 1400, 1028);

    private Set<PathBI> editingPathSet = new HashSet<PathBI>();

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
    // private String repositoryUrlStr;
    // private String svnWorkingCopy;
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

    // 20
    private boolean showAllQueues = false;

    private SortedSetModel<String> queueAddressesToShow = new SortedSetModel<String>();

    // 21

    private I_GetConceptData defaultImageType;

    private I_IntList editImageTypePopup;

    // 22
    private Set<REFSET_TYPES> enabledConceptExtTypes = new HashSet<REFSET_TYPES>();

    private Set<REFSET_TYPES> enabledDescExtTypes = new HashSet<REFSET_TYPES>();

    private Set<REFSET_TYPES> enabledRelExtTypes = new HashSet<REFSET_TYPES>();

    private Set<REFSET_TYPES> enabledImageExtTypes = new HashSet<REFSET_TYPES>();

    // 23
    private Set<TOGGLES> visibleComponentToggles = new HashSet<TOGGLES>();

    // 24
    private Set<String> visibleRefsets = new HashSet<String>();

    // 25
    private Map<TOGGLES, I_HoldRefsetPreferences> refsetPreferencesMap = setupRefsetPreferences();

    // 26
    private I_IntList refsetsToShowInTaxonomy = new IntList();

    // 27
    private boolean showViewerImagesInTaxonomy = false;

    private boolean variableHeightTaxonomyView = false;

    private boolean showInferredInTaxonomy = false;

    private boolean showRefsetInfoInTaxonomy = false;

    // 28
    private I_IntList refsetsToSortTaxonomy = new IntList();

    // 29
    private boolean sortTaxonomyUsingRefset = false;

    // 30

    private List<I_OverrideTaxonomyRenderer> taxonomyRendererOverrideList = new ArrayList<I_OverrideTaxonomyRenderer>();
    private List<I_FilterTaxonomyRels> taxonomyRelFilterList = new ArrayList<I_FilterTaxonomyRels>();

    // 31
    private Map<String, List<I_GetConceptData>> tabHistoryMap = new TreeMap<String, List<I_GetConceptData>>();

    // 32
    private boolean isAdministrative = false;

    // 33
    private Set<TopToggleTypes> hiddenTopToggles = new HashSet<TopToggleTypes>();

    // 34
    private I_GetConceptData context;

    // 35
    private I_GetConceptData classificationRoot;
    private I_GetConceptData classifierInputPathConcept;
    private I_GetConceptData classifierIsaType;
    private I_GetConceptData classifierOutputPathConcept;

    // 36
    private I_ManageContradiction contradictionStrategy;
    private boolean highlightConflictsInTaxonomyView;
    private boolean highlightConflictsInComponentPanel;

    // 37
    private Map<Integer, Color> pathColorMap = new HashMap<Integer, Color>();
    private I_IntList languagePreferenceList = new IntList();

    // 38
    private Map<String, Object> properties = new HashMap<String, Object>();

    // 39
    private Map<HOST_ENUM, Map<UUID, I_PluginToConceptPanel>> conceptPanelPlugins =
            new HashMap<HOST_ENUM, Map<UUID, I_PluginToConceptPanel>>();

    // 40
    private LANGUAGE_SORT_PREF langSortPref = LANGUAGE_SORT_PREF.TYPE_B4_LANG;

    // 41
    private I_IntSet prefFilterTypesForRel = new IntSet();

    // 42
    private boolean showPathInfoInTaxonomy = true;

    // 43

    private boolean searchWithDescTypeFilter = false;

    // 44
    private Set<PathBI> promotionPathSet = new HashSet<PathBI>();

    // 45
    private I_GetConceptData classificationRoleRoot;

    // 46, 47
    private Precedence precedence;

    // 48
     private I_GetConceptData classifierConcept;
     private RelAssertionType relAssertionType = RelAssertionType.INFERRED_THEN_STATED;

     // 49
     private CLASSIFIER_INPUT_MODE_PREF classifierInputMode;

    // transient
    private transient MasterWorker worker;

    private transient String statusMessage;

    private transient boolean commitEnabled = false;

    private transient I_GetConceptData lastViewed;

    private transient AceFrame aceFrame;

    private transient BundleType bundleType;

    private boolean autoApprovedOn = false;

    private boolean overrideOn = false;

    private TreeSet<? extends I_GetConceptData> workflowRoles = null;

    private TreeSet<? extends I_GetConceptData> workflowStates = null;

    private TreeSet<? extends I_GetConceptData> workflowActions = null;

    private TreeSet<UUID> availableWorkflowActions = null;

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

        out.writeObject(Terms.get().nativeToUuid(defaultStatus.getConceptNid()));
        out.writeObject(Terms.get().nativeToUuid(defaultDescriptionType.getConceptNid()));
        out.writeObject(Terms.get().nativeToUuid(defaultRelationshipType.getConceptNid()));
        out.writeObject(Terms.get().nativeToUuid(defaultRelationshipCharacteristic.getConceptNid()));
        out.writeObject(Terms.get().nativeToUuid(defaultRelationshipRefinability.getConceptNid()));

        IntList.writeIntList(out, treeDescPreferenceList);
        IntList.writeIntList(out, tableDescPreferenceList);
        IntList.writeIntList(out, shortLabelDescPreferenceList);
        IntList.writeIntList(out, longLabelDescPreferenceList);
        out.writeInt(termTreeDividerLoc);
        if (hierarchySelection != null) {
            out.writeObject(AceConfig.getVodb().nativeToUuid(hierarchySelection.getConceptNid()));
        } else {
            out.writeObject(null);
        }
        out.writeObject(null);
        out.writeObject(null);
        // 14
        out.writeObject(null);
        out.writeObject(null);
        out.writeObject(changeSetWriterFileName);
        // 15
        out.writeObject(username);
        out.writeObject(password);
        // 16
        out.writeObject(null);

        // 17
        out.writeObject(addressesList);

        // 18
        out.writeObject(adminUsername);
        out.writeObject(adminPassword);

        // 19
        out.writeObject(subversionMap);

        // 20
        out.writeBoolean(showAllQueues);
        out.writeObject(queueAddressesToShow);

        // 21
        try {
            if (defaultImageType == null) {
                defaultImageType =
                        AceConfig.getVodb().getConcept(ArchitectonicAuxiliary.Concept.AUXILLARY_IMAGE.getUids());
            }
            out.writeObject(AceConfig.getVodb().nativeToUuid(defaultImageType.getConceptNid()));
        } catch (TerminologyException e) {
            IOException newEx = new IOException();
            newEx.initCause(e);
            throw newEx;
        }
        IntList.writeIntList(out, editImageTypePopup);

        // 22
        out.writeObject(enabledConceptExtTypes);
        out.writeObject(enabledDescExtTypes);
        out.writeObject(enabledRelExtTypes);
        out.writeObject(enabledImageExtTypes);

        // 23
        out.writeObject(visibleComponentToggles);
        // 24
        out.writeObject(visibleRefsets);

        // 25
        out.writeObject(refsetPreferencesMap);

        // 26
        IntList.writeIntList(out, refsetsToShowInTaxonomy);

        // 27
        out.writeBoolean(showViewerImagesInTaxonomy);
        out.writeBoolean(variableHeightTaxonomyView);
        out.writeBoolean(showInferredInTaxonomy);
        out.writeBoolean(showRefsetInfoInTaxonomy);

        // 28
        IntList.writeIntList(out, refsetsToSortTaxonomy);

        // 29
        out.writeBoolean(sortTaxonomyUsingRefset);

        // 30
        out.writeObject(taxonomyRendererOverrideList);
        out.writeObject(taxonomyRelFilterList);

        // 31
        out.writeInt(tabHistoryMap.size());
        for (String keyStr : tabHistoryMap.keySet()) {
            out.writeObject(keyStr);
            IntList il = new IntList();
            for (I_GetConceptData concept : tabHistoryMap.get(keyStr)) {
            	if (concept.isCanceled() != true) {
                    il.add(concept.getConceptNid());
            	}
            }
            IntList.writeIntList(out, il);
        }

        // 32
        out.writeBoolean(isAdministrative);

        // 33
        out.writeObject(hiddenTopToggles);

        // 34
        IntList contextIntList = new IntList();
        if (context != null) {
            contextIntList.add(context.getConceptNid());
        }
        IntList.writeIntList(out, contextIntList);

        // 35
        writeConceptAsId(classificationRoot, out);
        writeConceptAsId(classifierIsaType, out);
        writeConceptAsId(classifierInputPathConcept, out);
        writeConceptAsId(classifierOutputPathConcept, out);

        // 36
        out.writeObject(contradictionStrategy);
        out.writeBoolean(highlightConflictsInComponentPanel);
        out.writeBoolean(highlightConflictsInTaxonomyView);

        // 37
        IntList pathColorMapKeyIntList = new IntList();
        for (Integer key : pathColorMap.keySet()) {
            pathColorMapKeyIntList.add(key);
        }
        IntList.writeIntList(out, pathColorMapKeyIntList);
        for (Integer key : pathColorMapKeyIntList.getListValues()) {
            Color pathColor = pathColorMap.get(key);
            out.writeObject(pathColor);
        }
        IntList.writeIntList(out, languagePreferenceList);

        // 38
        out.writeObject(properties);

        // 39
        out.writeObject(conceptPanelPlugins);

        // 40
        out.writeObject(langSortPref);

        // 41
        IntSet.writeIntSet(out, prefFilterTypesForRel);

        // 42
        out.writeObject(showPathInfoInTaxonomy);

        // 43
        out.writeBoolean(searchWithDescTypeFilter);

        // 44
        Path.writePathSet(out, promotionPathSet);

        // 45
        writeConceptAsId(classificationRoleRoot, out);

        // 46; 47 changed implementation class
        out.writeObject(precedence);

        // 48
        writeConceptAsId(classifierConcept, out);
        out.writeObject(relAssertionType);

        // 49
        out.writeObject(classifierInputMode);

    }

    private void writeConceptAsId(I_GetConceptData concept, ObjectOutputStream out) throws IOException {
        if (concept == null) {
            out.writeObject(null);
        } else {
            out.writeObject(AceConfig.getVodb().nativeToUuid(concept.getConceptNid()));
        }
    }

    public I_GetConceptData getContext() {
        return context;
    }

    public void setContext(I_GetConceptData context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        commitEnabled = false;
        int objDataVersion = in.readInt();
        if (objDataVersion >= 1) {
            this.vetoSupport = new VetoableChangeSupport(this);
            this.changeSupport = new PropertyChangeSupportWithPropagationId(this);
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
                editingPathSet = new HashSet<PathBI>();
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
                    defaultStatus =
                            Terms.get().getConcept(AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject()));
                    defaultDescriptionType =
                            Terms.get().getConcept(AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject()));
                    defaultRelationshipType =
                            Terms.get().getConcept(AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject()));
                    defaultRelationshipCharacteristic =
                            Terms.get().getConcept(AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject()));
                    defaultRelationshipRefinability =
                            Terms.get().getConcept(AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject()));
                } catch (Exception e) {
                    IOException newEx = new IOException();
                    newEx.initCause(e);
                    throw newEx;
                }
            } else {
                try {
                    defaultStatus =
                            Terms.get().getConcept(
                                AceConfig.getVodb().getId(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()).getNid());
                    defaultDescriptionType =
                            Terms.get().getConcept(
                                AceConfig.getVodb().getId(
                                    ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()).getNid());
                    defaultRelationshipType =
                            Terms.get().getConcept(
                                AceConfig.getVodb().getId(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getNid());
                    defaultRelationshipCharacteristic =
                            Terms.get().getConcept(
                                AceConfig.getVodb().getId(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids())
                                    .getNid());
                    defaultRelationshipRefinability =
                            Terms.get().getConcept(
                                AceConfig.getVodb().getId(
                                    ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()).getNid());
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
                List<UUID> uuidList = (List<UUID>) in.readObject();
                try {
                    if (uuidList != null) {
                        try {
                            hierarchySelection = Terms.get().getConcept(AceConfig.getVodb().uuidToNative(uuidList));
                        } catch (NoMappingException e) {
                            AceLog.getAppLog().info("No mapping for hierarchySelection: " + uuidList);
                        }
                    }
                } catch (Exception e) {
                    if (uuidList != null) {
                        AceLog.getAppLog().severe(
                            "Exception processing hierarchy selection with uuid list: " + uuidList);
                    }
                    AceLog.getAppLog().alertAndLogException(e);
                    hierarchySelection = null;
                }
            }
            if (objDataVersion >= 13) {
                // Do nothing here, the change set readers and writers are now
                // managed differently, and should be null...
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
                for (String s : queueAddressesToShow) {
                    if (s == null) {
                        queueAddressesToShow.clear();
                        break;
                    }
                }
            } else {
                showAllQueues = false;
                queueAddressesToShow = new SortedSetModel<String>();
                if (username != null) {
                    queueAddressesToShow.add(username);
                }
            }
            if (objDataVersion >= 21) {
                try {
                    defaultImageType =
                            Terms.get().getConcept(AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject()));
                } catch (TerminologyException e) {
                    throw new ToIoException(e);
                }
                editImageTypePopup = IntList.readIntListIgnoreMapErrors(in);
            } else {
                try {
                    defaultImageType =
                            AceConfig.getVodb().getConcept(ArchitectonicAuxiliary.Concept.AUXILLARY_IMAGE.getUids());
                } catch (TerminologyException e) {
                    throw new ToIoException(e);
                }
                editImageTypePopup = new IntList();
                editImageTypePopup.add(defaultImageType.getConceptNid());
            }
            if (objDataVersion >= 22) {
                enabledConceptExtTypes = (Set<REFSET_TYPES>) in.readObject();
                enabledDescExtTypes = (Set<REFSET_TYPES>) in.readObject();
                enabledRelExtTypes = (Set<REFSET_TYPES>) in.readObject();
                enabledImageExtTypes = (Set<REFSET_TYPES>) in.readObject();
            } else {
                enabledConceptExtTypes = new HashSet<REFSET_TYPES>();
                enabledDescExtTypes = new HashSet<REFSET_TYPES>();
                enabledRelExtTypes = new HashSet<REFSET_TYPES>();
                enabledImageExtTypes = new HashSet<REFSET_TYPES>();
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
                setTogglesInComponentPanelVisible(TOGGLES.LINEAGE_GRAPH, false);
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
                refsetPreferencesMap = (Map<TOGGLES, I_HoldRefsetPreferences>) in.readObject();
                if (refsetPreferencesMap == null) {
                    refsetPreferencesMap = setupRefsetPreferences();
                }
            } else {
                refsetPreferencesMap = setupRefsetPreferences();
            }

            if (objDataVersion >= 26) {
                refsetsToShowInTaxonomy = IntList.readIntListIgnoreMapErrors(in);
            } else {
                refsetsToShowInTaxonomy = new IntList();
            }

            if (objDataVersion >= 27) {
                showViewerImagesInTaxonomy = in.readBoolean();
                variableHeightTaxonomyView = in.readBoolean();
                showInferredInTaxonomy = in.readBoolean();
                showRefsetInfoInTaxonomy = in.readBoolean();
            } else {
                showViewerImagesInTaxonomy = false;
                variableHeightTaxonomyView = false;
                showInferredInTaxonomy = false;
                showRefsetInfoInTaxonomy = false;
            }

            if (objDataVersion >= 28) {
                refsetsToSortTaxonomy = IntList.readIntListIgnoreMapErrors(in);
            } else {
                refsetsToSortTaxonomy = new IntList();
            }
            if (objDataVersion >= 29) {
                sortTaxonomyUsingRefset = in.readBoolean();
            } else {
                sortTaxonomyUsingRefset = false;
            }

            if (objDataVersion >= 30) {
                taxonomyRendererOverrideList = (ArrayList<I_OverrideTaxonomyRenderer>) in.readObject();
                taxonomyRelFilterList = (ArrayList<I_FilterTaxonomyRels>) in.readObject();
            } else {
                taxonomyRendererOverrideList = new ArrayList<I_OverrideTaxonomyRenderer>();
                taxonomyRelFilterList = new ArrayList<I_FilterTaxonomyRels>();
            }
            tabHistoryMap = new TreeMap<String, List<I_GetConceptData>>();
            if (objDataVersion >= 31) {
                int mapSize = in.readInt();
                for (int i = 0; i < mapSize; i++) {
                    String mapId = (String) in.readObject();
                    IntList il = IntList.readIntListIgnoreMapErrors(in);
                    List<I_GetConceptData> tabHistoryList = new LinkedList<I_GetConceptData>();
                    for (int nid : il.getListArray()) {
                        try {
                            tabHistoryList.add(Terms.get().getConcept(nid));
                        } catch (Exception e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        }
                    }
                    tabHistoryMap.put(mapId, tabHistoryList);
                }
            }
            if (objDataVersion >= 32) {
                isAdministrative = in.readBoolean();
            } else {
                isAdministrative = false;
            }

            if (objDataVersion >= 33) {
                hiddenTopToggles = (Set<TopToggleTypes>) in.readObject();
            } else {
                hiddenTopToggles = new HashSet<TopToggleTypes>();
            }

            if (objDataVersion >= 34) {
                IntList contextIntList = IntList.readIntListIgnoreMapErrors(in);
                if (contextIntList.size() != 1) {
                    context = null;
                } else {
                    try {
                        context = Terms.get().getConcept(contextIntList.getListArray()[0]);
                    } catch (TerminologyException e) {
                        throw new ToIoException(e);
                    }
                }
            } else {
                context = null;
            }

            // 35
            if (objDataVersion >= 35) {
                try {
                    classificationRoot = readConceptFromSerializedUuids(in);
                    classifierIsaType = readConceptFromSerializedUuids(in);
                    classifierInputPathConcept = readConceptFromSerializedUuids(in);
                    classifierOutputPathConcept = readConceptFromSerializedUuids(in);
                } catch (TerminologyException e) {
                    IOException newEx = new IOException();
                    newEx.initCause(e);
                    throw newEx;
                }
            } else {
                classificationRoot = null;
                classifierIsaType = null;
                classifierInputPathConcept = null;
                classifierOutputPathConcept = null;
            }

            if (objDataVersion >= 36) {
                contradictionStrategy = (I_ManageContradiction) in.readObject();
                highlightConflictsInComponentPanel = in.readBoolean();
                highlightConflictsInTaxonomyView = in.readBoolean();
            } else {
                contradictionStrategy = new IdentifyAllConflictStrategy();
                highlightConflictsInComponentPanel = false;
                highlightConflictsInTaxonomyView = false;
            }

            pathColorMap = new HashMap<Integer, Color>();
            languagePreferenceList = new IntList();
            if (objDataVersion >= 37) {
                // 37
                IntList pathColorMapKeyIntList = IntList.readIntListStrict(in);
                for (Integer key : pathColorMapKeyIntList.getListValues()) {
                    Color pathColor = (Color) in.readObject();
                    pathColorMap.put(key, pathColor);
                }
                languagePreferenceList = IntList.readIntListStrict(in);
                if (languagePreferenceList == null) {
                    languagePreferenceList = new IntList();
                }
            }
            if (objDataVersion >= 38) {
                // 38
                properties = (Map<String, Object>) in.readObject();
            } else {
                properties = new HashMap<String, Object>();
            }
            if (objDataVersion >= 39) {
                // 39
                conceptPanelPlugins = (Map<HOST_ENUM, Map<UUID, I_PluginToConceptPanel>>) in.readObject();
                if (conceptPanelPlugins == null || conceptPanelPlugins.size() == 0) {
                    for (HOST_ENUM h : HOST_ENUM.values()) {
                        for (I_PluginToConceptPanel plugin : getDefaultConceptPanelPluginsForEditor()) {
                            addConceptPanelPlugins(h, plugin.getId(), plugin);
                        }
                    }
                }
            } else {
                for (HOST_ENUM h : HOST_ENUM.values()) {
                    for (I_PluginToConceptPanel plugin : getDefaultConceptPanelPluginsForEditor()) {
                        addConceptPanelPlugins(h, plugin.getId(), plugin);
                    }
                }
            }
            if (objDataVersion >= 40) {
                langSortPref = (LANGUAGE_SORT_PREF) in.readObject();
            } else {
                langSortPref = LANGUAGE_SORT_PREF.TYPE_B4_LANG;
            }

            if (objDataVersion >= 41) {
                prefFilterTypesForRel = IntSet.readIntSetIgnoreMapErrors(in);
            } else {
                prefFilterTypesForRel = new IntSet();
            }

            if (objDataVersion >= 42) {
                showPathInfoInTaxonomy = (Boolean) in.readObject();
            } else {
                showPathInfoInTaxonomy = true;
            }

            if (objDataVersion >= 43) {
                searchWithDescTypeFilter = in.readBoolean();
            } else {
                searchWithDescTypeFilter = false;
            }

            if (objDataVersion >= 44) {
                promotionPathSet = Path.readPathSet(in);
            } else {
                promotionPathSet = new HashSet<PathBI>();
            }

            if (objDataVersion >= 45) {
                try {
                    classificationRoleRoot = readConceptFromSerializedUuids(in);
                } catch (TerminologyException e) {
                    IOException newEx = new IOException();
                    newEx.initCause(e);
                    throw newEx;
                }
            } else {
                classificationRoleRoot = null;
            }

            if (objDataVersion == 46) {
            	PRECEDENCE p = (PRECEDENCE) in.readObject();
            	precedence = p.getTkPrecedence();
                if (precedence == null) {
                    precedence = Precedence.PATH;
                }
            } else if (objDataVersion >= 47) {
                precedence = (Precedence) in.readObject();
                if (precedence == null) {
                    precedence = Precedence.PATH;
                }
            } else {
                precedence = Precedence.PATH;
            }

            if (objDataVersion >= 48) {
                try {
                    classifierConcept = readConceptFromSerializedUuids(in);
                    if (classifierConcept == null) {
                        classifierConcept = Terms.get().getConcept(
                            ArchitectonicAuxiliary.Concept.SNOROCKET.getUids());
                    }
                    relAssertionType = (RelAssertionType) in.readObject();
                } catch (TerminologyException ex) {
                    throw new IOException(ex);
               }
            } else {
                try {
                    classifierConcept = Terms.get().getConcept(
                            ArchitectonicAuxiliary.Concept.SNOROCKET.getUids());
                    relAssertionType = RelAssertionType.INFERRED_THEN_STATED;
                } catch (TerminologyException ex) {
                    throw new IOException(ex);
               }
            }

            if (objDataVersion >= 49) {
                classifierInputMode = (CLASSIFIER_INPUT_MODE_PREF) in.readObject();
            } else {
                classifierInputMode = CLASSIFIER_INPUT_MODE_PREF.EDIT_PATH;
            }

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
        addListeners();
    }

   @Override
   public EditCoordinate getEditCoordinate() {

      NidSet editPaths = new NidSet(editingPathSet);
      return new EditCoordinate(getDbConfig().getUserConcept().getNid(),
              editPaths);
   }


    @Override
    public I_GetConceptData getClassifierConcept() {
        return classifierConcept;
    }

    @Override
    public void setClassifierConcept(I_GetConceptData classifierConcept) {
        if (classifierConcept != null) {
            Object old = this.classifierConcept;
            this.classifierConcept = classifierConcept;
            changeSupport.firePropertyChange("classifierConcept", old, classifierConcept);
        }
    }

    @SuppressWarnings("unchecked")
    private I_GetConceptData readConceptFromSerializedUuids(java.io.ObjectInputStream in) throws TerminologyException,
            IOException, ClassNotFoundException {
        Object obj = in.readObject();
        if (obj == null) {
            return null;
        } else {
            if (Terms.get().hasId((List<UUID>) obj)) {
                return Terms.get().getConcept((List<UUID>) obj);
            }
            return null;
        }
    }

    private static HashMap<TOGGLES, I_HoldRefsetPreferences> setupRefsetPreferences() throws IOException {
        HashMap<TOGGLES, I_HoldRefsetPreferences> map = new HashMap<TOGGLES, I_HoldRefsetPreferences>();
        for (TOGGLES toggle : TOGGLES.values()) {
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
        try {
            classifierConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SNOROCKET.getUids());
        } catch (TerminologyException ex) {
            throw new IOException(ex);
        }
        addListeners();
    }

    public AceFrameConfig() throws IOException {
        super();
        try {
            classifierConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SNOROCKET.getUids());
        } catch (TerminologyException ex) {
            throw new IOException(ex);
        }
        addListeners();
    }

    public boolean isAdministrative() {
        return isAdministrative;
    }

    public void setAdministrative(boolean isAdministrative) {
        this.isAdministrative = isAdministrative;
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

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#isActive()
     */
    public boolean isActive() {
        return active;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#setActive(boolean)
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getAllowedStatus()
     */
    public I_IntSet getAllowedStatus() {
        return allowedStatus;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#setAllowedStatus(org.dwfa.ace.api
     * .IntSet)
     */
    public void setAllowedStatus(I_IntSet allowedStatus) {
        this.allowedStatus = allowedStatus;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getDescTypes()
     */
    public I_IntSet getDescTypes() {
        return descTypes;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#setDescTypes(org.dwfa.ace.api.IntSet
     * )
     */
    public void setDescTypes(I_IntSet allowedTypes) {
        this.descTypes = allowedTypes;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getFrameName()
     */
    public String getFrameName() {
        return frameName;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#setFrameName(java.lang.String)
     */
    public void setFrameName(String frameName) {
        this.frameName = frameName;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#setViewPositions(java.util.Set)
     */
    public void setViewPositions(Set<PositionBI> positions) {
        if (positions == this.viewPositions) {
            return;
        }
        if (positions != null) {
            if (this.viewPositions != null) {
                if (positions.equals(this.viewPositions)) {
                    return;
                }
            }
        }
        this.viewPositions = positions;
        this.changeSupport.firePropertyChange("viewPositions", null, positions);
        Terms.get().resetViewPositions();
        toManyViewsWarning();
    }

    private void toManyViewsWarning() {
        if (this.viewPositions.size() > 2 && aceFrame != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(aceFrame, "<html>Selecting too many view positions may <br>"+
                                                                  "cause the application to run out of memory.",
                        "warning",
                        JOptionPane.WARNING_MESSAGE);
                }
            });
        }
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getVetoSupport()
     */
    public VetoableChangeSupport getVetoSupport() {
        return vetoSupport;
    }

    /*
     * (non-Javadoc)
     * @seeorg.dwfa.ace.config.I_ConfigAceFrame#setVetoSupport(java.beans.
     * VetoableChangeSupport)
     */
    public void setVetoSupport(VetoableChangeSupport vetoSupport) {
        this.vetoSupport = vetoSupport;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getBounds()
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#setBounds(java.awt.Rectangle)
     */
    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getSourceRelTypes()
     */
    public I_IntSet getSourceRelTypes() {
        return sourceRelTypes;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#setSourceRelTypes(org.dwfa.ace.api
     * .IntSet)
     */
    public void setSourceRelTypes(I_IntSet browseDownRels) {
        this.sourceRelTypes = browseDownRels;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getDestRelTypes()
     */
    public I_IntSet getDestRelTypes() {
        return destRelTypes;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#setDestRelTypes(org.dwfa.ace.api
     * .IntSet)
     */
    public void setDestRelTypes(I_IntSet browseUpRels) {
        this.destRelTypes = browseUpRels;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#addEditingPath(org.dwfa.vodb.types
     * .Path)
     */
    public void addEditingPath(PathBI p) {
        editingPathSet.add(p);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#removeEditingPath(org.dwfa.vodb.
     * types.Path)
     */
    public void removeEditingPath(PathBI p) {
        editingPathSet.remove(p);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#replaceEditingPath(org.dwfa.vodb
     * .types.Path, org.dwfa.vodb.types.Path)
     */
    public void replaceEditingPath(PathBI oldPath, PathBI newPath) {
        this.editingPathSet.remove(oldPath);
        this.editingPathSet.add(newPath);
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditingPathSet()
     */
    public Set<PathBI> getEditingPathSet() {
        return editingPathSet;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#addEditingPath(org.dwfa.vodb.types
     * .Path)
     */
    public void addPromotionPath(PathBI p) {
        promotionPathSet.add(p);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#removeEditingPath(org.dwfa.vodb.
     * types.Path)
     */
    public void removePromotionPath(PathBI p) {
        promotionPathSet.remove(p);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#replaceEditingPath(org.dwfa.vodb
     * .types.Path, org.dwfa.vodb.types.Path)
     */
    public void replacePromotionPathSet(PathBI oldPath, PathBI newPath) {
        this.promotionPathSet.remove(oldPath);
        this.promotionPathSet.add(newPath);
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditingPathSet()
     */
    public Set<PathBI> getPromotionPathSet() {
        return promotionPathSet;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#addViewPosition(org.dwfa.ace.api
     * .I_Position)
     */
    public void addViewPosition(PositionBI p) {
        if (viewPositions.contains(p)) {
            return;
        }
        viewPositions.add(p);
        this.changeSupport.firePropertyChange("viewPositions", null, p);
        toManyViewsWarning();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#removeViewPosition(org.dwfa.ace.
     * api.I_Position)
     */
    public void removeViewPosition(PositionBI p) {
        if (viewPositions.contains(p) == false) {
            return;
        }
        viewPositions.remove(p);
        Terms.get().resetViewPositions();
        this.changeSupport.firePropertyChange("viewPositions", p, null);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#replaceViewPosition(org.dwfa.ace
     * .api.I_Position, org.dwfa.ace.api.I_Position)
     */
    public void replaceViewPosition(PositionBI oldPosition, PositionBI newPosition) {
        if (oldPosition.equals(newPosition)) {
            return;
        }
        this.viewPositions.remove(oldPosition);
        this.viewPositions.add(newPosition);
        Terms.get().resetViewPositions();
        this.changeSupport.firePropertyChange("viewPositions", oldPosition, newPosition);
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getViewPositionSet()
     */
    public Set<PositionBI> getViewPositionSet() {
        return viewPositions;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getChildrenExpandedNodes()
     */
    public I_IntSet getChildrenExpandedNodes() {
        return childrenExpandedNodes;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getParentExpandedNodes()
     */
    public I_IntSet getParentExpandedNodes() {
        return parentExpandedNodes;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#addPropertyChangeListener(java.beans
     * .PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#fireCommit()
     */
    public static AtomicLong propigationId = new AtomicLong();
    public void fireCommit() {
        PropertyChangeEvent pce = new PropertyChangeEvent(this, "commit", null, null);
        pce.setPropagationId(AceFrameConfig.propigationId.incrementAndGet());
        changeSupport.firePropertyChange(pce);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#addPropertyChangeListener(java.lang
     * .String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#removePropertyChangeListener(java
     * .beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#removePropertyChangeListener(java
     * .lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getRoots()
     */
    public I_IntSet getRoots() {
        return roots;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#setRoots(org.dwfa.ace.api.IntSet)
     */
    public void setRoots(I_IntSet roots) {
        this.roots = roots;
        addRootListener();
        changeSupport.firePropertyChange("roots", null, roots);
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditDescTypePopup()
     */
    public I_IntList getEditDescTypePopup() {
        return editDescTypePopup;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditRelCharacteristicPopup()
     */
    public I_IntList getEditRelCharacteristicPopup() {
        return editRelCharacteristicPopup;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditRelRefinabiltyPopup()
     */
    public I_IntList getEditRelRefinabiltyPopup() {
        return editRelRefinabiltyPopup;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditRelTypePopup()
     */
    public I_IntList getEditRelTypePopup() {
        return editRelTypePopup;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getEditStatusTypePopup()
     */
    public I_IntList getEditStatusTypePopup() {
        return editStatusTypePopup;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#setEditDescTypePopup(org.dwfa.ace
     * .api.IntSet)
     */
    public void setEditDescTypePopup(I_IntList editDescTypePopup) {
        this.editDescTypePopup = editDescTypePopup;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#setEditRelCharacteristicPopup(org
     * .dwfa.ace.api.IntSet)
     */
    public void setEditRelCharacteristicPopup(I_IntList editRelCharacteristicPopup) {
        this.editRelCharacteristicPopup = editRelCharacteristicPopup;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#setEditRelRefinabiltyPopup(org.dwfa
     * .ace.api.IntSet)
     */
    public void setEditRelRefinabiltyPopup(I_IntList editRelRefinabiltyPopup) {
        this.editRelRefinabiltyPopup = editRelRefinabiltyPopup;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#setEditRelTypePopup(org.dwfa.ace
     * .api.IntSet)
     */
    public void setEditRelTypePopup(I_IntList editRelTypePopup) {
        this.editRelTypePopup = editRelTypePopup;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#setEditStatusTypePopup(org.dwfa.
     * ace.api.IntSet)
     */
    public void setEditStatusTypePopup(I_IntList editStatusTypePopup) {
        this.editStatusTypePopup = editStatusTypePopup;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getInferredViewTypes()
     */
    public I_IntSet getInferredViewTypes() {
        return inferredViewTypes;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#setInferredViewTypes(org.dwfa.ace
     * .api.IntSet)
     */
    public void setInferredViewTypes(I_IntSet inferredViewTypes) {
        this.inferredViewTypes = inferredViewTypes;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getStatedViewTypes()
     */
    public I_IntSet getStatedViewTypes() {
        return statedViewTypes;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#setStatedViewTypes(org.dwfa.ace.
     * api.IntSet)
     */
    public void setStatedViewTypes(I_IntSet statedViewTypes) {
        this.statedViewTypes = statedViewTypes;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getDefaultDescriptionType()
     */
    public I_GetConceptData getDefaultDescriptionType() {
        return defaultDescriptionType;
    }

    public void setDefaultDescriptionType(I_GetConceptData defaultDescriptionType) {
        Object old = this.defaultDescriptionType;
        this.defaultDescriptionType = defaultDescriptionType;
        changeSupport.firePropertyChange("defaultDescriptionType", old, defaultDescriptionType);

    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#getDefaultRelationshipCharacteristic
     * ()
     */
    public I_GetConceptData getDefaultRelationshipCharacteristic() {
        return defaultRelationshipCharacteristic;
    }

    public void setDefaultRelationshipCharacteristic(I_GetConceptData defaultRelationshipCharacteristic) {
        Object old = this.defaultRelationshipCharacteristic;
        this.defaultRelationshipCharacteristic = defaultRelationshipCharacteristic;
        changeSupport.firePropertyChange("defaultRelationshipCharacteristic", old, defaultRelationshipCharacteristic);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#getDefaultRelationshipRefinability()
     */
    public I_GetConceptData getDefaultRelationshipRefinability() {
        return defaultRelationshipRefinability;
    }

    public void setDefaultRelationshipRefinability(I_GetConceptData defaultRelationshipRefinability) {
        Object old = this.defaultRelationshipRefinability;
        this.defaultRelationshipRefinability = defaultRelationshipRefinability;
        changeSupport.firePropertyChange("defaultRelationshipRefinability", old, defaultRelationshipRefinability);
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getDefaultRelationshipType()
     */
    @Override
    public I_GetConceptData getDefaultRelationshipType() {
        return defaultRelationshipType;
    }

    @Override
    public void setDefaultRelationshipType(I_GetConceptData defaultRelationshipType) {
        Object old = this.defaultRelationshipType;
        this.defaultRelationshipType = defaultRelationshipType;
        changeSupport.firePropertyChange("defaultRelationshipType", old, defaultRelationshipType);
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getDefaultStatus()
     */
    public I_GetConceptData getDefaultStatus() {
        return defaultStatus;
    }

    public void setDefaultStatus(I_GetConceptData defaultStatus) {
        Object old = this.defaultStatus;
        this.defaultStatus = defaultStatus;
        changeSupport.firePropertyChange("defaultStatus", old, defaultStatus);
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getTreeDescPreferenceList()
     */
    public I_IntList getTreeDescPreferenceList() {
        return treeDescPreferenceList;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getTableDescPreferenceList()
     */
    public I_IntList getTableDescPreferenceList() {
        return tableDescPreferenceList;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#getLongLabelDescPreferenceList()
     */
    public I_IntList getLongLabelDescPreferenceList() {
        return longLabelDescPreferenceList;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#getShortLabelDescPreferenceList()
     */
    public I_IntList getShortLabelDescPreferenceList() {
        return shortLabelDescPreferenceList;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getTreeTermDividerLoc()
     */
    public int getTreeTermDividerLoc() {
        return termTreeDividerLoc;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#setTreeTermDividerLoc(int)
     */
    @Override
    public void setTreeTermDividerLoc(int termTreeDividerLoc) {
        this.termTreeDividerLoc = termTreeDividerLoc;
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getHierarchySelection()
     */
    @Override
    public I_GetConceptData getHierarchySelection() {
        return hierarchySelection;
    }

    @Override
    public void setHierarchySelection(I_GetConceptData hierarchySelection) {
        Object old = this.hierarchySelection;
        this.hierarchySelection = hierarchySelection;
        this.changeSupport.firePropertyChange("hierarchySelection", old, hierarchySelection);
    }

    @Override
    public void setHierarchySelectionAndExpand(I_GetConceptData hierarchySelection) throws IOException {
        setHierarchySelection(hierarchySelection);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    new ExpandPathToNodeStateListener(getAceFrame().getCdePanel().getTree(), AceFrameConfig.this,
                        getHierarchySelection());
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getWorker()
     */
    public MasterWorker getWorker() {
        return worker;
    }

    /*
     * (non-Javadoc)
     * @seeorg.dwfa.ace.config.I_ConfigAceFrame#setWorker(org.dwfa.bpa.worker.
     * MasterWorker)
     */
    public void setWorker(MasterWorker worker) {
        Object old = this.worker;
        this.worker = worker;
        this.worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), this);
        this.changeSupport.firePropertyChange("worker", old, worker);
    }

    /*
     * (non-Javadoc)
     * @see org.dwfa.ace.config.I_ConfigAceFrame#getStatusMessage()
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.dwfa.ace.config.I_ConfigAceFrame#setStatusMessage(java.lang.String)
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
        Object old = getChangeSetSubversionData().getWorkingCopyStr();
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

    public void removeUncommitted(I_GetConceptData uncommitted) {
        this.changeSupport.firePropertyChange("uncommitted", uncommitted, null);
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
        if (aceFrame == null) {
            return null;
        }
        if (aceFrame.getCdePanel() == null) {
            return null;
        }
        if (aceFrame.getCdePanel().getConceptPanels() == null) {
            return null;
        }
        if (index > aceFrame.getCdePanel().getConceptPanels().size()) {
            return null;
        }
        if (index == 0) {
            return aceFrame.getCdePanel().getConceptPanels().get(index);
        }
        return aceFrame.getCdePanel().getConceptPanels().get(index - 1);
    }

    public void selectConceptViewer(int index) {
        if (index == 0) {
            aceFrame.getCdePanel().getConceptTabs().setSelectedIndex(index);
        } else {
            aceFrame.getCdePanel().getConceptTabs().setSelectedIndex(index - 1);
        }
    }

    public JPanel getWorkflowPanel() {
        return aceFrame.getCdePanel().getWorkflowPanel();
    }

    public SortedSetModel<String> getAddressesList() {
        return addressesList;
    }

    public List<String> getSelectedAddresses() {
        List<String> addresses = new ArrayList<String>();
        for (Object address : aceFrame.getCdePanel().getAddressList().getSelectedValues()) {
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
        try {
            List<I_TestSearchResults> extraCriterion = new ArrayList<I_TestSearchResults>();
            if (root != null) {
                IsKindOf childTest = new IsKindOf();
                childTest.setParentTerm(new TermEntry(root.getUids()));
            }
            aceFrame.performLuceneSearch(query, extraCriterion);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    public void performLuceneSearch(String query, List<I_TestSearchResults> extraCriterion) {
        aceFrame.performLuceneSearch(query, extraCriterion);
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

    public void svnCheckout(SubversionData svd) throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.checkout(svd, getAuthenticator(svd), true);
    }

    private SvnPrompter getAuthenticator(SubversionData svd) {
        SvnPrompter authenticator = new SvnPrompter();
        authenticator.setUsername(svd.getUsername());
        authenticator.setPassword(svd.getPassword());
        return authenticator;
    }

    public void svnCleanup(SubversionData svd) throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.cleanup(svd, getAuthenticator(svd), true);
    }

    public void svnCommit(SubversionData svd) throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.commit(svd, getAuthenticator(svd), true);
    }

    public void svnImport(SubversionData svd) throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.doImport(svd, getAuthenticator(svd), true);
    }

    public void svnImport(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.doImport(svd, authenticator, interactive);
    }

    public void svnPurge(SubversionData svd) throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.purge(svd, getAuthenticator(svd), true);
    }

    public void svnStatus(SubversionData svd) throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.status(svd, getAuthenticator(svd), true);
    }

    public void svnUpdate(SubversionData svd) throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.update(svd, getAuthenticator(svd), true);
    }

    public void svnRevert(SubversionData svd) throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.revert(svd, getAuthenticator(svd), true);
    }

    public void svnRevert(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.revert(svd, authenticator, interactive);
    }

    public void svnUpdateDatabase(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.updateDatabase(svd, authenticator, interactive);
    }

    public void svnUpdateDatabase(SubversionData svd) throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.updateDatabase(svd, getAuthenticator(svd), true);
    }

    public void svnCompleteRepoInfo(SubversionData svd) throws TaskFailedException {
        File svnDir = new File(svd.getWorkingCopyStr(), ".svn");
        if (svnDir.exists()) {
            File svnEntries = new File(svnDir, "entries");
            try {
                LineNumberReader lnr = new LineNumberReader(new java.io.FileReader(svnEntries));
                @SuppressWarnings("unused")
                String line1 = lnr.readLine();
                @SuppressWarnings("unused")
                String line2 = lnr.readLine();
                @SuppressWarnings("unused")
                String line3 = lnr.readLine();
                @SuppressWarnings("unused")
                String line4 = lnr.readLine();
                String line5 = lnr.readLine();
                AceLog.getAppLog().info("Found url " + line5 + " for working copy: " + svd.getWorkingCopyStr());
                lnr.close();
                svd.setRepositoryUrlStr(line5);

            } catch (FileNotFoundException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
    }

    public List<String> svnList(SubversionData svd) throws TaskFailedException {
        aceFrame.setupSvn();
        return Svn.list(svd);
    }

    public void svnLock(SubversionData svd, File toLock, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.lock(svd, toLock, authenticator, interactive);
    }

    public void svnLock(SubversionData svd, File toLock) throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.lock(svd, toLock, getAuthenticator(svd), true);
    }

    public void svnUnlock(SubversionData svd, File toUnlock, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.unlock(svd, toUnlock, authenticator, interactive);
    }

    public void svnUnlock(SubversionData svd, File toUnlock) throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.unlock(svd, toUnlock, getAuthenticator(svd), true);
    }

    public class QueueFilter implements ServiceItemFilter {

        public boolean check(ServiceItem item) {
            if (showAllQueues) {
                return true;
            }
            HashSet<Entry> itemAttributes = new HashSet<Entry>(Arrays.asList(item.attributeSets));
            for (String address : queueAddressesToShow) {
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

    public void setShowWorkflowSignpostPanel(boolean show) {
        aceFrame.setShowWorkflowSignpostPanel(show);

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

    public void setDbConfig(I_ConfigAceDb dbConfig) {
        setMasterConfig((AceConfig) dbConfig);
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

    public boolean isSubversionToggleVisible() {
        return aceFrame.getCdePanel().isSubversionToggleVisible();
    }

    public void setAddressToggleVisible(boolean visible) {
        if (aceFrame != null) {
            aceFrame.getCdePanel().setAddressToggleVisible(visible);
        }
        if (visible) {
            hiddenTopToggles.remove(TopToggleTypes.ADDRESS);
        } else {
            hiddenTopToggles.add(TopToggleTypes.ADDRESS);
        }
    }

    public void setBuilderToggleVisible(boolean visible) {
        if (aceFrame != null) {
            aceFrame.getCdePanel().setBuilderToggleVisible(visible);
        }
        if (visible) {
            hiddenTopToggles.remove(TopToggleTypes.BUILDER);
        } else {
            hiddenTopToggles.add(TopToggleTypes.BUILDER);
        }
    }

    public void setComponentToggleVisible(boolean visible) {
        if (aceFrame != null) {
            aceFrame.getCdePanel().setComponentToggleVisible(visible);
        }
        if (visible) {
            hiddenTopToggles.remove(TopToggleTypes.COMPONENT);
        } else {
            hiddenTopToggles.add(TopToggleTypes.COMPONENT);
        }
    }

    public void setHierarchyToggleVisible(boolean visible) {
        if (aceFrame != null) {
            aceFrame.getCdePanel().setHierarchyToggleVisible(visible);
        }
        if (visible) {
            hiddenTopToggles.remove(TopToggleTypes.TAXONOMY);
        } else {
            hiddenTopToggles.add(TopToggleTypes.TAXONOMY);
        }
    }

    public void setHistoryToggleVisible(boolean visible) {
        if (aceFrame != null) {
            aceFrame.getCdePanel().setHistoryToggleVisible(visible);
        }
        if (visible) {
            hiddenTopToggles.remove(TopToggleTypes.HISTORY);
        } else {
            hiddenTopToggles.add(TopToggleTypes.HISTORY);
        }
    }

    public void setInboxToggleVisible(boolean visible) {
        if (aceFrame != null) {
            aceFrame.getCdePanel().setInboxToggleVisible(visible);
        }
        if (visible) {
            hiddenTopToggles.remove(TopToggleTypes.INBOX);
        } else {
            hiddenTopToggles.add(TopToggleTypes.INBOX);
        }
    }

    public void setPreferencesToggleVisible(boolean visible) {
        if (aceFrame != null) {
            aceFrame.getCdePanel().setPreferencesToggleVisible(visible);
        }
        if (visible) {
            hiddenTopToggles.remove(TopToggleTypes.PREFERENCES);
        } else {
            hiddenTopToggles.add(TopToggleTypes.PREFERENCES);
        }
    }

    public void setSubversionToggleVisible(boolean visible) {
        if (aceFrame != null) {
            aceFrame.getCdePanel().setSubversionToggleVisible(visible);
        }
        if (visible) {
            hiddenTopToggles.remove(TopToggleTypes.SUBVERSION);
        } else {
            hiddenTopToggles.add(TopToggleTypes.SUBVERSION);
        }
    }

    public Set<REFSET_TYPES> getEnabledConceptExtTypes() {
        return enabledConceptExtTypes;
    }

    public Set<REFSET_TYPES> getEnabledDescExtTypes() {
        return enabledDescExtTypes;
    }

    public Set<REFSET_TYPES> getEnabledImageExtTypes() {
        return enabledImageExtTypes;
    }

    public Set<REFSET_TYPES> getEnabledRelExtTypes() {
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

    public void setRefsetInToggleVisible(REFSET_TYPES refsetType, TOGGLES toggle, boolean visible) {
        if (visible) {
            visibleRefsets.add(refsetType.name() + toggle.toString());
        } else {
            visibleRefsets.remove(refsetType.name() + toggle.toString());
        }
        changeSupport.firePropertyChange("visibleRefsets", null, visibleRefsets);
    }

    public boolean isRefsetInToggleVisible(REFSET_TYPES refsetType, TOGGLES toggle) {
        return visibleRefsets.contains(refsetType.name() + toggle.toString());
    }

    public I_HoldRefsetPreferences getRefsetPreferencesForToggle(TOGGLES toggle) throws TerminologyException,
            IOException {
        if (refsetPreferencesMap == null) {
            try {
                refsetPreferencesMap = setupRefsetPreferences();
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
        I_HoldRefsetPreferences pref = refsetPreferencesMap.get(toggle);
        if (pref == null) {
            pref = new RefsetPreferences();
            refsetPreferencesMap.put(toggle, pref);
        }
        return pref;
    }

    public void setCommitAbortButtonsVisible(boolean visible) {
        if (aceFrame != null && aceFrame.getCdePanel() != null) {
            aceFrame.getCdePanel().setCommitAbortButtonsVisible(visible);
        }
    }

    public Map<TOGGLES, I_HoldRefsetPreferences> getRefsetPreferencesMap() {
        return refsetPreferencesMap;
    }

    public I_IntList getRefsetsToShowInTaxonomy() {
        return refsetsToShowInTaxonomy;
    }

    public boolean getShowViewerImagesInTaxonomy() {
        return showViewerImagesInTaxonomy;
    }

    public void setShowViewerImagesInTaxonomy(Boolean showViewerImagesInTaxonomy) {
        Boolean old = this.showViewerImagesInTaxonomy;
        this.showViewerImagesInTaxonomy = showViewerImagesInTaxonomy;
        changeSupport.firePropertyChange("showViewerImagesInTaxonomy", old, showViewerImagesInTaxonomy);
    }

    public Boolean getVariableHeightTaxonomyView() {
        return variableHeightTaxonomyView;
    }

    public void setVariableHeightTaxonomyView(Boolean variableHeightTaxonomyView) {
        Boolean old = this.variableHeightTaxonomyView;
        this.variableHeightTaxonomyView = variableHeightTaxonomyView;
        changeSupport.firePropertyChange("variableHeightTaxonomyView", old, variableHeightTaxonomyView);
    }

    public Boolean getShowInferredInTaxonomy() {
        return showInferredInTaxonomy;
    }

    public void setShowInferredInTaxonomy(Boolean showInferredInTaxonomy) {
        Boolean old = this.showInferredInTaxonomy;
        this.showInferredInTaxonomy = showInferredInTaxonomy;
        changeSupport.firePropertyChange("showInferredInTaxonomy", old, showInferredInTaxonomy);
    }

    public Boolean getShowPathInfoInTaxonomy() {
        return showPathInfoInTaxonomy;
    }

    public void setShowPathInfoInTaxonomy(Boolean showPathInfoInTaxonomy) {
        Boolean old = this.showPathInfoInTaxonomy;
        this.showPathInfoInTaxonomy = showPathInfoInTaxonomy;
        changeSupport.firePropertyChange("showPathInfoInTaxonomy", old, showPathInfoInTaxonomy);
    }

    public Boolean getShowRefsetInfoInTaxonomy() {
        return showRefsetInfoInTaxonomy;
    }

    public void setShowRefsetInfoInTaxonomy(Boolean showRefsetInfoInTaxonomy) {
        Boolean old = this.showRefsetInfoInTaxonomy;
        this.showRefsetInfoInTaxonomy = showRefsetInfoInTaxonomy;
        changeSupport.firePropertyChange("showRefsetInfoInTaxonomy", old, showRefsetInfoInTaxonomy);
    }

    public I_IntList getRefsetsToSortTaxonomy() {
        return refsetsToSortTaxonomy;
    }

    public Boolean getSortTaxonomyUsingRefset() {
        return sortTaxonomyUsingRefset;
    }

    public void setSortTaxonomyUsingRefset(boolean sortTaxonomyUsingRefset) {
        this.sortTaxonomyUsingRefset = sortTaxonomyUsingRefset;
    }

    public void setSortTaxonomyUsingRefset(Boolean sortTaxonomyUsingRefset) {
        this.sortTaxonomyUsingRefset = sortTaxonomyUsingRefset;
    }

    public List<I_OverrideTaxonomyRenderer> getTaxonomyRendererOverrideList() {
        return taxonomyRendererOverrideList;
    }

    public List<I_FilterTaxonomyRels> getTaxonomyRelFilterList() {
        return taxonomyRelFilterList;
    }

    public Map<String, List<I_GetConceptData>> getTabHistoryMap() {
        return tabHistoryMap;
    }

    public void svnCheckout(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.checkout(svd, authenticator, interactive);
    }

    public void svnCleanup(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.cleanup(svd, authenticator, interactive);
    }

    public void svnCommit(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.commit(svd, authenticator, interactive);
    }

    public void svnPurge(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.purge(svd, authenticator, interactive);
    }

    public void svnStatus(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.status(svd, authenticator, interactive);
    }

    public void svnUpdate(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException {
        aceFrame.setupSvn();
        Svn.update(svd, authenticator, interactive);
    }

    public void closeFrame() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    if (aceFrame != null) {
                        aceFrame.setVisible(false);
                        aceFrame.dispose();
                        aceFrame = null;
                    }
                }
            });
        } catch (InterruptedException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (InvocationTargetException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    public void setFrameVisible(final boolean visible) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                if (aceFrame != null) {
                    aceFrame.setVisible(visible);
                } else if (visible == true) {
                    try {
                        aceFrame =
                                new AceFrame(new String[] { masterConfig.getAceRiverConfigFile() }, null,
                                    AceFrameConfig.this, false);
                    } catch (Exception e) {
                        AceLog.getAppLog().alertAndLogException(e);
                    }
                }
            }
        });

    }

    public Set<TopToggleTypes> getHiddenTopToggles() {
        return hiddenTopToggles;
    }

    public void setHiddenTopToggles(Set<TopToggleTypes> hiddenTopToggles) {
        this.hiddenTopToggles = hiddenTopToggles;
    }

    public BundleType getBundleType() {
        if (bundleType == null) {
            File profileDirSvn = new File("profiles" + File.separator + ".svn");
            File dbDirSvn = new File("berkeley-db" + File.separator + "mutable" + File.separator + ".svn");
            if (dbDirSvn.exists()) {
                bundleType = BundleType.DB_UPDATE;
            } else if (profileDirSvn.exists()) {
                bundleType = BundleType.CHANGE_SET_UPDATE;
            } else {
                bundleType = BundleType.STAND_ALONE;
            }
        }
        return bundleType;
    }

    /**
     * @return the conflict resolution strategy in use by the profile
     */
    public I_ManageContradiction getConflictResolutionStrategy() {
        if (contradictionStrategy == null) {
            contradictionStrategy = new IdentifyAllConflictStrategy();
        }
        contradictionStrategy.setConfig(this);
        return contradictionStrategy;
    }

    /**
     * Sets the conflict resolution strategy for this profile
     *
     * @param conflictResolutionStrategy
     */
    public void setConflictResolutionStrategy(I_ManageContradiction conflictResolutionStrategy) {

        I_ManageContradiction old = this.contradictionStrategy;

        this.contradictionStrategy = conflictResolutionStrategy;

        changeSupport.firePropertyChange("conflictResolutionStrategy", old, conflictResolutionStrategy);
    }

    /**
     * Sets the conflict resolution strategy for this profile
     *
     * @param contradictionStrategy
     */
    public <T extends I_ManageContradiction> void setConflictResolutionStrategy(Class<T> conflictResolutionStrategyClass) {

        try {
            setConflictResolutionStrategy(conflictResolutionStrategyClass.newInstance());
        } catch (InstantiationException e) {
            alertAndLog("Cannot instanciate resolution strategy of type '" + conflictResolutionStrategyClass
                + "', will continue with existing value '" + getConflictResolutionStrategy().getClass() + "'", e);
        } catch (IllegalAccessException e) {
            alertAndLog("Cannot instanciate resolution strategy of type '" + conflictResolutionStrategyClass
                + "' due to permissions, will continue with existing value '"
                + getConflictResolutionStrategy().getClass() + "'", e);
        }
    }

    private void alertAndLog(String message, Exception e) {
        Terms.get().getEditLog().alertAndLog(Level.WARNING, message, e);
    }

    public Boolean getHighlightConflictsInTaxonomyView() {
        return highlightConflictsInTaxonomyView;
    }

    public void setHighlightConflictsInTaxonomyView(Boolean highlightConflictsInTaxonomyView) {

        Boolean old = this.highlightConflictsInTaxonomyView;

        this.highlightConflictsInTaxonomyView = highlightConflictsInTaxonomyView;

        changeSupport.firePropertyChange("highlightConflictsInTaxonomyView", old, highlightConflictsInTaxonomyView);
    }

    public Boolean getHighlightConflictsInComponentPanel() {
        return highlightConflictsInComponentPanel;
    }

    public void setHighlightConflictsInComponentPanel(Boolean highlightConflictsInComponentPanel) {

        Boolean old = this.highlightConflictsInComponentPanel;

        this.highlightConflictsInComponentPanel = highlightConflictsInComponentPanel;

        changeSupport.firePropertyChange("highlightConflictsInComponentPanel", old, highlightConflictsInComponentPanel);
    }

    public I_ManageContradiction[] getAllConflictResolutionStrategies() {
        I_ManageContradiction[] strategies = new I_ManageContradiction[6];
        strategies[0] = new IdentifyAllConflictStrategy();
        strategies[1] = new LastCommitWinsConflictResolutionStrategy();
        strategies[2] = new ViewPathLosesStrategy();
        strategies[3] = new ViewPathWinsStrategy();
        strategies[4] = new EditPathLosesStrategy();
        strategies[5] = new EditPathWinsStrategy();
        return strategies;
    }

    public I_GetConceptData getRefsetInSpecEditor() {
        if (aceFrame != null && aceFrame.getCdePanel() != null)
            return aceFrame.getCdePanel().getRefsetInSpecEditor();
        return null;
    }

    public I_ExtendByRef getSelectedRefsetClauseInSpecEditor() {
        if (aceFrame != null && aceFrame.getCdePanel() != null)
            return aceFrame.getCdePanel().getSelectedRefsetClauseInSpecEditor();
        return null;
    }

    public JTree getTreeInSpecEditor() {
        if (aceFrame != null && aceFrame.getCdePanel() != null)
            return aceFrame.getCdePanel().getTreeInSpecEditor();
        return null;
    }

    public I_GetConceptData getRefsetSpecInSpecEditor() throws IOException, TerminologyException {
        if (aceFrame != null && aceFrame.getCdePanel() != null)
            return aceFrame.getCdePanel().getRefsetSpecInSpecEditor();
        return null;
    }

    public I_GetConceptData getClassificationRoleRoot() {
        return classificationRoleRoot;
    }

    public I_GetConceptData getClassificationRoot() {
        return classificationRoot;
    }

    public CLASSIFIER_INPUT_MODE_PREF getClassifierInputMode() {
        return classifierInputMode;
    }

    public I_GetConceptData getClassifierInputPath() {
        return classifierInputPathConcept;
    }

    public I_GetConceptData getClassifierIsaType() {
        return classifierIsaType;
    }

    public I_GetConceptData getClassifierOutputPath() {
        return classifierOutputPathConcept;
    }

    public void setClassificationRoleRoot(I_GetConceptData classificationRoleRoot) {
        Object old = this.classificationRoleRoot;
        this.classificationRoleRoot = classificationRoleRoot;
        changeSupport.firePropertyChange("classificationRoleRoot", old, classificationRoleRoot);
    }

    public void setClassificationRoot(I_GetConceptData classificationRoot) {
        Object old = this.classificationRoot;
        this.classificationRoot = classificationRoot;
        changeSupport.firePropertyChange("classificationRoot", old, classificationRoot);
    }

    public void setClassifierInputMode(CLASSIFIER_INPUT_MODE_PREF classifierInputMode) {
       Object old = this.classifierInputMode;
       this.classifierInputMode = classifierInputMode;
       changeSupport.firePropertyChange("classifierInputMode", old, classifierInputMode);
    }

   public void setClassifierInputPath(I_GetConceptData inputPath) {
        Object old = inputPath;
        classifierInputPathConcept = inputPath;
        changeSupport.firePropertyChange("classifierInputPath", old, inputPath);
    }

    public void setClassifierIsaType(I_GetConceptData classifierIsaType) {
        Object old = classifierIsaType;
        this.classifierIsaType = classifierIsaType;
        changeSupport.firePropertyChange("classifierIsaType", old, classifierIsaType);
    }

    public void setClassifierOutputPath(I_GetConceptData outputPath) {
        Object old = outputPath;
        this.classifierOutputPathConcept = outputPath;
        changeSupport.firePropertyChange("classifierOutputPath", old, outputPath);
    }

    public Color getColorForPath(int pathNid) {
        return pathColorMap.get(pathNid);
    }

    public void setColorForPath(int pathNid, Color pathColor) {
        pathColorMap.put(pathNid, pathColor);
    }

    public Map<Integer, Color> getPathColorMap() {
        return pathColorMap;
    }

    public I_IntList getLanguagePreferenceList() {
        if (languagePreferenceList == null) {
            languagePreferenceList = new IntList();
        }
        if (languagePreferenceList.size() == 0) {
        	try {
				languagePreferenceList.add(ArchitectonicAuxiliary.Concept.EN_US.localize().getNid());
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
        }
        return languagePreferenceList;
    }

    public void invalidate() {
        aceFrame.getCdePanel().invalidate();
    }

    public void repaint() {
        aceFrame.getCdePanel().repaint();
    }

    public void validate() {
        aceFrame.getCdePanel().validate();
    }

    public Map<String, Object> getProperties() throws IOException {
        return properties;
    }

    public Object getProperty(String key) throws IOException {
        return properties.get(key);
    }

    public void setProperty(String key, Object value) throws IOException {
        properties.put(key, value);
    }

    public void addConceptPanelPlugins(HOST_ENUM host, UUID key, I_PluginToConceptPanel plugin) {
        checkConceptPanelPlugins();
        conceptPanelPlugins.get(host).put(key, plugin);
    }

    private void checkConceptPanelPlugins() {
        if (conceptPanelPlugins == null) {
            conceptPanelPlugins = new HashMap<HOST_ENUM, Map<UUID, I_PluginToConceptPanel>>();
        }
        if (conceptPanelPlugins.size() < HOST_ENUM.values().length) {
            for (HOST_ENUM h : HOST_ENUM.values()) {
                if (conceptPanelPlugins.get(h) == null) {
                    conceptPanelPlugins.put(h, new HashMap<UUID, I_PluginToConceptPanel>());
                }
            }
        }
    }

    public I_PluginToConceptPanel removeConceptPanelPlugin(HOST_ENUM host, UUID key) {
        checkConceptPanelPlugins();
        return conceptPanelPlugins.get(host).remove(key);
    }

    public Set<UUID> getConceptPanelPluginKeys(HOST_ENUM host) {
        checkConceptPanelPlugins();
        return conceptPanelPlugins.get(host).keySet();
    }

    public I_PluginToConceptPanel getConceptPanelPlugin(HOST_ENUM host, UUID key) {
        checkConceptPanelPlugins();
        return conceptPanelPlugins.get(host).get(key);
    }

    public Collection<I_PluginToConceptPanel> getConceptPanelPlugins(HOST_ENUM host) {
        return conceptPanelPlugins.get(host).values();
    }

    public List<I_PluginToConceptPanel> getDefaultConceptPanelPluginsForEditor() {
        return staticGetDefaultConceptPanelPlugins();
    }

    public static List<I_PluginToConceptPanel> staticGetDefaultConceptPanelPlugins() {
        List<I_PluginToConceptPanel> list = new ArrayList<I_PluginToConceptPanel>();
        int order = 0;
        try {
            list.add(new IdPlugin(false, order++));
            list.add(new ConceptAttributePlugin(true, order++));
            list.add(new DescriptionPlugin(true, order++));
            list.add(new SrcRelPlugin(true, order++));
            list.add(new DestRelPlugin(false, order++));
            list.add(new LineagePlugin(true, order++));
            list.add(new GraphPlugin(false, order++));
            list.add(new ImagePlugin(false, order++));
            list.add(new ConflictPlugin(false, order++));
            list.add(new StatedAndNormalFormsPlugin(false, order++));
            list.add(new LanguageRefsetDisplayPlugin(false, order++, TOGGLES.AU_DIALECT, Terms.get().getConcept(
                ArchitectonicAuxiliary.Concept.EN_AU.getUids())));
            list.add(new LanguageRefsetDisplayPlugin(false, order++, TOGGLES.UK_DIALECT, Terms.get().getConcept(
                ArchitectonicAuxiliary.Concept.EN_GB.getUids())));
            list.add(new LanguageRefsetDisplayPlugin(false, order++, TOGGLES.USA_DIALECT, Terms.get().getConcept(
                ArchitectonicAuxiliary.Concept.EN_US.getUids())));
            list.add(new LanguageRefsetDisplayPlugin(false, order++, TOGGLES.NZ_DIALECT, Terms.get().getConcept(
                ArchitectonicAuxiliary.Concept.EN_NZ.getUids())));
            list.add(new LanguageRefsetDisplayPlugin(false, order++, TOGGLES.CA_DIALECT, Terms.get().getConcept(
                ArchitectonicAuxiliary.Concept.EN_CA.getUids())));
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (UnsupportedEncodingException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return list;
    }

    public List<I_PluginToConceptPanel> getDefaultConceptPanelPluginsForViewer() {
        return staticGetDefaultConceptPanelPluginsForViewer();
    }

    public static List<I_PluginToConceptPanel> staticGetDefaultConceptPanelPluginsForViewer() {
        List<I_PluginToConceptPanel> list = new ArrayList<I_PluginToConceptPanel>();
        int order = 0;
        try {
            list.add(new IdPlugin(false, order++));
            list.add(new ConceptAttributePlugin(true, order++));
            list.add(new DescriptionPlugin(true, order++));
            list.add(new SrcRelPlugin(true, order++));
            list.add(new DestRelPlugin(false, order++));
            list.add(new LineagePlugin(true, order++));
            list.add(new GraphPlugin(false, order++));
            list.add(new ImagePlugin(false, order++));
            list.add(new ConflictPlugin(false, order++));
            list.add(new StatedAndNormalFormsPlugin(false, order++));
            list.add(new LanguageRefsetDisplayPlugin(false, order++, TOGGLES.AU_DIALECT, Terms.get().getConcept(
                ArchitectonicAuxiliary.Concept.EN_AU.getUids())));
            list.add(new LanguageRefsetDisplayPlugin(false, order++, TOGGLES.UK_DIALECT, Terms.get().getConcept(
                ArchitectonicAuxiliary.Concept.EN_GB.getUids())));
            list.add(new LanguageRefsetDisplayPlugin(false, order++, TOGGLES.USA_DIALECT, Terms.get().getConcept(
                ArchitectonicAuxiliary.Concept.EN_US.getUids())));
            list.add(new LanguageRefsetDisplayPlugin(false, order++, TOGGLES.NZ_DIALECT, Terms.get().getConcept(
                ArchitectonicAuxiliary.Concept.EN_NZ.getUids())));
            list.add(new LanguageRefsetDisplayPlugin(false, order++, TOGGLES.CA_DIALECT, Terms.get().getConcept(
                ArchitectonicAuxiliary.Concept.EN_CA.getUids())));
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (UnsupportedEncodingException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return list;
    }

    public void fireRefsetSpecChanged(I_ExtendByRef ext) {
        changeSupport.firePropertyChange("refsetSpecChanged", null, ext);
    }

    public JTree getTreeInTaxonomyPanel() {
        return aceFrame.getCdePanel().getTree();
    }

    public void setRefsetInSpecEditor(I_GetConceptData refset) {
        aceFrame.getCdePanel().setRefsetInSpecEditor(refset);
    }

    public LANGUAGE_SORT_PREF getLanguageSortPref() {
        return langSortPref;
    }

    public void setLanguageSortPref(LANGUAGE_SORT_PREF langSortPref) {
        Object old = this.langSortPref;
        this.langSortPref = langSortPref;
        changeSupport.firePropertyChange("refsetSpecChanged", old, langSortPref);
    }

    public I_IntSet getPrefFilterTypesForRel() {
        return prefFilterTypesForRel;
    }

    public I_DescriptionTuple getSearchResultsSelection() {
        return aceFrame.getCdePanel().getSearchResultsSelection();
    }

    public void showRefsetSpecPanel() {
        aceFrame.getCdePanel().showRefsetSpecPanel();
    }

    public void setShowActivityViewer(boolean show) {
        aceFrame.getCdePanel().setShowActivityViewer(show);
    }

    public JPanel getWorkflowDetailsSheet() {
        return aceFrame.getCdePanel().getWorkflowDetailsSheet();
    }

    public void setShowWorkflowDetailSheet(boolean visible) {
        aceFrame.getCdePanel().setWorfklowDetailSheetVisible(visible);
    }

    public void setWorkflowDetailSheetDimensions(Dimension dim) {
        aceFrame.getCdePanel().setWorkflowDetailSheetDimensions(dim);
    }

    public void setTopActivity(I_ShowActivity activity) {
        if (aceFrame != null) {
            aceFrame.getCdePanel().setTopActivity(activity);
        }
    }

    public I_ShowActivity getTopActivity() {
        if (aceFrame != null) {
           return aceFrame.getCdePanel().getTopActivity();
        }
        return null;
    }

    public void fireUpdateHierarchyView() {
        changeSupport.firePropertyChange("updateHierarchyView", false, true);
    }

    public boolean searchWithDescTypeFilter() {
        return searchWithDescTypeFilter;
    }

    public void setSearchWithDescTypeFilter(boolean searchWithDescTypeFilter) {
        boolean old = this.searchWithDescTypeFilter;
        this.searchWithDescTypeFilter = searchWithDescTypeFilter;
        this.changeSupport.firePropertyChange("searchWithDescTypeFilter", old, searchWithDescTypeFilter);
    }

    public Precedence getPrecedence() {
        return precedence;
    }

    public void setPrecedence(Precedence precedence) {
        Precedence old = this.precedence;
        this.precedence = precedence;
        this.changeSupport.firePropertyChange("precedence", old, precedence);
        this.changeSupport.firePropertyChange("viewPositions", null, this.viewPositions);
    }

    public void setSelectedPreferencesTab(String tabName) {
        aceFrame.getCdePanel().setSelectedPreferencesTab(tabName);
    }

    @Override
    public PositionSetReadOnly getViewPositionSetReadOnly() {
        return new PositionSetReadOnly(getViewPositionSet());
    }

    @Override
    public PathSetReadOnly getEditingPathSetReadOnly() {
        return new PathSetReadOnly(getEditingPathSet());
    }

    @Override
    public PathSetReadOnly getPromotionPathSetReadOnly() {
        return new PathSetReadOnly(getPromotionPathSet());
    }

    @Override
    public Boolean getShowPromotionCheckBoxes() {
        return aceFrame.getCdePanel().getShowPromotionCheckBoxes();
    }

    @Override
    public void setShowPromotionCheckBoxes(Boolean show) {
        aceFrame.getCdePanel().setShowPromotionCheckBoxes(show);
    }

    @Override
    public void refreshRefsetTab() {
        aceFrame.getCdePanel().refreshRefsetTab();
    }

    @Override
    public Boolean getShowPromotionFilters() {
        return aceFrame.getCdePanel().getShowPromotionFilters();
    }

    @Override
    public Boolean getShowPromotionTab() {
        return aceFrame.getCdePanel().getShowPromotionTab();
    }

    @Override
    public void setEnabledAllQueuesButton(boolean enable) {
        aceFrame.getCdePanel().setEnabledAllQueuesButton(enable);
    }

    @Override
    public void setEnabledExistingInboxButton(boolean enable) {
        aceFrame.getCdePanel().setEnabledExistingInboxButton(enable);
    }

    @Override
    public void setEnabledMoveListenerButton(boolean enable) {
        aceFrame.getCdePanel().setEnabledMoveListenerButton(enable);
    }

    @Override
    public void setEnabledNewInboxButton(boolean enable) {
        aceFrame.getCdePanel().setEnabledNewInboxButton(enable);
    }

    @Override
    public void setShowPromotionFilters(Boolean show) {
        aceFrame.getCdePanel().setShowPromotionFilters(show);
    }

    @Override
    public void setShowPromotionTab(Boolean show) {
        aceFrame.getCdePanel().setShowPromotionTab(show);
    }

	@Override
	public ViewCoordinate getViewCoordinate() {
		if (languagePreferenceList != null && languagePreferenceList.size()>1) {
			return  new ViewCoordinate(getPrecedence(), getViewPositionSetReadOnly(),
					getAllowedStatus(), getDestRelTypes(),
                                        getConflictResolutionStrategy(),
                                        languagePreferenceList.get(0),
                                        classifierConcept.getConceptNid(),
                                        relAssertionType,
                                        getLanguagePreferenceList(),
                                        getLanguageSortPref().getLangSort());
		} else {
			return  new ViewCoordinate(getPrecedence(), getViewPositionSetReadOnly(),
					getAllowedStatus(), getDestRelTypes(),
                                        getConflictResolutionStrategy(),
                                        Integer.MAX_VALUE,
                                        classifierConcept.getConceptNid(),
                                        relAssertionType,
                                        getLanguagePreferenceList(),
                                        getLanguageSortPref().getLangSort());
		}
	}

	@Override
	public void quit() {
		aceFrame.getCdePanel().quit();
	}

        @Override
    public RelAssertionType getRelAssertionType() {
        return relAssertionType;
    }

        @Override
    public void setRelAssertionType(RelAssertionType relAssertionType) {

        Object old = this.relAssertionType;
        this.relAssertionType = relAssertionType;
        changeSupport.firePropertyChange("relAssertionType", old, relAssertionType);

    }

	@Override
	public boolean isAutoApproveOn() {

		return autoApprovedOn;
	}

	@Override
	public void setAutoApprove(boolean b) {

			this.autoApprovedOn = b;
	}


	@Override
	public boolean isOverrideOn() {

		return overrideOn;
	}

	@Override
	public void setOverride(boolean b) {

			this.overrideOn = b;
	}

	@Override
	public TreeSet<? extends I_GetConceptData> getWorkflowRoles() {
		if (workflowRoles == null)
			WorkflowHelper.updateWorkflowUserRoles();

		return workflowRoles;
	}

	@Override
	public void setWorkflowRoles(TreeSet<? extends I_GetConceptData> roles) {
		workflowRoles = roles;
	}

	@Override
	public TreeSet<? extends I_GetConceptData> getWorkflowStates() {
		if (workflowStates == null)
			WorkflowHelper.updateWorkflowStates();

		return workflowStates;
	}

	@Override
	public void setWorkflowStates(TreeSet<? extends I_GetConceptData> states) {
		workflowStates = states;
	}

	@Override
	public TreeSet<? extends I_GetConceptData> getWorkflowActions() {
		if (workflowActions == null)
			WorkflowHelper.updateWorkflowActions();

		return workflowActions;
	}

	@Override
	public void setWorkflowActions(TreeSet<? extends I_GetConceptData> actions) {
		workflowActions = actions;
	}

	@Override
	public TreeSet<UUID> getAllAvailableWorkflowActionUids() {
		if (availableWorkflowActions == null)
			WorkflowHelper.updateWorkflowActions();

		return availableWorkflowActions;
	}

	@Override
	public void setAllAvailableWorkflowActionUids(TreeSet<UUID> actions) {
		availableWorkflowActions = actions;
	}
}
