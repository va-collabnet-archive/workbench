package org.ihtsdo.tk.api;

public interface NidBitSetBI {
   public void and(NidBitSetBI other);

   void andNot(NidBitSetBI other);

   /**
    *
    * @return number of set bits.
    */
   public int cardinality();

   public void clear();

   public NidBitSetItrBI iterator();

   public void or(NidBitSetBI other);

   public int totalBits();

   void union(NidBitSetBI other);

   void xor(NidBitSetBI other);

   //~--- get methods ---------------------------------------------------------

   public boolean isMember(int nid);

   //~--- set methods ---------------------------------------------------------

   public void setMember(int nid);

   public void setNotMember(int nid);
}
