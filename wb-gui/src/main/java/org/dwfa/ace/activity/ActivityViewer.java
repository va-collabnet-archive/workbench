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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import net.jini.config.ConfigurationException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.log.AceLog;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.util.ComponentFrame;
import org.ihtsdo.util.swing.GuiUtil;

/**
 * TODO get this viewer to work more directly with the java 6 Swing worker.  
 * @author kec
 *
 */
public class ActivityViewer implements ActionListener {

    private static ActivityViewer viewer;
    private static boolean resort = false;

    private static class CompleteListener implements I_ShowActivity {
        
        @SuppressWarnings("unused")
		I_ShowActivity source;
        

        private CompleteListener(I_ShowActivity source) {
            super();
            this.source = source;
        }

        @Override
        public void removeActivityFromViewer() {
            viewer.activitiesList.remove(this);
            updateActivity.set(true);
        }

        public void addRefreshActionListener(ActionListener l) {
        }

        public void addShowActivityListener(I_ShowActivity listener) {
        }

        public void complete() {
        	resort = true;
         }

        public int getMaximum() {
            return 0;
        }

        public long getStartTime() {
            return 0;
        }

        public int getValue() {
            return 0;
        }

        public JPanel getViewPanel(boolean shotBorder) {
            return null;
        }

        public boolean isComplete() {
            return false;
        }

        public boolean isIndeterminate() {
            return false;
        }

        public void removeRefreshActionListener(ActionListener l) {
        }

        public void removeShowActivityListener(I_ShowActivity listener) {
        }

        public void setIndeterminate(boolean newValue) {
            resort = true;
        }

        public void setMaximum(int n) {
        }

        public void setProgressInfoLower(String text) {
        }

        public void setProgressInfoUpper(String text) {
        }

        public void setStartTime(long time) {
        }

        public void setValue(int n) {
        }

 
        public void setStringPainted(boolean stringPainted) {
        }


        public I_ConfigAceFrame getAceFrameConfig() {
            return null;
        }

        public String getProgressInfoLower() {
            return null;
        }

        public String getProgressInfoUpper() {
            return null;
        }

        public boolean isStringPainted() {
            return false;
        }

        @Override
        public void update() {
            // Nothing to do
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Nothing to do
        }

        @Override
        public void addStopActionListener(ActionListener l) {
            // Nothing to do
        }

        @Override
        public boolean isStopButtonVisible() {
            return false;
        }

        @Override
        public void removeStopActionListener(ActionListener l) {
            // Nothing to do
        }

        @Override
        public void setStopButtonVisible(boolean visible) {
            // Nothing to do
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public boolean isCompleteForComparison() {
            return false;
        }

		@Override
		public void cancel() {
	        // Nothing to do
		}

    }

    private class ActivityViewerFrame extends ComponentFrame {
        /**
		 *
		 */
        private static final long serialVersionUID = 1L;

        public ActivityViewerFrame() throws Exception {
            super(null, null);
            getQuitList().clear();
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setSendToBackInsteadOfClose(true);
        }

        @Override
        public void addAppMenus(JMenuBar mainMenuBar) throws Exception {
            // Nothing to do
        }

        
        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public JMenuItem[] getNewWindowMenu() {
            return null;
        }

        @Override
        public String getNextFrameName() throws ConfigurationException {
            return "Activity Viewer";
        }

        @Override
        public JMenu getQuitMenu() {
            return null;
        }

        public void addInternalFrames(JMenu menu) {
            // nothing to do...
        }

    }

    private ComponentFrame viewerFrame;

    private List<I_ShowActivity> activitiesList = new CopyOnWriteArrayList<I_ShowActivity>();
    private Set<I_ShowActivity> activitiesSet = new CopyOnWriteArraySet<I_ShowActivity>();
    
    private static Timer resortTimer = new Timer(500, null);
    private static Timer updateTimer = new Timer(200, null);
    static {
        updateTimer.start();
        resortTimer.start();
    }
   
