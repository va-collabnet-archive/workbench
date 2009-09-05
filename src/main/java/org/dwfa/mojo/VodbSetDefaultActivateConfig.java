package org.dwfa.mojo;

import java.io.File;
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
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
       System.setProperty("java.awt.headless", "true");
       try {
           try {
               if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName(),
                       this.getClass(), targetDirectory)) {
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

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

 }