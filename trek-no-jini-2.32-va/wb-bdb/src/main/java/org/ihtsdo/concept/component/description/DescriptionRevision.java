package org.ihtsdo.concept.component.description;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.nio.charset.Charset;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

public class DescriptionRevision extends Revision<DescriptionRevision, Description>
        implements I_DescriptionPart<DescriptionRevision>, DescriptionAnalogBI<DescriptionRevision> {
   @SuppressWarnings("unused")
   private static Charset utf8 = Charset.forName("UTF-8");

   //~--- fields --------------------------------------------------------------

   private boolean initialCaseSignificant;
   private String  lang;
   private String  text;
   private int     typeNid;

   //~--- constructors --------------------------------------------------------

   public DescriptionRevision() {
      super();
   }

   protected DescriptionRevision(Description primoridalMember) {
      super(primoridalMember.primordialSapNid, primoridalMember);
      this.text                   = primoridalMember.getText();
      this.typeNid                = primoridalMember.typeNid;
      this.lang                   = primoridalMember.getLang();
      this.initialCaseSignificant = primoridalMember.isInitialCaseSignificant();
   }

   protected DescriptionRevision(DescriptionRevision another, Description primoridalMember) {
      super(another.getStatusAtPositionNid(), primoridalMember);
      this.text                   = another.text;
      this.typeNid                = another.typeNid;
      this.lang                   = another.lang;
      this.initialCaseSignificant = another.initialCaseSignificant;
   }

   public DescriptionRevision(int statusAtPositionNid, Description primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
   }

   public DescriptionRevision(TkDescriptionRevision edv, Description primoridalMember)
           throws TerminologyException, IOException {
      super(Bdb.uuidToNid(edv.getStatusUuid()),edv.getTime(), Terms.get().getAuthorNid(),
              Bdb.uuidToNid(edv.getModuleUuid()), Bdb.uuidToNid(edv.getPathUuid()),primoridalMember);
      initialCaseSignificant = edv.isInitialCaseSignificant();
      lang                   = edv.getLang();
      text                   = edv.getText();
      typeNid                = Bdb.uuidToNid(edv.getTypeUuid());
      sapNid                 = Bdb.getSapNid(edv);
   }

   protected DescriptionRevision(TupleInput input, Description primoridalMember) {
      super(input, primoridalMember);
      text = input.readString();

      if (text == null) {
         text = primoridalMember.getText();
      }

      lang = input.readString();

      if (lang == null) {
         lang = primoridalMember.getLang();
      }

      initialCaseSignificant = input.readBoolean();
      typeNid                = input.readInt();
   }

   public DescriptionRevision(UniversalAceDescriptionPart umPart, Description primoridalMember, int authorNid, int moduleNid)
           throws TerminologyException, IOException {
      super(Bdb.uuidsToNid(umPart.getStatusId()), umPart.getTime(), authorNid,
            moduleNid, Bdb.uuidsToNid(umPart.getPathId()),primoridalMember);
      text                   = umPart.getText();
      lang                   = umPart.getLang();
      initialCaseSignificant = umPart.getInitialCaseSignificant();
      typeNid                = Bdb.uuidsToNid(umPart.getTypeId());
   }

   protected DescriptionRevision(I_DescriptionPart another, int statusNid, long time, int authorNid, int moduleNid, int pathNid, Description primoridalMember) {
      super(statusNid, time, authorNid, moduleNid, pathNid, primoridalMember);
      this.text                   = another.getText();
      this.typeNid                = another.getTypeNid();
      this.lang                   = another.getLang();
      this.initialCaseSignificant = another.isInitialCaseSignificant();
   }

   //~--- methods -------------------------------------------------------------
   
   @Override
    public boolean matches(Pattern p) {
        String lastText = null;

        for (Description.Version desc : getVersions()) {
            if (!desc.getText().equals(lastText)) {
                lastText = desc.getText();

                Matcher m = p.matcher(lastText);

                if (m.find()) {
                    return true;
                }
            }
        }

        return false;
    }
   
   @Override
   protected void addComponentNids(Set<Integer> allNids) {
      allNids.add(typeNid);
   }

   @Override
   public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
      throw new UnsupportedOperationException();
   }

   @Override
   public DescriptionRevision duplicate() {
      return new DescriptionRevision(this, this.primordialComponent);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (DescriptionRevision.class.isAssignableFrom(obj.getClass())) {
         DescriptionRevision another = (DescriptionRevision) obj;

         return this.sapNid == another.sapNid;
      }

      return false;
   }
   
   @Override
   public DescriptionRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      DescriptionRevision newR;

      newR = new DescriptionRevision(this, statusNid, time, authorNid,
                                     moduleNid, pathNid,this.primordialComponent);
      this.primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRevision() {
      assert text != null : assertionString();
      assert lang != null : assertionString();
      assert typeNid != Integer.MAX_VALUE : assertionString();

      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();

      buf.append(this.getClass().getSimpleName()).append(":{");
      buf.append(" text:" + "'").append(this.getText()).append("'");
      buf.append(" initialCaseSignificant:").append(isInitialCaseSignificant());
      buf.append(" typeNid:");
      ConceptComponent.addNidToBuffer(buf, typeNid);
      buf.append(" lang:").append(this.getLang());
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   public String toUserString() {
      StringBuffer buf = new StringBuffer();

      ConceptComponent.addTextToBuffer(buf, typeNid);
      buf.append(": ");
      buf.append("'").append(this.getText()).append("'");

      return buf.toString();
   }

   /**
    * Test method to check to see if two objects are equal in all respects.
    * @param another
    * @return either a zero length String, or a String containing a description of the
    * validation failures.
    * @throws IOException
    */
   public String validate(DescriptionRevision another) throws IOException {
      assert another != null;

      StringBuilder buf = new StringBuilder();

      if (this.initialCaseSignificant != another.initialCaseSignificant) {
         buf.append(
             "\tDescriptionRevision.initialCaseSignificant not equal: \n"
             + "\t\tthis.initialCaseSignificant = ").append(this.initialCaseSignificant).append(
                 "\n" + "\t\tanother.initialCaseSignificant = ").append(
                 another.initialCaseSignificant).append("\n");
      }

      if (!this.text.equals(another.text)) {
         buf.append("\tDescriptionRevision.text not equal: \n" + "\t\tthis.text = " + this.text + "\n"
                    + "\t\tanother.text = " + another.text + "\n");
      }

      if (!this.lang.equals(another.lang)) {
         buf.append("\tDescriptionRevision.lang not equal: \n" + "\t\tthis.lang = " + this.lang + "\n"
                    + "\t\tanother.lang = " + another.lang + "\n");
      }

      if (this.typeNid != another.typeNid) {
         buf.append("\tDescriptionRevision.typeNid not equal: \n" + "\t\tthis.typeNid = " + this.typeNid
                    + "\n" + "\t\tanother.typeNid = " + another.typeNid + "\n");
      }

      // Compare the parents
      buf.append(super.validate(another));

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      if (text.equals(primordialComponent.getText())) {
         output.writeString((String) null);
      } else {
         output.writeString(text);
      }

      if (lang.equals(primordialComponent.getLang())) {
         output.writeString((String) null);
      } else {
         output.writeString(lang);
      }

      output.writeBoolean(initialCaseSignificant);
      output.writeInt(typeNid);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getConceptNid() {
      return primordialComponent.getConceptNid();
   }
   
   @Override
    public DescriptionCAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB {
        DescriptionCAB descBp = new DescriptionCAB(getConceptNid(), getTypeNid(),
                LANG_CODE.getLangCode(lang), getText(), initialCaseSignificant,
                getVersion(vc), vc);
        return descBp;
    }

   @Override
   public String getLang() {
      return lang;
   }

   @Override
   public Description getPrimordialVersion() {
      return primordialComponent;
   }

   @Override
   public String getText() {
      return text;
   }

   @Override
   @Deprecated
   public int getTypeId() {
      return typeNid;
   }

   @Override
   public int getTypeNid() {
      return typeNid;
   }

   @Override
   public ArrayIntList getVariableVersionNids() {
      ArrayIntList list = new ArrayIntList(3);

      list.add(typeNid);

      return list;
   }

   @Override
   public Description.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return primordialComponent.getVersion(c);
   }

   @Override
   public Collection<Description.Version> getVersions() {
      return ((Description) primordialComponent).getVersions();
   }

   @Override
   public Collection<Description.Version> getVersions(ViewCoordinate c) {
      return primordialComponent.getVersions(c);
   }

   @Override
   public boolean isInitialCaseSignificant() {
      return initialCaseSignificant;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setInitialCaseSignificant(boolean capStatus) {
      initialCaseSignificant = capStatus;
      modified();
   }

   @Override
   public void setLang(String lang) {
      this.lang = lang;
      modified();
   }

   @Override
   public void setText(String text) {
      this.text = text;
      modified();
   }

   @Override
   @Deprecated
   public void setTypeId(int typeNid) {
      this.typeNid = typeNid;
      modified();
   }

   public void setTypeNid(int typeNid) {
      this.typeNid = typeNid;
      modified();
   }
}
