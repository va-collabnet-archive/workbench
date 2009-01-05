package org.dwfa.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @goal vodb-open
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbOpen extends AbstractMojo {

    /**
     * Location of the vodb directory.
     *
     * @parameter default-value="${project.build.directory}/generated-resources/berkeley-db"
     * @required
     */
    File vodbDirectory;

    /**
     * True if the database is readonly.
     *
     * @parameter
     */
    Boolean readOnly = false;

    /**
     * Size of cache used by the database.
     *
     * @parameter
     */
    Long cacheSize = 600000000L;

    /**
     * Use existing if it is already open
     *
     * @parameter
     */
    boolean useExistingDb = false;


    /**
     * This parameter specifies whether to rerun this mojo even if it has run before.
     * @parameter default-value=false
     */
    boolean forceRerun;

    /**
     * @parameter
     */
    private DatabaseSetupConfig dbSetupConfig;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (useExistingDb && LocalVersionedTerminology.get() != null) {
                return;
            }

            if (!forceRerun) {
                try {
                    if (MojoUtil.alreadyRun(getLog(), vodbDirectory.getCanonicalPath())) {
                        return;
                    }
                } catch (NoSuchAlgorithmException e) {
                    throw new MojoExecutionException(e.getLocalizedMessage(), e);
                }
            }
            
            if (dbSetupConfig == null) {
                dbSetupConfig = new DatabaseSetupConfig();
            }
            getLog().info("vodb dir: " + vodbDirectory);
            LocalVersionedTerminology.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
        } catch (InstantiationException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IllegalAccessException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
		
	}

}
