package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.jini.config.Configuration;

import org.dwfa.vodb.jar.ImportBaselineJarReader;


public class ImportBaselineJar implements ActionListener {

	Configuration riverConfig;
	
	
	public ImportBaselineJar(Configuration riverConfig) {
		super();
		this.riverConfig = riverConfig;
	}


	public void actionPerformed(ActionEvent e) {
		new ImportBaselineJarReader(riverConfig);
	}

}
