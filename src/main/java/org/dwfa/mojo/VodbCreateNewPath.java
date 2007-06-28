package org.dwfa.mojo;

import java.io.IOException;
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
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

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
     * @required
     */
	SimpleUniversalAcePath[] origins;

    /**
     * Path UUID
     * 
     * @parameter
     * @required
     */
    String parentUUID;

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

    /**
     * Path UUID
     * 
     * @parameter
     * @required
     */
    String pathUUID;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // Use the architectonic branch for all path editing.
        try {
            I_TermFactory tf = LocalVersionedTerminology.get();
            I_ConfigAceFrame activeConfig = tf.getActiveAceFrameConfig();

            Set<I_Position> pathOrigins = new HashSet<I_Position>(origins.length);
            for (SimpleUniversalAcePath pos : origins) {
                I_Path originPath = tf.getPath(pos.getPathId());
                pathOrigins.add(tf.newPosition(originPath, pos.getTime()));
            }

            I_GetConceptData parent = tf.getConcept(new UUID[] { UUID.fromString(parentUUID) });
            activeConfig.setHierarchySelection(parent);

            I_GetConceptData pathConcept = tf
                    .newConcept(UUID.fromString(pathUUID), false, tf.getActiveAceFrameConfig());
            tf.newDescription(UUID.randomUUID(), pathConcept, "en", pathFsDesc,
                              ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), activeConfig);

            tf.newDescription(UUID.randomUUID(), pathConcept, "en", pathPrefDesc,
                              ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), activeConfig);

            tf.newRelationship(UUID.randomUUID(), pathConcept, activeConfig);
            
//          need to do an immedate commit so that new concept will be avaible to path when read from changeset
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
