package org.dwfa.mojo;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;

/**
 * 
 * @goal vodb-set-default-config
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbSetDefaultActivateConfig extends AbstractMojo {

    /**
     * Location of the directory to output data files to.
     * KEC: I added this field, because the maven plugin plugin would 
     * crash unless there was at least one commented field. This field is
     * not actually used by the plugin. 
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @SuppressWarnings("unused")
    private String outputDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
       try {
    	   try {
               if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName())) {
                   return;
               }
           } catch (NoSuchAlgorithmException e) {
               throw new MojoExecutionException(e.getLocalizedMessage(), e);
           } 
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