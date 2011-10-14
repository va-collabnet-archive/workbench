package org.ihtsdo.concept.component.refsetmember.cidLong;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidLong;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetCidLongMember;
import org.ihtsdo.etypes.ERefsetCidLongRevision;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_cnid_long.RefexCnidLongAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidlong.TkRefsetCidLongMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidlong.TkRefsetCidLongRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;

public class CidLongMember extends RefsetMember<CidLongRevision, CidLongMember>
        implements RefexCnidLongAnalogBI<CidLongRevision> {
   private static VersionComputer<RefsetMember<CidLongRevision, CidLongMember>.Version> computer =
      new VersionComputer<RefsetMember<CidLongRevision, CidLongMember>.Version>();

   //~--- fields --------------------------------------------------------------

   private int  c1Nid;
   private long longValue;

   //~--- constructors --------------------------------------------------------

   public CidLongMember() {
      super();
   }

   public CidLongMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public CidLongMember(TkRefsetCidLongMember refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid     = Bdb.uuidToNid(refsetMember.getC1Uuid());
      longValue = refsetMember.getLongValue();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<CidLongRevision, CidLongMember>(primordialSapNid);

         for (TkRefsetCidLongRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new CidLongRevision(eVersion, this));
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
      rcs.with(RefexProperty.CNID1, getCnid1());
      rcs.with(RefexProperty.LONG1, getLong1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (CidLongMember.class.isAssignableFrom(obj.getClass())) {
         CidLongMember another = (CidLongMember) obj;

         return this.c1Nid == another.c1Nid;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { c1Nid });
   }

   @Override
   public CidLongRevision makeAnalog() {
      CidLongRevision newR = new CidLongRevision(getStatusNid(), getPathNid(), getTime(), this);

      return newR;
   }

   @Override
   public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
      CidLongRevision newR = new CidLongRevision(statusNid, pathNid, time, this);

      addRevision(newR);

      return newR;
   }

   @Override
   public CidLongRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
      CidLongRevision newR = new CidLongRevision(statusNid, authorNid, pathNid, time, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean membersEqual(ConceptComponent<CidLongRevision, CidLongMember> obj) {
      if (CidLongMember.class.isAssignableFrom(obj.getClass())) {
         CidLongMember another = (CidLongMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.longValue == another.longValue);
      }

      return false;
   }

   @Override
   protected void readMemberFields(TupleInput input) {
      c1Nid     = input.readInt();
      longValue = input.readLong();
   }

   @Override
   protected final CidLongRevision readMemberRevision(TupleInput input) {
      return new CidLongRevision(input, this);
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
      buf.append(" longValue:").append(this.longValue);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeLong(longValue);
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
   public long getLong1() {
      return this.longValue;
   }

   public long getLongValue() {
      return longValue;
   }

   @Override
   public TkRefsetAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContraditionException, IOException {
      return new TkRefsetCidLongMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFSET_TYPE getTkRefsetType() {
      return TK_REFSET_TYPE.CID_LONG;
   }

   @Override
   public int getTypeId() {
      return REFSET_TYPES.CID_LONG.getTypeNid();
   }

   @Override
   public ArrayIntList getVariableVersionNids() {
      ArrayIntList variableNids = new ArrayIntList(3);

      variableNids.add(getC1Nid());

      return variableNids;
   }

   @Override
   protected VersionComputer<RefsetMember<CidLongRevision, CidLongMember>.Version> getVersionComputer() {
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
            for (CidLongRevision r : revisions) {
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
   public void setCnid1(int cnid1) throws PropertyVetoException {
      this.c1Nid = cnid1;
      modified();
   }

   @Override
   public void setLong1(long l) throws PropertyVetoException {
      this.longValue = l;
      modified();
   }

   public void setLongValue(long longValue) {
      this.longValue = longValue;
      modified();
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefsetMember<CidLongRevision, CidLongMember>.Version
           implements I_ExtendByRefVersion<CidLongRevision>, I_ExtendByRefPartCidLong<CidLongRevision>,
                      RefexCnidLongAnalogBI<CidLongRevision> {
      private Version(RefexCnidLongAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(I_ExtendByRefPart<CidLongRevision> o) {
         if (I_ExtendByRefPartCidLong.class.isAssignableFrom(o.getClass())) {
            I_ExtendByRefPartCidLong<CidLongRevision> another = (I_ExtendByRefPartCidLong<CidLongRevision>) o;

            if (this.getC1id() != another.getC1id()) {
               return this.getC1id() - another.getC1id();
            }

            if (this.getLong1() != another.getLongValue()) {
               if (this.getLong1() - another.getLongValue() > 0) {
                  return 1;
               }

               return -1;
            }
         }

         return super.compareTo(o);
      }

      @Override
      public int hashCodeOfParts() {
         return Hashcode.compute(new int[] { getC1Nid(), (int) getLong1() });
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public int getC1id() {
         return getCv().getCnid1();
      }

      @Override
      public int getCnid1() {
         return getCv().getCnid1();
      }

      RefexCnidLongAnalogBI getCv() {
         return (RefexCnidLongAnalogBI) cv;
      }

      @Override
      public ERefsetCidLongMember getERefsetMember() throws IOException {
         return new ERefsetCidLongMember(this);
      }

      @Override
      public ERefsetCidLongRevision getERefsetRevision() throws IOException {
         return new ERefsetCidLongRevision(this);
      }

      @Override
      public long getLong1() {
         return getCv().getLong1();
      }

      @Override
      public long getLongValue() {
         return getCv().getLong1();
      }

      //~--- set methods ------------------------------------------------------

      @Override
      public void setC1id(int c1id) throws PropertyVetoException {
         getCv().setCnid1(c1id);
      }

      @Override
      public void setCnid1(int cnid1) throws PropertyVetoException {
         getCv().setCnid1(cnid1);
      }

      @Override
      public void setLong1(long l) throws PropertyVetoException {
         getCv().setLong1(l);
      }

      @Override
      public void setLongValue(long longValue) throws PropertyVetoException {
         getCv().setLong1(longValue);
      }
   }
}
