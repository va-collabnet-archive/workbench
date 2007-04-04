package org.dwfa.ace.api;


public interface I_HostConceptPlugins extends I_ContainTermComponent {

	public static final String SHOW_HISTORY = "showHistory";
	public static final String USE_PREFS = "usePrefs";
	public enum VIEW_TYPE {STATED, INFERRED};

	public boolean getShowHistory();

	public boolean getUsePrefs();
	
	public VIEW_TYPE getViewType();
	
	public I_GetConceptData getHierarchySelection();
	
}
