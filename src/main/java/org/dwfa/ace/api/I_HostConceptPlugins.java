package org.dwfa.ace.api;


public interface I_HostConceptPlugins extends I_ContainTermComponent {

	public static final String SHOW_HISTORY = "showHistory";
	public static final String USE_PREFS = "usePrefs";
	public enum VIEW_TYPE {STATED, INFERRED};
	public enum TOGGLES { ID, ATTRIBUTES, DESCRIPTIONS, SOURCE_RELS, DEST_RELS, 
		LINEAGE, IMAGE, CONFLICT, STATED_INFERRED, PREFERENCES, HISTORY };
		
		public enum LINK_TYPE {
			UNLINKED, SEARCH_LINK, TREE_LINK, LIST_LINK
		};

	public boolean getShowHistory();

	public boolean getUsePrefs();
	
	public VIEW_TYPE getViewType();
	
	public I_GetConceptData getHierarchySelection();

	public void unlink();
	
	public void setToggleState(TOGGLES toggle, boolean state);
	
	public void setAllTogglesToState(boolean state);
	
	public void setLinkType(LINK_TYPE link);
	
}
