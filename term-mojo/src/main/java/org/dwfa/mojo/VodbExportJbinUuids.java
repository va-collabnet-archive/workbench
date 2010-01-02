package org.dwfa.mojo;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_ProcessIds;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;


/**
* 
* @goal vodb-export-jbin-uuids
* 
* @phase process-resources
* @requiresDependencyResolution compile
*/

public class VodbExportJbinUuids extends AbstractMojo implements I_ProcessIds, I_ProcessConcepts {
    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;


    private transient DataOutputStream dos;
    private transient int idCount = 0;
    private transient int uuidCount = 0;
    private transient int conceptCount = 0;


	private ObjectOutputStream oos;


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
            I_ConfigAceFrame activeConfig = LocalVersionedTerminology.get().getActiveAceFrameConfig();
            I_TermFactory tf = LocalVersionedTerminology.get();
            File outFile = new File(targetDirectory, "classes/uuids.jbin");
            outFile.getParentFile().mkdirs();
            OutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
            dos = new DataOutputStream(bos);
            tf.iterateIds(this);
            dos.close();
            getLog().info("Wrote " + idCount + " ids");
            getLog().info("Wrote " + uuidCount + " uuids");
            Properties props = new Properties();
            props.put("idCount", Integer.toString(idCount));
            props.put("uuidCount", Integer.toString(uuidCount));
            File propFile = new File(targetDirectory, "classes/exportData.xml");
            FileOutputStream fos = new FileOutputStream(propFile);
            props.storeToXML(fos, "");
            fos.close();
 
            File conceptsFile = new File(targetDirectory, "classes/concepts.jbin");
            bos = new BufferedOutputStream(new FileOutputStream(conceptsFile));
            oos = new ObjectOutputStream(bos);
            
            File eConceptsFile = new File(targetDirectory, "classes/eConcepts.jbin");
            BufferedOutputStream eConceptsBos = new BufferedOutputStream(new FileOutputStream(eConceptsFile));
            eConceptDOS = new DataOutputStream(eConceptsBos);
            
            tf.iterateConcepts(this);

            oos.close();
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

	public void processId(I_Identify versionedId)
			throws Exception {
		idCount++;
		List<UUID> uuids = versionedId.getUUIDs();
		dos.writeInt(uuids.size());
		for (UUID uuid: uuids) {
			uuidCount++;
			dos.writeLong(uuid.getMostSignificantBits());
			dos.writeLong(uuid.getLeastSignificantBits());
		}
	}

	public void processConcept(I_GetConceptData concept) throws Exception {
		if (conceptCount < conceptLimit) {
			//UniversalAceBean ubean = concept.getUniversalAceBean();
			//oos.writeObject(ubean);
			EConcept eC = new EConcept(concept);
			eC.writeExternal(eConceptDOS);
			conceptCount++;
			if (conceptCount % 100 == 0) {
				System.out.print("\b\b\b\b\b\b\b\b");
				System.out.print(conceptCount);
			}
		}
	}

}
