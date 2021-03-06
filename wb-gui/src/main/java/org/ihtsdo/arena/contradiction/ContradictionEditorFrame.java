/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.contradiction;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.rmi.MarshalledObject;
import java.security.NoSuchAlgorithmException;
import javax.naming.ConfigurationException;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins.HOST_ENUM;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.ComponentFrame;
import org.dwfa.bpa.util.OpenFramesWindowListener;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.Arena;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.ttk.preferences.gui.PanelLinkingPreferences.LINK_TYPE;

/**
 *
 * @author kec
 */
public class ContradictionEditorFrame extends ComponentFrame implements PropertyChangeListener {

    private static final long serialVersionUID = 1L;
    private static final Color ivoryColor = new Color(0xFFFFF0);
    private static final Color titleColor = new Color(255, 213, 162);
    private static final Color paneColor = Color.gray;
    protected JMenu adjudicatorMenu;
    private I_ConfigAceFrame newFrameConfig = null;
    private JSplitPane topSplit = new JSplitPane();
    private JTabbedPane conceptTabsPane = new JTabbedPane();
    private ConceptPanel c1Panel;
    private static TerminologyList batchConceptList = null;
    private ViewCoordinate viewCoord;
    private JButton stopButton = new JButton();
    private Arena arena;
    private JSplitPane resultsPane = new JSplitPane();
    private JProgressBar progressBar;
    
    private class FindContradictionMoreAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        private ContradictionEditorFrame frame;

