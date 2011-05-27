package org.ihtsdo.workflow.refset.mojo.init;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
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
            String line = null;
        	BufferedReader inputFile = new BufferedReader(new FileReader(filePath));    	

        	while ((line = inputFile.readLine()) != null)
            {
        		if (line.trim().length() == 0) {
        			continue;
        		}

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
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Exception: " + e.getMessage());
		}

	}
}
