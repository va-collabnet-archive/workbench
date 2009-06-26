package org.dwfa.ace.activity;

import java.awt.event.ActionListener;

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

}
