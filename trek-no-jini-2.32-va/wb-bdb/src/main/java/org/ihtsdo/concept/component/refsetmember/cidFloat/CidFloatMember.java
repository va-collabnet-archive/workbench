package org.ihtsdo.concept.component.refsetmember.cidFloat;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidFloat;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetCidFloatRevision;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_nid_float.RefexNidFloatAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float.TkRefexUuidFloatMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float.TkRefexUuidFloatRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_float.RefexNidFloatVersionBI;
import org.ihtsdo.tk.dto.RevisionHandling;

public class CidFloatMember extends RefsetMember<CidFloatRevision, CidFloatMember>
        implements RefexNidFloatAnalogBI<CidFloatRevision>, I_ExtendByRefPartCidFloat<CidFloatRevision> {
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

   public CidFloatMember(TkRefexUuidFloatMember refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid      = Bdb.uuidToNid(refsetMember.getUuid1());
      floatValue = refsetMember.getFloat1();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<CidFloatRevision, CidFloatMember>(primordialSapNid);

         for (TkRefexUuidFloatRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new CidFloatRevision(eVersion, this));
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
      rcs.with(RefexProperty.FLOAT1, getFloat1());
   }

   @Override
   public I_ExtendByRefPart duplicate() {
      throw new UnsupportedOperationException("Not supported yet.");
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
      return new CidFloatRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public CidFloatRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      CidFloatRevision newR = new CidFloatRevision(statusNid, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<CidFloatRevision, CidFloatMember> obj) {
      if (CidFloatMember.class.isAssignableFrom(obj.getClass())) {
         CidFloatMember another = (CidFloatMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.floatValue == another.floatValue);
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexNidFloatVersionBI.class.isAssignableFrom(another.getClass())){
            RefexNidFloatVersionBI cv = (RefexNidFloatVersionBI) another;
            return (this.c1Nid == cv.getNid1()) && (this.floatValue == cv.getFloat1());
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
   public int getC1id() {
      return getNid1();
   }

   @Override
   public int getNid1() {
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
   public float getMeasurementValue() {
      return getFloat1();
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefexUuidFloatMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.CID_FLOAT;
   }

   @Override
   public int getTypeId() {
      return REFSET_TYPES.CID_FLOAT.getTypeNid();
   }

   @Override
   public int getUnitsOfMeasureId() {
      return getC1Nid();
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

      return Collections.unmodifiableList((List<Version>) versions);
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   @Override
   public void setC1id(int c1id) throws PropertyVetoException {
      setC1Nid(c1Nid);
   }

   @Override
   public void setNid1(int cnid) throws PropertyVetoException {
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

   @Override
   public void setMeasurementValue(float measurementValue) {
      setFloatValue(floatValue);
   }

   @Override
   public void setUnitsOfMeasureId(int conceptId) {
      setC1Nid(conceptId);
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefsetMember<CidFloatRevision, CidFloatMember>.Version
           implements RefexNidFloatAnalogBI<CidFloatRevision>, I_ExtendByRefPartCidFloat<CidFloatRevision> {
      private Version(RefexNidFloatAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(RefexVersionBI o) {
         if (this.getNid1() != ((Version) o).getNid1()) {
            return this.getNid1() - ((Version) o).getNid1();
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
      public int getC1id() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public int getNid1() {
         return getCv().getNid1();
      }

      RefexNidFloatAnalogBI getCv() {
         return (RefexNidFloatAnalogBI) cv;
      }

      @Override
      public TkRefexUuidFloatMember getERefsetMember() throws IOException {
         return new TkRefexUuidFloatMember(this, RevisionHandling.EXCLUDE_REVISIONS);
      }

      @Override
      public ERefsetCidFloatRevision getERefsetRevision() throws IOException {
         return new ERefsetCidFloatRevision(this);
      }

      @Override
      public float getFloat1() {
         return getCv().getFloat1();
      }

      @Override
      public float getMeasurementValue() {
         return getCv().getFloat1();
      }

      @Override
      public int getUnitsOfMeasureId() {
         return getCv().getNid1();
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
      public void setFloat1(float f) throws PropertyVetoException {
         getCv().setFloat1(f);
      }

      @Override
      public void setMeasurementValue(float measurementValue) {
         ((I_ExtendByRefPartCidFloat) getCv()).setMeasurementValue(measurementValue);
      }

      @Override
      public void setUnitsOfMeasureId(int conceptId) {
         ((I_ExtendByRefPartCidFloat) getCv()).setUnitsOfMeasureId(conceptId);
      }
   }
}