    public static void removeFromUpdateTimer(ActionListener l) {
        updateTimer.removeActionListener(l);
    }
 
    public static void addToUpdateTimer(ActionListener l) {
        updateTimer.addActionListener(l);
    }
 
    private ActivityViewer() throws Exception {
        super();
        viewer = this;
        if (DwfaEnv.isHeadless() == false) {
            viewerFrame = new ActivityViewerFrame();
            JScrollPane scroller = new JScrollPane();
            scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            viewerFrame.setContentPane(scroller);
            viewerFrame.setLocation(20, 20);
            viewerFrame.setSize(600, 400);
            viewerFrame.setVisible(true);
            updateActivity.set(true);
            resortTimer.addActionListener(this);
        }

    }


    private static ActivityComparator activityComparator = new ActivityComparator();

    private static class ActivityComparator implements Comparator<I_ShowActivity> {

        public int compare(I_ShowActivity a1, I_ShowActivity a2) {
            if (a1.isCompleteForComparison() && a2.isCompleteForComparison()) {
                if (a1.getStartTime() < a2.getStartTime()) {
                    return 1;
                }
                return -1;
            }
            if (a1.isCompleteForComparison() != a2.isCompleteForComparison()) {
                if (a1.isCompleteForComparison()) {
                    return 1;
                }
                return -1;
            } else if (a1.isIndeterminate() != a2.isIndeterminate()) {
                if (a1.isIndeterminate()) {
                    return 1;
                }
                return -1;
            } else {
                float a1Percent = 0;
                if (a1.getMaximum() != 0) {
                    a1Percent = a1.getValue() / a1.getMaximum();
                }
                float a2Percent = 0;
                if (a2.getMaximum() != 0) {
                    a2Percent = a2.getValue() / a2.getMaximum();
                }
                if (a1Percent != a2Percent) {
                    if (a1Percent < a2Percent) {
                        return -1;
                    }
                    return 1;
                }
            }
            if (a1.getStartTime() < a2.getStartTime()) {
                return 1;
            }
            return -1;
        }

    }

