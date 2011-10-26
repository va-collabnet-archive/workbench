package org.ihtsdo.translation.ui;

import javax.swing.Icon;
import javax.swing.JPanel;



public abstract class MenuComponentPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public abstract String getLabelText();

	public abstract Icon getLabelIcon();
	
	public abstract void setLabelText(String labelText);
	
	public abstract void setLabelIcon(Icon icon);

}
