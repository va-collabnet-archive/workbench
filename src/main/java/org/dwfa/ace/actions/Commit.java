package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dwfa.ace.ACE;

public class Commit implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		ACE.commit();
	}

}
