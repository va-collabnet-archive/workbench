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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.swing.SwingTask;

public class ActivityPanel extends JPanel implements I_ShowActivity {

    private long startTime = System.currentTimeMillis();
    private I_ConfigAceFrame aceFrameConfig;
    private boolean eraseWhenFinishedEnabled = false;

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

    private class ShowStopAndProgress extends SwingTask {

        private ShowStopAndProgress() {
            super();
        }

        @Override
        public void doRun() {
            if (isComplete()) {
                stopButton.setVisible(false);
                progressBar.setVisible(false);
                if (eraseWhenFinishedEnabled) {
                    progressInfoLower.setText("");
                    progressInfoUpper.setText("");
                }
            } else {
                stopButton.setVisible(true);
                progressBar.setVisible(true);
            }
        }

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

    private class StopActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            complete();

        }

    }

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    JProgressBar progressBar = new ActivityProgress(100);

    JButton stopButton = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/stop.png")));

    JLabel progressInfoUpper = new JLabel();

    JLabel progressInfoLower = new JLabel();
    private boolean complete = false;

    public void syncWith(I_ShowActivity another) {
        this.complete = another.isComplete();
        stopButton.setVisible(false);
        setIndeterminate(another.isIndeterminate());
        setMaximum(another.getMaximum());
        setValue(another.getValue());
        setProgressInfoUpper(another.getProgressInfoUpper());
        setProgressInfoLower(another.getProgressInfoLower());
        ActionListener[] stopListeners = getStopButton().getActionListeners();
        setStartTime(another.getStartTime());
        for (ActionListener l : stopListeners) {
            getStopButton().removeActionListener(l);
        }
        for (ActionListener l : another.getStopButton().getActionListeners()) {
            getStopButton().addActionListener(l);
        }
        setStringPainted(another.isStringPainted());
        ACE.timer.schedule(new ShowStopAndProgress(), 1);
    }

    public ActivityPanel(boolean showBorder, I_ShowActivity secondaryPanel, I_ConfigAceFrame aceFrameConfig) {
        super(new GridBagLayout());
        this.secondaryPanel = secondaryPanel;
        this.aceFrameConfig = aceFrameConfig;
        progressInfoUpper.setFont(new Font("Dialog", java.awt.Font.BOLD, 10));
        progressInfoLower.setFont(new Font("Dialog", 0, 10));

        GridBagConstraints c = new GridBagConstraints();

        JPanel infoGridPanel = new JPanel(new GridLayout(2, 1));
        infoGridPanel.add(progressInfoUpper);
        infoGridPanel.add(progressInfoLower);
        infoGridPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.WEST;
        add(infoGridPanel, c);

        c.weightx = 0.0;
        c.gridx++;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        progressBar.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        add(progressBar, c);
        c.gridx++;
        add(stopButton, c);
        stopButton.addActionListener(new StopActionListener());
        if (showBorder) {
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray));
        }
    }

    public JPanel getViewPanel() {
        return this;
    }

    public void setProgressInfoUpper(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressInfoUpper.setText(text);
            }
        });
        for (I_ShowActivity shower : showActivityListeners) {
            shower.setProgressInfoUpper(text);
        }
    }

    public void setProgressInfoLower(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (text.startsWith("<html>")) {
                    progressInfoLower.setText("<html>   " + text.substring(6));
                } else {
                    progressInfoLower.setText("   " + text);
                }
            }
        });
        for (I_ShowActivity shower : showActivityListeners) {
            shower.setProgressInfoLower(text);
        }
    }

    public void setIndeterminate(boolean newValue) {
        progressBar.setIndeterminate(newValue);
        for (I_ShowActivity shower : showActivityListeners) {
            shower.setIndeterminate(newValue);
        }
    }

    public void setMaximum(final int n) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setMaximum(n);
            }
        });
        for (I_ShowActivity shower : showActivityListeners) {
            shower.setMaximum(n);
        }
    }

    public void setValue(final int n) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(n);
            }
        });
        for (I_ShowActivity shower : showActivityListeners) {
            shower.setValue(n);
        }
    }

    public void complete() {
        this.complete = true;
        ACE.timer.schedule(new ShowStopAndProgress(), 1);
        for (I_ShowActivity shower : showActivityListeners) {
            shower.complete();
        }
    }

    public void addActionListener(final ActionListener l) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                stopButton.addActionListener(l);
            }
        });
        for (I_ShowActivity shower : showActivityListeners) {
            shower.addActionListener(l);
        }
    }

    public void removeActionListener(final ActionListener l) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                stopButton.removeActionListener(l);
            }
        });
        for (I_ShowActivity shower : showActivityListeners) {
            shower.removeActionListener(l);
        }
    }

    private HashSet<I_ShowActivity> showActivityListeners = new HashSet<I_ShowActivity>();

    public void addShowActivityListener(I_ShowActivity listener) {
        showActivityListeners.add(listener);
    }

    public void removeShowActivityListener(I_ShowActivity listener) {
        showActivityListeners.remove(listener);
    }

    public int getMaximum() {
        return progressBar.getMaximum();
    }

    public int getValue() {
        return progressBar.getValue();
    }

    public boolean isComplete() {
        return this.complete;
    }

    public boolean isIndeterminate() {
        return progressBar.isIndeterminate();
    }

    I_ShowActivity secondaryPanel;

    public I_ShowActivity getSecondaryPanel() {
        return secondaryPanel;
    }

    public void setSecondaryPanel(I_ShowActivity panel) {
        this.secondaryPanel = panel;
    }

    public boolean isStringPainted() {
        return progressBar.isStringPainted();
    }

    public void setStringPainted(final boolean stringPainted) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setStringPainted(stringPainted);
            }
        });
        for (I_ShowActivity shower : showActivityListeners) {
            shower.setStringPainted(stringPainted);
        }
    }

    public JButton getStopButton() {
        return stopButton;
    }

    public void setStopButton(JButton stopButton) {
        this.stopButton = stopButton;
    }

    public I_ConfigAceFrame getAceFrameConfig() {
        return aceFrameConfig;
    }

    public String getProgressInfoLower() {
        return progressInfoLower.getText();
    }

    public String getProgressInfoUpper() {
        return progressInfoUpper.getText();
    }

    public boolean isEraseWhenFinishedEnabled() {
        return eraseWhenFinishedEnabled;
    }

    public void setEraseWhenFinishedEnabled(boolean eraseWhenFinishedEnabled) {
        this.eraseWhenFinishedEnabled = eraseWhenFinishedEnabled;
    }
}
