/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.dwfa.ace;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.naming.ConfigurationException;
import javax.security.auth.login.LoginException;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.dwfa.ace.CdePalette.TOGGLE_DIRECTION;
import org.dwfa.ace.actions.Abort;
import org.dwfa.ace.actions.ChangeFramePassword;
import org.dwfa.ace.actions.Commit;
import org.dwfa.ace.actions.SaveProfile;
import org.dwfa.ace.actions.SaveProfileAs;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.AceEditor;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ConfigAceFrame.CLASSIFIER_INPUT_MODE_PREF;
import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_HostConceptPlugins.HOST_ENUM;
import org.dwfa.ace.api.I_HostConceptPlugins.REFSET_TYPES;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.checks.UncommittedListModel;
import org.dwfa.ace.classifier.SnoRocketTabPanel;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.config.CreatePathPanel;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.gui.popup.ProcessPopupUtil;
import org.dwfa.ace.list.TerminologyIntList;
import org.dwfa.ace.list.TerminologyIntListModel;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.list.TerminologyTable;
import org.dwfa.ace.list.TerminologyTableModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.path.SelectPathAndPositionPanelWithCombo;
import org.dwfa.ace.queue.AddQueueListener;
import org.dwfa.ace.queue.NewQueueListener;
import org.dwfa.ace.refset.RefsetSpecEditor;
import org.dwfa.ace.refset.RefsetSpecPanel;
import org.dwfa.ace.search.SearchPanel;
import org.dwfa.ace.search.workflow.WorkflowHistorySearchPanel;
import org.dwfa.ace.table.refset.RefsetDefaults;
import org.dwfa.ace.table.refset.RefsetDefaultsConcept;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.commit.I_Fixup;
import org.dwfa.ace.task.commit.I_TestDataConstraints;
import org.dwfa.ace.task.gui.toptoggles.TopToggleTypes;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.ExecutionRecord;
import org.dwfa.bpa.gui.glue.PropertyListenerGlue;
import org.dwfa.bpa.gui.glue.PropertySetListenerGlue;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.util.I_DoQuitActions;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.queue.gui.QueueViewerPanel;
import org.dwfa.svn.Svn;
import org.dwfa.svn.SvnPanel;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.vodb.types.IntList;
import org.ihtsdo.arena.Arena;
import static org.ihtsdo.arena.conceptview.ConceptView.CONCEPT_REDRAW;
import static org.ihtsdo.arena.conceptview.DragPanelDescription.OK_TO_MOVE;
import static org.ihtsdo.arena.conceptview.DragPanelDescription.YEILD_FOCUS;
import org.ihtsdo.custom.statics.CustomStatics;
import org.ihtsdo.helper.descriptionlogic.DescriptionLogic;
import org.ihtsdo.objectCache.ObjectCache;
import org.ihtsdo.project.workflow.api.wf2.implementation.WorkflowStore;
import org.ihtsdo.taxonomy.TaxonomyHelper;
import org.ihtsdo.taxonomy.TaxonomyMouseListenerForAce;
import org.ihtsdo.taxonomy.TaxonomyTree;
import org.ihtsdo.taxonomy.model.NodeFactory;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.workflow.api.ProjectBI;
import org.ihtsdo.tk.workflow.api.WorkListBI;
import org.ihtsdo.tk.workflow.api.WorkflowStoreBI;
import org.ihtsdo.ttk.preferences.TtkPreferences;
import org.ihtsdo.ttk.preferences.gui.PanelLinkingPreferences;
import org.ihtsdo.ttk.preferences.gui.PanelLinkingPreferences.LINK_TYPE;
import org.ihtsdo.ttk.preferences.gui.QueueGuiPreferences;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class ACE extends JPanel implements PropertyChangeListener, I_DoQuitActions {
    public static final String DATA_CHECK_TAB_LABEL = "data checks";
    public static int          commitSequence       = 0;
    private static int         maxHistoryListSize   = 100;
    public static boolean      refsetOnly           = false;

    /**
     *
     */
    private static final long                serialVersionUID = 1L;
    private static String                    taxonomyTabLabel = "taxonomy";
    private static Set<? extends I_Transact> uncommitted      =
            Collections.synchronizedSet(new HashSet<I_Transact>());
    public static ExecutorService threadPool = Executors.newFixedThreadPool(Math.min(6,
            Runtime.getRuntime().availableProcessors()
            + 1), new NamedThreadFactory(new ThreadGroup("ACE "),
                    "GUI Background "));
    private static Timer                                                           swingTimer           =
            new Timer(500, null);
    private static boolean                                                         runShutdownProcesses = true;
    private static LinkedList<I_Transact>                                          imported             = new LinkedList<I_Transact>();
    public static boolean                                                          editMode             = true;
    private static Map<I_GetConceptData, Collection<AlertToDataConstraintFailure>> dataCheckMap         =
            new HashMap<I_GetConceptData, Collection<AlertToDataConstraintFailure>>();
    private static Set<I_WriteChangeSet>       csWriters     = new HashSet<I_WriteChangeSet>();
    private static Set<I_ReadChangeSet>        csReaders     = new HashSet<I_ReadChangeSet>();
    private static List<I_TestDataConstraints> creationTests = new ArrayList<I_TestDataConstraints>();
    private static List<I_TestDataConstraints> commitTests   = new ArrayList<I_TestDataConstraints>();
    public static AceConfig                    aceConfig;
    private static JButton                     synchronizeButton;

    //~--- static initializers -------------------------------------------------

    static {
        swingTimer.start();
        System.setProperty("yeildFocus", "true");
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addVetoableChangeListener("focusOwner", new VetoableChangeListener() {

            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                if(evt.getNewValue() == null){
//                    System.out.println("DEBUG -- New component is null, should yeild focus? " + System.getProperty(YEILD_FOCUS));
                }
                if (System.getProperty(YEILD_FOCUS) != null) {
                    if (System.getProperty(YEILD_FOCUS).equals(Boolean.FALSE.toString())) {
//                        System.out.println("DEBUG -- Vetoing change in focus");
//                        System.out.println("DEBUG -- Old: " + evt.getOldValue() + " New: " + evt.getNewValue() + " Source: " + evt.getSource());
                        throw new PropertyVetoException("Vetoed focus change.", evt);
                    }
                }
            }
        });
        
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            public void eventDispatched(AWTEvent event) {
                if (event instanceof MouseEvent) {
                    MouseEvent evt = (MouseEvent) event;
                    if (evt.getID() == MouseEvent.MOUSE_CLICKED) {
                        //clicking to another text area
                        if (editingComponent != null) {
                            if (!editingComponent.contains(evt.getPoint())) {
                                editingComponent.putClientProperty(OK_TO_MOVE, true);
//                                System.out.println("DEBUG -- 1 - OK to move: " + editingComponent.getClientProperty(OK_TO_MOVE) + " " + editingComponent.getText());
                                System.setProperty(YEILD_FOCUS, Boolean.TRUE.toString());
//                                System.out.println("DEBUG -- System yeilding focus.");
                            } else if (evt.getSource() instanceof JTextArea) {
                                JTextArea clickedText = (JTextArea) evt.getSource();
                                if (clickedText.getName() != null
                                        && clickedText != editingComponent) {
                                    editingComponent.putClientProperty(OK_TO_MOVE, true);
//                                    System.out.println("DEBUG -- 1 - OK to move: " + editingComponent.getClientProperty(OK_TO_MOVE) + " " + editingComponent.getText());
                                    System.setProperty(YEILD_FOCUS, Boolean.TRUE.toString());
                                    clickedText.requestFocusInWindow();
//                                    System.out.println("DEBUG -- System yeilding focus.");
                                }
                            }
                        }
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
        System.setProperty(CONCEPT_REDRAW, Boolean.FALSE.toString());
    }

    //~--- fields --------------------------------------------------------------

    private List<TermComponentDataCheckSelectionListener> dataCheckListeners =
            new ArrayList<TermComponentDataCheckSelectionListener>();
    int                                    refsetTabIndex              = -2;
    private JLabel                         statusLabel                 = new JLabel();
    private JTabbedPane                    leftTabs                    = new MemoriousJTabbedPane();
    public JTabbedPane                     conceptTabs                 = new MemoriousJTabbedPane();
    public JSplitPane                      upperLowerSplit             =
            new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    public JSplitPane                      termTreeConceptSplit        =
            new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private boolean                        showWorkflowInSignpostPanel = false;
    private TogglePanelsActionListener     resizeListener              = new TogglePanelsActionListener();
    private ManageBottomPaneActionListener bottomPanelActionListener   = new ManageBottomPaneActionListener();
    private TerminologyListModel           viewerHistoryTableModel     = new TerminologyListModel();
    boolean                                svnPositionSet              = false;
    private JPanel                         signpostPanel               = new JPanel();
    private TerminologyListModel           favoritesTableModel         = new TerminologyListModel();
    public AceFrameConfig                  aceFrameConfig;
    private JPanel                         activityPanel;
    private JButton                        addExistingInboxButton;
    protected JMenuItem                    addQueueMI, moveToDiskMI;
    private JList                          addressList;
    private CdePalette                     addressPalette;
    private AddressPaletteActionListener   apal;
    private Arena                          arena;
    private TerminologyList                batchConceptList;
    private TerminologyTable               batchConceptTable;
    private ConceptPanel                   c1Panel;
    private ConceptPanel                   c2Panel;
    private JButton                        cancelButton;
    private JButton                        commitButton;
    CollectionEditorContainer              conceptListEditor;
    private ArrayList<ConceptPanel>        conceptPanels;
    private UncommittedListModel           dataCheckListModel;
    private JPanel                         dataCheckListPanel;
    private JScrollPane                    dataCheckListScroller;
    private ConceptPanel                   dataCheckPanel;
    private JMenu                          fileMenu;
    private JFrame                         frame;
    private CdePalette                     historyPalette;
    private HistoryPaletteActionListener   hpal;
    private JPanel                         masterProcessBuilderPanel;
    private MasterWorker                   menuWorker;
    private JButton                        moveListenerButton;
    private JButton                        newInboxButton;
    protected JMenuItem                    newProcessMI, readProcessMI, takeProcessNoTranMI, takeProcessTranMI,
    saveProcessMI, saveForLauncherQueueMI, saveAsXmlMI;
    private Integer                          offset;
    private String                           pluginRoot;
    private PreferencesPaletteActionListener preferencesActionListener;
    private CdePalette                       preferencesPalette;
    private JTabbedPane                      preferencesTab;
    private CdePalette                       processPalette;
    private CdePalette                       queuePalette;
    QueueViewerPanel                         queueViewer;
    private RefsetSpecPanel                  refsetSpecPanel;
    public SearchPanel                       searchPanel;
    private JToggleButton                    showAddressesButton;
    private JToggleButton                    showAllQueuesButton;
    private JToggleButton                    showComponentButton;
    private JToggleButton                    showHistoryButton;
    private JToggleButton                    showPreferencesButton;
    private JToggleButton                    showProcessBuilder;
    private ProcessPaletteActionListener     showProcessBuilderActionListener;
    private JButton                          showProgressButton;
    private QueuesPaletteActionListener      showQueuesActionListener;
    private JToggleButton                    showQueuesButton;
    private JToggleButton                    showSearchToggle;
    private JToggleButton                    showSignpostPanelToggle;
    private JToggleButton                    showSubversionButton;
    private JToggleButton                    showTreeButton;
    private SnoRocketTabPanel                snoRocketPanel;
    javax.swing.Timer                        startupTimer;
    private CdePalette                       subversionPalette;
    private JTabbedPane                      svnTabs;
    public JComponent                        termTree;
    private ActivityPanel                    topActivity;
    private JPanel                           topPanel;
    public TaxonomyHelper                    treeHelper;
    private WorkflowHistorySearchPanel       wfSearchPanel;
    private JPanel                           workflowDetailsSheet;
    private JPanel                           workflowPanel;
    private JComboBox worklistCCSelectCombo;
    private WorkflowStoreBI wfStore;
    private static AtomicBoolean active = new AtomicBoolean(true);
    private static ArrayList<WeakReference<ListenForDataChecks>> dataChecks = new ArrayList<>();
    public static PanelLinkingPreferences linkPref;
    public static JTextArea editingComponent;

    //~--- constructors --------------------------------------------------------

    public ACE() {
        this("plugins");
    }

    /**
     * http://java.sun.com/developer/JDCTechTips/2003/tt1210.html#2
     *
     * @param aceFrameConfig
     * @throws PrivilegedActionException
     * @throws IOException
     * @throws ConfigurationException
     * @throws LoginException
     * @throws DatabaseException
     *
     * @throws DatabaseException
     */
    public ACE(String pluginRoot) {
        super(new GridBagLayout());
        this.pluginRoot = pluginRoot;
        linkPref =  new PanelLinkingPreferences(TtkPreferences.get());
        try {
            menuWorker = new MasterWorker(UUID.randomUUID(), "ACE MasterWorker");


            // only initialize these once as they are static lists...
            if (commitTests.isEmpty()) {
                loadTests("commit", commitTests);
            }

            if (creationTests.isEmpty()) {
                loadTests("precommit", creationTests);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.addComponentListener(new ResizeComponentAdaptor());
    }

    //~--- methods -------------------------------------------------------------

    private static JButton addActionButton(ActionListener actionListener, String resource, String tooltipText,
            JPanel topPanel, GridBagConstraints c) {
        JButton newProcess = new JButton(new ImageIcon(ACE.class.getResource(resource)));

        newProcess.setToolTipText(tooltipText);
        newProcess.addActionListener(actionListener);
        topPanel.add(newProcess, c);
        c.gridx++;

        return newProcess;
    }

    private static JToggleButton addActionToggleButton(ActionListener actionListener, String resource,
            String tooltipText, JPanel topPanel, GridBagConstraints c, int size) {
        JToggleButton newProcess;

        switch (size) {
        case 24 :
            newProcess = new JToggleButton(new ImageIcon(ACE.class.getResource(resource)));

            break;

        case 32 :
            newProcess = new JToggleButton(new ImageIcon(ACE.class.getResource(resource)));

            break;

        case 48 :
            newProcess = new JToggleButton(new ImageIcon(ACE.class.getResource(resource)));

            break;

        default :
            newProcess = new JToggleButton(new ImageIcon(ACE.class.getResource(resource)));

            break;
        }

        newProcess.setToolTipText(tooltipText);
        newProcess.addActionListener(actionListener);
        topPanel.add(newProcess, c);
        c.gridx++;

        return newProcess;
    }

    public void addDataCheckListener(TermComponentDataCheckSelectionListener l) {
        dataCheckListeners.add(l);
    }

    private void addDefaults(JTabbedPane editDefaultsTabs, RefsetDefaults defaults, REFSET_TYPES type) {

        // Start with defaults for all refsets...
        // Default refset
        JPanel             refsetsDefault = new JPanel(new GridLayout(0, 1));
                TermComponentLabel defaultRefset  = new TermComponentLabel(aceFrameConfig);

                defaultRefset.setTermComponent(defaults.getDefaultRefset());
                gluePreferenceLabel(defaults, "defaultRefset", defaultRefset);
                wrapAndAdd(refsetsDefault, defaultRefset, "Default refset: ");

                // Status
                TermComponentLabel defaultStatus = new TermComponentLabel(aceFrameConfig);

                defaultStatus.setTermComponent(defaults.getDefaultStatusForRefset());
                gluePreferenceLabel(defaults, "defaultStatus", defaultStatus);
                wrapAndAdd(refsetsDefault, defaultStatus, "Default status: ");

                switch (type) {
                case BOOLEAN :

                    // @todo
                    break;

                case CONCEPT :
                    TermComponentLabel defaultForConceptRefset = new TermComponentLabel(aceFrameConfig);

                    defaultForConceptRefset.setTermComponent(
                            ((RefsetDefaultsConcept) defaults).getDefaultForConceptRefset());
                    gluePreferenceLabel(defaults, "defaultForConceptRefset", defaultForConceptRefset);
                    wrapAndAdd(refsetsDefault, defaultForConceptRefset, "Default concept: ");

                    break;

                case CON_INT :
                    TermComponentLabel defaultForConIntRefset = new TermComponentLabel(aceFrameConfig);

                    defaultForConIntRefset.setTermComponent(
                            ((RefsetDefaultsConcept) defaults).getDefaultForConceptRefset());
                    gluePreferenceLabel(defaults, "defaultForConIntRefset", defaultForConIntRefset);
                    wrapAndAdd(refsetsDefault, defaultForConIntRefset, "Default concept: ");

                    break;

                case INTEGER :

                    // @todo
                    break;

                case STRING :

                    // @todo string
                    break;

                default :
                    break;
                }

                editDefaultsTabs.addTab("defaults", refsetsDefault);

                // add standard popups...
                editDefaultsTabs.addTab("refsets",
                        new JScrollPane(makePopupConfigPanel(defaults.getRefsetPopupIds(),
                                "Refsets for popup:")));
                editDefaultsTabs.addTab("status types",
                        new JScrollPane(makePopupConfigPanel(defaults.getStatusPopupIds(),
                                "Status for popup:")));
    }

    private void addEditMenu(JMenuBar menuBar, JMenu editMenu, JFrame aceFrame) {
        this.frame = aceFrame;
        editMenu.removeAll();

        JMenuItem menuItem;

        editMenu.setMnemonic(KeyEvent.VK_E);

        TransferActionListener actionListener = new TransferActionListener();

        menuItem = new JMenuItem("Cut");
        menuItem.setActionCommand((String) TransferHandler.getCutAction().getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.setMnemonic(KeyEvent.VK_T);
        editMenu.add(menuItem);
        menuItem = new JMenuItem("Copy");
        menuItem.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.setMnemonic(KeyEvent.VK_C);
        editMenu.add(menuItem);
        menuItem = new JMenuItem("Paste");
        menuItem.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.setMnemonic(KeyEvent.VK_P);
        editMenu.add(menuItem);
        editMenu.addSeparator();
        menuItem = new JMenuItem("Copy XML");
        menuItem.setActionCommand("Copy XML");
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                + java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        editMenu.add(menuItem);
        menuItem = new JMenuItem("Copy Tab Delimited Text");
        menuItem.setActionCommand("Copy TDT");
        menuItem.addActionListener(actionListener);
        editMenu.add(menuItem);
        menuItem = new JMenuItem("Copy SCT ID");
        menuItem.setActionCommand("Copy SCT ID");
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                + java.awt.event.InputEvent.ALT_DOWN_MASK));
        editMenu.add(menuItem);
        menuBar.add(editMenu);
    }

    private void addFileMenu(JMenuBar menuBar)
            throws LoginException, IOException, PrivilegedActionException,
            SecurityException, IntrospectionException, InvocationTargetException,
            IllegalAccessException, PropertyVetoException, ClassNotFoundException,
            NoSuchMethodException {
        JMenuItem menuItem = null;

        if (editMode) {

            /*
             * menuItem = new JMenuItem("Export Baseline Jar...");
             * menuItem.addActionListener(new WriteJar(aceConfig));
             * fileMenu.add(menuItem); fileMenu.addSeparator();
             * menuItem = new JMenuItem("Import Java Changeset...");
             * menuItem.addActionListener(new ImportJavaChangeset(config, aceFrame, aceConfig));
             * fileMenu.add(menuItem);
             * fileMenu.addSeparator();
             * menuItem = new JMenuItem("Test Tuple Calculator...");
             * menuItem.addActionListener(new TestTupleCalculator(config, aceFrame, aceConfig));
             * fileMenu.add(menuItem);
             * fileMenu.addSeparator();
             */

            /*
             * menuItem = new JMenuItem("Import Changeset Jar...");
             * menuItem.addActionListener(new ImportChangesetJar(config));
             * fileMenu.add(menuItem); menuItem = new
             * JMenuItem("Import Baseline Jar...");
             * menuItem.addActionListener(new ImportBaselineJar(config));
             * fileMenu.add(menuItem); fileMenu.addSeparator();
             */
            menuItem = new JMenuItem("Change Password...");
            menuItem.addActionListener(new ChangeFramePassword(this));
            fileMenu.add(menuItem);
            menuItem = new JMenuItem("Save Profile");
            menuItem.addActionListener(new SaveProfile(this.frame));
            fileMenu.add(menuItem);
            menuItem = new JMenuItem("Save Profile As...");
            menuItem.addActionListener(new SaveProfileAs(this.frame));
            fileMenu.add(menuItem);
        }
    }

    public static void addImported(I_Transact to) {
        imported.addLast(to);

        while (imported.size() > maxHistoryListSize) {
            imported.removeFirst();
        }

        if (aceConfig != null) {
            for (I_ConfigAceFrame frameConfig : getAceConfig().aceFrames) {
                frameConfig.setCommitEnabled(true);

                if (I_GetConceptData.class.isAssignableFrom(to.getClass())) {
                    frameConfig.addImported((I_GetConceptData) to);
                }
            }
        }
    }

    private void addProcessMenuItems(JMenuBar menuBar, File menuDir)
            throws IOException, FileNotFoundException, ClassNotFoundException {
        ProcessPopupUtil.addProcessMenuItems(menuBar, menuDir, menuWorker);
    }

    public void addProcessMenus(JMenuBar menuBar)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        File menuDir = new File(pluginRoot + File.separator + "menu");

        if (menuDir.listFiles() != null) {
            addProcessMenuItems(menuBar, menuDir);
        }
    }

    public void addSearchLinkedComponent(I_ContainTermComponent component) {
        searchPanel.addLinkedComponent(component);

        if (wfSearchPanel != null) {
            wfSearchPanel.addLinkedComponent(component);
        }
    }

    public void addTaxonomySelectionListener(TermComponentTreeSelectionListener treeListener) {
        treeHelper.addTreeSelectionListener(treeListener);
    }

    public JMenuBar addToMenuBar(JMenuBar menuBar, JMenu editMenu, JFrame aceFrame)
            throws LoginException, SecurityException, IOException,
            PrivilegedActionException, IntrospectionException, InvocationTargetException,
            IllegalAccessException, PropertyVetoException, ClassNotFoundException,
            NoSuchMethodException {
        this.frame = aceFrame;

        if (fileMenu == null) {
            fileMenu = new JMenu("File");
            menuBar.add(fileMenu, 0);
        }

        addFileMenu(menuBar);
        addEditMenu(menuBar, editMenu, aceFrame);
        ProcessPopupUtil.addProcessMenus(menuBar, pluginRoot, menuWorker);

        return menuBar;
    }

    public JMenuBar createMenuBar(JFrame frame)
            throws LoginException, SecurityException, IOException,
            PrivilegedActionException, IntrospectionException, InvocationTargetException,
            IllegalAccessException, PropertyVetoException, ClassNotFoundException,
            NoSuchMethodException {
        this.frame = frame;

        JMenuBar menuBar = new JMenuBar();

        if (fileMenu == null) {
            fileMenu = new JMenu("File");
            menuBar.add(fileMenu);
        }

        JMenu editMenu = new JMenu("Edit");

        menuBar.add(editMenu);
        addToMenuBar(menuBar, editMenu, frame);

        return menuBar;
    }

    public void deselectOthers(JToggleButton selectedOne) {
        AceLog.getAppLog().info("Deselecting others");

        if (showPreferencesButton != selectedOne) {
            if (showPreferencesButton.isSelected()) {
                showPreferencesButton.doClick();
            }
        }

        if (showSubversionButton != selectedOne) {
            if (showSubversionButton.isSelected()) {
                showSubversionButton.doClick();
            }
        }

        if (showQueuesButton != selectedOne) {
            if (showQueuesButton.isSelected()) {
                showQueuesButton.doClick();
            }
        }

        if (showProcessBuilder != selectedOne) {
            if (showProcessBuilder.isSelected()) {
                showProcessBuilder.doClick();
            }
        }
    }

    private static void doUpdate(I_ConfigAceFrame frameConfig) {
        try {
            if (((AceFrameConfig) frameConfig).getAceFrame() != null) {
                ACE aceInstance = ((AceFrameConfig) frameConfig).getAceFrame().getCdePanel();

                aceInstance.getDataCheckListScroller();
                aceInstance.getUncommittedListModel().clear();

                for (Collection<AlertToDataConstraintFailure> alerts : dataCheckMap.values()) {
                    aceInstance.getUncommittedListModel().addAll(alerts);
                }

                if (aceInstance.getUncommittedListModel().size() > 0) {
                    for (int i = 0; i < aceInstance.leftTabs.getTabCount(); i++) {
                        if (aceInstance.leftTabs.getTitleAt(i).equals(DATA_CHECK_TAB_LABEL)) {
                            aceInstance.leftTabs.setSelectedIndex(i);

                            break;
                        }
                    }

                    // show data checks tab...
                } else {
                    for (TermComponentDataCheckSelectionListener l : aceInstance.dataCheckListeners) {
                        l.setSelection(null);
                    }

                    // hide data checks tab...
                }
            }
        } catch (Exception e) {
            AceLog.getAppLog().warning(e.toString());
        }
    }

    private void executeShutdownProcesses(File shutdownFolder) {
        if (shutdownFolder.exists()) {
            AceLog.getAppLog().info("Shutdown folder exists: " + shutdownFolder.getAbsolutePath());

            File[] startupFiles = shutdownFolder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".bp");
                }
            });

            if (startupFiles != null) {
                for (int i = 0; i < startupFiles.length; i++) {
                    try {
                        AceLog.getAppLog().info("Executing shutdown business process: " + startupFiles[i]);

                        FileInputStream         fis     = new FileInputStream(startupFiles[i]);
                        BufferedInputStream     bis     = new BufferedInputStream(fis);
                        ObjectInputStream       ois     = new ObjectInputStream(bis);
                        I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois.readObject();

                        aceFrameConfig.getWorker().execute(process);
                        AceLog.getAppLog().info("Finished shutdown business process: " + startupFiles[i]);
                    } catch (Throwable e1) {
                        AceLog.getAppLog().alertAndLog(Level.SEVERE,
                                e1.getMessage() + " thrown by " + startupFiles[i], e1);
                    }
                }
            } else {
                AceLog.getAppLog().info("No shutdown processes found. Folder exists: " + shutdownFolder.exists());
            }
        } else {
            AceLog.getAppLog().info("No shutdown folder exists: " + shutdownFolder.getAbsolutePath());
        }
    }

    private void gluePreferenceLabel(RefsetDefaults defaults, String propertyName,
            TermComponentLabel labelToGlue) {
        defaults.addPropertyChangeListener(propertyName,
                new PropertyListenerGlue("setTermComponent",
                        I_AmTermComponent.class, labelToGlue));
        labelToGlue.addPropertyChangeListener("termComponent",
                new PropertyListenerGlue("set" + propertyName.toUpperCase().charAt(0)
                        + propertyName.substring(1), I_GetConceptData.class, defaults));
    }

    private void loadTests(String directory, List<I_TestDataConstraints> list) {
        File   componentPluginDir = new File(getPluginRoot() + File.separator + directory);
        File[] plugins            = componentPluginDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File arg0, String fileName) {
                return fileName.toLowerCase().endsWith(".task");
            }
        });

        if (plugins != null) {
            for (File f : plugins) {
                try {
                    FileInputStream       fis  = new FileInputStream(f);
                    BufferedInputStream   bis  = new BufferedInputStream(fis);
                    ObjectInputStream     ois  = new ObjectInputStream(bis);
                    I_TestDataConstraints test = (I_TestDataConstraints) ois.readObject();

                    ois.close();
                    list.add(test);
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLog(Level.WARNING, "Processing: " + f.getAbsolutePath(), e);
                }
            }
        }
    }

    private JComponent madeDefaultsPanel() {
        JPanel             defaultsPanel = new JPanel(new GridLayout(0, 1));
        TermComponentLabel defaultStatus = new TermComponentLabel(aceFrameConfig);

        defaultStatus.setTermComponent(aceFrameConfig.getDefaultStatus());
        aceFrameConfig.addPropertyChangeListener("defaultStatus",
                new PropertyListenerGlue("setTermComponent", I_AmTermComponent.class, defaultStatus));
        defaultStatus.addTermChangeListener(new PropertyListenerGlue("setDefaultStatus",
                I_GetConceptData.class, aceFrameConfig));
        wrapAndAdd(defaultsPanel, defaultStatus, "Default status: ");

        TermComponentLabel defaultImageType = new TermComponentLabel(aceFrameConfig);

        defaultImageType.setTermComponent(aceFrameConfig.getDefaultImageType());
        aceFrameConfig.addPropertyChangeListener("defaultImageType",
                new PropertyListenerGlue("setTermComponent", I_AmTermComponent.class, defaultImageType));
        defaultImageType.addTermChangeListener(new PropertyListenerGlue("setDefaultImageType",
                I_GetConceptData.class, aceFrameConfig));
        wrapAndAdd(defaultsPanel, defaultImageType, "Default image type: ");

        TermComponentLabel defaultDescType = new TermComponentLabel(aceFrameConfig);

        defaultDescType.setTermComponent(aceFrameConfig.getDefaultDescriptionType());
        aceFrameConfig.addPropertyChangeListener("defaultDescriptionType",
                new PropertyListenerGlue("setTermComponent", I_AmTermComponent.class, defaultDescType));
        defaultDescType.addTermChangeListener(new PropertyListenerGlue("setDefaultDescriptionType",
                I_GetConceptData.class, aceFrameConfig));
        wrapAndAdd(defaultsPanel, defaultDescType, "Default description type: ");

        TermComponentLabel defaultRelType = new TermComponentLabel(aceFrameConfig);

        defaultRelType.setTermComponent(aceFrameConfig.getDefaultRelationshipType());
        aceFrameConfig.addPropertyChangeListener("defaultRelationshipType",
                new PropertyListenerGlue("setTermComponent", I_AmTermComponent.class, defaultRelType));
        defaultRelType.addTermChangeListener(new PropertyListenerGlue("setDefaultRelationshipType",
                I_GetConceptData.class, aceFrameConfig));
        wrapAndAdd(defaultsPanel, defaultRelType, "Default relationship type: ");

        TermComponentLabel defaultRelCharacteristicType = new TermComponentLabel(aceFrameConfig);

        defaultRelCharacteristicType.setTermComponent(aceFrameConfig.getDefaultRelationshipCharacteristic());
        aceFrameConfig.addPropertyChangeListener("defaultRelationshipCharacteristic",
                new PropertyListenerGlue("setTermComponent", I_AmTermComponent.class,
                        defaultRelCharacteristicType));
        defaultRelCharacteristicType.addTermChangeListener(
                new PropertyListenerGlue(
                        "setDefaultRelationshipCharacteristic", I_GetConceptData.class, aceFrameConfig));
        wrapAndAdd(defaultsPanel, defaultRelCharacteristicType, "Default relationship characteristic: ");

        TermComponentLabel defaultRelRefinability = new TermComponentLabel(aceFrameConfig);

        defaultRelRefinability.setTermComponent(aceFrameConfig.getDefaultRelationshipRefinability());
        aceFrameConfig.addPropertyChangeListener("defaultRelationshipRefinability",
                new PropertyListenerGlue("setTermComponent", I_AmTermComponent.class, defaultRelRefinability));
        defaultRelRefinability.addTermChangeListener(
                new PropertyListenerGlue(
                        "setDefaultRelationshipRefinability", I_GetConceptData.class, aceFrameConfig));
        wrapAndAdd(defaultsPanel, defaultRelRefinability, "Default relationship refinability: ");

        return defaultsPanel;
    }

    private void makeAddressPalette() {
        JLayeredPane layers = getRootPane().getLayeredPane();

        addressPalette = new CdePalette(new BorderLayout(), new LeftPalettePoint());
        addressList    = new JList(aceFrameConfig.getAddressesList());
        addressPalette.add(new JScrollPane(addressList), BorderLayout.CENTER);
        addressPalette.setBorder(BorderFactory.createRaisedBevelBorder());
        layers.add(addressPalette, JLayeredPane.PALETTE_LAYER);

        int       width     = 400;
        int       height    = 500;
        Rectangle topBounds = getTopBoundsForPalette();

        addressPalette.setSize(width, height);
        addressPalette.setLocation(new Point(topBounds.x - width, topBounds.y + topBounds.height + 1));
        addressPalette.setOpaque(true);
        addressPalette.doLayout();
        addComponentListener(addressPalette);
        addressPalette.setVisible(true);
    }

    private JTabbedPane makeChroniclerPreferences() {
        JTabbedPane p = new JTabbedPane();

        p.addTab("precedence", makePrecedencePreferencePanel());
        p.addTab("contradiction", makeContradictionPreferencePanel());

        return p;
    }

    private JScrollPane makeClassifierConfig() throws Exception {
        return new JScrollPane(makeClassifierPrefPane());
    }

    private JScrollPane makeProjectConfig() throws Exception {
        return new JScrollPane(makeProjectPrefPane());
    }

    private JPanel makeClassifierPrefPane() {
        JPanel classifierPrefPanel = new JPanel(new GridLayout(0, 1));

        // INPUT SELECTION
        JPanel             classifierPrefInputSubpanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc                         = new GridBagConstraints();

        gbc.weightx    = 0;
        gbc.weighty    = 0;
        gbc.anchor     = GridBagConstraints.WEST;
        gbc.fill       = GridBagConstraints.HORIZONTAL;
        gbc.gridx      = 0;
        gbc.gridy      = 0;
        gbc.gridwidth  = 1;
        gbc.gridheight = 1;

        JLabel inputSelectLabel = new JLabel(" Classifier Input Mode:");

        inputSelectLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        classifierPrefInputSubpanel.add(inputSelectLabel, gbc);
        gbc.gridx++;
        gbc.weightx = 1;

        JComboBox pathSelectCombo = new JComboBox(CLASSIFIER_INPUT_MODE_PREF.values());

        pathSelectCombo.setSelectedItem(aceFrameConfig.getClassifierInputMode());
        classifierPrefInputSubpanel.add(pathSelectCombo, gbc);
        pathSelectCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox                  cb       = (JComboBox) e.getSource();
                CLASSIFIER_INPUT_MODE_PREF modePref = (CLASSIFIER_INPUT_MODE_PREF) cb.getSelectedItem();

                aceFrameConfig.setClassifierInputMode(modePref);

                try {
                    PathBI           editPath        = null;
                    I_GetConceptData editPathConcept = null;

                    if ((aceFrameConfig.getEditingPathSet() != null)
                            && (aceFrameConfig.getEditingPathSet().size() > 0)) {
                        editPath        = aceFrameConfig.getEditingPathSet().iterator().next();
                        editPathConcept = Terms.get().getConcept(editPath.getConceptNid());
                    }

                    PathBI           viewPath        = null;
                    I_GetConceptData viewPathConcept = null;

                    if ((aceFrameConfig.getViewPositionSet() != null)
                            && (aceFrameConfig.getViewPositionSet().size() > 0)) {
                        viewPath        = aceFrameConfig.getViewPositionSet().iterator().next().getPath();
                        viewPathConcept = Terms.get().getConcept(viewPath.getConceptNid());
                    }

                    if (modePref == CLASSIFIER_INPUT_MODE_PREF.EDIT_PATH) {
                        aceFrameConfig.setClassifierInputPath(editPathConcept);
                        aceFrameConfig.setClassifierOutputPath(viewPathConcept);
                    } else if (modePref == CLASSIFIER_INPUT_MODE_PREF.VIEW_PATH) {
                        aceFrameConfig.setClassifierInputPath(viewPathConcept);
                        aceFrameConfig.setClassifierOutputPath(viewPathConcept);
                    } else if (modePref == CLASSIFIER_INPUT_MODE_PREF.VIEW_PATH_WITH_EDIT_PRIORITY) {
                        aceFrameConfig.setClassifierInputPath(viewPathConcept);
                        aceFrameConfig.setClassifierOutputPath(viewPathConcept);
                    }
                } catch (TerminologyException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        // classifierPrefInputSubpanel.setBorder(BorderFactory.createTitledBorder(""));
        // classifierPrefInputSubpanel.add(someLabel);
        classifierPrefPanel.add(classifierPrefInputSubpanel);

        //
        TermComponentLabel classifierRootLabel = new TermComponentLabel(aceFrameConfig);

        classifierRootLabel.setTermComponent(aceFrameConfig.getClassificationRoot());
        aceFrameConfig.addPropertyChangeListener("classificationRoot",
                new PropertyListenerGlue("setTermComponent", I_AmTermComponent.class, classifierRootLabel));
        classifierRootLabel.addTermChangeListener(new PropertyListenerGlue("setClassificationRoot",
                I_GetConceptData.class, aceFrameConfig));
        wrapAndAdd(classifierPrefPanel, classifierRootLabel, "Classification root: ");

        TermComponentLabel classifierRoleRootLabel = new TermComponentLabel(aceFrameConfig);

        classifierRoleRootLabel.setTermComponent(aceFrameConfig.getClassificationRoleRoot());
        aceFrameConfig.addPropertyChangeListener("classificationRoleRoot",
                new PropertyListenerGlue("setTermComponent", I_AmTermComponent.class, classifierRoleRootLabel));
        classifierRoleRootLabel.addTermChangeListener(new PropertyListenerGlue("setClassificationRoleRoot",
                I_GetConceptData.class, aceFrameConfig));
        wrapAndAdd(classifierPrefPanel, classifierRoleRootLabel, "Role root: ");

        TermComponentLabel classificationIsaLabel = new TermComponentLabel(aceFrameConfig);

        classificationIsaLabel.setTermComponent(aceFrameConfig.getClassifierIsaType());
        aceFrameConfig.addPropertyChangeListener("classifierIsaType",
                new PropertyListenerGlue("setTermComponent", I_AmTermComponent.class, classificationIsaLabel));
        classificationIsaLabel.addTermChangeListener(new PropertyListenerGlue("setClassifierIsaType",
                I_GetConceptData.class, aceFrameConfig));
        wrapAndAdd(classifierPrefPanel, classificationIsaLabel, "Classification 'Is a': ");

        TermComponentLabel classifierConceptLabel = new TermComponentLabel(aceFrameConfig);

        classifierConceptLabel.setTermComponent(aceFrameConfig.getClassifierConcept());
        aceFrameConfig.addPropertyChangeListener("classifierConcept",
                new PropertyListenerGlue("setTermComponent", I_AmTermComponent.class, classifierConceptLabel));
        classifierConceptLabel.addTermChangeListener(new PropertyListenerGlue("setClassifierConcept",
                I_GetConceptData.class, aceFrameConfig));
        wrapAndAdd(classifierPrefPanel, classifierConceptLabel, "Classifier identity: ");

        if (DescriptionLogic.isPresent()) {  // :SNOOWL:
            JPanel owlPanel             = new JPanel(new GridLayout(1, 1));
            Component owlCheckBox = getCheckboxEditor("Enable OWL features",
                    "classifierOwlFeatureStatus", /* variable name */
                    DescriptionLogic.isVisible(), /* initial value */
                    true); /* enabled */
            owlPanel.add(owlCheckBox);
            classifierPrefPanel.add(owlCheckBox, BorderLayout.PAGE_START);
        }

        return classifierPrefPanel;
    }

    private JPanel makeProjectPrefPane() {

        wfStore = new WorkflowStore();

        JPanel projectPrefPanel = new JPanel(new GridLayout(0, 1));

        // INPUT SELECTION

        JPanel             projectChangedConceptSubpanel = new JPanel(new GridBagLayout());

        projectChangedConceptSubpanel.setBorder(BorderFactory.createTitledBorder(" Default configuration for changes review "));
        GridBagConstraints gbcCC                         = new GridBagConstraints();

        gbcCC.weightx    = 0;
        gbcCC.weighty    = 0;
        gbcCC.anchor     = GridBagConstraints.WEST;
        gbcCC.fill       = GridBagConstraints.HORIZONTAL ;
        gbcCC.gridx      = 0;
        gbcCC.gridy      = 0;
        gbcCC.gridwidth  = 1;
        gbcCC.gridheight = 1;


        JLabel inputProjectSelectLabel = new JLabel(" Project:");
        inputProjectSelectLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        projectChangedConceptSubpanel.add(inputProjectSelectLabel, gbcCC);
        gbcCC.gridx++;
        gbcCC.weightx = 1;

        JComboBox projectCCSelectCombo = new JComboBox();

        projectCCSelectCombo.addItem("none");

        if (WorkflowHelper.isWorkflowCapabilityAvailable()){
            projectCCSelectCombo.setEnabled(false);
        }else{
            Collection<ProjectBI> projectsCC=null;
            try {
                projectsCC = wfStore.getAllProjects();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            if (projectsCC!=null){
                for (ProjectBI proj:projectsCC){
                    I_GetConceptData conceptTmp;
                    try {
                        conceptTmp = Terms.get().getConcept(proj.getUuid());
                        projectCCSelectCombo.addItem(conceptTmp); 
                    } catch (TerminologyException | IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        projectChangedConceptSubpanel.add(projectCCSelectCombo, gbcCC);

        projectCCSelectCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox                  cb       = (JComboBox) e.getSource();

                worklistCCSelectCombo.removeAllItems();
                if (!(cb.getSelectedItem() instanceof I_GetConceptData)){
                    aceFrameConfig.setDefaultProjectForChangedConcept(null);
                    aceFrameConfig.setDefaultWorkflowForChangedConcept(null);
                    return;
                }
                I_GetConceptData projPref = (I_GetConceptData) cb.getSelectedItem();

                aceFrameConfig.setDefaultProjectForChangedConcept(projPref);

                Collection<WorkListBI> worklists=null;
                try {
                    worklists=wfStore.getProject(projPref.getPrimUuid()).getWorkLists();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                worklistCCSelectCombo.addItem("none");
                if (worklists!=null){
                    List<I_GetConceptData> concepts=new ArrayList<I_GetConceptData>();
                    for (WorkListBI worklist:worklists){

                        I_GetConceptData conceptTmp;
                        try {
                            conceptTmp = Terms.get().getConcept(worklist.getUuid());
                            worklistCCSelectCombo.addItem(conceptTmp);
                        } catch (TerminologyException | IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                worklistCCSelectCombo.setSelectedItem("none");
            }
        });

        gbcCC.weightx    = 0;
        gbcCC.gridx      = 0;
        gbcCC.gridy++ ;

        JLabel inputSelectWorklistCC = new JLabel(" Workflow:");

        inputSelectWorklistCC.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        projectChangedConceptSubpanel.add(inputSelectWorklistCC, gbcCC);
        gbcCC.gridx++;
        gbcCC.weightx = 1;

        worklistCCSelectCombo = new JComboBox();
        projectChangedConceptSubpanel.add(worklistCCSelectCombo, gbcCC);
        if (WorkflowHelper.isWorkflowCapabilityAvailable()){
            aceFrameConfig.setDefaultProjectForChangedConcept(null);
            aceFrameConfig.setDefaultWorkflowForChangedConcept(null);
            worklistCCSelectCombo.addItem("none");
            worklistCCSelectCombo.setEnabled(false);
        }else{
            I_GetConceptData projectCConcept=aceFrameConfig.getDefaultProjectForChangedConcept();

            if (projectCConcept!=null){
                projectCCSelectCombo.setSelectedItem(projectCConcept);
            }
        }

        worklistCCSelectCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox                  cb       = (JComboBox) e.getSource();

                if (!(cb.getSelectedItem() instanceof I_GetConceptData)){
                    aceFrameConfig.setDefaultWorkflowForChangedConcept(null);
                    return;
                }
                I_GetConceptData workList = (I_GetConceptData) cb.getSelectedItem();
                aceFrameConfig.setDefaultWorkflowForChangedConcept(workList);

            }
        });

        projectPrefPanel.add(projectChangedConceptSubpanel);

        if (WorkflowHelper.isWorkflowCapabilityAvailable()){
            worklistCCSelectCombo.setEnabled(false);
        }else{
            I_GetConceptData worklistConcept=aceFrameConfig.getDefaultWorkflowForChangedConcept();
            if (worklistConcept!=null){
                worklistCCSelectCombo.setSelectedItem(worklistConcept);
            }
        }
        return projectPrefPanel;
    }

    private JScrollPane makeComponentConfig() throws Exception {
        return new JScrollPane(makeComponentToggleCheckboxPane());
    }

    private JPanel makeComponentToggleCheckboxPane() {
        JPanel             checkBoxPane = new JPanel(new GridBagLayout());
        GridBagConstraints c            = new GridBagConstraints();

        c.anchor  = GridBagConstraints.WEST;
        c.gridx   = 0;
        c.gridy   = 0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;

        for (TOGGLES t : TOGGLES.values()) {
            JCheckBox box = new JCheckBox(t.name());

            box.setSelected(aceFrameConfig.isToggleVisible(t));
            box.addActionListener(new SetToggleVisibleListener(t));
            checkBoxPane.add(box, c);
            c.gridy++;
        }

        c.fill    = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        checkBoxPane.add(new JPanel(), c);

        return checkBoxPane;
    }

    private void makeConfigPalette() throws Exception {
        JLayeredPane layers = getRootPane().getLayeredPane();

        preferencesPalette = new CdePalette(new BorderLayout(), new RightPalettePoint());
        preferencesTab     = new JTabbedPane();
        preferencesTab.addTab("view", makeViewConfig());
        preferencesTab.addTab("edit", makeEditConfig());

        JTabbedPane pathPanes = new JTabbedPane();

        pathPanes.addTab("configure",
                new SelectPathAndPositionPanelWithCombo(false, "for view", aceFrameConfig,
                        new PropertySetListenerGlue("removeViewPosition", "addViewPosition",
                                "replaceViewPosition", "getViewPositionSet", PositionBI.class, aceFrameConfig)));
        pathPanes.addTab("create", new CreatePathPanel(aceFrameConfig));
        preferencesTab.addTab("path", pathPanes);
        preferencesTab.addTab("refset", makeRefsetConfig());
        preferencesTab.addTab("changeset", new ChangeSetConfigPanel(aceFrameConfig));
        preferencesTab.addTab("chronicler", makeChroniclerPreferences());
        preferencesTab.addTab("classifier", makeClassifierConfig());
        preferencesTab.addTab("workflow", makeProjectConfig());

        layers.add(preferencesPalette, JLayeredPane.PALETTE_LAYER);
        preferencesPalette.add(preferencesTab, BorderLayout.CENTER);
        preferencesPalette.setBorder(BorderFactory.createRaisedBevelBorder());

        int width  = 600;
        int height = 675;

        preferencesPalette.setSize(width, height);

        Rectangle topBounds = getTopBoundsForPalette();

        preferencesPalette.setLocation(new Point(topBounds.x + topBounds.width,
                topBounds.y + topBounds.height + 1));
        preferencesPalette.setOpaque(true);
        preferencesPalette.doLayout();
        addComponentListener(preferencesPalette);
        preferencesPalette.setVisible(true);
    }

    private Component makeContradictionPreferencePanel() {
        JPanel contradictionConfigPanel = new JPanel(new BorderLayout());
        JPanel controlPanel             = new JPanel(new GridLayout(3, 1));

        controlPanel.add(getCheckboxEditor("show contradictions in taxonomy view",
                "highlightConflictsInTaxonomyView",
                aceFrameConfig.getHighlightConflictsInTaxonomyView(), true));
        controlPanel.add(getCheckboxEditor("show contradictions in component panel",
                "highlightConflictsInComponentPanel",
                aceFrameConfig.getHighlightConflictsInComponentPanel(), true));

        final JTextPane descriptionPanel = new JTextPane();

        descriptionPanel.setEditable(false);
        descriptionPanel.setContentType("text/html");

        JScrollPane descriptionScroll = new JScrollPane(descriptionPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JComboBox conflictComboBox = new JComboBox(aceFrameConfig.getAllConflictResolutionStrategies());

        conflictComboBox.setSelectedItem(aceFrameConfig.getConflictResolutionStrategy());
        descriptionPanel.setText(aceFrameConfig.getConflictResolutionStrategy().getDescription());
        conflictComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionevent) {
                JComboBox             cb                         = (JComboBox) actionevent.getSource();
                ContradictionManagerBI conflictResolutionStrategy = (ContradictionManagerBI) cb.getSelectedItem();

                aceFrameConfig.setConflictResolutionStrategy(conflictResolutionStrategy);
                descriptionPanel.setText(aceFrameConfig.getConflictResolutionStrategy().getDescription());
            }
        });
        controlPanel.add(conflictComboBox);
        contradictionConfigPanel.add(controlPanel, BorderLayout.PAGE_START);
        contradictionConfigPanel.add(descriptionScroll, BorderLayout.CENTER);

        return contradictionConfigPanel;
    }

    private JComponent makeDescPanel() throws TerminologyException, IOException {
        JPanel             langPrefPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc           = new GridBagConstraints();

        gbc.weightx    = 0;
        gbc.weighty    = 0;
        gbc.anchor     = GridBagConstraints.WEST;
        gbc.fill       = GridBagConstraints.HORIZONTAL;
        gbc.gridx      = 0;
        gbc.gridy      = 0;
        gbc.gridwidth  = 1;
        gbc.gridheight = 1;

        JLabel sortOrderLabel = new JLabel(" Language/Type preference order:");

        sortOrderLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        langPrefPanel.add(sortOrderLabel, gbc);
        gbc.gridx++;
        gbc.weightx = 1;

        JComboBox sortOrderCombo = new JComboBox(LANGUAGE_SORT_PREF.values());

        sortOrderCombo.setSelectedItem(aceFrameConfig.getLanguageSortPref());
        langPrefPanel.add(sortOrderCombo, gbc);
        sortOrderCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox          cb       = (JComboBox) e.getSource();
                LANGUAGE_SORT_PREF sortPref = (LANGUAGE_SORT_PREF) cb.getSelectedItem();

                aceFrameConfig.setLanguageSortPref(sortPref);
            }
        });
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill      = GridBagConstraints.BOTH;
        gbc.weighty   = 1;
        gbc.gridwidth = 2;
        langPrefPanel.add(new JScrollPane(makeTermList("Language (Dialect) preference order:",
                aceFrameConfig.getLanguagePreferenceList())), gbc);
        gbc.gridy++;

        TerminologyListModel shortLabelPrefOrderTableModel = new TerminologyListModel();

        for (int id : aceFrameConfig.getShortLabelDescPreferenceList().getListValues()) {
            shortLabelPrefOrderTableModel.addElement(Terms.get().getConcept(id));
        }

        shortLabelPrefOrderTableModel.addListDataListener(aceFrameConfig.getShortLabelDescPreferenceList());

        TerminologyList shortLabelOrderList = new TerminologyList(shortLabelPrefOrderTableModel,
                aceFrameConfig);

        shortLabelOrderList.setBorder(BorderFactory.createTitledBorder("Short Label preference order: "));
        langPrefPanel.add(new JScrollPane(shortLabelOrderList), gbc);
        gbc.gridy++;

        TerminologyListModel longLabelPrefOrderTableModel = new TerminologyListModel();

        for (int id : aceFrameConfig.getLongLabelDescPreferenceList().getListValues()) {
            longLabelPrefOrderTableModel.addElement(Terms.get().getConcept(id));
        }

        longLabelPrefOrderTableModel.addListDataListener(aceFrameConfig.getLongLabelDescPreferenceList());

        TerminologyList longLabelOrderList = new TerminologyList(longLabelPrefOrderTableModel, aceFrameConfig);

        longLabelOrderList.setBorder(BorderFactory.createTitledBorder("Long label preference order: "));
        langPrefPanel.add(new JScrollPane(longLabelOrderList), gbc);
        gbc.gridy++;

        TerminologyListModel treeDescPrefOrderTableModel = new TerminologyListModel();

        for (int id : aceFrameConfig.getTreeDescPreferenceList().getListValues()) {
            treeDescPrefOrderTableModel.addElement(Terms.get().getConcept(id));
        }

        treeDescPrefOrderTableModel.addListDataListener(aceFrameConfig.getTreeDescPreferenceList());

        TerminologyList treePrefOrderList = new TerminologyList(treeDescPrefOrderTableModel, aceFrameConfig);

        treePrefOrderList.setBorder(BorderFactory.createTitledBorder("Tree preference order: "));
        langPrefPanel.add(new JScrollPane(treePrefOrderList), gbc);
        gbc.gridy++;

        TerminologyListModel descPrefOrderTableModel = new TerminologyListModel();

        for (int id : aceFrameConfig.getTableDescPreferenceList().getListValues()) {
            descPrefOrderTableModel.addElement(Terms.get().getConcept(id));
        }

        descPrefOrderTableModel.addListDataListener(aceFrameConfig.getTableDescPreferenceList());

        TerminologyList prefOrderList = new TerminologyList(descPrefOrderTableModel, aceFrameConfig);

        prefOrderList.setBorder(BorderFactory.createTitledBorder("Table preference order: "));
        langPrefPanel.add(new JScrollPane(prefOrderList), gbc);
        gbc.gridy++;

        return langPrefPanel;
    }

    private JTabbedPane makeEditConfig() throws Exception {
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("defaults", new JScrollPane(madeDefaultsPanel()));
        tabs.addTab("rel type",
                new JScrollPane(makePopupConfigPanel(aceFrameConfig.getEditRelTypePopup(),
                        "Relationship types for popup:")));
        tabs.addTab("rel refinabilty",
                new JScrollPane(makePopupConfigPanel(aceFrameConfig.getEditRelRefinabiltyPopup(),
                        "Relationship refinability for popup:")));
        tabs.addTab("rel characteristic",
                new JScrollPane(makePopupConfigPanel(aceFrameConfig.getEditRelCharacteristicPopup(),
                        "Relationship characteristics for popup:")));
        tabs.addTab("desc type",
                new JScrollPane(makePopupConfigPanel(aceFrameConfig.getEditDescTypePopup(),
                        "Description types for popup:")));
        tabs.addTab("image type",
                new JScrollPane(makePopupConfigPanel(aceFrameConfig.getEditImageTypePopup(),
                        "Image types for popup:")));
        tabs.addTab("status",
                new JScrollPane(makePopupConfigPanel(aceFrameConfig.getEditStatusTypePopup(),
                        "Status values for popup:")));

        return tabs;
    }

    private void makeHistoryPalette() {
        JLayeredPane layers = getRootPane().getLayeredPane();

        historyPalette = new CdePalette(new BorderLayout(), new LeftPalettePoint());

        JTabbedPane     tabs       = new JTabbedPane();
        TerminologyList viewerList = new TerminologyList(viewerHistoryTableModel, false, false, aceFrameConfig);

        tabs.addTab("viewer", new JScrollPane(viewerList));

        if (aceFrameConfig.getTabHistoryMap().get("favoritesList") == null) {
            aceFrameConfig.getTabHistoryMap().put("favoritesList", new ArrayList<I_GetConceptData>());
        }

        favoritesTableModel = new TerminologyListModel(aceFrameConfig.getTabHistoryMap().get("favoritesList"));

        TerminologyList favorites = new TerminologyList(favoritesTableModel, true, false, aceFrameConfig);

        tabs.addTab("favorites", new JScrollPane(favorites));
        historyPalette.add(tabs, BorderLayout.CENTER);
        historyPalette.setBorder(BorderFactory.createRaisedBevelBorder());
        layers.add(historyPalette, JLayeredPane.PALETTE_LAYER);

        int       width     = 400;
        int       height    = 500;
        Rectangle topBounds = getTopBoundsForPalette();

        historyPalette.setSize(width, height);
        historyPalette.setLocation(new Point(topBounds.x - width, topBounds.y + topBounds.height + 1));
        historyPalette.setOpaque(true);
        historyPalette.doLayout();
        addComponentListener(historyPalette);
        historyPalette.setVisible(true);
    }

    private JComponent makePopupConfigPanel(I_IntList list, String borderLabel) {
        TerminologyIntList popupList  = makeTermList(borderLabel, list);
        JPanel             popupPanel = new JPanel(new GridLayout(0, 1));

        popupPanel.add(new JScrollPane(popupList));

        return popupPanel;
    }

    private Component makePrecedencePreferencePanel() {
        JPanel          precedenceConfigPanel = new JPanel(new BorderLayout());
        JPanel          controlPanel          = new JPanel(new GridLayout(1, 1));
        final JTextPane descriptionPanel      = new JTextPane();

        descriptionPanel.setEditable(false);
        descriptionPanel.setContentType("text/html");

        JScrollPane descriptionScroll = new JScrollPane(descriptionPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JComboBox precedenceCombo = new JComboBox(Precedence.values());

        precedenceCombo.setSelectedItem(aceFrameConfig.getPrecedence());
        descriptionPanel.setText(aceFrameConfig.getPrecedence().getDescription());
        precedenceCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionevent) {
                JComboBox  cb                 = (JComboBox) actionevent.getSource();
                Precedence selectedPrecedence = (Precedence) cb.getSelectedItem();

                aceFrameConfig.setPrecedence(selectedPrecedence);
                descriptionPanel.setText(selectedPrecedence.getDescription());
            }
        });
        controlPanel.add(precedenceCombo);
        precedenceConfigPanel.add(controlPanel, BorderLayout.PAGE_START);
        precedenceConfigPanel.add(descriptionScroll, BorderLayout.CENTER);

        return precedenceConfigPanel;
    }

    private void makeProcessPalette() throws Exception {
        JLayeredPane layers = getRootPane().getLayeredPane();

        processPalette = new CdePalette(new BorderLayout(), new RightPalettePoint());
        layers.add(processPalette, JLayeredPane.PALETTE_LAYER);
        processPalette.add(masterProcessBuilderPanel, BorderLayout.CENTER);
        processPalette.setBorder(BorderFactory.createRaisedBevelBorder());

        int       width     = getWidth() - termTreeConceptSplit.getDividerLocation();
        int       height    = getHeight() - topPanel.getHeight();
        Rectangle topBounds = getTopBoundsForPalette();

        processPalette.setSize(width, height);
        processPalette.setLocation(new Point(topBounds.x + topBounds.width,
                topBounds.y + topBounds.height + 1));
        processPalette.setOpaque(true);
        processPalette.doLayout();
        addComponentListener(processPalette);
        processPalette.setVisible(true);
    }

    private void makeQueuePalette() throws Exception {
        JLayeredPane layers = getRootPane().getLayeredPane();

        queuePalette = new CdePalette(new BorderLayout(), new RightPalettePoint());
        layers.add(queuePalette, JLayeredPane.PALETTE_LAYER);

        MasterWorker worker = new MasterWorker(UUID.randomUUID(), "Queue Palette MasterWorker");

        worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), aceFrameConfig);
        queuePalette.add(makeQueueViewerPanel(worker),
                BorderLayout.CENTER);
        queuePalette.setBorder(BorderFactory.createRaisedBevelBorder());

        int       width     = getWidth() - termTreeConceptSplit.getDividerLocation();
        int       height    = getHeight() - topPanel.getHeight();
        Rectangle topBounds = getTopBoundsForPalette();

        queuePalette.setSize(width, height);
        queuePalette.setLocation(new Point(topBounds.x + topBounds.width, topBounds.y + topBounds.height + 1));
        queuePalette.setOpaque(true);
        queuePalette.doLayout();
        addComponentListener(queuePalette);
        queuePalette.setVisible(true);
    }

    public JPanel makeQueueViewerPanel(MasterWorker worker)
            throws Exception {
        queueViewer = new QueueViewerPanel(worker);

        JPanel             combinedPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c             = new GridBagConstraints();

        c.gridx      = 0;
        c.gridy      = 0;
        c.weightx    = 1;
        c.weighty    = 0;
        c.gridheight = 1;
        c.fill       = GridBagConstraints.HORIZONTAL;
        combinedPanel.add(getQueueViewerTopPanel(), c);
        c.gridy++;
        c.weighty = 1;
        c.fill    = GridBagConstraints.BOTH;
        combinedPanel.add(queueViewer, c);

        return combinedPanel;
    }

    private JTabbedPane makeRefsetConfig() throws Exception {
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Concept", makeRefsetDefaults(TOGGLES.ATTRIBUTES));
        tabs.addTab("Descriptions", makeRefsetDefaults(TOGGLES.DESCRIPTIONS));
        tabs.addTab("Source Rels", makeRefsetDefaults(TOGGLES.SOURCE_RELS));
        tabs.addTab("Dest Rels", makeRefsetDefaults(TOGGLES.DEST_RELS));
        tabs.addTab("Images", makeRefsetDefaults(TOGGLES.IMAGE));

        return tabs;
    }

    private JTabbedPane makeRefsetDefaults(TOGGLES toggle) throws TerminologyException, IOException {
        JTabbedPane tabs = new JTabbedPane();

        // tabs.addTab("enabled ref set types" , new
        // JScrollPane(makeRefsetCheckboxPane(toggle)));
        for (REFSET_TYPES type : REFSET_TYPES.values()) {
            tabs.addTab(type.getInterfaceName(), new JScrollPane(makeRefsetDefaultsPanel(toggle, type)));
        }

        return tabs;
    }

    private JPanel makeRefsetDefaultsPanel(TOGGLES toggle, REFSET_TYPES type)
            throws TerminologyException, IOException {
        JPanel             defaultsPane = new JPanel(new GridBagLayout());
        GridBagConstraints c            = new GridBagConstraints();

        c.anchor  = GridBagConstraints.WEST;
        c.gridx   = 0;
        c.gridy   = 0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;

        JCheckBox box = new JCheckBox(type.getInterfaceName() + " enabled");

        box.setSelected(aceFrameConfig.isRefsetInToggleVisible(type, toggle));
        box.addActionListener(new SetRefsetInToggleVisible(type, toggle));
        defaultsPane.add(box, c);
        c.gridy++;

        JTabbedPane editDefaultsTabs = new JTabbedPane();

        c.fill    = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        defaultsPane.add(editDefaultsTabs, c);

        switch (type) {
        case BOOLEAN :
            addDefaults(
                    editDefaultsTabs,
                    (RefsetDefaults) aceFrameConfig.getRefsetPreferencesForToggle(toggle).getBooleanPreferences(),
                    type);

            break;

        case CONCEPT :
            addDefaults(
                    editDefaultsTabs,
                    (RefsetDefaults) aceFrameConfig.getRefsetPreferencesForToggle(toggle).getConceptPreferences(),
                    type);
            editDefaultsTabs.addTab(
                    "concept types",
                    new JScrollPane(
                            makePopupConfigPanel(
                                    aceFrameConfig.getRefsetPreferencesForToggle(
                                            toggle).getConceptPreferences().getConceptPopupIds(), "Concept types for popup:")));

            break;

        case CON_INT :
            addDefaults(
                    editDefaultsTabs,
                    (RefsetDefaults) aceFrameConfig.getRefsetPreferencesForToggle(toggle).getConIntPreferences(),
                    type);
            editDefaultsTabs.addTab(
                    "concept types",
                    new JScrollPane(
                            makePopupConfigPanel(
                                    aceFrameConfig.getRefsetPreferencesForToggle(
                                            toggle).getConIntPreferences().getConceptPopupIds(), "Concept types for popup:")));

            break;

        case INTEGER :
            addDefaults(
                    editDefaultsTabs,
                    (RefsetDefaults) aceFrameConfig.getRefsetPreferencesForToggle(toggle).getIntegerPreferences(),
                    type);

            break;

        case STRING :
            addDefaults(
                    editDefaultsTabs,
                    (RefsetDefaults) aceFrameConfig.getRefsetPreferencesForToggle(toggle).getStringPreferences(),
                    type);

            break;

        default :
            break;
        }

        return defaultsPane;
    }

    private JComponent makeStatusPrefPanel() throws TerminologyException, IOException {
        TerminologyListModel statusValuesModel = new TerminologyListModel();

        for (int id : aceFrameConfig.getAllowedStatus().getSetValues()) {
            statusValuesModel.addElement(Terms.get().getConcept(id));
        }

        statusValuesModel.addListDataListener(aceFrameConfig.getAllowedStatus());

        TerminologyList statusList = new TerminologyList(statusValuesModel, aceFrameConfig);

        statusList.setBorder(BorderFactory.createTitledBorder("Status values for display:"));

        return new JScrollPane(statusList);
    }

    private JComponent makeTaxonomyPrefPanel() throws TerminologyException, IOException {
        JPanel               relPrefPanel = new JPanel(new GridLayout(0, 1));
        TerminologyListModel rootModel    = new TerminologyListModel();

        for (int id : aceFrameConfig.getRoots().getSetValues()) {
            rootModel.addElement(Terms.get().getConcept(id));
        }

        rootModel.addListDataListener(aceFrameConfig.getRoots());

        TerminologyList rootList = new TerminologyList(rootModel, aceFrameConfig);

        rootList.setBorder(BorderFactory.createTitledBorder("Roots:"));
        relPrefPanel.add(new JScrollPane(rootList));
        relPrefPanel.add(new JScrollPane(makeTermList("Parent relationships:",
                aceFrameConfig.getDestRelTypes())));
        relPrefPanel.add(new JScrollPane(makeTermList("Child relationships:",
                aceFrameConfig.getSourceRelTypes())));

        /*
         * checkPanel.add(getCheckboxEditor("allow variable height taxonomy view"
         * , "variableHeightTaxonomyView", aceFrameConfig
         * .getVariableHeightTaxonomyView(), false));
         */
        JPanel             checkPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc        = new GridBagConstraints();

        gbc.fill       = GridBagConstraints.HORIZONTAL;
        gbc.anchor     = GridBagConstraints.EAST;
        gbc.weightx    = 0;
        gbc.weighty    = 0;
        gbc.gridx      = 0;
        gbc.gridy      = 0;
        gbc.gridheight = 1;
        checkPanel.add(new JLabel("stated/inferred policy:"), gbc);
        gbc.weightx = 1;
        gbc.gridx   = 1;

        JComboBox relAssertionTypeComboBox = new JComboBox(RelAssertionType.values());

        relAssertionTypeComboBox.setSelectedItem(aceFrameConfig.getRelAssertionType());
        relAssertionTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionevent) {
                JComboBox        cb               = (JComboBox) actionevent.getSource();
                RelAssertionType relAssertionType = (RelAssertionType) cb.getSelectedItem();

                aceFrameConfig.setRelAssertionType(relAssertionType);
            }
        });
        checkPanel.add(relAssertionTypeComboBox, gbc);
        gbc.gridy++;
        gbc.gridx     = 0;
        gbc.gridwidth = 2;
        checkPanel.add(getCheckboxEditor("show viewer images in taxonomy view", "showViewerImagesInTaxonomy",
                aceFrameConfig.getShowViewerImagesInTaxonomy(), true), gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy++;
        checkPanel.add(getCheckboxEditor("show refset info in taxonomy view", "showRefsetInfoInTaxonomy",
                aceFrameConfig.getShowRefsetInfoInTaxonomy(), true), gbc);

        /*
         * gbc.weighty = 1;
         * gbc.gridheight = 3;
         * gbc.gridy++;
         * checkPanel.add(new JScrollPane(makeTermList("Refsets to show in taxonomy view: ", aceFrameConfig
         * .getRefsetsToShowInTaxonomy())), gbc);
         *
         */
        relPrefPanel.add(checkPanel);
        relPrefPanel.add(new JScrollPane(makeTermList("Refsets to show in taxonomy view: ",
                aceFrameConfig.getRefsetsToShowInTaxonomy())));

        JPanel checkPanel2 = new JPanel(new GridBagLayout());

        gbc            = new GridBagConstraints();
        gbc.fill       = GridBagConstraints.HORIZONTAL;
        gbc.anchor     = GridBagConstraints.EAST;
        gbc.weightx    = 1;
        gbc.weighty    = 0;
        gbc.gridx      = 0;
        gbc.gridy      = 0;
        gbc.gridheight = 1;
        checkPanel2.add(getCheckboxEditor("sort taxonomy using refsets", "sortTaxonomyUsingRefset",
                aceFrameConfig.getSortTaxonomyUsingRefset(), true), gbc);
        gbc.weighty = 1;
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.gridy++;
        gbc.gridheight = 3;
        checkPanel2.add(new JScrollPane(makeTermList("Refsets to sort taxonomy view: ",
                aceFrameConfig.getRefsetsToSortTaxonomy())), gbc);
        relPrefPanel.add(checkPanel2);

        return relPrefPanel;
    }

    private TerminologyIntList makeTermList(String title, I_IntList list) {
        TerminologyIntListModel termListModel   = new TerminologyIntListModel((IntList) list, true, aceFrameConfig);
        TerminologyIntList      terminologyList = new TerminologyIntList(termListModel, aceFrameConfig);

        terminologyList.setBorder(BorderFactory.createTitledBorder(title));

        return terminologyList;
    }

    private TerminologyList makeTermList(String title, I_IntSet set) throws TerminologyException, IOException {
        TerminologyListModel termListModel = new TerminologyListModel();

        for (int id : set.getSetValues()) {
            termListModel.addElement(Terms.get().getConcept(id));
        }

        termListModel.addListDataListener(set);

        TerminologyList terminologyList = new TerminologyList(termListModel, aceFrameConfig);

        terminologyList.setBorder(BorderFactory.createTitledBorder(title));

        return terminologyList;
    }

    private JComponent makeTypeFilterPanel() throws TerminologyException, IOException {
        TerminologyListModel descTypeTableModel = new TerminologyListModel();

        for (int id : aceFrameConfig.getDescTypes().getSetValues()) {
            descTypeTableModel.addElement(Terms.get().getConcept(id));
        }

        descTypeTableModel.addListDataListener(aceFrameConfig.getDescTypes());

        TerminologyList descList = new TerminologyList(descTypeTableModel, aceFrameConfig);

        descList.setBorder(BorderFactory.createTitledBorder("Description types: "));

        JPanel typeFilterPanel = new JPanel(new GridLayout(0, 1));

        typeFilterPanel.add(new JScrollPane(descList));

        TerminologyListModel relTypeTableModel = new TerminologyListModel();

        for (int id : aceFrameConfig.getPrefFilterTypesForRel().getSetValues()) {
            relTypeTableModel.addElement(Terms.get().getConcept(id));
        }

        relTypeTableModel.addListDataListener(aceFrameConfig.getPrefFilterTypesForRel());

        TerminologyList relTypeList = new TerminologyList(relTypeTableModel, aceFrameConfig);

        relTypeList.setBorder(BorderFactory.createTitledBorder("Relationship types: "));
        typeFilterPanel.add(new JScrollPane(relTypeList));

        return typeFilterPanel;
    }

    private JTabbedPane makeViewConfig() throws Exception {
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("descriptions", makeDescPanel());
        tabs.addTab("filters", makeTypeFilterPanel());
        tabs.addTab("status", makeStatusPrefPanel());
        tabs.addTab("taxonomy", makeTaxonomyPrefPanel());
        tabs.addTab("component", makeComponentConfig());

        return tabs;
    }

    public boolean okToClose() {
        if (Ts.get().hasUncommittedChanges()) {
            AceLog.getAppLog().info("Uncommitted: " + uncommitted);

            if (aceConfig != null) {
                for (I_ConfigAceFrame frameConfig : getAceConfig().aceFrames) {
                    frameConfig.setCommitEnabled(true);
                }
            }

            Object[] options = { "List Uncommitted", "OK" };
            int      n       = JOptionPane.showOptionDialog(frame, "Please commit or cancel before quitting.",
                    "There are uncommitted changes", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null,    // do not use a custom Icon
                    options,        // the titles of buttons
                    options[0]);    // default button title

            if (n == JOptionPane.YES_OPTION) {
                conceptListEditor.addUncommittedToListButton.doClick();
                showListView();
            }

            return false;
        }

        return true;
    }

    public void performLuceneSearch(String query, List<I_TestSearchResults> extraCriterion) {
        searchPanel.performLuceneSearch(query, extraCriterion);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("commit")) {
            removeConfigPalette();
        } else if (evt.getPropertyName().equals("commitEnabled")) {
            if (commitButton != null) {
                commitButton.setEnabled(aceFrameConfig.isCommitEnabled());

                if (aceFrameConfig.isCommitEnabled()) {
                    commitButton.setText("<html><b><font color='green'>commit</font></b>");
                } else {
                    commitButton.setText("commit");

                    if (dataCheckListModel != null) {
                        dataCheckListModel.clear();
                    }
                }
            }

            if (cancelButton != null) {
                cancelButton.setEnabled(aceFrameConfig.isCommitEnabled());
            }
        } else if (evt.getPropertyName().equals("lastViewed")) {
            viewerHistoryTableModel.addElement(0, (I_GetConceptData) evt.getNewValue());

            while (viewerHistoryTableModel.getSize() > maxHistoryListSize) {
                viewerHistoryTableModel.removeElement(viewerHistoryTableModel.getSize() - 1);
            }
        } else if (evt.getPropertyName().equals("uncommitted")) {

            // Nothing to do...
        } else if (evt.getPropertyName().equals("imported")) {

            // Nothing to do...
        }
    }

    @Override
    public boolean quit() {
        if (editMode) {
            if (okToClose()) {
                int option = JOptionPane.showConfirmDialog(this, "Save profile before quitting?",
                        "Save profile?", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    try {
                        AceConfig.config.save();
                        linkPref.exportFields(TtkPreferences.get());
                    } catch (IOException e) {
                        AceLog.getAppLog().alertAndLogException(e);

                        return false;
                    }
                }
            } else {
                return false;
            }
        }

        if (runShutdownProcesses) {
            runShutdownProcesses = false;

            File configFile     = aceFrameConfig.getMasterConfig().getProfileFile();
            File shutdownFolder = new File(configFile.getParentFile().getParentFile(), "shutdown");

            executeShutdownProcesses(shutdownFolder);
            shutdownFolder = new File(configFile.getParentFile(), "shutdown");
            executeShutdownProcesses(shutdownFolder);

            try {
                NodeFactory.close();
                Terms.get().close();
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

        return true;
    }

    public void refreshRefsetTab() {
        refsetSpecPanel.refresh();
    }

    public boolean refsetTabIsSelected() {
        return conceptTabs.getSelectedIndex() == refsetTabIndex;
    }

    private void removeConfigPalette() {
        if (preferencesPalette != null) {
            CdePalette oldPallette = preferencesPalette;

            preferencesPalette = null;
            oldPallette.setVisible(false);

            JLayeredPane layers = getRootPane().getLayeredPane();

            oldPallette.removeGhost();
            layers.remove(oldPallette);
        }

        if (showPreferencesButton.isSelected()) {
            try {
                makeConfigPalette();
                getRootPane().getLayeredPane().moveToFront(preferencesPalette);
                preferencesPalette.togglePalette(showPreferencesButton.isSelected(), TOGGLE_DIRECTION.LEFT_RIGHT);
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    public void removeDataCheckListener(TermComponentDataCheckSelectionListener l) {
        dataCheckListeners.remove(l);
    }

    public void removeSearchLinkedComponent(I_ContainTermComponent component) {
        searchPanel.removeLinkedComponent(component);

        if (wfSearchPanel != null) {
            wfSearchPanel.removeLinkedComponent(component);
        }
    }

    public void removeTaxonomySelectionListener(TermComponentTreeSelectionListener treeListener) {
        treeHelper.removeTreeSelectionListener(treeListener);
    }

    private void resizePalttes() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (queuePalette != null) {
                    if (showQueuesButton.isSelected()) {
                        getRootPane().getLayeredPane().moveToFront(queuePalette);
                        deselectOthers(showQueuesButton);
                    }

                    queuePalette.setSize(ACE.this.getWidth() - termTreeConceptSplit.getDividerLocation(),
                            conceptTabs.getHeight() + 4);
                    revalidateAllParents(queuePalette);
                    queuePalette.componentResized(null);
                }

                if (processPalette != null) {
                    if (showProcessBuilder.isSelected()) {
                        getRootPane().getLayeredPane().moveToFront(processPalette);
                        deselectOthers(showProcessBuilder);
                    }

                    processPalette.setSize(ACE.this.getWidth() - termTreeConceptSplit.getDividerLocation(),
                            conceptTabs.getHeight() + 4);
                    revalidateAllParents(processPalette);
                    processPalette.componentResized(null);
                }
            }
            private void revalidateAllParents(Container cont) {
                while (cont != null) {
                    cont.validate();
                    cont = cont.getParent();
                }
            }
            @SuppressWarnings("unused")
            private void revalidateAllDescendants(Container cont) {
                while (cont != null) {
                    cont.validate();

                    for (Component desc : cont.getComponents()) {
                        if (Container.class.isAssignableFrom(desc.getClass())) {
                            revalidateAllDescendants((Container) desc);
                        }
                    }
                }
            }
        });
    }

    public void setup(I_ConfigAceFrame aceFrameConfig) throws Exception {
        menuWorker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), aceFrameConfig);
        this.aceFrameConfig = (AceFrameConfig) aceFrameConfig;
        Terms.get().setActiveAceFrameConfig(aceFrameConfig);
        this.aceFrameConfig.addPropertyChangeListener(this);

        try {
            masterProcessBuilderPanel = new ProcessBuilderContainer(aceFrameConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        searchPanel = new SearchPanel(aceFrameConfig, this);
        searchPanel.addComponentListener(new ResizePalettesListener());

        if (WorkflowHelper.isWorkflowCapabilityAvailable()) {
            wfSearchPanel = new WorkflowHistorySearchPanel(aceFrameConfig, this);
            wfSearchPanel.addComponentListener(new ResizePalettesListener());
            wfSearchPanel.setMinimumSize(new Dimension(0, 0));
        }

        GridBagConstraints c = new GridBagConstraints();

        c.gridx     = 0;
        c.gridy     = 0;
        c.weightx   = 1;
        c.weighty   = 0;
        c.fill      = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        topPanel    = getTopPanel();
        add(topPanel, c);
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.fill    = GridBagConstraints.BOTH;
        add(getContentPanel(), c);
        c.gridx   = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridy++;
        c.gridwidth = 2;
        add(getBottomPanel(), c);
        aceFrameConfig.addPropertyChangeListener("statusMessage", new StatusChangeListener());

        if (aceFrameConfig.getTabHistoryMap().get("viewerHistoryList") == null) {
            aceFrameConfig.getTabHistoryMap().put("viewerHistoryList", new ArrayList<I_GetConceptData>());
        }

        viewerHistoryTableModel =
                new TerminologyListModel(aceFrameConfig.getTabHistoryMap().get("viewerHistoryList"));
        startupTimer = new javax.swing.Timer(500, new HandleFirstShow());
        startupTimer.setRepeats(false);
        startupTimer.start();
    }

    public void setupSvn() {
        try {
            updateSvnPalette();
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    public void showListView() {
        setShowComponentView(true);
        conceptTabs.setSelectedComponent(conceptListEditor);
    }

    public void showRefsetSpecPanel() {
        setShowComponentView(true);
        conceptTabs.setSelectedComponent(refsetSpecPanel);
    }

    public static void updateAlerts(final I_ConfigAceFrame frameConfig) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                doUpdate(frameConfig);
            }
        });
    }

    private void updateSvnPalette() throws Exception {
        if (subversionPalette == null) {
            JLayeredPane layers = getRootPane().getLayeredPane();

            subversionPalette = new CdePalette(new BorderLayout(), new RightPalettePoint());

            if (svnTabs == null) {
                svnTabs = new JTabbedPane();
            }

            AceLog.getAppLog().info("Subversion entries: " + aceFrameConfig.getSubversionMap().keySet());

            for (String key : aceFrameConfig.getSubversionMap().keySet()) {
                SvnPanel svnTable = new SvnPanel(aceFrameConfig, key);

                svnTabs.addTab(key, svnTable);
            }

            layers.add(subversionPalette, JLayeredPane.PALETTE_LAYER);
            subversionPalette.add(svnTabs, BorderLayout.CENTER);
            subversionPalette.setBorder(BorderFactory.createRaisedBevelBorder());
            subversionPalette.setVisible(false);
        } else {
            HashSet<String> tabTitles = new HashSet<String>();

            if (svnTabs.getTabCount() != aceFrameConfig.getSubversionMap().keySet().size()) {
                for (int i = 0; i < svnTabs.getTabCount(); i++) {
                    tabTitles.add(svnTabs.getTitleAt(i));
                }

                for (String key : aceFrameConfig.getSubversionMap().keySet()) {
                    if (tabTitles.contains(key) == false) {
                        SvnPanel svnTable = new SvnPanel(aceFrameConfig, key);

                        svnTabs.addTab(key, svnTable);
                    }
                }
            }
        }
    }

    private void wrapAndAdd(JPanel defaultsPanel, TermComponentLabel defaultLabel, String borderTitle) {
        JPanel defaultItemPanel = new JPanel(new GridLayout(1, 1));

        defaultItemPanel.setBorder(BorderFactory.createTitledBorder(borderTitle));
        defaultItemPanel.add(defaultLabel);
        defaultsPanel.add(defaultItemPanel);
    }

    //~--- get methods ---------------------------------------------------------

    public static AceConfig getAceConfig() {
        return aceConfig;
    }

    public I_ConfigAceFrame getAceFrameConfig() {
        return aceFrameConfig;
    }

    public JList getAddressList() {
        return addressList;
    }

    public Arena getArena() {
        return arena;
    }

    public JList getBatchConceptList() {
        return batchConceptTable.getList();
    }

    JPanel getBottomPanel() throws IOException {
        JPanel             bottomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c           = new GridBagConstraints();

        c.anchor         = GridBagConstraints.WEST;
        c.gridx          = 0;
        c.gridy          = 0;
        showSearchToggle = new JToggleButton(new ImageIcon(ACE.class.getResource("/32x32/plain/find.png")));
        showSearchToggle.addActionListener(bottomPanelActionListener);
        bottomPanel.add(showSearchToggle, c);
        showSearchToggle.setToolTipText("show/hide search panel");
        showSearchToggle.setSelected(false);
        c.gridx++;
        showSignpostPanelToggle =
                new JToggleButton(new ImageIcon(ACE.class.getResource("/32x32/plain/signpost.png")));
        showSignpostPanelToggle.addActionListener(bottomPanelActionListener);
        showSignpostPanelToggle.addActionListener(new WorkflowDetailsSheetActionListener());
        showSignpostPanelToggle.setVisible(true);
        bottomPanel.add(showSignpostPanelToggle, c);
        showSignpostPanelToggle.setToolTipText("show/hide signpost panel");
        c.gridx++;

        TransporterLabel flashButton =
                new TransporterLabel(new ImageIcon(ACE.class.getResource("/32x32/plain/flash.png")), this);

        bottomPanel.add(flashButton, c);
        c.gridx++;
        bottomPanel.add(new JLabel("  "), c);
        c.gridx++;
        Set<PathBI> editingPathSet = aceFrameConfig.getEditingPathSet();
        PathBI path = editingPathSet.iterator().next();
        JTextField pathDisplay = new JTextField();
        pathDisplay.setEditable(false);
        pathDisplay.setOpaque(false);
        Border outside = BorderFactory.createLineBorder(aceFrameConfig.getColorForPath(path.getConceptNid()), 2);
        Border inside = BorderFactory.createEmptyBorder(1, 2, 1, 2);
        Border border = BorderFactory.createCompoundBorder(outside, inside);
        pathDisplay.setBorder(border);
        pathDisplay.setText(Ts.get().getConceptForNid(path.getConceptNid()).toUserString());
        bottomPanel.add(pathDisplay, c);
        c.gridx++;
        bottomPanel.add(new JLabel("  "), c);
        c.gridx++;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        bottomPanel.add(statusLabel, c);
        c.fill    = GridBagConstraints.NONE;
        c.weightx = 0;
        c.gridx++;
        activityPanel = new JPanel(new GridLayout(1, 1));
        bottomPanel.add(activityPanel, c);
        c.gridx++;
        cancelButton = new JButton("cancel");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(new Abort(conceptListEditor));
        bottomPanel.add(cancelButton, c);
        cancelButton.setVisible(editMode);
        c.gridx++;
        commitButton = new JButton("commit");
        commitButton.setEnabled(false);
        commitButton.addActionListener(new Commit());
        commitButton.setVisible(editMode);
        bottomPanel.add(commitButton, c);
        c.gridx++;
        bottomPanel.add(new JLabel("   "), c);
        c.gridx++;
        signpostPanel = new JPanel();
        signpostPanel.add(new JLabel("Signpost Panel " + new Date()));
        signpostPanel.addComponentListener(new ResizePalettesListener());

        return bottomPanel;
    }

    private Component getCheckboxEditor(String label, String propertyName, boolean initialValue,
            boolean enabled) {
        CheckboxEditor checkBoxEditor = new CheckboxEditor();

        checkBoxEditor.getCustomEditor().setEnabled(enabled);
        checkBoxEditor.setValue(initialValue);
        checkBoxEditor.setPropertyDisplayName(label);
        aceFrameConfig.addPropertyChangeListener("show" + propertyName.toUpperCase().substring(0, 1)
                + propertyName.substring(1), new PropertyListenerGlue("setValue", Object.class,
                        checkBoxEditor));
        checkBoxEditor.addPropertyChangeListener(new PropertyListenerGlue("set"
                + propertyName.toUpperCase().substring(0, 1) + propertyName.substring(1), Boolean.class,
                aceFrameConfig));

        return checkBoxEditor.getCustomEditor();
    }

    public static List<AlertToDataConstraintFailure> getCommitErrorsAndWarnings() {
        List<AlertToDataConstraintFailure> warningsAndErrors = new ArrayList<AlertToDataConstraintFailure>();

        // Commented in the process of moving all checks into BdbCommitManager - ALO
        //    for (I_Transact to : uncommitted) {
        //        for (I_TestDataConstraints test : commitTests) {
        //            try {
        //                for (AlertToDataConstraintFailure failure : test.test(to, true)) {
        //                    warningsAndErrors.add(failure);
        //                }
        //            } catch (Exception e) {
        //                AceLog.getEditLog().alertAndLogException(e);
        //            }
        //        }
        //    }
        return warningsAndErrors;
    }

    /*
     * A class that tracks the focused component. This is necessary to delegate
     * the menu cut/copy/paste commands to the right component. An instance of
     * this class is listening and when the user fires one of these commands, it
     * calls the appropriate action on the currently focused component.
     */
    public Component getConceptListEditor()
            throws IOException, ClassNotFoundException, NoSuchAlgorithmException, TerminologyException {
        if (conceptListEditor == null) {
            if (aceFrameConfig.getTabHistoryMap().get("batchList") == null) {
                aceFrameConfig.getTabHistoryMap().put("batchList", new ArrayList<I_GetConceptData>());
            }

            TerminologyListModel batchListModel =
                    new TerminologyListModel(aceFrameConfig.getTabHistoryMap().get("batchList"));

            batchConceptList  = new TerminologyList(batchListModel, true, true, aceFrameConfig);
            TerminologyTableModel tableModel = new TerminologyTableModel(batchConceptList);
            batchConceptTable = new TerminologyTable(tableModel, aceFrameConfig);

            //         conceptListEditor = new CollectionEditorContainer(batchConceptList, this);
            conceptListEditor = new CollectionEditorContainer(batchConceptTable, this);
            conceptListEditor.setupArena();
        }

        return conceptListEditor;
    }

    public ArrayList<ConceptPanel> getConceptPanels() {
        return conceptPanels;
    }

    public JTabbedPane getConceptTabs() {
        return conceptTabs;
    }

    private JComponent getContentPanel() throws Exception {
        String custUI = (String) ObjectCache.INSTANCE.get(CustomStatics.CUSTOM_UI_CLASS);

        if ((custUI != null) && (custUI.length() > 0)) {
            I_ReturnMainPanel cp = (I_ReturnMainPanel) ObjectCache.INSTANCE.get(custUI);

            return cp.getContentPanel(this);
        } else {
            return getDefaultContentPanel();
        }
    }

    public static Set<I_ReadChangeSet> getCsReaders() {
        return csReaders;
    }

    public static Set<I_WriteChangeSet> getCsWriters() {
        return csWriters;
    }

    public JComponent getDataCheckListScroller() {
        if (dataCheckListScroller == null) {
            dataCheckListModel = new UncommittedListModel();
            dataCheckListPanel = new JPanel(new GridBagLayout());
            dataCheckListModel.addListDataListener(new ListenForDataChecks());
            dataCheckListScroller = new JScrollPane(dataCheckListPanel);
        }

        return dataCheckListScroller;
    }

    public List<TermComponentDataCheckSelectionListener> getDataCheckListeners() {
        return dataCheckListeners;
    }

    private JComponent getDefaultContentPanel() throws Exception {
        treeHelper = new TaxonomyHelper(this.aceFrameConfig, "Ace Taxonomy", null);
        termTree   = treeHelper.getHierarchyPanel();
        treeHelper.addMouseListener(new TaxonomyMouseListenerForAce(this, treeHelper));
        conceptPanels = new ArrayList<ConceptPanel>();
        c1Panel       = new ConceptPanel(HOST_ENUM.CONCEPT_PANEL_R1, aceFrameConfig, linkPref.getR1(),
                conceptTabs, 1, this.pluginRoot);
        c1Panel.setAce(this, linkPref.getR1());
        conceptPanels.add(c1Panel);
        c2Panel = new ConceptPanel(HOST_ENUM.CONCEPT_PANEL_R2, aceFrameConfig, linkPref.getR2(),
                conceptTabs, 2, this.pluginRoot);
        c2Panel.setAce(this, linkPref.getR2());
        conceptPanels.add(c2Panel);
        conceptTabs.addComponentListener(new ResizePalettesListener());

        // CONCEPT TAB R-1
        conceptTabs.addTab("empty", null, c1Panel, "unlinked");

        // CONCEPT TAB R-2
        conceptTabs.addTab("empty", null, c2Panel, "unlinked");

        // CONCEPT TAB R-3
        ConceptPanel c3Panel = new ConceptPanel(HOST_ENUM.CONCEPT_PANEL_R3, aceFrameConfig, linkPref.getR3(),
                conceptTabs, 3, this.pluginRoot);

        c3Panel.setAce(this, linkPref.getR3());
        conceptPanels.add(c3Panel);
        conceptTabs.addTab("empty", null, c3Panel, "Unlinked");

        // CONCEPT TAB R-4
        ConceptPanel c4Panel = new ConceptPanel(HOST_ENUM.CONCEPT_PANEL_R4, aceFrameConfig, linkPref.getR4(),
                conceptTabs, 4, this.pluginRoot);

        c4Panel.setAce(this, linkPref.getR4());
        conceptPanels.add(c4Panel);
        conceptTabs.addTab("empty", null, c4Panel, "Unlinked");

        // LIST TAB
        conceptTabs.addTab("   list   ", new ImageIcon(ACE.class.getResource("/16x16/plain/notebook.png")),
                getConceptListEditor());

        // REFSET SPEC TAB
        refsetTabIndex  = conceptTabs.getTabCount();
        refsetSpecPanel = new RefsetSpecPanel(this);
        refsetSpecPanel.setRefsetInSpecEditor(refsetSpecPanel.getRefsetInSpecEditor());
        conceptTabs.addTab("refSet spec", new ImageIcon(ACE.class.getResource("/16x16/plain/paperclip.png")),
                refsetSpecPanel);

        if (!refsetOnly) {

            // CLASSIFIER TAB
            snoRocketPanel = new SnoRocketTabPanel(this.aceFrameConfig);
            conceptTabs.addTab("classifier",
                    new ImageIcon(ACE.class.getResource("/16x16/plain/chrystal_ball.png")),
                    snoRocketPanel);
            arena = new Arena(this.aceFrameConfig, new File("arena/default.mxe"));
            conceptTabs.addTab("arena", new ImageIcon(ACE.class.getResource("/16x16/plain/eye.png")), arena);
        }

        /*
         */
        conceptTabs.setMinimumSize(new Dimension(0, 0));
        c2Panel.setMinimumSize(new Dimension(0, 0));
        conceptTabs.setSelectedIndex(conceptTabs.indexOfComponent(arena));
        termTreeConceptSplit.setRightComponent(conceptTabs);
        leftTabs.addTab(taxonomyTabLabel, termTree);

        ConceptPanel c5panel = new ConceptPanel(HOST_ENUM.CONCEPT_PANEL_L1, aceFrameConfig, LINK_TYPE.UNLINKED,
                leftTabs, 3, this.pluginRoot);

        c5panel.setAce(this, LINK_TYPE.UNLINKED);
        conceptPanels.add(c5panel);
        leftTabs.addTab("empty", null, c5panel, "Unlinked");
        leftTabs.setMinimumSize(new Dimension(0, 0));
        termTreeConceptSplit.setLeftComponent(leftTabs);
        termTree.setMinimumSize(new Dimension(0, 0));
        termTreeConceptSplit.setOneTouchExpandable(true);
        termTreeConceptSplit.setContinuousLayout(true);
        termTreeConceptSplit.setDividerLocation(aceFrameConfig.getTreeTermDividerLoc());
        termTreeConceptSplit.setResizeWeight(0.5);
        termTreeConceptSplit.setLastDividerLocation(aceFrameConfig.getTreeTermDividerLoc());
        upperLowerSplit.setTopComponent(termTreeConceptSplit);
        upperLowerSplit.setBottomComponent(searchPanel);
        upperLowerSplit.setOneTouchExpandable(true);
        upperLowerSplit.setContinuousLayout(true);
        upperLowerSplit.setResizeWeight(1);
        upperLowerSplit.setLastDividerLocation(500);
        upperLowerSplit.setDividerLocation(2000);
        searchPanel.setMinimumSize(new Dimension(0, 0));

        JPanel content = new JPanel();

        content.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.gridx      = 0;
        c.gridy      = 0;
        c.weightx    = 1;
        c.weighty    = 1;
        c.gridheight = 2;
        c.fill       = GridBagConstraints.BOTH;
        content.add(upperLowerSplit, c);

        return content;
    }

    public JMenu getFileMenu() {
        return fileMenu;
    }

    public JTabbedPane getLeftTabs() {
        return leftTabs;
    }

    public I_HostConceptPlugins getListConceptViewer() {
        return conceptListEditor.getConceptPanel();
    }

    private int getMenuSpacer() {
        if (offset == null) {
            if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
                offset = 49;
            } else {
                offset = 69;
            }
        }

        return offset;
    }

    public String getPluginRoot() {
        return pluginRoot;
    }

    public QueueViewerPanel getQueueViewer() {
        return queueViewer;
    }

    private JPanel getQueueViewerTopPanel() {
        JPanel             listEditorTopPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c                  = new GridBagConstraints();

        c.gridx      = 0;
        c.gridy      = 0;
        c.weightx    = 0;
        c.weighty    = 0;
        c.gridheight = 1;
        c.fill       = GridBagConstraints.BOTH;
        listEditorTopPanel.add(new JLabel(" "), c);    // placeholder for left

        // sided button
        c.weightx = 1.0;
        listEditorTopPanel.add(new JLabel(" "), c);    // filler
        c.gridx++;
        c.weightx = 0.0;

        /*
         *
         * org.dwfa.ace.ACE {
         * showQueueButtons = Boolean.TRUE;
         * }
         *
         */
        boolean showQueueButtons = false;

        try {
            QueueGuiPreferences queuePreferences = new QueueGuiPreferences(TtkPreferences.get());
            showQueueButtons = queuePreferences.showQueueButtons();
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }

        newInboxButton = addActionButton(new NewQueueListener(this), "/24x24/plain/inbox_new.png",
                "Create new inbox and add to profile", listEditorTopPanel, c);
        newInboxButton.setEnabled(showQueueButtons);
        newInboxButton.setVisible(showQueueButtons);
        addExistingInboxButton = addActionButton(new AddQueueListener(this), "/24x24/plain/inbox_add.png",
                "Add existing inbox to profile", listEditorTopPanel, c);
        addExistingInboxButton.setEnabled(showQueueButtons);
        addExistingInboxButton.setVisible(showQueueButtons);
        moveListenerButton = addActionButton(new MoveListener(), "/24x24/plain/outbox_out.png",
                "Take Selected Processes and Save To Disk (no transaction)", listEditorTopPanel, c);
        moveListenerButton.setEnabled(showQueueButtons);
        moveListenerButton.setVisible(showQueueButtons);
        showAllQueuesButton = addActionToggleButton(new ShowAllQueuesListener(),
                "/24x24/plain/funnel_delete.png", "Show all queues", listEditorTopPanel, c, 24);
        showAllQueuesButton.setEnabled(showQueueButtons);
        showAllQueuesButton.setVisible(showQueueButtons);

        return listEditorTopPanel;
    }

    public I_GetConceptData getRefsetInSpecEditor() {
        if (refsetSpecPanel != null) {
            return refsetSpecPanel.getRefsetInSpecEditor();
        }

        return null;
    }

    public RefsetSpecEditor getRefsetSpecEditor() {
        return refsetSpecPanel.getRefsetSpecEditor();
    }

    public I_GetConceptData getRefsetSpecInSpecEditor() throws IOException, TerminologyException {
        if (refsetSpecPanel == null) {
            return null;
        }

        return refsetSpecPanel.getRefsetSpecInSpecEditor();
    }

    public SearchPanel getSearchPanel() {
        return searchPanel;
    }

    public I_DescriptionTuple getSearchResultsSelection() {
        return searchPanel.getSearchResultsSelection();
    }

    public I_ExtendByRef getSelectedRefsetClauseInSpecEditor() {
        return refsetSpecPanel.getSelectedRefsetClauseInSpecEditor();
    }

    public Boolean getShowPromotionCheckBoxes() {
        return refsetSpecPanel.getShowPromotionCheckBoxes();
    }

    public Boolean getShowPromotionFilters() {
        return refsetSpecPanel.getShowPromotionFilters();
    }

    public Boolean getShowPromotionTab() {
        return refsetSpecPanel.getShowPromotionTab();
    }

    public JPanel getSignpostPanel() {
        return signpostPanel;
    }

    public I_ShowActivity getTopActivity() {
        return topActivity;
    }

    private Rectangle getTopBoundsForPalette() {
        Rectangle topBounds   = topPanel.getBounds();
        Point     topLocation = topPanel.getLocation();

        topBounds.y = topBounds.y + topLocation.y;

        return topBounds;
    }

    private JPanel getTopPanel() throws IOException, ClassNotFoundException {
        JPanel topPanel = new JPanel(new GridBagLayout());

        topPanel.setMaximumSize(new Dimension(3000, 48));
        topPanel.setPreferredSize(new Dimension(3000, 48));
        topPanel.setMinimumSize(new Dimension(800, 48));

        GridBagConstraints c = new GridBagConstraints();

        c.anchor          = GridBagConstraints.WEST;
        c.gridx           = 0;
        c.gridy           = 0;
        showHistoryButton =
                new JToggleButton(new ImageIcon(ACE.class.getResource("/32x32/plain/history2.png")));
        showHistoryButton.setFocusable(false);
        if (editMode) {
            showHistoryButton.setToolTipText("history of user commits and concepts viewed");
        } else {
            showHistoryButton.setToolTipText("history of concepts viewed");
        }

        hpal = new HistoryPaletteActionListener();
        showHistoryButton.addActionListener(hpal);

        if (aceFrameConfig.getHiddenTopToggles().contains(TopToggleTypes.HISTORY)) {
            showHistoryButton.setVisible(false);
        }

        topPanel.add(showHistoryButton, c);
        c.gridx++;
        showAddressesButton =
                new JToggleButton(new ImageIcon(ACE.class.getResource("/32x32/plain/address_book3.png")));
        showAddressesButton.setToolTipText("address book of project participants");
        showAddressesButton.setFocusable(false);
        apal = new AddressPaletteActionListener();
        showAddressesButton.addActionListener(apal);
        showAddressesButton.setVisible(editMode);

        if (aceFrameConfig.getHiddenTopToggles().contains(TopToggleTypes.ADDRESS)) {
            showAddressesButton.setVisible(false);
        }

        topPanel.add(showAddressesButton, c);
        c.gridx++;

        // address_book3.png
        topPanel.add(new JPanel(), c);
        c.gridx++;
        c.fill         = GridBagConstraints.NONE;
        c.weightx      = 0;
        showTreeButton = new JToggleButton(new ImageIcon(ACE.class.getResource("/32x32/plain/text_tree.png")));
        showTreeButton.setToolTipText("Show the hierarchy view of the terminology content.");
        showTreeButton.setFocusable(false);
        showTreeButton.setSelected(true);
        showTreeButton.addActionListener(resizeListener);

        if (aceFrameConfig.getHiddenTopToggles().contains(TopToggleTypes.TAXONOMY)) {
            showTreeButton.setVisible(false);
        }

        topPanel.add(showTreeButton, c);
        c.gridx++;
        showComponentButton =
                new JToggleButton(new ImageIcon(ACE.class.getResource("/32x32/plain/components.png")));
        showComponentButton.setToolTipText("Show the component view of the terminology content.");
        showComponentButton.setFocusable(false);
        showComponentButton.setSelected(true);
        showComponentButton.addActionListener(resizeListener);

        if (aceFrameConfig.getHiddenTopToggles().contains(TopToggleTypes.COMPONENT)) {
            showComponentButton.setVisible(false);
        }

        topPanel.add(showComponentButton, c);
        c.gridx++;
        topPanel.add(new JPanel(), c);
        c.gridx++;
        showProgressButton = new JButton(new ImageIcon(ACE.class.getResource("/32x32/plain/gears_view.png")));
        showProgressButton.setFocusable(false);
        showProgressButton.setToolTipText("Show the activity viewer, and bring to the front.");
        showProgressButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                ActivityViewer.toFront();
            }
        });
        topPanel.add(showProgressButton, c);
        c.gridx++;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JPanel oldWorkflowPanel = new JPanel();

        topPanel.add(oldWorkflowPanel, c);
        c.fill    = GridBagConstraints.NONE;
        c.weightx = 0;
        c.gridx++;

        // topPanel.add(getComponentToggles2(), c);
        // c.gridx++;
        File   componentPluginDir = new File(getPluginRoot() + File.separator + "viewer");
        File[] plugins            = componentPluginDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File arg0, String fileName) {
                return fileName.toLowerCase().endsWith(".bp");
            }
        });

        if (plugins != null) {
            c.weightx = 0.0;
            c.weightx = 0.0;
            c.fill    = GridBagConstraints.NONE;

            for (File f : plugins) {
                FileInputStream     fis = new FileInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream   ois = new ObjectInputStream(bis);
                BusinessProcess     bp  = (BusinessProcess) ois.readObject();

                ois.close();

                byte[] iconBytes = (byte[]) bp.readAttachement("button_icon");

                if (iconBytes != null) {
                    ImageIcon icon         = new ImageIcon(iconBytes);
                    JButton   pluginButton = new JButton(icon);
                    pluginButton.setFocusable(false);
                    pluginButton.setToolTipText(bp.getSubject());
                    pluginButton.addActionListener(new PluginListener(f));
                    c.gridx++;
                    topPanel.add(pluginButton, c);
                    AceLog.getAppLog().info("adding viewer plugin: " + f.getName());

                    if (bp.getName().equals("Synchronize with Subversion")) {
                        synchronizeButton = pluginButton;
                        synchronizeButton.setEnabled(Svn.isConnectedToSvn());
                        synchronizeButton.setFocusable(false);
                    }
                } else {
                    JButton pluginButton = new JButton(bp.getName());
                    pluginButton.setFocusable(false);
                    pluginButton.setToolTipText(bp.getSubject());
                    pluginButton.addActionListener(new PluginListener(f));
                    c.gridx++;
                    topPanel.add(pluginButton, c);
                    AceLog.getAppLog().info("adding viewer plugin: " + f.getName());

                    if (bp.getName().equals("Synchronize with Subversion")) {
                        synchronizeButton = pluginButton;
                        synchronizeButton.setEnabled(Svn.isConnectedToSvn());
                        synchronizeButton.setFocusable(false);
                    }
                }
            }
        }

        c.gridx++;
        topPanel.add(new JLabel("   "), c);
        c.gridx++;

        TransporterLabel flashButton =
                new TransporterLabel(new ImageIcon(ACE.class.getResource("/32x32/plain/flash.png")), this);
        flashButton.setFocusable(false);
        topPanel.add(flashButton, c);
        c.gridx++;
        showQueuesButton = new JToggleButton(new ImageIcon(ACE.class.getResource("/32x32/plain/inbox.png")));
        showQueuesButton.setFocusable(false);
        topPanel.add(showQueuesButton, c);
        showQueuesActionListener = new QueuesPaletteActionListener();
        showQueuesButton.addActionListener(showQueuesActionListener);
        showQueuesButton.setToolTipText("Show the queue viewer...");

        if (aceFrameConfig.getHiddenTopToggles().contains(TopToggleTypes.INBOX)) {
            showQueuesButton.setVisible(false);
        }

        c.gridx++;
        showProcessBuilder =
                new JToggleButton(new ImageIcon(ACE.class.getResource("/32x32/plain/cube_molecule.png")));
        showProcessBuilder.setFocusable(false);
        topPanel.add(showProcessBuilder, c);
        showProcessBuilderActionListener = new ProcessPaletteActionListener();
        showProcessBuilder.addActionListener(showProcessBuilderActionListener);
        showProcessBuilder.setToolTipText("Show the process builder...");
        showProcessBuilder.setVisible(editMode);

        if (aceFrameConfig.getHiddenTopToggles().contains(TopToggleTypes.BUILDER)) {
            showProcessBuilder.setVisible(false);
        }

        c.gridx++;
        showSubversionButton = new JToggleButton(new ImageIcon(ACE.class.getResource("/32x32/plain/svn.png")));
        showSubversionButton.setFocusable(false);
        topPanel.add(showSubversionButton, c);
        showSubversionButton.addActionListener(new SubversionPaletteActionListener());
        showSubversionButton.setToolTipText("Show Subversion panel...");

        if (aceFrameConfig.getHiddenTopToggles().contains(TopToggleTypes.SUBVERSION)) {
            showSubversionButton.setVisible(false);
        }

        c.gridx++;
        showPreferencesButton =
                new JToggleButton(new ImageIcon(ACE.class.getResource("/32x32/plain/preferences.png")));
        showPreferencesButton.setFocusable(false);
        preferencesActionListener = new PreferencesPaletteActionListener();
        showPreferencesButton.addActionListener(preferencesActionListener);
        topPanel.add(showPreferencesButton, c);
        showPreferencesButton.setToolTipText("Show preferences panel...");
        showPreferencesButton.setVisible(editMode);

        if (aceFrameConfig.getHiddenTopToggles().contains(TopToggleTypes.PREFERENCES)) {
            showPreferencesButton.setVisible(false);
        }

        c.gridx++;

        return topPanel;
    }

    public TaxonomyTree getTree() {
        return treeHelper.getTree();
    }

    public JTree getTreeInSpecEditor() {
        return refsetSpecPanel.getTreeInSpecEditor();
    }

    public static Set<I_Transact> getUncommitted() {
        return Collections.unmodifiableSet(uncommitted);
    }

    public UncommittedListModel getUncommittedListModel() {
        return dataCheckListModel;
    }

    public WorkflowHistorySearchPanel getWfSearchPanel() {
        return wfSearchPanel;
    }

    public JPanel getWorkflowDetailsPanel() {
        return workflowDetailsSheet;
    }

    public JPanel getWorkflowDetailsSheet() {
        return workflowDetailsSheet;
    }

    public JPanel getWorkflowPanel() {
        return workflowPanel;
    }

    public boolean isAddressToggleVisible() {
        return showAddressesButton.isVisible();
    }

    public boolean isBuilderToggleVisible() {
        return showProcessBuilder.isVisible();
    }

    public boolean isComponentToggleVisible() {
        return showComponentButton.isVisible();
    }

    public boolean isHierarchyToggleVisible() {
        return showTreeButton.isVisible();
    }

    public boolean isHistoryToggleVisible() {
        return showHistoryButton.isVisible();
    }

    public boolean isInboxToggleVisible() {
        return showQueuesButton.isVisible();
    }

    public boolean isPreferencesToggleVisible() {
        return showPreferencesButton.isVisible();
    }

    public boolean isSubversionToggleVisible() {
        return showSubversionButton.isVisible();
    }

    //~--- set methods ---------------------------------------------------------

    public static void setAceConfig(AceConfig aceConfig) {
        if (ACE.aceConfig == null) {
            ACE.aceConfig = aceConfig;
            AceEditor.setAceFrames(aceConfig.aceFrames);
        } else {
            throw new UnsupportedOperationException("Ace.aceConfig is already set");
        }
    }

    public void setAddressToggleVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showAddressesButton.setVisible(visible);
            }
        });
    }

    public void setBuilderToggleVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showProcessBuilder.setVisible(visible);
            }
        });
    }

    public void setCommitAbortButtonsVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                commitButton.setVisible(visible);
                cancelButton.setVisible(visible);
            }
        });
    }

    public void setComponentToggleVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showComponentButton.setVisible(visible);
            }
        });
    }

    public void setEnabledAllQueuesButton(boolean enable) {
        AceLog.getAppLog().info("set enable all queues button: " + enable);

        if (showAllQueuesButton != null) {
            showAllQueuesButton.setEnabled(enable);
            showAllQueuesButton.setVisible(true);
        }
    }

    public void setEnabledExistingInboxButton(boolean enable) {
        AceLog.getAppLog().info("set enable add-existing-inbox button: " + enable);

        if (addExistingInboxButton != null) {
            addExistingInboxButton.setEnabled(enable);
            addExistingInboxButton.setVisible(true);
        }
    }

    public void setEnabledMoveListenerButton(boolean enable) {
        AceLog.getAppLog().info("set enable move-listener button: " + enable);

        if (moveListenerButton != null) {
            moveListenerButton.setEnabled(enable);
            moveListenerButton.setVisible(true);
        }
    }

    public void setEnabledNewInboxButton(boolean enable) {
        AceLog.getAppLog().info("set enable new inbox button: " + enable);

        if (newInboxButton != null) {
            newInboxButton.setEnabled(enable);
            newInboxButton.setVisible(true);
        }
    }

    public void setHierarchyToggleVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showTreeButton.setVisible(visible);
            }
        });
    }

    public void setHistoryToggleVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showHistoryButton.setVisible(visible);
            }
        });
    }

    public void setInboxToggleVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showQueuesButton.setVisible(visible);
            }
        });
    }

    private void setInitialSvnPosition() {
        if (svnPositionSet == false) {
            svnPositionSet = true;

            int       width     = 750;
            int       height    = 550;
            Rectangle topBounds = getTopBoundsForPalette();

            subversionPalette.setSize(width, height);
            subversionPalette.setLocation(new Point(topBounds.x + topBounds.width,
                    topBounds.y + topBounds.height + 1));
            subversionPalette.setOpaque(true);
            subversionPalette.doLayout();
            addComponentListener(subversionPalette);
        }
    }

    public void setPreferencesToggleVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showPreferencesButton.setVisible(visible);
            }
        });
    }

    public void setRefsetInSpecEditor(I_GetConceptData refset) {
        refsetSpecPanel.setRefsetInSpecEditor(refset);
    }

    public void setSelectedPreferencesTab(String tabName) {
        for (int i = 0; i < preferencesTab.getTabCount(); i++) {
            if (preferencesTab.getTitleAt(i).toLowerCase().equals(tabName.toLowerCase())) {
                preferencesTab.setSelectedIndex(i);

                break;
            }
        }
    }

    public void setShowActivityViewer(boolean show) {
        if (show) {
            ActivityViewer.toFront();
        } else {
            ActivityViewer.toBack();
        }
    }

    public void setShowAddresses(boolean show) {
        if (show != showAddressesButton.isSelected()) {
            showAddressesButton.setSelected(show);
            apal.actionPerformed(new ActionEvent(showAddressesButton, 0, "toggle"));
        }
    }

    public void setShowComponentView(boolean show) {
        if (show != showComponentButton.isSelected()) {
            showComponentButton.setSelected(show);
            resizeListener.actionPerformed(new ActionEvent(showComponentButton, 0, "toggle"));
        }
    }

    public void setShowHierarchyView(boolean show) {
        if (show != showTreeButton.isSelected()) {
            showTreeButton.setSelected(show);
            resizeListener.actionPerformed(new ActionEvent(showTreeButton, 0, "toggle"));
        }
    }

    public void setShowHistory(boolean show) {
        if (show != showHistoryButton.isSelected()) {
            showHistoryButton.setSelected(show);
            hpal.actionPerformed(new ActionEvent(showHistoryButton, 0, "toggle"));
        }
    }

    public void setShowPreferences(boolean show) {
        if (show != showPreferencesButton.isSelected()) {
            showPreferencesButton.setSelected(show);
            preferencesActionListener.actionPerformed(new ActionEvent(showPreferencesButton, 0, "toggle"));
        }
    }

    public void setShowProcessBuilder(boolean show) {
        AceLog.getAppLog().info("set show process builder: " + show);

        if (show != showProcessBuilder.isSelected()) {
            showProcessBuilder.setSelected(show);
            showProcessBuilderActionListener.actionPerformed(new ActionEvent(showProcessBuilder, 0, "toggle"));
        }
    }

    public void setShowPromotionCheckBoxes(Boolean show) {
        refsetSpecPanel.setShowPromotionCheckBoxes(show);
    }

    public void setShowPromotionFilters(Boolean show) {
        refsetSpecPanel.setShowPromotionFilters(show);
    }

    public void setShowPromotionTab(Boolean show) {
        refsetSpecPanel.setShowPromotionTab(show);
    }

    public void setShowQueueViewer(boolean show) {
        AceLog.getAppLog().info("set show process builder: " + show);

        if (show != showQueuesButton.isSelected()) {
            showQueuesButton.setSelected(show);
            showQueuesActionListener.actionPerformed(new ActionEvent(showQueuesButton, 0, "toggle"));
        }
    }

    public void setShowSearch(boolean show) {
        if (show != showSearchToggle.isSelected()) {
            showSearchToggle.setSelected(show);
            bottomPanelActionListener.actionPerformed(new ActionEvent(showSearchToggle, 0, "toggle"));
        }
    }

    public void setShowSignpostPanel(boolean show) {
        if (show != showSignpostPanelToggle.isSelected()) {
            showSignpostPanelToggle.setSelected(show);
            bottomPanelActionListener.actionPerformed(new ActionEvent(showSignpostPanelToggle, 0, "toggle"));

            if (show) {
                setShowWorkflowSignpostPanel(false);
            }
        }
    }

    public void setShowSignpostToggleEnabled(boolean enabled) {
        showSignpostPanelToggle.setEnabled(enabled);
    }

    public void setShowSignpostToggleVisible(boolean visible) {
        showSignpostPanelToggle.setVisible(visible);
    }

    public void setShowWorkflowSignpostPanel(boolean show) {
        showWorkflowInSignpostPanel = show;
    }

    public void setSignpostToggleIcon(ImageIcon icon) {
        showSignpostPanelToggle.setIcon(icon);
    }

    public void setSubversionToggleVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showSubversionButton.setVisible(visible);
            }
        });
    }

    public static void setSynchronizeButtonIsEnabled(boolean enabled) {
        if (synchronizeButton != null) {
            synchronizeButton.setEnabled(enabled);
            synchronizeButton.setFocusable(false);
        }
    }

    public void setTopActivity(I_ShowActivity activity) {
        if (this.topActivity != null) {
            swingTimer.removeActionListener(this.topActivity);
        }

        this.topActivity = (ActivityPanel) activity;
        swingTimer.addActionListener(topActivity);
        this.topActivity = (ActivityPanel) activity;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (activityPanel != null) {
                    Component[] children = activityPanel.getComponents();

                    if (children != null) {
                        for (Component child : children) {
                            activityPanel.remove(child);
                        }
                    }

                    if (topActivity != null) {
                        activityPanel.add(topActivity.getViewPanel(false));
                    } else {
                        activityPanel.add(new JPanel());
                    }
                }
            }
        });
    }

    public void setWorfklowDetailSheetVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                workflowDetailsSheet.setVisible(visible);
            }
        });
    }

    public void setWorkflowDetailSheetDimensions(Dimension dim) {
        workflowDetailsSheet.setSize(dim);
    }

    public void setWorkflowDetailsSheet(JPanel workflowDetailsSheet) {
        this.workflowDetailsSheet = workflowDetailsSheet;
    }

    public void setWorkflowPanel(JPanel workflowPanel) {
        this.workflowPanel = workflowPanel;
    }

    //~--- inner classes -------------------------------------------------------

    private class AddressPaletteActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (addressPalette == null) {
                makeAddressPalette();
            }

            if (((JToggleButton) e.getSource()).isSelected()) {
                getRootPane().getLayeredPane().moveToFront(addressPalette);
            }

            addressPalette.togglePalette(((JToggleButton) e.getSource()).isSelected(),
                    TOGGLE_DIRECTION.LEFT_RIGHT);
        }
    }


    private class HandleFirstShow implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    showSearchToggle.doClick();
                    startupTimer = null;
                }
            });
        }
    }


    private class HistoryPaletteActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (historyPalette == null) {
                makeHistoryPalette();
            }

            if (((JToggleButton) e.getSource()).isSelected()) {
                getRootPane().getLayeredPane().moveToFront(historyPalette);
            }

            historyPalette.togglePalette(((JToggleButton) e.getSource()).isSelected(),
                    TOGGLE_DIRECTION.LEFT_RIGHT);
        }
    }


    private class LeftPalettePoint implements I_GetPalettePoint {
        Integer offset;

        //~--- get methods ------------------------------------------------------

        @Override
        public Point getPalettePoint() {
            if (frame != null) {
                JRootPane root = frame.getRootPane();
                Point     p    = new Point(topPanel.getLocation().x, getMenuSpacer());

                if (offset == null) {
                    if ((System.getProperty("exe4j.isInstall4j") != null)
                            && System.getProperty("exe4j.isInstall4j").equals("true")
                            && (System.getProperty("os.name") != null)
                            && System.getProperty("os.name").toLowerCase().startsWith("window")) {
                        System.out.println("Setting offset to 20");
                        offset = 20;
                    } else {
                        System.out.println("Setting offset to 0");
                        offset = 0;
                    }
                }

                p.y = p.y + offset;

                return SwingUtilities.convertPoint(ACE.this, p, root);
            }

            Point p = new Point(topPanel.getLocation().x, getMenuSpacer());

            return SwingUtilities.convertPoint(ACE.this, p, workflowPanel);
        }
    }

    public static void resumeDatacheckDisplay() {
        active.set(true);
        for(WeakReference wr : dataChecks){
            ListenForDataChecks listener = (ListenForDataChecks) wr.get();
            listener.layoutAlerts();
        }
    }

    public static void suspendDatacheckDisplay() {
        active.set(false);
    }
    
    public static boolean datachecksRunning(){
        return active.get();
    }
    
    public class ListenForDataChecks implements ListDataListener, ActionListener {

        public ListenForDataChecks() {
            dataChecks.add(new WeakReference(this));
        }
        
        @Override
        public void actionPerformed(ActionEvent evt) {
            JComboBox comboBox = (JComboBox) evt.getSource();

            if (I_Fixup.class.isAssignableFrom(comboBox.getSelectedItem().getClass())) {
                I_Fixup fixup = (I_Fixup) comboBox.getSelectedItem();

                try {
                    fixup.fix();

                    // ACE.fireCommit();
                } catch (Exception ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
        }

        private void addFocus(JComponent component, final AlertToDataConstraintFailure alert) {
            component.setFocusable(true);
            component.setEnabled(true);
            component.setRequestFocusEnabled(true);
            component.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {

                    // nothing to do
                }
                @Override
                public void mouseEntered(MouseEvent e) {

                    // nothing to do
                }
                @Override
                public void mouseExited(MouseEvent e) {

                    // nothing to do
                }
                @Override
                public void mousePressed(MouseEvent e) {

                    // nothing to do
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    for (TermComponentDataCheckSelectionListener l : dataCheckListeners) {
                        l.setSelection(alert.getConceptDataWithAlert());
                    }
                }
            });

            /*
             * Could not get the focus system working. TODO get focus system
             * working with alerts. component.addFocusListener(new
             * FocusListener() {
             * public void focusGained(FocusEvent e) {
             * AceLog.getAppLog().info("Alert is now focused"); }
             * public void focusLost(FocusEvent e) { // nothing to do...
             * }
             * });
             */
        }

        @Override
        public void contentsChanged(ListDataEvent listEvt) {
            if(active.get()){
                layoutAlerts();
            }
        }

        @Override
        public void intervalAdded(ListDataEvent arg0) {
            if(active.get()){
                layoutAlerts();
            }
        }

        @Override
        public void intervalRemoved(ListDataEvent arg0) {
            if(active.get()){
                layoutAlerts();
            }
        }

        private void layoutAlerts() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (editMode && active.get()) {
                        if (dataCheckListPanel == null) {
                            getDataCheckListScroller();
                        }
                        for (Component component : dataCheckListPanel.getComponents()) {
                            dataCheckListPanel.remove(component);
                        }

                        dataCheckListPanel.setLayout(new GridBagLayout());

                        GridBagConstraints c = new GridBagConstraints();

                        c.gridx      = 0;
                        c.gridy      = 0;
                        c.gridheight = 1;
                        c.gridwidth  = 1;
                        c.anchor     = GridBagConstraints.NORTHWEST;
                        c.fill       = GridBagConstraints.HORIZONTAL;
                        c.weightx    = 1;
                        c.weighty    = 0;

                        int dataCheckIndex = leftTabs.indexOfTab(DATA_CHECK_TAB_LABEL);
                        int taxonomyIndex  = leftTabs.indexOfTab(taxonomyTabLabel);

                        if (dataCheckListModel.size() > 0) {
                            for (AlertToDataConstraintFailure alert : dataCheckListModel) {
                                setupAlert(alert);
                                dataCheckListPanel.add(alert.getRendererComponent(), c);
                                c.gridy++;
                            }

                            c.weighty = 1;
                            c.weightx = 1;
                            c.fill    = GridBagConstraints.BOTH;
                            c.gridy++;
                            dataCheckListPanel.add(new JPanel(), c);
                            dataCheckListScroller.revalidate();
                            dataCheckListScroller.validate();
                            dataCheckListScroller.repaint(0, 0, 2000, 2000);

                            if (dataCheckIndex == -1) {
                                leftTabs.addTab(DATA_CHECK_TAB_LABEL, getDataCheckListScroller());
                                dataCheckIndex = leftTabs.indexOfTab(DATA_CHECK_TAB_LABEL);
                            }

                            leftTabs.setSelectedIndex(dataCheckIndex);

                            if (dataCheckPanel == null) {
                                try {
                                    dataCheckPanel = new ConceptPanel(HOST_ENUM.CONCPET_PANEL_DATA_CHECK,
                                            aceFrameConfig, LINK_TYPE.DATA_CHECK_LINK,
                                            conceptTabs, Integer.MAX_VALUE,
                                            ACE.this.pluginRoot);
                                    dataCheckPanel.setAce(ACE.this, LINK_TYPE.DATA_CHECK_LINK);
                                    conceptPanels.add(dataCheckPanel);
                                } catch (IOException e) {
                                    AceLog.getAppLog().alertAndLogException(e);
                                } catch (ClassNotFoundException e) {
                                    AceLog.getAppLog().alertAndLogException(e);
                                } catch (NoSuchAlgorithmException e) {
                                    AceLog.getAppLog().alertAndLogException(e);
                                } catch (TerminologyException e) {
                                    AceLog.getAppLog().alertAndLogException(e);
                                }
                            }

                            conceptTabs.addTab("Checks", ConceptPanel.SMALL_ALERT_LINK_ICON, dataCheckPanel,
                                    "Data Checks Linked");
                        } else {

                            // leftTabs.setSelectedIndex(taxonomyIndex);
                            if (dataCheckIndex != -1) {
                                leftTabs.removeTabAt(dataCheckIndex);
                            }

                            if (dataCheckPanel != null) {
                                c.weighty = 1;
                                c.gridy++;
                                dataCheckListPanel.add(new JPanel(), c);

                                if (conceptTabs.indexOfComponent(dataCheckPanel) >= 0) {
                                    conceptTabs.remove(dataCheckPanel);
                                    dataCheckPanel.setTermComponent(null);
                                }
                            }
                        }
                    }
                }
            });
        }

        private void setupAlert(AlertToDataConstraintFailure alert) {
            if (alert.getRendererComponent() == null) {
                JLabel label = new JLabel();

                label.setText(alert.getAlertMessage());

                switch (alert.getAlertType()) {
                case ERROR :
                    label.setIcon(AceImages.errorIcon);

                    break;

                case OMG :
                    label.setIcon(AceImages.errorIcon);

                    break;

                case INFORMATIONAL :
                    label.setIcon(AceImages.informationalIcon);

                    break;

                case RESOLVED :
                    label.setIcon(AceImages.resolvedIcon);

                    break;

                case WARNING :
                    label.setIcon(AceImages.warningIcon);

                    break;
                }

                JPanel             componentPanel = new JPanel(new GridBagLayout());
                GridBagConstraints c              = new GridBagConstraints();

                c.gridx      = 0;
                c.gridy      = 0;
                c.gridheight = 1;
                c.gridwidth  = 2;
                c.anchor     = GridBagConstraints.NORTHWEST;
                c.fill       = GridBagConstraints.HORIZONTAL;
                c.weightx    = 1;
                c.weighty    = 0;
                componentPanel.add(label, c);
                c.weightx   = 0;
                c.gridwidth = 1;
                c.gridy++;
                c.anchor = GridBagConstraints.EAST;

                if ((alert.getFixOptions() != null) && (alert.getFixOptions().size() > 0)) {
                    componentPanel.add(new JLabel("fixes: "), c);
                    c.anchor  = GridBagConstraints.WEST;
                    c.weightx = 1;
                    c.gridx++;

                    List<Object> fixList = new ArrayList<Object>();

                    fixList.add(" ");
                    fixList.addAll(alert.getFixOptions());

                    JComboBox testCombo = new JComboBox(fixList.toArray());

                    testCombo.addActionListener(this);
                    componentPanel.add(testCombo, c);
                }

                boolean isSelected = false;

                if (isSelected) {
                    componentPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,
                            1, 1, 1, Color.BLUE), BorderFactory.createEmptyBorder(1, 0, 0, 0)));
                } else {
                    componentPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,
                            0, 1, 0, Color.BLACK), BorderFactory.createEmptyBorder(1, 1, 0, 1)));
                }

                addFocus(componentPanel, alert);
                addFocus(label, alert);
                alert.setRendererComponent(componentPanel);
            }
        }
    }


    private class ManageBottomPaneActionListener implements ActionListener {
        int     lastLocation = 0;
        boolean hidden       = true;

        //~--- methods ----------------------------------------------------------

        @Override
        public void actionPerformed(ActionEvent e) {

            // AceLog.getAppLog().info("bottom panel action: " + e);
            boolean show = showSearchToggle.isSelected() || showSignpostPanelToggle.isSelected();

            if (show) {
                Container shownContainer = null;

                if (showSearchToggle == e.getSource()) {
                    if (showSignpostPanelToggle.isSelected()) {
                        showSignpostPanelToggle.setSelected(false);
                    }

                    int splitLoc = upperLowerSplit.getDividerLocation();

                    upperLowerSplit.setBottomComponent(searchPanel);
                    upperLowerSplit.setDividerLocation(splitLoc);
                    shownContainer = searchPanel;
                } else if (showSignpostPanelToggle == e.getSource()) {
                    if (showSearchToggle.isSelected()) {
                        showSearchToggle.setSelected(false);
                    }

                    int splitLoc = upperLowerSplit.getDividerLocation();

                    if (showWorkflowInSignpostPanel) {
                        upperLowerSplit.setBottomComponent(wfSearchPanel);
                        shownContainer = wfSearchPanel;
                    } else {
                        upperLowerSplit.setBottomComponent(signpostPanel);
                        shownContainer = signpostPanel;
                    }

                    upperLowerSplit.setDividerLocation(splitLoc);
                }

                if (hidden) {

                    // AceLog.getAppLog().info("showing bottom panel");
                    if (lastLocation == 0) {
                        lastLocation = upperLowerSplit.getHeight() - 200;
                    }

                    if (upperLowerSplit.getHeight() - lastLocation < 50) {
                        lastLocation = upperLowerSplit.getHeight() - 200;
                    }

                    upperLowerSplit.setDividerLocation(lastLocation);
                    hidden = false;
                } else {

                    // AceLog.getAppLog().info("bottom panel is already shown");
                }

                while (shownContainer != null) {
                    shownContainer.validate();
                    shownContainer = shownContainer.getParent();
                }
            } else {

                // AceLog.getAppLog().info("hiding bottom panel");
                lastLocation = upperLowerSplit.getDividerLocation();
                upperLowerSplit.setDividerLocation(upperLowerSplit.getHeight());
                hidden = true;
            }

            resizePalttes();

            if (showSearchToggle.isSelected()) {
                searchPanel.focusOnInput();
            }
        }
    }


    public class MoveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            queueViewer.getMoveListener().actionPerformed(evt);
        }
    }


    private class PluginListener implements ActionListener {
        File pluginProcessFile;

        //~--- constructors -----------------------------------------------------

        private PluginListener(File pluginProcessFile) {
            super();
            this.pluginProcessFile = pluginProcessFile;
        }

        //~--- methods ----------------------------------------------------------

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                FileInputStream       fis = new FileInputStream(pluginProcessFile);
                BufferedInputStream   bis = new BufferedInputStream(fis);
                ObjectInputStream     ois = new ObjectInputStream(bis);
                final BusinessProcess bp  = (BusinessProcess) ois.readObject();

                ois.close();
                aceFrameConfig.setStatusMessage("Executing: " + bp.getName());

                final MasterWorker worker = aceFrameConfig.getWorker();

                worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), aceFrameConfig);
                worker.writeAttachment(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name(), this);

                Runnable r = new Runnable() {
                    private String exceptionMessage;
                    @Override
                    public void run() {
                        I_EncodeBusinessProcess process = bp;

                        try {
                            worker.getLogger().log(Level.INFO, "Worker: {0} ({1}) executing process: {2}",
                                    new Object[] { worker.getWorkerDesc(),
                                    worker.getId(), process.getName() });
                            worker.execute(process);

                            SortedSet<ExecutionRecord> sortedRecords =
                                    new TreeSet<ExecutionRecord>(process.getExecutionRecords());
                            Iterator<ExecutionRecord> recordItr = sortedRecords.iterator();
                            StringBuilder             buff      = new StringBuilder();

                            while (recordItr.hasNext()) {
                                ExecutionRecord rec = recordItr.next();

                                buff.append("\n");
                                buff.append(rec.toString());
                            }

                            worker.getLogger().info(buff.toString());
                            exceptionMessage = "";
                        } catch (Throwable e1) {
                            worker.getLogger().log(Level.WARNING, e1.toString(), e1);
                            exceptionMessage = e1.toString();
                        }

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                aceFrameConfig.setStatusMessage("<html><font color='#006400'>execute");

                                if (exceptionMessage.equals("")) {
                                    aceFrameConfig.setStatusMessage("<html>Execution of <font color='blue'>"
                                            + bp.getName() + "</font> complete.");
                                } else {
                                    aceFrameConfig.setStatusMessage(
                                            "<html><font color='blue'>Process complete: <font color='red'>"
                                                    + exceptionMessage);
                                }
                            }
                        });
                    }
                };

                new Thread(r, "ACE-a").start();
            } catch (Exception e1) {
                aceFrameConfig.setStatusMessage("Exception during execution.");
                AceLog.getAppLog().alertAndLogException(e1);
            }
        }
    }


    private class PreferencesPaletteActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (preferencesPalette == null) {
                try {
                    makeConfigPalette();
                } catch (Exception ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }

            if (showPreferencesButton.isSelected()) {
                getRootPane().getLayeredPane().moveToFront(preferencesPalette);
                deselectOthers(showPreferencesButton);
            }

            preferencesPalette.togglePalette(((JToggleButton) e.getSource()).isSelected(),
                    TOGGLE_DIRECTION.LEFT_RIGHT);
        }
    }


    public class ProcessMenuActionListener implements ActionListener {
        private File   processFile;
        private I_Work worker;

        //~--- constructors -----------------------------------------------------

        public ProcessMenuActionListener(File processFile, I_Work worker) {
            super();
            this.processFile = processFile;
            this.worker      = worker;
        }

        //~--- methods ----------------------------------------------------------

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new MenuProcessThread(e.getActionCommand()), "Menu Process Execution").start();
        }

        //~--- inner classes ----------------------------------------------------

        private class MenuProcessThread implements Runnable {
            private String action;

            //~--- constructors --------------------------------------------------

            /**
             * @param action
             */
            public MenuProcessThread(String action) {
                super();
                this.action = action;
            }

            //~--- methods -------------------------------------------------------

            @Override
            public void run() {
                try {
                    ObjectInputStream ois =
                            new ObjectInputStream(new BufferedInputStream(new FileInputStream(processFile)));
                    I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois.readObject();

                    ois.close();

                    if (worker.isExecuting()) {
                        worker = worker.getTransactionIndependentClone();
                    }

                    process.execute(worker);
                    worker.commitTransactionIfActive();
                } catch (Exception ex) {
                    worker.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "<html>Exception processing action: " + action + "<p><p>"
                                    + ex.getMessage() + "<p><p>See log for details.");
                }
            }
        }


        ;
    }


    private class ProcessPaletteActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (processPalette == null) {
                try {
                    makeProcessPalette();
                } catch (Exception ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }

            if (showProcessBuilder.isSelected()) {
                getRootPane().getLayeredPane().moveToFront(processPalette);
                deselectOthers(showProcessBuilder);
            }

            processPalette.setSize(ACE.this.getWidth() - termTreeConceptSplit.getDividerLocation(),
                    conceptTabs.getHeight() + 4);
            processPalette.togglePalette(((JToggleButton) e.getSource()).isSelected(),
                    TOGGLE_DIRECTION.LEFT_RIGHT);
        }
    }


    private class QueuesPaletteActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (queuePalette == null) {
                try {
                    makeQueuePalette();
                } catch (Exception ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }

            if (showQueuesButton.isSelected()) {
                getRootPane().getLayeredPane().moveToFront(queuePalette);
                deselectOthers(showQueuesButton);
            }

            queuePalette.setSize(ACE.this.getWidth() - termTreeConceptSplit.getDividerLocation(),
                    conceptTabs.getHeight() + 4);
            queuePalette.togglePalette(((JToggleButton) e.getSource()).isSelected(),
                    TOGGLE_DIRECTION.LEFT_RIGHT);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    queueViewer.requestFocusOnEntry();
                }
            });
        }
    }


    private class ResizeComponentAdaptor extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            resizePalttes();
        }
    }


    public class ResizePalettesListener implements ComponentListener {
        @Override
        public void componentHidden(ComponentEvent e) {
            resizePalttes();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            resizePalttes();
        }

        @Override
        public void componentResized(ComponentEvent e) {
            resizePalttes();
        }

        @Override
        public void componentShown(ComponentEvent e) {
            resizePalttes();
        }
    }


    private class RightPalettePoint implements I_GetPalettePoint {
        @Override
        public Point getPalettePoint() {
            return new Point(topPanel.getLocation().x + topPanel.getWidth(), getMenuSpacer());
        }
    }


    public class SetRefsetInToggleVisible implements ActionListener {
        TOGGLES      t;
        REFSET_TYPES type;

        //~--- constructors -----------------------------------------------------

        public SetRefsetInToggleVisible(REFSET_TYPES type, TOGGLES t) {
            super();
            this.type = type;
            this.t    = t;
        }

        //~--- methods ----------------------------------------------------------

        @Override
        public void actionPerformed(ActionEvent evt) {
            JToggleButton button = (JToggleButton) evt.getSource();

            aceFrameConfig.setRefsetInToggleVisible(type, t, button.isSelected());
        }
    }


    public class SetToggleVisibleListener implements ActionListener {
        TOGGLES t;

        //~--- constructors -----------------------------------------------------

        public SetToggleVisibleListener(TOGGLES t) {
            super();
            this.t = t;
        }

        //~--- methods ----------------------------------------------------------

        @Override
        public void actionPerformed(ActionEvent evt) {
            JToggleButton button = (JToggleButton) evt.getSource();

            aceFrameConfig.setTogglesInComponentPanelVisible(t, button.isSelected());
        }
    }


    public class ShowAllQueuesListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            JToggleButton showButton = (JToggleButton) evt.getSource();

            aceFrameConfig.setShowAllQueues(showButton.isSelected());

            try {
                queueViewer.refreshQueues();
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
    }


    private class StatusChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            statusLabel.setText((String) evt.getNewValue());
        }
    }


    private class SubversionPaletteActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                updateSvnPalette();
                setInitialSvnPosition();

                if (showSubversionButton.isSelected()) {
                    subversionPalette.setVisible(true);
                    getRootPane().getLayeredPane().moveToFront(subversionPalette);
                    deselectOthers(showSubversionButton);
                }

                subversionPalette.togglePalette(((JToggleButton) e.getSource()).isSelected(),
                        TOGGLE_DIRECTION.LEFT_RIGHT);
            } catch (Exception e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }
        }
    }


    public class TestTupleCalculator implements ActionListener {
        AceConfig aceConfig;

        //~--- constructors -----------------------------------------------------

        public TestTupleCalculator(JFrame aceFrame, AceConfig aceConfig) {
            this.aceConfig = aceConfig;
        }

        //~--- methods ----------------------------------------------------------

        @Override
        public void actionPerformed(ActionEvent e) {
            I_TermFactory tf = Terms.get();

            try {
                I_GetConceptData refsetConcept =
                        tf.getConcept(UUID.fromString("6fd32c1f-8096-40a1-9053-1cc204bc61e3"));

                refsetConcept.getDescTuple(aceFrameConfig.getShortLabelDescPreferenceList(), aceFrameConfig);
                refsetConcept.getDescTuple(aceFrameConfig.getShortLabelDescPreferenceList(), aceFrameConfig);
                refsetConcept.getDescTuple(aceFrameConfig.getShortLabelDescPreferenceList(), aceFrameConfig);
                refsetConcept.getDescTuple(aceFrameConfig.getShortLabelDescPreferenceList(), aceFrameConfig);
                refsetConcept.getDescTuple(aceFrameConfig.getShortLabelDescPreferenceList(), aceFrameConfig);
                refsetConcept.getDescTuple(aceFrameConfig.getShortLabelDescPreferenceList(), aceFrameConfig);
            } catch (TerminologyException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            } catch (IOException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }
        }
    }


    private class TogglePanelsActionListener implements ActionListener, ComponentListener {
        private Rectangle bounds;
        private Integer   dividerLocation;
        private Integer   origWidth;

        //~--- methods ----------------------------------------------------------

        @Override
        public void actionPerformed(ActionEvent e) {
            bounds = getTopLevelAncestor().getBounds();

            if (origWidth == null) {
                getRootPane().addComponentListener(this);
                origWidth = bounds.width;
            }

            if (showComponentButton.isSelected() && (showTreeButton.isSelected() == false)) {
                dividerLocation = termTreeConceptSplit.getDividerLocation();

                // AceLog.getLog().info(dividerLocation);
            }

            if (showTreeButton.isSelected() && (showComponentButton.isSelected() == false)) {
                dividerLocation = termTreeConceptSplit.getDividerLocation();

                // AceLog.getLog().info(dividerLocation);
            }

            if (e.getSource() == showComponentButton) {
                if (showComponentButton.isSelected()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (showTreeButton.isSelected()) {
                                if (dividerLocation < 250) {
                                    dividerLocation = 250;
                                }

                                termTreeConceptSplit.setDividerLocation(dividerLocation);
                            } else {
                                termTreeConceptSplit.setDividerLocation(0);
                            }
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            termTreeConceptSplit.setDividerLocation(3000);

                            if (showTreeButton.isSelected() == false) {
                                showTreeButton.setSelected(true);
                            }
                        }
                    });
                }
            } else if (e.getSource() == showTreeButton) {
                if (showTreeButton.isSelected()) {
                    if (showComponentButton.isSelected()) {
                        if (dividerLocation < 250) {
                            dividerLocation = 250;
                        }

                        termTreeConceptSplit.setDividerLocation(dividerLocation);
                    } else {
                        termTreeConceptSplit.setDividerLocation(3000);
                    }
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            termTreeConceptSplit.setDividerLocation(0);
                            showComponentButton.setSelected(true);
                        }
                    });
                }
            }
        }

        @Override
        public void componentHidden(ComponentEvent e) {}

        @Override
        public void componentMoved(ComponentEvent e) {}

        @Override
        public void componentResized(ComponentEvent e) {
            bounds          = getTopLevelAncestor().getBounds();
            origWidth       = bounds.width;
            dividerLocation = termTreeConceptSplit.getDividerLocation();
        }

        @Override
        public void componentShown(ComponentEvent e) {}
    }


    private class TransferActionListener implements ActionListener, PropertyChangeListener {
        private JComponent focusOwner = null;

        //~--- constructors -----------------------------------------------------

        public TransferActionListener() {
            KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

            manager.addPropertyChangeListener("permanentFocusOwner", this);
        }

        //~--- methods ----------------------------------------------------------

        @Override
        public void actionPerformed(ActionEvent e) {
            if (focusOwner == null) {
                return;
            }

            String action = (String) e.getActionCommand();
            Action a      = focusOwner.getActionMap().get(action);

            if (a != null) {
                a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
            } else {
                AceLog.getAppLog().info("No action: " + action + " for: " + focusOwner);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            Object o = e.getNewValue();

            if (o instanceof JComponent) {
                focusOwner = (JComponent) o;
            } else {
                focusOwner = null;
            }
        }
    }

    private class WorkflowDetailsSheetActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {}
    }
}
