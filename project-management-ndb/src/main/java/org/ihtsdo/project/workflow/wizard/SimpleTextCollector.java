/*
 * Created by JFormDesigner on Wed Dec 07 21:30:04 GMT-03:00 2011
 */

package org.ihtsdo.project.workflow.wizard;

import java.awt.*;
import java.util.HashMap;

import javax.swing.*;

import org.ihtsdo.wizard.I_fastWizard;

/**
 * @author Guillermo Reynoso
 */
public class SimpleTextCollector extends JPanel implements I_fastWizard {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4533413277239846148L;
	public SimpleTextCollector() {
		initComponents();
	}

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
	private JLabel label1;
	private JTextField textField1;
	private String key;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	@Override
	public HashMap<String, Object> getData() throws Exception {
		if (textField1.getText().trim().equals("")){
			throw new Exception ("The name cannot be null.");
		}
		HashMap<String, Object> res=new HashMap<String, Object>();
		res.put(key, textField1.getText());
		return res;
	}

	@Override
	public void setKey(String key) {
		this.key=key;
	}
	public void setLabel(String strLabel){
		label1.setText(strLabel);
	}
}
