package org.dwfa.ace;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;

import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.gui.concept.ConceptPanel.LINK_TYPE;

import com.sleepycat.je.DatabaseException;

public class CollectionEditorContainer extends JPanel {
	
	private class ShowComponentActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (showComponentView.isSelected()) {
				showProcessBuilder.setSelected(false);
				listSplit.setBottomComponent(conceptPanelScroller);
				if (lastDividerLocation > 0) {
					listSplit.setDividerLocation(lastDividerLocation);
				} else {
					listSplit.setDividerLocation(0.30);
				}
			}
			if (showOnlyList()) {
				showListOnly();
			}
		}
		
	}

	private class ShowProcessBuilderActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (showProcessBuilder.isSelected()) {
				showComponentView.setSelected(false);
				listSplit.setBottomComponent(processBuilder);
				if (lastDividerLocation > 0) {
					listSplit.setDividerLocation(lastDividerLocation);
				} else {
					listSplit.setDividerLocation(0.30);
				}
			}
			if (showOnlyList()) {
				showListOnly();
			}
		}

		
	}
	private void showListOnly() {
		int dividerLocation = listSplit.getDividerLocation();
		if (dividerLocation != 3000) {
			lastDividerLocation = dividerLocation;
			listSplit.setBottomComponent(new JPanel());	
			listSplit.setDividerLocation(3000);
		}
	}
	private boolean showOnlyList() {
		return showComponentView.isSelected() == false && showProcessBuilder.isSelected() == false;
	}
	int lastDividerLocation = -1;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComponent conceptPanelScroller;
	private JComponent processBuilder;
	private JToggleButton showComponentView;
	private JToggleButton showProcessBuilder;
	private JSplitPane listSplit;

	public CollectionEditorContainer(JList list, ACE ace, JPanel descListProcessBuilderPanel) throws DatabaseException, IOException, ClassNotFoundException {
		super(new GridBagLayout());
		this.processBuilder = descListProcessBuilderPanel;
		ConceptPanel cp = new ConceptPanel(ace,
				LINK_TYPE.LIST_LINK, true);
		cp.setLinkedList(list);
		cp.changeLinkListener(LINK_TYPE.LIST_LINK);
		conceptPanelScroller = new JScrollPane(cp);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(getListEditorTopPanel(), c);
		c.gridy++;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		add(getListSplit(list, ace), c);
	}

	private JSplitPane getListSplit(JList list, ACE ace) throws DatabaseException,
			IOException, ClassNotFoundException {
		listSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		listSplit.setOneTouchExpandable(true);
		listSplit.setTopComponent(new JScrollPane(list));
		listSplit.setBottomComponent(conceptPanelScroller);
		listSplit.setDividerLocation(3000);
		return listSplit;
	}

	private JPanel getListEditorTopPanel() {
		JPanel listEditorTopPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 0;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		showComponentView = new JToggleButton(new ImageIcon(
				ACE.class.getResource("/32x32/plain/component.png")));
		showComponentView.addActionListener(new ShowComponentActionListener());
		listEditorTopPanel.add(showComponentView, c);
		c.gridx++;
		showProcessBuilder = new JToggleButton(new ImageIcon(
				ACE.class.getResource("/32x32/plain/cube_molecule.png")));
		listEditorTopPanel.add(showProcessBuilder, c);
		showProcessBuilder.addActionListener(new ShowProcessBuilderActionListener());
		c.gridx++;
		c.weightx = 1.0;
		listEditorTopPanel.add(new JLabel(" "), c);
		c.gridx++;
		c.weightx = 0.0;
		listEditorTopPanel.add(new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/branch_delete.png"))), c);
		return listEditorTopPanel;

	}

}
