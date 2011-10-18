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

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * The Class SpellCheckingDialogTester.
 */
public class SpellCheckingDialogTester extends JPanel implements ActionListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private static String lngCode;

	/** The query field. */
	JTextField queryField;
	
	/** The check button. */
	JButton checkButton;
	
	/** The frame. */
	Frame frame;
	
	/** The spell check dialog. */
	DictionaryResultsDialog spellCheckDialog;

	/**
	 * Instantiates a new spell checking dialog tester.
	 * 
	 * @param frame the frame
	 */
	public SpellCheckingDialogTester(JFrame frame, String langCode) {
		this.frame = frame;
		spellCheckDialog = new DictionaryResultsDialog(frame, null,langCode);
		spellCheckDialog.pack();
        
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		Container topContainer = new Container();
		topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.X_AXIS));
		topContainer.add(new JLabel("Search dictionary:"));
		queryField = new JTextField(30);
		topContainer.add(queryField);
		
		Container rightContainer = new Container();
		rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));
		checkButton = new JButton("Check");
		checkButton.setActionCommand("check");
		checkButton.addActionListener(this);
		checkButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		rightContainer.add(checkButton);
		
		Container leftContainer = new Container();
		leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.Y_AXIS));
		leftContainer.add(topContainer);

		this.add(leftContainer);
		this.add(rightContainer);
	}

	/**
	 * Creates the and show gui.
	 */
	private static void createAndShowGUI(String langCode) {
		//Create and set up the window.
		lngCode = langCode;
		JFrame frame = new JFrame("DictionaryResultsPanel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		SpellCheckingDialogTester newContentPane = new SpellCheckingDialogTester(frame,langCode);
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
		if ("check".equals(e.getActionCommand()) && queryField.getText() != null && !queryField.getText().equals("")) {
			String modifiedPhrase = DocumentManager.spellcheckPhrase(queryField.getText(), null,lngCode);
			queryField.setText(modifiedPhrase.trim());
		} 
	}
	

}
