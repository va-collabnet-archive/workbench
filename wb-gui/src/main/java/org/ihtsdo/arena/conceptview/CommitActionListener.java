package org.ihtsdo.arena.conceptview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;

public class CommitActionListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			Terms.get().commit();
		} catch (Exception e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
	}

}
