package org.ihtsdo.bdb.mojo;

import java.io.File;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.tapi.spec.ConceptSpec;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.cs.econcept.EConceptChangeSetWriter;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.helper.time.TimeHelper;

/**
 * Goal which generates an incremental e-concept file, in a change set 
 * format (.eccs), or in incremental e-concept format (.iec).
 * 
 * @goal generate-iec-file
 * 
 * @phase process-sources
 */

public class GenerateIECFile extends AbstractMojo  {

    /**
     * ConceptSpec for the view paths to base the export on.
     * 
     * @parameter
     * @required
     */
    private ConceptSpec[] pathsToExport;

    /**
     * Start date for inclusion in the change set, in
     * yyyy-MM-dd-HH.mm.ss format.
     * 
     * @parameter
     * @required
     */
    private String startDate;
    
    /**
     * End date for inclusion in the change set, in
     * yyyy-MM-dd-HH.mm.ss format.
     * 
     * @parameter
     * @required
     */
    private String endDate;

    /**
     * Policy for generation of the change set.
     * 
     * @parameter default-value="INCREMENTAL"
     * @required
     * 
     */
    private String policy;
    
    /**
     * File name for the resulting generated change set.
     * 
     * @parameter default-value="exportFile"
     * @required
     * 
     */
    private String changeSetFile;
    
    /**
     * Format of the generated file.
     * Allowed values are: <code>eccs</code> (e-concept change set) and <code>iec</code> (incremental e-concept).
     * <p>
     * The format will be appended to the end of the provided file name. In the default case, the resulting
     * file name will be: <code>exportFile.eccs</code>
     * 
     * @parameter default-value="eccs"
     * @required
     * 
     */
    private String format;
    
    /**
     * output directory.
     * 
     * @parameter expression="${project.build.directory}/classes"
     * @required
     */
    private File output;

    /**
     * Directory of the berkeley database to export from.
     * 
     * @parameter expression="${project.build.directory}/generated-resources/berkeley-db"
     * @required
     */
    private File berkeleyDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Bdb.setup(berkeleyDir.getAbsolutePath());
            IntSet pathIds = new IntSet();
            if (pathsToExport != null) {
                for (ConceptSpec spec: pathsToExport) {
                    pathIds.add(spec.localize().getNid());
                }
            }
            ChangeSetPolicy changeSetPolicy = ChangeSetPolicy.valueOf(policy);
            if (!changeSetFile.endsWith(format)) {
                changeSetFile = changeSetFile + "." + format;
            }
            boolean timeStampEnabled = format.toLowerCase().equals("eccs");
            EConceptChangeSetWriter writer = new EConceptChangeSetWriter(new File(output, changeSetFile), 
                new File(output, changeSetFile + ".tmp"), changeSetPolicy.convert(), timeStampEnabled);
            String key = UUID.randomUUID().toString();
            ChangeSetWriterHandler.addWriter(key, writer);
            IntSet sapsToWrite = Bdb.getSapDb().getSpecifiedSapNids(pathIds, TimeHelper.getFileDateFormat().parse(startDate).getTime(), 
                TimeHelper.getFileDateFormat().parse(endDate).getTime());
            getLog().info("Criterion matches " + sapsToWrite.size() + " sapNids: " + sapsToWrite);
            if (sapsToWrite.size() > 0) {
                ChangeSetWriterHandler handler = new ChangeSetWriterHandler(
                    Bdb.getConceptDb().getConceptNidSet(), System.currentTimeMillis(),
                    sapsToWrite, changeSetPolicy.convert(), 
                    ChangeSetWriterThreading.MULTI_THREAD, null);
                handler.run();
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
