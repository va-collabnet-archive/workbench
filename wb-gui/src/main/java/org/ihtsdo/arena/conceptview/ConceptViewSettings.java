package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentLabel.LabelText;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.arena.ArenaComponentSettings;
import org.ihtsdo.arena.PreferencesNode;
import org.ihtsdo.arena.contradiction.ContradictionConfig;
import org.ihtsdo.arena.promotion.PromotionConfig;
import org.ihtsdo.taxonomy.TaxonomyHelper;
import org.ihtsdo.taxonomy.TaxonomyMouseListener;
import org.ihtsdo.taxonomy.TaxonomyTree;
import org.ihtsdo.taxonomy.path.PathExpander;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.RelAssertionType;
import static org.ihtsdo.tk.api.RelAssertionType.SHORT_NORMAL_FORM;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

//~--- JDK imports ------------------------------------------------------------

public class ConceptViewSettings extends ArenaComponentSettings {

	public static final int NAVIGATOR_WIDTH = 400;
	private static final int dataVersion = 5;
	/**
     *
     */
	private static final long serialVersionUID = 1L;
	private static ImageIcon statedView = new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/graph_edge.png"));
	private static ImageIcon inferredView = new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/chrystal_ball.png"));
	private static ImageIcon inferredAndStatedView = new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/inferred-then-stated.png"));
        private static ImageIcon shortNormalView = new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/pencil--minus.png"));
        private static ImageIcon longNormalView = new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/pencil--plus.png"));
	public static HashMap<Integer, Set<WeakReference>> arenaPanelMap = new HashMap<>();
	// ~--- fields
	// --------------------------------------------------------------
	// dataVersion = 1;
	private Integer linkedTab = null;
	// dataVersion = 2;
	private boolean forAdjudication = false;
	// dataVersion = 3
	private DescType descType = DescType.PREFERRED;
	private DescType relType = DescType.PREFERRED;
	private DescType relTarget = DescType.FULLY_SPECIFIED;
	private DescType refexName = DescType.PREFERRED;
	private DescType c3Refex = DescType.FULLY_SPECIFIED;
	private DescType c2Refex = DescType.FULLY_SPECIFIED;
	private DescType c1Refex = DescType.FULLY_SPECIFIED;
	// dataversion = 4
	private RelAssertionType relAssertionType = RelAssertionType.STATED;
        // dataversion = 5
        private boolean forPromotion = false;
	// /
	private transient ConceptChangedListener conceptChangedListener;
	private transient JToggleButton navButton;
	private transient ConceptNavigator navigator;
	private transient TaxonomyTree navigatorTree;
	private transient JButton statedInferredButton;
	// transient
	private transient ConceptView view;
	protected InstanceActivitiesPanel activityInstanceNav;
	private JToggleButton aiButton;

	// ~--- constant enums
	// ------------------------------------------------------
	public enum SIDE {

		RIGHT, LEFT
	}

	// ~--- constructors
	// --------------------------------------------------------
	public ConceptViewSettings() {
		this(false,false,  0);
	}

	public ConceptViewSettings(boolean forAdjudication, boolean forPromotion) {
		this(forAdjudication, forPromotion, 0);
	}

	public ConceptViewSettings(boolean forAdjudication, boolean forPromotion, Integer linkedTab) {
		super();
		this.linkedTab = linkedTab;
		this.forAdjudication = forAdjudication;
                this.forPromotion = forPromotion;
	}

	// ~--- enums
	// ---------------------------------------------------------------
	public enum DescPreference {

		DESC_TYPE("desc type"), REL_TYPE("rel type"), REL_TARGET("rel target"), C1_REFEX("c1 refex"), C2_REFEX("c2 refex"), C3_REFEX("c3 refex"), REFEX_NAME(
				"refex name");
		String displayName;

		// ~--- constructors
		// -----------------------------------------------------
		private DescPreference(String displayName) {
			this.displayName = displayName;
		}

		// ~--- methods
		// ----------------------------------------------------------
		@Override
		public String toString() {
			return displayName;
		}
	}

	public enum DescType {

		PREFERRED("preferred"), FULLY_SPECIFIED("fully specified");
		private String displayName;

		// ~--- constructors
		// -----------------------------------------------------
		private DescType(String displayName) {
			this.displayName = displayName;
		}

		// ~--- methods
		// ----------------------------------------------------------
		@Override
		public String toString() {
			return displayName;
		}

