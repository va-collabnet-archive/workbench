package org.dwfa.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.refset.members.CleanableProcessExtByRef;
import org.dwfa.ace.task.refset.members.CleanableProcessExtByRefBuilder;
import org.dwfa.ace.task.refset.members.WriteRefsetDescriptionsProcessExtByRefBuilder;
import org.dwfa.ace.task.util.Logger;
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


    private final I_TermFactory termFactory;

    private final CleanableProcessExtByRefBuilder cleanableProcessExtByRefBuilder;


    public WriteRefsetDescriptions() {
        //default constructor for maven.
        termFactory = LocalVersionedTerminology.get();
        cleanableProcessExtByRefBuilder = new WriteRefsetDescriptionsProcessExtByRefBuilder();
    }

    //Used for testing.
    WriteRefsetDescriptions(final File outputDirectory, final File targetDirectory, final I_TermFactory termFactory,
                            final CleanableProcessExtByRefBuilder cleanableProcessExtByRefBuilder) {
        this.outputDirectory = outputDirectory;
        this.targetDirectory = targetDirectory;
        this.termFactory = termFactory;
        this.cleanableProcessExtByRefBuilder = cleanableProcessExtByRefBuilder;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			if (MojoUtil.alreadyRun(getLog(), outputDirectory.getAbsolutePath(), getClass(), targetDirectory)) {
				return;
			}

            Logger logger = new MojoLogger(getLog());

            CleanableProcessExtByRef refsetDescriptionWriter = cleanableProcessExtByRefBuilder.
                                                                withTermFactory(termFactory).
                                                                withLogger(logger).
                                                                withSelectedDir(targetDirectory).
                                                                build();

            //test what happens when an exception occurs
            termFactory.iterateExtByRefs(refsetDescriptionWriter);
            refsetDescriptionWriter.clean();
        } catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}
}