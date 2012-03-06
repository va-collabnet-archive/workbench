/*
 * Created by JFormDesigner on Mon Mar 05 17:48:05 GMT-03:00 2012
 */

package org.ihtsdo.translation.ui.translation;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.ihtsdo.project.issue.manager.*;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.qa.gui.viewers.ui.*;

/**
 * @author Guillermo Reynoso
 */
public class TranslationView extends JPanel {
	public TranslationView() {
		initComponents();
		initCustomComponents();
	}
	
	private void initCustomComponents() {
		sourceTable.setTitle("Source Language");
		targetTable.setTitle("Target Language");
	}

	public void updateUi(WfInstance instance, boolean readOnly){
		
	}

	private void label10MouseClicked(MouseEvent e) {
		// TODO add your code here
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		splitPane1 = new JSplitPane();
		splitPane2 = new JSplitPane();
		sourceTable = new LanguageTermPanel();
		tabbedPane1 = new JTabbedPane();
		comments = new CommentsManagerPanel();
		issues = new IssuesListPanel2();
		splitPane3 = new JSplitPane();
		panel1 = new JPanel();
		targetTable = new LanguageTermPanel();
		descriptionPanel1 = new DescriptionPanel();
		tabbedPane2 = new JTabbedPane();
		conceptDetailsPanel1 = new ConceptDetailsPanel();
		hierarchyNavigator1 = new HierarchyNavigator();
		panel2 = new JPanel();
		translationPanelMenu1 = new TranslationPanelMenu();
		label14 = new JLabel();
		label15 = new JLabel();
		label16 = new JLabel();
		label10 = new JLabel();

		//======== this ========
		setLayout(new BorderLayout(5, 5));

		//======== splitPane1 ========
		{
			splitPane1.setResizeWeight(0.2);
			splitPane1.setDividerSize(5);

			//======== splitPane2 ========
			{
				splitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitPane2.setResizeWeight(0.5);
				splitPane2.setDividerSize(5);
				splitPane2.setTopComponent(sourceTable);

				//======== tabbedPane1 ========
				{
					tabbedPane1.addTab("Comments", comments);

					tabbedPane1.addTab("Issues", issues);

				}
				splitPane2.setBottomComponent(tabbedPane1);
			}
			splitPane1.setLeftComponent(splitPane2);

			//======== splitPane3 ========
			{
				splitPane3.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitPane3.setResizeWeight(0.5);
				splitPane3.setDividerSize(5);

				//======== panel1 ========
				{
					panel1.setLayout(new BorderLayout());
					panel1.add(targetTable, BorderLayout.NORTH);
					panel1.add(descriptionPanel1, BorderLayout.CENTER);
				}
				splitPane3.setTopComponent(panel1);

				//======== tabbedPane2 ========
				{
					tabbedPane2.addTab("Concept Details", conceptDetailsPanel1);

					tabbedPane2.addTab("Hierarchy", hierarchyNavigator1);

				}
				splitPane3.setBottomComponent(tabbedPane2);
			}
			splitPane1.setRightComponent(splitPane3);
		}
		add(splitPane1, BorderLayout.CENTER);

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
			panel2.add(translationPanelMenu1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label14 ----
			label14.setText("S:-");
			label14.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel2.add(label14, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label15 ----
			label15.setText("TM:-");
			label15.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel2.add(label15, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label16 ----
			label16.setText("LG:-");
			label16.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel2.add(label16, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label10 ----
			label10.setText("text");
			label10.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					label10MouseClicked(e);
				}
			});
			panel2.add(label10, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, BorderLayout.NORTH);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JSplitPane splitPane1;
	private JSplitPane splitPane2;
	private LanguageTermPanel sourceTable;
	private JTabbedPane tabbedPane1;
	private CommentsManagerPanel comments;
	private IssuesListPanel2 issues;
	private JSplitPane splitPane3;
	private JPanel panel1;
	private LanguageTermPanel targetTable;
	private DescriptionPanel descriptionPanel1;
	private JTabbedPane tabbedPane2;
	private ConceptDetailsPanel conceptDetailsPanel1;
	private HierarchyNavigator hierarchyNavigator1;
	private JPanel panel2;
	private TranslationPanelMenu translationPanelMenu1;
	private JLabel label14;
	private JLabel label15;
	private JLabel label16;
	private JLabel label10;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
