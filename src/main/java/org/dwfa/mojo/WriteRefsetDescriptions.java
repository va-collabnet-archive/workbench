package org.dwfa.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.select.DescriptionSelector;
import org.dwfa.ace.select.DescriptionSelector.LanguagePreference;
import org.dwfa.ace.task.refset.members.CleanableProcessExtByRef;
import org.dwfa.ace.task.refset.members.CleanableProcessExtByRefBuilder;
import org.dwfa.ace.task.refset.members.ForTesting;
import org.dwfa.ace.task.refset.members.WriteRefsetDescriptionsProcessExtByRefBuilder;
import org.dwfa.ace.task.util.Logger;

/**
 * Mojo that exports all reference sets.
 * 
 * @goal write-refset-descriptions
 *
 * @phase process-classes
 * @requiresDependencyResolution compile
 * @author Dion McMurtrie
 */
public class WriteRefsetDescriptions extends AbstractMojo {

	/**
	 * Location of the directory to output data files to.  Refset files are exported to this directory.
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

    /**
     * A comma separated list of languages used to selected the most preferred description for each concept written.
     * @parameter 
     */
    private String language;

    private final I_TermFactory termFactory;

    private final CleanableProcessExtByRefBuilder cleanableProcessExtByRefBuilder;

    private final MojoUtilWrapper mojoUtilWrapper;

    //default constructor for maven.
    public WriteRefsetDescriptions() {
        termFactory = LocalVersionedTerminology.get();
        mojoUtilWrapper = new MojoUtilWrapperImpl();
        cleanableProcessExtByRefBuilder = new WriteRefsetDescriptionsProcessExtByRefBuilder();
    }

    @ForTesting
    WriteRefsetDescriptions(final File outputDirectory, final I_TermFactory termFactory,
        final CleanableProcessExtByRefBuilder cleanableProcessExtByRefBuilder, final File targetDirectory,
        final MojoUtilWrapper mojoUtilWrapper) {
        
        this.outputDirectory = outputDirectory;
        this.targetDirectory = targetDirectory;
        this.termFactory = termFactory;
        this.cleanableProcessExtByRefBuilder = cleanableProcessExtByRefBuilder;
        this.mojoUtilWrapper = mojoUtilWrapper;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			if (mojoUtilWrapper.alreadyRun(getLog(), outputDirectory.getAbsolutePath(), getClass(), targetDirectory)) {
				return;
			}

            Logger logger = new MojoLogger(getLog());

            if (language != null) {           	
            	cleanableProcessExtByRefBuilder.withLanguagePreference(
            			new DescriptionSelector(new LanguagePreference(csvToArray(language))));
            }
            
            CleanableProcessExtByRef refsetDescriptionWriter = cleanableProcessExtByRefBuilder.
                                                                withTermFactory(termFactory).
                                                                withLogger(logger).
                                                                withSelectedDir(outputDirectory).
                                                                build();
            try {
                termFactory.iterateExtByRefs(refsetDescriptionWriter);
            } finally {
                //close any open files.
                refsetDescriptionWriter.clean();
            }
        } catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}
    
    /**
     * Convert a comma separated list of values into an array of values. 
     */
    private String[] csvToArray(String csv) {
    	ArrayList<String> values = new ArrayList<String>();
    	StringTokenizer tokens = new StringTokenizer(csv, ",");
    	while (tokens.hasMoreTokens()) {
    		values.add(tokens.nextToken());
    	}
    	return values.toArray(new String[]{});
    }
}