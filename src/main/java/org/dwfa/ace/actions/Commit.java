package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import org.dwfa.ace.ACE;
import org.dwfa.ace.log.AceLog;

public class Commit implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		try {
			ACE.commit();
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}

	}

}
