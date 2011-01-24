package org.ihtsdo.translation.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JTabbedPane;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.panel.TranslationHelperPanel;

public class setInboxPanelFocus implements ActionListener{

	@Override
	public void actionPerformed(ActionEvent e) {
		AceFrameConfig config;
		try {
			config = (AceFrameConfig)Terms.get().getActiveAceFrameConfig();
			AceFrame acef=config.getAceFrame();
			
			JTabbedPane tp=acef.getCdePanel().getLeftTabs();
			
			if (tp!=null){
				int tabCount=tp.getTabCount();
				for (int i=0;i<tabCount;i++){
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_LEFT_MENU)){
						tp.setSelectedIndex(i);
						tp.revalidate();
						tp.repaint();
						return;
					}
				}
			}
		} catch (TerminologyException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
	}
}
