package org.dwfa.ace.activity;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
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

    public I_ShowActivity getSecondaryPanel() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setSecondaryPanel(I_ShowActivity panel) {
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

	public I_ConfigAceFrame getAceFrameConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProgressInfoLower() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProgressInfoUpper() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isStringPainted() {
		// TODO Auto-generated method stub
		return false;
	}

	public void syncWith(I_ShowActivity another) {
		// TODO Auto-generated method stub
		
	}
}
