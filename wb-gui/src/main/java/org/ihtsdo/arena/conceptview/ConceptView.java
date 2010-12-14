package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.drools.KnowledgeBase;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.ScrollablePanel;
import org.ihtsdo.arena.context.action.DropActionPanel;
import org.ihtsdo.arena.drools.EditPanelKb;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.TypedComponentAnalogBI;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.tk.drools.facts.Context;
import org.ihtsdo.tk.drools.facts.FactFactory;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelSpec;
import org.ihtsdo.tk.spec.SpecBI;
import org.ihtsdo.tk.spec.SpecFactory;
import org.ihtsdo.util.swing.GuiUtil;


public class ConceptView extends JPanel {
	
	public class LayoutConceptWorker extends SwingWorker<Map<SpecBI, Integer>, Boolean> {
		
		public LayoutConceptWorker() {
			super();
		}

		@Override
		protected Map<SpecBI, Integer> doInBackground() throws Exception {
			return kb.setConcept(concept);
		}

		@Override
		protected void done() {
	           try { 
	        	   Map<SpecBI, Integer> templates = get();
	   			removeAll();
				setLayout(new GridBagLayout());
				if (concept != null) {
					try {
						List<? extends I_RelTuple> rels = concept.getSourceRelTuples(config.getAllowedStatus(), 
								null, config.getViewPositionSetReadOnly(), 
								config.getPrecedence(), config.getConflictResolutionStrategy());
						GridBagConstraints gbc = new GridBagConstraints();
						gbc.weightx = 1;
						gbc.weighty = 0;
						gbc.anchor = GridBagConstraints.NORTHWEST;
						gbc.fill = GridBagConstraints.BOTH;
						gbc.gridheight = 1;
						gbc.gridwidth = 1;
						gbc.gridx = 1;
						gbc.gridy = 0;
						
						
						CollapsePanel  cpd = new CollapsePanel("descriptions", settings);
						add(cpd, gbc);
						gbc.gridy++;
						for (I_DescriptionTuple desc: concept.getDescriptionTuples(config.getAllowedStatus(), 
								null, config.getViewPositionSetReadOnly(), 
								config.getPrecedence(), config.getConflictResolutionStrategy())) {
							JComponent dc = getDescComponent(desc);
							cpd.addToggleComponent(dc);
							add(dc, gbc);
							gbc.gridy++;
						}
						
						Coordinate coordinate = config.getCoordinate();
						
						CollapsePanel  cpr = new CollapsePanel("relationships", settings);
						boolean cprAdded = false;
						for (I_RelTuple r: rels) {
							if (r.getGroup() == 0) {
								if (!cprAdded) {
									add(cpr, gbc);
									gbc.gridy++;
									cprAdded = true;
								}
								JComponent rc = getRelComponent(r);
								cpr.addToggleComponent(rc);
								add(rc, gbc);
								gbc.gridy++;
							}
						}	

						try {
							Collection<? extends RelGroupVersionBI> group = Ts.get().getConceptVersion(coordinate, concept.getNid()).getRelGroups();
							for (RelGroupVersionBI r: group) { 
								Collection<? extends RelationshipVersionBI> currentRels = r.getCurrentRels();
								if(!currentRels.isEmpty()) { 
									CollapsePanel  cprg = new CollapsePanel("rel groups", settings);
									boolean cprgAdded = false;
										if (!cprgAdded) {
											add(cprg, gbc);
											gbc.gridy++;
											cprgAdded = true;
										}
										
										JComponent rgc = getRelGroupComponent(r);
										cprg.addToggleComponent(rgc);
										add(rgc, gbc);
										gbc.gridy++;
								}
							}
						} catch (ContraditionException e) {
							AceLog.getAppLog().alertAndLogException(e);
						}
						
						if (templates.size() > 0) {
							CollapsePanel  cptemplate = new CollapsePanel("templates", settings);
							add(cptemplate, gbc);
							gbc.gridy++;
							for (Entry<SpecBI, Integer> entry: templates.entrySet()) {
								Class<?> entryClass = entry.getKey().getClass();
								if (RelSpec.class.isAssignableFrom(entryClass)) {
									RelSpec spec = (RelSpec) entry.getKey();
									JComponent template = getRelTemplate(spec);
									cptemplate.addToggleComponent(template);
									add(template, gbc);
									gbc.gridy++;
								} else if (DescriptionSpec.class.isAssignableFrom(entryClass)) {
									DescriptionSpec spec = (DescriptionSpec) entry.getKey();
									JComponent template = getDescTemplate(spec);
									cptemplate.addToggleComponent(template);
									add(template, gbc);
									gbc.gridy++;
								}
							}
						}
						
						gbc.weighty = 1;
						add(new JPanel(), gbc);
					} catch (IOException e) {
						AceLog.getAppLog().alertAndLogException(e);
					} catch (TerminologyException e) {
						AceLog.getAppLog().alertAndLogException(e);
					}
				}	           } catch (Exception e) {
	        	   AceLog.getAppLog().alertAndLogException(e);
	           }
				GuiUtil.tickle(ConceptView.this);
		}
		
	}
	
