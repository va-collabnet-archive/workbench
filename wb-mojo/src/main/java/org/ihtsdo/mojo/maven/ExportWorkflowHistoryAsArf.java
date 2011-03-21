package org.ihtsdo.mojo.maven;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrMember;

//import org.test.org.ihtsdo.workflow.refset.utilities.WorkflowRefsetHelper;

/**
 * Export the workflow history to initialize the WfHx refset in the database
 * in ARF format.
 *
 * @goal export-workflow-history-as-arf
 * @phase process-resources
 */

public class ExportWorkflowHistoryAsArf extends AbstractMojo {

    /**
     *
     * @author Jesse Efron
     *
     */

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File projBuildDirectory;

    /**
     * Applicable input sub directory under the target directory.
     * 
     * @parameter default-value=""
     */
    private String targetSubDir = "";

    /**
     * Location of the build directory.
     *
     * @parameter
     * @required
     */
    private String outputDirectory;

    /**
     * Editing path UUIDs
     *
     * @parameter default-value="generated-arf"
     */
    private String editPathUuid;

    /**
     * Specifies whether to create new files (default) or append to existing
     * files.
     *
     * @required
     * @parameter
     */
    private String inputFilePath;

    /**
     * Start date (inclusive)
     * 
     * @parameter
     */
    private String dateRefset;
    private Date dateRefsetObj;

    private static final int workflowIdPosition = 0;
    private static final int conceptIdPosition = 1;
    private static final int useCasePosition = 2;
    private static final int pathPosition = 3;
    private static final int modelerPosition = 4;
    private static final int actionPosition = 5;
    private static final int statePosition = 6;
    private static final int fsnPosition = 7;
    private static final int timeStampPosition = 8;

    private String wfHxRefsetParentUuidStr = null;
    private String wfHxRefsetUuidStr = null;
    private String activeUuidStr;
    private String pathUuidStr = null;
    private String currentConceptUuid = null;
    private HashMap<String, String> authorNameUuidMap;

    private static final String GENERIC_USER_UUID = "f7495b58-6630-3499-a44e-2052b5fcf06c";

    private static final String WORKFLOW_NAMESPACE_UUID_TYPE1 = "09cdec60-4fff-11e0-b8af-0800200c9a66";

    public void execute() throws MojoExecutionException {
        getLog().info("Exporting workflow history to ARF");

        try {
            initializeConstantUuids();

            // INPUT FILE SETUP
            BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(
                    inputFilePath), "UTF-8"));

            // OUTPUT FILE SETUP
            File directory = null;
            if (!targetSubDir.equals(""))
                directory = new File(projBuildDirectory.getCanonicalPath() + File.separatorChar
                        + targetSubDir + File.separatorChar + outputDirectory + File.separatorChar);
            else
                directory = new File(projBuildDirectory.getCanonicalPath() + File.separatorChar
                        + outputDirectory + File.separatorChar);

            directory.mkdirs();

            saveRefsetConcepts(directory.toString());

            File outputFile = new File(directory, "string_wfHistory.refset");
            outputFile.getParentFile().mkdirs();
            getLog().info("ARF path: " + outputFile.getPath());

