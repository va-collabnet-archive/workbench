package org.ihtsdo.tk.api;

public interface NidBitSetBI {

    public boolean isMember(int nid);

    public void setMember(int nid);

    public void setNotMember(int nid);

    public void and(NidBitSetBI other);

    public void or(NidBitSetBI other);

    public NidBitSetItrBI iterator();

    /**
     * 
     * @return number of set bits. 
     */
    public int cardinality();

    public int totalBits();

    public void clear();

	void andNot(NidBitSetBI other);

	void union(NidBitSetBI other);

	void xor(NidBitSetBI other);

}
