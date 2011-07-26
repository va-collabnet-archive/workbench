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

	private static final int refCompIdPosition = -1;  // immutable
    private static final int semTagPosition = 0;
    private static final int uidPosition = 1;
    private static final int numberOfColumns = 2;

    private String fileName = "semanticTags.txt";

    private SemanticTagsRefsetWriter writer = null;
    
    public InitializeSemanticTagsRefset() 
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
        			int ss = columns.length;
        			String s = columns[semTagPosition];
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
