package org.dwfa.ace.path;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.data.ArrayListModel;
import org.dwfa.tapi.TerminologyException;

public class SelectPathSetPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Set<I_Position> positionSet = new HashSet<I_Position>();

	private JList positionList;

	private ArrayListModel<I_Position> positionListModel;

	private SelectPathAndPositionPanelWithCombo pppwc;
	
	private class AddPathActionLister implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			try {
			if (positionSet.contains(getCurrentPosition()) == false) {
				positionSet.add(getCurrentPosition());
				positionListModel.add(getCurrentPosition());
			}
			} catch (Exception ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}
		}
		
	}

	public SelectPathSetPanel(I_ConfigAceFrame config) throws Exception {
		super(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;

		pppwc = new SelectPathAndPositionPanelWithCombo(true, "", 
				config, null);
		pppwc.setPositionCheckBoxVisible(false);
		add(pppwc, gbc);
		gbc.weighty = 0;
		gbc.gridy++;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		JButton addPathButton = new JButton("add position");
		addPathButton.addActionListener(new AddPathActionLister());
		add(addPathButton, gbc);
		
		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1;
		positionListModel = new ArrayListModel<I_Position>();
		positionList = new JList(positionListModel);
		JScrollPane positionScroller = new JScrollPane(positionList);
		positionScroller.setBorder(BorderFactory.createTitledBorder("Position set: "));
		add(positionScroller, gbc);
	}
	
	public I_Position getCurrentPosition() throws TerminologyException, IOException {
		return pppwc.getCurrentPosition();
	}

	

}
