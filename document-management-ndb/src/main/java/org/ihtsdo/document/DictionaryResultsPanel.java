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
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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

import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.util.IconUtilities;

/**
 * The Class DictionaryResultsPanel.
 */
public class DictionaryResultsPanel extends JPanel implements ActionListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The query field. */
	JTextField queryField;

	/** The result label. */
	JLabel resultLabel;

	/** The table model. */
	DefaultTableModel tableModel;

	/** The add button. */
	JButton addButton;

	/** The search button. */
	JButton searchButton;

	private String langCode;

	private JComboBox cmbLangs;
	
	private JLabel helpLabel;

	/**
	 * Instantiates a new dictionary results panel.
	 * 
	 * @param query the query
	 */
	public DictionaryResultsPanel(String query,String langCode) {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.langCode=langCode;
		Box topContainer = new Box(BoxLayout.X_AXIS);
		topContainer.add(new JLabel("Search dictionary:"));
		queryField = new JTextField(30);
		queryField.setText(query);
		topContainer.add(queryField);
		cmbLangs= new JComboBox();
		if (langCode ==null){
			File file=new File("spellIndexDirectory");
			for (File langDir:file.listFiles()){
				if (langDir.isDirectory()){
					String langName=null;
					try {
						langName=Terms.get().getConcept(ArchitectonicAuxiliary.getLanguageConcept(langDir.getName()).getUids()).getInitialText();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TerminologyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (langName!=null)
						cmbLangs.addItem(langDir.getName());
				}
			}
		}
		
		helpLabel = new JLabel();
		helpLabel.setIcon(IconUtilities.helpIcon);
		helpLabel.setText("");
		helpLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					HelpApi.openHelpForComponent("SEARCH_DICTIONARY");
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				};
			}
		});

		topContainer.add(new JLabel("Search dictionary:"));
		resultLabel = new JLabel("<html>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
		topContainer.add(resultLabel);
		if (cmbLangs.getItemCount()>0){
			topContainer.add(cmbLangs);
		}
		searchButton = new JButton("Search");
		searchButton.setActionCommand("search");
		searchButton.addActionListener(this);
		searchButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		topContainer.add(searchButton);

		addButton = new JButton("Add to dictionary");
		addButton.setActionCommand("add");
		addButton.setEnabled(false);
		addButton.addActionListener(this);
		addButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		topContainer.add(addButton);
		
		topContainer.add(helpLabel);

		Box leftContainer = new Box(BoxLayout.Y_AXIS);
		leftContainer.add(topContainer);
		String[] columnNames = {"Suggestions"};
		String[][] data = null;
		tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		if (query != null && langCode!=null)  {
			update(query);
		}
		JTable table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setGridColor(Color.GRAY);
		Container centerContainer = new Container();
		centerContainer.setLayout(new BorderLayout());
		centerContainer.add(table.getTableHeader(), BorderLayout.PAGE_START);
		centerContainer.add(scrollPane, BorderLayout.CENTER);
		leftContainer.add(centerContainer);

		this.add(leftContainer);
	}

	/**
	 * Creates the and show gui.
	 */
	private static void createAndShowGUI(String langCode) {
		//Create and set up the window.
		JFrame frame = new JFrame("DictionaryResultsPanel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		DictionaryResultsPanel newContentPane = new DictionaryResultsPanel(null,langCode);
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
				createAndShowGUI("EN");
			}
		});
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if ("search".equals(e.getActionCommand())) {
			update(queryField.getText());
		} else if ("add".equals(e.getActionCommand())) {

			String lCode=this.langCode;

			DocumentManager.addToDictionary(queryField.getText(),lCode);
			update(queryField.getText());
		}
	}

	/**
	 * Update.
	 * 
	 * @param word the word
	 */
	private void update(String word) {
		while (tableModel.getRowCount()>0){
			tableModel.removeRow(0);
		}
		String lCode=this.langCode;

		if (lCode==null && cmbLangs!=null && cmbLangs.getSelectedIndex()>-1)
			lCode=(String)cmbLangs.getSelectedItem();
		if (DocumentManager.existsInDictionary(word,lCode)) {
			resultLabel.setText("<html><font color='green'><b>OK");
			addButton.setEnabled(false);
		} else {
			resultLabel.setText("<html><font color='red'><b>Not in dictionary");
			addButton.setEnabled(true);
		}

		String[] results = DocumentManager.getSugestionsFromDictionary(word,lCode);

		for (String suggestion : results) {
			tableModel.addRow(new String[] {suggestion});
		}
	}

}
