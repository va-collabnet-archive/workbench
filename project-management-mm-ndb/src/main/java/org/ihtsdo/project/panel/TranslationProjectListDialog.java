/*
 * Created by JFormDesigner on Thu Jul 15 17:35:07 GMT-03:00 2010
 */

package org.ihtsdo.project.panel;

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
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;

/**
 * @author Guillermo Reynoso
 */
public class TranslationProjectListDialog extends JDialog {

	private static final long serialVersionUID = -2132747097563085391L;

	private static final String PROJECTNODE = "P";

	private I_ConfigAceFrame config;
	private DefaultMutableTreeNode rootNode;
	private DefaultTreeModel treeModel;
	private ArrayList<I_TerminologyProject> projectList = null;

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
	
	public ArrayList<I_TerminologyProject> showModalDialog() {
		setModal(true);
		pack();
		setVisible(true);
		return projectList;
	}

	private void close(ArrayList<I_TerminologyProject> canceled) {
		this.projectList = canceled;
		dispose();
	}
	
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
			e.printStackTrace();
		}
	}

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
	private JPanel panel1;
	private JButton okButton;
	private JButton cancelButton;
	private JPanel panel2;
	private JScrollPane scrollPane1;
	private JTree jTree1;
	private JLabel errorLabel;
	// JFormDesigner - End of variables declaration //GEN-END:variables

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

	private DefaultMutableTreeNode addProjectToTree (DefaultMutableTreeNode node,I_TerminologyProject project,boolean visibleChildren) throws Exception{
		DefaultMutableTreeNode tNode;
		tNode=addObject(node,new TreeObj(PROJECTNODE,project.getName(),project),visibleChildren);
		return tNode;
	}
	
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
