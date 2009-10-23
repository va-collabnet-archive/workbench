package org.dwfa.ace.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.jini.config.Configuration;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.cs.ImportChangeSetReader;

public class ImportJavaChangeset implements ActionListener {

	private Configuration riverConfig;
	private Frame parentFrame;
	private AceConfig config;
	
	public ImportJavaChangeset(Configuration riverConfig, Frame parentFrame, AceConfig config) {
		super();
		this.riverConfig = riverConfig;
		this.parentFrame = parentFrame;
		this.config = config;
	}

	public void actionPerformed(ActionEvent arg0) {
		new ImportChangeSetReader(riverConfig, parentFrame, config);
	}

}
