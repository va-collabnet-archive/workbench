package org.dwfa.mojo;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.cs.ComponentValidator;
import org.dwfa.ace.task.cs.ImportAllChangeSets;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.maven.MojoUtil;

/**
 * Read all binary change set under a specified directory hierarchy, and apply the results of 
 * that change set to the open database.
 * @goal bcs-read-all
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */


public class BinaryChangeSetReadAll extends AbstractMojo {
    /**
     * The change set directory
     *
     * @parameter default-value="${project.build.directory}/generated-resources/changesets/"
     */
    String changeSetDir;
    
    /**
     * List of validators to use when validating change sets if validate = true
     * 
     * @parameter
     */
    private String[] validators = new String[]{ComponentValidator.class.getName()};

    /**
     * Whether to validate the change set first or not. Default value is true; 
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
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + changeSetDir,
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
				//if not the last element
				validatorString += ",";
			}
		}
        
        importAllChangeSetsTask.setValidators(validatorString);
        importAllChangeSetsTask.setRootDirStr(changeSetDir);
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
}