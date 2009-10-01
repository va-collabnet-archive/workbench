package org.dwfa.mojo.refset.spec;

import java.io.File;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.file.TupleFileUtil;
import org.dwfa.maven.MojoUtil;
import org.dwfa.mojo.ConceptDescriptor;

/**
 * Import the specified refset spec from a file.
 * 
 * @goal import-single-refset-spec
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class ImportSingleRefsetSpec extends AbstractMojo {

    /**
     * The refset spec.
     * 
     * @parameter default-value=
     *            "${project.build.directory}/generated-resources/refsetspec/"
     * @required
     */
    private File refsetSpecFile;

    /**
     * The report file.
     * 
     * @parameter default-value=
     *            "${project.build.directory}/generated-resources/refsetspec/"
     * @required
     */
    private File reportFile;

    /**
     * Optional edit path (this will override refset spec file path data).
     * 
     * @parameter
     */
    private ConceptDescriptor editPathDescriptor = null;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + refsetSpecFile.getCanonicalPath()
                + reportFile.getCanonicalPath() + editPathDescriptor, this.getClass(), targetDirectory)) {
                return;
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        try {
            reportFile.getParentFile().mkdirs();
            TupleFileUtil tupleImporter = new TupleFileUtil();
            UUID uuid = null;
            if (editPathDescriptor != null) {
                uuid = editPathDescriptor.getVerifiedConcept().getUids().iterator().next();
            }
            getLog().info("Beginning import of refset spec :" + refsetSpecFile.getPath());
            tupleImporter.importFile(refsetSpecFile, reportFile, uuid);
            getLog().info("Finished importing refset spec from " + refsetSpecFile.getPath());

            LocalVersionedTerminology.get().commit();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}