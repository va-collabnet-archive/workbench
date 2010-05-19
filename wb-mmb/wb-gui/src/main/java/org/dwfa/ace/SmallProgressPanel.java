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
package org.dwfa.ace;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class SmallProgressPanel extends JPanel implements I_DisplayProgress {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    JProgressBar progressBar = new JProgressBar();
    JButton stopButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/stop.png")));
    JLabel progressLabel = new JLabel();

    public SmallProgressPanel() {
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0;
        add(progressBar, c);
        add(progressLabel, c);
        c.gridx++;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        add(stopButton, c);
        progressBar.setStringPainted(true);
        setActive(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.I_DisplayProgress#getProgressBar()
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.I_DisplayProgress#setProgressInfo(java.lang.String)
     */
    public void setProgressInfo(final String info) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (info.length() > progressBar.getString().length()) {
                    revalidate();
                }
                progressBar.setString(info);
                progressLabel.setText(info);
            }

        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.I_DisplayProgress#getStopButton()
     */
    public JButton getStopButton() {
        return stopButton;
    }

    public void setActive(final boolean active) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                stopButton.setVisible(active);
                progressBar.setVisible(active);
                progressLabel.setVisible(active == false);
            }

        });
    }

}
