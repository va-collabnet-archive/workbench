package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import org.dwfa.ace.ACE;
import org.dwfa.ace.AceLog;

import com.sleepycat.je.DatabaseException;

public class Commit implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		try {
			ACE.commit();
		} catch (DatabaseException e1) {
			AceLog.alertAndLog(Level.SEVERE, e1.getLocalizedMessage(), e1);
		}

	}

}
