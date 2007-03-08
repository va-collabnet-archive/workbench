package org.dwfa.ace.edit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dwfa.ace.I_ContainTermComponent;
import org.dwfa.ace.config.AceFrameConfig;

public abstract class AddComponent implements ActionListener {
	private I_ContainTermComponent termContainer;
	private AceFrameConfig config;

	public AddComponent(I_ContainTermComponent termContainer, AceFrameConfig config) {
		super();
		this.termContainer = termContainer;
		this.config = config;
	}

	public final void actionPerformed(ActionEvent e) {
		try {
			if (termContainer.getConfig().getEditingPathSet().size() == 0) {
				JOptionPane.showMessageDialog(new JFrame(), "You must select an editing path before editing...");
				return;
			}
			doEdit(termContainer, e, config);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(new JFrame(), e1.getMessage());
			e1.printStackTrace();
		}
	}

	protected abstract void doEdit(I_ContainTermComponent termContainer, ActionEvent e, AceFrameConfig config) throws Exception;

}
