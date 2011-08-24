package org.ihtsdo.arena.conceptview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.dwfa.ace.api.I_GetConceptData;

import org.dwfa.ace.log.AceLog;

public class CommitActionListener implements ActionListener {

    ConceptViewSettings settings;

    public CommitActionListener(ConceptViewSettings settings) {
        this.settings = settings;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (settings != null) {
                I_GetConceptData c = settings.getConcept();
                AceLog.getAppLog().info("Committing on concept: "
                    + c.toUserString()
                    + " UUID: " + c.getPrimUuid().toString());
                if (c != null) {
                    if (c.commit(
                            settings.getConfig().getDbConfig().getUserChangesChangeSetPolicy().convert(),
                            settings.getConfig().getDbConfig().getChangeSetWriterThreading().convert())) {
                        settings.getView().getCvRenderer().updateCancelAndCommit();
                    }
                }
            }
        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
}
