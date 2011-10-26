/*
 * Created by JFormDesigner on Thu Nov 04 19:18:09 GMT-03:00 2010
 */

package org.ihtsdo.project.tasks;

import java.awt.*;
import javax.swing.*;

/**
 * @author Guillermo Reynoso
 */
public class SetDeliveryOrSelfAssignPanel extends JPanel {
	public SetDeliveryOrSelfAssignPanel() {
		initComponents();
	}

	public boolean isDelivery(){
		return bDeli.isSelected();
	}
	public boolean isSelfAssign(){
		return bSelf.isSelected();
	}
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		bDeli = new JRadioButton();
		bSelf = new JRadioButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {20, 0, 15, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {20, 0, 0, 15, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

		//---- bDeli ----
		bDeli.setText("Deliver to workflow");
		bDeli.setSelected(true);
		add(bDeli, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- bSelf ----
		bSelf.setText("Execute process now");
		add(bSelf, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(bDeli);
		buttonGroup1.add(bSelf);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JRadioButton bDeli;
	private JRadioButton bSelf;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
