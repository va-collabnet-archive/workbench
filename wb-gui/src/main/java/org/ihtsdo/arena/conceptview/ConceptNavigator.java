package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.ihtsdo.arena.conceptview.ConceptViewSettings.SIDE;

public class ConceptNavigator extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTreeWithDragImage navigatorTree;
	private JScrollPane treeScroller;
	private FocusDrop focusDrop;
	private SIDE side = SIDE.RIGHT;
	
	public ConceptNavigator(JScrollPane treeScroller, I_ConfigAceFrame config) {
		super(new GridBagLayout());
		this.treeScroller = treeScroller;
		navigatorTree =  (JTreeWithDragImage) treeScroller.getViewport().getView();
		focusDrop = new FocusDrop(new ImageIcon(ACE.class.getResource("/16x16/plain/flash.png")), 
				navigatorTree, config);
		layoutNavigator();
	}

	public void setDropSide(SIDE side) {
		if (this.side != side) {
			this.side = side;
			layoutNavigator();
		}
	}
	
	private void layoutNavigator() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 0;
		
		add(setupTopPanel(), gbc);

		gbc.gridwidth = 1;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		add(treeScroller, gbc);
		gbc.weightx = 0;
		switch (side) {
			case LEFT:
				gbc.gridx = 2;
				break;
			case RIGHT:
				gbc.gridx = 0;
				break;
		}
		add(focusDrop, gbc);
	}

	private JPanel setupTopPanel() {
		JPanel topPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		
		JLabel navIcon = new JLabel(new ImageIcon(
                ConceptViewRenderer.class.getResource("/16x16/plain/compass.png")));
		topPanel.add(navIcon, gbc);
		navIcon.setVisible(false);
		gbc.weightx = 1;
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		topPanel.add(new JLabel(" "), gbc);
		
		gbc.gridx++;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		JButton taxonomyButton = new JButton(new ImageIcon(
                ConceptViewRenderer.class.getResource("/16x16/plain/text_tree.png")));
		taxonomyButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		topPanel.add(taxonomyButton, gbc);
		gbc.gridx++;
		JButton historyButton = new JButton(new ImageIcon(
                ConceptViewRenderer.class.getResource("/16x16/plain/radar-chart.png")));
		historyButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		topPanel.add(historyButton, gbc);
		gbc.gridx++;
		JButton statedInferredButton = new JButton(new ImageIcon(
                ConceptViewRenderer.class.getResource("/16x16/plain/chrystal_ball.png")));
		statedInferredButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		topPanel.add(statedInferredButton, gbc);
		
		
		topPanel.setBackground(ConceptViewTitle.TITLE_COLOR);
		topPanel.setOpaque(true);
		topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
		return topPanel;
	}
}
