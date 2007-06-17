package org.dwfa.mojo;

import java.io.File;
import java.io.FileFilter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.cs.I_ReadChangeSet;

/**
 * 
 * @goal vodb-import-jcs
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class VodbImportBinaryChangeSetsInDir extends AbstractMojo {

    /**
     * changeSetDirStr
     * 
     * @parameter default-value="${project.build.directory}/generated-resources/changesets/"
     */
    String changeSetDirStr;
 
    /**
     * changeSetSuffix
     * 
     * @parameter default-value=".jcs"
     */
    String changeSetSuffix;

    
    public void execute() throws MojoExecutionException, MojoFailureException {
            I_ImplementTermFactory termFactoryImpl = (I_ImplementTermFactory) LocalVersionedTerminology
                .get();
        try {
            File changeSetDir = new File(changeSetDirStr);
            File[] changeSets = changeSetDir.listFiles(new FileFilter() {

                public boolean accept(File f) {
                    return f.getName().endsWith(changeSetSuffix);
                }
                
            });
            if (changeSets != null) {
                for (File csf: changeSets) {
                    getLog().info("Importing: " + csf.getName());
                    I_ReadChangeSet reader = termFactoryImpl.newBinaryChangeSetReader(csf);
                    reader.read();
                }
            } else {
                getLog().info("No change sets found.");
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }       
    }
}