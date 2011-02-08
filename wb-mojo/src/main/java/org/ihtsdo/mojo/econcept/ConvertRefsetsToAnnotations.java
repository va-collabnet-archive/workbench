/*
 * Copyright 2010 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.mojo.econcept;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;

/**
 * Goal which converts specified Refsets in an econcept.jbin file into 
 * annotations.
 * 
 * @goal convert-refsets-to-annotations
 * 
 * @phase process-sources
 */
public class ConvertRefsetsToAnnotations extends AbstractMojo {

    /**
     * Watch concepts
     * 
     * @parameter
     */
    private List<ConceptDescriptor> conceptsToWatch;
    /**
     * Refsets to convert
     * 
     * @parameter
     * @required
     */
    private List<ConceptDescriptor> refsetsToConvert;
    /**
     * concepts file names.
     * 
     * @parameter default-value={"eConcepts.jbin"}
     * @required
     */
    private String[] conceptsFileNames;
    /**
     * Generated resources directory.
     * 
     * @parameter expression="${project.build.directory}/generated-resources"
     * @required
     */
    private String inputDir;
    /**
     * Generated resources directory.
     * 
     * @parameter expression="${project.build.directory}/generated-resources"
     * @required
     */
    private String outputDir;
    private AtomicInteger conceptsRead = new AtomicInteger();

    @Override
    public void execute() throws MojoExecutionException {
        executeMojo(conceptsFileNames, inputDir);
    }

    void executeMojo(String[] conceptsFileNames, String inputDir)
            throws MojoExecutionException {
        try {
            long startTime = System.currentTimeMillis();

            HashMap<UUID, ConceptDescriptor> refsetsToConvertMap =
                    new HashMap<UUID, ConceptDescriptor>();

            for (ConceptDescriptor cd : refsetsToConvert) {
                refsetsToConvertMap.put(UUID.fromString(cd.getUuid()), cd);
            }

            HashMap<UUID, ConceptDescriptor> conceptsToWatchMap =
                    new HashMap<UUID, ConceptDescriptor>();

            if (conceptsToWatch != null) {
                for (ConceptDescriptor cd : conceptsToWatch) {
                    conceptsToWatchMap.put(UUID.fromString(cd.getUuid()), cd);
                }
            }


            for (String fname : conceptsFileNames) {
                File conceptsInFile = new File(inputDir, fname);
                getLog().info("Starting load from: " + conceptsInFile.getAbsolutePath());

                FileInputStream fis = new FileInputStream(conceptsInFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream in = new DataInputStream(bis);

                File annotationsOutFile = new File(outputDir, fname + ".rsta");
                annotationsOutFile.getParentFile().mkdirs();
                FileOutputStream aofs = new FileOutputStream(annotationsOutFile);
                BufferedOutputStream aobs = new BufferedOutputStream(aofs);
                DataOutputStream aodos = new DataOutputStream(aobs);

                File conceptsOutFile = new File(outputDir, fname);
                FileOutputStream cofs = new FileOutputStream(conceptsOutFile);
                BufferedOutputStream cobs = new BufferedOutputStream(cofs);
                DataOutputStream codos = new DataOutputStream(cobs);

                try {
                    System.out.print(conceptsRead + "-");
                    while (true) {
                        boolean foundWatchConcept = false;
                        String watchConceptStr = "";
                        EConcept eConcept = new EConcept(in);
                        if (conceptsToWatchMap.containsKey(eConcept.primordialUuid)) {
                            ConceptDescriptor cd = conceptsToWatchMap.get(eConcept.primordialUuid);
                            foundWatchConcept = validateConceptDescriptor(eConcept, cd);
                            if (!foundWatchConcept) {
                                throw new MojoExecutionException("No desc for concept: "
                                        + eConcept + "\n\nConcept descriptor: \n"
                                        + cd);
                            }
                            watchConceptStr = eConcept.toString();
                            getLog().info("Found watch concept before: " + watchConceptStr);
                        }
                        if (refsetsToConvertMap.containsKey(eConcept.primordialUuid)) {
                            ConceptDescriptor cd = refsetsToConvertMap.get(eConcept.primordialUuid);
                            boolean found = validateConceptDescriptor(eConcept, cd);
                            if (found) {
                                if (eConcept.getRefsetMembers() != null) {
                                    System.out.println("Member Count: "
                                            + eConcept.getRefsetMembers().size());
                                    eConcept.writeExternal(aodos);
                                    eConcept.getRefsetMembers().clear();
                                } else {
                                    System.out.println("Null Refset Members. ");
                                }

                            } else {
                                throw new MojoExecutionException("No desc for concept: "
                                        + eConcept + "\n\nConcept descriptor: \n"
                                        + cd);
                            }
                            eConcept.setAnnotationStyleRefex(true);
                        }
                        eConcept.writeExternal(codos);
                        if (foundWatchConcept) {
                            if (watchConceptStr.equals(eConcept.toString())) {
                                getLog().info("Found watch concept after unchanged.");
                            } else {
                                getLog().info("Found watch concept after CHANGED: " + eConcept.toString());
                            }
                        }
                        int read = conceptsRead.incrementAndGet();
                        if (read % 1000 == 0) {
                            if (read % 80000 == 0) {
                                System.out.println('.');
                                System.out.print(read + "-");
                            } else {
                                System.out.print('.');
                            }
                        }
                    }
                } catch (EOFException e) {
                    in.close();
                }
                aodos.close();
                codos.close();
                System.out.println('.');
                getLog().info("Processed concept count: " + conceptsRead);
            }

            getLog().info("elapsed time: " + (System.currentTimeMillis() - startTime));

        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        } catch (Throwable ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }

    }

    private boolean validateConceptDescriptor(EConcept eConcept,
            ConceptDescriptor cd) throws IOException {
        boolean found = false;
        for (TkDescription desc : eConcept.descriptions) {
            if (cd.getDescription().equals(desc.getText())) {
                found = true;
                System.out.println("\nFound: " + cd);
                break;
            }
        }
        return found;
    }
}
