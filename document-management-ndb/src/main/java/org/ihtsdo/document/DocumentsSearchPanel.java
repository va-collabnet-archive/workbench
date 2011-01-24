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
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * The Class DocumentsSearchPanel.
 */
public class DocumentsSearchPanel extends JPanel implements ActionListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The query field. */
	JTextField queryField;

	/** The html pane. */
	JEditorPane htmlPane;

	/** The search button. */
	JButton searchButton;

	/** The browser pane. */
	JScrollPane browserPane;

	/**
	 * Instantiates a new documents search panel.
	 * 
	 * @param query the query
	 */
	public DocumentsSearchPanel(String query) {
		this.setLayout(new BorderLayout());
		Box topContainer = new Box(BoxLayout.X_AXIS);
		topContainer.add(new JLabel("Search documents:"));
		queryField = new JTextField(30);
		queryField.setText(query);
		topContainer.add(queryField);
		searchButton = new JButton("Search");
		searchButton.setActionCommand("search");
		searchButton.addActionListener(this);
		topContainer.add(searchButton);
		queryField.addKeyListener
	      (new KeyAdapter() {
	          public void keyPressed(KeyEvent e) {
	            int key = e.getKeyCode();
	            if (key == KeyEvent.VK_ENTER) {
	               //Toolkit.getDefaultToolkit().beep();   
	               searchButton.doClick();
	               }
	            }
	          }
	       );

		String results = "";

		if (query != null && !query.isEmpty()) {
			results = DocumentManager.searchDocuments(query);
		}
		htmlPane = new JEditorPane("text/html", results);
		htmlPane.setEditable(false);  
		htmlPane.setOpaque(false);

		htmlPane.addHyperlinkListener(new HyperlinkListener() {  
			public void hyperlinkUpdate(HyperlinkEvent hle) {  
				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {  
					System.out.println("Opening: " + hle.getURL());  
					System.out.println("Path: " +  hle.getURL().getHost() + hle.getURL().getPath());  
					try {
						Desktop desktop = null;
						if (Desktop.isDesktopSupported()) {
							desktop = Desktop.getDesktop();
//							TODO: research how to open a pdf with parameters to send a search command
//							if (hle.getURL().getPath().trim().endsWith("pdf")) {
//								String absoluteUrl = hle.getURL().getProtocol() + "://" + new File(".").getAbsolutePath();
//								absoluteUrl = absoluteUrl.substring(0, absoluteUrl.length() -1);
//								absoluteUrl = absoluteUrl + hle.getURL().getHost().replace(" ", "%20");
//								absoluteUrl = absoluteUrl + hle.getURL().getPath().replace(" ", "%20");
//								absoluteUrl = absoluteUrl.trim() + "#search=" + queryField.getText().trim().replace(" ", "%20") + "";
//								
//								System.out.println("URL: " + absoluteUrl);
//								desktop.browse(new URI(absoluteUrl));
//							} else {
								desktop.open(new File(hle.getURL().getHost() + hle.getURL().getPath()));
//							}
						}
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}

				}  
			}  
		});  

		browserPane = new JScrollPane(htmlPane);

		Container centerContainer = new Container();
		centerContainer.setLayout(new BorderLayout());
		centerContainer.add(browserPane, BorderLayout.CENTER);

		this.add(topContainer, BorderLayout.PAGE_START);
		this.add(centerContainer, BorderLayout.CENTER);
	}


	/**
	 * Creates the and show gui.
	 */
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("DocumentsSearchPanel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		DocumentsSearchPanel newContentPane = new DocumentsSearchPanel("snomed");
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
		if ("search".equals(e.getActionCommand()) && queryField.getText() != null) {
			String results = DocumentManager.searchDocuments(queryField.getText());
			htmlPane.setText(results);
			htmlPane.setCaretPosition(0);
		}
	}

}
