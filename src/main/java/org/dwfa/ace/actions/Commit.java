package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dwfa.ace.ACE;

import com.sleepycat.je.DatabaseException;

public class Commit implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		try {
			ACE.commit();
		} catch (DatabaseException e1) {
			e1.printStackTrace();
		}

	}

}
