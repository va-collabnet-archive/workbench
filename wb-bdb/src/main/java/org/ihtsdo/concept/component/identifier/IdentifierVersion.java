package org.ihtsdo.concept.component.identifier;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.bind.ThinVersionHelper;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.I_HandleFutureStatusAtPositionSetup;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class IdentifierVersion
        implements I_IdPart, I_IdVersion, I_HandleFutureStatusAtPositionSetup {

   //~--- fields --------------------------------------------------------------

   private int authorityNid;
   private int statusAtPositionNid;

   //~--- constructors --------------------------------------------------------

   protected IdentifierVersion() {
      super();
   }

   protected IdentifierVersion(TkIdentifier idv) {
      super();
      this.statusAtPositionNid = Bdb.getSapDb().getSapNid(Bdb.uuidToNid(idv.getStatusUuid()),
              idv.getTime(), Bdb.uuidToNid(idv.getAuthorUuid()), 
              Bdb.uuidToNid(idv.getModuleUuid()), Bdb.uuidToNid(idv.getPathUuid()));
      this.authorityNid = Bdb.uuidToNid(idv.getAuthorityUuid());
   }

   protected IdentifierVersion(TupleInput input) {
      super();
      statusAtPositionNid = input.readInt();
      authorityNid        = input.readInt();
   }

   protected IdentifierVersion(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      this.statusAtPositionNid = Bdb.getSapDb().getSapNid(statusNid, time, authorNid, moduleNid, pathNid);
   }

   protected IdentifierVersion(int statusNid, long time, int authorNid, int moduleNid, int pathNid,
                               IdentifierVersion idVersion) {
      this(statusNid, time, authorNid, moduleNid, pathNid);
      this.authorityNid = idVersion.authorityNid;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public I_IdPart duplicateIdPart() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (IdentifierVersion.class.isAssignableFrom(obj.getClass())) {
         IdentifierVersion another = (IdentifierVersion) obj;

         return (this.statusAtPositionNid == another.statusAtPositionNid)
                && (this.authorityNid == another.authorityNid);
      }

      return false;
   }

   @Override
   public int hashCode() {
      return HashFunction.hashCode(new int[] { statusAtPositionNid, authorityNid });
   }

   public final boolean readyToWrite() {
      assert statusAtPositionNid != Integer.MAX_VALUE : toString();
      assert authorityNid != Integer.MAX_VALUE : toString();
      assert readyToWriteIdentifier() : toString();

      return true;
   }

   public abstract boolean readyToWriteIdentifier();

   public boolean sapIsInRange(int min, int max) {
      return (statusAtPositionNid >= min) && (statusAtPositionNid <= max);
   }

   /*
    * (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();

      buf.append("sap:").append(statusAtPositionNid);
      buf.append(" authority:");
      ConceptComponent.addNidToBuffer(buf, authorityNid);
      buf.append(" path:");
      ConceptComponent.addNidToBuffer(buf, getPathId());
      buf.append(" module:");
      ConceptComponent.addNidToBuffer(buf, getModuleNid());
      buf.append(" tm:");

      if (getTime() == Long.MAX_VALUE) {
         buf.append(" uncommitted");
      } else if (getTime() == Long.MIN_VALUE) {
         buf.append(" uncommitted");
      } else {
         buf.append(Revision.fileDateFormat.format(new Date(getTime())));
      }

      buf.append(" status:");
      ConceptComponent.addNidToBuffer(buf, getStatusId());

      return buf.toString();
   }

   public final void writeIdPartToBdb(TupleOutput output) {
      output.writeInt(statusAtPositionNid);
      output.writeInt(authorityNid);
      writeSourceIdToBdb(output);
   }

   protected abstract void writeSourceIdToBdb(TupleOutput output);

   //~--- get methods ---------------------------------------------------------

   @Override
   public Set<Integer> getAllNidsForId() throws IOException {
      HashSet<Integer> allNids = new HashSet<Integer>();

      allNids.add(authorityNid);
      allNids.add(getStatusNid());
      allNids.add(getAuthorNid());
      allNids.add(getPathNid());

      return allNids;
   }

   public int getAuthorId() {
      return Bdb.getSapDb().getAuthorNid(statusAtPositionNid);
   }

   @Override
   public int getAuthorNid() {
      return Bdb.getSapDb().getAuthorNid(statusAtPositionNid);
   }

   @Override
   public int getAuthorityNid() {
      return authorityNid;
   }

   @Override
   public I_IdPart getMutableIdPart() {

      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public ArrayIntList getPartComponentNids() {

      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public int getPathId() {
      return Bdb.getSapDb().getPathNid(statusAtPositionNid);
   }

   @Override
   public int getPathNid() {
      return Bdb.getSapDb().getPathNid(statusAtPositionNid);
   }

    @Override
   public int getStampNid() {
      return statusAtPositionNid;
   }

   @Override
   public int getStatusId() {
      return Bdb.getSapDb().getStatusNid(statusAtPositionNid);
   }

   @Override
   public int getStatusNid() {
      return Bdb.getSapDb().getStatusNid(statusAtPositionNid);
   }
 
   @Override
   public int getModuleNid() {
      return Bdb.getSapDb().getModuleNid(statusAtPositionNid);
   }

   @Override
   public long getTime() {
      return Bdb.getSapDb().getTime(statusAtPositionNid);
   }

   @Override
   public Set<TimePathId> getTimePathSet() {

      // TODO Auto-generated method stub
      return null;
   }

   public abstract ConceptComponent.IDENTIFIER_PART_TYPES getType();

   @Override
   public List<UUID> getUUIDs() {

      // TODO Auto-generated method stub
      return null;
   }

   protected ArrayIntList getVariableVersionNids() {
      ArrayIntList nids = new ArrayIntList(3);

      nids.add(authorityNid);

      return nids;
   }

   @Override
   public int getVersion() {
      return ThinVersionHelper.convert(getTime());
   }

   /*
    * (non-Javadoc)
    * @see org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup#isSetup()
    */
   @Override
   public boolean isSetup() {
      return statusAtPositionNid != Integer.MAX_VALUE;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setAuthorityNid(int sourceNid) {
      this.authorityNid = sourceNid;
   }

   @Override
   public void setPathId(int pathId) {
      throw new UnsupportedOperationException();
   }

   /*
    * (non-Javadoc)
    * @see org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup#setStatusAtPositionNid(int)
    */
   @Override
   public void setStatusAtPositionNid(int sapNid) {
      this.statusAtPositionNid = sapNid;
   }

   @Override
   public void setStatusId(int statusId) {
      throw new UnsupportedOperationException();
   }

   public void setTime(long time) {
      if (getTime() != Long.MAX_VALUE) {
         throw new UnsupportedOperationException("Time alreay committed.");
      }

      this.statusAtPositionNid = Bdb.getSapDb().getSapNid(getStatusId(), time, getAuthorId(), getModuleNid(), getPathId());
   }

   @Override
   public void setVersion(int version) {
      throw new UnsupportedOperationException();
   }
}
