package org.dwfa.ace.gui.concept;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;

import org.dwfa.ace.I_ImplementActiveLabel;
import org.dwfa.ace.LabelForTuple;
import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.forms.FormsTableModel;
import org.dwfa.ace.table.forms.FormsTableRenderer;
import org.dwfa.ace.task.classify.SnoAB;
import org.dwfa.ace.task.classify.SnoGrp;
import org.dwfa.ace.task.classify.SnoGrpList;
import org.dwfa.ace.task.classify.SnoRel;
import org.dwfa.ace.task.classify.SnoTable;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceIdentification;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.ThinConTuple;
import org.dwfa.vodb.types.ThinRelTuple;

import com.sleepycat.je.DatabaseException;

/**
 * Stated & Inferred Panel view.
 * 
 */
public class LogicalFormsPanel extends JPanel implements ActionListener {

    /**
     * <b>DeltaColors</b><br>
     * Uses <code>AWT Color</code> object which use some of the following <a
     * href=http://www.w3schools.com/html/html_colornames.asp>color names &
     * values</a>. These colors are used to highlight differences.
     */
    public static class DeltaColors {

        private List<Color> colorList = new ArrayList<Color>(); // AWT: Color

        int currentColor = 0;

        public DeltaColors() {
            super();
            // Link for colors
            // http://www.w3schools.com/html/html_colornames.asp
            colorList.add(new Color(0x5F9EA0));
            colorList.add(new Color(0x7FFF00));
            colorList.add(new Color(0xD2691E));
            colorList.add(new Color(0x6495ED));
            colorList.add(new Color(0xDC143C));
            colorList.add(new Color(0xB8860B));
            colorList.add(new Color(0xFF8C00));
            colorList.add(new Color(0x8FBC8F));
            colorList.add(new Color(0x483D8B));
            colorList.add(new Color(0x1E90FF));
            colorList.add(new Color(0xFFD700));
            colorList.add(new Color(0xF0E68C));
            colorList.add(new Color(0x90EE90));
            colorList.add(new Color(0x8470FF)); // 14 colors
        }

        public Color getNextColor() {
            if (currentColor == colorList.size()) {
                reset();
            }
            return colorList.get(currentColor++);
        }

        public void reset() {
            currentColor = 0;
        }
    }

    private static final long serialVersionUID = 1L;

    // ** GRAPHICAL USER INTERFACE **
    private JPanel tableJPanel;

    private boolean deltaJPanelFlag = true;
    //private JPanel commonJPanel;
    //private JPanel commonPartJPanel;
    //private JPanel deltaJPanel;
    //private JPanel deltaPartJPanel;

    private boolean formsJPanelFlag = false;
    //private JPanel formsJPanel; // sub panels added using tmpJPanel

    boolean statsJPanelFlag = false;
    private JPanel statsJPanel; // for additional useful information

    private JCheckBox showStatusCB = new JCheckBox("show status");
    private JCheckBox showDetailCB = new JCheckBox("show detail");
    private JCheckBox showDistFormCB = new JCheckBox("Distribution");
    private JCheckBox showAuthFormCB = new JCheckBox("Authoring");
    private JCheckBox showLongFormCB = new JCheckBox("Long Canonical");
    private JCheckBox showShortFormCB = new JCheckBox("Short Canonical");

    // JLabel with ActionListener
    private List<I_ImplementActiveLabel> commonLabels;

    // AWT: Dimension(int Width, int Height) in pixels(???)
    private Dimension maxPartPanelSize = new Dimension(
            TermLabelMaker.LABEL_WIDTH + 20, 4000);
    private Dimension minPartPanelSize = new Dimension(
            TermLabelMaker.LABEL_WIDTH + 20, 100);

    private DeltaColors colors = new DeltaColors();

    // ** WORKBENCH PARTICULARS **
    private I_TermFactory tf;
    private I_ConfigAceFrame config;
    private ConceptBean theCBean;
    SnoAB util = null;
    boolean showGroupLabels = true; // toggles grouped vs. single label display

    // ** CORE CONSTANTS **
    private static int isaNid;
    private static int isCURRENT = Integer.MIN_VALUE;

    // INPUT PATHS
    I_GetConceptData cEditPathObj = null;
    I_Path cEditIPath = null;
    List<I_Position> cEditPathPos = null; // Edit (Stated) Path I_Positions

    // OUTPUT PATHS
    I_GetConceptData cClassPathObj;
    I_Path cClassIPath;
    List<I_Position> cClassPathPos; // Classifier (Inferred) Path I_Positions

    I_Path inferredPath;
    Position inferredPos;
    I_Path statedPath;
    Position statedPos;

    // ** STATISTICS **
    // !!! :TODO: ??? need reset statistics routine
    private int countFindIsaProxDuplPart = 0;
    private int countFindRoleProxDuplPart = 0;
    private int countFindSelfDuplPart = 0;
    private int countIsCDefinedDuplPart = 0;
    private int countFindIsaProxDuplPartGE2 = 0;
    private int countFindRoleProxDuplPartGE2 = 0;
    private int countFindSelfDuplPartGE2 = 0;
    private int countIsCDefinedDuplPartGE2 = 0;

    // ** :DEBUG: **
    private boolean debug = false;

    // private int countIsCDefinedDuplPart = 0;

