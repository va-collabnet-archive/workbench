package org.dwfa.mojo;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.UuidPrefixesForType5;
import org.dwfa.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * 
 * @goal vodb-set-edit-paths
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbSetEditPaths extends AbstractMojo {

    /**
     * Editing path UUIDs
     * 
     * @parameter
     * @required
     */
    private List<ConceptDescriptor> editPaths;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
     	   try {
               if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + editPaths)) {
                   return;
               }
           } catch (NoSuchAlgorithmException e) {
               throw new MojoExecutionException(e.getLocalizedMessage(), e);
           } 
            I_ConfigAceFrame activeConfig = LocalVersionedTerminology.get().getActiveAceFrameConfig();
            I_TermFactory tf = LocalVersionedTerminology.get();

            activeConfig.getEditingPathSet().clear();
            for (ConceptDescriptor pathConcept : editPaths) {
            	if (pathConcept.getUuid() == null) {
            		pathConcept.setUuid(Type5UuidFactory.get(UuidPrefixesForType5.PATH_ID_FROM_FS_DESC, 
            				pathConcept.getDescription()).toString());
            	}
                activeConfig.addEditingPath(tf.getPath(pathConcept.getVerifiedConcept().getUids()));
            }
        } catch (TerminologyException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
    }

}
