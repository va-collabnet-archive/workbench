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
/*
 * Created on Apr 29, 2005
 */
package org.dwfa.bpa.util;

import static org.dwfa.bpa.util.AppInfoProperties.ABOUT_BOX_RELEASE_EDITION;
import static org.dwfa.bpa.util.AppInfoProperties.ABOUT_BOX_RELEASE_VERSION;
import static org.dwfa.bpa.util.AppInfoProperties.ARCHETYPE_ARTIFACT_ID;
import static org.dwfa.bpa.util.AppInfoProperties.ARCHETYPE_GROUP_ID;
import static org.dwfa.bpa.util.AppInfoProperties.ARCHETYPE_VERSION;
import static org.dwfa.bpa.util.AppInfoProperties.ARTIFACT_ID;
import static org.dwfa.bpa.util.AppInfoProperties.GROUP_ID;
import static org.dwfa.bpa.util.AppInfoProperties.SITE_URL;
import static org.dwfa.bpa.util.AppInfoProperties.VERSION;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * @author kec
 * @author ocarlsen
 */
public class AboutBox {
    
    private static final Logger LOGGER = Logger.getLogger(AboutBox.class.getName());

    private static final String GRAPHIC_PROPERTY = "org.dwfa.AboutBoxGraphic";
    private static final String TITLE_PROPERTY = "org.dwfa.AboutBoxTitle";

    private static JDialog setupAbout(JFrame parent) {

        String title = getTitle();
        final JDialog aboutBox = new JDialog(parent, title);
        
        aboutBox.getContentPane().setBackground(Color.WHITE);

        String graphic = getGraphic();
        URL aboutBoxUrl = aboutBox.getClass().getResource(graphic);
        Toolkit tk = aboutBox.getToolkit();
        JLabel aboutLabel;
        if (aboutBoxUrl == null) {
            File aboutBoxFile = new File(graphic);
            if (aboutBoxFile.exists()) {
                try {
                    aboutBoxUrl = aboutBoxFile.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (aboutBoxUrl != null) {
            Image img = tk.getImage(aboutBoxUrl);
            aboutLabel = new JLabel(new ImageIcon(img));
        } else {
            aboutLabel = new JLabel("Cannot find " + graphic);
        }
        aboutLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // On some platforms, changing the resizable state affects
        // the insets of the Dialog.  As a result, multiple calls
        // to pack() can change the size of the dialog.  Fix that here
        // by preemptively calling setResizeable before the first pack().
        aboutBox.setResizable(false);
        
        aboutBox.setLayout(new BoxLayout(aboutBox.getContentPane(), BoxLayout.Y_AXIS));

        // As a convenience, JDialog adds to the contentPane directly.
        aboutBox.add(aboutLabel);
        
        final JLabel releaseEdition = createReleaseEditionLabel();
        aboutBox.add(releaseEdition);
        final JLabel releaseVersion = createReleaseVersionLabel();
        aboutBox.add(releaseVersion);
        
        final JLabel legal = createLegalLabel();
        aboutBox.add(legal);
        
        final JLabel versionLabel = createVersionLabel();
        aboutBox.add(versionLabel);
        aboutBox.pack();
        aboutBox.setModal(true);

        // Listen for mouse clicks on aboutLabel to show versionLabel.
        aboutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ev) {
                // Require Ctrl + "left click".
                if (ev.isControlDown() && SwingUtilities.isLeftMouseButton(ev)) {
                    
                    // Toggle visibility.
                    boolean visible = versionLabel.isVisible();
                    versionLabel.setVisible(! visible);
                    
                    // Update dialog bounds to accommodate new component.
                    aboutBox.pack();
                }
            }
        });

        return aboutBox;
    }

    private static String getGraphic() {
        String graphic = "config/about-box/ihtsdo_aboutbox.jpg";
        
        // Look for property override.
        if (System.getProperty(GRAPHIC_PROPERTY) != null
            && System.getProperty(GRAPHIC_PROPERTY).length() > 3) {
            graphic = removeQuotes(System.getProperty(GRAPHIC_PROPERTY));
        }
        
        return graphic;
    }

    private static String getTitle() {
        String title = "About the IHTSDO Editor";

        // Look for property override.
        if (System.getProperty(TITLE_PROPERTY) != null
            && System.getProperty(TITLE_PROPERTY).length() > 3) {
            title = removeQuotes(System.getProperty(TITLE_PROPERTY));
        }
        
        return title;
    }
    
    private static JLabel createReleaseVersionLabel() {
        Properties appInfoProperties = AppInfoProperties.getProperties();
        JLabel label = new JLabel(appInfoProperties.getProperty(ABOUT_BOX_RELEASE_VERSION));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(new EmptyBorder(5, 0, 5, 0));
        return label;
    }
    
    private static JLabel createReleaseEditionLabel() {
        Properties appInfoProperties = AppInfoProperties.getProperties();
        JLabel label = new JLabel(appInfoProperties.getProperty(ABOUT_BOX_RELEASE_EDITION));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(new EmptyBorder(5, 0, 5, 0));
        return label;
    }
    
