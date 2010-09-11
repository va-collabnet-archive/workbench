package org.ihtsdo.arena.conceptview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ihtsdo.arena.ArenaComponentSettings;

public class CollapsePanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	boolean collapsed = false;
	
	Set<JComponent> components = new HashSet<JComponent>();

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

	public CollapsePanel(String labelStr, ArenaComponentSettings settings) {
		super();
        setBackground(Color.LIGHT_GRAY);
        setOpaque(true);
        setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY));
        setLayout(new BorderLayout());

        JLabel label = new JLabel(labelStr);
        label.setFont(getFont().deriveFont(settings.getFontSize()));
        label.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
        add(label, BorderLayout.CENTER);
        JPanel toolBar2 = new JPanel();
        toolBar2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 2));
        toolBar2.setOpaque(false);
        toolBar2.add(getCollapseExpandButton());
        add(toolBar2, BorderLayout.EAST);
	}

	private JButton getCollapseExpandButton() {
		JButton button = new JButton(new AbstractAction("", new ImageIcon(
                ConceptViewRenderer.class.getResource(ArenaComponentSettings.IMAGE_PATH + "minimize.gif")))
        {

        	/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
        	
            public void actionPerformed(ActionEvent e)
            {
            	collapsed = !collapsed;
            	
            	for (JComponent jc: components) {
            		jc.setVisible(!collapsed);
            	}
                 ((JButton) e.getSource())
                        .setIcon(new ImageIcon(
                        		CollapsePanel.class
                                        .getResource(ArenaComponentSettings.IMAGE_PATH
                                                + (collapsed ? "maximize.gif"
                                                        : "minimize.gif"))));
            }
        });
        button.setPreferredSize(new Dimension(21, 16));
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.setToolTipText("Collapse/Expand");
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
		return button;
	}

	public void addToggleComponent(JComponent component) {
		components.add(component);
	}
}
