package org.ihtsdo.concept.component.refsetmember.strStr;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.*;
import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStrStr;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.concept.component.refsetmember.str.*;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringAnalogBI;
import org.ihtsdo.tk.api.refex.type_string_string.RefexStringStringAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_string_string.TkRefsetStrStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string_string.TkRefsetStrStrRevision;

public class StrStrRevision extends RefsetRevision<StrStrRevision, StrStrMember>
        implements I_ExtendByRefPartStrStr<StrStrRevision>, RefexStringStringAnalogBI<StrStrRevision> {
   private String string1Value;
   private String string2Value;

   //~--- constructors --------------------------------------------------------

   public StrStrRevision() {
      super();
   }

   public StrStrRevision(int statusAtPositionNid, StrStrMember another) {
      super(statusAtPositionNid, another);
      string1Value = another.getString1Value();
      string2Value = another.getString2Value();
   }

   public StrStrRevision(TkRefsetStrStrRevision eVersion, StrStrMember primoridalMember) {
      super(eVersion, primoridalMember);
      this.string1Value = eVersion.getString1();
      this.string2Value = eVersion.getString2();
   }

   public StrStrRevision(TupleInput input, StrStrMember primoridalMember) {
      super(input, primoridalMember);
      string1Value = input.readString();
      string2Value = input.readString();
   }

   public StrStrRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid, StrStrMember another) {
      super(statusNid, time, authorNid, moduleNid, pathNid, another);
      string1Value = another.getString1Value();
      string2Value = another.getString2Value();
   }

   protected StrStrRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid, StrStrRevision another) {
      super(statusNid, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      string1Value = another.string1Value;
      string2Value = another.string2Value;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      //
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.STRING1, getString1());
      rcs.with(RefexProperty.STRING2, getString2());
   }

   @Override
   public StrStrRevision duplicate() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (StrStrRevision.class.isAssignableFrom(obj.getClass())) {
         StrStrRevision another = (StrStrRevision) obj;

         return string1Value.equals(another.string1Value) && string2Value.equals(another.string2Value)
                 && super.equals(obj);
      }

      return false;
   }

   @Override
   public StrStrRevision makeAnalog() {
      return new StrStrRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public StrStrRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      StrStrRevision newR = new StrStrRevision(statusNid, time, authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<StrStrRevision> makePromotionPart(PathBI promotionPath, int authorNid) {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert string1Value != null;

      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName()).append(":{");
      buf.append(" string1Value:" + "'").append(this.string1Value).append("' ");
      buf.append(" string2Value:" + "'").append(this.string2Value).append("' ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeString(string1Value);
      output.writeString(string2Value);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getString1() {
      return string1Value;
   }

   @Override
   public String getString1Value() {
      return string1Value;
   }
   
   @Override
   public String getString2() {
      return string2Value;
   }

   @Override
   public String getString2Value() {
      return string2Value;
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefsetStrStrMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.STR_STR;
   }

   @Override
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public ArrayIntList getVariableVersionNids() {
      return new ArrayIntList(2);
   }

   @Override
   public StrStrMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (StrStrMember.Version) ((StrStrMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<StrStrMember.Version> getVersions() {
      return ((StrStrMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<StrStrRevision>> getVersions(ViewCoordinate c) {
      return ((StrStrMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setString1(String str) throws PropertyVetoException {
      this.string1Value = str;
      modified();
   }

   @Override
   public void setString1Value(String stringValue) {
      this.string1Value = stringValue;
      modified();
   }
   
   @Override
   public void setString2(String str) throws PropertyVetoException {
      this.string2Value = str;
      modified();
   }

   public void setString2Value(String stringValue) {
      this.string2Value = stringValue;
      modified();
   }
}
