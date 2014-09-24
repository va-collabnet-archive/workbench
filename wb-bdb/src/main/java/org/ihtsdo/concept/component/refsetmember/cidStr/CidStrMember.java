package org.ihtsdo.concept.component.refsetmember.cidStr;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetCidStrRevision;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_nid_string.RefexNidStringAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_string.TkRefexUuidStringMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_string.TkRefexUuidStringRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.tk.dto.RevisionHandling;

public class CidStrMember extends RefsetMember<CidStrRevision, CidStrMember>
        implements I_ExtendByRefPartCidString<CidStrRevision>, RefexNidStringAnalogBI<CidStrRevision> {
   private static VersionComputer<RefsetMember<CidStrRevision, CidStrMember>.Version> computer =
      new VersionComputer<RefsetMember<CidStrRevision, CidStrMember>.Version>();

   //~--- fields --------------------------------------------------------------

   private int    c1Nid;
   private String strValue;

   //~--- constructors --------------------------------------------------------

   public CidStrMember() {
      super();
   }

   public CidStrMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public CidStrMember(TkRefexUuidStringMember refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid    = Bdb.uuidToNid(refsetMember.getUuid1());
      strValue = refsetMember.getString1();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<CidStrRevision, CidStrMember>(primordialSapNid);

         for (TkRefexUuidStringRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new CidStrRevision(eVersion, this));
         }
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.CNID1, getNid1());
      rcs.with(RefexProperty.STRING1, getString1());
   }

   @Override
   public CidStrRevision duplicate() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (CidStrMember.class.isAssignableFrom(obj.getClass())) {
         CidStrMember another = (CidStrMember) obj;

         return this.c1Nid == another.c1Nid;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { c1Nid });
   }

   @Override
   public CidStrRevision makeAnalog() {
      CidStrRevision newR = new CidStrRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);

      return newR;
   }

   @Override
   public CidStrRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      CidStrRevision newR = new CidStrRevision(statusNid, time, authorNid, moduleNid, pathNid,this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<CidStrRevision, CidStrMember> obj) {
      if (CidStrMember.class.isAssignableFrom(obj.getClass())) {
         CidStrMember another = (CidStrMember) obj;

         return (this.c1Nid == another.c1Nid) && this.strValue.equals(another.strValue);
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexNidStringVersionBI.class.isAssignableFrom(another.getClass())){
            RefexNidStringVersionBI cv = (RefexNidStringVersionBI) another;
            return (this.c1Nid == cv.getNid1()) && this.strValue.equals(cv.getString1());
        }
        return false;
    }

   @Override
   protected void readMemberFields(TupleInput input) {
      c1Nid    = input.readInt();
      strValue = input.readString();
   }

   @Override
   protected final CidStrRevision readMemberRevision(TupleInput input) {
      return new CidStrRevision(input, this);
   }

   @Override
   public boolean readyToWriteRefsetMember() {
      assert c1Nid != Integer.MAX_VALUE;
      assert strValue != null;

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
      buf.append(" c1Nid: ");
      addNidToBuffer(buf, c1Nid);
      buf.append(" strValue:" + "'").append(this.strValue).append("'");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeString(strValue);
   }

   //~--- get methods ---------------------------------------------------------

   public int getC1Nid() {
      return c1Nid;
   }

   @Override
   public int getC1id() {
      return getC1Nid();
   }

   @Override
   public int getNid1() {
      return c1Nid;
   }

   @Override
   public String getString1() {
      return strValue;
   }

   @Override
   public String getString1Value() {
      return strValue;
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefexUuidStringMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.CID_STR;
   }

   @Override
   public int getTypeId() {
      return REFSET_TYPES.CID_STR.getTypeNid();
   }

   @Override
   public ArrayIntList getVariableVersionNids() {
      ArrayIntList variableNids = new ArrayIntList(3);

      variableNids.add(getC1id());

      return variableNids;
   }

   @Override
   protected VersionComputer<RefsetMember<CidStrRevision, CidStrMember>.Version> getVersionComputer() {
      return computer;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<Version> getVersions() {
      if (versions == null) {
         int count = 1;

         if (revisions != null) {
            count = count + revisions.size();
         }

         ArrayList<Version> list = new ArrayList<Version>(count);

         if (getTime() != Long.MIN_VALUE) {
            list.add(new Version(this));
         }

         if (revisions != null) {
            for (RefexNidStringAnalogBI r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new Version(r));
               }
            }
         }

         versions = list;
      }

      return Collections.unmodifiableList((List<Version>) versions);
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   @Override
   public void setC1id(int c1id) {
      setC1Nid(c1id);
   }

   @Override
   public void setNid1(int cnid) throws PropertyVetoException {
      this.c1Nid = cnid;
      modified();
   }

   @Override
   public void setString1(String str) throws PropertyVetoException {
      this.strValue = str;
      modified();
   }

   @Override
   public void setString1Value(String strValue) {
      this.strValue = strValue;
      modified();
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefsetMember<CidStrRevision, CidStrMember>.Version
           implements I_ExtendByRefVersion<CidStrRevision>, I_ExtendByRefPartCidString<CidStrRevision>,
                      RefexNidStringAnalogBI<CidStrRevision> {
      private Version(RefexNidStringAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(RefexVersionBI o) {
         if (I_ExtendByRefPartCidString.class.isAssignableFrom(o.getClass())) {
            I_ExtendByRefPartCidString<CidStrRevision> another =
               (I_ExtendByRefPartCidString<CidStrRevision>) o;

            if (this.getC1id() != another.getC1id()) {
               return this.getC1id() - another.getC1id();
            }

            if (!this.getString1Value().equals(another.getString1Value())) {
               return this.getString1Value().compareTo(another.getString1Value());
            }
         }

         return super.compareTo(o);
      }

      @Override
      public I_ExtendByRefPartCidString<CidStrRevision> duplicate() {
         return (I_ExtendByRefPartCidString<CidStrRevision>) super.duplicate();
      }

      @Override
      public int hashCodeOfParts() {
         return Hashcode.compute(new int[] { getC1Nid(), getString1Value().hashCode() });
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public int getC1id() {
         return getCv().getNid1();
      }

      @Override
      public int getNid1() {
         return getCv().getNid1();
      }

      RefexNidStringAnalogBI getCv() {
         return (RefexNidStringAnalogBI) cv;
      }

      @Override
      public TkRefexUuidStringMember getERefsetMember() throws IOException {
         return new TkRefexUuidStringMember(this, RevisionHandling.EXCLUDE_REVISIONS);
      }

      @Override
      public ERefsetCidStrRevision getERefsetRevision() throws IOException {
         return new ERefsetCidStrRevision(this);
      }

      @Override
      public String getString1() {
         return getCv().getString1();
      }

      @Override
      public String getString1Value() {
         return getCv().getString1();
      }

      //~--- set methods ------------------------------------------------------

      @Override
      public void setC1id(int c1id) throws PropertyVetoException {
         getCv().setNid1(c1id);
      }

      @Override
      public void setNid1(int c1id) throws PropertyVetoException {
         getCv().setNid1(c1id);
      }

      @Override
      public void setString1(String value) throws PropertyVetoException {
         getCv().setString1(value);
      }

      @Override
      public void setString1Value(String value) throws PropertyVetoException {
         getCv().setString1(value);
      }
   }
}
