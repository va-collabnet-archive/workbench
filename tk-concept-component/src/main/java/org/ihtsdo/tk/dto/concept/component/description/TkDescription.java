package org.ihtsdo.tk.dto.concept.component.description;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.ext.I_DescribeExternally;
import org.ihtsdo.tk.dto.concept.UtfHelper;
import org.ihtsdo.tk.dto.concept.component.TkComponent;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;

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

   public TkDescription(DescriptionChronicleBI desc) throws IOException {
      super(desc.getPrimordialVersion());

      Collection<? extends DescriptionVersionBI> versions  = desc.getVersions();
      Iterator<? extends DescriptionVersionBI>   itr       = versions.iterator();
      TerminologyStoreDI                         ts        = Ts.get();
      int                                        partCount = versions.size();
      DescriptionVersionBI                       part      = itr.next();

      conceptUuid            = ts.getUuidPrimordialForNid(desc.getConceptNid());
      initialCaseSignificant = part.isInitialCaseSignificant();
      lang                   = part.getLang();
      text                   = part.getText();
      typeUuid               = ts.getUuidPrimordialForNid(part.getTypeNid());
      pathUuid               = ts.getUuidPrimordialForNid(part.getPathNid());
      statusUuid             = ts.getUuidPrimordialForNid(part.getStatusNid());
      time                   = part.getTime();

      if (partCount > 1) {
         revisions = new ArrayList<TkDescriptionRevision>(partCount - 1);

         while (itr.hasNext()) {
             DescriptionVersionBI dv = itr.next();
            revisions.add(new TkDescriptionRevision(dv));
         }
      }
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

   public TkDescription(DescriptionVersionBI another, NidBitSetBI exclusions, Map<UUID, UUID> conversionMap,
                        long offset, boolean mapAll, ViewCoordinate vc)
           throws IOException, ContradictionException {
      super(another, exclusions, conversionMap, offset, mapAll, vc);

      if (mapAll) {
         this.conceptUuid = conversionMap.get(Ts.get().getComponent(another.getConceptNid()).getPrimUuid());
         this.typeUuid    = conversionMap.get(Ts.get().getComponent(another.getTypeNid()).getPrimUuid());
      } else {
         this.conceptUuid = Ts.get().getComponent(another.getConceptNid()).getPrimUuid();
         this.typeUuid    = Ts.get().getComponent(another.getTypeNid()).getPrimUuid();
      }

      this.initialCaseSignificant = another.isInitialCaseSignificant();
      this.lang                   = another.getLang();
      this.text                   = another.getText();
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
