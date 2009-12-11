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
package org.dwfa.mojo;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.cs.BinaryChangeSetWriter;
import org.dwfa.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;

/**
 * 
 * @goal vodb-add-change-set-writer
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbAddBinaryChangeSetWriter extends AbstractMojo {

    /**
     * Name for the binary change set.
     * 
     * @parameter
     */
    private String changeSetFileName =
            "maven." + UUID.randomUUID().toString() + ".jcs";

    /**
     * Indicates whether a timestamp should be added to the name of the changeset file
     * 
     * @parameter
     */
    private boolean addTimestampToFileName = false;

    /**
     * Name for the temporary binary change set.
     * 
     * @parameter
     */
    private String changeSetTempFileName = changeSetFileName + ".temp";

    /**
     * Set to true if you want to split the changeset files into multiple
     * files instead of one large file
     * 
     * @parameter
     */
    private boolean splitFiles = false;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (addTimestampToFileName) {
            changeSetFileName =
                    changeSetFileName.replaceAll(".jcs", "."
                        + System.currentTimeMillis() + ".jcs");
        }

        try {
            try {
                if (MojoUtil.alreadyRun(getLog(), changeSetFileName, this
                    .getClass(), targetDirectory)) {

                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
            I_ConfigAceFrame activeConfig =
                    LocalVersionedTerminology.get().getActiveAceFrameConfig();
            if (!splitFiles) {
                activeConfig.getChangeSetWriters().add(
                    new BinaryChangeSetWriter(new File(changeSetFileName),
                        new File(changeSetTempFileName)));

            } else {
                activeConfig.getChangeSetWriters().add(
                    new WriteChangeSetToMultipleBinaryFiles(new File(
                        changeSetFileName), new File(changeSetTempFileName)));

            }
            getLog().info(
                "Change set writers: " + activeConfig.getChangeSetWriters());
        } catch (TerminologyException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
