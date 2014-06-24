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
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.ihtsdo.wizard.I_fastWizard;

/**
 * The Class SimpleTextCollector.
 *
 * @author Guillermo Reynoso
 */
public class SimpleTextCollector extends JPanel implements I_fastWizard {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4533413277239846148L;
	
	/**
	 * Instantiates a new simple text collector.
	 */
	public SimpleTextCollector() {
		initComponents();
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		textField1 = new JTextField();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {15, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

		//---- label1 ----
		label1.setText("text");
		add(label1, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(textField1, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The label1. */
	private JLabel label1;
	
	/** The text field1. */
	private JTextField textField1;
	
	/** The key. */
	private String key;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	/* (non-Javadoc)
	 * @see org.ihtsdo.wizard.I_fastWizard#getData()
	 */
	@Override
	public HashMap<String, Object> getData() throws Exception {
		if (textField1.getText().trim().equals("")){
			throw new Exception ("The name cannot be null.");
		}
		HashMap<String, Object> res=new HashMap<String, Object>();
		res.put(key, textField1.getText());
		return res;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.wizard.I_fastWizard#setKey(java.lang.String)
	 */
	@Override
	public void setKey(String key) {
		this.key=key;
	}
	
	/**
	 * Sets the label.
	 *
	 * @param strLabel the new label
	 */
	public void setLabel(String strLabel){
		label1.setText(strLabel);
	}
}
