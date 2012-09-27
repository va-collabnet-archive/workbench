/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.db.bdb.computer.refset;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.*;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.refset.RefsetSpecEditor;
import org.dwfa.ace.refset.spec.I_HelpMemberRefset;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.cement.RefsetAuxiliary;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.db.change.ChangeNotifier;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

/**
 *
 * @author kec
 */
public class MarkedParentComputer {
   private static final int SETUP_ISA_CACHE_THRESHOLD = 5000;

   //~--- fields --------------------------------------------------------------

   private AtomicInteger                       members   = new AtomicInteger();
   private boolean                             canceled  = false;
   private long                                startTime = System.currentTimeMillis();
   private Collection<I_ShowActivity>          activities;
   private I_ShowActivity                      activity;
   private Collection<? extends I_ExtendByRef> allRefsetMembers;
   private int                                 conceptCount;
   private I_RepresentIdSet                    currentRefsetMemberComponentNids;
   private EditCoordinate                      editCoordinate;
   private Concept                             markedParentRefsetConcept;
   private I_HelpMemberRefset                  memberRefsetHelper;
   private Concept                             refsetConcept;
   private RefsetSpec                          specHelper;
   private StopActionListener                  stopListener;
   private ViewCoordinate                      viewCoordinate;
    private final TerminologyBuilderBI builder;

   //~--- constructors --------------------------------------------------------

   public MarkedParentComputer(Concept refsetConcept, Collection<RefsetMember<?, ?>> members,
                               I_ConfigAceFrame frameConfig, HashSet<I_ShowActivity> activities)
           throws Exception {
      super();
      this.activities     = activities;
      this.editCoordinate = frameConfig.getEditCoordinate();
      this.viewCoordinate = frameConfig.getViewCoordinate();
      this.builder = Ts.get().getSnapshot(viewCoordinate).getBuilder(editCoordinate);
      this.refsetConcept  = refsetConcept;
      this.conceptCount   = members.size();

      if (this.conceptCount == 0) {
         return;
      }

      memberRefsetHelper =
         Terms.get().getSpecRefsetHelper(frameConfig).getMemberHelper(refsetConcept.getConceptNid(),
                                         ReferenceConcepts.NORMAL_MEMBER.getNid());
      memberRefsetHelper.setAutocommitActive(false);

      if (memberRefsetHelper.getMarkedParentRefsetForRefset(refsetConcept, frameConfig).isEmpty()) {
         int n = JOptionPane.showConfirmDialog(frameConfig.getTreeInSpecEditor(),
                    "Would you like to create an associated marked parent refset?",
                    "No associated marked parent refset", JOptionPane.YES_NO_OPTION);

         switch (n) {
         case JOptionPane.YES_OPTION :
            RefsetSpecEditor.addRefsetMetadata(true, refsetConcept);

            break;

         case JOptionPane.NO_OPTION :
            conceptCount = 0;

            return;
         }
      }

      this.activity = Terms.get().newActivityPanel(true, frameConfig,
              "Computing marked parents for: " + refsetConcept.toString(), true);
      activities.add(activity);
      activity.setIndeterminate(true);
      activity.setProgressInfoUpper("Computing marked parents for: " + refsetConcept.toString());
      activity.setProgressInfoLower("Setting up the computer...");
      stopListener = new StopActionListener();
      activity.addStopActionListener(stopListener);
      ActivityViewer.addActivity(activity);
      allRefsetMembers                 = members;
      currentRefsetMemberComponentNids = filterNonCurrentRefsetMembers((Collection<RefsetMember<?,
              ?>>) allRefsetMembers);
      markedParentRefsetConcept = (Concept) memberRefsetHelper.getMarkedParentRefsetForRefset(refsetConcept,
              frameConfig).iterator().next();
      activity.setProgressInfoLower("Setting up is-a cache...");

      activity.setProgressInfoLower("Starting computation...");
      activity.setValue(0);
      activity.setMaximum(conceptCount);
      activity.setIndeterminate(false);
      specHelper = new RefsetSpec(refsetConcept, true, frameConfig);
   }

   //~--- methods -------------------------------------------------------------

   public void addUncommitted() throws Exception {
      if (this.conceptCount == 0) {
         return;
      }

      int parentMemberTypeNid =
         Terms.get().getConcept(RefsetAuxiliary.Concept.MARKED_PARENT.getUids()).getConceptNid();

      if (!canceled) {
         long   endTime    = System.currentTimeMillis();
         long   elapsed    = endTime - startTime;
         String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);

         activity.setIndeterminate(true);
         activity.setProgressInfoLower("Adding marked parents. Elapsed: " + elapsedStr + ";  Members: "
                                       + members.get());

         I_RepresentIdSet allParents = Bdb.getConceptDb().getEmptyIdSet();

         for (RefexVersionBI<?> mpv : markedParentRefsetConcept.getRefsetMembersActive(viewCoordinate)) {
            RefexNidVersionBI<?> cnidMpv = (RefexNidVersionBI) mpv;
            if (!allParents.isMember(cnidMpv.getNid1())) {
                RefexCAB rcBp = cnidMpv.makeBlueprint(viewCoordinate);
                rcBp.setStatusUuid(SnomedMetadataRfx.getSTATUS_RETIRED().getUuids()[0]);
                builder.constructIfNotCurrent(rcBp);
            } else {
                // remove existing marked parent... Already set. 
                allParents.setNotMember(cnidMpv.getNid1());
            }
         }

         NidBitSetItrBI newParentItr = allParents.iterator();

         while (newParentItr.next()) {
            ChangeNotifier.touchRefexRC(newParentItr.nid());
            memberRefsetHelper.newRefsetExtension(markedParentRefsetConcept.getNid(), newParentItr.nid(),
                    parentMemberTypeNid);
         }
      }

      if (!canceled) {
         BdbCommitManager.addUncommittedNoChecks(markedParentRefsetConcept);
      }

      long   elapsed    = System.currentTimeMillis() - startTime;
      String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);

      if (!canceled) {
         activity.setProgressInfoLower("Complete. Time: " + elapsedStr);
      } else {
         activity.setProgressInfoLower("Cancelled.");

         for (I_ShowActivity a : activities) {
            if (!a.isComplete()) {
               a.cancel();
               a.setProgressInfoLower("Cancelled.");
            }
         }
      }

      activity.complete();
      activity.removeStopActionListener(this.stopListener);
   }

   private I_RepresentIdSet filterNonCurrentRefsetMembers(Collection<RefsetMember<?, ?>> allRefsetMembers)
           throws Exception {
      I_RepresentIdSet newList = Terms.get().getEmptyIdSet();

      for (RefsetMember<?, ?> e : allRefsetMembers) {
         if (e.getVersions(viewCoordinate).size() > 0) {
            newList.setMember(e.getComponentNid());
         }
      }

      return newList;
   }

   //~--- inner classes -------------------------------------------------------

   public class StopActionListener implements ActionListener {
      public StopActionListener() {}

      //~--- methods ----------------------------------------------------------

      @Override
      public void actionPerformed(ActionEvent e) {
         if (!canceled) {
            canceled = true;

            for (I_ShowActivity a : activities) {
               a.cancel();
               a.setProgressInfoLower("Cancelled.");
            }

            activity.removeStopActionListener(this);
         }
      }
   }
}
