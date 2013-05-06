/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.ihtsdo.mojo.mojo;

//~--- non-JDK imports --------------------------------------------------------
import java.io.File;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.helper.bdb.NullComponentFinder;
import org.ihtsdo.helper.bdb.UuidDupFinder;
import org.ihtsdo.helper.bdb.UuidDupReporter;
import org.ihtsdo.mojo.maven.MojoUtil;
//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @goal vodb-close
 *
 * @phase process-resources @requiresDependencyResolution compile
 */
public class VodbClose extends AbstractMojo {

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}" @required
     */
    private File targetDirectory;
    
    /**
     * @parameter 
     * default-value=false
     */
    private boolean testForNullComponents;
    
    /**
     * @parameter 
     * default-value=false
     */
    private boolean testForDupUuids;

    //~--- methods -------------------------------------------------------------
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            if (testForNullComponents) {
                getLog().info("Testing for Null Components Started.");

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
            }
            if (testForDupUuids) {
                Concept.disableComponentsCRHM();
                getLog().info("Testing for dup UUIDs.");
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

                Concept.enableComponentsCRHM();
            }

            I_ImplementTermFactory termFactoryImpl = (I_ImplementTermFactory) Terms.get();

            try {
                if (MojoUtil.alreadyRun(getLog(), "VodbClose", this.getClass(), targetDirectory)) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }

            termFactoryImpl.close();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    //~--- get methods ---------------------------------------------------------
    public File getTargetDirectory() {
        return targetDirectory;
    }

    //~--- set methods ---------------------------------------------------------
    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }
}
