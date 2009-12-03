package org.dwfa.ace.task.classify;

import java.util.ArrayList;

public class SnoDLSet {
    static ArrayList<SnoDL> dlList;
    static ArrayList<SnoConSer> neverGrpList;

    public SnoDLSet() {
        init();
    }

    public static void init() {
        dlList = new ArrayList<SnoDL>();
        neverGrpList = new ArrayList<SnoConSer>();
    }

    public static void initCheck() {
        if (dlList == null || neverGrpList == null)
            init();
    }

    public static void addDL(int nid) {
        dlList.add(new SnoDL(new SnoConSer(nid, false)));
    }

    public static void deleteDL(int index) {
        dlList.remove(index);
    }

    public static void moveDL(int indexA, int direction) {
        int max = dlList.size();
        int indexB = indexA + direction;
        
        if (indexB >= 0 && indexB < max) {
            SnoDL tmp = dlList.get(indexA);
            dlList.set(indexA, dlList.get(indexB)); // move B to A
            dlList.set(indexB, tmp); // move A to B
        }
    }
    
    public static void duplicateDL(int index) {
        dlList.add(new SnoDL(dlList.get(index)));
    }

    public static ArrayList<SnoDL> getDLList() {
        return dlList;
    }
    
    public static void setDLList(ArrayList<SnoDL> dll) {
        dlList = dll;
    }

    public static int sizeDLList() {
        return dlList.size();
    }

    public static void addNeverGroup(int nid) {
        boolean doAdd = true;
        for (SnoConSer sn : neverGrpList)
            if (sn.id == nid) {
                doAdd = false;
                break;
            }

        // :NYI: check if is role below role root as set in preferences

        if (doAdd)
            neverGrpList.add(new SnoConSer(nid, false));
    }

    public static void delNeverGroup(int index) {
        neverGrpList.remove(index);
    }

    public static ArrayList<SnoConSer> getNeverGroup() {
        return neverGrpList;
    }

    public static void setNeverGroup(ArrayList<SnoConSer> scl) {
        neverGrpList = scl;
    }

    public static int sizeNeverGroup() {
        return neverGrpList.size();
    }

}
