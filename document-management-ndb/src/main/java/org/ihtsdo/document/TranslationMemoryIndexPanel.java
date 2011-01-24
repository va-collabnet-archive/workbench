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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.poi.poifs.filesystem.OfficeXmlFileException;

/**
 * The Class TranslationMemoryIndexPanel.
 */
public class TranslationMemoryIndexPanel extends JPanel  implements ActionListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The html pane. */
	JEditorPane htmlPane;

	/** The browser pane. */
	JScrollPane browserPane;

	/** The index button. */
	JButton indexButton;


	/**
	 * Instantiates a new translation memory index panel.
	 */
	public TranslationMemoryIndexPanel() {
		this.setLayout(new BorderLayout());

		String results = "<html><body><font style='font-family:arial,sans-serif'>" +
		"Press the button to select the Translation Memory file..." +
		"</font></body></html>";
		htmlPane = new JEditorPane("text/html", results);
		browserPane = new JScrollPane(htmlPane);

		indexButton = new JButton("Select file and index translation memory");
		indexButton.setActionCommand("index");
		indexButton.addActionListener(this);

		Container centerContainer = new Container();
		centerContainer.setLayout(new BorderLayout());
		centerContainer.add(browserPane, BorderLayout.CENTER);
		this.add(centerContainer, BorderLayout.CENTER);
		this.add(indexButton, BorderLayout.PAGE_END);

	}

	/**
	 * Creates the and show gui.
	 */
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("TranslationMemoryIndexPanel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		TranslationMemoryIndexPanel newContentPane = new TranslationMemoryIndexPanel();
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
		if ("index".equals(e.getActionCommand())) {
			String results = "<html><body><font style='font-family:arial,sans-serif'>" +
			"Indexing..." +
			"</font></body></html>";
			htmlPane.setText(results);
			htmlPane.setCaretPosition(0);
			try {
				results = DocumentManager.indexMemoryFromXls();
				JOptionPane.showMessageDialog(this,
						"Translation Memory indexed...",
						"Info",
						JOptionPane.OK_OPTION);
			} catch (OfficeXmlFileException e2) {
				e2.printStackTrace();
				JOptionPane.showMessageDialog(this,
						e2.getMessage(),
						"Error, the input file must be an XLS (not XLSX)",
						JOptionPane.ERROR_MESSAGE);
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(this,
						e1.getMessage(),
						"Error reading file",
						JOptionPane.ERROR_MESSAGE);
			} catch (Exception e3) {
				e3.printStackTrace();
				JOptionPane.showMessageDialog(this,
						e3.getMessage(),
						"Unspecified Error. Check logs.",
						JOptionPane.ERROR_MESSAGE);
			}
			htmlPane.setText(results);
			htmlPane.setCaretPosition(0);
		}
	}
}
