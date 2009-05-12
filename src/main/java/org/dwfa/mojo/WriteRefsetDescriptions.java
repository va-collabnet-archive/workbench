package org.dwfa.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.refset.members.CleanableProcessExtByRef;
import org.dwfa.ace.task.refset.members.WriteRefsetDescriptionsProcessExtByRef;
import org.dwfa.maven.MojoUtil;

import java.io.File;

/**
 *
 * @goal write-refset-descriptions
 *
 * @phase process-classes
 * @requiresDependencyResolution compile
 * @author Dion McMurtrie
 */
public class WriteRefsetDescriptions extends AbstractMojo {

	/**
	 * Location of the directory to output data files to.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			if (MojoUtil.alreadyRun(getLog(), outputDirectory.getAbsolutePath(), this.getClass(), targetDirectory)) {
				return;
			}

           CleanableProcessExtByRef exporter = new WriteRefsetDescriptionsProcessExtByRef(new MojoLogger(getLog()),
                    outputDirectory);
            LocalVersionedTerminology.get().iterateExtByRefs(exporter);
            exporter.clean();

        } catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}
}