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
    private static JDialog aboutBox;
    private static void setupAbout() {
        
        aboutBox = new JDialog(new JFrame(), "About the Workflow Bundle");
        URL aboutBoxUrl = aboutBox.getClass().getResource("/about-box.gif");
        Toolkit tk = aboutBox.getToolkit();
        JLabel aboutLabel;
        if (aboutBoxUrl != null) {
            Image img = tk.getImage(aboutBoxUrl);
            aboutLabel = new JLabel(new ImageIcon(img));
        } else {
            aboutLabel = new JLabel("Cannot find /about-box.gif");
        }
        aboutBox.getContentPane().setLayout(new GridLayout(1, 1));
        aboutBox.getContentPane().add(aboutLabel);
        aboutBox.pack();
    }
    
    public static JDialog getAboutBox() {
        if (aboutBox == null) {
            setupAbout();
        }
        return aboutBox;
    }
}
