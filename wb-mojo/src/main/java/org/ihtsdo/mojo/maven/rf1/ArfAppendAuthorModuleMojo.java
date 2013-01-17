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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

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
     * Location of the target directory. user == "f7495b58-6630-3499-a44e-2052b5fcf06c"
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

        for (int i = 0; i < arfInputDirs.length; i++) {
            arfInputDirs[i] = arfInputDirs[i].replace('/', File.separatorChar);
            getLog().info("    POM: Input Directory (" + i + ") = " + arfInputDirs[i]);
            if (!arfInputDirs[i].startsWith(FILE_SEPARATOR)) {
                arfInputDirs[i] = (FILE_SEPARATOR + arfInputDirs[i]);
            }
        }
        try {
            executeMojo(targetDir, targetSubDir, arfInputDirs);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ArfAppendAuthorModuleMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArfAppendAuthorModuleMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void executeMojo(String tDir, String tSubDir, String[] inDirs)
            throws MojoFailureException, FileNotFoundException, IOException {
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
                        || fileName.contains("relationships.txt")
                        || fileName.contains("ids.txt")) {
                    String fPathNameIn = dirPathName + FILE_SEPARATOR + fileName;
                    String fPathNameOut = dirPathName + FILE_SEPARATOR + "tmp_" + fileName;
                    appendAuthorModule(new File(fPathNameIn), new File(fPathNameOut));
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
}
