package org.ihtsdo.workflow.refset.mojo.init;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.semArea.SemanticAreaSearchRefset;
import org.ihtsdo.workflow.refset.semArea.SemanticAreaSearchRefsetWriter;


/**
 * @author Jesse Efron
 * 
 * @goal initialize-semantic-area-search-refset
 * @requiresDependencyResolution compile
 */
  
public class InitializeSemanticAreaSearchMojo extends AbstractMojo {

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
	
    private static final int searchTermPosition = 0;							// 0
    private static final int hierarchyPosition = searchTermPosition + 1;		// 1

    private static final int numberOfColumns = hierarchyPosition + 1;			// 2

    private SemanticAreaSearchRefsetWriter writer = null;
    
    public void execute() throws MojoExecutionException, MojoFailureException 
    {
        System.setProperty("java.awt.headless", "true");
        try {

        	SemanticAreaSearchRefset refset = new SemanticAreaSearchRefset();
            I_TermFactory tf = Terms.get();
            
            writer = new SemanticAreaSearchRefsetWriter();

            processHierarchies(new File(filePath));
       
	        tf.addUncommitted(refset.getRefsetConcept());
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			throw new MojoExecutionException(e.getMessage());
		}
	}

    private void processHierarchies(File f) throws TerminologyException, IOException {
        Scanner scanner = new Scanner(f);

        while (scanner.hasNextLine())
        {
        	String line = scanner.nextLine();
        	
        	if (line.trim().length() == 0)
        		continue;
        	
        	String[] columns = line.split("\t");

        	try {
        		if (columns.length == numberOfColumns)
        		{
        			writer.setSearchTerm(columns[searchTermPosition]);
        			writer.setHierarchy(Terms.get().getConcept(UUID.fromString(columns[hierarchyPosition])));

            		writer.addMember();
        		} else if (reportErrors) {
    				AceLog.getAppLog().log(Level.WARNING, line, new Exception("Unable to import this row into semantic area search refset"));
    			}
        	} catch (Exception e) {
            	AceLog.getAppLog().log(Level.WARNING, "Exception: " + e.getMessage() + " at line: " + line);
        	}
        };

        Terms.get().addUncommitted(writer.getRefsetConcept());
    }
}
