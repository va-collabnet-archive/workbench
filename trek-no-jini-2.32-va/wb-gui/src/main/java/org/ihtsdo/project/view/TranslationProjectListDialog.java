/*
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

package org.ihtsdo.project.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;

/**
 * The Class TranslationProjectListDialog.
 *
 * @author Guillermo Reynoso
 */
public class TranslationProjectListDialog extends JDialog {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2132747097563085391L;

	/** The Constant PROJECTNODE. */
	private static final String PROJECTNODE = "P";

	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The root node. */
	private DefaultMutableTreeNode rootNode;
	
	/** The tree model. */
	private DefaultTreeModel treeModel;
	
	/** The project list. */
	private ArrayList<I_TerminologyProject> projectList = null;

	/**
	 * Instantiates a new translation project list dialog.
	 */
	public TranslationProjectListDialog() {
		super();
		initComponents();
		initCustomComponents();
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close(null);
			}
		});
		
	}
	
	/**
	 * Show modal dialog.
	 *
	 * @return the array list
	 */
	public ArrayList<I_TerminologyProject> showModalDialog() {
		setModal(true);
		pack();
		setVisible(true);
		return projectList;
	}

	/**
	 * Close.
	 *
	 * @param canceled the canceled
	 */
	private void close(ArrayList<I_TerminologyProject> canceled) {
		this.projectList = canceled;
		dispose();
	}
	
	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {
		try {
			projectList = new ArrayList<I_TerminologyProject>();
			this.getRootPane().setDefaultButton(okButton);
			
			loadProjects();
			
			TreeSelectionListener tl = new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent arg0) {
					errorLabel.setText("");
				}
			};
			
			jTree1.addTreeSelectionListener(tl);
			
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					close(null);
				}
			});
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Ok button action performed.
	 *
	 * @param e the e
	 */
	private void okButtonActionPerformed(ActionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
		if (node == null) {
			errorLabel.setForeground(Color.RED);
			errorLabel.setText("Select a project to continue");
			return;
		}
		Object nodeInfo = node.getUserObject();
		TreeObj proj = (TreeObj)nodeInfo;
		if(proj.getAtrValue() instanceof I_TerminologyProject){
			projectList.add((I_TerminologyProject) proj.getAtrValue());
		}else if(proj.getAtrValue() instanceof ArrayList){
			projectList = (ArrayList<I_TerminologyProject>) proj.getAtrValue();
		}
		close(projectList);
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();
		panel2 = new JPanel();
		scrollPane1 = new JScrollPane();
		jTree1 = new JTree();
		errorLabel = new JLabel();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== panel1 ========
		{
			panel1.setLayout(new FlowLayout(FlowLayout.RIGHT));

			//---- okButton ----
			okButton.setText("Ok");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					okButtonActionPerformed(e);
				}
			});
			panel1.add(okButton);

			//---- cancelButton ----
			cancelButton.setText("Cancel");
			panel1.add(cancelButton);
		}
		contentPane.add(panel1, BorderLayout.SOUTH);

		//======== panel2 ========
		{
			panel2.setBorder(new EmptyBorder(5, 5, 5, 5));
			panel2.setLayout(new BorderLayout());

			//======== scrollPane1 ========
			{
				scrollPane1.setViewportView(jTree1);
			}
			panel2.add(scrollPane1, BorderLayout.CENTER);
			panel2.add(errorLabel, BorderLayout.SOUTH);
		}
		contentPane.add(panel2, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The panel1. */
	private JPanel panel1;
	
	/** The ok button. */
	private JButton okButton;
	
	/** The cancel button. */
	private JButton cancelButton;
	
	/** The panel2. */
	private JPanel panel2;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The j tree1. */
	private JTree jTree1;
	
	/** The error label. */
	private JLabel errorLabel;
	// JFormDesigner - End of variables declaration //GEN-END:variables

	/**
	 * Load projects.
	 *
	 * @throws Exception the exception
	 */
	private void loadProjects() throws Exception {
		int i;
		rootNode = new DefaultMutableTreeNode("Root Node");
		treeModel = new DefaultTreeModel(rootNode);

		jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		jTree1.setRootVisible(false);
		config = Terms.get().getActiveAceFrameConfig();

		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
		addObject(rootNode,new TreeObj(PROJECTNODE,"All projects",projects),false);
		
		for (i = 0; i < projects.size(); i++) {
			addProjectToTree(rootNode, projects.get(i), false);
		}

		jTree1.setModel(treeModel);
		jTree1.revalidate();

	}

	/**
	 * Adds the project to tree.
	 *
	 * @param node the node
	 * @param project the project
	 * @param visibleChildren the visible children
	 * @return the default mutable tree node
	 * @throws Exception the exception
	 */
	private DefaultMutableTreeNode addProjectToTree (DefaultMutableTreeNode node,I_TerminologyProject project,boolean visibleChildren) throws Exception{
		DefaultMutableTreeNode tNode;
		tNode=addObject(node,new TreeObj(PROJECTNODE,project.getName(),project),visibleChildren);
		return tNode;
	}
	
	/**
	 * Adds the object.
	 *
	 * @param parent the parent
	 * @param child the child
	 * @param shouldBeVisible the should be visible
	 * @return the default mutable tree node
	 */
	private DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
			Object child, boolean shouldBeVisible) {
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

		// Make sure the user can see the lovely new node.
		if (shouldBeVisible) {
			jTree1.scrollPathToVisible(new TreePath(childNode.getPath()));
		}
		return childNode;
	}
}
