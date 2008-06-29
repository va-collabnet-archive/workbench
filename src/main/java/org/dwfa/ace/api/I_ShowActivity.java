package org.dwfa.ace.api;

import java.awt.event.ActionListener;

import javax.swing.JPanel;

public interface I_ShowActivity {
	JPanel getViewPanel();

	public void setProgressInfoUpper(String text);

	public void setProgressInfoLower(String text);

	public void setIndeterminate(boolean newValue);

	public void setMaximum(int n);

	public void setValue(int n);

	public void addActionListener(ActionListener l);

	public void removeActionListener(ActionListener l);

	public void complete();

}
