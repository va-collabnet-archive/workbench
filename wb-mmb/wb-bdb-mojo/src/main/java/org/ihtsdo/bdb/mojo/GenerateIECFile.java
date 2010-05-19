package org.ihtsdo.bdb.mojo;

import java.io.File;

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
import org.ihtsdo.time.TimeUtil;


/**
 * Goal which generates an incremental e-concept (iec) file.
 * 
 * @goal generate-iec-file
 * 
 * @phase process-sources
 */

public class GenerateIECFile extends AbstractMojo  {

    /**
     * Parent of the new path.
     * 
     * @parameter
     * @required
     */
    private ConceptSpec[] pathsToExport;

    /**
     * Start date for inclusion in the change set.
     * 
     * @parameter
     * @required
     */
    private String startDate;
    
    /**
     * End date for inclusion in the change set.
     * 
     * @parameter
     * @required
     */
    private String endDate;

    /**
     * End date for inclusion in the change set.
     * 
     * @parameter default-value="INCREMENTAL"
     * @required
     * 
     */
    private String policy;
    
    /**
     * End date for inclusion in the change set.
     * 
     * @parameter default-value="export.iec"
     * @required
     * 
     */
    private String changeSetFile;
    
    /**
     * output directory.
     * 
     * @parameter expression="${project.build.directory}/classes"
     * @required
     */
    private File output;

    /**
     * Generated resources directory.
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
            EConceptChangeSetWriter writer = new EConceptChangeSetWriter(new File(output, changeSetFile), 
                new File(output, changeSetFile + ".tmp"), changeSetPolicy, false, false);
            ChangeSetWriterHandler.addWriter(writer);
            IntSet sapsToWrite = Bdb.getSapDb().getSpecifiedSapNids(pathIds, TimeUtil.getFileDateFormat().parse(startDate).getTime(), 
                TimeUtil.getFileDateFormat().parse(endDate).getTime());
            getLog().info("Criterion matches " + sapsToWrite.size() + " sapNids: " + sapsToWrite);
            if (sapsToWrite.size() > 0) {
                ChangeSetWriterHandler handler = new ChangeSetWriterHandler(
                    Bdb.getConceptDb().getConceptIdSet(), System.currentTimeMillis(),
                    sapsToWrite, changeSetPolicy, ChangeSetWriterThreading.MULTI_THREAD);
                handler.run();
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
