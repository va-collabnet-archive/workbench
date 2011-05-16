/*
 * Created by JFormDesigner on Mon Nov 29 18:17:07 GMT-03:00 2010
 */

package org.ihtsdo.qa.store.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.ihtsdo.qa.store.QAStoreBI;

/**
 * @author Guillermo Reynoso
 */
public class QAStorePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private QAStoreBI store;
	private QAResultsBrowser resultsPanel;
	private QACasesBrowser casesPanel;
	
	public QAStorePanel(QAStoreBI store) {
		this.store = store;
		initComponents();
		
		resultsPanel = new QAResultsBrowser(store, tabbedPane1, this);
		rulesContentPanel.add(resultsPanel, BorderLayout.CENTER);
		casesPanel = new QACasesBrowser(store, resultsPanel, tabbedPane2);
		casesContentPanel.add(casesPanel, BorderLayout.CENTER);
		
		resultsPanel.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				table1MouseClicked(e);
			}
		});
	}
	
	private void table1MouseClicked(MouseEvent e) {
		if(e.getClickCount() == 1){
			updateCasePanel();
		}
	}

	public void updateCasePanel() {
		casesPanel.setupPanel(store);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		splitPane1 = new JSplitPane();
		tabbedPane1 = new JTabbedPane();
		rulesContentPanel = new JPanel();
		tabbedPane2 = new JTabbedPane();
		casesContentPanel = new JPanel();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== splitPane1 ========
		{
			splitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitPane1.setResizeWeight(0.5);

			//======== tabbedPane1 ========
			{

				//======== rulesContentPanel ========
				{
					rulesContentPanel.setLayout(new BorderLayout());
				}
				tabbedPane1.addTab("QA Rules", rulesContentPanel);

			}
			splitPane1.setTopComponent(tabbedPane1);

			//======== tabbedPane2 ========
			{

				//======== casesContentPanel ========
				{
					casesContentPanel.setLayout(new BorderLayout());
				}
				tabbedPane2.addTab("Cases", casesContentPanel);

			}
			splitPane1.setBottomComponent(tabbedPane2);
		}
		add(splitPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JSplitPane splitPane1;
	private JTabbedPane tabbedPane1;
	private JPanel rulesContentPanel;
	private JTabbedPane tabbedPane2;
	private JPanel casesContentPanel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
