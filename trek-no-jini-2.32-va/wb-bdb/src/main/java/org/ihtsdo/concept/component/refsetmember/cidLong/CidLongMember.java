package org.ihtsdo.concept.component.refsetmember.cidLong;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidLong;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetCidLongRevision;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_nid_long.RefexNidLongAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long.TkRefexUuidLongMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long.TkRefexUuidLongRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_long.RefexNidLongVersionBI;
import org.ihtsdo.tk.dto.RevisionHandling;

public class CidLongMember extends RefsetMember<CidLongRevision, CidLongMember>
        implements RefexNidLongAnalogBI<CidLongRevision> {
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

   public CidLongMember(TkRefexUuidLongMember refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid     = Bdb.uuidToNid(refsetMember.getUuid1());
      longValue = refsetMember.getLong1();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<CidLongRevision, CidLongMember>(primordialSapNid);

         for (TkRefexUuidLongRevision eVersion : refsetMember.getRevisionList()) {
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
      rcs.with(RefexProperty.CNID1, getNid1());
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
      CidLongRevision newR = new CidLongRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),  this);

      return newR;
   }

   @Override
   public CidLongRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      CidLongRevision newR = new CidLongRevision(statusNid, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<CidLongRevision, CidLongMember> obj) {
      if (CidLongMember.class.isAssignableFrom(obj.getClass())) {
         CidLongMember another = (CidLongMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.longValue == another.longValue);
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexNidLongVersionBI.class.isAssignableFrom(another.getClass())){
            RefexNidLongVersionBI cv = (RefexNidLongVersionBI) another;
            return (this.c1Nid == cv.getNid1()) && (this.longValue == cv.getLong1());
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
   public int getNid1() {
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
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefexUuidLongMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.CID_LONG;
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

      return Collections.unmodifiableList((List<Version>) versions);
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   @Override
   public void setNid1(int cnid1) throws PropertyVetoException {
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
                      RefexNidLongAnalogBI<CidLongRevision> {
      private Version(RefexNidLongAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(RefexVersionBI o) {
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
         return getCv().getNid1();
      }

      @Override
      public int getNid1() {
         return getCv().getNid1();
      }

      RefexNidLongAnalogBI getCv() {
         return (RefexNidLongAnalogBI) cv;
      }

      @Override
      public TkRefexUuidLongMember getERefsetMember() throws IOException {
         return new TkRefexUuidLongMember(this, RevisionHandling.EXCLUDE_REVISIONS);
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
         getCv().setNid1(c1id);
      }

      @Override
      public void setNid1(int cnid1) throws PropertyVetoException {
         getCv().setNid1(cnid1);
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
