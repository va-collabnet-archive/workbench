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
import org.ihtsdo.workflow.refset.semhier.SemanticAreaHierarchyRefset;
import org.ihtsdo.workflow.refset.semhier.SemanticAreaHierarchyRefsetWriter;

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
    
    public void execute() throws MojoExecutionException, MojoFailureException 
    {
        SemanticAreaHierarchyRefset refset = new SemanticAreaHierarchyRefset();
        System.setProperty("java.awt.headless", "true");
        try {
            try {
                if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName(), this.getClass(), targetDirectory)) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }

            I_TermFactory tf = Terms.get();

            SemanticAreaHierarchyRefsetWriter writer = new SemanticAreaHierarchyRefsetWriter();

            File f= new File("src/main/resources/textRefset/semParRefset.txt");
            Scanner scanner = new Scanner(f);

            while (scanner.hasNextLine())
            {
            	String line = scanner.nextLine();
            	String[] columns = line.split("\t");
            
            	writer.setChildSemanticArea(columns[0]);
            	writer.setParentSemanticArea(columns[1]);

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
    
}