		// ~--- get methods
		// ------------------------------------------------------
		public LabelText getLabelText() {
			switch (this) {
			case FULLY_SPECIFIED:
				return LabelText.FULLYSPECIFIED;

			case PREFERRED:
				return LabelText.PREFERRED;

			default:
				throw new RuntimeException("Can't handle type: " + this);
			}
		}
	}

	// ~--- methods
	// -------------------------------------------------------------
	public boolean hideNavigator() {
		if (preferences != null) {
			JLayeredPane layers = renderer.getRootPane().getLayeredPane();

			preferences.setVisible(false);
			preferences.invalidate();
			layers.remove(preferences);
		}

		if (navButton.isSelected()) {
			navButton.doClick();
			return true;
		}

		return false;
	}
	
	public boolean hideAiNavigator(){
		if(aiButton.isSelected()){
			aiButton.doClick();
			return true;
		}
		return false;
	}

	@Override
	public ConceptView makeComponent(I_ConfigAceFrame config) {
		this.config = config;
		if (view == null) {
			this.conceptChangedListener = new ConceptChangedListener();
			view = new ConceptView(config, this, (ConceptViewRenderer) this.renderer);
			addHostListener(conceptChangedListener);

			if (config instanceof ContradictionConfig) {
				view.getSettings().setForAdjudication(true);
			}
                        
                        if (config instanceof PromotionConfig) {
				view.getSettings().setForPromotion(true);
			}

			try {
				view.layoutConcept((I_GetConceptData) getHost().getTermComponent());
			} catch (IOException ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}
		}

		Set<WeakReference> panelSet = arenaPanelMap.get(linkedTab);

		if (panelSet != null) {
			panelSet.add(new WeakReference(view));
		} else {
			panelSet = new HashSet<>();
			panelSet.add(new WeakReference(view));
		}
		activityInstanceNav = new InstanceActivitiesPanel(config, view);
		activityInstanceNav.setOpaque(true);
		activityInstanceNav.setBounds(0, 0, NAVIGATOR_WIDTH, 20);
		
		arenaPanelMap.put(linkedTab, panelSet);
		try {
			TaxonomyHelper hierarchicalTreeHelper = new TaxonomyHelper(config, "ConceptView taxnomy tab: " + linkedTab, null);
			JScrollPane treeScroller = hierarchicalTreeHelper.getHierarchyPanel();

			hierarchicalTreeHelper.addMouseListener(new TaxonomyMouseListener(hierarchicalTreeHelper));
			navigatorTree = (TaxonomyTree) treeScroller.getViewport().getView();
			navigatorTree.setFont(navigatorTree.getFont().deriveFont(getFontSize()));
			navigator = new ConceptNavigator(treeScroller, config, view);
			
			// navigator.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1,
			// Color.GRAY));
			navigator.setOpaque(true);
			navigator.setBounds(0, 0, NAVIGATOR_WIDTH, 20);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return view;
	}

	private PreferencesNode newDescTypeNode(DescPreference descPreference) {
		JComboBox descTypeCombo = new JComboBox(DescType.values());

		switch (descPreference) {
		case C1_REFEX:
			descTypeCombo.setSelectedItem(c1Refex);

			break;

		case C2_REFEX:
			descTypeCombo.setSelectedItem(c2Refex);

			break;

		case C3_REFEX:
			descTypeCombo.setSelectedItem(c3Refex);

			break;

		case DESC_TYPE:
			descTypeCombo.setSelectedItem(descType);

			break;

		case REL_TARGET:
			descTypeCombo.setSelectedItem(relTarget);

			break;

		case REL_TYPE:
			descTypeCombo.setSelectedItem(relType);

			break;

		case REFEX_NAME:
			descTypeCombo.setSelectedItem(refexName);

			break;

		default:
			throw new RuntimeException("Can't handle type: " + descPreference);
		}

		descTypeCombo.addActionListener(new DescTypeActionListener(descPreference));

		JPanel descTypePanel = new JPanel(new GridLayout(1, 1));

		descTypePanel.add(descTypeCombo);

		return new PreferencesNode(descPreference.toString(), descTypePanel);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();

		if (objDataVersion <= dataVersion) {
			linkedTab = (Integer) in.readObject();

			if (dataVersion >= 2) {
				forAdjudication = in.readBoolean();
			} else {
				forAdjudication = false;
			}

			if (dataVersion >= 3) {
				descType = (DescType) in.readObject();
				relType = (DescType) in.readObject();
				relTarget = (DescType) in.readObject();
				c1Refex = (DescType) in.readObject();
				c2Refex = (DescType) in.readObject();
				c3Refex = (DescType) in.readObject();
				refexName = (DescType) in.readObject();
			} else {
				descType = DescType.PREFERRED;
				relType = DescType.PREFERRED;
				relTarget = DescType.FULLY_SPECIFIED;
				c1Refex = DescType.PREFERRED;
				c2Refex = DescType.PREFERRED;
				c3Refex = DescType.PREFERRED;
				refexName = DescType.PREFERRED;
			}
			if (dataVersion >= 4) {
				relAssertionType = (RelAssertionType) in.readObject();
			} else {
				relAssertionType = RelAssertionType.STATED;
			}
                        if (dataVersion >= 5) {
				forPromotion = (boolean) in.readObject();
			} else {
				forPromotion = false;
			}
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public void regenerateWfPanel(I_GetConceptData con, boolean newHtmlCodeRquired) {
	}

	@Override
	protected void setupSubtypes() {
		this.getPrefRoot().add(newDescTypeNode(DescPreference.DESC_TYPE));
		this.getPrefRoot().add(newDescTypeNode(DescPreference.REL_TYPE));
		this.getPrefRoot().add(newDescTypeNode(DescPreference.REL_TARGET));
		this.getPrefRoot().add(newDescTypeNode(DescPreference.REFEX_NAME));
		this.getPrefRoot().add(newDescTypeNode(DescPreference.C1_REFEX));
		this.getPrefRoot().add(newDescTypeNode(DescPreference.C2_REFEX));
		this.getPrefRoot().add(newDescTypeNode(DescPreference.C3_REFEX));
	}

	public boolean showInferred() {
		if (relAssertionType == null) {
			relAssertionType = RelAssertionType.STATED;
		}

		return relAssertionType != RelAssertionType.STATED;
	}

	public boolean showNavigator() {
		if (!navButton.isSelected()) {
			navButton.doClick();

			return false;
		}

		return true;
	}

	public boolean showStated() {
		if (relAssertionType == null) {
			relAssertionType = RelAssertionType.STATED;
		}

		return relAssertionType != RelAssertionType.INFERRED;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(linkedTab);
		out.writeBoolean(forAdjudication);
		out.writeObject(descType);
		out.writeObject(relType);
		out.writeObject(relTarget);
		out.writeObject(c1Refex);
		out.writeObject(c2Refex);
		out.writeObject(c3Refex);
		out.writeObject(refexName);
		out.writeObject(relAssertionType);
                out.writeBoolean(forPromotion);
	}

	// ~--- get methods
	// ---------------------------------------------------------
	public DescType getC1Refex() {
		return c1Refex;
	}

	public DescType getC2Refex() {
		return c2Refex;
	}

	public DescType getC3Refex() {
		return c3Refex;
	}

	@Override
	public I_GetConceptData getConcept() {
		if (getHost() != null) {
			return (I_GetConceptData) getHost().getTermComponent();
		}

		return null;
	}

	public DescType getDescType() {
		return descType;
	}

	@Override
	public I_HostConceptPlugins getHost() {
		if ((linkedTab != null) && (linkedTab != -1)) {
			I_ConfigAceFrame hostConfig = getConfig();
			if (hostConfig != null) {
				if (linkedTab == -2) {
					return hostConfig.getListConceptViewer();
				}

				return hostConfig.getConceptViewer(linkedTab);
			}
		}

		return null;
	}

	@Override
	public JComponent getLinkComponent() {
		if ((linkedTab != null) && (linkedTab >= 0)) {
			JButton goToLinkButton = new JButton(new AbstractAction(" " + linkedTab.toString() + " ") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					if ((linkedTab != null) && (linkedTab != -1)) {
						getConfig().selectConceptViewer(linkedTab);
					}
				}
			});

			goToLinkButton.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 1));
			goToLinkButton.setForeground(Color.GRAY);

			return goToLinkButton;
		}

