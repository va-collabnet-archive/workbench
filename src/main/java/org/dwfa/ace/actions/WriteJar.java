package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.jar.JarWriter;

public class WriteJar implements ActionListener {
	AceConfig aceConfig;
	
	public WriteJar(AceConfig aceConfig) {
		super();
		this.aceConfig = aceConfig;
	}

	public void actionPerformed(ActionEvent e) {
		new JarWriter(aceConfig);
	}

}
