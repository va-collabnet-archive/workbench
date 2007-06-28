package org.dwfa.mojo;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.cs.BinaryChangeSetWriter;
import org.dwfa.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;

/**
 * 
 * @goal vodb-add-change-set-writer
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbAddBinaryChangeSetWriter extends AbstractMojo {

    /**
     * Name for the binary change set.
     * @parameter
     */
	   private String changeSetFileName = "maven."
			+ UUID.randomUUID().toString() + ".jcs";

	    /**
	     * Name for the temporary binary change set.
	     * @parameter
	     */
	   private String changeSetTempFileName = changeSetFileName + ".temp";

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
            try {
                if (MojoUtil.alreadyRun(getLog(), changeSetFileName)) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
			I_ConfigAceFrame activeConfig = LocalVersionedTerminology.get()
			.getActiveAceFrameConfig();
			activeConfig.getChangeSetWriters().add(
					new BinaryChangeSetWriter(new File(changeSetFileName), new File(changeSetTempFileName)));
		} catch (TerminologyException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

}
