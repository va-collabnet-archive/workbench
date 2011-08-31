package org.dwfa.vodb.conflict;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTuple;

import org.ihtsdo.tk.api.ComponentVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;

public class EditPathWinsStrategy extends ContradictionManagementStrategy {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- methods -------------------------------------------------------------

   @Override
   protected <T extends I_AmPart> boolean doesConflictExist(List<T> versions) {
      return false;
   }

   @Override
   public <T extends I_AmPart> List<T> resolveParts(List<T> parts) {
      List<T> returnValues = new ArrayList<T>(2);

      for (T v : parts) {
         if (config.getEditingPathSetReadOnly().getPathNidSet().contains(v.getPathNid())) {
            returnValues.add(v);
         }
      }

      if (returnValues.isEmpty()) {
         for (T part : parts) {
            if (part.isBaselineGeneration()) {
               returnValues.add(part);
            }
         }
      }

      return returnValues;
   }

   @Override
   public <T extends I_AmTuple> List<T> resolveTuples(List<T> tuples) {
      List<T> returnValues = new ArrayList<T>(2);

      for (T v : tuples) {
         if (config.getEditingPathSetReadOnly().getPathNidSet().contains(v.getPathNid())) {
            returnValues.add(v);
         }
      }

      if (returnValues.isEmpty()) {
         for (T part : tuples) {
            if (part.isBaselineGeneration()) {
               returnValues.add(part);
            }
         }
      }

      return returnValues;
   }

   @Override
   public <T extends ComponentVersionBI> List<T> resolveVersions(List<T> versions) {
      List<T> returnValues = new ArrayList<T>(2);

      for (T v : versions) {
         if (config.getEditingPathSetReadOnly().getPathNidSet().contains(v.getPathNid())) {
            returnValues.add(v);
         }
      }

      if (returnValues.isEmpty()) {
         for (T part : versions) {
            if (part.isBaselineGeneration()) {
               returnValues.add(part);
            }
         }
      }

      return returnValues;
   }

   @Override
   public <T extends ComponentVersionBI> List<T> resolveVersions(T part1, T part2) {
      List<T> returnValues = new ArrayList<T>(2);

      if (config.getEditingPathSetReadOnly().getPathNidSet().contains(part1.getPathNid())) {
         returnValues.add(part1);
      }

      if (config.getEditingPathSetReadOnly().getPathNidSet().contains(part2.getPathNid())) {
         returnValues.add(part2);
      }

      if (returnValues.isEmpty()) {
         if (part1.isBaselineGeneration()) {
            returnValues.add(part1);
         }

         if (part2.isBaselineGeneration()) {
            returnValues.add(part2);
         }
      }

      return returnValues;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getDescription() {
      return "<html>This resolution strategy implements resolution that"
             + "<li>suppresses the members that are NOT on the edit path(s) from </li>"
             + "<li>participating in the potential contradiction.</ul>" + "</html>";
   }

   @Override
   public String getDisplayName() {
      return "Suppress versions NOT on a edit path from contradictions";
   }
}
