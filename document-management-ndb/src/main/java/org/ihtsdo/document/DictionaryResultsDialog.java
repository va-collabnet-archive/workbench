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
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * The Class DictionaryResultsDialog.
 */
public class DictionaryResultsDialog extends JDialog implements ActionListener {

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
	
	/** The ignore button. */
	JButton ignoreButton;
	
	/** The change button. */
	JButton changeButton;
	
	/** The cancel button. */
	JButton cancelButton;
	
	/** The validated word. */
	String validatedWord;
	
	/** The table. */
	JTable table;
	
	/** The exit. */
	boolean exit = false;
	
	String langCode;

	/**
	 * Instantiates a new dictionary results dialog.
	 * 
	 * @param aFrame the a frame
	 * @param query the query
	 */
	public DictionaryResultsDialog(Frame aFrame, String query,String langCode) {
		super(aFrame, true);
		this.setLayout(new BorderLayout());
		
		this.langCode=langCode;

		Container topContainer = new Container();
		topContainer.setLayout(new FlowLayout());
		this.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		topContainer.add(new JLabel("Search dictionary:"));
		queryField = new JTextField(30);
		queryField.setText(query);
		topContainer.add(queryField);
		resultLabel = new JLabel("<html>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
		topContainer.add(resultLabel);


		Container rightContainer = new Container();
		rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));
		searchButton = new JButton("Search");
		searchButton.setActionCommand("search");
		searchButton.addActionListener(this);
		searchButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		rightContainer.add(searchButton);

		addButton = new JButton("Add to dictionary");
		addButton.setActionCommand("add");
		addButton.setEnabled(true);
		addButton.addActionListener(this);
		addButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		rightContainer.add(addButton);

		ignoreButton = new JButton("Ignore");
		ignoreButton.setActionCommand("ignore");
		ignoreButton.setEnabled(true);
		ignoreButton.addActionListener(this);
		ignoreButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		rightContainer.add(ignoreButton);

		changeButton = new JButton("Change");
		changeButton.setActionCommand("change");
		changeButton.setEnabled(true);
		changeButton.addActionListener(this);
		changeButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		rightContainer.add(changeButton);
		
		cancelButton = new JButton("cancel");
		cancelButton.setActionCommand("ignore");
		cancelButton.setEnabled(true);
		cancelButton.addActionListener(this);
		cancelButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		rightContainer.add(cancelButton);

		rightContainer.add(Box.createVerticalGlue());

		String[] columnNames = {"Suggestions"};
		String[][] data = null;
		tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		if (query != null)  {
			update(query);
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
		this.add(rightContainer, BorderLayout.LINE_END);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				validatedWord = null;
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
			DocumentManager.addToDictionary(queryField.getText(),this.langCode);
			validatedWord = queryField.getText();
			setVisible(false);
		} else if ("ignore".equals(e.getActionCommand())) {
			validatedWord = null;
			setVisible(false);
		} else if ("change".equals(e.getActionCommand())) {
			validatedWord = (String) tableModel.getValueAt(table.getSelectedRow(), 0);
			setVisible(false);
		}
	}

	/**
	 * Update.
	 * 
	 * @param word the word
	 */
	public void update(String word) {
		while (tableModel.getRowCount()>0){
			tableModel.removeRow(0);
		}

		if (DocumentManager.existsInDictionary(word, this.langCode)) {
			resultLabel.setText("<html><font color='green'><b>OK");
		} else {
			resultLabel.setText("<html><font color='red'><b>Unknown");
		}

		String[] results = DocumentManager.getSugestionsFromDictionary(word, this.langCode);

		for (String suggestion : results) {
			tableModel.addRow(new String[] {suggestion});
		}
		
		if (tableModel.getRowCount() > 0) table.setRowSelectionInterval(0, 0);
		
	}

	/**
	 * Gets the validated word.
	 * 
	 * @return the validated word
	 */
	public String getValidatedWord() {
		return validatedWord;
	}

	/**
	 * Sets the validated word.
	 * 
	 * @param word the new validated word
	 */
	public void setValidatedWord(String word) {
		this.validatedWord = word;
	}

}
