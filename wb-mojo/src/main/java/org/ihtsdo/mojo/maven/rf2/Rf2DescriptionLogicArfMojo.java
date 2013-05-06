/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.mojo.maven.rf2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.rf2.constant.I_Constants;

/**
 *
 * @author marc 
 * @goal generate-dl-refset-arf
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class Rf2DescriptionLogicArfMojo extends AbstractMojo implements Serializable {

    private static final String FILE_SEPARATOR = File.separator;
    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;
    /**
     * Applicable input sub directory under the build directory.
     *
     * @parameter
     */
    private String targetSubDir = "";
    /**
     * Directory used to output the eConcept format files
     *
     * @parameter
     * @required
     */
    private String outputDir;
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("::: BEGIN GenerateClassifierDLRefsetArfMojo");
        // SHOW DIRECTORIES
        String wDir = targetDirectory.getAbsolutePath();
        getLog().info("    POM Target Directory: " + targetDirectory.getAbsolutePath());
        getLog().info("    POM Target Sub Directory: " + targetSubDir);

        // "8c230474-9f11-30ce-9cad-185a96fd03a2"
        String snomedCorePathStr = I_Constants.SNOMED_CORE_PATH_UID;
        // "2faa9260-8fb2-11db-b606-0800200c9a66" PrimordialId.ACE_AUXILIARY_ID.toString()
        String workbenchAuxiliaryPathStr = "2faa9260-8fb2-11db-b606-0800200c9a66";

        try {
            // FILE & DIRECTORY SETUP
            // Create multiple directories
            String outDir = wDir + FILE_SEPARATOR + targetSubDir + FILE_SEPARATOR
                    + outputDir + FILE_SEPARATOR;
            boolean success = (new File(outDir)).mkdirs();
            if (success) {
                getLog().info("::: Output Directory: " + outDir);
            }

            // WRITE DESCRIPTION LOGIC METADATA AND REFSET CONCEPTS
            ArrayList<Rf2_RefsetId> refsetIdList = new ArrayList<>();

            // DESCRIPTION LOGIC CHARACTERISTIC TYPE
            refsetIdList.add(new Rf2_RefsetId(Long.MAX_VALUE, /* refsetSctIdOriginal N/A */
                    "2002.01.31", /* refsetDate */
                    workbenchAuxiliaryPathStr, /* Worbench Auxiliary :!!!: */
                    "logic", /* refsetPrefTerm */
                    "description logic (characteristic type)", /* refsetFsName */
                    "f88e2a66-3a5b-3358-92f0-5b3f5e82b270")); /* characteristic type */

            // Description Logic Refset
            // "c7942fa6-98a6-50a7-a4fd-24e5574c2a5d"
            Rf2_RefsetId dlogicParent = new Rf2_RefsetId(Long.MAX_VALUE, /* refsetSctIdOriginal N/A */
                    "2002.01.31", /* refsetDate */
                    workbenchAuxiliaryPathStr, /* refsetPathUuidStr */
                    "Description logic refset", /* refsetPrefTerm */
                    "Description logic refset", /* refsetFsName */
                    "3e0cd740-2cc6-3d68-ace7-bad2eb2621da"); // "refset'
            refsetIdList.add(dlogicParent); // refsetParentUuid
            String dlogicParentUuidStr = dlogicParent.getRefsetUuidStr();

            // Disjoint Sets
            // "a18af1aa-c78e-537e-97eb-582f749fc9ce"
            refsetIdList.add(new Rf2_RefsetId(Long.MAX_VALUE, /* refsetSctIdOriginal N/A */
                    "2002.01.31", /* refsetDate */
                    workbenchAuxiliaryPathStr, /* refsetPathUuidStr */
                    "Disjoint sets refset", /* refsetPrefTerm */
                    "Disjoint sets refset", /* refsetFsName */
                    dlogicParentUuidStr)); /* refsetParentUuid */

            // Negation
            // "fb85778f-6f96-58a5-8975-7c455728ac18"
            refsetIdList.add(new Rf2_RefsetId(Long.MAX_VALUE, /* refsetSctIdOriginal N/A */
                    "2002.01.31", /* refsetDate */
                    workbenchAuxiliaryPathStr, /* refsetPathUuidStr */
                    "Negation refset", /* refsetPrefTerm */
                    "Negation refset", /* refsetFsName */
                    dlogicParentUuidStr)); /* refsetParentUuid */

            // Union Sets Refset
            // "1fc9c4e6-07bb-5f41-a1b0-1c080d43f0e2"
            refsetIdList.add(new Rf2_RefsetId(Long.MAX_VALUE, /* refsetSctIdOriginal N/A */
                    "2002.01.31", /* refsetDate */
                    workbenchAuxiliaryPathStr, /* refsetPathUuidStr */
                    "Union sets refset", /* refsetPrefTerm */
                    "Union sets refset", /* refsetFsName */
                    dlogicParentUuidStr)); /* refsetParentUuid == Attribute*/

            // Description logic concept (description logic concept)
            // "264a2234-357e-5eb9-bbb5-9ea78cb2182a"
//            refsetIdList.add(new Rf2_RefsetId(Long.MAX_VALUE, /* refsetSctIdOriginal N/A */
//                    "2002.01.31", /* refsetDate */
//                    snomedCorePathStr, /* refsetPathUuidStr */
//                    "Description logic concept", /* refsetPrefTerm */
//                    "Description logic concept (description logic concept)", /* refsetFsName */
//                    "65cc653d-94b0-32f1-b2bf-4166d980e7cc")); /* parent == Special Concept*/

            // Union set concept (description logic concept)
            // "06b50261-daa9-5ce2-9680-41495705693a"
//            refsetIdList.add(new Rf2_RefsetId(Long.MAX_VALUE, /* refsetSctIdOriginal N/A */
//                    "2002.01.31", /* refsetDate */
//                    snomedCorePathStr, /* refsetPathUuidStr */
//                    "Union set concept", /* refsetPrefTerm */
//                    "Union set concept (description logic concept)", /* refsetFsName */
//                    "264a2234-357e-5eb9-bbb5-9ea78cb2182a")); /* parent: Description logic concept*/

            // ConDOR reasoner
            // "03683f69-d9d3-533e-9e97-9e49aa872d85"
            refsetIdList.add(new Rf2_RefsetId(Long.MAX_VALUE, /* refsetSctIdOriginal N/A */
                    "2002.01.31", /* refsetDate */
                    workbenchAuxiliaryPathStr, /* refsetPathUuidStr */
                    "ConDOR", /* refsetPrefTerm */
                    "ConDOR Reasoner", /* refsetFsName */
                    "f7495b58-6630-3499-a44e-2052b5fcf06c")); /* parent == user */

            Rf2_RefsetId.saveRefsetConcept(outDir, refsetIdList);

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Rf2DescriptionLogicArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException(
                    "GenerateClassifierDLRefsetArfMojo NoSuchAlgorithmException error", ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Rf2DescriptionLogicArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException(
                    "GenerateClassifierDLRefsetArfMojo UnsupportedEncodingException error", ex);
        } catch (IOException ex) {
            Logger.getLogger(Rf2DescriptionLogicArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("GenerateClassifierDLRefsetArfMojo file error", ex);
        }
        getLog().info("::: END GenerateClassifierDLRefsetArfMojo");
    }
}
