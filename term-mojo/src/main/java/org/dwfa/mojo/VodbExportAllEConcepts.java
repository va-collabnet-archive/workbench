package org.dwfa.mojo;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.Terms;
import org.dwfa.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;


/**
* 
* @goal vodb-export-all-econcepts
* 
* @phase process-resources
* @requiresDependencyResolution compile
*/

public class VodbExportAllEConcepts extends AbstractMojo implements I_ProcessConcepts {
    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;


    private transient int conceptCount = 0;
    
    private transient int debugCount = 0;



	private DataOutputStream eConceptDOS;
    private static int conceptLimit = Integer.MAX_VALUE;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
    	getLog().info("VodbExportAllEConcepts execute called");
       try {
            try {
                if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName(), this.getClass(),
                    targetDirectory)) {
                	getLog().info("Already run ....returning");
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
            
            File eConceptsFile = new File(targetDirectory, "classes/eConcepts.jbin");
            getLog().info("Created eConcepts.jbin");
            eConceptsFile.getParentFile().mkdirs();
            BufferedOutputStream eConceptsBos = new BufferedOutputStream(new FileOutputStream(eConceptsFile));
            eConceptDOS = new DataOutputStream(eConceptsBos);
            getLog().info("About to iterate concepts");
            Terms.get().iterateConcepts(this);
            getLog().info("Finished Iterating closing Stream");
            eConceptDOS.close();
            getLog().info("Finished");
        

        } catch (TerminologyException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

	public void processConcept(I_GetConceptData concept) throws Exception {
		
		//if(conceptCount == 0) {
			getLog().info("processConcept called " +concept);
			getLog().info("processConcept conceptCount = " +conceptCount);
		//}
		
		
		if (conceptCount < conceptLimit) {
			getLog().info("Turning Concept into EConcept");
			boolean found = false;
		/*if (concept.getUids().contains(UUID.fromString("181e45e8-b05a-33da-8b52-7027cbee6856"))) {
				System.out.println("\n\nWriting entry: " + conceptCount);
				System.out.println("\n\nWriting: " + concept);
				found = true;
			}*/
			if(conceptCount == 0 || debugCount == 1000) {
				getLog().info("\n\nWriting entry: " + conceptCount);
				getLog().info("\n\nWriting: " + concept);
				found = true;
			}
			getLog().info("About to create EConcept");
			EConcept eC = new EConcept(concept);
			getLog().info("EConcept created about to write");
			eC.writeExternal(eConceptDOS);
			getLog().info("EConcept writen");
			if (found) {
				System.out.println("\n\nWrote: " + eC);
				System.out.println("\n\n");
			}
			debugCount++;
			if(debugCount == 1000) {
				System.out.println("\n\nFound: " + conceptCount);	
				debugCount = 0;
			}
			conceptCount++;
		}
	}

}
