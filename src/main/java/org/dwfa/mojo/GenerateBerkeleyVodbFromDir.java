package org.dwfa.mojo;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;

/**
 * Import <a href='../dataimport.html'>ACE format</a> data files from a directory and load into an ACE Berkeley database. 
 * 
 * @goal berkley-vodb-dir
 * 
 * @phase generate-resources
 * @requiresDependencyResolution compile
 */

public class GenerateBerkeleyVodbFromDir extends AbstractMojo {

   /**
    * Location of the data directory.
    * 
    * @parameter expression="${project.build.directory}/generated-resources/ace/"
    * @required
    */
   File dataDirectory;

   private String[] allowedGoals = new String[] { "install" };

   /**
    * The maven session
    * 
    * @parameter expression="${session}"
    * @required
    */
   private MavenSession session;

   public void execute() throws MojoExecutionException {
      if (MojoUtil.allowedGoal(getLog(), session.getGoals(), allowedGoals)) {
         try {
            if (MojoUtil.alreadyRun(getLog(), dataDirectory.getCanonicalPath())) {
               return;
            }
            LocalVersionedTerminology.get().loadFromDirectory(dataDirectory);
         } catch (Exception ex) {
            throw new MojoExecutionException("Error processing dependency. Reason: " + ex.getMessage(), ex);
         }
      }
   }

}
