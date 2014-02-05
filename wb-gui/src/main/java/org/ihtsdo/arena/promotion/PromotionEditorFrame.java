/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.promotion;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.ConfigurationException;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins.HOST_ENUM;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.classifier.SnoRocketTabPanel;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.list.PromotionTerminologyTable;
import org.dwfa.ace.list.PromotionTerminologyTableModel;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.classify.SnorocketExTask;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.ComponentFrame;
import org.dwfa.bpa.util.OpenFramesWindowListener;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.arena.Arena;
import org.ihtsdo.helper.promote.TerminologyPromoterBI;
import org.ihtsdo.helper.query.ConceptDefinedChangedQuery;
import org.ihtsdo.helper.query.DescriptionChangedQuery;
import org.ihtsdo.helper.query.Query;
import org.ihtsdo.helper.query.QueryBuilderBI;
import org.ihtsdo.helper.query.RelationshipInferredChangedQuery;
import org.ihtsdo.helper.query.RelationshipStatedChangedQuery;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.PathCB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.ttk.preferences.gui.PanelLinkingPreferences.LINK_TYPE;

/**
 *
 * @author kec
 */
public class PromotionEditorFrame extends ComponentFrame implements PropertyChangeListener {

    private static final long serialVersionUID = 1L;
    private static final Color ivoryColor = new Color(0xFFFFF0);
    private static final Color titleColor = new Color(255, 213, 162);
    private static final Color paneColor = Color.gray;
    protected JMenu promotionMenu;
    private I_ConfigAceFrame mergeConfig = null;
    private I_ConfigAceFrame origConfig = null;
    private I_ConfigAceFrame sourceConfig = null;
    private I_ConfigAceFrame targetConfig = null;
    private JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private JTabbedPane conceptTabsPane = new JTabbedPane();
    private ConceptPanel c1Panel;
    private static TerminologyList batchConceptList = null;
    private static PromotionTerminologyTable promotionConceptTable = null;
    private ViewCoordinate mergeVc;
    private JButton stopButton = new JButton();
    private JPanel resultsPane = new JPanel();
    private JProgressBar progressBar;
    private NidBitSetBI descChange;
    private NidBitSetBI infChange;
    private NidBitSetBI statedChange;
    private NidBitSetBI allChange;
    private Arena mergeArena;
    private Arena sourceArena;
    private Arena targetArena;
    private ConceptChronicleBI mergePathConcept;
    private boolean classifierDone = true;
    private boolean reportDone = true;
    private boolean promoteDone = true;