	private Object lastThingBeingDropped;

	public void setupDrop(Object thingBeingDropped) {

			if (thingBeingDropped != null) {
				if (lastThingBeingDropped == null || 
						thingBeingDropped.equals(lastThingBeingDropped) == false) {
					lastThingBeingDropped = thingBeingDropped;
					actionList.clear();
					getKbActions(thingBeingDropped);
					dropComponents.clear();
					if (actionList.size() > -1) {					
						for (Action a: actionList) {
							try {
								dropComponents.add(new DropActionPanel(a));
							} catch (TooManyListenersException e) {
								AceLog.getAppLog().alertAndLogException(e);
							}
						}
					}
				}
			} else {
				System.out.println("Changing to null");
				actionList.clear();
				dropComponents.clear();
			}
			
		}


	public class DropPanelActionManager implements ActionListener, I_DispatchDragStatus  {
		private Timer timer;
		private boolean dragging = false;
		private JComponent dropPanel = new JLabel("dropPanel");
		private boolean panelAdded = false;
		private JScrollPane sfpScroller;
		private boolean gridLayout = true;
		private JPanel sfp;
		private Collection<JComponent> addedDropComponents = new ArrayList<JComponent>();
		
		public DropPanelActionManager() {
			super();
			new DropPanelProxy(this);
			timer = new Timer(50, this);
		}
		/* (non-Javadoc)
		 * @see org.ihtsdo.arena.conceptview.I_DispatchDragStatus#dragStarted()
		 */
		public void dragStarted() {
			
			LayoutManager layout = new FlowLayout(FlowLayout.LEADING, 5, 5);
			sfp = new ScrollablePanel(layout);
			if (gridLayout) {
				layout = new GridLayout(0, 1, 5, 5);
				sfp = new JPanel(layout);
			}
			
			sfp.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			dropPanel = new JPanel(new GridLayout(1, 1));
			sfpScroller = new JScrollPane(sfp);
			sfpScroller.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			sfpScroller.setAutoscrolls(true);
			sfpScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			sfpScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			dropPanel.add(sfpScroller);
			dragging = true;
			timer.start();
		}
		/* (non-Javadoc)
		 * @see org.ihtsdo.arena.conceptview.I_DispatchDragStatus#dragFinished()
		 */
		public void dragFinished() {
			timer.stop();
			dragging = false;
        	setDragPanelVisible(false);
        	dropPanel = null;
        	addedDropComponents.clear();
        	actionList.clear();
        	dropComponents.clear();
        	lastThingBeingDropped = null;
		}
		/* (non-Javadoc)
		 * @see org.ihtsdo.arena.conceptview.I_DispatchDragStatus#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (dragging) {
				if (addedDropComponents.equals(dropComponents) == false) {
					sfp.removeAll();
					System.out.println("Concept changing drop components.");
					addedDropComponents = new ArrayList<JComponent>();
					for (JComponent c: dropComponents) {
						addedDropComponents.add(c);
						sfp.add(c);
					}
				}		
				if (addedDropComponents.size() == 0) {
					return;
				}
				Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
				Point mouseLocationForConceptView = mouseLocation.getLocation();
	            SwingUtilities.convertPointFromScreen(mouseLocationForConceptView, 
	            		ConceptView.this.getParent());
	            if (ConceptView.this.getParent().contains(mouseLocationForConceptView)) {
	            	setDragPanelVisible(true);
	            } else {
					Point mouseLocationForDropPanel = mouseLocation.getLocation();
    	            SwingUtilities.convertPointFromScreen(mouseLocationForDropPanel, dropPanel);
    	            if (mouseLocationForDropPanel.y <= 0) {
    	            	mouseLocationForDropPanel.y = mouseLocationForDropPanel.y + 10;
    	            } else if (mouseLocationForDropPanel.y >= dropPanel.getHeight()) {
    	            	mouseLocationForDropPanel.y = mouseLocationForDropPanel.y - 10;
    	            }
	            	if (dropPanel.contains(mouseLocationForDropPanel) && panelAdded) {
	            		if (mouseLocationForDropPanel.y < 10) {
	            			BoundedRangeModel scrollerModel = sfpScroller.getVerticalScrollBar().getModel();
	            			scrollerModel.setExtent(1);
	            			if (scrollerModel.getValue() < scrollerModel.getMaximum()) {
	            				scrollerModel.setValue(scrollerModel.getValue() - 5);
	            			}
	            		} else if (dropPanel.getHeight() - mouseLocationForDropPanel.y < 10) {
	            			BoundedRangeModel scrollerModel = sfpScroller.getVerticalScrollBar().getModel();
	            			scrollerModel.setExtent(1);
	            			if (scrollerModel.getValue() < scrollerModel.getMaximum()) {
	            				scrollerModel.setValue(scrollerModel.getValue() + 5);
	            			}
	            		}
	            	} else {
		            	setDragPanelVisible(false);
	            	}
	            	
	            }
			} else {
            	setDragPanelVisible(false);
			}
		}
		
		private void setDragPanelVisible(boolean visible) {
			if (visible) {
				if (ConceptView.this.isVisible()) {
					if (!panelAdded) {
						panelAdded = true;
						Point loc = ConceptView.this.getParent().getLocation();
						JLayeredPane rootLayers = ConceptView.this.getRootPane().getLayeredPane();
						rootLayers.add(dropPanel, JLayeredPane.PALETTE_LAYER);
						loc = SwingUtilities.convertPoint(ConceptView.this.getParent(), loc, rootLayers);
						dropPanel.setSize(sfp.getPreferredSize().width + 4, ConceptView.this.getParent().getHeight());
						dropPanel.setLocation(loc.x - dropPanel.getWidth(), loc.y);
						dropPanel.setVisible(true);
						dropPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
					}
				}
			} else {
				if (panelAdded) {
					panelAdded = false;
					JLayeredPane rootLayers = ConceptView.this.getRootPane().getLayeredPane();
					dropPanel.setVisible(false);
					rootLayers.remove(dropPanel);
				}
			}
		}

	}

	private class UpdateTextDocumentListener implements DocumentListener, ActionListener {
		FixedWidthJEditorPane editorPane;
		DescriptionAnalogBI desc;
		Timer t;
		I_GetConceptData c;
		boolean update = false;


        public UpdateTextDocumentListener(FixedWidthJEditorPane editorPane, DescriptionAnalogBI desc) throws TerminologyException, IOException {
			super();
			this.editorPane = editorPane;
			this.desc = desc;
			t = new Timer(1000, this);
			t.start();
			c = Terms.get().getConcept(desc.getConceptNid());
		}

		public void insertUpdate(DocumentEvent e) {
			update = true;
        }

		public void removeUpdate(DocumentEvent e) {
			update = true;
        }

        public void changedUpdate(DocumentEvent e) {
			update = true;
        }

		@Override
		public void actionPerformed(ActionEvent e) {

			try {
				if (update) {
					update = false;
					desc.setText(editorPane.extractText());
				}
			} catch (PropertyVetoException ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}
		}
        
        
    }

	private class UpdateTextTemplateDocumentListener implements DocumentListener, ActionListener {
		FixedWidthJEditorPane editorPane;
		DescriptionSpec desc;
		Timer t;
		I_GetConceptData c;
		boolean update = false;


        public UpdateTextTemplateDocumentListener(FixedWidthJEditorPane editorPane, DescriptionSpec desc) throws TerminologyException, IOException {
			super();
			this.editorPane = editorPane;
			this.desc = desc;
			t = new Timer(1000, this);
			t.start();
			c = Terms.get().getConcept(desc.getConceptSpec().getNid());
		}

		public void insertUpdate(DocumentEvent e) {
			update = true;
        }

		public void removeUpdate(DocumentEvent e) {
			update = true;
        }

        public void changedUpdate(DocumentEvent e) {
			update = true;
        }

		@Override
		public void actionPerformed(ActionEvent e) {

			if (update) {
				update = false;
				desc.setDescText(editorPane.extractText());
			}
		}
        
        
    }


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private I_ConfigAceFrame config;

	private ConceptViewSettings settings;
		
	private EditPanelKb kb;
	
	private I_DispatchDragStatus dropPanelMgr = new DropPanelActionManager();
	
	private Collection<Action> actionList = Collections.synchronizedCollection(new ArrayList<Action>());

	private KnowledgeBase kbase;

	private I_GetConceptData concept;

	public I_GetConceptData getConcept() {
		return concept;
	}


	public void setConcept(I_GetConceptData concept) {
		this.concept = concept;
	}

	private Collection<JComponent> dropComponents = Collections.synchronizedList(new ArrayList<JComponent>());

	public ConceptView() throws TerminologyException, IOException {
		this.config = Terms.get().getActiveAceFrameConfig();
		kb = new EditPanelKb(config);
		addCommitListener(settings);
	}

	
	public ConceptView(I_ConfigAceFrame config, ConceptViewSettings settings) {
		super();
		this.config = config;
		this.settings = settings;
		kb = new EditPanelKb(config);
		addCommitListener(settings);
	}


	private void addCommitListener(ConceptViewSettings settings) {
		settings.getConfig().addPropertyChangeListener("commit", new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				layoutConcept(ConceptView.this.concept);
			}
		});
	}
	
	
	public void layoutConcept(I_GetConceptData concept) {
		removeAll();
		this.concept = concept;
		(new LayoutConceptWorker()).execute();
	}
	
	public JComponent getRelGroupComponent(RelGroupVersionBI group) throws TerminologyException, IOException, ContraditionException {
		DragPanelRelGroup relGroupPanel = new DragPanelRelGroup(new GridBagLayout(), settings);
		relGroupPanel.setupDrag(group);
		relGroupPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel relGroupLabel = getJLabel(" ");
		relGroupLabel.setBackground(Color.GREEN);
		relGroupLabel.setOpaque(true);
		relGroupPanel.setDropPopupInset(relGroupLabel.getPreferredSize().width);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = group.getRels().size() + 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		relGroupPanel.add(relGroupLabel, gbc);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.gridheight = 1;
		CollapsePanel cprg = new CollapsePanel("Group: ", settings);
		relGroupPanel.add(cprg, gbc);
		gbc.gridy++;
		for (RelationshipVersionBI r: group.getCurrentRels()) {
			DragPanelRel dpr = getRelComponent(r);
			cprg.addToggleComponent(dpr);
			dpr.setInGroup(true);
			relGroupPanel.add(dpr, gbc);
			gbc.gridy++;
		}
	
		return relGroupPanel;

	}
	
	private abstract class PropertyChangeManager<T extends TypedComponentAnalogBI> implements PropertyChangeListener {
		private T component;
		
		public PropertyChangeManager(T component) {
			super();
			this.setComponent(component);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			I_GetConceptData newValue = (I_GetConceptData) evt.getNewValue();
			changeProperty(newValue);
		}
		
		protected abstract void changeProperty(I_GetConceptData newValue);

		protected void setComponent(T component) {
			this.component = component;
		}

		protected T getComponent() {
			return component;
		}
		
	}
	
	private abstract class SpecPropertyChangeManager<T extends TypedComponentAnalogBI> implements PropertyChangeListener {
		private T component;
		
		public SpecPropertyChangeManager(T component) {
			super();
			this.setComponent(component);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			I_GetConceptData newValue = (I_GetConceptData) evt.getNewValue();
			changeProperty(newValue);
		}
		
		protected abstract void changeProperty(I_GetConceptData newValue);

		protected void setComponent(T component) {
			this.component = component;
		}

		protected T getComponent() {
			return component;
		}
		
	}

	public JComponent getDescComponent(DescriptionAnalogBI desc) throws TerminologyException, IOException {
		DragPanelDescription descPanel = new DragPanelDescription(new GridBagLayout(), settings);
		boolean canDrop = false;
		if (desc.getTime() == Long.MAX_VALUE) {
			descPanel.setOpaque(true);
			descPanel.setBackground(Color.YELLOW);
			canDrop = true;
		}
		descPanel.setupDrag(desc);
		descPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel descLabel = getJLabel(" ");
		descLabel.setBackground(Color.ORANGE);
		descLabel.setOpaque(true);
		descPanel.setDropPopupInset(descLabel.getPreferredSize().width);
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
		TermComponentLabel typeLabel = getLabel(desc.getTypeNid(), canDrop);
		descPanel.add(typeLabel, gbc);
		if (desc.isUncommitted()) {
			typeLabel.addPropertyChangeListener("termComponent", new PropertyChangeManager<DescriptionAnalogBI>(desc) {
				@Override
				protected void changeProperty(I_GetConceptData newValue) {
					try {
						getComponent().setTypeNid(newValue.getNid());
						if (getComponent().isUncommitted()) {
		                    Terms.get().addUncommitted(Terms.get().getConcept(getComponent().getConceptNid()));
						}
					} catch (PropertyVetoException e) {
						AceLog.getAppLog().alertAndLogException(e);
					} catch (TerminologyException e) {
						AceLog.getAppLog().alertAndLogException(e);
					} catch (IOException e) {
						AceLog.getAppLog().alertAndLogException(e);
					}
				}
			});
		}
		gbc.gridx++;
		descPanel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
		gbc.weightx = 1;		
		gbc.gridx++;
		FixedWidthJEditorPane textPane = new FixedWidthJEditorPane(); 
		textPane.setEditable(canDrop);
		textPane.setOpaque(false);
		textPane.setFont(textPane.getFont().deriveFont(settings.getFontSize()));
		textPane.setText(desc.getText());
		descPanel.add(textPane, gbc);
		if (desc.isUncommitted()) {
			textPane.getDocument().addDocumentListener(new UpdateTextDocumentListener(textPane, desc));
		}
		return descPanel;
	}

	
	public JComponent getDescTemplate(final DescriptionSpec desc) throws TerminologyException, IOException {
		DragPanelDescTemplate descPanel = new DragPanelDescTemplate(new GridBagLayout(), settings);
		descPanel.setupDrag(desc);
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
		TermComponentLabel typeLabel = getLabel(desc.getDescTypeSpec().getNid(), true);
		descPanel.add(typeLabel, gbc);
		typeLabel.addPropertyChangeListener("termComponent", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				desc.setDescTypeSpec(SpecFactory.get((I_GetConceptData) evt.getNewValue()));
			}
		});
		
		gbc.gridx++;
		descPanel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
		gbc.weightx = 1;		
		gbc.gridx++;
		FixedWidthJEditorPane textPane = new FixedWidthJEditorPane(); 
		textPane.setEditable(true);
		textPane.setOpaque(false);
		
		textPane.setFont(textPane.getFont().deriveFont(settings.getFontSize()));
		textPane.setText(desc.getDescText());
		descPanel.add(textPane, gbc);
			textPane.getDocument().addDocumentListener(new UpdateTextTemplateDocumentListener(textPane, desc));
		return descPanel;
	}

	public JComponent getRelTemplate(final RelSpec spec) throws TerminologyException, IOException {
		Coordinate coordinate = config.getCoordinate();
		DragPanelRelTemplate relPanel = new DragPanelRelTemplate(new GridBagLayout(), settings);
		relPanel.setupDrag(spec);
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
		TermComponentLabel typeLabel = getLabel(spec.getRelTypeSpec().get(coordinate).getNid(), true);
		relPanel.add(typeLabel, gbc);
		typeLabel.addPropertyChangeListener("termComponent", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				spec.setRelTypeSpec(SpecFactory.get((I_GetConceptData) evt.getNewValue()));
			}
		});
		gbc.gridx++;
		relPanel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
		gbc.weightx = 1;		
		gbc.gridx++;
		TermComponentLabel destLabel = getLabel(spec.getDestinationSpec().get(coordinate).getNid(), true);
		relPanel.add(destLabel, gbc);
		destLabel.addPropertyChangeListener("termComponent", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				spec.setDestinationSpec(SpecFactory.get((I_GetConceptData) evt.getNewValue()));
			}
		});
		return relPanel;
	}

	public DragPanelRel getRelComponent(RelationshipVersionBI r) throws TerminologyException, IOException {
		DragPanelRel relPanel = new DragPanelRel(new GridBagLayout(), settings);
		boolean canDrop = false;
		if (r.getTime() == Long.MAX_VALUE) {
			relPanel.setOpaque(true);
			relPanel.setBackground(Color.YELLOW);
			canDrop = true;
		}
		relPanel.setupDrag(r);
		relPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JLabel relLabel = getJLabel(" ");
		relLabel.setBackground(Color.BLUE);
		relLabel.setOpaque(true);
		relPanel.setDropPopupInset(relLabel.getPreferredSize().width);
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
		TermComponentLabel typeLabel = getLabel(r.getTypeNid(), canDrop);
		typeLabel.addPropertyChangeListener("termComponent", new PropertyChangeManager<RelationshipAnalogBI>((RelationshipAnalogBI) r) {
			@Override
			protected void changeProperty(I_GetConceptData newValue) {
				try {
					getComponent().setTypeNid(newValue.getNid());
					if (getComponent().isUncommitted()) {
	                    Terms.get().addUncommitted(Terms.get().getConcept(getComponent().getOriginNid()));
					}
				} catch (PropertyVetoException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (TerminologyException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
			
		});
		relPanel.add(typeLabel, gbc);
		gbc.gridx++;
		relPanel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
		gbc.weightx = 1;		
		gbc.gridx++;
		TermComponentLabel destLabel = getLabel(r.getDestinationNid(), canDrop);
		typeLabel.addPropertyChangeListener("termComponent", new PropertyChangeManager<RelationshipAnalogBI>((RelationshipAnalogBI) r) {
			@Override
			protected void changeProperty(I_GetConceptData newValue) {
				try {
					getComponent().setDestinationNid(newValue.getNid());
					if (getComponent().isUncommitted()) {
	                    Terms.get().addUncommitted(Terms.get().getConcept(getComponent().getOriginNid()));
					}
				} catch (PropertyVetoException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (TerminologyException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
		});
		relPanel.add(destLabel, gbc);
		return relPanel;
	}

	private JLabel getJLabel(String text) {
		JLabel l = new JLabel(text);
		l.setFont(l.getFont().deriveFont(settings.getFontSize()));
		l.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
		return l;
	}

	private TermComponentLabel getLabel(int nid, boolean canDrop)
			throws TerminologyException, IOException {
		TermComponentLabel termLabel = new TermComponentLabel();
		termLabel.setLineWrapEnabled(true);
		termLabel.getDropTarget().setActive(canDrop);
		termLabel.setFixedWidth(100);
		termLabel.setFont(termLabel.getFont().deriveFont(settings.getFontSize()));
		termLabel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
		termLabel.setTermComponent(Terms.get().getConcept(nid));
		return termLabel;
	}
	
	private void getKbActions(Object thingToDrop) {
		if (kbase == null) {
			try {
				kbase = EditPanelKb.setupKb(new File("drools-rules/ContextualDropActions.drl"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			actionList.clear();
			if (I_GetConceptData.class.isAssignableFrom(thingToDrop.getClass())) {
				I_GetConceptData conceptToDrop = (I_GetConceptData) thingToDrop;
				thingToDrop = Ts.get().getConceptVersion(config.getCoordinate(), conceptToDrop.getConceptNid());
			}
			
			if (ComponentVersionBI.class.isAssignableFrom(thingToDrop.getClass()) ||
					SpecBI.class.isAssignableFrom(thingToDrop.getClass())) {
				StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
				boolean uselogger = false;
				
				KnowledgeRuntimeLogger logger = null;
				if (uselogger) {
					logger = KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
				}
				try {
					ksession.setGlobal("actions", actionList);
					if (AceLog.getAppLog().isLoggable(Level.FINE)) {
						AceLog.getAppLog().fine("dropTarget: " + concept);
						AceLog.getAppLog().fine("thingToDrop: " + thingToDrop);
					}			
					ksession.insert(FactFactory.get(Context.DROP_OBJECT, thingToDrop));
					ksession.insert(FactFactory.get(Context.DROP_TARGET, 
							Ts.get().getConceptVersion(config.getCoordinate(), concept.getNid())));
					ksession.fireAllRules();
				} finally {
					if (logger != null) {
						logger.close();
					}
				}
			}
		} catch (Throwable e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

}
