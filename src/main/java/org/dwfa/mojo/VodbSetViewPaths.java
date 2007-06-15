package org.dwfa.mojo;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.tapi.TerminologyException;

/**
 * 
 * @goal vodb-set-view-paths
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbSetViewPaths extends AbstractMojo {

    /**
     * View path UUIDs
     * 
     * @parameter
     * @required
     */
    private List<String> viewPathUuids;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            I_ConfigAceFrame activeConfig = LocalVersionedTerminology.get().getActiveAceFrameConfig();
            I_TermFactory tf = LocalVersionedTerminology.get();
            activeConfig.getViewPositionSet().clear();
            for (String pathId : viewPathUuids) {
                I_Path viewPath = tf.getPath(new UUID[] { UUID.fromString(pathId) });
                activeConfig.addViewPosition(tf.newPosition(viewPath, Integer.MAX_VALUE));
            }
        } catch (TerminologyException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
