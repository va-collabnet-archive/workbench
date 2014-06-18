package org.dwfa.bpa.util;

import static org.dwfa.bpa.util.AppInfoProperties.ARCHETYPE_ARTIFACT_ID;
import static org.dwfa.bpa.util.AppInfoProperties.ARCHETYPE_GROUP_ID;
import static org.dwfa.bpa.util.AppInfoProperties.ARCHETYPE_VERSION;
import static org.dwfa.bpa.util.AppInfoProperties.ARTIFACT_ID;
import static org.dwfa.bpa.util.AppInfoProperties.GROUP_ID;
import static org.dwfa.bpa.util.AppInfoProperties.VERSION;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import static org.dwfa.bpa.util.AppInfoProperties.BASELINE_DATA_ARTIFACT_ID;
import static org.dwfa.bpa.util.AppInfoProperties.BASELINE_DATA_GROUP_ID;
import static org.dwfa.bpa.util.AppInfoProperties.BASELINE_DATA_VERSION;
import static org.dwfa.bpa.util.AppInfoProperties.BUNDLE_TYPE;
import static org.dwfa.bpa.util.AppInfoProperties.PROJECT_DESCRIPTION;
import static org.dwfa.bpa.util.AppInfoProperties.PROJECT_NAME;
import static org.dwfa.bpa.util.AppInfoProperties.SNOMED_CORE_RELEASE_DATE;
import static org.dwfa.bpa.util.AppInfoProperties.TOOLKIT_VERSION;

public class AboutBoxTest {

    private final String groupId = "abc";
    private final String artifactId = "def";
    private final String version = "1.1";
    private final String archetypeGroupId = "ghi";
    private final String archetypeArtifactId = "jkl";
    private final String archetypeVersion = "2.2";
    private final String projectName = "project";
    private final String projectDesc = "project description";
    private final String baselineDataArtifactId = "baselineArtifactId";
    private final String baselineDataGroupId = "baselineGroup";
    private final String baselineDataVersion = "baselineVersion";
    private final String tkVersion = "1.0-SNAPSHOT";
    private final String snomedCTVersion = "00-00-00-00.00.00";
    private final String type = "UAT";

    private Properties appInfoProperties;

    @Before
    public void setUp() {
        // Set test properties.
        appInfoProperties = new Properties();
        appInfoProperties.setProperty(GROUP_ID, groupId);
        appInfoProperties.setProperty(ARTIFACT_ID, artifactId);
        appInfoProperties.setProperty(VERSION, version);
        appInfoProperties.setProperty(ARCHETYPE_GROUP_ID, archetypeGroupId);
        appInfoProperties.setProperty(ARCHETYPE_ARTIFACT_ID, archetypeArtifactId);
        appInfoProperties.setProperty(ARCHETYPE_VERSION, archetypeVersion);
        appInfoProperties.setProperty(PROJECT_NAME, projectName);
        appInfoProperties.setProperty(PROJECT_DESCRIPTION, projectDesc);
        appInfoProperties.setProperty(BASELINE_DATA_ARTIFACT_ID, baselineDataArtifactId);
        appInfoProperties.setProperty(BASELINE_DATA_GROUP_ID, baselineDataGroupId);
        appInfoProperties.setProperty(BASELINE_DATA_VERSION, baselineDataVersion);
        appInfoProperties.setProperty(TOOLKIT_VERSION, tkVersion);
        appInfoProperties.setProperty(SNOMED_CORE_RELEASE_DATE, snomedCTVersion);
        appInfoProperties.setProperty(BUNDLE_TYPE, type);
    }

    @Test
    public void testBuildProjectId() {
        String projectId = AboutBox.buildProjectId(appInfoProperties);
        assertEquals("abc:def:1.1", projectId);
    }

    @Test
    public void testBuildArchetypeId() {
        String archetypeId = AboutBox.buildArchetypeId(appInfoProperties);
        assertEquals("ghi:jkl:2.2", archetypeId);
    }

    @Test
    public void testBuildArchetypeId_groupIdNull() {
        // Remove groupId.
        appInfoProperties.remove(ARCHETYPE_GROUP_ID);

        String archetypeId = AboutBox.buildArchetypeId(appInfoProperties);
        assertNull(archetypeId);
    }

    @Test
    public void testBuildArchetypeId_artifactIdNull() {
        // Remove artifactId.
        appInfoProperties.remove(ARCHETYPE_ARTIFACT_ID);

        String archetypeId = AboutBox.buildArchetypeId(appInfoProperties);
        assertNull(archetypeId);
    }

    @Test
    public void testBuildArchetypeId_versionNull() {
        // Remove version.
        appInfoProperties.remove(ARCHETYPE_VERSION);

        String archetypeId = AboutBox.buildArchetypeId(appInfoProperties);
        assertNull(archetypeId);
    }

    @Test
    public void testBuildArchetypeId_allNull() {
        String archetypeId = AboutBox.buildArchetypeId(new Properties());
        assertNull(archetypeId);
    }

    @Test
    public void testBuildLabelText() {
        String href = "http://zzz.net";
        String name = AboutBox.buildProjectName(appInfoProperties);
        String projectDescription = AboutBox.buildProjectDesc(appInfoProperties);
        String projectId = AboutBox.buildProjectId(appInfoProperties);
        String archetypeId = AboutBox.buildArchetypeId(appInfoProperties);
        String dataId = AboutBox.buildDataId(appInfoProperties);
        String toolkitVersion = AboutBox.buildToolkitVersion(appInfoProperties);
        String snomedCoreReleaseDate = AboutBox.buildSnomedCoreReleaseDate(appInfoProperties);
        String type = AboutBox.buildType(appInfoProperties);
        String linkLabelText = AboutBox.buildLabelText(href, name, projectDescription, projectId, type, archetypeId,
                dataId, toolkitVersion, snomedCoreReleaseDate);

        String expectedValue = "<html><blockquote>"
                + "<br><b>Workbench name: </b>" + this.projectName
                + "<br><b>Workbench description: </b>" + this.projectDesc
                + "<br><b>Workbench type: </b>" + this.type
                + "<br><b>Workbench version: </b><a href=\"\">abc:def:1.1</a>"
                + "<br><b>SNOMED CT version: </b>" + this.snomedCTVersion
                + "<br><br><b>Built from: </b>" + this.archetypeGroupId + ":"
                + this.archetypeArtifactId + ":" + this.archetypeVersion
                + "<br><b>Database version: </b>" + this.baselineDataGroupId
                + ":" + this.baselineDataArtifactId + ":" + this.baselineDataVersion
                + "<br><b>Software version: </b>" + this.tkVersion
                + "</blockquote></html>";

        System.out.println(expectedValue);
        System.out.println(linkLabelText);
        assertEquals(expectedValue, linkLabelText);
    }
}
