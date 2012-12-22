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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.ihtsdo.document.DocumentsIndexPanel.DocumentIndexWorker;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.util.IconUtilities;

/**
 * The Class TranslationMemoryIndexPanel.
 */
public class TranslationMemoryIndexPanel extends JPanel implements ActionListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The html pane. */
	JEditorPane htmlPane;

	/** The browser pane. */
	JScrollPane browserPane;

	/** The index button. */
	JButton indexButton;

	/** The help label. */
	private JLabel helpLabel;

	private SwingWorker<String, String> memoryIndexWorker;

	private JProgressBar progressBar = new JProgressBar();

	/**
	 * Instantiates a new translation memory index panel.
	 */
	public TranslationMemoryIndexPanel() {
		this.setLayout(new BorderLayout());

		String results = "<html><body><font style='font-family:arial,sans-serif'>" + "Press the button to select the Translation Memory file..." + "</font></body></html>";
		htmlPane = new JEditorPane("text/html", results);
		browserPane = new JScrollPane(htmlPane);

		indexButton = new JButton("Select file and index translation memory");
		indexButton.setActionCommand("index");
		indexButton.addActionListener(this);

		helpLabel = new JLabel();
		helpLabel.setIcon(IconUtilities.helpIcon);
		helpLabel.setText("");
		helpLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					HelpApi.openHelpForComponent("INDEX_TRANS_MEMORY");
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
				;
			}
		});

		Container centerContainer = new Container();
		centerContainer.setLayout(new BorderLayout());
		centerContainer.add(browserPane, BorderLayout.CENTER);
		this.add(centerContainer, BorderLayout.CENTER);

		Container bottomContainer = new Container();
		bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));
		bottomContainer.add(indexButton);
		bottomContainer.add(helpLabel);
		bottomContainer.add(progressBar);

		this.add(bottomContainer, BorderLayout.PAGE_END);

	}

	/**
	 * Creates the and show gui.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("TranslationMemoryIndexPanel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		TranslationMemoryIndexPanel newContentPane = new TranslationMemoryIndexPanel();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if ("index".equals(e.getActionCommand())) {
			if (memoryIndexWorker != null && !memoryIndexWorker.isDone()) {
				memoryIndexWorker.cancel(true);
				memoryIndexWorker = null;
			}

			memoryIndexWorker = new MemoryIndexWorker();
			memoryIndexWorker.addPropertyChangeListener(new ProgressListener(progressBar));
			memoryIndexWorker.execute();
		}
	}
	class MemoryIndexWorker extends SwingWorker<String, String> {
		String results = "<html><body><font style='font-family:arial,sans-serif'>" + "Indexing..." + "</font></body></html>";

		@Override
		protected String doInBackground() throws Exception {
			htmlPane.setText(results);
			htmlPane.setCaretPosition(0);
			try {
				results = null;
				results = DocumentManager.indexMemoryFromXls();
				JOptionPane.showMessageDialog(TranslationMemoryIndexPanel.this, "Translation Memory indexed...", "Info", JOptionPane.INFORMATION_MESSAGE);
			} catch (OfficeXmlFileException e2) {
				e2.printStackTrace();
				JOptionPane.showMessageDialog(TranslationMemoryIndexPanel.this, e2.getMessage(), "Error, the input file must be an XLS (not XLSX)", JOptionPane.ERROR_MESSAGE);
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(TranslationMemoryIndexPanel.this, e1.getMessage(), "Error reading file", JOptionPane.ERROR_MESSAGE);
			} catch (Exception e3) {
				e3.printStackTrace();
				JOptionPane.showMessageDialog(TranslationMemoryIndexPanel.this, e3.getMessage(), "Unspecified Error. Check logs.", JOptionPane.ERROR_MESSAGE);
			}
			return results;
		}

		@Override
		protected void done() {
			try {
				results = get();
				if(results != null){
					htmlPane.setText(results);
					htmlPane.setCaretPosition(0);
				}else{
					htmlPane.setText("");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			super.done();
		}

	}
}
