package org.ihtsdo.workflow.refset.mojo.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.workflow.refset.semHier.SemanticHierarchyRefsetWriter;

/**
 * 
 * @author Jesse Efron
 */ 

public class InitializeSemanticHierarchyRefset implements I_InitializeWorkflowRefset {
	private static final int refCompIdPosition = -1;  // immutable
    private static final int childSemanticAreaPosition = 0;
    private static final int parentSemanticAreaPosition = 1;
    private static final int numberOfColumns = 2;

    private String fileName = "semParRefset.txt";

    private SemanticHierarchyRefsetWriter writer;
    
    public  InitializeSemanticHierarchyRefset() 
    {
        try {

            writer = new SemanticHierarchyRefsetWriter();
        } catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to initialize semantic hierarchy refset with error: " + e.getMessage());
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
	    			writer.setChildSemanticArea(columns[childSemanticAreaPosition]);
	    			writer.setParentSemanticArea(columns[parentSemanticAreaPosition]);
	
	    			writer.addMember();
	    		} else {
	            	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into semantic hierarchy refset: " + line);
				}
	        }
	
	    	Terms.get().addUncommitted(writer.getRefsetConcept());
	    	return true;
    	} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into semantic hierarchy refset: " + line);
    		return false;
    	}
    }
}
