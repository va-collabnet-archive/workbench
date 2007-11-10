package org.dwfa.ace.api.ebr;

import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;

public interface I_ThinExtByRefVersioned {

   public int getMemberId();

   public int getComponentId();

   public int getTypeId();

   public List<? extends I_ThinExtByRefPart> getVersions();

   public int getRefsetId();

   public void addVersion(I_ThinExtByRefPart part);

   public void setRefsetId(int refsetId);

   public void setTypeId(int typeId);

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_RelVersioned#addTuples(org.dwfa.ace.IntSet,
    *      org.dwfa.ace.IntSet, java.util.Set, java.util.List, boolean)
    */
   public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions,
         List<I_ThinExtByRefTuple> returnTuples, boolean addUncommitted);

}