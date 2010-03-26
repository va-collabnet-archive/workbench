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
package org.ihtsdo.mojo.maven.sct;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type3UuidFactory;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EConceptAttributesRevision;
import org.ihtsdo.etypes.EDescription;
import org.ihtsdo.etypes.EDescriptionRevision;
import org.ihtsdo.etypes.ERelationship;
import org.ihtsdo.etypes.ERelationshipRevision;

/**
 * <b>DESCRIPTION: </b><br>
 * 
 * SctSiToEConceptMojo is a maven mojo which converts SNOMED stated and inferred
 * (Distribution Normal Form) release files to IHTSDO Workbench 
 * versioned import eConcepts format.
 * <p>
 * 
 * <code><pre>
 * relGroupList = in concept1-type-concept2 sorted order
 *              {triplet_A(concept1-type-concept2),
 *               triplet_B(concept1-type-concept2), 
 *               triplet_C(concept1-type-concept2), ...}
 *
 * relationship_id = createUUID_Type5(REL_ID_NAMESPACE_UUID_TYPE1,
 *                concept1_sctid_as_string +  
 *                type_sctid_as_string + 
 *                concept2_sctid_as_string + 
 *                relGroupList_as_long_string);
 * </pre></code><p>
 * 
 * <b>INPUTS:</b><br>
 * 
 * The POM needs to specify mutually exclusive extensions in separate
 * directories in the array <code>sctInputDirArray</code> parameter. Each
 * directory entry will be parsed to locate SNOMED formated text files in any
 * sub-directories. <br>
 * <br>
 * 
 * Each SNOMED file should contain a version date in the file name
 * <code>"sct_*yyyyMMdd.txt"</code>. If a valid date is not found in the file
 * name then the parent directory name will be checked for a date in the format
 * <code>"yyyy-MM-dd"</code>.
 * 
 * Versioning is performed for the files under the SAME
 * <code>sctInputDirArray[a]</code> directory. Records of the same primary ids
 * are compared in historical sequence to other records of the same primary ids
 * for all applicable files under directory <code>sctInputDirArray[a]</code>.
 * <p>
 * 
 * Set <code>includeCTV3ID</code> and/or <code>includeSNOMEDRTID</code> to true
 * to have the corresponding CTV3 IDs and SNOMED RT IDs to be included in
 * <code>ids.txt</code> output file. The default value is false to not include
 * the CTV3 IDs and SNOMED RT IDs.
 * <p>
 * <b>OUTPUTS:</b><br>
 * The following files are generated in {project.build.directory}/classes/ace:
 * <p>
 * <code>
 * &#160;&#160;&#160;&#160;concepts.txt, descriptions.txt, descriptions_report.txt<br>
 * &#160;&#160;&#160;&#160;ids.txt, relationships.txt, relationships_report.txt</code>
 * <p>
 * 
 * <b>REQUIRMENTS:</b><br>
 * 
 * 1. RELEASE DATE must be in either the SNOMED file name or the parent folder
 * name. The date must have the format of <code>yyyy-MM-dd</code> or
 * <code>yyyyMMdd</code>. <br>
 * 
 * 2. SNOMED EXTENSIONS must be mutually exclusive from SNOMED CORE and each
 * other; and, placed under separate <code>sctInputDirArray</code> directories.
 * <p>
 * <b>PROCESSING:</b><br>
 * Step 1. Versioning & Relationship Generated IDs.  Merge time series of releases into 
 * a versioned intermediate concept, description, and relationship files.  This step 
 * also adds an algorithmically computed relationship ids.<br>
 * Step 2. Sort. Sorts concepts, descriptions, and relationships to be in concept order.<br>
 * Step 3. Create EConcepts.  <br>
 * <p>
 * <b>NOTES:</b><br>
 * Records are NOT VERSIONED between files under DIFFERENT
 * <code>sctInputDirArray</code> directories. The versioned output from
 * <code>sctInputDirArray[a+1]</code> is appended to the versioned output from
 * <code>sctInputDirArray[a]</code>. <br>
 * 
 * @author Marc E. Campbell
 * 
 * @goal sct-si-to-econcepts
 * @requiresDependencyResolution compile
 * @requiresProject false
 */

