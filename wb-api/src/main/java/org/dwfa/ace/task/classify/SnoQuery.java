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
package org.dwfa.ace.task.classify;

import java.util.ArrayList;

public class SnoQuery {

    static ArrayList<SnoRel> isaAdded;
    static ArrayList<SnoRel> isaDropped;
    static ArrayList<SnoRel> roleAdded;
    static ArrayList<SnoRel> roleDropped;
    static SnoConGrpList equivCon;
    private static boolean dirty;

    public SnoQuery() {
        isaAdded = new ArrayList<SnoRel>();
        isaDropped = new ArrayList<SnoRel>();
        roleAdded = new ArrayList<SnoRel>();
        roleDropped = new ArrayList<SnoRel>();
        equivCon = new SnoConGrpList();
    }

    static public void clearAll() {
        isaAdded.clear();
        isaDropped.clear();
        roleAdded.clear();
        roleDropped.clear();
        equivCon.clear();
    }

    static public void clearEquiv() {
        equivCon.clear();
    }

    static public void clearDiff() {
        isaAdded.clear();
        isaDropped.clear();
        roleAdded.clear();
        roleDropped.clear();
    }

    static public void initAll() {
        isaAdded = new ArrayList<SnoRel>();
        isaDropped = new ArrayList<SnoRel>();
        roleAdded = new ArrayList<SnoRel>();
        roleDropped = new ArrayList<SnoRel>();
        equivCon = new SnoConGrpList();
    }

    static public ArrayList<SnoRel> getIsaAdded() {
        return isaAdded;
    }

    static public ArrayList<SnoRel> getIsaDropped() {
        return isaDropped;
    }

    static public ArrayList<SnoRel> getRoleAdded() {
        return roleAdded;
    }

    static public ArrayList<SnoRel> getRoleDropped() {
        return roleDropped;
    }

    static public SnoConGrpList getEquiv() {
        return equivCon;
    }

    public static void setDirty(boolean dirty) {
        SnoQuery.dirty = dirty;
    }

    public static boolean isDirty() {
        return dirty;
    }

}
