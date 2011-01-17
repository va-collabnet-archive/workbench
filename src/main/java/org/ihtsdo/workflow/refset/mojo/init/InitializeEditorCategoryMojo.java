package org.ihtsdo.workflow.refset.mojo.init;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefset;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * @author Jesse Efron
 * 
 * @goal initialize-editor-category-refset
 * @requiresDependencyResolution compile
 */

public class InitializeEditorCategoryMojo extends AbstractMojo {

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     */
    private File baseDirectory;

    /**
     * The name of the database to create. All sql inserts will be against this
     * database.
     * 
     * @parameter
     * @required
     */
    private String filePath;

    private String basePath = "/../resources/";
    
    public void execute() throws MojoExecutionException, MojoFailureException 
    {
        System.setProperty("java.awt.headless", "true");
        String line = null;
        
        try {
            I_GetConceptData  currentReferencedCompId = null;

            String resourceFilePath = baseDirectory.getAbsoluteFile() + basePath + filePath;
            System.out.println(resourceFilePath);

        	EditorCategoryRefset refset = new EditorCategoryRefset();
            I_TermFactory tf = Terms.get();

        	refset = new EditorCategoryRefset();
            EditorCategoryRefsetWriter writer = new EditorCategoryRefsetWriter();

            Scanner scanner = new Scanner(new File(resourceFilePath));

            while (scanner.hasNextLine())
            {
            	line = scanner.nextLine();
            	String[] columns = line.split("\t");
            
            	writer.setEditor(getEditor(columns[0]));
            	writer.setSemanticArea(columns[1]);
            	writer.setCategory(WorkflowHelper.lookupEditorCategory(columns[2]));

            	writer.addMember();
            }
            	
            Terms.get().addUncommitted(writer.getRefsetConcept());
        } catch (Exception e) {
        	System.out.println("Failure with line: " + line);
			throw new MojoExecutionException(e.getMessage());
		}
	}
    
    public File getTargetDirectory() {
        return targetDirectory;
	}

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
	}
    
    private I_GetConceptData getEditor(String editor) throws TerminologyException, IOException {
    	return WorkflowHelper.lookupModeler(editor);
    }
}
