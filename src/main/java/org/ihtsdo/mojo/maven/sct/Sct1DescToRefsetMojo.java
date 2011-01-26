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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.mojo.maven.rf1.Rf1Dir;

/**
 * 
 * @author marc
 *
 * @goal sct1-descriptions-to-refset
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class Sct1DescToRefsetMojo extends AbstractMojo implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String FILE_SEPARATOR = File.separator;
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";

    private static final String UUID_NORMAL_MEMBER = "cc624429-b17d-4ac5-a69e-0b32448aaf3c";

    private static String uuidCurrentStr = null;
    private static String uuidRetiredStr = null;

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
     * @parameter 
     * @required
     */
    private String inputDir;

    /**
     * @parameter default-value="false"
     */
    private boolean rf2Mapping;
    
    /**
     * language concept -> acceptability -> acceptable UUID
     * 
     * @parameter default-value="51b45763-09c4-34eb-a303-062ba8e0c0e9"
     */
    private String uuidAcceptable;

    /**
     * language concept -> acceptability -> preferred acceptability UUID
     * 
     * @parameter default-value="15877c09-60d7-3464-bed8-635a98a7e5b2"
     */
    private String uuidPrefAccept;

    /**
     * Directory used to output the eConcept format files
     * Default value "/classes" set programmatically due to file separator
     * 
     * @parameter default-value="generated-arf"
     */
    private String outputDir;

    /**
     * @parameter
     * @required
     */
    private String outputInfix;

    /**
     * @parameter
     * @required
     */
    String refsetFsName;

    /**
     * @parameter
     * @required
     */
    String refsetPrefTerm;

    /**
     * @parameter
     * @required
     */
    String refsetPathUuid;

    /**
     * @parameter
     * @required
     */
    String refsetDate;
    /**
     * @parameter
     * @required
     */
    String refsetParentUuid;

    private UUID refsetUuid;

    private String refsetUuidStr;
    
    private static final String decriptionTypeUuidStrArray[] = new String[5]; 

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            executeMojo(targetDirectory, targetSubDir, inputDir, outputDir);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new MojoFailureException("MojoFailureException: Parse");
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("MojoFailureException: IOException");
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoFailureException("MojoFailureException: Exception");
        }
    }

    public void executeMojo(File tDir, String tSubDir, String inDir, String outDir)
            throws Exception {
        
        // SETUP DESCRIPTION TYPES
        for (int i = 0; i < 5; i++)
            decriptionTypeUuidStrArray[i] = ArchitectonicAuxiliary.getSnomedDescriptionType(i).getUids()
                    .iterator().next().toString();

        // REFORMAT THE DATE
        // :NYI: possible to add support of other date formats here...
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
        Date d = format.parse(refsetDate);
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        refsetDate = format.format(d);

        // SETUP INPUT FILES
        String fPathInDir = tDir + FILE_SEPARATOR + tSubDir + inDir;
        List<Sct1File> inFiles = Sct1File.getSctFiles(tDir.getAbsolutePath(), tSubDir, inDir,
                "descriptions", ".txt");
        Collections.sort(inFiles);
        getLog().info(":::  Input Path " + fPathInDir);
        getLog().info(":::  Input " + inFiles.toString());

        // SETUP OUTPUT FILE
        String fPathOutDir = tDir + FILE_SEPARATOR + tSubDir + FILE_SEPARATOR + outDir;
        boolean success = (new File(fPathOutDir)).mkdirs();
        if (success) 
            getLog().info("::: OUTPUT PATH " + fPathOutDir);
        String fNameSubsetConRefsetArf = fPathOutDir + FILE_SEPARATOR + "concept_" + outputInfix + ".refset";
        BufferedWriter bwRefset = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                fNameSubsetConRefsetArf), "UTF-8"));
        getLog().info("::: OUTPUT: " + fNameSubsetConRefsetArf);

        UUID uuidCurrent = ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next();
        uuidCurrentStr = uuidCurrent.toString();
        UUID uuidRetired = ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next();
        uuidRetiredStr = uuidRetired.toString();

        this.refsetUuid = Type5UuidFactory.get(Rf1Dir.SUBSETREFSET_ID_NAMESPACE_UUID_TYPE1
                + refsetFsName);
        this.refsetUuidStr = refsetUuid.toString();

        saveRefsetConcept(fPathOutDir, outputInfix, refsetPathUuid);
        processDescriptionsFiles(bwRefset, inFiles, refsetPathUuid);
        
        bwRefset.close();
    }

    protected void processDescriptionsFiles(BufferedWriter out, List<Sct1File> fl,
            String uuidPathStr) throws Exception {
        int count1, count2; // records in arrays 1 & 2
        Sct1_DesRecord[] a1, a2, a3 = null;

        getLog().info("START DESCRIPTIONS PROCESSING...");

        Iterator<Sct1File> fit = fl.iterator(); // File Iterator

        // READ file1 as MASTER FILE
        Sct1File f1 = fit.next();
        Date date = f1.revDate;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = formatter.format(date);
        getLog().info("::: ... " + f1.file.getName());

        a1 = Sct1_DesRecord.parseDescriptions(f1);
        count1 = a1.length;

        for (int i = 0; i < a1.length; i++)
            writeToArfFile(a1[i], dateStr, uuidPathStr, out);

        while (fit.hasNext()) {
            // SETUP CURRENT CONCEPTS INPUT FILE
            Sct1File f2 = fit.next();
            date = f2.revDate;
            dateStr = formatter.format(date);
            getLog().info("::: ... " + f2.file.getName());

            // Parse in file2
            a2 = Sct1_DesRecord.parseDescriptions(f2);
            count2 = a2.length;

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
                    writeToArfFile(a2[r2], dateStr, uuidPathStr, out);

                    // Update master via pointer assignment
                    a1[r1] = a2[r2];
                    r1++;
                    r2++;
                    nMod++;
                    break;

                case 3: // ADDED DESCRIPTION
                    // Write history
                    writeToArfFile(a2[r2], dateStr, uuidPathStr, out);

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
                        writeToArfFile(a1[r1], dateStr, uuidPathStr, out);
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
                    writeToArfFile(a2[r2], dateStr, uuidPathStr, out);

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

    }

    private int compareDescription(Sct1_DesRecord d1, Sct1_DesRecord d2) {

        if (d1.desUuidMsb == d2.desUuidMsb && d1.desUuidLsb == d2.desUuidLsb) {
            if (d1.status == d2.status && d1.descriptionType == d2.descriptionType)
                // REFSET ONLY TRACKS THE STATUS
                // d1.languageCode.contentEquals(d2.languageCode)
                // d1.termText.equals(d2.termText)
                // d1.capStatus == d2.capStatus
                // d1.conSnoId == d2.conSnoId
                return 1; // SAME
            else
                return 2; // MODIFIED

        } else if (d1.desUuidMsb > d2.desUuidMsb) {
            return 3; // ADDED

        } else if (d1.desUuidMsb == d2.desUuidMsb && d1.desUuidLsb > d2.desUuidLsb) {
            return 3; // ADDED

        } else {
            return 4; // DROPPED
        }
    }

    private void writeToArfFile(Sct1_DesRecord m, String date, String path, BufferedWriter bw)
            throws NoSuchAlgorithmException, IOException {
        UUID desUuid = new UUID(m.desUuidMsb, m.desUuidLsb);

        // REFSET_UUID
        bw.write(refsetUuidStr + TAB_CHARACTER);
        // MEMBER_UUID ... of refset member
        UUID uuid = Type5UuidFactory.get(Rf1Dir.SUBSETMEMBER_ID_NAMESPACE_UUID_TYPE1 + refsetFsName
                + desUuid);
        bw.write(uuid.toString() + TAB_CHARACTER);
        // STATUS_UUID
        if (m.status == 0)
            bw.write(uuidCurrentStr + TAB_CHARACTER);
        else
            bw.write(uuidRetiredStr + TAB_CHARACTER);

        // COMPONENT_UUID ... of member's referenced (concept, description, ...) component
        bw.write(desUuid.toString() + TAB_CHARACTER);
        // EFFECTIVE_DATE
        bw.write(date + TAB_CHARACTER);
        // PATH_UUID
        bw.write(path + TAB_CHARACTER);

        // SNOMED_CONCEPT_VALUE
        if (rf2Mapping) {
            if (m.descriptionType == 1) // 1 == preferred ... preferred acceptability
                bw.write(uuidPrefAccept + LINE_TERMINATOR);
            else if (m.descriptionType == 3) // 3 == FSN ... preferred acceptability
                bw.write(uuidPrefAccept + LINE_TERMINATOR);
            else // 2 == synonym ... acceptable
                // ... includes 0 = unspecified ... acceptable
                bw.write(uuidAcceptable + LINE_TERMINATOR);
        } else {
            if (m.descriptionType >= 0 && m.descriptionType < 4)
                bw.write(decriptionTypeUuidStrArray[m.descriptionType] + LINE_TERMINATOR);
            else
                bw.write(decriptionTypeUuidStrArray[4] + LINE_TERMINATOR);
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

    private void saveRefsetConcept(String arfDir, String infix, String pathStr)
            throws MojoFailureException {

        try {

            Writer concepts;
            concepts = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(
                    arfDir, "concepts_" + infix + ".txt")), "UTF-8"));
            Writer descriptions = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(arfDir, "descriptions_" + infix + ".txt")), "UTF-8"));
            Writer relationships = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(arfDir, "relationships_" + infix + ".txt")), "UTF-8"));

            concepts.append(refsetUuidStr); // refset concept uuid
            concepts.append("\t");
            concepts.append(uuidCurrentStr); //status uuid
            concepts.append("\t");
            concepts.append("1"); // primitive
            concepts.append("\t");
            concepts.append(refsetDate); // effective date
            concepts.append("\t");
            concepts.append(pathStr); //path uuid
            concepts.append("\n");

            // DESCRIPTION - FULLY SPECIFIED
            descriptions.append(Type5UuidFactory.get(
                    Rf1Dir.SUBSETREFSET_ID_NAMESPACE_UUID_TYPE1 + "Subset Fully Specified Name"
                            + refsetUuidStr).toString()); // description uuid
            descriptions.append("\t");
            descriptions.append(uuidCurrentStr); // status uuid
            descriptions.append("\t");
            descriptions.append(refsetUuidStr); // refset concept uuid
            descriptions.append("\t");
            descriptions.append(refsetFsName); // term
            descriptions.append("\t");
            descriptions.append("1"); // primitive
            descriptions.append("\t");
            descriptions.append(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
                    .getUids().iterator().next().toString()); // description type uuid
            descriptions.append("\t");
            descriptions.append("en"); // language code
            descriptions.append("\t");
            descriptions.append(refsetDate); // effective date
            descriptions.append("\t");
            descriptions.append(pathStr); //path uuid
            descriptions.append("\n");

            // DESCRIPTION - FULLY PREFERRED
            descriptions.append(Type5UuidFactory.get(
                    Rf1Dir.SUBSETREFSET_ID_NAMESPACE_UUID_TYPE1 + "Subset Preferred Name"
                            + refsetUuidStr).toString()); // description uuid
            descriptions.append("\t");
            descriptions.append(uuidCurrentStr); // status uuid
            descriptions.append("\t");
            descriptions.append(refsetUuidStr); // refset concept uuid
            descriptions.append("\t");
            descriptions.append(refsetPrefTerm); // term
            descriptions.append("\t");
            descriptions.append("1"); // primitive
            descriptions.append("\t");
            descriptions.append(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
                    .getUids().iterator().next().toString()); // description type uuid
            descriptions.append("\t");
            descriptions.append("en"); // language code
            descriptions.append("\t");
            descriptions.append(refsetDate); // effective date
            descriptions.append("\t");
            descriptions.append(pathStr); //path uuid
            descriptions.append("\n");

            // RELATIONSHIP
            relationships.append(Type5UuidFactory.get(
                    Rf1Dir.SUBSETREFSET_ID_NAMESPACE_UUID_TYPE1 + "Relationship"
                            + refsetUuidStr).toString()); // relationship uuid
            relationships.append("\t");
            relationships.append(uuidCurrentStr); // status uuid
            relationships.append("\t");
            relationships.append(refsetUuidStr); // refset source concept uuid
            relationships.append("\t");
            relationships.append(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids().iterator()
                    .next().toString()); // relationship type uuid
            relationships.append("\t");
            relationships.append(refsetParentUuid); // destination concept uuid
            relationships.append("\t");
            relationships.append(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()
                    .iterator().next().toString()); // characteristic type uuid
            relationships.append("\t");
            relationships.append(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids().iterator()
                    .next().toString()); // refinability uuid
            relationships.append("\t");
            relationships.append("0"); // relationship group
            relationships.append("\t");
            relationships.append(refsetDate); // effective date
            relationships.append("\t");
            relationships.append(pathStr); // path uuid
            relationships.append("\n");

            concepts.close();
            descriptions.close();
            relationships.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("RefToArfSubsetsMojo IO Error", e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new MojoFailureException("RefToArfSubsetsMojo no such algorithm", e);
        }

    }

}
