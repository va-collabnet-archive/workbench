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
package org.ihtsdo.bdb.mojo;

import java.io.File;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.commitlog.CommitLog;
import org.dwfa.ace.config.AceConfig;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.cs.econcept.EConceptChangeSetWriter;

/**
 * Set up changeset read/writing in the database.
 * 
 * @extendsPlugin wb-mojo
 * @goal bcs-open
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class BinaryChangeSetWriterOpen extends AbstractMojo {
    /**
     * The change set directory
     * 
     * @parameter
     *            expression="${project.build.directory}/generated-resources/changesets/"
     * 
     * @required
     */
    File changeSetDir;

    /**
     * The change set file name
     * 
     * @parameter
     */
    String changeSetFileName = UUID.randomUUID() + ".eccs";

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            if (changeSetFileName == null) {
                changeSetFileName = UUID.randomUUID() + ".eccs";
            }
            if (!changeSetFileName.endsWith(".eccs")) {
                String firstPart = changeSetFileName.substring(0, changeSetFileName.lastIndexOf('.'));
                changeSetFileName = firstPart.concat(".eccs");
            }

            AceConfig.config.setChangeSetWriterFileName(changeSetFileName);
            AceConfig.config.setChangeSetRoot(changeSetDir);

            ChangeSetWriterHandler.addWriter(new EConceptChangeSetWriter(new File(AceConfig.config.getChangeSetRoot(),
                AceConfig.config.getChangeSetWriterFileName()), new File(AceConfig.config.getChangeSetRoot(), "."
                + AceConfig.config.getChangeSetWriterFileName()), ChangeSetPolicy.MUTABLE_ONLY, true));
            ChangeSetWriterHandler.addWriter(new CommitLog(new File(AceConfig.config.getChangeSetRoot(),
                "commitLog.xls"), new File(AceConfig.config.getChangeSetRoot(), "." + "commitLog.xls")));

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
