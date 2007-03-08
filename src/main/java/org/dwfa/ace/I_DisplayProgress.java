package org.dwfa.ace;

import javax.swing.JButton;
import javax.swing.JProgressBar;

public interface I_DisplayProgress {

	public JProgressBar getProgressBar();

	public void setProgressInfo(String info);

	public JButton getStopButton();
	
	public void setActive(boolean active);

}