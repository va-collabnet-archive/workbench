package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.jini.config.Configuration;

import org.dwfa.vodb.jar.ImportUpdateJarReader;


public class ImportChangesetJar implements ActionListener {

	private Configuration riverConfig;

	public ImportChangesetJar(Configuration riverConfig) {
		this.riverConfig = riverConfig;
	}

	public void actionPerformed(ActionEvent e) {
		new ImportUpdateJarReader(riverConfig);
	}

}
