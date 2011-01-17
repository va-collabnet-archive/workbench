package org.dwfa.ace.task;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefset;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * @author Alex Byrnes
 * 
 */

public class InitializeUserPermission {


    private File targetDirectory;

    public void execute() 
    {

    	HashMap<String, I_GetConceptData> modelers = null;

        System.setProperty("java.awt.headless", "true");

        try {

            EditorCategoryRefset refset = new EditorCategoryRefset();
            I_TermFactory tf = Terms.get();

            modelers = new HashMap<String, I_GetConceptData>();
        	refset = new EditorCategoryRefset();
            EditorCategoryRefsetWriter writer = new EditorCategoryRefsetWriter();
            File f= new File("workflow/userPermissionRefset.txt");

            Scanner scanner = new Scanner(f);

            modelers = WorkflowHelper.getModelers();

            while (scanner.hasNextLine())
            {
            	String line = scanner.nextLine();

            	String[] columns = line.split(",");
            	//Get rid of "User permission"
            	columns[0] = (String) columns[0].subSequence("User permission (".length(), columns[0].length());
            	//remove ")"
            	columns[2] = columns[2].trim();
            	columns[2] = columns[2].substring(0, columns[2].length() - 1);

            	int i = 0;
            	for (String c : columns) {
            		columns[i++] = c.split("=")[1].trim();
            	}

            	//System.out.println(columns[0] + "\t" + columns[1] + "\t" + columns[2] + "\n");

            	writer.setEditor(modelers.get(columns[0]));
            	writer.setSemanticArea(columns[1]);

            	writer.setCategory(WorkflowHelper.lookupEditorCategory(columns[2]));
            	writer.addMember();
            	

            };

	        tf.addUncommitted(refset.getRefsetConcept());
	        //RefsetReaderUtility.getContents(refset.getRefsetId());
        } catch (Exception e) {
			e.printStackTrace();
			e.getMessage();

		}
	}



    public File getTargetDirectory() {
        return targetDirectory;
	}

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
	}

}
