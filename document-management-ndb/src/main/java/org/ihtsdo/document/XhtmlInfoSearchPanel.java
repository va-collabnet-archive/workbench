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
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.UUID;

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

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * The Class XhtmlInfoSearchPanel.
 */
public class XhtmlInfoSearchPanel extends JPanel implements ActionListener {

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
	
	/** The vodb directory. */
	static File vodbDirectory;
	
	/** The read only. */
	static boolean readOnly = false;
	
	/** The cache size. */
	static Long cacheSize = Long.getLong("600000000");
	
	/** The db setup config. */
	static DatabaseSetupConfig dbSetupConfig;
	
	/** The config. */
	static I_ConfigAceFrame config;
	
	/** The tf. */
	static I_TermFactory tf;

	/**
	 * Instantiates a new xhtml info search panel.
	 * 
	 * @param query the query
	 * @param config the config
	 */
	public XhtmlInfoSearchPanel(String query, I_ConfigAceFrame config) {
		XhtmlInfoSearchPanel.config = config;
		this.setLayout(new BorderLayout());
		Box topContainer = new Box(BoxLayout.X_AXIS);
		topContainer.add(new JLabel("Search XHTML Info:"));
		queryField = new JTextField(30);
		queryField.setText(query);
		topContainer.add(queryField);
		searchButton = new JButton("Search");
		searchButton.setActionCommand("search");
		searchButton.addActionListener(this);
		topContainer.add(searchButton);

		String results = DocumentManager.getInfoForTerm(queryField.getText(), config);
		htmlPane = new JEditorPane("text/html", results);
		htmlPane.addHyperlinkListener(new HyperlinkListener() {  
			public void hyperlinkUpdate(HyperlinkEvent hle) {  
				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {  
					System.out.println(hle.getURL());  
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
		XhtmlInfoSearchPanel newContentPane = new XhtmlInfoSearchPanel("", config);
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
		
		try {
			vodbDirectory = new File("/Users/alo/Documents/TermMed/Eclipse/MrcmMaven/miniSct-ide-sa2/target/sct-wb-ide-sa-bundle.dir/berkeley-db");
			dbSetupConfig = new DatabaseSetupConfig();
			LocalVersionedTerminology.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
			tf = LocalVersionedTerminology.get();
			FileInputStream fin = new FileInputStream("config.dat");
			ObjectInputStream ois = new ObjectInputStream(fin);
			config = (I_ConfigAceFrame) ois.readObject();
			ois.close();
			config.getViewPositionSet().add(tf.newPosition(tf.getPath(new UUID[] {UUID.fromString("d2f8b990-82b9-11de-8a39-0800200c9a66")}), 
					Integer.MAX_VALUE));
			tf.setActiveAceFrameConfig(config);
		}
		catch (Exception e) { e.printStackTrace(); }

		
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
			String results = DocumentManager.getInfoForTerm(queryField.getText(), config);
			htmlPane.setText(results);
			htmlPane.setCaretPosition(0);
		}
	}

}
