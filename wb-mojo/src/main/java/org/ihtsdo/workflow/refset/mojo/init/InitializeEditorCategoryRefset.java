package org.ihtsdo.workflow.refset.mojo.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
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

    private String fileName = "edCatRefset.txt";

    private EditorCategoryRefsetWriter writer = null;
    
    public InitializeEditorCategoryRefset()  
    {
        try {
            writer = new EditorCategoryRefsetWriter();
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to initialize semantic tag refset with error: " + e.getMessage());
		}
	}
    
    
   @Override
   public boolean initializeRefset(String resourceFilePath)  {
		String line = null;
		Map<String, UUID> users = new HashMap<String, UUID>();
		Map<String, UUID> categories = new HashMap<String, UUID>();
        
		try {
			I_GetConceptData parentEditorConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getPrimoridalUid());
			for (I_GetConceptData con : WorkflowHelper.getChildren(parentEditorConcept)) {
				users.put(WorkflowHelper.getPrefTerm(con).toLowerCase(), con.getPrimUuid());
			}

			I_GetConceptData parentCategoryConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ROLES.getPrimoridalUid());
			for (I_GetConceptData con : WorkflowHelper.getChildren(parentCategoryConcept)) {
				categories.put(WorkflowHelper.getPrefTerm(con).toLowerCase(), con.getPrimUuid());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
        			writer.setEditor(users.get(columns[editorPosition].toLowerCase()));
	            	writer.setSemanticArea(columns[semanticAreaPosition]);
	            	writer.setCategory(categories.get(columns[categoryPosition].toLowerCase()));

	            	writer.addMember(true);
        		} else {
	            	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into editor category refset: " + line);
    			}
            }

        	return true;
    	} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into editor category refset: " + line);
        	return false;
    	}
	}
}