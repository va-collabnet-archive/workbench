package org.dwfa.vodb.types;

import java.util.ArrayList;
import java.util.List;

/**
 * @todo add version to vodb -> added as getProperty...
 * @todo add imported change set info to vodb, need to set theProperty...
 * 
 * @todo have change sets automatically increment as size increases over a certain size. 
 * Added increment to change set file name format. 
 * @todo add extension ability
 * 
 * @author kec
 *
 */
public class ThinExtVersioned {
   
   private int refsetId;
   private int memberId;
   private int componentId;
   private int typeId; //Use an enumeration when reading/writing, and convert it to the corresponding concept nid...
   private List<ThinExtPart> versions ;
   
   public ThinExtVersioned(int refsetId, int memberId, int componentId, int typeId) {
      this(refsetId, memberId, componentId, typeId, 1);
   }
   
   public ThinExtVersioned(int refsetId, int memberId, int componentId, int typeId, int partCount) {
      super();
      this.refsetId = refsetId;
      this.memberId = memberId;
      this.componentId = componentId;
      this.typeId = typeId;
      this.versions = new ArrayList<ThinExtPart>(partCount);
   }
   
   public int getMemberId() {
      return memberId;
   }

   public int getComponentId() {
      return componentId;
   }

   public int getTypeId() {
      return typeId;
   }

   public List<? extends ThinExtPart> getVersions() {
      return versions;
   }

   public int getRefsetId() {
      return refsetId;
   }
   @Override
   public boolean equals(Object obj) {
      ThinExtVersioned another = (ThinExtVersioned) obj;
      return ((refsetId == another.refsetId) &&
            (memberId == another.memberId) &&
            (componentId == another.componentId) &&
            (typeId == another.typeId) &&
            (versions.equals(another.versions)));
   }
   @Override
   public int hashCode() {
      return HashFunction.hashCode(new int[] {refsetId, memberId, componentId, typeId });
   }

   public void addVersion(ThinExtPart part) {
      versions.add(part);
   }

}
