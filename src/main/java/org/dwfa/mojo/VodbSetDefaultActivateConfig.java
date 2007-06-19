package org.dwfa.mojo;

import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.tapi.TerminologyException;

/**
 * 
 * @goal vodb-set-default-config
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbSetDefaultActivateConfig extends AbstractMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            I_TermFactory tf = LocalVersionedTerminology.get();
            I_ConfigAceFrame activeConfig = NewDefaultProfile.newProfile(null, null, null, null);

            tf.setActiveAceFrameConfig(activeConfig);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (TerminologyException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

    }

 }