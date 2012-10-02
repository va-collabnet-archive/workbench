/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.ihtsdo.tk.binding.snomed.Snomed;

// TODO: Auto-generated Javadoc
/**
 * The Class TkRevision.
 */
public abstract class TkRevision implements I_VersionExternally {
   
   /** The Constant serialVersionUID. */
   private static final long serialVersionUID    = 1;
   
   /** The unspecified user uuid. */
   public static UUID        unspecifiedUserUuid = UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c");
   
   /** The unspecified module uuid. */
   public static UUID        unspecifiedModuleUuid = UUID.fromString("40d1c869-b509-32f8-b735-836eac577a67");

   //~--- fields --------------------------------------------------------------

   /** The time. */
   public long time = Long.MIN_VALUE;
   
   /** The author uuid. */
   public UUID authorUuid;
   
   /** The path uuid. */
   public UUID pathUuid;
   
   /** The status uuid. */
   public UUID statusUuid;
   
   /** The module uuid. */
   public UUID moduleUuid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new tk revision.
    */
   public TkRevision() {
      super();
   }

   /**
    * Instantiates a new tk revision.
    *
    * @param componentVersion the component version
    * @throws IOException signals that an I/O exception has occurred.
    */
   public TkRevision(ComponentVersionBI componentVersion) throws IOException {
      super();
      this.statusUuid = Ts.get().getComponent(componentVersion.getStatusNid()).getPrimUuid();
      this.authorUuid = Ts.get().getComponent(componentVersion.getAuthorNid()).getPrimUuid();
      this.pathUuid   = Ts.get().getComponent(componentVersion.getPathNid()).getPrimUuid();
      this.moduleUuid   = Ts.get().getComponent(componentVersion.getModuleNid()).getPrimUuid();
      assert pathUuid != null : componentVersion;
      assert authorUuid != null : componentVersion;
      assert statusUuid != null : componentVersion;
      assert moduleUuid != null : componentVersion;
      this.time = componentVersion.getTime();
   }

   /**
    * Instantiates a new tk revision.
    *
    * @param id the id
    * @throws IOException signals that an I/O exception has occurred.
    */
   public TkRevision(IdBI id) throws IOException {
      super();
      this.authorUuid = Ts.get().getComponent(id.getAuthorNid()).getPrimUuid();
      this.pathUuid   = Ts.get().getComponent(id.getPathNid()).getPrimUuid();
      this.statusUuid = Ts.get().getComponent(id.getStatusNid()).getPrimUuid();
      this.moduleUuid   = Ts.get().getComponent(id.getModuleNid()).getPrimUuid();
      this.time       = id.getTime();
      assert pathUuid != null : id;
      assert authorUuid != null : id;
      assert statusUuid != null : id;
      assert moduleUuid != null : id;
   }

