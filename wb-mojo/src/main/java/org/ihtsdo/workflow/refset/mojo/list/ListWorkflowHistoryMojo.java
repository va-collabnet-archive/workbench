package org.ihtsdo.workflow.refset.mojo.list;

import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.workflow.WorkflowHistoryRefsetSearcher;

/**
 * @author Jesse Efron
 * 
 * @goal list-workflow-history-refset
 * @requiresDependencyResolution compile
 */

public class ListWorkflowHistoryMojo extends AbstractMojo {

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    public void execute() throws MojoExecutionException, MojoFailureException 
    {
    	WorkflowHistoryRefsetSearcher searcher;
		try {
			searcher = new WorkflowHistoryRefsetSearcher();
	    	searcher.listWorkflowHistory();
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Couldn't list workflow history refset", e);
		}
	}
}
