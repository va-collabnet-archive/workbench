package org.dwfa.ace.api;

import java.awt.event.ActionListener;

import javax.swing.JPanel;

public interface I_ShowActivity {
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
	public void setStringPainted(boolean stringPainted); // displays a string on the progress bar
}
