package org.dwfa.ace;

import java.awt.event.ActionListener;

import javax.swing.JLabel;

import com.sleepycat.je.DatabaseException;

public interface I_ImplementActiveLabel {
	
	public static final int DOUBLE_CLICK = 1;
	public static final int POPUP = 2;

	public JLabel getLabel();
	
	public I_ImplementActiveLabel copy() throws DatabaseException;
	
	public void addActionListener(ActionListener l);
	public void removeActionListener(ActionListener l);

}
