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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.LANG_CODE;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.util.IconUtilities;

/**
 * The Class DictionaryIndexPanel.
 */
public class DictionaryIndexPanel extends JPanel  implements ActionListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The html pane. */
	JEditorPane htmlPane;
	
	/** The browser pane. */
	JScrollPane browserPane;
	
	/** The index file button. */
	JButton indexFileButton;
	
	/** The index wb button. */
	JButton indexWBButton;

	private JComboBox cmbLangs;
	
	private JLabel helpLabel;

	
	/**
	 * Instantiates a new dictionary index panel.
	 */
	public DictionaryIndexPanel() {
		this.setLayout(new BorderLayout());
		
		String results = "<html><body><font style='font-family:arial,sans-serif'>" +
		"Select an option to index the dictionary..." +
		"</font></body></html>";
		htmlPane = new JEditorPane("text/html", results);
		browserPane = new JScrollPane(htmlPane);

		cmbLangs= new JComboBox();
		for (LANG_CODE lCode:ArchitectonicAuxiliary.LANG_CODE.values()){
			I_GetConceptData langName;
			try {
				langName = Terms.get().getConcept(ArchitectonicAuxiliary.getLanguageConcept(lCode.name()).getUids());

				cmbLangs.addItem(langName);
			} catch (TerminologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (Exception e){
//				e.printStackTrace();
			}
		}

		indexFileButton = new JButton("Index dictionary from text file");
		indexFileButton.setActionCommand("indexFromFile");
		indexFileButton.addActionListener(this);
		
		helpLabel = new JLabel();
		helpLabel.setIcon(IconUtilities.helpIcon);
		helpLabel.setText("");
		helpLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					HelpApi.openHelpForComponent("INDEX_DICTIONARY");
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				};
			}
		});
		
//		indexWBButton = new JButton("Index dictionary from current workbench database");
//		indexWBButton.setActionCommand("indexFromWBDB");
//		indexWBButton.addActionListener(this);
		
		Container centerContainer = new Container();
		centerContainer.setLayout(new BorderLayout());
		centerContainer.add(browserPane, BorderLayout.CENTER);
		this.add(centerContainer, BorderLayout.CENTER);
		
		Container bottomContainer = new Container();
		bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));
		bottomContainer.add(cmbLangs);
		bottomContainer.add(indexFileButton);
		bottomContainer.add(helpLabel);
		
//		bottomContainer.add(indexWBButton);
		
		this.add(bottomContainer, BorderLayout.PAGE_END);
		
	}
	
	 /**
 	 * Creates the and show gui.
 	 */
 	private static void createAndShowGUI() {
	        //Create and set up the window.
	        JFrame frame = new JFrame("DictionaryIndexPanel");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	        //Create and set up the content pane.
	        DictionaryIndexPanel newContentPane = new DictionaryIndexPanel();
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
    		if ("indexFromFile".equals(e.getActionCommand())) {
    			String results = "<html><body><font style='font-family:arial,sans-serif'>" +
    			"Indexing from file..." +
    			"</font></body></html>";
    			htmlPane.setText(results);
    			htmlPane.setCaretPosition(0);
    			htmlPane.revalidate();
    			I_GetConceptData concLang= (I_GetConceptData)cmbLangs.getSelectedItem();
    			if (concLang!=null){
    				String lCode=null;
    				try {
    					lCode = ArchitectonicAuxiliary.getLanguageCode(concLang.getUids());
    				} catch (NoSuchElementException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				} catch (IOException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}
    				if (lCode!=null){
    					JFileChooser fileopen = new JFileChooser();
    					int ret = fileopen.showDialog(this, "Open file");
    					File dictionaryTextFile = null;
    					if (ret == JFileChooser.APPROVE_OPTION) {
    						dictionaryTextFile = fileopen.getSelectedFile();
    					}
    					if(dictionaryTextFile != null){
    						results = DocumentManager.indexDictionaryFromTextFile(true,dictionaryTextFile);
    					}else{
    						results = "No file was selected...";
    					}
    					htmlPane.setText(results);
    					htmlPane.setCaretPosition(0);
    					htmlPane.revalidate();
    				}
    			}
    		}
	    	
	    	if ("indexFromWBDB".equals(e.getActionCommand())) {
	    		String results = "<html><body><font style='font-family:arial,sans-serif'>" +
	    		"Indexing from WorkBench database..." +
	    		"</font></body></html>";
	    		htmlPane.setText(results);
	    		htmlPane.setCaretPosition(0);
	    		htmlPane.revalidate();
	    		results = DocumentManager.indexDictionaryFromWorkbenchDatabase(true);
	    		htmlPane.setText(results);
	    		htmlPane.setCaretPosition(0);
	    		htmlPane.revalidate();
	    	}
	    }
}