    public LogicalFormsPanel() {
        super();
        setLayout(new GridBagLayout()); // LogicalFormsPanel LayoutManager
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST; // Place LogicalFormsPanel

        // TOP ROW
        c.gridy = 0; // first row
        c.gridx = 0; // reset at west side of row
        c.weightx = 0.0; // no extra space
        c.weighty = 0.0; // no extra space
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;

        // NORMAL FORMS TABLE VIEW
        tableJPanel = newMinMaxJPanel();
        tableJPanel.setLayout(new GridBagLayout());
        tableJPanel.setName("Norm Forms Table");
        tableJPanel.setBorder(BorderFactory
                .createTitledBorder("Normal Forms Table View: "));
        add(tableJPanel, c);

        // ADD CHECK BOXES
        if (false) {
        c.gridy++;// next row
        c.gridx = 0;
        c.gridwidth = 5;
        JLabel label = new JLabel("Normal Forms Expanded View:");
        label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
        add(label, c);
        c.gridx++;
        add(showDistFormCB, c);
        c.gridx++;
        add(showAuthFormCB, c);
        c.gridx++;
        add(showLongFormCB, c);
        c.gridx++;
        add(showShortFormCB, c);
        }
        // FORM SELECTION CHECKBOX ROW
        c.gridy++; // next row
        c.gridx = 0; // first cell in row
        c.gridwidth = 1;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;

//        label = new JLabel("Information:");
//        label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
//        add(label, c);
//
//        c.gridx++;
//        add(showDetailCB, c);
//        c.gridx++;
//        add(showStatusCB, c);

        // SETUP CHECKBOX VALUES & LISTENER
        showStatusCB.setSelected(false);
        showStatusCB.addActionListener(this);
        showDetailCB.setSelected(false);
        showDetailCB.addActionListener(this);
        showDistFormCB.setSelected(false);
        showDistFormCB.addActionListener(this);
        showAuthFormCB.setSelected(false);
        showAuthFormCB.addActionListener(this);
        showLongFormCB.setSelected(false);
        showLongFormCB.addActionListener(this);
        showShortFormCB.setSelected(false);
        showShortFormCB.addActionListener(this);

        // COMMON & DIFFERENT PANELS ROW
//        c.gridy++;
//        c.gridx = 0;
//        c.gridwidth = 2;
//        c.fill = GridBagConstraints.BOTH;
//        c.weightx = 0;
//        commonJPanel = newMinMaxJPanel();
//        commonJPanel.setLayout(new GridLayout(0, 1));
//        commonJPanel.setName("Common Panel");
//        commonJPanel.setBorder(BorderFactory.createTitledBorder("Common: "));
//        add(commonJPanel, c);
//        
//        c.gridx = c.gridx + 1;
//        deltaJPanel = newMinMaxJPanel();
//        deltaJPanel.setLayout(new GridLayout(0, 1));
//        deltaJPanel.setName("Differences Panel");
//        deltaJPanel.setBorder(BorderFactory.createTitledBorder("Different: "));
//        add(deltaJPanel, c);

        // FORMS PANEL ROW
//        c.gridy++;// next row
//        c.gridx = 0; // reset at west side of row
//        c.gridwidth = 2; // number of cells in row
//        c.fill = GridBagConstraints.BOTH;
//        formsJPanel = new JPanel(new GridBagLayout());
//        formsJPanel.setName("Forms Panel");
//        formsJPanel.setBorder(BorderFactory.createTitledBorder("Forms: "));
//        JScrollPane formJScrollPane = new JScrollPane(formsJPanel);
//        formJScrollPane
//                .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
//        add(formJScrollPane, c);

        // STATS PANEL ROW
        if (statsJPanelFlag) {
            c.gridy++;// next row
            c.gridx = 0; // reset at west side of row
            c.gridwidth = 1;
            statsJPanel = new JPanel(new GridBagLayout());
            statsJPanel.setName("Stats Panel");
            statsJPanel.setBorder(BorderFactory.createTitledBorder("Stats: "));
            add(statsJPanel, c);
        }

        // COMPONENT BORDER
        setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createEmptyBorder(1, 1, 1, 3), BorderFactory
                .createLineBorder(Color.GRAY)));

        tf = LocalVersionedTerminology.get();
        // SETUP CLASSIFIER PREFERENCE FIELDS
        try {
            config = tf.getActiveAceFrameConfig();
            if (config.getClassifierIsaType() == null) {
                String errStr = "Classifier Is-a -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new Exception(errStr));
                return;
            }
            isaNid = config.getClassifierIsaType().getConceptId();
            // :TODO: review as acceptable status set @@@
            isCURRENT = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT
                    .getUids()); // 0 CURRENT, 1 RETIRED

            // GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
            // GET ALL EDIT_PATH ORIGINS
            I_GetConceptData cEditPathObj = config.getClassifierInputPath();
            if (cEditPathObj == null) {
                String errStr = "Classifier Input (Edit) Path -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new Exception(errStr));
                return;
            }
            cEditIPath = tf.getPath(cEditPathObj.getUids());
            cEditPathPos = new ArrayList<I_Position>();
            cEditPathPos.add(new Position(Integer.MAX_VALUE, cEditIPath));
            addPathOrigins(cEditPathPos, cEditIPath);

            // GET ALL CLASSIFER_PATH ORIGINS
            I_GetConceptData cClassPathObj = config.getClassifierOutputPath();
            if (cClassPathObj == null) {
                String errStr = "Classifier Output (Inferred) Path -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new Exception(errStr));
                return;
            }
            cClassIPath = tf.getPath(cClassPathObj.getUids());
            cClassPathPos = new ArrayList<I_Position>();
            cClassPathPos.add(new Position(Integer.MAX_VALUE, cClassIPath));
            addPathOrigins(cClassPathPos, cClassIPath);

            util = new SnoAB();
            SnoAB.isCURRENT = isCURRENT;
            SnoAB.isaNid = isaNid;
            SnoAB.posList = cClassPathPos;

        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void addPathOrigins(List<I_Position> origins, I_Path p) {
        origins.addAll(p.getOrigins());
        for (I_Position o : p.getOrigins()) {
            addPathOrigins(origins, o.getPath());
        }
    }

    private void setMinMaxSize(JPanel panel) {
        panel.setMinimumSize(minPartPanelSize);
        panel.setMaximumSize(maxPartPanelSize);
    }

    private JPanel newMinMaxJPanel() {
        JPanel p = new JPanel() {
            /**
	          * 
	          */
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = Math.max(d.width, minPartPanelSize.width);
                d.height = Math.max(d.height, minPartPanelSize.height);
                d.height = Math.min(d.height, maxPartPanelSize.height);
                d.height = Math.min(d.height, maxPartPanelSize.height);
                return d;
            }
        };
        setMinMaxSize(p);
        return p;
    }

    /**
     * Called each time the concept selection changes.
     * 
     * @param theCBean
     * @param config
     * @throws IOException
     */
    public void setConcept(ConceptBean conceptIn, I_ConfigAceFrame config)
            throws IOException {
        this.theCBean = conceptIn;
        this.config = config;
        tableJPanel.removeAll();
        // commonJPanel.removeAll();
        // deltaJPanel.removeAll();
        //formsJPanel.removeAll(); // FORMS HAS TWO SUBPANELS: STATED & COMPUTED
        if (statsJPanelFlag)
            statsJPanel.removeAll();

        if (conceptIn != null) {

            // COMMON & DIFFERENT SECTION
            // COMMON PANEL
//            commonLabels = getCommonLabels(showDetailCB.isSelected(),
//                    showStatusCB.isSelected(), config); // ####
//            commonPartJPanel = new JPanel();
//            setMinMaxSize(commonPartJPanel);
//            commonPartJPanel.setLayout(new BoxLayout(commonPartJPanel,
//                    BoxLayout.Y_AXIS));
//            for (I_ImplementActiveLabel l : commonLabels) {
//                commonPartJPanel.add(l.getLabel());
//            }
//
//            GridBagConstraints c = new GridBagConstraints();
//            c.fill = GridBagConstraints.NONE;
//            c.anchor = GridBagConstraints.NORTHWEST;
//            c.gridheight = 1;
//            c.gridwidth = 1;
//            c.weightx = 0;
//            c.weighty = 0;
//            c.gridx = 0;
//            c.gridy = 0;
//            commonJPanel.add(commonPartJPanel, c);

            // DELTA (DIFFERENCES) PANEL
            Map<I_ConceptAttributeTuple, Color> conAttrColorMap = new HashMap<I_ConceptAttributeTuple, Color>();
            Map<I_DescriptionTuple, Color> desColorMap = new HashMap<I_DescriptionTuple, Color>();
            Map<I_RelTuple, Color> relColorMap = new HashMap<I_RelTuple, Color>();
            colors.reset();
            Collection<I_ImplementActiveLabel> deltaLabels = getDeltaLabels(
                    showDetailCB.isSelected(), showStatusCB.isSelected(),
                    config, colors, conAttrColorMap, desColorMap, relColorMap); // ####
//            deltaPartJPanel = new JPanel();
//            deltaPartJPanel.setLayout(new BoxLayout(deltaPartJPanel,
//                    BoxLayout.Y_AXIS));
//            for (I_ImplementActiveLabel l : deltaLabels) {
//                deltaPartJPanel.add(l.getLabel());
//            }
//            deltaJPanel.add(deltaPartJPanel);

            // FORM STATED PANEL
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.VERTICAL;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 0; // horizontal free space distribution weight
            c.weighty = 0; // vertical free space distribution weight
            c.gridx = 0;
            c.gridy = 0;

            I_Path path;
            if (config.getClassifierInputPath() == null) {
                String errStr = "Classifier Input (Edit) Path -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new Exception(errStr));
                return;
            }

            try {
                path = ((VodbEnv) LocalVersionedTerminology.get())
                        .getPath(config.getClassifierInputPath().getConceptId());
            } catch (Exception e) {
                throw new ToIoException(e);
            }
            I_Position p = new Position(Integer.MAX_VALUE, path);

            JPanel tmpJPanel;
            tmpJPanel = newFormStatedJPanel("Stated Form:", p, config,
                    conAttrColorMap, desColorMap, relColorMap); // ####
            setMinMaxSize(tmpJPanel);
            //formsJPanel.add(tmpJPanel, c);

            // TABLE JPANEL
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weighty = 0; // vertical free space distribution weight
            c.weightx = 0; // horizontal free space distribution weight
            c.gridy = 0; // reset to row one
            c.gridx = 0; // reset to column one
            tmpJPanel = newFormTableJPanel("", inferredPos, config); // ####
            setMinMaxSize(tmpJPanel);
            tableJPanel.add(tmpJPanel, c);

            // FORM DISTRIBUTION NORMAL PANEL
            if (showDistFormCB.isSelected()) {
                c.gridx++;
                if (c.gridx == 2) {
                    c.gridx = 0;
                    c.gridy++;
                }
                tmpJPanel = newFormDistJPanel("Distribution Normal Form:",
                        inferredPos, config, conAttrColorMap, desColorMap,
                        relColorMap); // ####
                setMinMaxSize(tmpJPanel);
                //formsJPanel.add(tmpJPanel, c);
            }

            // AUTHORING NORMAL FORM PANEL
            if (showAuthFormCB.isSelected()) {
                c.gridx++;
                if (c.gridx == 2) {
                    c.gridx = 0;
                    c.gridy++;
                }
                tmpJPanel = newFormAuthJPanel("Authoring Normal Form:",
                        inferredPos, config, conAttrColorMap, desColorMap,
                        relColorMap); // ####
                setMinMaxSize(tmpJPanel);
                //formsJPanel.add(tmpJPanel, c);
            }

            // LONG CANONICAL FORM PANEL
            if (showLongFormCB.isSelected()) {
                c.gridx++;
                if (c.gridx == 2) {
                    c.gridx = 0;
                    c.gridy++;
                }
                tmpJPanel = newFormLongJPanel("Long Canonical Form:",
                        inferredPos, config, conAttrColorMap, desColorMap,
                        relColorMap); // ####
                setMinMaxSize(tmpJPanel);
                //formsJPanel.add(tmpJPanel, c);
            }

            // FORM SHORT CANONICAL PANEL
            if (showShortFormCB.isSelected()) {
                c.gridx++;
                if (c.gridx == 2) {
                    c.gridx = 0;
                    c.gridy++;
                }
                tmpJPanel = newFormShortJPanel("Short Canonical Form:",
                        inferredPos, config, conAttrColorMap, desColorMap,
                        relColorMap); // ####
                setMinMaxSize(tmpJPanel);
                //formsJPanel.add(tmpJPanel, c);
            }

            // STATISTICS PANEL
            if (statsJPanelFlag) {
                c = new GridBagConstraints();
                c.fill = GridBagConstraints.BOTH;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.weighty = 0; // vertical free space distribution weight
                c.weightx = 0; // horizontal free space distribution weight
                c.gridy = 0; // reset to row one
                c.gridx = 0; // reset to column one

                c.gridy++; // reset to column one
                String markup = statsToHtml();
                JEditorPane ep2 = new JEditorPane("text/html", markup);
                JScrollPane statsJScroll = new JScrollPane(ep2);
                statsJScroll.setBorder(new TitledBorder("Statistics"));
                statsJPanel.add(statsJScroll, c);

                AceLog.getAppLog().log(Level.INFO, statsToString());
                statsReset();
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        try {
            setConcept(theCBean, config);
            revalidate();
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLog(this, Level.SEVERE,
                    "Database Exception: " + e1.getLocalizedMessage(), e1);
        }
    }

    public List<I_ImplementActiveLabel> getCommonLabels(boolean showLongForm,
            boolean showStatus, I_ConfigAceFrame config) throws IOException {
        List<I_ImplementActiveLabel> labelList = new ArrayList<I_ImplementActiveLabel>();

        // GET CONCEPT ATTRIBUTES
        Set<I_ConceptAttributeTuple> commonConTuples = this.theCBean
                .getCommonConceptAttributeTuples(config); // #### COMMON CON
        // CREATE CONCEPT ATTRIBUTE LABELS
        if (commonConTuples != null) {
            for (I_ConceptAttributeTuple t : commonConTuples) {
                I_ImplementActiveLabel conAttrLabel = TermLabelMaker.newLabel(
                        t, showLongForm, showStatus);
                setBorder(conAttrLabel.getLabel(), null);
                labelList.add(conAttrLabel);
            }
        }

        // GET SOURCE RELATIONSHIPS
        Set<I_RelTuple> commonRelTuples = this.theCBean
                .getCommonRelTuples(config); // #### COMMON REL
        // CREATE RELATIONSHIP LABELS
        if (commonRelTuples != null) {
            for (I_RelTuple t : commonRelTuples) {
                I_ImplementActiveLabel relLabel = TermLabelMaker.newLabel(t,
                        showLongForm, showStatus);
                setBorder(relLabel.getLabel(), null);
                labelList.add(relLabel);
            }
        }

        return labelList;
    }

    private void setBorder(JLabel tLabel, Color deltaColor) {
        if (deltaColor == null) {
            deltaColor = Color.white;
        }
        Dimension size = tLabel.getSize();
        tLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createRaisedBevelBorder(), BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 5, 1, 5, deltaColor),
                BorderFactory.createEmptyBorder(1, 3, 1, 3))));
        size.width = size.width + 18;
        size.height = size.height + 6;
        tLabel.setSize(size);
        tLabel.setPreferredSize(size);
        tLabel.setMaximumSize(size);
        tLabel.setMinimumSize(size);
    }

    public Collection<I_ImplementActiveLabel> getDeltaLabels(
            boolean showLongForm, boolean showStatus, I_ConfigAceFrame config,
            DeltaColors colors,
            Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
            Map<I_DescriptionTuple, Color> descColorMap,
            Map<I_RelTuple, Color> relColorMap) throws IOException {

        Set<I_ConceptAttributeTuple> allConAttrTuples = new HashSet<I_ConceptAttributeTuple>();
        Set<I_RelTuple> allRelTuples = new HashSet<I_RelTuple>();

        // FIND ALL...
        for (I_Position p : config.getViewPositionSet()) {
            Set<I_Position> posSet = new HashSet<I_Position>();
            posSet.add(p);

            // concept attributes
            List<I_ConceptAttributeTuple> conTuplesForPosition = this.theCBean
                    .getConceptAttributeTuples(config.getAllowedStatus(),
                            posSet, false); // #### ALL COMMON CON
            allConAttrTuples.addAll(conTuplesForPosition);

            // relationships
            List<I_RelTuple> relTuplesForPosition = this.theCBean
                    .getSourceRelTuples(config.getAllowedStatus(), null,
                            posSet, false); // #### ALL REL
            allRelTuples.addAll(relTuplesForPosition);
        }

        // FIND & REMOVE COMMON...
        Set<I_ConceptAttributeTuple> commonConAttrTuples = this.theCBean
                .getCommonConceptAttributeTuples(config); // #### COMMON CON
        allConAttrTuples.removeAll(commonConAttrTuples);
        Set<I_RelTuple> commonRelTuples = this.theCBean
                .getCommonRelTuples(config); // #### COMMON REL
        allRelTuples.removeAll(commonRelTuples);

        Collection<I_ImplementActiveLabel> labelList = new ArrayList<I_ImplementActiveLabel>();
        // CREATE CONCEPT ATTRIBUTE LABELS
        for (I_ConceptAttributeTuple t : allConAttrTuples) {
            I_ImplementActiveLabel conAttrLabel = TermLabelMaker.newLabel(t,
                    showLongForm, showStatus);
            Color deltaColor = colors.getNextColor();
            conAttrColorMap.put(t, deltaColor);
            setBorder(conAttrLabel.getLabel(), deltaColor);
            labelList.add(conAttrLabel);
        }
        // CREATE RELATIONSHIP LABELS
        for (I_RelTuple t : allRelTuples) {
            I_ImplementActiveLabel relLabel = TermLabelMaker.newLabel(t,
                    showLongForm, showStatus);
            Color deltaColor = colors.getNextColor();
            relColorMap.put(t, deltaColor);
            setBorder(relLabel.getLabel(), deltaColor);
            labelList.add(relLabel);
        }

        return labelList;
    }

    /**
     * <code><b>
     * List<I_RelTuple> findIsaProximal<br> List<I_RelTuple> findIsaProximalPrim<br>
     * List<I_RelTuple> findRoleProximal<br> SnoGrpList findRoleDiffFromRoot<br>
     * SnoGrpList findRoleDiffFromProx<br> SnoGrpList findRoleDiffFromProxPrim<br>
     * </b></code>
     */

    private Object[][] findAllFormsData() {
        Object[][] tmpData = {
                {
                        "<html><font face='Dialog' size='3' color='blue'>Is a",
                        "<html><font face='Dialog' size='3' color='green'>Operative procedure on cornea",
                        "X", "X", "X", "X", " ", " " },
                {
                        "<html><font face='Dialog' size='3' color='blue'>Is a",
                        "<html><font face='Dialog' size='3' color='green'>Removal of implanted material from anterior segment of eye",
                        "X", "X", "X", "X", " ", " " },
                {
                        "<html><font face='Dialog' size='3' color='blue'>Acess",
                        "<html><font face='Dialog' size='3' color='green'>Surgical access values",
                        "X", "X", "X", " ", " ", " " },
                {
                        "<html><font face='Dialog' size='3' color='blue'>Priorities",
                        "<html><font face='Dialog' size='3' color='green'>Surgical access values",
                        "X", "X", "X", " ", " ", " " },
                {
                        "<html><font face='Dialog' size='3' color='blue'>Method<br>Direct device<br>Procudure site - Indirect",
                        "<html><font face='Dialog' size='3' color='green'>Surgical removal - action<br>Corneal implant<br>Corneal structure",
                        "X", "X", "X", "X", " ", " " } };

        // SHOW SELF CONCEPT
        I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);

        // add self to table !!!

        // SHOW PROXIMAL ISAs -- as relationships
        List<I_RelTuple> relList = findIsaProximal(theCBean, cClassPathPos);
        for (I_RelTuple rTuple : relList) {
            // add IS-A to table !!!
        }

        // SHOW ROLES, NON-REDUNDANT, DIFFERENTIATED FROM ROOT
        SnoGrpList sgl = findRoleDiffFromRoot(theCBean, cClassPathPos);
        if (sgl.size() > 0) {
            int i = 0;
            SnoGrp sg = sgl.get(0);
            // show each of the non-grouped Rels
            if (sg.size() > 0 && sg.get(0).group == 0) {
                for (SnoRel sr : sg) {
                    // SHOW RELATIONSHIP !!!
                }
                i++; // skip past 0 index of the "un-grouped"
            }

            // show each of the groups
            for (; i < sgl.size(); i++) {
                sg = sgl.get(i);
                if (sg.size() == 0)
                    continue;
                if (showGroupLabels) { // true shows one label per group
                    List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();
                    for (SnoRel sr : sg) {
                        // CONVERT SNOREL LIST TO TUPLE LIST
                        grpTuple.add(new ThinRelTuple(sr.relVers, sr.relPart));
                    }
                    // ADD GROUP TO TABLE !!!
                }
            }
        }

        return tmpData;
    } // findAllFormsData

    private List<I_RelTuple> findIsaProximal(ConceptBean cBean,
            List<I_Position> posList) {
        List<I_RelTuple> returnRTuples = new ArrayList<I_RelTuple>();
        try {
            List<I_RelVersioned> relList = cBean.getSourceRels();
            for (I_RelVersioned rel : relList) { // FOR EACH [C1, C2] PAIR
                // FIND MOST_RECENT REL PART, ON HIGHEST_PRIORITY_PATH
                I_RelPart rp1 = null;
                for (I_Position pos : posList) { // FOR EACH PATH POSITION
                    // FIND MOST CURRENT
                    int tmpCountDupl = 0;
                    for (I_RelPart rp : rel.getVersions()) {
                        if (rp.getPathId() == pos.getPath().getConceptId()) {
                            if (rp1 == null) {
                                rp1 = rp; // ... KEEP FIRST_INSTANCE PART
                            } else if (rp1.getVersion() < rp.getVersion()) {
                                rp1 = rp; // ... KEEP MORE_RECENT PART
                            } else if (rp1.getVersion() == rp.getVersion()) {
                                // DUPLICATE PART SHOULD NEVER HAPPEN
                                tmpCountDupl++;
                            }
                        }
                    }
                    if (rp1 != null) {
                        if (rp1.getStatusId() == isCURRENT
                                && rp1.getTypeId() == isaNid) {
                            returnRTuples.add(new ThinRelTuple(rel, rp1));
                        }
                        // VERIFICATION STATISTICS
                        if (tmpCountDupl > 1) {
                            countFindIsaProxDuplPart++;
                            countFindIsaProxDuplPartGE2++;
                        } else if (tmpCountDupl == 1) {
                            countFindIsaProxDuplPart++;
                        }
                        break; // IF FOUND ON THIS PATH, STOP SEARCHING
                    }
                } // FOR EACH PATH POSITION

            } // FOR EACH [C1, C2] PAIR
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return returnRTuples;
    }

    private List<I_RelTuple> findIsaProximalPrim(ConceptBean cBean,
            List<I_Position> posList) {

        List<ConceptBean> isaCBNext = new ArrayList<ConceptBean>();
        List<I_RelTuple> isaRTFinal = new ArrayList<I_RelTuple>();

        List<I_RelTuple> isaRTProx = findIsaProximal(cBean, posList);
        while (isaRTProx.size() > 0) {
            // TEST LIST FOR PRIMITIVE OR NOT
            for (I_RelTuple isaRT : isaRTProx) {
                ConceptBean isaCB = ConceptBean.get(isaRT.getC2Id());
                if (isCDefined(isaCB, cEditPathPos)) { // i.e. not primitive
                    isaCBNext.add(isaCB); // keep looking for primitive
                } else {
                    int z = 0;
                    while ((z < isaRTFinal.size())
                            && isaRTFinal.get(z).getC2Id() != isaRT.getC2Id()) {
                        z++;
                    }
                    // IF NOT_REDUNDANT, THEN ADD
                    if (z == isaRTFinal.size()) {
                        isaRTFinal.add(isaRT); // add to return primitives list
                    }
                }
            }

            // GET ALL NEXT LEVEL RELS FOR NON_PRIMITIVE CONCEPTS
            isaRTProx = new ArrayList<I_RelTuple>();
            for (ConceptBean cbNext : isaCBNext) {
                List<I_RelTuple> nextTuples = findIsaProximal(cbNext, posList);
                if (nextTuples.size() > 0)
                    isaRTProx.addAll(nextTuples);
            }

            // RESET NEXT LEVEL SEARCH LIST
            isaCBNext = new ArrayList<ConceptBean>(); // clear find next list
        }

        return isaRTFinal;
    }

    private List<I_RelTuple> findRoleProximal(ConceptBean cBean,
            List<I_Position> posList) {
        List<I_RelTuple> returnRTuples = new ArrayList<I_RelTuple>();
        try {
            List<I_RelVersioned> relList = cBean.getSourceRels();
            for (I_RelVersioned rel : relList) { // FOR EACH [C1, C2] PAIR
                // FIND MOST_RECENT REL PART, ON HIGHEST_PRIORITY_PATH
                I_RelPart rp1 = null;
                for (I_Position pos : posList) { // FOR EACH PATH POSITION
                    // FIND MOST CURRENT
                    int tmpCountDupl = 0;
                    for (I_RelPart rp : rel.getVersions()) {
                        if (rp.getPathId() == pos.getPath().getConceptId()) {
                            if (rp1 == null) {
                                rp1 = rp; // ... KEEP FIRST_INSTANCE PART
                            } else if (rp1.getVersion() < rp.getVersion()) {
                                rp1 = rp; // ... KEEP MORE_RECENT PART
                            } else if (rp1.getVersion() == rp.getVersion()) {
                                // DUPLICATE PART SHOULD NEVER HAPPEN
                                tmpCountDupl++;
                            }
                        }
                    }
                    if (rp1 != null) {
                        if (rp1.getStatusId() == isCURRENT
                                && rp1.getTypeId() != isaNid) {
                            returnRTuples.add(new ThinRelTuple(rel, rp1));
                        }
                        // VERIFICATION STATISTICS
                        if (tmpCountDupl > 1) {
                            countFindRoleProxDuplPart++;
                            countFindRoleProxDuplPartGE2++;
                        } else if (tmpCountDupl == 1) {
                            countFindRoleProxDuplPart++;
                        }
                        break; // IF FOUND ON THIS PATH, STOP SEARCHING
                    }
                } // FOR EACH PATH POSITION

            } // FOR EACH [C1, C2] PAIR
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return returnRTuples;
    }

    private SnoGrpList findRoleDiffFromRoot(ConceptBean cBean,
            List<I_Position> posList) {
        SnoGrpList rgl_A;
        SnoGrpList rgl_B;
        SnoGrp rv_A;
        SnoGrp rv_B;

        // :DEBUG:
        Set<Integer> debugRoleSet = new HashSet<Integer>();
        Set<Integer> debugValueSet = new HashSet<Integer>();

        // GET IMMEDIATE PROXIMAL ROLES & SEPARATE GROUPS
        List<I_RelTuple> roleRTProx = findRoleProximal(cBean, posList);
        if (debug)
            debugUpdateSets(roleRTProx, debugRoleSet, debugValueSet);
        rgl_A = splitGrouped(roleRTProx);
        rv_A = splitNonGrouped(roleRTProx);

        // GET PROXIMAL ISAs, one next level up at a time
        List<I_RelTuple> isaRTNext = findIsaProximal(cBean, posList);
        List<I_RelTuple> isaRTNextB = new ArrayList<I_RelTuple>();
        while (isaRTNext.size() > 0) {

            // FOR EACH CURRENT PROXIMAL CONCEPT...
            for (I_RelTuple isaRT : isaRTNext) {
                ConceptBean isaCB = ConceptBean.get(isaRT.getC2Id());

                // ... EVALUATE PROXIMAL ROLES & SEPARATE GROUP
                roleRTProx = findRoleProximal(isaCB, posList);
                if (debug)
                    debugUpdateSets(roleRTProx, debugRoleSet, debugValueSet);
                rgl_B = splitGrouped(roleRTProx);
                rv_B = splitNonGrouped(roleRTProx);

                // KEEP DIFFERENTIATED, STAND ALONE, ROLE-VALUE PAIRS
                rv_A = rv_A.whichRoleValDifferFrom(rv_B);
                // setup rv_A for the next iteration
                // add anything new which also differentiates
                rv_A.addAllWithSort(rv_B.whichRoleValDifferFrom(rv_A));

                // KEEP DIFFERENTIATED GROUPS
                // keep what continues to differentiate
                rgl_A = rgl_A.whichDifferentiateFrom(rgl_B);
                // add anything new which also differentiates
                rgl_A.addAll(rgl_B.whichDifferentiateFrom(rgl_A));

                // ... GET PROXIMAL ISAs
                isaRTNextB.addAll(findIsaProximal(isaCB, posList));
            }

            // SETUP NEXT LEVEL OF ISAs
            isaRTNext = isaRTNextB;
            isaRTNextB = new ArrayList<I_RelTuple>();
        }

        // last check for redundant -- check may not be needed
        rv_A = rv_A.whichRoleValAreNonRedundant();
        rgl_A = rgl_A.whichNonRedundant();

        if (debug)
            debugToStringNidSet(debugRoleSet, debugValueSet);

        // Remove any un-grouped which do not differentiate from other groups
        SnoGrpList grpList0 = new SnoGrpList();
        for (SnoRel a : rv_A)
            grpList0.add(new SnoGrp(a));
        grpList0 = grpList0.whichDifferentiateFrom(rgl_A);

        // Repackage the un-grouped for passing to the GUI label presentation.
        SnoGrp keepGrp0 = new SnoGrp();
        for (SnoGrp g : grpList0)
            keepGrp0.add(g.get(0));
        // All un-grouped must in position 0 of the returned group list
        rgl_A.add(0, keepGrp0.sortByType());

        return rgl_A;
    }

    private SnoGrpList findRoleDiffFromProx(ConceptBean cBean,
            List<I_RelTuple> isaList, List<I_Position> posList) {

        // FIND IMMEDIATE ROLES OF *THIS*CONCEPT*
        List<I_RelTuple> roleRTSetA = findRoleProximal(cBean, posList);
        SnoGrpList grpListA = splitGrouped(roleRTSetA);
        SnoGrp unGrpA = splitNonGrouped(roleRTSetA);

        // FIND NON-REDUNDANT ROLE SET OF PROXIMATE ISA
        SnoGrpList grpListB = new SnoGrpList();
        SnoGrp unGrpB = new SnoGrp();
        for (I_RelTuple isaRT : isaList) {
            ConceptBean isaCB = ConceptBean.get(isaRT.getC2Id());
            SnoGrpList tmpGrpList = findRoleDiffFromRoot(isaCB, posList);

            // separate un-grouped
            SnoGrp tmpUnGrp;
            if (tmpGrpList.size() > 0)
                tmpUnGrp = tmpGrpList.remove(0);
            else
                break;

            // KEEP DIFFERENTIATED, STAND ALONE, ROLE-VALUE PAIRS
            unGrpB = unGrpB.whichRoleValDifferFrom(tmpUnGrp);
            // setup rv_A for the next iteration
            // add anything new which also differentiates
            unGrpB.addAllWithSort(tmpUnGrp.whichRoleValDifferFrom(unGrpB));

            // keep role-groups which continue to differentiate
            grpListB = grpListB.whichDifferentiateFrom(tmpGrpList);
            // add anything new which also differentiates
            grpListB.addAll(tmpGrpList.whichDifferentiateFrom(grpListB));
        }

        // KEEP ONLY ROLES DIFFERENTIATED FROM MOST PROXIMATE
        unGrpA = unGrpA.whichRoleValDifferFrom(unGrpB);
        grpListA.whichDifferentiateFrom(grpListB);

        // Remove any un-grouped which do not differentiate from other groups
        SnoGrpList grpList0 = new SnoGrpList();
        for (SnoRel a : unGrpA)
            grpList0.add(new SnoGrp(a));
        grpList0 = grpList0.whichDifferentiateFrom(grpListA);

        // Repackage the un-grouped for passing to the GUI label presentation.
        SnoGrp keepGrp0 = new SnoGrp();
        for (SnoGrp g : grpList0)
            keepGrp0.add(g.get(0));
        // All un-grouped must in position 0 of the returned group list
        grpListA.add(0, keepGrp0.sortByType());

        return grpListA;
    }

    private SnoGrpList findRoleDiffFromProxPrim(ConceptBean cBean,
            List<I_RelTuple> isaList, List<I_Position> posList) {

        // FIND ALL NON-REDUNDANT INHERITED ROLES OF *THIS*CONCEPT*
        SnoGrpList grpListA = findRoleDiffFromRoot(cBean, posList);
        // separate un-grouped
        SnoGrp unGrpA;
        if (grpListA.size() > 0)
            unGrpA = grpListA.remove(0);
        else {
            return grpListA;
        }

        // FIND ROLE SET OF MOST PROXIMATE PRIMITIVE
        // List<I_RelTuple> roleRTSetB = new ArrayList<I_RelTuple>();
        SnoGrpList grpListB = new SnoGrpList();
        SnoGrp unGrpB = new SnoGrp();
        for (I_RelTuple isaRT : isaList) {
            ConceptBean isaCB = ConceptBean.get(isaRT.getC2Id());
            SnoGrpList tmpGrpList = findRoleDiffFromRoot(isaCB, posList);

            // separate un-grouped
            SnoGrp tmpUnGrp;
            if (tmpGrpList.size() > 0)
                tmpUnGrp = tmpGrpList.remove(0);
            else
                break;

            // KEEP DIFFERENTIATED, STAND ALONE, ROLE-VALUE PAIRS
            unGrpB = unGrpB.whichRoleValDifferFrom(tmpUnGrp);
            // setup rv_A for the next iteration
            // add anything new which also differentiates
            unGrpB.addAllWithSort(tmpUnGrp.whichRoleValDifferFrom(unGrpB));

            // keep role-groups which continue to differentiate
            grpListB = grpListB.whichDifferentiateFrom(tmpGrpList);
            // add anything new which also differentiates
            grpListB.addAll(tmpGrpList.whichDifferentiateFrom(grpListB));
        }

        // KEEP ONLY ROLES DIFFERENTIATED FROM MOST PROXIMATE PRIMITIVE
        unGrpA = unGrpA.whichRoleValDifferFrom(unGrpB);
        grpListA.whichDifferentiateFrom(grpListB);

        // Remove any un-grouped which do not differentiate from other groups
        SnoGrpList grpList0 = new SnoGrpList();
        for (SnoRel a : unGrpA)
            grpList0.add(new SnoGrp(a));
        grpList0 = grpList0.whichDifferentiateFrom(grpListA);

        // Repackage the un-grouped for passing to the GUI label presentation.
        SnoGrp keepGrp0 = new SnoGrp();
        for (SnoGrp g : grpList0)
            keepGrp0.add(g.get(0));
        // All un-grouped must in position 0 of the returned group list
        grpListA.add(0, keepGrp0.sortByType());

        return grpListA;
    }

    private SnoGrp splitNonGrouped(List<I_RelTuple> relsIn) {
        List<SnoRel> relsOut = new ArrayList<SnoRel>();
        for (I_RelTuple r : relsIn)
            if (r.getGroup() == 0)
                relsOut.add(new SnoRel(r.getFixedPart(), r.getPart(), -1));
        SnoGrp sgOut = new SnoGrp(relsOut, true);
        sgOut = sgOut.whichRoleValAreNonRedundant();
        return sgOut; // returns as sorted.
    }

    private SnoGrpList splitGrouped(List<I_RelTuple> relsIn) {
        SnoGrpList sg = new SnoGrpList();

        // Step 1: Segment
        // :TODO: flag rel-groups with only one member.
        List<SnoRel> srl = new ArrayList<SnoRel>();
        for (I_RelTuple r : relsIn)
            if (r.getGroup() != 0)
                srl.add(new SnoRel(r.getRelVersioned(), r.getPart(), -1));

        if (srl.size() == 0)
            return sg; // this is an empty set.

        Collections.sort(srl);

        // :TODO: since sorted, array may be partitioned a bit more efficiently
        int i = 0;
        int max = srl.size();
        int lastGroupId = srl.get(0).group;
        SnoGrp groupList = new SnoGrp();
        while (i < max) {
            SnoRel thisSnoRel = srl.get(i++);
            if (thisSnoRel.group != lastGroupId) {
                sg.add(groupList); // has been pre-sorted
                groupList = new SnoGrp();
            }
            groupList.add(thisSnoRel);
            lastGroupId = thisSnoRel.group;
        }
        if (groupList.size() > 0)
            sg.add(groupList);

        // Step 2: Get non-Redundant set
        sg = sg.whichNonRedundant();

        return sg;
    }

    private I_ConceptAttributeTuple findSelf(ConceptBean cBean,
            List<I_Position> posList) {
        try {
            I_ConceptAttributeVersioned cv = cBean.getConceptAttributes();
            List<I_ConceptAttributePart> cvList = cv.getVersions();
            I_ConceptAttributePart cp1 = null;
            for (I_Position pos : posList) {
                int tmpCountDupl = 0;
                for (I_ConceptAttributePart cp : cvList) {
                    // FIND MOST RECENT
                    if (cp.getPathId() == pos.getPath().getConceptId()) {
                        if (cp1 == null) {
                            cp1 = cp; // ... KEEP FIRST_INSTANCE PART
                        } else if (cp1.getVersion() < cp.getVersion()) {
                            cp1 = cp; // ... KEEP MORE_RECENT PART
                        } else if (cp1.getVersion() == cp.getVersion()) {
                            // !!! THIS DUPLICATE SHOULD NEVER HAPPEN
                            tmpCountDupl++;
                        }
                    }
                }
                // cp1.getStatusId() == isCURRENT
                if (cp1 != null) { // IF FOUND ON THIS PATH, STOP SEARCHING
                    // VERIFICATION STATISTICS
                    if (tmpCountDupl > 1) {
                        countFindSelfDuplPart++;
                        countFindSelfDuplPartGE2++;
                    } else if (tmpCountDupl == 1) {
                        countFindSelfDuplPart++;
                    }
                    return new ThinConTuple(cv, cp1);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private boolean isCDefined(ConceptBean cBean, List<I_Position> posList) {

        try {
            I_ConceptAttributeVersioned cv = cBean.getConceptAttributes();
            List<I_ConceptAttributePart> cvList = cv.getVersions();
            I_ConceptAttributePart cp1 = null;
            for (I_Position pos : posList) {
                int tmpCountDupl = 0;
                for (I_ConceptAttributePart cp : cvList) {
                    // FIND MOST RECENT
                    if (cp.getPathId() == pos.getPath().getConceptId()) {
                        if (cp1 == null) {
                            cp1 = cp; // ... KEEP FIRST_INSTANCE, CURRENT PART
                        } else if (cp1.getVersion() < cp.getVersion()) {
                            cp1 = cp; // ... KEEP MORE_RECENT, CURRENT PART
                        } else if (cp1.getVersion() == cp.getVersion()) {
                            // !!! THIS DUPLICATE SHOULD NEVER HAPPEN
                            tmpCountDupl++;
                        }
                    }
                }
                if (cp1 != null) { // IF FOUND ON THIS PATH, STOP SEARCHING
                    // VERIFICATION STATISTICS
                    if (tmpCountDupl > 1) {
                        countIsCDefinedDuplPart++;
                        countIsCDefinedDuplPartGE2++;
                    } else if (tmpCountDupl == 1) {
                        countIsCDefinedDuplPart++;
                    }
                    return cp1.isDefined();
                }
            }
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // EXCEPTION --> cBean.getConceptAttributes() FAILED
            e.printStackTrace();
            return false;
        }
    }

    /**
     * <b>Authoring Normal Form</b><li>Most Proximate Supertypes (IS-A)</li>
     */
    public JPanel newFormAuthJPanel(String label, I_Position p,
            I_ConfigAceFrame config,
            Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
            Map<I_DescriptionTuple, Color> desColorMap,
            Map<I_RelTuple, Color> relColorMap) throws IOException {
        JPanel formJPanel = newMinMaxJPanel();
        formJPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        Set<I_Position> posSet = new HashSet<I_Position>(1);
        posSet.add(p);

        List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.NORTHWEST;

        // SHOW SELF CONCEPT
        I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);
        I_ImplementActiveLabel tmpTLabel = TermLabelMaker.newLabelForm(cTuple,
                showDetailCB.isSelected(), showStatusCB.isSelected());
        tLabelList.add((LabelForTuple) tmpTLabel);
        Color tmpDeltaColor = conAttrColorMap.get(cTuple);
        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
        formJPanel.add(tmpTLabel.getLabel(), c);
        c.gridy++;

        // SHOW PROXIMAL ISAs -- as relationships
        List<I_RelTuple> isaList = findIsaProximal(theCBean, cClassPathPos);
        for (I_RelTuple t : isaList) {
            I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t,
                    showDetailCB.isSelected(), showStatusCB.isSelected());
            tLabelList.add((LabelForTuple) tLabel);
            Color deltaColor = relColorMap.get(t);
            setBorder(tLabel.getLabel(), deltaColor);
            formJPanel.add(tLabel.getLabel(), c);
            c.gridy++;
        }

        // FIND NON-REDUNDANT ROLES, DIFFERENTIATED FROM PROXIMATE ISA
        SnoGrpList sgl = findRoleDiffFromProx(theCBean, isaList, cClassPathPos);
        // SHOW ROLE SET
        if (sgl.size() > 0) {
            int i = 0;
            SnoGrp sg = sgl.get(0);
            // show each of the non-Rels
            if (sg.size() > 0 && sg.get(0).group == 0) {
                for (SnoRel sr : sg) {
                    I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                    tmpTLabel = TermLabelMaker.newLabelForm(rTuple,
                            showDetailCB.isSelected(), showStatusCB
                                    .isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(rTuple);
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                }
                i++; // skip past 0 index of the "un-grouped"
            }

            // show each of the groups
            for (; i < sgl.size(); i++) {
                sg = sgl.get(i);
                if (sg.size() == 0)
                    continue; // :TODO: investigate why empty sets exist
                if (showGroupLabels) { // true shows one label per group
                    List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();
                    for (SnoRel sr : sg) {
                        grpTuple.add(new ThinRelTuple(sr.relVers, sr.relPart));
                    }
                    tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB
                            .isSelected(), showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(grpTuple.get(0));
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                } else { // if false, show 1 rel per label
                    for (SnoRel sr : sg) {
                        I_RelTuple rTuple = new ThinRelTuple(sr.relVers,
                                sr.relPart);
                        tmpTLabel = TermLabelMaker.newLabelForm(rTuple,
                                showDetailCB.isSelected(), showStatusCB
                                        .isSelected());
                        tLabelList.add((LabelForTuple) tmpTLabel);
                        tmpDeltaColor = relColorMap.get(rTuple);
                        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                        formJPanel.add(tmpTLabel.getLabel(), c);
                        c.gridy++;
                    }
                    c.gridy++;
                }
            }
        }

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        formJPanel.add(new JPanel(), c);
        formJPanel.setBorder(BorderFactory.createTitledBorder(label));

        return formJPanel;
    }

    /**
     * <b>Distribution Normal Form</b><li>Most Proximate Supertypes (IS-A)</li>
     */
    public JPanel newFormTableJPanel(String label, I_Position p,
            I_ConfigAceFrame config) throws IOException {
        JPanel tableJPanel = newMinMaxJPanel();
        tableJPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 1;
        tableJPanel.add(new JPanel(), c);
        tableJPanel.setBorder(BorderFactory.createTitledBorder(label));

        SnoTable theSnoTable = new SnoTable();
        String[][] theTableStr = null;
        try {
            theTableStr = theSnoTable.getStringData(theCBean);
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return tableJPanel;
        }
        FormsTableModel theTableModel = new FormsTableModel(theTableStr);
        JTable table = new JTable(theTableModel);

        FormsTableRenderer renderer = new FormsTableRenderer(config);
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(String.class, renderer);

        // set column widths
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn tc = table.getColumnModel().getColumn(i);

            // 6 normal forms
            if (i < 6) {
                tc.setMaxWidth(22);
                tc.setMinWidth(22);
                tc.setPreferredWidth(22); // 22 * 6 = 132
                tc.setResizable(false);
            } else {
                tc.setPreferredWidth(400 + 368);
            }
        }

        // set row heights
        int totalRowHeight = 0;
        for (int i = 0; i < table.getRowCount(); i++) {
            // count <br>
            int lineCount = 1;
            int lineIdx = theTableStr[i][6].indexOf("<br>");
            while (lineIdx > -1) {
                lineCount++;
                lineIdx = theTableStr[i][6].indexOf("<br>", lineIdx + 4);
            }
            // set row height
            table.setRowHeight(i, 18 * lineCount);
            totalRowHeight += 18 * lineCount;
        }
        table.setPreferredScrollableViewportSize(new Dimension(900,
                totalRowHeight));
        tableJPanel.add(new JScrollPane(table));

        return tableJPanel;
    }

    /**
     * <b>Distribution Normal Form</b><li>Most Proximate Supertypes (IS-A)</li>
     */
    public JPanel newFormDistJPanel(String label, I_Position p,
            I_ConfigAceFrame config,
            Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
            Map<I_DescriptionTuple, Color> desColorMap,
            Map<I_RelTuple, Color> relColorMap) throws IOException {
        JPanel formJPanel = newMinMaxJPanel();
        formJPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        Set<I_Position> posSet = new HashSet<I_Position>(1);
        posSet.add(p);

        List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.NORTHWEST;

        // SHOW SELF CONCEPT
        I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);
        I_ImplementActiveLabel tmpTLabel = TermLabelMaker.newLabelForm(cTuple,
                showDetailCB.isSelected(), showStatusCB.isSelected());
        tLabelList.add((LabelForTuple) tmpTLabel);
        Color tmpDeltaColor = conAttrColorMap.get(cTuple);
        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
        formJPanel.add(tmpTLabel.getLabel(), c);
        c.gridy++;

        // SHOW PROXIMAL ISAs -- as relationships
        List<I_RelTuple> relList = findIsaProximal(theCBean, cClassPathPos);
        for (I_RelTuple rTuple : relList) {
            tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB
                    .isSelected(), showStatusCB.isSelected());
            tLabelList.add((LabelForTuple) tmpTLabel);
            tmpDeltaColor = relColorMap.get(rTuple);
            setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
            formJPanel.add(tmpTLabel.getLabel(), c);
            c.gridy++;
        }

        // SHOW ROLES, NON-REDUNDANT, DIFFERENTIATED FROM ROOT
        SnoGrpList sgl = findRoleDiffFromRoot(theCBean, cClassPathPos);
        if (sgl.size() > 0) {
            int i = 0;
            SnoGrp sg = sgl.get(0);
            // show each of the non-Rels
            if (sg.size() > 0 && sg.get(0).group == 0) {
                for (SnoRel sr : sg) {
                    I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                    tmpTLabel = TermLabelMaker.newLabelForm(rTuple,
                            showDetailCB.isSelected(), showStatusCB
                                    .isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(rTuple);
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                }
                i++; // skip past 0 index of the "un-grouped"
            }

            // show each of the groups
            for (; i < sgl.size(); i++) {
                sg = sgl.get(i);
                if (sg.size() == 0)
                    continue;
                if (showGroupLabels) { // true shows one label per group
                    List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();
                    for (SnoRel sr : sg) {
                        grpTuple.add(new ThinRelTuple(sr.relVers, sr.relPart));
                    }
                    tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB
                            .isSelected(), showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(grpTuple.get(0));
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                } else { // if false, show 1 rel per label
                    for (SnoRel sr : sg) {
                        I_RelTuple rTuple = new ThinRelTuple(sr.relVers,
                                sr.relPart);
                        tmpTLabel = TermLabelMaker.newLabelForm(rTuple,
                                showDetailCB.isSelected(), showStatusCB
                                        .isSelected());
                        tLabelList.add((LabelForTuple) tmpTLabel);
                        tmpDeltaColor = relColorMap.get(rTuple);
                        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                        formJPanel.add(tmpTLabel.getLabel(), c);
                        c.gridy++;
                    }
                    c.gridy++;
                }
            }
        }
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        formJPanel.add(new JPanel(), c);
        formJPanel.setBorder(BorderFactory.createTitledBorder(label));

        return formJPanel;
    }

    /**
     * <b>Long Canonical Form</b><li>Most Proximate PRIMITIVE Supertypes (IS-A)</li>
     * 
     */
    public JPanel newFormLongJPanel(String label, I_Position p,
            I_ConfigAceFrame config,
            Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
            Map<I_DescriptionTuple, Color> desColorMap,
            Map<I_RelTuple, Color> relColorMap) throws IOException {
        JPanel formJPanel = newMinMaxJPanel();
        formJPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        Set<I_Position> posSet = new HashSet<I_Position>(1);
        posSet.add(p);

        List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.NORTHWEST;

        // SHOW SELF CONCEPT
        I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);
        I_ImplementActiveLabel tmpTLabel = TermLabelMaker.newLabel(cTuple,
                showDetailCB.isSelected(), showStatusCB.isSelected());
        tLabelList.add((LabelForTuple) tmpTLabel);
        Color tmpDeltaColor = conAttrColorMap.get(cTuple);
        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
        formJPanel.add(tmpTLabel.getLabel(), c);
        c.gridy++;

        // SHOW PROXIMAL ISAs -- as relationships
        List<I_RelTuple> relList = findIsaProximalPrim(theCBean, cClassPathPos);
        for (I_RelTuple rTuple : relList) {
            tmpTLabel = TermLabelMaker.newLabel(rTuple, showDetailCB
                    .isSelected(), showStatusCB.isSelected());
            tLabelList.add((LabelForTuple) tmpTLabel);
            tmpDeltaColor = relColorMap.get(rTuple);
            setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
            formJPanel.add(tmpTLabel.getLabel(), c);
            c.gridy++;
        }

        // SHOW ROLES, NON-REDUNDANT, DIFFERENTIATED FROM ROOT
        SnoGrpList sgl = findRoleDiffFromRoot(theCBean, cClassPathPos);
        if (sgl.size() > 0) {
            int i = 0;
            SnoGrp sg = sgl.get(0);
            // show each of the non-Rels
            if (sg.size() > 0 && sg.get(0).group == 0) {
                for (SnoRel sr : sg) {
                    I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                    tmpTLabel = TermLabelMaker.newLabelForm(rTuple,
                            showDetailCB.isSelected(), showStatusCB
                                    .isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(rTuple);
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                }
                i++; // skip past 0 index of the "un-grouped"
            }

            // show each of the groups
            for (; i < sgl.size(); i++) {
                sg = sgl.get(i);
                if (sg.size() == 0)
                    continue;
                if (showGroupLabels) { // set to true to show one label per
                    // group
                    List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();
                    for (SnoRel sr : sg) {
                        grpTuple.add(new ThinRelTuple(sr.relVers, sr.relPart));
                    }
                    tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB
                            .isSelected(), showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(grpTuple.get(0));
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                } else { // if false, show 1 relationship per label
                    for (SnoRel sr : sg) {
                        I_RelTuple rTuple = new ThinRelTuple(sr.relVers,
                                sr.relPart);
                        tmpTLabel = TermLabelMaker.newLabelForm(rTuple,
                                showDetailCB.isSelected(), showStatusCB
                                        .isSelected());
                        tLabelList.add((LabelForTuple) tmpTLabel);
                        tmpDeltaColor = relColorMap.get(rTuple);
                        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                        formJPanel.add(tmpTLabel.getLabel(), c);
                        c.gridy++;
                    }
                    c.gridy++;
                }
            }
        }

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        formJPanel.add(new JPanel(), c);
        formJPanel.setBorder(BorderFactory.createTitledBorder(label));

        return formJPanel;
    }

    /**
     * <b>Short Canonical Form</b><li>Most Proximate PRIMITIVE Supertypes (IS-A)
     * </li>
     */
    public JPanel newFormShortJPanel(String label, I_Position p,
            I_ConfigAceFrame config,
            Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
            Map<I_DescriptionTuple, Color> desColorMap,
            Map<I_RelTuple, Color> relColorMap) throws IOException {
        JPanel formJPanel = newMinMaxJPanel();
        formJPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        Set<I_Position> posSet = new HashSet<I_Position>(1);
        posSet.add(p);

        List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.NORTHWEST;

        // SHOW SELF CONCEPT
        I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);
        I_ImplementActiveLabel tmpTLabel = TermLabelMaker.newLabelForm(cTuple,
                showDetailCB.isSelected(), showStatusCB.isSelected());
        tLabelList.add((LabelForTuple) tmpTLabel);
        Color tmpDeltaColor = conAttrColorMap.get(cTuple);
        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
        formJPanel.add(tmpTLabel.getLabel(), c);
        c.gridy++;

        // SHOW PROXIMAL PRIMITIVE ISAs -- as relationships
        List<I_RelTuple> isaList = findIsaProximalPrim(theCBean, cClassPathPos);
        for (I_RelTuple t : isaList) {
            I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t,
                    showDetailCB.isSelected(), showStatusCB.isSelected());
            tLabelList.add((LabelForTuple) tLabel);
            Color deltaColor = relColorMap.get(t);
            setBorder(tLabel.getLabel(), deltaColor);
            formJPanel.add(tLabel.getLabel(), c);
            c.gridy++;
        }

        // SHOW ROLES
        SnoGrpList sgl = findRoleDiffFromProxPrim(theCBean, isaList,
                cClassPathPos);
        if (sgl.size() > 0) {
            int i = 0;
            SnoGrp sg = sgl.get(0);
            // show each of the non-Rels
            if (sg.size() > 0 && sg.get(0).group == 0) {
                for (SnoRel sr : sg) {
                    I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                    tmpTLabel = TermLabelMaker.newLabelForm(rTuple,
                            showDetailCB.isSelected(), showStatusCB
                                    .isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(rTuple);
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                }
                i++; // skip past 0 index of the "un-grouped"
            }

            // show each of the groups
            for (; i < sgl.size(); i++) {
                sg = sgl.get(i);
                if (sg.size() == 0)
                    continue;
                if (showGroupLabels) { // true shows one label per group
                    List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();
                    for (SnoRel sr : sg) {
                        grpTuple.add(new ThinRelTuple(sr.relVers, sr.relPart));
                    }
                    tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB
                            .isSelected(), showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(grpTuple.get(0));
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                } else { // if false, show 1 rel per label
                    for (SnoRel sr : sg) {
                        I_RelTuple rTuple = new ThinRelTuple(sr.relVers,
                                sr.relPart);
                        tmpTLabel = TermLabelMaker.newLabelForm(rTuple,
                                showDetailCB.isSelected(), showStatusCB
                                        .isSelected());
                        tLabelList.add((LabelForTuple) tmpTLabel);
                        tmpDeltaColor = relColorMap.get(rTuple);
                        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                        formJPanel.add(tmpTLabel.getLabel(), c);
                        c.gridy++;
                    }
                    c.gridy++;
                }
            }
        }

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        formJPanel.add(new JPanel(), c);
        formJPanel.setBorder(BorderFactory.createTitledBorder(label));

        return formJPanel;
    }

    public JPanel newFormStatedJPanel(String label, I_Position p,
            I_ConfigAceFrame config,
            Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
            Map<I_DescriptionTuple, Color> desColorMap,
            Map<I_RelTuple, Color> relColorMap) throws IOException {
        JPanel formJPanel = newMinMaxJPanel();
        formJPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        Set<I_Position> posSet = new HashSet<I_Position>(1);
        posSet.add(p);

        List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.NORTHWEST;

        // SHOW SELF CONCEPT
        I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);
        I_ImplementActiveLabel tmpTLabel = TermLabelMaker.newLabelForm(cTuple,
                showDetailCB.isSelected(), showStatusCB.isSelected());
        tLabelList.add((LabelForTuple) tmpTLabel);
        Color tmpDeltaColor = conAttrColorMap.get(cTuple);
        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
        formJPanel.add(tmpTLabel.getLabel(), c);
        c.gridy++;

        // SHOW PROXIMAL ISAs -- as relationships
        List<I_RelTuple> relList = findIsaProximal(theCBean, cEditPathPos);
        for (I_RelTuple rTuple : relList) {
            tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB
                    .isSelected(), showStatusCB.isSelected());
            tLabelList.add((LabelForTuple) tmpTLabel);
            tmpDeltaColor = relColorMap.get(rTuple);
            setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
            formJPanel.add(tmpTLabel.getLabel(), c);
            c.gridy++;
        }

        // GET IMMEDIATE PROXIMAL ROLES & SEPARATE INTO GROUPS
        List<I_RelTuple> roleRTProx = findRoleProximal(theCBean, cEditPathPos);
        SnoGrpList sgl = splitGrouped(roleRTProx);
        sgl.add(0, splitNonGrouped(roleRTProx)); // un-group to first position
        if (sgl.size() > 0) {
            int i = 0;
            SnoGrp sg = sgl.get(0);
            // show each of the non-Rels
            if (sg.size() > 0 && sg.get(0).group == 0) {
                for (SnoRel sr : sg) {
                    I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                    tmpTLabel = TermLabelMaker.newLabelForm(rTuple,
                            showDetailCB.isSelected(), showStatusCB
                                    .isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(rTuple);
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                }
                i++; // skip past 0 index of the "un-grouped"
            }

            // show each of the groups
            for (; i < sgl.size(); i++) {
                sg = sgl.get(i);
                if (sg.size() == 0)
                    continue;
                if (showGroupLabels) { // true shows one label per group
                    List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();
                    for (SnoRel sr : sg) {
                        grpTuple.add(new ThinRelTuple(sr.relVers, sr.relPart));
                    }
                    tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB
                            .isSelected(), showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(grpTuple.get(0));
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                } else { // if false, show 1 rel per label
                    for (SnoRel sr : sg) {
                        I_RelTuple rTuple = new ThinRelTuple(sr.relVers,
                                sr.relPart);
                        tmpTLabel = TermLabelMaker.newLabelForm(rTuple,
                                showDetailCB.isSelected(), showStatusCB
                                        .isSelected());
                        tLabelList.add((LabelForTuple) tmpTLabel);
                        tmpDeltaColor = relColorMap.get(rTuple);
                        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                        formJPanel.add(tmpTLabel.getLabel(), c);
                        c.gridy++;
                    }
                    c.gridy++;
                }
            }
        }

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        formJPanel.add(new JPanel(), c);
        formJPanel.setBorder(BorderFactory.createTitledBorder(label));
        return formJPanel;
    }

    public String statsToHtml() {
        StringBuilder html = new StringBuilder(256);

        html.append("<font face=\"monospace\">");
        html.append("<br>");
        html.append("<br>countFindIsaProxDuplPart=\t"
                + countFindIsaProxDuplPart);
        html.append("<br>countFindRoleProxDuplPart=\t"
                + countFindRoleProxDuplPart);
        html.append("<br>countFindSelfDuplPart=\t" + countFindSelfDuplPart);
        html.append("<br>countIsCDefinedDuplPart=\t" + countIsCDefinedDuplPart);
        html.append("<br>countFindIsaProxDuplPartGE2=\t"
                + countFindIsaProxDuplPartGE2);
        html.append("<br>countFindRoleProxDuplPartGE2=\t"
                + countFindRoleProxDuplPartGE2);
        html.append("<br>countFindSelfDuplPartGE2=\t"
                + countFindSelfDuplPartGE2);
        html.append("<br>countIsCDefinedDuplPartGE2=\t"
                + countIsCDefinedDuplPartGE2);
        return html.toString();
    }

    public String statsToString() {
        StringBuilder s = new StringBuilder(256);

        s.append("\r\n::: [LogicalFormsPanel]");
        s.append("\r\n:::");
        s.append("\r\n:::countFindIsaProxDuplPart=\t"
                + countFindIsaProxDuplPart);
        s.append("\r\n:::countFindRoleProxDuplPart=\t"
                + countFindRoleProxDuplPart);
        s.append("\r\n:::countFindSelfDuplPart=\t" + countFindSelfDuplPart);
        s.append("\r\n:::countIsCDefinedDuplPart=\t" + countIsCDefinedDuplPart);
        s.append("\r\n:::countFindIsaProxDuplPartGE2=\t"
                + countFindIsaProxDuplPartGE2);
        s.append("\r\n:::countFindRoleProxDuplPartGE2=\t"
                + countFindRoleProxDuplPartGE2);
        s.append("\r\n:::countFindSelfDuplPartGE2=\t"
                + countFindSelfDuplPartGE2);
        s.append("\r\n:::countIsCDefinedDuplPartGE2=\t"
                + countIsCDefinedDuplPartGE2);
        return s.toString();
    }

    private String toStringNid(int nid) {
        try {
            I_GetConceptData a = tf.getConcept(nid);
            a.getUids().iterator().next().toString();
            String s = nid + "\t" + a.getUids().iterator().next().toString()
                    + "\t" + a.getInitialText();
            return s;
        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    // Most verbose. Includes "everything" (except the nid native id!)
    // Everything: UUIDs, attrib., descr., source rels, images[], uncommitted[]
    private String toStringNidUAB(int nid) {
        try {
            I_GetConceptData a = tf.getConcept(nid);
            UniversalAceBean au = a.getUniversalAceBean();
            return au.toString();
        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    // Several UUIDs related to the immediate concept.
    private String toStringNidUAI(int nid) {
        try {
            I_IdVersioned idv = tf.getId(nid);
            UniversalAceIdentification uai = idv.getUniversal();
            return uai.toString();
        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void statsReset() {
        countFindIsaProxDuplPart = 0;
        countFindRoleProxDuplPart = 0;
        countFindSelfDuplPart = 0;
        countIsCDefinedDuplPart = 0;
        countFindIsaProxDuplPartGE2 = 0;
        countFindRoleProxDuplPartGE2 = 0;
        countFindSelfDuplPartGE2 = 0;
        countIsCDefinedDuplPartGE2 = 0;
    }

    private void debugToStringNidSet(Set<Integer> roleSet, Set<Integer> valueSet) {
        // Initial Text
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: DEBUG ROLE_TYPE NIDS");
        for (Integer i : roleSet)
            s.append("\r\n::: \t" + toStringNid(i.intValue()));
        AceLog.getAppLog().log(Level.INFO, s.toString());

        s = new StringBuilder();
        s.append("\r\n::: DEBUG ROLE_VALUE NIDS");
        for (Integer i : valueSet)
            s.append("\r\n::: \t" + toStringNid(i.intValue()));
        AceLog.getAppLog().log(Level.INFO, s.toString());

        if (false) {
            // Universal Bean
            s = new StringBuilder();
            s.append("\r\n::: DEBUG ROLE_TYPE UBEAN");
            for (Integer i : roleSet)
                s.append("\r\n::: \r\n" + toStringNidUAB(i.intValue()));
            AceLog.getAppLog().log(Level.INFO, s.toString());

            s = new StringBuilder();
            s.append("\r\n::: DEBUG ROLE_VALUE UBEAN");
            for (Integer i : valueSet)
                s.append("\r\n::: \r\n" + toStringNidUAB(i.intValue()));
            AceLog.getAppLog().log(Level.INFO, s.toString());
        }

        if (false) {
            // Universal ID
            s = new StringBuilder();
            s.append("\r\n::: DEBUG ROLE_TYPE UID");
            for (Integer i : roleSet)
                s.append("\r\n::: \r\n" + toStringNidUAI(i.intValue()));
            AceLog.getAppLog().log(Level.INFO, s.toString());

            s = new StringBuilder();
            s.append("\r\n::: DEBUG ROLE_VALUE UID");
            for (Integer i : valueSet)
                s.append("\r\n::: \r\n" + toStringNidUAI(i.intValue()));
            AceLog.getAppLog().log(Level.INFO, s.toString());
        }
    }

    private void debugUpdateSets(List<I_RelTuple> rtlist, Set<Integer> typeSet,
            Set<Integer> cid2Set) {
        for (I_RelTuple rt : rtlist) {
            typeSet.add(new Integer(rt.getTypeId()));
            cid2Set.add(new Integer(rt.getC2Id()));
        }
    }

}
