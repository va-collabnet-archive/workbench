/*
 * Created by JFormDesigner on Wed Mar 24 16:46:20 GMT-03:00 2010
 */

package org.ihtsdo.project.panel.details;

import java.awt.*;
import javax.swing.*;

/**
 * @author Alejandro Rodriguez
 */
public class ProjectDetailsContainer extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel guestPanel;
	public ProjectDetailsContainer() {
		initComponents();
	}
	public void addContent(JPanel panel){
		if (panel!=null){
			this.guestPanel=panel;
			add(this.guestPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
		}
	}
	
	public void removeContent(){
		if (this.guestPanel!=null){
			remove(this.guestPanel);
			this.guestPanel=null;
		}
	}
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents

		//======== this ========
		setBackground(new Color(238, 238, 238));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
