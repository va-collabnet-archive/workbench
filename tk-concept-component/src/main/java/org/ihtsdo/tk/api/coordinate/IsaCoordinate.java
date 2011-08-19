package org.ihtsdo.tk.api.coordinate;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

public class IsaCoordinate {

   /**
    * The allowed status cNids for which this cache is valid.
    */
   protected NidSetBI               allowedStatusNids;
   protected int                    classifierNid;
   protected ContradictionManagerBI contradictionMgr;
   protected Precedence             precedence;
   protected RelAssertionType       relAssertionType;

   /**
    * The set of destination rels type nids for which this cache is valid.
    */
   protected NidSetBI relTypeNids;

   /**
    * The view position for which this cache is valid.
    */
   protected PositionBI viewPosition;

   /**
    * cached value so that viewPositionSet does not have to be recreated
    * each time a query is performed.
    */
   protected PositionSetBI viewPositionSet;

   //~--- constructors --------------------------------------------------------

   public IsaCoordinate(PositionBI viewPosition, NidSetBI allowedStatus, NidSetBI relTypeNids,
                        Precedence precedence, ContradictionManagerBI contradictionMgr, int classifierNid,
                        RelAssertionType relAssertionType) {
      super();
      this.viewPosition      = viewPosition;
      this.allowedStatusNids = new NidSet(allowedStatus.getSetValues());
      assert allowedStatus != null : "Cannot use null wildcard for allowed status.";
      assert allowedStatus.size() != 0 : "Cannot use an empty set for allowed status.";

      try {
         assert allowedStatus.contains(SnomedMetadataRfx.getSTATUS_RETIRED_NID()) == false :
                "Cannot include a retired status. May surface cycles: " + allowedStatus;
      } catch (IOException e) {
         e.printStackTrace();
      }

      this.relTypeNids      = new NidSet(relTypeNids.getSetValues());
      this.viewPositionSet  = new PositionSet(viewPosition);
      this.precedence       = precedence;
      this.contradictionMgr = contradictionMgr;
      this.classifierNid    = classifierNid;
      this.relAssertionType = relAssertionType;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object obj) {
      if (IsaCoordinate.class.isAssignableFrom(obj.getClass())) {
         IsaCoordinate another = (IsaCoordinate) obj;

         if (!viewPosition.equals(another.viewPosition)) {
            return false;
         }

         // TODO: research status list... gets contaminated with all active subtypes of active
         // (like develpmental, experimental, etc)
//       if (!allowedStatusNids.equals(another.allowedStatusNids)) {
//          return false;
//       }
         if (!relTypeNids.equals(another.relTypeNids)) {
            return false;
         }

         return true;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { viewPosition.getPath().getConceptNid() });
   }

   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append("KindOfSpec: viewPosition: ");
      buff.append(viewPosition);
      buff.append("\n allowedStatus: ");
      buff.append(allowedStatusNids);
      buff.append("\n relTypes: ");
      buff.append(relTypeNids);
      buff.append("\n viewPositions: ");
      buff.append(viewPositionSet);

      return buff.toString();
   }

   //~--- get methods ---------------------------------------------------------

   public NidSetBI getAllowedStatusNids() {
      return allowedStatusNids;
   }

   public int getClassifierNid() {
      return classifierNid;
   }

   public ContradictionManagerBI getContradictionMgr() {
      return contradictionMgr;
   }

   public ViewCoordinate getCoordinate() {
      return new ViewCoordinate(precedence, viewPositionSet, allowedStatusNids, relTypeNids,
                                contradictionMgr, Integer.MIN_VALUE, classifierNid, relAssertionType, null,
                                null);
   }

   public Precedence getPrecedence() {
      return precedence;
   }

   public RelAssertionType getRelAssertionType() {
      return relAssertionType;
   }

   public NidSetBI getRelTypeNids() {
      return relTypeNids;
   }

   public PositionBI getViewPosition() {
      return viewPosition;
   }

   public PositionSetBI getViewPositionSet() {
      return viewPositionSet;
   }
}


//~ Formatted by Jindent --- http://www.jindent.com
