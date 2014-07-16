/*
 * Copyright 2014 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.mojo.maven.jws;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonArray;
import javax.json.JsonObject;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 *
 * @author Marc Campbell
 * 
 * @goal jws-dir-to-manifest
 * @requiresProject false
 */
public class JwsDirToManifestMojo extends AbstractMojo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Process Directory. Directory to process to create JSON manifest.
     *
     * @parameter
     * @required
     */
    private String processDirectory;
    /**
     * Relative Root Directory. Directory path to be remove for JSON path to create relative paths.
     *
     * @parameter
     * @required
     */
    private String relativeRootDirectory;
    /**
     * JSON File Path. Output file path to which to write resulting JSON manifest file.
     *
     * @parameter
     * @required
     */
    private String jsonFilePath;
    /**
     * JRE Version.
     *
     * @parameter
     * @required
     */
    private String jreVersion;
    /**
     * Skip Always. patterns
     *
     * @parameter
     */
    private String[] skipAlways;
    /**
     * Skip On Update. patterns
     *
     * @parameter
     */
    private String[] skipOnUpdate;
    /**
     * Update Priority. optional | required
     *
     * @parameter
     * @required
     */
    private String updatePriority;
    /**
     * JSON Install File Path. Output file path to which to write resulting InstallVersion JSON
     * manifest file
     *
     * @parameter
     * @required
     */
    private String jsonInstallFilePath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        StringBuilder sb = new StringBuilder();
        sb.append("\n::: JwsDirToJsonMojo");
        sb.append("\n  processDirectory  = ");
        sb.append(processDirectory);
        sb.append("\n  jsonFilePath = ");
        sb.append(jsonFilePath);
        getLog().info(sb.toString());

        try {
            JwsFileTreeNode node = new JwsFileTreeNode();
            node.nodePath = ""; // root node
            node.nodeType = "root"; // root node
            JwsFileTreeNode tree = JwsFileTreeUtils.createJwsTreeFromDirectory(
                    processDirectory,
                    relativeRootDirectory,
                    node
            );
            JsonArray json = JwsFileTreeUtils.convertTreeNodeToJson(tree);
            JwsFileTreeUtils.writeJsonFile(json, jsonFilePath);
            // Create the install.json file
            // generate Date
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ssZ");
            String manifestDate = dateFormatter.format(new Date());
            // generate SHA-1
            String manifestSha1 = JwsFileTreeUtils.createSha1(new File(jsonFilePath));
            //
            ArrayList<String> skipAlwaysList = new ArrayList<>();
            if (skipAlways != null) {
                skipAlwaysList.addAll(Arrays.asList(skipAlways));
            }
            //
            ArrayList<String> skipOnUpdateList = new ArrayList<>();
            if (skipOnUpdate != null) {
                skipOnUpdateList.addAll(Arrays.asList(skipOnUpdate));
            }
            //
            JwsInstallVersion iVersion = new JwsInstallVersion(jreVersion,
                    manifestDate,
                    manifestSha1,
                    skipAlwaysList,
                    skipOnUpdateList,
                    updatePriority);
            JsonObject vJson = JwsInstallVersionUtils.convertInstallVersionToJson(iVersion);
            JwsInstallVersionUtils.writeJsonFile(vJson, jsonInstallFilePath);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(JwsDirToManifestMojo.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
