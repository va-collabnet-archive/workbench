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
package org.dwfa.ace.api;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public interface I_ShowActivity {
    public JButton getStopButton();

    public void setStopButton(JButton stopButton);

    public JPanel getViewPanel();

    public void setProgressInfoUpper(String text);

    public void setProgressInfoLower(String text);

    public void setIndeterminate(boolean newValue);

    public boolean isIndeterminate();

    public void setMaximum(int n);

    public int getMaximum();

    public void setValue(int n);

    public int getValue();

    public void addActionListener(ActionListener l);

    public void removeActionListener(ActionListener l);

    public void complete();

    public boolean isComplete();

    public long getStartTime();

    public void setStartTime(long time);

    public JPanel getSecondaryPanel();

    public void setSecondaryPanel(JPanel panel);

    public void addShowActivityListener(I_ShowActivity listener);

    public void removeShowActivityListener(I_ShowActivity listener);

    public void setStringPainted(boolean stringPainted); // displays a string on
    // the progress bar
}
