package org.ihtsdo.concept.component.identifier;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.dwfa.ace.api.I_IdPart;

import org.ihtsdo.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.tk.api.id.LongIdBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierLong;

public class IdentifierVersionLong extends IdentifierVersion implements LongIdBI {
   private long longDenotation;

   //~--- constructors --------------------------------------------------------

   public IdentifierVersionLong() {
      super();
   }

   public IdentifierVersionLong(TkIdentifierLong idv) {
      super(idv);
      longDenotation = idv.getDenotation();
   }

   public IdentifierVersionLong(TupleInput input) {
      super(input);
      longDenotation = input.readLong();
   }

   public IdentifierVersionLong(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      super(statusNid, time, authorNid, moduleNid, pathNid);
   }

   public IdentifierVersionLong(IdentifierVersionLong another, int statusNid, long time, int authorNid,
           int moduleNid, int pathNid) {
      super(statusNid, time, authorNid, moduleNid, pathNid);
      longDenotation = (Long) another.getDenotation();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (IdentifierVersionLong.class.isAssignableFrom(obj.getClass())) {
         IdentifierVersionLong another = (IdentifierVersionLong) obj;

         return this.getStampNid() == another.getStampNid();
      }

      return false;
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }
   
   @Override
   public I_IdPart makeIdAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      return new IdentifierVersionLong(this, authorNid, time, 
              statusNid, moduleNid, pathNid);
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
      buf.append("denotation:").append(this.longDenotation);
      buf.append(" ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeSourceIdToBdb(TupleOutput output) {
      output.writeLong(longDenotation);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Long getDenotation() {
      return longDenotation;
   }

   @Override
   public IDENTIFIER_PART_TYPES getType() {
      return IDENTIFIER_PART_TYPES.LONG;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDenotation(Object sourceDenotation) {
      longDenotation = (Long) sourceDenotation;
   }
}
