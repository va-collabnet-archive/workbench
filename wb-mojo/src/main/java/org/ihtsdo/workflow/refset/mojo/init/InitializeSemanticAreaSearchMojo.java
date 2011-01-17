package org.ihtsdo.workflow.refset.mojo.init;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.semArea.SemanticAreaSearchRefset;
import org.ihtsdo.workflow.refset.semArea.SemanticAreaSearchRefsetWriter;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefset;


/**
 * @author Jesse Efron
 * 
 * @goal initialize-semantic-area-search-refset
 * @requiresDependencyResolution compile
 */
  
public class InitializeSemanticAreaSearchMojo extends AbstractMojo {

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

    private SemanticAreaSearchRefsetWriter writer = null;
    
    public void execute() throws MojoExecutionException, MojoFailureException 
    {
        System.setProperty("java.awt.headless", "true");
        try {
            
            String resourceFilePath = baseDirectory.getAbsoluteFile() + basePath + filePath;
            System.out.println(resourceFilePath);

        	SemanticAreaSearchRefset refset = new SemanticAreaSearchRefset();
            I_TermFactory tf = Terms.get();
            
            writer = new SemanticAreaSearchRefsetWriter();

            processV2StateTransitions(resourceFilePath);
                                
	        tf.addUncommitted(refset.getRefsetConcept());
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			throw new MojoExecutionException(e.getMessage());
		}
	}

    private void processV2StateTransitions(String resourceFilePath) throws TerminologyException, IOException {
        System.out.println(resourceFilePath);
        processHierarchies(new File(resourceFilePath));
    }
    

    private void processHierarchies(File f) throws TerminologyException, IOException {
    	SemanticAreaSearchRefset refset = new SemanticAreaSearchRefset();
        Scanner scanner = new Scanner(f);

        while (scanner.hasNextLine())
        {
        	String line = scanner.nextLine();
        	
        	if (line.trim().length() == 0)
        		continue;
        	
        	String[] columns = line.split("\t");
        	try {
        		writer.setHierarchy(Terms.get().getConcept(UUID.fromString(columns[1])));
        		writer.setSearchTerm(columns[0]);

        		writer.addMember();
        	} catch (Exception e) {
        		e.printStackTrace();
        		System.out.println("Row: " + line);
        	}
        };

        Terms.get().addUncommitted(writer.getRefsetConcept());
    }
}
