package org.dwfa.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.dwfa.util.io.FileIO;

/**
 * Goal which copies a directory to the specified location (including hidden files).
 * 
 * @goal copy-dir
 * @requiresDependencyResolution compile
 * 
 */

public class CopyDirectory extends AbstractMojo {
    /**
     * Location of the build directory.
     * 
     * @parameter
     * @required
     */
    private File inputDirectory;
    
    /**
     * Location of the build directory.
     * 
     * @parameter
     * @required
     */
    private File outputDirectory;

    /**
     * Location of the build directory.
     * 
     * @parameter
     * @required
     */
    private Boolean copyInvisibles;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Log l = getLog();
            l.info("Now executing CopyDirectory from: " + inputDirectory + 
            		" to: " + outputDirectory + " invisibles: " + copyInvisibles);

            // calculate the SHA-1 hashcode for this mojo based on input
            if (MojoUtil.alreadyRun(l, inputDirectory.getAbsolutePath() + outputDirectory.getAbsolutePath(), 
            		this.getClass(), targetDirectory)) {
                return;
            }
            FileIO.recursiveCopy(inputDirectory, outputDirectory, copyInvisibles);

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }


}
