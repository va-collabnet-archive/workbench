package org.ihtsdo.mojo.mojo.svn;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_HandleSubversion;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.svn.Svn;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.tigris.subversion.javahl.PromptUserPassword3;


/**
 * Update with svn changes.
 * 
 * @goal svn-checkout
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class Checkout extends AbstractMojo implements PromptUserPassword3 {
    /**
     * Location of the svn working copy
     * 
     * @parameter
     */
    String workingCopyStr;

    /**
     * The svn repository url
     * 
     * @parameter
     */
    String repositoryUrlStr;

    /**
     * The svn username
     * 
     * @parameter
     */
    String username;

    /**
     * The svn password
     * 
     * @parameter
     */
    String password;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

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
            I_HandleSubversion svn = new Svn();
            SubversionData svd = new SubversionData(repositoryUrlStr, workingCopyStr);
            getLog().info("Connecting to: " + repositoryUrlStr + " as: " + username);
            svd.setUsername(username);
            svd.setPassword(password);
            svn.svnCheckout(svd, this, false);
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (TaskFailedException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    public String askQuestion(String arg0, String arg1, boolean arg2, boolean arg3) {
        throw new UnsupportedOperationException();
    }

    public boolean prompt(String realm, String username, boolean maySave) {
        return true;
    }

    public boolean userAllowedSave() {
        return false;
    }

    public int askTrustSSLServer(String arg0, boolean arg1) {
        throw new UnsupportedOperationException();
    }

    public String askQuestion(String arg0, String arg1, boolean arg2) {
        throw new UnsupportedOperationException();
    }

    public boolean askYesNo(String arg0, String arg1, boolean arg2) {
        throw new UnsupportedOperationException();
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean prompt(String arg0, String arg1) {
        throw new UnsupportedOperationException();
    }
}
