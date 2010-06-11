package org.ihtsdo.arena.editor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class ArenaConceptView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private I_ConfigAceFrame config;

	public ArenaConceptView(I_ConfigAceFrame config) {
		super();
		this.config = config;
	}
	
	
	public void layoutConcept(I_GetConceptData concept) {
		this.removeAll();
		this.setLayout(new GridBagLayout());
		if (concept != null) {
			try {
				List<? extends I_RelTuple> rels = concept.getSourceRelTuples(config.getAllowedStatus(), 
						null, config.getViewPositionSetReadOnly(), 
						config.getPrecedence(), config.getConflictResolutionStrategy());
				HashMap<Integer, List<I_RelTuple>> relGroups = new HashMap<Integer, List<I_RelTuple>>();
				for (I_RelTuple r: rels) {
					 List<I_RelTuple> group = relGroups.get(r.getGroup());
					 if (group == null) {
						 group = new ArrayList<I_RelTuple>();
						 relGroups.put(r.getGroup(), group);
					 }
					 group.add(r);
				}
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.weightx = 1;
				gbc.weighty = 0;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.gridheight = 1;
				gbc.gridwidth = 1;
				gbc.gridx = 1;
				gbc.gridy = 0;
				List<I_RelTuple> group = relGroups.get(0);
				if (group != null) {
					for (I_RelTuple r: group) {
						this.add(getRelComponent(r), gbc);
						gbc.gridy++;
					}				
				}
				for (Entry<Integer, List<I_RelTuple>> e: relGroups.entrySet()) {
					if (e.getKey() != 0) {
						this.add(getRelGroupComponent(e.getValue()), gbc);
						gbc.gridy++;
					}
				}
				gbc.weighty = 1;
				this.add(new JPanel(), gbc);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
	}
	
	public static JComponent getRelGroupComponent(List<I_RelTuple> group) throws TerminologyException, IOException {
		JPanel relPanel = new JPanel(new GridBagLayout());
		relPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel relLabel = new JLabel("  ");
		relLabel.setBackground(Color.GREEN);
		relLabel.setOpaque(true);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = group.size();
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		relPanel.add(relLabel, gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.gridheight = 1;
		for (I_RelTuple r: group) {
			relPanel.add(getRelComponent(r), gbc);
			gbc.gridy++;
		}
		return relPanel;
	}
	
	public static JComponent getRelComponent(I_RelTuple r) throws TerminologyException, IOException {
		JPanel relPanel = new JPanel(new GridBagLayout());
		relPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel relLabel = new JLabel("  ");
		relLabel.setBackground(Color.BLUE);
		relLabel.setOpaque(true);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		relPanel.add(relLabel, gbc);
		gbc.gridx++;
		relPanel.add(getLabel(r.getTypeId()), gbc);
		gbc.weightx = 1;		
		gbc.gridx++;
		relPanel.add(getLabel(r.getC2Id()), gbc);
		
		return relPanel;
	}


	private static TermComponentLabel getLabel(int nid)
			throws TerminologyException, IOException {
		TermComponentLabel relType = new TermComponentLabel();
		relType.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
		relType.setTermComponent(Terms.get().getConcept(nid));
		return relType;
	}
}
