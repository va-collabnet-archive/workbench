/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.mojo.mojo.refset.spec;

import java.io.File;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.file.TupleFileUtil;
import org.dwfa.ace.task.refset.spec.compute.ComputeRefsetFromSpecTask;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.mojo.mojo.ConceptDescriptor;

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

    /**
     * Set to true to also compute the refset
     * 
     * @parameter default-value = false
     */
    private boolean computeP;

    private I_ConfigAceFrame config;

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
            config = Terms.get().getActiveAceFrameConfig();
            if (config == null) {
                throw new MojoExecutionException(
                    "You must set up the configuration prior to this call. Please see: vodb-set-default-config and vodb-set-view-paths goals. ");
            }
            reportFile.getParentFile().mkdirs();
            TupleFileUtil tupleImporter = new TupleFileUtil();
            UUID pathUuid = null;
            if (editPathDescriptor != null) {
                pathUuid = editPathDescriptor.getVerifiedConcept().getUids().iterator().next();
            }
            getLog().info("Beginning import of refset spec :" + refsetSpecFile.getPath());

            if (pathUuid != null) {
                config.setProperty("override", true);
                config.setProperty("pathUuid", pathUuid);
                config.getEditingPathSet().add(Terms.get().getPath(pathUuid));
            } else {
                config.setProperty("override", false);
            }
            I_GetConceptData refsetSpec =
                    tupleImporter.importFile(refsetSpecFile, reportFile, config, Terms.get().newActivityPanel(false,
                        config, "Importing refset spec...", true));
            if (refsetSpec != null) {
                getLog().info("Refset is: " + refsetSpec.getInitialText() + " " + refsetSpec.getUids().get(0));
            } else {
                getLog().info("Refset is: " + refsetSpec);
            }
            getLog().info("Finished importing refset spec from " + refsetSpecFile.getPath());

            Terms.get().commit();

            if (computeP) {
                getLog().info("Computing refset spec " + refsetSpecFile.getPath());
                I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
                boolean showActivityPanel = false;
                ComputeRefsetFromSpecTask task = new ComputeRefsetFromSpecTask();
                task.computeRefset(config, refsetSpec, showActivityPanel);
                getLog().info("Finished computing refset spec " + refsetSpecFile.getPath());
                Terms.get().commit();
            }

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
