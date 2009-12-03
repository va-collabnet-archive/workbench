package org.dwfa.ace.task.classify;

import java.util.ArrayList;

public class SnoQuery {

    static ArrayList<SnoRel> isaAdded;
    static ArrayList<SnoRel> isaDropped;
    static ArrayList<SnoRel> roleAdded;
    static ArrayList<SnoRel> roleDropped;
    static SnoConGrpList equivCon;

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
}
