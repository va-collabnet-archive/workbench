package org.ihtsdo.workflow.refset.mojo.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.workflow.refset.semTag.SemanticTagsRefsetWriter;


/**
 * @author Jesse Efron
 * 
 */
  
public class InitializeSemanticTagsRefset implements I_InitializeWorkflowRefset {

	private static final int refCompIdPosition = 0;  // immutable
    private static final int semTagPosition = 1;
    private static final int uidPosition = 2;
    private static final int numberOfColumns = 3;

    private String fileName = "";

    private SemanticTagsRefsetWriter writer = null;
    
    public void execute() throws MojoExecutionException, MojoFailureException 
    {
        try {
            writer = new SemanticTagsRefsetWriter();
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to initialize semantic tag refset with error: " + e.getMessage());
		}
	}

    @Override
    public boolean initializeRefset(String resourceFilePath)  {
    	String line = null;

    	try {
    		File f = new File(resourceFilePath + File.separatorChar + fileName);
	    	BufferedReader inputFile = new BufferedReader(new FileReader(f));    	
	
	    	while ((line = inputFile.readLine()) != null)
	        {
	        	if (line.trim().length() == 0) {
	        		continue;
	        	}
	        	
	        	String[] columns = line.split("\t");
	
        		if (columns.length == numberOfColumns)
        		{
        			writer.setSemanticTag(columns[semTagPosition]);
        			writer.setSemanticTagUUID(columns[uidPosition]);

            		writer.addMember();
        		} else  {
	            	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into semantic tag refset: " + line);
    			}
	        }

	    	Terms.get().addUncommitted(writer.getRefsetConcept());
	    	return true;
    	} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into semantic tag refset: " + line);
	    	return false;
    	}
    }
}
