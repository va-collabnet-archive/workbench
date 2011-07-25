/*
 * Copyright 2010 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.bdb.mojo;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_PluginToConceptPanel;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_HostConceptPlugins.HOST_ENUM;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 *
 * @author kec
 */
public class DefaultRf1Rf2Config {

    public static I_ConfigAceFrame newProfile() throws TerminologyException, IOException {

        I_ImplementTermFactory tf = (I_ImplementTermFactory) Terms.get();
        I_ConfigAceFrame activeConfig = tf.newAceFrameConfig();

        for (HOST_ENUM h : HOST_ENUM.values()) {
            for (I_PluginToConceptPanel plugin : activeConfig.getDefaultConceptPanelPluginsForEditor()) {
                activeConfig.addConceptPanelPlugins(h, plugin.getId(), plugin);
            }
        }

        I_ConfigAceDb newDbProfile = tf.newAceDbConfig();
        newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
        newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
        newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
        newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
        activeConfig.setDbConfig(newDbProfile);
        newDbProfile.setUserConcept(Bdb.getConcept(Concept.USER.localize().getNid()));

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
        addIfNotNull(statusPopupTypes, Concept.PENDING_MOVE, tf);
      addIfNotNull(statusPopupTypes, SnomedMetadataRf2.INACTIVE_VALUE_RF2, tf);
		addIfNotNull(statusPopupTypes, SnomedMetadataRf2.ACTIVE_VALUE_RF2, tf);
		activeConfig.setEditStatusTypePopup(statusPopupTypes);

        I_IntList descPopupTypes = tf.newIntList();
        descPopupTypes.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.SYNONYM_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.UNSPECIFIED_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.ENTRY_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(Concept.XHTML_DEF.getUids()));

    	descPopupTypes.add(tf.uuidToNative(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()));
        descPopupTypes.add(tf.uuidToNative(SnomedMetadataRf2.SYNONYM_RF2.getUuids()));
        descPopupTypes.add(tf.uuidToNative(SnomedMetadataRf2.PREFERRED_RF2.getUuids()));
        descPopupTypes.add(tf.uuidToNative(SnomedMetadataRf2.ACCEPTABLE_RF2.getUuids()));
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
	    relCharacteristic.add(tf.uuidToNative(SnomedMetadataRf2.ADDITIONAL_RELATIONSHIP_RF2.getUuids()));
	    relCharacteristic.add(tf.uuidToNative(SnomedMetadataRf2.DEFINING_RELATIONSHIP_RF2.getUuids()));
	    relCharacteristic.add(tf.uuidToNative(SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getUuids()));
	    relCharacteristic.add(tf.uuidToNative(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids()));
	    relCharacteristic.add(tf.uuidToNative(SnomedMetadataRf2.QUALIFYING_RELATIONSSHIP_RF2.getUuids()));
        activeConfig.setEditRelCharacteristicPopup(relCharacteristic);

        I_IntList relRefinabilty = tf.newIntList();
        relRefinabilty.add(tf.uuidToNative(Concept.MANDATORY_REFINABILITY.getUids()));
        relRefinabilty.add(tf.uuidToNative(Concept.OPTIONAL_REFINABILITY.getUids()));
        relRefinabilty.add(tf.uuidToNative(Concept.NOT_REFINABLE.getUids()));
      
        relRefinabilty.add(tf.uuidToNative(SnomedMetadataRf2.NOT_REFINABLE_RF2.getUuids()));
        relRefinabilty.add(tf.uuidToNative(SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids()));
        relRefinabilty.add(tf.uuidToNative(SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2.getUuids()));
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
        allowedStatus.add(tf.uuidToNative(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()));
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
        descTypes.add(tf.uuidToNative(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()));
        descTypes.add(tf.uuidToNative(SnomedMetadataRf2.SYNONYM_RF2.getUuids()));
        descTypes.add(tf.uuidToNative(SnomedMetadataRf2.PREFERRED_RF2.getUuids()));
        descTypes.add(tf.uuidToNative(SnomedMetadataRf2.ACCEPTABLE_RF2.getUuids()));

        activeConfig.setDescTypes(descTypes);

//        activeConfig.setDefaultDescriptionType(tf.getConcept(Concept.SYNONYM_DESCRIPTION_TYPE.getUids()));
        activeConfig.setDefaultDescriptionType(tf.getConcept(SnomedMetadataRf2.SYNONYM_RF2.getUuids()));
        try {
            activeConfig.setDefaultImageType(tf.getConcept(Concept.AUXILLARY_IMAGE.getUids()));
        } catch (NoMappingException ex) {
        }

//        activeConfig.setDefaultRelationshipCharacteristic(tf.getConcept(Concept.STATED_RELATIONSHIP.getUids()));
//        activeConfig.setDefaultRelationshipRefinability(tf.getConcept(Concept.OPTIONAL_REFINABILITY.getUids()));
        activeConfig.setDefaultRelationshipCharacteristic(tf.getConcept(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids()));
        activeConfig.setDefaultRelationshipRefinability(tf.getConcept(SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids()));

