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
package org.dwfa.ace.task.profile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_PluginToConceptPanel;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_HostConceptPlugins.HOST_ENUM;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/profile", type = BeanType.TASK_BEAN) })
public class NewDefaultProfile extends NewProfile {

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            // nothing to do
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    protected I_ConfigAceFrame setupNewProfile(String fullName, String username, String password, String adminUsername,
            String adminPassword) throws TerminologyException, IOException {
        return newProfile(fullName, username, password, adminUsername, adminPassword);
    }

    public static I_ConfigAceFrame newProfile(String fullName, String username, String password, String adminUsername,
            String adminPassword) throws TerminologyException, IOException {

        I_ImplementTermFactory tf = (I_ImplementTermFactory) Terms.get();
        I_ConfigAceFrame activeConfig = tf.newAceFrameConfig();

        for (HOST_ENUM h : HOST_ENUM.values()) {
            for (I_PluginToConceptPanel plugin : activeConfig.getDefaultConceptPanelPluginsForEditor()) {
                activeConfig.addConceptPanelPlugins(h, plugin.getId(), plugin);
            }
        }

        I_ConfigAceDb newDbProfile = tf.newAceDbConfig();
        newDbProfile.setUsername(username);
        newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
        newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
        newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
        newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
        activeConfig.setDbConfig(newDbProfile);

        if (fullName == null || fullName.length() < 2) {
            fullName = "Full Name";
        }

        if (username == null || username.length() < 2) {
            username = "username";
        }
        if (password == null || password.length() < 2) {
            password = "sct";
        }
        if (adminUsername == null || adminUsername.length() < 2) {
            adminUsername = "admin";
            adminPassword = "visit.bend";
        }

        activeConfig.getDbConfig().setFullName(fullName);
        activeConfig.setUsername(username);
        activeConfig.setPassword(password);
        activeConfig.setAdminPassword(adminPassword);
        activeConfig.setAdminUsername(adminUsername);

        I_IntList statusPopupTypes = tf.newIntList();
        addIfNotNull(statusPopupTypes, Concept.ACTIVE, tf);
        addIfNotNull(statusPopupTypes, Concept.CURRENT, tf);
        addIfNotNull(statusPopupTypes, Concept.FLAGGED_FOR_REVIEW, tf);
        addIfNotNull(statusPopupTypes, Concept.LIMITED, tf);
        addIfNotNull(statusPopupTypes, Concept.PENDING_MOVE, tf);
        addIfNotNull(statusPopupTypes, Concept.INACTIVE, tf);
        addIfNotNull(statusPopupTypes, Concept.RETIRED, tf);
        addIfNotNull(statusPopupTypes, Concept.DUPLICATE, tf);
        addIfNotNull(statusPopupTypes, Concept.OUTDATED, tf);
        addIfNotNull(statusPopupTypes, Concept.AMBIGUOUS, tf);
        addIfNotNull(statusPopupTypes, Concept.ERRONEOUS, tf);
        addIfNotNull(statusPopupTypes, Concept.LIMITED, tf);
        addIfNotNull(statusPopupTypes, Concept.INAPPROPRIATE, tf);
        addIfNotNull(statusPopupTypes, Concept.MOVED_ELSEWHERE, tf);
        addIfNotNull(statusPopupTypes, Concept.PENDING_MOVE, tf);
        activeConfig.setEditStatusTypePopup(statusPopupTypes);

        I_IntList descPopupTypes = tf.newIntList();
        descPopupTypes.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.SYNONYM_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.UNSPECIFIED_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.ENTRY_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.XHTML_DEF.getUids()));
        activeConfig.setEditDescTypePopup(descPopupTypes);

        I_IntList imagePopupTypes = tf.newIntList();
        addIfNotNull(imagePopupTypes, Concept.AUXILLARY_IMAGE, tf);
        addIfNotNull(imagePopupTypes, Concept.VIEWER_IMAGE, tf);
        activeConfig.setEditImageTypePopup(imagePopupTypes);

        I_IntList relCharacteristic = tf.newIntList();
        relCharacteristic.add(tf.uuidToNative(Concept.STATED_RELATIONSHIP.getUids()));
        relCharacteristic.add(tf.uuidToNative(Concept.INFERRED_RELATIONSHIP.getUids()));
        relCharacteristic.add(tf.uuidToNative(Concept.QUALIFIER_CHARACTERISTIC.getUids()));
        relCharacteristic.add(tf.uuidToNative(Concept.HISTORICAL_CHARACTERISTIC.getUids()));
        relCharacteristic.add(tf.uuidToNative(Concept.ADDITIONAL_CHARACTERISTIC.getUids()));
        activeConfig.setEditRelCharacteristicPopup(relCharacteristic);

        I_IntList relRefinabilty = tf.newIntList();
        relRefinabilty.add(tf.uuidToNative(Concept.MANDATORY_REFINABILITY.getUids()));
        relRefinabilty.add(tf.uuidToNative(Concept.OPTIONAL_REFINABILITY.getUids()));
        relRefinabilty.add(tf.uuidToNative(Concept.NOT_REFINABLE.getUids()));
        activeConfig.setEditRelRefinabiltyPopup(relRefinabilty);

        I_IntList relTypes = tf.newIntList();
        relTypes.add(tf.uuidToNative(Concept.IS_A_REL.getUids()));

        try {
            relTypes.add(tf.uuidToNative(SNOMED.Concept.IS_A.getUids()));
            activeConfig.setClassifierIsaType(tf.getConcept(SNOMED.Concept.IS_A.getUids()));
            activeConfig.setClassificationRoot(tf.getConcept(SNOMED.Concept.ROOT.getUids()));
        } catch (NoMappingException e) {
            activeConfig.setClassifierIsaType(tf.getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids()));
            activeConfig.setClassificationRoot(tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
            // nothing else to do...
        }
        activeConfig.setEditRelTypePopup(relTypes);

        I_IntSet roots = tf.newIntSet();
        addIfNotNull(roots, Concept.ARCHITECTONIC_ROOT_CONCEPT, tf);
        addIfNotNull(roots, SNOMED.Concept.ROOT, tf);
        activeConfig.setRoots(roots);

        I_IntSet allowedStatus = tf.newIntSet();
        allowedStatus.add(tf.uuidToNative(Concept.ACTIVE.getUids()));
        addIfNotNull(allowedStatus, Concept.ADJUDICATED, tf);
        addIfNotNull(allowedStatus, Concept.ADJUDICATED_AND_PROCESSED, tf);
        addIfNotNull(allowedStatus, Concept.CONCEPT_RETIRED, tf);
        allowedStatus.add(tf.uuidToNative(Concept.CURRENT.getUids()));
        addIfNotNull(allowedStatus, Concept.DO_NOT_EDIT_INTERNAL_USE, tf);
        addIfNotNull(allowedStatus, Concept.DO_NOT_EDIT_FOR_RELEASE, tf);
        addIfNotNull(allowedStatus, Concept.DUAL_REVIEWED, tf);
        addIfNotNull(allowedStatus, Concept.DUAL_REVIEWED_AND_PROCESSED, tf);
        addIfNotNull(allowedStatus, Concept.DUPLICATE_PENDING_RETIREMENT, tf);
        addIfNotNull(allowedStatus, Concept.FLAGGED_FOR_REVIEW, tf);
        addIfNotNull(allowedStatus, Concept.FLAGGED_POTENTIAL_DESC_STYLE_ERROR, tf);
        addIfNotNull(allowedStatus, Concept.FLAGGED_POTENTIAL_DUPLICATE, tf);
        addIfNotNull(allowedStatus, Concept.FLAGGED_POTENTIAL_REL_ERROR, tf);
        addIfNotNull(allowedStatus, Concept.CURRENT_TEMP_INTERNAL_USE, tf);
        addIfNotNull(allowedStatus, Concept.CURRENT_UNREVIEWED, tf);
        addIfNotNull(allowedStatus, Concept.FLAGGED_FOR_DUAL_REVIEW, tf);
        addIfNotNull(allowedStatus, Concept.INTERNAL_USE_ONLY, tf);
        addIfNotNull(allowedStatus, Concept.LIMITED, tf);
        addIfNotNull(allowedStatus, Concept.PENDING_MOVE, tf);
        addIfNotNull(allowedStatus, Concept.PROCESSED, tf);
        addIfNotNull(allowedStatus, Concept.RESOLVED_IN_DUAL, tf);
        addIfNotNull(allowedStatus, Concept.PROMOTED, tf);
        addIfNotNull(allowedStatus, Concept.RESOLVED_IN_DUAL_AND_PROCESSED, tf);
        addIfNotNull(allowedStatus, Concept.CONFLICTING, tf);
        addIfNotNull(allowedStatus, Concept.CONSTANT, tf);
        addIfNotNull(allowedStatus, Concept.READY_TO_PROMOTE, tf);
        activeConfig.setAllowedStatus(allowedStatus);

        I_IntSet destRelTypes = tf.newIntSet();
        try {
            destRelTypes.add(tf.uuidToNative(SNOMED.Concept.IS_A.getUids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }
        destRelTypes.add(tf.uuidToNative(Concept.IS_A_REL.getUids()));

        activeConfig.setDestRelTypes(destRelTypes);

        I_IntSet sourceRelTypes = tf.newIntSet();
        activeConfig.setSourceRelTypes(sourceRelTypes);

        I_IntSet descTypes = tf.newIntSet();
        addIfNotNull(descTypes, Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE, tf);
        addIfNotNull(descTypes, Concept.PREFERRED_DESCRIPTION_TYPE, tf);
        addIfNotNull(descTypes, Concept.SYNONYM_DESCRIPTION_TYPE, tf);
        addIfNotNull(descTypes, Concept.DESCRIPTION_TYPE, tf);
        addIfNotNull(descTypes, Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE, tf);
        addIfNotNull(descTypes, Concept.XHTML_PREFERRED_DESC_TYPE, tf);
        addIfNotNull(descTypes, Concept.XHTML_DEF, tf);
        activeConfig.setDescTypes(descTypes);

        activeConfig.setDefaultDescriptionType(tf.getConcept(Concept.SYNONYM_DESCRIPTION_TYPE.getUids()));
        try {
            activeConfig.setDefaultImageType(tf.getConcept(Concept.AUXILLARY_IMAGE.getUids()));
        } catch (NoMappingException ex) {

        }

        activeConfig.setDefaultRelationshipCharacteristic(tf.getConcept(Concept.STATED_RELATIONSHIP.getUids()));
        activeConfig.setDefaultRelationshipRefinability(tf.getConcept(Concept.OPTIONAL_REFINABILITY.getUids()));

        activeConfig.setDefaultRelationshipType(tf.getConcept(Concept.IS_A_REL.getUids()));

        try {
            activeConfig.setDefaultRelationshipType(tf.getConcept(SNOMED.Concept.IS_A.getUids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }

        activeConfig.setDefaultStatus(tf.getConcept(Concept.ACTIVE.getUids()));

        I_IntList treeDescPrefList = activeConfig.getTreeDescPreferenceList();
        treeDescPrefList.add(tf.uuidToNative(Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        treeDescPrefList.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        treeDescPrefList.add(tf.uuidToNative(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        treeDescPrefList.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

        I_IntList shortLabelDescPrefList = activeConfig.getShortLabelDescPreferenceList();
        shortLabelDescPrefList.add(tf.uuidToNative(Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        shortLabelDescPrefList.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        shortLabelDescPrefList.add(tf.uuidToNative(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        shortLabelDescPrefList.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

        I_IntList longLabelDescPrefList = activeConfig.getLongLabelDescPreferenceList();
        longLabelDescPrefList.add(tf.uuidToNative(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        longLabelDescPrefList.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        longLabelDescPrefList.add(tf.uuidToNative(Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        longLabelDescPrefList.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));

        I_IntList tableDescPrefList = activeConfig.getTableDescPreferenceList();
        tableDescPrefList.add(tf.uuidToNative(Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        tableDescPrefList.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        tableDescPrefList.add(tf.uuidToNative(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        tableDescPrefList.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

        activeConfig.setDefaultStatus(tf.getConcept(Concept.CURRENT.getUids()));
        activeConfig.setDefaultDescriptionType(tf.getConcept(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

        activeConfig.setSubversionToggleVisible(false);

        activeConfig.setDefaultRelationshipType(tf.getConcept(Concept.IS_A_REL.getUids()));
        activeConfig.setDefaultRelationshipCharacteristic(tf.getConcept(Concept.STATED_RELATIONSHIP.getUids()));
        activeConfig.setDefaultRelationshipRefinability(tf.getConcept(Concept.OPTIONAL_REFINABILITY.getUids()));

        try {
            I_Path editPath = tf.getPath(Concept.ARCHITECTONIC_BRANCH.getUids());
            // activeConfig.addEditingPath(editPath);

            I_Position viewPosition = tf.newPosition(editPath, Integer.MAX_VALUE);
            Set<I_Position> viewSet = new HashSet<I_Position>();
            viewSet.add(viewPosition);
            activeConfig.setViewPositions(viewSet);
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        activeConfig.setTogglesInComponentPanelVisible(TOGGLES.ID, true);
        activeConfig.setTogglesInComponentPanelVisible(TOGGLES.ATTRIBUTES, true);
        activeConfig.setTogglesInComponentPanelVisible(TOGGLES.DESCRIPTIONS, true);
        activeConfig.setTogglesInComponentPanelVisible(TOGGLES.SOURCE_RELS, true);
        activeConfig.setTogglesInComponentPanelVisible(TOGGLES.DEST_RELS, true);
        activeConfig.setTogglesInComponentPanelVisible(TOGGLES.LINEAGE, true);
        activeConfig.setTogglesInComponentPanelVisible(TOGGLES.LINEAGE_GRAPH, false);
        activeConfig.setTogglesInComponentPanelVisible(TOGGLES.IMAGE, true);
        activeConfig.setTogglesInComponentPanelVisible(TOGGLES.CONFLICT, true);
        activeConfig.setTogglesInComponentPanelVisible(TOGGLES.STATED_INFERRED, false);
        activeConfig.setTogglesInComponentPanelVisible(TOGGLES.PREFERENCES, true);
        activeConfig.setTogglesInComponentPanelVisible(TOGGLES.HISTORY, true);
        activeConfig.setTogglesInComponentPanelVisible(TOGGLES.REFSETS, false);

        return activeConfig;
    }

}