    private static JLabel createLegalLabel() {
        JLabel label = new JLabel("<html>Legal Notices:<br>This product includes software developed by the Apache Software Foundation<br>"
            + "(http://www.apache.org)<br><br>"
            + "The programs and Clinical Terminologies included herin are subject to a<br>"
            + "restricted use license and can only be used in conjunction with this application.<br>"
            + "Please see the software license file that accompanies this distribution.</html>");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(new Font(label.getFont().getName(), Font.PLAIN, 11));
        label.setBorder(new EmptyBorder(15, 10, 15, 10));
        return label;
    }

    private static JLabel createVersionLabel() {
        Properties appInfoProperties = AppInfoProperties.getProperties();
        String projectId = buildProjectId(appInfoProperties); 
        String archetypeId = buildArchetypeId(appInfoProperties); 

        // Get site URL properties from AppInfo.
        // These are not required, and may be null.
        String siteURL = AppInfoProperties.getProperty(SITE_URL);

        // Build a link label if site is specified, a plain old label otherwise.
        JLabel label = null;
        if (siteURL != null) {
            label = createLinkLabel(siteURL, projectId, archetypeId);           
        } else {
            label = createPlainLabel(projectId, archetypeId);
        }

        // Center label within dialog. 
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Make invisible until activated by user.
        label.setVisible(false);

        return label;
    }

    static String buildProjectId(Properties appInfoProperties) {
        // Get project properties from AppInfo.
        // These are @required in AppInfoMojo.
        String groupId = appInfoProperties.getProperty(GROUP_ID);
        String artifactId = appInfoProperties.getProperty(ARTIFACT_ID);
        String version = appInfoProperties.getProperty(VERSION);
        
        return groupId + ":" + artifactId + ":" + version;
    }

    static String buildArchetypeId(Properties appInfoProperties) {
        // Get archetype properties from AppInfo.
        // These are not required, and may be null.
        String archetypeGroupId = appInfoProperties.getProperty(ARCHETYPE_GROUP_ID);
        String archetypeArtifactId = appInfoProperties.getProperty(ARCHETYPE_ARTIFACT_ID);
        String archetypeVersion = appInfoProperties.getProperty(ARCHETYPE_VERSION);
        
        // Some older POMs may not have archetype information embedded.
        // If any of these properties are null, abort.
        if ((archetypeGroupId == null) || (archetypeArtifactId == null) || (archetypeVersion == null)) {
            return null;
        }
        
        return archetypeGroupId+ ":" + archetypeArtifactId + ":" + archetypeVersion;
    }

    private static JLabel createLinkLabel(final String siteURL, String projectId, String archetypeId) {
        final String href = buildHref(siteURL);
        String labelText = buildLabelText(href, projectId, archetypeId);
        JLabel label = new JLabel(labelText);

        // Simulate hyperlink.
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setToolTipText(href);
        
        // Open browser on mouse click, if supported.
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(href));
                    } catch (Exception ex) {
                        String msg = "Could not open link: " + href;
                        LOGGER.log(Level.WARNING, msg, ex);  // Will print stack trace.
                    }
                } else {
                    LOGGER.info("Desktop API not supported, cannot open browser.");
                }
            }
        });
        
        return label;
    }

    private static JLabel createPlainLabel(String projectId, String archetypeId) {
        String labelText = buildLabelText(null, projectId, archetypeId);
        return new JLabel(labelText);
    }

    private static String buildHref(String siteURL) {
        // Prevent double-slash URLs, which break generated site.
        if (siteURL.endsWith("/")) {
            return siteURL + "index.html";
        } else {
            return siteURL + "/index.html";
        }
    }

    static String buildLabelText(String href, String projectId, String archetypeId) {
        StringBuilder labelTextBuilder = new StringBuilder("<html><blockquote>");
        
        // Link project version if href is not null.
        if (href != null) {
            labelTextBuilder.append("Project version <a href=\"\">" + projectId + "</a>");
        } else {
            labelTextBuilder.append("Project version " + projectId);
        }
        
        // Append archetype text if not null.
        if (archetypeId != null) {
            labelTextBuilder.append("<br>Built from " + archetypeId);
        }
        
        labelTextBuilder.append("</blockquote></html>");
       
        return labelTextBuilder.toString();
    }

    public static String removeQuotes(String str) {
        if (str.startsWith("\"")) {
            str = str.substring(1);
        }
        if (str.endsWith("\"")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static JDialog getAboutBox(JFrame parent) {
        return setupAbout(parent);
    }
    
    /**
     * Helpful for testing outside an editor bundle.
     */
    public static void main(String[] args) throws Exception {
        // Configure image location via System property. 
        // (See getGraphic() method above.) 

        // Configure path to "profiles" directory from command-line.
        String pathToProfiles = args[0];

        // Load AppInfo properties because downstream the AboutBox will need them.
        File profileDir = new File(pathToProfiles);
        AppInfoProperties.loadFromXML(profileDir, "appinfo.properties");

        JFrame frame = new JFrame(getTitle());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400, 400));
        frame.pack();
        frame.setVisible(true);

        // This logic copied from ComponentFrameBean#about().
        JDialog aboutBox = AboutBox.getAboutBox(frame);
        aboutBox.pack();
        aboutBox.setLocation((int) frame.getLocation().getX() + 22, (int) frame.getLocation().getY() + 22);
        aboutBox.setResizable(false);
        aboutBox.setVisible(true);
        aboutBox.toFront();
    }
}
