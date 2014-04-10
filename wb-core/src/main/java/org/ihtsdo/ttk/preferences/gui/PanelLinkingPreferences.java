/*
 * Copyright 2013
 * International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.ttk.preferences.gui;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.ttk.preferences.EnumBasedPreferences;
import org.ihtsdo.ttk.preferences.PreferenceObject;
import org.ihtsdo.ttk.preferences.PreferenceWithDefaultEnumBI;

/**
 *
 * @author akf
 */
public class PanelLinkingPreferences implements PreferenceObject {
   private LINK_TYPE r1;
   private LINK_TYPE r2;
   private LINK_TYPE r3;
   private LINK_TYPE r4;

   public PanelLinkingPreferences() {
      this.r1 = LINK_TYPE.valueOf(Fields.R1_LINK_TYPE.getDefaultValue());
      this.r2 = LINK_TYPE.valueOf(Fields.R2_LINK_TYPE.getDefaultValue());
      this.r3 = LINK_TYPE.valueOf(Fields.R3_LINK_TYPE.getDefaultValue());
      this.r4 = LINK_TYPE.valueOf(Fields.R4_LINK_TYPE.getDefaultValue());
   }

   public PanelLinkingPreferences(LINK_TYPE r1, LINK_TYPE r2, LINK_TYPE r3, LINK_TYPE r4) {
      this.r1 = r1;
      this.r2 = r2;
      this.r3 = r3;
      this.r4 = r4;
   }

   public PanelLinkingPreferences(EnumBasedPreferences preferences) {
      this.r1 = LINK_TYPE.valueOf(preferences.get(Fields.R1_LINK_TYPE));
      this.r2 = LINK_TYPE.valueOf(preferences.get(Fields.R2_LINK_TYPE));
      this.r3 = LINK_TYPE.valueOf(preferences.get(Fields.R3_LINK_TYPE));
      this.r4 = LINK_TYPE.valueOf(preferences.get(Fields.R4_LINK_TYPE));
   }

   enum Fields implements PreferenceWithDefaultEnumBI<String> {
        R1_LINK_TYPE(LINK_TYPE.UNLINKED), R2_LINK_TYPE(LINK_TYPE.UNLINKED),
        R3_LINK_TYPE(LINK_TYPE.UNLINKED), R4_LINK_TYPE(LINK_TYPE.UNLINKED);

      final LINK_TYPE defaultValue;

        private Fields(LINK_TYPE defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public String getDefaultValue() {
            return defaultValue.toString();
        }
   }
   
   public enum LINK_TYPE {
        UNLINKED, SEARCH_LINK, TREE_LINK, 
        DATA_CHECK_LINK, LIST_LINK, ARENA_LINK,
        TABLE_LINK;
    };

   @Override
   public void exportFields(EnumBasedPreferences preferences) {
      preferences.put(Fields.R1_LINK_TYPE, r1.toString());
      preferences.put(Fields.R2_LINK_TYPE, r2.toString());
      preferences.put(Fields.R3_LINK_TYPE, r3.toString());
      preferences.put(Fields.R4_LINK_TYPE, r4.toString());
   }

    public LINK_TYPE getR1() {
        return r1;
    }

    public LINK_TYPE getR2() {
        return r2;
    }

    public LINK_TYPE getR3() {
        return r3;
    }

    public LINK_TYPE getR4() {
        return r4;
    }

    public void setR1(LINK_TYPE r1) {
        this.r1 = r1;
    }

    public void setR2(LINK_TYPE r2) {
        this.r2 = r2;
    }

    public void setR3(LINK_TYPE r3) {
        this.r3 = r3;
    }

    public void setR4(LINK_TYPE r4) {
        this.r4 = r4;
    }

    public void set(int panel, LINK_TYPE type){
        if(panel == 1){
            r1 = type;
        }else if(panel == 2){
            r2 = type;
        }else if(panel == 3){
            r3 = type;
        }else if(panel == 4){
            r4 = type;
        }
    }
}
