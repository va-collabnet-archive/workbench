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
package org.ihtsdo.mojo.mojo;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.cs.ComponentValidator;
import org.dwfa.ace.task.cs.ImportAllChangeSets;
import org.dwfa.bpa.process.TaskFailedException;
import org.ihtsdo.mojo.maven.MojoUtil;
import net.nhs.cfh.ace.config.WorkbenchConfiguration;
import net.nhs.cfh.ace.gui.service.ConceptService;
import net.nhs.cfh.ace.service.extensions.LuceneCommitListener;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.objectCache.ObjectCache;
import org.ihtsdo.custom.statics.CustomStatics;


/**
 * Read all binary change set under a specified directory hierarchy, and apply
 * the results of
 * that change set to the open database.
 * 
 * @goal bcs-read-all
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class BinaryChangeSetReadAll extends AbstractMojo {
    /**
     * The change set directory
     * 
     * @parameter default-value=
     *            "${project.build.directory}/generated-resources/changesets/"
     */
    String changeSetDir;

    /**
     * List of validators to use when validating change sets if validate = true
     * 
     * @parameter
     */
    private String[] validators = new String[] { ComponentValidator.class.getName() };

    /**
     * Whether to validate the change set first or not. Default value is true;
     * 
     * @parameter
     */
    boolean validate = true;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + changeSetDir, this.getClass(),
                targetDirectory)) {
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
        getLog().info("importing change sets in: " + changeSetDir);
        ImportAllChangeSets importAllChangeSetsTask = new ImportAllChangeSets();
        importAllChangeSetsTask.setValidateChangeSets(validate);
        String validatorString = "";
        for (int i = 0; i < validators.length; i++) {
            validatorString += validators[i];
            if (i != validators.length - 1) {
                // if not the last element
                validatorString += ",";
            }
        }

        importAllChangeSetsTask.setValidators(validatorString);
        importAllChangeSetsTask.setRootDirStr(changeSetDir);
        
        Properties custProps = new Properties();
        custProps.put("conceptService", "net.nhs.cfh.ace.gui.service.NHSAceConceptServiceImpl");
        custProps.put("conceptServiceName", "nhs-ace");
        custProps.put("nhs-ace.readv2.c6af074b-1992-3e70-8586-856538d53d56", "net.nhs.cfh.ace.gui.service.handlers.read.v2.ReadV2Handler");
        custProps.put("nhs-ace.readv3.98aee43f-9d13-35af-b2a0-bb2bab1e50b3", "net.nhs.cfh.ace.gui.service.handlers.read.v3.ReadV3Handler");
        custProps.put("nhs-ace.snomed.0418a591-f75b-39ad-be2c-3ab849326da9", "net.nhs.cfh.ace.gui.service.handlers.snomed.SnomedHandler");
        ObjectCache.INSTANCE.put(CustomStatics.CUSTOMPROPS, custProps);

          WorkbenchConfiguration.setResourceName("ci-config.xml");
          ConceptService conceptService = (ConceptService) WorkbenchConfiguration
		.getDefaultInstance().getBeanFactory()
		.getBean("conceptService");
        
        BdbCommitManager.addCommitListener(new LuceneCommitListener(conceptService));
        try {
            importAllChangeSetsTask.importAllChangeSets(new LoggerAdaptor(getLog()));
        } catch (TaskFailedException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    public String getChangeSetDir() {
        return changeSetDir;
    }

    public void setChangeSetDir(String changeSetDir) {
        this.changeSetDir = changeSetDir;
    }

    public String[] getValidators() {
        return validators;
    }

    public void setValidators(String[] validators) {
        this.validators = validators;
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }
}
