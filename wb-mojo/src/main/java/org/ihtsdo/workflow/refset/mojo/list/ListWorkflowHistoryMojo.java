package org.ihtsdo.workflow.refset.mojo.list;

import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetSearcher;

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
    	System.out.println("\n^\n^\n^\n^\n^\n^\n^\n^\n^\n^\n^|n^\n^\n^\n^");
    	
    	WorkflowHistoryRefsetSearcher searcher;
		try {
			searcher = new WorkflowHistoryRefsetSearcher();
	    	searcher.listWorkflowHistory();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
