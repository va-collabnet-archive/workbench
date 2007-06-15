package org.dwfa.mojo;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.tapi.TerminologyException;

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
    private List<String> editPathUuids;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            I_ConfigAceFrame activeConfig = LocalVersionedTerminology.get().getActiveAceFrameConfig();
            I_TermFactory tf = LocalVersionedTerminology.get();

            activeConfig.getEditingPathSet().clear();
            for (String pathId : editPathUuids) {
                activeConfig.addEditingPath(tf.getPath(new UUID[] { UUID.fromString(pathId) }));
            }
        } catch (TerminologyException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
