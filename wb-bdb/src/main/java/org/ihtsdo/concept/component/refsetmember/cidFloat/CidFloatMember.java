package org.ihtsdo.concept.component.refsetmember.cidFloat;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.refex.type_cnid_float.RefexCnidFloatAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.cidflt.TkRefsetCidFloatMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidflt.TkRefsetCidFloatRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class CidFloatMember extends RefsetMember<CidFloatRevision, CidFloatMember>
        implements RefexCnidFloatAnalogBI<CidFloatRevision> {
   private static VersionComputer<RefsetMember<CidFloatRevision, CidFloatMember>.Version> computer =
      new VersionComputer<RefsetMember<CidFloatRevision, CidFloatMember>.Version>();

   //~--- fields --------------------------------------------------------------

   private int   c1Nid;
   private float floatValue;

   //~--- constructors --------------------------------------------------------

   public CidFloatMember() {
      super();
   }

   public CidFloatMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public CidFloatMember(TkRefsetCidFloatMember refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid      = Bdb.uuidToNid(refsetMember.getC1Uuid());
      floatValue = refsetMember.getFloatValue();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<CidFloatRevision, CidFloatMember>(primordialSapNid);

         for (TkRefsetCidFloatRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new CidFloatRevision(eVersion, this));
         }
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.CNID1, getCnid1());
      rcs.with(RefexProperty.FLOAT1, getFloat1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (CidFloatMember.class.isAssignableFrom(obj.getClass())) {
         CidFloatMember another = (CidFloatMember) obj;

         return this.c1Nid == another.c1Nid;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { c1Nid });
   }

   @Override
   public CidFloatRevision makeAnalog() {
      return new CidFloatRevision(getStatusNid(), getPathNid(), getTime(), this);
   }

   @Override
   public CidFloatRevision makeAnalog(int statusNid, int pathNid, long time) {
      CidFloatRevision newR = new CidFloatRevision(statusNid, pathNid, time, this);

      addRevision(newR);

      return newR;
   }

   @Override
   public CidFloatRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
      CidFloatRevision newR = new CidFloatRevision(statusNid, authorNid, pathNid, time, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean membersEqual(ConceptComponent<CidFloatRevision, CidFloatMember> obj) {
      if (CidFloatMember.class.isAssignableFrom(obj.getClass())) {
         CidFloatMember another = (CidFloatMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.floatValue == another.floatValue);
      }

      return false;
   }

   @Override
   protected void readMemberFields(TupleInput input) {
      c1Nid      = input.readInt();
      floatValue = input.readFloat();
   }

   @Override
   protected final CidFloatRevision readMemberRevision(TupleInput input) {
      return new CidFloatRevision(input, this);
   }

   @Override
   public boolean readyToWriteRefsetMember() {
      assert c1Nid != Integer.MAX_VALUE;

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
      buf.append(" floatValue:").append(this.floatValue);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeFloat(floatValue);
   }

   //~--- get methods ---------------------------------------------------------

   public int getC1Nid() {
      return c1Nid;
   }

   @Override
   public int getCnid1() {
      return c1Nid;
   }

   @Override
   public float getFloat1() {
      return this.floatValue;
   }

   public float getFloatValue() {
      return floatValue;
   }

   @Override
   protected TK_REFSET_TYPE getTkRefsetType() {
      return TK_REFSET_TYPE.CID_FLOAT;
   }

   @Override
   public int getTypeId() {
      return REFSET_TYPES.CID_FLOAT.getTypeNid();
   }

   @Override
   public ArrayIntList getVariableVersionNids() {
      ArrayIntList variableNids = new ArrayIntList(3);

      variableNids.add(getC1Nid());

      return variableNids;
   }

   @Override
   protected VersionComputer<RefsetMember<CidFloatRevision, CidFloatMember>.Version> getVersionComputer() {
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
            for (CidFloatRevision r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new Version(r));
               }
            }
         }

         versions = list;
      }

      return (List<Version>) versions;
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   @Override
   public void setCnid1(int cnid) throws PropertyVetoException {
      this.c1Nid = cnid;
      modified();
   }

   @Override
   public void setFloat1(float f) throws PropertyVetoException {
      this.floatValue = f;
      modified();
   }

   public void setFloatValue(float floatValue) {
      this.floatValue = floatValue;
      modified();
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefsetMember<CidFloatRevision, CidFloatMember>.Version
           implements RefexCnidFloatAnalogBI<CidFloatRevision> {
      private Version(RefexCnidFloatAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(I_ExtendByRefPart<CidFloatRevision> o) {
         if (this.getCnid1() != ((Version) o).getCnid1()) {
            return this.getCnid1() - ((Version) o).getCnid1();
         }

         if (this.getFloat1() != ((Version) o).getFloat1()) {
            if (this.getFloat1() - ((Version) o).getFloat1() > 0) {
               return 1;
            } else {
               return -1;
            }
         }

         return super.compareTo(o);
      }

      @Override
      public int hashCodeOfParts() {
         return Hashcode.compute(new int[] { getC1Nid(), Float.floatToRawIntBits(getFloat1()) });
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public int getCnid1() {
         return getCv().getCnid1();
      }

      RefexCnidFloatAnalogBI getCv() {
         return (RefexCnidFloatAnalogBI) cv;
      }

      @Override
      public float getFloat1() {
         return getCv().getFloat1();
      }

      //~--- set methods ------------------------------------------------------

      @Override
      public void setCnid1(int cnid1) throws PropertyVetoException {
         getCv().setCnid1(cnid1);
      }

      @Override
      public void setFloat1(float f) throws PropertyVetoException {
         getCv().setFloat1(f);
      }
   }
}
