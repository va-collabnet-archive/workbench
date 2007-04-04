package org.dwfa.ace.api;

import java.beans.PropertyChangeListener;


public interface I_ContainTermComponent {
	
	public static final String TERM_COMPONENT = "termComponent";

	public I_AmTermComponent getTermComponent();

	public void setTermComponent(I_AmTermComponent termComponent);

	public void addPropertyChangeListener(String property, PropertyChangeListener l);
	
	public void removePropertyChangeListener(String property, PropertyChangeListener l);
	
	public I_ConfigAceFrame getConfig();
}