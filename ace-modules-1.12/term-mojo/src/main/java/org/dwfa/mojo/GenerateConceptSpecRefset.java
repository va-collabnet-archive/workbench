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
package org.dwfa.mojo;

import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;

/**
 * Generate a Refset for all concepts in the Concept enum
 * 
 * @goal generate-concept-spec-refset
 * 
 * @phase generate-resources
 * 
 * @requiresDependencyResolution compile
 */
public class GenerateConceptSpecRefset extends AbstractMojo {

    /**
     * The name of the RefSet.
     * 
     * @parameter
     */
    private String refsetName;

    /**
     * The Uuid of the RefSet.
     * 
     * @parameter
     */
    private String refsetUuid;

    /*
     * Creates the refset concept
     */
    protected I_GetConceptData createRefsetConcept() throws Exception {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        I_GetConceptData fully_specified_description_type = termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
        I_GetConceptData preferred_description_type = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
        I_ConfigAceFrame config = termFactory.newAceFrameConfig();
        I_Path path = termFactory.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
        config.addEditingPath(path);
        config.setDefaultStatus(termFactory.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));
        // UUID uuid = UUID.randomUUID();
        UUID uuid = UUID.fromString(this.refsetUuid);
        I_GetConceptData newConcept = termFactory.newConcept(uuid, false, config);
        // Install the FSN
        termFactory.newDescription(UUID.randomUUID(), newConcept, "en", refsetName, fully_specified_description_type,
            config);
        // Install the preferred term
        termFactory.newDescription(UUID.randomUUID(), newConcept, "en", refsetName, preferred_description_type, config);
        termFactory.newRelationship(UUID.randomUUID(), newConcept,
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
            termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids()),
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()),
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()), 0, config);
        termFactory.newRelationship(UUID.randomUUID(), newConcept,
            termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids()),
            termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids()),
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()),
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()), 0, config);
        termFactory.commit();
        return newConcept;
    }

    /*
     * Adds a concept to a refset
     */
    private void addToRefset(I_TermFactory termFactory, int refsetId, int conceptId) throws Exception {
        I_GetConceptData include_individual = termFactory.getConcept(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids());
        // System.out.println("Include: " +
        // include_individual.getUids().get(0));
        int typeId = include_individual.getConceptId();
        I_GetConceptData active_status = termFactory.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
        int statusId = active_status.getConceptId();
        int memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
            ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), termFactory.getPaths(),
            Integer.MAX_VALUE);
        I_ThinExtByRefVersioned newExtension = termFactory.newExtension(refsetId, memberId, conceptId, typeId);
        I_ThinExtByRefPartConcept conceptExtension = termFactory.newConceptExtensionPart();
        conceptExtension.setConceptId(conceptId);
        I_GetConceptData path = termFactory.getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
        // System.out.println("Path: " + path.getUids().get(0));
        conceptExtension.setPathId(path.getConceptId());
        conceptExtension.setStatusId(statusId);
        conceptExtension.setVersion(Integer.MAX_VALUE);
        newExtension.addVersion(conceptExtension);
        termFactory.addUncommitted(newExtension);
    }

    /*
     * Primary method to build the refset <br> Create the refset concept <br>
     * Iterate over the enums and add them to the refset
     */
    private void buildRefset() throws Exception {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        I_GetConceptData refset = createRefsetConcept();
        getLog().info("Refset: " + refset.getInitialText());
        getLog().info("Refset: " + refset.getUids().get(0));
        for (ArchitectonicAuxiliary.Concept c : ArchitectonicAuxiliary.Concept.values()) {
            getLog().info("Processing concept:" + c.name());
            try {
                I_GetConceptData member = termFactory.getConcept(c.getUids());
                addToRefset(termFactory, refset.getConceptId(), member.getConceptId());
            } catch (Exception ex) {
                getLog().error("Skipping concept:" + c.name());
            }
        }
        for (RefsetAuxiliary.Concept c : RefsetAuxiliary.Concept.values()) {
            getLog().info("Processing concept:" + c.name());
            try {
                I_GetConceptData member = termFactory.getConcept(c.getUids());
                addToRefset(termFactory, refset.getConceptId(), member.getConceptId());
            } catch (Exception ex) {
                getLog().error("Skipping concept:" + c.name());
            }
        }
        termFactory.commit();
    }

    /*
     * 
     * Example use:
     * 
     * <br>&lt;execution&gt;
     * 
     * <br>&lt;id&gt;generate-concept-spec-refset&lt;/id&gt;
     * 
     * <br>&lt;phase&gt;generate-sources&lt;/phase&gt;
     * 
     * <br>&lt;goals&gt;
     * 
     * <br>&lt;goal&gt;generate-concept-spec-refset&lt;/goal&gt;
     * 
     * <br>&lt;/goals&gt;
     * 
     * <br>&lt;configuration&gt;
     * 
     * <br>&lt;refsetName&gt;IHTSDO Concept Spec Refset&lt;/refsetName&gt;
     * 
     * <br>&lt;/configuration&gt;
     * 
     * <br>&lt;/execution&gt;
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            buildRefset();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
