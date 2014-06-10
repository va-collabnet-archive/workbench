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

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonArray;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 *
 * @goal jws-dir-to-manifest
 */
public class JwsDirToManifestMojo extends AbstractMojo implements Serializable {

    /**
     * concepts file names.
     *
     * @parameter
     * @required
     */
    private String processDirectory;

    /**
     * concepts file names.
     *
     * @parameter
     * @required
     */
    private String relativeRootDirectory;

    /**
     * concepts file names.
     *
     * @parameter
     * @required
     */
    private String jsonFilePath;

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
            JwsFileTreeNode tree = JwsFileTreeUtils.createJwsTreeFromDirectory(
                            processDirectory,
                            relativeRootDirectory,
                            node
           );
            JsonArray json = JwsFileTreeUtils.convertTreeNodeToJson(tree);
            JwsFileTreeUtils.writeJsonFile(json, jsonFilePath);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JwsDirToManifestMojo.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
