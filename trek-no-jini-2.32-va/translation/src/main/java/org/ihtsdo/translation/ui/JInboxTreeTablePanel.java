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

package org.ihtsdo.translation.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import org.ihtsdo.translation.ui.treetable.AbstractTreeTableModel;
import org.ihtsdo.translation.ui.treetable.JTreeTable;

/**
 * The Class JInboxTreeTablePanel.
 *
 * @author Guillermo Reynoso
 */
public class JInboxTreeTablePanel extends MenuComponentPanel {
	
	/**
	 * Instantiates a new j inbox tree table panel.
	 */
	public JInboxTreeTablePanel() {
		initComponents();
//		treeTable1.setRootVisible(false);
//		treeTable1.setShowsRootHandles(false);
	}

	/**
	 * Sets the tree model.
	 *
	 * @param treeModel the new tree model
	 */
	public void setTreeModel(AbstractTreeTableModel treeModel){
		this.treeTable1.setTreeTableModel(treeModel);
		this.treeTable1.revalidate();
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		scrollPane1 = new JScrollPane();
		treeTable1 = new JTreeTable();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(treeTable1);
		}
		add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The tree table1. */
	private JTreeTable treeTable1;
	
	/** The icon. */
	private Icon icon;
	
	/** The label text. */
	private String labelText;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	/* (non-Javadoc)
	 * @see org.ihtsdo.translation.ui.MenuComponentPanel#getLabelIcon()
	 */
	@Override
	public Icon getLabelIcon() {
		return icon;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.translation.ui.MenuComponentPanel#getLabelText()
	 */
	@Override
	public String getLabelText() {
		return labelText;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.translation.ui.MenuComponentPanel#setLabelIcon(javax.swing.Icon)
	 */
	@Override
	public void setLabelIcon(Icon icon) {
		this.icon=icon;
		
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.translation.ui.MenuComponentPanel#setLabelText(java.lang.String)
	 */
	@Override
	public void setLabelText(String labelText) {
		this.labelText=labelText;
		
	}
	
	/**
	 * Gets the tree table.
	 *
	 * @return the tree table
	 */
	public JTreeTable getTreeTable() {
		return treeTable1;
	}

//	public void setTreeTableMouseListener(
//			org.ihtsdo.translation.ui.InboxPanelOld.InboxTreeTableMouselistener inboxTreeTableMouselistener) {
//		//this.treeTable1.addMouseListener(inboxTreeTableMouselistener);
//		
//	}
}
