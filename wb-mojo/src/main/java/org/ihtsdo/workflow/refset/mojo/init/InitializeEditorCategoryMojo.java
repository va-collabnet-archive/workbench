package org.ihtsdo.workflow.refset.mojo.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
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

    private static final int editorPosition = 0;							// 0
    private static final int semanticAreaPosition = editorPosition + 1;		// 1
    private static final int categoryPosition = semanticAreaPosition + 1;	// 2

    private static final int numberOfColumns = categoryPosition + 1;		// 3

    private static ViewCoordinate viewCoord;
    
   @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        System.setProperty("java.awt.headless", "true");
        String line = null;
        
        try {
            viewCoord = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
            EditorCategoryRefsetWriter writer = new EditorCategoryRefsetWriter();
        	BufferedReader inputFile = new BufferedReader(new FileReader(filePath));    	

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
        		} else if (reportErrors) {
        			AceLog.getAppLog().log(Level.WARNING, line, new Exception("Unable to import this row into editor category refset"));
    			}
            }

        	Terms.get().addUncommitted(writer.getRefsetConcept());
        } catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, line);
		}
	}

    public File getTargetDirectory() {
        return targetDirectory;
	}

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
	}

    private ConceptVersionBI getEditor(String editor) throws TerminologyException, IOException {
    	return WorkflowHelper.lookupModeler(editor);
    }
}
