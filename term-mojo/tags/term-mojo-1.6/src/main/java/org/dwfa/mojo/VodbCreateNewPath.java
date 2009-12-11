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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.status.SetStatusUtil;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * 
 * @goal vodb-create-new-path
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbCreateNewPath extends AbstractMojo {

    /**
     * Path origins
     * 
     * @parameter
     */
    SimpleUniversalAcePosition[] origins;

    /**
     * Path UUID
     * 
     * @parameter
     * @required
     */
    //String parentUUID;

    /**
     * Parent of the new path.
     * 
     * @parameter
     * @required
     */
    private ConceptDescriptor pathParent;

    /**
     * Parent of the new path.
     * 
     * @parameter
     */
    private ConceptDescriptor status;

    /**
     * Path Description
     * 
     * @parameter
     * @required
     */
    String pathFsDesc;

    /**
     * Path Description
     * 
     * @parameter
     * @required
     */
    String pathPrefDesc;

    public void execute() throws MojoExecutionException, MojoFailureException {
		// Use the architectonic branch for all path editing.
		try {
			try {
				if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + pathFsDesc)) {
					return;
				}
			} catch (NoSuchAlgorithmException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			} 
			I_TermFactory tf = LocalVersionedTerminology.get();
			I_ConfigAceFrame activeConfig = tf.getActiveAceFrameConfig();
			Set<I_Position> pathOrigins = null;
			if (origins != null) {
				pathOrigins = new HashSet<I_Position>(origins.length);
				for (SimpleUniversalAcePosition pos : origins) {
					I_Path originPath = tf.getPath(pos.getPathId());
					pathOrigins.add(tf.newPosition(originPath, pos.getTime()));
				}
			}

			I_GetConceptData parent = pathParent.getVerifiedConcept();
			activeConfig.setHierarchySelection(parent);

			UUID pathUUID = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, pathFsDesc);

			I_GetConceptData pathConcept = tf
			.newConcept(pathUUID, false, tf.getActiveAceFrameConfig());

			I_ConceptAttributeVersioned cav = pathConcept.getConceptAttributes();
			if (status!=null) {
				SetStatusUtil.setStatusOfConceptInfo(status.getVerifiedConcept(),cav.getTuples());
			}

			UUID fsDescUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, 
					pathUUID.toString() + 
					ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids() + 
					pathFsDesc);

			I_DescriptionVersioned idv = tf.newDescription(fsDescUuid, pathConcept, "en", pathFsDesc,
					ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), activeConfig);
			if (status!=null) {
				SetStatusUtil.setStatusOfDescriptionInfo(status.getVerifiedConcept(),idv.getTuples());
			}
			UUID prefDescUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, 
					pathUUID.toString() + 
					ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids() + 
					pathPrefDesc);

			I_DescriptionVersioned idvpt = tf.newDescription(prefDescUuid, pathConcept, "en", pathPrefDesc,
					ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), activeConfig);
			if (status!=null) {
				SetStatusUtil.setStatusOfDescriptionInfo(status.getVerifiedConcept(),idvpt.getTuples());
			}

			UUID relUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, 
					pathUUID.toString() + fsDescUuid + prefDescUuid);

			I_RelVersioned rel = tf.newRelationship(relUuid, pathConcept, activeConfig);
			if (status!=null) {
				SetStatusUtil.setStatusOfRelInfo(status.getVerifiedConcept(),rel.getTuples());
			}            
//			need to do an immediate commit so that new concept will be avaible to path when read from changeset
			tf.commit(); 

			tf.newPath(pathOrigins, pathConcept);
		} catch (TerminologyException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (ParseException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

}
