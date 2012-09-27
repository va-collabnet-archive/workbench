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

package org.ihtsdo.qa.store.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.ihtsdo.qa.store.QAStoreBI;

/**
 * The Class QAStorePanel.
 *
 * @author Guillermo Reynoso
 */
public class QAStorePanel extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The store. */
	private QAStoreBI store;
	
	/** The results panel. */
	private QAResultsBrowser resultsPanel;
	
	/** The cases panel. */
	private QACasesBrowser casesPanel;
	
	/**
	 * Instantiates a new qA store panel.
	 *
	 * @param store the store
	 */
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
	
	/**
	 * Table1 mouse clicked.
	 *
	 * @param e the e
	 */
	private void table1MouseClicked(MouseEvent e) {
		if(e.getClickCount() == 1){
			updateCasePanel();
		}
	}

	/**
	 * Update case panel.
	 */
	public void updateCasePanel() {
		casesPanel.setupPanel(store);
	}

	/**
	 * Inits the components.
	 */
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
	/** The split pane1. */
	private JSplitPane splitPane1;
	
	/** The tabbed pane1. */
	private JTabbedPane tabbedPane1;
	
	/** The rules content panel. */
	private JPanel rulesContentPanel;
	
	/** The tabbed pane2. */
	private JTabbedPane tabbedPane2;
	
	/** The cases content panel. */
	private JPanel casesContentPanel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
