package org.ihtsdo.db.util;

import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.cern.colt.list.IntArrayList;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.hash.Hashcode;

public abstract class NidPair implements Comparable<NidPair> {

    protected int nid1;
    protected int nid2;
    private int hash;

    public static NidPair getNidPair(long nids) {
        int nid1 = (int) nids;
        int nid2 = (int) (nids >>> 32);
        return getRefexNidMemberNidPair(nid1, nid2);
    }

    public static int[] getOriginsForRels(long[] nidPairArray, 
            NidSetBI relTypes) {
        IntArrayList returnValues = new IntArrayList(nidPairArray.length);
        for (long nids: nidPairArray) {
            int nid1 = (int) nids;
            int nid2 = (int) (nids >>> 32);
            if (relTypes.contains(nid2)) {
                returnValues.add(Bdb.nidCidMapDb.getCNid(nid1));
            }
        }
        returnValues.trimToSize();
        return returnValues.elements();
    }

   public static List<NidPairForRefex> getNidPairsForRefset(long[] nidPairArray) {
        List<NidPairForRefex> returnValues = new ArrayList<NidPairForRefex>(nidPairArray.length);
        for (long nids: nidPairArray) {
            int nid1 = (int) nids;
            int nid2 = (int) (nids >>> 32);
            if (Bdb.nidCidMapDb.getCNid(nid2) != nid2) {
                returnValues.add(new NidPairForRefex(nid1, nid2));
            }
        }
        return returnValues;
    }

    public static NidPairForRefex getRefexNidMemberNidPair(int refsetNid, int memberNid) {
        // the refset (nid1) is a concept, the memberNid is not. 
        return new NidPairForRefex(refsetNid, memberNid);
    }

    public abstract boolean isRelPair();

    protected NidPair(long nids) {
        this((int) nids, (int) (nids >>> 32));
    }

    protected NidPair(int nid1, int nid2) {
        super();
        assert nid1 != 0;
        assert nid2 != 0;
        this.nid1 = nid1;
        this.nid2 = nid2;
        this.hash = Hashcode.compute(new int[]{nid1, nid2});
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
        if (obj.getClass().isAssignableFrom(NidPair.class)) {
            NidPair another = (NidPair) obj;
            return this.nid1 == another.nid1 && this.nid2 == another.nid2;
        }
        return false;
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

    @Override
    public int compareTo(NidPair o) {
        long diff = asLong() - o.asLong();
        if (diff > 0) {
            return 1;
        }
        if (diff < 0) {
            return -1;
        }
        return 0;
    }
}
