package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dwfa.vodb.jar.ImportUpdateJarReader;


public class ImportChangesetJar implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		new ImportUpdateJarReader();
	}

}
