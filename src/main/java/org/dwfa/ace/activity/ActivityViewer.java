package org.dwfa.ace.activity;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.JButton;
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

    private static class CompleteListener implements I_ShowActivity {

        public void addActionListener(ActionListener l) {
        }

        public void addShowActivityListener(I_ShowActivity listener) {
        }

        public void complete() {
            reSort();
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

        public JPanel getViewPanel() {
            return null;
        }

        public boolean isComplete() {
            return false;
        }

        public boolean isIndeterminate() {
            return false;
        }

        public void removeActionListener(ActionListener l) {
        }

        public void removeShowActivityListener(I_ShowActivity listener) {
        }

        public void setIndeterminate(boolean newValue) {
            reSort();
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

        public JPanel getSecondaryPanel() {
            return null;
        }

        public void setSecondaryPanel(JPanel panel) {
        }

        public void setStringPainted(boolean stringPainted) {
        }

        public JButton getStopButton() {
            // TODO Auto-generated method stub
            return null;
        }

        public void setStopButton(JButton stopButton) {
            // TODO Auto-generated method stub

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
        public JMenuItem[] getNewWindowMenu() {
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

    static CompleteListener completeListener = new CompleteListener();

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

    private static ActivityComparator activityComparator = new ActivityComparator();

    private static class ActivityComparator implements
            Comparator<I_ShowActivity> {

        public int compare(I_ShowActivity a1, I_ShowActivity a2) {
            if (a1.isComplete() && a2.isComplete()) {
                if (a1.getStartTime() < a2.getStartTime()) {
                    return 1;
                }
                return -1;
            }
            if (a1.isComplete() != a2.isComplete()) {
                if (a1.isComplete()) {
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

    public static void addActivity(final I_ShowActivity activity)
            throws Exception {
        if (headless == false) {
            activity.addShowActivityListener(completeListener);
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
                        viewer.activitiesList.add(0, activity);
                        Collections.sort(viewer.activitiesList,
                                activityComparator);
                        synchronized (viewer.activitiesList) {
                            while (viewer.activitiesList.size() > 10) {
                                viewer.activitiesList.remove(10);
                            }
                        }
                        Set<JPanel> secondaryPanels = new HashSet<JPanel>();
                        viewer.activitiesPanel.removeAll();
                        for (I_ShowActivity a : viewer.activitiesList) {
                            viewer.activitiesPanel.add(a.getViewPanel());
                            addSecondaryActivityPanel(secondaryPanels, a);
                        }
                        tickleSize();
                    } catch (HeadlessException e) {
                        AceLog.getAppLog().log(Level.WARNING, e.toString(), e);
                    }
                }

            });
        }
    }

    public static void reSort() {
        if (headless == false) {
            SwingUtilities.invokeLater(new Runnable() {

                // TODO turn this into a future task... So sorting does not
                // occur on event loop...
                public void run() {
                    try {
                        ArrayList<I_ShowActivity> origOrder = null;
                        synchronized (viewer.activitiesList) {
                            origOrder = new ArrayList<I_ShowActivity>(
                                    viewer.activitiesList);
                        }
                        Collections.sort(viewer.activitiesList,
                                activityComparator);
                        if (origOrder.equals(viewer.activitiesList) == false) {
                            viewer.activitiesPanel.removeAll();
                            Set<JPanel> secondaryPanels = new HashSet<JPanel>();

                            for (I_ShowActivity a : viewer.activitiesList) {
                                viewer.activitiesPanel.add(a.getViewPanel());
                                addSecondaryActivityPanel(secondaryPanels, a);
                            }
                            tickleSize();
                        }
                    } catch (HeadlessException e) {
                        AceLog.getAppLog().log(Level.WARNING, e.toString(), e);
                    }
                }

            });
        }
    }

    private static void addSecondaryActivityPanel(
            final Set<JPanel> secondaryPanels, final I_ShowActivity a) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (a.getSecondaryPanel() != null
                        && secondaryPanels.contains(a.getSecondaryPanel()) == false) {
                    secondaryPanels.add(a.getSecondaryPanel());
                    for (Component c : a.getSecondaryPanel().getComponents()) {
                        a.getSecondaryPanel().remove(c);
                    }
                    if (a.isComplete() == false) {
                        ActivityPanel secondaryAP = new ActivityPanel(false,
                                true, null);
                        a.addShowActivityListener(secondaryAP);
                        a.getSecondaryPanel().add(secondaryAP.getViewPanel());
                    }
                }
            }
        });

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
    
    public static void toFront() {
    	viewer.viewerFrame.setVisible(true);
    	viewer.viewerFrame.toFront();
    }
}
