/*
 * Created by JFormDesigner on Mon Jan 24 13:55:16 GMT-03:00 2011
 */

package org.ihtsdo.qa.store.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.ihtsdo.qa.store.QAStoreBI;
import org.ihtsdo.qa.store.model.QaCaseComment;

/**
 * @author Guillermo Reynoso
 */
public class CommentDialog extends JDialog {
	
	private static final long serialVersionUID = -221429705860411995L;
	private QaCaseComment comment;
	private QAStoreBI store;
	private String currentUser;
	private UUID caseUuid;
	
	public CommentDialog(Frame owner) {
		super(owner);
		initComponents();
		initCustomComponents();
	}

	public CommentDialog(Dialog owner, QAStoreBI store, String currentUser, UUID caseUuid) {
		super(owner);
		this.store = store;
		this.currentUser = currentUser;
		this.caseUuid = caseUuid;
		initComponents();
		initCustomComponents();
	}
	
	
	private void initCustomComponents() {
		comment = null;
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close(null);
			}
		});
	}

	public QaCaseComment showModalDialog() {
		setModal(true);
		this.setPreferredSize(new Dimension(500,500));
		pack();
		setVisible(true);
		return comment;
	}

	private void close(QaCaseComment canceled) {
		this.comment = canceled;
		dispose();
	}

	private void cancelButtonActionPerformed(ActionEvent e) {
		close(null);
	}

	private void okButtonActionPerformed(ActionEvent e) {
		if(!commentTextArea.getText().equals("")){
			QaCaseComment qaComment = new QaCaseComment();
			qaComment.setAuthor(currentUser);
			qaComment.setComment(commentTextArea.getText());
			qaComment.setEffectiveTime(new Date());
			qaComment.setCaseUuid(caseUuid);
			try {
				store.persistQAComment(qaComment);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			close(qaComment);
		}else{
			close(null);
		}
	}


	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		scrollPane1 = new JScrollPane();
		commentTextArea = new JTextArea();
		label1 = new JLabel();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new BorderLayout(5, 5));

				//======== scrollPane1 ========
				{
					scrollPane1.setViewportView(commentTextArea);
				}
				contentPanel.add(scrollPane1, BorderLayout.CENTER);

				//---- label1 ----
				label1.setText("Comment");
				contentPanel.add(label1, BorderLayout.NORTH);
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

				//---- okButton ----
				okButton.setText("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed(e);
					}
				});
				buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancelButtonActionPerformed(e);
					}
				});
				buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JScrollPane scrollPane1;
	private JTextArea commentTextArea;
	private JLabel label1;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
