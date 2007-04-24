package org.dwfa.ace.edit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dwfa.ace.AceLog;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;

public abstract class AddComponent implements ActionListener {
	private I_ContainTermComponent termContainer;
	private I_ConfigAceFrame config;

	public AddComponent(I_ContainTermComponent termContainer, I_ConfigAceFrame config) {
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
			AceLog.getAppLog().alertAndLogException(e1);
		}
	}

	protected abstract void doEdit(I_ContainTermComponent termContainer, ActionEvent e, I_ConfigAceFrame config) throws Exception;

}
