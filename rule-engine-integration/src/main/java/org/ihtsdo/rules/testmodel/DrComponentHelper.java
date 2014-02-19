/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.rules.testmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.metadata.MetadataConversor;
import org.ihtsdo.qa.inheritance.RelationshipsDAO;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.RulesLibrary.INFERRED_VIEW_ORIGIN;
import org.ihtsdo.testmodel.*;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid.RefexCnidCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid_cnid.RefexCnidCnidCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid_str.RefexCnidCnidStrVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_float.RefexCnidFloatVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_int.RefexCnidIntVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_long.RefexCnidLongVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_str.RefexCnidStrVersionBI;
import org.ihtsdo.tk.api.refex.type_float.RefexFloatVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.tk.api.refex.type_str.RefexStrVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * The Class DrComponentHelper.
 */
public class DrComponentHelper {

    /**
     * The all rels.
     */
    public static I_IntSet allRels;
    /**
     * The hist rels.
     */
    public static I_IntSet histRels;
    /**
     * The Cpt model rels.
     */
    public static I_IntSet CptModelRels;

    /**
     * Gets the dr concept.
     *
     * @param conceptBi the concept bi
     * @param factContextName the fact context name
     * @param inferredOrigin the inferred origin
     * @return the dr concept
     */
    public static DrConcept getDrConcept(ConceptVersionBI conceptBi, String factContextName,
            INFERRED_VIEW_ORIGIN inferredOrigin) {
        I_TermFactory tf = Terms.get();
        TerminologyStoreDI ts = Ts.get();
        DrConcept concept = new DrConcept();
        MetadataConversor metadataConversor = new MetadataConversor();

        try {
            I_GetConceptData oldStyleConcept = tf.getConcept(conceptBi.getNid());
            I_ConfigAceFrame config = tf.getActiveAceFrameConfig();

//            ##Commented out to support KP path structure
//            Set<PositionBI> viewPositions = new HashSet<PositionBI>();
//            for (PathBI loopPath : config.getEditingPathSet()) {
//                PositionBI pos = Terms.get().newPosition(loopPath, Long.MAX_VALUE);
//                viewPositions.add(pos);
//            }
//            PositionSet mockViewSet = new PositionSet(viewPositions);
//            ViewCoordinate mockVc = new ViewCoordinate(config.getViewCoordinate());
//            mockVc.setPositionSet(mockViewSet);
            
            PositionSet mockViewSet = new PositionSet(config.getViewPositionSet());
            ViewCoordinate mockVc = config.getViewCoordinate();

            List<? extends I_ConceptAttributeTuple> attributeTuples = oldStyleConcept.getConceptAttributeTuples(null,
                    mockViewSet, config.getPrecedence(),
                    config.getConflictResolutionStrategy());

            if (attributeTuples != null && !attributeTuples.isEmpty()) {
                I_ConceptAttributeTuple attributeTuple = attributeTuples.iterator().next();
                concept.setDefined(attributeTuple.isDefined());
                concept.setPathUuid(tf.nidToUuid(attributeTuple.getPathNid()).toString());
                concept.setPrimordialUuid(attributeTuple.getPrimUuid().toString());
                concept.setStatusUuid(tf.nidToUuid(metadataConversor.getRf2Value(attributeTuple.getStatusNid())).toString());
                concept.setTime(attributeTuple.getTime());
                concept.setFactContextName(factContextName);
            }

            Collection<? extends DescriptionVersionBI> descriptionsList = oldStyleConcept.getDescriptionTuples(null,
                    null, mockViewSet,
                    config.getPrecedence(), config.getConflictResolutionStrategy());

            HashMap<Integer, DrLanguageDesignationSet> languageDesignationSetsMap = new HashMap<Integer, DrLanguageDesignationSet>();

            ConceptSpec referToRefset = new ConceptSpec("REFERS TO concept association reference set (foundation metadata concept)", UUID.fromString("d15fde65-ed52-3a73-926b-8981e9743ee9"));

            for (DescriptionVersionBI descriptionVersion : descriptionsList) {
                Collection<? extends RefexVersionBI<?>> currentAnnotations = descriptionVersion.getChronicle().getCurrentAnnotations(mockVc);
                for (RefexVersionBI<?> annotation : currentAnnotations) {
                    if (annotation instanceof RefexCnidVersionBI) {
                        RefexCnidVersionBI annotationCnid = (RefexCnidVersionBI) annotation;
                        int languageNid = annotationCnid.getCollectionNid();
                        if (!languageDesignationSetsMap.containsKey(languageNid) && annotationCnid.getCollectionNid() != referToRefset.getLenient().getNid()) {
                            DrLanguageDesignationSet langDefSet = new DrLanguageDesignationSet();
                            langDefSet.setLanguageRefsetUuid(tf.nidToUuid(annotationCnid.getCollectionNid()).toString());
                            languageDesignationSetsMap.put(languageNid, langDefSet);
                        }
                    }

                }
            }

            for (DescriptionVersionBI descriptionVersion : descriptionsList) {
                DrDescription loopDescription = new DrDescription();
                loopDescription.setAuthorUuid(tf.nidToUuid(descriptionVersion.getAuthorNid()).toString());
                loopDescription.setConceptUuid(tf.nidToUuid(descriptionVersion.getConceptNid()).toString());
                loopDescription.setInitialCaseSignificant(descriptionVersion.isInitialCaseSignificant());
                loopDescription.setLang(descriptionVersion.getLang());
                loopDescription.setText(descriptionVersion.getText());
                loopDescription.setTime(descriptionVersion.getTime());
                loopDescription.setStatusUuid(tf.nidToUuid(metadataConversor.getRf2Value(descriptionVersion.getStatusNid())).toString());
                loopDescription.setPathUuid(tf.nidToUuid(descriptionVersion.getPathNid()).toString());
                loopDescription.setPrimordialUuid(descriptionVersion.getPrimUuid().toString());
                loopDescription.setTypeUuid(tf.nidToUuid(metadataConversor.getRf2Value(descriptionVersion.getTypeNid())).toString());
                loopDescription.setPublished(!getSnomedIntId(descriptionVersion.getNid()).equals("0"));
                loopDescription.setFactContextName(factContextName);
                addAnnotationsToDescription(loopDescription, descriptionVersion, mockVc, factContextName);

                Collection<? extends RefexVersionBI<?>> currentAnnotations = descriptionVersion.getChronicle().getCurrentAnnotationMembers(mockVc);
                for (RefexVersionBI<?> annotation : currentAnnotations) {
                    if (annotation instanceof RefexCnidVersionBI) {
                        RefexCnidVersionBI annotationCnid = (RefexCnidVersionBI) annotation;
                        if (annotationCnid.getCollectionNid() == referToRefset.getLenient().getNid()) {
                            loopDescription.setReferToConceptUuid(tf.nativeToUuid(annotationCnid.getCnid1()).iterator().next().toString());
                        } else {
                            DrDescription langDescription = new DrDescription();
                            langDescription.setAuthorUuid(tf.nidToUuid(descriptionVersion.getAuthorNid()).toString());
                            langDescription.setConceptUuid(tf.nidToUuid(descriptionVersion.getConceptNid()).toString());
                            langDescription.setInitialCaseSignificant(descriptionVersion.isInitialCaseSignificant());
                            langDescription.setLang(descriptionVersion.getLang());
                            langDescription.setText(descriptionVersion.getText());
                            langDescription.setTime(descriptionVersion.getTime());
                            langDescription.setStatusUuid(tf.nidToUuid(metadataConversor.getRf2Value(descriptionVersion.getStatusNid())).toString());
                            langDescription.setPathUuid(tf.nidToUuid(descriptionVersion.getPathNid()).toString());
                            langDescription.setPrimordialUuid(descriptionVersion.getPrimUuid().toString());
                            langDescription.setTypeUuid(tf.nidToUuid(metadataConversor.getRf2Value(descriptionVersion.getTypeNid())).toString());
                            langDescription.setFactContextName(factContextName);

                            int languageNid = annotationCnid.getCollectionNid();
                            DrLanguageDesignationSet langDefSet = languageDesignationSetsMap.get(languageNid);
                            langDescription.setAcceptabilityUuid(tf.nidToUuid(metadataConversor.getRf2Value(annotationCnid.getCnid1())).toString());
                            langDescription.setLanguageRefsetUuid(tf.nidToUuid(annotationCnid.getCollectionNid()).toString());
                            langDefSet.getDescriptions().add(langDescription);
                        }
                    }
                }

                concept.getDescriptions().add(loopDescription);

            }

            for (DrLanguageDesignationSet langSet : languageDesignationSetsMap.values()) {
                concept.getLanguageDesignationSets().add(langSet);
            }

            if (allRels == null) {
                allRels = RulesLibrary.getAllRels();
            }

            int stated = SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid();
            int inferred = SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid();
            int historical = tf.uuidToNative(ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC.getUids());

            if (inferredOrigin == INFERRED_VIEW_ORIGIN.STATED) {
                for (RelationshipVersionBI<?> relTuple : oldStyleConcept.getSourceRelTuples(null,
                        null,
                        mockViewSet, config.getPrecedence(),
                        config.getConflictResolutionStrategy())) {
                    if (metadataConversor.getRf2Value(relTuple.getCharacteristicNid()) == stated) {
                        DrRelationship loopRel = new DrRelationship();
                        loopRel.setModifierUuid("someUuid");
                        loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
                        loopRel.setSourceUuid(tf.nidToUuid(relTuple.getOriginNid()).toString());
                        loopRel.setTargetUuid(tf.nidToUuid(relTuple.getDestinationNid()).toString());
                        loopRel.setCharacteristicUuid(tf.nidToUuid(metadataConversor.getRf2Value(relTuple.getCharacteristicNid())).toString());
                        loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
                        loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
                        loopRel.setRelGroup(relTuple.getGroup());
                        loopRel.setStatusUuid(tf.nidToUuid(metadataConversor.getRf2Value(relTuple.getStatusNid())).toString());
                        loopRel.setTime(relTuple.getTime());
                        loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
                        loopRel.setFactContextName(factContextName);
                        loopRel.setPublished(!getSnomedIntId(relTuple.getNid()).equals("0"));
                        concept.getOutgoingRelationships().add(loopRel);
                    }
                }
            } else if (inferredOrigin == INFERRED_VIEW_ORIGIN.INFERRED) {
                for (RelationshipVersionBI<?> relTuple : oldStyleConcept.getSourceRelTuples(null,
                        null,
                        mockViewSet, config.getPrecedence(),
                        config.getConflictResolutionStrategy())) {
                    if (metadataConversor.getRf2Value(relTuple.getCharacteristicNid()) == inferred) {
                        DrRelationship loopRel = new DrRelationship();
                        loopRel.setModifierUuid("someUuid");
                        loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
                        loopRel.setSourceUuid(tf.nidToUuid(relTuple.getOriginNid()).toString());
                        loopRel.setTargetUuid(tf.nidToUuid(relTuple.getDestinationNid()).toString());
                        loopRel.setCharacteristicUuid(tf.nidToUuid(metadataConversor.getRf2Value(relTuple.getCharacteristicNid())).toString());
                        loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
                        loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
                        loopRel.setRelGroup(relTuple.getGroup());
                        loopRel.setStatusUuid(tf.nidToUuid(metadataConversor.getRf2Value(relTuple.getStatusNid())).toString());
                        loopRel.setTime(relTuple.getTime());
                        loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
                        loopRel.setFactContextName(factContextName);
                        loopRel.setPublished(!getSnomedIntId(relTuple.getNid()).equals("0"));
                        concept.getOutgoingRelationships().add(loopRel);
                    }
                }
            } else if (inferredOrigin == INFERRED_VIEW_ORIGIN.CONSTRAINT_NORMAL_FORM) {
                RelationshipsDAO rDao = new RelationshipsDAO();
                concept.getOutgoingRelationships().addAll(rDao.getConstraintNormalForm(oldStyleConcept, factContextName));
                rDao = null;
                System.gc();
            }

            DrDefiningRolesSet statedRolesSet = new DrDefiningRolesSet();
            statedRolesSet.setRolesSetType("Stated");

            DrDefiningRolesSet inferredRolesSet = new DrDefiningRolesSet();
            inferredRolesSet.setRolesSetType("Inferred");

            DrDefiningRolesSet modelersRolesSet = new DrDefiningRolesSet();
            modelersRolesSet.setRolesSetType("Modelers");

            DrDefiningRolesSet definingFormRolesSet = new DrDefiningRolesSet();
            definingFormRolesSet.setRolesSetType("Defining");

            for (RelationshipVersionBI relTuple : oldStyleConcept.getSourceRelTuples(null,
                    null,
                    mockViewSet, config.getPrecedence(),
                    config.getConflictResolutionStrategy())) {
                DrRelationship loopRel = new DrRelationship();
                loopRel.setModifierUuid("someUuid");
                loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
                loopRel.setSourceUuid(tf.nidToUuid(relTuple.getOriginNid()).toString());
                loopRel.setTargetUuid(tf.nidToUuid(relTuple.getDestinationNid()).toString());
                loopRel.setCharacteristicUuid(tf.nidToUuid(metadataConversor.getRf2Value(relTuple.getCharacteristicNid())).toString());
                loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
                loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
                loopRel.setRelGroup(relTuple.getGroup());
                loopRel.setStatusUuid(tf.nidToUuid(metadataConversor.getRf2Value(relTuple.getStatusNid())).toString());
                loopRel.setTime(relTuple.getTime());
                loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
                loopRel.setFactContextName(factContextName);
                loopRel.setPublished(!getSnomedIntId(relTuple.getNid()).equals("0"));

                if (metadataConversor.getRf2Value(relTuple.getCharacteristicNid()) == historical
                        && config.getAllowedStatus().contains(metadataConversor.getRf2Value(relTuple.getStatusNid()))) {
                    concept.getOutgoingRelationships().add(loopRel);
                }

                if (metadataConversor.getRf2Value(relTuple.getCharacteristicNid()) == stated
                        && config.getAllowedStatus().contains(metadataConversor.getRf2Value(relTuple.getStatusNid()))) {
                    statedRolesSet.getRelationships().add(loopRel);
                }

                if (metadataConversor.getRf2Value(relTuple.getCharacteristicNid()) != inferred) {
                    modelersRolesSet.getRelationships().add(loopRel);
                }

                if (metadataConversor.getRf2Value(relTuple.getCharacteristicNid()) == inferred
                        && config.getAllowedStatus().contains(metadataConversor.getRf2Value(relTuple.getStatusNid()))) {
                    inferredRolesSet.getRelationships().add(loopRel);
                }
            }
            concept.getDefiningRoleSets().add(statedRolesSet);
            concept.getDefiningRoleSets().add(modelersRolesSet);
            concept.getDefiningRoleSets().add(inferredRolesSet);

            definingFormRolesSet.setRelationships(new ArrayList<DrRelationship>());
            definingFormRolesSet.getRelationships().addAll(concept.getOutgoingRelationships());
            concept.getDefiningRoleSets().add(definingFormRolesSet);
            //TODO: incoming rels is heavy on performance moved to helper method
            //			for (RelationshipVersionBI relTuple :  oldStyleConcept.getDestRelTuples(config.getAllowedStatus(), 
            //					null, 
            //					mockViewSet, config.getPrecedence(), 
            //					config.getConflictResolutionStrategy())) {
            //				if (relTuple.getCharacteristicNid() == historical) {
            //					DrRelationship loopRel = new DrRelationship();
            //					loopRel.setModifierUuid("someUuid");
            //					loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
            //					loopRel.setSourceUuid(tf.nidToUuid(relTuple.getOriginNid()).toString());
            //					loopRel.setTargetUuid(tf.nidToUuid(relTuple.getDestinationNid()).toString());
            //					loopRel.setCharacteristicUuid(tf.nidToUuid(relTuple.getCharacteristicNid()).toString());
            //					loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
            //					loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
            //					loopRel.setRelGroup(relTuple.getGroup());
            //					loopRel.setStatusUuid(tf.nidToUuid(relTuple.getStatusNid()).toString());
            //					loopRel.setTime(relTuple.getTime());
            //					loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
            //					loopRel.setFactContextName(factContextName);
            //					concept.getIncomingRelationships().add(loopRel);
            //				}
            //			}
            
            addAnnotationsToConcept(concept, conceptBi, mockVc, factContextName);

        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }

        return concept;

    }
    
