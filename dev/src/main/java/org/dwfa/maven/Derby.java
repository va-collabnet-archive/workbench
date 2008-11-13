package org.dwfa.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.maven.derby.DerbyClient;
import org.dwfa.maven.derby.DerbyClientImpl;
import org.dwfa.maven.derby.DerbyHashBuilder;
import org.dwfa.maven.derby.LogFileCreatorImpl;
import org.dwfa.maven.derby.SQLFileTransformationCopier;
import org.dwfa.maven.derby.SQLFileTransformationCopierImpl;
import org.dwfa.maven.derby.SQLSourceFinderImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Goal which executes derby sql commands to generate a
 * database or perform other such tasks.
 *
 * @goal run-derby
 * @phase process-resources
 */
public class Derby extends AbstractMojo {

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Location of the source directory.
     *
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     */
    private File sourceDirectory;

    /**
     * Specify the plugin version.
     *
     * @parameter expression="${project.version}"
     * @required
     */
    private String version;

    /**
     * The name of the database to create. All sql inserts will be against this database.
     *
     * @parameter
     * @required
     */
    private String dbName;

    /**
     * Specifies a list of source sql files.
     *
     * @parameter
     */
    private String[] sources = {};

    /**
     * Specifies whether to replace the "/" with a platform specific version.
     *
     * @parameter
     */
    private boolean replaceForwardSlash = true;

    /**
     * Specifies the direct location of sql files. No copying is down between sourceDirectory and the target directory.
     * When this is specified:
     * sourceDirectory,
     * sources and 
     * replaceForwardSlash is ignored.
     *
     * @parameter
     */
    private String[] sqlLocations = {};

    /**
     * Turns verbose on|off. The default is false.
     * Be careful when running with verbose on. If the sql file size is very large it could lead to OutOfMemoryErrors. 
     *
     * @parameter
     */
    private boolean verbose = false;


    /**
    * List of source roots containing non-test code.
    * @parameter default-value="${project.compileSourceRoots}"
    * @required
    * @readonly
    */
    private List sourceRoots;

    public void execute() throws MojoExecutionException, MojoFailureException {
        
        SQLFileTransformationCopier copier = new SQLFileTransformationCopierImpl(getLog(), outputDirectory,
                replaceForwardSlash);

        // calculate the SHA-1 hashcode for this mojo based on input
        String buildHashCode = generateHashForBuild();

        File goalFileDirectory = new File("target" + File.separator + "completed-mojos");
        File goalFile = new File(goalFileDirectory, buildHashCode);

        // check to see if this goal has been executed previously
        if(!goalFile.exists()) {
            // hasn't been executed previously
            try {
                File sqlSrcDir = new File(sourceDirectory.getParentFile(), "sql");
                File sqlTargetDir = new File(outputDirectory, "sql");
                sqlTargetDir.mkdirs();
                File dbDir = new File(outputDirectory, dbName.replace('/', File.separatorChar));

                copySQLFilesToTarget(copier, sqlSrcDir, sqlTargetDir);
                File dbErrLog = createErrorLog(dbDir);
                runScripts(sqlTargetDir, dbDir, dbErrLog);                
                // create a new file to indicate this execution has completed
                writeHashFile(goalFileDirectory, goalFile);
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            } 
        } else {
            // skip execution as it has already been done previously
            getLog().warn("Skipping goal - executed previously.");
        }
    }

    private void runScripts(final File sqlTargetDir, final File dbDir, final File dbErrLog) throws IOException {
        DerbyClient derbyClient = new DerbyClientImpl(dbDir.getCanonicalPath(), dbErrLog.getCanonicalPath(),
                getLog());
        derbyClient.openConnection();
        runScripts(derbyClient, sqlTargetDir);
        derbyClient.closeConnection();
    }

    private void copySQLFilesToTarget(final SQLFileTransformationCopier copier, final File sqlSrcDir,
                                      final File sqlTargetDir) {
        if (sqlLocations.length == 0) {
            copier.copySQLFilesToTarget(sqlSrcDir, sqlTargetDir);
        }
    }

    private void runScripts(final DerbyClient derbyClient, final File sqlTargetDir) throws IOException {
        File[] sqlSources = findSources(sqlTargetDir);
        for (File file : sqlSources) {
            getLog().info("Executing: " + file.getName());
            derbyClient.executeScript(file.getCanonicalPath(), verbose);
        }
    }

    private void writeHashFile(final File goalFileDirectory, final File goalFile) throws IOException {
        goalFileDirectory.mkdirs();
        goalFile.createNewFile();
    }

    private File[] findSources(final File sqlTargetDir) {
        return new SQLSourceFinderImpl().find(sqlTargetDir, sources, sqlLocations);
    }

    private File createErrorLog(final File dbDir) throws IOException {
        return new LogFileCreatorImpl().createLog(dbDir.getParentFile(), "derbyErr.log", version);
    }

    private String generateHashForBuild() {
        return new DerbyHashBuilder(getLog()).
                withOutputDirectory(outputDirectory).
                withSourceDirectory(sourceDirectory).
                withVersion(version).
                withDatabaseName(dbName).
                withSourceRoots(sourceRoots).
                withSources(sources).
                withSQLLocations(sqlLocations).
                build();        
    }
}
