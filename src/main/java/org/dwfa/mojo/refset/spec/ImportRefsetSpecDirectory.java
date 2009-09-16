package org.dwfa.mojo.refset.spec;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.file.TupleFileUtil;
import org.dwfa.maven.MojoUtil;

/**
 * Imports all the refset specs in a specified directory.
 * 
 * @goal import-refset-spec-directory
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class ImportRefsetSpecDirectory extends AbstractMojo {

    /**
     * The refset spec directory.
     * 
     * @parameter default-value=
     *            "${project.build.directory}/generated-resources/refsetspec/"
     */
    File dir;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + dir.getCanonicalPath(), this
                .getClass(), targetDirectory)) {
                return;
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        try {
            TupleFileUtil tupleImporter = new TupleFileUtil();
            if (!dir.isDirectory()) {
                throw new Exception("Directory has not been configured : " + dir.getPath());
            } else {
                getLog().info("Importing refset specs from " + dir.getPath());
                for (File f : dir.listFiles()) {
                    getLog().info("Beginning import of refset spec :" + f.getPath());
                    // tupleImporter.importFile(f);
                    getLog().info("Finished importing refset spec from " + f.getPath());
                }

                LocalVersionedTerminology.get().commit();
            }

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}