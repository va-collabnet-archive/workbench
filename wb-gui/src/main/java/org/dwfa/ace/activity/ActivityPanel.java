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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.tapi.ComputationCanceled;

public class ActivityPanel implements I_ShowActivity, AncestorListener {
    
	private ConcurrentHashMap<ActivityPanelImpl, ActivityPanelImpl> panels = 
    			new ConcurrentHashMap<ActivityPanelImpl, ActivityPanelImpl>();
    private ConcurrentHashMap<ActionListener, ActionListener> listeners = 
    			new ConcurrentHashMap<ActionListener, ActionListener>();
    private ConcurrentHashMap<ActionListener, ActionListener> stopActionListeners = 
    			new ConcurrentHashMap<ActionListener, ActionListener>();
    private CopyOnWriteArraySet<I_ShowActivity> showActivityListeners = 
    			new CopyOnWriteArraySet<I_ShowActivity>();

    
    
    private class StopActionListenerPropigator implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
           canceled = true;
           complete = true;
           stopButtonVisible = false;
           progressInfoLowerStr = "canceled by user...";
           for (I_ShowActivity shower : showActivityListeners) {
               try {
                shower.complete();
               } catch (ComputationCanceled e1) {
                   // nothing to report. ;
               }
           }
           showActivityListeners.clear();
           for (ActionListener sal: stopActionListeners.keySet()) {
               sal.actionPerformed(e);
           }
           stopActionListeners.clear();
        }
        
    }
    private class ActivityPanelImpl extends JPanel {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        JProgressBar progressBar = new ActivityProgress(100);

        JButton stopButton = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/stop.png")));

        JLabel progressInfoUpper = new JLabel();

        JLabel progressInfoLower = new JLabel();
                
        boolean stopped = false;

        
        public ActivityPanelImpl(boolean showBorder) {
            super(new GridBagLayout());
            progressInfoUpper.setFont(new Font("Dialog", java.awt.Font.BOLD, 10));
            progressInfoUpper.setText(progressInfoUpperStr);
            progressInfoLower.setFont(new Font("Dialog", 0, 10));
            progressInfoLower.setText(progressInfoLowerStr);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.gridheight = 1;
            c.weightx = 1.0;
            c.weighty = 0;
            c.anchor = GridBagConstraints.WEST;
            add(makeInfoPanel(), c);

            c.weightx = 0.0;
            c.gridx++;
            c.anchor = GridBagConstraints.EAST;
            c.fill = GridBagConstraints.HORIZONTAL;
            progressBar.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            progressBar.setVisible(false);
            add(progressBar, c);
            c.gridx++;
            stopButton.setVisible(stopButtonVisible);
            if (stopButtonVisible) {
                add(stopButton, c);
            }
            stopButton.addActionListener(sal);
            if (showBorder) {
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray));
            }
        }

        private JPanel makeInfoPanel() {
            JPanel infoGridPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.gridheight = 1;
            c.weightx = 1.0;
            c.weighty = 0;
            c.anchor = GridBagConstraints.WEST;
            infoGridPanel.add(progressInfoUpper, c);
            c.gridy++;
            infoGridPanel.add(progressInfoLower, c);
            infoGridPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            return infoGridPanel;
        }
        
        private class ShowStopAndProgress implements ActionListener {

            private ShowStopAndProgress() {
                super();
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (complete) {
                    stopButton.setVisible(false);
                    progressBar.setVisible(false);
                    stopButtonVisible = false;
                    if (eraseWhenFinishedEnabled) {
                        progressInfoLower.setText("");
                        progressInfoUpper.setText("");
                        ActivityViewer.removeActivity(ActivityPanel.this);
                        ActivityViewer.removeFromUpdateTimer(ActivityPanel.this);
                    }
                    stopped = true;
                    if (aceFrameConfig != null && aceFrameConfig.getTopActivity() == ActivityPanel.this) {
                        aceFrameConfig.setTopActivity(null);
                    }
                } else {
                    stopButton.setVisible(true);
                    progressBar.setVisible(true);
                }
                ActivityViewer.removeFromUpdateTimer(this);
            }

        }

        public void update() {
            if (!progressInfoUpper.getText().equals(progressInfoUpperStr)) {
                progressInfoUpper.setText(progressInfoUpperStr);
            }
            if (!progressInfoLower.getText().equals(progressInfoLowerStr)) {
                progressInfoLower.setText(progressInfoLowerStr);
            }
            if (stopButtonVisible) {
                if (progressBar.isVisible() == false) {
                    progressBar.setVisible(true);
                }
            }
            
            if (max != progressBar.getMaximum()) {
                if (progressBar.isVisible() == false) {
                    progressBar.setVisible(true);
                }
                progressBar.setMaximum(max);
            }
            if (value != progressBar.getValue()) {
                if (progressBar.isVisible() == false) {
                    progressBar.setVisible(true);
                }
                progressBar.setValue(value);
            }
            if (indeterminate != progressBar.isIndeterminate()) {
                progressBar.setIndeterminate(indeterminate);
            }
            
            if (stringPainted != progressBar.isStringPainted()) {
                progressBar.setStringPainted(stringPainted);
            }
            
            if (stopButtonVisible != stopButton.isVisible()) {
                stopButton.setVisible(stopButtonVisible);
            }
            if (complete && !stopped) {
                ActivityViewer.addToUpdateTimer(new ShowStopAndProgress());
            }
        }
    }

    private long startTime = System.currentTimeMillis();
    private I_ConfigAceFrame aceFrameConfig;
    private boolean eraseWhenFinishedEnabled = false;
    private String progressInfoUpperStr = "";
    private String progressInfoLowerStr = "";
    private int max = Integer.MAX_VALUE;
    private int value = 0;
    private boolean indeterminate = true;
    private boolean removed = false;
    private boolean stringPainted = false;
    private boolean stopButtonVisible = true;
    private boolean canceled = false;

    private StopActionListenerPropigator sal = new StopActionListenerPropigator();
    
    public void update() {
        for (ActivityPanelImpl panel: panels.keySet()) {
            panel.update();
        }
        if (complete) {
            ActivityViewer.removeFromUpdateTimer(this);
        }
    }
    
    
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    char[] spinners = new char[] { '|', '\\', '-', '/' };
    int spinnerIndex = 0;

    public char nextSpinner() {
        if (spinnerIndex == spinners.length) {
            spinnerIndex = 0;
        }
        return spinners[spinnerIndex++];
    }


    private class ActivityProgress extends JProgressBar {
        /**
		 *
		 */
        private static final long serialVersionUID = 1L;
        int width;

        public ActivityProgress(int width) {
            super();
            this.width = width;
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension d = super.getMaximumSize();
            d.width = width;
            return d;
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            d.width = width;
            return d;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = width;
            return d;
        }
    }

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private boolean complete = false;
    
    public boolean isStopButtonVisible() {
        return stopButtonVisible;
    }


    public void setStopButtonVisible(boolean stopButtonVisible) {
        this.stopButtonVisible = stopButtonVisible;
        for (I_ShowActivity shower : showActivityListeners) {
            shower.setStopButtonVisible(stopButtonVisible);
        }
    }


    public ActivityPanel(I_ConfigAceFrame aceFrameConfig, boolean showStop) {
        this.aceFrameConfig = aceFrameConfig;
        this.stopButtonVisible = showStop;
    }

    @Override
    public void removeActivityFromViewer() {
        if (!removed) {
            removed = true;
            ActivityViewer.removeActivity(this);
        }
    }

    public JPanel getViewPanel(boolean showBorder) {
        ActivityPanelImpl viewPanel = new ActivityPanelImpl(showBorder);
        panels.put(viewPanel, viewPanel);
        viewPanel.addAncestorListener(this);
        viewPanel.update();
        return viewPanel;
    }

    public void setProgressInfoUpper(String text) {
    	assert text != null;
    	progressInfoUpperStr = text;
        for (I_ShowActivity shower : showActivityListeners) {
            shower.setProgressInfoUpper(text);
        }
    }

    public void setProgressInfoLower(String text) {
    	assert text != null;
    	progressInfoLowerStr = text;
        for (I_ShowActivity shower : showActivityListeners) {
            shower.setProgressInfoLower(text);
        }
    }

    public void setIndeterminate(boolean newValue) {
        this.indeterminate = newValue;
        for (I_ShowActivity shower : showActivityListeners) {
            shower.setIndeterminate(newValue);
        }
    }

    public void setMaximum(int n) {
        this.max = n;
    }

    public void setValue(int n) {
        this.value = n;
        for (I_ShowActivity shower : showActivityListeners) {
            shower.setMaximum(this.max);
            shower.setValue(n);
        }
    }

    public void complete() throws ComputationCanceled {
        this.complete = true;
        this.stopButtonVisible = false;
        for (I_ShowActivity shower : showActivityListeners) {
            shower.complete();
        }
        if (canceled) {
            throw new ComputationCanceled();
        }
    }

    public void addRefreshActionListener(final ActionListener l) {
        listeners.put(l, l);
        for (I_ShowActivity shower : showActivityListeners) {
            shower.addRefreshActionListener(l);
        }
    }

    public void removeRefreshActionListener(final ActionListener l) {
        listeners.remove(l);
        for (I_ShowActivity shower : showActivityListeners) {
            shower.removeRefreshActionListener(l);
        }
    }

    public void addShowActivityListener(I_ShowActivity listener) {
        showActivityListeners.add(listener);
    }

    public void removeShowActivityListener(I_ShowActivity listener) {
        showActivityListeners.remove(listener);
    }

    public int getMaximum() {
        return max;
    }

    public int getValue() {
        return value;
    }

    public boolean isComplete() throws ComputationCanceled {
        if (canceled) {
            throw new ComputationCanceled();
        }
        return this.complete;
    }

    public boolean isCompleteForComparison() {
        return this.complete;
    }

    public boolean isIndeterminate() {
        return indeterminate;
    }

    public boolean isStringPainted() {
        return this.stringPainted;
    }

    public void setStringPainted(final boolean stringPainted) {
        this.stringPainted = stringPainted;
        for (I_ShowActivity shower : showActivityListeners) {
            shower.setStringPainted(stringPainted);
        }
    }

    public I_ConfigAceFrame getAceFrameConfig() {
        return aceFrameConfig;
    }

    public String getProgressInfoLower() {
        return progressInfoLowerStr;
    }

    public String getProgressInfoUpper() {
        return progressInfoUpperStr;
    }

    public boolean isEraseWhenFinishedEnabled() {
        return eraseWhenFinishedEnabled;
    }

    public void setEraseWhenFinishedEnabled(boolean eraseWhenFinishedEnabled) {
        this.eraseWhenFinishedEnabled = eraseWhenFinishedEnabled;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        update();
    }


    @Override
    public void ancestorAdded(AncestorEvent event) {
        // Nothing to do...
    }


    @Override
    public void ancestorMoved(AncestorEvent event) {
        // Nothing to do...
    }


    @Override
    public void ancestorRemoved(AncestorEvent event) {
        ActivityPanelImpl aPanel = (ActivityPanelImpl) event.getComponent();
        panels.remove(aPanel);
        aPanel.removeAncestorListener(this);
        aPanel.progressBar.setIndeterminate(false);
        aPanel.progressBar.setEnabled(false);
        aPanel.removeAll();
        aPanel.setVisible(false);
    }


    @Override
    public void addStopActionListener(ActionListener l) {
        stopActionListeners.put(l, l);
    }


    @Override
    public void removeStopActionListener(ActionListener l) {
        stopActionListeners.remove(l);
    }
    
    
    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public void cancel() {
    	this.canceled = true;
        this.complete = true;
        this.stopButtonVisible = false;
        for (I_ShowActivity shower : showActivityListeners) {
            try {
				shower.complete();
			} catch (ComputationCanceled e) {
				// Nothing to do...
			}
        }
        showActivityListeners.clear();
           for (ActionListener sal2: stopActionListeners.keySet()) {
               sal2.actionPerformed(null);
           }
           stopActionListeners.clear();
        
    }
}
