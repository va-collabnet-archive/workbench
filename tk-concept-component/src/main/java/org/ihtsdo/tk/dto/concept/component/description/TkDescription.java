package org.ihtsdo.tk.dto.concept.component.description;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.ext.I_DescribeExternally;
import org.ihtsdo.tk.dto.concept.UtfHelper;
import org.ihtsdo.tk.dto.concept.component.TkComponent;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TkDescription extends TkComponent<TkDescriptionRevision> implements I_DescribeExternally {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public UUID    conceptUuid;
   public boolean initialCaseSignificant;
   public String  lang;
   public String  text;
   public UUID    typeUuid;

   //~--- constructors --------------------------------------------------------

   public TkDescription() {
      super();
   }

   public TkDescription(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkDescription(TkDescription another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.conceptUuid = conversionMap.get(another.conceptUuid);
         this.typeUuid    = conversionMap.get(another.typeUuid);
      } else {
         this.conceptUuid = another.conceptUuid;
         this.typeUuid    = another.typeUuid;
      }

      this.initialCaseSignificant = another.initialCaseSignificant;
      this.lang                   = another.lang;
      this.text                   = another.text;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EDescription</tt> object, and contains the same values, field by field,
    * as this <tt>EDescription</tt>.
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

      if (TkDescription.class.isAssignableFrom(obj.getClass())) {
         TkDescription another = (TkDescription) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare conceptUuid
         if (!this.conceptUuid.equals(another.conceptUuid)) {
            return false;
         }

         // Compare initialCaseSignificant
         if (this.initialCaseSignificant != another.initialCaseSignificant) {
            return false;
         }

         // Compare lang
         if (!this.lang.equals(another.lang)) {
            return false;
         }

         // Compare text
         if (!this.text.equals(another.text)) {
            return false;
         }

         // Compare typeUuid
         if (!this.typeUuid.equals(another.typeUuid)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   @Override
   public TkDescription makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkDescription(this, conversionMap, offset, mapAll);
   }

   @Override
   public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      conceptUuid            = new UUID(in.readLong(), in.readLong());
      initialCaseSignificant = in.readBoolean();
      lang                   = in.readUTF();
      text                   = UtfHelper.readUtfV6(in, dataVersion);
      typeUuid               = new UUID(in.readLong(), in.readLong());

      int versionLength = in.readInt();

      if (versionLength > 0) {
         revisions = new ArrayList<TkDescriptionRevision>(versionLength);

         for (int i = 0; i < versionLength; i++) {
            revisions.add(new TkDescriptionRevision(in, dataVersion));
         }
      }
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append("'").append(this.text).append("'");
      buff.append(" concept:");
      buff.append(informAboutUuid(this.conceptUuid));
      buff.append(" ics:");
      buff.append(this.initialCaseSignificant);
      buff.append(" lang:");
      buff.append("'").append(this.lang).append("'");
      buff.append(" type:");
      buff.append(informAboutUuid(this.typeUuid));
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(conceptUuid.getMostSignificantBits());
      out.writeLong(conceptUuid.getLeastSignificantBits());
      out.writeBoolean(initialCaseSignificant);
      out.writeUTF(lang);
      UtfHelper.writeUtf(out, text);
      out.writeLong(typeUuid.getMostSignificantBits());
      out.writeLong(typeUuid.getLeastSignificantBits());

      if (revisions == null) {
         out.writeInt(0);
      } else {
         out.writeInt(revisions.size());

         for (TkDescriptionRevision edv : revisions) {
            edv.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getConceptUuid() {
      return conceptUuid;
   }

   @Override
   public String getLang() {
      return lang;
   }

   @Override
   public List<TkDescriptionRevision> getRevisionList() {
      return revisions;
   }

   @Override
   public String getText() {
      return text;
   }

   @Override
   public UUID getTypeUuid() {
      return typeUuid;
   }

   @Override
   public boolean isInitialCaseSignificant() {
      return initialCaseSignificant;
   }

   //~--- set methods ---------------------------------------------------------

   public void setConceptUuid(UUID conceptUuid) {
      this.conceptUuid = conceptUuid;
   }

   public void setInitialCaseSignificant(boolean initialCaseSignificant) {
      this.initialCaseSignificant = initialCaseSignificant;
   }

   public void setLang(String lang) {
      this.lang = lang;
   }

   public void setText(String text) {
      this.text = text;
   }

   public void setTypeUuid(UUID typeUuid) {
      this.typeUuid = typeUuid;
   }
}
