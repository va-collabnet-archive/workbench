/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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

package org.ihtsdo.project.view.issue;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.apache.xalan.trace.SelectionEvent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.issue.manager.implementation.CollabnetIssueManager;
import org.ihtsdo.project.model.TranslationProject;

/**
 * The Class IssuesListPanel.
 */
public class IssuesListPanel2 extends JPanel implements PropertyChangeListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The config. */
	private I_ConfigAceFrame config;

	/** The issue repo. */
	private IssueRepository issueRepo;

	/** The concept. */
	private I_GetConceptData concept;

	/** The regis. */
	private IssueRepoRegistration regis;

	/** The read only mode. */
	private boolean readOnlyMode;

	private TranslationProject project;

	public IssuesListPanel2(){
		super();
		try {
			initComponents();
			this.readOnlyMode = false;
			config = Terms.get().getActiveAceFrameConfig();
			Dimension minimumSize = new Dimension(3, 3);
			Dimension maximumSize = new Dimension(350, 350);
			splitPane1.getLeftComponent().setMinimumSize(minimumSize);
			splitPane1.getLeftComponent().setPreferredSize(minimumSize);
			splitPane1.getLeftComponent().setMaximumSize(maximumSize);
			splitPane1.getRightComponent().setMinimumSize(minimumSize);
			splitPane1.getRightComponent().setPreferredSize(minimumSize);
			splitPane1.getRightComponent().setMaximumSize(maximumSize);
			addListeners();
			
			initCustomComponents();
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}
	
	/**
	 * Instantiates a new issues list panel.
	 * 
	 * @param readOnlyMode
	 *            the read only mode
	 * @throws Exception
	 *             the exception
	 */
	public IssuesListPanel2(boolean readOnlyMode) throws Exception {
		initComponents();
		this.readOnlyMode = readOnlyMode;
		config = Terms.get().getActiveAceFrameConfig();
		Dimension minimumSize = new Dimension(3, 3);
		Dimension maximumSize = new Dimension(350, 350);
		splitPane1.getLeftComponent().setMinimumSize(minimumSize);
		splitPane1.getLeftComponent().setPreferredSize(minimumSize);
		splitPane1.getLeftComponent().setMaximumSize(maximumSize);
		splitPane1.getRightComponent().setMinimumSize(minimumSize);
		splitPane1.getRightComponent().setPreferredSize(minimumSize);
		splitPane1.getRightComponent().setMaximumSize(maximumSize);
		addListeners();

		initCustomComponents();
	}

	/**
	 * Instantiates a new issues list panel.
	 * 
	 * @param config
	 *            the config
	 * @param readOnlyMode
	 *            the read only mode
	 * @throws Exception
	 *             the exception
	 */
	public IssuesListPanel2(I_ConfigAceFrame config, boolean readOnlyMode) throws Exception {
		// this.termFactory=termFactory;
		initComponents();
		this.readOnlyMode = readOnlyMode;
		this.config = config;
		this.config.addPropertyChangeListener("commit", this);
		// loadRepos(null);
		addListeners();
		initComponents();
	}

	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {
		bCreateIssue.setEnabled(true && !readOnlyMode);
	}

	/**
	 * Adds the listeners.
	 */
	private void addListeners() {
		SelectionListener listener = new SelectionListener(table1);
		table1.getSelectionModel().addListSelectionListener(listener);
		table1.addMouseListener(new JTableMouselistener(table1));
		table1.getSelectionModel().addListSelectionListener(listener);
	}

	private IssueRepository getIssueRepo() {
		return this.issueRepo;
	}

	/**
	 * Creates the issue.
	 * 
	 * @param concept
	 *            the concept
	 */
	public void createIssue(I_GetConceptData concept) {
		this.concept = concept;
		showNewIssuePanel();
	}

	/**
	 * Load issues.
	 * 
	 * @param concept
	 *            the concept
	 * @param repo
	 *            the repo
	 * @param regis
	 *            the regis
	 * @return the integer
	 * @throws Exception 
	 * @throws IOException 
	 * @throws TerminologyException 
	 */
	public Integer loadIssues(I_GetConceptData concept, TranslationProject project, boolean readOnlyMode, I_ConfigAceFrame config) throws TerminologyException, IOException, Exception {
		this.config = config;
		this.concept = concept;
		this.project = project;
		this.issueRepo = IssueRepositoryDAO.getIssueRepository(project.getProjectIssueRepo());
		IssueRepoRegistration regis = null;
		if(project.getProjectIssueRepo() != null){
			IssueRepository repo = IssueRepositoryDAO.getIssueRepository(project.getProjectIssueRepo());
			if(repo!= null){
				regis = IssueRepositoryDAO.getRepositoryRegistration(repo.getUuid(), config);
				this.regis = regis;
			}
		}
		bCreateIssue.setEnabled(false);
		List<Issue> issueL = new ArrayList<Issue>();
		if (concept != null) {
			getIssueRepo();
			if (issueRepo != null && issueRepo.getType() == IssueRepository.REPOSITORY_TYPE.WEB_SITE.ordinal()) {
				CollabnetIssueManager cIM = new CollabnetIssueManager();
				bCreateIssue.setEnabled(true && !readOnlyMode);
				try {
					cIM.openRepository(issueRepo, getSiteUserName(), getSiteUserPassword());
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
					message("Sorry, cannot connect to repository.\n" + e.getMessage());
					return 0;
				}
				try {
					issueL = cIM.getIssuesForComponentId(this.concept.getUids().iterator().next().toString());
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
					message("Sorry, cannot retrieve comments for this issue.\n" + e.getMessage());
					return 0;
				}
			}
		}

		if (issueCommentsPanel1 != null)
			issueCommentsPanel1.clear();
		loadIssuesTable(issueL);
		return issueL.size();
	}
	public void loadIssues() {
		
		
	}

	/**
	 * Gets the site user password.
	 * 
	 * @return the site user password
	 */
	private String getSiteUserPassword() {
		return regis.getPassword();
	}

	/**
	 * Gets the site user name.
	 * 
	 * @return the site user name
	 */
	private String getSiteUserName() {
		return regis.getUserId();
	}

	/**
	 * Show new issue panel.
	 */
	public void showNewIssuePanel() {

		getIssueRepo();
		IssuesPanel2 iPanel;
		iPanel = new IssuesPanel2();

		int action = JOptionPane.showOptionDialog(null, iPanel, "Enter new Issue", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

		this.requestFocus();

		if (action == JOptionPane.CANCEL_OPTION) {
			return;
		}
		if (iPanel.getIssueTitle().trim().equals("")) {
			message("Cannot create a Issue without title.");
			return;
		}
		if (iPanel.getIssueDescription().trim().equals("")) {
			message("Cannot create a Issue without description.");
			return;
		}

		try {
			if (issueRepo.getType() == IssueRepository.REPOSITORY_TYPE.WEB_SITE.ordinal()) {
				CollabnetIssueManager im = new CollabnetIssueManager();

				try {
					im.openRepository(issueRepo, getSiteUserName(), getSiteUserPassword());
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
					message("Sorry, cannot connect to repository.\n" + e.getMessage());
					return;
				}
				Issue issue = new Issue();

				issue.setCategory(Issue.CATEGORY.Source_Error.name());
				issue.setComponent(concept.getInitialText());
				issue.setComponentId(concept.getUids().iterator().next().toString());
				issue.setDescription(iPanel.getIssueDescription().trim());
				// issue.setExternalId(lblExtId.getText());
				issue.setPriority(String.valueOf(Issue.PRIORITY.DEFAULT.ordinal()));
				issue.setProjectId(issueRepo.getExternalProjectId());
				issue.setDownloadStatus(Issue.STATUS.Open.name());
				issue.setTitle(iPanel.getIssueTitle().trim());
				issue.setExternalUser(config.getUsername());
				issue.setUser(getSiteUserName());

				im.openRepository(issueRepo, getSiteUserName(), getSiteUserPassword());
				im.postNewIssue(issue);
				message("Issue posted");
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						try{
							loadIssues(concept, project, readOnlyMode, config);
						}catch (Exception e) {
							AceLog.getAppLog().alertAndLogException(e);
						}

					}

				});
			}
		} catch (Exception e) {
			message("Error:" + e.getMessage());
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * The listener interface for receiving tableItemAction events. The class
	 * that is interested in processing a tableItemAction event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's
	 * <code>addTableItemActionListener<code> method. When
	 * the tableItemAction event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see TableItemActionEvent
	 */
	class TableItemActionListener implements ActionListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e) {

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					showNewIssuePanel();

				}

			});
		}
	}

	/**
	 * The Class JTableMouselistener.
	 */
	public class JTableMouselistener extends MouseAdapter {

		/** The j table. */
		private JTable jTable;

		/** The menu. */
		private JPopupMenu menu;

		/** The m item listener. */
		private TableItemActionListener mItemListener;

		/** The m item. */
		private JMenuItem mItem;

		/** The x point. */
		private int xPoint;

		/** The y point. */
		private int yPoint;

		/**
		 * Instantiates a new j table mouselistener.
		 * 
		 * @param jTable
		 *            the j table
		 */
		JTableMouselistener(JTable jTable) {
			this.jTable = jTable;
			menu = new JPopupMenu();
			mItem = new JMenuItem();
			mItemListener = new TableItemActionListener();
			mItem.addActionListener(mItemListener);
			menu.add(mItem);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {

			if (e.getButton() == java.awt.event.MouseEvent.BUTTON3) {
				if (concept != null) {
					xPoint = e.getX();
					yPoint = e.getY();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							menu.show(jTable, xPoint, yPoint);
						}
					});
				}

			}
		}

	}

	/**
	 * Load issues table.
	 * 
	 * @param issueL
	 *            the issue l
	 */
	private void loadIssuesTable(List<Issue> issueL) {
		String[] columnNames = { "Title", "Description", "Status", "User" };
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		for (int i = 0; i < issueL.size(); i++) {
			tableModel.addRow(new Object[] { new ListObj("I", issueL.get(i).getTitle(), issueL.get(i)), issueL.get(i).getDescription(), issueL.get(i).getDownloadStatus(), issueL.get(i).getExternalUser() });
		}

		table1.setModel(tableModel);
		TableColumnModel cmodel = table1.getColumnModel();
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(0).setCellRenderer(textAreaRenderer);
		cmodel.getColumn(1).setCellRenderer(textAreaRenderer);
		cmodel.getColumn(2).setCellRenderer(textAreaRenderer);
		cmodel.getColumn(3).setCellRenderer(textAreaRenderer);
		table1.revalidate();

	}

	/**
	 * Message.
	 * 
	 * @param string
	 *            the string
	 */
	private void message(String string) {

		JOptionPane.showOptionDialog(this, string, "Information", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
	}

	/**
	 * Load repos.
	 * 
	 * @param cmb
	 *            the cmb
	 * @param object
	 *            the object
	 * @param name
	 *            the name
	 * @param selected
	 *            the selected
	 */

	/**
	 * Adds the object to combo.
	 * 
	 * @param cmb
	 *            the cmb
	 * @param object
	 *            the object
	 * @param name
	 *            the name
	 * @param selected
	 *            the selected
	 */
	private void addObjectToCombo(JComboBox cmb, Object object, String name, boolean selected) {
		// TODO Auto-generated method stub
		ListObj lo = new ListObj("P", name, object);
		cmb.addItem(lo);
		if (selected)
			cmb.setSelectedItem(lo);
	}

	/**
	 * B create issue action performed.
	 */
	private void bCreateIssueActionPerformed() {
		showNewIssuePanel();
	}

	private void table1MouseClicked(MouseEvent e) {
		int first = table1.getSelectedRow();
		if (first > -1) {
			Object row = table1.getModel().getValueAt(first, 0);
			if (row != null) {
				Issue issue = (Issue) ((ListObj) row).getAtrValue();
				if (issue != null && issueRepo != null && regis != null) {
					issueCommentsPanel1.setInit(issue, issueRepo, regis, config.getUsername(), readOnlyMode);
				}
			}
		}
	}

	/**
	 * Inits the components.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private void initComponents() throws Exception {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		splitPane1 = new JSplitPane();
		panel3 = new JPanel();
		panel1 = new JPanel();
		panel2 = new JPanel();
		bCreateIssue = new JButton();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		panel4 = new JPanel();
		issueCommentsPanel1 = new IssueCommentsPanel();

		// ======== this ========
		setLayout(new BorderLayout());

		// ======== splitPane1 ========
		{
			splitPane1.setOneTouchExpandable(true);
			splitPane1.setResizeWeight(0.5);

			// ======== panel3 ========
			{
				panel3.setLayout(new BorderLayout());

				// ======== panel1 ========
				{
					panel1.setLayout(new GridBagLayout());
					((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 159, 0 };
					((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0, 0 };
					((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
					((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 0.0, 1.0, 1.0E-4 };

					// ======== panel2 ========
					{
						panel2.setLayout(new GridBagLayout());
						((GridBagLayout) panel2.getLayout()).columnWidths = new int[] { 0, 0, 0 };
						((GridBagLayout) panel2.getLayout()).rowHeights = new int[] { 0, 0 };
						((GridBagLayout) panel2.getLayout()).columnWeights = new double[] { 0.0, 0.0, 1.0E-4 };
						((GridBagLayout) panel2.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

						// ---- bCreateIssue ----
						bCreateIssue.setText("Create Issue");
						bCreateIssue.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								bCreateIssueActionPerformed();
							}
						});
						panel2.add(bCreateIssue, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
					}
					panel1.add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

					// ======== scrollPane1 ========
					{

						// ---- table1 ----
						table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						table1.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								table1MouseClicked(e);
							}
						});
						scrollPane1.setViewportView(table1);
					}
					panel1.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				}
				panel3.add(panel1, BorderLayout.CENTER);
			}
			splitPane1.setLeftComponent(panel3);

			// ======== panel4 ========
			{
				panel4.setLayout(new BorderLayout());
				panel4.add(issueCommentsPanel1, BorderLayout.CENTER);
			}
			splitPane1.setRightComponent(panel4);
		}
		add(splitPane1, BorderLayout.CENTER);
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JSplitPane splitPane1;
	private JPanel panel3;
	private JPanel panel1;
	private JPanel panel2;
	private JButton bCreateIssue;
	private JScrollPane scrollPane1;
	private JTable table1;
	private JPanel panel4;
	private IssueCommentsPanel issueCommentsPanel1;

	// JFormDesigner - End of variables declaration //GEN-END:variables

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		// This is the property change listener that listens to the commit
		// action
	}

	
	/**
	 * The listener interface for receiving selection events. The class that is
	 * interested in processing a selection event implements this interface, and
	 * the object created with that class is registered with a component using
	 * the component's <code>addSelectionListener<code> method. When
	 * the selection event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see SelectionEvent
	 */
	class SelectionListener implements ListSelectionListener {

		/** The table. */
		JTable table;

		// It is necessary to keep the table since it is not possible
		// to determine the table from the event's source
		/**
		 * Instantiates a new selection listener.
		 * 
		 * @param table
		 *            the table
		 */
		SelectionListener(JTable table) {
			this.table = table;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.
		 * event.ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent e) {
			// If cell selection is enabled, both row and column change events
			// are fired
			if (!(e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed())) {
				return;
			}

			if (e.getValueIsAdjusting()) {
				// The mouse button has not yet been released
				return;
			} else {
				int first = table.getSelectedRow();
				if (first > -1) {
					Object row = table.getModel().getValueAt(first, 0);
					if (row != null) {
						Issue issue = (Issue) ((ListObj) row).getAtrValue();
						if (issue != null && issueRepo != null && regis != null) {
							issueCommentsPanel1.setInit(issue, issueRepo, regis, config.getUsername(), readOnlyMode);
						}
					}
				}

			}
		}
	}

}
