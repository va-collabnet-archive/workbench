/*
 * Created by JFormDesigner on Thu Jun 17 16:31:58 GMT-03:00 2010
 */

package org.ihtsdo.translation.ui.config;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.ihtsdo.translation.ui.ConfigTranslationModule;

/**
 * @author Guillermo Reynoso
 */
public class ConfigDialog extends JDialog {

	private static final long serialVersionUID = 3579653366151472214L;
	/** The config. */
	private EditorModePanel editorModePanel;
	private SimilarityDefaultSettingsPanel similarityDefaultSettingPanel;
	private FsnGenerationStrategyPanel fsnGenStrategyPanel;
	private SourceTreeComponentsPanel sourceTreeComponentsPanel;
	private TargetTreeComponentsPanel targetTreeComponentsPanel;
	private InboxColumnComponentsPanel inboxColumnComponentsPanel;
	private IcsGenerationStrategyPanel icsConGenerationStrategyPanel;
	private InboxItemConfigurationPanel inboxItemConfigurationPanel;
	private TranslationIssuesRepositoryIdsPanel translationIssuesRepositoryIdsPanel;
	private FileLinkConfigurationPanel fileLinkConfigurationPanel;
	private TranslatorDefaultEditorModePanel translatorDefaultEditorModePanel;
	private SpellCheckerConfigPanel spellCheckConfigPanel;
	private PreferedTermDefaultPanel preferdTermDefaultPanel;

	private boolean isProjectConfiguration;
	private ConfigTranslationModule confTrans;
	private I_ConfigAceFrame config;

