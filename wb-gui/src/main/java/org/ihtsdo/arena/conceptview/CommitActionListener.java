package org.ihtsdo.arena.conceptview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dwfa.ace.log.AceLog;

public class CommitActionListener implements ActionListener {

   ConceptViewSettings settings;

   public CommitActionListener(ConceptViewSettings settings) {
      this.settings = settings;
   }

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			settings.getConcept().commit(
                 settings.getConfig().getDbConfig().getUserChangesChangeSetPolicy().convert(),
                 settings.getConfig().getDbConfig().getChangeSetWriterThreading().convert());
		} catch (Exception e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
	}

}