    private static void addAnnotationsToConcept(DrConcept concept, ConceptVersionBI componentBi, 
            ViewCoordinate mockVc, String factContextName) throws IOException, ContradictionException {
        I_TermFactory tf = Terms.get();
        if (componentBi != null && componentBi.getConAttrsActive() != null) {
        Collection<? extends RefexVersionBI<?>> annotations = componentBi.getConAttrsActive().getCurrentAnnotationMembers(mockVc);
            
            for (RefexVersionBI annotation : annotations) {
                DrRefsetExtension extension = new DrRefsetExtension();
                extension.setActive(true);
                extension.setComponentUuid(tf.nidToUuid(annotation.getReferencedComponentNid()).toString());
                extension.setRefsetUuid(tf.nidToUuid(annotation.getCollectionNid()).toString());
                extension.setPrimordialUuid(annotation.getPrimUuid().toString());
                extension.setFactContextName(factContextName);                
                if (annotation instanceof RefexCnidVersionBI) {
                    RefexCnidVersionBI annotationTyped = (RefexCnidVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexCnidCnidCnidVersionBI) {
                    RefexCnidCnidCnidVersionBI annotationTyped = (RefexCnidCnidCnidVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setC2Uuid(tf.nidToUuid(annotationTyped.getCnid2()).toString());
                    extension.setC3Uuid(tf.nidToUuid(annotationTyped.getCnid3()).toString());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexCnidCnidStrVersionBI) {
                    RefexCnidCnidStrVersionBI annotationTyped = (RefexCnidCnidStrVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setC2Uuid(tf.nidToUuid(annotationTyped.getCnid2()).toString());
                    extension.setStrValue(annotationTyped.getStr1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexCnidCnidVersionBI) {
                    RefexCnidCnidVersionBI annotationTyped = (RefexCnidCnidVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setC2Uuid(tf.nidToUuid(annotationTyped.getCnid2()).toString());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexCnidFloatVersionBI) {
                    RefexCnidFloatVersionBI annotationTyped = (RefexCnidFloatVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setFloatValue(annotationTyped.getFloat1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexCnidIntVersionBI) {
                    RefexCnidIntVersionBI annotationTyped = (RefexCnidIntVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setIntValue(annotationTyped.getInt1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexCnidLongVersionBI) {
                    RefexCnidLongVersionBI annotationTyped = (RefexCnidLongVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setLongValue(annotationTyped.getLong1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexCnidStrVersionBI) {
                    RefexCnidStrVersionBI annotationTyped = (RefexCnidStrVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setStrValue(annotationTyped.getStr1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexFloatVersionBI) {
                    RefexFloatVersionBI annotationTyped = (RefexFloatVersionBI) annotation;
                    extension.setFloatValue(annotationTyped.getFloat1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexIntVersionBI) {
                    RefexIntVersionBI annotationTyped = (RefexIntVersionBI) annotation;
                    extension.setIntValue(annotationTyped.getInt1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexLongVersionBI) {
                    RefexLongVersionBI annotationTyped = (RefexLongVersionBI) annotation;
                    extension.setLongValue(annotationTyped.getLong1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexStrVersionBI) {
                    RefexStrVersionBI annotationTyped = (RefexStrVersionBI) annotation;
                    extension.setStrValue(annotationTyped.getStr1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else {
                    // unknown refset
                }
                if (concept.getExtensions() == null) {
                    concept.setExtensions(new ArrayList<DrRefsetExtension>());
                }
                
                concept.getExtensions().add(extension);
            }
        }
    }
    
    private static void addAnnotationsToDescription(DrDescription description, DescriptionVersionBI componentBi, 
            ViewCoordinate mockVc, String factContextName) throws IOException {
        I_TermFactory tf = Terms.get();
        Collection<? extends RefexVersionBI<?>> annotations = componentBi.getCurrentAnnotationMembers(mockVc);
            
            for (RefexVersionBI annotation : annotations) {
                DrRefsetExtension extension = new DrRefsetExtension();
                extension.setActive(true);
                extension.setComponentUuid(tf.nidToUuid(annotation.getReferencedComponentNid()).toString());
                extension.setRefsetUuid(tf.nidToUuid(annotation.getCollectionNid()).toString());
                extension.setPrimordialUuid(annotation.getPrimUuid().toString());
                extension.setFactContextName(factContextName);                
                if (annotation instanceof RefexCnidVersionBI) {
                    RefexCnidVersionBI annotationTyped = (RefexCnidVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexCnidCnidCnidVersionBI) {
                    RefexCnidCnidCnidVersionBI annotationTyped = (RefexCnidCnidCnidVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setC2Uuid(tf.nidToUuid(annotationTyped.getCnid2()).toString());
                    extension.setC3Uuid(tf.nidToUuid(annotationTyped.getCnid3()).toString());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexCnidCnidStrVersionBI) {
                    RefexCnidCnidStrVersionBI annotationTyped = (RefexCnidCnidStrVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setC2Uuid(tf.nidToUuid(annotationTyped.getCnid2()).toString());
                    extension.setStrValue(annotationTyped.getStr1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexCnidCnidVersionBI) {
                    RefexCnidCnidVersionBI annotationTyped = (RefexCnidCnidVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setC2Uuid(tf.nidToUuid(annotationTyped.getCnid2()).toString());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexCnidFloatVersionBI) {
                    RefexCnidFloatVersionBI annotationTyped = (RefexCnidFloatVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setFloatValue(annotationTyped.getFloat1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexCnidIntVersionBI) {
                    RefexCnidIntVersionBI annotationTyped = (RefexCnidIntVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setIntValue(annotationTyped.getInt1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexCnidLongVersionBI) {
                    RefexCnidLongVersionBI annotationTyped = (RefexCnidLongVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setLongValue(annotationTyped.getLong1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexCnidStrVersionBI) {
                    RefexCnidStrVersionBI annotationTyped = (RefexCnidStrVersionBI) annotation;
                    extension.setC1Uuid(tf.nidToUuid(annotationTyped.getCnid1()).toString());
                    extension.setStrValue(annotationTyped.getStr1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexFloatVersionBI) {
                    RefexFloatVersionBI annotationTyped = (RefexFloatVersionBI) annotation;
                    extension.setFloatValue(annotationTyped.getFloat1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexIntVersionBI) {
                    RefexIntVersionBI annotationTyped = (RefexIntVersionBI) annotation;
                    extension.setIntValue(annotationTyped.getInt1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexLongVersionBI) {
                    RefexLongVersionBI annotationTyped = (RefexLongVersionBI) annotation;
                    extension.setLongValue(annotationTyped.getLong1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else if (annotation instanceof RefexStrVersionBI) {
                    RefexStrVersionBI annotationTyped = (RefexStrVersionBI) annotation;
                    extension.setStrValue(annotationTyped.getStr1());
                    extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
                    extension.setTime(annotationTyped.getTime());
                    extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
                } else {
                    // unknown refset
                }
                if (description.getExtensions() == null) {
                    description.setExtensions(new ArrayList<DrRefsetExtension>());
                }
                description.getExtensions().add(extension);
            }
    }

    /**
     * Gets the descendants.
     *
     * @param descendants the descendants
     * @param concept the concept
     * @return the descendants
     */
    public static Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
        try {
            I_TermFactory termFactory = Terms.get();
            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            Set<PositionBI> viewPositions = new HashSet<PositionBI>();
            for (PathBI loopPath : config.getEditingPathSet()) {
                PositionBI pos = termFactory.newPosition(loopPath, Long.MAX_VALUE);
                viewPositions.add(pos);
            }
            PositionSet mockViewSet = new PositionSet(viewPositions);
            Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
            childrenSet.addAll(concept.getDestRelOrigins(config.getAllowedStatus(),
                    config.getDestRelTypes(), mockViewSet, config.getPrecedence(), config.getConflictResolutionStrategy()));
            descendants.addAll(childrenSet);
            for (I_GetConceptData loopConcept : childrenSet) {
                descendants = getDescendants(descendants, loopConcept);
            }
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return descendants;
    }

    /**
     * Gets the snomed int id.
     *
     * @param nid the nid
     * @return the snomed int id
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public static String getSnomedIntId(int nid) throws IOException, TerminologyException {
        Long descriptionId = 0L; //If description is new then descriptionid doesn't exist in workbench so use dummy value.
        I_Identify desc_Identify = Terms.get().getId(nid);
        List<? extends I_IdVersion> i_IdentifyList = desc_Identify.getIdVersions();
        if (i_IdentifyList.size() > 0) {
            for (int i = 0; i < i_IdentifyList.size(); i++) {
                I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
                Object denotation = (Object) i_IdVersion.getDenotation();
				Long idval = null;
				Object ido = i_IdVersion.getDenotation();
				if (ido instanceof String) {
					idval = new Long((String) ido);
				} else if (ido instanceof Long) {
					idval = ((Long) ido);
				}

				int authorityNid = i_IdVersion.getAuthorityNid();
                int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
                if (authorityNid == arcAuxSnomedIntegerNid) {
                    descriptionId = idval;
                }
            }
        }
        return descriptionId.toString();
    }
}
