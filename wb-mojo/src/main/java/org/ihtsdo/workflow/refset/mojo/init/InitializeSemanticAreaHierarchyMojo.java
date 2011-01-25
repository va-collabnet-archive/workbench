package org.ihtsdo.workflow.refset.mojo.init;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Scanner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.workflow.refset.semHier.SemanticAreaHierarchyRefset;
import org.ihtsdo.workflow.refset.semHier.SemanticAreaHierarchyRefsetWriter;

/**
 * @author Jesse Efron
 * 
 * @goal initialize-semantic-area-hierarchy-refset
 * @requiresDependencyResolution compile
 */ 

public class InitializeSemanticAreaHierarchyMojo extends AbstractMojo {

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
        try {

            String resourceFilePath = baseDirectory.getAbsoluteFile() + basePath + filePath;
 
            SemanticAreaHierarchyRefset refset = new SemanticAreaHierarchyRefset();
            I_TermFactory tf = Terms.get();

            SemanticAreaHierarchyRefsetWriter writer = new SemanticAreaHierarchyRefsetWriter();

            Scanner scanner = new Scanner(new File(resourceFilePath));

            while (scanner.hasNextLine())
            {
            	String line = scanner.nextLine();
            	String[] columns = line.split("\t");
            
            	writer.setChildSemanticArea(columns[0]);
            	writer.setParentSemanticArea(columns[1]);

            	writer.addMember();
            }

            // Single RefCompId, so commit at end

            Terms.get().addUncommitted(writer.getRefsetConcept());
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage());
		}

	}

    public File getTargetDirectory() {
        return targetDirectory;
	}

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
	}
    
}
