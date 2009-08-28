package org.dwfa.ace.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.fd.FileDialogUtil;

public class SaveProfile implements ActionListener {

	
	Frame parentFrame;
	
	
	
	public SaveProfile(Frame parentFrame) {
		super();
		this.parentFrame = parentFrame;
	}



	public void actionPerformed(ActionEvent e) {
		try {
            if (AceConfig.config.getProfileFile() == null) {
                File outFile = FileDialogUtil.getNewFile("Save to profile...", new File("profiles/profile.ace"),
                		parentFrame);
                AceConfig.config.setProfileFile(outFile);
            } 
            AceConfig.config.save();
		} catch (Exception e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}

	}

}
