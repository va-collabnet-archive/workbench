package org.dwfa.ace.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.jini.config.Configuration;

import org.dwfa.ace.cs.ImportChangeSetReader;

public class ImportJavaChangeset implements ActionListener {

	Configuration riverConfig;
	private Frame parentFrame;
	
	public ImportJavaChangeset(Configuration riverConfig, Frame parentFrame) {
		super();
		this.riverConfig = riverConfig;
		this.parentFrame = parentFrame;
	}

	public void actionPerformed(ActionEvent arg0) {
		new ImportChangeSetReader(riverConfig, parentFrame);
	}

}
