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
import org.ihtsdo.db.bdb.sap.StatusAtPositionBdb;
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
   private static StatusAtPositionBdb sapBdb = Bdb.getSapDb();

   //~--- fields --------------------------------------------------------------

   private int authorityNid;
   private int statusAtPositionNid;

   //~--- constructors --------------------------------------------------------

   protected IdentifierVersion() {
      super();
   }

   protected IdentifierVersion(TkIdentifier idv) {
      super();
      this.statusAtPositionNid = sapBdb.getSapNid(Bdb.uuidToNid(idv.getStatusUuid()),
              Bdb.uuidToNid(idv.getAuthorUuid()), Bdb.uuidToNid(idv.getPathUuid()), idv.getTime());
      this.authorityNid = Bdb.uuidToNid(idv.getAuthorityUuid());
   }

   protected IdentifierVersion(TupleInput input) {
      super();
      statusAtPositionNid = input.readInt();
      authorityNid        = input.readInt();
   }

   protected IdentifierVersion(int statusNid, int authorNid, int pathNid, long time) {
      this.statusAtPositionNid = sapBdb.getSapNid(statusNid, authorNid, pathNid, time);
   }

   protected IdentifierVersion(int statusNid, int authorNid, int pathNid, long time,
                               IdentifierVersion idVersion) {
      this(statusNid, authorNid, pathNid, time);
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
      return sapBdb.getAuthorNid(statusAtPositionNid);
   }

   @Override
   public int getAuthorNid() {
      return sapBdb.getAuthorNid(statusAtPositionNid);
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
      return sapBdb.getPathNid(statusAtPositionNid);
   }

   @Override
   public int getPathNid() {
      return sapBdb.getPathNid(statusAtPositionNid);
   }

   public int getSapNid() {
      return statusAtPositionNid;
   }

   @Override
   public int getStatusId() {
      return sapBdb.getStatusNid(statusAtPositionNid);
   }

   @Override
   public int getStatusNid() {
      return sapBdb.getStatusNid(statusAtPositionNid);
   }

   @Override
   public long getTime() {
      return sapBdb.getTime(statusAtPositionNid);
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

      this.statusAtPositionNid = sapBdb.getSapNid(getStatusId(), getAuthorId(), getPathId(), time);
   }

   @Override
   public void setVersion(int version) {
      throw new UnsupportedOperationException();
   }
}
