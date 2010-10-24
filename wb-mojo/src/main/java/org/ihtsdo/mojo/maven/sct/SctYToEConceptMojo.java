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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.io.UnsupportedEncodingException;
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
import org.dwfa.util.id.Type3UuidFactory;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EConceptAttributesRevision;
import org.ihtsdo.etypes.EDescription;
import org.ihtsdo.etypes.EDescriptionRevision;
import org.ihtsdo.etypes.EIdentifierLong;
import org.ihtsdo.etypes.EIdentifierString;
import org.ihtsdo.etypes.ERefsetBooleanMember;
import org.ihtsdo.etypes.ERefsetCidMember;
import org.ihtsdo.etypes.ERefsetIntMember;
import org.ihtsdo.etypes.ERefsetStrMember;
import org.ihtsdo.etypes.ERelationship;
import org.ihtsdo.etypes.ERelationshipRevision;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

/**
 * <b>DESCRIPTION: </b><br>
 * 
 * SctSiToEConceptMojo is a maven mojo which converts SNOMED stated and inferred
 * (Distribution Normal Form) RF1 release files to IHTSDO Workbench 
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
 * <b>OUTPUTS:</b> EConcept jbin file. (default name: sctSiEConcept.jbin) <br>
 * <p>
 * <b>REQUIRMENTS:</b><br>
 * 
 * 1. RELEASE DATE must be in either the SNOMED file name or the parent folder
 * name. The date must have the format of <code>yyyy-MM-dd</code> or
 * <code>yyyyMMdd</code>. <br>
 * <br>
 * 2. SNOMED EXTENSIONS must be mutually exclusive from SNOMED CORE and each
 * other; and, placed under separate <code>sctInputDirArray</code> directories.<br>
 * <br>
 * 3. STATED & INFERRED. Stated relationship files names must begin with "sct_relationships_stated". 
 * Inferred relationship file names must begin with "sct_relationships_inferred".  
 * Relationship file names without "_stated" or "_inferred" are not supported.
 * <p>
 * <b>PROCESSING:</b><br>
 * Step 1. Versioning & Relationship Generated IDs.  Merge time series of releases into 
 * a versioned intermediate concept, description, and relationship files.  This step 
 * also adds an algorithmically computed relationship ids.  Ids are kept directly with each primary 
 * (concept, description & relationship) component. <br>
 * <br>
 * Step 2. ARF files. Append arf files to sct binary records files.<br>
 * <br>
 * Step 3. Destination Rels.  Build file for destination rels. Non-required fields are dropped.<br>
 * <br>
 * Step 4. Sort. Sort concept, description, source relationship and destination relationship files
 *  to be in concept order.<br>
 *  <br>
 *  Step 5. Inflate IDs.  Attached IDs to components.
 *  <br>
 * Step #7. Create EConcepts.  Concurrently read pre-sorted concept, description, source relationship
 *  and destination relationship files and creates eConcepts.<br>
 * <p>
 * <b>NOTES:</b><br>
 * Records are NOT VERSIONED between files under DIFFERENT
 * <code>sctInputDirArray</code> directories. The versioned output from
 * <code>sctInputDirArray[a+1]</code> is appended to the versioned output from
 * <code>sctInputDirArray[a]</code>. <br>
 * 
 * @author Marc E. Campbell
 * 
 * @goal sct-y-to-econcepts
 * @requiresDependencyResolution compile
 * @requiresProject false
 */

