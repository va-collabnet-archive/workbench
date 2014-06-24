/*
 * MainFrame.java
 *
 * Copyright 2006 Sun Microsystems, Inc. ALL RIGHTS RESERVED Use of
 * this software is authorized pursuant to the terms of the license
 * found at http://developers.sun.com/berkeley_license.html.
 *
 */

package org.ihtsdo.rf2.file.delta.snapshot.ui;
import java.awt.Dimension;

import org.apache.commons.configuration.CompositeConfiguration;

public class MainFrame extends javax.swing.JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5111078273854834183L;
	
	CompositeConfiguration config;

	/**
     * Creates new form MainFrame
     */
    public MainFrame(CompositeConfiguration config) {
        this.config = config;
        initComponents();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 500));
        setTitle("IHTSDO - RF2 to RF1 Conversion tool");
        this.add(new ConversionPrefsPanel(config));
        pack();
    }
}
