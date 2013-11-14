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
package org.ihtsdo.mojo.maven.rf2;

import edu.emory.mathcs.backport.java.util.Collections;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.etypes.EIdentifierLong;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;

/**
 *
 * @goal add-description-sctids
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class AddDesSctIdsMojo extends AbstractMojo {

    private static final String FILE_SEPARATOR = File.separator;
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";

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
     * @required
     */
    private String[] inputFiles;
    /**
     * input EConcepts file
     *
     * @parameter
     */
    String eConceptFromFilePathName;
    /**
     * output EConcepts file
     *
     * @parameter
     */
    String eConceptToFilePathName;
    /**
     * effective time
     *
     * @parameter
     */
    String effectiveTime = "2013-01-31 00:00:00";
    long effectiveTimeL;

    // Internal Variables
    private static final String UUID_STATUS_CURRENT_RF1 = "2faa9261-8fb2-11db-b606-0800200c9a66";
    private static final UUID uuidStatusCurrentRf1 = UUID.fromString(UUID_STATUS_CURRENT_RF1);
    private static final String UUID_STATUS_ACTIVE = "d12702ee-c37f-385f-a070-61d56d4d0f1f";
    private static final UUID uuidStatusActiveRf2 = UUID.fromString(UUID_STATUS_ACTIVE);
    // SNOMED integer id -- Workbench Auxiliary
    private static final String UUID_SCTID_SOURCE = "0418a591-f75b-39ad-be2c-3ab849326da9";
    private static final UUID uuidSctIdSource = UUID.fromString(UUID_SCTID_SOURCE);
    // SNOMED CT integer identifier -- part of RF2 release
    private static final String UUID_SCTID_SOURCE_RF2 = "87360947-e603-3397-804b-efd0fcc509b9";
    private static final UUID uuidSctIdSourceRf2 = UUID.fromString(UUID_SCTID_SOURCE_RF2);
    // KpAxiomConcepts.DescriptionTypes.UUID_KP_DESCRIPTION_TYPE
    private static final UUID uuidKpd = UUID.fromString("ecfd4324-04de-5503-8274-3116f8f07217");
    // KpAxiomConcepts.DescriptionTypes.UUID_PT_FRIENDLY_DESCRIPTION_TYPE    
    private static final UUID uuidPfdn = UUID.fromString("084283a0-b7ca-5626-b604-6dd69fb5ff2d");

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("AddDesSctIdsMojo : add current active sctids ");
        // SHOW DIRECTORIES
        String wDir = targetDirectory.getAbsolutePath();
        getLog().info("  POM       Target Directory:           "
                + targetDirectory.getAbsolutePath());

        ArrayList<File> inputSctIdFileList = new ArrayList<>();
        for (String filePathName : inputFiles) {
            inputSctIdFileList.add(new File(wDir + filePathName));
        }

        try {
            ArrayList<DescRecord> descriptionList = new ArrayList<>();
            step1_readInAllSctIds(inputSctIdFileList, descriptionList);
            descriptionList = step2_keepMostCurrentActiveSctIds(descriptionList); // report exceptions
            step3_processEConceptsfile(descriptionList);
        } catch (FileNotFoundException ex) {
            throw new MojoExecutionException("AddDesSctIdsMojo exception", ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AddDesSctIdsMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AddDesSctIdsMojo.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void step1_readInAllSctIds(ArrayList<File> inputSctIdFileList,
            ArrayList<DescRecord> descriptionList)
            throws FileNotFoundException, UnsupportedEncodingException, IOException {
        for (File inputFileName : inputSctIdFileList) {
            getLog().info("... reading description sctids from: " + inputFileName);
            FileInputStream fis = new FileInputStream(inputFileName);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            int ID_DENOTATION_SCTID = 0;
            int DESCRIPTION_UUID = 1;
            int CONCEPT_UUID = 2;
            int DESCRIPTION_TEXT = 3;
            int ID_STATUS_UUID = 4;
            int ID_TIME = 5; // yyyy-MM-dd HH:mm:ss
            int ID_TIME_EFFECTIVE = 6;
            int ID_AUTHOR_UUID = 7;
            int ID_MODULE_UUID = 8;
            int ID_PATH_UUID = 9;

            getLog().info("... header:" + br.readLine());
            while (br.ready()) {
                String currentLine = br.readLine();
                String[] line = currentLine.split(TAB_CHARACTER);

                // ID_DENOTATION_SCTID = 0;
                Long idDenotationSctId = Long.parseLong(line[ID_DENOTATION_SCTID]);
                // DESCRIPTION_UUID = 1;
                UUID descriptionUuid = UUID.fromString(line[DESCRIPTION_UUID]);
                // CONCEPT_UUID = 2;
                UUID conceptUuid = UUID.fromString(line[CONCEPT_UUID]);
                // DESCRIPTION_TEXT = 3;
                // String descriptionText = line[DESCRIPTION_TEXT];
                // ID_STATUS_UUID = 4;
                UUID idStatusUuid = UUID.fromString(line[ID_STATUS_UUID]);
                if (idStatusUuid.compareTo(uuidStatusCurrentRf1) == 0) {
                    idStatusUuid = uuidStatusActiveRf2;
                }
                // ID_TIME = 5; // yyyy-MM-dd HH:mm:ss
                Long idTime = Long.parseLong(line[ID_TIME]);
                // ID_TIME_EFFECTIVE = 6;
                // String idTimeEffective = line[ID_TIME_EFFECTIVE];
                // ID_AUTHOR_UUID = 7;
                UUID idAuthorUuid = UUID.fromString(line[ID_AUTHOR_UUID]);
                // ID_MODULE_UUID = 8;
                UUID idModuleUuid = UUID.fromString(line[ID_MODULE_UUID]);
                // ID_PATH_UUID = 9;
                UUID idPathUuid = UUID.fromString(line[ID_PATH_UUID]);

                descriptionList.add(new DescRecord(idDenotationSctId,
                        descriptionUuid,
                        conceptUuid,
                        idStatusUuid,
                        idTime,
                        idAuthorUuid,
                        idModuleUuid,
                        idPathUuid));
            }
        }
    }

    private ArrayList<DescRecord> step2_keepMostCurrentActiveSctIds(ArrayList<DescRecord> descriptionList) {
        ArrayList<DescRecord> keepList = new ArrayList<>();
        int countSctIdWithActiveUse = 0;
        int countSctIdWithoutActiveUse = 0;

        Collections.sort(descriptionList);

        DescRecord mostRecentActiveRecord = null;
        // DescRecord mostRecentInactiveRecord = null;
        for (int i = 0; i < descriptionList.size(); i++) {
            DescRecord dRecCurrent = descriptionList.get(i);
            if (i < descriptionList.size() - 1) {
                if (dRecCurrent.idStatusUuid.compareTo(uuidStatusActiveRf2) == 0) { // 
                    mostRecentActiveRecord = dRecCurrent;
                } else {
                    // mostRecentInactiveRecord = dRecCurrent;
                }
                DescRecord dRecNext = descriptionList.get(i + 1);
                if (dRecCurrent.idDenotationSctId != dRecNext.idDenotationSctId) {
                    if (mostRecentActiveRecord != null) {
                        keepList.add(mostRecentActiveRecord);
                        countSctIdWithActiveUse++;
                    } else {
                        countSctIdWithoutActiveUse++;
                    }
                    mostRecentActiveRecord = null;
                    // mostRecentInactiveRecord = null;
                }
            } else { // last description
                if (dRecCurrent.idStatusUuid.compareTo(uuidStatusActiveRf2) == 0) { // 
                    mostRecentActiveRecord = dRecCurrent;
                } else {
                    // mostRecentInactiveRecord = dRecCurrent;
                }
                if (mostRecentActiveRecord != null) {
                    keepList.add(mostRecentActiveRecord);
                    countSctIdWithActiveUse++;
                } else {
                    countSctIdWithoutActiveUse++;
                }
            }
        }

        getLog().info("countSctIdWithActiveUse : " + countSctIdWithActiveUse); //
        getLog().info("countSctIdWithoutActiveUse : " + countSctIdWithoutActiveUse); //
        return keepList;
    }

    private void step3_processEConceptsfile(ArrayList<DescRecord> descriptionList) throws MojoFailureException {
        Comparator<DescRecord> comp = new Comparator<DescRecord>() {
            @Override
            public int compare(DescRecord o1, DescRecord o2) {
                return o1.descriptionUuid.compareTo(o2.descriptionUuid);
            }
        };

        Collections.sort(descriptionList, comp);
        UUID[] dUuidArray = new UUID[descriptionList.size()];
        for (int i = 0; i < descriptionList.size(); i++) {
            dUuidArray[i] = descriptionList.get(i).descriptionUuid;
        }

        try {
            FileInputStream fis = new FileInputStream(targetDirectory + FILE_SEPARATOR + eConceptFromFilePathName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);

            File fileOutput = new File(targetDirectory + FILE_SEPARATOR + eConceptToFilePathName);
            fileOutput.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(fileOutput);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            DataOutputStream dos = new DataOutputStream(bos);

            while (true) {
                TkConcept tkConcept;
                try {
                    tkConcept = new TkConcept(dis);
                } catch (EOFException e) {
                    dis.close();
                    dos.flush();
                    dos.close();
                    getLog().info("::: AddLegacyUuids: output file closed");
                    break;
                }
                addSctIdToEConcept(tkConcept, dUuidArray, descriptionList);
                tkConcept.writeExternal(dos);
            }
        } catch (IOException | ClassNotFoundException ex) {
            throw new MojoFailureException("AddDesSctIdsMojo step3_processEConceptsfile", ex);
        }

    }

    private TkConcept addSctIdToEConcept(TkConcept tkConcept, UUID[] dUuidArray,
            ArrayList<DescRecord> descriptionList) {

        // Descriptions
        List<TkDescription> eDescriptions = tkConcept.getDescriptions();
        if (descriptionList != null) {
            for (TkDescription tkd : eDescriptions) {
                if (tkd.typeUuid.compareTo(uuidKpd) == 0
                        || tkd.typeUuid.compareTo(uuidPfdn) == 0) {
                    // find descriptionUuid
                    int dRecIdx = Arrays.binarySearch(dUuidArray, tkd.primordialUuid);
                    if (dRecIdx >= 0) {
                        DescRecord dRec = descriptionList.get(dRecIdx);
                        if (tkd.additionalIds == null) {
                            tkd.additionalIds = new ArrayList<>();
                        }
                        EIdentifierLong tmpEIdentifierLong = new EIdentifierLong();
                        tmpEIdentifierLong.denotation = dRec.idDenotationSctId;
                        tmpEIdentifierLong.authorityUuid = uuidSctIdSource;
                        tmpEIdentifierLong.statusUuid = dRec.idStatusUuid;
                        tmpEIdentifierLong.time = dRec.idTime;
                        tmpEIdentifierLong.authorUuid = dRec.idAuthorUuid;
                        tmpEIdentifierLong.moduleUuid = dRec.idModuleUuid;
                        tmpEIdentifierLong.pathUuid = dRec.idPathUuid;
                        tkd.additionalIds.add(tmpEIdentifierLong);
                    }
                }
            }
        }

        return tkConcept;
    }

    class DescRecord implements Comparable<DescRecord> {

        Long idDenotationSctId;
        UUID descriptionUuid;
        UUID conceptUuid;
        UUID idStatusUuid;
        Long idTime;
        UUID idAuthorUuid;
        UUID idModuleUuid;
        UUID idPathUuid;

        public DescRecord(Long denotationSctId, UUID descriptionUuid,
                UUID conceptUuid, UUID statusUuid,
                Long time, UUID authorUuid,
                UUID moduleUuid, UUID pathUuid) {
            this.idDenotationSctId = denotationSctId;
            this.descriptionUuid = descriptionUuid;
            this.conceptUuid = conceptUuid;
            this.idStatusUuid = statusUuid;
            this.idTime = time;
            this.idAuthorUuid = authorUuid;
            this.idModuleUuid = moduleUuid;
            this.idPathUuid = pathUuid;
        }

        @Override
        public int compareTo(DescRecord o) {
            if (this.idDenotationSctId < o.idDenotationSctId) {
                return -1; // instance less than received
            } else if (this.idDenotationSctId > o.idDenotationSctId) {
                return 1; // instance greater than received
            } else {
                if (this.idTime < o.idTime) {
                    return -1; // instance less than received
                } else if (this.idTime > o.idTime) {
                    return 1; // instance greater than received
                }
            }
            return 0; // instance == received
        }

    }

}
