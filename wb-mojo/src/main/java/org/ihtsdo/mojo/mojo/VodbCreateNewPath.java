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
package org.ihtsdo.mojo.mojo;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.status.TupleListUtil;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

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
    // String parentUUID;

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
     */
    String pathPrefDesc;

    /**
     * Path UUID
     * 
     * @parameter
     */
    String pathUuidStr;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    /**
     * If true, set version timestamps (commit time) to the beginning of SNOMED
     * time to ensure it
     * preceeds all other changes.
     * 
     * @parameter
     */
    boolean setVersionAsBeginningOfTime = false;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // Use the architectonic branch for all path editing.
        try {
            try {
                if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + pathFsDesc, this.getClass(),
                    targetDirectory)) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
            I_TermFactory tf = Terms.get();
            I_ConfigAceFrame activeConfig = tf.getActiveAceFrameConfig();
            if (activeConfig == null) {
                throw new TaskFailedException("Use the vodb-set-default-config and vodb-set-ace-edit-path goals prior to calling this goal.");
            }
            Set<PositionBI> pathOrigins = null;
            if (origins != null) {
                pathOrigins = new HashSet<PositionBI>(origins.length);
                for (SimpleUniversalAcePosition pos : origins) {
                    PathBI originPath = tf.getPath(pos.getPathId());
                    pathOrigins.add(tf.newPosition(originPath, pos.getTime()));
                }
            }

            I_GetConceptData parent = pathParent.getVerifiedConcept();
            activeConfig.setHierarchySelection(parent);
            
            UUID pathUUID = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, pathFsDesc);
            if (pathUuidStr != null && pathUuidStr.length() > 1) {
            	pathUUID = UUID.fromString(pathUuidStr);
            }
            getLog().info("VodbCreateNewPath pathUUID= "+pathUUID);	

            I_GetConceptData pathConcept;

            if (tf.hasId(pathUUID)) {
            	getLog().info("VodbCreateNewPath tf has pathUUID ");
                pathConcept = tf.getConcept(new UUID[] { pathUUID });

                /**
                 * This addresses a problem where a UUID has been specified
                 * as a path in generated transform output files, without
                 * the path explicitly being defined. In this case we end
                 * up with a concept without descriptions. To handle this
                 * we check for descriptions and create the path concept
                 * anyway... as the existing concept is not complete/usable
                 */
                if (pathConcept.getDescriptions() == null || pathConcept.getDescriptions().isEmpty()) {
                	getLog().warn("VodbCreateNewPath tf has pathUUID but no desc");
                    pathConcept = createNewPathConcept(tf, activeConfig, pathUUID);
                }
            } else {
            	getLog().info("VodbCreateNewPath tf has NO pathUUID So creating NEW");
                pathConcept = createNewPathConcept(tf, activeConfig, pathUUID);
                getLog().info("VodbCreateNewPath createNewPathConcept called new concept ID = "+pathConcept.getConceptNid());
            }

            tf.newPath(pathOrigins, pathConcept);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private I_GetConceptData createNewPathConcept(I_TermFactory tf, I_ConfigAceFrame activeConfig, UUID pathUUID)
            throws TerminologyException, IOException, Exception, NoSuchAlgorithmException, UnsupportedEncodingException {
    	getLog().info("VodbCreateNewPath entering createNewPathConcept");
    	
        List<I_AmTuple> newTuples = new ArrayList<I_AmTuple>();

        I_GetConceptData pathConcept = tf.newConcept(pathUUID, false, tf.getActiveAceFrameConfig());

        I_ConceptAttributeVersioned cav = pathConcept.getConceptAttributes();
        newTuples.addAll(cav.getTuples());

        UUID fsDescUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, pathUUID.toString()
            + ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids() + pathFsDesc);

        I_GetConceptData descTypeConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
        
        I_DescriptionVersioned idv = tf.newDescription(fsDescUuid, pathConcept, "en", pathFsDesc,
        		descTypeConcept, activeConfig);
        getLog().info("VodbCreateNewPath.createNewPathConcept should be adding a desc of "+pathFsDesc);
        newTuples.addAll(idv.getTuples());

        UUID prefDescUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, pathUUID.toString()
            + ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids() + pathPrefDesc);

        I_DescriptionVersioned idvpt = tf.newDescription(prefDescUuid, pathConcept, "en", pathPrefDesc,
        		descTypeConcept, activeConfig);
        newTuples.addAll(idvpt.getTuples());

        UUID relUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, pathUUID.toString() + fsDescUuid
            + prefDescUuid);

        I_RelVersioned rel = tf.newRelationship(relUuid, pathConcept, activeConfig);
        newTuples.addAll(rel.getTuples());

        if (status != null) {
            TupleListUtil.setStatus(status.getVerifiedConcept(), newTuples);
        }

        if (setVersionAsBeginningOfTime) {
            TupleListUtil.setVersion(Integer.MIN_VALUE, newTuples);
        }

        // need to do an immediate commit so that new concept will be available
        // to path when read from changeset
        
        getLog().info("VodbCreateNewPath createNewPathConcept new Concept id = "+pathConcept.getConceptNid());
        //getLog().error("VodbCreateNewPath createNewPathConcept new Concept id = "+pathConcept.getConceptNid());
        for (I_DescriptionVersioned<?> desc: pathConcept.getDescriptions()) {
        	getLog().info("VodbCreateNewPath createNewPathConcept getDescriptions descID = "+desc.getDescId());
        	for (I_DescriptionPart desl : desc.getMutableParts()) {
        		getLog().info("VodbCreateNewPath createNewPathConcept dscParth text = "+desl.getText());
        	}
        	
        }
        
        tf.addUncommitted(pathConcept);
        tf.commit();
        return pathConcept;
    }

    public SimpleUniversalAcePosition[] getOrigins() {
        return origins;
    }

    public void setOrigins(SimpleUniversalAcePosition[] origins) {
        this.origins = origins;
    }

    public ConceptDescriptor getPathParent() {
        return pathParent;
    }

    public void setPathParent(ConceptDescriptor pathParent) {
        this.pathParent = pathParent;
    }

    public ConceptDescriptor getStatus() {
        return status;
    }

    public void setStatus(ConceptDescriptor status) {
        this.status = status;
    }

    public String getPathFsDesc() {
        return pathFsDesc;
    }

    public void setPathFsDesc(String pathFsDesc) {
        this.pathFsDesc = pathFsDesc;
    }

    public String getPathPrefDesc() {
        return pathPrefDesc;
    }

    public void setPathPrefDesc(String pathPrefDesc) {
        this.pathPrefDesc = pathPrefDesc;
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

}
