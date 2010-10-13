package org.ihtsdo.mojo.mojo.svn;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_HandleSubversion;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.svn.Svn;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.mojo.maven.MojoUtil;

/**
 * Update with svn changes.
 * 
 * @goal svn-checkout
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class Checkout
    extends AbstractSvnMojo
{

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + workingCopyStr + repositoryUrlStr,
                this.getClass(), targetDirectory)) {
                return;
            }
            if (workingCopyStr != null && workingCopyStr.length() > 1) {
                workingCopyStr = FileIO.getNormalizedRelativePath(new File(workingCopyStr));
            } else {
                workingCopyStr = System.getProperty("user.dir");
            }
            Svn.setConnectedToSvn(true);
            if ( username == null )
            {
                Svn.setUseCachedCredentials(true);
            }
            I_HandleSubversion svn = new Svn();
            SubversionData svd = new SubversionData(repositoryUrlStr, workingCopyStr);
            getLog().info("Connecting to: " + repositoryUrlStr + " as: " + username);
            svd.setUsername(username);
            svd.setPassword(password);
            svn.svnCheckout(svd, username != null ? this : null, false);
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (TaskFailedException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
