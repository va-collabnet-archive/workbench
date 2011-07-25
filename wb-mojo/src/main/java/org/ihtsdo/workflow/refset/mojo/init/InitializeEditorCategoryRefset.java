package org.ihtsdo.workflow.refset.mojo.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;


/**
 * @author Jesse Efron
 * 
 */
  
public class InitializeEditorCategoryRefset implements I_InitializeWorkflowRefset {

    private static final int editorPosition = 0;
    private static final int semanticAreaPosition = 1;
    private static final int categoryPosition = 2;
    private static final int numberOfColumns = 3;

    private String fileName = "";

    private static ViewCoordinate viewCoord;
    private EditorCategoryRefsetWriter writer = null;
    
    public void execute() throws MojoExecutionException, MojoFailureException 
    {
        try {
            viewCoord = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
            writer = new EditorCategoryRefsetWriter();
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
        			writer.setEditor(getEditor(columns[editorPosition]).getVersion(viewCoord));
	            	writer.setSemanticArea(columns[semanticAreaPosition]);
	            	writer.setCategory(WorkflowHelper.lookupEditorCategory(columns[categoryPosition], viewCoord));

	            	writer.addMember();
        		} else {
	            	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into editor category refset: " + line);
    			}
            }

        	Terms.get().addUncommitted(writer.getRefsetConcept());
        	return true;
    	} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into editor category refset: " + line);
        	return false;
    	}
	}

    private ConceptVersionBI getEditor(String editor) throws TerminologyException, IOException {
    	return WorkflowHelper.lookupModeler(editor);
    }
}