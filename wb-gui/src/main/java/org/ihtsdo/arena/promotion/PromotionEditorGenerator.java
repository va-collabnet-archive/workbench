/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.promotion;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.MarshalledObject;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.dwfa.ace.task.classify.SnorocketExTask;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.I_InitComponentMenus;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

/**
 *
 * @author kec
 */
public class PromotionEditorGenerator implements I_InitComponentMenus{
    JComboBox sourcePathCombo;
    JComboBox targetPathCombo;
    JComboBox mergePathCombo;
    PathBI sourcePath = null;
    PathBI targetPath = null;
    PathBI mergePath = null;

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
                mergePath = null;
                sourcePath = null;
                targetPath = null;
//TODO: check for uncommitted changes, don't open until resolved
                if(Ts.get().hasUncommittedChanges()){
                    JOptionPane.showMessageDialog(null,
                            "There are uncommitted changes. Please commit or cancel before continuing.",
                            "Check for uncommitted",
                            JOptionPane.OK_OPTION);
                }else{
                    doPathSelection("Please select promotion parameters.");
                }
            } catch (Exception e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }

        }
    }
    
    private void doPathSelection(String message) throws Exception{
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
        for (PathBI path : sortedPaths) {
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
        for (PathBI path : sortedPaths) {
            targetPathCombo.addItem(path);
        }
        targetPathCombo.addActionListener(new TargetPathListener());
        pane.add(targetPathCombo, gbc);
        
        gbc.gridy++;
        JLabel mergeLabel = new JLabel("Select merge path to continue working on existing merge:");
        pane.add(mergeLabel, gbc);

        gbc.gridy++;
        mergePathCombo = new JComboBox();
        mergePathCombo.addItem(null);
        for (PathBI path : sortedPaths) {
            mergePathCombo.addItem(path);
        }
        mergePathCombo.addActionListener(new MergePathListener());
        pane.add(mergePathCombo, gbc);
        
        
        int value = JOptionPane.showConfirmDialog(null, pane,
                message,
                JOptionPane.OK_CANCEL_OPTION);

        if (JOptionPane.OK_OPTION == value && sourcePath != null
                && targetPath != null) {
            I_ConfigAceFrame activeConfig = AceConfig.config.getActiveConfig();
            if (activeConfig != null) {
                MarshalledObject<I_ConfigAceFrame> marshalledFrame =
                        new MarshalledObject<I_ConfigAceFrame>(AceConfig.config.getActiveConfig());
                final AceFrameConfig newFrameConfig = (AceFrameConfig) marshalledFrame.get();
                newFrameConfig.setAceFrame(((AceFrameConfig) activeConfig).getAceFrame());
                newFrameConfig.setDbConfig(activeConfig.getDbConfig());
                newFrameConfig.setWorker(activeConfig.getWorker());

                final PromotionEditorFrame newFrame = new PromotionEditorFrame(newFrameConfig,
                        sourcePath, targetPath, mergePath);
                if (mergePath == null) {
                    //run classifier before opening window
                    new Thread(
                            new Runnable() {
                        @Override
                        public void run() {
                            try {
                                SnorocketExTask classifier = new SnorocketExTask();
                                classifier.runClassifier(newFrameConfig);
                                classifier.commitClassification();
                                
                                PositionBI sourcePos = Ts.get().newPosition(sourcePath,
                                        TimeHelper.getTimeFromString("latest", TimeHelper.getFileDateFormat()));
                                I_ConfigAceFrame sourceConfig = new PromotionSourceConfig(newFrame, newFrameConfig, sourcePos);
                                sourceConfig.getEditingPathSet().clear();
                                sourceConfig.getEditingPathSet().add(sourcePath);
                                
                                SnorocketExTask classifier2 = new SnorocketExTask();
                                classifier2.runClassifier(sourceConfig);
                                classifier2.commitClassification();
                                newFrame.setVisible(true);
                            } catch (ComputationCanceled ex) {
                                Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (TerminologyException ex) {
                                Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (TaskFailedException ex) {
                                Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (Exception ex) {
                                Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }).start();
                } else {
                    newFrame.setVisible(true);
                }
            }
        } else if (JOptionPane.CANCEL_OPTION != value && (sourcePath == null || targetPath == null)) {                   
            doPathSelection("Please set paths before contiuing");
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
    
    private class MergePathListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            PathBI path = (PathBI) mergePathCombo.getSelectedItem();
            if (path == null) {
                return;
            }
            mergePath = path;
        }
        
    }
    
}
