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
package org.ihtsdo.mojo.maven.rf1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 *
 * @author Marc E. Campbell
 *
 * @goal arf-append-author-module
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class ArfAppendAuthorModuleMojo extends AbstractMojo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String FILE_SEPARATOR = File.separator;
    /**
     * Line terminator is deliberately set to CR-LF which is DOS style
     */
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
    /**
     * Location of the target directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;
    /**
     * Applicable input sub directory under the target directory.
     *
     * @parameter
     */
    private String targetSubDir = "";
    /**
     * Input Directories Array. The directory array parameter supported extensions via separate
     * directories in the array.
     *
     * @parameter
     */
    private String[] arfInputDirs;
    /**
     * Location of the target directory.
     *
     * @parameter expression
     */
    private String keepMapDir;
    /**
     * user == "f7495b58-6630-3499-a44e-2052b5fcf06c"
     *
     * @parameter default-value="f7495b58-6630-3499-a44e-2052b5fcf06c"
     * @required
     */
    private String authorUuid;
    /**
     * Location of the target directory.<br> SNOMED CT core module ==
     * "1b4f1ba5-b725-390f-8c3b-33ec7096bdca"<br> Module (core metadata concept) ==
     * "40d1c869-b509-32f8-b735-836eac577a67"
     *
     * @parameter default-value="40d1c869-b509-32f8-b735-836eac577a67"
     * @required
     */
    private String moduleUuid;

    @Override
    public void execute()
            throws MojoExecutionException, MojoFailureException {
        getLog().info("::: BEGIN ArfAppendAuthorModuleMojo");
        // SHOW target directory from POM file
        String targetDir = targetDirectory.getAbsolutePath();
        getLog().info("    POM: Target Directory: " + targetDir);

        // SHOW input sub directory from POM file
        if (!targetSubDir.equals("")) {
            targetSubDir = FILE_SEPARATOR + targetSubDir;
            getLog().info("    POM: Target Sub Directory: " + targetSubDir);
        }

        if (arfInputDirs == null) {
            throw new MojoExecutionException("ArfAppendAuthorModuleMojo <arfInputDirs> not provided");
        }

        // if applicable, create an sct id keepMap<sctid, uuid> filter.
        HashMap<Long, UUID> keepMap = null;
        if (keepMapDir != null) {
            keepMap = parseKeepMap(targetDir + FILE_SEPARATOR + keepMapDir + FILE_SEPARATOR + "keep_ids.txt");
        }

        for (int i = 0; i < arfInputDirs.length; i++) {
            arfInputDirs[i] = arfInputDirs[i].replace('/', File.separatorChar);
            getLog().info("    POM: Input Directory (" + i + ") = " + arfInputDirs[i]);
            if (!arfInputDirs[i].startsWith(FILE_SEPARATOR)) {
                arfInputDirs[i] = (FILE_SEPARATOR + arfInputDirs[i]);
            }
        }
        try {
            executeMojo(targetDir, targetSubDir, arfInputDirs, keepMap);
        } catch (TerminologyException | FileNotFoundException ex) {
            getLog().error("ArfAppendAuthorModuleMojo.execute ", ex);
        } catch (IOException ex) {
            getLog().error("ArfAppendAuthorModuleMojo.execute IOException", ex);
        }
    }

    private void executeMojo(String tDir, String tSubDir, String[] inDirs, HashMap<Long, UUID> keepMap)
            throws MojoFailureException, FileNotFoundException, IOException, TerminologyException {
        getLog().info("::: Target Directory: " + tDir);
        getLog().info("::: Target Sub Directory:     " + tSubDir);

        for (String inDirName : inDirs) {
            String dirPathName = tDir + tSubDir + FILE_SEPARATOR + inDirName;
            File dir = new File(dirPathName);
            // list files in directory
            for (String fileName : dir.list()) {
                if (fileName.contains(".refset")
                        || fileName.contains("concepts.txt")
                        || fileName.contains("descriptions.txt")
                        || fileName.contains("relationships.txt")) {
                    String fPathNameIn = dirPathName + FILE_SEPARATOR + fileName;
                    String fPathNameOut = dirPathName + FILE_SEPARATOR + "tmp_" + fileName;
                    appendAuthorModule(new File(fPathNameIn), new File(fPathNameOut));
                } else if (fileName.contains("ids.txt")) {
                    String fPathNameIn = dirPathName + FILE_SEPARATOR + fileName;
                    String fPathNameOut = dirPathName + FILE_SEPARATOR + "tmp_" + fileName;
                    appendAuthorModuleFiltered(new File(fPathNameIn), new File(fPathNameOut), keepMap);
                }
            }
        }
    }

    private void appendAuthorModule(File inFile, File outFile)
            throws FileNotFoundException, IOException {
        getLog().info("appending AuthorId &  ModuleId to: " + inFile.getAbsolutePath());

        FileReader fr = new FileReader(inFile);
        try (BufferedReader br = new BufferedReader(fr)) {
            FileWriter fw = new FileWriter(outFile);
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                String eachLine = br.readLine();
                while (eachLine != null && eachLine.length() > 8) {
                    bw.write(eachLine + TAB_CHARACTER + authorUuid
                            + TAB_CHARACTER + moduleUuid + LINE_TERMINATOR);
                    eachLine = br.readLine();
                }

                bw.flush();
                bw.close();
                br.close();

                inFile.delete();
                outFile.renameTo(inFile);
            }
        }
    }

    private void appendAuthorModuleFiltered(File inFile, File outFile, HashMap<Long, UUID> keepMap)
            throws FileNotFoundException, IOException, TerminologyException {
        getLog().info("appending AuthorId &  ModuleId to: " + inFile.getAbsolutePath());

        FileReader fr = new FileReader(inFile);
        try (BufferedReader br = new BufferedReader(fr)) {
            FileWriter fw = new FileWriter(outFile);
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                String eachLine = br.readLine();
                while (eachLine != null && eachLine.length() > 8) {
                    String[] split = eachLine.split(TAB_CHARACTER);
                    UUID enclosingUuid = UUID.fromString(split[0]); // PRIMARY_UUID 0 
                    UUID sourceSystemUuid = UUID.fromString(split[1]); // SOURCE_SYSTEM_UUID 1
                    UUID intIdUuid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getPrimoridalUid();
                    if (keepMap == null) {
                        bw.write(eachLine + TAB_CHARACTER + authorUuid
                                + TAB_CHARACTER + moduleUuid + LINE_TERMINATOR);
                    } else if (sourceSystemUuid.compareTo(intIdUuid) == 0) {
                        Long sctId = Long.valueOf(split[2]); // SOURCE_ID
                        if (keepMap.containsKey(sctId)) {
                            UUID filterUuid = keepMap.get(sctId);
                            // keep only the id lines which match
                            if (enclosingUuid.compareTo(filterUuid) == 0) {
                                bw.write(eachLine + TAB_CHARACTER + authorUuid
                                        + TAB_CHARACTER + moduleUuid + LINE_TERMINATOR);
                            } else {
                                getLog().info("re-used SCTID-UUID instance not kept\t" + sctId + "\t" + filterUuid.toString());
                            }
                        } else {
                            bw.write(eachLine + TAB_CHARACTER + authorUuid
                                    + TAB_CHARACTER + moduleUuid + LINE_TERMINATOR);
                        }
                    } else {
                        bw.write(eachLine + TAB_CHARACTER + authorUuid
                                + TAB_CHARACTER + moduleUuid + LINE_TERMINATOR);
                    }

                    eachLine = br.readLine();
                }

                bw.flush();
                bw.close();
                br.close();

                inFile.delete();
                outFile.renameTo(inFile);
            }
        }
    }

    private HashMap<Long, UUID> parseKeepMap(String filePathName) {
        getLog().info(filePathName);
        FileReader fr = null;
        try {
            HashMap<Long, UUID> keepMap = new HashMap<>();
            fr = new FileReader(new File(filePathName));
            try (BufferedReader br = new BufferedReader(fr)) {
                String eachLine = br.readLine();
                while (eachLine != null && eachLine.length() > 8) {
                    String[] split = eachLine.split(TAB_CHARACTER);
                    keepMap.put(Long.valueOf(split[0]), UUID.fromString(split[1]));
                    eachLine = br.readLine();
                }
            }
            return keepMap;
        } catch (IOException ex) {
            getLog().error("IO Exception: " + filePathName, ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                getLog().error("IO Exception: (finally) ", ex);
            }
        }
        return null;
    }
}
