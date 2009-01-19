package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.fd.FileDialogUtil;

public class SaveProfileAs implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        try {
            File outFile = FileDialogUtil.getNewFile("Save profile as...",
                                                     new File("profiles/default.ace"));
            AceConfig.config.setProfileFile(outFile);
            AceConfig.config.save();
        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }

    }

}
