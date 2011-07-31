/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ihtsdo.arena.contradiction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.MarshalledObject;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.I_InitComponentMenus;

/**
 *
 * @author kec
 */
public class ContradictionEditorGenerator implements I_InitComponentMenus {

    @Override
    public void addAppMenus(JMenuBar mainMenuBar) throws Exception {
        // nothing to do
    }

    @Override
    public JMenu getQuitMenu() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addInternalFrames(JMenu menu) {
        // nothing to do
    }

    @Override
    public JMenuItem[] getNewWindowMenu() {
        if (ACE.editMode) {
            JMenuItem newViewer = new JMenuItem("Contradiction Adjudicator");
            newViewer.addActionListener(new NewAdjudicatorFrame());
            return new JMenuItem[]{newViewer,};
        }
        return new JMenuItem[]{};
    }

        public class NewAdjudicatorFrame implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                MarshalledObject<I_ConfigAceFrame> marshalledFrame =
                        new MarshalledObject<I_ConfigAceFrame>(AceConfig.config.getActiveConfig());
                AceFrameConfig newFrameConfig = (AceFrameConfig) marshalledFrame.get();
                newFrameConfig.setAceFrame(((AceFrameConfig) AceConfig.config.getActiveConfig()).getAceFrame());
                newFrameConfig.setDbConfig(AceConfig.config.getActiveConfig().getDbConfig());

                ContradictionEditorFrame newFrame = new ContradictionEditorFrame(newFrameConfig);
                
                newFrame.setVisible(true);
            } catch (Exception e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }

        }
    }

}
