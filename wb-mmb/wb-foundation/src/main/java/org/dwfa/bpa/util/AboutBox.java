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

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * @author kec
 * 
 */
public class AboutBox {
    private static JDialog setupAbout(JFrame parent) {

        String title = "About the Workflow Bundle";

        if (System.getProperty("org.dwfa.AboutBoxTitle") != null
            && System.getProperty("org.dwfa.AboutBoxTitle").length() > 3) {
            title = removeQuotes(System.getProperty("org.dwfa.AboutBoxTitle"));
        }
        JDialog aboutBox = new JDialog(parent, title);

        String graphic = "/about-box.gif";
        if (System.getProperty("org.dwfa.AboutBoxGraphic") != null
            && System.getProperty("org.dwfa.AboutBoxGraphic").length() > 3) {
            graphic = removeQuotes(System.getProperty("org.dwfa.AboutBoxGraphic"));
        }
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
        aboutBox.getContentPane().setLayout(new GridLayout(1, 1));
        aboutBox.getContentPane().add(aboutLabel);
        aboutBox.pack();
        aboutBox.setModal(true);
        return aboutBox;
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
}
