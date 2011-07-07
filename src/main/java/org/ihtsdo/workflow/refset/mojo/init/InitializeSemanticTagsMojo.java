package org.ihtsdo.workflow.refset.mojo.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.semTag.SemanticTagsRefsetReader;
import org.ihtsdo.workflow.refset.semTag.SemanticTagsRefsetWriter;


/**
 * @author Jesse Efron
 * 
 * @goal initialize-semantic-tags-refset
 * @requiresDependencyResolution compile
 */
  
public class InitializeSemanticTagsMojo extends AbstractMojo {

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
	
    private static final int semTagPosition = 0;							// 0
    private static final int uidPosition = semTagPosition + 1;		// 1

    private static final int numberOfColumns = uidPosition + 1;			// 2

    private SemanticTagsRefsetWriter writer = null;
    
    public void execute() throws MojoExecutionException, MojoFailureException 
    {
        System.setProperty("java.awt.headless", "true");
        try {

        	SemanticTagsRefsetReader refset = new SemanticTagsRefsetReader();
            I_TermFactory tf = Terms.get();
            
            writer = new SemanticTagsRefsetWriter();

            processHierarchies(new File(filePath));
       
	        tf.addUncommitted(refset.getRefsetConcept());
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to initialize semantic tag refset with error: " + e.getMessage());
		}
	}

    private void processHierarchies(File f) throws TerminologyException, IOException {
    	BufferedReader inputFile = new BufferedReader(new FileReader(f));    	
    	String line = null;

    	while ((line = inputFile.readLine()) != null)
        {
        	if (line.trim().length() == 0) {
        		continue;
        	}
        	
        	String[] columns = line.split("\t");

        	try {
        		if (columns.length == numberOfColumns)
        		{
        			writer.setSemanticTag(columns[semTagPosition]);
        			writer.setUUID(columns[uidPosition]);

            		writer.addMember();
        		} else if (reportErrors) {
    				AceLog.getAppLog().log(Level.WARNING, line, new Exception("Unable to import this row into semantic tags refset"));
    			}
        	} catch (Exception e) {
            	AceLog.getAppLog().log(Level.WARNING, "Exception: " + e.getMessage() + " at line: " + line);
        	}
        };
    }
}
