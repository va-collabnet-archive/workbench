package org.ihtsdo.arena.conceptview;

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
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class ConceptView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private I_ConfigAceFrame config;

	private ConceptViewSettings settings;

	public ConceptView(I_ConfigAceFrame config, ConceptViewSettings settings) {
		super();
		this.config = config;
		this.settings = settings;
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
				
				
				for (I_DescriptionVersioned desc: concept.getDescriptions()) {
					this.add(getDescComponent(desc), gbc);
					gbc.gridy++;
				}
				

				
				
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
				this.add(getRelTemplate(), gbc);
				gbc.gridy++;
				
				gbc.weighty = 1;
				this.add(new JPanel(), gbc);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
	}
	
	public JComponent getRelGroupComponent(List<I_RelTuple> group) throws TerminologyException, IOException {
		JPanel relPanel = new JPanel(new GridBagLayout());
		relPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel relLabel = new JLabel("   ");
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
	
	
	public JComponent getDescComponent(I_DescriptionVersioned desc) throws TerminologyException, IOException {
		JPanel descPanel = new JPanel(new GridBagLayout());
		descPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel descLabel = new JLabel("   ");
		descLabel.setBackground(Color.ORANGE);
		descLabel.setOpaque(true);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		descPanel.add(descLabel, gbc);
		gbc.gridx++;
		descPanel.add(getLabel(desc.getFirstTuple().getTypeId()), gbc);
		gbc.gridx++;
		descPanel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
		gbc.weightx = 1;		
		gbc.gridx++;
		JLabel textLabel = new JLabel(desc.getFirstTuple().getText());
		textLabel.setFont(textLabel.getFont().deriveFont(settings.getFontSize()));
		descPanel.add(textLabel, gbc);
		return descPanel;
	}

	public JComponent getRelTemplate() throws TerminologyException, IOException {
		JPanel relPanel = new JPanel(new GridBagLayout());
		relPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel relLabel = new JLabel("   ");
		relLabel.setBackground(Color.YELLOW);
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
		relPanel.add(getLabel(ArchitectonicAuxiliary.Concept.RELATIONSHIP.localize().getNid()), gbc);
		gbc.gridx++;
		relPanel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
		gbc.weightx = 1;		
		gbc.gridx++;
		relPanel.add(getLabel(ArchitectonicAuxiliary.Concept.ACCEPTABLE.localize().getNid()), gbc);
		
		return relPanel;
	}

	public JComponent getRelComponent(I_RelTuple r) throws TerminologyException, IOException {
		JPanel relPanel = new JPanel(new GridBagLayout());
		relPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel relLabel = new JLabel("   ");
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
		gbc.gridx++;
		relPanel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
		gbc.weightx = 1;		
		gbc.gridx++;
		relPanel.add(getLabel(r.getC2Id()), gbc);
		
		return relPanel;
	}


	private TermComponentLabel getLabel(int nid)
			throws TerminologyException, IOException {
		TermComponentLabel termLabel = new TermComponentLabel();
		termLabel.setFont(termLabel.getFont().deriveFont(settings.getFontSize()));
		termLabel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
		termLabel.setTermComponent(Terms.get().getConcept(nid));
		return termLabel;
	}
}
