/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.mojo.mojo;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 *
 * @goal vodb-import-images
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbImportImages extends AbstractMojo {

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;
    /**
     * @parameter
     */
    private String baseImportFilePath;
    /**
     * @parameter
     */
    private ImportImage[] importImageArray;
    

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        I_TermFactory terms = Terms.get();
        getLog().info("base image import file path: " + baseImportFilePath);
        try {
            // UUID_VIEWER_IMAGE
            UUID uvi = UUID.fromString("5f5be40f-24c1-374f-bd04-4a5003e366ea");
            int viewImageNid = terms.getConcept(uvi).getNid();

            // Concept.VIEWER_IMAGE;
            for (ImportImage importImage : importImageArray) {
                String fName = targetDirectory + File.separator
                        + baseImportFilePath + File.separator
                        + importImage.importImageFile;

                UUID uuid;
                if (importImage.conceptUuid != null) {
                    uuid = UUID.fromString(importImage.conceptUuid);
                } else {
                    getLog().info("  image missing uuid : " + fName);
                    continue;
                }

                if (terms.hasId(uuid)) {
                    I_GetConceptData concept = terms.getConcept(uuid);

                    File imageFile = new File(fName);
                    if (imageFile.exists()) {
                        FileInputStream fis = new FileInputStream(imageFile);
                        int size = (int) imageFile.length();
                        byte[] image = new byte[size];
                        int read = fis.read(image, 0, image.length);
                        while (read != size) {
                            size = size - read;
                            read = fis.read(image, read, size);
                        }

                        int dotLoc = imageFile.getName().lastIndexOf('.');
                        String format = imageFile.getName().substring(dotLoc + 1);
                        
                        String descriptionString = null;
                        if (importImage.getImportImageDescription() != null) {
                            descriptionString = importImage.getImportImageDescription();
                        }

                        I_ConfigAceFrame config = terms.getActiveAceFrameConfig();
                        I_ImageVersioned imageCB = terms.newImage(UUID.randomUUID(),
                                                 concept.getNid(),
                                                 viewImageNid,
                                                 image,
                                                 descriptionString,
                                                 format,
                                                 config);

                        NidSetBI rf2Nids = new NidSet();
                        rf2Nids.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
                        rf2Nids.add(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
                        I_ConceptAttributeVersioned attributes = concept.getConAttrs();
                        if (attributes != null
                                && rf2Nids.contains(attributes.getStatusNid())) {
                            imageCB.setStatusNid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
                        }

                        terms.addUncommitted(concept);
                        getLog().info("  image (added) : " + fName);

                    } else {
                        getLog().info("  image (not found) : " + fName);
                    }
                }
            }
        } catch (IOException | TerminologyException | PropertyVetoException ex) {
            Logger.getLogger(VodbImportImages.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static class ImportImage {

        private String conceptUuid;
        private String importImageDescription;
        private String importImageFile;
        private String importImageType;

        public ImportImage() {
        }

        public String getImportImageDescription() {
            return importImageDescription;
        }

        public String getConceptUuid() {
            return conceptUuid;
        }

        public String getImportImageFile() {
            return importImageFile;
        }

        public String getImportImageType() {
            return importImageType;
        }

        public void setImportImageDescription(String imageDescription) {
            this.importImageDescription = imageDescription;
        }

        public void setConceptUuid(String conceptUuid) {
            this.conceptUuid = conceptUuid;
        }

        public void setImportImageFile(String importImageFile) {
            this.importImageFile = importImageFile;
        }

        public void setImportImageType(String importImageType) {
            this.importImageType = importImageType;
        }
    }
}
