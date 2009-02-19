package org.dwfa.mojo;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;

/**
 * Import <a href='../dataimport.html'>ACE format</a> data files from a directory and load into an ACE Berkeley database. 
 * <p><font color=red>IMPORTANT USAGE NOTE</font>
 * Please note that this goal cannot be used for incremental imports. It uses a memory buffer for managing identifiers, 
 * and this memory buffer is read from the disk files, and maintained in memory until all files have been imported, 
 * then it is flushed to the database. This buffering provides significant performance improvements over relying on the
 * database for the identifier lookup. 
 * <p>
 * If this goal is applied to an existing database, identifiers might get overwritten, 
 * <font color=red>resulting in data corruption</font>. 
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

   private String[] allowedGoals = new String[] { "install","deploy" };

   /**
    * The maven session
    * 
    * @parameter expression="${session}"
    * @required
    */
   private MavenSession session;
   
   /**
    * The encoding of the input files. The default is "UTF-8";
    * @parameter
    */
   private String fileEncoding = "UTF8";

   /**
    * Location of the build directory.
    *
    * @parameter expression="${project.build.directory}"
    * @required
    */
   private File targetDirectory;

   public void execute() throws MojoExecutionException {
      if (MojoUtil.allowedGoal(getLog(), session.getGoals(), allowedGoals)) {
         try {
            if (MojoUtil.alreadyRun(getLog(), dataDirectory.getCanonicalPath(), targetDirectory)) {
               return;
            }
            LocalVersionedTerminology.get().loadFromDirectory(dataDirectory, fileEncoding);
         } catch (Exception ex) {
            throw new MojoExecutionException("Error processing dependency. Reason: " + ex.getMessage(), ex);
         }
      }
   }

}
