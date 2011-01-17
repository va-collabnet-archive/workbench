package org.ihtsdo.workflow.refset.mojo.init;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefset;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * @author Alex Byrnes
 * 
 * @goal initialize-user-permission-refset
 * @requiresDependencyResolution compile
 */

public class InitializeUserPermissionMojo extends AbstractMojo {

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;


    private HashMap<String, I_GetConceptData> modelers = null;
    
    public void execute() throws MojoExecutionException, MojoFailureException 
    {
        System.setProperty("java.awt.headless", "true");
        
        try {
            
            EditorCategoryRefset refset = new EditorCategoryRefset();
            I_TermFactory tf = Terms.get();

            modelers = new HashMap<String, I_GetConceptData>();
        	refset = new EditorCategoryRefset();
            EditorCategoryRefsetWriter writer = new EditorCategoryRefsetWriter();
            File f= new File("src/main/resources/textRefset/userPermissionRefset.txt");
            String s = f.getAbsolutePath();
            Scanner scanner = new Scanner(f);

            modelers = WorkflowHelper.getModelers();

            while (scanner.hasNextLine())
            {
            	String line = scanner.nextLine();

            	String[] columns = line.split(",");
            	//Get rid of "User permission"
            	columns[0] = (String) columns[0].subSequence("User permission (".length(), columns[0].length());
            	//remove ")"
            	columns[2] = columns[2].substring(0, columns[2].length() - 1);
            	
            	int i = 0;
            	for (String c : columns) {
            		columns[i++] = c.split("=")[1].trim();
            	}
            	
            	//System.out.println(columns[0] + "\t" + columns[1] + "\t" + columns[2] + "\n");
                    	
            	writer.setEditor(getEditor(columns[0]));
            	writer.setSemanticArea(columns[1]);
            	
            	writer.setCategory(WorkflowHelper.lookupEditorCategory(columns[2]));
            	writer.addMember();
            	
    	        Collection<? extends I_ExtendByRef> extVersions = Terms.get().getRefsetExtensionMembers(refset.getRefsetId());
    	        int size = extVersions.size();
            };
        
	        tf.addUncommitted(refset.getRefsetConcept());
	        //RefsetReaderUtility.getContents(refset.getRefsetId());
        } catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			throw new MojoExecutionException(e.getMessage());
		}
	}
    
    private String convertLongFormCategory (String c){
    	
    	return c.substring(c.length() - 1).toLowerCase();
    	
    }
    
    public File getTargetDirectory() {
        return targetDirectory;
	}

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
	}
    
    private I_GetConceptData getEditor(String editor) {
    	return modelers.get(editor);
    }
}
