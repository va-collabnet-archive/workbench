/*
 * Created by JFormDesigner on Wed Jun 23 15:10:15 GMT-03:00 2010
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
 * @author Guillermo Reynoso
 */
public class JInboxTreeTablePanel extends MenuComponentPanel {
	public JInboxTreeTablePanel() {
		initComponents();
//		treeTable1.setRootVisible(false);
//		treeTable1.setShowsRootHandles(false);
	}

	public void setTreeModel(AbstractTreeTableModel treeModel){
		this.treeTable1.setTreeTableModel(treeModel);
		this.treeTable1.revalidate();
	}

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
	private JScrollPane scrollPane1;
	private JTreeTable treeTable1;
	private Icon icon;
	private String labelText;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	@Override
	public Icon getLabelIcon() {
		return icon;
	}

	@Override
	public String getLabelText() {
		return labelText;
	}

	@Override
	public void setLabelIcon(Icon icon) {
		this.icon=icon;
		
	}

	@Override
	public void setLabelText(String labelText) {
		this.labelText=labelText;
		
	}
	public JTreeTable getTreeTable() {
		return treeTable1;
	}

//	public void setTreeTableMouseListener(
//			org.ihtsdo.translation.ui.InboxPanelOld.InboxTreeTableMouselistener inboxTreeTableMouselistener) {
//		//this.treeTable1.addMouseListener(inboxTreeTableMouselistener);
//		
//	}
}