    public static void addActivity(final I_ShowActivity activity) throws Exception {
        if (DwfaEnv.isHeadless() == false) {
           
            activity.addShowActivityListener(new CompleteListener(activity));
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
                        if (!viewer.activitiesSet.contains(activity)) {
                            viewer.activitiesSet.add(activity);
                            ActivityViewer.updateTimer.addActionListener(activity);
                            resort = true;
                        }
                        
                        updateActivities();
                    } catch (HeadlessException e) {
                        AceLog.getAppLog().log(Level.WARNING, e.toString(), e);
                    }
                }

 
            });
        }
    }

    private static void updateActivities() {
    	updateActivity.set(true);
    }


    private static void linkToSourceFrameActivityPanel() {
        for (I_ShowActivity a : viewer.activitiesList) {
            if (a.getAceFrameConfig() != null) {
                if (a.getAceFrameConfig().getTopActivity() != null) {
                    a.getAceFrameConfig().setTopActivity(null);
                }
            }
        }
        for (I_ShowActivity a : viewer.activitiesList) {
            if (a.getAceFrameConfig() != null) {
                if (a.getAceFrameConfig().getTopActivity() == null) {
                    a.getAceFrameConfig().setTopActivity(a);
                }
            }
        }
    }

    private static class ActivitySorter extends SwingWorker<Boolean, Object> {

        @Override
        protected Boolean doInBackground() throws Exception {
            ArrayList<I_ShowActivity> origOrder = null;
            origOrder = new ArrayList<I_ShowActivity>(viewer.activitiesList);
            List<I_ShowActivity> listToSort = new ArrayList<I_ShowActivity>(viewer.activitiesSet);
            Collections.sort(listToSort, activityComparator);
            if (origOrder.equals(listToSort)) {
                return false;
            }
            synchronized (viewer.activitiesList) {
                while (viewer.activitiesList.size() > 60) {
                    viewer.activitiesList.remove(60);
                }
            }
            viewer.activitiesList = new CopyOnWriteArrayList<I_ShowActivity>(listToSort); 
            return true;
        }

        @Override
        protected void done() {
            try {
                boolean changed = get();
                if (changed) {
                    updateActivities();
                }
            } catch (InterruptedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (ExecutionException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
        
    }
    
    public static void removeActivity(final I_ShowActivity activity) {
        if (DwfaEnv.isHeadless() == false) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    updateTimer.removeActionListener(activity);
                    viewer.activitiesList.remove(activity);
                    viewer.activitiesSet.remove(activity);
                    updateActivities();
                }
            });
        }
    }

    public static void toFront() {
        if (SwingUtilities.isEventDispatchThread()) {
            if (viewer != null) {
                viewer.viewerFrame.setVisible(true);
                viewer.viewerFrame.toFront();
            }
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                
                @Override
                public void run() {
                    if (viewer != null) {
                        viewer.viewerFrame.setVisible(true);
                        viewer.viewerFrame.toFront();
                    }
                }
            });
        }
    }

    public static void toBack() {
         if (SwingUtilities.isEventDispatchThread()) {
             viewer.viewerFrame.toBack();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                 @Override
                public void run() {
                    viewer.viewerFrame.toBack();
                }
            });
        }
    }

    public static JFrame getActivityFrame() {
        if (viewer == null) {
            return null;
        }
        return viewer.viewerFrame;
    }

    static AtomicBoolean updateActivity = new AtomicBoolean(false);
	@Override
	public void actionPerformed(ActionEvent e) {
		if (resort) {
			resort = false;
	        if (DwfaEnv.isHeadless() == false) {
	            new ActivitySorter().execute();
	         }
		}
		
		synchronized (this) {
            boolean update = updateActivity.getAndSet(false);
            if (update) {
                new UpdateActivities().execute();
            }
        }
	}
	
	   private static class UpdateActivities extends SwingWorker<JPanel, Object> {

	        @Override
	        protected JPanel doInBackground() throws Exception {
	        		JPanel newPanel = new JPanel(new GridBagLayout());
	                GridBagConstraints gbc = new GridBagConstraints();
	                gbc.gridx = 0;
	                gbc.gridy = 0;
	                gbc.weightx = 1;
	                gbc.weighty = 0;
	                gbc.fill = GridBagConstraints.HORIZONTAL;
	                gbc.anchor = GridBagConstraints.NORTHWEST;
	                gbc.gridwidth = 1;
	                gbc.gridheight = 1;
	                linkToSourceFrameActivityPanel();
	                for (I_ShowActivity a : viewer.activitiesList) {
	                    newPanel.add(a.getViewPanel(true), gbc);
	                    gbc.gridy++;
	                }
	                JPanel activitiesAndFillerPanel = new JPanel(new GridBagLayout());
	                gbc = new GridBagConstraints();
	                gbc.fill = GridBagConstraints.HORIZONTAL;
	                gbc.gridx = 0;
	                gbc.gridy = 0;
	                gbc.gridheight = 1;
	                gbc.weightx = 1.0;
	                gbc.weighty = 0;
	                gbc.anchor = GridBagConstraints.NORTHWEST;
	                activitiesAndFillerPanel.add(newPanel, gbc);

	                gbc.gridy++;
	                gbc.weighty = 1;
	                gbc.fill = GridBagConstraints.BOTH;
	                activitiesAndFillerPanel.add(new JPanel(), gbc);
	         	                return activitiesAndFillerPanel;
	        }

	        @Override
	        protected void done() {
	        	try {
	                viewer.viewerFrame.setContentPane(new JScrollPane(get()));
					boolean tickle = true;
					if (tickle) {
						GuiUtil.tickle(viewer.viewerFrame);
					}
				} catch (InterruptedException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (ExecutionException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
	        }

	        
	    }

}
