package org.dwfa.ace.classifier;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.classify.SnoAB;
import org.dwfa.ace.task.classify.SnoQuery;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

public class SnoRocketTabPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    private class SnorocketListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // TODO Auto-generated method stub
            if (SnoQuery.isDirty()) {
                // DIFFS JPANEL
                diffPathJPanel.update();

                // EQUIVALENTS JPANEL
                equivJPanel.update();

                revalidate();
                repaint();
                
                SnoQuery.setDirty(false);
            }            
        }
    }

    // ** WORKBENCH PARTICULARS **
    private I_TermFactory tf;
    private I_ConfigAceFrame config;

//    private List<PositionBI> cEditPathPos;
    private List<PositionBI> cClassPositionBIList;

    // ** CONFIGURATION PARTICULARS **
    private static final boolean debug = false; // :DEBUG:
    private boolean viewPathTF = false;

    // ** GUI **
    private DiffPathPanel diffPathJPanel;
    private EquivPanel equivJPanel;
    private DLPanel logicJPanel;
    private ViewPathPanel viewPathJPanel;

    // ** GUI LAYOUT PARTICULARS **
    final static String TAB_DIFF = "Differences";
    final static String TAB_EQUIV = "Equivalents";
    final static String TAB_DL = "Logic";
    final static String TAB_VIEW = "Version";
    final static String TAB_STATS = "Stats";

    public SnoRocketTabPanel(ACE ace) throws IOException {
        super();
        getClassifyPrefs(ace);
        getClassifyPrefs(ace);
        config.addPropertyChangeListener("commit", new SnorocketListener());

        // COMPONENT BORDER
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 3),
                BorderFactory.createLineBorder(Color.GRAY)));

        // SETUP TABBED PANE
        JTabbedPane tabbedPane = new JTabbedPane();
        // tabbedPane.setMinimumSize(new Dimension(0, 0));

        // ADD DIFF VIEW
        diffPathJPanel = new DiffPathPanel(config);
        diffPathJPanel.setBorder(BorderFactory.createTitledBorder(TAB_DIFF + ": "));
        String tip = "All differenced between the current and previous run of the Classifer.";
        tabbedPane.addTab(TAB_DIFF, null, diffPathJPanel, tip);

        // ADD EQUIVALENCE VIEW
        equivJPanel = new EquivPanel(config);
        equivJPanel.setBorder(BorderFactory.createTitledBorder(TAB_EQUIV + ": "));
        tip = "All reported equivalent concepts found in the most recent run of the Classifer.";
        tabbedPane.addTab(TAB_EQUIV, null, equivJPanel, tip);

        // ADD DESCRIPTION LOGIC TAB
        logicJPanel = new DLPanel(config);
        logicJPanel.setBorder(BorderFactory.createTitledBorder(TAB_DL + ": "));
        tip = "Description Logic Declarations which will be submitted to the Classifer"
                + "in addition to input data.";
        // tabbedPane.addTab(TAB_DL, null, logicJPanel, tip);
        JScrollPane logicJScrollPane = new JScrollPane(logicJPanel);
        tabbedPane.addTab(TAB_DL, null, logicJScrollPane, tip);

        // ADD VIEW PATH VERSIONS TAB
        if (viewPathTF) {
            viewPathJPanel = new ViewPathPanel(config);
            viewPathJPanel.setBorder(BorderFactory.createTitledBorder(TAB_VIEW + ": "));
            tabbedPane.addTab(TAB_VIEW, viewPathJPanel);
        }

        // SETUP GRIDBAGLAYOUT
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 0; // first row
        c.gridx = 0; // first column
        c.weightx = 0.5; // absorb right & left space
        c.weighty = 0.5; // absorb top & bottom space
        c.gridwidth = 1; // use one cell for component

        this.add(tabbedPane, c);

        this.updateSubPanels();
        this.revalidate();
        this.repaint();
    }

    /**
     * Called each time the concept selection changes.
     * 
     * @param theCBean
     * @param config
     * @throws IOException
     */
    public void updateSubPanels() throws IOException {

        // DIFFS JPANEL
        diffPathJPanel.update();

        // EQUIVALENTS JPANEL
        equivJPanel.update();

        // EQUIVALENTS JPANEL
        logicJPanel.update();

        // EQUIVALENTS JPANEL
        if (viewPathTF)
            viewPathJPanel.update();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            updateSubPanels();
            revalidate();
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLog(this, Level.SEVERE,
                    "Database Exception: " + e1.getLocalizedMessage(), e1);
        }
    }

    private void getClassifyPrefs(ACE ace) {
        tf = Terms.get();
        try {
            config = ace.getAceFrameConfig();
            // CHECK CLASSIFIER ISA
            if (config.getClassifierIsaType() == null) {
                String errStr = "Classification 'Is a' -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
                return;
            }

            // CHECK & GET EDIT_PATH
//            I_GetConceptData cEditPathObj = config.getClassifierInputPath();
//            if (cEditPathObj != null) {
//                PathBI cEditIPath = tf.getPath(cEditPathObj.getNid());
//                cEditPathPos = new ArrayList<PositionBI>();
//                cEditPathPos.add(new Position(Integer.MAX_VALUE, cEditIPath));
//                addPathOrigins(cEditPathPos, cEditIPath);
//            }

            // CHECK & GET CLASSIFER_PATH
            I_GetConceptData cClassPathObj = config.getClassifierOutputPath();
            if (cClassPathObj != null) {
                PathBI cClassIPath = tf.getPath(cClassPathObj.getUids());
                cClassPositionBIList = new ArrayList<PositionBI>();
                cClassPositionBIList.add(new Position(Long.MAX_VALUE, cClassIPath));
                addPathOrigins(cClassPositionBIList, cClassIPath);
            } else {
                if (config.getViewPositionSet() == null) {
                    String errStr = "(SnoRocketTabPanel error) View path is not set.";
                    AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
                } else if (config.getViewPositionSet().size() != 1) {
                    String errStr = "(SnoRocketTabPanel error) Profile must have exactly one view path. Found: "
                            + config.getViewPositionSet();
                    AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
                } else {
                    cClassPositionBIList = new ArrayList<PositionBI>();
                    cClassPositionBIList.add(config.getViewPositionSet().iterator().next());
                }
            }

            SnoAB.posList = cClassPositionBIList;
            SnoAB.snorocketAuthorNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.USER.SNOROCKET
                    .getUids());
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    private void addPathOrigins(List<PositionBI> origins, PathBI p) {
        origins.addAll(p.getOrigins());
        for (PositionBI o : p.getOrigins()) {
            addPathOrigins(origins, o.getPath());
        }
    }
}
