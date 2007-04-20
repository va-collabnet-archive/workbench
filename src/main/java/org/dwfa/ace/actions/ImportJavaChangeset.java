package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dwfa.ace.cs.ImportChangeSetReader;

public class ImportJavaChangeset implements ActionListener {

	public void actionPerformed(ActionEvent arg0) {
		new ImportChangeSetReader();
	}

}
