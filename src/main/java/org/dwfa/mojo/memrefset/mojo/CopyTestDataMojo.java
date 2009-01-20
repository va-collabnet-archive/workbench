package org.dwfa.mojo.memrefset.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.mojo.relformat.mojo.sql.filter.FileMatcherImpl;
import org.dwfa.mojo.relformat.mojo.sql.io.FileLister;
import org.dwfa.mojo.relformat.mojo.sql.io.FileListerImpl;
import org.dwfa.util.io.FileIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This plugin copies unique test cases for the refset test framework from
 * target/changesets/users/processes/member-refset to src/test/resources/expected.
 *
 * @goal copy-test-cases-to-expected-dir
 */
public final class CopyTestDataMojo extends AbstractMojo {

   /**
    * Directory from which the test files are read.
    * @parameter default-value=${project.build.directory}/changesets/users/processes/member-refset
    */
    private File sourceDirectory;

    /**
     * Directory to which output files are written.
     *
     * @parameter default-value=${basedir}/src/test/resources/expected
     */
    private File outputDirectory;

    /**
     * Default extension of input files.
     * @parameter default-value=xml
     */
    private String inputExtension;

    private final FileLister fileLister = new FileListerImpl(new FileMatcherImpl());

    private final ChangeSetNameComparer nameComparer = new ChangeSetNameComparerImpl();

    public void execute() throws MojoExecutionException, MojoFailureException {
        List<File> actualFiles = fileLister.list(sourceDirectory, getXMLFiles(), Arrays.<String>asList());
        List<File> expectedFiles = fileLister.list(outputDirectory, getXMLFiles(), Arrays.<String>asList());

        for (File actualFile : actualFiles) {
            if (!alreadyExists(actualFile, expectedFiles)) {
                try {
                    FileIO.copyFile(actualFile, new File(outputDirectory, actualFile.getName()));
                } catch (IOException e) {
                    getLog().warn("Could not copy file: " + actualFile + " to " + outputDirectory);
                }
            }
        }
    }

    private boolean alreadyExists(final File actualFile, final List<File> expectedFiles) {
        String fullActualName = actualFile.getName();
        String prefix = fullActualName.substring(0, fullActualName.indexOf('.'));
        return nameComparer.containsPrefix(prefix, getFileNames(expectedFiles));
    }

    private List<String> getFileNames(final List<File> expectedFiles) {
        List<String> files = new ArrayList<String>();

        for (File expectedFile : expectedFiles) {
            files.add(expectedFile.getName());
        }

        return files;
    }

    private List<String> getXMLFiles() {
        return Arrays.asList("(.)*\\." + inputExtension);
    }
}
