package org.dwfa.ace.path;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.gui.glue.PropertySetListenerGlue;
import org.dwfa.tapi.TerminologyException;

public class SelectPathAndPositionPanelWithCombo extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel positionPanelContainer;
	private JComboBox pathCombo;
	boolean selectPositionOnly;
	private String purpose;
	private I_ConfigAceFrame aceConfig;
	private PropertySetListenerGlue selectGlue;
	private PositionPanel currentPositionPanel;
	private I_Path currentPath;

	public SelectPathAndPositionPanelWithCombo(boolean selectPositionOnly, String purpose, 
			I_ConfigAceFrame aceConfig, PropertySetListenerGlue selectGlue) throws Exception {
		super(new GridBagLayout());
		this.selectPositionOnly = selectPositionOnly;
		this.purpose = purpose;
		this.aceConfig = aceConfig;
		this.selectGlue = selectGlue;
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		add(new JLabel(" path:"), gbc);
		
		gbc.gridx++;
		gbc.weightx = 1;
		pathCombo = new JComboBox(LocalVersionedTerminology.get().getPaths().toArray());
		if (aceConfig.getEditingPathSet().size() > 0) {
			pathCombo.setSelectedItem(aceConfig.getEditingPathSet().iterator().next());
		}
		pathCombo.addActionListener(this);
		add(pathCombo, gbc);
		
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weighty = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		positionPanelContainer = new JPanel(new GridLayout(1,1));
		add(positionPanelContainer, gbc);
		actionPerformed(null);
	}

	public void actionPerformed(ActionEvent evt) {
		try {
			I_Path path = (I_Path) pathCombo.getSelectedItem();
			if (path == null) {
				return;
			}
			List<TimePathId> timePathEntries = LocalVersionedTerminology.get()
					.getTimePathList();
			int modTimeCount = 0;
			for (TimePathId tp : timePathEntries) {
				if (tp != null && path != null) {
					if (tp.getPathId() == path.getConceptId()) {
						modTimeCount++;
					}
				}
			}
			if (currentPositionPanel != null) {
				positionPanelContainer.remove(currentPositionPanel);
				if (currentPositionPanel.isPositionSelected()) {
					positionMap.put(currentPath, 
							currentPositionPanel.getPosition());
				} else {
					positionMap.remove(currentPath);
				}
				positionPanelContainer.setLayout(new GridLayout(1,1));
				
			}
			PositionPanel pp;
			if (positionMap.containsKey(path)) {
				pp = new PositionPanel(path, selectPositionOnly,
						purpose, path.toString(), aceConfig, selectGlue, positionMap.get(path));
			} else {
				pp = new PositionPanel(path, selectPositionOnly,
						purpose, path.toString(), aceConfig, selectGlue);
			}
			
			positionPanelContainer.add(pp);
			currentPositionPanel = pp;
			pp.setPositionCheckBoxVisible(positionCheckBoxVisible);
			currentPath = path;
		} catch (Exception ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		}
	}
	
	private Map<I_Path, I_Position> positionMap = new HashMap<I_Path, I_Position>();
	private boolean positionCheckBoxVisible = true;
	
	public Collection<I_Position> getSelectedPositions() throws TerminologyException, IOException {
		if (currentPositionPanel.isPositionSelected()) {
			positionMap.put(currentPath, 
					currentPositionPanel.getPosition());
		} else {
			positionMap.remove(currentPath);
		}
		return positionMap.values();
	}

	public void setPositionCheckBoxVisible(boolean b) {
		currentPositionPanel.setPositionCheckBoxVisible(b);
		positionCheckBoxVisible  = b;
	}

	public I_Position getCurrentPosition() throws TerminologyException, IOException {
		return currentPositionPanel.getPosition();
	}

}
