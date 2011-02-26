package org.ihtsdo.db.util;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.util.HashFunction;
import org.ihtsdo.cern.colt.list.IntArrayList;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.NidSetBI;

public abstract class NidPair {

    protected int nid1;
    protected int nid2;
    private int hash;

    protected NidPair(long nids) {
        super();
    }

    public static NidPair getNidPair(long nids) {
        int nid1 = (int) nids;
        int nid2 = (int) (nids >>> 32);
        if (Bdb.getConceptNid(nid2) == nid2) {
            return getTypeNidRelNidPair(nid2, nid1);
        }
        return getRefsetNidMemberNidPair(nid1, nid2);
    }

    public static List<NidPairForRel> getNidPairsForRel(long[] nidPairArray) {
        List<NidPairForRel> returnValues = new ArrayList<NidPairForRel>(nidPairArray.length);
        for (long nids : nidPairArray) {
            int nid1 = (int) nids;
            int nid2 = (int) (nids >>> 32);
            if (Bdb.nidCidMapDb.getCNid(nid2) == nid2) {
                returnValues.add(new NidPairForRel(nid1, nid2));
            }
        }
        return returnValues;
    }

    public static List<NidPairForRel> getNidPairsForRel(long[] nidPairArray,
            NidSetBI relTypes) {
        List<NidPairForRel> returnValues = new ArrayList<NidPairForRel>(nidPairArray.length);
        for (long nids : nidPairArray) {
            int nid1 = (int) nids;
            int nid2 = (int) (nids >>> 32);
            if (relTypes.contains(nid2)) {
                returnValues.add(new NidPairForRel(nid1, nid2));
            }
        }
        return returnValues;
    }

    public static int[] getOriginsForRels(long[] nidPairArray,
            NidSetBI relTypes) {
        IntArrayList returnValues = new IntArrayList(nidPairArray.length);
        for (long nids : nidPairArray) {
            int nid1 = (int) nids;
            int nid2 = (int) (nids >>> 32);
            if (relTypes.contains(nid2)) {
                returnValues.add(Bdb.nidCidMapDb.getCNid(nid1));
            }
        }
        returnValues.trimToSize();
        return returnValues.elements();
    }

    public static List<NidPairForRefset> getNidPairsForRefset(long[] nidPairArray) {
        List<NidPairForRefset> returnValues = new ArrayList<NidPairForRefset>(nidPairArray.length);
        for (long nids : nidPairArray) {
            int nid1 = (int) nids;
            int nid2 = (int) (nids >>> 32);
            if (Bdb.nidCidMapDb.getCNid(nid2) != nid2) {
                returnValues.add(new NidPairForRefset(nid1, nid2));
            }
        }
        return returnValues;
    }

    public static NidPairForRel getTypeNidRelNidPair(int typeNid, int rNid) {
        // the type (nid2) is a concept, the rNid is not. 
        return new NidPairForRel(rNid, typeNid);
    }

    public static NidPairForRefset getRefsetNidMemberNidPair(int refsetNid, int memberNid) {
        // the refset (nid1) is a concept, the memberNid is not. 
        return new NidPairForRefset(refsetNid, memberNid);
    }

    public abstract boolean isRelPair();

    protected NidPair(int nid1, int nid2) {
        super();
        this.nid1 = nid1;
        this.nid2 = nid2;
        HashFunction.hashCode(new int[]{nid1, nid2});
    }

    public long asLong() {
        long returnValue = nid2;
        returnValue = returnValue & 0x00000000FFFFFFFFL;
        long nid1Long = nid1;
        nid1Long = nid1Long & 0x00000000FFFFFFFFL;
        returnValue = returnValue << 32;
        returnValue = returnValue | nid1Long;
        return returnValue;
    }

    @Override
    public boolean equals(Object obj) {
        NidPair another = (NidPair) obj;
        return this.nid1 == another.nid1 && this.nid2 == another.nid2;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "nid1: " + nid1 + " nid2:" + nid2;
    }

    public void addToList(List<Integer> list) {
        list.add(nid1);
        list.add(nid2);
    }

    public boolean isRefsetPair() {
        return !isRelPair();
    }
}
