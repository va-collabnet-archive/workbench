package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.jini.config.Configuration;

import org.dwfa.ace.cs.ImportChangeSetReader;

public class ImportJavaChangeset implements ActionListener {

	Configuration riverConfig;
	
	public ImportJavaChangeset(Configuration riverConfig) {
		super();
		this.riverConfig = riverConfig;
	}

	public void actionPerformed(ActionEvent arg0) {
		new ImportChangeSetReader(riverConfig);
	}

}
