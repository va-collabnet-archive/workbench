package org.ihtsdo.mojo.pbl;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal calls pbl.py to deploy to the maven repository.
 * 
 * Example: mvn -e clean install org.dwfa:pbl-mojo:pbl-pre-deploy deploy
 * org.dwfa:pbl-mojo:pbl-deploy
 * 
 * @goal pbl-deploy
 */

public class PblDeployArtifact extends AbstractMojo {

    /**
     * CuBIT project to upload to.
     * 
     * @parameter
     * @required
     */
    private String cubitProject;

    /**
     * Visibility is {pub|priv}. Default is pub
     * 
     * @parameter
     */
    private String visibility = "pub";

    /**
     * Group id
     * 
     * @parameter expression="${project.groupId}"
     * @required
     */
    String groupId;

    /**
     * artifactId
     * 
     * @parameter expression="${project.artifactId}"
     * @required
     */

    String artifactId;

    /**
     * version
     * 
     * @parameter expression="${project.version}"
     * @required
     */
    String version;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            String[] command = new String[] {"pbl.py", "upload", "-v", "--project", cubitProject, 
            		"--force", "-t", visibility
                    + "-d", "\"artifact deploy\"", "maven2"};
            int exitCondition = Executor.executeCommand(command, getLog());
            if (exitCondition != 0) {
                throw new MojoExecutionException("Exit condition not normal: " + exitCondition);
            }

            // Now delete extra files...

            File deployDir = new File("maven2");
            for (String part : groupId.split("\\.")) {
                deployDir = new File(deployDir, part);
            }
            deployDir = new File(deployDir, artifactId);
            deployDir = new File(deployDir, version);
            SortedSet<String> timeStamps = new TreeSet<String>();
            for (File f : deployDir.listFiles()) {
                if (f.getName().endsWith("sources.jar") || f.getName().endsWith(".pom")) {
                    String[] parts = f.getName().split("-");
                    String timestamp = parts[parts.length - 3];
                    getLog().info("Time stamp: " + timestamp + " for: " + f.getName());
                    if (timestamp.length() == 15) {
                        timeStamps.add(timestamp);
                    }
                } else if (f.getName().endsWith(".jar") || f.getName().endsWith(".pom")) {
                    String[] parts = f.getName().split("-");
                    String timestamp = parts[parts.length - 2];
                    getLog().info("Time stamp: " + timestamp + " for: " + f.getName());
                    if (timestamp.length() == 15) {
                        timeStamps.add(timestamp);
                    }
                }

            }

            File mavenRoot = new File("maven2");
            String absoluteMaven = mavenRoot.getAbsolutePath();
            
            if (timeStamps.size() > 2) {
                timeStamps.remove(timeStamps.first());
                timeStamps.remove(timeStamps.first());
                for (File f : deployDir.listFiles()) {
                    boolean delete = false;
                    for (String stamp: timeStamps) {
                        if (f.getName().contains(stamp)) {
                            delete = true;
                            break;
                        }
                    }

                    if (delete) {
                        
                        String relativePath = f.getAbsolutePath().substring(absoluteMaven.length());
                                                
                        command = new String[] {"pbl.py", "delete", "-v", "--project", 
                        		cubitProject, "--force", "-t", visibility,
                        "-d", "\"artifact delete\"", "maven2", relativePath};
                        exitCondition = Executor.executeCommand(command, getLog());
                        if (exitCondition != 0) {
                            throw new MojoExecutionException("Exit condition not normal: " + exitCondition);
                        }
                        
                        f.delete();
                    }
                }
            }

        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

}
