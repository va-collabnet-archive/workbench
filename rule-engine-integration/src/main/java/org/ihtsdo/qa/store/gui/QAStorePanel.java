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

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.ihtsdo.qa.store.QAStoreBI;

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
		
		resultsPanel = new QAResultsBrowser(store, tabbedPane1);
		panel2.add(resultsPanel, BorderLayout.CENTER);
		casesPanel = new QACasesBrowser(store, resultsPanel, tabbedPane2);
		panel3.add(casesPanel, BorderLayout.CENTER);
		
		resultsPanel.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				table1MouseClicked(e);
			}
		});
	}
	
	private void table1MouseClicked(MouseEvent e) {
		if(e.getClickCount() == 1){
			casesPanel.setupPanel(store);
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		tabbedPane1 = new JTabbedPane();
		panel2 = new JPanel();
		tabbedPane2 = new JTabbedPane();
		panel3 = new JPanel();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0, 1.0E-4};

		//======== tabbedPane1 ========
		{

			//======== panel2 ========
			{
				panel2.setLayout(new BorderLayout());
			}
			tabbedPane1.addTab("QA Rules", panel2);

		}
		add(tabbedPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== tabbedPane2 ========
		{

			//======== panel3 ========
			{
				panel3.setLayout(new BorderLayout());
			}
			tabbedPane2.addTab("Cases", panel3);

		}
		add(tabbedPane2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JTabbedPane tabbedPane1;
	private JPanel panel2;
	private JTabbedPane tabbedPane2;
	private JPanel panel3;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
