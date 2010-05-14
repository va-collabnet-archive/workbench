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
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.file.TupleFileUtil;
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

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			if (MojoUtil.alreadyRun(getLog(), this.getClass()
					.getCanonicalName()
					+ inputDir.getCanonicalPath(), this.getClass(),
					targetDirectory)) {
				return;
			}
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}

		try {
			TupleFileUtil tupleImporter = new TupleFileUtil();
			if (!inputDir.isDirectory()) {
				throw new Exception("Directory has not been configured : "
						+ inputDir.getPath());
			}
			outputDir.mkdirs();
			getLog().info("Importing refset specs from " + inputDir.getPath());
			for (File inputFile : inputDir.listFiles()) {
				if (inputFile.getName().endsWith(".txt")) {
					String reportFileName = inputFile.getName().replace(".txt",
							".log");
					if (inputFile.getName().equals(reportFileName)) {
						reportFileName = inputFile.getName() + ".log";
					}
					File outputFile = new File(outputDir, reportFileName);

					UUID uuid = null;
					if (editPathDescriptor != null) {
						uuid = editPathDescriptor.getVerifiedConcept()
								.getUids().iterator().next();
					}

					getLog().info(
							"Beginning import of refset spec :"
									+ inputFile.getPath());
					tupleImporter.importFile(inputFile, outputFile, Terms.get()
							.getActiveAceFrameConfig(), null);
					getLog().info(
							"Finished importing refset spec from "
									+ inputFile.getPath());
				}
			}

			LocalVersionedTerminology.get().commit();

		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}
}