    public PromotionEditorFrame(final AceFrameConfig origConfig,
            PathBI sourcePath, PathBI targetPath, PathBI mergePath) throws Exception {
        super(new String[]{}, null);
        batchConceptList = null;
        this.origConfig = origConfig;
        // Set the title for the frame
        setTitle(getNextFrameName());

        PositionBI sourcePos = Ts.get().newPosition(sourcePath,
                TimeHelper.getTimeFromString("latest", TimeHelper.getFileDateFormat()));
        this.sourceConfig = new PromotionSourceConfig(this, origConfig, sourcePos);
        PositionBI targetPos = Ts.get().newPosition(targetPath,
                TimeHelper.getTimeFromString("latest", TimeHelper.getFileDateFormat()));
        this.targetConfig = new PromotionTargetConfig(this, origConfig, targetPos);
        if (mergePath == null) {
            mergePath = createMergePath(sourcePath, origConfig);
        }
        mergePathConcept = Ts.get().getConcept(mergePath.getConceptNid());
        PositionBI mergePos = Ts.get().newPosition(mergePath,
                TimeHelper.getTimeFromString("latest", TimeHelper.getFileDateFormat()));
        this.mergeConfig = new PromotionConfig(this, origConfig, mergePos);

        this.mergeConfig.addPropertyChangeListener(this);

        this.mergeVc = this.mergeConfig.getViewCoordinate();

        // Set the position and size of frame
        setBounds(10, 10, 500, 500);

        // Add the panel and editor pane to the frame
        Container cp = getContentPane();

        // Setup Right Side
        cp.add(topSplit);
        createBottomComponent();

        descChange = Ts.get().getEmptyNidSet();
        statedChange = Ts.get().getEmptyNidSet();
        infChange = Ts.get().getEmptyNidSet();
        allChange = Ts.get().getEmptyNidSet();
        
        // Setup top
        if (batchConceptList == null) {
            TerminologyListModel batchListModel = new TerminologyListModel();
            NidBitSetItrBI allItr = allChange.iterator();
            while (allItr.next()) {
                batchListModel.addElement((I_GetConceptData) Ts.get().getConcept(allItr.nid()));
            }
            batchConceptList = new TerminologyList(batchListModel, true, true, mergeConfig);

            PromotionTerminologyTableModel model = new PromotionTerminologyTableModel(batchConceptList, descChange, statedChange,
                    infChange, mergeVc, mergePath.getConceptNid());
            promotionConceptTable = new PromotionTerminologyTable(model, mergeConfig);
            promotionConceptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            promotionConceptTable.getSelectionModel().addListSelectionListener(new SelectionListener());

            //add values from previous merge
            TerminologyListModel listModel = (TerminologyListModel) batchConceptList.getModel();
            Collection<? extends RefexVersionBI<?>> previousMerge = mergePathConcept.getRefsetMembersActive(mergeVc);
            if (!previousMerge.isEmpty()) {
                HashMap<Integer, Color> diffColor = new HashMap<>();
                Random random = new Random();
                for (RefexVersionBI member : previousMerge) {
                    ComponentChronicleBI<?> component = Ts.get().getComponent(member.getReferencedComponentNid());
                    if (component != null) {
                        if (DescriptionChronicleBI.class.isAssignableFrom(component.getClass())) {
                            descChange.setMember(component.getNid());
                        } else if (RelationshipChronicleBI.class.isAssignableFrom(component.getClass())) {
                            RelationshipChronicleBI r = (RelationshipChronicleBI) component;
                            RelationshipVersionBI version = r.getVersion(mergeConfig.getViewCoordinate().getViewCoordinateWithAllStatusValues());
                                int charNid = version.getCharacteristicNid();
                                if (charNid == SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID()) {
                                    infChange.setMember(component.getNid());
                                } else if (charNid == SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID()) {
                                    statedChange.setMember(component.getNid());
                                }
                        } else if (ConceptChronicleBI.class.isAssignableFrom(component.getClass())) {
                            statedChange.setMember(component.getNid());
                        }
                        //make colors
                        float hue = random.nextFloat();
                        float saturation = (random.nextInt(2000) + 1000) / 10000f;
                        float luminance = 0.9f;
                        Color color = Color.getHSBColor(hue, saturation, luminance);
                        diffColor.put(component.getNid(), color);
                        listModel.addElement((I_GetConceptData) Ts.get().getConcept(component.getConceptNid()));
                    }
  
                }
                mergeArena.getEditor().setDiffColor(diffColor);
                sourceArena.getEditor().setDiffColor(diffColor);
                targetArena.getEditor().setDiffColor(diffColor);
                model.setChangedDesc(descChange);
                model.setChangedInferred(infChange);
                model.setChangedStated(statedChange);
                allChange.or(descChange);
                allChange.or(statedChange);
            }
        }

        createTopComponent();

        topSplit.setTopComponent(resultsPane);
        topSplit.setBottomComponent(conceptTabsPane);
        topSplit.setDividerLocation(350);
        resultsPane.setMinimumSize(new Dimension(cp.getWidth(), 350));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mergeConfig.getConceptViewer(1).setTermComponent(null);
            }
        });
        addWindowListener(new AceWindowActionListener());
    }

    private void createTopComponent() {
        /* Setting up resultsPane */
        resultsPane.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        JSplitPane conceptList = createConceptListPanel();
        resultsPane.add(conceptList, gbc);
        resultsPane.setSize(conceptTabsPane.getWidth(), conceptList.getHeight());
    }

    private JSplitPane createConceptListPanel() {
        // Setup Label
        JLabel resultsTitle = new JLabel("Differences", JLabel.CENTER);
        PathBI sourcePath = sourceConfig.getViewCoordinate().getPositionSet().iterator().next().getPath();
        JLabel sourceLabel = new JLabel(sourcePath.toString(),
                JLabel.LEFT);
        Border outside = BorderFactory.createLineBorder(sourceConfig.getColorForPath(sourcePath.getConceptNid()), 2);
        Border inside = BorderFactory.createEmptyBorder(1, 2, 1, 2);
        Border border = BorderFactory.createCompoundBorder(outside, inside);
        sourceLabel.setBorder(border);

        PathBI targetPath = targetConfig.getViewCoordinate().getPositionSet().iterator().next().getPath();
        JLabel targetLabel = new JLabel(targetPath.toString(), JLabel.RIGHT);
        outside = BorderFactory.createLineBorder(targetConfig.getColorForPath(targetPath.getConceptNid()), 2);
        inside = BorderFactory.createEmptyBorder(1, 2, 1, 2);
        border = BorderFactory.createCompoundBorder(outside, inside);
        targetLabel.setBorder(border);

        JPanel resultsLabel = new JPanel();
        resultsLabel.add(sourceLabel);
        resultsLabel.add(resultsTitle);
        resultsLabel.add(targetLabel);
        resultsLabel.setBackground(titleColor);

        // Setup Concept List
        JScrollPane conceptList = new JScrollPane(promotionConceptTable);
        // Setup conceptListPanel
        JSplitPane conceptListPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, conceptList, resultsLabel);
        conceptListPane.setDividerSize(3);
        conceptListPane.setDividerLocation(310);
        conceptListPane.setEnabled(false);

        return conceptListPane;
    }

    private void createBottomComponent() throws NoSuchAlgorithmException, IOException, ClassNotFoundException, TerminologyException {
        /* Setting up conceptTabsPane */
        c1Panel = new ConceptPanel(HOST_ENUM.CONCEPT_PANEL_R1,
                mergeConfig, LINK_TYPE.UNLINKED,
                conceptTabsPane, 1, "plugins/contradiction");
        conceptTabsPane.add(c1Panel);
        SnoRocketTabPanel classifierPanel = new SnoRocketTabPanel(mergeConfig);
        conceptTabsPane.add("classifier", classifierPanel);
        mergeArena = new Arena(mergeConfig, new File("arena/1-up.mxe"), false, true);
        sourceArena = new Arena(sourceConfig, new File("arena/1-up.mxe"), false, true);
        targetArena = new Arena(targetConfig, new File("arena/1-up.mxe"), false, true);

        JPanel arenaHolder = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        arenaHolder.add(sourceArena, gbc);
        gbc.gridx++;
        arenaHolder.add(mergeArena, gbc);
        gbc.gridx++;
        arenaHolder.add(targetArena, gbc);
        conceptTabsPane.addTab("arena",
                new ImageIcon(ACE.class.getResource("/16x16/plain/eye.png")), arenaHolder);
        conceptTabsPane.setSelectedIndex(2);
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
        mainMenuBar.add(promotionMenu = new JMenu("Promotion"));
        promotionMenu.add(new ClassifyAction(this));
        promotionMenu.add(new QueryAction(this));
        promotionMenu.add(new PromoteSelectedWBAction(this));
        promotionMenu.add(new PromoteSelectedAction(this));
        promotionMenu.add(new PromoteAllWBAction(this));
        promotionMenu.add(new PromoteAllAction(this));
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getQuitMenu()
     */
    @Override
    public JMenu getQuitMenu() {
        return this.promotionMenu;
    }

    /**
     * @see
     * org.dwfa.bpa.util.I_InitComponentMenus#addInternalFrames(javax.swing.JMenu)
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
        String title = "Termingology Promoter";
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
            int selectedIndex = promotionConceptTable.getSelectedRow();
            PromotionTerminologyTableModel model = (PromotionTerminologyTableModel) promotionConceptTable.getModel();
            try {
                selectedIndex = promotionConceptTable.convertRowIndexToModel(selectedIndex);
                I_GetConceptData concept = (I_GetConceptData) model.getValueAt(selectedIndex, 0);
                c1Panel.setTermComponent(concept);
            } catch (IndexOutOfBoundsException e) {
//TODO: don't need to do anything, updating on its self
            }
        }
    }

    @Override
    public JMenuItem[] getNewWindowMenu() {
        return null;
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
        return mergeConfig;
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
            Terms.get().setActiveAceFrameConfig(mergeConfig);
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

    private class QueryAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        private PromotionEditorFrame frame;

        public QueryAction(PromotionEditorFrame promotionEditorFrame) {
            super("Run difference finder");

            this.frame = promotionEditorFrame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (classifierDone == true && reportDone == true && promoteDone == true) {
                final I_ShowActivity activity = Terms.get().newActivityPanel(true, mergeConfig, "Starting difference finder.", true);
                new Thread(
                        new Runnable() {
                    @Override
                    public void run() {
                        try {
                            reportDone = false;
                            PromotionTerminologyTableModel model = (PromotionTerminologyTableModel) promotionConceptTable.getModel();
                            //clear old first?
                            allChange.clear();
                            frame.query();
                            activity.setProgressInfoLower("compelete");
                            activity.complete();
                            I_ShowActivity activity1 = Terms.get().newActivityPanel(true, mergeConfig, "Finished query, processing results.", true);
                            model.setChangedDesc(descChange);
                            model.setChangedInferred(infChange);
                            model.setChangedStated(statedChange);
                            TerminologyListModel listModel = (TerminologyListModel) batchConceptList.getModel();
                            listModel.clear();
                            NidBitSetItrBI allItr = allChange.iterator();
                            HashMap<Integer, Color> diffColor = new HashMap<>();
                            Random random = new Random();
                            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(mergeConfig.getEditCoordinate(), mergeVc);
                            while (allItr.next()) {
                                ConceptChronicleBI concept = Ts.get().getConceptForNid(allItr.nid());
                                ComponentChronicleBI component = Ts.get().getComponent(allItr.nid());
                                //add component to promotion refset
                                RefexCAB promoteBp = new RefexCAB(TK_REFEX_TYPE.CID,
                                        component.getNid(), mergePathConcept.getNid());
                                promoteBp.put(RefexCAB.RefexProperty.CNID1, TermAux.UNREVIEWED.getLenient().getNid());
                                builder.construct(promoteBp);
                                //add to table
                                listModel.addElement((I_GetConceptData) concept);

                                //make colors
                                float hue = random.nextFloat();
                                float saturation = (random.nextInt(2000) + 1000) / 10000f;
                                float luminance = 0.9f;
                                Color color = Color.getHSBColor(hue, saturation, luminance);
                                diffColor.put(allItr.nid(), color);
                            }
                            Ts.get().addUncommittedNoChecks(mergePathConcept);
                            Ts.get().commit(mergePathConcept);
                            mergeArena.getEditor().setDiffColor(diffColor);
                            sourceArena.getEditor().setDiffColor(diffColor);
                            targetArena.getEditor().setDiffColor(diffColor);
                            model.fireTableDataChanged();
                            reportDone = true;
                            activity1.setProgressInfoLower("compelete");
                            activity1.complete();
                            I_ShowActivity activity2 = Terms.get().newActivityPanel(true, mergeConfig, "Difference finder complete", true);
                            activity2.setProgressInfoLower("compelete");
                            activity2.complete();
                        } catch (IOException ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ParseException ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }).start();
            } else if (classifierDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for classification to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            } else if (reportDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for difference finder to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            } else if (promoteDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for promotion to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private class PromoteAllWBAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        private PromotionEditorFrame frame;

        public PromoteAllWBAction(PromotionEditorFrame promotionEditorFrame) {
            super("Promote all - write back");

            this.frame = promotionEditorFrame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (classifierDone == true && reportDone == true && promoteDone == true) {
                new Thread(
                        new Runnable() {
                    @Override
                    public void run() {
                        try {
                            promoteDone = false;
                            frame.promote(allChange, true);

                            //run classifier
                            SnorocketExTask classifier = new SnorocketExTask();
                            classifier.runClassifier(origConfig);
                            classifier.commitClassification();
                            SnorocketExTask classifierMerge = new SnorocketExTask();
                            classifierMerge.runClassifier(mergeConfig);
                            classifierMerge.commitClassification();

                            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(mergeConfig.getEditCoordinate(), mergeVc);
                            for (RefexVersionBI m : mergePathConcept.getRefsetMembersActive(mergeVc)) {
                                RefexNidVersionBI member = (RefexNidVersionBI) m;
                                if (member.getNid1() == TermAux.PROMOTE.getLenient().getConceptNid()
                                        || member.getNid1() == TermAux.UNREVIEWED.getLenient().getConceptNid()) {
                                    RefexCAB memberBp = member.makeBlueprint(mergeVc);
                                    memberBp.put(RefexCAB.RefexProperty.CNID1, TermAux.PROMOTED.getLenient().getConceptNid());
                                    memberBp.setMemberUuid(member.getPrimUuid());
                                    builder.construct(memberBp);
                                }
                            }
                            Ts.get().addUncommitted(mergePathConcept);
                            Ts.get().commit(mergePathConcept);
                            PromotionTerminologyTableModel model = (PromotionTerminologyTableModel) promotionConceptTable.getModel();
                            model.fireTableDataChanged();
                            SnorocketExTask classifierTarget = new SnorocketExTask();
                            classifierTarget.runClassifier(targetConfig);
                            classifierTarget.commitClassification();
                            promoteDone = true;
                        } catch (IOException ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ParseException ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }).start();
            } else if (classifierDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for classification to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            } else if (reportDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for difference finder to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            } else if (promoteDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for promotion to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private class PromoteAllAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        private PromotionEditorFrame frame;

        public PromoteAllAction(PromotionEditorFrame promotionEditorFrame) {
            super("Promote all");

            this.frame = promotionEditorFrame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (classifierDone == true && reportDone == true && promoteDone == true) {
                new Thread(
                        new Runnable() {
                    @Override
                    public void run() {
                        try {
                            promoteDone = false;
                            NidBitSetBI promotionNids = allChange;
                            promotionNids.andNot(infChange);
                            frame.promote(promotionNids, false);

                            //run classifier
                            SnorocketExTask classifier = new SnorocketExTask();
                            classifier.runClassifier(origConfig);
                            classifier.commitClassification();
                            SnorocketExTask classifierMerge = new SnorocketExTask();
                            classifierMerge.runClassifier(mergeConfig);
                            classifierMerge.commitClassification();

                            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(mergeConfig.getEditCoordinate(), mergeVc);
                            for (RefexVersionBI m : mergePathConcept.getRefsetMembersActive(mergeVc)) {
                                RefexNidVersionBI member = (RefexNidVersionBI) m;
                                if (member.getNid1() == TermAux.PROMOTE.getLenient().getConceptNid()
                                        || member.getNid1() == TermAux.UNREVIEWED.getLenient().getConceptNid()) {
                                    RefexCAB memberBp = member.makeBlueprint(mergeVc);
                                    memberBp.put(RefexCAB.RefexProperty.CNID1, TermAux.PROMOTED.getLenient().getConceptNid());
                                    memberBp.setMemberUuid(member.getPrimUuid());
                                    builder.construct(memberBp);
                                }
                            }
                            Ts.get().addUncommitted(mergePathConcept);
                            Ts.get().commit(mergePathConcept);
                            PromotionTerminologyTableModel model = (PromotionTerminologyTableModel) promotionConceptTable.getModel();
                            model.fireTableDataChanged();
                            SnorocketExTask classifierTarget = new SnorocketExTask();
                            classifierTarget.runClassifier(targetConfig);
                            classifierTarget.commitClassification();
                            promoteDone = true;
                        } catch (IOException ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ParseException ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }).start();
            } else if (classifierDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for classification to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            } else if (reportDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for difference finder to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            } else if (promoteDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for promotion to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private class PromoteSelectedAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        private PromotionEditorFrame frame;

        public PromoteSelectedAction(PromotionEditorFrame promotionEditorFrame) {
            super("Promote selected");

            this.frame = promotionEditorFrame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (classifierDone == true && reportDone == true && promoteDone == true) {
                new Thread(
                        new Runnable() {
                    @Override
                    public void run() {
                        try {
                            promoteDone = false;
                            Collection<? extends RefexVersionBI<?>> members = mergePathConcept.getRefsetMembersActive(mergeVc);
                            NidBitSetBI nidsToPromote = Ts.get().getEmptyNidSet();
                            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(mergeConfig.getEditCoordinate(), mergeVc);
                            for (RefexVersionBI m : members) {
                                RefexNidVersionBI member = (RefexNidVersionBI) m;
                                if (member.getNid1() == TermAux.PROMOTE.getLenient().getConceptNid()) {
                                    RefexCAB memberBp = member.makeBlueprint(mergeVc);
                                    nidsToPromote.setMember(Ts.get().getConceptNidForNid(member.getReferencedComponentNid()));
                                    memberBp.put(RefexCAB.RefexProperty.CNID1, TermAux.PROMOTED.getLenient().getConceptNid());
                                    memberBp.setMemberUuid(member.getPrimUuid());
                                    builder.construct(memberBp);
                                }
                            }
                            Ts.get().addUncommitted(mergePathConcept);
                            Ts.get().commit(mergePathConcept);
                            frame.promote(nidsToPromote, false);

                            //run classifier
                            SnorocketExTask classifier = new SnorocketExTask();
                            classifier.runClassifier(origConfig);
                            classifier.commitClassification();
                            SnorocketExTask classifierMerge = new SnorocketExTask();
                            classifierMerge.runClassifier(mergeConfig);
                            classifierMerge.commitClassification();

                            PromotionTerminologyTableModel model = (PromotionTerminologyTableModel) promotionConceptTable.getModel();
                            model.fireTableDataChanged();
                            SnorocketExTask classifierTarget = new SnorocketExTask();
                            classifierTarget.runClassifier(targetConfig);
                            classifierTarget.commitClassification();
                            promoteDone = true;
                        } catch (IOException ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ParseException ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }).start();
            } else if (classifierDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for classification to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            } else if (reportDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for difference finder to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            } else if (promoteDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for promotion to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private class PromoteSelectedWBAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        private PromotionEditorFrame frame;

        public PromoteSelectedWBAction(PromotionEditorFrame promotionEditorFrame) {
            super("Promote selected - write back");

            this.frame = promotionEditorFrame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (classifierDone == true && reportDone == true && promoteDone == true) {
                new Thread(
                        new Runnable() {
                    @Override
                    public void run() {
                        try {
                            promoteDone = false;
                            Collection<? extends RefexVersionBI<?>> members = mergePathConcept.getRefsetMembersActive(mergeVc);
                            NidBitSetBI nidsToPromote = Ts.get().getEmptyNidSet();
                            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(mergeConfig.getEditCoordinate(), mergeVc);
                            for (RefexVersionBI m : members) {
                                RefexNidVersionBI member = (RefexNidVersionBI) m;
                                if (member.getNid1() == TermAux.PROMOTE.getLenient().getConceptNid()) {
                                    RefexCAB memberBp = member.makeBlueprint(mergeVc);
                                    nidsToPromote.setMember(Ts.get().getConceptNidForNid(member.getReferencedComponentNid()));
                                    memberBp.put(RefexCAB.RefexProperty.CNID1, TermAux.PROMOTED.getLenient().getConceptNid());
                                    memberBp.setMemberUuid(member.getPrimUuid());
                                    builder.construct(memberBp);
                                }
                            }
                            Ts.get().addUncommitted(mergePathConcept);
                            Ts.get().commit(mergePathConcept);
                            frame.promote(nidsToPromote, false);

                            //run classifier
                            SnorocketExTask classifier = new SnorocketExTask();
                            classifier.runClassifier(origConfig);
                            classifier.commitClassification();
                            SnorocketExTask classifierMerge = new SnorocketExTask();
                            classifierMerge.runClassifier(mergeConfig);
                            classifierMerge.commitClassification();

                            PromotionTerminologyTableModel model = (PromotionTerminologyTableModel) promotionConceptTable.getModel();
                            model.fireTableDataChanged();
                            SnorocketExTask classifierTarget = new SnorocketExTask();
                            classifierTarget.runClassifier(targetConfig);
                            classifierTarget.commitClassification();
                            promoteDone = true;
                        } catch (IOException ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ParseException ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }).start();
            } else if (classifierDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for classification to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            } else if (reportDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for difference finder to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            } else if (promoteDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for promotion to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private class ClassifyAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        private PromotionEditorFrame frame;

        public ClassifyAction(PromotionEditorFrame promotionEditorFrame) {
            super("Run Clasifier");

            this.frame = promotionEditorFrame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (classifierDone == true && reportDone == true && promoteDone == true) {
                new Thread(
                        new Runnable() {
                    @Override
                    public void run() {
                        try {
                            classifierDone = false;
                            SnorocketExTask classifier = new SnorocketExTask();
                            classifier.runClassifier(mergeConfig);
                            classifier.commitClassification();
                            classifierDone = true;
                        } catch (IOException ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ParseException ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(PromotionEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }).start();
            } else if (classifierDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for classification to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            } else if (reportDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for difference finder to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            } else if (promoteDone == false) {
                JOptionPane.showMessageDialog(null, "Please wait for promotion to finish.", "please wait", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void query() throws IOException, ParseException, Exception {
        ViewCoordinate vcSource = mergeConfig.getViewCoordinate();
        ViewCoordinate vcTarget = targetConfig.getViewCoordinate();

        //query: added description
        //query: retired description
        //query: added stated rel, retired stated rel
        //query: added inferred rel, retired inferred rel
        long startTime = System.currentTimeMillis();
        QueryBuilderBI builder = Ts.get().getQueryBuilder(mergeConfig.getViewCoordinate());
        Query q1 = new DescriptionChangedQuery(vcTarget, vcSource).getQuery();
        Query q2 = new RelationshipInferredChangedQuery(vcTarget, vcSource).getQuery();
        Query q3 = new RelationshipStatedChangedQuery(vcTarget, vcSource).getQuery();
        Query q4 = new ConceptDefinedChangedQuery(vcTarget, vcSource).getQuery();

        //results
        ArrayList<NidBitSetBI> results = builder.getResults(q1, q2, q3, q4);        
           
        int count = 0;
        for (NidBitSetBI result : results) {
            System.out.println(result);
            if (count == 0) {
                descChange = result;
                allChange.or(result);
            } else if (count == 1) {
                infChange = result;
                allChange.or(result);
            } else if (count == 2) {
                statedChange = result;
                allChange.or(result);
            } else if (count == 3) {
                //need to combine stated rel changes with concept defined change
                statedChange.or(result);
                allChange.or(result);
            }
            count++;
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        long minutes = elapsedTime / 60000;
        long seconds = (elapsedTime % 60000) / 1000;
        System.out.println("Query time: " + minutes + " minutes, " + seconds + " seconds.");
    }
//TODO: specify origin position    

    private void promote(NidBitSetBI nidsToPromote, boolean writeBack) throws IOException, Exception {
        long startTime = System.currentTimeMillis();
        TerminologyPromoterBI promoter = Ts.get().getTerminologyPromoter(sourceConfig.getViewCoordinate(),
                targetConfig.getEditCoordinate(),
                targetConfig.getViewCoordinate());
        promoter.promote(nidsToPromote, writeBack);
        long elapsedTime = System.currentTimeMillis() - startTime;
        long minutes = elapsedTime / 60000;
        long seconds = (elapsedTime % 60000) / 1000;
        System.out.println("Promotion time: " + minutes + " minutes, " + seconds + " seconds.");
    }

    private PathBI createMergePath(PathBI originPath, I_ConfigAceFrame origConfig) throws IOException, TerminologyException, InvalidCAB, ContradictionException, Exception {
        String pathName = "Merge Path " + TimeHelper.formatDate(System.currentTimeMillis());
        //make path concept
        UUID pathUUID = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, pathName);
        ConceptCB pathConceptBp = new ConceptCB(
                pathName,
                pathName,
                LANG_CODE.EN,
                ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid(),
                ArchitectonicAuxiliary.Concept.RELEASE.getPrimoridalUid());
        pathConceptBp.setComponentUuid(pathUUID);

        RefexCAB pathRefexBp = new RefexCAB(TK_REFEX_TYPE.CID,
                TermAux.PATH.getLenient().getConceptNid(),
                RefsetAux.PATH_REFSET.getLenient().getNid());
        pathRefexBp.put(RefexCAB.RefexProperty.UUID1, pathConceptBp.getComponentUuid());
        pathRefexBp.setMemberUuid(UUID.randomUUID());

        RefexCAB pathOriginRefexBp = new RefexCAB(TK_REFEX_TYPE.CID_INT,
                pathConceptBp.getComponentUuid(),
                RefsetAux.PATH_ORIGIN_REFEST.getLenient().getNid(), null, null);
        pathOriginRefexBp.put(RefexCAB.RefexProperty.UUID1, Ts.get().getUuidPrimordialForNid(originPath.getConceptNid()));
        //        pathOriginRefexBp.put(RefexCAB.RefexProperty.INTEGER1, Terms.get().convertToThinVersion(System.currentTimeMillis()));
        pathOriginRefexBp.put(RefexCAB.RefexProperty.INTEGER1, Integer.MAX_VALUE);
        pathRefexBp.setMemberUuid(UUID.randomUUID());

        PathCB pathBp = new PathCB(pathConceptBp,
                pathRefexBp,
                pathOriginRefexBp,
                Ts.get().getConcept(originPath.getConceptNid()));

        TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(origConfig.getEditCoordinate(), origConfig.getViewCoordinate());
        PathBI mergePath = builder.construct(pathBp);;

        return mergePath;
    }
}
