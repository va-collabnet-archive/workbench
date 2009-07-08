/*
 * Created on Apr 29, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.util;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * @author kec
 *
 */
public class AboutBox  {
    private static JDialog setupAbout(JFrame parent) {
       
        String title = "About the Workflow Bundle";
        
        if (System.getProperty("org.dwfa.AboutBoxTitle") != null && System.getProperty("org.dwfa.AboutBoxTitle").length() > 3) {
           title = removeQuotes(System.getProperty("org.dwfa.AboutBoxTitle"));
        }
        JDialog aboutBox = new JDialog(parent, title);
        
        String graphic = "/about-box.gif";
        if (System.getProperty("org.dwfa.AboutBoxGraphic") != null && System.getProperty("org.dwfa.AboutBoxGraphic").length() > 3) {
           graphic = removeQuotes(System.getProperty("org.dwfa.AboutBoxGraphic"));
        }
        URL aboutBoxUrl = aboutBox.getClass().getResource(graphic);
        Toolkit tk = aboutBox.getToolkit();
        JLabel aboutLabel;
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