            Writer bWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    outputFile), "UTF-8"));

            // CONVERT EACH LINE TO ARF
            while (bReader.ready()) {
                String[] line = bReader.readLine().split("\t");
                bWriter.write(toArfString(line));
            }

            bWriter.flush();
            bWriter.close();
            bReader.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private String toArfString(String[] row) throws IOException, TerminologyException,
            ParseException {
        StringBuffer sb = new StringBuffer();

        // [0] Refset Uuid
        sb.append(wfHxRefsetUuidStr + "\t");

        // [1] Refset member Uuid
        sb.append(UUID.randomUUID().toString() + "\t");

        // [2] Member Status Active/Inactive
        sb.append(activeUuidStr + "\t");

        // [3] Refset Component Uuid
        sb.append(row[conceptIdPosition] + "\t");

        // [4] EFFECTIVE DATE
        sb.append(row[timeStampPosition] + "\t");

        // [5] Path writing on
        sb.append(pathUuidStr + "\t");

        // [6] STRING VALUE
        sb.append("toXml(row) goes here" + "\t");

        // [7] Author of WfHx
        String author = authorNameUuidMap.get(row[modelerPosition]);
        if (author == null)
            author = GENERIC_USER_UUID;
        sb.append(author + "\r\n");

        return sb.toString();
    }

    private void initializeConstantUuids() throws IOException, TerminologyException,
            NoSuchAlgorithmException {
        wfHxRefsetParentUuidStr = RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getPrimoridalUid()
                .toString();

        wfHxRefsetUuidStr = Type5UuidFactory.get(
                WORKFLOW_NAMESPACE_UUID_TYPE1 + "Workflow Refset UUID").toString();

        activeUuidStr = ArchitectonicAuxiliary.Concept.ACTIVE.getPrimoridalUid().toString();

        pathUuidStr = ArchitectonicAuxiliary.Concept.SNOMED_CORE.getPrimoridalUid().toString();

        authorNameUuidMap = new HashMap<String, String>();
        authorNameUuidMap.put("alopez", "800e6651-a619-3edf-bb90-74ab279966c9");
        authorNameUuidMap.put("mvanber", "09422a35-01ed-3249-ba7a-0b7fe63472e3");
        authorNameUuidMap.put("mgerard", "471db693-66d0-38b2-bf95-72f5e680d478");
        authorNameUuidMap.put("khaake", "3bcb4e92-129b-3319-9148-17dca4ce9914");

    }

    private void saveRefsetConcepts(String arfDir) throws MojoFailureException {

        try {
            Writer concepts = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(arfDir, "concepts_workflow_history.txt")), "UTF-8"));
            Writer descriptions = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(arfDir, "descriptions_workflow_history.txt")), "UTF-8"));
            Writer relationships = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(arfDir, "relationships_workflow_history.txt")), "UTF-8"));

            saveRefsetConcept(wfHxRefsetUuidStr, wfHxRefsetParentUuidStr, "Workflow History",
                    concepts, descriptions, relationships);

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

    private void saveRefsetConcept(String refsetUuid, String refsetParentUuid, String name,
            Writer concepts, Writer descriptions, Writer relationships) throws IOException,
            NoSuchAlgorithmException {
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String effectiveDate = format.format(dateRefsetObj);

            concepts.append(refsetUuid); // refset concept uuid
            concepts.append("\t");
            concepts.append(activeUuidStr); //status uuid
            concepts.append("\t");
            concepts.append("1"); // primitive
            concepts.append("\t");
            concepts.append(effectiveDate); // effective date
            concepts.append("\t");
            concepts.append(pathUuidStr); //path uuid
            concepts.append("\n");

            descriptions.append(Type5UuidFactory
                    .get(
                            WORKFLOW_NAMESPACE_UUID_TYPE1
                                    + "Workflow History Fully Specified Name " + name).toString()); // description uuid
            descriptions.append("\t");
            descriptions.append(activeUuidStr); // status uuid
            descriptions.append("\t");
            descriptions.append(refsetUuid); // refset concept uuid
            descriptions.append("\t");
            descriptions.append(name); // term
            descriptions.append("\t");
            descriptions.append("1"); // primitive
            descriptions.append("\t");
            descriptions.append(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
                    .getUids().iterator().next().toString()); // description type uuid
            descriptions.append("\t");
            descriptions.append("en"); // language code
            descriptions.append("\t");
            descriptions.append(effectiveDate); // effective date
            descriptions.append("\t");
            descriptions.append(pathUuidStr); //path uuid
            descriptions.append("\n");

            descriptions.append(Type5UuidFactory.get(
                    WORKFLOW_NAMESPACE_UUID_TYPE1 + "Workflow History Preferred Name " + name)
                    .toString()); // description uuid
            descriptions.append("\t");
            descriptions.append(activeUuidStr); // status uuid
            descriptions.append("\t");
            descriptions.append(refsetUuid); // refset concept uuid
            descriptions.append("\t");
            descriptions.append(name); // term
            descriptions.append("\t");
            descriptions.append("1"); // primitive
            descriptions.append("\t");
            descriptions.append(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
                    .getUids().iterator().next().toString()); // description type uuid
            descriptions.append("\t");
            descriptions.append("en"); // language code
            descriptions.append("\t");
            descriptions.append(effectiveDate); // effective date
            descriptions.append("\t");
            descriptions.append(pathUuidStr); //path uuid
            descriptions.append("\n");

            relationships.append(Type5UuidFactory.get(
                    WORKFLOW_NAMESPACE_UUID_TYPE1 + "Relationship" + refsetUuid + refsetParentUuid)
                    .toString()); // relationship uuid
            relationships.append("\t");
            relationships.append(activeUuidStr); // status uuid
            relationships.append("\t");
            relationships.append(refsetUuid); // refset source concept uuid
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
            relationships.append(effectiveDate); // effective date
            relationships.append("\t");
            relationships.append(pathUuidStr); // path uuid
            relationships.append("\n");
        }
    }

    private String toXml(String[] row) throws IOException, TerminologyException {
        try {

            //        UUID modeler = WorkflowRefsetHelper.lookupModelerUid(row[modelerPosition]);
            UUID modeler = ArchitectonicAuxiliary.Concept.ROBERT_TURNBULL.getUids().iterator()
                    .next();
            //        String state = WorkflowRefsetHelper.getStateUid(row[statePosition]).toString();
            String state = "PLACE_HOLDER_STATE";
            //        String action = WorkflowRefsetHelper.getActionUid(row[actionPosition]).toString();
            String action = "PLACE_HOLDER_ACTION";
            String useCase = getCaseTypeUidStringByUseCase(row[useCasePosition]);

            return "<properties>" + "<property>" + "<key>workflowId</key>" + "<value>"
                    + row[workflowIdPosition] + "</value>" + "</property>" + "<property>"
                    + "<key>useCase</key>" + "<value>" + useCase + "</value>" + "</property>"
                    + "<property>" + "<key>path</key>" + "<value>" + row[pathPosition] + "</value>"
                    + "</property>" + "<property>" + "<key>modeler</key>" + "<value>" + modeler
                    + "</value>" + "</property>" + "<property>" + "<key>state</key>" + "<value>"
                    + state + "</value>" + "</property>" + "<property>" + "<key>action</key>"
                    + "<value>" + action + "</value>" + "</property>" + "<property>"
                    + "<key>fsn</key>" + "<value>" + row[fsnPosition] + "</value>" + "</property>"
                    + "</properties>";
        } catch (Exception e) {
            System.out.println("Offending Row:");
            System.out.println("Modeler: " + row[modelerPosition]);
            System.out.println("State: " + row[statePosition]);
            System.out.println("Action: " + row[actionPosition]);
            return "";
        }
    }

    private String getCaseTypeUidStringByUseCase(String input) {
        if (input.equals("NEW"))
            return "05395fa5-d8ea-3f0f-8850-8795ea0f9aea";
        else
            return "de6a2fcf-24b7-3a46-aa62-27d1958e3a16";
    }

    public String getDateRefset() {
        return this.dateRefset;
    }

    public void setDateRefset(String sStart) throws MojoFailureException {
        this.dateRefset = sStart;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        try {
            this.dateRefsetObj = formatter.parse(sStart + " 00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
            throw new MojoFailureException("SimpleDateFormat yyyy.MM.dd dateStart parse error: "
                    + sStart);
        }
        getLog().info("::: REFSET DATE " + this.dateRefset);
    }

}