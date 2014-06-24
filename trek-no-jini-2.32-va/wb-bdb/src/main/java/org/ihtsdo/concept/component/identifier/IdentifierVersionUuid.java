package org.ihtsdo.concept.component.identifier;

//~--- non-JDK imports --------------------------------------------------------
      
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.dwfa.ace.api.I_IdPart;

import org.ihtsdo.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.tk.api.id.UuidIdBI;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierUuid;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

public class IdentifierVersionUuid extends IdentifierVersion implements UuidIdBI {
   private long lsb;
   private long msb;

   //~--- constructors --------------------------------------------------------

   public IdentifierVersionUuid() {
      super();
   }

   public IdentifierVersionUuid(TkIdentifierUuid idv) {
      super(idv);
      msb = idv.getDenotation().getMostSignificantBits();
      lsb = idv.getDenotation().getLeastSignificantBits();
   }

   public IdentifierVersionUuid(TupleInput input) { 
      super(input);
      msb = input.readLong();
      lsb = input.readLong();
   }

   public IdentifierVersionUuid(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      super(statusNid, time, authorNid, moduleNid, pathNid);
   }

   public IdentifierVersionUuid(IdentifierVersionUuid another, int statusNid, long time, int authorNid,
           int moduleNid, int pathNid) {
      super(statusNid, time, authorNid, moduleNid, pathNid);
      msb = another.msb;
      lsb = another.lsb;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (IdentifierVersionUuid.class.isAssignableFrom(obj.getClass())) {
         IdentifierVersionUuid another = (IdentifierVersionUuid) obj;

         return (this.msb == another.msb) && (this.lsb == another.lsb) && super.equals(another);
      }

      return false;
   }

   @Override
   public int hashCode() {
      int hash = 3;

      hash = 97 * hash + (int) (this.msb ^ (this.msb >>> 32));

      return hash;
   }

   @Override
   public I_IdPart makeIdAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      return new IdentifierVersionUuid(this, statusNid, time, authorNid, moduleNid, pathNid);
   }

   @Override
   public final boolean readyToWriteIdentifier() {
      return true;
   }

   /*
    * (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName()).append(": ");
      buf.append("uuid:").append(getUuid());
      buf.append(" ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeSourceIdToBdb(TupleOutput output) {
      output.writeLong(msb);
      output.writeLong(lsb);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public UUID getDenotation() {
      return getUuid();
   }

   @Override
   public IDENTIFIER_PART_TYPES getType() {
      return IDENTIFIER_PART_TYPES.UUID;
   }

   public UUID getUuid() {
      return new UUID(msb, lsb);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDenotation(Object sourceDenotation) {
      if (sourceDenotation instanceof UUID) {
         UUID uuid = (UUID) sourceDenotation;

         msb = uuid.getMostSignificantBits();
         lsb = uuid.getLeastSignificantBits();
      }
   }
}
