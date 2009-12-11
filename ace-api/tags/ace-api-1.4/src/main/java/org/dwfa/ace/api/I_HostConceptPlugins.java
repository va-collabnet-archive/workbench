/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.api;

public interface I_HostConceptPlugins extends I_ContainTermComponent {

   public static final String SHOW_HISTORY = "showHistory";

   public static final String USE_PREFS = "usePrefs";

   public enum VIEW_TYPE {
      STATED, INFERRED
   };

   public enum TOGGLES {
      ID, ATTRIBUTES, DESCRIPTIONS, SOURCE_RELS, DEST_RELS, LINEAGE, IMAGE, CONFLICT, STATED_INFERRED, PREFERENCES, HISTORY, REFSETS
   };

   public enum LINK_TYPE {
      UNLINKED, SEARCH_LINK, TREE_LINK, LIST_LINK
   };

   public boolean getShowHistory();

   public boolean getShowRefsets();

   public boolean getUsePrefs();

   public VIEW_TYPE getViewType();

   public I_GetConceptData getHierarchySelection();

   public void unlink();

   public void setToggleState(TOGGLES toggle, boolean state);

   public void setAllTogglesToState(boolean state);

   public void setLinkType(LINK_TYPE link);
   
}
