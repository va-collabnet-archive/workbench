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



package org.ihtsdo.db.change;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.TermChangeListener;

//~--- JDK imports ------------------------------------------------------------

import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author kec
 */
public class ChangeNotifier {
   private static final Timer                                     timer                               =
      new Timer("LastChange", true);
   private static AtomicReference<ConcurrentSkipListSet<Integer>> referencedComponentsOfChangedRefexs =
      new AtomicReference<ConcurrentSkipListSet<Integer>>(new ConcurrentSkipListSet<Integer>());
   private static AtomicReference<ConcurrentSkipListSet<Integer>> originsOfChangedRels =
      new AtomicReference<ConcurrentSkipListSet<Integer>>(new ConcurrentSkipListSet<Integer>());
   private static AtomicReference<ConcurrentSkipListSet<Integer>> destinationsOfChangedRels =
      new AtomicReference<ConcurrentSkipListSet<Integer>>(new ConcurrentSkipListSet<Integer>());
   private static AtomicReference<ConcurrentSkipListSet<Integer>> changedComponents =
      new AtomicReference<ConcurrentSkipListSet<Integer>>(new ConcurrentSkipListSet<Integer>());
   private static AtomicReference<ConcurrentSkipListSet<Integer>> changedComponentAlerts =
      new AtomicReference<ConcurrentSkipListSet<Integer>>(new ConcurrentSkipListSet<Integer>());
   private static AtomicReference<ConcurrentSkipListSet<Integer>> changedComponentTemplates =
      new AtomicReference<ConcurrentSkipListSet<Integer>>(new ConcurrentSkipListSet<Integer>());
   private static ConcurrentSkipListSet<WeakReference<TermChangeListener>> changeListenerRefs =
      new ConcurrentSkipListSet<WeakReference<TermChangeListener>>();
   private static AtomicBoolean active = new AtomicBoolean(true);
   private static AtomicBoolean fromClassification = new AtomicBoolean(false);

   //~--- static initializers -------------------------------------------------

   static {
      timer.schedule(new Notifier(), 5000, 2000);
   }

   //~--- constant enums ------------------------------------------------------

   public enum Change { COMPONENT, REL_XREF, REFEX_XREF, REL_ORIGIN,
                        COMPONENT_ALERT, COMPONENT_TEMPLATE}

   //~--- methods -------------------------------------------------------------

   public static void addTermChangeListener(TermChangeListener cl) {
      changeListenerRefs.add(new ComparableWeakRef(cl));
   }

   public static void removeTermChangeListener(TermChangeListener cl) {
      changeListenerRefs.remove(new ComparableWeakRef(cl));
   }

   public static void resumeNotifications() {
      active.set(true);
   }

   public static void suspendNotifications() {
      active.set(false);
   }

   public static void touch(Concept c) {
      for (int nid : c.getUncommittedNids().getListArray()) {
         touch(nid, Change.COMPONENT);
      }

      touch(c.getNid(), Change.COMPONENT);
   }
   
   public static void touch(Concept c, boolean classifier) {
      for (int nid : c.getUncommittedNids().getListArray()) {
         touch(nid, Change.COMPONENT);
      }

      touch(c.getNid(), Change.COMPONENT);
      fromClassification.set(classifier);
   }

   public static void touch(int nid, Change changeType) {
      if (nid == Integer.MAX_VALUE) {
         return;
      }

      int cNid = Bdb.getConceptNid(nid);

      switch (changeType) {
      case COMPONENT :
         changedComponents.get().add(nid);

         if ((cNid != nid) && (cNid != Integer.MAX_VALUE)) {
            changedComponents.get().add(cNid);
         }

         break;
      case COMPONENT_ALERT :
         changedComponentAlerts.get().add(nid);

         break;
          
      case COMPONENT_TEMPLATE:
         changedComponentTemplates.get().add(nid);

         break;
          
      case REL_ORIGIN :
          originsOfChangedRels.get().add(nid);
          
          break;

      case REL_XREF :
         destinationsOfChangedRels.get().add(nid);

         break;

      case REFEX_XREF :
         referencedComponentsOfChangedRefexs.get().add(nid);

         if ((cNid != nid) && (cNid != Integer.MAX_VALUE)) {
            referencedComponentsOfChangedRefexs.get().add(cNid);
         }

         break;
      }
   }

