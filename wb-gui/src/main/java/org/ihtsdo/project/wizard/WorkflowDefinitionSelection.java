/*
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

package org.ihtsdo.project.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.wizard.I_fastWizard;

/**
 * The Class WorkflowDefinitionSelection.
 *
 * @author Alejandro Rodriguez
 */
public class WorkflowDefinitionSelection extends JPanel implements I_fastWizard  {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 664645041131555989L;
	
	/**
	 * Instantiates a new workflow definition selection.
	 */
	public WorkflowDefinitionSelection() {
		initComponents();
		loadCombo();
		
	}

	/**
	 * Load combo.
	 */
	private void loadCombo() {
		List<File> files=WfComponentProvider.getWorkflowDefinitionFiles();
		for (File file : files) {
			comboBox1.addItem(file);
		}
	}


	/**
	 * Button1 action performed.
	 */
	private void button1ActionPerformed() {

		File businessProcessFile = null;
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("./sampleProcesses"));

		chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".wfd");
			}

			public String getDescription() {
				return "WFD workflow Definition files";
			}
		});

		int r = chooser.showOpenDialog(null);
		if (r == JFileChooser.APPROVE_OPTION) {
			businessProcessFile = chooser.getSelectedFile();
			if (businessProcessFile!=null){
				comboBox1.addItem(businessProcessFile);
				comboBox1.setSelectedItem(businessProcessFile);
			}
		}
		
		chooser=null;

	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		comboBox1 = new JComboBox();
		button1 = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {15, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

		//---- label1 ----
		label1.setText("Workflow Definition File:");
		add(label1, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(comboBox1, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- button1 ----
		button1.setText("...");
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button1ActionPerformed();
			}
		});
		add(button1, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The label1. */
	private JLabel label1;
	
	/** The combo box1. */
	private JComboBox comboBox1;
	
	/** The button1. */
	private JButton button1;
	
	/** The key. */
	private String key;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	/* (non-Javadoc)
	 * @see org.ihtsdo.wizard.I_fastWizard#getData()
	 */
	@Override
	public HashMap<String, Object> getData() throws Exception {
		HashMap<String, Object> retMap=new HashMap<String, Object>();
		
		File f=(File)comboBox1.getSelectedItem();
		if (f!=null && f.exists()){
			retMap.put(key, f);
		}else{
			throw new Exception("The Worflow definition file does't exist or is null.");
		}
			
		return retMap;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.wizard.I_fastWizard#setKey(java.lang.String)
	 */
	@Override
	public void setKey(String key) {
		this.key=key;

	}

}
