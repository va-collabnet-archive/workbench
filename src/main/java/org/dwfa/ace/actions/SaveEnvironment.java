package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.AceLog;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.fd.FileDialogUtil;

public class SaveEnvironment implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		try {
			File outFile = FileDialogUtil.getNewFile("Save environment to new profile...", new File("profile/profile.ace"));
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outFile));
			oos.writeObject(AceConfig.config);
		} catch (Exception e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}

	}

}
