package org.dwfa.mojo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.cs.ComponentValidator;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.task.cs.ImportAllChangeSets;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.maven.MojoUtil;

/**
 * Read all binary change set under a specified directory hierarchy, and apply the results of 
 * that change set to the open database.
 * @goal bcs-read-all
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */


public class BinaryChangeSetReadAll extends AbstractMojo {
    /**
     * The change set directory
     *
     * @parameter default-value="${project.build.directory}/generated-resources/changesets/"
     */
    String changeSetDir;

    /**
     * Whether to validate the change set first or not.
     * @parameter
     */
    boolean validate = false;

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
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        
        ImportAllChangeSets importAllChangeSetsTask = new ImportAllChangeSets();
        importAllChangeSetsTask.setRootDirStr(changeSetDir);
        try {
        	importAllChangeSetsTask.importAllChangeSets(new LoggerAdaptor(getLog()));
	    } catch (TaskFailedException e) {
	        throw new MojoExecutionException(e.getLocalizedMessage(), e);
	    }
    }
}