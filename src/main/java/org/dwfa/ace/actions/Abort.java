package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dwfa.ace.ACE;

import com.sleepycat.je.DatabaseException;

public class Abort implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		try {
			ACE.abort();
		} catch (DatabaseException e1) {
			e1.printStackTrace();
		}
	}

}
