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
package org.ihtsdo.mojo.maven;

import static org.dwfa.bpa.util.AppInfoProperties.ARTIFACT_ID;
import static org.dwfa.bpa.util.AppInfoProperties.GROUP_ID;
import static org.dwfa.bpa.util.AppInfoProperties.SITE_URL;
import static org.dwfa.bpa.util.AppInfoProperties.VERSION;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Goal which writes project properties to a properties file.
 * 
 * @see {@link org.dwfa.bpa.util.AppInfoProperties} For an API to read the
 * file into memory and provide access to the properties.
 * 
 * @goal app-info
 * @requiresDependencyResolution runtime
 * 
 */
public class AppInfoMojo extends AbstractMojo {

    /**
     * Location of the workbench bundle directory.
     * 
     * @parameter expression="${project.build.directory}/wb-bundle"
     * @required
     */
    private File wbBundleDir;

    /**
     * The project groupId.
     *
     * @parameter expression="${project.groupId}"
     * @required
     */
    private String groupId;

    /**
     * The project artifactId.
     *
     * @parameter expression="${project.artifactId}"
     * @required
     */
    private String artifactId;

    /**
     * The project version.
     *
     * @parameter expression="${project.version}"
     * @required
     */
    private String version;

    /**
     * The site URL.  Allowed to be null.
     *
     * @parameter expression="${siteURL}"
     */
    private String siteURL;

    /**
     * The groupId of the archetype which generated this project.
     *
     * @parameter expression="${archetypeGroupId}"
     * @required
     */
    private String archetypeGroupId;

    /**
     * The artifactId of the archetype which generated this project.
     *
     * @parameter expression="${archetypeArtifactId}"
     * @required
     */
    private String archetypeArtifactId;

    /**
     * The version of the archetype which generated this project.
     *
     * @parameter expression="${archetypeVersion}"
     * @required
     */
    private String archetypeVersion;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Properties appInfoProperties = exportAppInfoProperties();
            getLog().info(" ** appInfoProperties=" + appInfoProperties);
        } catch (IOException ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }
    }

    private Properties exportAppInfoProperties() throws FileNotFoundException, IOException {
       Properties appInfoProperties = new Properties();

       // Set workbench build properties.
       appInfoProperties.setProperty(GROUP_ID, groupId);
       appInfoProperties.setProperty(ARTIFACT_ID, artifactId);
       appInfoProperties.setProperty(VERSION, version);

       // Set workbench site properties, if specified.
       if (siteURL != null) {
           appInfoProperties.setProperty(SITE_URL, siteURL); 
       }

       // Archetype properties.
       // TODO: Factor keys out as constant somewhere.
       appInfoProperties.setProperty("archetypeGroupId", archetypeGroupId);
       appInfoProperties.setProperty("archetypeArtifactId", archetypeArtifactId);
       appInfoProperties.setProperty("archetypeVersion", archetypeVersion);

       // Write out to file.
       File profileRoot = new File(wbBundleDir, "profiles");
       File appInfoPropertiesFile = new File(profileRoot, "appinfo.properties");
       String comment = "App Info";
       appInfoProperties.storeToXML(new FileOutputStream(appInfoPropertiesFile), comment);

       return appInfoProperties;
    }
}
