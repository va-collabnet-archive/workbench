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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import org.ihtsdo.etypes.ERefsetBooleanRevision;
import org.ihtsdo.etypes.ERefsetCidMember;
import org.ihtsdo.etypes.ERefsetCidRevision;
import org.ihtsdo.etypes.ERefsetIntMember;
import org.ihtsdo.etypes.ERefsetIntRevision;
import org.ihtsdo.etypes.ERefsetStrMember;
import org.ihtsdo.etypes.ERefsetStrRevision;
import org.ihtsdo.etypes.ERelationship;
import org.ihtsdo.etypes.ERelationshipRevision; // import org.ihtsdo.mojo.econcept.ConceptDescriptor;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.Boolean.TkRefsetBooleanRevision;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidRevision;
import org.ihtsdo.tk.dto.concept.component.refset.integer.TkRefsetIntRevision;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrRevision;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

/**
 * <b>DESCRIPTION: </b><br>
 * 
 * Sct1ArfToEConceptMojo is a maven mojo which converts SNOMED concepts, descriptions,
 * stated relationships and inferred relationships (Distribution Normal Form) RF1 release 
 * files to IHTSDO Workbench versioned import eConcepts format.  ARF formatted files can also be 
 * combined with the SCT 1 files.
 * <p>
 * <b>Relationship uuids are generated based on the algorithm below.  Note that changing the role group
 * in terms of relationship members or an non-mutable part any role group member will cause that role group
 * to be retired and a new role group to be created.</b> 
 * <pre>
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
 * </pre>
 * <b>INPUTS:</b>
 * <pre>
 * &lt;targetSub&gt;       subdirname -- working sub directly under build directory
 * &lt;outputDirectory&gt; dirname    -- directory for output eConcepts files
 * &lt;dateStart&gt;       yyyy.mm.dd -- filter excludes files before startDate
 * &lt;dateStop&gt;        yyyy.mm.dd -- filter excludes files after stopDate
 * &lt;uuidSnorocket&gt;   uuid -- Snorocket User UUID for defining inferred relationships
 * &lt;uuidUser&gt;        uuid -- User UUID if not a defining inferred relationship
 * &lt;rf2Mapping&gt;        true= maps preferred description type to synomym like RF2
 *                     false=retains strict RF1 description type
 * &lt;includeCTV3ID&gt;     true | false
 * &lt;includeSNOMEDRTID&gt; true | false
 * 
 * &lt;sct1Dirs&gt;                  -- list of sct input directory items
 *    &lt;sct1Dir&gt;                -- detailed input directory item
 *       &lt;directoryName&gt; name  -- directory name
 *       &lt;mapSctIdInferredToStated&gt;   true | false
 *       &lt;keepHistoricalFromInferred&gt; true | false
 *       &lt;keepQualifierFromInferred&gt;  true | false
 *       &lt;keepAdditionalFromInferred&gt; true | false
 *       &lt;corePathUuid&gt;     uuid -- core path UUID
 *       &lt;inferredPathName&gt; name -- inferred path name
 *       &lt;statedPathName&gt;   name -- stated path name
 *
 * &lt;arfInputDirs&gt;
 *       &lt;param&gt;/cement/&lt;/param&gt;
 * </pre>  
 * The POM needs to specify mutually exclusive extensions in separate
 * directories in the array <code>sctInputDirArray</code> parameter. Each
 * directory entry will be parsed to locate SNOMED formated text files in any
 * sub-directories. <br>
 * <br>
 * 
 * Each SNOMED file should contain a version date in the file name
 * <code>"sct1_*yyyyMMdd.txt"</code>. If a valid date is not found in the file
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
 * 1. RELEASE DATE must be in either the SNOMED file name. The preferred date format in <code>yyyyMMdd</code>. <br>
 * <br>
 * 2. SNOMED EXTENSIONS must be mutually exclusive from SNOMED CORE and each
 * other; and, placed under separate <code>sctInputDirArray</code> directories.<br>
 * <br>
 * 3. STATED & INFERRED. Stated relationship files names must begin with "sct1_relationships_stated". 
 * Inferred relationship file names must begin with "sct1_relationships_inferred".  
 * Relationship file names without "_stated" or "_inferred" are not supported.
 * <p>
 * <b>PROCESSING:</b><br>
 * Step #1. Versioning & Relationship Generated IDs.  Merge time series of releases into 
 * a versioned intermediate concept, description, and relationship files.  This step 
 * also adds an algorithmically computed relationship ids.  Ids are kept directly with each primary 
 * (concept, description & relationship) component. <br>
 * <br>
 * Step #2. ARF files. Append arf files to sct binary records files.<br>
 * <br>
 * Step #3. Destination Rels.  Build file for destination rels. Non-required fields are dropped.<br>
 * <br>
 * Step #4. Match IDs. Associate ids with each specific component.<br>
 *  <br>
 * Step #5. Refset. Refset preparation.<br>
 *  <br>
 * Step #6. Sort.  Sort into concept order for merging the prepared files to create eConcepts in
 * the next step.<br>
 *  <br>
 * Step #7. Create EConcepts.  Concurrently read pre-sorted concept, description, source relationship
 *  and destination relationship files and creates eConcepts.<br>
 * <p>
 * <b>NOTES:</b><br>
 * <b>Records are NOT VERSIONED between files under DIFFERENT
 * <code>sctInputDirArray</code> directories. The versioned output from
 * <code>sctInputDirArray[a+1]</code> is appended to the versioned output from
 * <code>sctInputDirArray[a]</code>. </b><br>
 * 
 * @author Marc E. Campbell
 * 
 * @goal sct1-arf-to-econcepts
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class Sct1ArfToEConceptMojo extends AbstractMojo implements Serializable {

    private static final long serialVersionUID = 1L;
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
    private static final int ooResetInterval = 100;
    // workaround to set stated relationship characteristic as STATED_RELATIONSHIP 
    // starts a integer 5 at beginning of import pipeline
    // integer 5 is replaced with STATED_RELATIONSHIP UUID at eConcept creation
    private static final int STATED_CHAR_WORKAROUND = 5;
    /**
     * Line terminator is deliberately set to CR-LF which is DOS style
     */
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
    /**
     * Start date (inclusive)
     * 
     * @parameter
     */
    private String dateStart;
    private Date dateStartObj;
    /**
     * Stop date (inclusive)
     * 
     * @parameter
     */
    private String dateStop;
    private Date dateStopObj;
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
     * ARF Input Directories Array. The directory array parameter supported
     * extensions via separate directories in the array.
     * 
     * @parameter
     */
    private String[] arfInputDirs;
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
    private Sct1Dir[] sct1Dirs;
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
    private boolean useSctRelId;
    /**
     * @parameter default-value="false"
     */
    private boolean rf2Mapping;
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
    /**
     * 
     * @parameter
     * @required
     */
    private UUID uuidUser;
    //    /**
    //     * Watch concepts
    //     * 
    //     * @parameter
    //     */
    //    private List<ConceptDescriptor> conceptsToWatch;
    //    HashMap<UUID, ConceptDescriptor> conceptsToWatchMap;
    /**
     * Watch concepts
     * 
     * @parameter default-value="false"
     */
    private boolean debug;

    public void setUuidUser(String uuidStr) {
        uuidUser = UUID.fromString(uuidStr);
    }
    /**
     * Snorocket "User" UUID
     * @parameter
     * @required
     */
    private UUID uuidUserSnorocket;
    private static final int USER_DEFAULT_IDX = 0;
    private static final int USER_SNOROCKET_IDX = 1;

    public void setUuidUserSnorocket(String uuidStr) {
        uuidUserSnorocket = UUID.fromString(uuidStr);
    }
    private String scratchDirectory = FILE_SEPARATOR + "tmp_steps";
    private static final String REL_ID_NAMESPACE_UUID_TYPE1 = "84fd0460-2270-11df-8a39-0800200c9a66";
    //private Sct1_RelUuidMinimalMap relUuidMap; // :yyy:
    private HashMap<UUID, Long> relUuidMap; // :yyy:
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
    private static UUID uuidCurrent = ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next();
    private static UUID uuidSourceSnomedLong;
    private static int uuidSourceSnomedIdx;
    private static UUID uuidSourceCtv3;
    private static UUID uuidSourceSnomedRt;
    private SimpleDateFormat arfSimpleDateFormat;
    private SimpleDateFormat arfSimpleDateFormatDot;

    private class ARFFile {

        File file;

        public ARFFile(File f) {
            this.file = f;
        }

        @Override
        public String toString() {
            return " :: " + file.getPath();
        }
    }

    private class SCTFile {

        File file;
        String revDate;
        //        String pathUuidStr;
        String sourceUuid;
        Boolean isStated;
        Boolean hasStatedSctRelId;
        Boolean mapSctIdInferredToStated; // :DEPRECIATED: Cross map inferred id to stated.
        Boolean keepQualifier; // 1
        Boolean keepHistorical; // 2
        Boolean keepAdditional; // 3
        long zRevTime;
        int pathIdx;
        int pathInferredIdx;
        int pathStatedIdx;
        int sourceUuidIdx;

        public SCTFile(File f, String wDir, String subDir, String d, Sct1Dir sctDir)
                throws ParseException {
            this.file = f;
            this.revDate = d; // yyyy-MM-dd 00:00:00 format

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            this.zRevTime = df.parse(revDate).getTime();

            this.sourceUuid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator().next().toString();
            this.sourceUuidIdx = lookupZSourceUuidIdx(sourceUuid); // "ID SOURCE" i.e. SNOMED IDs

            // PATHS
            this.pathIdx = lookupZPathIdx(sctDir.getWbPathUuidCore().toString());
            if (sctDir.getWbPathUuidInferred() != null) {
                this.pathInferredIdx = lookupZPathIdx(sctDir.getWbPathUuidInferred().toString());
            } else {
                this.pathInferredIdx = pathIdx; // DEFAULT TO CORE PATH
            }
            if (sctDir.getWbPathUuidStated() != null) {
                this.pathStatedIdx = lookupZPathIdx(sctDir.getWbPathUuidStated().toString());
            } else {
                this.pathStatedIdx = pathIdx; // DEFAULT TO CORE PATH
            }
            // NON-DEFINING RELATIONSHIPS FILTER
            this.keepQualifier = sctDir.getKeepQualifierFromInferred(); // 1
            this.keepHistorical = sctDir.getKeepHistoricalFromInferred(); // 2
            this.keepAdditional = sctDir.getKeepAdditionalFromInferred(); // 3

            // RELATIONSHIP SCT ID INFERRED TO STATED MAPPING
            boolean doCrossMap = false;
            boolean hasSnomedId = true;
            this.isStated = false;
            if (useSctRelId) {
                if (f.getName().toUpperCase().contains("STATED")) {
                    this.isStated = true;
                }
                doCrossMap = false;
                hasSnomedId = sctDir.isStatedSctRelIdPresent();
            } else {
                if (f.getName().toUpperCase().contains("STATED")) {
                    this.isStated = true;
                    if (sctDir.doMapSctIdInferredToStated()) {
                        doCrossMap = true;
                        hasSnomedId = false;
                    }
                } else if (f.getName().toUpperCase().contains("INFERRED")
                        && sctDir.doMapSctIdInferredToStated()) {
                    doCrossMap = true;
                    hasSnomedId = true;
                }
            }

            this.hasStatedSctRelId = hasSnomedId;
            this.mapSctIdInferredToStated = doCrossMap;

            // setup PATH UUID (puuid), hasStatedSctRelId, doCrossMap
            getLog().info("           " + f.getName() + " QUEUED");
        }

        @Override
        public String toString() {
            return file.getPath();
        }
    }
    // AUTHOR UUID LOOKUP
    private HashMap<String, Integer> zAuthorMap; // <UUID, index>
    private ArrayList<String> zAuthorList;
    private UUID[] zAuthorUuidArray;
    private int zAuthorIdxCounter;

    private int lookupZAuthorIdx(String authorIdStr) {
        Integer tmp = zAuthorMap.get(authorIdStr);
        if (tmp == null) {
            zAuthorIdxCounter++;
            zAuthorMap.put(authorIdStr, Integer.valueOf(zAuthorIdxCounter));
            zAuthorList.add(authorIdStr);
            return zAuthorIdxCounter;
        } else {
            return tmp.intValue();
        }
    }
    // PATH UUID LOOKUP
    private HashMap<String, Integer> zPathMap;
    private ArrayList<String> zPathList;
    private UUID[] zPathArray;
    private int zPathIdxCounter;

    private int lookupZPathIdx(String pathIdStr) {
        Integer tmp = zPathMap.get(pathIdStr);
        if (tmp == null) {
            zPathIdxCounter++;
            zPathMap.put(pathIdStr, Integer.valueOf(zPathIdxCounter));
            zPathList.add(pathIdStr);
            return zPathIdxCounter;
        } else {
            return tmp.intValue();
        }
    }
    // SOURCE UUID LOOKUP
    private HashMap<String, Integer> zSourceUuidMap;
    private ArrayList<String> zSourceUuidList;
    private UUID[] zSourceUuidArray;
    private int zSourceUuidIdxCounter;

    private int lookupZSourceUuidIdx(String sourceUuidStr) {
        Integer tmp = zSourceUuidMap.get(sourceUuidStr);
        if (tmp == null) {
            zSourceUuidIdxCounter++;
            zSourceUuidMap.put(sourceUuidStr, Integer.valueOf(zSourceUuidIdxCounter));
            zSourceUuidList.add(sourceUuidStr);
            return zSourceUuidIdxCounter;
        } else {
            return tmp.intValue();
        }
    }
    // STATUS TYPE LOOKUP
    private HashMap<String, Integer> zStatusUuidMap;
    private ArrayList<String> zStatusUuidList;
    private UUID[] zStatusUuidArray;
    private int zStatusUuidIdxCounter;

    private int lookupZStatusUuidIdx(String statusUuidStr) {
        Integer tmp = zStatusUuidMap.get(statusUuidStr);
        if (tmp == null) {
            zStatusUuidIdxCounter++;
            zStatusUuidMap.put(statusUuidStr, Integer.valueOf(zStatusUuidIdxCounter));
            zStatusUuidList.add(statusUuidStr);
            return zStatusUuidIdxCounter;
        } else {
            return tmp.intValue();
        }
    }
    // DESCRIPTION TYPE LOOKUP
    private HashMap<String, Integer> zDesTypeUuidMap;
    private ArrayList<String> zDesTypeUuidList;
    private UUID[] zDesTypeUuidArray;
    private int zDesTypeUuidIdxCounter;

    private int lookupZDesTypeUuidIdx(String desTypeUuidStr) {
        Integer tmp = zDesTypeUuidMap.get(desTypeUuidStr);
        if (tmp == null) {
            zDesTypeUuidIdxCounter++;
            zDesTypeUuidMap.put(desTypeUuidStr, Integer.valueOf(zDesTypeUuidIdxCounter));
            zDesTypeUuidList.add(desTypeUuidStr);
            return zDesTypeUuidIdxCounter;
        } else {
            return tmp.intValue();
        }
    }
    // RELATIONSHIP CHARACTERISTIC LOOKUP
    private UUID[] zRelCharArray;
    private String[] zRelCharStrArray;

    private int lookupRelCharTypeIdx(String uuid) {
        int idx = 0;
        while (idx < 6) {
            if (uuid.equalsIgnoreCase(zRelCharStrArray[idx])) {
                break;
            }
            idx++;
        }
        return idx;
    }
    // RELATIONSHIP REFINIBILITY LOOKUP
    private UUID[] zRelRefArray;
    private String[] zRelRefStrArray;

    private int lookupRelRefTypeIdx(String uuid) {
        int idx = 0;
        while (idx < 3) {
            if (uuid.equalsIgnoreCase(zRelRefStrArray[idx])) {
                break;
            }
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
    private List<RoleTypeEntry> zRoleTypeList;

    private int lookupRoleTypeIdxFromSnoId(long roleTypeSnoId) {
        int last = zRoleTypeList.size();
        for (int idx = 0; idx < last; idx++) {
            if (zRoleTypeList.get(idx).snomedId == roleTypeSnoId) {
                return idx;
            }
        }

        RoleTypeEntry tmp = new RoleTypeEntry(roleTypeSnoId);
        zRoleTypeList.add(tmp);
        return last;
    }

    private int lookupRoleTypeIdx(String uStr) {
        int last = zRoleTypeList.size();
        for (int idx = 0; idx < last; idx++) {
            if (zRoleTypeList.get(idx).uuidStr.equalsIgnoreCase(uStr)) {
                return idx;
            }
        }

        RoleTypeEntry tmp = new RoleTypeEntry(uStr);
        zRoleTypeList.add(tmp);
        return last;
    }

    // 
    private UUID lookupRoleType(int roleTypeIdx) {
        return zRoleTypeList.get(roleTypeIdx).uuid;
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
    private List<IdSrcSystemEntry> zIdSrcSystemList;

    private int lookupSrcSystemIdx(String uuidStr) {
        int last = zIdSrcSystemList.size();
        for (int idx = 0; idx < last; idx++) {
            if (uuidStr.equalsIgnoreCase(zIdSrcSystemList.get(idx).srcSystemIdStr)) {
                return idx;
            }
        }

        // NOT FOUND IN LIST
        zIdSrcSystemList.add(new IdSrcSystemEntry(uuidStr, IdDataType.STRING));
        getLog().info(" ::: IMPORT DISCOVERED NOT-DECLARED ID SYSTEM = " + uuidStr);
        return last;
    }

    private UUID lookupSrcSystemUUID(int idx) {
        if (idx < zIdSrcSystemList.size()) {
            return zIdSrcSystemList.get(idx).srcSystemUuid;
        } else {
            return null;
        }
    }

    private void setupLookupPartA() throws MojoFailureException {
        // Relationship Role Types
        zRoleTypeList = new ArrayList<RoleTypeEntry>();

        try {
            // Status Array
            for (int idx = 0; idx < 12; idx++) {
                lookupZStatusUuidIdx(ArchitectonicAuxiliary.getStatusFromId(idx).getUids().iterator().next().toString());
            }

            // DESCRIPTION TYPES
            // Setup the standard description types used in SNOMED
            for (int i = 0; i < 5; i++) {
                lookupZDesTypeUuidIdx(ArchitectonicAuxiliary.getSnomedDescriptionType(i).getUids().iterator().next().toString());
            }
            lookupZDesTypeUuidIdx(ArchitectonicAuxiliary.Concept.TEXT_DEFINITION_TYPE.getUids().iterator().next().toString());

            // RELATIONSHIP CHARACTERISTIC
            zRelCharArray = new UUID[6];
            for (int i = 0; i < 5; i++) {
                zRelCharArray[i] = ArchitectonicAuxiliary.getSnomedCharacteristicType(i).getUids().iterator().next();
            }
            zRelCharArray[STATED_CHAR_WORKAROUND] = ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids().iterator().next();

            // string lookup array
            zRelCharStrArray = new String[6];
            for (int idx = 0; idx < 6; idx++) {
                zRelCharStrArray[idx] = zRelCharArray[idx].toString();
            }

            // RELATIONSHIP REFINABILITY
            zRelRefArray = new UUID[3];
            for (int i = 0; i < 3; i++) {
                zRelRefArray[i] = ArchitectonicAuxiliary.getSnomedRefinabilityType(i).getUids().iterator().next();
            }
            // string lookup array
            zRelRefStrArray = new String[3];
            for (int idx = 0; idx < 3; idx++) {
                zRelRefStrArray[idx] = zRelRefArray[idx].toString();
            }

            // ID SOURCE SYSTEM
            zIdSrcSystemList = new ArrayList<IdSrcSystemEntry>();
            zIdSrcSystemList.add(new IdSrcSystemEntry(uuidSourceSnomedLong, IdDataType.LONG));
            uuidSourceSnomedIdx = 0;
            zIdSrcSystemList.add(new IdSrcSystemEntry(uuidSourceCtv3, IdDataType.STRING));
            zIdSrcSystemList.add(new IdSrcSystemEntry(uuidSourceSnomedRt, IdDataType.STRING));
            zIdSrcSystemList.add(new IdSrcSystemEntry(ArchitectonicAuxiliary.Concept.ICD_9.getUids().iterator().next(), IdDataType.STRING));

        } catch (Exception e) {
            getLog().info(e);
            throw new MojoFailureException("FAILED: Sct1ArfToEConcept -- setupLookupPartA()");
        }

    }

    private void setupLookupPartB() throws MojoFailureException {
        zStatusUuidArray = new UUID[zStatusUuidList.size()];
        int i = 0;
        for (String s : zStatusUuidList) {
            zStatusUuidArray[i] = UUID.fromString(s);
            i++;
        }

        zDesTypeUuidArray = new UUID[zDesTypeUuidList.size()];
        i = 0;
        for (String s : zDesTypeUuidList) {
            zDesTypeUuidArray[i] = UUID.fromString(s);
            i++;
        }

        zAuthorUuidArray = new UUID[zAuthorList.size()];
        i = 0;
        for (String s : zAuthorList) {
            zAuthorUuidArray[i] = UUID.fromString(s);
            i++;
        }

        zPathArray = new UUID[zPathList.size()];
        i = 0;
        for (String s : zPathList) {
            zPathArray[i] = UUID.fromString(s);
            i++;
        }

        // SNOMED_INT ... :FYI: soft code in SctZ1ConRecord
        zSourceUuidArray = new UUID[zSourceUuidList.size()];
        i = 0;
        for (String s : zSourceUuidList) {
            zSourceUuidArray[i] = UUID.fromString(s);
            i++;
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //           conceptsToWatchMap =
        //                    new HashMap<UUID, ConceptDescriptor>();
        //            if (conceptsToWatch != null) {
        //                for (ConceptDescriptor cd : conceptsToWatch) {
        //                    conceptsToWatchMap.put(UUID.fromString(cd.getUuid()), cd);
        //                }
        //            }

        getLog().info("::: BEGIN Sct1ArfToEConcept");

        // SHOW build directory from POM file
        String targetDir = targetDirectory.getAbsolutePath();
        getLog().info("    POM Target Directory: " + targetDir);

        // SHOW input sub directory from POM file
        if (!targetSubDir.equals("")) {
            targetSubDir = FILE_SEPARATOR + targetSubDir;
            getLog().info("    POM Target Sub Directory: " + targetSubDir);
        }

        // SHOW input directories from POM file
        for (int i = 0; i < sct1Dirs.length; i++) {
            sct1Dirs[i].setDirectoryName(sct1Dirs[i].getDirectoryName().replace('/',
                    File.separatorChar));
            getLog().info("POM SCT Input Directory (" + i + ") = " + sct1Dirs[i]);
            if (!sct1Dirs[i].getDirectoryName().startsWith(FILE_SEPARATOR)) {
                sct1Dirs[i].setDirectoryName(FILE_SEPARATOR + sct1Dirs[i].getDirectoryName());
            }
        }

        if (arfInputDirs == null) {
            arfInputDirs = new String[0];
        }
        for (int i = 0; i < arfInputDirs.length; i++) {
            arfInputDirs[i] = arfInputDirs[i].replace('/', File.separatorChar);
            getLog().info("POM ARF Input Directory (" + i + ") = " + arfInputDirs[i]);
            if (!arfInputDirs[i].startsWith(FILE_SEPARATOR)) {
                arfInputDirs[i] = FILE_SEPARATOR + arfInputDirs[i];
            }
        }

        // SHOW input sub directory from POM file
        if (!outputDirectory.equals("")) {
            outputDirectory = FILE_SEPARATOR + outputDirectory;
            getLog().info("POM Output Directory: " + outputDirectory);
        }

        executeMojo(targetDir, targetSubDir, arfInputDirs, sct1Dirs, outputDirectory,
                includeCTV3ID, includeSNOMEDRTID);
        getLog().info("::: END Sct1ArfToEConcept");
    }

    void executeMojo(String tDir, String tSubDir, String[] arfDirs, Sct1Dir[] sctDirs,
            String outDir, boolean ctv3idTF, boolean snomedrtTF) throws MojoFailureException {

        // :DEBUG:TEST:
        countRefsetMember = 0;
        countRefsetMaster = 0;
        statRsBoolFromArf = 0;
        statRsIntFromArf = 0;
        statRsConFromArf = 0;
        statRsStrFromArf = 0;

        getLog().info("::: RF2 Mapping: " + rf2Mapping);
        getLog().info("::: Target Directory: " + tDir);
        getLog().info("::: Target Sub Directory:     " + tSubDir);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.ss hh:mm:ss");
        if (dateStartObj != null) {
            getLog().info("::: Start date (inclusive) = " + sdf.format(dateStartObj));
        }
        if (dateStopObj != null) {
            getLog().info(":::  Stop date (inclusive) = " + sdf.format(dateStopObj));
        }

        for (int i = 0; i < sctDirs.length; i++) {
            getLog().info("::: SCT Input Directory (" + i + ") = " + sctDirs[i].getDirectoryName());
            getLog().info(
                    ":::     UUID Core:     " + sctDirs[i].getWbPathUuidCore() + " : "
                    + sctDirs[i].getWbPathUuidCoreFromName());
            getLog().info(
                    ":::     UUID Stated:   " + sctDirs[i].getWbPathUuidStated() + " : "
                    + sctDirs[i].getWbPathUuidStatedFromName());
            getLog().info(
                    ":::     UUID Inferred: " + sctDirs[i].getWbPathUuidInferred() + " : "
                    + sctDirs[i].getWbPathUuidInferredFromName());
            getLog().info(
                    ":::     Keep Qualifier Rels from _inferred_ file:  "
                    + sctDirs[i].getKeepQualifierFromInferred());
            getLog().info(
                    ":::     Keep Historical Rels from _inferred_ file: "
                    + sctDirs[i].getKeepHistoricalFromInferred());
            getLog().info(
                    ":::     Keep Additional Rels from _inferred_ file: "
                    + sctDirs[i].getKeepAdditionalFromInferred());
            getLog().info(
                    ":::     Map SCT REL IDs from inferred to stated: "
                    + sctDirs[i].doMapSctIdInferredToStated());
        }
        for (int i = 0; i < arfDirs.length; i++) {
            getLog().info("::: ARF Input Directory (" + i + ") = " + arfDirs[i]);
        }
        getLog().info("::: Output Directory:  " + outDir);

        fNameStep1Con = tDir + scratchDirectory + FILE_SEPARATOR + "step1_concepts.ser";
        fNameStep1Rel = tDir + scratchDirectory + FILE_SEPARATOR + "step1_relationships.ser";
        fNameStep1Desc = tDir + scratchDirectory + FILE_SEPARATOR + "step1_descriptions.ser";
        fNameStep1Ids = tDir + scratchDirectory + FILE_SEPARATOR + "step1_ids.ser";

        fNameStep2Refset = tDir + scratchDirectory + FILE_SEPARATOR + "step2_refset.ser";

        fNameStep3RelDest = tDir + scratchDirectory + FILE_SEPARATOR + "step3_rel_dest.ser";

        fNameStep4Con = tDir + scratchDirectory + FILE_SEPARATOR + "step4_concepts.ser";
        fNameStep4Desc = tDir + scratchDirectory + FILE_SEPARATOR + "step4_descriptions.ser";
        fNameStep4Rel = tDir + scratchDirectory + FILE_SEPARATOR + "step4_relationships.ser";

        fNameStep5RsByCon = tDir + scratchDirectory + FILE_SEPARATOR + "step5_refset_by_con.ser";
        fNameStep5RsByRs = tDir + scratchDirectory + FILE_SEPARATOR + "step5_refet_by_refset.ser";

        fNameStep6Con = tDir + scratchDirectory + FILE_SEPARATOR + "step6_concepts.ser";
        fNameStep6Desc = tDir + scratchDirectory + FILE_SEPARATOR + "step6_descriptions.ser";
        fNameStep6Rel = tDir + scratchDirectory + FILE_SEPARATOR + "step6_relationships.ser";
        fNameStep6RelDest = tDir + scratchDirectory + FILE_SEPARATOR + "step6_rel_dest.ser";

        fNameStep7ECon = tDir + outDir + FILE_SEPARATOR + "sctSiEConcepts.jbin";

        zAuthorMap = new HashMap<String, Integer>();
        zAuthorList = new ArrayList<String>();
        zAuthorIdxCounter = -1;

        zPathMap = new HashMap<String, Integer>();
        zPathList = new ArrayList<String>();
        zPathIdxCounter = -1;

        zSourceUuidMap = new HashMap<String, Integer>();
        zSourceUuidList = new ArrayList<String>();
        zSourceUuidIdxCounter = -1;

        zStatusUuidMap = new HashMap<String, Integer>();
        zStatusUuidList = new ArrayList<String>();
        zStatusUuidIdxCounter = -1;

        zDesTypeUuidMap = new HashMap<String, Integer>();
        zDesTypeUuidList = new ArrayList<String>();
        zDesTypeUuidIdxCounter = -1;

        setupUuids();

        // Setup target (build) directory
        getLog().info("    Target Build Directory: " + tDir);

        arfSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        arfSimpleDateFormatDot = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        ObjectOutputStream oosCon = null;
        ObjectOutputStream oosDes = null;
        ObjectOutputStream oosRel = null;
        ObjectOutputStream oosIds = null;
        ObjectOutputStream oosRefSet = null;
        // SETUP OUTPUT directory
        try {
            // Create multiple directories
            boolean success = (new File(tDir + outDir)).mkdirs();
            if (success) {
                getLog().info("OUTPUT DIRECTORY: " + tDir + outDir);
            }

            String tmpDir = scratchDirectory;
            success = (new File(tDir + tmpDir)).mkdirs();
            if (success) {
                getLog().info("SCRATCH DIRECTORY: " + tDir + tmpDir);
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
            executeMojoStep1(tDir, tSubDir, sctDirs, ctv3idTF, snomedrtTF, oosCon, oosDes, oosRel,
                    oosIds);
            System.gc();

            // STEP #2. Convert arf files to versioned binary objects file.
            // Uses existing relationship uuid
            // Appends to binary stream created in Step 1.
            executeMojoStep2(tDir, tSubDir, arfDirs, oosCon, oosDes, oosRel, oosIds, oosRefSet);

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
            getLog().info("Sct1ArfToEConceptsMojo sct1-arf-to-econcepts Error");
            throw new MojoFailureException("Error", e);
        }
    }

    private void executeMojoStep1(String wDir, String subDir, Sct1Dir[] inDirs, boolean ctv3idTF,
            boolean snomedrtTF, ObjectOutputStream oosCon, ObjectOutputStream oosDes,
            ObjectOutputStream oosRel, ObjectOutputStream oosIds) throws MojoFailureException {
        getLog().info("*** Sct1ArfToEConcept STEP #1 BEGIN SCT1 PROCESSING ***");
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
            getLog().info(e1);
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
            getLog().info(e1);
            throw new MojoFailureException("FAILED: processDescriptionsFiles()", e1);
        }

        // 3,254,249 from 2002.07 through 2010.01 
        // relUuidMap = new Sct1_RelUuidMinimalMap(); // :yyy:
        relUuidMap = new HashMap<UUID, Long>(); // :yyy:

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
            erw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(erFileName),
                    "UTF-8"));
            getLog().info("RELATIONSHIPS Exceptions Report OUTPUT: " + erFileName);

            processRelationshipsFiles(wDir, listOfRiDirs, oosRel, oosIds, erw, USER_SNOROCKET_IDX);
            processRelationshipsFiles(wDir, listOfRsDirs, oosRel, oosIds, erw, USER_DEFAULT_IDX);

            erw.close(); // Need to be sure to the close file!
        } catch (Exception e1) {
            getLog().info("FAILED: processRelationshipsFiles()");
            getLog().info(e1);
            throw new MojoFailureException("FAILED: processRelationshipsFiles()", e1);
        }

        relUuidMap = null; // memory not needed any more.
        System.gc();
        getLog().info(
                "*** VERSIONING TIME: " + ((System.currentTimeMillis() - start) / 1000)
                + " seconds");
        getLog().info("*** Sct1ArfToEConcept STEP #1  SCT1 PROCESSING COMPLETE ***\r\n");
    }

    private void executeMojoStep2(String wDir, String subDir, String[] arfDirs,
            ObjectOutputStream oosCon, ObjectOutputStream oosDes, ObjectOutputStream oosRel,
            ObjectOutputStream oosIds, ObjectOutputStream oosRefSet) {
        getLog().info("*** Sct1ArfToEConcept STEP #2 BEGINNING - INGEST ARF ***");
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
            getLog().info(e1);
        } catch (IOException e) {
            getLog().info(e);
        } catch (ParseException e) {
            getLog().info("FAILED: ParseException");
            getLog().info(e);
        }

        getLog().info(
                "*** ARF TO BINARY OBJECT TIME: " + ((System.currentTimeMillis() - start) / 1000)
                + " seconds");
        getLog().info("*** Sct1ArfToEConcept STEP #2 COMPLETED - INGEST ARF ***\r\n");
    }

    private void processArfConFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException, ParseException {
        for (List<ARFFile> laf : listOfDirs) {
            for (ARFFile f : laf) {
                parseArfConFile(f.file, oos);
            }
        }
    }

    private void parseArfConFile(File f, ObjectOutputStream oos) throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f),
                "UTF-8"));

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
            int conceptStatus = lookupZStatusUuidIdx(line[CONCEPT_STATUS]);
            // Primitive
            String isPrimitiveStr = line[ISPRIMITIVE];
            int isPrimitive = 0;
            if (isPrimitiveStr.startsWith("1") || isPrimitiveStr.startsWith("t")
                    || isPrimitiveStr.startsWith("T")) {
                isPrimitive = 1;
            }
            // Effective Date
            long revTime = convertDateStrToTime(line[EFFECTIVE_DATE]);
            // Path UUID
            int pathIdx = lookupZPathIdx(line[PATH_UUID]);

            Sct1_ConRecord tmpConRec = new Sct1_ConRecord(uuidCon, conceptStatus, isPrimitive,
                    revTime, pathIdx);

            oos.writeUnshared(tmpConRec);
        }
        br.close();
    }

    private void processArfDesFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException, ParseException {
        for (List<ARFFile> laf : listOfDirs) {
            for (ARFFile f : laf) {
                parseArfDesFile(f.file, oos);
            }
        }
    }

    private void parseArfDesFile(File f, ObjectOutputStream oos) throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f),
                "UTF-8"));

        int DESCRIPTION_UUID = 0;
        int STATUS_UUID = 1;
        int CONCEPT_UUID = 2;
        int TERM_STRING = 3;
        int CAPITALIZATION_STATUS_INT = 4;
        int DESCRIPTION_TYPE_UUID = 5;
        int LANGUAGE_CODE_STR = 6;
        int EFFECTIVE_DATE = 7;
        int PATH_UUID = 8;

        int RF1_UNSPECIFIED = 0;
        int RF1_PREFERRED = 1;
        int RF1_SYNOMYM = 2;

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // DESCRIPTION_UUID = 0;
            UUID uuidDes = UUID.fromString(line[DESCRIPTION_UUID]);
            // STATUS_UUID = 1;
            int status = lookupZStatusUuidIdx(line[STATUS_UUID]);
            // CONCEPT_UUID = 2;
            UUID uuidCon = UUID.fromString(line[CONCEPT_UUID]);
            // TERM_STRING = 3;
            String termStr = line[TERM_STRING];
            // CAPITALIZATION_STATUS = 4;
            // int capitalization = Integer.parseInt(line[CAPITALIZATION_STATUS_INT]);
            String capitalizationStr = line[CAPITALIZATION_STATUS_INT];
            int capitalization = 0;
            if (capitalizationStr.startsWith("1") || capitalizationStr.startsWith("t")
                    || capitalizationStr.startsWith("T")) {
                capitalization = 1;
            }

            // DESCRIPTION_TYPE = 5;
            int descriptionType = lookupZDesTypeUuidIdx(line[DESCRIPTION_TYPE_UUID]);
            if (rf2Mapping == true
                    && (descriptionType == RF1_UNSPECIFIED || descriptionType == RF1_PREFERRED)) {
                descriptionType = RF1_SYNOMYM;
            }
            // LANGUAGE_CODE = 6;
            String langCodeStr = line[LANGUAGE_CODE_STR];
            // EFFFECTIVE_DATE = 7;
            long revTime = convertDateStrToTime(line[EFFECTIVE_DATE]);
            // PATH_UUID = 8;
            int pathIdx = lookupZPathIdx(line[PATH_UUID]);

            Sct1_DesRecord tmpDesRec = new Sct1_DesRecord(uuidDes, status, uuidCon, termStr,
                    capitalization, descriptionType, langCodeStr, revTime, pathIdx);

            // :DEBUG:
            //            if (debug)
            //            if (tmpDesRec.conUuidMsb == -8120194779924901686L
            //                    && tmpDesRec.conUuidLsb == -6989461898667750587L) {
            //                getLog().info(":DEBUG: ################ " + tmpDesRec.conSnoId);
            //                getLog().info(":DEBUG: ... conSnoId   = " + tmpDesRec.conSnoId);
            //                getLog().info(":DEBUG: ... conUuidLsb = " + tmpDesRec.conUuidLsb);
            //                getLog().info(":DEBUG: ... conUuidMsb = " + tmpDesRec.conUuidMsb);
            //                getLog().info(":DEBUG: ... termText   = " + tmpDesRec.termText);
            //            }
            // :DEBUG:END 

            try {
                oos.writeUnshared(tmpDesRec);
            } catch (Exception e) {
                getLog().info(e);
            }
        }

        br.close();
    }

    private void processArfRelFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException, ParseException {
        for (List<ARFFile> laf : listOfDirs) {
            for (ARFFile f : laf) {
                parseArfRelFile(f.file, oos);
            }
        }
    }

    private void parseArfRelFile(File f, ObjectOutputStream oos) throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f),
                "UTF-8"));

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
            int status = lookupZStatusUuidIdx(line[STATUS_UUID]);
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
            long revTime = convertDateStrToTime(line[EFFECTIVE_DATE]);
            // PATH_UUID = 9;
            int pathIdx = lookupZPathIdx(line[PATH_UUID]);

            Sct1_RelRecord tmpRelRec = new Sct1_RelRecord(uuidRelId, status, uuidC1, roleTypeIdx,
                    uuidC2, characteristic, refinability, group, revTime, pathIdx);

            oos.writeUnshared(tmpRelRec);
        }

        br.close();
    }

    private void processArfIdsFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException, ParseException {
        for (List<ARFFile> laf : listOfDirs) {
            for (ARFFile f : laf) {
                parseArfIdsFile(f.file, oos);
            }
        }
    }

    private void parseArfIdsFile(File f, ObjectOutputStream oos) throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f),
                "UTF-8"));

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
            int status = lookupZStatusUuidIdx(line[STATUS_UUID]);
            // EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
            long revTime = convertDateStrToTime(line[EFFECTIVE_DATE]);
            // PATH_UUID = 5;
            int pathIdx = lookupZPathIdx(line[PATH_UUID]);

            Sct1_IdRecord tmpIdRec = new Sct1_IdRecord(uuidPrimaryId, sourceSystemIdx,
                    idFromSourceSystem, status, revTime, pathIdx, USER_DEFAULT_IDX);

            oos.writeUnshared(tmpIdRec);
        }

        br.close();
    }

    private void processArfRsBoolFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException, ParseException {
        for (List<ARFFile> laf : listOfDirs) {
            for (ARFFile f : laf) {
                parseArfRsBoolFile(f.file, oos);
            }
        }
    }

    private void parseArfRsBoolFile(File f, ObjectOutputStream oos) throws IOException,
            ParseException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f),
                "UTF-8"));

        int REFSEST_UUID = 0;
        int MEMBER_UUID = 1;
        int STATUS_UUID = 2;
        int REFERENCED_COMPONENT_UUID = 3;
        int EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
        int PATH_UUID = 5;
        int EXT_VALUE_UUID = 6;
        int AUTHOR_UUID = 7;

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // REFSEST_UUID = 0;
            UUID uuidRefset = UUID.fromString(line[REFSEST_UUID]);
            // MEMBER_UUID = 1;
            UUID uuidMember = UUID.fromString(line[MEMBER_UUID]);
            // STATUS_UUID = 2;
            int status = lookupZStatusUuidIdx(line[STATUS_UUID]);
            // REFERENCED_COMPONENT_UUID = 3;
            UUID uuidComponent = UUID.fromString(line[REFERENCED_COMPONENT_UUID]);
            // EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
            long revTime = convertDateStrToTime(line[EFFECTIVE_DATE]);
            // PATH_UUID = 5;
            int pathIdx = lookupZPathIdx(line[PATH_UUID]);
            // EXT_VALUE_UUID = 6;
            boolean vBool = false;
            if (line[EXT_VALUE_UUID].charAt(0) == 't' || line[EXT_VALUE_UUID].charAt(0) == 'T') {
                vBool = true;
            }
            // AUTHOR_UUID = 7;
            int authorIdx = -1;
            if (line.length > 7) {
                authorIdx = lookupZAuthorIdx(line[AUTHOR_UUID]);
            }

            // :DEBUG:
            //            if (uuidComponent.equals(UUID.fromString("7c57f6b4-4a63-52ad-b762-73acc15f23de"))) 
            //                getLog().info("FOUND IT");

            Sct1_RefSetRecord tmpRsRec = new Sct1_RefSetRecord(uuidRefset, uuidMember,
                    uuidComponent, status, revTime, pathIdx, vBool, authorIdx);

            statRsBoolFromArf++;
            oos.writeUnshared(tmpRsRec);
        }

        br.close();
    }

    private void processArfRsConFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException, ParseException {
        for (List<ARFFile> laf : listOfDirs) {
            for (ARFFile f : laf) {
                parseArfRsConFile(f.file, oos);
            }
        }
    }

    private void parseArfRsConFile(File f, ObjectOutputStream oos) throws IOException,
            ParseException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f),
                "UTF-8"));

        int REFSEST_UUID = 0;
        int MEMBER_UUID = 1;
        int STATUS_UUID = 2;
        int REFERENCED_COMPONENT_UUID = 3;
        int EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
        int PATH_UUID = 5;
        int EXT_VALUE_UUID = 6;
        int AUTHOR_UUID = 7;

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // REFSEST_UUID = 0;
            UUID uuidRefset = UUID.fromString(line[REFSEST_UUID]);
            // MEMBER_UUID = 1;
            UUID uuidMember = UUID.fromString(line[MEMBER_UUID]);
            // STATUS_UUID = 2;
            int status = lookupZStatusUuidIdx(line[STATUS_UUID]);
            // REFERENCED_COMPONENT_UUID = 3;
            UUID uuidComponent = UUID.fromString(line[REFERENCED_COMPONENT_UUID]);
            // EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
            long revTime = convertDateStrToTime(line[EFFECTIVE_DATE]);
            // PATH_UUID = 5;
            int pathIdx = lookupZPathIdx(line[PATH_UUID]);
            // EXT_VALUE_UUID = 6;
            UUID uuidConExt = UUID.fromString(line[EXT_VALUE_UUID]);
            // AUTHOR_UUID = 7;
            int authorIdx = -1;
            if (line.length > 7) {
                authorIdx = lookupZAuthorIdx(line[AUTHOR_UUID]);
            }

            Sct1_RefSetRecord tmpRsRec = new Sct1_RefSetRecord(uuidRefset, uuidMember,
                    uuidComponent, status, revTime, pathIdx, uuidConExt, authorIdx);

            statRsConFromArf++;
            oos.writeUnshared(tmpRsRec);
        }

        br.close();
    }

    private void processArfRsIntFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException, ParseException {
        for (List<ARFFile> laf : listOfDirs) {
            for (ARFFile f : laf) {
                parseArfRsIntFile(f.file, oos);
            }
        }
    }

    private void parseArfRsIntFile(File f, ObjectOutputStream oos) throws IOException,
            ParseException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f),
                "UTF-8"));

        int REFSEST_UUID = 0;
        int MEMBER_UUID = 1;
        int STATUS_UUID = 2;
        int REFERENCED_COMPONENT_UUID = 3;
        int EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
        int PATH_UUID = 5;
        int EXT_VALUE_UUID = 6;
        int AUTHOR_UUID = 7;

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // REFSEST_UUID = 0;
            UUID uuidRefset = UUID.fromString(line[REFSEST_UUID]);
            // MEMBER_UUID = 1;
            UUID uuidMember = UUID.fromString(line[MEMBER_UUID]);
            // STATUS_UUID = 2;
            int status = lookupZStatusUuidIdx(line[STATUS_UUID]);
            // REFERENCED_COMPONENT_UUID = 3;
            UUID uuidComponent = UUID.fromString(line[REFERENCED_COMPONENT_UUID]);
            // EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
            long revTime = convertDateStrToTime(line[EFFECTIVE_DATE]);
            // PATH_UUID = 5;
            int pathIdx = lookupZPathIdx(line[PATH_UUID]);
            // CONCEPT_EXT_VALUE_UUID = 6;
            int vInt = Integer.valueOf(line[EXT_VALUE_UUID]);
            // AUTHOR_UUID = 7;
            int authorIdx = -1;
            if (line.length > 7) {
                authorIdx = lookupZAuthorIdx(line[AUTHOR_UUID]);
            }

            Sct1_RefSetRecord tmpRsRec = new Sct1_RefSetRecord(uuidRefset, uuidMember,
                    uuidComponent, status, revTime, pathIdx, vInt, authorIdx);

            statRsIntFromArf++;
            oos.writeUnshared(tmpRsRec);
        }

        br.close();
    }

    private void processArfRsStrFiles(String wDir, List<List<ARFFile>> listOfDirs,
            ObjectOutputStream oos) throws IOException, ParseException {
        for (List<ARFFile> laf : listOfDirs) {
            for (ARFFile f : laf) {
                parseArfRsStrFile(f.file, oos);
            }
        }
    }

    private void parseArfRsStrFile(File f, ObjectOutputStream oos) throws IOException,
            ParseException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f),
                "UTF-8"));

        int REFSEST_UUID = 0;
        int MEMBER_UUID = 1;
        int STATUS_UUID = 2;
        int REFERENCED_COMPONENT_UUID = 3;
        int EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
        int PATH_UUID = 5;
        int EXT_VALUE_UUID = 6;
        int AUTHOR_UUID = 7;

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // REFSEST_UUID = 0;
            UUID uuidRefset = UUID.fromString(line[REFSEST_UUID]);
            // MEMBER_UUID = 1;
            UUID uuidMember = UUID.fromString(line[MEMBER_UUID]);
            // STATUS_UUID = 2;
            int status = lookupZStatusUuidIdx(line[STATUS_UUID]);
            // REFERENCED_COMPONENT_UUID = 3;
            UUID uuidComponent = UUID.fromString(line[REFERENCED_COMPONENT_UUID]);
            // EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
            long revTime = convertDateStrToTime(line[EFFECTIVE_DATE]);
            // PATH_UUID = 5;
            int pathIdx = lookupZPathIdx(line[PATH_UUID]);
            // CONCEPT_EXT_VALUE_UUID = 6;
            String vStr = line[EXT_VALUE_UUID];
            // AUTHOR_UUID = 7;
            int authorIdx = -1;
            if (line.length > 7) {
                authorIdx = lookupZAuthorIdx(line[AUTHOR_UUID]);
            }

            Sct1_RefSetRecord tmpRsRec = new Sct1_RefSetRecord(uuidRefset, uuidMember,
                    uuidComponent, status, revTime, pathIdx, vStr, authorIdx);

            statRsStrFromArf++;
            oos.writeUnshared(tmpRsRec);
        }

        br.close();
    }

    private void executeMojoStep3() throws MojoFailureException {
        getLog().info("*** Sct1ArfToEConcept STEP #3 BEGINNING -- GATHER DESTINATION RELs ***");
        long start = System.currentTimeMillis();

        try {
            // read in relationships, sort by C2-ROLETYPE
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(fNameStep1Rel)));
            ArrayList<Sct1_RelRecord> aRel = new ArrayList<Sct1_RelRecord>();

            int count = 0;
            Object obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof Sct1_RelRecord) {
                        aRel.add((Sct1_RelRecord) obj);
                        count++;
                        if (count % 100000 == 0) {
                            getLog().info(" relationship count = " + count);
                        }
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" relationship count = " + count + " @EOF\r\n");
            } catch (ClassNotFoundException e) {
                getLog().info(e);
                throw new MojoFailureException("ClassNotFoundException -- Step 2 reading file");
            }
            ois.close();
            getLog().info(" relationship count = " + count + "\r\n");

            // SORT BY [C2-RoleType]
            Comparator<Sct1_RelRecord> compRelDest = new Comparator<Sct1_RelRecord>() {

                @Override
                public int compare(Sct1_RelRecord o1, Sct1_RelRecord o2) {
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
            for (Sct1_RelRecord r : aRel) {
                if (r.relUuidMsb != lastRelMsb || r.relUuidLsb != lastRelLsb) {
                    oos.writeUnshared(new Sct1_RelDestRecord(r.relUuidMsb, r.relUuidLsb,
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
            getLog().info(e);
        } catch (IOException e) {
            getLog().info(e);
        }

        getLog().info(
                "*** DESTINATION RELs: " + ((System.currentTimeMillis() - start) / 1000)
                + " seconds");
        getLog().info("*** Sct1ArfToEConcept STEP #3 COMPLETED -- GATHER DESTINATION RELs ***\r\n");
    }

    private void executeMojoStep4() throws MojoFailureException {
        getLog().info("*** Sct1ArfToEConcept STEP #4 BEGINNING -- MATCH IDs ***");
        long start = System.currentTimeMillis();
        int nWrite = 0; // counter for memory optimization for object files writing

        try {
            // Read in IDs. Sort by primary uuid
            // *** IDs ***
            ObjectInputStream ois;
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fNameStep1Ids)));
            ArrayList<Sct1_IdRecord> aId = new ArrayList<Sct1_IdRecord>();

            int count = 0;
            Object obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof Sct1_IdRecord) {
                        aId.add((Sct1_IdRecord) obj);
                        count++;
                        if (count % 100000 == 0) {
                            getLog().info(" id count = " + count);
                        }
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
            ArrayList<Sct1_ConRecord> aCon = new ArrayList<Sct1_ConRecord>();

            count = 0;
            obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof Sct1_ConRecord) {
                        aCon.add((Sct1_ConRecord) obj);
                        count++;
                        if (count % 100000 == 0) {
                            getLog().info(" concept count = " + count);
                        }
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
            // PLACE IDs ON FIRST UUID INSTANCE OF CONCEPT
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(fNameStep4Con)));

            int lastIdIdx = aId.size();
            int lastConIdx = aCon.size();
            int theIdIdx = 0;
            int theConIdx = 0;
            while (theIdIdx < lastIdIdx && theConIdx < lastConIdx) {
                Sct1_ConRecord tmpCon = aCon.get(theConIdx);
                int match = checkIdConMatched(aId.get(theIdIdx), tmpCon);

                if (match == 0) {
                    // MATCH
                    if (tmpCon.addedIds == null) {
                        tmpCon.addedIds = new ArrayList<Sct1_IdRecord>();
                    }
                    tmpCon.addedIds.add(aId.get(theIdIdx));
                    theIdIdx++; // Get next id.
                } else if (match == 1) {
                    // Ids are ahead of the concepts.
                    oos.writeUnshared(tmpCon); // Save this concept.
                    theConIdx++; // Get next concept.

                    // PERIODIC RESET IMPROVES MEMORY USE
                    nWrite++;
                    if (nWrite % ooResetInterval == 0) {
                        oos.reset();
                    }
                } else {
                    // Concepts are ahead of the ids.
                    theIdIdx++; // Get the next id.
                }
            }
            while (theConIdx < lastConIdx) {
                oos.writeUnshared(aCon.get(theConIdx)); // Save this concept.
                theConIdx++;

                // PERIODIC RESET IMPROVES MEMORY USE
                nWrite++;
                if (nWrite % ooResetInterval == 0) {
                    oos.reset();
                }
            }
            oos.flush();
            oos.close();
            aCon = null;

            // Read in des.  Sort by des uuid.
            // *** DESCRIPTIONS ***
            ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(fNameStep1Desc)));
            ArrayList<Sct1_DesRecord> aDes = new ArrayList<Sct1_DesRecord>();

            count = 0;
            obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof Sct1_DesRecord) {
                        aDes.add((Sct1_DesRecord) obj);
                        count++;

                        if (count % 100000 == 0) {
                            getLog().info(" description count = " + count);
                        }
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" description count = " + count + " @EOF\r\n");
            }
            ois.close();
            getLog().info(" description count = " + count + "\r\n");

            Collections.sort(aDes);

            // MATCH & ADD ID TO DESCRIPTION
            // PLACE IDs ON FIRST UUID INSTANCE OF DESCRIPTIONS
            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep4Desc)));

            lastIdIdx = aId.size();
            theIdIdx = 0;
            int lastDesIdx = aDes.size();
            int theDesIdx = 0;
            while (theIdIdx < lastIdIdx && theDesIdx < lastDesIdx) {
                Sct1_DesRecord tmpDes = aDes.get(theDesIdx);
                int match = checkIdDesMatched(aId.get(theIdIdx), tmpDes);
                if (match == 0) { // MATCH
                    if (tmpDes.addedIds == null) {
                        tmpDes.addedIds = new ArrayList<Sct1_IdRecord>();
                    }
                    tmpDes.addedIds.add(aId.get(theIdIdx));
                    theIdIdx++; // Get next id.
                } else if (match == 1) { // Ids are ahead of the descriptions.
                    oos.writeUnshared(tmpDes); // Save this description.
                    theDesIdx++; // Get next description.

                    // PERIODIC RESET IMPROVES MEMORY USE
                    nWrite++;
                    if (nWrite % ooResetInterval == 0) {
                        oos.reset();
                    }
                } else { // Descriptions are ahead of the ids.
                    theIdIdx++; // Get the next id.
                }
            }
            while (theDesIdx < lastDesIdx) {
                oos.writeUnshared(aDes.get(theDesIdx)); // Save this concept.
                theDesIdx++;

                // PERIODIC RESET IMPROVES MEMORY USE
                nWrite++;
                if (nWrite % ooResetInterval == 0) {
                    oos.reset();
                }
            }
            oos.flush();
            oos.close();
            aDes = null;

            // Read in rel. Sort by rel uuid.
            // *** RELATIONSHIPS ***
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fNameStep1Rel)));
            ArrayList<Sct1_RelRecord> aRel = new ArrayList<Sct1_RelRecord>();

            count = 0;
            obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof Sct1_RelRecord) {
                        aRel.add((Sct1_RelRecord) obj);
                        count++;
                        if (count % 100000 == 0) {
                            getLog().info(" relationships count = " + count);
                        }
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" relationships count = " + count + " @EOF\r\n");
            }
            ois.close();
            getLog().info(" relationships count = " + count + "\r\n");

            Collections.sort(aRel);

            // MATCH & ADD ID TO RELATIONSHIP
            // PLACE IDs ON FIRST UUID INSTANCE OF RELATIONSHIP
            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep4Rel)));

            theIdIdx = 0;
            lastIdIdx = aId.size();
            int lastRelIdx = aRel.size();
            int theRelIdx = 0;
            while (theIdIdx < lastIdIdx && theRelIdx < lastRelIdx) {
                Sct1_RelRecord tmpRel = aRel.get(theRelIdx);
                int match = checkIdRelMatched(aId.get(theIdIdx), tmpRel);

                if (match == 0) { // MATCH
                    if (tmpRel.addedIds == null) {
                        tmpRel.addedIds = new ArrayList<Sct1_IdRecord>(1);
                    }
                    tmpRel.addedIds.add(aId.get(theIdIdx));
                    theIdIdx++; // Get next id.
                } else if (match == 1) { // Ids are ahead of the relationships.
                    oos.writeUnshared(tmpRel); // Save this relationship.
                    theRelIdx++; // Get next relationship.

                    // PERIODIC RESET IMPROVES MEMORY USE
                    nWrite++;
                    if (nWrite % ooResetInterval == 0) {
                        oos.reset();
                    }
                } else { // Relationships are ahead of the ids.
                    theIdIdx++; // Get the next id.
                }
            }
            while (theRelIdx < lastRelIdx) {
                oos.writeUnshared(aRel.get(theRelIdx)); // Save this concept.
                theRelIdx++;

                // PERIODIC RESET IMPROVES MEMORY USE
                nWrite++;
                if (nWrite % ooResetInterval == 0) {
                    oos.reset();
                }
            }
            oos.flush();
            oos.close();
            aRel = null;

        } catch (FileNotFoundException e) {
            getLog().info(e);
            throw new MojoFailureException("FileNotFoundException");
        } catch (IOException e) {
            getLog().info(e);
            throw new MojoFailureException("IOException");
        } catch (ClassNotFoundException e) {
            getLog().info(e);
            throw new MojoFailureException("ClassNotFoundException");
        }

        getLog().info(
                "*** ATTACH IDs TIME: " + ((System.currentTimeMillis() - start) / 1000)
                + " seconds");
        getLog().info("*** Sct1ArfToEConcept STEP #4 COMPLETED - MATCH IDs ***\r\n");
    }

    private TkIdentifier createEIdentifier(Sct1_IdRecord id) {
        if (id.denotation != null) {
            return createEIdentifierString(id);
        } else {
            return createEIdentifierLong(id);
        }
    }

    private TkIdentifier createEIdentifierString(Sct1_IdRecord id) {
        EIdentifierString eId = new EIdentifierString();
        eId.setAuthorityUuid(lookupSrcSystemUUID(id.srcSystemIdx));

        eId.setDenotation(id.denotation);

        // PATH
        long msb = zPathArray[id.pathIdx].getMostSignificantBits();
        long lsb = zPathArray[id.pathIdx].getLeastSignificantBits();
        eId.setPathUuid(new UUID(msb, lsb));

        // STATUS
        msb = zStatusUuidArray[id.status].getMostSignificantBits();
        lsb = zStatusUuidArray[id.status].getLeastSignificantBits();
        eId.setStatusUuid(new UUID(msb, lsb));

        // VERSION (REVISION TIME)
        eId.setTime(id.revTime);

        // USER
        if (id.userIdx == USER_SNOROCKET_IDX) {
            eId.setAuthorUuid(uuidUserSnorocket);
        } else {
            eId.setAuthorUuid(uuidUser);
        }

        return eId;
    }

    private TkIdentifier createEIdentifierLong(Sct1_IdRecord id) {
        EIdentifierLong eId = new EIdentifierLong();
        eId.setAuthorityUuid(lookupSrcSystemUUID(id.srcSystemIdx));

        eId.setDenotation(id.denotationLong);

        // PATH
        long msb = zPathArray[id.pathIdx].getMostSignificantBits();
        long lsb = zPathArray[id.pathIdx].getLeastSignificantBits();
        eId.setPathUuid(new UUID(msb, lsb));

        // STATUS
        msb = zStatusUuidArray[id.status].getMostSignificantBits();
        lsb = zStatusUuidArray[id.status].getLeastSignificantBits();
        eId.setStatusUuid(new UUID(msb, lsb));

        // VERSION (REVISION TIME)
        eId.setTime(id.revTime);

        // USER
        if (id.userIdx == USER_SNOROCKET_IDX) {
            eId.setAuthorUuid(uuidUserSnorocket);
        } else {
            eId.setAuthorUuid(uuidUser);
        }

        return eId;
    }

    private int checkIdConMatched(Sct1_IdRecord id, Sct1_ConRecord con) {
        final int BEFORE = -1;
        final int MATCH = 0;
        final int AFTER = 1;

        if (id.primaryUuidMsb > con.conUuidMsb) {
            return AFTER;
        } else if (id.primaryUuidMsb == con.conUuidMsb && id.primaryUuidLsb > con.conUuidLsb) {
            return AFTER;
        }

        if (id.primaryUuidMsb == con.conUuidMsb && id.primaryUuidLsb == con.conUuidLsb) {
            return MATCH;
        }

        return BEFORE;
    }

    private int checkIdDesMatched(Sct1_IdRecord id, Sct1_DesRecord des) {
        final int BEFORE = -1;
        final int MATCH = 0;
        final int AFTER = 1;

        if (id.primaryUuidMsb > des.desUuidMsb) {
            return AFTER;
        } else if (id.primaryUuidMsb == des.desUuidMsb && id.primaryUuidLsb > des.desUuidLsb) {
            return AFTER;
        }

        if (id.primaryUuidMsb == des.desUuidMsb && id.primaryUuidLsb == des.desUuidLsb) {
            return MATCH;
        }

        return BEFORE;
    }

    private int checkIdRelMatched(Sct1_IdRecord id, Sct1_RelRecord rel) {
        final int BEFORE = -1;
        final int MATCH = 0;
        final int AFTER = 1;

        if (id.primaryUuidMsb > rel.relUuidMsb) {
            return AFTER;
        } else if (id.primaryUuidMsb == rel.relUuidMsb && id.primaryUuidLsb > rel.relUuidLsb) {
            return AFTER;
        }

        if (id.primaryUuidMsb == rel.relUuidMsb && id.primaryUuidLsb == rel.relUuidLsb) {
            return MATCH;
        }

        return BEFORE;
    }

    private void executeMojoStep5() {
        getLog().info("*** Sct1ArfToEConcept Step #5 BEGINNING -- REFSET ATTACHMENT ***");
        long start = System.currentTimeMillis();

        try {
            // *** READ IN REFSET ***
            int numObj = countFileObjects(fNameStep2Refset);

            ObjectInputStream ois;
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                    fNameStep2Refset)));
            ArrayList<Sct1_RefSetRecord> aRs = new ArrayList<Sct1_RefSetRecord>(numObj);

            int count = 0;
            Object obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof Sct1_RefSetRecord) {
                        aRs.add((Sct1_RefSetRecord) obj);
                        count++;
                        if (count % 100000 == 0) {
                            getLog().info(" refset member in = " + count);
                        }
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" refset member in = " + count + " @EOF\r\n");
            }
            ois.close();
            getLog().info(" refset member in = " + count + "\r\n");

            // Sort by [COMPONENTID]
            Collections.sort(aRs);
            int aRsMax = aRs.size();

            // ATTACH ENVELOPE CONCEPTS (3 PASS)
            // *** CONCEPTS ***
            int idxRsA = 0;
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fNameStep4Con)));
            try {
                count = 0;
                obj = ois.readUnshared();
                while (obj != null && idxRsA < aRsMax) {
                    Sct1_RefSetRecord rsRec = aRs.get(idxRsA);
                    Sct1_ConRecord conRec = (Sct1_ConRecord) obj;
                    int rsVin = compareMsbLsb(rsRec.referencedComponentUuidMsb,
                            rsRec.referencedComponentUuidLsb, conRec.conUuidMsb, conRec.conUuidLsb);

                    if (rsVin == 0) {
                        rsRec.conUuidMsb = conRec.conUuidMsb;
                        rsRec.conUuidLsb = conRec.conUuidLsb;
                        rsRec.componentType = Sct1_RefSetRecord.ComponentType.CONCEPT;
                        idxRsA++;
                    } else if (rsVin > 0) {
                        obj = ois.readUnshared();
                        count++;
                        if (count % 100000 == 0) {
                            getLog().info(" concept count = " + count);
                        }
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
                obj = ois.readUnshared();
                while (obj != null && idxRsA < aRsMax) {
                    Sct1_RefSetRecord rsRec = aRs.get(idxRsA);
                    Sct1_DesRecord desRec = (Sct1_DesRecord) obj;

                    int rsVin = compareMsbLsb(rsRec.referencedComponentUuidMsb,
                            rsRec.referencedComponentUuidLsb, desRec.desUuidMsb, desRec.desUuidLsb);

                    if (rsVin == 0) {
                        if (rsRec.conUuidMsb != Long.MAX_VALUE) {
                            getLog().info(
                                    "ERROR: Refset Envelop UUID Concept/Description conflict"
                                    + "\r\nExisting UUID:"
                                    + new UUID(rsRec.conUuidMsb, rsRec.conUuidLsb)
                                    + "\r\nDescription UUID:"
                                    + new UUID(desRec.desUuidMsb, desRec.desUuidLsb));
                        }

                        rsRec.conUuidMsb = desRec.conUuidMsb;
                        rsRec.conUuidLsb = desRec.conUuidLsb;
                        rsRec.componentType = Sct1_RefSetRecord.ComponentType.DESCRIPTION;
                        idxRsA++;
                    } else if (rsVin > 0) {
                        obj = ois.readUnshared();
                        count++;
                        if (count % 100000 == 0) {
                            getLog().info(" description count = " + count);
                        }
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
                obj = ois.readUnshared();
                while (obj != null && idxRsA < aRsMax) {
                    Sct1_RefSetRecord rsRec = aRs.get(idxRsA);
                    Sct1_RelRecord relRec = (Sct1_RelRecord) obj;
                    int rsVin = compareMsbLsb(rsRec.referencedComponentUuidMsb,
                            rsRec.referencedComponentUuidLsb, relRec.relUuidMsb, relRec.relUuidLsb);

                    if (rsVin == 0) {
                        if (rsRec.conUuidMsb != Long.MAX_VALUE) {
                            getLog().info(
                                    "ERROR: Refset Envelop UUID Concept/Relationship conflict"
                                    + "\r\nExisting UUID:"
                                    + new UUID(rsRec.conUuidMsb, rsRec.conUuidLsb)
                                    + "\r\nRelationship UUID:"
                                    + new UUID(relRec.relUuidMsb, relRec.relUuidLsb));
                        }

                        rsRec.conUuidMsb = relRec.c1UuidMsb;
                        rsRec.conUuidLsb = relRec.c1UuidLsb;
                        rsRec.componentType = Sct1_RefSetRecord.ComponentType.RELATIONSHIP;
                        idxRsA++;
                    } else if (rsVin > 0) {
                        obj = ois.readUnshared();
                        count++;
                        if (count % 100000 == 0) {
                            getLog().info(" relationship count = " + count);
                        }
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
            ArrayList<Sct1_RefSetRecord> bRs = new ArrayList<Sct1_RefSetRecord>(aRs);
            Comparator<Sct1_RefSetRecord> compRsByRs = new Comparator<Sct1_RefSetRecord>() {

                @Override
                public int compare(Sct1_RefSetRecord o1, Sct1_RefSetRecord o2) {
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
                            if (o1.pathIdx > o2.pathIdx) {
                                return thisMore;
                            } else if (o1.pathIdx < o2.pathIdx) {
                                return thisLess;
                            } else {
                                // Revision
                                if (o1.revTime > o2.revTime) {
                                    return thisMore;
                                } else if (o1.revTime < o2.revTime) {
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
                Sct1_RefSetRecord rsRecA = aRs.get(idxRsA);
                Sct1_RefSetRecord rsRecB = bRs.get(idxRsB);
                int rsVin = compareMsbLsb(rsRecA.referencedComponentUuidMsb,
                        rsRecA.referencedComponentUuidLsb, rsRecB.refsetUuidMsb,
                        rsRecB.refsetUuidLsb);

                if (rsVin == 0) {
                    if (rsRecA.conUuidMsb != Long.MAX_VALUE) {
                        getLog().info(
                                "ERROR: Refset Envelop UUID Concept/Refset conflict"
                                + "\r\nExisting UUID:"
                                + new UUID(rsRecA.conUuidMsb, rsRecA.conUuidLsb)
                                + "\r\nRefset UUID:"
                                + new UUID(rsRecB.refsetUuidMsb, rsRecB.refsetUuidLsb));
                    }

                    rsRecA.conUuidMsb = rsRecB.refsetUuidMsb;
                    rsRecA.conUuidLsb = rsRecB.refsetUuidLsb;
                    rsRecA.componentType = Sct1_RefSetRecord.ComponentType.MEMBER;
                    idxRsA++;
                } else if (rsVin > 0) {
                    idxRsB++;
                    count++;
                    if (count % 100000 == 0) {
                        getLog().info(" refset count = " + count);
                    }
                } else {
                    idxRsA++;
                }
            }

            // SAVE FILE SORTED BY "ENVELOP eConcept" UUID, path, revision
            Comparator<Sct1_RefSetRecord> compRsByCon = new Comparator<Sct1_RefSetRecord>() {

                @Override
                public int compare(Sct1_RefSetRecord o1, Sct1_RefSetRecord o2) {
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
                            if (o1.pathIdx > o2.pathIdx) {
                                return thisMore;
                            } else if (o1.pathIdx < o2.pathIdx) {
                                return thisLess;
                            } else {
                                // Revision
                                if (o1.revTime > o2.revTime) {
                                    return thisMore;
                                } else if (o1.revTime < o2.revTime) {
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
            for (Sct1_RefSetRecord r : aRs) {
                oos.writeUnshared(r);
            }
            oos.flush();
            oos.close();

            // SAVE FILE SORTED BY REFSET UUID
            Collections.sort(aRs, compRsByRs);
            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    fNameStep5RsByRs)));
            for (Sct1_RefSetRecord r : aRs) {
                oos.writeUnshared(r);
            }
            oos.flush();
            oos.close();

            // :NYI: check to see if any refset member remained unassigned. 

            aRs = null;
            System.gc();

        } catch (FileNotFoundException e) {
            getLog().info(e);
        } catch (IOException e) {
            getLog().info(e);
        } catch (ClassNotFoundException e) {
            getLog().info(e);
        }

        getLog().info(
                "*** MASTER SORT TIME: " + ((System.currentTimeMillis() - start) / 1000)
                + " seconds");
        getLog().info("*** Sct1ArfToEConcept Step #5 COMPLETED -- REFSET ATTACHMENT ***\r\n");
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

    // :NYI: concepts may not need to be sorted again after previous step.
    private void executeMojoStep6() throws MojoFailureException {
        getLog().info("*** Sct1ArfToEConcept Step #6 BEGINNING -- SORT BY CONCEPT ***");
        long start = System.currentTimeMillis();
        try {

            // *** CONCEPTS ***
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(fNameStep4Con)));
            ArrayList<Sct1_ConRecord> aCon = new ArrayList<Sct1_ConRecord>();

            int count = 0;
            Object obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof Sct1_ConRecord) {
                        aCon.add((Sct1_ConRecord) obj);
                        count++;
                        if (count % 100000 == 0) {
                            getLog().info(" concept count = " + count);
                        }
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" concept count = " + count + " @EOF\r\n");
            }
            ois.close();
            getLog().info(" concept count = " + count + "\r\n");

            // SORT BY [CONCEPTID, Path, Revision]
            Comparator<Sct1_ConRecord> compCon = new Comparator<Sct1_ConRecord>() {

                @Override
                public int compare(Sct1_ConRecord o1, Sct1_ConRecord o2) {
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
                            if (o1.path > o2.path) {
                                return thisMore;
                            } else if (o1.path < o2.path) {
                                return thisLess;
                            } else {
                                // Revision
                                if (o1.revTime > o2.revTime) {
                                    return thisMore;
                                } else if (o1.revTime < o2.revTime) {
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
            for (Sct1_ConRecord r : aCon) {
                oos.writeUnshared(r);
            }
            oos.flush();
            oos.close();
            aCon = null;
            System.gc();

            // *** DESCRIPTIONS ***
            ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(fNameStep4Desc)));
            ArrayList<Sct1_DesRecord> aDes = new ArrayList<Sct1_DesRecord>();

            count = 0;
            obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof Sct1_DesRecord) {
                        aDes.add((Sct1_DesRecord) obj);
                        count++;
                        if (count % 100000 == 0) {
                            getLog().info(" description count = " + count);
                        }
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" description count = " + count + " @EOF\r\n");
            }
            ois.close();
            getLog().info(" description count = " + count + "\r\n");

            // SORT BY [CONCEPTID, DESCRIPTIONID, Path, Revision]
            Comparator<Sct1_DesRecord> compDes = new Comparator<Sct1_DesRecord>() {

                @Override
                public int compare(Sct1_DesRecord o1, Sct1_DesRecord o2) {
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
                                    if (o1.pathIdx > o2.pathIdx) {
                                        return thisMore;
                                    } else if (o1.pathIdx < o2.pathIdx) {
                                        return thisLess;
                                    } else {
                                        // Revision
                                        if (o1.revTime > o2.revTime) {
                                            return thisMore;
                                        } else if (o1.revTime < o2.revTime) {
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
            for (Sct1_DesRecord r : aDes) {
                oos.writeUnshared(r);
            }

            oos.flush();
            oos.close();
            aDes = null;
            System.gc();

            // *** RELATIONSHIPS ***
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fNameStep4Rel)));
            ArrayList<Sct1_RelRecord> aRel = new ArrayList<Sct1_RelRecord>();

            count = 0;
            obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof Sct1_RelRecord) {
                        aRel.add((Sct1_RelRecord) obj);
                        count++;
                        if (count % 100000 == 0) {
                            getLog().info(" relationships count = " + count);
                        }
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" relationships count = " + count + "\r\n");
            }
            ois.close();
            getLog().info(" relationships count = " + count + " @EOF\r\n");

            // SORT BY [C1-Group-RoleType-Path-RevisionVersion]
            Comparator<Sct1_RelRecord> compRel = new Comparator<Sct1_RelRecord>() {

                @Override
                public int compare(Sct1_RelRecord o1, Sct1_RelRecord o2) {
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
                                            if (o1.pathIdx > o2.pathIdx) {
                                                return thisMore;
                                            } else if (o1.pathIdx < o2.pathIdx) {
                                                return thisLess;
                                            } else {
                                                // VERSION
                                                if (o1.revTime > o2.revTime) {
                                                    return thisMore;
                                                } else if (o1.revTime < o2.revTime) {
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
            for (Sct1_RelRecord r : aRel) {
                oos.writeUnshared(r);
            }
            oos.flush();
            oos.close();
            aRel = null;

            // ** DESTINATION RELATIONSHIPS **
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                    fNameStep3RelDest)));
            ArrayList<Sct1_RelDestRecord> aRelDest = new ArrayList<Sct1_RelDestRecord>();

            count = 0;
            obj = null;
            try {
                while ((obj = ois.readObject()) != null) {
                    if (obj instanceof Sct1_RelDestRecord) {
                        aRelDest.add((Sct1_RelDestRecord) obj);
                        count++;
                        if (count % 100000 == 0) {
                            getLog().info(" destination relationships count = " + count);
                        }
                    }
                }
            } catch (EOFException ex) {
                getLog().info(" destination relationships count = " + count + "\r\n");
            }
            ois.close();
            getLog().info(" destination relationships count = " + count + " @EOF\r\n");

            // SORT BY [C2-RoleType]
            Comparator<Sct1_RelDestRecord> compRelDest = new Comparator<Sct1_RelDestRecord>() {

                @Override
                public int compare(Sct1_RelDestRecord o1, Sct1_RelDestRecord o2) {
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
            for (Sct1_RelDestRecord r : aRelDest) {
                oos.writeUnshared(r);
            }
            oos.flush();
            oos.close();
            aRelDest = null;

            System.gc();

        } catch (FileNotFoundException e) {
            getLog().info(e);
            throw new MojoFailureException("File Not Found -- Step 6 Sort");
        } catch (IOException e) {
            getLog().info(e);
            throw new MojoFailureException("IO Exception -- Step 6 Sort");
        } catch (ClassNotFoundException e) {
            getLog().info(e);
            throw new MojoFailureException("ClassNotFoundException -- Step 6 Sort");
        }
        getLog().info(
                "*** MASTER SORT TIME: " + ((System.currentTimeMillis() - start) / 1000)
                + " seconds");
        getLog().info("*** Sct1ArfToEConcept Step #6 COMPLETED -- SORT BY CONCEPT ***\r\n");
    }

    /**
     * executeMojoStep6() reads concepts, descriptions, relationship, 
     * destination relationships, & ids files in concept order.
     * 
     * @throws MojoFailureException
     * @throws IOException 
     */
    private void executeMojoStep7() throws MojoFailureException, IOException {
        statCon = 0;
        statDes = 0;
        statRel = 0;
        statRelDest = 0;
        statRsByCon = 0;
        statRsByRs = 0;

        getLog().info("*** Sct1ArfToEConcept STEP #7 BEGINNING -- CREATE eCONCEPTS ***");
        long start = System.currentTimeMillis();
        countEConWritten = 0;

        // Lists hold records for the immediate operations 
        ArrayList<Sct1_ConRecord> conList = new ArrayList<Sct1_ConRecord>();
        ArrayList<Sct1_DesRecord> desList = new ArrayList<Sct1_DesRecord>();
        ArrayList<Sct1_RelRecord> relList = new ArrayList<Sct1_RelRecord>();
        ArrayList<Sct1_RelDestRecord> relDestList = new ArrayList<Sct1_RelDestRecord>();
        ArrayList<Sct1_RefSetRecord> rsByConList = new ArrayList<Sct1_RefSetRecord>();
        ArrayList<Sct1_RefSetRecord> rsByRsList = new ArrayList<Sct1_RefSetRecord>();

        // Since readObject must look one record ahead,
        // the look ahead record is stored as "Next"
        Sct1_ConRecord conNext = null;
        Sct1_DesRecord desNext = null;
        Sct1_RelRecord relNext = null;
        Sct1_RelDestRecord relDestNext = null;
        Sct1_RefSetRecord rsByConNext = null;
        Sct1_RefSetRecord rsByRsNext = null;

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
            getLog().info(e);
            throw new MojoFailureException("File Not Found -- Step #7");
        } catch (IOException e) {
            getLog().info(e);
            throw new MojoFailureException("IO Exception -- Step #7");
        }

        // :DEBUG:
        //        boolean readMoreBug = true;
        //        boolean nextBug = false;
        //        int bugCount = 0;
        //        while (readMoreBug) {
        //            Object bugO;
        //            try {
        //                bugO = oisDes.readUnshared()
        //                if (bugO instanceof Sct1_DesRecord || nextBug == true) {
        //                    Sct1_DesRecord bugDes = (Sct1_DesRecord) bugO;
        //                    if (bugDes.conUuidMsb == -8120194779924901686L
        //                            && bugDes.conUuidLsb == -6989461898667750587L) {
        //                        getLog().info(":DEBUG: ...  ## count ## " + bugCount);
        //                        getLog().info(":DEBUG: ... conSnoId   = " + bugDes.conSnoId);
        //                        getLog().info(":DEBUG: ... conUuidLsb = " + bugDes.conUuidLsb);
        //                        getLog().info(":DEBUG: ... conUuidMsb = " + bugDes.conUuidMsb);
        //                        getLog().info(":DEBUG: ... termText   = " + bugDes.termText);
        //                        nextBug = !nextBug;
        //                    }
        //                } else 
        //                    readMoreBug = false;
        //                    
        //            } catch (IOException e) {
        //                getLog().info(e);
        //                readMoreBug = false;
        //            } catch (ClassNotFoundException e) {
        //                getLog().info(e);
        //                readMoreBug = false;
        //            }
        //            bugCount++;
        //        } 
        // :DEBUG:END 

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
            Sct1_ConRecord tmpConRec = conList.get(0);
            theCon = new UUID(tmpConRec.conUuidMsb, tmpConRec.conUuidLsb);
            countCon++;

            while (theDes.compareTo(theCon) == IS_LESS) {
                desNext = readNextDes(oisDes, desList, desNext);
                if (desNext == null && desList.isEmpty()) {
                    theDes = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
                } else {
                    Sct1_DesRecord tmpDes = desList.get(0);
                    theDes = new UUID(tmpDes.conUuidMsb, tmpDes.conUuidLsb);
                    countDes++;
                    if (theDes.compareTo(theCon) == IS_LESS) {
                        getLog().info("ORPHAN DESCRIPTION :: " + desList.get(0).termText);
                    }
                }
            }

            while (theRel.compareTo(theCon) == IS_LESS) {
                relNext = readNextRel(oisRel, relList, relNext);
                if (relNext == null && relList.isEmpty()) {
                    theRel = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
                } else {
                    // theRel = relList.get(0).c1SnoId;
                    Sct1_RelRecord tmpRel = relList.get(0);
                    theRel = new UUID(tmpRel.c1UuidMsb, tmpRel.c1UuidLsb);
                    countRel++;
                    if (theRel.compareTo(theCon) == IS_LESS) {
                        getLog().info(
                                "ORPHAN RELATIONSHIP :: relid=" + relList.get(0).relSnoId + " c1=="
                                + relList.get(0).c1SnoId);
                    }
                }
            }

            while (theRelDest.compareTo(theCon) == IS_LESS) {
                relDestNext = readNextRelDest(oisRelDest, relDestList, relDestNext);
                if (relDestNext == null && relDestList.isEmpty()) {
                    theRelDest = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
                } else {
                    // theRelDest = relDestList.get(0).c2SnoId;
                    Sct1_RelDestRecord tmpRelDest = relDestList.get(0);
                    theRelDest = new UUID(tmpRelDest.c2UuidMsb, tmpRelDest.c2UuidLsb);
                    countRelDest++;
                    if (theRelDest.compareTo(theCon) == IS_LESS) {
                        getLog().info(
                                "ORPHAN DEST. RELATIONSHIP :: relid="
                                + relList.get(0).relSnoId
                                + " c2=="
                                + new UUID(relDestList.get(0).c2UuidMsb,
                                relDestList.get(0).c2UuidLsb));
                    }
                }
            }

            while (theRsByCon.compareTo(theCon) == IS_LESS) {
                rsByConNext = readNextRsByCon(oisRsByCon, rsByConList, rsByConNext);

                if (rsByConNext == null && rsByConList.isEmpty()) {
                    theRsByCon = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
                } else {
                    Sct1_RefSetRecord tmpRsByCon = rsByConList.get(0);
                    theRsByCon = new UUID(tmpRsByCon.conUuidMsb, tmpRsByCon.conUuidLsb);
                    countRsByCon++;
                    if (theRsByCon.compareTo(theCon) == IS_LESS) {
                        getLog().info(
                                "ORPHAN REFSET MEMBER RECORD_A :: "
                                + new UUID(rsByConList.get(0).refsetMemberUuidMsb,
                                rsByConList.get(0).refsetMemberUuidLsb));
                    }
                }
            }

            while (theRsByRs.compareTo(theCon) == IS_LESS) {
                rsByRsNext = readNextRsByRs(oisRsByRs, rsByRsList, rsByRsNext);
                // :DEBUG:
                //                UUID debugUuidRsByCon111 = UUID.fromString("ccbd4a65-9b1a-5df3-94d1-4a1085f3c758");

                if (rsByRsNext == null && rsByRsList.isEmpty()) {
                    theRsByRs = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
                } else {
                    Sct1_RefSetRecord tmpRsByRs = rsByRsList.get(0);
                    theRsByRs = new UUID(tmpRsByRs.refsetUuidMsb, tmpRsByRs.refsetUuidLsb);
                    countRsByRs++;
                    if (theRsByRs.compareTo(theCon) == IS_LESS) {
                        getLog().info(
                                "ORPHAN REFSET MEMBER RECORD_B :: "
                                + new UUID(rsByRsList.get(0).refsetMemberUuidMsb,
                                rsByRsList.get(0).refsetMemberUuidLsb));
                    }
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

            ArrayList<Sct1_RefSetRecord> addRsByCon = null;
            if (theCon.compareTo(theRsByCon) == IS_EQUAL) {
                addRsByCon = rsByConList;
            }
            ArrayList<Sct1_RefSetRecord> addRsByRs = null;
            if (theCon.compareTo(theRsByRs) == IS_EQUAL) {
                addRsByRs = rsByRsList;
            }

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
                //                if (debug) {
                //                    getLog().info(
                //                            "!!! Case what case is this??? -- Step 4" + " theCon=\t" + theCon
                //                                    + "\ttheDes=\t" + theDes + "\ttheRel=\t" + theRel
                //                                    + "\ttheRelDest\t" + theRelDest);
                //                    getLog().info("!!! --- concept UUID id   =" + theCon);
                //                    getLog().info("!!! --- concept SNOMED id =" + conList.get(0).conSnoId);
                //                    
                //                    getLog().info("!!! --- concept counter   #" + countCon);
                //                    getLog().info("!!! --- description       \"" + desList.get(0).termText + "\"");
                //                    getLog().info("!!! \r\n");
                //                }
                throw new MojoFailureException("Case not implemented -- executeMojoStep7()");
            }

            if (conNext == null && desNext == null && relNext == null) {
                notDone = false;
            }

            prevCon = theCon;
            prevDes = theDes;
            prevRel = theRel;
            prevRelDest = theRelDest;
            prevRsByCon = theRsByCon;
            prevRsByRs = theRsByRs;

            if ((addRsByRs != null) && (addRsByRs.size() > 4096)) {
                System.gc();
            }
            if (countCon % 500000 == 0) {
                System.gc();
            }

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
            oisRelDest.close();
            oisRsByCon.close();
            oisRsByRs.close();
            dos.close();
        } catch (IOException e) {
            getLog().info(e);
            throw new MojoFailureException("IO Exception -- Step 4, closing files");
        }
        getLog().info(
                "*** ECONCEPT CREATION TIME: " + ((System.currentTimeMillis() - start) / 1000)
                + " seconds");
        getLog().info("*** ECONCEPTS WRITTEN TO FILE = " + countEConWritten);
        getLog().info("*** Sct1ArfToEConcept STEP #7 COMPLETED -- CREATE eCONCEPTS ***\r\n");
    }

    // ICD-0-3 == cff53f1a-1d11-5ae7-801e-d3301cfdbea0
    //private static final UUID debugUuid01 = UUID.fromString("cff53f1a-1d11-5ae7-801e-d3301cfdbea0");
    // UUID OF INTEREST
    //private static final UUID debugUuid02 = UUID.fromString("daa9598a-2ddb-5527-beda-ee4303a7656c");
    //private static final UUID debugUuid03 = UUID.fromString("3ca0d065-06b8-596c-8ca0-e4d2a605701c");
    private void createEConcept(ArrayList<Sct1_ConRecord> conList,
            ArrayList<Sct1_DesRecord> desList, ArrayList<Sct1_RelRecord> relList,
            ArrayList<Sct1_RelDestRecord> relDestList, ArrayList<Sct1_RefSetRecord> rsByConList,
            ArrayList<Sct1_RefSetRecord> rsByRsList, DataOutputStream dos)
            throws MojoFailureException {
        if (conList.size() < 1) {
            throw new MojoFailureException("createEConcept(), empty conList");
        }

        statCon++;
        if (desList != null) {
            statDes += desList.size();
        }
        if (relList != null) {
            statRel += relList.size();
        }
        if (relDestList != null) {
            statRelDest += relDestList.size();
        }
        if (rsByConList != null) {
            statRsByCon += rsByConList.size();
        }
        if (rsByRsList != null) {
            statRsByRs += rsByRsList.size();
        }

        Collections.sort(conList);
        Sct1_ConRecord cRec0 = conList.get(0);
        UUID theConUUID = new UUID(cRec0.conUuidMsb, cRec0.conUuidLsb);

        EConcept ec = new EConcept();
        ec.setPrimordialUuid(theConUUID);

        // ADD CONCEPT ATTRIBUTES        
        EConceptAttributes ca = new EConceptAttributes();
        ca.primordialUuid = theConUUID;
        ca.setDefined(cRec0.isprimitive == 0 ? true : false);
        ca.setAuthorUuid(uuidUser);

        ArrayList<TkIdentifier> tmpAdditionalIds = new ArrayList<TkIdentifier>();

        // SNOMED ID, if present
        if (cRec0.conSnoId < Long.MAX_VALUE) {
            EIdentifierLong cid = new EIdentifierLong();
            cid.setAuthorityUuid(uuidSourceSnomedLong);
            cid.setDenotation(cRec0.conSnoId);
            cid.setPathUuid(zPathArray[cRec0.path]);
            cid.setStatusUuid(uuidCurrent);
            cid.setTime(cRec0.revTime);
            tmpAdditionalIds.add(cid);
        }
        // CTV 3 ID, if present
        if (cRec0.ctv3id != null) {
            EIdentifierString cids = new EIdentifierString();
            cids.setAuthorityUuid(uuidSourceCtv3);
            cids.setDenotation(cRec0.ctv3id);
            cids.setPathUuid(zPathArray[cRec0.path]);
            cids.setStatusUuid(uuidCurrent);
            cids.setTime(cRec0.revTime);
            tmpAdditionalIds.add(cids);
        }
        // SNOMED RT ID, if present
        if (cRec0.snomedrtid != null) {
            EIdentifierString cids = new EIdentifierString();
            cids.setAuthorityUuid(uuidSourceSnomedRt);
            cids.setDenotation(cRec0.snomedrtid);
            cids.setPathUuid(zPathArray[cRec0.path]);
            cids.setStatusUuid(uuidCurrent);
            cids.setTime(cRec0.revTime);
            tmpAdditionalIds.add(cids);
        }
        if (cRec0.addedIds != null) {
            for (Sct1_IdRecord eId : cRec0.addedIds) {
                tmpAdditionalIds.add(createEIdentifier(eId));
            }
        }

        if (tmpAdditionalIds.size() > 0) {
            ca.additionalIds = tmpAdditionalIds;
        } else {
            ca.additionalIds = null;
        }

        ca.setStatusUuid(zStatusUuidArray[cRec0.status]);
        ca.setPathUuid(zPathArray[cRec0.path]);
        ca.setTime(cRec0.revTime); // long

        int max = conList.size();
        List<TkConceptAttributesRevision> caRevisions = new ArrayList<TkConceptAttributesRevision>();
        for (int i = 1; i < max; i++) {
            EConceptAttributesRevision rev = new EConceptAttributesRevision();
            Sct1_ConRecord cRec = conList.get(i);
            rev.setDefined(cRec.isprimitive == 0 ? true : false);
            rev.setStatusUuid(zStatusUuidArray[cRec.status]);
            rev.setPathUuid(zPathArray[cRec.path]);
            rev.setTime(cRec.revTime);
            caRevisions.add(rev);
        }

        if (caRevisions.size() > 0) {
            ca.revisions = caRevisions;
        } else {
            ca.revisions = null;
        }
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
            for (Sct1_DesRecord dRec : desList) {
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
                        did.setAuthorityUuid(uuidSourceSnomedLong);
                        did.setDenotation(dRec.desSnoId);
                        did.setPathUuid(zPathArray[dRec.pathIdx]);
                        did.setStatusUuid(uuidCurrent);
                        did.setTime(dRec.revTime);
                        tmpDesAdditionalIds.add(did);
                    }
                    if (dRec.addedIds != null) {
                        for (Sct1_IdRecord eId : dRec.addedIds) {
                            tmpDesAdditionalIds.add(createEIdentifier(eId));
                        }
                    }
                    if (tmpDesAdditionalIds.size() > 0) {
                        des.additionalIds = tmpDesAdditionalIds;
                    } else {
                        des.additionalIds = null;
                    }

                    theDesMsb = dRec.desUuidMsb;
                    theDesLsb = dRec.desUuidLsb;
                    des.setPrimordialComponentUuid(new UUID(theDesMsb, theDesLsb));
                    des.setConceptUuid(theConUUID);
                    des.setText(dRec.termText);
                    des.setInitialCaseSignificant(dRec.capStatus == 1 ? true : false);
                    des.setLang(dRec.languageCode);
                    des.setTypeUuid(zDesTypeUuidArray[dRec.descriptionType]);
                    des.setStatusUuid(zStatusUuidArray[dRec.status]);
                    des.setPathUuid(zPathArray[dRec.pathIdx]);
                    des.setTime(dRec.revTime);
                    des.revisions = null;
                } else {
                    EDescriptionRevision edv = new EDescriptionRevision();
                    edv.setText(dRec.termText);
                    edv.setTypeUuid(zDesTypeUuidArray[dRec.descriptionType]);
                    edv.setInitialCaseSignificant(dRec.capStatus == 1 ? true : false);
                    edv.setLang(dRec.languageCode);
                    edv.setStatusUuid(zStatusUuidArray[dRec.status]);
                    edv.setPathUuid(zPathArray[dRec.pathIdx]);
                    edv.setTime(dRec.revTime);
                    revisions.add(edv);
                }
            }
            if (des != null && revisions.size() > 0) {
                des.revisions = revisions;
            }
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
            for (Sct1_RelRecord rRec : relList) {
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

                    ArrayList<TkIdentifier> tmpRelAdditionalIds = new ArrayList<TkIdentifier>(1);
                    if (rRec.addedIds != null) {
                        for (Sct1_IdRecord eId : rRec.addedIds) {
                            tmpRelAdditionalIds.add(createEIdentifier(eId));
                        }
                    }
                    if (tmpRelAdditionalIds.size() > 0) {
                        rel.additionalIds = tmpRelAdditionalIds;
                    } else {
                        rel.additionalIds = null;
                    }

                    theRelMsb = rRec.relUuidMsb;
                    theRelLsb = rRec.relUuidLsb;
                    rel.setPrimordialComponentUuid(new UUID(theRelMsb, theRelLsb));
                    rel.setC1Uuid(theConUUID);
                    rel.setC2Uuid(new UUID(rRec.c2UuidMsb, rRec.c2UuidLsb));
                    rel.setTypeUuid(lookupRoleType(rRec.roleTypeIdx));
                    rel.setRelGroup(rRec.group);
                    rel.setCharacteristicUuid(zRelCharArray[rRec.characteristic]);
                    rel.setRefinabilityUuid(zRelRefArray[rRec.refinability]);
                    rel.setStatusUuid(zStatusUuidArray[rRec.status]);
                    rel.setPathUuid(zPathArray[rRec.pathIdx]);
                    rel.setTime(rRec.revTime);
                    if (rRec.userIdx == USER_SNOROCKET_IDX) {
                        rel.setAuthorUuid(uuidUserSnorocket);
                    } else {
                        rel.setAuthorUuid(uuidUser);
                    }
                    rel.revisions = null;
                } else {
                    ERelationshipRevision erv = new ERelationshipRevision();
                    erv.setTypeUuid(lookupRoleType(rRec.roleTypeIdx));
                    erv.setRelGroup(rRec.group);
                    erv.setCharacteristicUuid(zRelCharArray[rRec.characteristic]);
                    erv.setRefinabilityUuid(zRelRefArray[rRec.refinability]);
                    erv.setStatusUuid(zStatusUuidArray[rRec.status]);
                    erv.setPathUuid(zPathArray[rRec.pathIdx]);
                    erv.setTime(rRec.revTime);
                    if (rRec.userIdx == USER_SNOROCKET_IDX) {
                        erv.setAuthorUuid(uuidUserSnorocket);
                    } else {
                        erv.setAuthorUuid(uuidUser);
                    }
                    revisions.add(erv);
                }
            }
            if (rel != null && revisions.size() > 0) {
                rel.revisions = revisions;
            }
            eRelList.add(rel);
            ec.setRelationships(eRelList);
        }

        // ADD REFSET INDEX
        if (rsByConList != null && rsByConList.size() > 0) {
            List<UUID> listRefsetUuidMemberUuidForCon = new ArrayList<UUID>();
            List<UUID> listRefsetUuidMemberUuidForDes = new ArrayList<UUID>();
            List<UUID> listRefsetUuidMemberUuidForImage = new ArrayList<UUID>();
            List<UUID> listRefsetUuidMemberUuidForRefsetMember = new ArrayList<UUID>();
            List<UUID> listRefsetUuidMemberUuidForRel = new ArrayList<UUID>();

            Collections.sort(rsByConList);
            int length = rsByConList.size();
            for (int rIdx = 0; rIdx < length; rIdx++) {
                Sct1_RefSetRecord r = rsByConList.get(rIdx);
                if (rIdx < length - 1) {
                    Sct1_RefSetRecord rNext = rsByConList.get(rIdx + 1);
                    if (r.refsetUuidMsb == rNext.refsetUuidMsb
                            && r.refsetUuidLsb == rNext.refsetUuidLsb
                            && r.refsetMemberUuidMsb == rNext.refsetMemberUuidMsb
                            && r.refsetMemberUuidLsb == rNext.refsetMemberUuidLsb) {
                        continue;
                    }
                }

                if (r.componentType == Sct1_RefSetRecord.ComponentType.CONCEPT) {
                    listRefsetUuidMemberUuidForCon.add(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForCon.add(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                } else if (r.componentType == Sct1_RefSetRecord.ComponentType.DESCRIPTION) {
                    listRefsetUuidMemberUuidForDes.add(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForDes.add(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                } else if (r.componentType == Sct1_RefSetRecord.ComponentType.IMAGE) {
                    listRefsetUuidMemberUuidForImage.add(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForImage.add(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                } else if (r.componentType == Sct1_RefSetRecord.ComponentType.MEMBER) {
                    listRefsetUuidMemberUuidForRefsetMember.add(new UUID(r.refsetUuidMsb,
                            r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForRefsetMember.add(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                } else if (r.componentType == Sct1_RefSetRecord.ComponentType.RELATIONSHIP) {
                    listRefsetUuidMemberUuidForRel.add(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForRel.add(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                } else {
                    throw new UnsupportedOperationException("Cannot handle case");
                }
            }

            countRefsetMember++;

        }

        // ADD REFSET MEMBER VALUES
        if (rsByRsList != null && rsByRsList.size() > 0) {
            List<TkRefsetAbstractMember<?>> listErm = new ArrayList<TkRefsetAbstractMember<?>>();
            Collections.sort(rsByRsList);

            if (rsByRsList.size() > 100000) {
                UUID tmpUUID = new UUID(cRec0.conUuidMsb, cRec0.conUuidLsb);
                getLog().info(
                        "::: NOTE: concept with MANY refset members = " + rsByRsList.size()
                        + ", concept UUID = " + tmpUUID.toString());
            }

            int rsmMax = rsByRsList.size(); // NUMBER OF REFSET MEMBERS
            int rsmIdx = 0;
            long lastRefsetMemberUuidMsb = Long.MAX_VALUE;
            long lastRefsetMemberUuidLsb = Long.MAX_VALUE;
            Sct1_RefSetRecord r = null;
            boolean hasMembersToProcess = false;
            if (rsmIdx < rsmMax) {
                r = rsByRsList.get(rsmIdx++);
                hasMembersToProcess = true;
            }
            while (hasMembersToProcess) {

                if (r.valueType == Sct1_RefSetRecord.ValueType.BOOLEAN) {
                    ERefsetBooleanMember tmp = new ERefsetBooleanMember();
                    tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    tmp.setPrimordialComponentUuid(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                    tmp.setComponentUuid(new UUID(r.referencedComponentUuidMsb,
                            r.referencedComponentUuidLsb));
                    tmp.setStatusUuid(zStatusUuidArray[r.status]);
                    tmp.setTime(r.revTime);
                    tmp.setPathUuid(zPathArray[r.pathIdx]);
                    tmp.setBooleanValue(r.valueBoolean);
                    if (r.authorIdx != -1) {
                        tmp.setAuthorUuid(zAuthorUuidArray[r.authorIdx]);
                    }

                    if (rsmIdx < rsmMax) { // CHECK REVISIONS
                        lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                        lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                        r = rsByRsList.get(rsmIdx++);
                        if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                            // FIRST REVISION
                            List<TkRefsetBooleanRevision> revisionList = new ArrayList<TkRefsetBooleanRevision>();
                            ERefsetBooleanRevision revision = new ERefsetBooleanRevision();
                            revision.setBooleanValue(r.valueBoolean);
                            revision.setStatusUuid(zStatusUuidArray[r.status]);
                            revision.setPathUuid(zPathArray[r.pathIdx]);
                            revision.setTime(r.revTime);
                            if (r.authorIdx != -1) {
                                revision.setAuthorUuid(zAuthorUuidArray[r.authorIdx]);
                            }
                            revisionList.add(revision);

                            boolean checkForMoreVersions = true;
                            do {
                                // SET UP NEXT MEMBER
                                if (rsmIdx < rsmMax) {
                                    lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                                    lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                                    r = rsByRsList.get(rsmIdx++);
                                    if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                            && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                                        revision = new ERefsetBooleanRevision();
                                        revision.setBooleanValue(r.valueBoolean);
                                        revision.setStatusUuid(zStatusUuidArray[r.status]);
                                        revision.setPathUuid(zPathArray[r.pathIdx]);
                                        revision.setTime(r.revTime);
                                        if (r.authorIdx != -1) {
                                            revision.setAuthorUuid(zAuthorUuidArray[r.authorIdx]);
                                        }
                                        revisionList.add(revision);
                                    } else {
                                        checkForMoreVersions = false;
                                    }
                                } else {
                                    checkForMoreVersions = false;
                                    hasMembersToProcess = false;
                                }

                            } while (checkForMoreVersions);

                            tmp.setRevisions(revisionList); // ADD REVISIONS
                        }
                    } else {
                        hasMembersToProcess = false;
                    }

                    // :NYI: tmp.setAdditionalIdComponents(additionalIdComponents);
                    listErm.add(tmp);
                } else if (r.valueType == Sct1_RefSetRecord.ValueType.CONCEPT) {
                    ERefsetCidMember tmp = new ERefsetCidMember();
                    tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    tmp.setPrimordialComponentUuid(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                    tmp.setComponentUuid(new UUID(r.referencedComponentUuidMsb,
                            r.referencedComponentUuidLsb));
                    tmp.setStatusUuid(zStatusUuidArray[r.status]);
                    tmp.setTime(r.revTime);
                    tmp.setPathUuid(zPathArray[r.pathIdx]);
                    tmp.setC1Uuid(new UUID(r.valueConUuidMsb, r.valueConUuidLsb));
                    if (r.authorIdx != -1) {
                        tmp.setAuthorUuid(zAuthorUuidArray[r.authorIdx]);
                    }

                    if (rsmIdx < rsmMax) { // CHECK REVISIONS
                        lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                        lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                        r = rsByRsList.get(rsmIdx++);
                        if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                            // FIRST REVISION
                            List<TkRefsetCidRevision> revisionList = new ArrayList<TkRefsetCidRevision>();
                            ERefsetCidRevision revision = new ERefsetCidRevision();
                            revision.setC1Uuid(new UUID(r.valueConUuidMsb, r.valueConUuidLsb));
                            revision.setStatusUuid(zStatusUuidArray[r.status]);
                            revision.setPathUuid(zPathArray[r.pathIdx]);
                            revision.setTime(r.revTime);
                            if (r.authorIdx != -1) {
                                revision.setAuthorUuid(zAuthorUuidArray[r.authorIdx]);
                            }
                            revisionList.add(revision);

                            boolean checkForMoreVersions = true;
                            do {
                                // SET UP NEXT MEMBER
                                if (rsmIdx < rsmMax) {
                                    lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                                    lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                                    r = rsByRsList.get(rsmIdx++);
                                    if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                            && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                                        revision = new ERefsetCidRevision();
                                        revision.setC1Uuid(new UUID(r.valueConUuidMsb,
                                                r.valueConUuidLsb));
                                        revision.setStatusUuid(zStatusUuidArray[r.status]);
                                        revision.setPathUuid(zPathArray[r.pathIdx]);
                                        revision.setTime(r.revTime);
                                        if (r.authorIdx != -1) {
                                            revision.setAuthorUuid(zAuthorUuidArray[r.authorIdx]);
                                        }
                                        revisionList.add(revision);
                                    } else {
                                        checkForMoreVersions = false;
                                    }
                                } else {
                                    checkForMoreVersions = false;
                                    hasMembersToProcess = false;
                                }

                            } while (checkForMoreVersions);

                            tmp.setRevisions(revisionList); // ADD REVISIONS
                        }
                    } else {
                        hasMembersToProcess = false;
                    }

                    // :NYI: tmp.setAdditionalIdComponents(additionalIdComponents);
                    listErm.add(tmp);
                } else if (r.valueType == Sct1_RefSetRecord.ValueType.INTEGER) {
                    ERefsetIntMember tmp = new ERefsetIntMember();
                    tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    tmp.setPrimordialComponentUuid(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                    tmp.setComponentUuid(new UUID(r.referencedComponentUuidMsb,
                            r.referencedComponentUuidLsb));
                    tmp.setStatusUuid(zStatusUuidArray[r.status]);
                    tmp.setTime(r.revTime);
                    tmp.setPathUuid(zPathArray[r.pathIdx]);
                    tmp.setIntValue(r.valueInt);
                    if (r.authorIdx != -1) {
                        tmp.setAuthorUuid(zAuthorUuidArray[r.authorIdx]);
                    }

                    if (rsmIdx < rsmMax) { // CHECK REVISIONS
                        lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                        lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                        r = rsByRsList.get(rsmIdx++);
                        if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                            // FIRST REVISION
                            List<TkRefsetIntRevision> revisionList = new ArrayList<TkRefsetIntRevision>();
                            ERefsetIntRevision revision = new ERefsetIntRevision();
                            revision.setIntValue(r.valueInt);
                            revision.setStatusUuid(zStatusUuidArray[r.status]);
                            revision.setPathUuid(zPathArray[r.pathIdx]);
                            revision.setTime(r.revTime);
                            if (r.authorIdx != -1) {
                                revision.setAuthorUuid(zAuthorUuidArray[r.authorIdx]);
                            }
                            revisionList.add(revision);

                            boolean checkForMoreVersions = true;
                            do {
                                // SET UP NEXT MEMBER
                                if (rsmIdx < rsmMax) {
                                    lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                                    lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                                    r = rsByRsList.get(rsmIdx++);
                                    if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                            && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                                        revision = new ERefsetIntRevision();
                                        revision.setIntValue(r.valueInt);
                                        revision.setStatusUuid(zStatusUuidArray[r.status]);
                                        revision.setPathUuid(zPathArray[r.pathIdx]);
                                        revision.setTime(r.revTime);
                                        if (r.authorIdx != -1) {
                                            revision.setAuthorUuid(zAuthorUuidArray[r.authorIdx]);
                                        }
                                        revisionList.add(revision);
                                    } else {
                                        checkForMoreVersions = false;
                                    }
                                } else {
                                    checkForMoreVersions = false;
                                    hasMembersToProcess = false;
                                }

                            } while (checkForMoreVersions);

                            tmp.setRevisions(revisionList); // ADD REVISIONS
                        }
                    } else {
                        hasMembersToProcess = false;
                    }

                    // :NYI: tmp.setAdditionalIdComponents(additionalIdComponents);
                    listErm.add(tmp);
                } else if (r.valueType == Sct1_RefSetRecord.ValueType.STRING) {
                    // :DEBUG:
                    //                    UUID debugUuid = new UUID(r.refsetMemberUuidMsb, r.refsetMemberUuidLsb);
                    //                    if (debugUuid.compareTo(debugUuid02) == 0) 
                    //                        System.out.println(":DEBUG:");

                    ERefsetStrMember tmp = new ERefsetStrMember();
                    tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    tmp.setPrimordialComponentUuid(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                    tmp.setComponentUuid(new UUID(r.referencedComponentUuidMsb,
                            r.referencedComponentUuidLsb));
                    tmp.setStatusUuid(zStatusUuidArray[r.status]);
                    tmp.setTime(r.revTime);
                    tmp.setPathUuid(zPathArray[r.pathIdx]);
                    tmp.setStrValue(r.valueString);
                    if (r.authorIdx != -1) {
                        tmp.setAuthorUuid(zAuthorUuidArray[r.authorIdx]);
                    }

                    if (rsmIdx < rsmMax) { // CHECK REVISIONS
                        lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                        lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                        r = rsByRsList.get(rsmIdx++);
                        if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                            // FIRST REVISION
                            List<TkRefsetStrRevision> revisionList = new ArrayList<TkRefsetStrRevision>();
                            ERefsetStrRevision revision = new ERefsetStrRevision();
                            revision.setStringValue(r.valueString);
                            revision.setStatusUuid(zStatusUuidArray[r.status]);
                            revision.setPathUuid(zPathArray[r.pathIdx]);
                            revision.setTime(r.revTime);
                            if (r.authorIdx != -1) {
                                revision.setAuthorUuid(zAuthorUuidArray[r.authorIdx]);
                            }
                            revisionList.add(revision);

                            boolean checkForMoreVersions = true;
                            do {
                                // SET UP NEXT MEMBER
                                if (rsmIdx < rsmMax) {
                                    lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                                    lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                                    r = rsByRsList.get(rsmIdx++);
                                    if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                            && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                                        revision = new ERefsetStrRevision();
                                        revision.setStringValue(r.valueString);
                                        revision.setStatusUuid(zStatusUuidArray[r.status]);
                                        revision.setPathUuid(zPathArray[r.pathIdx]);
                                        revision.setTime(r.revTime);
                                        if (r.authorIdx != -1) {
                                            revision.setAuthorUuid(zAuthorUuidArray[r.authorIdx]);
                                        }
                                        revisionList.add(revision);
                                    } else {
                                        checkForMoreVersions = false;
                                    }
                                } else {
                                    checkForMoreVersions = false;
                                    hasMembersToProcess = false;
                                }

                            } while (checkForMoreVersions);

                            tmp.setRevisions(revisionList); // ADD REVISIONS
                        }
                    } else {
                        hasMembersToProcess = false;
                    }

                    // :NYI: tmp.setAdditionalIdComponents(additionalIdComponents);
                    listErm.add(tmp);
                } else {
                    throw new UnsupportedOperationException("Cannot handle case");
                }

            }
            countRefsetMaster++;

            ec.setRefsetMembers(listErm);
            //            if (conceptsToWatchMap.containsKey(ec.primordialUuid)) {
            //                getLog().info("Found watch concept after adding refset members: "
            //                        + ec);
            //            }
        }

        try {
            ec.writeExternal(dos);
            //            if (theConUUID.compareTo(debugUuid01) == 0) {
            //                getLog().info(":DEBUG: "  + ec);
            //            }

            countEConWritten++;
            if (countEConWritten % 50000 == 0) {
                getLog().info("  ... econcepts written " + countEConWritten);
            }
        } catch (IOException e) {
            getLog().info(e);
        }

    }

    private Sct1_ConRecord readNextCon(ObjectInputStream ois, ArrayList<Sct1_ConRecord> conList,
            Sct1_ConRecord conNext) throws MojoFailureException {
        conList.clear();
        if (conNext != null) {
            conList.add(conNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readUnshared();
                if (obj instanceof Sct1_ConRecord) {
                    conNext = (Sct1_ConRecord) obj;
                    conList.add(conNext);
                } else {
                    return null;
                }
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                getLog().info(e);
                throw new MojoFailureException("IO Exception - readNextCon()");
            } catch (ClassNotFoundException e) {
                getLog().info(e);
                throw new MojoFailureException("ClassNotFoundException - readNextCon()");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readUnshared();
                if (obj instanceof Sct1_ConRecord) {
                    Sct1_ConRecord rec = (Sct1_ConRecord) obj;
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
            getLog().info(e);
            throw new MojoFailureException("IO Exception -- readNextCon()");
        } catch (ClassNotFoundException e) {
            getLog().info(e);
            throw new MojoFailureException("ClassNotFoundException -- readNextCon()");
        }

        return conNext; // first record of next concept id
    }

    private Sct1_DesRecord readNextDes(ObjectInputStream ois, ArrayList<Sct1_DesRecord> desList,
            Sct1_DesRecord desNext) throws MojoFailureException {
        desList.clear();
        if (desNext != null) {
            desList.add(desNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readUnshared();
                if (obj instanceof Sct1_DesRecord) {
                    desNext = (Sct1_DesRecord) obj;
                    desList.add(desNext);
                } else {
                    return null;
                }
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                getLog().info(e);
                throw new MojoFailureException("IO Exception - readNextDes()");
            } catch (ClassNotFoundException e) {
                getLog().info(e);
                throw new MojoFailureException("ClassNotFoundException - readNextDes()");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readUnshared();
                if (obj instanceof Sct1_DesRecord) {
                    Sct1_DesRecord rec = (Sct1_DesRecord) obj;
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
            getLog().info(e);
            throw new MojoFailureException("IO Exception -- readNextDes()");
        } catch (ClassNotFoundException e) {
            getLog().info(e);
            throw new MojoFailureException("ClassNotFoundException -- readNextDes()");
        }

        return desNext; // first record of next concept id
    }

    private Sct1_RelRecord readNextRel(ObjectInputStream ois, ArrayList<Sct1_RelRecord> relList,
            Sct1_RelRecord relNext) throws MojoFailureException {
        relList.clear();
        if (relNext != null) {
            relList.add(relNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readUnshared();
                if (obj instanceof Sct1_RelRecord) {
                    relNext = (Sct1_RelRecord) obj;
                    relList.add(relNext);
                } else {
                    return null;
                }
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                getLog().info(e);
                throw new MojoFailureException("IO Exception - readNextRel()");
            } catch (ClassNotFoundException e) {
                getLog().info(e);
                throw new MojoFailureException("ClassNotFoundException - readNextRel()");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readUnshared();
                if (obj instanceof Sct1_RelRecord) {
                    Sct1_RelRecord rec = (Sct1_RelRecord) obj;
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
            getLog().info(e);
            throw new MojoFailureException("IO Exception -- readNextRel()");
        } catch (ClassNotFoundException e) {
            getLog().info(e);
            throw new MojoFailureException("ClassNotFoundException -- readNextRel()");
        }

        return relNext; // first record of next concept id
    }

    private Sct1_RelDestRecord readNextRelDest(ObjectInputStream ois,
            ArrayList<Sct1_RelDestRecord> relDestList, Sct1_RelDestRecord relDestNext)
            throws MojoFailureException {
        relDestList.clear();
        if (relDestNext != null) {
            relDestList.add(relDestNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readUnshared();
                if (obj instanceof Sct1_RelDestRecord) {
                    relDestNext = (Sct1_RelDestRecord) obj;
                    relDestList.add(relDestNext);
                } else {
                    return null;
                }
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                getLog().info(e);
                throw new MojoFailureException("IO Exception - readNextRelDest()");
            } catch (ClassNotFoundException e) {
                getLog().info(e);
                throw new MojoFailureException("ClassNotFoundException - readNextRelDest()");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readUnshared();
                if (obj instanceof Sct1_RelDestRecord) {
                    Sct1_RelDestRecord rec = (Sct1_RelDestRecord) obj;
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
            getLog().info(e);
            throw new MojoFailureException("IO Exception -- readNextRelDest()");
        } catch (ClassNotFoundException e) {
            getLog().info(e);
            throw new MojoFailureException("ClassNotFoundException -- readNextRelDest()");
        }

        return relDestNext; // first record of next concept id
    }

    private Sct1_RefSetRecord readNextRsByCon(ObjectInputStream ois,
            ArrayList<Sct1_RefSetRecord> rsByConList, Sct1_RefSetRecord rsByConNext)
            throws MojoFailureException {
        rsByConList.clear();
        if (rsByConNext != null) {
            rsByConList.add(rsByConNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readUnshared();
                if (obj instanceof Sct1_RefSetRecord) {
                    rsByConNext = (Sct1_RefSetRecord) obj;
                    rsByConList.add(rsByConNext);
                } else {
                    return null;
                }
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                getLog().info(e);
                throw new MojoFailureException("IO Exception - readNextRsByCon()");
            } catch (ClassNotFoundException e) {
                getLog().info(e);
                throw new MojoFailureException("ClassNotFoundException - readNextRsByCon()");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readUnshared();
                if (obj instanceof Sct1_RefSetRecord) {
                    Sct1_RefSetRecord rec = (Sct1_RefSetRecord) obj;
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
            getLog().info(e);
            throw new MojoFailureException("IO Exception -- readNextRsByCon()");
        } catch (ClassNotFoundException e) {
            getLog().info(e);
            throw new MojoFailureException("ClassNotFoundException -- readNextRsByCon()");
        }

        return rsByConNext; // first record of next concept id
    }

    private Sct1_RefSetRecord readNextRsByRs(ObjectInputStream ois,
            ArrayList<Sct1_RefSetRecord> rsByRsList, Sct1_RefSetRecord rsByRsNext)
            throws MojoFailureException {
        rsByRsList.clear();
        if (rsByRsNext != null) {
            rsByRsList.add(rsByRsNext);
        } else {
            try { // CHECK FOR FIRST RECORD SITUATION
                Object obj = ois.readUnshared();
                if (obj instanceof Sct1_RefSetRecord) {
                    rsByRsNext = (Sct1_RefSetRecord) obj;
                    rsByRsList.add(rsByRsNext);
                } else {
                    return null;
                }
            } catch (EOFException ex) {
                return null;
            } catch (IOException e) {
                getLog().info(e);
                throw new MojoFailureException("IO Exception - readNextRsByRs()");
            } catch (ClassNotFoundException e) {
                getLog().info(e);
                throw new MojoFailureException("ClassNotFoundException - readNextRsByRs()");
            }
        }

        try {
            boolean notDone = true;
            while (notDone) {
                Object obj = ois.readUnshared();
                if (obj instanceof Sct1_RefSetRecord) {
                    Sct1_RefSetRecord rec = (Sct1_RefSetRecord) obj;
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
            getLog().info(e);
            throw new MojoFailureException("IO Exception -- readNextRsByRs()");
        } catch (ClassNotFoundException e) {
            getLog().info(e);
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

            uuidPathSnomedCore = ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids().iterator().next();
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
            uuidSourceSnomedRt = ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.getUids().iterator().next();
            uuidSourceSnomedLong = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator().next();

            getLog().info("SNOMED CT Root       = " + uuidRootSnomedStr);
            getLog().info("SNOMED Core          = " + uuidPathSnomedCore);
            getLog().info("SNOMED Core Stated   = " + uuidPathSnomedStatedStr);
            getLog().info("  ... Stated rel     = " + uuidStatedRel.toString());

            getLog().info("SNOMED Core Inferred = " + uuidPathSnomedInferredStr);
            getLog().info("  ... Inferred rel   = " + uuidInferredRel.toString());

            getLog().info("SNOMED integer id UUID = " + uuidSourceSnomedLong);
            getLog().info("SNOMED CTV3 id UUID    = " + uuidSourceCtv3);
            getLog().info("SNOMED RT id UUID      = " + uuidSourceSnomedRt);

        } catch (NoSuchAlgorithmException e2) {
            getLog().info(e2);
            throw new MojoFailureException("FAILED: SNOMED Core Stated/Inferred Path", e2);
        } catch (UnsupportedEncodingException e2) {
            getLog().info(e2);
            throw new MojoFailureException("FAILED: SNOMED Core Stated/Inferred Path", e2);
        }
    }

    private List<List<ARFFile>> getArfFiles(String wDir, String subDir, String[] arfDirs,
            String prefix, String postfix) throws MojoFailureException {

        List<List<ARFFile>> listOfDirs = new ArrayList<List<ARFFile>>();
        if (arfDirs == null) {
            return listOfDirs;
        }

        for (int ii = 0; ii < arfDirs.length; ii++) {
            ArrayList<ARFFile> listOfFiles = new ArrayList<ARFFile>();

            getLog().info(prefix.toUpperCase() + " (" + ii + ") " + wDir + subDir + arfDirs[ii]);

            File f1 = new File(new File(wDir, subDir), arfDirs[ii]);
            ArrayList<File> fv = new ArrayList<File>();
            listFilesRecursive(fv, f1, prefix, postfix);

            File[] files = new File[0];
            files = fv.toArray(files);
            Arrays.sort(files);

            FileFilter filter = new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    if (inputFilters == null || inputFilters.length == 0) {
                        return true;
                    } else {
                        for (String filter : inputFilters) {
                            if (pathname.getAbsolutePath().replace(File.separatorChar, '/').matches(filter)) {
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

    private List<List<SCTFile>> getSctFiles(String wDir, String subDir, Sct1Dir[] inDirs,
            String prefix, String postfix) throws MojoFailureException {

        List<List<SCTFile>> listOfDirs = new ArrayList<List<SCTFile>>();
        for (Sct1Dir sctDir : inDirs) {
            ArrayList<SCTFile> listOfFiles = new ArrayList<SCTFile>();

            getLog().info(
                    String.format("%1$s (%2$s%3$s%4$s) ", prefix.toUpperCase(), wDir, subDir,
                    sctDir.getDirectoryName()));

            File f1 = new File(new File(wDir, subDir), sctDir.getDirectoryName());
            ArrayList<File> fv = new ArrayList<File>();
            listFilesRecursive(fv, f1, "sct1_" + prefix, postfix);

            File[] files = new File[0];
            files = fv.toArray(files);
            Arrays.sort(files);

            FileFilter filter = new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    if (inputFilters == null || inputFilters.length == 0) {
                        return true;
                    } else {
                        for (String filter : inputFilters) {
                            if (pathname.getAbsolutePath().replace(File.separatorChar, '/').matches(filter)) {
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

                    try {
                        if (inDateRange(revDate)) {

                            SCTFile fo = new SCTFile(f2, wDir, subDir, revDate, sctDir);
                            listOfFiles.add(fo);
                            getLog().info(
                                    "::: FILE : " + f2.getName() + " " + revDate + " hasSnomedId="
                                    + fo.hasStatedSctRelId + " doCrossMap="
                                    + fo.mapSctIdInferredToStated);
                        }
                    } catch (ParseException e) {
                        getLog().info(e);
                        getLog().info(
                                "::: Date format missing or not supported : " + f2.getName() + " "
                                + revDate);
                    }
                }

            }

            listOfDirs.add(listOfFiles);
        }
        return listOfDirs;
    }

    boolean inDateRange(String revDateStr) throws ParseException {
        String pattern = "yyyy-MM-dd hh:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date revDate = formatter.parse(revDateStr);

        if (dateStartObj != null && revDate.compareTo(dateStartObj) < 0) {
            return false; // precedes start date
        }
        if (dateStopObj != null && revDate.compareTo(dateStopObj) > 0) {
            return false; // after end date
        }
        return true;
    }

    public String getDateStart() {
        return this.dateStart;
    }

    public void setDateStart(String sStart) throws MojoFailureException {
        this.dateStart = sStart;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        try {
            this.dateStartObj = formatter.parse(sStart + " 00:00:00");
        } catch (ParseException e) {
            getLog().info(e);
            throw new MojoFailureException("SimpleDateFormat yyyy.MM.dd dateStart parse error: "
                    + sStart);
        }
        getLog().info("::: START DATE (INCLUSIVE) " + this.dateStart);
    }

    public String getDateStop() {
        return this.dateStop;
    }

    public void setDateStop(String sStop) throws MojoFailureException {
        this.dateStop = sStop;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        try {
            this.dateStopObj = formatter.parse(sStop + " 23:59:59");
        } catch (ParseException e) {
            getLog().info(e);
            throw new MojoFailureException("SimpleDateFormat yyyy.MM.dd dateStop parse error: "
                    + sStop);
        }
        getLog().info(":::  STOP DATE (INCLUSIVE) " + this.dateStop);
    }

    /*
     * ORDER: CONCEPTID CONCEPTSTATUS FULLYSPECIFIEDNAME CTV3ID SNOMEDID
     * ISPRIMITIVE
     * 
     * KEEP: CONCEPTID CONCEPTSTATUS ISPRIMITIVE
     * 
     * IGNORE: FULLYSPECIFIEDNAME CTV3ID SNOMEDID
     */
    private void processConceptsFiles(String wDir, List<List<SCTFile>> sctv, boolean ctv3idTF,
            boolean snomedrtTF, ObjectOutputStream oos) throws Exception {
        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        int pathID;
        long revTime;
        Sct1_ConRecord[] a1, a2, a3 = null;

        getLog().info("START CONCEPTS PROCESSING...");

        Iterator<List<SCTFile>> dit = sctv.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<SCTFile> fl = dit.next(); // File List
            Iterator<SCTFile> fit = fl.iterator(); // File Iterator
            if (fit.hasNext() == false) {
                continue;
            }

            // READ file1 as MASTER FILE
            SCTFile f1 = fit.next();
            fName1 = f1.file.getPath();
            revTime = f1.zRevTime;
            pathID = f1.pathIdx;

            count1 = countFileLines(fName1);
            getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
            a1 = new Sct1_ConRecord[count1];
            parseConcepts(fName1, a1, count1, ctv3idTF, snomedrtTF);
            writeConcepts(oos, a1, count1, revTime, pathID);

            while (fit.hasNext()) {
                // SETUP CURRENT CONCEPTS INPUT FILE
                SCTFile f2 = fit.next();
                fName2 = f2.file.getPath();
                revTime = f2.zRevTime;
                pathID = f2.pathIdx;

                count2 = countFileLines(fName2);
                getLog().info("Counted: " + count2 + " records, " + fName2);

                // Parse in file2
                a2 = new Sct1_ConRecord[count2];
                parseConcepts(fName2, a2, count2, ctv3idTF, snomedrtTF);

                int r1 = 0, r2 = 0, r3 = 0; // reset record indices
                int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
                a3 = new Sct1_ConRecord[count2]; // max3
                while ((r1 < count1) && (r2 < count2)) {

                    switch (compareConcept(a1[r1], a2[r2])) {
                        case 1: // SAME CONCEPT, skip to next
                            r1++;
                            r2++;
                            nSame++;
                            break;

                        case 2: // MODIFIED CONCEPT
                            // Write history
                            a2[r2].path = pathID;
                            a2[r2].revTime = revTime;
                            oos.writeUnshared(a2[r2]);
                            // Update master via pointer assignment
                            a1[r1] = a2[r2];
                            r1++;
                            r2++;
                            nMod++;
                            break;

                        case 3: // ADDED CONCEPT
                            // Write history
                            a2[r2].path = pathID;
                            a2[r2].revTime = revTime;
                            oos.writeUnshared(a2[r2]);

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
                                a1[r1].path = pathID;
                                a1[r1].revTime = revTime;
                                oos.writeUnshared(a1[r1]);
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
                        a2[r2].path = pathID;
                        a2[r2].revTime = revTime;
                        oos.writeUnshared(a2[r2]);

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
                a2 = new Sct1_ConRecord[count1 + nAdd];
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

    private void processDescriptionsFiles(String wDir, List<List<SCTFile>> sctv,
            ObjectOutputStream oos) throws Exception {
        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        int pathID;
        long revTime;
        Sct1_DesRecord[] a1, a2, a3 = null;

        getLog().info("START DESCRIPTIONS PROCESSING...");

        Iterator<List<SCTFile>> dit = sctv.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<SCTFile> fl = dit.next(); // File List
            Iterator<SCTFile> fit = fl.iterator(); // File Iterator
            if (fit.hasNext() == false) {
                continue;
            }

            // READ file1 as MASTER FILE
            SCTFile f1 = fit.next();
            fName1 = f1.file.getPath();
            revTime = f1.zRevTime;
            pathID = f1.pathIdx;

            count1 = countFileLines(fName1);
            getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
            a1 = new Sct1_DesRecord[count1];
            parseDescriptions(fName1, a1, count1);
            writeDescriptions(oos, a1, count1, revTime, pathID);

            while (fit.hasNext()) {
                // SETUP CURRENT CONCEPTS INPUT FILE
                SCTFile f2 = fit.next();
                fName2 = f2.file.getPath();
                revTime = f2.zRevTime;
                pathID = f2.pathIdx;

                count2 = countFileLines(fName2);
                getLog().info("Counted: " + count2 + " records, " + fName2);

                // Parse in file2
                a2 = new Sct1_DesRecord[count2];
                parseDescriptions(fName2, a2, count2);

                int r1 = 0, r2 = 0, r3 = 0; // reset record indices
                int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
                a3 = new Sct1_DesRecord[count2];
                while ((r1 < count1) && (r2 < count2)) {

                    switch (compareDescription(a1[r1], a2[r2])) {
                        case 1: // SAME DESCRIPTION, skip to next
                            r1++;
                            r2++;
                            nSame++;
                            break;

                        case 2: // MODIFIED DESCRIPTION
                            // Write history
                            a2[r2].pathIdx = pathID;
                            a2[r2].revTime = revTime;
                            oos.writeUnshared(a2[r2]);

                            // Update master via pointer assignment
                            a1[r1] = a2[r2];
                            r1++;
                            r2++;
                            nMod++;
                            break;

                        case 3: // ADDED DESCRIPTION
                            // Write history
                            a2[r2].pathIdx = pathID;
                            a2[r2].revTime = revTime;
                            oos.writeUnshared(a2[r2]);

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
                                a1[r1].pathIdx = pathID;
                                a1[r1].revTime = revTime;
                                oos.writeUnshared(a1[r1]);
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
                        a2[r2].pathIdx = pathID;
                        a2[r2].revTime = revTime;
                        oos.writeUnshared(a2[r2]);

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
                a2 = new Sct1_DesRecord[count1 + nAdd];
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

    }

    private void processRelationshipsFiles(String wDir, List<List<SCTFile>> sctI,
            ObjectOutputStream oos, ObjectOutputStream oosIds, BufferedWriter er, int user)
            throws Exception {
        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        long revTime;
        Sct1_RelRecord[] a1, a2, a3 = null;

        Iterator<List<SCTFile>> dit = sctI.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<SCTFile> fl = dit.next(); // File List
            Iterator<SCTFile> fit = fl.iterator(); // File Iterator
            if (fit.hasNext() == false) {
                continue;
            }

            // READ file1 as MASTER FILE
            SCTFile f1 = fit.next();
            fName1 = f1.file.getPath();
            revTime = f1.zRevTime;

            count1 = countFileLines(fName1);
            getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
            a1 = new Sct1_RelRecord[count1];
            a1 = parseRelationships(fName1, a1, count1, f1);
            getLog().info("            " + a1.length + " after non-defining filter");
            a1 = removeDuplRels(a1);
            getLog().info("            " + a1.length + " after duplicate removal");
            count1 = a1.length;
            writeRelationships(oos, oosIds, a1, count1, revTime, user);

            while (fit.hasNext()) {
                // SETUP CURRENT RELATIONSHIPS INPUT FILE
                SCTFile f2 = fit.next();
                fName2 = f2.file.getPath();
                revTime = f2.zRevTime;

                count2 = countFileLines(fName2);
                getLog().info("Counted: " + count2 + " records, " + fName2);

                // Parse in file2
                a2 = new Sct1_RelRecord[count2];
                a2 = parseRelationships(fName2, a2, count2, f2);
                getLog().info("            " + a2.length + " after non-defining filter");
                a2 = removeDuplRels(a2);
                getLog().info("            " + a2.length + " after duplicate removal");
                count2 = a2.length;

                int r1 = 0, r2 = 0, r3 = 0; // reset record indices
                int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
                int nModSidChange = 0, nSidOnlyChange = 0; // counters related to SNOMED_ID change
                int nWrite = 0; // counter for memory optimization for object files writing
                a3 = new Sct1_RelRecord[count2];
                while ((r1 < count1) && (r2 < count2)) {

                    // :DEBUG:
                    //                    if (debug)
                    //                        if ((a1[r1].relSnoId == 2455349029L || a2[r2].relSnoId == 2455349029L)
                    //                                || (a1[r1].relSnoId == 2671123026L || a2[r2].relSnoId == 2671123026L)) {
                    //                            int tmpCompare = compareRelationship(a1[r1], a2[r2]);
                    //                            getLog().info("!!! ");
                    //                            getLog().info("!!! CASE == " + tmpCompare);
                    //                            getLog().info("!!! a1[r1] @ " + revTime + " = "
                    //                                    + a1[r1].toString());
                    //                            getLog().info("!!! ");
                    //                            getLog().info("!!! a2[r2] @ " + revTime + " = "
                    //                                    + a2[r2].toString());
                    //                            getLog().info("!!! ");
                    //                        }

                    switch (compareRelationship(a1[r1], a2[r2])) {
                        case 1: // SAME RELATIONSHIP, SAME SNOMED_ID skip to next
                            r1++;
                            r2++;
                            nSame++;
                            break;

                        case 5: // SAME LOGICAL RELATIONSHIP, CHANGED SNOMED_ID
                            // RETIRE EXISTING SNOMED_ID
                            Sct1_IdRecord idOnlyChange = null;
                            if (a1[r1].relSnoId < Long.MAX_VALUE) {
                                idOnlyChange = new Sct1_IdRecord(a1[r1].relUuidMsb, a1[r1].relUuidLsb,
                                        uuidSourceSnomedIdx, a1[r1].relSnoId, 1, revTime,
                                        a1[r1].pathIdx, user);
                                oosIds.writeUnshared(idOnlyChange);
                            }

                            // WRITE CURRENT SNOMED_ID
                            if (a2[r2].relSnoId < Long.MAX_VALUE) {
                                idOnlyChange = new Sct1_IdRecord(a2[r2].relUuidMsb, a2[r2].relUuidLsb,
                                        uuidSourceSnomedIdx, a2[r2].relSnoId, 0, revTime,
                                        a2[r2].pathIdx, user);
                                oosIds.writeUnshared(idOnlyChange);
                            }

                            a1[r1] = a2[r2];
                            r1++;
                            r2++;
                            nSidOnlyChange++;

                            // PERIODIC RESET IMPROVES MEMORY USE
                            nWrite++;
                            if (nWrite % ooResetInterval == 0) {
                                oos.reset();
                                oosIds.reset();
                            }
                            break;

                        case 2: // SAME LOGICAL RELATIONSHIP, SAME SNOMED_ID, MODIFIED OTHER
                            // Write history
                            a2[r2].revTime = revTime;
                            oos.writeUnshared(a2[r2]);

                            // Update master via pointer assignment
                            a1[r1] = a2[r2];
                            r1++;
                            r2++;
                            nMod++;

                            // PERIODIC RESET IMPROVES MEMORY USE
                            nWrite++;
                            if (nWrite % ooResetInterval == 0) {
                                oos.reset();
                                oosIds.reset();
                            }
                            break;

                        case 7: // SAME LOGICAL RELATIONSHIP, SAME SNOMED_ID, MODIFIED USER
                            // RETIRE PREVIOUS USER
                            a1[r1].status = 1; // RETIRE OLD USER
                            a1[r1].revTime = revTime;
                            oos.writeUnshared(a1[r1]);

                            // MAKE CURRENT NEW USER
                            a2[r2].revTime = revTime;
                            oos.writeUnshared(a2[r2]);

                            // Update master via pointer assignment
                            a1[r1] = a2[r2];
                            r1++;
                            r2++;
                            nMod++;

                            // PERIODIC RESET IMPROVES MEMORY USE
                            nWrite++;
                            if (nWrite % ooResetInterval == 0) {
                                oos.reset();
                                oosIds.reset();
                            }
                            break;

                        case 6: // SAME LOGICAL RELATIONSHIP, CHANGED SNOMED_ID, MODIFIED OTHER
                            // Write history
                            a2[r2].revTime = revTime;
                            oos.writeUnshared(a2[r2]);

                            // RETIRE EXISTING SNOMED_ID
                            Sct1_IdRecord idMod = null;
                            if (a1[r1].relSnoId < Long.MAX_VALUE) {
                                idMod = new Sct1_IdRecord(a1[r1].relUuidMsb, a1[r1].relUuidLsb,
                                        uuidSourceSnomedIdx, a1[r1].relSnoId, 1, revTime,
                                        a1[r1].pathIdx, user);
                                oosIds.writeUnshared(idMod);
                            }

                            // WRITE CURRENT SNOMED_ID
                            if (a2[r2].relSnoId < Long.MAX_VALUE) {
                                idMod = new Sct1_IdRecord(a2[r2].relUuidMsb, a2[r2].relUuidLsb,
                                        uuidSourceSnomedIdx, a2[r2].relSnoId, 0, revTime,
                                        a2[r2].pathIdx, user);
                                oosIds.writeUnshared(idMod);
                            }

                            // Update master via pointer assignment
                            a1[r1] = a2[r2];
                            r1++;
                            r2++;
                            nModSidChange++;

                            // PERIODIC RESET IMPROVES MEMORY USE
                            nWrite++;
                            if (nWrite % ooResetInterval == 0) {
                                oos.reset();
                                oosIds.reset();
                            }
                            break;

                        case 8: // MODIFIED LOGICAL RELATIONSHIP, CHANGED SNOMED_ID, CHANGED USER
                            // RETIRE PREVIOUS USER
                            a1[r1].status = 1; // RETIRE OLD USER
                            a1[r1].revTime = revTime;
                            oos.writeUnshared(a1[r1]);

                            // MAKE CURRENT NEW USER
                            a2[r2].revTime = revTime;
                            oos.writeUnshared(a2[r2]);

                            // RETIRE EXISTING SNOMED_ID
                            Sct1_IdRecord idMod2 = null;
                            if (a1[r1].relSnoId < Long.MAX_VALUE) {
                                idMod2 = new Sct1_IdRecord(a1[r1].relUuidMsb, a1[r1].relUuidLsb,
                                        uuidSourceSnomedIdx, a1[r1].relSnoId, 1, revTime,
                                        a1[r1].pathIdx, user);
                                oosIds.writeUnshared(idMod2);
                            }

                            // WRITE CURRENT SNOMED_ID
                            if (a2[r2].relSnoId < Long.MAX_VALUE) {
                                idMod2 = new Sct1_IdRecord(a2[r2].relUuidMsb, a2[r2].relUuidLsb,
                                        uuidSourceSnomedIdx, a2[r2].relSnoId, 0, revTime,
                                        a2[r2].pathIdx, user);
                                oosIds.writeUnshared(idMod2);
                            }

                            // Update master via pointer assignment
                            a1[r1] = a2[r2];
                            r1++;
                            r2++;
                            nModSidChange++;
                            break;

                        case 3: // ADDED LOGICAL RELATIONSHIP
                            // Write history
                            a2[r2].revTime = revTime;
                            oos.writeUnshared(a2[r2]);

                            // WRITE CURRENT SNOMED_ID
                            if (a2[r2].relSnoId < Long.MAX_VALUE) {
                                Sct1_IdRecord idAdded = new Sct1_IdRecord(a2[r2].relUuidMsb,
                                        a2[r2].relUuidLsb, uuidSourceSnomedIdx, a2[r2].relSnoId,
                                        a2[r2].status, revTime, a2[r2].pathIdx, user);
                                oosIds.writeUnshared(idAdded);
                            }

                            // hold pointer to append to master
                            a3[r3] = a2[r2];
                            r2++;
                            r3++;
                            nAdd++;

                            // PERIODIC RESET IMPROVES MEMORY USE
                            nWrite++;
                            if (nWrite % ooResetInterval == 0) {
                                oos.reset();
                                oosIds.reset();
                            }
                            break;

                        case 4: // DROPPED LOGICAL RELATIONSHIP
                            // see ArchitectonicAuxiliary.getStatusFromId()
                            if (a1[r1].status != 1) { // if not RETIRED
                                a1[r1].status = 1; // set to RETIRED
                                a1[r1].revTime = revTime;
                                oos.writeUnshared(a1[r1]);

                                // RETIRE EXISTING SNOMED_ID
                                if (a1[r1].relSnoId < Long.MAX_VALUE) {
                                    Sct1_IdRecord idDropped = new Sct1_IdRecord(a1[r1].relUuidMsb,
                                            a1[r1].relUuidLsb, uuidSourceSnomedIdx, a1[r1].relSnoId,
                                            a1[r1].status, revTime, a1[r1].pathIdx, user);
                                    oosIds.writeUnshared(idDropped);
                                }

                                // PERIODIC RESET IMPROVES MEMORY USE
                                nWrite++;
                                if (nWrite % ooResetInterval == 0) {
                                    oos.reset();
                                    oosIds.reset();
                                }
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
                            a1[r1].revTime = revTime;
                            oos.writeUnshared(a1[r1]);

                            // RETIRE EXISTING SNOMED_ID
                            if (a1[r1].relSnoId < Long.MAX_VALUE) {
                                Sct1_IdRecord idDropped = new Sct1_IdRecord(a1[r1].relUuidMsb,
                                        a1[r1].relUuidLsb, uuidSourceSnomedIdx, a1[r1].relSnoId,
                                        a1[r1].status, revTime, a1[r1].pathIdx, user);
                                oosIds.writeUnshared(idDropped);
                            }
                        }
                        r1++;
                        nDrop++;
                    }
                }

                if (r2 < count2) {
                    while (r2 < count2) { // ADD REMAINING RELATIONSHIP INPUT
                        // Write history
                        a2[r2].revTime = revTime;
                        oos.writeUnshared(a2[r2]);

                        // WRITE CURRENT SNOMED_ID
                        if (a2[r2].relSnoId < Long.MAX_VALUE) {
                            Sct1_IdRecord idAdded = new Sct1_IdRecord(a2[r2].relUuidMsb,
                                    a2[r2].relUuidLsb, uuidSourceSnomedIdx, a2[r2].relSnoId,
                                    a2[r2].status, revTime, a2[r2].pathIdx, user);
                            oosIds.writeUnshared(idAdded);
                        }

                        // Add to append array
                        a3[r3] = a2[r2];
                        nAdd++;
                        r2++;
                        r3++;
                    }
                }

                // Check counter numbers to master and input file record counts
                countCheck(count1, count2, nSame, nMod, nAdd, nDrop, nModSidChange, nSidOnlyChange);

                // SETUP NEW MASTER ARRAY
                a2 = new Sct1_RelRecord[count1 + nAdd];
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

    private int compareConcept(Sct1_ConRecord c1, Sct1_ConRecord c2) {
        if (c1.conUuidMsb == c2.conUuidMsb && c1.conUuidLsb == c2.conUuidLsb) {
            if ((c1.status == c2.status) && (c1.isprimitive == c2.isprimitive)) {
                return 1; // SAME
            } else {
                return 2; // MODIFIED
            }
        } else if (c1.conUuidMsb > c2.conUuidMsb) {
            return 3; // ADDED

        } else if (c1.conUuidMsb == c2.conUuidMsb && c1.conUuidLsb > c2.conUuidLsb) {
            return 3; // ADDED

        } else {
            return 4; // DROPPED
        }
    }

    private int compareDescription(Sct1_DesRecord d1, Sct1_DesRecord d2) {

        if (d1.desUuidMsb == d2.desUuidMsb && d1.desUuidLsb == d2.desUuidLsb) {
            if ((d1.status == d2.status) && (d1.conSnoId == d2.conSnoId)
                    && d1.termText.contentEquals(d2.termText) && (d1.capStatus == d2.capStatus)
                    && (d1.descriptionType == d2.descriptionType)
                    && d1.languageCode.contentEquals(d2.languageCode)) {
                return 1; // SAME
            } else {
                return 2; // MODIFIED
            }
        } else if (d1.desUuidMsb > d2.desUuidMsb) {
            return 3; // ADDED

        } else if (d1.desUuidMsb == d2.desUuidMsb && d1.desUuidLsb > d2.desUuidLsb) {
            return 3; // ADDED

        } else {
            return 4; // DROPPED
        }
    }

    private int compareRelationship(Sct1_RelRecord c1, Sct1_RelRecord c2) {
        if (c1.relUuidMsb == c2.relUuidMsb && c1.relUuidLsb == c2.relUuidLsb) {
            // SAME REL UUID
            if ((c1.status == c2.status) && (c1.characteristic == c2.characteristic)
                    && (c1.refinability == c2.refinability) && (c1.group == c2.group)) {
                if (c1.relSnoId == c2.relSnoId) {
                    return 1; // SAME LOGICAL REL, SAME SNOMED_ID
                } else {
                    return 5; // SAME LOGICAL REL, CHANGED SNOMED_ID
                }
            } else if (c1.relSnoId == c2.relSnoId) {
                if (isUserChanged(c1.characteristic, c2.characteristic) == false) {
                    return 2; // SAME LOGICAL REL, SAME SNOMED_ID, MODIFIED OTHER
                } else {
                    return 7; // SAME LOGICAL REL, SAME SNOMED_ID, MODIFIED USER
                }
            } else if (isUserChanged(c1.characteristic, c2.characteristic) == false) {
                return 6; // SAME LOGICAL REL, CHANGED SNOMED_ID, MODIFIED OTHER
            } else {
                return 8; // SAME LOGICAL REL, CHANGED SNOMED_ID, MODIFIED USER
            }

        } else if (c1.relUuidMsb > c2.relUuidMsb) {
            return 3; // ADDED

        } else if (c1.relUuidMsb == c2.relUuidMsb && c1.relUuidLsb > c2.relUuidLsb) {
            return 3; // ADDED

        } else {
            return 4; // DROPPED
        }
    }

    private boolean isUserChanged(int older, int newer) {
        if (older == newer) {
            return false;
        }
        if (older != 0 && newer != 0) // both not defining
        {
            return false;
        }
        return true;
    }

    private void parseConcepts(String fName, Sct1_ConRecord[] a, int count, boolean ctv3idTF,
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

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fName),
                "UTF-8"));
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
            a[concepts] = new Sct1_ConRecord(conceptKey, conceptStatus, ctv3Str, snomedrtStr,
                    isPrimitive);
            concepts++;
        }
        Arrays.sort(a);

        getLog().info(
                "Parse & sort time: " + concepts + " concepts, "
                + (System.currentTimeMillis() - start) + " milliseconds");
    }

    private void parseDescriptions(String fName, Sct1_DesRecord[] a, int count) throws Exception {

        long start = System.currentTimeMillis();

        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fName),
                "UTF-8"));
        int descriptions = 0;

        int DESCRIPTIONID = 0;
        int DESCRIPTIONSTATUS = 1;
        int CONCEPTID = 2;
        int TERM = 3;
        int INITIALCAPITALSTATUS = 4;
        int DESCRIPTIONTYPE = 5;
        int LANGUAGECODE = 6;

        int RF1_UNSPECIFIED = 0;
        int RF1_PREFERRED = 1;
        int RF1_SYNOMYM = 2;

        // Header row
        r.readLine();

        while (r.ready()) {
            String[] line = r.readLine().split(TAB_CHARACTER);

            // DESCRIPTIONID
            long descriptionId = Long.parseLong(line[DESCRIPTIONID]);
            // DESCRIPTIONSTATUS
            int status = Integer.parseInt(line[DESCRIPTIONSTATUS]);
            // CONCEPTID
            long conSnoId = Long.parseLong(line[CONCEPTID]);
            // TERM
            String text = line[TERM];
            // INITIALCAPITALSTATUS
            int capStatus = Integer.parseInt(line[INITIALCAPITALSTATUS]);
            // DESCRIPTIONTYPE
            int typeInt = Integer.parseInt(line[DESCRIPTIONTYPE]);
            if (rf2Mapping == true && (typeInt == RF1_UNSPECIFIED || typeInt == RF1_PREFERRED)) {
                typeInt = RF1_SYNOMYM;
            }
            // LANGUAGECODE
            String lang = line[LANGUAGECODE];

            // Save to sortable array
            a[descriptions] = new Sct1_DesRecord(descriptionId, status, conSnoId, text, capStatus,
                    typeInt, lang);
            descriptions++;

        }
        Arrays.sort(a);

        getLog().info(
                "Parse & sort time: " + descriptions + " descriptions, "
                + (System.currentTimeMillis() - start) + " milliseconds");
    }

    private Sct1_RelRecord[] parseRelationships(String fName, Sct1_RelRecord[] a, int count,
            SCTFile f) throws Exception {

        long start = System.currentTimeMillis();

        int RELATIONSHIPID = 0;
        int CONCEPTID1 = 1;
        int RELATIONSHIPTYPE = 2;
        int CONCEPTID2 = 3;
        int CHARACTERISTICTYPE = 4;
        int REFINABILITY = 5;
        int RELATIONSHIPGROUP = 6;

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fName),
                "UTF-8"));
        int relationships = 0;

        // Header row
        br.readLine();

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // RELATIONSHIPID
            long relID = Long.MAX_VALUE;
            if ((f.isStated == true && f.hasStatedSctRelId) || f.isStated == false) {
                relID = Long.parseLong(line[RELATIONSHIPID]);
            }
            // ADD STATUS VALUE: see ArchitectonicAuxiliary.getStatusFromId()
            // STATUS VALUE MUST BE ADDED BECAUSE NOT PRESENT IN SNOMED INPUT
            int status = 0; // status added as CURRENT '0' for parsed record

            // CONCEPTID1
            long conceptOneID = Long.parseLong(line[CONCEPTID1]);
            // RELATIONSHIPTYPE
            long roleTypeSnoId = Long.parseLong(line[RELATIONSHIPTYPE]);
            int roleTypeIdx = lookupRoleTypeIdxFromSnoId(roleTypeSnoId);
            // CONCEPTID2
            long conceptTwoID = Long.parseLong(line[CONCEPTID2]);
            // CHARACTERISTICTYPE
            int characteristic = Integer.parseInt(line[CHARACTERISTICTYPE]);
            // REFINABILITY
            int refinability = Integer.parseInt(line[REFINABILITY]);
            // RELATIONSHIPGROUP
            int group = Integer.parseInt(line[RELATIONSHIPGROUP]);

            // Save to sortable array
            int pathIdx = f.pathIdx;
            int userIdx = USER_DEFAULT_IDX;
            // 0=Defining
            if (characteristic == 0 && f.isStated) {
                pathIdx = f.pathStatedIdx;
                characteristic = STATED_CHAR_WORKAROUND; // :NOTE: transient use for STATED_RELATIONSHIP 
            } else if (characteristic == 0) {
                pathIdx = f.pathInferredIdx;
                userIdx = USER_SNOROCKET_IDX;
            }

            // 0=Defining, 1=Qualifier, 2=Historical, 3=Additional, 5=STATED_CHAR_WORKAROUND
            if (characteristic == 0 || (characteristic == 1 && f.keepQualifier)
                    || (characteristic == 2 && f.keepHistorical)
                    || (characteristic == 3 && f.keepAdditional)
                    || characteristic == STATED_CHAR_WORKAROUND) {
                a[relationships] = new Sct1_RelRecord(relID, status, conceptOneID, roleTypeSnoId,
                        roleTypeIdx, conceptTwoID, characteristic, refinability, group, pathIdx,
                        userIdx);
                relationships++;
            } else {
                // :NYI: count "not kept"
            }
            //            if (conceptOneID == 391181005 && roleTypeSnoId == 116680003 && conceptTwoID == 6254007) {
            //                getLog().info(":DEBUG: found 391181005-116680003-6254007");
            //            }
        }

        a = Arrays.copyOf(a, relationships);
        if (useSctRelId) {
            computeRelationshipUuids(a, f.isStated, f.hasStatedSctRelId);
        } else {
            // :DEPRECIATED:
            computeRelationshipUuids_Old(a, f.hasStatedSctRelId, f.mapSctIdInferredToStated);
        }
        Arrays.sort(a);

        getLog().info(
                "Parse & sort time: " + relationships + " relationships, "
                + (System.currentTimeMillis() - start) + " milliseconds");
        return a;
    }

    private Sct1_RelRecord[] removeDuplRels(Sct1_RelRecord[] a) {

        // REMOVE DUPLICATES
        int lenA = a.length;
        ArrayList<Integer> duplIdxList = new ArrayList<Integer>();
        for (int idx = 0; idx < lenA - 2; idx++) {
            if ((a[idx].relUuidMsb == a[idx + 1].relUuidMsb)
                    && (a[idx].relUuidLsb == a[idx + 1].relUuidLsb)) {
                duplIdxList.add(Integer.valueOf(idx));
                getLog().info(
                        "::: WARNING -- Logically Duplicate Relationships:" + "\r\n::: A:" + a[idx]
                        + "\r\n::: B:" + a[idx + 1]);
            }
        }
        if (duplIdxList.size() > 0) {
            Sct1_RelRecord[] b = new Sct1_RelRecord[lenA - duplIdxList.size()];
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
        } else {
            return a;
        }
    }

    private void computeRelationshipUuids(Sct1_RelRecord[] a,
            boolean isStated,
            boolean isStatedSctRelIdPresent)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // SORT BY [C1-Group-RoleType-C2]
        Comparator<Sct1_RelRecord> comp = new Comparator<Sct1_RelRecord>() {

            @Override
            public int compare(Sct1_RelRecord o1, Sct1_RelRecord o2) {
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

        long lastC1 = a[0].c1SnoId;
        int lastGroup = a[0].group;
        String GroupListStr = getGroupListString(a, 0);
        int max = a.length;
        for (int i = 0; i < max; i++) {
            // DETERMINE IF NEW GroupListStr IS NEEDED
            if (lastC1 != a[i].c1SnoId || lastGroup != a[i].group) {
                GroupListStr = getGroupListString(a, i);
            }

            // SET RELATIONSHIP UUID
            if (isStated == true && isStatedSctRelIdPresent == false) {
                UUID uuid = Type5UuidFactory.get(REL_ID_NAMESPACE_UUID_TYPE1 + a[i].c1SnoId
                        + a[i].roleTypeSnoId + a[i].c2SnoId + GroupListStr);
                a[i].relUuidMsb = uuid.getMostSignificantBits();
                a[i].relUuidLsb = uuid.getLeastSignificantBits();
            } else {
                UUID uuid = Type3UuidFactory.fromSNOMED(a[i].relSnoId);
                a[i].relUuidMsb = uuid.getMostSignificantBits();
                a[i].relUuidLsb = uuid.getLeastSignificantBits();
            }

            lastC1 = a[i].c1SnoId;
            lastGroup = a[i].group;
        }
    }

    private void computeRelationshipUuids_Old(Sct1_RelRecord[] a, boolean hasSnomedId,
            boolean doCrossMap) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // SORT BY [C1-Group-RoleType-C2]
        Comparator<Sct1_RelRecord> comp = new Comparator<Sct1_RelRecord>() {

            @Override
            public int compare(Sct1_RelRecord o1, Sct1_RelRecord o2) {
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

        long lastC1 = a[0].c1SnoId;
        int lastGroup = a[0].group;
        String GroupListStr = getGroupListString(a, 0);
        int max = a.length;
        for (int i = 0; i < max; i++) {
            // DETERMINE IF NEW GroupListStr IS NEEDED
            if (lastC1 != a[i].c1SnoId || lastGroup != a[i].group) {
                GroupListStr = getGroupListString(a, i);
            }

            // SET RELATIONSHIP UUID
            UUID uuid = Type5UuidFactory.get(REL_ID_NAMESPACE_UUID_TYPE1 + a[i].c1SnoId
                    + a[i].roleTypeSnoId + a[i].c2SnoId + GroupListStr);
            a[i].relUuidMsb = uuid.getMostSignificantBits();
            a[i].relUuidLsb = uuid.getLeastSignificantBits();

            // :DEBUG
            // if (uuid.toString().compareToIgnoreCase("8003f34d-e069-57a5-b7db-919fec994ced") == 0)
            //     getLog().info("!!!:PARSE: 8003f34d-e069-57a5-b7db-919fec994ced... Rel="
            //             + a[i].relSnoId + ":" + a[i].c1SnoId + "-" + a[i].roleTypeSnoId + "-"
            //             + a[i].c2SnoId + " G" + a[i].group + " RG(" + GroupListStr + ")");
            // if (a[i].c1SnoId == 391181005 && a[i].roleTypeSnoId == 116680003 && a[i].c2SnoId == 6254007) {
            //     getLog().info(":DEBUG: found 391181005-116680003-6254007 (compute uuids)");
            // }

            // UPDATE SNOMED ID
            if (doCrossMap) {
                if (hasSnomedId) // relUuidMap.put(a[i].relUuidMsb, a[i].relUuidLsb, a[i].relSnoId);
                {
                    relUuidMap.put(uuid, new Long(a[i].relSnoId));
                } else {
                    // a[i].relSnoId = relUuidMap.get(a[i].relUuidMsb, a[i].relUuidLsb);
                    Long tmpLong = relUuidMap.get(uuid);
                    if (tmpLong != null) {
                        a[i].relSnoId = relUuidMap.get(uuid).longValue();
                    } else {
                        a[i].relSnoId = Long.MAX_VALUE;
                    }
                }
            }

            lastC1 = a[i].c1SnoId;
            lastGroup = a[i].group;
        }
    }

    private String getGroupListString(Sct1_RelRecord[] a, int startIdx) {
        StringBuilder sb = new StringBuilder();

        int max = a.length;
        if (a[startIdx].group > 0) {
            long keepC1 = a[startIdx].c1SnoId;
            int keepGroup = a[startIdx].group;
            int i = startIdx;
            while ((i < max - 1) && (a[i].c1SnoId == keepC1) && (a[i].group == keepGroup)) {
                sb.append(a[i].c1SnoId).append("-");
                sb.append(a[i].roleTypeSnoId).append("-");
                sb.append(a[i].c2SnoId).append(";");
                i++;
            }
        }
        return sb.toString();
    }

    private void writeConcepts(ObjectOutputStream oos, Sct1_ConRecord[] a, int count,
            long releaseDateTime, int pathIdx) throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            a[i].path = pathIdx;
            a[i].revTime = releaseDateTime;
            oos.writeUnshared(a[i]);

            // PERIODIC RESET IMPROVES MEMORY USE
            if (i % ooResetInterval == 0) {
                oos.reset();
            }
        }

        getLog().info(
                "Output time: " + count + " records, " + (System.currentTimeMillis() - start)
                + " milliseconds");
    }

    private void writeDescriptions(ObjectOutputStream oos, Sct1_DesRecord[] a, int count,
            long releaseDateTime, int pathIdx) throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            a[i].pathIdx = pathIdx;
            a[i].revTime = releaseDateTime;
            oos.writeUnshared((Object) a[i]);

            // PERIODIC RESET IMPROVES MEMORY USE
            if (i % ooResetInterval == 0) {
                oos.reset();
            }
        }

        getLog().info(
                "Output time: " + count + " records, " + (System.currentTimeMillis() - start)
                + " milliseconds");
    }

    private void writeRelationships(ObjectOutputStream oos, ObjectOutputStream oosIds,
            Sct1_RelRecord[] a, int count, long releaseDateTime, int user) throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            a[i].revTime = releaseDateTime;
            oos.writeUnshared(a[i]);

            if (a[i].relSnoId < Long.MAX_VALUE) {
                Sct1_IdRecord id = new Sct1_IdRecord(a[i].relUuidMsb, a[i].relUuidLsb,
                        uuidSourceSnomedIdx, a[i].relSnoId, a[i].status, a[i].revTime,
                        a[i].pathIdx, user);
                oosIds.writeUnshared(id);
            }

            // PERIODIC RESET IMPROVES MEMORY USE
            if (i % ooResetInterval == 0) {
                oos.reset();
                oosIds.reset();
            }
        }

        getLog().info(
                "Output time: " + count + " records, " + (System.currentTimeMillis() - start)
                + " milliseconds");
    }

    private void stateSave(String wDir) {
        try {
            String fNameState = wDir + scratchDirectory + FILE_SEPARATOR + "state.ser";
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(fNameState)));

            oos.writeObject(zAuthorMap);
            oos.writeObject(zAuthorList);
            oos.writeObject(Integer.valueOf(zAuthorIdxCounter));

            oos.writeObject(zPathMap);
            oos.writeObject(zPathList);
            oos.writeObject(Integer.valueOf(zPathIdxCounter));

            oos.writeObject(zSourceUuidMap);
            oos.writeObject(zSourceUuidList);
            oos.writeObject(Integer.valueOf(zSourceUuidIdxCounter));

            oos.close();
        } catch (Exception e) {
            getLog().info(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void stateRestore(String wDir) {
        try {
            String fNameState = wDir + scratchDirectory + FILE_SEPARATOR + "state.ser";
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(fNameState)));

            zAuthorMap = (HashMap<String, Integer>) ois.readObject();
            zAuthorList = (ArrayList<String>) ois.readObject();
            zAuthorIdxCounter = (Integer) ois.readObject();

            zPathMap = (HashMap<String, Integer>) ois.readObject();
            zPathList = (ArrayList<String>) ois.readObject();
            zPathIdxCounter = (Integer) ois.readObject();

            zSourceUuidMap = (HashMap<String, Integer>) ois.readObject();
            zSourceUuidList = (ArrayList<String>) ois.readObject();
            zSourceUuidIdxCounter = (Integer) ois.readObject();

            ois.close();
        } catch (Exception e) {
            getLog().info(e);
        }

    }

    private long convertDateStrToTime(String date) throws ParseException {
        if (date.contains(".")) {
            return (arfSimpleDateFormatDot.parse(date)).getTime();
        } else {
            return arfSimpleDateFormat.parse(date).getTime();
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

    private void countCheck(int count1, int count2, int same, int modified, int added, int dropped,
            int idmod, int idonly) {

        // CHECK COUNTS TO MASTER FILE1 RECORD COUNT
        if ((same + modified + dropped + idmod + idonly) == count1) {
            getLog().info(
                    "PASSED1:: SAME+MODIFIED+DROPPED+MODIFIED_IDCHANGE+IDCHANGEONLY = " + same
                    + "+" + modified + "+" + dropped + "+" + idmod + "+" + idonly + " = "
                    + (same + modified + dropped + idmod + idonly) + " == " + count1);
        } else {
            getLog().info(
                    "FAILED1:: SAME+MODIFIED+DROPPED+MODIFIED_IDCHANGE+IDCHANGEONLY = " + same
                    + "+" + modified + "+" + dropped + "+" + idmod + "+" + idonly + " = "
                    + (same + modified + dropped + idmod + idonly) + " != " + count1);
        }

        // CHECK COUNTS TO UPDATE FILE2 RECORD COUNT
        if ((same + modified + added + idmod + idonly) == count2) {
            getLog().info(
                    "PASSED2:: SAME+MODIFIED+ADDED+MODIFIED_IDCHANGE+IDCHANGEONLY   = " + same
                    + "+" + modified + "+" + added + "+" + idmod + "+" + idonly + " = "
                    + (same + modified + added + idmod + idonly) + " == " + count2);
        } else {
            getLog().info(
                    "FAILED2:: SAME+MODIFIED+ADDED+MODIFIED_IDCHANGE+IDCHANGEONLY   = " + same
                    + "+" + modified + "+" + added + "+" + idmod + "+" + idonly + " = "
                    + (same + modified + added + idmod + idonly) + " != " + count2);
        }

    }

    private static int countFileLines(String fileName) throws MojoFailureException {
        int lineCount = 0;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            try {
                while (br.readLine() != null) {
                    lineCount++;
                }
            } catch (IOException ex) {
                throw new MojoFailureException("FAILED: error counting lines in " + fileName, ex);
            } finally {
                br.close();
            }
        } catch (IOException ex) {
            throw new MojoFailureException("FAILED: error open BufferedReader for " + fileName, ex);
        }

        // lineCount NOTE: COUNT -1 BECAUSE FIRST LINE SKIPPED
        // lineCount NOTE: REQUIRES THAT LAST LINE IS VALID RECORD
        return lineCount - 1;
    }

    private int countFileObjects(String fName) throws FileNotFoundException, IOException,
            ClassNotFoundException {
        int objCount = 0;

        ObjectInputStream ois;
        ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fName)));
        try {
            while ((ois.readObject()) != null) {
                objCount++;
            }
        } catch (EOFException ex) {
            getLog().info(" object count = " + objCount + " @EOF " + fName + "\r\n");
        }

        return objCount;
    }

    /**
     * Returns file date string in "yyyy-MM-dd 00:00:00" format.
     * @param f
     * @return
     * @throws MojoFailureException
     */
    private String getFileRevDate(File f) throws MojoFailureException {
        int pos;
        // Check file name for date yyyyMMdd
        // EXAMPLE: ../net/nhs/uktc/ukde/sct1_relationships_uk_drug_20090401.txt
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
            String name = files[i].getName().toUpperCase();

            if (files[i].isFile() && name.endsWith(postfix.toUpperCase())
                    && name.contains(prefix.toUpperCase())) {
                list.add(files[i]);
            }
            if (files[i].isDirectory()) {
                listFilesRecursive(list, files[i], prefix, postfix);
            }
        }
    }
}