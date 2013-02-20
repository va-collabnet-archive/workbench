/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.bdb.mojo;

import java.io.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.helper.bdb.NullComponentFinder;
import org.ihtsdo.helper.bdb.UuidDupFinder;
import org.ihtsdo.helper.bdb.UuidDupReporter;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * Goal which verifies integrity of a bdb.
 * 
 * @goal verify-bdb
 * 
 * @phase test
 */
public class VerifyBdb extends AbstractMojo {

    /**
     * Berkeley directory.
     *
     * @parameter expression="${project.build.directory}/berkeley-db" @required
     */
    private File berkeleyDir;
    /**
     * Watch concepts that will be printed to log when encountered.
     *
     * @parameter
     */
    private String[] watchConceptUuids;
    ConcurrentSkipListSet<Object> watchSet = new ConcurrentSkipListSet<Object>();

    @Override
    public void execute() throws MojoExecutionException {
        executeMojo(berkeleyDir);

    }

    void executeMojo(File berkeleyDir) throws MojoExecutionException {
        long startTime = System.currentTimeMillis();
        
        if (watchConceptUuids != null) {
            for (String uuidStr : watchConceptUuids) {
                watchSet.add(UUID.fromString(uuidStr));
            }
        }
        try {
            getLog().info("****************\n  Verifying: " + berkeleyDir + "\n****************\n");

            Bdb.selectJeProperties(berkeleyDir,
                    berkeleyDir);

            Bdb.setup(berkeleyDir.getAbsolutePath());
            getLog().info("\nConcept count: " + Bdb.getConceptDb().getCount());

            getLog().info("\nTesting for dup uuids.");


            Concept.disableComponentsCRHM();
            UuidDupFinder dupFinder = new UuidDupFinder();
            Bdb.getConceptDb().iterateConceptDataInParallel(dupFinder);
            System.out.println();
            if (dupFinder.getDupUuids().isEmpty()) {
                getLog().info("No dup UUIDs found.");
            } else {
                dupFinder.writeDupFile();
                getLog().warn("\n\nDuplicate UUIDs found: " + dupFinder.getDupUuids().size() + "\n"
                        + dupFinder.getDupUuids() + "\n");
                UuidDupReporter reporter = new UuidDupReporter(dupFinder.getDupUuids());
                Bdb.getConceptDb().iterateConceptDataInParallel(reporter);
                reporter.reportDupClasses();

            }
            getLog().info("\nGetting all paths.");
            Bdb.getPathManager().resetPathMap();
            getLog().info("\nPaths: " + Bdb.getPathManager().getAll());
            
            getLog().info("\nTesting for references to null components.");

            Concept.disableComponentsCRHM();

            NullComponentFinder nullComponentFinder = new NullComponentFinder();
            Bdb.getConceptDb().iterateConceptDataInParallel(nullComponentFinder);
            System.out.println();

            if (nullComponentFinder.getNidsWithNullComponents().isEmpty()) {
                getLog().info("No Null component found.");
            } else {
                getLog().warn("\n\n Null Components found: " + nullComponentFinder.getNidsWithNullComponents().size() + "\n"
                        + nullComponentFinder.getNidsWithNullComponents() + "\n");
            }
            Concept.enableComponentsCRHM();
            getLog().info("Testing for Null Components Finished.");



            Concept.enableComponentsCRHM();
            getLog().info("Starting close.");
            Bdb.close();
            getLog().info("db closed");
            getLog().info("elapsed time: " + (System.currentTimeMillis() - startTime));
            if (!dupFinder.getDupUuids().isEmpty()) {
                throw new Exception("Duplicate UUIDs found: " + dupFinder.getDupUuids().size());
            }
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        } catch (Throwable ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }

    }

    private void validateSpec(ConceptChronicleBI toValidate, ConceptSpec spec) throws IOException {
        boolean validated = false;
        for (DescriptionChronicleBI desc : toValidate.getDescriptions()) {
            for (DescriptionVersionBI descV : desc.getVersions()) {
                if (descV.getText().equals(
                        spec.getDescription())) {
                    validated = true;
                    break;
                }
            }
            if (validated) {
                break;
            }
        }
        if (!validated) {
            throw new IOException("Unable to validate spec: " + spec);
        }
    }
}