		return new JLabel();
	}

	public Integer getLinkedTab() {
		return linkedTab;
	}

	public JToggleButton getNavButton() {
		return navButton;
	}

	protected InstanceActivitiesPanel getAiNavigator() {

		InstanceActivitiesPanel mypanel = new InstanceActivitiesPanel(config, view);

		return mypanel;
	}

	protected ConceptNavigator getNavigator() {

//Commenting out for now, is slowing down the view of the history panel when working with retired concepts
//as navigator is trying to show the concept in the taxonomy.
            
//		if (getHost() != null) {
//			if (getHost().getTermComponent() != null) {
//				SwingUtilities.invokeLater(new Runnable() {
//					@Override
//					public void run() {
//						try {
//							PathExpander epl = new PathExpander(navigatorTree, config, (ConceptChronicleBI) getHost().getTermComponent());
//
//							ACE.threadPool.submit(epl);
//						} catch (IOException e) {
//							AceLog.getAppLog().alertAndLogException(e);
//						}
//					}
//				});
//			}
//		}

		return navigator;
	}

	private JToggleButton getNavigatorButton() {
		JToggleButton button = new JToggleButton(new AbstractAction("", new ImageIcon(
				ConceptViewRenderer.class.getResource("/16x16/plain/compass.png"))) {
			/**
             *
             */
			private static final long serialVersionUID = 1L;
			boolean showNavigator = false;
			boolean historyWasShown = false;

			@Override
			public void actionPerformed(ActionEvent e) {
				showNavigator = !showNavigator;

				if (showNavigator) {
					try {
						try {
							view.getCvLayout().setupPathCheckboxPane();
						} catch (IOException | ContradictionException ex) {
							AceLog.getAppLog().alertAndLogException(ex);
						}

						navigator = getNavigator();
						navigator.setVisible(true);
						setNavigatorLocation();

						if (((JToggleButton) e.getSource()).isSelected() == false) {
							((JToggleButton) e.getSource()).setSelected(true);
						}
						view.setHistoryShown(historyWasShown);
						navigator.updateHistoryPanel();
						view.resetLastLayoutSequence();
						view.redoConceptViewLayout();
					} catch (IOException ex) {
						AceLog.getAppLog().alertAndLogException(ex);
					}
				} else {
					navigator.setVisible(false);
					historyWasShown = view.isHistoryShown();
					view.setHistoryShown(false);
					navigator.invalidate();
					JLayeredPane layers = renderer.getRootPane().getLayeredPane();
					int index = 0;
					Component[] components = layers.getComponents();
					for (Component layer : components) {
						if (layer instanceof ConceptNavigator) {
							layers.remove(index);
						}
						index++;
					}

					if (((JToggleButton) e.getSource()).isSelected()) {
						((JToggleButton) e.getSource()).setSelected(false);
					}
				}
			}
		});

		button.setPreferredSize(new Dimension(21, 16));
		button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		button.setToolTipText("show navigator");
		button.setOpaque(false);
		button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		return button;
	}

	private JToggleButton getInstanceActivitiesButton() {
		JToggleButton button = new JToggleButton(new AbstractAction("", new ImageIcon("icons/text_code_colored.png")) {
			/**
    				 *
    				 */
			private static final long serialVersionUID = 1L;
			boolean showWlNavigator = false;
			boolean historyWlWasShown = false;

			@Override
			public void actionPerformed(ActionEvent e) {
				showWlNavigator = !showWlNavigator;

				if (showWlNavigator) {
					try {
						try {
							view.getCvLayout().setupPathCheckboxPane();
						} catch (IOException | ContradictionException ex) {
							AceLog.getAppLog().alertAndLogException(ex);
						}

						activityInstanceNav = getAiNavigator();
						activityInstanceNav.setVisible(true);
						if (activityInstanceNav != null) {
							int rightSpace = -1;
							int leftSpace = -1;
							JLayeredPane layers = renderer.getRootPane().getLayeredPane();
							Point loc = SwingUtilities.convertPoint(renderer, new Point(0, 0), layers);

							if (layers.getWidth() > loc.x + renderer.getWidth() + activityInstanceNav.getWidth() + rightSpace) {
								loc.x = loc.x + renderer.getWidth() + rightSpace;
								activityInstanceNav.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.GRAY));
								activityInstanceNav.setDropSide(SIDE.RIGHT);
							} else {
								loc.x = loc.x - activityInstanceNav.getWidth() - leftSpace;
								activityInstanceNav.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.GRAY));
								activityInstanceNav.setDropSide(SIDE.LEFT);
							}

							activityInstanceNav.setBounds(loc.x, loc.y, activityInstanceNav.getWidth(), renderer.getHeight() + 1);
							layers.add(activityInstanceNav, JLayeredPane.POPUP_LAYER);
						}

						if (((JToggleButton) e.getSource()).isSelected() == false) {
							((JToggleButton) e.getSource()).setSelected(true);
						}
						//view.setHistoryShown(historyWlWasShown);
						activityInstanceNav.updateActivityInstances(view.getConcept());
						view.resetLastLayoutSequence();
						view.redoConceptViewLayout();
					} catch (IOException ex) {
						AceLog.getAppLog().alertAndLogException(ex);
					}
				} else {
					activityInstanceNav.setVisible(false);
					historyWlWasShown = view.isHistoryShown();
					view.setHistoryShown(false);
					activityInstanceNav.invalidate();
					JLayeredPane layers = renderer.getRootPane().getLayeredPane();
					int index = 0;
					Component[] components = layers.getComponents();
					for (Component layer : components) {
						if (layer instanceof InstanceActivitiesPanel) {
							layers.remove(index);
						}
						index++;
					}

					if (((JToggleButton) e.getSource()).isSelected()) {
						((JToggleButton) e.getSource()).setSelected(false);
					}
				}
			}
		});

		button.setPreferredSize(new Dimension(21, 16));
		button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		button.setToolTipText("show workflow history");
		button.setOpaque(false);
		button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		return button;
	}

	public DescType getRefexName() {
		return refexName;
	}

	public RelAssertionType getRelAssertionType() {
		return relAssertionType;
	}

	public DescType getRelTarget() {
		return relTarget;
	}

	public DescType getRelType() {
		return relType;
	}

	@Override
	public List<AbstractButton> getSpecializedButtons() {
		List<AbstractButton> buttons = new ArrayList<AbstractButton>();

		navButton = getNavigatorButton();
		aiButton = getInstanceActivitiesButton();
		if (!WorkflowHelper.isWorkflowCapabilityAvailable()) {
			buttons.add(aiButton);
		}
		buttons.add(navButton);
		statedInferredButton = getStatedInferredButton();
		buttons.add(statedInferredButton);

		return buttons;
	}

	protected JButton getStatedInferredButton() {
		JButton button = new JButton(new AbstractAction("", statedView) {
			@Override
			public void actionPerformed(ActionEvent e) {
				view.resetLastLayoutSequence();

				JButton button = (JButton) e.getSource();

				if (relAssertionType == null) {
					relAssertionType = RelAssertionType.STATED;
				}

				switch (relAssertionType) {

				case INFERRED:
					relAssertionType = RelAssertionType.INFERRED_THEN_STATED;

					button.setIcon(inferredAndStatedView);
					button.setToolTipText("showing inferred and stated, toggle to show short normal form...");
					fireConceptChanged();
					break;
				case STATED:
					relAssertionType = RelAssertionType.INFERRED;
					button.setIcon(inferredView);
					button.setToolTipText("showing inferred, toggle to show toggle to show inferred and stated...");
					fireConceptChanged();

					break;
				case INFERRED_THEN_STATED:
					relAssertionType = RelAssertionType.SHORT_NORMAL_FORM;
					button.setIcon(shortNormalView);
					button.setToolTipText("showing short normal form, toggle to show long nomal form...");
					fireConceptChanged();

					break;
                                case SHORT_NORMAL_FORM:
					relAssertionType = RelAssertionType.LONG_NORMAL_FORM;
					button.setIcon(longNormalView);
					button.setToolTipText("showing long normal form, toggle to show stated...");
					fireConceptChanged();

					break;
                                case LONG_NORMAL_FORM:
					relAssertionType = RelAssertionType.STATED;
					button.setIcon(statedView);
					button.setToolTipText("showing stated, toggle to show inferred...");
					fireConceptChanged();

					break;
				}
			}
		});

		button.setSelected(true);
                button.setToolTipText("rel display type");
		button.setPreferredSize(new Dimension(21, 16));
		button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		button.setOpaque(false);
		button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		return button;
	}

	public void fireConceptChanged() {
		conceptChangedListener.propertyChange(null);
	}

	@Override
	public String getTitle() {
		if (getHost() != null) {
			if (getHost().getTermComponent() != null) {
				try {
					ConceptChronicleBI cc = (ConceptChronicleBI) getHost().getTermComponent();
					ConceptVersionBI cv = Ts.get().getConceptVersion(config.getViewCoordinate(), cc.getConceptNid());
					if (!cv.getDescriptionsFullySpecifiedActive().isEmpty()) {
						DescriptionVersionBI fsn = cv.getDescriptionsFullySpecifiedActive().iterator().next();
						return fsn.getText();
					} else {
						for (DescriptionChronicleBI desc : cv.getDescriptions()) {
							for (DescriptionVersionBI dv : desc.getVersions()) {
								if (dv.getTypeNid() == SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID()) {
									return dv.getText();
								}
								return dv.getText();
							}
						}
					}
					return "concept missing descriptions";

				} catch (IOException ex) {
					AceLog.getAppLog().alertAndLogException(ex);
				}
				return getHost().getTermComponent().toString();
			}
		}
		return "empty";
	}

	public ConceptView getView() {
		return view;
	}

	public boolean isForAdjudication() {
		return forAdjudication;
	}
        
        public boolean isForPromotion() {
		return forPromotion;
	}

	public boolean isNavigatorSetup() {
		if (navigator == null) {
			return false;
		}

		if (getConfig() == null) {
			return false;
		}

		if (getConfig().getConceptViewer(linkedTab) == null) {
			return false;
		}

		return true;
	}

	// ~--- set methods
	// ---------------------------------------------------------
	public void setForAdjudication(boolean forAdjudication) {
		this.forAdjudication = forAdjudication;
	}
        
        public void setForPromotion(boolean forPromotion) {
		this.forPromotion = forPromotion;
	}

	public void setLinkedTab(Integer linkedTab) {
		this.linkedTab = linkedTab;
	}

	public void setNavigatorLocation() {
		if (navigator != null) {
			int rightSpace = -1;
			int leftSpace = -1;
			JLayeredPane layers = renderer.getRootPane().getLayeredPane();
			Point loc = SwingUtilities.convertPoint(renderer, new Point(0, 0), layers);

			if (layers.getWidth() > loc.x + renderer.getWidth() + navigator.getWidth() + rightSpace) {
				loc.x = loc.x + renderer.getWidth() + rightSpace;
				navigator.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.GRAY));
				navigator.setDropSide(SIDE.RIGHT);
			} else {
				loc.x = loc.x - navigator.getWidth() - leftSpace;
				navigator.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.GRAY));
				navigator.setDropSide(SIDE.LEFT);
			}

			navigator.setBounds(loc.x, loc.y, navigator.getWidth(), renderer.getHeight() + 1);
			layers.add(navigator, JLayeredPane.POPUP_LAYER);
		}
	}

	// ~--- inner classes
	// -------------------------------------------------------
	private class ConceptChangedListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (view != null && getHost() != null) {
				try {
					view.layoutConcept((I_GetConceptData) getHost().getTermComponent());
				} catch (IOException iOException) {
					AceLog.getAppLog().alertAndLogException(iOException);
				}
			}
		}
	}

	private class DescTypeActionListener implements ActionListener {

		DescPreference descPreference;

		// ~--- constructors
		// -----------------------------------------------------
		public DescTypeActionListener(DescPreference descPreference) {
			this.descPreference = descPreference;
		}

		// ~--- methods
		// ----------------------------------------------------------
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				JComboBox descTypeCombo = (JComboBox) e.getSource();

				switch (descPreference) {
				case C1_REFEX:
					c1Refex = (DescType) descTypeCombo.getSelectedItem();

					break;

				case C2_REFEX:
					c2Refex = (DescType) descTypeCombo.getSelectedItem();

					break;

				case C3_REFEX:
					c3Refex = (DescType) descTypeCombo.getSelectedItem();

					break;

				case DESC_TYPE:
					descType = (DescType) descTypeCombo.getSelectedItem();

					break;

				case REL_TARGET:
					relTarget = (DescType) descTypeCombo.getSelectedItem();

					break;

				case REL_TYPE:
					relType = (DescType) descTypeCombo.getSelectedItem();

					break;

				case REFEX_NAME:
					refexName = (DescType) descTypeCombo.getSelectedItem();

					break;

				default:
					throw new RuntimeException("Can't handle type: " + descPreference);
				}

				getView().resetLastLayoutSequence();
				getView().layoutConcept(getConcept());
			} catch (IOException ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}
		}
	};
}