   public static void touchComponent(int nid) {
      touch(nid, Change.COMPONENT);
   }
   
   public static void touchComponentAlert(int nid) {
      touch(nid, Change.COMPONENT_ALERT);
   }
   
   public static void touchComponentTemplate(int nid) {
      touch(nid, Change.COMPONENT_TEMPLATE);
   }

   public static void touchComponents(Collection<Integer> cNidSet) {
      for (Integer cNid : cNidSet) {
         touch(cNid, Change.COMPONENT);
      }
   }

   public static void touchRefexRC(int nid) {
      touch(nid, Change.REFEX_XREF);
   }

   public static void touchRelOrigin(int nid) {
      touch(nid, Change.REL_ORIGIN);
   }

   public static void touchRelTarget(int nid) {
      touch(nid, Change.REL_XREF);
   }

   //~--- inner classes -------------------------------------------------------

   public static class ComparableWeakRef extends WeakReference<TermChangeListener>
           implements Comparable<ComparableWeakRef> {
      int id;

      //~--- constructors -----------------------------------------------------

      public ComparableWeakRef(TermChangeListener cl) {
         super(cl);
         this.id = cl.getListenerId();
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(ComparableWeakRef o) {
         return this.id - o.id;
      }
   }


   private static class Notifier extends TimerTask {
      @Override
      public void run() {
         if (active.get()) {
            ConcurrentSkipListSet<Integer> destinationsOfChangedRels =
               ChangeNotifier.destinationsOfChangedRels.getAndSet(new ConcurrentSkipListSet<Integer>());
            ConcurrentSkipListSet<Integer> originsOfChangedRels =
               ChangeNotifier.originsOfChangedRels.getAndSet(new ConcurrentSkipListSet<Integer>());
            ConcurrentSkipListSet<Integer> referencedComponentsOfChangedRefexs =
               ChangeNotifier.referencedComponentsOfChangedRefexs.getAndSet(
                   new ConcurrentSkipListSet<Integer>());
            ConcurrentSkipListSet<Integer> changedComponents =
               ChangeNotifier.changedComponents.getAndSet(new ConcurrentSkipListSet<Integer>());
            ConcurrentSkipListSet<Integer> changedComponentAlerts =
               ChangeNotifier.changedComponentAlerts.getAndSet(new ConcurrentSkipListSet<Integer>());
            ConcurrentSkipListSet<Integer> changedComponentTemplates =
               ChangeNotifier.changedComponentTemplates.getAndSet(new ConcurrentSkipListSet<Integer>());
            long sequence = BdbCommitSequence.nextSequence();

            if (!destinationsOfChangedRels.isEmpty() ||!referencedComponentsOfChangedRefexs.isEmpty()
                    ||!changedComponents.isEmpty() ||!changedComponentAlerts.isEmpty() 
                    ||!changedComponentTemplates.isEmpty()) {
               List<WeakReference<TermChangeListener>> toRemove =
                  new ArrayList<WeakReference<TermChangeListener>>();

               for (WeakReference<TermChangeListener> clr : changeListenerRefs) {
                  TermChangeListener cl = clr.get();

                  if (cl == null) {
                     toRemove.add(clr);
                  } else {
                     try {
                        cl.changeNotify(sequence, originsOfChangedRels, destinationsOfChangedRels,
                                        referencedComponentsOfChangedRefexs, changedComponents,
                                        changedComponentAlerts, changedComponentTemplates, fromClassification.get());
                     } catch (Throwable e) {
                        e.printStackTrace();
                        toRemove.add(clr);
                     }
                  }
               }
               fromClassification.set(false);
               changeListenerRefs.removeAll(toRemove);
            }
         }
      }
   }
}
