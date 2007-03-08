package org.dwfa.ace.activity;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class ActivityViewer {

	private static ActivityViewer viewer = new ActivityViewer();

	JFrame viewerFrame = new JFrame("Activity Viewer");

	JPanel activitiesPanel = new JPanel(new GridLayout(0, 1));

	List<I_ShowActivity> activitiesList = new ArrayList<I_ShowActivity>();

	private ActivityViewer() {
		super();
		JScrollPane scroller = new JScrollPane();
		scroller
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		viewerFrame.setContentPane(scroller);
		viewerFrame.setLocation(20, 1000);
		viewerFrame.setSize(500, 300);
		viewerFrame.setVisible(true);

		JPanel activitiesAndFillerPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		activitiesAndFillerPanel.add(activitiesPanel, gbc);

		gbc.gridy++;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		activitiesAndFillerPanel.add(new JPanel(), gbc);
		scroller.setViewportView(activitiesAndFillerPanel);

	}

	public static void addActivity(I_ShowActivity activity) {
		while (viewer.activitiesList.size() > 10) {
			viewer.activitiesList.remove(9);
		}
		viewer.activitiesList.add(0, activity);
		viewer.activitiesPanel.removeAll();
		for (I_ShowActivity a : viewer.activitiesList) {
			viewer.activitiesPanel.add(a.getViewPanel());
		}
		tickleSize();
	}

	public static void removeActivity(I_ShowActivity activity) {
		viewer.activitiesList.remove(activity);
		viewer.activitiesPanel.removeAll();
		for (I_ShowActivity a : viewer.activitiesList) {
			viewer.activitiesPanel.add(a.getViewPanel());
		}
		tickleSize();
	}

	private static void tickleSize() {
		Dimension size = viewer.viewerFrame.getSize();
		Dimension tempSize = new Dimension(size.width, size.height + 1);
		viewer.viewerFrame.setSize(tempSize);
		viewer.viewerFrame.setSize(size);
	}

}
