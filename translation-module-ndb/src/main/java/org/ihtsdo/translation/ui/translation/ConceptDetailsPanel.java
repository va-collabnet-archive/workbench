/*
 * Created by JFormDesigner on Tue Mar 06 16:20:05 GMT-03:00 2012
 */

package org.ihtsdo.translation.ui.translation;

import java.awt.*;
import javax.swing.*;

/**
 * @author Guillermo Reynoso
 */
public class ConceptDetailsPanel extends JPanel {
	public ConceptDetailsPanel() {
		initComponents();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		tree3 = new JTree();

		//======== this ========
		setLayout(new BorderLayout());

		//---- tree3 ----
		tree3.setVisibleRowCount(4);
		tree3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		add(tree3, BorderLayout.CENTER);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JTree tree3;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
