package org.ihtsdo.arena;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;

public class ArenaPreferencesPanel extends JPanel {
	
	private ArenaComponentSettings settings;
	private JTree settingsTree;
	private JSplitPane splitPanel = new JSplitPane();

	public ArenaPreferencesPanel(ArenaComponentSettings settings) {
		super(new GridLayout(1,1));
		this.settings = settings;
		this.settingsTree = new JTree(settings.getPrefRoot());
		this.settingsTree.setFont(this.settingsTree.getFont().deriveFont(settings.getFontSize()));
		this.settingsTree.setShowsRootHandles(true);
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) this.settingsTree.getCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setIcon(null);
		renderer.setClosedIcon(null);
		renderer.setOpenIcon(null);
		this.settingsTree.setRootVisible(false);
		settingsTree.addTreeSelectionListener(new PreferencesSelectionListener());
		add(splitPanel);
		splitPanel.setLeftComponent(new JScrollPane(settingsTree));
		splitPanel.setRightComponent(new JLabel("Select preference from tree"));
	}
	
	
	public class PreferencesSelectionListener implements TreeSelectionListener {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			PreferencesNode node = (PreferencesNode)
             		settingsTree.getLastSelectedPathComponent();			
			if (node != null) {
				splitPanel.setRightComponent(new JScrollPane(node.getPrefPanel()));
			} else {
				splitPanel.setRightComponent(new JPanel());
			}
		}
		
	}

}