        activeConfig.setDefaultRelationshipType(tf.getConcept(Concept.IS_A_REL.getUids()));

        try {
            activeConfig.setDefaultRelationshipType(tf.getConcept(SNOMED.Concept.IS_A.getUids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }

//        activeConfig.setDefaultStatus(tf.getConcept(Concept.ACTIVE.getUids()));
        activeConfig.setDefaultStatus(tf.getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()));

        I_IntList treeDescPrefList = activeConfig.getTreeDescPreferenceList();
        treeDescPrefList.add(tf.uuidToNative(Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        treeDescPrefList.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        treeDescPrefList.add(tf.uuidToNative(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        treeDescPrefList.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        treeDescPrefList.add(tf.uuidToNative(SnomedMetadataRf2.PREFERRED_RF2.getUuids()));
        treeDescPrefList.add(tf.uuidToNative(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()));

        I_IntList shortLabelDescPrefList = activeConfig.getShortLabelDescPreferenceList();
        shortLabelDescPrefList.add(tf.uuidToNative(Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        shortLabelDescPrefList.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        shortLabelDescPrefList.add(tf.uuidToNative(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        shortLabelDescPrefList.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        shortLabelDescPrefList.add(tf.uuidToNative(SnomedMetadataRf2.PREFERRED_RF2.getUuids()));
        shortLabelDescPrefList.add(tf.uuidToNative(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()));

        I_IntList longLabelDescPrefList = activeConfig.getLongLabelDescPreferenceList();
        longLabelDescPrefList.add(tf.uuidToNative(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        longLabelDescPrefList.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        longLabelDescPrefList.add(tf.uuidToNative(Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        longLabelDescPrefList.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        longLabelDescPrefList.add(tf.uuidToNative(SnomedMetadataRf2.PREFERRED_RF2.getUuids()));
        longLabelDescPrefList.add(tf.uuidToNative(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()));

        I_IntList tableDescPrefList = activeConfig.getTableDescPreferenceList();
        tableDescPrefList.add(tf.uuidToNative(Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        tableDescPrefList.add(tf.uuidToNative(Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        tableDescPrefList.add(tf.uuidToNative(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        tableDescPrefList.add(tf.uuidToNative(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        tableDescPrefList.add(tf.uuidToNative(SnomedMetadataRf2.PREFERRED_RF2.getUuids()));
        tableDescPrefList.add(tf.uuidToNative(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()));

        activeConfig.setDefaultStatus(tf.getConcept(Concept.CURRENT.getUids()));
        activeConfig.setDefaultDescriptionType(tf.getConcept(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        activeConfig.setDefaultStatus(tf.getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()));
        activeConfig.setDefaultDescriptionType(tf.getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()));

        activeConfig.setSubversionToggleVisible(false);

        activeConfig.setDefaultRelationshipType(tf.getConcept(Concept.IS_A_REL.getUids()));
        activeConfig.setDefaultRelationshipCharacteristic(tf.getConcept(Concept.STATED_RELATIONSHIP.getUids()));
        activeConfig.setDefaultRelationshipRefinability(tf.getConcept(Concept.OPTIONAL_REFINABILITY.getUids()));
        activeConfig.setDefaultRelationshipCharacteristic(tf.getConcept(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids()));
        activeConfig.setDefaultRelationshipRefinability(tf.getConcept(SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids()));

        try {
            PathBI editPath = tf.getPath(Concept.ARCHITECTONIC_BRANCH.getUids());
            // activeConfig.addEditingPath(editPath);

            PositionBI viewPosition = tf.newPosition(editPath, Long.MAX_VALUE);
            Set<PositionBI> viewSet = new HashSet<PositionBI>();
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

        activeConfig.setPrecedence(Precedence.PATH);

        return activeConfig;
    }

    private static void addIfNotNull(I_IntList intSet,
			ConceptSpec concept, I_ImplementTermFactory tf) throws TerminologyException, IOException {
        try {
            intSet.add(tf.uuidToNative(concept.getUuids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }
	}

	public static void addIfNotNull(I_IntSet intSet, I_ConceptualizeUniversally concept, I_TermFactory tf)
            throws TerminologyException, IOException {
        try {
            intSet.add(tf.uuidToNative(concept.getUids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }
    }

    public static void addIfNotNull(I_IntList roots, I_ConceptualizeUniversally concept, I_TermFactory tf)
            throws TerminologyException, IOException {
        try {
            roots.add(tf.uuidToNative(concept.getUids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }
    }
}
