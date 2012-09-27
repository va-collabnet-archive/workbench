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
 * Imports all the refset specs in a specified directory.
 * 
 * @goal import-refset-spec-directory
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class ImportRefsetSpecDirectory extends AbstractMojo {

    /**
     * The input refset spec directory.
     * 
     * @parameter default-value=
     *            "${project.build.directory}/generated-resources/refsetspec/"
     */
    File inputDir;

    /**
     * The output refset report directory.
     * 
     * @parameter default-value=
     *            "${project.build.directory}/generated-resources/refsetspec/reports"
     */
    File outputDir;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    /**
     * Optional edit path (this will override refset spec file path data).
     * 
     * @parameter
     */
    private ConceptDescriptor editPathDescriptor = null;

    /**
     * Set to true to also compute the refset
     * 
     * @parameter default-value = false
     */
    private boolean computeP;

    /**
     * Set to true generate changesets
     * 
     * @parameter default-value = true
     */
    private boolean writeChangesets = true;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + inputDir.getCanonicalPath(), this
                .getClass(), targetDirectory)) {
                return;
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        try {
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

            if (!writeChangesets) {
                Terms.get().suspendChangeSetWriters();
            } else {
                Terms.get().resumeChangeSetWriters();
            }
            if (config == null) {
                throw new MojoExecutionException(
                    "You must set up the configuration prior to this call. Please see: vodb-set-default-config and vodb-set-view-paths goals. ");
            }

            TupleFileUtil tupleImporter = new TupleFileUtil();
            if (!inputDir.isDirectory()) {
                throw new Exception("Directory has not been configured : " + inputDir.getPath());
            }
            outputDir.mkdirs();
            getLog().info("Importing refset specs from " + inputDir.getPath());
            boolean checkCreationTestsEnabled = Terms.get().isCheckCreationDataEnabled();
            boolean checkCommitTestsEnabled = Terms.get().isCheckCommitDataEnabled();

            for (File inputFile : inputDir.listFiles()) {
                if (inputFile.getName().endsWith(".txt")) {
                    String reportFileName = inputFile.getName().replace(".txt", ".log");
                    if (inputFile.getName().equals(reportFileName)) {
                        reportFileName = inputFile.getName() + ".log";
                    }
                    File outputFile = new File(outputDir, reportFileName);

                    UUID pathUuid = null;
                    if (editPathDescriptor != null) {
                        pathUuid = editPathDescriptor.getVerifiedConcept().getUids().iterator().next();
                    }

                    if (pathUuid != null) {
                        config.setProperty("override", true);
                        config.setProperty("pathUuid", pathUuid);
                        config.getEditingPathSet().add(Terms.get().getPath(pathUuid));
                    } else {
                        config.setProperty("override", false);
                    }

                    getLog().info("Beginning import of refset spec :" + inputFile.getPath());
                    I_GetConceptData refsetSpec =
                            tupleImporter.importFile(inputFile, outputFile, config, Terms.get().newActivityPanel(false,
                                config, "Importing refset spec...", true));

                    if (refsetSpec != null) {
                        getLog().info("Refset is: " + refsetSpec.getInitialText() + " " + refsetSpec.getUids().get(0));
                    } else {
                        getLog().info("Refset is: " + refsetSpec);
                    }

                    getLog().info("Finished importing refset spec from " + inputFile.getPath());

                    if (computeP) {
                        Terms.get().commit();
                        getLog().info("Computing refset spec " + inputFile.getPath());
                        boolean showActivityPanel = false;
                        ComputeRefsetFromSpecTask task = new ComputeRefsetFromSpecTask();
                        task.computeRefset(config, refsetSpec, showActivityPanel);
                        getLog().info("Finished computing refset spec " + inputFile.getPath());
                        Terms.get().commit();
                    }
                }
            }

            Terms.get().commit();

            Terms.get().setCheckCommitDataEnabled(checkCommitTestsEnabled);
            Terms.get().setCheckCreationDataEnabled(checkCreationTestsEnabled);

            if (!writeChangesets) {
                Terms.get().resumeChangeSetWriters();
            }
        } catch (Exception e) {
            if (!writeChangesets) {
                Terms.get().resumeChangeSetWriters();
            }
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
