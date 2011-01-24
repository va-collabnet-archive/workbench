/*
 * Created by JFormDesigner on Mon Jun 28 16:29:05 GMT-03:00 2010
 */

package org.ihtsdo.translation.ui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkListMember;

/**
 * @author Guillermo Reynoso
 */
public class TranslationConceptFrame extends JFrame {
	TranslationConceptEditorRO translationConceptViewer1;
	public TranslationConceptFrame() {
		initComponents();
	}
	public TranslationConceptFrame(WorkListMember member,
			TranslationProject project) {
		initComponents();


		WindowListener wndCloser = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
				dispose();

			}
		};
		addWindowListener(wndCloser);
		setSize(800, 500);
		Container contentPane = getContentPane();
		translationConceptViewer1=new TranslationConceptEditorRO();
		contentPane.add(translationConceptViewer1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		this.validate();
		this.repaint();
		translationConceptViewer1.updateUI(project,member);
	}
	public TranslationConceptEditorRO getTranslationConceptViewer(){
		return translationConceptViewer1;
	}
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
