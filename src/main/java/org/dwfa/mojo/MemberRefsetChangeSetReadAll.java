package org.dwfa.mojo;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.task.cmrscs.ImportCmrscs;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.maven.MojoUtil;

/**
 * Read all binary change set under a specified directory hierarchy, and apply the results of 
 * that change set to the open database.
 * @goal member-cs-read-all
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */


public class MemberRefsetChangeSetReadAll extends AbstractMojo {
    /**
     * The change set directory
     *
     * @parameter default-value="${project.build.directory}/generated-resources/changesets/"
     */
    String changeSetDir;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + changeSetDir)) {
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
        
        ImportCmrscs importCmrscs = new ImportCmrscs();
        
        importCmrscs.setRootDirStr(changeSetDir);
        try {
        	importCmrscs.importAllChangeSets(new LoggerAdaptor(getLog()));
	    } catch (TaskFailedException e) {
	        throw new MojoExecutionException(e.getLocalizedMessage(), e);
	    }
    }
}