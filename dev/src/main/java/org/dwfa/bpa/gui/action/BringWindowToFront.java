/*
 * Created on Mar 15, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

/**
 * @author kec
 *
 */
public class BringWindowToFront implements ActionListener {
	private JFrame frame;
	/**
	 * 
	 */
	public BringWindowToFront(JFrame frame) {
		this.frame = frame;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		this.frame.requestFocus();
        this.frame.toFront();

	}

}
