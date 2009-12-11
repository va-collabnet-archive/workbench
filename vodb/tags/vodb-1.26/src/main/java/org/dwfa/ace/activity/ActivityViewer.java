/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.activity;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import net.jini.config.ConfigurationException;

import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.ComponentFrame;

public class ActivityViewer {

	private static boolean headless = true;

	private static ActivityViewer viewer;

	private class ActivityViewerFrame extends ComponentFrame {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ActivityViewerFrame() throws Exception {
			super(null, null);
			getQuitList().clear();
		}

		@Override
		public void addAppMenus(JMenuBar mainMenuBar) throws Exception {
			// TODO Auto-generated method stub

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public JMenuItem getNewWindowMenu() {
			return null;
		}

		@Override
		public String getNextFrameName() throws ConfigurationException {
			return "Activity Viewer";
		}

		@Override
		public JMenu getQuitMenu() {
			// TODO Auto-generated method stub
			return null;
		}

		public void addInternalFrames(JMenu menu) {
			// TODO Auto-generated method stub

		}

	}

	ComponentFrame viewerFrame;

	JPanel activitiesPanel = new JPanel(new GridLayout(0, 1));

	List<I_ShowActivity> activitiesList = new ArrayList<I_ShowActivity>();

	private ActivityViewer() throws Exception {
		super();
		if (headless == false) {
			viewerFrame = new ActivityViewerFrame();
			JScrollPane scroller = new JScrollPane();
			scroller
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			viewerFrame.setContentPane(scroller);
			viewerFrame.setLocation(20, 20);
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

	}

	public static void addActivity(final I_ShowActivity activity)
			throws Exception {
		if (headless == false) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					try {
						if (viewer == null) {
							try {
								viewer = new ActivityViewer();
							} catch (Exception e) {
								AceLog.getAppLog().alertAndLogException(e);
							}
						}
						while (viewer.activitiesList.size() > 10) {
							viewer.activitiesList.remove(9);
						}
						viewer.activitiesList.add(0, activity);
						viewer.activitiesPanel.removeAll();
						for (I_ShowActivity a : viewer.activitiesList) {
							viewer.activitiesPanel.add(a.getViewPanel());
						}
						tickleSize();
					} catch (HeadlessException e) {
						AceLog.getAppLog().log(Level.WARNING, e.toString(), e);
					}
				}

			});
		}
	}

	public static void removeActivity(final I_ShowActivity activity) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				viewer.activitiesList.remove(activity);
				viewer.activitiesPanel.removeAll();
				for (I_ShowActivity a : viewer.activitiesList) {
					viewer.activitiesPanel.add(a.getViewPanel());
				}
				tickleSize();
			}
		});
	}

	private static void tickleSize() {
		Dimension size = viewer.viewerFrame.getSize();
		Dimension tempSize = new Dimension(size.width, size.height + 1);
		viewer.viewerFrame.setSize(tempSize);
		viewer.viewerFrame.setSize(size);
	}

	public static boolean isHeadless() {
		return headless;
	}

	public static void setHeadless(boolean headless) {
		ActivityViewer.headless = headless;
	}

}
