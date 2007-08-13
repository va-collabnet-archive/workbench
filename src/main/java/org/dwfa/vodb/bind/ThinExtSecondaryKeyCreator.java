package org.dwfa.vodb.bind;

import org.dwfa.vodb.types.ThinExtVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class ThinExtSecondaryKeyCreator implements SecondaryKeyCreator {
   public enum KEY_TYPE { REFSET_ID, COMPONENT_ID };

   private static ThinExtBinder fixedOnlyBinder = new ThinExtBinder(true);
   
   private static TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);
   
   private KEY_TYPE keyType;

   public static class MemberAndSecondaryId {
      int memberId;
      int secondaryId;
      public MemberAndSecondaryId() {
         super();
      }
      
      public MemberAndSecondaryId(int secondaryId, int memberId) {
         super();
         this.memberId = memberId;
         this.secondaryId = secondaryId;
      }
      public int getSecondaryId() {
         return secondaryId;
      }
      public void setSecondaryId(int c1Id) {
         this.secondaryId = c1Id;
      }
      public int getMemberId() {
         return memberId;
      }
      public void setMemberId(int relId) {
         this.memberId = relId;
      }
   }
   public static class MemberAndSecondaryIdBinding extends TupleBinding {

      public MemberAndSecondaryId entryToObject(TupleInput ti) {
         return new MemberAndSecondaryId(ti.readInt(),ti.readInt());
      }

      public void objectToEntry(Object obj, TupleOutput to) {
         MemberAndSecondaryId id = (MemberAndSecondaryId) obj;
         to.writeInt(id.getSecondaryId());
         to.writeInt(id.getMemberId());
      }

   }
   
   MemberAndSecondaryId memberAndSecondaryId = new MemberAndSecondaryId();
   MemberAndSecondaryIdBinding relAndC1IdBinding = new MemberAndSecondaryIdBinding();

   public ThinExtSecondaryKeyCreator(KEY_TYPE keyType) {
      super();
      this.keyType = keyType;
   }

   public boolean createSecondaryKey(SecondaryDatabase secDb,
         DatabaseEntry keyEntry,
         DatabaseEntry dataEntry,
         DatabaseEntry resultEntry) throws DatabaseException {
      ThinExtVersioned core = (ThinExtVersioned) fixedOnlyBinder.entryToObject(dataEntry);
      switch (keyType) {
     case REFSET_ID:
        intBinder.objectToEntry(core.getRefsetId(), resultEntry);
         break;
     case COMPONENT_ID:
        intBinder.objectToEntry(core.getComponentId(), resultEntry);
        break;

      default:
        throw new RuntimeException("Can't handle keytype:" + keyType);
      }
      intBinder.objectToEntry(core.getMemberId(), resultEntry);      
      return true;
   }
   public synchronized boolean createSecondaryKey(int memberId, 
         int c1id,
         DatabaseEntry resultEntry)
      throws DatabaseException {
   memberAndSecondaryId.setSecondaryId(c1id);
   memberAndSecondaryId.setMemberId(memberId);
   relAndC1IdBinding.objectToEntry(memberAndSecondaryId, resultEntry);
   return true;
}

}