   /**
    * Instantiates a new tk revision.
    *
    * @param in the in
    * @param dataVersion the data version
    * @throws IOException signals that an I/O exception has occurred.
    * @throws ClassNotFoundException the class not found exception
    */
   public TkRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
      assert pathUuid != null : this;
      assert authorUuid != null : this;
      assert statusUuid != null : this;
      assert moduleUuid != null : this;
   }

   /**
    * Instantiates a new tk revision.
    *
    * @param componentVersion the component version
    * @param conversionMap the conversion map
    * @param offset the offset
    * @param mapAll the map all
    * @throws IOException signals that an I/O exception has occurred.
    */
   public TkRevision(ComponentVersionBI componentVersion, Map<UUID, UUID> conversionMap, long offset, boolean mapAll)
           throws IOException {
      super();

      if (mapAll) {
         this.statusUuid = conversionMap.get(Ts.get().getComponent(componentVersion.getStatusNid()).getPrimUuid());
         this.authorUuid = conversionMap.get(Ts.get().getComponent(componentVersion.getAuthorNid()).getPrimUuid());
         this.pathUuid   = conversionMap.get(Ts.get().getComponent(componentVersion.getPathNid()).getPrimUuid());
         this.moduleUuid   = conversionMap.get(Ts.get().getComponent(componentVersion.getModuleNid()).getPrimUuid());
      } else {
         this.statusUuid = Ts.get().getComponent(componentVersion.getStatusNid()).getPrimUuid();
         this.authorUuid = Ts.get().getComponent(componentVersion.getAuthorNid()).getPrimUuid();
         this.pathUuid   = Ts.get().getComponent(componentVersion.getPathNid()).getPrimUuid();
         this.moduleUuid   = Ts.get().getComponent(componentVersion.getModuleNid()).getPrimUuid();
      }

      assert pathUuid != null : componentVersion;
      assert authorUuid != null : componentVersion;
      assert statusUuid != null : componentVersion;
      assert moduleUuid != null : componentVersion;
      this.time = componentVersion.getTime() + offset;
   }

   /**
    * Instantiates a new tk revision.
    *
    * @param another the another
    * @param conversionMap the conversion map
    * @param offset the offset
    * @param mapAll the map all
    */
   public TkRevision(TkRevision another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      super();

      if (mapAll) {
         this.statusUuid = conversionMap.get(another.statusUuid);
         this.authorUuid = conversionMap.get(another.authorUuid);
         this.pathUuid   = conversionMap.get(another.pathUuid);
         this.moduleUuid   = conversionMap.get(another.moduleUuid);
      } else {
         this.statusUuid = another.statusUuid;
         this.authorUuid = another.authorUuid;
         this.pathUuid   = another.pathUuid;
         this.moduleUuid   = another.moduleUuid;
      }

      assert pathUuid != null : another;
      assert authorUuid != null : another;
      assert statusUuid != null : another;
      assert moduleUuid != null : another;
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
         
         if ((this.moduleUuid != null) && (another.moduleUuid != null)) {
            if (!this.moduleUuid.equals(another.moduleUuid)) {
               return false;
            }
         } else if (!((this.moduleUuid == null) && (another.moduleUuid == null))) {
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

   /**
    * Inform about uuid.
    *
    * @param uuid the uuid
    * @return the char sequence
    */
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

   /**
    * Make conversion.
    *
    * @param conversionMap the conversion map
    * @param offset the offset
    * @param mapAll the map all
    * @return the tk revision
    */
   public abstract TkRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll);

   /**
    * Read external.
    *
    * @param in the in
    * @param dataVersion the data version
    * @throws IOException signals that an I/O exception has occurred.
    * @throws ClassNotFoundException the class not found exception
    */
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      pathUuid   = new UUID(in.readLong(), in.readLong());
      statusUuid = new UUID(in.readLong(), in.readLong());

      if (dataVersion >= 3) {
         authorUuid = new UUID(in.readLong(), in.readLong());
      } else {
         authorUuid = unspecifiedUserUuid;
      }
      if (dataVersion >= 8) {
         moduleUuid = new UUID(in.readLong(), in.readLong());
      } else {
         moduleUuid = unspecifiedModuleUuid;
      }

      time = in.readLong();

      if (time == Long.MAX_VALUE) {
         time = Long.MIN_VALUE;
      }
   }

   /**
    * Returns a string representation of the object.
    *
    * @return the string
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(" s:");
      buff.append(informAboutUuid(this.statusUuid));
      buff.append(" t: ");
      buff.append(new Date(this.time)).append(" ").append(this.time);
      buff.append(" a:");
      buff.append(informAboutUuid(this.authorUuid));
      buff.append(" m:");
      buff.append(informAboutUuid(this.moduleUuid));
      buff.append(" p:");
      buff.append(informAboutUuid(this.pathUuid));
      

      return buff.toString();
   }

   /**
    * Write external.
    *
    * @param out the out
    * @throws IOException signals that an I/O exception has occurred.
    */
   public void writeExternal(DataOutput out) throws IOException {
      if (time == Long.MAX_VALUE) {
         time = Long.MIN_VALUE;
      }
      if(moduleUuid == null){
          System.out.println("HERE");
      }
      assert pathUuid != null : this;
      assert authorUuid != null : this;
      assert statusUuid != null : this;
      assert moduleUuid != null : this;

      out.writeLong(pathUuid.getMostSignificantBits());
      out.writeLong(pathUuid.getLeastSignificantBits());
      out.writeLong(statusUuid.getMostSignificantBits());
      out.writeLong(statusUuid.getLeastSignificantBits());
      out.writeLong(authorUuid.getMostSignificantBits());
      out.writeLong(authorUuid.getLeastSignificantBits());
      out.writeLong(moduleUuid.getMostSignificantBits());
      out.writeLong(moduleUuid.getLeastSignificantBits());
      out.writeLong(time);
   }

   //~--- get methods ---------------------------------------------------------

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.api.ext.I_VersionExternally#getAuthorUuid()
    */
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
   
   /* (non-Javadoc)
    * @see org.ihtsdo.tk.api.ext.I_VersionExternally#getModuleUuid()
    */
   public UUID getModuleUuid() {
      return moduleUuid;
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

   /**
    * Sets the author uuid.
    *
    * @param authorUuid the new author uuid
    */
   public void setAuthorUuid(UUID authorUuid) {
      this.authorUuid = authorUuid;
      assert authorUuid != null : this;
   }

   /**
    * Sets the path uuid.
    *
    * @param pathUuid the new path uuid
    */
   public void setPathUuid(UUID pathUuid) {
      this.pathUuid = pathUuid;
   }

   /**
    * Sets the status uuid.
    *
    * @param statusUuid the new status uuid
    */
   public void setStatusUuid(UUID statusUuid) {
      this.statusUuid = statusUuid;
   }
   
   /**
    * Sets the module uuid.
    *
    * @param moduleUuid the new module uuid
    */
   public void setModuleUuid(UUID moduleUuid) {
      this.moduleUuid = moduleUuid;
   }

   /**
    * Sets the time.
    *
    * @param time the new time
    */
   public void setTime(long time) {
      this.time = time;
   }

}
