package org.dwfa.ace;

import java.awt.Component;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.types.ConceptBean;

public class TermComponentListSelectionListener implements
		ListSelectionListener {

	I_ContainTermComponent linkedComponent;
	
	private boolean warnForUncommitted = false;

	public TermComponentListSelectionListener(I_ContainTermComponent linkedComponent) {
		super();
		this.linkedComponent = linkedComponent;
	}

	public void valueChanged(ListSelectionEvent e) {
		try {
			ConceptBean currentBean = (ConceptBean) linkedComponent
					.getTermComponent();
			if (currentBean != null) {
				if (currentBean.isUncommitted()) {
					if (warnForUncommitted) {
						int option = JOptionPane.showConfirmDialog((Component) linkedComponent, 
								"This view contains an uncommited concept. If you continue, the " +
								"focused concept will change...", 
								"Uncommitted component", 
								JOptionPane.OK_CANCEL_OPTION);
						if (JOptionPane.OK_OPTION == option) {
							setLinkedComponent(e);
						}
					} else {
						setLinkedComponent(e);
					}
				} else {
					setLinkedComponent(e);
				}
			} else {
				setLinkedComponent(e);
			}
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLog(Level.SEVERE, e1.getLocalizedMessage(), e1);
		}
	}


	private void setLinkedComponent(ListSelectionEvent e) {
		JList list = (JList) e.getSource();
		TerminologyListModel model = (TerminologyListModel) list.getModel();
		if (list.getSelectedIndex() == -1) {
			linkedComponent.setTermComponent(null);
		} else {
			linkedComponent.setTermComponent(model.getElementAt(list.getSelectedIndex()));
		}
	}

	public boolean getWarnForUncommitted() {
		return warnForUncommitted;
	}

	public void setWarnForUncommitted(boolean warnForUncommitted) {
		this.warnForUncommitted = warnForUncommitted;
	}

}
