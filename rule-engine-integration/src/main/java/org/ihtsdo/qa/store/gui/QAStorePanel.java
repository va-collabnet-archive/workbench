/*
 * Created by JFormDesigner on Mon Nov 29 18:17:07 GMT-03:00 2010
 */

package org.ihtsdo.qa.store.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.*;

import org.ihtsdo.qa.store.QAStoreBI;
import org.ihtsdo.qa.store.model.QACoordinate;
import org.ihtsdo.qa.store.model.Rule;

/**
 * @author Guillermo Reynoso
 */
public class QAStorePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private QAStoreBI store;
	private QAResultsBrowser resultsPanel;
	private QACasesBrowser casesPanel;
	
	public QAStorePanel(QAStoreBI store) {
		this.store = store;
		initComponents();
		resultsPanel = new QAResultsBrowser(store);
		panel2.add(resultsPanel, BorderLayout.CENTER);
		casesPanel = new QACasesBrowser(store, null, null);
		panel3.add(casesPanel, BorderLayout.CENTER);
		
		resultsPanel.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				table1MouseClicked(e);
			}
		});
	}
	
	private void table1MouseClicked(MouseEvent e) {
		QACoordinate coordinate = resultsPanel.getQACoordinate();
		int selectedRow = resultsPanel.getTable().getSelectedRow();
		Rule rule = (Rule) resultsPanel.getTable().getValueAt(selectedRow, 1);
		casesPanel.setupPanel(store, coordinate, rule);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		label1 = new JLabel();
		panel2 = new JPanel();
		panel3 = new JPanel();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label1 ----
			label1.setText("QA Store panel");
			panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel2 ========
		{
			panel2.setLayout(new BorderLayout());
		}
		add(panel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel3 ========
		{
			panel3.setLayout(new BorderLayout());
		}
		add(panel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JLabel label1;
	private JPanel panel2;
	private JPanel panel3;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
