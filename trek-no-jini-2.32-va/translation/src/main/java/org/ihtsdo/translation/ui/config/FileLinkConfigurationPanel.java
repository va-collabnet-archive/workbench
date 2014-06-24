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

package org.ihtsdo.translation.ui.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.translation.ui.ConfigTranslationModule;

/**
 * The Class FileLinkConfigurationPanel.
 *
 * @author Guillermo Reynoso
 */
public class FileLinkConfigurationPanel extends JPanel {
	
	/** The config. */
	I_ConfigAceFrame config;
	
	/** The top. */
	DefaultMutableTreeNode top;
	
	/** The tree model. */
	DefaultTreeModel treeModel;
	
	/** The file chusa. */
	final JFileChooser fileChusa;
	
	/** The dynamic tree1. */
	FileLinkDynamicTree dynamicTree1;
	
	/** The tf. */
	I_TermFactory tf = Terms.get();
	//boolean modifFileLinkPermission;
	
	/**
	 * Instantiates a new file link configuration panel.
	 *
	 * @param config the config
	 * @param confTrans the conf trans
	 */
	public FileLinkConfigurationPanel(I_ConfigAceFrame config, ConfigTranslationModule confTrans) {
		super();
		this.config = config;
		this.fileChusa = new JFileChooser();
		initComponents();
		initCustomComponents();
		
		//ProjectPermissionsAPI permissionApi = new ProjectPermissionsAPI(config);
		
//			modifFileLinkPermission = permissionApi.checkPermissionForProject(
//					config.getDbConfig().getUserConcept(), 
//					tf.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
//					tf.getConcept(ArchitectonicAuxiliary.Concept.MODIFY_FILE_LINK_PERMISSION.localize().getNid()));
//			if(!modifFileLinkPermission){
//				SwingUtils.disabledAllComponents(this);
//			}
	}

	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {
		error.setBorder(new EmptyBorder(new Insets(5, 5, 3, 0)));
		
		fileChusa.setDialogTitle("Select file or folder");
		fileChusa.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		dynamicTree1 = new FileLinkDynamicTree(config);
		treeContainer.add(dynamicTree1,BorderLayout.CENTER);
		
		treeContainer.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5), new EtchedBorder(EtchedBorder.RAISED)));
		removeButton.setEnabled(false);
		dynamicTree1.setPreferredSize(new Dimension(700, 400));
		
		addFileLinkButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = fileChusa.showOpenDialog(FileLinkConfigurationPanel.this);
				try {
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fileChusa.getSelectedFile();
						dynamicTree1.addObject(file);
					}
				} catch (Exception e1) {
					error.setText("Problems adding file link to the configuration, contact your administrator.");
					e1.printStackTrace();
				}
			}
		});
		
		removeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					dynamicTree1.removeCurrentNode();
				} catch (IOException e1) {
					error.setText("Problems removing file link from configuration, contact your administrator.");
					e1.printStackTrace();
				}
			}
		});
		
		TreeSelectionListener sl = new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) dynamicTree1.getTree().getLastSelectedPathComponent();
				if (node == null) {
					addFileLinkButton.setEnabled(false);
					removeButton.setEnabled(false);
					return;
				}
				Object nodeInfo = node.getUserObject();
				if(nodeInfo instanceof FileLinkNodeInfo){
					addFileLinkButton.setEnabled(false);
					removeButton.setEnabled(true);
				}else if(nodeInfo instanceof CategoryNodeInfo){
					removeButton.setEnabled(false);
					addFileLinkButton.setEnabled(true);
				}
			}
		};
		dynamicTree1.getTree().addTreeSelectionListener(sl);
		
	}
	
	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		buttonContainer = new JPanel();
		addFileLinkButton = new JButton();
		removeButton = new JButton();
		errorContainer = new JPanel();
		error = new JLabel();
		treeContainer = new JPanel();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());

		//======== buttonContainer ========
		{
			buttonContainer.setBorder(new MatteBorder(1, 0, 0, 0, Color.gray));
			buttonContainer.setLayout(new FlowLayout(FlowLayout.RIGHT));

			//---- addFileLinkButton ----
			addFileLinkButton.setText("Add file link");
			buttonContainer.add(addFileLinkButton);

			//---- removeButton ----
			removeButton.setText("Remove");
			buttonContainer.add(removeButton);
		}
		add(buttonContainer, BorderLayout.SOUTH);

		//======== errorContainer ========
		{
			errorContainer.setLayout(new BorderLayout(5, 5));

			//---- error ----
			error.setForeground(UIManager.getColor("Button.light"));
			errorContainer.add(error, BorderLayout.SOUTH);

			//======== treeContainer ========
			{
				treeContainer.setBorder(null);
				treeContainer.setLayout(new BorderLayout(5, 5));
			}
			errorContainer.add(treeContainer, BorderLayout.NORTH);
		}
		add(errorContainer, BorderLayout.CENTER);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The button container. */
	private JPanel buttonContainer;
	
	/** The add file link button. */
	private JButton addFileLinkButton;
	
	/** The remove button. */
	private JButton removeButton;
	
	/** The error container. */
	private JPanel errorContainer;
	
	/** The error. */
	private JLabel error;
	
	/** The tree container. */
	private JPanel treeContainer;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

}
class MyTreeModelListener implements TreeModelListener {
    public void treeNodesChanged(TreeModelEvent e) {
        DefaultMutableTreeNode node;
        node = (DefaultMutableTreeNode)(e.getTreePath().getLastPathComponent());
        
        System.out.println();

        try {
            int index = e.getChildIndices()[0];
            node = (DefaultMutableTreeNode)(node.getChildAt(index));
        } catch (NullPointerException exc) {}

        System.out.println("The user has finished editing the node.");
        System.out.println("New value: " + node.getUserObject());
    }
    public void treeNodesInserted(TreeModelEvent e) {
    }
    public void treeNodesRemoved(TreeModelEvent e) {
    }
    public void treeStructureChanged(TreeModelEvent e) {
    }
}

