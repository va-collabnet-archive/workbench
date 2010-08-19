package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.drools.EditPanelKb;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelSpec;
import org.ihtsdo.tk.spec.SpecBI;

public class ConceptView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private I_ConfigAceFrame config;

	private ConceptViewSettings settings;
	
	private EditPanelKb kb;

	public ConceptView(I_ConfigAceFrame config, ConceptViewSettings settings) {
		super();
		this.config = config;
		this.settings = settings;
		kb = new EditPanelKb(config);
	}
	
	
	public void layoutConcept(I_GetConceptData concept) {
		Map<SpecBI, Integer> templates = kb.setConcept(concept);
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
				gbc.anchor = GridBagConstraints.NORTHWEST;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.gridheight = 1;
				gbc.gridwidth = 1;
				gbc.gridx = 1;
				gbc.gridy = 0;
				
				
				for (I_DescriptionTuple desc: concept.getDescriptionTuples(config.getAllowedStatus(), 
						null, config.getViewPositionSetReadOnly(), 
						config.getPrecedence(), config.getConflictResolutionStrategy())) {
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
				
				for (Entry<SpecBI, Integer> entry: templates.entrySet()) {
					Class<?> entryClass = entry.getKey().getClass();
					if (RelSpec.class.isAssignableFrom(entryClass)) {
						RelSpec spec = (RelSpec) entry.getKey();
						this.add(getRelTemplate(spec), gbc);
						gbc.gridy++;
					} else if (DescriptionSpec.class.isAssignableFrom(entryClass)) {
						DescriptionSpec spec = (DescriptionSpec) entry.getKey();
						this.add(getDescTemplate(spec), gbc);
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
	
	public JComponent getRelGroupComponent(List<I_RelTuple> group) throws TerminologyException, IOException {
		DragPanelRelGroup relPanel = new DragPanelRelGroup(new GridBagLayout());
		relPanel.setupDrag(new RelGroupForDragPanel(group));
		relPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel relLabel = getJLabel(" ");
		relLabel.setBackground(Color.GREEN);
		relLabel.setOpaque(true);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = group.size();
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		relPanel.add(relLabel, gbc);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.gridheight = 1;
		for (I_RelTuple r: group) {
			relPanel.add(getRelComponent(r), gbc);
			gbc.gridy++;
		}
		return relPanel;
	}
	
	
	public JComponent getDescComponent(I_DescriptionTuple desc) throws TerminologyException, IOException {
		DragPanelDescription descPanel = new DragPanelDescription(new GridBagLayout());
		descPanel.setupDrag(desc);
		descPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel descLabel = getJLabel(" ");
		descLabel.setBackground(Color.ORANGE);
		descLabel.setOpaque(true);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		descPanel.add(descLabel, gbc);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx++;
		descPanel.add(getLabel(desc.getTypeNid()), gbc);
		gbc.gridx++;
		descPanel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
		gbc.weightx = 1;		
		gbc.gridx++;
		JLabel textLabel = new JLabel() {
		    /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			
			int fixedWidth = 150;
			
		    Dimension wrapSize = new Dimension();
		    
		    @Override
			public void setSize(Dimension d) {
				setSize(d.width, d.height);
			}

			@Override
			public void setSize(int width, int height) {
				super.setSize(fixedWidth, height);
			}
			
			@Override
			public void setBounds(int x, int y, int width, int height) {
				super.setBounds(x, y, wrapSize.width, wrapSize.height);
			}

			@Override
			public void setBounds(Rectangle r) {
				setBounds(r.x, r.y, r.width, r.height);
			}

			@Override
			public Dimension getMaximumSize() {
				return wrapSize;
			}

			@Override
			public Dimension getMinimumSize() {
				return wrapSize;
			}

			@Override
			public Dimension getPreferredSize() {
				return wrapSize;
			}

			@Override
			public void setText(String text) {
				if (!BasicHTML.isHTMLString(text)) {
					text = "<html>" + text;
				}
            	super.setText(text);
                View v = BasicHTML.createHTMLView(this, getText());
                v.setSize(fixedWidth, 0);
                float prefYSpan = v.getPreferredSpan(View.Y_AXIS);
                if (prefYSpan > 16) {
                	wrapSize = new Dimension(fixedWidth, (int) (prefYSpan + 4));
                	setSize(wrapSize);
                } else {
                	wrapSize = new Dimension(fixedWidth, (int) prefYSpan);
                	setSize(wrapSize);
                }
			}
		};
		
		textLabel.setFont(textLabel.getFont().deriveFont(settings.getFontSize()));
		textLabel.setText(desc.getText());
		descPanel.add(textLabel, gbc);
		return descPanel;
	}

	
	public JComponent getDescTemplate(DescriptionSpec desc) throws TerminologyException, IOException {
		DragPanelDescription descPanel = new DragPanelDescription(new GridBagLayout());
		//descPanel.setupDrag(desc);
		descPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel descLabel = getJLabel("T");
		descLabel.setBackground(Color.ORANGE);
		descLabel.setOpaque(true);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		descPanel.add(descLabel, gbc);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx++;
		descPanel.add(getLabel(desc.getDescTypeSpec().getNid()), gbc);
		gbc.gridx++;
		descPanel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
		gbc.weightx = 1;		
		gbc.gridx++;
		JLabel textLabel = new JLabel() {
		    /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			
			int fixedWidth = 150;
			
		    Dimension wrapSize = new Dimension();
		    
		    @Override
			public void setSize(Dimension d) {
				setSize(d.width, d.height);
			}

			@Override
			public void setSize(int width, int height) {
				super.setSize(fixedWidth, height);
			}
			
			@Override
			public void setBounds(int x, int y, int width, int height) {
				super.setBounds(x, y, wrapSize.width, wrapSize.height);
			}

			@Override
			public void setBounds(Rectangle r) {
				setBounds(r.x, r.y, r.width, r.height);
			}

			@Override
			public Dimension getMaximumSize() {
				return wrapSize;
			}

			@Override
			public Dimension getMinimumSize() {
				return wrapSize;
			}

			@Override
			public Dimension getPreferredSize() {
				return wrapSize;
			}

			@Override
			public void setText(String text) {
				if (!BasicHTML.isHTMLString(text)) {
					text = "<html>" + text;
				}
            	super.setText(text);
                View v = BasicHTML.createHTMLView(this, getText());
                v.setSize(fixedWidth, 0);
                float prefYSpan = v.getPreferredSpan(View.Y_AXIS);
                if (prefYSpan > 16) {
                	wrapSize = new Dimension(fixedWidth, (int) (prefYSpan + 4));
                	setSize(wrapSize);
                } else {
                	wrapSize = new Dimension(fixedWidth, (int) prefYSpan);
                	setSize(wrapSize);
                }
			}
		};
		
		textLabel.setFont(textLabel.getFont().deriveFont(settings.getFontSize()));
		textLabel.setText(desc.getDescText());
		descPanel.add(textLabel, gbc);
		return descPanel;
	}

	public JComponent getRelTemplate(RelSpec spec) throws TerminologyException, IOException {
		Coordinate coordinate = new Coordinate(config.getPrecedence(),
				config.getViewPositionSetReadOnly(), config
						.getAllowedStatus(), config.getDestRelTypes(),
				config.getConflictResolutionStrategy());
		DragPanelRel relPanel = new DragPanelRel(new GridBagLayout());
		relPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel relLabel = getJLabel("T");
		relLabel.setBackground(Color.YELLOW);
		relLabel.setOpaque(true);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		relPanel.add(relLabel, gbc);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx++;
		relPanel.add(getLabel(spec.getRelTypeSpec().get(coordinate).getNid()), gbc);
		gbc.gridx++;
		relPanel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
		gbc.weightx = 1;		
		gbc.gridx++;
		relPanel.add(getLabel(spec.getDestinationSpec().get(coordinate).getNid()), gbc);
		
		return relPanel;
	}

	public JComponent getRelComponent(I_RelTuple r) throws TerminologyException, IOException {
		DragPanelRel relPanel = new DragPanelRel(new GridBagLayout());
		relPanel.setupDrag(r);
		relPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel relLabel = getJLabel(" ");
		relLabel.setBackground(Color.BLUE);
		relLabel.setOpaque(true);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		relPanel.add(relLabel, gbc);
		gbc.gridx++;
		relPanel.add(getLabel(r.getTypeNid()), gbc);
		gbc.gridx++;
		relPanel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
		gbc.weightx = 1;		
		gbc.gridx++;
		relPanel.add(getLabel(r.getC2Id()), gbc);
		return relPanel;
	}

	private JLabel getJLabel(String text) {
		JLabel l = new JLabel(text);
		l.setFont(l.getFont().deriveFont(settings.getFontSize()));
		l.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
		return l;
	}

	private TermComponentLabel getLabel(int nid)
			throws TerminologyException, IOException {
		TermComponentLabel termLabel = new TermComponentLabel();
		termLabel.setLineWrapEnabled(true);
		termLabel.setFixedWidth(100);
		termLabel.setFont(termLabel.getFont().deriveFont(settings.getFontSize()));
		termLabel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
		termLabel.setTermComponent(Terms.get().getConcept(nid));
		return termLabel;
	}
}
