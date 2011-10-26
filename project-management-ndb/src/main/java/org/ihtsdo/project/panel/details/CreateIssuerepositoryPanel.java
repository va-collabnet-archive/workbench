/*
 * Created by JFormDesigner on Wed Sep 01 18:19:09 GMT-03:00 2010
 */

package org.ihtsdo.project.panel.details;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.dwfa.ace.api.Terms;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;

/**
 * @author Guillermo Reynoso
 */
public class CreateIssuerepositoryPanel extends JPanel {
	public CreateIssuerepositoryPanel() {
		initComponents();
	}

	private void button1ActionPerformed(ActionEvent e) {
		try {
			if (textField1.getText() == null || textField1.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"Issue Repository name is required!", 
						"Error", JOptionPane.ERROR_MESSAGE);

				throw new Exception("Issue Repository name is required!");
			}
			if (textField2.getText() == null || textField2.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"Issue Repository id is required!", 
						"Error", JOptionPane.ERROR_MESSAGE);
				
				throw new Exception("Issue Repository id is required!");
			}
			if (textField3.getText() == null || textField3.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"Issue Repository url is required!", 
						"Error", JOptionPane.ERROR_MESSAGE);
				
				throw new Exception("Issue Repository url is required!");
			}
			IssueRepository issueRepoWithMetadata = new IssueRepository(textField2.getText(),
					textField3.getText(), textField1.getText(), IssueRepository.REPOSITORY_TYPE.WEB_SITE.ordinal());
			IssueRepositoryDAO.addIssueRepoToMetahier(issueRepoWithMetadata, Terms.get().getActiveAceFrameConfig());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		close();
	}

	private void button2ActionPerformed() {
			close();
	}
	
	private void close(){

		Window w=SwingUtilities.getWindowAncestor(this);
		w.setVisible(false);
		w.dispose();
	}
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		label1 = new JLabel();
		hSpacer1 = new JPanel(null);
		panel3 = new JPanel();
		label2 = new JLabel();
		textField1 = new JTextField();
		label3 = new JLabel();
		textField2 = new JTextField();
		label4 = new JLabel();
		textField3 = new JTextField();
		hSpacer2 = new JPanel(null);
		panel2 = new JPanel();
		button2 = new JButton();
		button1 = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label1 ----
			label1.setText("Create new Issue Repository");
			panel1.add(label1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 5), 0, 0));
		}
		add(panel1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(hSpacer1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {133, 188, 0};
			((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
			((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

			//---- label2 ----
			label2.setText("Repository name:");
			panel3.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
			panel3.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- label3 ----
			label3.setText("ID:");
			panel3.add(label3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
			panel3.add(textField2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- label4 ----
			label4.setText("URL:");
			panel3.add(label4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel3.add(textField3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(hSpacer2, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- button2 ----
			button2.setText("Cancel");
			button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed();
				}
			});
			panel2.add(button2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button1 ----
			button1.setText("Save");
			button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			panel2.add(button1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
			new Insets(0, 0, 0, 5), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JLabel label1;
	private JPanel hSpacer1;
	private JPanel panel3;
	private JLabel label2;
	private JTextField textField1;
	private JLabel label3;
	private JTextField textField2;
	private JLabel label4;
	private JTextField textField3;
	private JPanel hSpacer2;
	private JPanel panel2;
	private JButton button2;
	private JButton button1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
