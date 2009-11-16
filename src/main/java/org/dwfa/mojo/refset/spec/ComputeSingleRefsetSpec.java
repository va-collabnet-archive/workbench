package org.dwfa.mojo.refset.spec;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.task.refset.spec.compute.ComputeRefsetFromSpecTask;
import org.dwfa.maven.MojoUtil;
import org.dwfa.mojo.ConceptDescriptor;

/**
 * Computes the membership of the specified refset spec.
 * 
 * @goal compute-single-refset-membership
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class ComputeSingleRefsetSpec extends AbstractMojo {

    /**
     * The refset spec.
     * 
     * @parameter
     * @required
     */
    private ConceptDescriptor refsetSpecDescriptor;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + refsetSpecDescriptor, this
                .getClass(), targetDirectory)) {
                return;
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        try {

            I_GetConceptData refsetSpec = refsetSpecDescriptor.getVerifiedConcept();
            RefsetSpec refsetSpecHelper = new RefsetSpec(refsetSpec);
            I_GetConceptData memberRefset = refsetSpecHelper.getMemberRefsetConcept();

            ComputeRefsetFromSpecTask task = new ComputeRefsetFromSpecTask();
            boolean showActivityPanel = false;
            task.computeRefset(LocalVersionedTerminology.get().getActiveAceFrameConfig(), memberRefset,
                showActivityPanel);

            LocalVersionedTerminology.get().commit();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}