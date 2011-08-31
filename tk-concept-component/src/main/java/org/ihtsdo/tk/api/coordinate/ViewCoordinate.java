package org.ihtsdo.tk.api.coordinate;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidList;
import org.ihtsdo.tk.api.NidListBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ViewCoordinate {
   private long                   lastModSequence = Long.MIN_VALUE;
   private NidSetBI               allowedStatusNids;
   private int                    classifierNid;
   private ContradictionManagerBI contradictionManager;
   private NidSetBI               isaTypeNids;
   private NidListBI              langPrefList;
   private LANGUAGE_SORT          langSort;
   private int                    languageNid;
   private PositionSetBI          positionSet;
   private Precedence             precedence;
   private RelAssertionType       relAssertionType;
   private ViewCoordinate         vcWithAllStatusValues;

   //~--- constructors --------------------------------------------------------

   public ViewCoordinate(ViewCoordinate another) {
      super();
      this.precedence = another.precedence;

      if (another.positionSet != null) {
         this.positionSet = new PositionSet(another.positionSet);
      }

      if (another.allowedStatusNids != null) {
         this.allowedStatusNids = new NidSet(another.allowedStatusNids.getSetValues());
      }

      if (another.isaTypeNids != null) {
         this.isaTypeNids = new NidSet(another.isaTypeNids.getSetValues());
      }

      this.contradictionManager = another.contradictionManager;
      this.languageNid          = another.languageNid;
      this.classifierNid        = another.classifierNid;
      this.relAssertionType     = another.relAssertionType;

      if (another.langPrefList != null) {
         this.langPrefList = new NidList(another.langPrefList.getListArray());
      }

      this.langSort        = another.langSort;
      this.lastModSequence = another.lastModSequence;
   }

   public ViewCoordinate(Precedence precedence, PositionSetBI positionSet, NidSetBI allowedStatusNids,
                         NidSetBI isaTypeNids, ContradictionManagerBI contradictionManager, int languageNid,
                         int classifierNid, RelAssertionType relAssertionType, NidListBI langPrefList,
                         LANGUAGE_SORT langSort) {
      super();
      assert precedence != null;
      assert contradictionManager != null;
      this.precedence = precedence;

      if (positionSet != null) {
         this.positionSet = new PositionSet(positionSet);
      }

      if (allowedStatusNids != null) {
         this.allowedStatusNids = new NidSet(allowedStatusNids.getSetValues());
      }

      if (isaTypeNids != null) {
         this.isaTypeNids = new NidSet(isaTypeNids.getSetValues());
      }

      this.contradictionManager = contradictionManager;
      this.languageNid          = languageNid;
      this.classifierNid        = classifierNid;
      this.relAssertionType     = relAssertionType;

      if (langPrefList != null) {
         this.langPrefList = new NidList(langPrefList.getListArray());
      }

      this.langSort = langSort;
   }

   //~--- enums ---------------------------------------------------------------

   public enum LANGUAGE_SORT {
      LANG_BEFORE_TYPE("language before type"), TYPE_BEFORE_LANG("type before language"),
      LANG_REFEX("use language refex"), RF2_LANG_REFEX("use RF2 language refex");;

      private String desc;

      //~--- constructors -----------------------------------------------------

      private LANGUAGE_SORT(String desc) {
         this.desc = desc;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public String toString() {
         return desc;
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if (o instanceof ViewCoordinate) {
         ViewCoordinate another = (ViewCoordinate) o;

         if (!testEquals(precedence, another.precedence)) {
            return false;
         }

         if (!testEquals(positionSet, another.positionSet)) {
            return false;
         }

         if (!testEquals(allowedStatusNids, another.allowedStatusNids)) {
            return false;
         }

         if (!testEquals(isaTypeNids, another.isaTypeNids)) {
            return false;
         }

         if (!testEquals(contradictionManager, another.contradictionManager)) {
            return false;
         }

         if (!testEquals(languageNid, another.languageNid)) {
            return false;
         }

         if (!testEquals(classifierNid, another.classifierNid)) {
            return false;
         }

         if (!testEquals(relAssertionType, another.relAssertionType)) {
            return false;
         }

         if (!testEquals(langPrefList, another.langPrefList)) {
            return false;
         }

         if (!testEquals(langSort, another.langSort)) {
            return false;
         }

         return true;
      }

      return false;
   }

   @Override
   public int hashCode() {
      int hashCode = 0;

      for (PositionBI pos : positionSet.getPositionArray()) {
         hashCode = Hashcode.compute(new int[] { hashCode, pos.getPath().getConceptNid(), pos.getVersion() });
      }

      return hashCode;
   }

   private static boolean testEquals(Object o1, Object o2) {
      if ((o1 == null) && (o2 == null)) {
         return true;
      }

      if (o1 == o2) {
         return true;
      }

      if (o1 instanceof NidSetBI) {
         NidSetBI ns1 = (NidSetBI) o1;
         NidSetBI ns2 = (NidSetBI) o2;

         return Arrays.equals(ns1.getSetValues(), ns2.getSetValues());
      }

      if (o1 instanceof NidListBI) {
         NidListBI ns1 = (NidListBI) o1;
         NidListBI ns2 = (NidListBI) o2;

         return Arrays.equals(ns1.getListArray(), ns2.getListArray());
      }

      if (o1 instanceof PositionSetBI) {
         PositionSetBI ns1 = (PositionSetBI) o1;
         PositionSetBI ns2 = (PositionSetBI) o2;

         if (ns1.size() == 1) {
            if (ns2.size() == 1) {
               return ns1.getPositionArray()[0].equals(ns2.getPositionArray()[0]);
            }
         }

         return Arrays.equals(ns1.getPositionArray(), ns2.getPositionArray());
      }

      if (o1.equals(o2)) {
         return true;
      }

      return false;
   }

   @Override
   public String toString() {
      TerminologySnapshotDI snap = Ts.get().getSnapshot(this);
      StringBuilder         sb   = new StringBuilder();

      sb.append("precedence: ").append(precedence);
      sb.append(" \npositions: ").append(positionSet);

      String statusStr = "all";

      if (allowedStatusNids != null) {
         statusStr = allowedStatusNids.toString();
      }

      sb.append(" \nallowedStatus: ");

      if (statusStr.length() < 50) {
         sb.append(statusStr);
      } else {
         sb.append(statusStr.substring(0, 50)).append("...");
      }

      sb.append(" \nisaTypes: ").append(isaTypeNids);
      sb.append(" \ncontradiction: ").append(contradictionManager);
      getConceptText(sb.append(" \nlanguage: "), snap, languageNid);
      getConceptText(sb.append(" \nclassifier: "), snap, classifierNid);
      sb.append(" \nrelAssertionType: ").append(relAssertionType);

      return sb.toString();
   }

   //~--- get methods ---------------------------------------------------------

   public NidSetBI getAllowedStatusNids() {
      return allowedStatusNids;
   }

   public int getClassifierNid() {
      return classifierNid;
   }

   private void getConceptText(StringBuilder sb, TerminologySnapshotDI snap, int nid) {
      if (nid == Integer.MAX_VALUE) {
         sb.append("Integer.MAX_VALUE");

         return;
      }

      if (nid == Integer.MIN_VALUE) {
         sb.append("Integer.MIN_VALUE");

         return;
      }

      try {
         if ((snap.getConceptVersion(nid) != null)
                 && (snap.getConceptVersion(nid).getPreferredDescription() != null)) {
            sb.append(snap.getConceptVersion(nid).getPreferredDescription().getText());
         } else {
            sb.append(Integer.toString(nid));
         }
      } catch (IOException ex) {
         sb.append(ex.getLocalizedMessage());
      } catch (ContraditionException ex) {
         sb.append(ex.getLocalizedMessage());
      }
   }

   public ContradictionManagerBI getContradictionManager() {
      return contradictionManager;
   }

   public Collection<IsaCoordinate> getIsaCoordinates() {
       List<IsaCoordinate> isaCoordinates = new ArrayList<IsaCoordinate>(positionSet.size());
       for (PositionBI p: positionSet) {
           isaCoordinates.add(new IsaCoordinate(p, allowedStatusNids, isaTypeNids, precedence, contradictionManager, classifierNid, relAssertionType));
       }
      return isaCoordinates;
   }

   public NidSetBI getIsaTypeNids() {
      return isaTypeNids;
   }

   public NidListBI getLangPrefList() {
      return langPrefList;
   }

   public LANGUAGE_SORT getLangSort() {
      return langSort;
   }

   public int getLanguageNid() {
      return languageNid;
   }

   public long getLastModSequence() {
      return lastModSequence;
   }

   public PositionSetBI getPositionSet() {
      return positionSet;
   }

   public Precedence getPrecedence() {
      return precedence;
   }

   public RelAssertionType getRelAssertionType() {
      return relAssertionType;
   }

   public ViewCoordinate getVcWithAllStatusValues() {
      if (vcWithAllStatusValues == null) {
         vcWithAllStatusValues                   = new ViewCoordinate(this);
         vcWithAllStatusValues.allowedStatusNids = null;
      }

      return vcWithAllStatusValues;
   }

   //~--- set methods ---------------------------------------------------------

   public void setAllowedStatusNids(NidSetBI allowedStatusNids) {
      this.lastModSequence   = Ts.get().getSequence();
      this.allowedStatusNids = allowedStatusNids;
   }

   public void setClassifierNid(int classifierNid) {
      this.lastModSequence       = Ts.get().getSequence();
      this.vcWithAllStatusValues = null;
      this.classifierNid         = classifierNid;
   }

   public void setContradictionManager(ContradictionManagerBI contradictionManager) {
      this.lastModSequence      = Ts.get().getSequence();
      this.contradictionManager = contradictionManager;
   }

   public void setPositionSet(PositionSetBI positionSet) {
      this.lastModSequence       = Ts.get().getSequence();
      this.vcWithAllStatusValues = null;
      this.positionSet           = positionSet;
   }

   public void setRelAssertionType(RelAssertionType relAssertionType) {
      this.lastModSequence       = Ts.get().getSequence();
      this.vcWithAllStatusValues = null;
      this.relAssertionType      = relAssertionType;
   }
}


//~ Formatted by Jindent --- http://www.jindent.com
