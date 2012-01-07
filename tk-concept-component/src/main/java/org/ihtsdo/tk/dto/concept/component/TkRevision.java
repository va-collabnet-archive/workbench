package org.ihtsdo.tk.dto.concept.component;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.ext.I_VersionExternally;
import org.ihtsdo.tk.api.id.IdBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class TkRevision implements I_VersionExternally {
   private static final long serialVersionUID    = 1;
   public static UUID        unspecifiedUserUuid = UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c");

   //~--- fields --------------------------------------------------------------

   public long time = Long.MIN_VALUE;
   public UUID authorUuid;
   public UUID pathUuid;
   public UUID statusUuid;

   //~--- constructors --------------------------------------------------------

   public TkRevision() {
      super();
   }

   public TkRevision(ComponentVersionBI another) throws IOException {
      super();
      this.statusUuid = Ts.get().getComponent(another.getStatusNid()).getPrimUuid();
      this.authorUuid = Ts.get().getComponent(another.getAuthorNid()).getPrimUuid();
      this.pathUuid   = Ts.get().getComponent(another.getPathNid()).getPrimUuid();
      assert pathUuid != null : another;
      assert authorUuid != null : another;
      assert statusUuid != null : another;
      this.time = another.getTime();
   }

   public TkRevision(IdBI id) throws IOException {
      super();
      this.authorUuid = Ts.get().getComponent(id.getAuthorNid()).getPrimUuid();
      this.pathUuid   = Ts.get().getComponent(id.getPathNid()).getPrimUuid();
      this.statusUuid = Ts.get().getComponent(id.getStatusNid()).getPrimUuid();
      this.time       = id.getTime();
      assert pathUuid != null : id;
      assert authorUuid != null : id;
      assert statusUuid != null : id;
   }

   public TkRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
      assert pathUuid != null : this;
      assert authorUuid != null : this;
      assert statusUuid != null : this;
   }

   public TkRevision(ComponentVersionBI another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll)
           throws IOException {
      super();

      if (mapAll) {
         this.statusUuid = conversionMap.get(Ts.get().getComponent(another.getStatusNid()).getPrimUuid());
         this.authorUuid = conversionMap.get(Ts.get().getComponent(another.getAuthorNid()).getPrimUuid());
         this.pathUuid   = conversionMap.get(Ts.get().getComponent(another.getPathNid()).getPrimUuid());
      } else {
         this.statusUuid = Ts.get().getComponent(another.getStatusNid()).getPrimUuid();
         this.authorUuid = Ts.get().getComponent(another.getAuthorNid()).getPrimUuid();
         this.pathUuid   = Ts.get().getComponent(another.getPathNid()).getPrimUuid();
      }

      assert pathUuid != null : another;
      assert authorUuid != null : another;
      assert statusUuid != null : another;
      this.time = another.getTime() + offset;
   }

   public TkRevision(TkRevision another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      super();

      if (mapAll) {
         this.statusUuid = conversionMap.get(another.statusUuid);
         this.authorUuid = conversionMap.get(another.authorUuid);
         this.pathUuid   = conversionMap.get(another.pathUuid);
      } else {
         this.statusUuid = another.statusUuid;
         this.authorUuid = another.authorUuid;
         this.pathUuid   = another.pathUuid;
      }

      assert pathUuid != null : another;
      assert authorUuid != null : another;
      assert statusUuid != null : another;
      this.time = another.time + offset;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EVersion</tt> object, and contains the same values,
    * field by field, as this <tt>EVersion</tt>.
    *
    * @param obj the object to compare with.
    * @return <code>true</code> if the objects are the same;
    *         <code>false</code> otherwise.
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (TkRevision.class.isAssignableFrom(obj.getClass())) {
         TkRevision another = (TkRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         if (!this.statusUuid.equals(another.statusUuid)) {
            return false;
         }

         if ((this.authorUuid != null) && (another.authorUuid != null)) {
            if (!this.authorUuid.equals(another.authorUuid)) {
               return false;
            }
         } else if (!((this.authorUuid == null) && (another.authorUuid == null))) {
            return false;
         }

         if (!this.pathUuid.equals(another.pathUuid)) {
            return false;
         }

         if (this.time != another.time) {
            return false;
         }

         // Objects are equal! (Don't climb any higher in the hierarchy)
         return true;
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>EVersion</code>.
    *
    * @return a hash code value for this <tt>EVersion</tt>.
    */
   @Override
   public int hashCode() {
      return Arrays.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time,
                                         (int) (time >>> 32) });
   }

   public static CharSequence informAboutUuid(UUID uuid) {
      if (Ts.get() == null) {
          if (uuid == null) {
              return "null uuid";
          }
         return uuid.toString();
      }

      StringBuilder sb = new StringBuilder();

      if (uuid != null && Ts.get().hasUuid(uuid)) {
         try {
            int nid  = Ts.get().getNidForUuids(uuid);
            int cNid = Ts.get().getConceptNidForNid(nid);

            if (cNid == nid) {
               ConceptChronicleBI cc = Ts.get().getConcept(cNid);

               sb.append("'");
               sb.append(cc.toUserString());
               sb.append("' ");
               sb.append(cNid);
               sb.append(" ");
            } else {
               ComponentBI component = Ts.get().getComponent(nid);

               sb.append("comp: '");

               if (component != null) {
                  sb.append(component.toUserString());
               } else {
                  sb.append("null");
               }

               sb.append("' ");
               sb.append(nid);
               sb.append(" ");
            }
         } catch (IOException ex) {
            Logger.getLogger(TkRevision.class.getName()).log(Level.SEVERE, null, ex);
         }
         sb.append(uuid.toString());
      } else {
          sb.append("null uuid");
      }
      
      

      return sb;
   }

   public abstract TkRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll);

   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      pathUuid   = new UUID(in.readLong(), in.readLong());
      statusUuid = new UUID(in.readLong(), in.readLong());

      if (dataVersion >= 3) {
         authorUuid = new UUID(in.readLong(), in.readLong());
      } else {
         authorUuid = unspecifiedUserUuid;
      }

      time = in.readLong();

      if (time == Long.MAX_VALUE) {
         time = Long.MIN_VALUE;
      }
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(" s:");
      buff.append(informAboutUuid(this.statusUuid));
      buff.append(" a:");
      buff.append(informAboutUuid(this.authorUuid));
      buff.append(" p:");
      buff.append(informAboutUuid(this.pathUuid));
      buff.append(" t: ");
      buff.append(new Date(this.time)).append(" ").append(this.time);

      return buff.toString();
   }

   public void writeExternal(DataOutput out) throws IOException {
      if (time == Long.MAX_VALUE) {
         time = Long.MIN_VALUE;
      }
      assert pathUuid != null : this;
      assert authorUuid != null : this;
      assert statusUuid != null : this;

      out.writeLong(pathUuid.getMostSignificantBits());
      out.writeLong(pathUuid.getLeastSignificantBits());
      out.writeLong(statusUuid.getMostSignificantBits());
      out.writeLong(statusUuid.getLeastSignificantBits());
      out.writeLong(authorUuid.getMostSignificantBits());
      out.writeLong(authorUuid.getLeastSignificantBits());
      out.writeLong(time);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public UUID getAuthorUuid() {
      return authorUuid;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.etypes.I_VersionExternal#getPathUuid()
    */
   @Override
   public UUID getPathUuid() {
      return pathUuid;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.etypes.I_VersionExternal#getStatusUuid()
    */
   @Override
   public UUID getStatusUuid() {
      return statusUuid;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.etypes.I_VersionExternal#getTime()
    */
   @Override
   public long getTime() {
      return time;
   }

   //~--- set methods ---------------------------------------------------------

   public void setAuthorUuid(UUID authorUuid) {
      this.authorUuid = authorUuid;
      assert authorUuid != null : this;
   }

   public void setPathUuid(UUID pathUuid) {
      this.pathUuid = pathUuid;
   }

   public void setStatusUuid(UUID statusUuid) {
      this.statusUuid = statusUuid;
   }

   public void setTime(long time) {
      this.time = time;
   }
}
