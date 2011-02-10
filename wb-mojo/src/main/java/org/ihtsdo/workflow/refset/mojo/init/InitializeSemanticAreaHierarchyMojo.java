package org.ihtsdo.workflow.refset.mojo.init;

import java.io.File;
import java.util.Scanner;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
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
    
    /**
     * Whether to alert user of a bad row that can't be imported into the database
     * 
     * @parameter
     * default-value=true
     * @required
     */
    private boolean reportErrors;

    private static final int childSemanticAreaPosition = 0;							// 0
    private static final int parentSemanticAreaPosition = childSemanticAreaPosition + 1;		// 1

    private static final int numberOfColumns = parentSemanticAreaPosition + 1;			// 2

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
            
        		if (columns.length == numberOfColumns)
        		{
        			writer.setChildSemanticArea(columns[childSemanticAreaPosition]);
        			writer.setParentSemanticArea(columns[parentSemanticAreaPosition]);

        			writer.addMember();
        		} else if (reportErrors) {
                	AceLog.getAppLog().log(Level.WARNING, line, new Exception("Unable to import this row into semantic area hierarchy refset"));
    			}
            }

            // Single RefCompId, so commit at end

            Terms.get().addUncommitted(writer.getRefsetConcept());
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage());
		}

	}
}