	public ConfigDialog(I_ConfigAceFrame config, ConfigTranslationModule confTrans, boolean isProjectConfiguration, String projectName) {
		super();
		this.isProjectConfiguration = isProjectConfiguration;
		this.config = config;
		this.confTrans = confTrans;

		if (projectName != null) {
			setTitle("Translation preferences for " + projectName);
		} else {
			setTitle("Translation preferences");
		}
		setPreferredSize(new Dimension(640, 480));
		setLocation(150, 150);

		// ================Initialize Configuration Panels=============
		initComponents();
		initCustomComponents();

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close(null);
			}
		});

	}

	public JPanel getPanel() {
		return (JPanel) this.getContentPane();
	}

	/**
	 * Opens the configuration dialog<br>
	 * 
	 * @return The ConfigTranslationModule if configuration was accepted, <br>
	 *         else returns null
	 */
	public ConfigTranslationModule showModalDialog() {
		setModal(true);
		pack();
		setVisible(true);
		return this.confTrans;
	}

	private void close(ConfigTranslationModule canceled) {
		this.confTrans = canceled;
		dispose();
	}

	private void initCustomComponents() {

		// =================== CONFIG TREE RENDERER CONFIGURATION
		// ===================
		DefaultTreeCellRenderer rend = new DefaultTreeCellRenderer();
		rend.setOpenIcon(null);
		rend.setClosedIcon(null);
		rend.setLeafIcon(null);
		rend.setIconTextGap(-4);
		configTree.setCellRenderer(rend);
		configTree.setRowHeight(15);

		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Config");
		createNodes(top);
		DefaultTreeModel model = new DefaultTreeModel(top);
		configTree.setModel(model);

		// Selects the first node by default
		configTree.setSelectionRow(0);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) configTree.getLastSelectedPathComponent();

		title.setText(((ConfigTreeInfo) node.getUserObject()).getName());
		title.setBorder(new EmptyBorder(new Insets(5, 20, 5, 0)));

		scrollPane1.setPreferredSize(new Dimension(230, 300));
		scrollPane1.setMinimumSize(new Dimension(230, 300));

		splitPane1.getLeftComponent().setMinimumSize(new Dimension(230, 300));
		splitPane1.setDividerSize(1);

		contentPanel.setMinimumSize(new Dimension(300, 300));

		TreeSelectionListener sl = new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) configTree.getLastSelectedPathComponent();
				if (node == null) {
					return;
				}
				Object nodeInfo = node.getUserObject();
				ConfigTreeInfo conf = (ConfigTreeInfo) nodeInfo;
				title.setText(conf.getName());
				try {
					CardLayout cardLo = (CardLayout) contentPanel.getLayout();
					if (conf.getItem().equals(ConfigurationConstants.EDITOR_MODE_PANEL)) {
						editorModePanel.selectCurrentConfButton();
						cardLo.show(contentPanel, ConfigurationConstants.EDITOR_MODE_PANEL);
					} else if (conf.getItem().equals(ConfigurationConstants.SIMILARITY_DEFAULT_SETTING_PANEL)) {
						similarityDefaultSettingPanel.selectCurrentConfButton();
						cardLo.show(contentPanel, ConfigurationConstants.SIMILARITY_DEFAULT_SETTING_PANEL);
					} else if (conf.getItem().equals(ConfigurationConstants.FSN_GENERATION_STRATEGY_PANEL)) {
						fsnGenStrategyPanel.selectCurrentConfButton();
						cardLo.show(contentPanel, ConfigurationConstants.FSN_GENERATION_STRATEGY_PANEL);
					} else if (conf.getItem().equals(ConfigurationConstants.SOURCE_TREE_COMPONENTS_PANEL)) {
						sourceTreeComponentsPanel.initializeLists();
						cardLo.show(contentPanel, ConfigurationConstants.SOURCE_TREE_COMPONENTS_PANEL);
					} else if (conf.getItem().equals(ConfigurationConstants.TARGET_TREE_COMPONENTS_PANEL)) {
						sourceTreeComponentsPanel.initializeLists();
						cardLo.show(contentPanel, ConfigurationConstants.TARGET_TREE_COMPONENTS_PANEL);
					} else if (conf.getItem().equals(ConfigurationConstants.INBOX_COLUMN_COMPONENTS_PANEL)) {
						inboxColumnComponentsPanel.initializeLists();
						cardLo.show(contentPanel, ConfigurationConstants.INBOX_COLUMN_COMPONENTS_PANEL);
					} else if (conf.getItem().equals(ConfigurationConstants.ICS_GENERATION_STRATEGY_PANEL)) {
						icsConGenerationStrategyPanel.selectCurrentConfButton();
						cardLo.show(contentPanel, ConfigurationConstants.ICS_GENERATION_STRATEGY_PANEL);
					} else if (conf.getItem().equals(ConfigurationConstants.INBOX_ITEM_CONFIGURATION_PANEL)) {
						inboxItemConfigurationPanel.selectCurrentConfButton();
						cardLo.show(contentPanel, ConfigurationConstants.INBOX_ITEM_CONFIGURATION_PANEL);
					} else if (conf.getItem().equals(ConfigurationConstants.TRANSLATION_ISSUE_REPOSITORIES)) {
						// inboxItemConfigurationPanel.selectCurrentConfButton();
						cardLo.show(contentPanel, ConfigurationConstants.TRANSLATION_ISSUE_REPOSITORIES);
					} else if (conf.getItem().equals(ConfigurationConstants.FILE_LINK_CONFIGURATION_PANEL)) {
						// inboxItemConfigurationPanel.selectCurrentConfButton();
						cardLo.show(contentPanel, ConfigurationConstants.FILE_LINK_CONFIGURATION_PANEL);
					} else if (conf.getItem().equals(ConfigurationConstants.TRANSLATOR_DEFAULT_EDITOR_MODE)) {
						translatorDefaultEditorModePanel.revertSelections();
						cardLo.show(contentPanel, ConfigurationConstants.TRANSLATOR_DEFAULT_EDITOR_MODE);
					} else if (conf.getItem().equals(ConfigurationConstants.SPELL_CHECK_CONFIGURATION_PANEL)) {
						spellCheckConfigPanel.selectCurrentConfButton();
						cardLo.show(contentPanel, ConfigurationConstants.SPELL_CHECK_CONFIGURATION_PANEL);
					} else if (conf.getItem().equals(ConfigurationConstants.PREFERD_TERM_DEFAULT_PANEL)) {
						preferdTermDefaultPanel.selectCurrentConfButton();
						cardLo.show(contentPanel, ConfigurationConstants.PREFERD_TERM_DEFAULT_PANEL);
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		configTree.addTreeSelectionListener(sl);
		// =================== CONFIGURATIOIN PANELS INITIALIZATION
		// ===================

		if (!isProjectConfiguration) {
			inboxColumnComponentsPanel = new InboxColumnComponentsPanel(this.config, this.confTrans);
			inboxColumnComponentsPanel.initializeLists();
			inboxItemConfigurationPanel = new InboxItemConfigurationPanel(config, this.confTrans);
			sourceTreeComponentsPanel = new SourceTreeComponentsPanel(config, this.confTrans);
			targetTreeComponentsPanel = new TargetTreeComponentsPanel(config, this.confTrans);
			spellCheckConfigPanel = new SpellCheckerConfigPanel(config, confTrans);
			preferdTermDefaultPanel = new PreferedTermDefaultPanel(config, confTrans);

			contentPanel.add(inboxColumnComponentsPanel, ConfigurationConstants.INBOX_COLUMN_COMPONENTS_PANEL);
			contentPanel.add(inboxItemConfigurationPanel, ConfigurationConstants.INBOX_ITEM_CONFIGURATION_PANEL);
			contentPanel.add(preferdTermDefaultPanel, ConfigurationConstants.PREFERD_TERM_DEFAULT_PANEL);
			contentPanel.add(sourceTreeComponentsPanel, ConfigurationConstants.SOURCE_TREE_COMPONENTS_PANEL);
			contentPanel.add(spellCheckConfigPanel, ConfigurationConstants.SPELL_CHECK_CONFIGURATION_PANEL);
			contentPanel.add(targetTreeComponentsPanel, ConfigurationConstants.TARGET_TREE_COMPONENTS_PANEL);
		} else {
			editorModePanel = new EditorModePanel(config, this.confTrans);
			editorModePanel.selectCurrentConfButton();
			similarityDefaultSettingPanel = new SimilarityDefaultSettingsPanel(config, this.confTrans);
			fsnGenStrategyPanel = new FsnGenerationStrategyPanel(config, this.confTrans);
			icsConGenerationStrategyPanel = new IcsGenerationStrategyPanel(config, this.confTrans);
			translationIssuesRepositoryIdsPanel = new TranslationIssuesRepositoryIdsPanel(config, this.confTrans);
			fileLinkConfigurationPanel = new FileLinkConfigurationPanel(config, this.confTrans);
			translatorDefaultEditorModePanel = new TranslatorDefaultEditorModePanel(config, this.confTrans);

			contentPanel.add(editorModePanel, ConfigurationConstants.EDITOR_MODE_PANEL);
			contentPanel.add(fileLinkConfigurationPanel, ConfigurationConstants.FILE_LINK_CONFIGURATION_PANEL);
			contentPanel.add(fsnGenStrategyPanel, ConfigurationConstants.FSN_GENERATION_STRATEGY_PANEL);
			contentPanel.add(icsConGenerationStrategyPanel, ConfigurationConstants.ICS_GENERATION_STRATEGY_PANEL);
			contentPanel.add(similarityDefaultSettingPanel, ConfigurationConstants.SIMILARITY_DEFAULT_SETTING_PANEL);
			contentPanel.add(translationIssuesRepositoryIdsPanel, ConfigurationConstants.TRANSLATION_ISSUE_REPOSITORIES);
			contentPanel.add(translatorDefaultEditorModePanel, ConfigurationConstants.TRANSLATOR_DEFAULT_EDITOR_MODE);
		}

		// =================== CLOSE BUTTONS ====================
		configCancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				close(null);
			}

		});

		acceptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				close(confTrans);
			}
		});

	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel2 = new JPanel();
		acceptButton = new JButton();
		configCancelButton = new JButton();
		splitPane1 = new JSplitPane();
		scrollPane1 = new JScrollPane();
		configTree = new JTree();
		panel1 = new JPanel();
		titlePanel = new JPanel();
		title = new JLabel();
		contentPanel = new JPanel();

		// ======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		// ======== panel2 ========
		{
			panel2.setLayout(new FlowLayout(FlowLayout.RIGHT));

			// ---- acceptButton ----
			acceptButton.setText("Accept");
			panel2.add(acceptButton);

			// ---- configCancelButton ----
			configCancelButton.setText("Close");
			panel2.add(configCancelButton);
		}
		contentPane.add(panel2, BorderLayout.SOUTH);

		// ======== splitPane1 ========
		{
			splitPane1.setEnabled(false);

			// ======== scrollPane1 ========
			{
				scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

				// ---- configTree ----
				configTree.setRootVisible(false);
				configTree.setShowsRootHandles(true);
				scrollPane1.setViewportView(configTree);
			}
			splitPane1.setLeftComponent(scrollPane1);

			// ======== panel1 ========
			{
				panel1.setLayout(new BorderLayout());

				// ======== titlePanel ========
				{
					titlePanel.setBorder(new EtchedBorder());
					titlePanel.setLayout(new GridBagLayout());
					((GridBagLayout) titlePanel.getLayout()).columnWidths = new int[] { 0, 0 };
					((GridBagLayout) titlePanel.getLayout()).rowHeights = new int[] { 29, 0 };
					((GridBagLayout) titlePanel.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
					((GridBagLayout) titlePanel.getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };
					titlePanel.add(title, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				}
				panel1.add(titlePanel, BorderLayout.NORTH);

				// ======== contentPanel ========
				{
					contentPanel.setBorder(new EtchedBorder());
					contentPanel.setLayout(new CardLayout());
				}
				panel1.add(contentPanel, BorderLayout.CENTER);
			}
			splitPane1.setRightComponent(panel1);
		}
		contentPane.add(splitPane1, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel panel2;
	private JButton acceptButton;
	private JButton configCancelButton;
	private JSplitPane splitPane1;
	private JScrollPane scrollPane1;
	private JTree configTree;
	private JPanel panel1;
	private JPanel titlePanel;
	private JLabel title;
	private JPanel contentPanel;

	// JFormDesigner - End of variables declaration //GEN-END:variables

	private void createNodes(DefaultMutableTreeNode top) {
		DefaultMutableTreeNode category = null;
		if (!isProjectConfiguration) {
			category = new DefaultMutableTreeNode(new ConfigTreeInfo("Inbox columns display", ConfigurationConstants.INBOX_COLUMN_COMPONENTS_PANEL));
			top.add(category);

			category = new DefaultMutableTreeNode(new ConfigTreeInfo("Inbox item configuration", ConfigurationConstants.INBOX_ITEM_CONFIGURATION_PANEL));
			top.add(category);

			category = new DefaultMutableTreeNode(new ConfigTreeInfo("Preferd term default mode", ConfigurationConstants.PREFERD_TERM_DEFAULT_PANEL));
			top.add(category);
			
			category = new DefaultMutableTreeNode(new ConfigTreeInfo("Source descriptions display", ConfigurationConstants.SOURCE_TREE_COMPONENTS_PANEL));
			top.add(category);

			category = new DefaultMutableTreeNode(new ConfigTreeInfo("Spell check configuration", ConfigurationConstants.SPELL_CHECK_CONFIGURATION_PANEL));
			top.add(category);

			category = new DefaultMutableTreeNode(new ConfigTreeInfo("Target descriptions display", ConfigurationConstants.TARGET_TREE_COMPONENTS_PANEL));
			top.add(category);
		} else {
			category = new DefaultMutableTreeNode(new ConfigTreeInfo("Editor mode", ConfigurationConstants.EDITOR_MODE_PANEL));
			top.add(category);

			category = new DefaultMutableTreeNode(new ConfigTreeInfo("File link configuration", ConfigurationConstants.FILE_LINK_CONFIGURATION_PANEL));
			top.add(category);

			category = new DefaultMutableTreeNode(new ConfigTreeInfo("FSN generation strategy", ConfigurationConstants.FSN_GENERATION_STRATEGY_PANEL));
			top.add(category);

			category = new DefaultMutableTreeNode(new ConfigTreeInfo("Ics generation strategy", ConfigurationConstants.ICS_GENERATION_STRATEGY_PANEL));
			top.add(category);

			category = new DefaultMutableTreeNode(new ConfigTreeInfo("Similarity default setting", ConfigurationConstants.SIMILARITY_DEFAULT_SETTING_PANEL));
			top.add(category);

			category = new DefaultMutableTreeNode(new ConfigTreeInfo("Translation issue repositories", ConfigurationConstants.TRANSLATION_ISSUE_REPOSITORIES));
			top.add(category);

			category = new DefaultMutableTreeNode(new ConfigTreeInfo("Translator default editor mode", ConfigurationConstants.TRANSLATOR_DEFAULT_EDITOR_MODE));
			top.add(category);
		}

	}

}