public class SctYToEConceptMojo extends AbstractMojo implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final boolean debug = true;
    private int countEConWritten;
    private int statCon;
    private int statDes;
    private int statRel;
    private int statRelDest;
    private int statRsByCon;
    private int statRsByRs;
    private int countRefsetMember;
    private int countRefsetMaster;
    private int statRsBoolFromArf;
    private int statRsIntFromArf;
    private int statRsConFromArf;
    private int statRsStrFromArf;

    private static final int IS_LESS = -1;
    private static final int IS_EQUAL = 0;
    private static final int IS_GREATER = 1;
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
     * ARF Input Directories Array. The directory array parameter supported
     * extensions via separate directories in the array.
     * 
     * @parameter
     * @required
     */
    private String[] arfInputDirArray;

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
     * @parameter default-value="classes"
     */
    private String outputDirectory;

    private String scratchDirectory = FILE_SEPARATOR + "tmp_steps";

    private static final String REL_ID_NAMESPACE_UUID_TYPE1 = "84fd0460-2270-11df-8a39-0800200c9a66";
    private HashMap<UuidMinimal, Long> relUuidMap; // :yyy:

    private String fNameStep1Con;
    private String fNameStep1Desc;
    private String fNameStep1Rel;
    private String fNameStep1Ids;

    private String fNameStep2Refset;

    private String fNameStep3RelDest;

    private String fNameStep4Con;
    private String fNameStep4Desc;
    private String fNameStep4Rel;

    private String fNameStep6Con;
    private String fNameStep6Desc;
    private String fNameStep6Rel;
    private String fNameStep6RelDest;
    private String fNameStep5RsByCon; //
    private String fNameStep5RsByRs; //

    private String fNameStep7ECon;

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

    private static UUID uuidSourceCtv3;
    private static UUID uuidSourceSnomedRt;
    private static UUID uuidSourceSnomedInteger;

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

    private class ARFFile {
        File file;

        public ARFFile(File f) {
            this.file = f;
        }

        public String toString() {
            return " :: " + file.getPath();
        }
    }

    private class SCTFile {
        File file;
        String revDate;
        String pathUuidStr;
        String sourceUuid;
        Boolean hasSnomedId;
        Boolean doCrossMap; // Cross map inferred id to stated.
        int yRevDate;
        int yPathIdx;
        int ySourceUuid;

        public SCTFile(File f, String d, String pid, Boolean hasSnomedId, Boolean doCrossMap) {
            this.file = f;
            this.revDate = d;
            this.pathUuidStr = pid;
            this.sourceUuid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator()
                    .next().toString();
            this.hasSnomedId = hasSnomedId;
            this.doCrossMap = doCrossMap;
            this.yRevDate = lookupYRevDateIdx(revDate);
            this.yPathIdx = lookupYPathIdx(pathUuidStr);
            this.ySourceUuid = lookupYSourceUuidIdx(sourceUuid);
        }

        public String toString() {
            return pathUuidStr + " :: " + revDate + " :: " + file.getPath();
        }
    }

    private HashMap<String, Integer> yPathMap;
    private ArrayList<String> yPathList;
    private UUID[] yPathArray;
    private int yPathIdxCounter;

    private int lookupYPathIdx(String pathIdStr) {
        Integer tmp = yPathMap.get(pathIdStr);
        if (tmp == null) {
            yPathIdxCounter++;
            yPathMap.put(pathIdStr, Integer.valueOf(yPathIdxCounter));
            yPathList.add(pathIdStr);
            return yPathIdxCounter;
        } else
            return tmp.intValue();
    }

    private HashMap<String, Integer> yRevDateMap;
    private ArrayList<String> yRevDateList;
    private long[] yRevDateArray;
    private int yRevDateIdxCounter;

    private int lookupYRevDateIdx(String revDateStr) {
        Integer tmp = yRevDateMap.get(revDateStr);
        if (tmp == null) {
            yRevDateIdxCounter++;
            yRevDateMap.put(revDateStr, Integer.valueOf(yRevDateIdxCounter));
            yRevDateList.add(revDateStr);
            return yRevDateIdxCounter;
        } else
            return tmp.intValue();
    }

    // SOURCE UUID LOOKUP
    private HashMap<String, Integer> ySourceUuidMap;
    private ArrayList<String> ySourceUuidList;
    private UUID[] ySourceUuidArray;
    private int ySourceUuidIdxCounter;

    private int lookupYSourceUuidIdx(String sourceUuidStr) {
        Integer tmp = ySourceUuidMap.get(sourceUuidStr);
        if (tmp == null) {
            ySourceUuidIdxCounter++;
            ySourceUuidMap.put(sourceUuidStr, Integer.valueOf(ySourceUuidIdxCounter));
            ySourceUuidList.add(sourceUuidStr);
            return ySourceUuidIdxCounter;
        } else
            return tmp.intValue();
    }

    // STATUS TYPE LOOKUP
    private HashMap<String, Integer> yStatusUuidMap;
    private ArrayList<String> yStatusUuidList;
    private UUID[] yStatusUuidArray;
    private int yStatusUuidIdxCounter;

    private int lookupYStatusUuidIdx(String statusUuidStr) {
        Integer tmp = yStatusUuidMap.get(statusUuidStr);
        if (tmp == null) {
            yStatusUuidIdxCounter++;
            yStatusUuidMap.put(statusUuidStr, Integer.valueOf(yStatusUuidIdxCounter));
            yStatusUuidList.add(statusUuidStr);
            return yStatusUuidIdxCounter;
        } else
            return tmp.intValue();
    }

    //    // STATUS LOOKUP
    //    private UUID[] yStatusArray;
    //    private String[] yStatusStrArray;
    //
    //    private UUID lookupYStatus(int j) {
    //        return yStatusArray[j + 2];
    //    }
    //
    //    private int lookupYStatusIdx(String status) {
    //        int idx = 0;
    //        while (idx < 12) {
    //            if (status.equalsIgnoreCase(yStatusStrArray[idx]))
    //                break;
    //            idx++;
    //        }
    //        return idx - 2;
    //    }

    // DESCRIPTION TYPE LOOKUP
    private HashMap<String, Integer> yDesTypeUuidMap;
    private ArrayList<String> yDesTypeUuidList;
    private UUID[] yDesTypeUuidArray;
    private int yDesTypeUuidIdxCounter;

    private int lookupYDesTypeUuidIdx(String desTypeUuidStr) {
        Integer tmp = yDesTypeUuidMap.get(desTypeUuidStr);
        if (tmp == null) {
            yDesTypeUuidIdxCounter++;
            yDesTypeUuidMap.put(desTypeUuidStr, Integer.valueOf(yDesTypeUuidIdxCounter));
            yDesTypeUuidList.add(desTypeUuidStr);
            return yDesTypeUuidIdxCounter;
        } else
            return tmp.intValue();
    }

    //    // DESCRIPTION TYPE LOOKUP
    //    private UUID[] yDesTypeArray;
    //    private String[] yDesTypeStrArray;
    //
    //    private int lookupDesTypeIdx(String uuid) {
    //        int idx = 0;
    //        while (idx < 4) {
    //            if (uuid.equalsIgnoreCase(yDesTypeStrArray[idx]))
    //                break;
    //            idx++;
    //        }
    //        return idx;
    //    }

    // RELATIONSHIP CHARACTERISTIC LOOKUP
    private UUID[] yRelCharArray;
    private String[] yRelCharStrArray;

    private int lookupRelCharTypeIdx(String uuid) {
        int idx = 0;
        while (idx < 4) {
            if (uuid.equalsIgnoreCase(yRelCharStrArray[idx]))
                break;
            idx++;
        }
        return idx;
    }

    // RELATIONSHIP REFINIBILITY LOOKUP
    private UUID[] yRelRefArray;
    private String[] yRelRefStrArray;

    private int lookupRelRefTypeIdx(String uuid) {
        int idx = 0;
        while (idx < 3) {
            if (uuid.equalsIgnoreCase(yRelRefStrArray[idx]))
                break;
            idx++;
        }
        return idx;
    }

    // RELATIONSHIP ROLE TYPE LOOKUP
    private class RoleTypeEntry {
        long snomedId;
        String uuidStr;
        UUID uuid;

        public RoleTypeEntry(String uStr) {
            super();
            this.snomedId = Integer.MAX_VALUE;
            this.uuidStr = uStr;
            this.uuid = UUID.fromString(uStr);
        }

        public RoleTypeEntry(long snomedId) {
            super();
            this.snomedId = snomedId;
            this.uuid = Type3UuidFactory.fromSNOMED(snomedId);
            this.uuidStr = uuid.toString();
        }
    }

    private List<RoleTypeEntry> yRoleTypeList;

    private int lookupRoleTypeIdxFromSnoId(long roleTypeSnoId) {
        int last = yRoleTypeList.size();
        for (int idx = 0; idx < last; idx++)
            if (yRoleTypeList.get(idx).snomedId == roleTypeSnoId)
                return idx;

        RoleTypeEntry tmp = new RoleTypeEntry(roleTypeSnoId);
        yRoleTypeList.add(tmp);
        return last;
    }

    private int lookupRoleTypeIdx(String uStr) {
        int last = yRoleTypeList.size();
        for (int idx = 0; idx < last; idx++)
            if (yRoleTypeList.get(idx).uuidStr.equalsIgnoreCase(uStr))
                return idx;

        RoleTypeEntry tmp = new RoleTypeEntry(uStr);
        yRoleTypeList.add(tmp);
        return last;
    }

    // 
    private UUID lookupRoleType(int roleTypeIdx) {
        return yRoleTypeList.get(roleTypeIdx).uuid;
    }

    // ID SYSTEM SOURCE LOOKUP
    enum IdDataType {
        STRING, LONG
    };

    private class IdSrcSystemEntry {
        UUID srcSystemUuid;
        String srcSystemIdStr;
        IdDataType type;

        public IdSrcSystemEntry(UUID srcSysUuid, IdDataType type) {
            super();
            this.srcSystemUuid = srcSysUuid;
            this.srcSystemIdStr = srcSystemUuid.toString();
            this.type = type;
        }

        public IdSrcSystemEntry(String srcSysIdStr, IdDataType type) {
            super();
            this.srcSystemIdStr = srcSysIdStr;
            this.srcSystemUuid = UUID.fromString(srcSystemIdStr);
            this.type = type;
        }

    }

    private List<IdSrcSystemEntry> yIdSrcSystemList;

    private int lookupSrcSystemIdx(String uuidStr) {
        int last = yIdSrcSystemList.size();
        for (int idx = 0; idx < last; idx++)
            if (uuidStr.equalsIgnoreCase(yIdSrcSystemList.get(idx).srcSystemIdStr))
                return idx;

        // NOT FOUND IN LIST
        yIdSrcSystemList.add(new IdSrcSystemEntry(uuidStr, IdDataType.STRING));
        getLog().info(" ::: IMPORT DISCOVERED NOT-DECLARED ID SYSTEM = " + uuidStr);
        return last;
    }

    private UUID lookupSrcSystemUUID(int idx) {
        if (idx < yIdSrcSystemList.size())
            return yIdSrcSystemList.get(idx).srcSystemUuid;
        else
            return null;
    }

    private void setupLookupPartA() throws MojoFailureException {
        // Relationship Role Types
        yRoleTypeList = new ArrayList<RoleTypeEntry>();

        // Status Array
        //        yStatusArray = new UUID[14];
        //        int i = 0;
        //        int j = -2;
        //        while (j < 12) {
        //            try {
        //                yStatusArray[i] = ArchitectonicAuxiliary.getStatusFromId(j).getUids().iterator()
        //                        .next();
        //            } catch (IOException e) {
        //                yStatusArray[i] = null;
        //                e.printStackTrace();
        //            } catch (TerminologyException e) {
        //                yStatusArray[i] = null;
        //                e.printStackTrace();
        //            }
        //            i++;
        //            j++;
        //        }

        // Status String Array
        //        yStatusStrArray = new String[14];
        //        for (int idx = 0; idx < 14; idx++)
        //            yStatusStrArray[idx] = yStatusArray[idx].toString();

        try {
            // Status Array
            for (int idx = 0; idx < 12; idx++)
                lookupYStatusUuidIdx(ArchitectonicAuxiliary.getStatusFromId(idx).getUids()
                        .iterator().next().toString());

            // DESCRIPTION TYPES
            // Setup the standard description types used in SNOMED
            for (int i = 0; i < 4; i++)
                lookupYDesTypeUuidIdx(ArchitectonicAuxiliary.getSnomedDescriptionType(i).getUids()
                        .iterator().next().toString());
            //            // DESCRIPTION TYPES
            //            yDesTypeArray = new UUID[4];
            //            for (i = 0; i < 4; i++)
            //                yDesTypeArray[i] = ArchitectonicAuxiliary.getSnomedDescriptionType(i).getUids()
            //                        .iterator().next();
            //            // string lookup array
            //            yDesTypeStrArray = new String[4];
            //            for (int idx = 0; idx < 4; idx++)
            //                yDesTypeStrArray[idx] = yDesTypeArray[idx].toString();

            // RELATIONSHIP CHARACTERISTIC
            yRelCharArray = new UUID[5];
            for (int i = 0; i < 5; i++)
                yRelCharArray[i] = ArchitectonicAuxiliary.getSnomedCharacteristicType(i).getUids()
                        .iterator().next();
            // string lookup array
            yRelCharStrArray = new String[5];
            for (int idx = 0; idx < 5; idx++)
                yRelCharStrArray[idx] = yRelCharArray[idx].toString();

            // RELATIONSHIP REFINABILITY
            yRelRefArray = new UUID[3];
            for (int i = 0; i < 3; i++)
                yRelRefArray[i] = ArchitectonicAuxiliary.getSnomedRefinabilityType(i).getUids()
                        .iterator().next();
            // string lookup array
            yRelRefStrArray = new String[3];
            for (int idx = 0; idx < 3; idx++)
                yRelRefStrArray[idx] = yRelRefArray[idx].toString();

            // ID SOURCE SYSTEM
            yIdSrcSystemList = new ArrayList<IdSrcSystemEntry>();
            yIdSrcSystemList.add(new IdSrcSystemEntry(ArchitectonicAuxiliary.Concept.ICD_9
                    .getUids().iterator().next(), IdDataType.STRING));

        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoFailureException("FAILED: SctSiToEConcept -- setupLookupPartA()");
        }

    }

    private void setupLookupPartB() throws MojoFailureException {
        yStatusUuidArray = new UUID[yStatusUuidList.size()];
        int i = 0;
        for (String s : yStatusUuidList) {
            yStatusUuidArray[i] = UUID.fromString(s);
            i++;
        }

        yDesTypeUuidArray = new UUID[yDesTypeUuidList.size()];
        i = 0;
        for (String s : yDesTypeUuidList) {
            yDesTypeUuidArray[i] = UUID.fromString(s);
            i++;
        }

        yPathArray = new UUID[yPathList.size()];
        i = 0;
        for (String s : yPathList) {
            yPathArray[i] = UUID.fromString(s);
            i++;
        }

        yRevDateArray = new long[yRevDateList.size()];
        try {
            i = 0;
            for (String s : yRevDateList) {
                SimpleDateFormat df;
                if (s.contains("-"))
                    df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                else
                    df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                yRevDateArray[i] = df.parse(s).getTime();
                i++;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            throw new MojoFailureException(
                    "FAILED: SctSiToEConcept -- setupLookupConverion(), date parse error");
        }

        // SNOMED_INT ... :FYI: soft code in SctYConRecord
        ySourceUuidArray = new UUID[ySourceUuidList.size()];
        i = 0;
        for (String s : ySourceUuidList) {
            ySourceUuidArray[i] = UUID.fromString(s);
            i++;
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
            getLog().info("POM SCT Input Directory (" + i + "): " + sctInputDirArray[i]);
            if (!sctInputDirArray[i].startsWith(FILE_SEPARATOR)) {
                sctInputDirArray[i] = FILE_SEPARATOR + sctInputDirArray[i];
            }
        }

        for (int i = 0; i < arfInputDirArray.length; i++) {
            arfInputDirArray[i] = arfInputDirArray[i].replace('/', File.separatorChar);
            getLog().info("POM ARF Input Directory (" + i + "): " + arfInputDirArray[i]);
            if (!arfInputDirArray[i].startsWith(FILE_SEPARATOR)) {
                arfInputDirArray[i] = FILE_SEPARATOR + arfInputDirArray[i];
            }
        }

        // SHOW input sub directory from POM file
        if (!outputDirectory.equals("")) {
            outputDirectory = FILE_SEPARATOR + outputDirectory;
            getLog().info("POM Output Directory: " + outputDirectory);
        }

        executeMojo(buildDir, targetSubDir, arfInputDirArray, sctInputDirArray, outputDirectory,
                includeCTV3ID, includeSNOMEDRTID);
        getLog().info("POM PROCESSING COMPLETE ");
    }

    void executeMojo(String wDir, String subDir, String[] arfDirs, String[] sctDirs, String outDir,
            boolean ctv3idTF, boolean snomedrtTF) throws MojoFailureException {

        // :DEBUG:TEST:
        countRefsetMember = 0;
        countRefsetMaster = 0;
        statRsBoolFromArf = 0;
        statRsIntFromArf = 0;
        statRsConFromArf = 0;
        statRsStrFromArf = 0;

        getLog().info("::: Working Directory: " + wDir);
        getLog().info("::: Sub Directory:     " + subDir);
        for (int i = 0; i < sctDirs.length; i++)
            getLog().info("::: SCT Input Directory (" + i + "): " + sctDirs[i]);
        for (int i = 0; i < arfDirs.length; i++)
            getLog().info("::: ARF Input Directory (" + i + "): " + arfDirs[i]);
        getLog().info("::: Output Directory:  " + outDir);

        fNameStep1Con = wDir + scratchDirectory + FILE_SEPARATOR + "step1_concepts.ser";
        fNameStep1Rel = wDir + scratchDirectory + FILE_SEPARATOR + "step1_relationships.ser";
        fNameStep1Desc = wDir + scratchDirectory + FILE_SEPARATOR + "step1_descriptions.ser";
        fNameStep1Ids = wDir + scratchDirectory + FILE_SEPARATOR + "step1_ids.ser";

        fNameStep2Refset = wDir + scratchDirectory + FILE_SEPARATOR + "step2_refset.ser";

        fNameStep3RelDest = wDir + scratchDirectory + FILE_SEPARATOR + "step3_rel_dest.ser";

        fNameStep4Con = wDir + scratchDirectory + FILE_SEPARATOR + "step4_concepts.ser";
        fNameStep4Desc = wDir + scratchDirectory + FILE_SEPARATOR + "step4_descriptions.ser";
        fNameStep4Rel = wDir + scratchDirectory + FILE_SEPARATOR + "step4_relationships.ser";

        fNameStep5RsByCon = wDir + scratchDirectory + FILE_SEPARATOR + "step5_refset_by_con.ser";
        fNameStep5RsByRs = wDir + scratchDirectory + FILE_SEPARATOR + "step5_refet_by_refset.ser";

        fNameStep6Con = wDir + scratchDirectory + FILE_SEPARATOR + "step6_concepts.ser";
        fNameStep6Desc = wDir + scratchDirectory + FILE_SEPARATOR + "step6_descriptions.ser";
        fNameStep6Rel = wDir + scratchDirectory + FILE_SEPARATOR + "step6_relationships.ser";
        fNameStep6RelDest = wDir + scratchDirectory + FILE_SEPARATOR + "step6_rel_dest.ser";

        fNameStep7ECon = wDir + outDir + FILE_SEPARATOR + "sctSiEConcepts.jbin";

        yPathMap = new HashMap<String, Integer>();
        yPathList = new ArrayList<String>();
        yPathIdxCounter = -1;

        yRevDateMap = new HashMap<String, Integer>();
        yRevDateList = new ArrayList<String>();
        yRevDateIdxCounter = -1;

        ySourceUuidMap = new HashMap<String, Integer>();
        ySourceUuidList = new ArrayList<String>();
        ySourceUuidIdxCounter = -1;

        yStatusUuidMap = new HashMap<String, Integer>();
        yStatusUuidList = new ArrayList<String>();
        yStatusUuidIdxCounter = -1;

        yDesTypeUuidMap = new HashMap<String, Integer>();
        yDesTypeUuidList = new ArrayList<String>();
        yDesTypeUuidIdxCounter = -1;

        setupUuids();

        // Setup build directory
        getLog().info("Build Directory: " + wDir);

        ObjectOutputStream oosCon = null;
        ObjectOutputStream oosDes = null;
        ObjectOutputStream oosRel = null;
        ObjectOutputStream oosIds = null;
        ObjectOutputStream oosRefSet = null;
        // SETUP OUTPUT directory
        try {
            // Create multiple directories
            boolean success = (new File(wDir + outDir)).mkdirs();
            if (success) {
                getLog().info("OUTPUT DIRECTORY: " + wDir + outDir);
            }

            String tmpDir = scratchDirectory;
            success = (new File(wDir + tmpDir)).mkdirs();
            if (success) {
                getLog().info("SCRATCH DIRECTORY: " + wDir + tmpDir);
            }

            // SETUP CONCEPTS OUTPUT FILE
            oosCon = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep1Con)));
            getLog().info("Step 1 CONCEPTS OUTPUT: " + fNameStep1Con);

            // SETUP DESCRIPTIONS OUTPUT FILE
            oosDes = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep1Desc)));
            getLog().info("Step 1 DESCRIPTIONS OUTPUT: " + fNameStep1Desc);

            // SETUP RELATIONSHIPS OUTPUT FILE
            oosRel = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep1Rel)));
            getLog().info("RELATIONSHIPS Step 1 OUTPUT: " + fNameStep1Rel);

            // SETUP IDS OUTPUT FILE
            oosIds = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep1Ids)));
            getLog().info("IDS Step 1 OUTPUT: " + fNameStep1Ids);

            // SETUP REFSET OUTPUT FILE
            oosRefSet = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep2Refset)));
            getLog().info("REFSET Step 2 OUTPUT: " + fNameStep2Refset);

            setupLookupPartA();

            // STEP #1. Convert to versioned binary objects file.  
            // Also computes algorithmic relationship uuid.
            executeMojoStep1(wDir, subDir, sctDirs, ctv3idTF, snomedrtTF, oosCon, oosDes, oosRel);
            System.gc();

            // STEP #2. Convert arf files to versioned binary objects file.
            // Uses existing relationship uuid
            // Appends to binary stream created in Step 1.
            executeMojoStep2(wDir, subDir, arfDirs, oosCon, oosDes, oosRel, oosIds, oosRefSet);

            // stateSave(wDir);
            oosCon.close();
            oosDes.close();
            oosRel.close();
            oosIds.close();
            oosRefSet.close();

            // stateRestore(wDir);
            setupLookupPartB();

            // STEP #3. Gather destination relationship lists
            executeMojoStep3();
            System.gc();

            // STEP #4. Add IDs to components.
            executeMojoStep4();
            System.gc();

            // STEP #5. Add IDs to components.
            executeMojoStep5();

            // Step #6. Sort files to concept order for next stage
            executeMojoStep6();

            // STEP #7. Convert to EConcepts
            executeMojoStep7();

        } catch (Exception e) { // Catch exception if any
            getLog().info("SctYToEConceptsMojo sct-y-to-econcepts Error");
            throw new MojoFailureException("Error", e);
        }
    }

    private void executeMojoStep1(String wDir, String subDir, String[] inDirs, boolean ctv3idTF,
            boolean snomedrtTF, ObjectOutputStream oosCon, ObjectOutputStream oosDes,
            ObjectOutputStream oosRel) throws MojoFailureException {
        getLog().info("*** SctSiToEConcept STEP #1 BEGINNING ***");
        long start = System.currentTimeMillis();

        // PROCESS SNOMED FILES
        try {
            // SETUP CONCEPTS INPUT SCTFile ArrayList
            List<List<SCTFile>> listOfCDirs = getSctFiles(wDir, subDir, inDirs, "concept", ".txt");
            processConceptsFiles(wDir, listOfCDirs, ctv3idTF, snomedrtTF, oosCon);
            listOfCDirs = null;
            System.gc();
        } catch (Exception e1) {
            getLog().info("FAILED: processConceptsFiles()");
            e1.printStackTrace();
            throw new MojoFailureException("FAILED: processConceptsFiles()", e1);
        }

        try {
            // SETUP DESCRIPTIONS INPUT SCTFile ArrayList
            List<List<SCTFile>> listOfDDirs = getSctFiles(wDir, subDir, inDirs, "descriptions",
                    ".txt");
            processDescriptionsFiles(wDir, listOfDDirs, oosDes);
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
        List<List<SCTFile>> listOfRiDirs = getSctFiles(wDir, subDir, inDirs,
                "relationships_inferred", ".txt");

        // SETUP STATED RELATIONSHIPS INPUT SCTFile ArrayList
        List<List<SCTFile>> listOfRsDirs = getSctFiles(wDir, subDir, inDirs,
                "relationships_stated", ".txt");
        try {
            getLog().info("START RELATIONSHIPS PROCESSING...");

            // SETUP RELATIONSHIPS EXCEPTION REPORT FILE
            String erFileName = wDir + scratchDirectory + FILE_SEPARATOR
                    + "relationships_report.txt";
            BufferedWriter erw;
            erw = new BufferedWriter(new FileWriter(erFileName));
            getLog().info("RELATIONSHIPS Exceptions Report OUTPUT: " + erFileName);

            processRelationshipsFiles(wDir, listOfRiDirs, oosRel, erw);
            processRelationshipsFiles(wDir, listOfRsDirs, oosRel, erw);

            erw.close(); // Need to be sure to the close file!
        } catch (Exception e1) {
            getLog().info("FAILED: processRelationshipsFiles()");
            e1.printStackTrace();
            throw new MojoFailureException("FAILED: processRelationshipsFiles()", e1);
        }

        relUuidMap = null; // memory not needed any more.
        System.gc();
        getLog().info(
                "*** VERSIONING TIME: " + ((System.currentTimeMillis() - start) / 1000)
                        + " seconds");
        getLog().info("*** SctSiToEConcept STEP #1 COMPLETED ***\r\n");
    }

    private void executeMojoStep2(String wDir, String subDir, String[] arfDirs,
            ObjectOutputStream oosCon, ObjectOutputStream oosDes, ObjectOutputStream oosRel,
            ObjectOutputStream oosIds, ObjectOutputStream oosRefSet) {
        getLog().info("*** SctSiToEConcept STEP #2 BEGINNING - INGEST ARF ***");
        long start = System.currentTimeMillis();

        try {

            // PROCESS CONCEPT ARF FILES
            List<List<ARFFile>> listOfCDirs = getArfFiles(wDir, subDir, arfDirs, "concepts", ".txt");
            processArfConFiles(wDir, listOfCDirs, oosCon);
            listOfCDirs = null;
            System.gc();

            // PROCESS DESCRIPTION ARF FILES
            List<List<ARFFile>> listOfDDirs = getArfFiles(wDir, subDir, arfDirs, "descriptions",
                    ".txt");
            processArfDesFiles(wDir, listOfDDirs, oosDes);
            listOfDDirs = null;
            System.gc();

            // PROCESS RELATIONSHIP ARF FILES
            List<List<ARFFile>> listOfRDirs = getArfFiles(wDir, subDir, arfDirs, "relationships",
                    ".txt");
            processArfRelFiles(wDir, listOfRDirs, oosRel);
            listOfRDirs = null;
            System.gc();

            // PROCESS IDS ARF FILES
            List<List<ARFFile>> listOfIDirs = getArfFiles(wDir, subDir, arfDirs, "ids", ".txt");
            processArfIdsFiles(wDir, listOfIDirs, oosIds);
            listOfIDirs = null;
            System.gc();

            // PROCESS REFSET BOOLEAN FILES
            List<List<ARFFile>> listOfRsBoolDirs = getArfFiles(wDir, subDir, arfDirs, "boolean",
                    ".refset");
            processArfRsBoolFiles(wDir, listOfRsBoolDirs, oosRefSet);
            listOfRsBoolDirs = null;
            System.gc();

            // PROCESS REFSET CONCEPT FILES
            List<List<ARFFile>> listOfRsConDirs = getArfFiles(wDir, subDir, arfDirs, "concept",
                    ".refset");
            processArfRsConFiles(wDir, listOfRsConDirs, oosRefSet);
            listOfRsConDirs = null;
            System.gc();

            // PROCESS REFSET INTEGER FILES
            List<List<ARFFile>> listOfRsIntDirs = getArfFiles(wDir, subDir, arfDirs, "integer",
                    ".refset");
            processArfRsIntFiles(wDir, listOfRsIntDirs, oosRefSet);
            listOfRsIntDirs = null;
            System.gc();

            // PROCESS REFSET STRING FILES
            List<List<ARFFile>> listOfRsStrDirs = getArfFiles(wDir, subDir, arfDirs, "string",
                    ".refset");
            processArfRsStrFiles(wDir, listOfRsStrDirs, oosRefSet);
            listOfRsStrDirs = null;
            System.gc();

            getLog().info(
                    "\r\nstatRsBoolFromArf= " + statRsBoolFromArf + "\r\nstatRsIntFromArf= "
                            + statRsIntFromArf + "\r\nstatRsConFromArf= " + statRsConFromArf
                            + "\r\nstatRsStrFromArf= " + statRsStrFromArf);

        } catch (MojoFailureException e1) {
            getLog().info("FAILED: processArfIdFiles()");
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        getLog().info(
                "*** ARF TO BINARY OBJECT TIME: " + ((System.currentTimeMillis() - start) / 1000)
                        + " seconds");
        getLog().info("*** SctSiToEConcept STEP #2 COMPLETED - INGEST ARF ***\r\n");
    }

    private void processArfConFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException {
        for (List<ARFFile> laf : listOfDirs)
            for (ARFFile f : laf)
                parseArfConFile(f.file, oos);
    }

    private void parseArfConFile(File f, ObjectOutputStream oos) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));

        int CONCEPT_UUID = 0;
        int CONCEPT_STATUS = 1;
        int ISPRIMITIVE = 2; // primitive
        int EFFECTIVE_DATE = 3; // Effective Date
        int PATH_UUID = 4; // Path UUID

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // Status UUID
            UUID uuidCon = UUID.fromString(line[CONCEPT_UUID]);
            // Status
            int conceptStatus = lookupYStatusUuidIdx(line[CONCEPT_STATUS]);
            // Primitive
            String isPrimitiveStr = line[ISPRIMITIVE];
            int isPrimitive = 0;
            if (isPrimitiveStr.startsWith("1") || isPrimitiveStr.startsWith("t")
                    || isPrimitiveStr.startsWith("T"))
                isPrimitive = 1;
            // Effective Date
            int revDate = lookupYRevDateIdx(line[EFFECTIVE_DATE]);
            // Path UUID
            int pathIdx = lookupYPathIdx(line[PATH_UUID]);

            SctYConRecord tmpConRec = new SctYConRecord(uuidCon, conceptStatus, isPrimitive,
                    revDate, pathIdx);

            oos.writeUnshared(tmpConRec);
        }
        br.close();
    }

    private void processArfDesFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException {
        for (List<ARFFile> laf : listOfDirs)
            for (ARFFile f : laf)
                parseArfDesFile(f.file, oos);
    }

    private void parseArfDesFile(File f, ObjectOutputStream oos) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));

        int DESCRIPTION_UUID = 0;
        int STATUS_UUID = 1;
        int CONCEPT_UUID = 2;
        int TERM_STRING = 3;
        int CAPITALIZATION_STATUS_INT = 4;
        int DESCRIPTION_TYPE_UUID = 5;
        int LANGUAGE_CODE_STR = 6;
        int EFFECTIVE_DATE = 7;
        int PATH_UUID = 8;

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // DESCRIPTION_UUID = 0;
            UUID uuidDes = UUID.fromString(line[DESCRIPTION_UUID]);
            // STATUS_UUID = 1;
            int status = lookupYStatusUuidIdx(line[STATUS_UUID]);
            // CONCEPT_UUID = 2;
            UUID uuidCon = UUID.fromString(line[CONCEPT_UUID]);
            // TERM_STRING = 3;
            String termStr = line[TERM_STRING];
            // CAPITALIZATION_STATUS = 4;
            // int capitalization = Integer.parseInt(line[CAPITALIZATION_STATUS_INT]);
            String capitalizationStr = line[CAPITALIZATION_STATUS_INT];
            int capitalization = 0;
            if (capitalizationStr.startsWith("1") || capitalizationStr.startsWith("t")
                    || capitalizationStr.startsWith("T"))
                capitalization = 1;

            // DESCRIPTION_TYPE = 5;
            int descriptionType = lookupYDesTypeUuidIdx(line[DESCRIPTION_TYPE_UUID]);
            //            int descriptionType = lookupDesTypeIdx(line[DESCRIPTION_TYPE_UUID]);
            // LANGUAGE_CODE = 6;
            String langCodeStr = line[LANGUAGE_CODE_STR];
            // EFFFECTIVE_DATE = 7;
            int revDate = lookupYRevDateIdx(line[EFFECTIVE_DATE]);
            // PATH_UUID = 8;
            int pathIdx = lookupYPathIdx(line[PATH_UUID]);

            SctYDesRecord tmpDesRec = new SctYDesRecord(uuidDes, status, uuidCon, termStr,
                    capitalization, descriptionType, langCodeStr, revDate, pathIdx);

            try {
                oos.writeUnshared(tmpDesRec);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        br.close();
    }

    private void processArfRelFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException {
        for (List<ARFFile> laf : listOfDirs)
            for (ARFFile f : laf)
                parseArfRelFile(f.file, oos);
    }

    private void parseArfRelFile(File f, ObjectOutputStream oos) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));

        int RELATIONSHIP_UUID = 0;
        int STATUS_UUID = 1;
        int C1_UUID = 2;
        int ROLE_TYPE_UUID = 3;
        int C2_UUID = 4;
        int CHARACTERISTIC_UUID = 5;
        int REFINABILITY_UUID = 6;
        int GROUP = 7;
        int EFFECTIVE_DATE = 8; // yyyy-MM-dd HH:mm:ss
        int PATH_UUID = 9;

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // RELATIONSHIP_UUID = 0;
            UUID uuidRelId = UUID.fromString(line[RELATIONSHIP_UUID]);
            // STATUS_UUID = 1;
            int status = lookupYStatusUuidIdx(line[STATUS_UUID]);
            // C1_UUID = 2;
            UUID uuidC1 = UUID.fromString(line[C1_UUID]);
            // ROLE_TYPE_UUID = 3;
            int roleTypeIdx = lookupRoleTypeIdx(line[ROLE_TYPE_UUID]);
            // C2_UUID = 4;
            UUID uuidC2 = UUID.fromString(line[C2_UUID]);
            // CHARACTERISTIC_UUID = 5;
            int characteristic = lookupRelCharTypeIdx(line[CHARACTERISTIC_UUID]);
            // REFINABILITY_UUID = 6;
            int refinability = lookupRelRefTypeIdx(line[REFINABILITY_UUID]);
            // GROUP = 7;
            int group = Integer.parseInt(line[GROUP]);
            // EFFECTIVE_DATE = 8;  // yyyy-MM-dd HH:mm:ss
            int revDate = lookupYRevDateIdx(line[EFFECTIVE_DATE]);
            // PATH_UUID = 9;
            int pathIdx = lookupYPathIdx(line[PATH_UUID]);

            SctYRelRecord tmpRelRec = new SctYRelRecord(uuidRelId, status, uuidC1, roleTypeIdx,
                    uuidC2, characteristic, refinability, group, revDate, pathIdx);

            oos.writeUnshared(tmpRelRec);
        }

        br.close();
    }

    private void processArfIdsFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException {
        for (List<ARFFile> laf : listOfDirs)
            for (ARFFile f : laf)
                parseArfIdsFile(f.file, oos);
    }

    private void parseArfIdsFile(File f, ObjectOutputStream oos) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));

        int PRIMARY_UUID = 0;
        int SOURCE_SYSTEM_UUID = 1;
        int ID_FROM_SOURCE_SYSTEM = 2;
        int STATUS_UUID = 3;
        int EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
        int PATH_UUID = 5;

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // PRIMARY_UUID = 0;
            UUID uuidPrimaryId = UUID.fromString(line[PRIMARY_UUID]);
            // SOURCE_SYSTEM_UUID = 1;
            int sourceSystemIdx = lookupSrcSystemIdx(line[SOURCE_SYSTEM_UUID]);
            // ID_FROM_SOURCE_SYSTEM = 2;
            String idFromSourceSystem = line[ID_FROM_SOURCE_SYSTEM];
            // STATUS_UUID = 3;
            int status = lookupYStatusUuidIdx(line[STATUS_UUID]);
            // EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
            int revDate = lookupYRevDateIdx(line[EFFECTIVE_DATE]);
            // PATH_UUID = 5;
            int pathIdx = lookupYPathIdx(line[PATH_UUID]);

            SctYIdRecord tmpIdRec = new SctYIdRecord(uuidPrimaryId, sourceSystemIdx,
                    idFromSourceSystem, status, revDate, pathIdx);

            oos.writeUnshared(tmpIdRec);
        }

        br.close();
    }

    private void processArfRsBoolFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException {
        for (List<ARFFile> laf : listOfDirs)
            for (ARFFile f : laf)
                parseArfRsBoolFile(f.file, oos);
    }

    private void parseArfRsBoolFile(File f, ObjectOutputStream oos) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));

        int REFSEST_UUID = 0;
        int MEMBER_UUID = 1;
        int STATUS_UUID = 2;
        int COMPONENT_UUID = 3;
        int EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
        int PATH_UUID = 5;
        int EXT_VALUE_UUID = 6;

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // REFSEST_UUID = 0;
            UUID uuidRefset = UUID.fromString(line[REFSEST_UUID]);
            // MEMBER_UUID = 1;
            UUID uuidMember = UUID.fromString(line[MEMBER_UUID]);
            // STATUS_UUID = 2;
            int status = lookupYStatusUuidIdx(line[STATUS_UUID]);
            // COMPONENT_UUID = 3;
            UUID uuidComponent = UUID.fromString(line[COMPONENT_UUID]);
            // EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
            int revDate = lookupYRevDateIdx(line[EFFECTIVE_DATE]);
            // PATH_UUID = 5;
            int pathIdx = lookupYPathIdx(line[PATH_UUID]);
            // EXT_VALUE_UUID = 6;
            boolean vBool = false;
            if (line[EXT_VALUE_UUID].charAt(0) == 't' || line[EXT_VALUE_UUID].charAt(0) == 'T')
                vBool = true;

            // :DEBUG:!!!:
            //            if (uuidComponent.equals(UUID.fromString("7c57f6b4-4a63-52ad-b762-73acc15f23de"))) 
            //                getLog().info("FOUND IT");

            SctYRefSetRecord tmpRsRec = new SctYRefSetRecord(uuidRefset, uuidMember, uuidComponent,
                    status, revDate, pathIdx, vBool);

            statRsBoolFromArf++;
            oos.writeUnshared(tmpRsRec);
        }

        br.close();
    }

    private void processArfRsConFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException {
        for (List<ARFFile> laf : listOfDirs)
            for (ARFFile f : laf)
                parseArfRsConFile(f.file, oos);
    }

    private void parseArfRsConFile(File f, ObjectOutputStream oos) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));

        int REFSEST_UUID = 0;
        int MEMBER_UUID = 1;
        int STATUS_UUID = 2;
        int COMPONENT_UUID = 3;
        int EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
        int PATH_UUID = 5;
        int EXT_VALUE_UUID = 6;

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // REFSEST_UUID = 0;
            UUID uuidRefset = UUID.fromString(line[REFSEST_UUID]);
            // MEMBER_UUID = 1;
            UUID uuidMember = UUID.fromString(line[MEMBER_UUID]);
            // STATUS_UUID = 2;
            int status = lookupYStatusUuidIdx(line[STATUS_UUID]);
            // COMPONENT_UUID = 3;
            UUID uuidComponent = UUID.fromString(line[COMPONENT_UUID]);
            // EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
            int revDate = lookupYRevDateIdx(line[EFFECTIVE_DATE]);
            // PATH_UUID = 5;
            int pathIdx = lookupYPathIdx(line[PATH_UUID]);
            // EXT_VALUE_UUID = 6;
            UUID uuidConExt = UUID.fromString(line[EXT_VALUE_UUID]);

            SctYRefSetRecord tmpRsRec = new SctYRefSetRecord(uuidRefset, uuidMember, uuidComponent,
                    status, revDate, pathIdx, uuidConExt);

            statRsConFromArf++;
            oos.writeUnshared(tmpRsRec);
        }

        br.close();
    }

    private void processArfRsIntFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException {
        for (List<ARFFile> laf : listOfDirs)
            for (ARFFile f : laf)
                parseArfRsIntFile(f.file, oos);
    }

    private void parseArfRsIntFile(File f, ObjectOutputStream oos) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));

        int REFSEST_UUID = 0;
        int MEMBER_UUID = 1;
        int STATUS_UUID = 2;
        int COMPONENT_UUID = 3;
        int EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
        int PATH_UUID = 5;
        int EXT_VALUE_UUID = 6;

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // REFSEST_UUID = 0;
            UUID uuidRefset = UUID.fromString(line[REFSEST_UUID]);
            // MEMBER_UUID = 1;
            UUID uuidMember = UUID.fromString(line[MEMBER_UUID]);
            // STATUS_UUID = 2;
            int status = lookupYStatusUuidIdx(line[STATUS_UUID]);
            // COMPONENT_UUID = 3;
            UUID uuidComponent = UUID.fromString(line[COMPONENT_UUID]);
            // EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
            int revDate = lookupYRevDateIdx(line[EFFECTIVE_DATE]);
            // PATH_UUID = 5;
            int pathIdx = lookupYPathIdx(line[PATH_UUID]);
            // CONCEPT_EXT_VALUE_UUID = 6;
            int vInt = Integer.valueOf(line[EXT_VALUE_UUID]);

            SctYRefSetRecord tmpRsRec = new SctYRefSetRecord(uuidRefset, uuidMember, uuidComponent,
                    status, revDate, pathIdx, vInt);

            statRsIntFromArf++;
            oos.writeUnshared(tmpRsRec);
        }

        br.close();
    }

    private void processArfRsStrFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException {
        for (List<ARFFile> laf : listOfDirs)
            for (ARFFile f : laf)
                parseArfRsStrFile(f.file, oos);
    }

    private void parseArfRsStrFile(File f, ObjectOutputStream oos) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));

        int REFSEST_UUID = 0;
        int MEMBER_UUID = 1;
        int STATUS_UUID = 2;
        int COMPONENT_UUID = 3;
        int EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
        int PATH_UUID = 5;
        int EXT_VALUE_UUID = 6;

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // REFSEST_UUID = 0;
            UUID uuidRefset = UUID.fromString(line[REFSEST_UUID]);
            // MEMBER_UUID = 1;
            UUID uuidMember = UUID.fromString(line[MEMBER_UUID]);
            // STATUS_UUID = 2;
            int status = lookupYStatusUuidIdx(line[STATUS_UUID]);
            // COMPONENT_UUID = 3;
            UUID uuidComponent = UUID.fromString(line[COMPONENT_UUID]);
            // EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
            int revDate = lookupYRevDateIdx(line[EFFECTIVE_DATE]);
            // PATH_UUID = 5;
            int pathIdx = lookupYPathIdx(line[PATH_UUID]);
            // CONCEPT_EXT_VALUE_UUID = 6;
            String vStr = line[EXT_VALUE_UUID];

            SctYRefSetRecord tmpRsRec = new SctYRefSetRecord(uuidRefset, uuidMember, uuidComponent,
                    status, revDate, pathIdx, vStr);

            statRsStrFromArf++;
            oos.writeUnshared(tmpRsRec);
        }

        br.close();
    }

    private void executeMojoStep3() throws MojoFailureException {
        getLog().info("*** SctSiToEConcept STEP #3 BEGINNING -- GATHER DESTINATION RELs ***");
        long start = System.currentTimeMillis();

        try {
            // read in relationships, sort by C2-ROLETYPE
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(fNameStep1Rel)));
            ArrayList<SctYRelRecord> aRel = new ArrayList<SctYRelRecord>();

            int count = 0;
            Object obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof SctYRelRecord) {
                        aRel.add((SctYRelRecord) obj);
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" relationship count = " + count);
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" relationship count = " + count + " @EOF\r\n");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new MojoFailureException("ClassNotFoundException -- Step 2 reading file");
            }
            ois.close();
            getLog().info(" relationship count = " + count + "\r\n");

            // SORT BY [C2-RoleType]
            Comparator<SctYRelRecord> compRelDest = new Comparator<SctYRelRecord>() {
                public int compare(SctYRelRecord o1, SctYRelRecord o2) {
                    int thisMore = 1;
                    int thisLess = -1;
                    // C2
                    if (o1.c2UuidMsb > o2.c2UuidMsb) {
                        return thisMore;
                    } else if (o1.c2UuidMsb < o2.c2UuidMsb) {
                        return thisLess;
                    } else {
                        if (o1.c2UuidLsb > o2.c2UuidLsb) {
                            return thisMore;
                        } else if (o1.c2UuidLsb < o2.c2UuidLsb) {
                            return thisLess;
                        } else {
                            // ROLE TYPE
                            if (o1.relUuidMsb > o2.relUuidMsb) {
                                return thisMore;
                            } else if (o1.relUuidMsb < o2.relUuidMsb) {
                                return thisLess;
                            } else {
                                if (o1.relUuidLsb > o2.relUuidLsb) {
                                    return thisMore;
                                } else if (o1.relUuidLsb < o2.relUuidLsb) {
                                    return thisLess;
                                } else {
                                    return 0; // EQUAL
                                }
                            }
                        }
                    }
                } // compare()
            };
            Collections.sort(aRel, compRelDest);

            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(fNameStep3RelDest)));
            long lastRelMsb = Long.MIN_VALUE;
            long lastRelLsb = Long.MIN_VALUE;
            for (SctYRelRecord r : aRel) {
                if (r.relUuidMsb != lastRelMsb || r.relUuidLsb != lastRelLsb) {
                    oos.writeUnshared(new SctYRelDestRecord(r.relUuidMsb, r.relUuidLsb,
                            r.c2UuidMsb, r.c2UuidLsb, r.roleTypeIdx));
                }
                lastRelMsb = r.relUuidMsb;
                lastRelLsb = r.relUuidLsb;
            }
            oos.flush();
            oos.close();
            aRel = null;
            System.gc();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        getLog().info(
                "*** DESTINATION RELs: " + ((System.currentTimeMillis() - start) / 1000)
                        + " seconds");
        getLog().info("*** SctSiToEConcept STEP #3 COMPLETED -- GATHER DESTINATION RELs ***\r\n");
    }

    private void executeMojoStep4() {
        getLog().info("*** SctSiToEConcept STEP #4 BEGINNING -- MATCH IDs ***");
        long start = System.currentTimeMillis();

        try {
            // Read in IDs. Sort by primary uuid
            // *** IDs ***
            ObjectInputStream ois;
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fNameStep1Ids)));
            ArrayList<SctYIdRecord> aId = new ArrayList<SctYIdRecord>();

            int count = 0;
            Object obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof SctYIdRecord) {
                        aId.add((SctYIdRecord) obj);
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" concept count = " + count);
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" id count = " + count + "\r\n");
            }
            ois.close();

            // SORT BY [PRIMARYID, Path, Revision]
            Collections.sort(aId);

            // Read in con.  Sort by con uuid.
            // *** CONCEPTS ***
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fNameStep1Con)));
            ArrayList<SctYConRecord> aCon = new ArrayList<SctYConRecord>();

            count = 0;
            obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof SctYConRecord) {
                        aCon.add((SctYConRecord) obj);
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" concept count = " + count);
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" concept count = " + count + " @EOF\r\n");
            }
            ois.close();
            getLog().info(" concept count = " + count + "\r\n");

            // SORT BY [CONCEPTID, Path, Revision]
            Collections.sort(aCon);

            // MATCH & ADD ID TO CONCEPT
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(fNameStep4Con)));

            int lastIdIdx = aId.size();
            int lastConIdx = aCon.size();
            int theIdIdx = 0;
            int theConIdx = 0;
            while (theIdIdx < lastIdIdx && theConIdx < lastConIdx) {
                SctYConRecord tmpCon = aCon.get(theConIdx);
                int match = checkIdConMatched(aId.get(theIdIdx), tmpCon);

                if (match == 0) {
                    // MATCH
                    if (tmpCon.addedIds == null) {
                        tmpCon.addedIds = new ArrayList<SctYIdRecord>();
                    }
                    tmpCon.addedIds.add(aId.get(theIdIdx));
                    theIdIdx++; // Get next id.
                } else if (match == 1) {
                    // Ids are ahead of the concepts.
                    oos.writeUnshared(tmpCon); // Save this concept.
                    theConIdx++; // Get next concept.
                } else {
                    // Concepts are ahead of the ids.
                    theIdIdx++; // Get the next id.
                }
            }
            while (theConIdx < lastConIdx) {
                oos.writeUnshared(aCon.get(theConIdx)); // Save this concept.
                theConIdx++;
            }
            oos.flush();
            oos.close();
            aCon = null;

            // Read in des.  Sort by des uuid.
            // *** DESCRIPTIONS ***
            ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(fNameStep1Desc)));
            ArrayList<SctYDesRecord> aDes = new ArrayList<SctYDesRecord>();

            count = 0;
            obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof SctYDesRecord) {
                        aDes.add((SctYDesRecord) obj);
                        count++;

                        if (count % 100000 == 0)
                            getLog().info(" description count = " + count);
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" description count = " + count + " @EOF\r\n");
            }
            ois.close();
            getLog().info(" description count = " + count + "\r\n");

            // SORT BY [CONCEPTID, DESCRIPTIONID, Path, Revision]
            Comparator<SctYDesRecord> compDes = new Comparator<SctYDesRecord>() {
                public int compare(SctYDesRecord o1, SctYDesRecord o2) {
                    int thisMore = 1;
                    int thisLess = -1;
                    // DESCRIPTION UUID
                    if (o1.desUuidMsb > o2.desUuidMsb) {
                        return thisMore;
                    } else if (o1.desUuidMsb < o2.desUuidMsb) {
                        return thisLess;
                    } else {
                        if (o1.desUuidLsb > o2.desUuidLsb) {
                            return thisMore;
                        } else if (o1.desUuidLsb < o2.desUuidLsb) {
                            return thisLess;
                        } else {
                            // Path
                            if (o1.yPath > o2.yPath) {
                                return thisMore;
                            } else if (o1.yPath < o2.yPath) {
                                return thisLess;
                            } else {
                                // Revision
                                if (o1.yRevision > o2.yRevision) {
                                    return thisMore;
                                } else if (o1.yRevision < o2.yRevision) {
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

            // MATCH & ADD ID TO DESCRIPTION
            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep4Desc)));

            lastIdIdx = aId.size();
            theIdIdx = 0;
            int lastDesIdx = aDes.size();
            int theDesIdx = 0;
            while (theIdIdx < lastIdIdx && theDesIdx < lastDesIdx) {
                SctYDesRecord tmpDes = aDes.get(theDesIdx);
                int match = checkIdDesMatched(aId.get(theIdIdx), tmpDes);
                if (match == 0) {
                    // MATCH
                    if (tmpDes.addedIds == null) {
                        tmpDes.addedIds = new ArrayList<SctYIdRecord>();
                    }
                    tmpDes.addedIds.add(aId.get(theIdIdx));
                    theIdIdx++; // Get next id.
                } else if (match == 1) {
                    // Ids are ahead of the concepts.
                    oos.writeUnshared(tmpDes); // Save this description.
                    theDesIdx++; // Get next description.
                } else {
                    // Concepts are ahead of the ids.
                    theIdIdx++; // Get the next id.
                }
            }
            while (theDesIdx < lastDesIdx) {
                oos.writeUnshared(aDes.get(theDesIdx)); // Save this concept.
                theDesIdx++;
            }
            oos.flush();
            oos.close();
            aDes = null;

            // Read in rel. Sort by rel uuid.
            // *** RELATIONSHIPS ***
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fNameStep1Rel)));
            ArrayList<SctYRelRecord> aRel = new ArrayList<SctYRelRecord>();

            count = 0;
            obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof SctYRelRecord) {
                        aRel.add((SctYRelRecord) obj);
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" relationships count = " + count);
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" relationships count = " + count + " @EOF\r\n");
            }
            ois.close();
            getLog().info(" relationships count = " + count + "\r\n");

            // SORT BY [RelUUID-Path-RevisionVersion]
            Comparator<SctYRelRecord> compRel = new Comparator<SctYRelRecord>() {
                public int compare(SctYRelRecord o1, SctYRelRecord o2) {
                    int thisMore = 1;
                    int thisLess = -1;
                    // RELATIONSHIP UUID
                    if (o1.relUuidMsb > o2.relUuidMsb) {
                        return thisMore;
                    } else if (o1.relUuidMsb < o2.relUuidMsb) {
                        return thisLess;
                    } else {
                        if (o1.relUuidLsb > o2.relUuidLsb) {
                            return thisMore;
                        } else if (o1.relUuidLsb < o2.relUuidLsb) {
                            return thisLess;
                        } else {
                            // PATH
                            if (o1.yPath > o2.yPath) {
                                return thisMore;
                            } else if (o1.yPath < o2.yPath) {
                                return thisLess;
                            } else {
                                // VERSION
                                if (o1.yRevision > o2.yRevision) {
                                    return thisMore;
                                } else if (o1.yRevision < o2.yRevision) {
                                    return thisLess;
                                } else {
                                    return 0; // EQUAL
                                }
                            }
                        }
                    }
                } // compare()
            };
            Collections.sort(aRel, compRel);

            // MATCH & ADD ID TO RELATIONSHIP
            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep4Rel)));

            theIdIdx = 0;
            lastIdIdx = aId.size();
            int lastRelIdx = aRel.size();
            int theRelIdx = 0;
            while (theIdIdx < lastIdIdx && theRelIdx < lastRelIdx) {
                SctYRelRecord tmpRel = aRel.get(theRelIdx);
                int match = checkIdRelMatched(aId.get(theIdIdx), tmpRel);

                if (match == 0) {
                    // MATCH
                    if (tmpRel.addedIds == null) {
                        tmpRel.addedIds = new ArrayList<SctYIdRecord>();
                    }
                    tmpRel.addedIds.add(aId.get(theIdIdx));
                    theIdIdx++; // Get next id.
                } else if (match == 1) {
                    // Ids are ahead of the concepts.
                    oos.writeUnshared(tmpRel); // Save this concept.
                    theRelIdx++; // Get next concept.
                } else {
                    // Concepts are ahead of the ids.
                    theIdIdx++; // Get the next id.
                }
            }
            while (theRelIdx < lastRelIdx) {
                oos.writeUnshared(aRel.get(theRelIdx)); // Save this concept.
                theRelIdx++;
            }
            oos.flush();
            oos.close();
            aRel = null;

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        getLog().info(
                "*** ATTACH IDs TIME: " + ((System.currentTimeMillis() - start) / 1000)
                        + " seconds");
        getLog().info("*** SctSiToEConcept STEP #4 COMPLETED - MATCH IDs ***\r\n");
    }

    private TkIdentifier createEIdentifier(SctYIdRecord id) {
        // :!!!:NYI: add Long, String, type detection.
        // This version is just for string identifiers.
        EIdentifierString eId = new EIdentifierString();
        eId.setAuthorityUuid(lookupSrcSystemUUID(id.srcSystemIdx));
        eId.setDenotation(id.denotation);
        long msb = yPathArray[id.yPath].getMostSignificantBits();
        long lsb = yPathArray[id.yPath].getLeastSignificantBits();
        eId.setPathUuid(new UUID(msb, lsb));
        // eId.setPathUuid(yPathArray[id.yPath]);
        msb = yStatusUuidArray[id.status].getMostSignificantBits();
        lsb = yStatusUuidArray[id.status].getLeastSignificantBits();
        eId.setStatusUuid(new UUID(msb, lsb));
        // eId.setStatusUuid(lookupYStatus(id.status));
        eId.setTime(id.yRevision);

        return eId;
    }

    private int checkIdConMatched(SctYIdRecord id, SctYConRecord con) {
        // TODO Auto-generated method stub
        final int BEFORE = -1;
        final int MATCH = 0;
        final int AFTER = 1;

        if (id.primaryUuidMsb > con.conUuidMsb)
            return AFTER;
        else if (id.primaryUuidMsb == con.conUuidMsb && id.primaryUuidLsb > con.conUuidLsb)
            return AFTER;
        else if (id.primaryUuidMsb == con.conUuidMsb && id.primaryUuidLsb == con.conUuidLsb
                && id.yPath > con.yPath)
            return AFTER;
        else if (id.primaryUuidMsb == con.conUuidMsb && id.primaryUuidLsb == con.conUuidLsb
                && id.yPath == con.yPath && id.yRevision > con.yRevision)
            return AFTER;

        if (id.primaryUuidMsb == con.conUuidMsb && id.primaryUuidLsb == con.conUuidLsb
                && id.yPath == con.yPath && id.yRevision == con.yRevision)
            return MATCH;
        return BEFORE;
    }

    private int checkIdDesMatched(SctYIdRecord id, SctYDesRecord des) {
        // TODO Auto-generated method stub
        final int BEFORE = -1;
        final int MATCH = 0;
        final int AFTER = 1;

        if (id.primaryUuidMsb > des.desUuidMsb)
            return AFTER;
        else if (id.primaryUuidMsb == des.desUuidMsb && id.primaryUuidLsb > des.desUuidLsb)
            return AFTER;
        else if (id.primaryUuidMsb == des.desUuidMsb && id.primaryUuidLsb == des.desUuidLsb
                && id.yPath > des.yPath)
            return AFTER;
        else if (id.primaryUuidMsb == des.desUuidMsb && id.primaryUuidLsb == des.desUuidLsb
                && id.yPath == des.yPath && id.yRevision > des.yRevision)
            return AFTER;

        if (id.primaryUuidMsb == des.desUuidMsb && id.primaryUuidLsb == des.desUuidLsb
                && id.yPath == des.yPath && id.yRevision == des.yRevision)
            return MATCH;
        return BEFORE;
    }

    private int checkIdRelMatched(SctYIdRecord id, SctYRelRecord rel) {
        // TODO Auto-generated method stub
        final int BEFORE = -1;
        final int MATCH = 0;
        final int AFTER = 1;

        if (id.primaryUuidMsb > rel.relUuidMsb)
            return AFTER;
        else if (id.primaryUuidMsb == rel.relUuidMsb && id.primaryUuidLsb > rel.relUuidLsb)
            return AFTER;
        else if (id.primaryUuidMsb == rel.relUuidMsb && id.primaryUuidLsb == rel.relUuidLsb
                && id.yPath > rel.yPath)
            return AFTER;
        else if (id.primaryUuidMsb == rel.relUuidMsb && id.primaryUuidLsb == rel.relUuidLsb
                && id.yPath == rel.yPath && id.yRevision > rel.yRevision)
            return AFTER;

        if (id.primaryUuidMsb == rel.relUuidMsb && id.primaryUuidLsb == rel.relUuidLsb
                && id.yPath == rel.yPath && id.yRevision == rel.yRevision)
            return MATCH;
        return BEFORE;
    }

    private void executeMojoStep5() {
        getLog().info("*** SctSiToEConcept Step #5 BEGINNING -- REFSET PREPARATION ***");
        long start = System.currentTimeMillis();

        try {
            // *** READ IN REFSET ***
            ObjectInputStream ois;
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                    fNameStep2Refset)));
            ArrayList<SctYRefSetRecord> aRs = new ArrayList<SctYRefSetRecord>();

            int count = 0;
            Object obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof SctYRefSetRecord) {
                        aRs.add((SctYRefSetRecord) obj);
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" concept count = " + count);
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" id count = " + count + " @EOF\r\n");
            }
            ois.close();
            getLog().info(" id count = " + count + "\r\n");

            // Sort by [COMPONENTID]
            Collections.sort(aRs);
            int aRsMax = aRs.size();

            // ATTACH ENVELOPE CONCEPTS (3 PASS)
            // *** CONCEPTS ***
            int idxRsA = 0;
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fNameStep4Con)));
            try {
                count = 0;
                obj = ois.readObject();
                while (obj != null && idxRsA < aRsMax) {
                    SctYRefSetRecord rsRec = aRs.get(idxRsA);
                    SctYConRecord conRec = (SctYConRecord) obj;
                    int rsVin = compareMsbLsb(rsRec.componentUuidMsb, rsRec.componentUuidLsb,
                            conRec.conUuidMsb, conRec.conUuidLsb);

                    if (rsVin == 0) {
                        rsRec.conUuidMsb = conRec.conUuidMsb;
                        rsRec.conUuidLsb = conRec.conUuidLsb;
                        rsRec.componentType = SctYRefSetRecord.ComponentType.CONCEPT;
                        idxRsA++;
                    } else if (rsVin > 0) {
                        obj = ois.readObject();
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" concept count = " + count);
                    } else {
                        idxRsA++;
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" concept count = " + count + " @EOF\r\n");
            }
            ois.close();
            getLog().info(" concept count = " + count + "\r\n");

            // *** DESCRIPTIONS ***
            ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(fNameStep4Desc)));
            try {
                count = 0;
                idxRsA = 0;
                obj = ois.readObject();
                while (obj != null && idxRsA < aRsMax) {
                    SctYRefSetRecord rsRec = aRs.get(idxRsA);
                    SctYDesRecord desRec = (SctYDesRecord) obj;

                    // :DEBUG:!!!:
                    //                    UUID debugThis = new UUID(rsRec.componentUuidMsb, rsRec.componentUuidLsb);
                    //                    UUID debugThat = new UUID(desRec.desUuidMsb, desRec.desUuidLsb);
                    //                    if (UUID.fromString("7c57f6b4-4a63-52ad-b762-73acc15f23de").equals(debugThis)) 
                    //                        getLog().info("FOUND THIS");
                    //                    if (UUID.fromString("7c57f6b4-4a63-52ad-b762-73acc15f23de").equals(debugThat)) 
                    //                        getLog().info("FOUND THAT");

                    int rsVin = compareMsbLsb(rsRec.componentUuidMsb, rsRec.componentUuidLsb,
                            desRec.desUuidMsb, desRec.desUuidLsb);

                    if (rsVin == 0) {
                        if (rsRec.conUuidMsb != Long.MAX_VALUE)
                            getLog().info(
                                    "ERROR: Refset Envelop UUID Concept/Description conflict"
                                            + "\r\nExisting UUID:"
                                            + new UUID(rsRec.conUuidMsb, rsRec.conUuidLsb)
                                            + "\r\nDescription UUID:"
                                            + new UUID(desRec.desUuidMsb, desRec.desUuidLsb));

                        rsRec.conUuidMsb = desRec.conUuidMsb;
                        rsRec.conUuidLsb = desRec.conUuidLsb;
                        rsRec.componentType = SctYRefSetRecord.ComponentType.DESCRIPTION;
                        idxRsA++;
                    } else if (rsVin > 0) {
                        obj = ois.readObject();
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" description count = " + count);
                    } else {
                        idxRsA++;
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" description count = " + count + " @EOF\r\n");
            }
            ois.close();
            getLog().info(" description count = " + count + "\r\n");

            // *** RELATIONSHIPS ***
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fNameStep4Rel)));
            try {
                count = 0;
                idxRsA = 0;
                obj = ois.readObject();
                while (obj != null && idxRsA < aRsMax) {
                    SctYRefSetRecord rsRec = aRs.get(idxRsA);
                    SctYRelRecord relRec = (SctYRelRecord) obj;
                    int rsVin = compareMsbLsb(rsRec.componentUuidMsb, rsRec.componentUuidLsb,
                            relRec.relUuidMsb, relRec.relUuidLsb);

                    if (rsVin == 0) {
                        if (rsRec.conUuidMsb != Long.MAX_VALUE)
                            getLog().info(
                                    "ERROR: Refset Envelop UUID Concept/Relationship conflict"
                                            + "\r\nExisting UUID:"
                                            + new UUID(rsRec.conUuidMsb, rsRec.conUuidLsb)
                                            + "\r\nRelationship UUID:"
                                            + new UUID(relRec.relUuidMsb, relRec.relUuidLsb));

                        rsRec.conUuidMsb = relRec.c1UuidMsb;
                        rsRec.conUuidLsb = relRec.c1UuidLsb;
                        rsRec.componentType = SctYRefSetRecord.ComponentType.RELATIONSHIP;
                        idxRsA++;
                    } else if (rsVin > 0) {
                        obj = ois.readObject();
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" relationship count = " + count);
                    } else {
                        idxRsA++;
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" relationships count = " + count + " @ eof\r\n");
            }
            ois.close();
            getLog().info(" relationships count = " + count + "\r\n");

            // *** MEMBERS WHICH ARE REFSET CONCEPTS ***
            ArrayList<SctYRefSetRecord> bRs = new ArrayList<SctYRefSetRecord>(aRs);
            Comparator<SctYRefSetRecord> compRsByRs = new Comparator<SctYRefSetRecord>() {
                public int compare(SctYRefSetRecord o1, SctYRefSetRecord o2) {
                    int thisMore = 1;
                    int thisLess = -1;
                    // CONCEPTID
                    if (o1.refsetUuidMsb > o2.refsetUuidMsb) {
                        return thisMore;
                    } else if (o1.refsetUuidMsb < o2.refsetUuidMsb) {
                        return thisLess;
                    } else {
                        if (o1.refsetUuidLsb > o2.refsetUuidLsb) {
                            return thisMore;
                        } else if (o1.refsetUuidLsb < o2.refsetUuidLsb) {
                            return thisLess;
                        } else {
                            // Path
                            if (o1.yPath > o2.yPath) {
                                return thisMore;
                            } else if (o1.yPath < o2.yPath) {
                                return thisLess;
                            } else {
                                // Revision
                                if (o1.yRevision > o2.yRevision) {
                                    return thisMore;
                                } else if (o1.yRevision < o2.yRevision) {
                                    return thisLess;
                                } else {
                                    return 0; // EQUAL
                                }
                            }
                        }
                    }
                } // compare()
            };
            Collections.sort(bRs, compRsByRs);

            count = 0;
            idxRsA = 0;
            int idxRsB = 0;
            while (idxRsA < aRsMax && idxRsB < aRsMax) {
                SctYRefSetRecord rsRecA = aRs.get(idxRsA);
                SctYRefSetRecord rsRecB = bRs.get(idxRsB);
                int rsVin = compareMsbLsb(rsRecA.componentUuidMsb, rsRecA.componentUuidLsb,
                        rsRecB.refsetUuidMsb, rsRecB.refsetUuidLsb);

                if (rsVin == 0) {
                    if (rsRecA.conUuidMsb != Long.MAX_VALUE)
                        getLog().info(
                                "ERROR: Refset Envelop UUID Concept/Refset conflict"
                                        + "\r\nExisting UUID:"
                                        + new UUID(rsRecA.conUuidMsb, rsRecA.conUuidLsb)
                                        + "\r\nRefset UUID:"
                                        + new UUID(rsRecB.refsetUuidMsb, rsRecB.refsetUuidLsb));

                    rsRecA.conUuidMsb = rsRecB.refsetUuidMsb;
                    rsRecA.conUuidLsb = rsRecB.refsetUuidLsb;
                    rsRecA.componentType = SctYRefSetRecord.ComponentType.MEMBER;
                    idxRsA++;
                } else if (rsVin > 0) {
                    idxRsB++;
                    count++;
                    if (count % 100000 == 0)
                        getLog().info(" refset count = " + count);
                } else {
                    idxRsA++;
                }
            }

            // SAVE FILE SORTED BY "ENVELOP CONCEPTS" UUID
            Comparator<SctYRefSetRecord> compRsByCon = new Comparator<SctYRefSetRecord>() {
                public int compare(SctYRefSetRecord o1, SctYRefSetRecord o2) {
                    int thisMore = 1;
                    int thisLess = -1;
                    // CONCEPTID
                    if (o1.conUuidMsb > o2.conUuidMsb) {
                        return thisMore;
                    } else if (o1.conUuidMsb < o2.conUuidMsb) {
                        return thisLess;
                    } else {
                        if (o1.conUuidLsb > o2.conUuidLsb) {
                            return thisMore;
                        } else if (o1.conUuidLsb < o2.conUuidLsb) {
                            return thisLess;
                        } else {
                            // Path
                            if (o1.yPath > o2.yPath) {
                                return thisMore;
                            } else if (o1.yPath < o2.yPath) {
                                return thisLess;
                            } else {
                                // Revision
                                if (o1.yRevision > o2.yRevision) {
                                    return thisMore;
                                } else if (o1.yRevision < o2.yRevision) {
                                    return thisLess;
                                } else {
                                    return 0; // EQUAL
                                }
                            }
                        }
                    }
                } // compare()
            };
            Collections.sort(aRs, compRsByCon);
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(fNameStep5RsByCon)));
            for (SctYRefSetRecord r : aRs)
                oos.writeUnshared(r);
            oos.flush();
            oos.close();

            // SAVE FILE SORTED BY REFSET UUID
            Collections.sort(aRs, compRsByRs);
            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep5RsByRs)));
            for (SctYRefSetRecord r : aRs)
                oos.writeUnshared(r);
            oos.flush();
            oos.close();

            // :!!!:NYI: check to see if any refset member remained unassigned. 

            aRs = null;
            System.gc();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        getLog().info(
                "*** MASTER SORT TIME: " + ((System.currentTimeMillis() - start) / 1000)
                        + " seconds");
        getLog().info("*** SctSiToEConcept Step #5 COMPLETED -- REFSET PREPARATION ***\r\n");
    }

    private int compareMsbLsb(long aMsb, long aLsb, long bMsb, long bLsb) {
        int thisMore = 1;
        int thisLess = -1;
        if (aMsb < bMsb) {
            return thisLess; // instance less than received
        } else if (aMsb > bMsb) {
            return thisMore; // instance greater than received
        } else {
            if (aLsb < bLsb) {
                return thisLess;
            } else if (aLsb > bLsb) {
                return thisMore;
            } else {
                return 0; // instance == received
            }
        }
    }

    // :!!!:NYI: concepts may not need to be sorted again after previous step.
    private void executeMojoStep6() throws MojoFailureException {
        getLog().info("*** SctSiToEConcept Step #6 BEGINNING -- SORT BY CONCEPT ***");
        long start = System.currentTimeMillis();
        try {

            // *** CONCEPTS ***
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(fNameStep4Con)));
            ArrayList<SctYConRecord> aCon = new ArrayList<SctYConRecord>();

            int count = 0;
            Object obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof SctYConRecord) {
                        aCon.add((SctYConRecord) obj);
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" concept count = " + count);
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" concept count = " + count + " @EOF\r\n");
            }
            ois.close();
            getLog().info(" concept count = " + count + "\r\n");

            // SORT BY [CONCEPTID, Path, Revision]
            Comparator<SctYConRecord> compCon = new Comparator<SctYConRecord>() {
                public int compare(SctYConRecord o1, SctYConRecord o2) {
                    int thisMore = 1;
                    int thisLess = -1;
                    // CONCEPTID
                    if (o1.conUuidMsb > o2.conUuidMsb) {
                        return thisMore;
                    } else if (o1.conUuidMsb < o2.conUuidMsb) {
                        return thisLess;
                    } else {
                        if (o1.conUuidLsb > o2.conUuidLsb) {
                            return thisMore;
                        } else if (o1.conUuidLsb < o2.conUuidLsb) {
                            return thisLess;
                        } else {
                            // Path
                            if (o1.yPath > o2.yPath) {
                                return thisMore;
                            } else if (o1.yPath < o2.yPath) {
                                return thisLess;
                            } else {
                                // Revision
                                if (o1.yRevision > o2.yRevision) {
                                    return thisMore;
                                } else if (o1.yRevision < o2.yRevision) {
                                    return thisLess;
                                } else {
                                    return 0; // EQUAL
                                }
                            }
                        }
                    }
                } // compare()
            };
            Collections.sort(aCon, compCon);

            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(fNameStep6Con)));
            for (SctYConRecord r : aCon) {
                oos.writeUnshared(r);
            }
            oos.flush();
            oos.close();
            aCon = null;
            System.gc();

            // *** DESCRIPTIONS ***
            ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(fNameStep4Desc)));
            ArrayList<SctYDesRecord> aDes = new ArrayList<SctYDesRecord>();

            count = 0;
            obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof SctYDesRecord) {
                        aDes.add((SctYDesRecord) obj);
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" description count = " + count);
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" description count = " + count + " @EOF\r\n");
            }
            ois.close();
            getLog().info(" description count = " + count + "\r\n");

            // SORT BY [CONCEPTID, DESCRIPTIONID, Path, Revision]
            Comparator<SctYDesRecord> compDes = new Comparator<SctYDesRecord>() {
                public int compare(SctYDesRecord o1, SctYDesRecord o2) {
                    int thisMore = 1;
                    int thisLess = -1;
                    // CONCEPTID
                    if (o1.conUuidMsb > o2.conUuidMsb) {
                        return thisMore;
                    } else if (o1.conUuidMsb < o2.conUuidMsb) {
                        return thisLess;
                    } else {
                        if (o1.conUuidLsb > o2.conUuidLsb) {
                            return thisMore;
                        } else if (o1.conUuidLsb < o2.conUuidLsb) {
                            return thisLess;
                        } else {
                            // DESCRIPTIONID
                            if (o1.desUuidMsb > o2.desUuidMsb) {
                                return thisMore;
                            } else if (o1.desUuidMsb < o2.desUuidMsb) {
                                return thisLess;
                            } else {
                                if (o1.desUuidLsb > o2.desUuidLsb) {
                                    return thisMore;
                                } else if (o1.desUuidLsb < o2.desUuidLsb) {
                                    return thisLess;
                                } else {
                                    // Path
                                    if (o1.yPath > o2.yPath) {
                                        return thisMore;
                                    } else if (o1.yPath < o2.yPath) {
                                        return thisLess;
                                    } else {
                                        // Revision
                                        if (o1.yRevision > o2.yRevision) {
                                            return thisMore;
                                        } else if (o1.yRevision < o2.yRevision) {
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
            Collections.sort(aDes, compDes);

            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep6Desc)));
            for (SctYDesRecord r : aDes)
                oos.writeUnshared(r);

            oos.flush();
            oos.close();
            aDes = null;
            System.gc();

            // *** RELATIONSHIPS ***
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fNameStep4Rel)));
            ArrayList<SctYRelRecord> aRel = new ArrayList<SctYRelRecord>();

            count = 0;
            obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof SctYRelRecord) {
                        aRel.add((SctYRelRecord) obj);
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" relationships count = " + count);
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" relationships count = " + count + "\r\n");
            }
            ois.close();
            getLog().info(" relationships count = " + count + " @EOF\r\n");

            // SORT BY [C1-Group-RoleType-Path-RevisionVersion]
            Comparator<SctYRelRecord> compRel = new Comparator<SctYRelRecord>() {
                public int compare(SctYRelRecord o1, SctYRelRecord o2) {
                    int thisMore = 1;
                    int thisLess = -1;
                    // C1
                    if (o1.c1UuidMsb > o2.c1UuidMsb) {
                        return thisMore;
                    } else if (o1.c1UuidMsb < o2.c1UuidMsb) {
                        return thisLess;
                    } else {
                        if (o1.c1UuidLsb > o2.c1UuidLsb) {
                            return thisMore;
                        } else if (o1.c1UuidLsb < o2.c1UuidLsb) {
                            return thisLess;
                        } else {
                            // GROUP
                            if (o1.group > o2.group) {
                                return thisMore;
                            } else if (o1.group < o2.group) {
                                return thisLess;
                            } else {
                                // ROLE TYPE
                                if (o1.roleTypeIdx > o2.roleTypeIdx) {
                                    return thisMore;
                                } else if (o1.roleTypeIdx < o2.roleTypeIdx) {
                                    return thisLess;
                                } else {
                                    // C2
                                    if (o1.c2UuidMsb > o2.c2UuidMsb) {
                                        return thisMore;
                                    } else if (o1.c2UuidMsb < o2.c2UuidMsb) {
                                        return thisLess;
                                    } else {
                                        if (o1.c2UuidLsb > o2.c2UuidLsb) {
                                            return thisMore;
                                        } else if (o1.c2UuidLsb < o2.c2UuidLsb) {
                                            return thisLess;
                                        } else {
                                            // PATH
                                            if (o1.yPath > o2.yPath) {
                                                return thisMore;
                                            } else if (o1.yPath < o2.yPath) {
                                                return thisLess;
                                            } else {
                                                // VERSION
                                                if (o1.yRevision > o2.yRevision) {
                                                    return thisMore;
                                                } else if (o1.yRevision < o2.yRevision) {
                                                    return thisLess;
                                                } else {
                                                    return 0; // EQUAL
                                                }
                                            }
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
                    fNameStep6Rel)));
            for (SctYRelRecord r : aRel)
                oos.writeUnshared(r);
            oos.flush();
            oos.close();
            aRel = null;

            // ** DESTINATION RELATIONSHIPS **
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                    fNameStep3RelDest)));
            ArrayList<SctYRelDestRecord> aRelDest = new ArrayList<SctYRelDestRecord>();

            count = 0;
            obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof SctYRelDestRecord) {
                        aRelDest.add((SctYRelDestRecord) obj);
                        count++;
                        if (count % 100000 == 0)
                            getLog().info(" destination relationships count = " + count);
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" destination relationships count = " + count + "\r\n");
            }
            ois.close();
            getLog().info(" destination relationships count = " + count + " @EOF\r\n");

            // SORT BY [C2-RoleType]
            Comparator<SctYRelDestRecord> compRelDest = new Comparator<SctYRelDestRecord>() {
                public int compare(SctYRelDestRecord o1, SctYRelDestRecord o2) {
                    int thisMore = 1;
                    int thisLess = -1;
                    // C2
                    if (o1.c2UuidMsb > o2.c2UuidMsb) {
                        return thisMore;
                    } else if (o1.c2UuidMsb < o2.c2UuidMsb) {
                        return thisLess;
                    } else {
                        if (o1.c2UuidLsb > o2.c2UuidLsb) {
                            return thisMore;
                        } else if (o1.c2UuidLsb < o2.c2UuidLsb) {
                            return thisLess;
                        } else {
                            // ROLE TYPE
                            if (o1.roleTypeIdx > o2.roleTypeIdx) {
                                return thisMore;
                            } else if (o1.roleTypeIdx < o2.roleTypeIdx) {
                                return thisLess;
                            } else {
                                return 0; // EQUAL
                            }
                        }
                    }
                } // compare()
            };
            Collections.sort(aRelDest, compRelDest);

            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep6RelDest)));
            for (SctYRelDestRecord r : aRelDest) {
                oos.writeUnshared(r);
            }
            oos.flush();
            oos.close();
            aRelDest = null;

            System.gc();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new MojoFailureException("File Not Found -- Step 3");
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("IO Exception -- Step 3");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        getLog().info(
                "*** MASTER SORT TIME: " + ((System.currentTimeMillis() - start) / 1000)
                        + " seconds");
        getLog().info("*** SctSiToEConcept Step #6 COMPLETED -- SORT BY CONCEPT ***\r\n");
    }

    /**
     * executeMojoStep6() reads concepts, descriptions, relationship, 
     * destination relationships, & ids files in concept order.
     * 
     * @throws MojoFailureException
     */
    private void executeMojoStep7() throws MojoFailureException {
        statCon = 0;
        statDes = 0;
        statRel = 0;
        statRelDest = 0;
        statRsByCon = 0;
        statRsByRs = 0;

        // :DEBUG:!!!:
        //        int xyzdebug = 0;
        //        for (UUID u : yPathArray)
        //            getLog().info("PATH UUID :: " + u + " #=" + xyzdebug++);

        getLog().info("*** SctSiToEConcept STEP #7 BEGINNING -- CREATE eCONCEPTS ***");
        long start = System.currentTimeMillis();
        countEConWritten = 0;

        // Lists hold records for the immediate operations 
        ArrayList<SctYConRecord> conList = new ArrayList<SctYConRecord>();
        ArrayList<SctYDesRecord> desList = new ArrayList<SctYDesRecord>();
        ArrayList<SctYRelRecord> relList = new ArrayList<SctYRelRecord>();
        ArrayList<SctYRelDestRecord> relDestList = new ArrayList<SctYRelDestRecord>();
        ArrayList<SctYRefSetRecord> rsByConList = new ArrayList<SctYRefSetRecord>();
        ArrayList<SctYRefSetRecord> rsByRsList = new ArrayList<SctYRefSetRecord>();

        // Since readObject must look one record ahead,
        // the look ahead record is stored as "Next"
        SctYConRecord conNext = null;
        SctYDesRecord desNext = null;
        SctYRelRecord relNext = null;
        SctYRelDestRecord relDestNext = null;
        SctYRefSetRecord rsByConNext = null;
        SctYRefSetRecord rsByRsNext = null;

        // Open Input and Output Streams
        ObjectInputStream oisCon = null;
        ObjectInputStream oisDes = null;
        ObjectInputStream oisRel = null;
        ObjectInputStream oisRelDest = null;
        ObjectInputStream oisRsByCon = null;
        ObjectInputStream oisRsByRs = null;
        DataOutputStream dos = null;
        try {
            oisCon = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                    fNameStep6Con)));
            oisDes = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                    fNameStep6Desc)));
            oisRel = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                    fNameStep6Rel)));
            oisRelDest = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                    fNameStep6RelDest)));
            oisRsByCon = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                    fNameStep5RsByCon)));
            oisRsByRs = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                    fNameStep5RsByRs)));
            dos = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(fNameStep7ECon)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new MojoFailureException("File Not Found -- Step #7");
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("IO Exception -- Step #7");
        }

        int countCon = 0;
        int countDes = 0;
        int countRel = 0;
        int countRelDest = 0;
        int countRsByCon = 0;
        int countRsByRs = 0;
        boolean notDone = true;
        UUID theCon;
        UUID theDes = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);
        UUID theRel = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);
        UUID theRelDest = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);
        UUID theRsByCon = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);
        UUID theRsByRs = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);
        UUID prevCon = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);
        UUID prevDes = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);
        UUID prevRel = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);
        UUID prevRelDest = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);
        UUID prevRsByCon = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);
        UUID prevRsByRs = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);
        while (notDone) {
            // Get next Concept record(s) for 1 id.
            conNext = readNextCon(oisCon, conList, conNext);
            SctYConRecord tmpConRec = conList.get(0);
            theCon = new UUID(tmpConRec.conUuidMsb, tmpConRec.conUuidLsb);
            countCon++;

            while (theDes.compareTo(theCon) == IS_LESS) {
                desNext = readNextDes(oisDes, desList, desNext);
                if (desNext == null && desList.size() == 0)
                    theDes = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
                else {
                    SctYDesRecord tmpDes = desList.get(0);
                    theDes = new UUID(tmpDes.conUuidMsb, tmpDes.conUuidLsb);
                    countDes++;
                    if (theDes.compareTo(theCon) == IS_LESS)
                        getLog().info("ORPHAN DESCRIPTION :: " + desList.get(0).termText);
                }
            }

            while (theRel.compareTo(theCon) == IS_LESS) {
                relNext = readNextRel(oisRel, relList, relNext);
                if (relNext == null && relList.size() == 0)
                    theRel = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
                else {
                    // theRel = relList.get(0).c1SnoId;
                    SctYRelRecord tmpRel = relList.get(0);
                    theRel = new UUID(tmpRel.c1UuidMsb, tmpRel.c1UuidLsb);
                    countRel++;
                    if (theRel.compareTo(theCon) == IS_LESS)
                        getLog().info(
                                "ORPHAN RELATIONSHIP :: relid=" + relList.get(0).relSnoId + " c1=="
                                        + relList.get(0).c1SnoId);
                }
            }

            while (theRelDest.compareTo(theCon) == IS_LESS) {
                relDestNext = readNextRelDest(oisRelDest, relDestList, relDestNext);
                if (relDestNext == null && relDestList.size() == 0)
                    theRelDest = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
                else {
                    // theRelDest = relDestList.get(0).c2SnoId;
                    SctYRelDestRecord tmpRelDest = relDestList.get(0);
                    theRelDest = new UUID(tmpRelDest.c2UuidMsb, tmpRelDest.c2UuidLsb);
                    countRelDest++;
                    if (theRelDest.compareTo(theCon) == IS_LESS)
                        getLog().info(
                                "ORPHAN DEST. RELATIONSHIP :: relid="
                                        + relList.get(0).relSnoId
                                        + " c2=="
                                        + new UUID(relDestList.get(0).c2UuidMsb,
                                                relDestList.get(0).c2UuidLsb));
                }
            }

            while (theRsByCon.compareTo(theCon) == IS_LESS) {
                rsByConNext = readNextRsByCon(oisRsByCon, rsByConList, rsByConNext);
                // :DEBUG:
                //                UUID debugUuidRsByCon111 = UUID.fromString("ccbd4a65-9b1a-5df3-94d1-4a1085f3c758");
                //                long db111Msb = debugUuidRsByCon111.getMostSignificantBits();
                //                long db111Lsb = debugUuidRsByCon111.getLeastSignificantBits();
                //                if ((rsByConNext.conUuidMsb == db111Msb && rsByConNext.conUuidLsb == db111Lsb)
                //                        || (rsByConNext.componentUuidMsb == db111Msb && rsByConNext.componentUuidLsb == db111Lsb)
                //                        || (rsByConNext.memberUuidMsb == db111Msb && rsByConNext.memberUuidLsb == db111Lsb)
                //                        || (rsByConNext.refsetUuidMsb == db111Msb && rsByConNext.refsetUuidLsb == db111Lsb)) {
                //                    getLog().info("FOUND IT");
                //
                //                }
                if (rsByConNext == null && rsByConList.size() == 0)
                    theRsByCon = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
                else {
                    SctYRefSetRecord tmpRsByCon = rsByConList.get(0);
                    theRsByCon = new UUID(tmpRsByCon.conUuidMsb, tmpRsByCon.conUuidLsb);
                    countRsByCon++;
                    if (theRsByCon.compareTo(theCon) == IS_LESS)
                        getLog().info(
                                "ORPHAN REFSET MEMBER RECORD_A :: "
                                        + new UUID(rsByConList.get(0).memberUuidMsb, rsByConList
                                                .get(0).memberUuidLsb));
                }
            }

            while (theRsByRs.compareTo(theCon) == IS_LESS) {
                rsByRsNext = readNextRsByRs(oisRsByRs, rsByRsList, rsByRsNext);
                // :DEBUG:
                //                UUID debugUuidRsByCon111 = UUID.fromString("ccbd4a65-9b1a-5df3-94d1-4a1085f3c758");

                if (rsByRsNext == null && rsByRsList.size() == 0)
                    theRsByRs = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
                else {
                    SctYRefSetRecord tmpRsByRs = rsByRsList.get(0);
                    theRsByRs = new UUID(tmpRsByRs.refsetUuidMsb, tmpRsByRs.refsetUuidLsb);
                    countRsByRs++;
                    if (theRsByRs.compareTo(theCon) == IS_LESS)
                        getLog().info(
                                "ORPHAN REFSET MEMBER RECORD_B :: "
                                        + new UUID(rsByRsList.get(0).memberUuidMsb, rsByRsList
                                                .get(0).memberUuidLsb));
                }
            }

            // Check for next sync
            if (theCon.compareTo(theDes) != IS_EQUAL || theCon.compareTo(theRel) != IS_EQUAL /*|| theCon != theRelDest*/) {
                getLog().info("CONFIRM: ROOT CONCEPT ");
                UUID uuid = new UUID(conList.get(0).conUuidMsb, conList.get(0).conUuidLsb);
                getLog().info(" -is- concept SNOMED id =" + uuid.toString());
                getLog().info(" -is- concept counter #" + countCon);
                getLog().info(" -is- description \"" + desList.get(0).termText + "\"\r\n");
                getLog().info(
                        " ...prev... " + prevCon + " " + prevDes + " " + prevRel + " "
                                + prevRelDest);
                getLog().info(
                        " ...-is-... " + theCon + " " + theDes + " " + theRel + " " + theRelDest);
                String cnStr = "*null*";
                if (conNext != null) {
                    uuid = new UUID(conNext.conUuidMsb, conNext.conUuidLsb);
                    cnStr = uuid.toString();
                }
                String dnStr = "*null*";
                if (desNext != null) {
                    uuid = new UUID(desNext.conUuidMsb, desNext.conUuidLsb);
                    dnStr = uuid.toString();
                }
                String rnStr = "*null*";
                if (relNext != null) {
                    uuid = new UUID(relNext.c1UuidMsb, relNext.c1UuidLsb);
                    rnStr = uuid.toString();
                }
                String rdnStr = "*null*";
                if (relDestNext != null) {
                    uuid = new UUID(relDestNext.c2UuidMsb, relDestNext.c2UuidLsb);
                    rdnStr = uuid.toString();
                }
                getLog().info(
                        " ..\"next\".. " + cnStr + " " + dnStr + " " + rnStr + " " + rdnStr
                                + "\r\n");
            }

            ArrayList<SctYRefSetRecord> addRsByCon = null;
            if (theCon.compareTo(theRsByCon) == IS_EQUAL)
                addRsByCon = rsByConList;
            ArrayList<SctYRefSetRecord> addRsByRs = null;
            if (theCon.compareTo(theRsByRs) == IS_EQUAL)
                addRsByRs = rsByRsList;

            if (theCon.compareTo(theDes) == IS_EQUAL && theCon.compareTo(theRel) == IS_EQUAL
                    && theCon.compareTo(theRelDest) == IS_EQUAL) {
                // MIDDLE CASE theCon ==theDes ==theRel ==theRelDest
                createEConcept(conList, desList, relList, relDestList, addRsByCon, addRsByRs, dos);
            } else if (theCon.compareTo(theDes) == IS_EQUAL && theCon.compareTo(theRel) != IS_EQUAL
                    && theCon.compareTo(theRelDest) == IS_EQUAL) {
                // TOP CASE  theCon ==theDes !=theRel ==theRelDest
                createEConcept(conList, desList, null, relDestList, addRsByCon, addRsByRs, dos);
            } else if (theCon.compareTo(theDes) == IS_EQUAL && theCon.compareTo(theRel) == IS_EQUAL
                    && theCon.compareTo(theRelDest) != IS_EQUAL) {
                // BOTTOM CASE theCon ==theDes ==theRel !=theRelDest
                createEConcept(conList, desList, relList, null, addRsByCon, addRsByRs, dos);
            } else if (theCon.compareTo(theDes) == IS_EQUAL && theCon.compareTo(theRel) != IS_EQUAL
                    && theCon.compareTo(theRelDest) != IS_EQUAL) {
                // UNCONNECTED CONCEPT theCon ==theDes !=theRel !=theRelDest
                createEConcept(conList, desList, null, null, addRsByCon, addRsByRs, dos);
            } else {
                if (debug) {
                    getLog().info(
                            "--- Case what case is this??? -- Step 4" + " theCon=\t" + theCon
                                    + "\ttheDes=\t" + theDes + "\ttheRel=\t" + theRel
                                    + "\ttheRelDest\t" + theRelDest);
                    getLog().info("--- --- concept SNOMED id =" + theCon);
                    getLog().info("--- --- concept counter   #" + countCon);
                    getLog().info("--- --- description       \"" + desList.get(0).termText + "\"");
                    getLog().info("--- \r\n");
                }
                throw new MojoFailureException("Case not implemented -- executeMojoStep6()");
            }

            if (conNext == null && desNext == null && relNext == null)
                notDone = false;

            prevCon = theCon;
            prevDes = theDes;
            prevRel = theRel;
            prevRelDest = theRelDest;
            prevRsByCon = theRsByCon;
            prevRsByRs = theRsByRs;

        }
        getLog().info(
                "RECORD COUNT = " + countCon + "(Con) " + countDes + "(Des) " + countRel + "(Rel)");
        getLog().info(
                "COMPONENT COUNT = " + statCon + "(statCon) " + statDes + "(statDes) " + statRel
                        + "(statRel)");
        getLog().info(
                "INDEX COUNT = " + statRelDest + "(statRelDest) " + statRsByCon + "(statRsByCon) "
                        + statRsByRs + "(statRsByRs)");
        getLog().info(
                "REFSET COUNT = " + countRsByCon + "(countRsByCon) " + countRsByRs
                        + "(countRsByRs) ");

        // CLOSE FILES
        try {
            oisCon.close();
            oisDes.close();
            oisRel.close();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("IO Exception -- Step 4, closing files");
        }
        getLog().info(
                "*** ECONCEPT CREATION TIME: " + ((System.currentTimeMillis() - start) / 1000)
                        + " seconds");
        getLog().info("*** ECONCEPTS WRITTEN TO FILE = " + countEConWritten);
        getLog().info("*** SctSiToEConcept STEP #7 COMPLETED -- CREATE eCONCEPTS ***\r\n");
    }

    private void createEConcept(ArrayList<SctYConRecord> conList, ArrayList<SctYDesRecord> desList,
            ArrayList<SctYRelRecord> relList, ArrayList<SctYRelDestRecord> relDestList,
            ArrayList<SctYRefSetRecord> rsByConList, ArrayList<SctYRefSetRecord> rsByRsList,
            DataOutputStream dos) throws MojoFailureException {
        if (conList.size() < 1)
            throw new MojoFailureException("createEConcept(), empty conList");

        statCon++;
        if (desList != null)
            statDes += desList.size();
        if (relList != null)
            statRel += relList.size();
        if (relDestList != null)
            statRelDest += relDestList.size();
        if (rsByConList != null)
            statRsByCon += rsByConList.size();
        if (rsByRsList != null)
            statRsByRs += rsByRsList.size();

        Collections.sort(conList);
        SctYConRecord cRec0 = conList.get(0);
        UUID theConUUID = new UUID(cRec0.conUuidMsb, cRec0.conUuidLsb);

        // :DEBUG:
        //        if (theConUUID.equals(UUID.fromString("8857ca3c-eeed-57e9-ba25-5d7f3a4ba160"))) 
        //            getLog().info("FOUND IT");

        EConcept ec = new EConcept();
        ec.setPrimordialUuid(theConUUID);

        // ADD CONCEPT ATTRIBUTES        
        EConceptAttributes ca = new EConceptAttributes();
        ca.primordialUuid = theConUUID;
        ca.setDefined(cRec0.isprimitive == 0 ? true : false);

        ArrayList<TkIdentifier> tmpAdditionalIds = new ArrayList<TkIdentifier>();

        // SNOMED ID, if present
        if (cRec0.conSnoId < Long.MAX_VALUE) {
            EIdentifierLong cid = new EIdentifierLong();
            cid.setAuthorityUuid(uuidSourceSnomedInteger);
            cid.setDenotation(cRec0.conSnoId);
            cid.setPathUuid(yPathArray[cRec0.yPath]);
            cid.setStatusUuid(uuidCurrent);
            cid.setTime(yRevDateArray[cRec0.yRevision]);
            tmpAdditionalIds.add(cid);
        }
        // CTV 3 ID, if present
        if (cRec0.ctv3id != null) {
            EIdentifierString cids = new EIdentifierString();
            cids.setAuthorityUuid(uuidSourceCtv3);
            cids.setDenotation(cRec0.ctv3id);
            cids.setPathUuid(yPathArray[cRec0.yPath]);
            cids.setStatusUuid(uuidCurrent);
            cids.setTime(yRevDateArray[cRec0.yRevision]);
            tmpAdditionalIds.add(cids);
        }
        // SNOMED RT ID, if present
        if (cRec0.snomedrtid != null) {
            EIdentifierString cids = new EIdentifierString();
            cids.setAuthorityUuid(uuidSourceSnomedRt);
            cids.setDenotation(cRec0.snomedrtid);
            cids.setPathUuid(yPathArray[cRec0.yPath]);
            cids.setStatusUuid(uuidCurrent);
            cids.setTime(yRevDateArray[cRec0.yRevision]);
            tmpAdditionalIds.add(cids);
        }
        if (cRec0.addedIds != null) {
            for (SctYIdRecord eId : cRec0.addedIds)
                tmpAdditionalIds.add(createEIdentifier(eId));
        }

        if (tmpAdditionalIds.size() > 0)
            ca.additionalIds = tmpAdditionalIds;
        else
            ca.additionalIds = null;

        ca.setStatusUuid(yStatusUuidArray[cRec0.status]);
        ca.setPathUuid(yPathArray[cRec0.yPath]);
        ca.setTime(yRevDateArray[cRec0.yRevision]); // long

        int max = conList.size();
        List<TkConceptAttributesRevision> caRevisions = new ArrayList<TkConceptAttributesRevision>();
        for (int i = 1; i < max; i++) {
            EConceptAttributesRevision rev = new EConceptAttributesRevision();
            SctYConRecord cRec = conList.get(i);
            rev.setDefined(cRec.isprimitive == 0 ? true : false);
            rev.setStatusUuid(yStatusUuidArray[cRec.status]);
            rev.setPathUuid(yPathArray[cRec.yPath]);
            rev.setTime(yRevDateArray[cRec.yRevision]);
            caRevisions.add(rev);
        }

        if (caRevisions.size() > 0)
            ca.revisions = caRevisions;
        else
            ca.revisions = null;
        ec.setConceptAttributes(ca);

        // ADD DESCRIPTIONS
        if (desList != null) {
            Collections.sort(desList);
            List<TkDescription> eDesList = new ArrayList<TkDescription>();
            // long theDesId = Long.MIN_VALUE;
            long theDesMsb = Long.MIN_VALUE;
            long theDesLsb = Long.MIN_VALUE;
            EDescription des = null;
            List<TkDescriptionRevision> revisions = new ArrayList<TkDescriptionRevision>();
            for (SctYDesRecord dRec : desList) {
                if (dRec.desUuidMsb != theDesMsb || dRec.desUuidLsb != theDesLsb) {
                    // CLOSE OUT OLD RELATIONSHIP
                    if (des != null) {
                        if (revisions.size() > 0) {
                            des.revisions = revisions;
                            revisions = new ArrayList<TkDescriptionRevision>();
                        }
                        eDesList.add(des);
                    }

                    // CREATE NEW DESCRIPTION
                    des = new EDescription();

                    ArrayList<TkIdentifier> tmpDesAdditionalIds = new ArrayList<TkIdentifier>();
                    if (dRec.desSnoId < Long.MAX_VALUE) {
                        EIdentifierLong did = new EIdentifierLong();
                        did.setAuthorityUuid(uuidSourceSnomedInteger);
                        did.setDenotation(dRec.desSnoId);
                        did.setPathUuid(yPathArray[dRec.yPath]);
                        did.setStatusUuid(uuidCurrent);
                        did.setTime(yRevDateArray[dRec.yRevision]);
                        tmpDesAdditionalIds.add(did);
                    }
                    if (dRec.addedIds != null) {
                        for (SctYIdRecord eId : dRec.addedIds)
                            tmpDesAdditionalIds.add(createEIdentifier(eId));
                    }
                    if (tmpDesAdditionalIds.size() > 0)
                        des.additionalIds = tmpDesAdditionalIds;
                    else
                        des.additionalIds = null;

                    theDesMsb = dRec.desUuidMsb;
                    theDesLsb = dRec.desUuidLsb;
                    des.setPrimordialComponentUuid(new UUID(theDesMsb, theDesLsb));
                    des.setConceptUuid(theConUUID);
                    des.setText(dRec.termText);
                    des.setInitialCaseSignificant(dRec.capStatus == 1 ? true : false);
                    des.setLang(dRec.languageCode);
                    des.setTypeUuid(yDesTypeUuidArray[dRec.descriptionType]);
                    des.setStatusUuid(yStatusUuidArray[dRec.status]);
                    des.setPathUuid(yPathArray[dRec.yPath]);
                    des.setTime(yRevDateArray[dRec.yRevision]);
                    des.revisions = null;
                } else {
                    EDescriptionRevision edv = new EDescriptionRevision();
                    edv.setText(dRec.termText);
                    edv.setTypeUuid(yDesTypeUuidArray[dRec.descriptionType]);
                    edv.setInitialCaseSignificant(dRec.capStatus == 1 ? true : false);
                    edv.setLang(dRec.languageCode);
                    edv.setStatusUuid(yStatusUuidArray[dRec.status]);
                    edv.setPathUuid(yPathArray[dRec.yPath]);
                    edv.setTime(yRevDateArray[dRec.yRevision]);
                    revisions.add(edv);
                }
            }
            if (des != null && revisions.size() > 0)
                des.revisions = revisions;
            eDesList.add(des);
            ec.setDescriptions(eDesList);
        }

        // ADD RELATIONSHIPS
        if (relList != null) {
            Collections.sort(relList);
            List<TkRelationship> eRelList = new ArrayList<TkRelationship>();
            long theRelMsb = Long.MIN_VALUE;
            long theRelLsb = Long.MIN_VALUE;
            ERelationship rel = null;
            List<TkRelationshipRevision> revisions = new ArrayList<TkRelationshipRevision>();
            for (SctYRelRecord rRec : relList) {
                if (rRec.relUuidMsb != theRelMsb || rRec.relUuidLsb != theRelLsb) {
                    // CLOSE OUT OLD RELATIONSHIP
                    if (rel != null) {
                        if (revisions.size() > 0) {
                            rel.revisions = revisions;
                            revisions = new ArrayList<TkRelationshipRevision>();
                        }
                        eRelList.add(rel);
                    }

                    // CREATE NEW RELATIONSHIP
                    rel = new ERelationship();

                    ArrayList<TkIdentifier> tmpRelAdditionalIds = new ArrayList<TkIdentifier>();
                    if (rRec.relSnoId < Long.MAX_VALUE) {
                        EIdentifierLong rid = new EIdentifierLong();
                        rid.setAuthorityUuid(uuidSourceSnomedInteger);
                        rid.setDenotation(rRec.relSnoId);
                        rid.setPathUuid(yPathArray[rRec.yPath]);
                        rid.setStatusUuid(uuidCurrent);
                        rid.setTime(yRevDateArray[rRec.yRevision]);
                        tmpRelAdditionalIds.add(rid);
                    }
                    if (rRec.addedIds != null) {
                        for (SctYIdRecord eId : rRec.addedIds)
                            tmpRelAdditionalIds.add(createEIdentifier(eId));
                    }
                    if (tmpRelAdditionalIds.size() > 0)
                        rel.additionalIds = tmpRelAdditionalIds;
                    else
                        rel.additionalIds = null;

                    theRelMsb = rRec.relUuidMsb;
                    theRelLsb = rRec.relUuidLsb;
                    rel.setPrimordialComponentUuid(new UUID(theRelMsb, theRelLsb));
                    rel.setC1Uuid(theConUUID);
                    rel.setC2Uuid(new UUID(rRec.c2UuidMsb, rRec.c2UuidLsb));
                    rel.setTypeUuid(lookupRoleType(rRec.roleTypeIdx));
                    rel.setRelGroup(rRec.group);
                    rel.setCharacteristicUuid(yRelCharArray[rRec.characteristic]);
                    rel.setRefinabilityUuid(yRelRefArray[rRec.refinability]);
                    rel.setStatusUuid(yStatusUuidArray[rRec.status]);
                    if (rRec.characteristic == 0) // 0=DEFINING
                        rel.setPathUuid(yPathArray[rRec.yPath]);
                    else
                        // 1=Qualifier, 2=Historical, 3=Additional
                        rel.setPathUuid(uuidPathSnomedCore);
                    rel.setTime(yRevDateArray[rRec.yRevision]);
                    rel.revisions = null;
                } else {
                    ERelationshipRevision erv = new ERelationshipRevision();
                    erv.setTypeUuid(lookupRoleType(rRec.roleTypeIdx));
                    erv.setRelGroup(rRec.group);
                    erv.setCharacteristicUuid(yRelCharArray[rRec.characteristic]);
                    erv.setRefinabilityUuid(yRelRefArray[rRec.refinability]);
                    erv.setStatusUuid(yStatusUuidArray[rRec.status]);
                    if (rRec.characteristic == 0) // 0=DEFINING
                        erv.setPathUuid(yPathArray[rRec.yPath]);
                    else
                        // 1=Qualifier, 2=Historical, 3=Additional
                        erv.setPathUuid(uuidPathSnomedCore);                    
                    erv.setTime(yRevDateArray[rRec.yRevision]);
                    revisions.add(erv);
                }
            }
            if (rel != null && revisions.size() > 0)
                rel.revisions = revisions;
            eRelList.add(rel);
            ec.setRelationships(eRelList);
        }

        // ADD REFSET INDEX
        if (rsByConList != null && rsByConList.size() > 0) {
            List<UUID> listRefsetUuidMemberUuidForCon = new ArrayList<UUID>();
            List<UUID> listRefsetUuidMemberUuidForDes = new ArrayList<UUID>();
            List<UUID> listRefsetUuidMemberUuidForImage = new ArrayList<UUID>();
            // :!!!:???: review detail on how to handle Refset Members
            List<UUID> listRefsetUuidMemberUuidForRefsetMember = new ArrayList<UUID>();
            List<UUID> listRefsetUuidMemberUuidForRel = new ArrayList<UUID>();

            for (SctYRefSetRecord r : rsByConList) {
                if (r.componentType == SctYRefSetRecord.ComponentType.CONCEPT) {
                    listRefsetUuidMemberUuidForCon.add(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForCon.add(new UUID(r.memberUuidMsb, r.memberUuidLsb));
                } else if (r.componentType == SctYRefSetRecord.ComponentType.DESCRIPTION) {
                    listRefsetUuidMemberUuidForDes.add(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForDes.add(new UUID(r.memberUuidMsb, r.memberUuidLsb));
                } else if (r.componentType == SctYRefSetRecord.ComponentType.IMAGE) {
                    listRefsetUuidMemberUuidForImage
                            .add(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForImage
                            .add(new UUID(r.memberUuidMsb, r.memberUuidLsb));
                } else if (r.componentType == SctYRefSetRecord.ComponentType.MEMBER) {
                    listRefsetUuidMemberUuidForRefsetMember.add(new UUID(r.refsetUuidMsb,
                            r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForRefsetMember.add(new UUID(r.memberUuidMsb,
                            r.memberUuidLsb));
                } else if (r.componentType == SctYRefSetRecord.ComponentType.RELATIONSHIP) {
                    listRefsetUuidMemberUuidForRel.add(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForRel.add(new UUID(r.memberUuidMsb, r.memberUuidLsb));
                } else
                    throw new UnsupportedOperationException("Cannot handle case");
            }

            // :DEBUG:TEST:!!!: ec primordialUuid vs ca primordialUuid
            //            if (countRefsetMember < 10)
            //                getLog().info(
            //                        "Refset Member: " + ec.getConceptAttributes().primordialUuid.toString());
            countRefsetMember++;

        }

        // ADD REFSET MEMBER VALUES
        if (rsByRsList != null && rsByRsList.size() > 0) {
            List<TkRefsetAbstractMember<?>> listErm = new ArrayList<TkRefsetAbstractMember<?>>();

            for (SctYRefSetRecord r : rsByRsList) {

                if (r.valueType == SctYRefSetRecord.ValueType.BOOLEAN) {
                    ERefsetBooleanMember tmp = new ERefsetBooleanMember();
                    tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    tmp.setPrimordialComponentUuid(new UUID(r.memberUuidMsb, r.memberUuidLsb));
                    tmp.setComponentUuid(new UUID(r.componentUuidMsb, r.componentUuidLsb));
                    tmp.setStatusUuid(yStatusUuidArray[r.status]);
                    tmp.setTime(yRevDateArray[r.yRevision]);
                    tmp.setPathUuid(yPathArray[r.yPath]);
                    // :!!!: tmp.setAdditionalIdComponents(additionalIdComponents);
                    // :!!!: tmp.setRevisions(revisions);

                    tmp.setBooleanValue(r.valueBoolean);

                    listErm.add(tmp);
                } else if (r.valueType == SctYRefSetRecord.ValueType.CONCEPT) {
                    ERefsetCidMember tmp = new ERefsetCidMember();
                    tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    tmp.setPrimordialComponentUuid(new UUID(r.memberUuidMsb, r.memberUuidLsb));
                    tmp.setComponentUuid(new UUID(r.componentUuidMsb, r.componentUuidLsb));
                    tmp.setStatusUuid(yStatusUuidArray[r.status]);
                    tmp.setTime(yRevDateArray[r.yRevision]);
                    tmp.setPathUuid(yPathArray[r.yPath]);
                    // :!!!: tmp.setAdditionalIdComponents(additionalIdComponents);
                    // :!!!: tmp.setRevisions(revisions);

                    tmp.setC1Uuid(new UUID(r.valueConUuidMsb, r.valueConUuidLsb));

                    listErm.add(tmp);
                } else if (r.valueType == SctYRefSetRecord.ValueType.INTEGER) {
                    ERefsetIntMember tmp = new ERefsetIntMember();
                    tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    tmp.setPrimordialComponentUuid(new UUID(r.memberUuidMsb, r.memberUuidLsb));
                    tmp.setComponentUuid(new UUID(r.componentUuidMsb, r.componentUuidLsb));
                    tmp.setStatusUuid(yStatusUuidArray[r.status]);
                    tmp.setTime(yRevDateArray[r.yRevision]);
                    tmp.setPathUuid(yPathArray[r.yPath]);
                    // :!!!: tmp.setAdditionalIdComponents(additionalIdComponents);
                    // :!!!: tmp.setRevisions(revisions);

                    tmp.setIntValue(r.valueInt);

                    listErm.add(tmp);
                } else if (r.valueType == SctYRefSetRecord.ValueType.STRING) {
                    ERefsetStrMember tmp = new ERefsetStrMember();
                    tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    tmp.setPrimordialComponentUuid(new UUID(r.memberUuidMsb, r.memberUuidLsb));
                    tmp.setComponentUuid(new UUID(r.componentUuidMsb, r.componentUuidLsb));
                    tmp.setStatusUuid(yStatusUuidArray[r.status]);
                    tmp.setTime(yRevDateArray[r.yRevision]);
                    tmp.setPathUuid(yPathArray[r.yPath]);
                    // :!!!: tmp.setAdditionalIdComponents(additionalIdComponents);
                    // :!!!: tmp.setRevisions(revisions);

                    tmp.setStrValue(r.valueString);

                    listErm.add(tmp);
                } else
                    throw new UnsupportedOperationException("Cannot handle case");

            }
            // :DEBUG:TEST:!!!: ec primordialUuid vs ca primordialUuid
            //            if (countRefsetMaster < 10)
            //                getLog().info("Refset Master: " + ec.getPrimordialUuid().toString());
            countRefsetMaster++;

            ec.setRefsetMembers(listErm);
        }

        try {
            ec.writeExternal(dos);
            countEConWritten++;
            if (countEConWritten % 50000 == 0)
                getLog().info("  ... econcepts written " + countEConWritten);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private SctYConRecord readNextCon(ObjectInputStream ois, ArrayList<SctYConRecord> conList,
            SctYConRecord conNext) throws MojoFailureException {
        conList.clear();
        if (conNext != null) {
            conList.add(conNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readObject();
                if (obj instanceof SctYConRecord) {
                    conNext = (SctYConRecord) obj;
                    conList.add(conNext);
                } else
                    return null;
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                throw new MojoFailureException("IO Exception - readNextCon()");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new MojoFailureException("ClassNotFoundException - readNextCon()");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readObject();
                if (obj instanceof SctYConRecord) {
                    SctYConRecord rec = (SctYConRecord) obj;
                    if (rec.conUuidMsb == conNext.conUuidMsb
                            && rec.conUuidLsb == conNext.conUuidLsb) {
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
            throw new MojoFailureException("IO Exception -- readNextCon()");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new MojoFailureException("ClassNotFoundException -- readNextCon()");
        }

        return conNext; // first record of next concept id
    }

    private SctYDesRecord readNextDes(ObjectInputStream ois, ArrayList<SctYDesRecord> desList,
            SctYDesRecord desNext) throws MojoFailureException {
        desList.clear();
        if (desNext != null) {
            desList.add(desNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readObject();
                if (obj instanceof SctYDesRecord) {
                    desNext = (SctYDesRecord) obj;
                    desList.add(desNext);
                } else
                    return null;
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                throw new MojoFailureException("IO Exception - readNextDes()");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new MojoFailureException("ClassNotFoundException - readNextDes()");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readObject();
                if (obj instanceof SctYDesRecord) {
                    SctYDesRecord rec = (SctYDesRecord) obj;
                    // rec.conSnoId == desNext.conSnoId
                    if (rec.conUuidMsb == desNext.conUuidMsb
                            && rec.conUuidLsb == desNext.conUuidLsb) {
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
            throw new MojoFailureException("IO Exception -- readNextDes()");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new MojoFailureException("ClassNotFoundException -- readNextDes()");
        }

        return desNext; // first record of next concept id
    }

    private SctYRelRecord readNextRel(ObjectInputStream ois, ArrayList<SctYRelRecord> relList,
            SctYRelRecord relNext) throws MojoFailureException {
        relList.clear();
        if (relNext != null) {
            relList.add(relNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readObject();
                if (obj instanceof SctYRelRecord) {
                    relNext = (SctYRelRecord) obj;
                    relList.add(relNext);
                } else
                    return null;
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                throw new MojoFailureException("IO Exception - readNextRel()");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new MojoFailureException("ClassNotFoundException - readNextRel()");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readObject();
                if (obj instanceof SctYRelRecord) {
                    SctYRelRecord rec = (SctYRelRecord) obj;
                    if (rec.c1UuidMsb == relNext.c1UuidMsb && rec.c1UuidLsb == relNext.c1UuidLsb) {
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
            throw new MojoFailureException("IO Exception -- readNextRel()");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new MojoFailureException("ClassNotFoundException -- readNextRel()");
        }

        return relNext; // first record of next concept id
    }

    private SctYRelDestRecord readNextRelDest(ObjectInputStream ois,
            ArrayList<SctYRelDestRecord> relDestList, SctYRelDestRecord relDestNext)
            throws MojoFailureException {
        relDestList.clear();
        if (relDestNext != null) {
            relDestList.add(relDestNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readObject();
                if (obj instanceof SctYRelDestRecord) {
                    relDestNext = (SctYRelDestRecord) obj;
                    relDestList.add(relDestNext);
                } else
                    return null;
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                throw new MojoFailureException("IO Exception - readNextRelDest()");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new MojoFailureException("ClassNotFoundException - readNextRelDest()");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readObject();
                if (obj instanceof SctYRelDestRecord) {
                    SctYRelDestRecord rec = (SctYRelDestRecord) obj;
                    // rec.c2SnoId == relDestNext.c2SnoId
                    if (rec.c2UuidMsb == relDestNext.c2UuidMsb
                            && rec.c2UuidLsb == relDestNext.c2UuidLsb) {
                        relDestList.add(rec);
                    } else {
                        relDestNext = rec;
                        notDone = false;
                    }
                }
            }
        } catch (EOFException ex) {
            relDestNext = null;
            return null; // end reached, no more records
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("IO Exception -- readNextRelDest()");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new MojoFailureException("ClassNotFoundException -- readNextRelDest()");
        }

        return relDestNext; // first record of next concept id
    }

    private SctYRefSetRecord readNextRsByCon(ObjectInputStream ois,
            ArrayList<SctYRefSetRecord> rsByConList, SctYRefSetRecord rsByConNext)
            throws MojoFailureException {
        rsByConList.clear();
        if (rsByConNext != null) {
            rsByConList.add(rsByConNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readObject();
                if (obj instanceof SctYRefSetRecord) {
                    rsByConNext = (SctYRefSetRecord) obj;
                    rsByConList.add(rsByConNext);
                } else
                    return null;
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                throw new MojoFailureException("IO Exception - readNextRsByCon()");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new MojoFailureException("ClassNotFoundException - readNextRsByCon()");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readObject();
                if (obj instanceof SctYRefSetRecord) {
                    SctYRefSetRecord rec = (SctYRefSetRecord) obj;
                    if (rec.conUuidMsb == rsByConNext.conUuidMsb
                            && rec.conUuidLsb == rsByConNext.conUuidLsb) {
                        rsByConList.add(rec);
                    } else {
                        rsByConNext = rec;
                        notDone = false;
                    }
                }
            }
        } catch (EOFException ex) {
            rsByConNext = null;
            return null; // end reached, no more records
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("IO Exception -- readNextRsByCon()");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new MojoFailureException("ClassNotFoundException -- readNextRsByCon()");
        }

        return rsByConNext; // first record of next concept id
    }

    private SctYRefSetRecord readNextRsByRs(ObjectInputStream ois,
            ArrayList<SctYRefSetRecord> rsByRsList, SctYRefSetRecord rsByRsNext)
            throws MojoFailureException {
        rsByRsList.clear();
        if (rsByRsNext != null) {
            rsByRsList.add(rsByRsNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readObject();
                if (obj instanceof SctYRefSetRecord) {
                    rsByRsNext = (SctYRefSetRecord) obj;
                    rsByRsList.add(rsByRsNext);
                } else
                    return null;
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                throw new MojoFailureException("IO Exception - readNextRsByRs()");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new MojoFailureException("ClassNotFoundException - readNextRsByRs()");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readObject();
                if (obj instanceof SctYRefSetRecord) {
                    SctYRefSetRecord rec = (SctYRefSetRecord) obj;
                    if (rec.refsetUuidMsb == rsByRsNext.refsetUuidMsb
                            && rec.refsetUuidLsb == rsByRsNext.refsetUuidLsb) {
                        rsByRsList.add(rec);
                    } else {
                        rsByRsNext = rec;
                        notDone = false;
                    }
                }
            }
        } catch (EOFException ex) {
            rsByRsNext = null;
            return null; // end reached, no more records
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("IO Exception -- readNextRsByRs()");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new MojoFailureException("ClassNotFoundException -- readNextRsByRs()");
        }

        return rsByRsNext; // first record of next concept id
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

            uuidCurrent = ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next();

            uuidSourceCtv3 = ArchitectonicAuxiliary.Concept.CTV3_ID.getUids().iterator().next();
            uuidSourceSnomedRt = ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.getUids().iterator()
                    .next();
            uuidSourceSnomedInteger = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids()
                    .iterator().next();

            getLog().info("SNOMED CT Root       = " + uuidRootSnomedStr);
            getLog().info("SNOMED Core          = " + uuidPathSnomedCore);
            getLog().info("SNOMED Core Stated   = " + uuidPathSnomedStatedStr);
            getLog().info("  ... Stated rel     = " + uuidStatedRel.toString());

            getLog().info("SNOMED Core Inferred = " + uuidPathSnomedInferredStr);
            getLog().info("  ... Inferred rel   = " + uuidInferredRel.toString());

            getLog().info("SNOMED integer id UUID = " + uuidSourceSnomedInteger);
            getLog().info("SNOMED CTV3 id UUID    = " + uuidSourceCtv3);
            getLog().info("SNOMED RT id UUID      = " + uuidSourceSnomedRt);

        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
            throw new MojoFailureException("FAILED: SNOMED Core Stated/Inferred Path", e2);
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
            throw new MojoFailureException("FAILED: SNOMED Core Stated/Inferred Path", e2);
        }
    }

    private List<List<ARFFile>> getArfFiles(String wDir, String subDir, String[] arfDirs,
            String prefix, String postfix) throws MojoFailureException {

        List<List<ARFFile>> listOfDirs = new ArrayList<List<ARFFile>>();
        for (int ii = 0; ii < arfDirs.length; ii++) {
            ArrayList<ARFFile> listOfFiles = new ArrayList<ARFFile>();

            getLog().info(
                    String.format("%1$s (%2$s): %3$s%4$s%5$s", prefix.toUpperCase(), ii, wDir,
                            subDir, arfDirs[ii]));

            File f1 = new File(new File(wDir, subDir), arfDirs[ii]);
            ArrayList<File> fv = new ArrayList<File>();
            listFilesRecursive(fv, f1, prefix, postfix);

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

                    listOfFiles.add(new ARFFile(f2));
                    getLog().info("    FILE : " + f2.getParent() + FILE_SEPARATOR + f2.getName());
                }

            }

            listOfDirs.add(listOfFiles);
        }
        return listOfDirs;
    }

    private List<List<SCTFile>> getSctFiles(String wDir, String subDir, String[] inDirs,
            String prefix, String postfix) throws MojoFailureException {

        List<List<SCTFile>> listOfDirs = new ArrayList<List<SCTFile>>();
        for (int ii = 0; ii < inDirs.length; ii++) {
            ArrayList<SCTFile> listOfFiles = new ArrayList<SCTFile>();

            getLog().info(
                    String.format("%1$s (%2$s): %3$s%4$s%5$s", prefix.toUpperCase(), ii, wDir,
                            subDir, inDirs[ii]));

            File f1 = new File(new File(wDir, subDir), inDirs[ii]);
            ArrayList<File> fv = new ArrayList<File>();
            listFilesRecursive(fv, f1, "sct_" + prefix, postfix);

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
                    String revDate = getFileRevDate(f2);

                    SCTFile fo = createNewSctFile(f2, wDir, subDir, revDate);
                    listOfFiles.add(fo);
                    getLog().info(
                            "    FILE : " + f2.getName() + " " + revDate + " hasSnomedId="
                                    + fo.hasSnomedId + " doCrossMap=" + fo.doCrossMap);
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
            boolean snomedrtTF, ObjectOutputStream oos) throws Exception {
        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        int xSourceUUID, xRevDate, yPathID;
        SctYConRecord[] a1, a2, a3 = null;

        getLog().info("START CONCEPTS PROCESSING...");

        Iterator<List<SCTFile>> dit = sctv.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<SCTFile> fl = dit.next(); // File List
            Iterator<SCTFile> fit = fl.iterator(); // File Iterator

            // READ file1 as MASTER FILE
            SCTFile f1 = fit.next();
            fName1 = f1.file.getPath();
            xRevDate = f1.yRevDate;
            yPathID = f1.yPathIdx;
            xSourceUUID = f1.ySourceUuid;

            count1 = countFileLines(fName1);
            getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
            a1 = new SctYConRecord[count1];
            parseConcepts(fName1, a1, count1, ctv3idTF, snomedrtTF);
            writeConcepts(oos, a1, count1, xRevDate, yPathID);

            // :!!!:TODO: properly write ids with associated source
            // writeConceptIds(idstxt, a1, count1, sourceUUID, revDate, pathID);

            while (fit.hasNext()) {
                // SETUP CURRENT CONCEPTS INPUT FILE
                SCTFile f2 = fit.next();
                fName2 = f2.file.getPath();
                xRevDate = f2.yRevDate;
                yPathID = f2.yPathIdx;
                xSourceUUID = f2.ySourceUuid;

                count2 = countFileLines(fName2);
                getLog().info("Counted: " + count2 + " records, " + fName2);

                // Parse in file2
                a2 = new SctYConRecord[count2];
                parseConcepts(fName2, a2, count2, ctv3idTF, snomedrtTF);

                int r1 = 0, r2 = 0, r3 = 0; // reset record indices
                int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
                a3 = new SctYConRecord[count2]; // max3
                while ((r1 < count1) && (r2 < count2)) {

                    switch (compareConcept(a1[r1], a2[r2])) {
                    case 1: // SAME CONCEPT, skip to next
                        r1++;
                        r2++;
                        nSame++;
                        break;

                    case 2: // MODIFIED CONCEPT
                        // Write history
                        a2[r2].yPath = yPathID;
                        a2[r2].yRevision = xRevDate;
                        oos.writeUnshared(a2[r2]);
                        // Update master via pointer assignment
                        a1[r1] = a2[r2];
                        r1++;
                        r2++;
                        nMod++;
                        break;

                    case 3: // ADDED CONCEPT
                        // Write history
                        a2[r2].yPath = yPathID;
                        a2[r2].yRevision = xRevDate;
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
                            a1[r1].yPath = yPathID;
                            a1[r1].yRevision = xRevDate;
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
                        a2[r2].yPath = yPathID;
                        a2[r2].yRevision = xRevDate;
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
                a2 = new SctYConRecord[count1 + nAdd];
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
    }

    protected void processDescriptionsFiles(String wDir, List<List<SCTFile>> sctv,
            ObjectOutputStream oos) throws Exception {
        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        int xSourceUUID, xRevDate, yPathID;
        SctYDesRecord[] a1, a2, a3 = null;

        getLog().info("START DESCRIPTIONS PROCESSING...");
        // SETUP DESCRIPTIONS EXCEPTION REPORT
        String erFileName = wDir + scratchDirectory + FILE_SEPARATOR + "descriptions_report.txt";
        BufferedWriter er;
        er = new BufferedWriter(new FileWriter(erFileName));
        getLog().info("exceptions report OUTPUT: " + erFileName);

        Iterator<List<SCTFile>> dit = sctv.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<SCTFile> fl = dit.next(); // File List
            Iterator<SCTFile> fit = fl.iterator(); // File Iterator

            // READ file1 as MASTER FILE
            SCTFile f1 = fit.next();
            fName1 = f1.file.getPath();
            xRevDate = f1.yRevDate;
            yPathID = f1.yPathIdx;
            xSourceUUID = f1.ySourceUuid;

            count1 = countFileLines(fName1);
            getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
            a1 = new SctYDesRecord[count1];
            parseDescriptions(fName1, a1, count1);
            writeDescriptions(oos, a1, count1, xRevDate, yPathID);

            while (fit.hasNext()) {
                // SETUP CURRENT CONCEPTS INPUT FILE
                SCTFile f2 = fit.next();
                fName2 = f2.file.getPath();
                xRevDate = f2.yRevDate;
                yPathID = f2.yPathIdx;
                xSourceUUID = f2.ySourceUuid;

                count2 = countFileLines(fName2);
                getLog().info("Counted: " + count2 + " records, " + fName2);

                // Parse in file2
                a2 = new SctYDesRecord[count2];
                parseDescriptions(fName2, a2, count2);

                int r1 = 0, r2 = 0, r3 = 0; // reset record indices
                int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
                a3 = new SctYDesRecord[count2];
                while ((r1 < count1) && (r2 < count2)) {

                    switch (compareDescription(a1[r1], a2[r2])) {
                    case 1: // SAME DESCRIPTION, skip to next
                        r1++;
                        r2++;
                        nSame++;
                        break;

                    case 2: // MODIFIED DESCRIPTION
                        // Write history
                        a2[r2].yPath = yPathID;
                        a2[r2].yRevision = xRevDate;
                        oos.writeUnshared(a2[r2]);
                        // :xxx: bw.write(a2[r2].toStringArf(revDate, pathID));

                        // REPORT DESCRIPTION CHANGE EXCEPTION
                        if (a1[r1].conSnoId != a2[r2].conSnoId) {
                            er.write("** CONCEPTID CHANGE ** WAS/IS " + LINE_TERMINATOR);
                            er.write("id" + TAB_CHARACTER + "status" + TAB_CHARACTER + ""
                                    + "conSnoId" + TAB_CHARACTER + "" + "termText" + TAB_CHARACTER
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
                        a2[r2].yPath = yPathID;
                        a2[r2].yRevision = xRevDate;
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
                            a1[r1].yPath = yPathID;
                            a1[r1].yRevision = xRevDate;
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
                        a2[r2].yPath = yPathID;
                        a2[r2].yRevision = xRevDate;
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
                a2 = new SctYDesRecord[count1 + nAdd];
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

        er.close(); // Need to be sure to the close file!
    }

    protected void processRelationshipsFiles(String wDir, List<List<SCTFile>> sctI,
            ObjectOutputStream oos, BufferedWriter er) throws Exception {
        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        int xSourceUUID, xRevDate, yPathID;
        SctYRelRecord[] a1, a2, a3 = null;

        Iterator<List<SCTFile>> dit = sctI.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<SCTFile> fl = dit.next(); // File List
            Iterator<SCTFile> fit = fl.iterator(); // File Iterator
            if (fit.hasNext() == false)
                continue;

            // READ file1 as MASTER FILE
            SCTFile f1 = fit.next();
            fName1 = f1.file.getPath();
            xRevDate = f1.yRevDate;
            yPathID = f1.yPathIdx;
            xSourceUUID = f1.ySourceUuid;

            count1 = countFileLines(fName1);
            getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
            a1 = new SctYRelRecord[count1];
            parseRelationships(fName1, a1, count1, f1.hasSnomedId, f1.doCrossMap);
            a1 = removeDuplRels(a1);
            count1 = a1.length;
            writeRelationships(oos, a1, count1, xRevDate, yPathID);

            while (fit.hasNext()) {
                // SETUP CURRENT RELATIONSHIPS INPUT FILE
                SCTFile f2 = fit.next();
                fName2 = f2.file.getPath();
                xRevDate = f2.yRevDate;
                yPathID = f2.yPathIdx;
                xSourceUUID = f2.ySourceUuid;

                count2 = countFileLines(fName2);
                getLog().info("Counted: " + count2 + " records, " + fName2);

                // Parse in file2
                a2 = new SctYRelRecord[count2];
                parseRelationships(fName2, a2, count2, f2.hasSnomedId, f2.doCrossMap);
                a2 = removeDuplRels(a2);
                count2 = a2.length;

                int r1 = 0, r2 = 0, r3 = 0; // reset record indices
                int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
                a3 = new SctYRelRecord[count2];
                while ((r1 < count1) && (r2 < count2)) {
                    switch (compareRelationship(a1[r1], a2[r2])) {
                    case 1: // SAME RELATIONSHIP, skip to next
                        r1++;
                        r2++;
                        nSame++;
                        break;

                    case 2: // MODIFIED RELATIONSHIP

                        // REPORT & HANDLE CHANGE EXCEPTION
                        if ((a1[r1].c1SnoId != a2[r2].c1SnoId)
                                || (a1[r1].c2SnoId != a2[r2].c2SnoId)) {
                            er.write("** CONCEPTID CHANGE ** WAS/IS " + LINE_TERMINATOR);
                            er.write("id" + TAB_CHARACTER + "" + "status" + TAB_CHARACTER + ""
                                    + "c1SnoId" + TAB_CHARACTER + "" + "roleType" + TAB_CHARACTER
                                    + "" + "c2SnoId" + LINE_TERMINATOR);
                            er.write(a1[r1].toString());
                            er.write(a2[r2].toString());

                            // RETIRE & WRITE MASTER RELATIONSHIP a1[r1]
                            a1[r1].status = 1; // set to RETIRED
                            a1[r1].yPath = yPathID;
                            a1[r1].yRevision = xRevDate;
                            oos.writeUnshared(a1[r1]);
                            // :xxx: bw.write(a1[r1].toStringArf(revDate, pathID));

                            // SET EXCEPTIONFLAG for subsequence writes
                            // WILL WRITE INPUT RELATIONSHIP w/ NEGATIVE
                            // SNOMEDID
                            a2[r2].exceptionFlag = true;
                        }

                        // Write history
                        a2[r2].yPath = yPathID;
                        a2[r2].yRevision = xRevDate;
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
                        a2[r2].yPath = yPathID;
                        a2[r2].yRevision = xRevDate;
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
                            a1[r1].yPath = yPathID;
                            a1[r1].yRevision = xRevDate;
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
                            a1[r1].yPath = yPathID;
                            a1[r1].yRevision = xRevDate;
                            oos.writeUnshared(a1[r1]);
                            // :xxx: bw.write(a1[r1].toStringArf(revDate, pathID));
                        }
                        r1++;
                        nDrop++;
                    }
                }

                if (r2 < count2) {
                    while (r2 < count2) { // ADD REMAINING RELATIONSHIP INPUT
                        // Write history
                        a2[r2].yPath = yPathID;
                        a2[r2].yRevision = xRevDate;
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
                a2 = new SctYRelRecord[count1 + nAdd];
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

    private int compareConcept(SctYConRecord c1, SctYConRecord c2) {
        if (c1.conUuidMsb == c2.conUuidMsb && c1.conUuidLsb == c2.conUuidLsb) {
            if ((c1.status == c2.status) && (c1.isprimitive == c2.isprimitive))
                return 1; // SAME
            else
                return 2; // MODIFIED

        } else if (c1.conUuidMsb > c2.conUuidMsb) {
            return 3; // ADDED

        } else if (c1.conUuidMsb == c2.conUuidMsb && c1.conUuidLsb > c2.conUuidLsb) {
            return 3; // ADDED

        } else {
            return 4; // DROPPED
        }
    }

    private int compareDescription(SctYDesRecord c1, SctYDesRecord c2) {
        if (c1.desSnoId == c2.desSnoId) {
            if ((c1.status == c2.status) && (c1.conSnoId == c2.conSnoId)
                    && c1.termText.equals(c2.termText) && (c1.capStatus == c2.capStatus)
                    && (c1.descriptionType == c2.descriptionType)
                    && c1.languageCode.equals(c2.languageCode))
                return 1; // SAME
            else
                return 2; // MODIFIED

        } else if (c1.desSnoId > c2.desSnoId) {
            return 3; // ADDED

        } else {
            return 4; // DROPPED
        }
    }

    private int compareRelationship(SctYRelRecord c1, SctYRelRecord c2) {
        if (c1.relUuidMsb == c2.relUuidMsb && c1.relUuidLsb == c2.relUuidLsb) {
            if ((c1.status == c2.status) && (c1.characteristic == c2.characteristic)
                    && (c1.refinability == c2.refinability) && (c1.group == c2.group))
                return 1; // SAME
            else
                return 2; // MODIFIED

        } else if (c1.relUuidMsb > c2.relUuidMsb) {
            return 3; // ADDED

        } else if (c1.relUuidMsb == c2.relUuidMsb && c1.relUuidLsb > c2.relUuidLsb) {
            return 3; // ADDED

        } else {
            return 4; // DROPPED
        }
    }

    protected void parseConcepts(String fName, SctYConRecord[] a, int count, boolean ctv3idTF,
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
            a[concepts] = new SctYConRecord(conceptKey, conceptStatus, ctv3Str, snomedrtStr,
                    isPrimitive);
            concepts++;
        }
        Arrays.sort(a);

        getLog().info(
                "Parse & sort time: " + concepts + " concepts, "
                        + (System.currentTimeMillis() - start) + " milliseconds");
    }

    protected void parseDescriptions(String fName, SctYDesRecord[] a, int count) throws Exception {

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
            long conSnoId = Long.parseLong(st.sval);
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
            a[descriptions] = new SctYDesRecord(descriptionId, status, conSnoId, text, capStatus,
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

    protected void parseRelationships(String fName, SctYRelRecord[] a, int count,
            boolean hasSnomedId, boolean doCrossMap) throws Exception {

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
            if (hasSnomedId) {
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
            long roleTypeSnoId = Long.parseLong(st.sval);
            int roleTypeIdx = lookupRoleTypeIdxFromSnoId(roleTypeSnoId);
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
            a[relationships] = new SctYRelRecord(relID, status, conceptOneID, roleTypeSnoId,
                    roleTypeIdx, conceptTwoID, characteristic, refinability, group);
            relationships++;

            // CR
            tokenType = st.nextToken();
            // LF
            tokenType = st.nextToken();
            // Beginning of loop
            tokenType = st.nextToken();

        }

        computeRelationshipUuids(a, hasSnomedId, doCrossMap);
        Arrays.sort(a);

        getLog().info(
                "Parse & sort time: " + relationships + " relationships, "
                        + (System.currentTimeMillis() - start) + " milliseconds");
    }

    private SctYRelRecord[] removeDuplRels(SctYRelRecord[] a) {

        // REMOVE DUPLICATES
        int lenA = a.length;
        ArrayList<Integer> duplIdxList = new ArrayList<Integer>();
        for (int idx = 0; idx < lenA - 2; idx++)
            if ((a[idx].relUuidMsb == a[idx + 1].relUuidMsb)
                    && (a[idx].relUuidLsb == a[idx + 1].relUuidLsb)) {
                duplIdxList.add(Integer.valueOf(idx));
                getLog().info(
                        "::: WARNING -- Logically Duplicate Relationships:" + "\r\n::: A:" + a[idx] + "\r\n::: B:"
                                + a[idx + 1]);
            }
        if (duplIdxList.size() > 0) {
            SctYRelRecord[] b = new SctYRelRecord[lenA - duplIdxList.size()];
            int aPos = 0;
            int bPos = 0;
            int len;
            for (int dropIdx : duplIdxList) {
                len = dropIdx - aPos;
                System.arraycopy(a, aPos, b, bPos, len);
                bPos = bPos + len;
                aPos = aPos + len + 1;
            }
            len = lenA - aPos;
            System.arraycopy(a, aPos, b, bPos, len);
            return b;
        } else
            return a;
    }

    private void computeRelationshipUuids(SctYRelRecord[] a, boolean hasSnomedId, boolean doCrossMap)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // SORT BY [C1-Group-RoleType-C2]
        Comparator<SctYRelRecord> comp = new Comparator<SctYRelRecord>() {
            public int compare(SctYRelRecord o1, SctYRelRecord o2) {
                int thisMore = 1;
                int thisLess = -1;
                // C1
                if (o1.c1SnoId > o2.c1SnoId) {
                    return thisMore;
                } else if (o1.c1SnoId < o2.c1SnoId) {
                    return thisLess;
                } else {
                    // GROUP
                    if (o1.group > o2.group) {
                        return thisMore;
                    } else if (o1.group < o2.group) {
                        return thisLess;
                    } else {
                        // ROLE TYPE
                        if (o1.roleTypeSnoId > o2.roleTypeSnoId) {
                            return thisMore;
                        } else if (o1.roleTypeSnoId < o2.roleTypeSnoId) {
                            return thisLess;
                        } else {
                            // C2
                            if (o1.c2SnoId > o2.c2SnoId) {
                                return thisMore;
                            } else if (o1.c2SnoId < o2.c2SnoId) {
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
        long lastC1 = a[0].c1SnoId;
        int lastGroup = a[0].group;
        String GroupListStr = getGroupListString(a, 0);
        int max = a.length;
        for (int i = 0; i < max; i++) {
            // DETERMINE IF NEW GroupListStr IS NEEDED
            if (lastC1 != a[i].c1SnoId || lastGroup != a[i].group)
                GroupListStr = getGroupListString(a, i);

            // SET RELATIONSHIP UUID
            UUID uuid = Type5UuidFactory.get(REL_ID_NAMESPACE_UUID_TYPE1 + a[i].c1SnoId
                    + a[i].roleTypeSnoId + a[i].c2SnoId + GroupListStr);
            // :yyy:
            a[i].relUuidMsb = uuid.getMostSignificantBits();
            a[i].relUuidLsb = uuid.getLeastSignificantBits();
            UuidMinimal uuidMinimal = new UuidMinimal(a[i].relUuidMsb, a[i].relUuidLsb);

            // UPDATE SNOMED ID
            if (doCrossMap)
                if (hasSnomedId) {
                    relUuidMap.put(uuidMinimal, Long.valueOf(a[i].relSnoId));
                    // :yyy: relUuidMap.put(a[i].uuid, Long.valueOf(a[i].id));
                } else {
                    // get (check for existing) relationship id
                    Long tmp = relUuidMap.get(uuidMinimal); // :yyy:
                    if (tmp != null)
                        a[i].relSnoId = tmp.longValue();
                }

            lastC1 = a[i].c1SnoId;
            lastGroup = a[i].group;
        }
    }

    private String getGroupListString(SctYRelRecord[] a, int startIdx) {
        StringBuilder sb = new StringBuilder();

        int max = a.length;
        if (a[startIdx].group > 0) {
            long keepC1 = a[startIdx].c1SnoId;
            int keepGroup = a[startIdx].group;
            int i = startIdx;
            while ((i < max - 1) && (a[i].c1SnoId == keepC1) && (a[i].group == keepGroup)) {
                sb.append(a[i].c1SnoId + "-" + a[i].roleTypeSnoId + "-" + a[i].c2SnoId + ";");
                i++;
            }
        }
        return sb.toString();
    }

    protected void writeConcepts(ObjectOutputStream oos, SctYConRecord[] a, int count,
            int releaseDateIdx, int pathIdx) throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            a[i].yPath = pathIdx;
            a[i].yRevision = releaseDateIdx;
            oos.writeUnshared(a[i]);
        }

        getLog().info(
                "Output time: " + count + " records, " + (System.currentTimeMillis() - start)
                        + " milliseconds");
    }

    //    protected void writeConceptIds(Writer w, SctYConRecord[] a, int count, String source,
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

    protected void writeDescriptions(ObjectOutputStream oos, SctYDesRecord[] a, int count,
            int releaseDateIdx, int pathIdx) throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            a[i].yPath = pathIdx;
            a[i].yRevision = releaseDateIdx;
            oos.writeUnshared((Object) a[i]);
        }

        getLog().info(
                "Output time: " + count + " records, " + (System.currentTimeMillis() - start)
                        + " milliseconds");
    }

    //    protected void writeDescriptionIds(Writer w, SctYDesRecord[] a, int count,
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

    protected void writeRelationships(ObjectOutputStream oos, SctYRelRecord[] a, int count,
            int releaseDateIdx, int pathIdx) throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            a[i].yPath = pathIdx;
            a[i].yRevision = releaseDateIdx;
            oos.writeUnshared(a[i]);
        }

        getLog().info(
                "Output time: " + count + " records, " + (System.currentTimeMillis() - start)
                        + " milliseconds");
    }

    //    protected void writeRelationshipIds(Writer w, SctYRelRecord[] a, int count,
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

            oos.writeObject(yPathMap);
            oos.writeObject(yPathList);
            oos.writeObject(Integer.valueOf(yPathIdxCounter));

            oos.writeObject(yRevDateMap);
            oos.writeObject(yRevDateList);
            oos.writeObject(Integer.valueOf(yRevDateIdxCounter));

            oos.writeObject(ySourceUuidMap);
            oos.writeObject(ySourceUuidList);
            oos.writeObject(Integer.valueOf(ySourceUuidIdxCounter));

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

            yPathMap = (HashMap<String, Integer>) ois.readObject();
            yPathList = (ArrayList<String>) ois.readObject();
            yPathIdxCounter = (Integer) ois.readObject();

            yRevDateMap = (HashMap<String, Integer>) ois.readObject();
            yRevDateList = (ArrayList<String>) ois.readObject();
            yRevDateIdxCounter = (Integer) ois.readObject();

            ySourceUuidMap = (HashMap<String, Integer>) ois.readObject();
            ySourceUuidList = (ArrayList<String>) ois.readObject();
            ySourceUuidIdxCounter = (Integer) ois.readObject();

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
        // normalize date format
        s1 = s1.substring(0, 4) + "-" + s1.substring(4, 6) + "-" + s1.substring(6);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
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

    private SCTFile createNewSctFile(File f, String baseDir, String subDir, String revDate)
            throws MojoFailureException {
        String puuid = null;
        UUID u;
        boolean hasSnomedId = true;
        boolean doCrossMap = false;

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
            if (f.getAbsolutePath().contains("sct_relationships_stated")) {
                puuid = uuidPathSnomedStatedStr;
                getLog().info("  PATH UUID: " + "SNOMED Core Stated " + puuid);
                hasSnomedId = false;
                doCrossMap = true;
            } else if (f.getAbsolutePath().contains("sct_relationships_inferred")) {
                puuid = uuidPathSnomedInferredStr;
                getLog().info("  PATH UUID: " + "SNOMED Core Inferred " + puuid);
                doCrossMap = true;
            } else {
                // SNOMED_CORE Path UUID
                puuid = uuidPathSnomedCoreStr;
                getLog().info("  PATH UUID: " + "SNOMED Core " + puuid);
            }
        } else if (s.startsWith(NHS_UK_EXTENSION_FILE_PATH)) {
            // "UK Extensions" Path UUID
            try {
                if (f.getAbsolutePath().contains("sct_relationships_stated"))
                    u = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                            "NHS UK Extension Path Stated");
                else if (f.getAbsolutePath().contains("sct_relationships_inferred"))
                    u = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                            "NHS UK Extension Path Inferred");
                else
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
                if (f.getAbsolutePath().contains("sct_relationships_stated"))
                    u = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                            "NHS UK Drug Extension Path Stated");
                else if (f.getAbsolutePath().contains("sct_relationships_inferred"))
                    u = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                            "NHS UK Drug Extension Path Inferred");
                else
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
                if (f.getAbsolutePath().contains("sct_relationships_stated"))
                    u = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, s + " Stated");
                else if (f.getAbsolutePath().contains("sct_relationships_inferred"))
                    u = Type5UuidFactory
                            .get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, s + " Inferred");
                else
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

        return new SCTFile(f, revDate, puuid, hasSnomedId, doCrossMap);
    }

    /*
     * 1. build directory buildDir
     */

    private static void listFilesRecursive(ArrayList<File> list, File root, String prefix,
            String postfix) {
        if (root.isFile()) {
            list.add(root);
            return;
        }
        File[] files = root.listFiles();
        Arrays.sort(files);
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && files[i].getName().endsWith(postfix)
                    && files[i].getName().startsWith(prefix)) {
                list.add(files[i]);
            }
            if (files[i].isDirectory()) {
                listFilesRecursive(list, files[i], prefix, postfix);
            }
        }
    }

}