public class SctSiToEConceptMojo extends AbstractMojo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int countEConWritten;
    private static final boolean debug = false;

    private static final String FILE_SEPARATOR = File.separator;

    /**
     * Line terminator is deliberately set to CR-LF which is DOS style
     */
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";

    private static final String NHS_UK_DRUG_EXTENSION_FILE_PATH = FILE_SEPARATOR + "net"
            + FILE_SEPARATOR + "nhs" + FILE_SEPARATOR + "uktc" + FILE_SEPARATOR + "ukde";

    private static final String NHS_UK_EXTENSION_FILE_PATH = FILE_SEPARATOR + "net"
            + FILE_SEPARATOR + "nhs" + FILE_SEPARATOR + "uktc" + FILE_SEPARATOR + "uke";

    private static final String SNOMED_FILE_PATH = FILE_SEPARATOR + "org" + FILE_SEPARATOR
            + "snomed";

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File buildDirectory;

    /**
     * Applicable input sub directory under the build directory.
     * 
     * @parameter
     */
    private String targetSubDir = "";

    /**
     * SCT Input Directories Array. The directory array parameter supported
     * extensions via separate directories in the array.
     * 
     * Files under the SAME directory entry in the array will be versioned
     * relative each other. Each input directory in the array is treated as
     * mutually exclusive to others directories in the array.
     * 
     * @parameter
     * @required
     */
    private String[] sctInputDirArray;

    /**
     * If this contains anything, only convert paths which match one of the
     * enclosed regex
     * 
     * @parameter
     */
    private String[] inputFilters;

    /**
     * @parameter default-value="false"
     */
    private boolean includeCTV3ID;

    /**
     * @parameter default-value="false"
     */
    private boolean includeSNOMEDRTID;

    /**
     * Directory used to output the econcept format files
     * Default value "/classes" set programmatically due to file separator
     * 
     * @parameter
     */
    private String outputDirectory = FILE_SEPARATOR + "classes";

    private String scratchDirectory = FILE_SEPARATOR + "tmp_steps";

    private static final String REL_ID_NAMESPACE_UUID_TYPE1 = "84fd0460-2270-11df-8a39-0800200c9a66";
    private HashMap<UuidMinimal, Long> relUuidMap; // :yyy:

    private String fNameStep1Con;
    private String fNameStep1Rel;
    private String fNameStep1Des;
    private String fNameStep2Con;
    private String fNameStep2Rel;
    private String fNameStep2Des;
    private String fNameStep3ECon;

    // UUIDs
    private static UUID uuidPathWbAux;
    private static String uuidPathWbAuxStr;
    private static UUID uuidDescPrefTerm;
    private static UUID uuidDescFullSpec;
    private static UUID uuidRelCharStated;
    private static UUID uuidRelNotRefinable;
    private static UUID uuidWbAuxIsa;

    UUID uuidStatedDescFs;
    UUID uuidStatedDescPt;
    UUID uuidStatedRel;
    UUID uuidInferredDescFs;
    UUID uuidInferredDescPt;
    UUID uuidInferredRel;

    private static UUID uuidPathSnomedCore;
    private static String uuidPathSnomedCoreStr;
    private static UUID uuidRootSnomed;
    private static String uuidRootSnomedStr;
    private static UUID uuidPathSnomedInferred;
    private static String uuidPathSnomedInferredStr;
    private static UUID uuidPathSnomedStated;
    private static String uuidPathSnomedStatedStr;

    private static UUID uuidCurrent = ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator()
            .next();

    private static final UUID uuidSourceCtv3 = ArchitectonicAuxiliary.Concept.CTV3_ID.getUids()
            .iterator().next();
    private static final UUID uuidSourceSnomedRt = ArchitectonicAuxiliary.Concept.SNOMED_RT_ID
            .getUids().iterator().next();

    private class UuidMinimal {
        @SuppressWarnings("unused")
        long uuidMostSigBits;
        @SuppressWarnings("unused")
        long uuidLeastSigBits;

        public UuidMinimal(long msw, long lsw) {
            super();
            this.uuidMostSigBits = msw;
            this.uuidLeastSigBits = lsw;
        }
    }

    private class SCTFile {
        File file;
        String revDate;
        String pathId;
        String sourceUuid;
        int xRevDate;
        int xPathId;
        int xSourceUuid;

        public SCTFile(File f, String d, String pid) {
            file = f;
            revDate = d;
            pathId = pid;
            sourceUuid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator().next()
                    .toString();
            xRevDate = lookupXRevDateIdx(revDate);
            xPathId = lookupXPathIdx(pathId);
            xSourceUuid = lookupXSourceUuidIdx(sourceUuid);
        }

        public String toString() {
            return pathId + " :: " + revDate + " :: " + file.getPath();
        }
    }

    private HashMap<String, Integer> xPathMap;
    private ArrayList<String> xPathList;
    private UUID[] xPathArray;
    private int xPathIdxCounter;

    private int lookupXPathIdx(String pathIdStr) {
        Integer tmp = xPathMap.get(pathIdStr);
        if (tmp == null) {
            xPathIdxCounter++;
            xPathMap.put(pathIdStr, Integer.valueOf(xPathIdxCounter));
            xPathList.add(pathIdStr);
            return xPathIdxCounter;
        } else
            return tmp.intValue();
    }

    private HashMap<String, Integer> xRevDateMap;
    private ArrayList<String> xRevDateList;
    private long[] xRevDateArray;
    private int xRevDateIdxCounter;

    private int lookupXRevDateIdx(String revDateStr) {
        Integer tmp = xRevDateMap.get(revDateStr);
        if (tmp == null) {
            xRevDateIdxCounter++;
            xRevDateMap.put(revDateStr, Integer.valueOf(xRevDateIdxCounter));
            xRevDateList.add(revDateStr);
            return xRevDateIdxCounter;
        } else
            return tmp.intValue();
    }

    private HashMap<String, Integer> xSourceUuidMap;
    private ArrayList<String> xSourceUuidList;
    private UUID[] xSourceUuidArray;
    private int xSourceUuidIdxCounter;

    private int lookupXSourceUuidIdx(String sourceUuidStr) {
        Integer tmp = xSourceUuidMap.get(sourceUuidStr);
        if (tmp == null) {
            xSourceUuidIdxCounter++;
            xSourceUuidMap.put(sourceUuidStr, Integer.valueOf(xSourceUuidIdxCounter));
            xSourceUuidList.add(sourceUuidStr);
            return xSourceUuidIdxCounter;
        } else
            return tmp.intValue();
    }

    private UUID[] xStatusArray;

    private UUID lookupXStatus(int j) {
        return xStatusArray[j + 2];
    }

    private UUID[] xDesTypeArray;
    private UUID[] xRelCharArray;
    private UUID[] xRelRefArray;

    private HashMap<Long, UUID> xRoleTypeUuidMap;

    private UUID lookupRoleType(long roleType) {
        Long key = Long.valueOf(roleType);
        UUID uuid = xRoleTypeUuidMap.get(key);
        if (uuid == null) {
            uuid = Type3UuidFactory.fromSNOMED(roleType);
            xRoleTypeUuidMap.put(key, uuid);
        }
        return uuid;
    }

    private void lookupConverion() throws MojoFailureException {
        // Relationship Role Types
        xRoleTypeUuidMap = new HashMap<Long, UUID>(80);

        // Status Array
        xStatusArray = new UUID[14];
        int i = 0;
        int j = -2;
        while (j < 12) {
            try {
                xStatusArray[i] = ArchitectonicAuxiliary.getStatusFromId(j).getUids().iterator()
                        .next();
            } catch (IOException e) {
                xStatusArray[i] = null;
                e.printStackTrace();
            } catch (TerminologyException e) {
                xStatusArray[i] = null;
                e.printStackTrace();
            }
            i++;
            j++;
        }

        xPathArray = new UUID[xPathList.size()];
        i = 0;
        for (String s : xPathList) {
            xPathArray[i] = UUID.fromString(s);
            i++;
        }

        xRevDateArray = new long[xRevDateList.size()];
        try {
            i = 0;
            for (String s : xRevDateList) {
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                xRevDateArray[i] = df.parse(s).getTime();
                i++;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            throw new MojoFailureException(
                    "FAILED: SctSiToEConcept -- lookupConverion(), date parse error");
        }

        // SNOMED_INT ... :FYI: soft code in SctXConRecord
        xSourceUuidArray = new UUID[xSourceUuidList.size()];
        i = 0;
        for (String s : xSourceUuidList) {
            xSourceUuidArray[i] = UUID.fromString(s);
            i++;
        }

        try {
            // DESCRIPTION TYPES
            xDesTypeArray = new UUID[4];
            for (i = 0; i < 4; i++)
                xDesTypeArray[i] = ArchitectonicAuxiliary.getSnomedDescriptionType(i).getUids()
                        .iterator().next();

            // RELATIONSHIP CHARACTERISTIC
            xRelCharArray = new UUID[4];
            for (i = 0; i < 4; i++)
                xRelCharArray[i] = ArchitectonicAuxiliary.getSnomedCharacteristicType(i).getUids()
                        .iterator().next();

            // RELATIONSHIP REFINABILITY
            xRelRefArray = new UUID[3];
            for (i = 0; i < 3; i++)
                xRelRefArray[i] = ArchitectonicAuxiliary.getSnomedRefinabilityType(i).getUids()
                        .iterator().next();

        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoFailureException("FAILED: SctSiToEConcept -- lookupConverion()");
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        // SHOW build directory from POM file
        String buildDir = buildDirectory.getAbsolutePath();
        getLog().info("POM Target Directory: " + buildDir);

        // SHOW input sub directory from POM file
        if (!targetSubDir.equals("")) {
            targetSubDir = FILE_SEPARATOR + targetSubDir;
            getLog().info("POM Input Sub Directory: " + targetSubDir);
        }

        // SHOW input directories from POM file
        for (int i = 0; i < sctInputDirArray.length; i++) {
            sctInputDirArray[i] = sctInputDirArray[i].replace('/', File.separatorChar);
            getLog().info("POM Input Directory (" + i + "): " + sctInputDirArray[i]);
            if (!sctInputDirArray[i].startsWith(FILE_SEPARATOR)) {
                sctInputDirArray[i] = FILE_SEPARATOR + sctInputDirArray[i];
            }
        }

        executeMojo(buildDir, targetSubDir, sctInputDirArray, includeCTV3ID, includeSNOMEDRTID);
        getLog().info("POM PROCESSING COMPLETE ");
    }

    void executeMojo(String wDir, String subDir, String[] inDirs, boolean ctv3idTF,
            boolean snomedrtTF) throws MojoFailureException {
        fNameStep1Con = wDir + scratchDirectory + FILE_SEPARATOR + "step1_concepts.ser";
        fNameStep1Rel = wDir + scratchDirectory + FILE_SEPARATOR + "step1_relationships.ser";
        fNameStep1Des = wDir + scratchDirectory + FILE_SEPARATOR + "step1_descriptions.ser";
        fNameStep2Con = wDir + scratchDirectory + FILE_SEPARATOR + "step2_concepts.ser";
        fNameStep2Rel = wDir + scratchDirectory + FILE_SEPARATOR + "step2_relationships.ser";
        fNameStep2Des = wDir + scratchDirectory + FILE_SEPARATOR + "step2_descriptions.ser";
        fNameStep3ECon = wDir + outputDirectory + FILE_SEPARATOR + "sctSiEConcepts.jbin";

        xPathMap = new HashMap<String, Integer>();
        xPathList = new ArrayList<String>();
        xPathIdxCounter = -1;

        xRevDateMap = new HashMap<String, Integer>();
        xRevDateList = new ArrayList<String>();
        xRevDateIdxCounter = -1;

        xSourceUuidMap = new HashMap<String, Integer>();
        xSourceUuidList = new ArrayList<String>();
        xSourceUuidIdxCounter = -1;

        setupUuids();

        // STEP 1. Convert to versioned binary objects file.  
        // Also computes algorithmic relationship uuid.
        executeMojoStep1(wDir, subDir, inDirs, ctv3idTF, snomedrtTF);
        stateSave(wDir);
        System.gc();

        // STEP 2. Sort in concept order
        // ... check size of just reading in.
        executeMojoStep2();
        System.gc();

        // STEP 3. Convert to EConcepts
        // stateRestore(wDir);
        lookupConverion();
        executeMojoStep3();
    }

    void executeMojoStep2() throws MojoFailureException {
        getLog().info("*** SctSiToEConcept BEGIN STEP #1 ***");
        long start = System.currentTimeMillis();
        try {

            // *** CONCEPTS ***
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(fNameStep1Con)));
            ArrayList<SctXConRecord> aCon = new ArrayList<SctXConRecord>();

            int count = 0;
            Object obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof SctXConRecord) {
                        aCon.add((SctXConRecord) obj);
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" concept count = " + count);
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" concept count = " + count + "\r\n");
            }
            ois.close();

            // SORT BY [CONCEPTID, DESCRIPTIONID, Path, Revision]
            Comparator<SctXConRecord> compCon = new Comparator<SctXConRecord>() {
                public int compare(SctXConRecord o1, SctXConRecord o2) {
                    int thisMore = 1;
                    int thisLess = -1;
                    // CONCEPTID
                    if (o1.id > o2.id) {
                        return thisMore;
                    } else if (o1.id < o2.id) {
                        return thisLess;
                    } else {
                        // Path
                        if (o1.xPath > o2.xPath) {
                            return thisMore;
                        } else if (o1.xPath < o2.xPath) {
                            return thisLess;
                        } else {
                            // Revision
                            if (o1.xRevision > o2.xRevision) {
                                return thisMore;
                            } else if (o1.xRevision < o2.xRevision) {
                                return thisLess;
                            } else {
                                return 0; // EQUAL
                            }
                        }
                    }
                } // compare()
            };
            Collections.sort(aCon, compCon);

            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(fNameStep2Con)));
            for (SctXConRecord r : aCon)
                oos.writeUnshared(r);
            oos.flush();
            oos.close();
            aCon = null;
            System.gc();

            // *** DESCRIPTIONS ***
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fNameStep1Des)));
            ArrayList<SctXDesRecord> aDes = new ArrayList<SctXDesRecord>();

            count = 0;
            obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof SctXDesRecord) {
                        aDes.add((SctXDesRecord) obj);
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" description count = " + count);
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" description count = " + count + "\r\n");
            }
            ois.close();

            // SORT BY [CONCEPTID, DESCRIPTIONID, Path, Revision]
            Comparator<SctXDesRecord> compDes = new Comparator<SctXDesRecord>() {
                public int compare(SctXDesRecord o1, SctXDesRecord o2) {
                    int thisMore = 1;
                    int thisLess = -1;
                    // CONCEPTID
                    if (o1.conceptId > o2.conceptId) {
                        return thisMore;
                    } else if (o1.conceptId < o2.conceptId) {
                        return thisLess;
                    } else {
                        // DESCRIPTIONID
                        if (o1.id > o2.id) {
                            return thisMore;
                        } else if (o1.id < o2.id) {
                            return thisLess;
                        } else {
                            // Path
                            if (o1.xPath > o2.xPath) {
                                return thisMore;
                            } else if (o1.xPath < o2.xPath) {
                                return thisLess;
                            } else {
                                // Revision
                                if (o1.xRevision > o2.xRevision) {
                                    return thisMore;
                                } else if (o1.xRevision < o2.xRevision) {
                                    return thisLess;
                                } else {
                                    return 0; // EQUAL
                                }
                            }
                        }
                    }
                } // compare()
            };
            Collections.sort(aDes, compDes);

            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep2Des)));
            for (SctXDesRecord r : aDes)
                oos.writeUnshared(r);
            oos.flush();
            oos.close();
            aDes = null;
            System.gc();

            // *** RELATIONSHIPS ***
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fNameStep1Rel)));
            ArrayList<SctXRelRecord> aRel = new ArrayList<SctXRelRecord>();

            count = 0;
            obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof SctXRelRecord) {
                        aRel.add((SctXRelRecord) obj);
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" relationships count = " + count);
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" relationships count = " + count + "\r\n");
            }
            ois.close();

            // SORT BY [C1-Group-RoleType-Path-RevisionVersion]
            Comparator<SctXRelRecord> compRel = new Comparator<SctXRelRecord>() {
                public int compare(SctXRelRecord o1, SctXRelRecord o2) {
                    int thisMore = 1;
                    int thisLess = -1;
                    // C1
                    if (o1.conceptOneID > o2.conceptOneID) {
                        return thisMore;
                    } else if (o1.conceptOneID < o2.conceptOneID) {
                        return thisLess;
                    } else {
                        // GROUP
                        if (o1.group > o2.group) {
                            return thisMore;
                        } else if (o1.group < o2.group) {
                            return thisLess;
                        } else {
                            // ROLE TYPE
                            if (o1.roleType > o2.roleType) {
                                return thisMore;
                            } else if (o1.roleType < o2.roleType) {
                                return thisLess;
                            } else {
                                // C2
                                if (o1.conceptTwoID > o2.conceptTwoID) {
                                    return thisMore;
                                } else if (o1.conceptTwoID < o2.conceptTwoID) {
                                    return thisLess;
                                } else {
                                    // PATH
                                    if (o1.xPath > o2.xPath) {
                                        return thisMore;
                                    } else if (o1.xPath < o2.xPath) {
                                        return thisLess;
                                    } else {
                                        // VERSION
                                        if (o1.xRevision > o2.xRevision) {
                                            return thisMore;
                                        } else if (o1.xRevision < o2.xRevision) {
                                            return thisLess;
                                        } else {
                                            return 0; // EQUAL
                                        }
                                    }
                                }
                            }
                        }
                    }
                } // compare()
            };
            Collections.sort(aRel, compRel);

            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep2Rel)));
            for (SctXRelRecord r : aRel)
                oos.writeUnshared(r);
            oos.flush();
            oos.close();
            aCon = null;
            System.gc();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new MojoFailureException("File Not Found -- Step 2");
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("IO Exception -- Step 2");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        getLog().info(
                "MASTER SORT TIME: " + ((System.currentTimeMillis() - start) / 1000) + " seconds");
        getLog().info("*** SctSiToEConcept STEP #2 COMPLETED ***");
    }

    void executeMojoStep3() throws MojoFailureException {
        getLog().info("*** SctSiToEConcept BEGIN STEP #3 ***");
        long start = System.currentTimeMillis();
        countEConWritten = 0;

        // Lists hold records for the immediate operations 
        ArrayList<SctXConRecord> conList = new ArrayList<SctXConRecord>();
        ArrayList<SctXDesRecord> desList = new ArrayList<SctXDesRecord>();
        ArrayList<SctXRelRecord> relList = new ArrayList<SctXRelRecord>();

        // Since readObject must look one record ahead,
        // the look ahead record is stored as "Next"
        SctXConRecord conNext = null;
        SctXDesRecord desNext = null;
        SctXRelRecord relNext = null;

        // Open Input and Output Streams
        ObjectInputStream oisCon = null;
        ObjectInputStream oisDes = null;
        ObjectInputStream oisRel = null;
        DataOutputStream dos = null;
        try {
            oisCon = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                    fNameStep2Con)));
            oisDes = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                    fNameStep2Des)));
            oisRel = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                    fNameStep2Rel)));
            dos = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(fNameStep3ECon)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new MojoFailureException("File Not Found -- Step 3");
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("IO Exception -- Step 3");
        }

        createSctSiEConcept(dos);
        
        int countCon = 0;
        int countDes = 0;
        int countRel = 0;
        boolean notDone = true;
        long theCon;
        long theDes = Long.MIN_VALUE;
        long theRel = Long.MIN_VALUE;
        long prevCon = Long.MIN_VALUE;
        long prevDes = Long.MIN_VALUE;
        long prevRel = Long.MIN_VALUE;
        while (notDone) {
            // Get next Concept record(s) for 1 id.
            conNext = readNextCon(oisCon, conList, conNext);
            theCon = conList.get(0).id;
            countCon++;

            while (theDes < theCon) {
                desNext = readNextDes(oisDes, desList, desNext);
                theDes = desList.get(0).conceptId;
                countDes++;
                if (theDes < theCon)
                    getLog().info("ORPHAN DESCRIPTION :: " + desList.get(0).termText);
            }

            while (theRel < theCon) {
                relNext = readNextRel(oisRel, relList, relNext);
                theRel = relList.get(0).conceptOneID;
                countRel++;
                if (theRel < theCon)
                    getLog().info(
                            "ORPHAN RELATIONSHIP :: relid=" + relList.get(0).id + " c1=="
                                    + relList.get(0).conceptOneID);
            }

            // Check for next sync
            if (theCon != theDes || theCon != theRel) {
                getLog().info("CAUTION: non-matching ids ");
                getLog().info(" ...prev... " + prevCon + " " + prevDes + " " + prevRel);
                getLog().info(" ...-is-... " + theCon + " " + theDes + " " + theRel);
                getLog().info(
                        " ..\"next\".. " + conNext.id + " " + desNext.conceptId + " "
                                + relNext.conceptOneID);
                getLog().info(" -is- description \"" + desList.get(0).termText + "\"\r\n");
            }

            if (theCon == theDes && theCon == theRel) {
                createEConcept(conList, desList, relList, dos);
            } else if (theCon == theDes && theCon != theRel) {
                createEConcept(conList, desList, null, dos);
            } else {
                throw new MojoFailureException("Case not implemented -- Step 3");
            }

            if (conNext == null && desNext == null && relNext == null)
                notDone = false;

            prevCon = theCon;
            prevDes = theDes;
            prevRel = theRel;
        }
        getLog().info(
                "RECORD COUNT = " + countCon + "(Con) " + countDes + "(Des) " + countRel + "(Rel)");

        // CLOSE FILES
        try {
            oisCon.close();
            oisDes.close();
            oisRel.close();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("IO Exception -- Step 3, closing files");
        }
        getLog().info(
                "ECONCEPT CREATION TIME: " + ((System.currentTimeMillis() - start) / 1000)
                        + " seconds");
        getLog().info("ECONCEPTS WRITTEN TO FILE = " + countEConWritten);
        getLog().info("*** SctSiToEConcept STEP #3 COMPLETED ***");
    }

    private void createEConcept(ArrayList<SctXConRecord> conList, ArrayList<SctXDesRecord> desList,
            ArrayList<SctXRelRecord> relList, DataOutputStream dos) throws MojoFailureException {
        if (conList.size() < 1)
            throw new MojoFailureException("createEConcept(), empty conList");

        Collections.sort(conList);
        SctXConRecord cRec0 = conList.get(0);
        UUID theConUUID = Type3UuidFactory.fromSNOMED(cRec0.id);
        EConcept ec = new EConcept();

        // ADD CONCEPT ATTRIBUTES        
        EConceptAttributes ca = new EConceptAttributes();
        ca.primordialUuid = theConUUID;
        ca.setDefined(cRec0.isprimitive == 0 ? true : false);
        ca.additionalIds = null; // :!!!:
        ca.setStatusUuid(lookupXStatus(cRec0.status));
        ca.setPathUuid(xPathArray[cRec0.xPath]);
        ca.setTime(xRevDateArray[cRec0.xRevision]); // long

        int max = conList.size();
        List<EConceptAttributesRevision> caRevisions = new ArrayList<EConceptAttributesRevision>();
        for (int i = 1; i < max; i++) {
            EConceptAttributesRevision rev = new EConceptAttributesRevision();
            SctXConRecord cRec = conList.get(i);
            rev.setDefined(cRec.isprimitive == 0 ? true : false);
            rev.setStatusUuid(lookupXStatus(cRec.status));
            rev.setPathUuid(xPathArray[cRec.xPath]);
            rev.setTime(xRevDateArray[cRec.xRevision]);
            caRevisions.add(rev);
            // :!!!: new records have possible new "additional" ids???
        }

        if (caRevisions.size() > 0)
            ca.revisions = caRevisions;
        else
            ca.revisions = null; // :!!!:???:
        ec.setConceptAttributes(ca);
        // ec.setDestRelUuidTypeUuids(destRelOriginUuidTypeUuids); :!!!:???:

        // ADD DESCRIPTIONS
        if (desList != null) {
            Collections.sort(desList);
            List<EDescription> eDesList = new ArrayList<EDescription>();
            long theDesId = Long.MIN_VALUE;
            EDescription des = null;
            List<EDescriptionRevision> revisions = new ArrayList<EDescriptionRevision>();
            for (SctXDesRecord dRec : desList) {
                if (dRec.id != theDesId) {
                    // CLOSE OUT OLD RELATIONSHIP
                    if (des != null) {
                        if (revisions.size() > 0) {
                            des.revisions = revisions;
                            revisions = new ArrayList<EDescriptionRevision>();                            
                        }
                        eDesList.add(des);
                    }

                    // CREATE NEW DESCRIPTION
                    des = new EDescription();
                    theDesId = dRec.id;
                    des.additionalIds = null; // :!!!:
                    des.primordialUuid = Type3UuidFactory.fromSNOMED(theDesId);
                    des.setConceptUuid(theConUUID);
                    des.setText(dRec.termText);
                    des.setInitialCaseSignificant(dRec.capStatus == 1 ? true : false);
                    des.setLang(dRec.languageCode);
                    des.setTypeUuid(xDesTypeArray[dRec.descriptionType]);
                    des.setStatusUuid(lookupXStatus(dRec.status));
                    des.setPathUuid(xPathArray[dRec.xPath]);
                    des.setTime(xRevDateArray[dRec.xRevision]);
                    des.revisions = null;
                } else {
                    EDescriptionRevision edv = new EDescriptionRevision();
                    edv.setText(dRec.termText);
                    edv.setTypeUuid(xDesTypeArray[dRec.descriptionType]);
                    edv.setInitialCaseSignificant(dRec.capStatus == 1 ? true : false);
                    edv.setLang(dRec.languageCode);
                    edv.setStatusUuid(lookupXStatus(dRec.status));
                    edv.setPathUuid(xPathArray[dRec.xPath]);
                    edv.setTime(xRevDateArray[dRec.xRevision]);
                    revisions.add(edv);
                }
            }
            if (des != null && revisions.size() > 0)
                des.revisions = revisions;
            eDesList.add(des);
            ec.setDescriptions(eDesList);
        }

        // ADD ROLES
        if (relList != null) {
            Collections.sort(relList);
            List<ERelationship> eRelList = new ArrayList<ERelationship>();
            long theRelMsb = Long.MIN_VALUE;
            long theRelLsb = Long.MIN_VALUE;
            ERelationship rel = null;
            List<ERelationshipRevision> revisions = new ArrayList<ERelationshipRevision>();
            for (SctXRelRecord rRec : relList) {
                if (rRec.uuidLeastSigBits != theRelMsb || rRec.uuidLeastSigBits != theRelLsb) {
                    // CLOSE OUT OLD RELATIONSHIP
                    if (rel != null) {
                        if (revisions.size() > 0) {
                            rel.revisions = revisions;
                            revisions = new ArrayList<ERelationshipRevision>();                            
                        }
                        eRelList.add(rel);
                    }

                    // CREATE NEW RELATIONSHIP
                    rel = new ERelationship();
                    rel.additionalIds = null; // :!!!:
                    rel.setAdditionalIdComponents(null); // :!!!:
                    theRelMsb = rRec.uuidMostSigBits;
                    theRelLsb = rRec.uuidLeastSigBits;
                    rel.setPrimordialComponentUuid(new UUID(theRelMsb, theRelLsb));
                    rel.setC1Uuid(theConUUID);
                    rel.setC2Uuid(Type3UuidFactory.fromSNOMED(rRec.conceptTwoID));
                    rel.setTypeUuid(lookupRoleType(rRec.roleType));
                    rel.setRelGroup(rRec.group);
                    rel.setCharacteristicUuid(xRelCharArray[rRec.characteristic]);
                    rel.setRefinabilityUuid(xRelRefArray[rRec.refinability]);
                    rel.setStatusUuid(lookupXStatus(rRec.status));
                    rel.setPathUuid(xPathArray[rRec.xPath]);
                    rel.setTime(xRevDateArray[rRec.xRevision]);
                    rel.revisions = null;
                } else {
                    ERelationshipRevision erv = new ERelationshipRevision();
                    erv.setTypeUuid(lookupRoleType(rRec.roleType));
                    erv.setRelGroup(rRec.group);
                    erv.setCharacteristicUuid(xRelCharArray[rRec.characteristic]);
                    erv.setRefinabilityUuid(xRelRefArray[rRec.refinability]);
                    erv.setStatusUuid(lookupXStatus(rRec.status));
                    erv.setPathUuid(xPathArray[rRec.xPath]);
                    erv.setTime(xRevDateArray[rRec.xRevision]);
                    revisions.add(erv);
                }
            }
            if (rel != null && revisions.size() > 0)
                rel.revisions = revisions;
            eRelList.add(rel);
            ec.setRelationships(eRelList);
        }

        try {
            ec.writeExternal(dos);
            countEConWritten++;
            if (countEConWritten % 50000 == 0)
                getLog().info("  ... econcepts written " + countEConWritten);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void createSctSiEConcept(DataOutputStream dos) throws MojoFailureException {
        try {
            // **************
            // *** STATED ***
            // **************
            EConcept ec = new EConcept();

            // ADD CONCEPT ATTRIBUTES        
            EConceptAttributes ca = new EConceptAttributes();
            ca.primordialUuid = uuidPathSnomedStated;
            ca.setDefined(false);
            ca.additionalIds = null;
            ca.setStatusUuid(uuidCurrent);
            ca.setPathUuid(uuidPathWbAux);
            ca.setTime(Long.MIN_VALUE); // Beginning of time
            ca.revisions = null; // no revisions
            ec.setConceptAttributes(ca);
            // ec.setDestRelUuidTypeUuids(destRelOriginUuidTypeUuids); :!!!:???:

            // CREATE & ADD DESCRIPTIONS
            List<EDescription> eDesList = new ArrayList<EDescription>();

            // add full specified term
            EDescription des = new EDescription();
            des.additionalIds = null;
            des.primordialUuid = uuidStatedDescFs;
            des.setConceptUuid(uuidPathSnomedStated);
            des.setText("SNOMED Core Stated");
            des.setInitialCaseSignificant(true);
            des.setLang("en");
            des.setTypeUuid(uuidDescFullSpec);
            des.setStatusUuid(uuidCurrent);
            des.setPathUuid(uuidPathWbAux);
            des.setTime(Long.MIN_VALUE);
            des.revisions = null;
            eDesList.add(des);

            // add preferred term
            des = new EDescription();
            des.additionalIds = null;
            des.primordialUuid = uuidStatedDescPt;
            des.setConceptUuid(uuidPathSnomedStated);
            des.setText("SNOMED Core Stated");
            des.setInitialCaseSignificant(true);
            des.setLang("en");
            des.setTypeUuid(uuidDescPrefTerm);
            des.setStatusUuid(uuidCurrent);
            des.setPathUuid(uuidPathWbAux);
            des.setTime(Long.MIN_VALUE);
            des.revisions = null;
            eDesList.add(des);

            ec.setDescriptions(eDesList);

            // CREATE & ADD RELATIONSHIPS
            List<ERelationship> eRelList = new ArrayList<ERelationship>();
            ERelationship rel = new ERelationship();
            rel = new ERelationship();
            rel.additionalIds = null;
            rel.setAdditionalIdComponents(null);

            rel.setPrimordialComponentUuid(uuidStatedRel);
            rel.setC1Uuid(uuidPathSnomedStated);
            rel.setC2Uuid(uuidPathSnomedCore);
            rel.setTypeUuid(uuidWbAuxIsa);
            rel.setRelGroup(0);
            rel.setCharacteristicUuid(uuidRelCharStated);
            rel.setRefinabilityUuid(uuidRelNotRefinable);
            rel.setStatusUuid(uuidCurrent);
            rel.setPathUuid(uuidPathWbAux);
            rel.setTime(Long.MIN_VALUE);
            rel.revisions = null;
            eRelList.add(rel);
            ec.setRelationships(eRelList);

            ec.writeExternal(dos);
            countEConWritten++;
            getLog().info("\"SNOMED Core Stated\" econcept written");

            // ****************
            // *** INFERRED ***
            // ****************
            ec = new EConcept();

            // ADD CONCEPT ATTRIBUTES        
            ca = new EConceptAttributes();
            ca.primordialUuid = uuidPathSnomedInferred;
            ca.setDefined(false);
            ca.additionalIds = null;
            ca.setStatusUuid(uuidCurrent);
            ca.setPathUuid(uuidPathWbAux);
            ca.setTime(Long.MIN_VALUE); // Beginning of time
            ca.revisions = null; // no revisions
            ec.setConceptAttributes(ca);
            // ec.setDestRelUuidTypeUuids(destRelOriginUuidTypeUuids); :!!!:???:

            // CREATE & ADD DESCRIPTIONS
            eDesList = new ArrayList<EDescription>();

            // add full specified term
            des = new EDescription();
            des.additionalIds = null;
            des.primordialUuid = uuidInferredDescFs;
            des.setConceptUuid(uuidPathSnomedInferred);
            des.setText("SNOMED Core Inferred");
            des.setInitialCaseSignificant(true);
            des.setLang("en");
            des.setTypeUuid(uuidDescFullSpec);
            des.setStatusUuid(uuidCurrent);
            des.setPathUuid(uuidPathWbAux);
            des.setTime(Long.MIN_VALUE);
            des.revisions = null;
            eDesList.add(des);

            // add preferred term
            des = new EDescription();
            des.additionalIds = null;
            des.primordialUuid = uuidInferredDescPt;
            des.setConceptUuid(uuidPathSnomedInferred);
            des.setText("SNOMED Core Inferred");
            des.setInitialCaseSignificant(true);
            des.setLang("en");
            des.setTypeUuid(uuidDescPrefTerm);
            des.setStatusUuid(uuidCurrent);
            des.setPathUuid(uuidPathWbAux);
            des.setTime(Long.MIN_VALUE);
            des.revisions = null;
            eDesList.add(des);

            ec.setDescriptions(eDesList);

            // CREATE & ADD RELATIONSHIPS
            eRelList = new ArrayList<ERelationship>();
            rel = new ERelationship();
            rel = new ERelationship();
            rel.additionalIds = null;
            rel.setAdditionalIdComponents(null);

            rel.setPrimordialComponentUuid(uuidInferredRel);
            rel.setC1Uuid(uuidPathSnomedInferred);
            rel.setC2Uuid(uuidPathSnomedCore);
            rel.setTypeUuid(uuidWbAuxIsa);
            rel.setRelGroup(0);
            rel.setCharacteristicUuid(uuidRelCharStated);
            rel.setRefinabilityUuid(uuidRelNotRefinable);
            rel.setStatusUuid(uuidCurrent);
            rel.setPathUuid(uuidPathWbAux);
            rel.setTime(Long.MIN_VALUE);
            rel.revisions = null;
            eRelList.add(rel);
            ec.setRelationships(eRelList);

            ec.writeExternal(dos);
            countEConWritten++;
            getLog().info("\"SNOMED Core Inferred\" econcept written");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private SctXConRecord readNextCon(ObjectInputStream ois, ArrayList<SctXConRecord> conList,
            SctXConRecord conNext) throws MojoFailureException {
        conList.clear();
        if (conNext != null) {
            conList.add(conNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readObject();
                if (obj instanceof SctXConRecord) {
                    conNext = (SctXConRecord) obj;
                    conList.add(conNext);
                } else
                    return null;
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                throw new MojoFailureException("IO Exception -- Step 3, reading conNext");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new MojoFailureException("ClassNotFoundException -- Step 3, reading conNext");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readObject();
                if (obj instanceof SctXConRecord) {
                    SctXConRecord rec = (SctXConRecord) obj;
                    if (rec.id == conNext.id) {
                        conList.add(rec);
                    } else {
                        conNext = rec;
                        notDone = false;
                    }
                }
            }
        } catch (EOFException ex) {
            conNext = null;
            return null; // end reached, no more records
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("IO Exception -- Step 3, reading concepts");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new MojoFailureException("ClassNotFoundException -- Step 3, reading concepts");
        }

        return conNext; // first record of next concept id
    }

    private SctXDesRecord readNextDes(ObjectInputStream ois, ArrayList<SctXDesRecord> desList,
            SctXDesRecord desNext) throws MojoFailureException {
        desList.clear();
        if (desNext != null) {
            desList.add(desNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readObject();
                if (obj instanceof SctXDesRecord) {
                    desNext = (SctXDesRecord) obj;
                    desList.add(desNext);
                } else
                    return null;
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                throw new MojoFailureException("IO Exception -- Step 3, reading desNext");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new MojoFailureException("ClassNotFoundException -- Step 3, reading desNext");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readObject();
                if (obj instanceof SctXDesRecord) {
                    SctXDesRecord rec = (SctXDesRecord) obj;
                    if (rec.conceptId == desNext.conceptId) {
                        desList.add(rec);
                    } else {
                        desNext = rec;
                        notDone = false;
                    }
                }
            }
        } catch (EOFException ex) {
            desNext = null;
            return null; // end reached, no more records
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("IO Exception -- Step 3, reading descriptions");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new MojoFailureException("ClassNotFoundException -- Step 3, reading descriptions");
        }

        return desNext; // first record of next concept id
    }

    private SctXRelRecord readNextRel(ObjectInputStream ois, ArrayList<SctXRelRecord> relList,
            SctXRelRecord relNext) throws MojoFailureException {
        relList.clear();
        if (relNext != null) {
            relList.add(relNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readObject();
                if (obj instanceof SctXRelRecord) {
                    relNext = (SctXRelRecord) obj;
                    relList.add(relNext);
                } else
                    return null;
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                throw new MojoFailureException("IO Exception -- Step 3, reading relNext");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new MojoFailureException("ClassNotFoundException -- Step 3, reading relNext");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readObject();
                if (obj instanceof SctXRelRecord) {
                    SctXRelRecord rec = (SctXRelRecord) obj;
                    if (rec.conceptOneID == relNext.conceptOneID) {
                        relList.add(rec);
                    } else {
                        relNext = rec;
                        notDone = false;
                    }
                }
            }
        } catch (EOFException ex) {
            relNext = null;
            return null; // end reached, no more records
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("IO Exception -- Step 3, reading relationships");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new MojoFailureException(
                    "ClassNotFoundException -- Step 3, reading relationships");
        }

        return relNext; // first record of next concept id
    }

    private void setupUuids() throws MojoFailureException {
        try {
            uuidPathWbAuxStr = "2faa9260-8fb2-11db-b606-0800200c9a66";
            uuidPathWbAux = UUID.fromString(uuidPathWbAuxStr);
            uuidDescPrefTerm = UUID.fromString("d8e3b37d-7c11-33ef-b1d0-8769e2264d44");
            uuidDescFullSpec = UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66");
            uuidRelCharStated = UUID.fromString("3fde38f6-e079-3cdc-a819-eda3ec74732d");
            uuidRelNotRefinable = UUID.fromString("e4cde443-8fb6-11db-b606-0800200c9a66");
            uuidWbAuxIsa = UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66");

            uuidRootSnomedStr = "ee9ac5d2-a07c-3981-a57a-f7f26baf38d8";
            uuidRootSnomed = UUID.fromString(uuidRootSnomedStr);

            uuidPathSnomedCore = ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids().iterator()
                    .next();
            uuidPathSnomedCoreStr = uuidPathSnomedCore.toString();

            uuidPathSnomedInferred = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                    "SNOMED Core Inferred");
            uuidPathSnomedInferredStr = uuidPathSnomedInferred.toString();
            getLog().info("SNOMED Core Inferred = " + uuidPathSnomedInferredStr);

            uuidPathSnomedStated = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                    "SNOMED Core Stated");
            uuidPathSnomedStatedStr = uuidPathSnomedStated.toString();

            uuidStatedDescFs = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                    uuidPathWbAux + uuidDescFullSpec.toString() + "SNOMED Core Stated");

            uuidStatedDescPt = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                    uuidWbAuxIsa + uuidDescPrefTerm.toString() + "SNOMED Core Stated");

            uuidStatedRel = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                    uuidWbAuxIsa + uuidStatedDescFs.toString() + uuidStatedDescPt.toString());

            uuidInferredDescFs = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                    uuidPathWbAux + uuidDescFullSpec.toString() + "SNOMED Core Inferred");

            uuidInferredDescPt = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                    uuidWbAuxIsa + uuidDescPrefTerm.toString() + "SNOMED Core Inferred");

            uuidInferredRel = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                    uuidWbAuxIsa + uuidInferredDescFs.toString() + uuidInferredDescPt.toString());

            getLog().info("SNOMED CT Root       = " + uuidRootSnomedStr);
            getLog().info("SNOMED Core          = " + uuidPathSnomedCore);
            getLog().info("SNOMED Core Stated   = " + uuidPathSnomedStatedStr);
            getLog().info("  ... Stated rel     = " + uuidStatedRel.toString());
            
            getLog().info("SNOMED Core Inferred = " + uuidPathSnomedInferredStr);
            getLog().info("  ... Inferred rel   = " + uuidInferredRel.toString());

        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
            throw new MojoFailureException("FAILED: SNOMED Core Stated/Inferred Path", e2);
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
            throw new MojoFailureException("FAILED: SNOMED Core Stated/Inferred Path", e2);
        }
    }

    void executeMojoStep1(String wDir, String subDir, String[] inDirs, boolean ctv3idTF,
            boolean snomedrtTF) throws MojoFailureException {
        getLog().info("*** SctSiToEConcept BEGIN STEP #1 ***");
        long start = System.currentTimeMillis();

        // Setup build directory
        getLog().info("Build Directory: " + wDir);

        // SETUP OUTPUT directory
        try {
            // Create multiple directories
            String outDir = outputDirectory;
            boolean success = (new File(wDir + outDir)).mkdirs();
            if (success) {
                getLog().info("OUTPUT DIRECTORY: " + wDir + outDir);
            }

            String tmpDir = scratchDirectory;
            success = (new File(wDir + tmpDir)).mkdirs();
            if (success) {
                getLog().info("SCRATCH DIRECTORY: " + wDir + tmpDir);
            }
        } catch (Exception e) { // Catch exception if any
            getLog().info("Error: could not create output directories");
            throw new MojoFailureException("Error: could not create output directories", e);
        }

        // PROCESS SNOMED FILES
        try {
            // SETUP CONCEPTS INPUT SCTFile ArrayList
            List<List<SCTFile>> listOfCDirs = getSnomedFiles(wDir, subDir, inDirs, "concept");
            processConceptsFiles(wDir, listOfCDirs, ctv3idTF, snomedrtTF);
            listOfCDirs = null;
            System.gc();
        } catch (Exception e1) {
            getLog().info("FAILED: processConceptsFiles()");
            e1.printStackTrace();
            throw new MojoFailureException("FAILED: processConceptsFiles()", e1);
        }

        try {
            // SETUP DESCRIPTIONS INPUT SCTFile ArrayList
            List<List<SCTFile>> listOfDDirs = getSnomedFiles(wDir, subDir, inDirs, "descriptions");
            processDescriptionsFiles(wDir, listOfDDirs);
            listOfDDirs = null;
            System.gc();
        } catch (Exception e1) {
            getLog().info("FAILED: processDescriptionsFiles()");
            e1.printStackTrace();
            throw new MojoFailureException("FAILED: processDescriptionsFiles()", e1);
        }

        // 3,254,249 from 2002.07 through 2010.01 
        relUuidMap = new HashMap<UuidMinimal, Long>(4000000); // :yyy: 
        // SETUP INFERRED RELATIONSHIPS INPUT SCTFile ArrayList
        List<List<SCTFile>> listOfRiDirs = getSnomedFiles(wDir, subDir, inDirs,
                "relationships_inferred");

        // SETUP STATED RELATIONSHIPS INPUT SCTFile ArrayList
        List<List<SCTFile>> listOfRsDirs = getSnomedFiles(wDir, subDir, inDirs,
                "relationships_stated");
        try {
            getLog().info("START RELATIONSHIPS PROCESSING...");

            // SETUP RELATIONSHIPS OUTPUT FILE
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(fNameStep1Rel)));
            getLog().info("RELATIONSHIPS Step 1 OUTPUT: " + fNameStep1Rel);

            // SETUP RELATIONSHIPS EXCEPTION REPORT FILE
            String erFileName = wDir + scratchDirectory + FILE_SEPARATOR
                    + "relationships_report.txt";
            BufferedWriter erw;
            erw = new BufferedWriter(new FileWriter(erFileName));
            getLog().info("RELATIONSHIPS Exceptions Report OUTPUT: " + erFileName);

            processRelationshipsFiles(wDir, listOfRiDirs, false, oos, erw);
            processRelationshipsFiles(wDir, listOfRsDirs, true, oos, erw);

            oos.close(); // Need to be sure to the close file!
            erw.close(); // Need to be sure to the close file!
        } catch (Exception e1) {
            getLog().info("FAILED: processRelationshipsFiles()");
            e1.printStackTrace();
            throw new MojoFailureException("FAILED: processRelationshipsFiles()", e1);
        }

        relUuidMap = null; // memory not needed any more.
        System.gc();
        getLog().info(
                "VERSIONING TIME: " + ((System.currentTimeMillis() - start) / 1000) + " seconds");
        getLog().info("*** SctSiToEConcept STEP #1 COMPLETED ***");
    }

    private List<List<SCTFile>> getSnomedFiles(String wDir, String subDir, String[] inDirs,
            String pattern) throws MojoFailureException {

        List<List<SCTFile>> listOfDirs = new ArrayList<List<SCTFile>>();
        for (int ii = 0; ii < inDirs.length; ii++) {
            ArrayList<SCTFile> listOfFiles = new ArrayList<SCTFile>();

            getLog().info(
                    String.format("%1$s (%2$s): %3$s%4$s%5$s", pattern.toUpperCase(), ii, wDir,
                            subDir, inDirs[ii]));

            File f1 = new File(new File(wDir, subDir), inDirs[ii]);
            ArrayList<File> fv = new ArrayList<File>();
            listFilesRecursive(fv, f1, "sct_" + pattern);

            File[] files = new File[0];
            files = fv.toArray(files);
            Arrays.sort(files);

            FileFilter filter = new FileFilter() {
                public boolean accept(File pathname) {
                    if (inputFilters == null || inputFilters.length == 0) {
                        return true;
                    } else {
                        for (String filter : inputFilters) {
                            if (pathname.getAbsolutePath().replace(File.separatorChar, '/')
                                    .matches(filter)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            };

            for (File f2 : files) {

                if (filter.accept(f2)) {
                    // ADD SCTFile Entry
                    String tempRevDate = getFileRevDate(f2);
                    String tmpPathID = getFilePathID(f2, wDir, subDir);
                    SCTFile tmpObj = new SCTFile(f2, tempRevDate, tmpPathID);
                    listOfFiles.add(tmpObj);
                    getLog().info("    FILE : " + f2.getName() + " " + tempRevDate);
                }

            }

            listOfDirs.add(listOfFiles);
        }
        return listOfDirs;
    }

    /*
     * ORDER: CONCEPTID CONCEPTSTATUS FULLYSPECIFIEDNAME CTV3ID SNOMEDID
     * ISPRIMITIVE
     * 
     * KEEP: CONCEPTID CONCEPTSTATUS ISPRIMITIVE
     * 
     * IGNORE: FULLYSPECIFIEDNAME CTV3ID SNOMEDID
     */
    protected void processConceptsFiles(String wDir, List<List<SCTFile>> sctv, boolean ctv3idTF,
            boolean snomedrtTF) throws Exception {
        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        int xSourceUUID, xRevDate, xPathID;
        SctXConRecord[] a1, a2, a3 = null;

        getLog().info("START CONCEPTS PROCESSING...");

        // SETUP CONCEPTS OUTPUT FILE
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                new FileOutputStream(fNameStep1Con)));
        getLog().info("Step 1 CONCEPTS OUTPUT: " + fNameStep1Con);

        Iterator<List<SCTFile>> dit = sctv.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<SCTFile> fl = dit.next(); // File List
            Iterator<SCTFile> fit = fl.iterator(); // File Iterator

            // READ file1 as MASTER FILE
            SCTFile f1 = fit.next();
            fName1 = f1.file.getPath();
            xRevDate = f1.xRevDate;
            xPathID = f1.xPathId;
            xSourceUUID = f1.xSourceUuid;

            count1 = countFileLines(fName1);
            getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
            a1 = new SctXConRecord[count1];
            parseConcepts(fName1, a1, count1, ctv3idTF, snomedrtTF);
            writeConcepts(oos, a1, count1, xRevDate, xPathID);

            // :!!!:TODO: properly write ids with associated source
            // writeConceptIds(idstxt, a1, count1, sourceUUID, revDate, pathID);

            while (fit.hasNext()) {
                // SETUP CURRENT CONCEPTS INPUT FILE
                SCTFile f2 = fit.next();
                fName2 = f2.file.getPath();
                xRevDate = f2.xRevDate;
                xPathID = f2.xPathId;
                xSourceUUID = f2.xSourceUuid;

                count2 = countFileLines(fName2);
                getLog().info("Counted: " + count2 + " records, " + fName2);

                // Parse in file2
                a2 = new SctXConRecord[count2];
                parseConcepts(fName2, a2, count2, ctv3idTF, snomedrtTF);

                int r1 = 0, r2 = 0, r3 = 0; // reset record indices
                int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
                a3 = new SctXConRecord[count2]; // max3
                while ((r1 < count1) && (r2 < count2)) {

                    switch (compareConcept(a1[r1], a2[r2])) {
                    case 1: // SAME CONCEPT, skip to next
                        r1++;
                        r2++;
                        nSame++;
                        break;

                    case 2: // MODIFIED CONCEPT
                        // Write history
                        a2[r2].xPath = xPathID;
                        a2[r2].xRevision = xRevDate;
                        oos.writeUnshared(a2[r2]);
                        // Update master via pointer assignment
                        a1[r1] = a2[r2];
                        r1++;
                        r2++;
                        nMod++;
                        break;

                    case 3: // ADDED CONCEPT
                        // Write history
                        a2[r2].xPath = xPathID;
                        a2[r2].xRevision = xRevDate;
                        oos.writeUnshared(a2[r2]);
                        // :xxx: bw.write(a2[r2].toStringArf(revDate, pathID));
                        // :xxx: idstxt.write(a2[r2].toIdsTxt(sourceUUID, revDate, pathID));
                        // Hold pointer to append to master
                        a3[r3] = a2[r2];
                        r2++;
                        r3++;
                        nAdd++;
                        break;

                    case 4: // DROPPED CONCEPT
                        // see ArchitectonicAuxiliary.getStatusFromId()
                        if (a1[r1].status != 1) { // if not RETIRED
                            a1[r1].status = 1; // set to RETIRED
                            a1[r1].xPath = xPathID;
                            a1[r1].xRevision = xRevDate;
                            oos.writeUnshared(a1[r1]);
                            // :xxx: bw.write(a1[r1].toStringArf(revDate, pathID));
                        }
                        r1++;
                        nDrop++;
                        break;

                    }
                } // WHILE (NOT END OF EITHER A1 OR A2)

                // NOT MORE TO COMPARE, HANDLE REMAINING CONCEPTS
                if (r1 < count1) {
                    getLog().info("ERROR: MISSED CONCEPT RECORDS r1 < count1");
                }

                if (r2 < count2) {
                    while (r2 < count2) { // ADD CONCEPT REMAINING INPUT
                        // Write history
                        a2[r2].xPath = xPathID;
                        a2[r2].xRevision = xRevDate;
                        oos.writeUnshared(a2[r2]);
                        // :xxx: bw.write(a2[r2].toStringArf(revDate, pathID));
                        // :xxx: idstxt.write(a2[r2].toIdsTxt(sourceUUID, revDate, pathID));
                        // Add to append array
                        a3[r3] = a2[r2];
                        nAdd++;
                        r2++;
                        r3++;
                    }
                }

                // Check counter numbers to master and input file record counts
                countCheck(count1, count2, nSame, nMod, nAdd, nDrop);

                // SETUP NEW MASTER ARRAY
                a2 = new SctXConRecord[count1 + nAdd];
                r2 = 0;
                while (r2 < count1) {
                    a2[r2] = a1[r2];
                    r2++;
                }
                r3 = 0;
                while (r3 < nAdd) {
                    a2[r2] = a3[r3];
                    r2++;
                    r3++;
                }
                count1 = count1 + nAdd;
                a1 = a2;
                Arrays.sort(a1);

            } // WHILE (EACH CONCEPTS INPUT FILE)
        } // WHILE (EACH CONCEPTS DIRECTORY) *

        oos.close(); // Need to be sure to the close file!
    }

    protected void processDescriptionsFiles(String wDir, List<List<SCTFile>> sctv) throws Exception {
        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        int xSourceUUID, xRevDate, xPathID;
        SctXDesRecord[] a1, a2, a3 = null;

        getLog().info("START DESCRIPTIONS PROCESSING...");
        // SETUP DESCRIPTIONS EXCEPTION REPORT
        String erFileName = wDir + scratchDirectory + FILE_SEPARATOR + "descriptions_report.txt";
        BufferedWriter er;
        er = new BufferedWriter(new FileWriter(erFileName));
        getLog().info("exceptions report OUTPUT: " + erFileName);

        // SETUP DESCRIPTIONS OUTPUT FILE
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                new FileOutputStream(fNameStep1Des)));
        getLog().info("Step 1 DESCRIPTIONS OUTPUT: " + fNameStep1Des);

        Iterator<List<SCTFile>> dit = sctv.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<SCTFile> fl = dit.next(); // File List
            Iterator<SCTFile> fit = fl.iterator(); // File Iterator

            // READ file1 as MASTER FILE
            SCTFile f1 = fit.next();
            fName1 = f1.file.getPath();
            xRevDate = f1.xRevDate;
            xPathID = f1.xPathId;
            xSourceUUID = f1.xSourceUuid;

            count1 = countFileLines(fName1);
            getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
            a1 = new SctXDesRecord[count1];
            parseDescriptions(fName1, a1, count1);
            writeDescriptions(oos, a1, count1, xRevDate, xPathID);

            while (fit.hasNext()) {
                // SETUP CURRENT CONCEPTS INPUT FILE
                SCTFile f2 = fit.next();
                fName2 = f2.file.getPath();
                xRevDate = f2.xRevDate;
                xPathID = f2.xPathId;
                xSourceUUID = f2.xSourceUuid;

                count2 = countFileLines(fName2);
                getLog().info("Counted: " + count2 + " records, " + fName2);

                // Parse in file2
                a2 = new SctXDesRecord[count2];
                parseDescriptions(fName2, a2, count2);

                int r1 = 0, r2 = 0, r3 = 0; // reset record indices
                int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
                a3 = new SctXDesRecord[count2];
                while ((r1 < count1) && (r2 < count2)) {

                    switch (compareDescription(a1[r1], a2[r2])) {
                    case 1: // SAME DESCRIPTION, skip to next
                        r1++;
                        r2++;
                        nSame++;
                        break;

                    case 2: // MODIFIED DESCRIPTION
                        // Write history
                        a2[r2].xPath = xPathID;
                        a2[r2].xRevision = xRevDate;
                        oos.writeUnshared(a2[r2]);
                        // :xxx: bw.write(a2[r2].toStringArf(revDate, pathID));

                        // REPORT DESCRIPTION CHANGE EXCEPTION
                        if (a1[r1].conceptId != a2[r2].conceptId) {
                            er.write("** CONCEPTID CHANGE ** WAS/IS " + LINE_TERMINATOR);
                            er.write("id" + TAB_CHARACTER + "status" + TAB_CHARACTER + ""
                                    + "conceptId" + TAB_CHARACTER + "" + "termText" + TAB_CHARACTER
                                    + "" + "capStatus" + TAB_CHARACTER + "" + "descriptionType"
                                    + TAB_CHARACTER + "" + "languageCode" + LINE_TERMINATOR);
                            er.write(a1[r1].toString());
                            er.write(a2[r2].toString());
                        }

                        // Update master via pointer assignment
                        a1[r1] = a2[r2];
                        r1++;
                        r2++;
                        nMod++;
                        break;

                    case 3: // ADDED DESCRIPTION
                        // Write history
                        a2[r2].xPath = xPathID;
                        a2[r2].xRevision = xRevDate;
                        oos.writeUnshared(a2[r2]);

                        // :xxx: bw.write(a2[r2].toStringArf(revDate, pathID));
                        // :xxx: idstxt.write(a2[r2].toIdsTxt(sourceUUID, revDate, pathID));
                        // Hold pointer to append to master
                        a3[r3] = a2[r2];
                        r2++;
                        r3++;
                        nAdd++;
                        break;

                    case 4: // DROPPED DESCRIPTION
                        // see ArchitectonicAuxiliary.getStatusFromId()
                        if (a1[r1].status != 1) { // if not RETIRED
                            a1[r1].status = 1; // set to RETIRED
                            a1[r1].xPath = xPathID;
                            a1[r1].xRevision = xRevDate;
                            oos.writeUnshared(a1[r1]);

                            // :xxx: bw.write(a1[r1].toStringArf(revDate, pathID));
                        }
                        r1++;
                        nDrop++;
                        break;

                    }
                } // WHILE (NOT END OF EITHER A1 OR A2)

                // NOT MORE TO COMPARE, HANDLE REMAINING CONCEPTS
                if (r1 < count1) {
                    getLog().info("ERROR: MISSED DESCRIPTION RECORDS r1 < count1");
                }

                if (r2 < count2) {
                    while (r2 < count2) { // ADD REMAINING DESCRIPTION INPUT
                        // Write history
                        a2[r2].xPath = xPathID;
                        a2[r2].xRevision = xRevDate;
                        oos.writeUnshared(a2[r2]);

                        // :xxx: bw.write(a2[r2].toStringArf(revDate, pathID));
                        // :xxx: idstxt.write(a2[r2].toIdsTxt(sourceUUID, revDate, pathID));
                        // Add to append array
                        a3[r3] = a2[r2];
                        nAdd++;
                        r2++;
                        r3++;
                    }
                }

                // Check counter numbers to master and input file record counts
                countCheck(count1, count2, nSame, nMod, nAdd, nDrop);

                // SETUP NEW MASTER ARRAY
                a2 = new SctXDesRecord[count1 + nAdd];
                r2 = 0;
                while (r2 < count1) {
                    a2[r2] = a1[r2];
                    r2++;
                }
                r3 = 0;
                while (r3 < nAdd) {
                    a2[r2] = a3[r3];
                    r2++;
                    r3++;
                }
                count1 = count1 + nAdd;
                a1 = a2;
                Arrays.sort(a1);

            } // WHILE (EACH DESCRIPTIONS INPUT FILE)
        } // WHILE (EACH DESCRIPTIONS DIRECTORY) *

        oos.close(); // Need to be sure to the close file!
        er.close(); // Need to be sure to the close file!
    }

    protected void processRelationshipsFiles(String wDir, List<List<SCTFile>> sctI,
            boolean isStated, ObjectOutputStream oos, BufferedWriter er) throws Exception {
        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        int xSourceUUID, xRevDate, xPathID;
        SctXRelRecord[] a1, a2, a3 = null;

        Iterator<List<SCTFile>> dit = sctI.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<SCTFile> fl = dit.next(); // File List
            Iterator<SCTFile> fit = fl.iterator(); // File Iterator

            // READ file1 as MASTER FILE
            SCTFile f1 = fit.next();
            fName1 = f1.file.getPath();
            xRevDate = f1.xRevDate;
            xPathID = f1.xPathId;
            xSourceUUID = f1.xSourceUuid;

            count1 = countFileLines(fName1);
            getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
            a1 = new SctXRelRecord[count1];
            parseRelationships(fName1, a1, count1, isStated);
            writeRelationships(oos, a1, count1, xRevDate, xPathID);

            while (fit.hasNext()) {
                // SETUP CURRENT RELATIONSHIPS INPUT FILE
                SCTFile f2 = fit.next();
                fName2 = f2.file.getPath();
                xRevDate = f2.xRevDate;
                xPathID = f2.xPathId;
                xSourceUUID = f2.xSourceUuid;

                count2 = countFileLines(fName2);
                getLog().info("Counted: " + count2 + " records, " + fName2);

                // Parse in file2
                a2 = new SctXRelRecord[count2];
                parseRelationships(fName2, a2, count2, isStated);

                int r1 = 0, r2 = 0, r3 = 0; // reset record indices
                int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
                a3 = new SctXRelRecord[count2];
                while ((r1 < count1) && (r2 < count2)) {
                    switch (compareRelationship(a1[r1], a2[r2])) {
                    case 1: // SAME RELATIONSHIP, skip to next
                        r1++;
                        r2++;
                        nSame++;
                        break;

                    case 2: // MODIFIED RELATIONSHIP

                        // REPORT & HANDLE CHANGE EXCEPTION
                        if ((a1[r1].conceptOneID != a2[r2].conceptOneID)
                                || (a1[r1].conceptTwoID != a2[r2].conceptTwoID)) {
                            er.write("** CONCEPTID CHANGE ** WAS/IS " + LINE_TERMINATOR);
                            er.write("id" + TAB_CHARACTER + "" + "status" + TAB_CHARACTER + ""
                                    + "conceptOneID" + TAB_CHARACTER + "" + "roleType"
                                    + TAB_CHARACTER + "" + "conceptTwoID" + LINE_TERMINATOR);
                            er.write(a1[r1].toString());
                            er.write(a2[r2].toString());

                            // RETIRE & WRITE MASTER RELATIONSHIP a1[r1]
                            a1[r1].status = 1; // set to RETIRED
                            a1[r1].xPath = xPathID;
                            a1[r1].xRevision = xRevDate;
                            oos.writeUnshared(a1[r1]);
                            // :xxx: bw.write(a1[r1].toStringArf(revDate, pathID));

                            // SET EXCEPTIONFLAG for subsequence writes
                            // WILL WRITE INPUT RELATIONSHIP w/ NEGATIVE
                            // SNOMEDID
                            a2[r2].exceptionFlag = true;
                        }

                        // Write history
                        a2[r2].xPath = xPathID;
                        a2[r2].xRevision = xRevDate;
                        oos.writeUnshared(a2[r2]);
                        // :xxx: bw.write(a2[r2].toStringArf(revDate, pathID));

                        // Update master via pointer assignment
                        a1[r1] = a2[r2];
                        r1++;
                        r2++;
                        nMod++;
                        break;

                    case 3: // ADDED RELATIONSHIP
                        // Write history
                        a2[r2].xPath = xPathID;
                        a2[r2].xRevision = xRevDate;
                        oos.writeUnshared(a2[r2]);

                        // :xxx: bw.write(a2[r2].toStringArf(revDate, pathID));
                        // :!!!: double check dropping idstxt.write()
                        // if (a2[r2].id < Long.MAX_VALUE)
                        //     idstxt.write(a2[r2].toIdsTxt(sourceUUID, revDate, pathID));
                        // hold pointer to append to master
                        a3[r3] = a2[r2];
                        r2++;
                        r3++;
                        nAdd++;
                        break;

                    case 4: // DROPPED RELATIONSHIP
                        // see ArchitectonicAuxiliary.getStatusFromId()
                        if (a1[r1].status != 1) { // if not RETIRED
                            a1[r1].status = 1; // set to RETIRED
                            a1[r1].xPath = xPathID;
                            a1[r1].xRevision = xRevDate;
                            oos.writeUnshared(a1[r1]);

                            // :xxx: bw.write(a1[r1].toStringArf(revDate, pathID));
                        }
                        r1++;
                        nDrop++;
                        break;

                    } // SWITCH (COMPARE RELATIONSHIP)
                } // WHILE (NOT END OF EITHER A1 OR A2)

                // NOT MORE TO COMPARE, HANDLE REMAINING RELATIONSHIPS
                if (r1 < count1) {
                    while (r1 < count1) {
                        // see ArchitectonicAuxiliary.getStatusFromId()
                        if (a1[r1].status != 1) { // if not RETIRED
                            a1[r1].status = 1; // set to RETIRED
                            a1[r1].xPath = xPathID;
                            a1[r1].xRevision = xRevDate;
                            oos.writeUnshared(a1[r1]);
                            // :xxx: bw.write(a1[r1].toStringArf(revDate, pathID));
                        }
                        r1++;
                        nDrop++;
                    }
                    getLog().info("ERROR: MISSED RELATIONSHIP RECORDS r1 < count1");
                }

                if (r2 < count2) {
                    while (r2 < count2) { // ADD REMAINING RELATIONSHIP INPUT
                        // Write history
                        a2[r2].xPath = xPathID;
                        a2[r2].xRevision = xRevDate;
                        oos.writeUnshared(a2[r2]);
                        // :xxx: bw.write(a2[r2].toStringArf(revDate, pathID));

                        // :!!!: double check dropping...
                        // if (a2[r2].id < Long.MAX_VALUE)
                        //     idstxt.write(a2[r2].toIdsTxt(sourceUUID, revDate, pathID));
                        // Add to append array
                        a3[r3] = a2[r2];
                        nAdd++;
                        r2++;
                        r3++;
                    }
                }

                // Check counter numbers to master and input file record counts
                countCheck(count1, count2, nSame, nMod, nAdd, nDrop);

                // SETUP NEW MASTER ARRAY
                a2 = new SctXRelRecord[count1 + nAdd];
                r2 = 0;
                while (r2 < count1) {
                    a2[r2] = a1[r2];
                    r2++;
                }
                r3 = 0;
                while (r3 < nAdd) {
                    a2[r2] = a3[r3];
                    r2++;
                    r3++;
                }
                count1 = count1 + nAdd;
                a1 = a2;
                Arrays.sort(a1);

            } // WHILE (EACH INPUT RELATIONSHIPS FILE)
        } // WHILE (EACH RELATIONSHIPS DIRECTORY) *

    }

    private int compareConcept(SctXConRecord c1, SctXConRecord c2) {
        if (c1.id == c2.id) {
            if ((c1.status == c2.status) && (c1.isprimitive == c2.isprimitive))
                return 1; // SAME
            else
                return 2; // MODIFIED

        } else if (c1.id > c2.id) {
            return 3; // ADDED

        } else {
            return 4; // DROPPED
        }
    }

    private int compareDescription(SctXDesRecord c1, SctXDesRecord c2) {
        if (c1.id == c2.id) {
            if ((c1.status == c2.status) && (c1.conceptId == c2.conceptId)
                    && c1.termText.equals(c2.termText) && (c1.capStatus == c2.capStatus)
                    && (c1.descriptionType == c2.descriptionType)
                    && c1.languageCode.equals(c2.languageCode))
                return 1; // SAME
            else
                return 2; // MODIFIED

        } else if (c1.id > c2.id) {
            return 3; // ADDED

        } else {
            return 4; // DROPPED
        }
    }

    private int compareRelationship(SctXRelRecord c1, SctXRelRecord c2) {
        if (c1.uuidMostSigBits == c2.uuidMostSigBits && c1.uuidLeastSigBits == c2.uuidLeastSigBits) {
            if ((c1.status == c2.status) && (c1.characteristic == c2.characteristic)
                    && (c1.refinability == c2.refinability))
                return 1; // SAME
            else
                return 2; // MODIFIED

        } else if (c1.uuidMostSigBits > c2.uuidMostSigBits) {
            return 3; // ADDED

        } else if (c1.uuidMostSigBits == c2.uuidMostSigBits
                && c1.uuidLeastSigBits > c2.uuidLeastSigBits) {
            return 3; // ADDED

        } else {
            return 4; // DROPPED
        }
    }

    protected void parseConcepts(String fName, SctXConRecord[] a, int count, boolean ctv3idTF,
            boolean snomedrtTF) throws Exception {

        String ctv3Str;
        String snomedrtStr;
        long start = System.currentTimeMillis();

        int CONCEPTID = 0;
        int CONCEPTSTATUS = 1;
        // int FULLYSPECIFIEDNAME = 2;
        int CTV3ID = 3;
        int SNOMEDID = 4; // SNOMED RT ID (Read Code)
        int ISPRIMITIVE = 5;

        BufferedReader br = new BufferedReader(new FileReader(fName));
        int concepts = 0;

        // Header row
        br.readLine();

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);
            long conceptKey = Long.parseLong(line[CONCEPTID]);
            int conceptStatus = Integer.parseInt(line[CONCEPTSTATUS]);
            if (ctv3idTF && (line[CTV3ID].length() > 2)) {
                ctv3Str = new String(line[CTV3ID]);
            } else {
                ctv3Str = null;
            }
            if (snomedrtTF && (line[SNOMEDID].length() > 2)) {
                snomedrtStr = new String(line[SNOMEDID]);
            } else {
                snomedrtStr = null;
            }

            int isPrimitive = Integer.parseInt(line[ISPRIMITIVE]);

            // Save to sortable array
            a[concepts] = new SctXConRecord(conceptKey, conceptStatus, ctv3Str, snomedrtStr,
                    isPrimitive);
            concepts++;
        }
        Arrays.sort(a);

        getLog().info(
                "Parse & sort time: " + concepts + " concepts, "
                        + (System.currentTimeMillis() - start) + " milliseconds");
    }

    protected void parseDescriptions(String fName, SctXDesRecord[] a, int count) throws Exception {

        long start = System.currentTimeMillis();

        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fName),
                "UTF-8"));
        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('\u001F', '\u00FF');
        st.whitespaceChars('\t', '\t');
        st.eolIsSignificant(true);
        int descriptions = 0;

        skipLineOne(st);
        int tokenType = st.nextToken();
        while ((tokenType != StreamTokenizer.TT_EOF) && (descriptions < count)) {
            // DESCRIPTIONID
            long descriptionId = Long.parseLong(st.sval);
            // DESCRIPTIONSTATUS
            tokenType = st.nextToken();
            int status = Integer.parseInt(st.sval);
            // CONCEPTID
            tokenType = st.nextToken();
            long conceptId = Long.parseLong(st.sval);
            // TERM
            tokenType = st.nextToken();
            String text = st.sval;
            // INITIALCAPITALSTATUS
            tokenType = st.nextToken();
            int capStatus = Integer.parseInt(st.sval);
            // DESCRIPTIONTYPE
            tokenType = st.nextToken();
            int typeInt = Integer.parseInt(st.sval);
            // LANGUAGECODE
            tokenType = st.nextToken();
            String lang = st.sval;

            // Save to sortable array
            a[descriptions] = new SctXDesRecord(descriptionId, status, conceptId, text, capStatus,
                    typeInt, lang);
            descriptions++;

            // CR
            tokenType = st.nextToken();
            // LF
            tokenType = st.nextToken();
            // Beginning of loop
            tokenType = st.nextToken();
        }
        Arrays.sort(a);

        getLog().info(
                "Parse & sort time: " + descriptions + " descriptions, "
                        + (System.currentTimeMillis() - start) + " milliseconds");
    }

    protected void parseRelationships(String fName, SctXRelRecord[] a, int count, boolean isStated)
            throws Exception {

        long start = System.currentTimeMillis();

        BufferedReader r = new BufferedReader(new FileReader(fName));
        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('\u001F', '\u00FF');
        st.whitespaceChars('\t', '\t');
        st.eolIsSignificant(true);
        int relationships = 0;

        skipLineOne(st);
        int tokenType = st.nextToken();
        while ((tokenType != StreamTokenizer.TT_EOF) && (relationships < count)) {
            // RELATIONSHIPID
            long relID = Long.MAX_VALUE;
            if (isStated == false) {
                relID = Long.parseLong(st.sval);
                tokenType = st.nextToken();
            }
            // ADD STATUS VALUE: see ArchitectonicAuxiliary.getStatusFromId()
            // STATUS VALUE MUST BE ADDED BECAUSE NOT PRESENT IN SNOMED INPUT
            int status = 0; // status added as CURRENT '0' for parsed record
            // CONCEPTID1
            long conceptOneID = Long.parseLong(st.sval);
            tokenType = st.nextToken();
            // RELATIONSHIPTYPE
            long relationshipTypeConceptID = Long.parseLong(st.sval);
            tokenType = st.nextToken();
            // CONCEPTID2
            long conceptTwoID = Long.parseLong(st.sval);
            tokenType = st.nextToken();
            // CHARACTERISTICTYPE
            int characteristic = Integer.parseInt(st.sval);
            tokenType = st.nextToken();
            // REFINABILITY
            int refinability = Integer.parseInt(st.sval);
            tokenType = st.nextToken();
            // RELATIONSHIPGROUP
            int group = Integer.parseInt(st.sval);

            // Save to sortable array
            a[relationships] = new SctXRelRecord(relID, status, conceptOneID,
                    relationshipTypeConceptID, conceptTwoID, characteristic, refinability, group);
            relationships++;

            // CR
            tokenType = st.nextToken();
            // LF
            tokenType = st.nextToken();
            // Beginning of loop
            tokenType = st.nextToken();

        }

        computeRelationshipUuids(a, isStated);
        Arrays.sort(a);

        getLog().info(
                "Parse & sort time: " + relationships + " relationships, "
                        + (System.currentTimeMillis() - start) + " milliseconds");
    }

    private void computeRelationshipUuids(SctXRelRecord[] a, boolean isStated)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // SORT BY [C1-Group-RoleType-C2]
        Comparator<SctXRelRecord> comp = new Comparator<SctXRelRecord>() {
            public int compare(SctXRelRecord o1, SctXRelRecord o2) {
                int thisMore = 1;
                int thisLess = -1;
                // C1
                if (o1.conceptOneID > o2.conceptOneID) {
                    return thisMore;
                } else if (o1.conceptOneID < o2.conceptOneID) {
                    return thisLess;
                } else {
                    // GROUP
                    if (o1.group > o2.group) {
                        return thisMore;
                    } else if (o1.group < o2.group) {
                        return thisLess;
                    } else {
                        // ROLE TYPE
                        if (o1.roleType > o2.roleType) {
                            return thisMore;
                        } else if (o1.roleType < o2.roleType) {
                            return thisLess;
                        } else {
                            // C2
                            if (o1.conceptTwoID > o2.conceptTwoID) {
                                return thisMore;
                            } else if (o1.conceptTwoID < o2.conceptTwoID) {
                                return thisLess;
                            } else {
                                return 0; // EQUAL
                            }
                        }
                    }
                }
            } // compare()
        };
        Arrays.sort(a, comp);

        // 
        long lastC1 = a[0].conceptOneID;
        int lastGroup = a[0].group;
        String GroupListStr = getGroupListString(a, 0);
        int max = a.length;
        for (int i = 0; i < max; i++) {
            // DETERMINE IF NEW GroupListStr IS NEEDED
            if (lastC1 != a[i].conceptOneID || lastGroup != a[i].group)
                GroupListStr = getGroupListString(a, i);

            // SET RELATIONSHIP UUID
            UUID uuid = Type5UuidFactory.get(REL_ID_NAMESPACE_UUID_TYPE1 + a[i].conceptOneID
                    + a[i].roleType + a[i].conceptTwoID + GroupListStr);
            // :yyy:
            a[i].uuidMostSigBits = uuid.getMostSignificantBits();
            a[i].uuidLeastSigBits = uuid.getLeastSignificantBits();
            UuidMinimal uuidMinimal = new UuidMinimal(a[i].uuidMostSigBits, a[i].uuidMostSigBits);

            // UPDATE SNOMED ID
            if (isStated) {
                // get (check for existing) relationship id
                Long tmp = relUuidMap.get(uuidMinimal); // :yyy:
                if (tmp != null)
                    a[i].id = tmp.longValue();
            } else {
                relUuidMap.put(uuidMinimal, Long.valueOf(a[i].id));
                // :yyy: relUuidMap.put(a[i].uuid, Long.valueOf(a[i].id));
            }

            lastC1 = a[i].conceptOneID;
            lastGroup = a[i].group;
        }
    }

    private String getGroupListString(SctXRelRecord[] a, int startIdx) {
        StringBuilder sb = new StringBuilder();

        int max = a.length;
        if (a[startIdx].group > 0) {
            long keepC1 = a[startIdx].conceptOneID;
            int keepGroup = a[startIdx].group;
            int i = startIdx;
            while ((i < max - 1) && (a[i].conceptOneID == keepC1) && (a[i].group == keepGroup)) {
                sb.append(a[i].conceptOneID + "-" + a[i].roleType + "-" + a[i].conceptTwoID + ";");
                i++;
            }
        }
        return sb.toString();
    }

    protected void writeConcepts(ObjectOutputStream oos, SctXConRecord[] a, int count,
            int releaseDateIdx, int pathIdx) throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            a[i].xPath = pathIdx;
            a[i].xRevision = releaseDateIdx;
            oos.writeUnshared(a[i]);
        }

        getLog().info(
                "Output time: " + count + " records, " + (System.currentTimeMillis() - start)
                        + " milliseconds");
    }

    //    protected void writeConceptIds(Writer w, SctXConRecord[] a, int count, String source,
    //            String releaseDate, String path) throws Exception {
    //
    //        long start = System.currentTimeMillis();
    //
    //        for (int i = 0; i < count; i++) {
    //            w.write(a[i].toIdsTxt(source, releaseDate, path));
    //        }
    //
    //        getLog().info(
    //                "Output time: " + count + " records, " + (System.currentTimeMillis() - start)
    //                        + " milliseconds");
    //    }

    protected void writeDescriptions(ObjectOutputStream oos, SctXDesRecord[] a, int count,
            int releaseDateIdx, int pathIdx) throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            a[i].xPath = pathIdx;
            a[i].xRevision = releaseDateIdx;
            oos.writeUnshared((Object) a[i]);
        }

        getLog().info(
                "Output time: " + count + " records, " + (System.currentTimeMillis() - start)
                        + " milliseconds");
    }

    //    protected void writeDescriptionIds(Writer w, SctXDesRecord[] a, int count,
    //            String source, String releaseDate, String path) throws Exception {
    //
    //        long start = System.currentTimeMillis();
    //
    //        for (int i = 0; i < count; i++) {
    //            w.write(a[i].toIdsTxt(source, releaseDate, path));
    //        }
    //
    //        getLog().info(
    //                "Output time: " + count + " records, " + (System.currentTimeMillis() - start)
    //                        + " milliseconds");
    //    }

    protected void writeRelationships(ObjectOutputStream oos, SctXRelRecord[] a, int count,
            int releaseDateIdx, int pathIdx) throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            a[i].xPath = pathIdx;
            a[i].xRevision = releaseDateIdx;
            oos.writeUnshared(a[i]);
        }

        getLog().info(
                "Output time: " + count + " records, " + (System.currentTimeMillis() - start)
                        + " milliseconds");
    }

    //    protected void writeRelationshipIds(Writer w, SctXRelRecord[] a, int count,
    //            String source, String releaseDate, String path) throws Exception {
    //
    //        long start = System.currentTimeMillis();
    //
    //        for (int i = 0; i < count; i++) {
    //            if (a[i].id < Long.MAX_VALUE)
    //                w.write(a[i].toIdsTxt(source, releaseDate, path));
    //        }
    //
    //        getLog().info(
    //                "Output time: " + count + " records, " + (System.currentTimeMillis() - start)
    //                        + " milliseconds");
    //    }

    private void skipLineOne(StreamTokenizer st) throws IOException {
        int tokenType = st.nextToken();
        while (tokenType != StreamTokenizer.TT_EOL) {
            tokenType = st.nextToken();
        }
    }

    private void stateSave(String wDir) {
        try {
            String fNameState = wDir + scratchDirectory + FILE_SEPARATOR + "state.ser";
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(fNameState)));

            oos.writeObject(xPathMap);
            oos.writeObject(xPathList);
            oos.writeObject(Integer.valueOf(xPathIdxCounter));

            oos.writeObject(xRevDateMap);
            oos.writeObject(xRevDateList);
            oos.writeObject(Integer.valueOf(xRevDateIdxCounter));

            oos.writeObject(xSourceUuidMap);
            oos.writeObject(xSourceUuidList);
            oos.writeObject(Integer.valueOf(xSourceUuidIdxCounter));

            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unchecked")
    private void stateRestore(String wDir) {
        try {
            String fNameState = wDir + scratchDirectory + FILE_SEPARATOR + "state.ser";
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(fNameState)));

            xPathMap = (HashMap<String, Integer>) ois.readObject();
            xPathList = (ArrayList<String>) ois.readObject();
            xPathIdxCounter = (Integer) ois.readObject();

            xRevDateMap = (HashMap<String, Integer>) ois.readObject();
            xRevDateList = (ArrayList<String>) ois.readObject();
            xRevDateIdxCounter = (Integer) ois.readObject();

            xSourceUuidMap = (HashMap<String, Integer>) ois.readObject();
            xSourceUuidList = (ArrayList<String>) ois.readObject();
            xSourceUuidIdxCounter = (Integer) ois.readObject();

            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void countCheck(int count1, int count2, int same, int modified, int added, int dropped) {

        // CHECK COUNTS TO MASTER FILE1 RECORD COUNT
        if ((same + modified + dropped) == count1) {
            getLog().info(
                    "PASSED1:: SAME+MODIFIED+DROPPED = " + same + "+" + modified + "+" + dropped
                            + " = " + (same + modified + dropped) + " == " + count1);
        } else {
            getLog().info(
                    "FAILED1:: SAME+MODIFIED+DROPPED = " + same + "+" + modified + "+" + dropped
                            + " = " + (same + modified + dropped) + " != " + count1);
        }

        // CHECK COUNTS TO UPDATE FILE2 RECORD COUNT
        if ((same + modified + added) == count2) {
            getLog().info(
                    "PASSED2:: SAME+MODIFIED+ADDED   = " + same + "+" + modified + "+" + added
                            + " = " + (same + modified + added) + " == " + count2);
        } else {
            getLog().info(
                    "FAILED2:: SAME+MODIFIED+ADDED   = " + same + "+" + modified + "+" + added
                            + " = " + (same + modified + added) + " != " + count2);
        }

    }

    private static int countFileLines(String fileName) throws MojoFailureException {
        int lineCount = 0;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(fileName));
            try {
                while (br.readLine() != null) {
                    lineCount++;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new MojoFailureException("FAILED: error counting lines in " + fileName, ex);
            } finally {
                br.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new MojoFailureException("FAILED: error open BufferedReader for " + fileName, ex);
        }

        // lineCount NOTE: COUNT -1 BECAUSE FIRST LINE SKIPPED
        // lineCount NOTE: REQUIRES THAT LAST LINE IS VALID RECORD
        return lineCount - 1;
    }

    private String getFileRevDate(File f) throws MojoFailureException {
        int pos;
        // Check file name for date yyyyMMdd
        // EXAMPLE: ../net/nhs/uktc/ukde/sct_relationships_uk_drug_20090401.txt
        pos = f.getName().length() - 12; // "yyyyMMdd.txt"
        String s1 = f.getName().substring(pos, pos + 8);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(s1);
        } catch (ParseException pe) {
            s1 = null;
        }

        // Check path for date yyyy-MM-dd
        // EXAMPLE: ../org/snomed/2003-01-31
        pos = f.getParent().length() - 10; // "yyyy-MM-dd"
        String s2 = f.getParent().substring(pos);
        // normalize date format
        s2 = s2.substring(0, 4) + s2.substring(5, 7) + s2.substring(8, 10);
        try {
            dateFormat.parse(s2);
        } catch (ParseException pe) {
            s2 = null;
        }

        //
        if ((s1 != null) && (s2 != null)) {
            if (s1.equals(s2)) {
                return s1 + " 00:00:00";
            } else {
                throw new MojoFailureException("FAILED: file name date "
                        + "and directory name date do not agree. ");
            }
        } else if (s1 != null) {
            return s1 + " 00:00:00";
        } else if (s2 != null) {
            return s2 + " 00:00:00";
        } else {
            throw new MojoFailureException("FAILED: date can not be determined"
                    + " from either file name date or directory name date.");
        }
    }

    private String getFilePathID(File f, String baseDir, String subDir) throws MojoFailureException {
        String puuid = null;
        UUID u;

        String s;
        if (subDir.equals("")) {
            // :NYI: TEST NO SUBDIRECTORY CODE BRANCH
            s = f.getParent().substring(baseDir.length() - 1);
        } else {
            s = f.getParent().substring(baseDir.length() + subDir.length());
        }

        // :NYI: (Maybe) Additional checks if last directory branch is a date
        // @@@ (Maybe just use the directory branch for UUID)
        if (f.getAbsolutePath().contains(SNOMED_FILE_PATH)) {
            if (f.getAbsolutePath().contains("sct_relationships_stated_")) {
                puuid = uuidPathSnomedStatedStr;
                getLog().info("  PATH UUID: " + "SNOMED Core Stated " + puuid);
            } else if (f.getAbsolutePath().contains("sct_relationships_inferred")) {
                puuid = uuidPathSnomedInferredStr;
                getLog().info("  PATH UUID: " + "SNOMED Core Inferred " + puuid);
            } else {
                // SNOMED_CORE Path UUID
                puuid = uuidPathSnomedCoreStr;
                getLog().info("  PATH UUID: " + "SNOMED Core " + puuid);
            }
        } else if (s.startsWith(NHS_UK_EXTENSION_FILE_PATH)) {
            // "UK Extensions" Path UUID
            try {
                u = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                        "NHS UK Extension Path");
                puuid = u.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                throw new MojoFailureException("FAILED: NHS UK Extension Path.."
                        + "getFilePathID() NoSuchAlgorithmException", e);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new MojoFailureException("FAILED: NHS UK Extension Path.."
                        + "getFilePathID() UnsupportedEncodingException", e);
            }
            getLog().info("  PATH UUID (uke): " + s + " " + puuid);
        } else if (s.startsWith(NHS_UK_DRUG_EXTENSION_FILE_PATH)) {
            // "UK Drug Extensions" Path UUID
            try {
                u = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                        "NHS UK Drug Extension Path");
                puuid = u.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                throw new MojoFailureException(
                        "FAILED: NHS UK Drug Extension Path.. getFilePathID()"
                                + " NoSuchAlgorithmException", e);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new MojoFailureException(
                        "FAILED: NHS UK Drug Extension Path.. getFilePathID()"
                                + " UnsupportedEncodingException", e);
            }
            getLog().info("  PATH UUID (ukde): " + s + " " + puuid);

        } else {
            // OTHER PATH UUID: based on directory path
            try {
                u = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, s);
                puuid = u.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                throw new MojoFailureException("FAILED: getFilePathID() NoSuchAlgorithmException",
                        e);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new MojoFailureException("FAILED: getFilePathID() "
                        + "UnsupportedEncodingException", e);
            }
            getLog().info("  PATH UUID: " + s + " " + puuid);
        }

        return puuid;
    }

    /*
     * 1. build directory buildDir
     */

    private static void listFilesRecursive(ArrayList<File> list, File root, String prefix) {
        if (root.isFile()) {
            list.add(root);
            return;
        }
        File[] files = root.listFiles();
        Arrays.sort(files);
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && files[i].getName().endsWith(".txt")
                    && files[i].getName().startsWith(prefix)) {
                list.add(files[i]);
            }
            if (files[i].isDirectory()) {
                listFilesRecursive(list, files[i], prefix);
            }
        }
    }

}
