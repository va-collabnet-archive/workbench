package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.tree.ExpandPathToNodeStateListener;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.dwfa.ace.tree.TermTreeHelper;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.ArenaComponentSettings;
import org.ihtsdo.arena.PreferencesNode;
import org.ihtsdo.arena.editor.ArenaRenderer;

public class ConceptViewSettings extends ArenaComponentSettings {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	// dataVersion = 1;
	private Integer linkedTab = null;
	
	// transient
	private ConceptView view;
	private JComponent navigator;

	private JTreeWithDragImage navigatorTree;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(linkedTab);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			linkedTab = (Integer) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	public ConceptViewSettings(Integer linkedTab) {
		super();
		this.linkedTab = linkedTab;
	}	
	
	private class ConceptChangedListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (view != null) {
				view.layoutConcept((I_GetConceptData) getHost().getTermComponent());
			}
			
		}
		
	}
	@Override
	public ConceptView makeComponent(I_ConfigAceFrame config) {
		if (view == null) {
			view = new ConceptView(config, this);
			addHostListener(new ConceptChangedListener());
		}
		return view;
	}

	@Override
	public String getTitle() {
		if (getHost() != null) {
			if (getHost().getTermComponent() != null) {
				return getHost().getTermComponent().toString();
			}
		}
		return "empty";
	}

	@Override
	public I_HostConceptPlugins getHost() {
		if (linkedTab != null && linkedTab != -1) {
			if (getConfig() != null) {
				return getConfig().getConceptViewer(linkedTab);
			}
		}
		return null;
	}

	public JComponent getLinkComponent() {
		if (linkedTab != null) {
			JButton goToLinkButton = new JButton(new AbstractAction(" "
					+ linkedTab.toString() + " ") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					if (linkedTab != null && linkedTab != -1) {
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

	@Override
	public List<AbstractButton> getSpecializedButtons() {
		List<AbstractButton> buttons = new ArrayList<AbstractButton>();
		buttons.add(getNavigatorButton());
		buttons.add(getButton("/16x16/plain/element_preferences.png", "show component prefs"));
		return buttons;
	}
	
	protected AbstractButton getNavigatorButton() {
		AbstractButton button = new JToggleButton(new AbstractAction("", new ImageIcon(
                ArenaRenderer.class.getResource("/16x16/plain/compass.png")))
        {

        	/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			boolean showNavigator = false;
        	
            public void actionPerformed(ActionEvent e) {
            	showNavigator = !showNavigator;
           		JLayeredPane layers = renderer.getRootPane().getLayeredPane();
             	if (showNavigator) {
            		setNavigatorLocation();
            		getNavigator().setVisible(true);
            	} else {
            		getNavigator().setVisible(false);
            		getNavigator().invalidate();
             		layers.remove(getNavigator());
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

	private void setNavigatorLocation() {
   		JLayeredPane layers = renderer.getRootPane().getLayeredPane();
		Point loc = SwingUtilities.convertPoint(renderer, new Point(0,0), layers);
		if (layers.getWidth() > loc.x + renderer.getWidth() + getNavigator().getWidth()) {
			loc.x = loc.x + renderer.getWidth();
			getNavigator().setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.GRAY));
		} else {
			loc.x = loc.x - getNavigator().getWidth();
			getNavigator().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.GRAY));
		}
		getNavigator().setBounds(loc.x, loc.y, getNavigator().getWidth(), renderer.getHeight());
		layers.add(getNavigator(), JLayeredPane.PALETTE_LAYER);
	}

	
	protected JComponent getNavigator() {
		if (navigator == null) {
			try {
				TermTreeHelper hierarchicalTreeHelper = new TermTreeHelper(ace.getAceFrameConfig(),
						ace);
				JScrollPane treeScroller = hierarchicalTreeHelper.getHierarchyPanel();
				navigatorTree =  (JTreeWithDragImage) treeScroller.getViewport().getView();
				navigatorTree.setFont(navigatorTree.getFont().deriveFont(getFontSize()));
				navigator = new JPanel(new GridLayout(1,1));
				navigator.add(treeScroller);
				navigator.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.GRAY));
				navigator.setOpaque(true);
				navigator.setBounds(0, 0, 450, 20);
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
		
		if (getHost() != null) {
			if (getHost().getTermComponent() != null) {
		        try {
					new ExpandPathToNodeStateListener(navigatorTree, this.ace.getAceFrameConfig(),
					        (I_GetConceptData) getHost().getTermComponent());
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (TerminologyException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
		}
		

		return navigator;
	}

	@Override
	protected void setupSubtypes() {
		this.getPrefRoot().add(addComponentPrefs("attributes"));
		this.getPrefRoot().add(addComponentPrefs("descriptions"));
		this.getPrefRoot().add(addComponentPrefs("relationships"));
		this.getPrefRoot().add(addComponentPrefs("images"));
		this.getPrefRoot().add(addComponentPrefs("refset members"));
	}

	private PreferencesNode addComponentPrefs(String componentStr) {
		PreferencesNode componentNode = new PreferencesNode(componentStr, new JCheckBox("show " + componentStr + ": "));
		componentNode.add(new PreferencesNode("identifiers", new JCheckBox("show identifiers: ")));
		componentNode.add(new PreferencesNode("extensions", new JCheckBox("show extensions: ")));
		PreferencesNode templateNode = new PreferencesNode("templates", new JCheckBox("show templates: "));
		componentNode.add(templateNode);
		templateNode.add(new PreferencesNode("drools template", new JCheckBox("drools templates: ")));
		
		PreferencesNode filterNode = new PreferencesNode("filters", new JCheckBox("show filters: "));
		filterNode.add(new PreferencesNode("drools filter", new JCheckBox("drools filters: ")));
		componentNode.add(filterNode);
		if (componentStr.equals("descriptions")) {
			componentNode.add(new PreferencesNode("language", new JCheckBox("show language: ")));
			componentNode.add(new PreferencesNode("case sensitivity", new JCheckBox("case sensitive: ")));
			templateNode.add(new PreferencesNode("fully specified", new JCheckBox("fully specified: ")));
		} else if (componentStr.equals("relationships")) {
			componentNode.add(new PreferencesNode("refinability", new JCheckBox("show refinability: ")));
			componentNode.add(new PreferencesNode("characteristic", new JCheckBox("show characteristic: ")));
			templateNode.add(new PreferencesNode("procedures", new JCheckBox("procedures: ")));
			templateNode.add(new PreferencesNode("medicine", new JCheckBox("medicine: ")));
			templateNode.add(new PreferencesNode("finding", new JCheckBox("finding: ")));
			templateNode.add(new PreferencesNode("lab test", new JCheckBox("lab test: ")));
		}
		return componentNode;
	}
}
