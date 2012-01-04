/*
 * Created by JFormDesigner on Mon Dec 05 21:31:12 GMT-03:00 2011
 */

package org.ihtsdo.project.workflow.wizard;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;

import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.wizard.I_fastWizard;

/**
 * @author Alejandro Rodriguez
 */
public class WorkflowDefinitionSelection extends JPanel implements I_fastWizard  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 664645041131555989L;
	/**
	 * 
	 */
	public WorkflowDefinitionSelection() {
		initComponents();
		loadCombo();
		
	}

	private void loadCombo() {
		List<File> files=WfComponentProvider.getWorkflowDefinitionFiles();
		for (File file : files) {
			comboBox1.addItem(file);
		}
	}


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
	private JLabel label1;
	private JComboBox comboBox1;
	private JButton button1;
	private String key;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
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

	@Override
	public void setKey(String key) {
		this.key=key;

	}

}
