package org.dwfa.mojo;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    
    SimpleDateFormat simple = new SimpleDateFormat("dd.MM.yyyy H:m:s");



	private DataOutputStream eConceptDOS;
    //private static int conceptLimit = Integer.MAX_VALUE;
    
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
            Date startD = new Date();       
            long dateLongStart = startD.getTime();
            getLog().info("About to iterate concepts starting at: "+simple.format(startD));
            Terms.get().iterateConcepts(this);
            getLog().info("Finished Iterating closing Stream processed "+conceptCount +" concepts");
            
            Date endD = new Date(); 
            long dateLongEnd = endD.getTime();
            long millistaken = dateLongEnd - dateLongStart;
            long seconds = millistaken/1000;
            getLog().info("Finished at = "+simple.format(endD));
            getLog().info("Time taken in seconds = "+seconds);
            getLog().info("Concepts/Second = "+conceptCount/seconds);
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
		
	if(conceptCount == 0) {
			getLog().info("processConcept called " +concept);
	}
		
	//if (conceptCount < conceptLimit) {
		EConcept eC = new EConcept(concept);
		eC.writeExternal(eConceptDOS);
		debugCount++;
		if(debugCount == 1000) {
			getLog().info("\n\n Found: " + conceptCount);
			getLog().info("\n\n Wrote: " + concept);	
			debugCount = 0;
		}
		conceptCount++;	
	//}
	}
}
