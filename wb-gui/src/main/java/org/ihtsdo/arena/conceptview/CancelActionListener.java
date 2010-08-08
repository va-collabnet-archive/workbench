package org.ihtsdo.arena.conceptview;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;

public class CancelActionListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		int n = JOptionPane.showConfirmDialog(
				(Component) e.getSource(),
			    "Canceling changes cannot be undone. \n"
			    + "Do you want to proceed?\n",
			    "Cancel changes?",
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.WARNING_MESSAGE);
		if (n == JOptionPane.YES_OPTION) {
			try {
				Terms.get().cancel();
			} catch (IOException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			}
		}

	}

}
