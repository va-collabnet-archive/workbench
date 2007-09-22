package org.dwfa.mojo;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.UuidPrefixesForType5;
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
            
            UUID pathUUID = Type5UuidFactory.get(UuidPrefixesForType5.PATH_ID_FROM_FS_DESC, pathFsDesc);

            I_GetConceptData pathConcept = tf
                    .newConcept(pathUUID, false, tf.getActiveAceFrameConfig());
            
            UUID fsDescUuid = Type5UuidFactory.get(UuidPrefixesForType5.PATH_ID_FROM_FS_DESC, 
            		pathUUID.toString() + 
            		ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids() + 
            		pathFsDesc);
            
            tf.newDescription(fsDescUuid, pathConcept, "en", pathFsDesc,
                              ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), activeConfig);

            UUID prefDescUuid = Type5UuidFactory.get(UuidPrefixesForType5.PATH_ID_FROM_FS_DESC, 
            		pathUUID.toString() + 
            		ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids() + 
            		pathPrefDesc);

            tf.newDescription(prefDescUuid, pathConcept, "en", pathPrefDesc,
                              ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), activeConfig);

            
            UUID relUuid = Type5UuidFactory.get(UuidPrefixesForType5.PATH_ID_FROM_FS_DESC, 
            		pathUUID.toString() + fsDescUuid + prefDescUuid);

            tf.newRelationship(relUuid, pathConcept, activeConfig);
            
//          need to do an immediate commit so that new concept will be avaible to path when read from changeset
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
