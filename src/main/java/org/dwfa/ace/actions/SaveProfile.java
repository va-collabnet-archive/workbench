package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.fd.FileDialogUtil;

public class SaveProfile implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		try {
            if (AceConfig.config.getConfigFile() == null) {
                File outFile = FileDialogUtil.getNewFile("Save to profile...", new File("profiles/profile.ace"));
                AceConfig.config.setConfigFile(outFile);
            } 
            AceConfig.config.save();
		} catch (Exception e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}

	}

}
