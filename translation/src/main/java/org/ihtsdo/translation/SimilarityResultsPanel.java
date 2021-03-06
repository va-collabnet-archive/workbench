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
package org.ihtsdo.translation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.I_ConfigAceFrame;

/**
 * The Class SimilarityResultsPanel.
 */
public class SimilarityResultsPanel extends JPanel implements ActionListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The query field. */
	JTextField queryField;
	
	/** The source lang. */
	JComboBox sourceLang;
	
	/** The target lang. */
	JComboBox targetLang;
	
	/** The table. */
	JTable table;
	
	/** The config. */
	static I_ConfigAceFrame config;
	
	/** The table model. */
	DefaultTableModel tableModel;
	
	/**
	 * Instantiates a new similarity results panel.
	 * 
	 * @param query the query
	 * @param sourceLangCode the source lang code
	 * @param targetLangCode the target lang code
	 * @param results the results
	 * @param config the config
	 */
	public SimilarityResultsPanel(String query, String sourceLangCode, String targetLangCode, List<SimilarityMatchedItem> results,
			I_ConfigAceFrame config) {
		this.setLayout(new BorderLayout());
		this.config =  config;
		Box topContainer = new Box(BoxLayout.X_AXIS);
		topContainer.add(new JLabel("Text to search:"));
		queryField = new JTextField(30);
		queryField.setText(query);
		topContainer.add(queryField);
		topContainer.add(new JLabel("  Search in language:"));
		String[] langCodes = {"en","es"};
		sourceLang = new JComboBox(langCodes);
		sourceLang.setSelectedItem(sourceLangCode);
		topContainer.add(sourceLang);
		topContainer.add(new JLabel("  Show results from language:"));
		targetLang = new JComboBox(langCodes);
		targetLang.setSelectedItem(targetLangCode);
		topContainer.add(targetLang);
		
		Box bottomContainer = new Box(BoxLayout.X_AXIS);
		JButton searchButton = new JButton("Search");
		searchButton.setActionCommand("search");
		searchButton.addActionListener(this);
		
		bottomContainer.add(searchButton);

		String[] columnNames = {"Source Text",
                "Target Text",
                "Score"};
		String[][] data = null;
		tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
				}
			};
		for (SimilarityMatchedItem item : results) {
			tableModel.addRow(new String[] {item.getSourceText(),item.getTargetText(),String.valueOf(item.getScore())});
		}
		table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setGridColor(Color.GRAY);
		Container centerContainer = new Container();
		centerContainer.setLayout(new BorderLayout());
		centerContainer.add(table.getTableHeader(), BorderLayout.PAGE_START);
		centerContainer.add(scrollPane, BorderLayout.CENTER);
		
		this.add(topContainer, BorderLayout.PAGE_START);
		this.add(centerContainer, BorderLayout.CENTER);
		this.add(bottomContainer, BorderLayout.PAGE_END);
	}
	
	 /**
 	 * Creates the and show gui.
 	 */
 	private static void createAndShowGUI() {
	        //Create and set up the window.
	        JFrame frame = new JFrame("SimpleTableDemo");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        
	        SimilarityMatchedItem item1 = new SimilarityMatchedItem(0, 0, "1source 1", 0, "3source 2", 0.1423F, "original term");
	        SimilarityMatchedItem item2 = new SimilarityMatchedItem(0, 0, "2source 1", 0, "2source 2", 0.1323F, "original term");
	        SimilarityMatchedItem item3 = new SimilarityMatchedItem(0, 0, "3source 1", 0, "1source 2", 0.9423F, "original term");
	        
	        List<SimilarityMatchedItem> results = new ArrayList<SimilarityMatchedItem>();
	        results.add(item1);
	        results.add(item2);
	        results.add(item3);
	        
			try {
				FileInputStream fin = new FileInputStream("config.dat");
				ObjectInputStream ois = new ObjectInputStream(fin);
				config = (I_ConfigAceFrame) ois.readObject();
				ois.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

	        //Create and set up the content pane.
	        SimilarityResultsPanel newContentPane = new SimilarityResultsPanel("demo", "en", "es", results, config);
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
	    		List<SimilarityMatchedItem> results = LanguageUtil.getSimilarityResults(queryField.getText(), 
	    				(String) sourceLang.getSelectedItem(), (String) targetLang.getSelectedItem(), config);
	    		while (tableModel.getRowCount()>0){
	    			tableModel.removeRow(0);
	    			}
	    		for (SimilarityMatchedItem item : results) {
	    			tableModel.addRow(new String[] {item.getSourceText(),item.getTargetText(),String.valueOf(item.getScore())});
	    		}
	    	}
	    }
}
