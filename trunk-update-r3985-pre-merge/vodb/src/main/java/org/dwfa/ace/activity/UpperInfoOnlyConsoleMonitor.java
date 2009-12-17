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

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.log.AceLog;

public class UpperInfoOnlyConsoleMonitor implements I_ShowActivity {

    long startTime;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void addActionListener(ActionListener l) {
        // TODO Auto-generated method stub

    }

    public void complete() {
        // TODO Auto-generated method stub

    }

    public JPanel getViewPanel() {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeActionListener(ActionListener l) {
        // TODO Auto-generated method stub

    }

    public void setIndeterminate(boolean newValue) {
        // TODO Auto-generated method stub

    }

    public void setMaximum(int n) {
        // TODO Auto-generated method stub

    }

    public void setProgressInfoLower(String text) {
        // TODO Auto-generated method stub

    }

    public void setProgressInfoUpper(String text) {
        AceLog.getAppLog().info(text);

    }

    public void setValue(int n) {
        // TODO Auto-generated method stub

    }

    public void addShowActivityListener(I_ShowActivity listener) {
        throw new UnsupportedOperationException();
    }

    public void removeShowActivityListener(I_ShowActivity listener) {
        throw new UnsupportedOperationException();
    }

    public int getMaximum() {
        throw new UnsupportedOperationException();
    }

    public int getValue() {
        throw new UnsupportedOperationException();
    }

    public boolean isComplete() {
        throw new UnsupportedOperationException();
    }

    public boolean isIndeterminate() {
        throw new UnsupportedOperationException();
    }

    public JPanel getSecondaryPanel() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setSecondaryPanel(JPanel panel) {
        // TODO Auto-generated method stub

    }

    public void setStringPainted(boolean stringPainted) {
        // TODO Auto-generated method stub
    }

    public JButton getStopButton() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setStopButton(JButton stopButton) {
        // TODO Auto-generated method stub

    }
}
