package org.dwfa.ace.gui.concept;

import org.dwfa.ace.I_ContainTermComponent;

public interface I_HostConceptPlugins extends I_ContainTermComponent {

	public static final String SHOW_HISTORY = "showHistory";
	public static final String USE_PREFS = "usePrefs";

	public boolean getShowHistory();

	public boolean getUsePrefs();
	
}
