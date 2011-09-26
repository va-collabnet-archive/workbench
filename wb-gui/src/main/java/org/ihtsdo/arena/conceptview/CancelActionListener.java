package org.ihtsdo.arena.conceptview;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JOptionPane;
import org.dwfa.ace.api.I_GetConceptData;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.Ts;

public class CancelActionListener implements ActionListener {

    ConceptViewSettings settings;

    public CancelActionListener(ConceptViewSettings settings) {
        this.settings = settings;
    }

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
                I_GetConceptData cToCancel = settings.getConcept();
                AceLog.getAppLog().info("Canceling on concept: "
                    + cToCancel.toUserString()
                    + " UUID: " + cToCancel.getPrimUuid().toString());
                cToCancel.cancel();
                Ts.get().addUncommitted(cToCancel);
                if (cToCancel.isCanceled()) {
                    settings.getHost().setTermComponent(null);
                }
            } catch (IOException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }
        }
    }
}
