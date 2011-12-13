/*
 * Created by JFormDesigner on Mon Dec 05 17:41:01 GMT-03:00 2011
 */

package org.ihtsdo.wizard;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.SoftBevelBorder;

/**
 * @author Guillermo Reynoso
 */
public class WizardFrame extends JDialog {
	private I_fastWizard[] panels;
	private int index;
	private I_fastWizard actualPanel;
	private I_wizardResult result;
	private HashMap<String,Object> mapCollector;
	public WizardFrame(I_fastWizard[] panels,I_wizardResult result,I_notifyPanelChange notifier) {
		initComponents();
		this.notifier=notifier;
		this.panels=panels;
		index=0;
//		updateButtons();
		this.result=result;
		isNotifiying=false;
		this.mapCollector=new HashMap<String,Object>();
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		this.addWindowListener(new WindowAdapter(){
			  public void windowClosing(WindowEvent we){
					setVisible(false);
					removeAll();
					dispose();
					System.gc();
				  
			  }
			});
	}
	public void addPanels(I_fastWizard[] addPanels){
		int newLen=panels.length + addPanels.length; 
		I_fastWizard[] newPanels=new I_fastWizard[newLen];
		int cont=0;
		for (int i=0 ;i<panels.length;i++){
			newPanels[cont]=panels[i];
			cont++;
		}
		for (int i=0 ;i<addPanels.length;i++){
			newPanels[cont]=addPanels[i];
			cont++;
		}
		panels=newPanels;
	}
	public I_fastWizard[] getPanels(){
		return panels;
	}
	public void setPanels( I_fastWizard[] panels,int index){
		this.panels=panels;
		this.index=index;
		updatePanel();
	}
	public void setPanels( I_fastWizard[] panels){
		this.panels=panels;
		updatePanel();
		
	}
	private void updateButtons() {
		if(index==0){
			bback.setEnabled(false);
		}else{
			bback.setEnabled(true);
		}
			
		if (index==panels.length-1)
			bnext.setText("Finish");
		else
			bnext.setText("Next");

	}

	private void bbackActionPerformed() {
		index--;
		updatePanel();
	}

	private void bnextActionPerformed() {
		try {
			getMap();
		} catch (Exception e) {

			JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (bnext.getText().equals("Finish")){
			finish();
			return;
		}
		panel1.removeAll();
		index++;
		updatePanel();

	}

	private void finish() {
		result.setResultMap(mapCollector);
		this.setVisible(false);
		this.removeAll();
		this.dispose();
		System.gc();
	}

	private void getMap() throws Exception {
		mapCollector.putAll(actualPanel.getData());
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		bback = new JButton();
		bnext = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {15, 0, 0, 0, 10, 0};
		((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {15, 0, 0, 0};
		((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4};
		((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};
		}
		contentPane.add(panel1, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- bback ----
		bback.setText("Back");
		bback.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bbackActionPerformed();
			}
		});
		contentPane.add(bback, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 5), 0, 0));

		//---- bnext ----
		bnext.setText("Next");
		bnext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bnextActionPerformed();
			}
		});
		contentPane.add(bnext, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 5), 0, 0));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JButton bback;
	private JButton bnext;
	private I_notifyPanelChange notifier;
	private boolean isNotifiying;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	public void setPanel(int i) {

		index=i;
		updatePanel();

	}

	private void updatePanel() {
		panel1.removeAll();
		updateButtons();
		if (!isNotifiying){
			isNotifiying=true;
			notifyLauncher();
			isNotifiying=false;
		}
		((JPanel)panels[index]).revalidate();
		((JPanel)panels[index]).validate();
		actualPanel=panels[index];
		panel1.add((JPanel)actualPanel, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		((JPanel)actualPanel).repaint();
		panel1.revalidate();
		((JPanel)actualPanel).revalidate();
		((JPanel)actualPanel).validate();

		panel1.repaint();
		this.setSize(this.getWidth()-1, this.getHeight()-1);
		this.validate();
		this.setSize(this.getWidth()+1, this.getHeight()+1);
	
	}

	private void notifyLauncher() {
		if (notifier!=null)
			notifier.notifyThis(this,index,mapCollector);
		
	}

	public HashMap<String, Object> getMapCollector() {
		return mapCollector;
	}
}
