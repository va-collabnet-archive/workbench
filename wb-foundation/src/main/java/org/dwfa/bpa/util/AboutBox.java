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

import static org.dwfa.bpa.util.AppInfoProperties.ARTIFACT_ID;
import static org.dwfa.bpa.util.AppInfoProperties.GROUP_ID;
import static org.dwfa.bpa.util.AppInfoProperties.SITE_URL;
import static org.dwfa.bpa.util.AppInfoProperties.VERSION;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
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
        JDialog aboutBox = new JDialog(parent, title);

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

        // As a convenience, JDialog adds to the contentPane directly.
        aboutBox.add(aboutLabel, BorderLayout.CENTER);
        JLabel versionLabel = createVersionLabel();
        aboutBox.add(versionLabel, BorderLayout.SOUTH);
        aboutBox.pack();
        aboutBox.setModal(true);
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

    private static JLabel createVersionLabel() {
        // Get from properties from AppInfo.
        String groupId = AppInfoProperties.getProperty(GROUP_ID);
        String artifactId = AppInfoProperties.getProperty(ARTIFACT_ID);
        String version = AppInfoProperties.getProperty(VERSION);
        String site_url = AppInfoProperties.getProperty(SITE_URL);
        
        String labelText = "Version " + groupId + ":" + artifactId + ":" + version;
        JLabel label = null;

        // Build a link label if site is specified, a plain old label otherwise.
        if (site_url != null) {
            label = createLinkLabel(site_url, labelText);           
        } else {
            label = new JLabel(labelText);
        }

        // Center label within dialog. 
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(new EmptyBorder(0, 0, 5, 0));  // Padding on bottom.
        
        return label;
    }

    private static JLabel createLinkLabel(String siteURL, String labelText) {
        final String href = buildHref(siteURL);
        JLabel label = new JLabel("<html><a href=\"" + href + "\">" + labelText + "</a></html>");
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Open browser on mouse click.
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(href));
                } catch (Exception ex) {
                    String msg = "Could not open link: " + href;
                    LOGGER.log(Level.WARNING, msg, ex);  // Will print stack trace.
                }
            }
        });
        
        return label;
    }

    private static String buildHref(String siteURL) {
        // Prevent double-slash URLs, which break generated site.
        if (siteURL.endsWith("/")) {
            return siteURL + "index.html";
        } else {
            return siteURL + "/index.html";
        }
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
