package org.ihtsdo.workflow.refset.mojo.init;

import java.io.File;
import java.security.NoSuchAlgorithmException;
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
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefset;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetHelper;

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


    private HashMap<String, I_GetConceptData> modelers = null;
    
    public void execute() throws MojoExecutionException, MojoFailureException 
    {
        System.setProperty("java.awt.headless", "true");
        EditorCategoryRefset refset = null;
        
        try {
            try {
                if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName(), this.getClass(), targetDirectory)) {
                    return;
                } 
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
            I_TermFactory tf = Terms.get();

            modelers = new HashMap<String, I_GetConceptData>();
        	refset = new EditorCategoryRefset();
            EditorCategoryRefsetWriter writer = new EditorCategoryRefsetWriter();
            File f= new File("src/main/resources/textRefset/edCatRefset.txt");
            String s = f.getAbsolutePath();
            Scanner scanner = new Scanner(f);

            modelers = WorkflowRefsetHelper.getModelers();

            while (scanner.hasNextLine())
            {
            	String line = scanner.nextLine();
            	String[] columns = line.split("\t");
            
            	writer.setClinicalEditorField(getEditor(columns[0]));
            	writer.setSemanticArea(columns[1]);
            	writer.setEditorCategory(WorkflowRefsetHelper.getModelerCategory(columns[2]));

            	writer.addMember();
            	
    	        Collection<? extends I_ExtendByRef> extVersions = Terms.get().getRefsetExtensionMembers(refset.getRefsetId());
    	        int size = extVersions.size();
            };
        
	        tf.addUncommitted(tf.getConcept(refset.getRefsetConcept()));
	        //RefsetReaderUtility.getContents(refset.getRefsetId());
        } catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			throw new MojoExecutionException(e.getMessage());
		}
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
