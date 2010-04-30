package org.ihtsdo.mojo.mojo;

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
import org.ihtsdo.mojo.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;


/**
* 
* @goal vodb-export-econcepts
* 
* @phase process-resources
* @requiresDependencyResolution compile
*/

public class VodbExportEConcepts extends AbstractMojo implements I_ProcessConcepts {
    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;


    private transient int conceptCount = 0;



	private DataOutputStream eConceptDOS;
    private static int conceptLimit = Integer.MAX_VALUE;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            try {
                if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName(), this.getClass(),
                    targetDirectory)) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
            
            File eConceptsFile = new File(targetDirectory, "classes/eConcepts.jbin");
            eConceptsFile.getParentFile().mkdirs();
            BufferedOutputStream eConceptsBos = new BufferedOutputStream(new FileOutputStream(eConceptsFile));
            eConceptDOS = new DataOutputStream(eConceptsBos);
            Terms.get().iterateConcepts(this);
            eConceptDOS.close();
            getLog().info("Wrote " + conceptCount + " concepts");

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
		if (conceptCount < conceptLimit) {
			boolean found = false;
			EConcept eC = new EConcept(concept);
			eC.writeExternal(eConceptDOS);
			if (found) {
				System.out.println("\n\nWrote: " + eC);
				System.out.println("\n\n");
			}
			conceptCount++;
		}
	}

}
