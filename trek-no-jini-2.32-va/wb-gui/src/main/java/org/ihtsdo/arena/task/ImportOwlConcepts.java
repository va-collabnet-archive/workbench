/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.arena.task;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.helper.dialect.DialectHelper;
import org.ihtsdo.helper.dialect.UnsupportedDialectOrLanguage;
import org.ihtsdo.helper.owl.OwlConcept;
import org.ihtsdo.helper.owl.OwlConceptConverter;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.Language;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipType;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * Imports OwlConcepts and creates a changeset. Will perform a commit of all
 * uncommitted changes. Should check for uncommitted changes first before adding
 * this task to a bp.
 */
@BeanList(specs = {
    @Spec(directory = "tasks/owl", type = BeanType.TASK_BEAN)})
public class ImportOwlConcepts extends AbstractTask {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    private String fileProp = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();
    String parentConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    private HashMap<String, OwlConcept> owlConcepts;
    UUID topParentUuid;
    String semanticTag;
    int existingRels = 0;
    int existingConcepts = 0;
    int importCount = 0;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(fileProp);
        out.writeObject(parentConceptPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            fileProp = (String) in.readObject();
            parentConceptPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException, ContradictionException {

        new Thread(
                new Runnable() {
            @Override
            public void run() {
                try {
                    doImport(process, worker);
                } catch (TaskFailedException ex) {
                    worker.getLogger().log(Level.SEVERE, null, ex);
                }
            }
        }).start();
        return Condition.CONTINUE;
    }

    private void doImport(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            Ts.get().suspendChangeNotifications();
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            String fileName = (String) process.getProperty(
                    ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey());
            File file = new File(fileName);
            topParentUuid = (UUID) process.getProperty(
                    ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey());
            ConceptVersionBI topParent = Ts.get().getConceptVersion(config.getViewCoordinate(), topParentUuid);
            semanticTag = topParent.getDescriptionFullySpecified().getText();
            semanticTag = semanticTag.substring(semanticTag.lastIndexOf("("), semanticTag.lastIndexOf(")") + 1);

            //get concepts in OWL to import
            OwlConceptConverter converter = new OwlConceptConverter(file);
            converter.convert();
            owlConcepts = converter.getOwlConcepts();
            config.setStatusMessage("Preparing to import " + owlConcepts.size() + " concepts ...");
            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());
            for (OwlConcept owl : owlConcepts.values()) {
                //assuming sctId present means concept exists
                if (owl.getSctId() != null) {
                    Set<ConceptChronicleBI> concepts = Ts.get().getConceptChronicle(owl.getSctId().toString());
                    if (concepts.size() == 1) {
                        ConceptChronicleBI concept = concepts.iterator().next();
                        HashSet<UUID> relsSeen = new HashSet<>();
                        //get parents
                        for (String parentId : owl.getParents()) {
                            OwlConcept parentOwl = owlConcepts.get(parentId);
                            if (parentOwl != null) {
                                //assuming sctId present means parent concept exists
                                if (parentOwl.getSctId() != null) {
                                    Set<ConceptChronicleBI> parentConcepts = Ts.get().getConceptChronicle(parentOwl.getSctId().toString());
                                    if (parentConcepts.isEmpty()) {
                                        worker.getLogger().info("No concept found for parent: "
                                                + parentOwl.getFsn());
                                    } else if (parentConcepts.size() == 1) { //add new relationship
                                        ConceptChronicleBI parent = parentConcepts.iterator().next();
                                        //check if relationship exists
                                        ConceptVersionBI conceptVersion = concept.getVersion(config.getViewCoordinate());
                                        boolean addRel = true;
                                        for (RelationshipVersionBI rel : conceptVersion.getRelationshipsOutgoingActiveIsa()) {
                                            if (rel.getTargetNid() == parent.getConceptNid()) {
                                                addRel = false;
                                                existingRels++;
                                                relsSeen.add(rel.getPrimUuid());
                                                break;
                                            }
                                        }
                                        //add new
                                        if (addRel) {
                                            RelationshipCAB rel = new RelationshipCAB(
                                                    concept.getConceptNid(),
                                                    Snomed.IS_A.getLenient().getConceptNid(),
                                                    parent.getConceptNid(),
                                                    0,
                                                    TkRelationshipType.STATED_HIERARCHY);
                                            RelationshipChronicleBI newRel = builder.construct(rel);
                                            relsSeen.add(newRel.getPrimUuid());
//                                        Ts.get().addUncommittedNoChecks(Ts.get().getConcept(rel.getSourceNid()));
                                            importCount++;
                                        }
                                    } else {
                                        worker.getLogger().info("More than one concept found for SCT ID: "
                                                + parentOwl.getSctId() + " Concepts: " + parentConcepts);
                                    }
                                } else { //create parent concept and add child relationship
                                    UUID parentUuid = parentOwl.getConceptUuid();
                                    if (parentUuid == null) {
                                        parentUuid = addNewConcept(parentOwl, worker, builder);
                                    }
                                    RelationshipCAB relBp = new RelationshipCAB(
                                            concept.getConceptNid(),
                                            Snomed.IS_A.getLenient().getConceptNid(),
                                            Ts.get().getNidForUuids(parentUuid),
                                            0,
                                            TkRelationshipType.STATED_HIERARCHY);
                                    //check for previously imported
                                    if (!Ts.get().hasUuid(relBp.getComponentUuid())) {
                                        builder.construct(relBp);
                                        relsSeen.add(relBp.getComponentUuid());
//                                    Ts.get().addUncommittedNoChecks(Ts.get().getConcept(relBp.getSourceNid()));
                                        importCount++;
                                    } else {
                                        relsSeen.add(relBp.getComponentUuid());
                                        existingRels++;
                                    }
                                }
                            } else if (parentId.equalsIgnoreCase("thing")) {
                                //check if relationship exists
                                ConceptVersionBI conceptVersion = concept.getVersion(config.getViewCoordinate());
                                boolean addRel = true;
                                for (RelationshipVersionBI rel : conceptVersion.getRelationshipsOutgoingActiveIsa()) {
                                    if (rel.getTargetNid() == Ts.get().getNidForUuids(topParentUuid)) {
                                        addRel = false;
                                        relsSeen.add(rel.getPrimUuid());
                                        existingRels++;
                                        break;
                                    }
                                }
                                //add new
                                if (addRel) {
                                    RelationshipCAB relBp = new RelationshipCAB(
                                            concept.getConceptNid(),
                                            Snomed.IS_A.getLenient().getConceptNid(),
                                            Ts.get().getNidForUuids(topParentUuid),
                                            0,
                                            TkRelationshipType.STATED_HIERARCHY);
                                    RelationshipChronicleBI newRel = builder.construct(relBp);
                                    relsSeen.add(newRel.getPrimUuid());
//                                Ts.get().addUncommittedNoChecks(Ts.get().getConcept(relBp.getSourceNid()));
                                    importCount++;
                                }
                            } else {
                                worker.getLogger().info("No parent concepts in file. For concept "
                                        + owl.getFsn());
                            }
                        }
                        //retire exisitng is-a rels that aren't part of definition
                        ConceptVersionBI conceptVersion = concept.getVersion(config.getViewCoordinate());
                        for (RelationshipVersionBI rel : conceptVersion.getRelationshipsOutgoingActiveIsa()) {
                            if (!relsSeen.contains(rel.getPrimUuid()) 
                                    && rel.getCharacteristicNid() != SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID()
                                    && rel.getTypeNid() == Snomed.IS_A.getLenient().getConceptNid()) {
                                RelationshipCAB relBp = rel.makeBlueprint(config.getViewCoordinate());
                                relBp.setRetired();
                                relBp.setComponentUuidNoRecompute(rel.getPrimUuid());
                                RelationshipChronicleBI construct = builder.construct(relBp);
                            }
                        }
                        Ts.get().addUncommittedNoChecks(conceptVersion);
                    } else if (concepts.isEmpty()) {
                        worker.getLogger().info("No concepts found for SCT ID: "
                                + owl.getSctId());
                    } else {
                        worker.getLogger().info("More than one concept found for SCT ID: "
                                + owl.getSctId() + " Concepts: " + concepts);
                    }
                } else {
                    //create new concept
                    if (owl.getConceptUuid() != null) {
                        addNewConcept(owl, worker, builder);
                    }
                }
            }
            Ts.get().resumeChangeNotifications();
            Ts.get().commit();
            config.setStatusMessage("Import complete. Total imported: " + importCount
                    + " Existing rels: " + existingRels + " Existing concepts: " + existingConcepts);
            importCount = 0;
            existingRels = 0;
            existingConcepts = 0;
        } catch (IntrospectionException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | IOException | ContradictionException | ParseException | InvalidCAB | TaskFailedException | UnsupportedDialectOrLanguage | NoSuchAlgorithmException ex) {
            throw new TaskFailedException(ex);
        }

    }

    private UUID addNewConcept(OwlConcept owl, I_Work worker, TerminologyBuilderBI builder) throws IOException,
            InvalidCAB, ContradictionException, ParseException, TaskFailedException,
            ValidationException, UnsupportedDialectOrLanguage, NoSuchAlgorithmException {
        ArrayList<String> parents = owl.getParents();
        UUID[] parentUuids = new UUID[parents.size()];
        int count = 0;
        boolean add = true;
        for (String parentId : parents) {
            //check for topmost parent
            if (parentId.equalsIgnoreCase("thing")) {
                parentUuids[count++] = topParentUuid;
            } else {
                if (owl.getId().equals(parentId)) {
                    System.out.println("PARENT AND CHILD EQUAL");
                    throw new TaskFailedException("Parent and child equal. Stopping import. " + owl);
                }
                OwlConcept parentOwl = owlConcepts.get(parentId);
                //add parent to concept if exists
                if (parentOwl.getSctId() != null) {
                    Set<ConceptChronicleBI> parentConcepts = Ts.get().getConceptChronicle(parentOwl.getSctId().toString());
                    if (parentConcepts.isEmpty()) {
                        worker.getLogger().info("No concept found for parent: "
                                + parentOwl.getFsn());
                        add = false;
                    } else if (parentConcepts.size() == 1) {
                        ConceptChronicleBI parent = parentConcepts.iterator().next();
                        if (parent.getPrimUuid() != null) {
                            parentUuids[count++] = parent.getPrimUuid();
                        }
                    } else {
                        worker.getLogger().info("More than one concept found for SCT ID: "
                                + parentOwl.getSctId() + " Concepts: " + parentConcepts);
                        add = false;
                    }
                } else { //create parent
                    UUID parentUuid = addNewConcept(parentOwl, worker, builder);
                    if (parentUuid != null) {
                        parentUuids[count++] = parentOwl.getConceptUuid();
                    }
                }
            }
        }
        //create concept
        if (add) {
            if (owl.getConceptUuid() == null) {
                if (owl.getFsn() == null) {
                    String fsn = owl.getLabel() + " " + semanticTag;
                    fsn.trim();
                    owl.setFsn(fsn);
                }
                if (owl.getLabel() == null) {
                    String label = owl.getFsn().replace(semanticTag, "");
                    label.trim();
                    owl.setLabel(label);
                }
                ConceptCB newConceptBp = new ConceptCB(owl.getFsn(),
                        owl.getLabel(),
                        LANG_CODE.EN,
                        Snomed.IS_A.getLenient().getPrimUuid(),
                        parentUuids);
                try {
                    //check for previous import
                    if (!Ts.get().hasUuid(newConceptBp.getComponentUuid())) {
                        addLanguageRefexes(newConceptBp);
                        ConceptChronicleBI concept = builder.construct(newConceptBp);
                        Ts.get().addUncommittedNoChecks(concept);
                        importCount++;
                    } else {
                        existingConcepts++;
                    }
                    owl.setConceptUuid(newConceptBp.getComponentUuid());
                } catch (InvalidCAB e) {
                    ConceptChronicleBI concept = Ts.get().getConcept(newConceptBp.getComponentUuid());
                    owl.setConceptUuid(concept.getPrimUuid());
                    existingConcepts++;
                } catch (AssertionError e) {
                    ConceptChronicleBI concept = Ts.get().getConcept(newConceptBp.getComponentUuid());
                    owl.setConceptUuid(concept.getPrimUuid());
                    existingConcepts++;
                }
            }
            return owl.getConceptUuid();
        }
        return null;
    }

    private void addLanguageRefexes(ConceptCB conceptBp) throws ValidationException,
            UnsupportedDialectOrLanguage, IOException, InvalidCAB, ContradictionException, NoSuchAlgorithmException {

        String fsn = conceptBp.getFullySpecifiedName();
        DescriptionCAB fsnBp = conceptBp.getFullySpecifiedNameCABs().iterator().next();
        if (DialectHelper.isTextForDialect(fsn, Language.EN_US.getLenient().getNid())
                && DialectHelper.isTextForDialect(fsn, Language.EN_UK.getLenient().getNid())) {
            conceptBp.addFullySpecifiedName(fsnBp, LANG_CODE.EN);
        } else if (DialectHelper.isTextForDialect(fsn, Language.EN_UK.getLenient().getNid())) { //check if lang is en-us
            conceptBp.addFullySpecifiedName(fsnBp, LANG_CODE.EN_US);
        } else if (DialectHelper.isTextForDialect(fsn, Language.EN_US.getLenient().getNid())) {//check if lang is en-gb
            conceptBp.addFullySpecifiedName(fsnBp, LANG_CODE.EN_GB);
        }

        String pref = conceptBp.getPreferredName();
        DescriptionCAB prefBp = conceptBp.getPreferredNameCABs().iterator().next();
        if (DialectHelper.isTextForDialect(pref, Language.EN_US.getLenient().getNid())
                && DialectHelper.isTextForDialect(pref, Language.EN_UK.getLenient().getNid())) {
            conceptBp.addPreferredName(prefBp, LANG_CODE.EN);
        } else if (DialectHelper.isTextForDialect(pref, Language.EN_UK.getLenient().getNid())) { //check if lang is en-us
            conceptBp.addPreferredName(prefBp, LANG_CODE.EN_US);
        } else if (DialectHelper.isTextForDialect(pref, Language.EN_US.getLenient().getNid())) {//check if lang is en-gb
            conceptBp.addPreferredName(prefBp, LANG_CODE.EN_GB);
        }
    }

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        //nothing to do
    }

    @Override
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }

    public String getFileProp() {
        return fileProp;
    }

    public void setFileProp(String fileProp) {
        this.fileProp = fileProp;
    }

    public String getParentConceptPropName() {
        return parentConceptPropName;
    }

    public void setParentConceptPropName(String parentConceptPropName) {
        this.parentConceptPropName = parentConceptPropName;
    }
}
