package org.ihtsdo.mojo.mojo;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.util.io.FileIO;

/**
 * Goal which loads an EConcept.jbin file into a bdb.
 * 
 * @goal load-econcepts-multi
 * 
 * @phase vodb-mutable-to-readonly
 */

public class VodbMutableToReadOnly extends AbstractMojo {

    /**
     * Berkeley directory.
     * 
     * @parameter expression="${project.build.directory}/classes/berkeley-db"
     * @required
     */
    private File berkeleyDir;

    public void execute() throws MojoExecutionException {
        executeMojo(berkeleyDir);

    }

    void executeMojo(File berkeleyDir) throws MojoExecutionException {
        try {
            FileIO.recursiveDelete(new File(berkeleyDir, "read-only"));
            File dirToMove = new File(berkeleyDir, "mutable");
            dirToMove.renameTo(new File(berkeleyDir, "read-only"));
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        } catch (Throwable ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }
    }
}
