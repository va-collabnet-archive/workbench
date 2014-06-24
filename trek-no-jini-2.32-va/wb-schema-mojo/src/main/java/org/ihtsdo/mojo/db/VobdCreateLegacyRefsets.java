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
package org.ihtsdo.mojo.db;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.tk.api.PathBI;

/**
 * @goal vodb-create-legacy-refsets
 *
 * @phase process-resources
 *
 * @author marc
 */
public class VobdCreateLegacyRefsets extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            I_TermFactory tf = Terms.get();
            I_ConfigAceFrame activeConfig = tf.getActiveAceFrameConfig();
            I_GetConceptData fully_specified_description_type = tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
            I_GetConceptData preferred_description_type = tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
            I_GetConceptData activeStatus = tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
            int activeStatusNid = activeStatus.getNid();
            PathBI path = tf.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());

            if (activeConfig == null) {
                throw new MojoFailureException("Use the vodb-set-default-config and vodb-set-ace-edit-path goals prior to calling this goal.");
            }

            //Concept: "GB English Dialect Subset"
            UUID gbSubsetUuid = UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd");
            I_GetConceptData gbSubsetConcept = tf.newConcept(gbSubsetUuid,
                    true,
                    activeConfig,
                    activeStatusNid,
                    1012464000000L);
            gbSubsetConcept.setAnnotationIndex(false);

            UUID gbSubsetDescriptionFsnUuid = UUID.fromString("0abc055a-486a-50c2-b0a3-9587b92a2721");
            I_DescriptionVersioned gbSubsetDescriptionFsn = tf.newDescription(
                    gbSubsetDescriptionFsnUuid,
                    gbSubsetConcept,
                    "en", // String lang
                    "GB English Dialect Subset", // String text
                    fully_specified_description_type, // I_GetConceptData descType
                    activeConfig, // I_ConfigAceFrame aceFrameConfig
                    activeStatus,
                    1012464000000L);

            UUID gbSubsetDescriptionPtUuid = UUID.fromString("f7d79dde-0394-565d-891f-6b8e99d8abf7");
            I_DescriptionVersioned gbSubsetDescriptionPt = tf.newDescription(
                    gbSubsetDescriptionPtUuid,
                    gbSubsetConcept,
                    "en", // String lang
                    "GB English Dialect Subset", // String text
                    preferred_description_type, // I_GetConceptData descType
                    activeConfig, // I_ConfigAceFrame aceFrameConfig
                    activeStatus, // I_GetConceptData status
                    1012464000000L);

            UUID gbSubsetRelationshipUuid = UUID.fromString("73628c45-d6b5-537d-a78a-5f67365d57a5");

            tf.newRelationship(gbSubsetRelationshipUuid, gbSubsetConcept,
                    tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
                    tf.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()), 0, activeConfig);
    
            tf.commit();

            //Concept: "US English Dialect Subset"
            UUID usSubsetUuid = UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6");
            I_GetConceptData usSubsetConcept = tf.newConcept(usSubsetUuid,
                    true,
                    activeConfig,
                    activeStatusNid,
                    1012464000000L);
            usSubsetConcept.setAnnotationIndex(false);

            UUID usSubsetDescriptionFsnUuid = UUID.fromString("cdfd7076-a3cf-5e66-8ec2-98c9e7db345a");
            I_DescriptionVersioned usSubsetDescriptionFsn = tf.newDescription(
                    usSubsetDescriptionFsnUuid,
                    usSubsetConcept,
                    "en", // String lang
                    "US English Dialect Subset", // String text
                    fully_specified_description_type, // I_GetConceptData descType
                    activeConfig, // I_ConfigAceFrame aceFrameConfig
                    activeStatus,
                    1012464000000L);

            UUID usSubsetDescriptionPtUuid = UUID.fromString("f5ce2135-1557-5034-bf05-c157990daa48");
            I_DescriptionVersioned usSubsetDescriptionPt = tf.newDescription(
                    usSubsetDescriptionPtUuid,
                    usSubsetConcept,
                    "en", // String lang
                    "US English Dialect Subset", // String text
                    preferred_description_type, // I_GetConceptData descType
                    activeConfig, // I_ConfigAceFrame aceFrameConfig
                    activeStatus, // I_GetConceptData status
                    1012464000000L);

            UUID usSubsetRelationshipUuid = UUID.fromString("c4d207e4-526c-5e48-939a-138ee28aefaa");

            tf.newRelationship(usSubsetRelationshipUuid, usSubsetConcept,
                    tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
                    tf.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()), 0, activeConfig);

            tf.commit();

        } catch (Exception ex) {
            Logger.getLogger(VobdCreateLegacyRefsets.class.getName()).log(Level.SEVERE, null, ex);
        }



    }
}
