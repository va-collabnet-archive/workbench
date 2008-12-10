package org.dwfa.ace.api;

public interface I_HostConceptPlugins extends I_ContainTermComponent {

   public static final String SHOW_HISTORY = "showHistory";

   public static final String USE_PREFS = "usePrefs";

   public enum VIEW_TYPE {
      STATED, INFERRED
   };

   public enum TOGGLES {
      ID, ATTRIBUTES, AU_DIALECT, UK_DIALECT, USA_DIALECT, NZ_DIALECT, CA_DIALECT, DESCRIPTIONS, SOURCE_RELS, 
      DEST_RELS, LINEAGE, LINEAGE_GRAPH, IMAGE, CONFLICT, STATED_INFERRED, PREFERENCES, HISTORY, REFSETS
   };
   
   public enum REFSET_TYPES {
       BOOLEAN, CONCEPT, CON_INT,STRING, INTEGER, 
       MEASUREMENT, LANGUAGE, SCOPED_LANGUAGE, 
       TEMPLATE_FOR_REL, TEMPLATE, CROSS_MAP_FOR_REL, CROSS_MAP
   }

   public enum LINK_TYPE {
      UNLINKED, SEARCH_LINK, TREE_LINK, DATA_CHECK_LINK, LIST_LINK 
   };

   public boolean getShowHistory();

   public boolean getShowRefsets();

   public boolean getUsePrefs();

   public VIEW_TYPE getViewType();

   public I_GetConceptData getHierarchySelection();

   public void unlink();

   public void setToggleState(TOGGLES toggle, boolean state);

   public boolean getToggleState(TOGGLES toggle);

   public void setAllTogglesToState(boolean state);

   public void setLinkType(LINK_TYPE link);
   
}
