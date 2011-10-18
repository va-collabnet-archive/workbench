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

package org.ihtsdo.issue.integration.panels;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.TaskFailedException;

/**
 * The Class RefsetSpecReviewDetailPanel.
 */
public class RefsetSpecReviewDetailPanel extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The business process. */
	private BusinessProcess businessProcess;
	
	/** The bp file. */
	private File bpFile;
	
	/**
	 * Instantiates a new refset spec review detail panel.
	 */
	public RefsetSpecReviewDetailPanel() {
		initComponents();
	}

	/**
	 * Instantiates a new refset spec review detail panel.
	 * 
	 * @param config the config
	 */
	public RefsetSpecReviewDetailPanel(I_ConfigAceFrame config){
		initComponents();
		if (config.getRefsetInSpecEditor() != null) {
			setRefsetName( config.getRefsetInSpecEditor().toString());
			try {
				setRefsetUUId(config.getRefsetInSpecEditor().getUids().iterator().next().toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			config.setStatusMessage("Using hierarchy selection...");
			setRefsetName( config.getHierarchySelection().toString());
			try {
				setRefsetUUId(config.getHierarchySelection().getUids().iterator().next().toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * The Class BPFilter.
	 */
	class BPFilter extends javax.swing.filechooser.FileFilter {
		
		/* (non-Javadoc)
		 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File file) {
			String filename = file.getName();
			return filename.endsWith(".bp");
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		public String getDescription() {
			return "Business process file";
		}
	}

	/**
	 * Bp_click.
	 * 
	 * @param e the e
	 */
	private void bp_click(ActionEvent e) {

		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle("Select BusinessProcess to attach...");
		fileChooser.addChoosableFileFilter(new BPFilter());
		txtBpFile.setText("");

		try {
			int returnValue = fileChooser
			.showDialog(new Frame(), "Choose BP file");
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				bpFile=fileChooser.getSelectedFile();
				txtBpFile.setText(bpFile.getName());
			} else {
				throw new TaskFailedException("User failed to select a file.");
			}
			InputStream file = new FileInputStream(bpFile);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream (buffer);
			businessProcess = (BusinessProcess) input.readObject();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (TaskFailedException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		refsetName = new JLabel();
		label2 = new JLabel();
		refsetUUId = new JLabel();
		separator1 = new JSeparator();
		label3 = new JLabel();
		scrollPane1 = new JScrollPane();
		textArea1 = new JTextArea();
		label6 = new JLabel();
		label4 = new JLabel();
		txtBpFile = new JTextField();
		button1 = new JButton();
		label5 = new JLabel();
		textField1 = new JTextField();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

		//---- label1 ----
		label1.setText("Refset Name:");
		add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
		add(refsetName, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

		//---- label2 ----
		label2.setText("Refset UUID:");
		add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
		add(refsetUUId, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));
		add(separator1, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

		//---- label3 ----
		label3.setText("Comment:");
		add(label3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(textArea1);
		}
		add(scrollPane1, new GridBagConstraints(1, 3, 2, 2, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

		//---- label6 ----
		label6.setIcon(null);
		label6.setText(" ");
		add(label6, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

		//---- label4 ----
		label4.setText("Business Process:");
		add(label4, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
		add(txtBpFile, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

		//---- button1 ----
		button1.setText("...");
		button1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bp_click(e);
			}
		});
		add(button1, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 5, 0), 0, 0));

		//---- label5 ----
		label5.setText("SME Name:");
		add(label5, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
		add(textField1, new GridBagConstraints(1, 6, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The label1. */
	private JLabel label1;
	
	/** The refset name. */
	private JLabel refsetName;
	
	/** The label2. */
	private JLabel label2;
	
	/** The refset uu id. */
	private JLabel refsetUUId;
	
	/** The separator1. */
	private JSeparator separator1;
	
	/** The label3. */
	private JLabel label3;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The text area1. */
	private JTextArea textArea1;
	
	/** The label6. */
	private JLabel label6;
	
	/** The label4. */
	private JLabel label4;
	
	/** The txt bp file. */
	private JTextField txtBpFile;
	
	/** The button1. */
	private JButton button1;
	
	/** The label5. */
	private JLabel label5;
	
	/** The text field1. */
	private JTextField textField1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	/**
	 * Gets the business process.
	 * 
	 * @return the business process
	 */
	public BusinessProcess getBusinessProcess() {
		return businessProcess;
	}

	/**
	 * Sets the business process.
	 * 
	 * @param businessProcess the new business process
	 */
	public void setBusinessProcess(BusinessProcess businessProcess) {
		this.businessProcess = businessProcess;
	}

	/**
	 * Gets the bp file.
	 * 
	 * @return the bp file
	 */
	public File getBpFile() {
		return bpFile;
	}

	/**
	 * Sets the bp file.
	 * 
	 * @param bpFile the new bp file
	 */
	public void setBpFile(File bpFile) {
		this.bpFile = bpFile;
	}

	/**
	 * Gets the refset name.
	 * 
	 * @return the refset name
	 */
	public String getRefsetName() {
		return refsetName.getText();
	}

	/**
	 * Sets the refset name.
	 * 
	 * @param refsetName the new refset name
	 */
	public void setRefsetName(String refsetName) {
		this.refsetName.setText( refsetName);
	}

	/**
	 * Gets the refset uu id.
	 * 
	 * @return the refset uu id
	 */
	public String getRefsetUUId() {
		return refsetUUId.getText();
	}

	/**
	 * Sets the refset uu id.
	 * 
	 * @param refsetUUId the new refset uu id
	 */
	public void setRefsetUUId(String refsetUUId) {
		this.refsetUUId.setText(refsetUUId);
	}

	/**
	 * Gets the comment.
	 * 
	 * @return the comment
	 */
	public String getComment() {
		return textArea1.getText();
	}

	/**
	 * Sets the text area1.
	 * 
	 * @param comment the new text area1
	 */
	public void setTextArea1(String comment) {
		this.textArea1.setText(comment) ;
	}

	/**
	 * Gets the sME name.
	 * 
	 * @return the sME name
	 */
	public String getSMEName() {
		return textField1.getText();
	}

	/**
	 * Sets the sME name.
	 * 
	 * @param SMEName the new sME name
	 */
	public void setSMEName(String SMEName) {
		this.textField1.setText(SMEName);
	}
}
