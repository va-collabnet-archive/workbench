/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.promotion;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.MarshalledObject;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.I_InitComponentMenus;
import org.ihtsdo.tk.api.PathBI;

/**
 *
 * @author kec
 */
public class PromotionEditorGenerator implements I_InitComponentMenus{
    JComboBox sourcePathCombo;
    JComboBox targetPathCombo;
    PathBI sourcePath = null;
    PathBI targetPath = null;

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
            JMenuItem newViewer = new JMenuItem("Terminology Promoter");
            newViewer.addActionListener(new NewAdjudicatorFrame());
            return new JMenuItem[]{newViewer,};
        }
        return new JMenuItem[]{};
    }

    public class NewAdjudicatorFrame implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
//TODO: check for uncommitted changes, don't open until resolved
                
                JPanel pane = new JPanel();
                pane.setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.gridx = 0;
                gbc.gridy = 1;
                
                SortedSet<PathBI> sortedPaths = new TreeSet<>(new Comparator<PathBI>() {
                    @Override
                    public int compare(PathBI o1, PathBI o2) {
                        return o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase());
                    }
                });
                JLabel sourceLabel = new JLabel("source path:");
                pane.add(sourceLabel, gbc);
                
                gbc.gridy++;
                sortedPaths.addAll(Terms.get().getPaths());
                sourcePathCombo = new JComboBox();
                sourcePathCombo.addItem(null);
                for(PathBI path : sortedPaths){
                    sourcePathCombo.addItem(path);
                }
                sourcePathCombo.addActionListener(new SourcePathListener());
                pane.add(sourcePathCombo, gbc);
                
                gbc.gridy++;
                JLabel targetLabel = new JLabel("target path:");
                pane.add(targetLabel, gbc);
                
                gbc.gridy++;
                targetPathCombo = new JComboBox();
                targetPathCombo.addItem(null);
                for(PathBI path : sortedPaths){
                    targetPathCombo.addItem(path);
                }
                targetPathCombo.addActionListener(new TargetPathListener());
                pane.add(targetPathCombo, gbc);
                int value = JOptionPane.showConfirmDialog(null, pane,
                                                 "Please select promotion paths.",
                                                 JOptionPane.OK_CANCEL_OPTION);
                
                if(JOptionPane.OK_OPTION == value && sourcePath != null &&
                        targetPath != null){
                    I_ConfigAceFrame activeConfig = AceConfig.config.getActiveConfig();
                    if (activeConfig != null) {
                        MarshalledObject<I_ConfigAceFrame> marshalledFrame =
                                new MarshalledObject<I_ConfigAceFrame>(AceConfig.config.getActiveConfig());
                        AceFrameConfig newFrameConfig = (AceFrameConfig) marshalledFrame.get();
                        newFrameConfig.setAceFrame(((AceFrameConfig) activeConfig).getAceFrame());
                        newFrameConfig.setDbConfig(activeConfig.getDbConfig());
                        newFrameConfig.setWorker(activeConfig.getWorker());

                        PromotionEditorFrame newFrame = new PromotionEditorFrame(newFrameConfig, sourcePath, targetPath);
                        newFrame.setVisible(true);
                    }
                }else if(sourcePath == null || targetPath == null){
//TODO need message                    
                    AceLog.getAppLog().info("Please set paths before contiuing");
                }
                
            } catch (Exception e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }

        }
    }
    
    private class SourcePathListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            PathBI path = (PathBI) sourcePathCombo.getSelectedItem();
            if (path == null) {
                return;
            }
            sourcePath = path;
        }
        
    }
    
    private class TargetPathListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            PathBI path = (PathBI) targetPathCombo.getSelectedItem();
            if (path == null) {
                return;
            }
            targetPath = path;
        }
        
    }
    
}