        public FindContradictionMoreAction(ContradictionEditorFrame contradictionEditorFrame) {
            super("Debug Contradiction Finder");

            this.frame = contradictionEditorFrame;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            // START SEARCH
            if(frame.getActiveFrameConfig().getEditingPathSet().isEmpty()){
                JOptionPane.showMessageDialog(new JFrame(),
                                                "Please set adjudication path before continuing.",
                                                "No adjudication path set.", JOptionPane.ERROR_MESSAGE);
            }else{
                ContradictionFinderDebugSwingWorker worker =
                    new ContradictionFinderDebugSwingWorker(frame, viewCoord);
                 worker.execute();
            }
        }
    }
    
    private class FindContradictionLessAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        private ContradictionEditorFrame frame;

        public FindContradictionLessAction(ContradictionEditorFrame contradictionEditorFrame) {
            super("Run Contradiction Finder");

            this.frame = contradictionEditorFrame;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            // START SEARCH
            if(frame.getActiveFrameConfig().getEditingPathSet().isEmpty()){
                JOptionPane.showMessageDialog(new JFrame(),
                                                "Please set adjudication path before continuing.",
                                                "No adjudication path set.", JOptionPane.ERROR_MESSAGE);
            }else{
                ContradictionFinderSwingWorker worker =
                    new ContradictionFinderSwingWorker(frame, viewCoord);
                worker.execute();
            }
        }
    }

    public ContradictionEditorFrame(AceFrameConfig origConfig) throws Exception {
        super(new String[]{}, null);
        // Set the title for the frame
        setTitle(getNextFrameName());
        this.newFrameConfig = new ContradictionConfig(this, origConfig);
        this.newFrameConfig.addPropertyChangeListener(this);
        this.viewCoord = this.newFrameConfig.getViewCoordinate();

        // Set the position and size of frame
        setBounds(10, 10, 500, 500);

        // Add the panel and editor pane to the frame
        Container cp = getContentPane();

        // Setup Right Side
        cp.add(topSplit);
        createRightComponent();

        // Setup Left Side
        if (batchConceptList == null) {
	        TerminologyListModel batchListModel = new TerminologyListModel();
                int conflictRefsetNid = Ts.get().getNidForUuids(RefsetAuxiliary.Concept.CONFLICT_RECORD.getPrimoridalUid());
                ConceptChronicleBI conflictRefset = Ts.get().getConceptForNid(conflictRefsetNid);
                for(RefexVersionBI member : conflictRefset.getRefsetMembersActive(viewCoord)){
                    if(member.getRefexNid() == conflictRefsetNid){
                        batchListModel.addElement((I_GetConceptData)
                            Ts.get().getConceptForNid(member.getReferencedComponentNid()));
                    }
                }
	        batchConceptList = new TerminologyList(batchListModel, true, true, newFrameConfig);
        }
	     
        createLeftComponent();

        topSplit.setLeftComponent(resultsPane);
        topSplit.setRightComponent(conceptTabsPane);
        topSplit.setDividerLocation(350); 
        resultsPane.setMinimumSize(new Dimension(350, cp.getHeight()));
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                newFrameConfig.getConceptViewer(1).setTermComponent(null);
            }
        });
        addWindowListener(new AceWindowActionListener());
    }

    private void createLeftComponent() {
        /* Setting up resultsPane */
        JSplitPane progressIndicator = createProgressIndicator();
        JSplitPane conceptList = createConceptListPanel();

        resultsPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        resultsPane.setTopComponent(progressIndicator);
        resultsPane.setBottomComponent(conceptList);
        resultsPane.setDividerLocation(72);
        resultsPane.setEnabled(false);
    }

    private JSplitPane createProgressIndicator() {
        // Setup Label
        JLabel statusTitle = new JLabel("Status", JLabel.CENTER);
        JPanel statusLabel = new JPanel();
        statusLabel.add(statusTitle);
        statusLabel.setBackground(titleColor);

        // Create Stop Button
        stopButton = new JButton(new ImageIcon(ACE.class.getResource("/32x32/plain/stop.png")));
        stopButton.setPreferredSize(new Dimension(30, 30));
        stopButton.setEnabled(false);
        stopButton.setToolTipText("stop the current search");
        stopButton.setVisible(true);

        // Create Progress Bar
        progressBar = new JProgressBar();
        progressBar.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.WHITE));
        progressBar.setBackground(ivoryColor);
        progressBar.setPreferredSize(new Dimension(300, 30));
        progressBar.setVisible(true);
        setProgressInfo("Ready to run Contradiction Detector");

        // Add stop & progress bar and set progress panel
        JPanel progressPane = new JPanel();
        progressPane.add(stopButton);
        progressPane.add(progressBar);
        progressPane.setBackground(paneColor);

        // Setup conceptListPanel with label and progress panes
        JSplitPane progressIndicatorPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, statusLabel, progressPane);

        progressIndicatorPane.setBackground(Color.WHITE);
        progressIndicatorPane.setDividerSize(3);
        progressIndicatorPane.setEnabled(false);

        return progressIndicatorPane;
    }

    private JSplitPane createConceptListPanel() {
        // Setup Label
        JLabel resultsTitle = new JLabel("Results", JLabel.CENTER);
        JPanel resultsLabel = new JPanel();
        resultsLabel.add(resultsTitle);
        resultsLabel.setBackground(titleColor);

        // Setup Concept List
        JScrollPane conceptList = new JScrollPane(batchConceptList);
        batchConceptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        batchConceptList.addListSelectionListener(new SelectionListener());

        // Setup conceptListPanel
        JSplitPane conceptListPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resultsLabel, conceptList);

        conceptListPane.setDividerSize(3);
        conceptListPane.setEnabled(false);

        return conceptListPane;
    }

    private void createRightComponent() throws NoSuchAlgorithmException, IOException, ClassNotFoundException, TerminologyException {
        /* Setting up conceptTabsPane */
        c1Panel = new ConceptPanel(HOST_ENUM.CONCEPT_PANEL_R1,
                newFrameConfig, LINK_TYPE.UNLINKED,
                conceptTabsPane, 1, "plugins/contradiction");
        conceptTabsPane.add(c1Panel);

        arena = new Arena(newFrameConfig, new File("arena/adjudicate.mxe"), true);
//        arena.getEditor().setForAjudication(true);
        conceptTabsPane.addTab("arena",
                new ImageIcon(ACE.class.getResource("/16x16/plain/eye.png")), arena);
        conceptTabsPane.setSelectedIndex(1);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.addWindowListener(new OpenFramesWindowListener(this, this.cfb));
        this.setBounds(getDefaultFrameSize());
    }

    public ConceptPanel getC1Panel() {
        return c1Panel;
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#addAppMenus(javax.swing.JMenuBar)
     */
    @Override
    public void addAppMenus(JMenuBar mainMenuBar) throws Exception {
        mainMenuBar.add(adjudicatorMenu = new JMenu("Adjudicator"));
//        adjudicatorMenu.add(new FindContradictionMoreAction(this)); //DEBUG
        adjudicatorMenu.add(new FindContradictionLessAction(this));
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getQuitMenu()
     */
    @Override
    public JMenu getQuitMenu() {
        return this.adjudicatorMenu;
    }

    /**
     * @see org.dwfa.bpa.util.I_InitComponentMenus#addInternalFrames(javax.swing.JMenu)
     */
    @Override
    public void addInternalFrames(JMenu menu) {
    }

    void selectTab(int hostIndex) {
        conceptTabsPane.setSelectedIndex(0);
    }

    /**
     * @throws ConfigurationException
     * @see org.dwfa.bpa.util.ComponentFrame#getNextFrameName()
     */
    @Override
    public String getNextFrameName() {
        String title = "Contradiction Adjudicator";
        if (count > 0) {
            return title + " " + count++;
        }
        count++;
        return title;
    }
    private static int count = 0;

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getCount()
     */
    @Override
    public int getCount() {
        return count;
    }

    /* SelectionListener */
    private class SelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent lse) {
            int selectedIndex = batchConceptList.getSelectedIndex();
            if (selectedIndex >= 0) {
                TerminologyListModel tm = (TerminologyListModel) batchConceptList.getModel();
                I_GetConceptData concept = tm.getElementAt(selectedIndex);
                c1Panel.setTermComponent(concept);
            }
        }
    }

    /* NewAdjudicatorFrame */
    public class NewAdjudicatorFrame implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                I_ConfigAceFrame activeConfig = AceConfig.config.getActiveConfig();
                if (activeConfig != null) {
                    MarshalledObject<I_ConfigAceFrame> marshalledFrame =
                            new MarshalledObject<I_ConfigAceFrame>(AceConfig.config.getActiveConfig());
                    AceFrameConfig newFrameConfig = (AceFrameConfig) marshalledFrame.get();
                    newFrameConfig.setAceFrame(((AceFrameConfig) activeConfig).getAceFrame());
                    newFrameConfig.setDbConfig(activeConfig.getDbConfig());
                    newFrameConfig.setWorker(activeConfig.getWorker());

                    ContradictionEditorFrame newFrame = new ContradictionEditorFrame(newFrameConfig);
                    newFrame.setVisible(true);
                }
            } catch (Exception e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }

        }
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

    public TerminologyList getBatchConceptList() {
        return batchConceptList;
    }

    /* Action Listener Methods */
    public void setProgressInfo(String string) {
        progressBar.setStringPainted(true);
        progressBar.setString(string);
    }

    public void setProgressIndeterminate(boolean b) {
        progressBar.setIndeterminate(b);
    }

    public void setProgressValue(int i) {
        progressBar.setValue(i);
    }

    public void addStopActionListener(ActionListener stopListener) {
        stopButton.addActionListener(stopListener);
    }

    public void setProgressMaximum(int totalConceptCount) {
        progressBar.setMaximum(totalConceptCount);
    }

    public long getProgressMaximum() {
        return progressBar.getMaximum();
    }

    public I_ConfigAceFrame getActiveFrameConfig() {
        return newFrameConfig;
    }

    public void removeStopActionListener(ActionListener stopListener) {
        stopButton.removeActionListener(stopListener);
    }

    public void enableStopButton(boolean show) {
        stopButton.setEnabled(show);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("commit")) {
        } else if (evt.getPropertyName().equals("commitEnabled")) {
        } else if (evt.getPropertyName().equals("lastViewed")) {
        } else if (evt.getPropertyName().equals("uncommitted")) {
            // Nothing to do...
        }
    }

    private void doWindowActivation() {
        try {
            Terms.get().setActiveAceFrameConfig(newFrameConfig);
        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    private class AceWindowActionListener implements WindowListener {

        public void windowActivated(WindowEvent e) {
            doWindowActivation();
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
            doWindowActivation();
        }
    }
}
