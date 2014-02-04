/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.mojo.maven;

import static org.dwfa.bpa.util.AppInfoProperties.ARCHETYPE_ARTIFACT_ID;
import static org.dwfa.bpa.util.AppInfoProperties.ARCHETYPE_GROUP_ID;
import static org.dwfa.bpa.util.AppInfoProperties.ARCHETYPE_VERSION;
import static org.dwfa.bpa.util.AppInfoProperties.ARTIFACT_ID;
import static org.dwfa.bpa.util.AppInfoProperties.GROUP_ID;
import static org.dwfa.bpa.util.AppInfoProperties.SITE_URL;
import static org.dwfa.bpa.util.AppInfoProperties.VERSION;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import static org.dwfa.bpa.util.AppInfoProperties.BASELINE_DATA_ARTIFACT_ID;
import static org.dwfa.bpa.util.AppInfoProperties.BASELINE_DATA_GROUP_ID;
import static org.dwfa.bpa.util.AppInfoProperties.BASELINE_DATA_VERSION;
import static org.dwfa.bpa.util.AppInfoProperties.PROJECT_DESCRIPTION;
import static org.dwfa.bpa.util.AppInfoProperties.PROJECT_NAME;
import static org.dwfa.bpa.util.AppInfoProperties.SNOMED_CORE_RELEASE_DATE;
import static org.dwfa.bpa.util.AppInfoProperties.TOOLKIT_VERSION;

/**
 * Goal which writes project properties to a properties file.
 *
 * @see {@link org.dwfa.bpa.util.AppInfoProperties} For an API to read the file
 * into memory and provide access to the properties.
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
     * Name of the PA.
     *
     * @parameter expression="${project.name}
     * @required
     */
    private String projectName;

    /**
     * Description of the PA.
     *
     * @parameter expression="${project.description}"
     * @required
     */
    private String projectDescription;

    
    /**
     * SNOMED Core Release Date.
     * 
     * @parameter expression="${snomedCoreReleaseDate}"
     * @required
     */
    private String releaseDate;
    
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
     * The site URL. May be {@code null}.
     *
     * @parameter expression="${siteURL}"
     */
    private String siteURL;

    /**
     * The groupId of the archetype which generated this project. May be
     * {@code null}.
     *
     * @parameter expression="${archetypeGroupId}"
     */
    private String archetypeGroupId;

    /**
     * The artifactId of the archetype which generated this project. May be
     * {@code null}.
     *
     * @parameter expression="${archetypeArtifactId}"
     */
    private String archetypeArtifactId;

    /**
     * The version of the archetype which generated this project. May be
     * {@code null}.
     *
     * @parameter expression="${archetypeVersion}"
     */
    private String archetypeVersion;

    /**
     * The groupId of the baseline data.
     *
     * @required
     *
     * @parameter expression="${baselineDataGroup}"
     */
    private String baselineDataGroupId;

    /**
     * The artifactId of the baseline data.
     *
     * @required
     *
     * @parameter expression="${baselineDataArtifactId}"
     */
    private String baselineDataArtifactId;

    /**
     * The version of the baseline data.
     *
     * @required
     *
     * @parameter expression="${baselineDataVersion}"
     */
    private String baselineDataVersion;

    /**
     * The version of the toolkit.
     *
     * @required
     *
     * @parameter expression="${org.ihtsdo.wb-toolkit.version}"
     */
    private String toolkitVersion;

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
        appInfoProperties.setProperty(PROJECT_NAME, projectName);
        appInfoProperties.setProperty(PROJECT_DESCRIPTION, projectDescription);
        appInfoProperties.setProperty(GROUP_ID, groupId);
        appInfoProperties.setProperty(ARTIFACT_ID, artifactId);
        appInfoProperties.setProperty(VERSION, version);
        appInfoProperties.setProperty(BASELINE_DATA_GROUP_ID, baselineDataGroupId);
        appInfoProperties.setProperty(BASELINE_DATA_ARTIFACT_ID, baselineDataArtifactId);
        appInfoProperties.setProperty(BASELINE_DATA_VERSION, baselineDataVersion);
        appInfoProperties.setProperty(TOOLKIT_VERSION, toolkitVersion);
        appInfoProperties.setProperty(SNOMED_CORE_RELEASE_DATE, releaseDate);

        // Set workbench site properties, if specified.
        if (siteURL != null) {
            appInfoProperties.setProperty(SITE_URL, siteURL);
        }

        // Archetype properties, if specified.
        if (archetypeGroupId != null) {
            appInfoProperties.setProperty(ARCHETYPE_GROUP_ID, archetypeGroupId);
        }
        if (archetypeArtifactId != null) {
            appInfoProperties.setProperty(ARCHETYPE_ARTIFACT_ID, archetypeArtifactId);
        }
        if (archetypeVersion != null) {
            appInfoProperties.setProperty(ARCHETYPE_VERSION, archetypeVersion);
        }

        // Write out to file.
        File profileRoot = new File(wbBundleDir, "profiles");
        File appInfoPropertiesFile = new File(profileRoot, "appinfo.properties");
        String comment = "App Info";
        appInfoProperties.storeToXML(new FileOutputStream(appInfoPropertiesFile), comment);

        return appInfoProperties;
    }
}
