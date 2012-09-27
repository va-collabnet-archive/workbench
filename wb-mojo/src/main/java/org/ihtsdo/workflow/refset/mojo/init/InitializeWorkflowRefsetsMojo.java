package org.ihtsdo.workflow.refset.mojo.init;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @author Jesse Efron
 *
 * @goal initialize-workflow-refsets
 * @requiresDependencyResolution compile
 */

public class InitializeWorkflowRefsetsMojo extends AbstractMojo {

    /**
     * The name of the database to create. All sql inserts will be against this
     * database.
     *
     * @parameter
     * @required
     */
    private String filePath;
	
    
   @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        System.setProperty("java.awt.headless", "true");
        
        I_InitializeWorkflowRefset initializer = new InitializeEditorCategoryRefset();
        initializer.initializeRefset(filePath);
        
        initializer = new InitializeSemanticHierarchyRefset();
        initializer.initializeRefset(filePath);

        initializer = new InitializeSemanticTagsRefset();
        initializer.initializeRefset(filePath);

        initializer = new InitializeStateTransitionRefset();
        initializer.initializeRefset(filePath);
    }
}
