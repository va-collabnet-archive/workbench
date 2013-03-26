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
import javax.swing.JLabel;

public class AboutBoxTest {

    private final String groupId = "abc";
    private final String artifactId = "def";
    private final String version = "1.1";
    private final String archetypeGroupId = "ghi";
    private final String archetypeArtifactId = "jkl";
    private final String archetypeVersion = "2.2";

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
        String projectId = AboutBox.buildProjectId(appInfoProperties);
        String archetypeId = AboutBox.buildArchetypeId(appInfoProperties);
        String linkLabelText = AboutBox.buildLabelText(href, projectId, archetypeId);
        
        String expectedValue = "<html><blockquote>" +
                "Project version <a href=\"\">abc:def:1.1</a>" +
                "<br>Built from ghi:jkl:2.2" +
                "</blockquote></html>";
        assertEquals(expectedValue, linkLabelText);
    }
    
    @Test
    public void testBuildLinkLabelText_nullHref() {
        String href = null;
        String projectId = AboutBox.buildProjectId(appInfoProperties);
        String archetypeId = AboutBox.buildArchetypeId(appInfoProperties);
        String linkLabelText = AboutBox.buildLabelText(href, projectId, archetypeId);
        
        String expectedValue = "<html><blockquote>" +
                "Project version abc:def:1.1" +
                "<br>Built from ghi:jkl:2.2" +
                "</blockquote></html>";
        assertEquals(expectedValue, linkLabelText);
    }
    
    @Test
    public void testBuildLinkLabelText_nullArchetypeId() {
        String href = "http://zzz.net";
        String projectId = AboutBox.buildProjectId(appInfoProperties);
        String archetypeId =  null;
        String linkLabelText = AboutBox.buildLabelText(href, projectId, archetypeId);
        
        String expectedValue = "<html><blockquote>" +
                "Project version <a href=\"\">abc:def:1.1</a>" +
                "</blockquote></html>";
        assertEquals(expectedValue, linkLabelText);
    }
}
