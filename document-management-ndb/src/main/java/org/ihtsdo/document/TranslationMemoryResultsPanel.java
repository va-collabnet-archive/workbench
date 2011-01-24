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
package org.ihtsdo.document;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * The Class TranslationMemoryResultsPanel.
 */
public class TranslationMemoryResultsPanel extends JPanel implements ActionListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The query field. */
	JTextField queryField;
	
	/** The table model. */
	DefaultTableModel tableModel;
	
	/**
	 * Instantiates a new translation memory results panel.
	 * 
	 * @param query the query
	 */
	public TranslationMemoryResultsPanel(String query) {
		this.setLayout(new BorderLayout());
		Box topContainer = new Box(BoxLayout.X_AXIS);
		topContainer.add(new JLabel("Search translation memory:"));
		queryField = new JTextField(30);
		queryField.setText(query);
		topContainer.add(queryField);
		
		if (query == null) query = "*";
		if (query.equals("")) query = "*";
		
		JButton searchButton = new JButton("Search");
		searchButton.setActionCommand("search");
		searchButton.addActionListener(this);
		topContainer.add(searchButton);

		String[] columnNames = {"Source Text",
                "Translation"};
		String[][] data = null;
		tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
				}
			};
			
		HashMap<String,String> results = DocumentManager.matchTranslationMemory(query);
		
		for (String key : results.keySet()) {
			tableModel.addRow(new String[] {key,results.get(key)});
		}
		JTable table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setGridColor(Color.GRAY);
		Container centerContainer = new Container();
		centerContainer.setLayout(new BorderLayout());
		centerContainer.add(table.getTableHeader(), BorderLayout.PAGE_START);
		centerContainer.add(scrollPane, BorderLayout.CENTER);
		
		this.add(topContainer, BorderLayout.PAGE_START);
		this.add(centerContainer, BorderLayout.CENTER);
	}
	
	 /**
 	 * Creates the and show gui.
 	 */
 	private static void createAndShowGUI() {
	        //Create and set up the window.
	        JFrame frame = new JFrame("TranslationMemoryResultsPanel");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        
	        //Create and set up the content pane.
	        TranslationMemoryResultsPanel newContentPane = new TranslationMemoryResultsPanel("*");
	        newContentPane.setOpaque(true); //content panes must be opaque
	        frame.setContentPane(newContentPane);

	        //Display the window.
	        frame.pack();
	        frame.setVisible(true);
	    }

	    /**
    	 * The main method.
    	 * 
    	 * @param args the arguments
    	 */
    	public static void main(String[] args) {
	        //Schedule a job for the event-dispatching thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                createAndShowGUI();
	            }
	        });
	    }

	    /* (non-Javadoc)
    	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    	 */
    	public void actionPerformed(ActionEvent e) {
	    	if ("search".equals(e.getActionCommand())) {
	    		HashMap<String,String> results = DocumentManager.matchTranslationMemory(queryField.getText());
	    		while (tableModel.getRowCount()>0){
	    			tableModel.removeRow(0);
	    			}
	    		for (String key : results.keySet()) {
	    			tableModel.addRow(new String[] {key,results.get(key)});
	    		}
	    	}
	    }

}
