package org.ihtsdo.workflow.refset.mojo.init;

import java.io.File;
import java.util.Scanner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.workflow.refset.semHier.SemanticAreaHierarchyRefsetWriter;

/**
 * @author Jesse Efron
 * 
 * @goal initialize-semantic-area-hierarchy-refset
 * @requiresDependencyResolution compile
 */ 

public class InitializeSemanticAreaHierarchyMojo extends AbstractMojo {

    /**
     * The name of the database to create. All sql inserts will be against this
     * database.
     * 
     * @parameter
     * @required
     */
    private String filePath;

    public void execute() throws MojoExecutionException, MojoFailureException 
    {
        System.setProperty("java.awt.headless", "true");
        try {

            SemanticAreaHierarchyRefsetWriter writer = new SemanticAreaHierarchyRefsetWriter();

            Scanner scanner = new Scanner(new File(filePath));

            while (scanner.hasNextLine())
            {
            	String line = scanner.nextLine();
            	String[] columns = line.split("\t");
            
            	if (columns.length == 2)
            	{
	            	writer.setChildSemanticArea(columns[0]);
	            	writer.setParentSemanticArea(columns[1]);
	
	            	writer.addMember();
            	}
            }

            // Single RefCompId, so commit at end

            Terms.get().addUncommitted(writer.getRefsetConcept());
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage());
		}

	}
}
