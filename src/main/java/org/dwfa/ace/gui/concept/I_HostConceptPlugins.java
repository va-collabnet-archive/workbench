package org.dwfa.ace.gui.concept;

import org.dwfa.ace.I_ContainTermComponent;
import org.dwfa.vodb.types.ConceptBean;

public interface I_HostConceptPlugins extends I_ContainTermComponent {

	public static final String SHOW_HISTORY = "showHistory";
	public static final String USE_PREFS = "usePrefs";
	public enum VIEW_TYPE {STATED, INFERRED};

	public boolean getShowHistory();

	public boolean getUsePrefs();
	
	public VIEW_TYPE getViewType();
	
	public ConceptBean getHierarchySelection();
	
}
